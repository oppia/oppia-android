package org.oppia.android.data.persistence

import android.app.Application
import androidx.annotation.GuardedBy
import com.google.protobuf.MessageLite
import kotlinx.coroutines.Deferred
import org.oppia.android.app.model.ProfileId
import org.oppia.android.util.data.AsyncDataSubscriptionManager
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProvider
import org.oppia.android.util.data.InMemoryBlockingCache
import org.oppia.android.util.profile.DirectoryManagementUtil
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.concurrent.locks.ReentrantLock
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.concurrent.withLock

/**
 * An on-disk persistent cache for proto messages that ensures reads and writes happen in a
 * well-defined order.
 *
 * Note that if this cache is used like a [DataProvider], there is a race condition between the
 * initial store's data being retrieved and any early writes to the store (writes generally win). If
 * this is not ideal, callers should use [primeInMemoryAndDiskCacheAsync] to synchronously kick-off
 * a read update to the store that is guaranteed to complete before any writes. This will be
 * reflected in the first time the store's state is delivered to a subscriber to a LiveData version
 * of this data provider. Note that this priming will always complete before any updates if it's
 * called before updates/reads.
 *
 * Note that this is a fast-response data provider, meaning it will provide a [AsyncResult.Pending]
 * result to subscribers immediately until the actual store is retrieved from disk.
 */
class PersistentCacheStore<T : MessageLite> private constructor(
  application: Application,
  cacheFactory: InMemoryBlockingCache.Factory,
  private val asyncDataSubscriptionManager: AsyncDataSubscriptionManager,
  cacheName: String,
  private val initialValue: T,
  directory: File = application.filesDir
) : DataProvider<T>(application) {
  private val cacheFileName = "$cacheName.cache"
  private val providerId = PersistentCacheStoreId(cacheFileName)
  private val failureLock = ReentrantLock()

  private val cacheFile = File(directory, cacheFileName)
  @GuardedBy("failureLock")
  private var deferredLoadCacheFailure: Throwable? = null
  private val cache =
    cacheFactory.create(CachePayload(state = CacheState.UNLOADED, value = initialValue))

  init {
    cache.observeChanges { oldValue, newValue ->
      // Only notice subscribers if the in-memory version of the cache actually changed (not just
      // its load state). This extra check ensures that priming the cache does not unnecessarily
      // trigger a notification which would result in an unnecessary retrieveData() call. The
      // exception is that changing from an UNLOADED state always results in a notification (since
      // UNLOADED is treated as 'pending' by default).
      val wasPending = oldValue?.state == CacheState.UNLOADED
      val nowPending = newValue?.state == CacheState.UNLOADED
      if ((wasPending && !nowPending) || oldValue?.value != newValue?.value) {
        asyncDataSubscriptionManager.notifyChange(providerId)
      }
    }
  }

  override fun getId(): Any = providerId

  override suspend fun retrieveData(): AsyncResult<T> {
    cache.readIfPresentAsync().await().let { cachePayload ->
      // First, determine whether the current cache has been attempted to be retrieved from disk.
      if (cachePayload.state == CacheState.UNLOADED) {
        deferLoadFile()
        return AsyncResult.Pending()
      }

      // Second, check if a previous deferred read failed. The store stays in a failed state until
      // the next storeData() call to avoid hitting the same failure again. Eventually, the class
      // could be updated with some sort of retry or recovery mechanism if failures show up in real
      // use cases.
      failureLock.withLock {
        deferredLoadCacheFailure?.let {
          // A previous read failed.
          return AsyncResult.Failure(it)
        }
      }

      // Finally, check if there's an in-memory cached value that can be loaded now.
      // Otherwise, there should be a guaranteed in-memory value to use, instead.
      return AsyncResult.Success(cachePayload.value)
    }
  }

  /**
   * Primes the current cache such that certain guarantees can be assured for both the in-memory and
   * on-disk version of this cache, depending on which policies are selected.
   *
   * Note that the value of the returned [Deferred] is not useful. The state of the cache should
   * monitored by treating this provider as a [DataProvider]. This method may result in an update
   * notification to observers of this [DataProvider].
   *
   * Note also that this method is particularly useful in two specific cases:
   * 1. When an instance of this cache needs to be loaded from disk before an update operation
   *   occurs (otherwise update() will likely complete before a load, overwriting the current
   *   on-disk cache state).
   * 2. When the cache needs to be initialized exactly once to a specific value (such as the case
   *   when an ID that cannot change after initialization needs to be generated and stored exactly
   *   once).
   *
   * Each of the above states are possible using different combinations of the provided [UpdateMode]
   * and [PublishMode] parameters.
   *
   * Finally, this method succeeding more or less guarantees that the cache store is now in a good
   * state (i.e. it will even recover from a corrupted or invalid disk cache file).
   *
   * @param updateMode how the cache should be changed (depending on whether it's been loaded yet,
   *     and whether it has an on-disk cache)
   * @param publishMode whether changes to the cache's in-memory copy during priming should be kept
   *     in-memory and sent to observers (otherwise, only store the results on-disk and do not
   *     notify changes). Note that the in-memory cache *will* be updated if it hasn't yet been
   *     initialized (which may mean saving a result from [update]).
   * @param update an optional function to transform the cache's current (in-memory if loaded, or
   *     from-disk if not) state to a new state, and then update the on-disk cache (and potentially
   *     the in-memory cache based on [publishMode]). Note that if the cache has not been loaded yet
   *     and has no on-disk copy then the cache's [initialValue] will be passed, instead. Omitting
   *     this transformation will just ensure the in-memory and/or on-disk cache are appropriately
   *     initialized.
   * @return a [Deferred] tracking the success/failure of priming this cache store
   */
  fun primeInMemoryAndDiskCacheAsync(
    updateMode: UpdateMode,
    publishMode: PublishMode,
    update: (T) -> T = { it }
  ): Deferred<Any> {
    return cache.updateIfPresentAsync { cachePayload ->
      // It's expected 'oldState' to match 'cachePayload' unless the cache hasn't yet been read
      // (since then 'cachePayload' will be based on the store's default value).
      val (oldState, newState) = when (cachePayload.state) {
        CacheState.UNLOADED -> {
          val loadedPayload = loadFileCache(cachePayload)
          when (loadedPayload.state) {
            // The state should never stay as UNLOADED.
            CacheState.UNLOADED ->
              error("Something went wrong loading the cache during priming: $cacheFile")
            CacheState.IN_MEMORY_ONLY -> {
              // Needs saving. In this case, there is no "old" value since the cache was never
              // initialized.
              val storedPayload = storeFileCache(loadedPayload, update)
              storedPayload to storedPayload
            }
            CacheState.IN_MEMORY_AND_ON_DISK -> // Loaded from disk successfully.
              loadedPayload to loadedPayload.maybeReprimePayload(updateMode, update)
          }
        }
        // Generally indicates that the cache was loaded but never written.
        CacheState.IN_MEMORY_ONLY -> cachePayload to storeFileCache(cachePayload, update)
        CacheState.IN_MEMORY_AND_ON_DISK ->
          cachePayload to cachePayload.maybeReprimePayload(updateMode, update)
      }

      // The returned payload is always expected to be IN_MEMORY_AND_ON_DISK, but the in-memory copy
      // may be intentionally kept out-of-date so that cache reads pick up the original version
      // rather than the new one stored on-disk. Furthermore, this method guarantees by this point
      // that the cache is in a good, non-error state (so the error is cleared in case one occurred
      // during early priming).
      failureLock.withLock { deferredLoadCacheFailure = null }
      return@updateIfPresentAsync when (publishMode) {
        PublishMode.PUBLISH_TO_IN_MEMORY_CACHE -> newState
        PublishMode.DO_NOT_PUBLISH_TO_IN_MEMORY_CACHE -> newState.copy(value = oldState.value)
      }
    }
  }

  /**
   * Callers should use this read function if they they don't care or specifically do not want to
   * observe changes to the underlying store. If the file is not in memory, it will loaded from disk
   * and observers will be notified.
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
   * Calls the specified value with the current on-disk contents and saves the result of the
   * function to disk. Note that the function used here should be non-blocking, thread-safe, and
   * should have no side effects.
   *
   * @param updateInMemoryCache indicates whether this change to the on-disk store should also
   *     update the in-memory store, and propagate that change to all subscribers to this data
   *     provider. This may be ideal if callers want to control "snapshots" of the store that
   *     subscribers have access to, however it's recommended to keep all store calls consistent in
   *     whether they update the in-memory cache to avoid complex potential in-memory/on-disk sync
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
    update: suspend (T) -> Pair<T, V>
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
   * Returns a [Deferred] indicating when the cache was cleared and its on-disk file, removed. This
   * does notify subscribers.
   */
  fun clearCacheAsync(): Deferred<Any> {
    return cache.updateIfPresentAsync { currentPayload ->
      if (cacheFile.exists()) {
        cacheFile.delete()
      }
      failureLock.withLock {
        deferredLoadCacheFailure = null
      }
      // Always clear the in-memory cache and reset it to the initial value (the cache itself should
      // never be fully deleted since the rest of the store assumes a value is always present in
      // it).
      currentPayload.copy(state = CacheState.UNLOADED, value = initialValue)
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
   * Loads the file store from disk, and returns the most up-to-date cache payload. This should only
   * be called from the cache's update thread.
   */
  @Suppress("UNCHECKED_CAST") // Cast is ensured since root proto is initialValue with type T.
  private fun loadFileCache(currentPayload: CachePayload<T>): CachePayload<T> {
    if (!cacheFile.exists()) {
      // The store is not yet persisted on disk.
      return currentPayload.copy(state = CacheState.IN_MEMORY_ONLY)
    }

    // It's possible for multiple load requests to happen simultaneously, but finish out of order.
    // Attempting to load the cache a second time can result in a number of problems:
    // - Losing in-memory cache state.
    // - Duplicating loaded on-disk state (since "mergeFrom()" is used for loading).
    if (currentPayload.state != CacheState.UNLOADED) return currentPayload

    val cacheBuilder = currentPayload.value.toBuilder()
    return try {
      currentPayload.copy(
        state = CacheState.IN_MEMORY_AND_ON_DISK,
        value = FileInputStream(cacheFile).use { cacheBuilder.mergeFrom(it) }.build() as T
      )
    } catch (e: IOException) {
      failureLock.withLock {
        deferredLoadCacheFailure = e
      }
      // Update the cache to have an in-memory copy of the current payload since on-disk retrieval
      // failed.
      currentPayload.copy(state = CacheState.IN_MEMORY_ONLY, value = currentPayload.value)
    }
  }

  /**
   * Stores the file store to disk, and returns the persisted payload. This should only be called
   * from the cache's update thread.
   */
  private fun storeFileCache(currentPayload: CachePayload<T>, update: (T) -> T): CachePayload<T> {
    val updatedCacheValue = update(currentPayload.value)
    FileOutputStream(cacheFile).use { updatedCacheValue.writeTo(it) }
    return currentPayload.copy(state = CacheState.IN_MEMORY_AND_ON_DISK, value = updatedCacheValue)
  }

  /** See [storeFileCache]. Returns payload and custom result. */
  private suspend fun <V> storeFileCacheWithCustomChannel(
    currentPayload: CachePayload<T>,
    update: suspend (T) -> Pair<T, V>
  ): Pair<CachePayload<T>, V> {
    val (updatedCacheValue, customResult) = update(currentPayload.value)
    // TODO(#4264): Move this over to using an I/O-specific dispatcher.
    FileOutputStream(cacheFile).use { updatedCacheValue.writeTo(it) }
    return Pair(
      currentPayload.copy(state = CacheState.IN_MEMORY_AND_ON_DISK, value = updatedCacheValue),
      customResult
    )
  }

  private fun CachePayload<T>.maybeReprimePayload(
    updateMode: UpdateMode,
    initialize: (T) -> T
  ): CachePayload<T> {
    return when (updateMode) {
      UpdateMode.UPDATE_IF_NEW_CACHE -> this // Nothing extra to do.
      UpdateMode.UPDATE_ALWAYS -> storeFileCache(this, initialize) // Recompute the payload.
    }
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

  private data class CachePayload<T>(val state: CacheState, val value: T)

  /**
   * The mode of on-disk data updating that can be configured for specific operations like cache
   * priming.
   *
   * This mode only configures on-disk data changes, not in-memory (see [PublishMode] for that).
   */
  enum class UpdateMode {
    /** Indicates that the on-disk cache should only be changed if it doesn't already exist. */
    UPDATE_IF_NEW_CACHE,

    /**
     * Indicates that the on-disk cache should always be changed regardless of if it already exists.
     */
    UPDATE_ALWAYS
  }

  /**
   * The mode of in-memory data updating that can be configured for specific operations like cache
   * priming.
   *
   * This mode only configures in-memory data changes, not on-disk (see [UpdateMode] for that).
   */
  enum class PublishMode {
    /**
     * Indicates that data changes should update the in-memory cache and be broadcast to
     * subscribers.
     */
    PUBLISH_TO_IN_MEMORY_CACHE,

    /** Indicates that data changes should not change the in-memory cache. */
    DO_NOT_PUBLISH_TO_IN_MEMORY_CACHE
  }

  /**
   * An injectable factory for [PersistentCacheStore]s. The stores themselves should be retrievable
   * from central controllers since they can't be placed directly in the Dagger graph.
   */
  @Singleton
  class Factory @Inject constructor(
    private val application: Application,
    private val cacheFactory: InMemoryBlockingCache.Factory,
    private val asyncDataSubscriptionManager: AsyncDataSubscriptionManager,
    private val directoryManagementUtil: DirectoryManagementUtil
  ) {
    /**
     * Returns a new [PersistentCacheStore] with the specified cache name and initial value under
     * the shared directory context.filesDir.
     *
     * Use this method when data is shared by all profiles.
     */
    fun <T : MessageLite> create(cacheName: String, initialValue: T): PersistentCacheStore<T> {
      return PersistentCacheStore(
        application,
        cacheFactory,
        asyncDataSubscriptionManager,
        cacheName,
        initialValue
      )
    }

    /**
     * Returns a new [PersistentCacheStore] with the specified cache name and initial value under
     * the directory specified by profileId. Use this method when data is unique to each profile.
     */
    fun <T : MessageLite> createPerProfile(
      cacheName: String,
      initialValue: T,
      profileId: ProfileId
    ): PersistentCacheStore<T> {
      val profileDirectory = directoryManagementUtil.getOrCreateDir(profileId.loggedInInternalProfileId.toString())
      return PersistentCacheStore(
        application,
        cacheFactory,
        asyncDataSubscriptionManager,
        cacheName,
        initialValue,
        profileDirectory
      )
    }
  }
}
