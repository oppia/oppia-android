package org.oppia.data.persistence

import android.content.Context
import androidx.annotation.GuardedBy
import com.google.protobuf.MessageLite
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.concurrent.locks.ReentrantLock
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.concurrent.withLock
import kotlinx.coroutines.Deferred
import org.oppia.app.model.ProfileId
import org.oppia.util.data.AsyncDataSubscriptionManager
import org.oppia.util.data.AsyncResult
import org.oppia.util.data.DataProvider
import org.oppia.util.data.InMemoryBlockingCache
import org.oppia.util.profile.DirectoryManagementUtil

/**
 * An on-disk persistent cache for proto messages that ensures reads and writes happen in a well-defined order. Note
 * that if this cache is used like a [DataProvider], there is a race condition between the initial store's data being
 * retrieved and any early writes to the store (writes generally win). If this is not ideal, callers should use
 * [primeCacheAsync] to synchronously kick-off a read update to the store that is guaranteed to complete before any writes.
 * This will be reflected in the first time the store's state is delivered to a subscriber to a LiveData version of this
 * data provider.
 *
 * Note that this is a fast-response data provider, meaning it will provide a pending [AsyncResult] to subscribers
 * immediately until the actual store is retrieved from disk.
 */
class PersistentCacheStore<T : MessageLite> private constructor(
  context: Context,
  cacheFactory: InMemoryBlockingCache.Factory,
  private val asyncDataSubscriptionManager: AsyncDataSubscriptionManager,
  cacheName: String,
  private val initialValue: T,
  directory: File = context.filesDir
) : DataProvider<T> {
  private val cacheFileName = "$cacheName.cache"
  private val providerId = PersistentCacheStoreId(cacheFileName)
  private val failureLock = ReentrantLock()

  private val cacheFile = File(directory, cacheFileName)
  @GuardedBy("failureLock")
  private var deferredLoadCacheFailure: Throwable? = null
  private val cache =
    cacheFactory.create(CachePayload(state = CacheState.UNLOADED, value = initialValue))

  init {
    cache.observeChanges {
      asyncDataSubscriptionManager.notifyChange(providerId)
    }
  }

  override fun getId(): Any {
    return providerId
  }

  override suspend fun retrieveData(): AsyncResult<T> {
    cache.readIfPresentAsync().await().let { cachePayload ->
      // First, determine whether the current cache has been attempted to be retrieved from disk.
      if (cachePayload.state == CacheState.UNLOADED) {
        deferLoadFile()
        return AsyncResult.pending()
      }

      // Second, check if a previous deferred read failed. The store stays in a failed state until the next storeData()
      // call to avoid hitting the same failure again. Eventually, the class could be updated with some sort of retry or
      // recovery mechanism if failures show up in real use cases.
      failureLock.withLock {
        deferredLoadCacheFailure?.let {
          // A previous read failed.
          return AsyncResult.failed(it)
        }
      }

      // Finally, check if there's an in-memory cached value that can be loaded now.
      // Otherwise, there should be a guaranteed in-memory value to use, instead.
      return AsyncResult.success(cachePayload.value)
    }
  }

  /**
   * Kicks off a read operation to update the in-memory cache. This operation blocks against calls to [storeDataAsync]
   * and deferred calls to [retrieveData].
   *
   * @param forceUpdate indicates whether to force a reset of the in-memory cache. Note that this only forces a load; if
   *     the load fails then the store will remain in its same state. If this value is false (the default), it will only
   *     perform file I/O if the cache is not already loaded into memory.
   * @returns a [Deferred] that completes upon the completion of priming the cache, or failure to do so with the failed
   *     exception. Note that the failure reason will not be propagated to a LiveData-converted version of this data
   *     provider, so it must be handled at the callsite for this method.
   */
  fun primeCacheAsync(forceUpdate: Boolean = false): Deferred<Any> {
    return cache.updateIfPresentAsync { cachePayload ->
      if (forceUpdate || cachePayload.state == CacheState.UNLOADED) {
        // Store the retrieved on-disk cache, if it's present (otherwise set up state such that retrieveData() does not
        // attempt to load the file from disk again since the attempt was made here).
        loadFileCache(cachePayload)
      } else {
        // Otherwise, keep the cache the same.
        cachePayload
      }
    }
  }

  /**
   * Callers should use this read function if they they don't care or specifically do not
   * want to observe changes to the underlying store. If the file is not in memory, it will
   * loaded from disk and observers will be notified.
   *
   * @return a deferred value that contains the value of the cached payload.
   */
  fun readDataAsync(): Deferred<T> {
    val deferred = cache.updateWithCustomChannelIfPresentAsync { cachePayload ->
      if (cachePayload.state == CacheState.UNLOADED) {
        val filePayload = loadFileCache(cachePayload)
        Pair(filePayload, filePayload.value)
      } else {
        Pair(cachePayload, cachePayload.value)
      }
    }
    deferred.invokeOnCompletion {
      failureLock.withLock {
        deferredLoadCacheFailure = it ?: deferredLoadCacheFailure
      }
    }
    return deferred
  }

  /**
   * Calls the specified value with the current on-disk contents and saves the result of the function to disk. Note that
   * the function used here should be non-blocking, thread-safe, and should have no side effects.
   *
   * @param updateInMemoryCache indicates whether this change to the on-disk store should also update the in-memory
   *     store, and propagate that change to all subscribers to this data provider. This may be ideal if callers want to
   *     control "snapshots" of the store that subscribers have access to, however it's recommended to keep all store
   *     calls consistent in whether they update the in-memory cache to avoid complex potential in-memory/on-disk sync
   *     issues.
   */
  fun storeDataAsync(updateInMemoryCache: Boolean = true, update: (T) -> T): Deferred<Any> {
    return cache.updateIfPresentAsync { cachedPayload ->
      val updatedPayload = storeFileCache(cachedPayload, update)
      if (updateInMemoryCache) updatedPayload else cachedPayload
    }
  }

  /** See [storeDataAsync]. Stores data and allows for a custom deferred result. */
  fun <V> storeDataWithCustomChannelAsync(
    updateInMemoryCache: Boolean = true,
    update: (T) -> Pair<T, V>
  ): Deferred<V> {
    return cache.updateWithCustomChannelIfPresentAsync { cachedPayload ->
      val (updatedPayload, customResult) = storeFileCacheWithCustomChannel(cachedPayload, update)
      if (updateInMemoryCache) Pair(updatedPayload, customResult) else Pair(
        cachedPayload,
        customResult
      )
    }
  }

  /**
   * Returns a [Deferred] indicating when the cache was cleared and its on-disk file, removed. This does notify
   * subscribers.
   */
  fun clearCacheAsync(): Deferred<Any> {
    return cache.updateIfPresentAsync {
      if (cacheFile.exists()) {
        cacheFile.delete()
      }
      failureLock.withLock {
        deferredLoadCacheFailure = null
      }
      // Always clear the in-memory cache and reset it to the initial value (the cache itself should never be fully
      // deleted since the rest of the store assumes a value is always present in it).
      CachePayload(state = CacheState.UNLOADED, value = initialValue)
    }
  }

  private fun deferLoadFile() {
    cache.updateIfPresentAsync { cachePayload ->
      loadFileCache(cachePayload)
    }.invokeOnCompletion {
      failureLock.withLock {
        // Other failures should be captured for reporting.
        deferredLoadCacheFailure = it ?: deferredLoadCacheFailure
      }
    }
  }

  /**
   * Loads the file store from disk, and returns the most up-to-date cache payload. This should only be called from the
   * cache's update thread.
   */
  @Suppress("UNCHECKED_CAST") // Cast is ensured since root proto is initialValue with type T.
  private fun loadFileCache(currentPayload: CachePayload<T>): CachePayload<T> {
    if (!cacheFile.exists()) {
      // The store is not yet persisted on disk.
      return currentPayload.moveToState(CacheState.IN_MEMORY_ONLY)
    }

    val cacheBuilder = currentPayload.value.toBuilder()
    return try {
      CachePayload(
        state = CacheState.IN_MEMORY_AND_ON_DISK,
        value = FileInputStream(cacheFile).use { cacheBuilder.mergeFrom(it) }.build() as T
      )
    } catch (e: IOException) {
      failureLock.withLock {
        deferredLoadCacheFailure = e
      }
      // Update the cache to have an in-memory copy of the current payload since on-disk retrieval failed.
      CachePayload(
        state = CacheState.IN_MEMORY_ONLY,
        value = currentPayload.value
      )
    }
  }

  /**
   * Stores the file store to disk, and returns the persisted payload. This should only be called from the cache's
   * update thread.
   */
  private fun storeFileCache(currentPayload: CachePayload<T>, update: (T) -> T): CachePayload<T> {
    val updatedCacheValue = update(currentPayload.value)
    FileOutputStream(cacheFile).use { updatedCacheValue.writeTo(it) }
    return CachePayload(state = CacheState.IN_MEMORY_AND_ON_DISK, value = updatedCacheValue)
  }

  /** See [storeFileCache]. Returns payload and custom result. */
  private fun <V> storeFileCacheWithCustomChannel(
    currentPayload: CachePayload<T>,
    update: (T) -> Pair<T, V>
  ): Pair<CachePayload<T>, V> {
    val (updatedCacheValue, customResult) = update(currentPayload.value)
    FileOutputStream(cacheFile).use { updatedCacheValue.writeTo(it) }
    return Pair(
      CachePayload(state = CacheState.IN_MEMORY_AND_ON_DISK, value = updatedCacheValue),
      customResult
    )
  }

  private data class PersistentCacheStoreId(private val id: String)

  /** Represents different states the cache store can be in. */
  private enum class CacheState {
    /** Indicates that the cache has not yet been attempted to be retrieved from disk. */
    UNLOADED,

    /** Indicates that the cache exists only in memory and not on disk. */
    IN_MEMORY_ONLY,

    /** Indicates that the cache exists both in memory and on disk. */
    IN_MEMORY_AND_ON_DISK
  }

  private data class CachePayload<T>(val state: CacheState, val value: T) {
    /** Returns a copy of this payload with the new, specified [CacheState]. */
    fun moveToState(newState: CacheState): CachePayload<T> {
      return CachePayload(state = newState, value = value)
    }
  }

  // TODO(#59): Use @ApplicationContext instead of Context once package dependencies allow for cross-module circular
  // dependencies. Currently, the data module cannot depend on the app module.
  /**
   * An injectable factory for [PersistentCacheStore]s. The stores themselves should be retrievable from central
   * controllers since they can't be placed directly in the Dagger graph.
   */
  @Singleton
  class Factory @Inject constructor(
    private val context: Context,
    private val cacheFactory: InMemoryBlockingCache.Factory,
    private val asyncDataSubscriptionManager: AsyncDataSubscriptionManager,
    private val directoryManagementUtil: DirectoryManagementUtil
  ) {
    /**
     * Returns a new [PersistentCacheStore] with the specified cache name and initial value under the shared directory context.filesDir.
     *
     * Use this method when data is shared by all profiles.
     */
    fun <T : MessageLite> create(cacheName: String, initialValue: T): PersistentCacheStore<T> {
      return PersistentCacheStore(
        context,
        cacheFactory,
        asyncDataSubscriptionManager,
        cacheName,
        initialValue
      )
    }

    /**
     * Returns a new [PersistentCacheStore] with the specified cache name and initial value under the directory specified by profileId.
     * Use this method when data is unique to each profile.
     */
    fun <T : MessageLite> createPerProfile(
      cacheName: String,
      initialValue: T,
      profileId: ProfileId
    ): PersistentCacheStore<T> {
      val profileDirectory = directoryManagementUtil.getOrCreateDir(profileId.internalId.toString())
      return PersistentCacheStore(
        context,
        cacheFactory,
        asyncDataSubscriptionManager,
        cacheName,
        initialValue,
        profileDirectory
      )
    }
  }
}
