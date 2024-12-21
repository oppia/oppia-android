package org.oppia.android.util.data

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.withContext
import org.oppia.android.util.threading.BlockingDispatcher
import javax.inject.Inject
import javax.inject.Singleton

/**
 * An in-memory cache that provides blocking CRUD operations such that each operation is guaranteed to operate exactly
 * after any prior started operations began, and before any future operations. This class is thread-safe. Note that it's
 * safe to execute long-running operations in lambdas passed into the methods of this class.
 *
 * This cache is primarily intended to be used with immutable payloads, but mutable payloads can be used if calling code
 * takes caution to restrict all read/write access to those mutable values to operations invoked by this class.
 */
class InMemoryBlockingCache<T : Any> private constructor(
  blockingDispatcher: CoroutineDispatcher,
  initialValue: T?
) {
  private val scope = CoroutineScope(blockingDispatcher + SupervisorJob())

  /**
   * The value of the cache. Note that this does not require a lock since it's only ever accessed via the blocking
   * dispatcher's single thread.
   */
  private var value: T? = initialValue
  private var changeObserver: suspend (T?, T?) -> Unit = { _, _ -> }

  private sealed class CacheOp<out T : Any, R> {
    abstract val response: CompletableDeferred<R>
    abstract suspend fun execute(cache: InMemoryBlockingCache<@UnsafeVariance T>)
  }

  private class Create<T : Any>(
    val newValue: T,
    override val response: CompletableDeferred<T>
  ) : CacheOp<T, T>() {
    override suspend fun execute(cache: InMemoryBlockingCache<T>) {
      response.complete(cache.setCache(newValue))
    }
  }

  private class CreateIfAbsent<T : Any>(
    val generate: suspend () -> T,
    override val response: CompletableDeferred<T>
  ) : CacheOp<T, T>() {
    override suspend fun execute(cache: InMemoryBlockingCache<T>) {
      response.complete(cache.setCache(cache.value ?: generate()))
    }
  }

  private class Read<T : Any>(
    override val response: CompletableDeferred<T?>
  ) : CacheOp<T, T?>() {
    override suspend fun execute(cache: InMemoryBlockingCache<T>) {
      response.complete(cache.value)
    }
  }

  private class Update<T : Any>(
    val update: suspend (T?) -> T,
    override val response: CompletableDeferred<T>
  ) : CacheOp<T, T>() {
    override suspend fun execute(cache: InMemoryBlockingCache<T>) {
      response.complete(cache.setCache(update(cache.value)))
    }
  }

  private class UpdateIfPresent<T : Any>(
    val update: suspend (T) -> T,
    override val response: CompletableDeferred<T>
  ) : CacheOp<T, T>() {
    override suspend fun execute(cache: InMemoryBlockingCache<T>) {
      val currentValue = checkNotNull(cache.value) {
        "Expected to update the cache only after it's been created"
      }
      response.complete(cache.setCache(update(currentValue)))
    }
  }

  private class UpdateWithCustomChannel<T : Any, V>(
    val update: suspend (T) -> Pair<T, V>,
    override val response: CompletableDeferred<V>
  ) : CacheOp<T, V>() {
    override suspend fun execute(cache: InMemoryBlockingCache<T>) {
      val currentValue = checkNotNull(cache.value) {
        "Expected to update the cache only after it's been created"
      }
      val (newValue, result) = update(currentValue)
      cache.setCache(newValue)
      response.complete(result)
    }
  }

  private class Delete<T : Any>(
    override val response: CompletableDeferred<Unit>
  ) : CacheOp<T, Unit>() {
    override suspend fun execute(cache: InMemoryBlockingCache<T>) {
      cache.clearCache()
      response.complete(Unit)
    }
  }

  private class MaybeDelete<T : Any>(
    val shouldDelete: suspend (T) -> Boolean,
    override val response: CompletableDeferred<Boolean>
  ) : CacheOp<T, Boolean>() {
    override suspend fun execute(cache: InMemoryBlockingCache<T>) {
      val valueSnapshot = cache.value
      if (valueSnapshot != null && shouldDelete(valueSnapshot)) {
        cache.clearCache()
        response.complete(true)
      } else {
        response.complete(false)
      }
    }
  }

  private class MaybeForceDelete<T : Any>(
    val shouldDelete: suspend (T?) -> Boolean,
    override val response: CompletableDeferred<Boolean>
  ) : CacheOp<T, Boolean>() {
    override suspend fun execute(cache: InMemoryBlockingCache<T>) {
      if (shouldDelete(cache.value)) {
        cache.clearCache()
        response.complete(true)
      } else {
        response.complete(false)
      }
    }
  }

  @OptIn(ObsoleteCoroutinesApi::class)
  private val actor = scope.actor<CacheOp<T, *>>(
    capacity = Channel.UNLIMITED
  ) {
    for (msg in channel) {
      try {
        msg.execute(this@InMemoryBlockingCache)
      } catch (e: Exception) {
        msg.response.completeExceptionally(e)
      }
    }
  }

  /** Registers an observer that is called synchronously whenever this cache's contents are changed. */
  fun observeChanges(changeObserver: suspend (T?, T?) -> Unit) {
    this.changeObserver = changeObserver
  }

  /**
   * Returns a [Deferred] that, upon completion, guarantees that the cache has been recreated and initialized to the
   * specified value. The [Deferred] will be passed the most up-to-date state of the cache.
   */
  fun createAsync(newValue: T): Deferred<T> = scope.async {
    CompletableDeferred<T>().also { deferred ->
      actor.send(Create(newValue, deferred))
    }.await()
  }

  /**
   * Returns a [Deferred] that provides the most-up-to-date value of the cache, after either retrieving the current
   * state (if defined), or calling the provided generator to create a new state and initialize the cache to that state.
   * The provided function must be thread-safe and should have no side effects.
   */
  fun createIfAbsentAsync(generate: suspend () -> T): Deferred<T> = scope.async {
    CompletableDeferred<T>().also { deferred ->
      actor.send(CreateIfAbsent(generate, deferred))
    }.await()
  }

  /**
   * Returns a [Deferred] that will provide the most-up-to-date value stored in the cache, or null if it's not yet
   * initialized.
   */
  fun readAsync(): Deferred<T?> = scope.async {
    CompletableDeferred<T?>().also { deferred ->
      actor.send(Read(deferred))
    }.await()
  }

  /**
   * Returns a [Deferred] similar to [readAsync], except this assumes the cache to have been created already otherwise
   * an exception will be thrown.
   */
  fun readIfPresentAsync(): Deferred<T> = scope.async {
    val deferred = CompletableDeferred<T?>()
    actor.send(Read(deferred))
    checkNotNull(deferred.await()) { "Expected to read the cache only after it's been created" }
  }

  /**
   * Returns a [Deferred] that provides the most-up-to-date value of the cache, after atomically updating it based on
   * the specified update function. Note that the update function provided here must be thread-safe and should have no
   * side effects. This function is safe to call regardless of whether the cache has been created, meaning it can be
   * used also to initialize the cache.
   */
  fun updateAsync(update: suspend (T?) -> T): Deferred<T> = scope.async {
    CompletableDeferred<T>().also { deferred ->
      actor.send(Update(update, deferred))
    }.await()
  }

  /**
   * Returns a [Deferred] in the same way as [updateAsync], excepted this update is expected to occur after cache
   * creation otherwise an exception will be thrown.
   */
  fun updateIfPresentAsync(update: suspend (T) -> T): Deferred<T> = scope.async {
    CompletableDeferred<T>().also { deferred ->
      actor.send(UpdateIfPresent(update, deferred))
    }.await()
  }

  /** See [updateIfPresentAsync]. Returns a custom deferred result. */
  fun <V> updateWithCustomChannelIfPresentAsync(
    update: suspend (T) -> Pair<T, V>
  ): Deferred<V> = scope.async {
    CompletableDeferred<V>().also { deferred ->
      actor.send(UpdateWithCustomChannel(update, deferred))
    }.await()
  }

  /**
   * Returns a [Deferred] that executes when this cache has been fully cleared, or if it's already been cleared.
   */
  fun deleteAsync(): Deferred<Unit> = scope.async {
    CompletableDeferred<Unit>().also { deferred ->
      actor.send(Delete(deferred))
    }.await()
  }

  /**
   * Returns a [Deferred] that executes when checking the specified function on whether this cache should be deleted,
   * and returns whether it was deleted.
   *
   * Note that the provided function will not be called if the cache is already cleared.
   */
  fun maybeDeleteAsync(shouldDelete: suspend (T) -> Boolean): Deferred<Boolean> = scope.async {
    CompletableDeferred<Boolean>().also { deferred ->
      actor.send(MaybeDelete(shouldDelete, deferred))
    }.await()
  }

  /**
   * Returns a [Deferred] in the same way as [maybeDeleteAsync], except the deletion function provided is guaranteed to
   * be called regardless of the state of the cache, and whose return value will be returned in this method's
   * [Deferred].
   */
  fun maybeForceDeleteAsync(shouldDelete: suspend (T?) -> Boolean): Deferred<Boolean>
  = scope.async {
    CompletableDeferred<Boolean>().also { deferred ->
      actor.send(MaybeForceDelete(shouldDelete, deferred))
    }.await()
  }
  private suspend fun notifyChange(oldValue: T?, newValue: T?) {
    withContext(Dispatchers.Main) {
      changeObserver(oldValue, newValue)
    }
  }
  private suspend fun setCache(newValue: T): T {
    val oldValue = value
    value = newValue
    notifyChange(oldValue, newValue)
    return newValue
  }

  private suspend fun clearCache() {
    val oldValue = value
    value = null
    changeObserver(oldValue, null)
  }

  /** An injectable factory for [InMemoryBlockingCache]es. */
  @Singleton
  class Factory @Inject constructor(
    @BlockingDispatcher private val blockingDispatcher: CoroutineDispatcher
  ) {
    fun <T : Any> create(initialValue: T? = null): InMemoryBlockingCache<T> {
      return InMemoryBlockingCache(blockingDispatcher, initialValue)
    }
  }
}
