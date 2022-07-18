package org.oppia.android.util.data

import com.google.common.util.concurrent.SettableFuture
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.guava.asDeferred
import org.oppia.android.util.threading.BackgroundDispatcher

/**
 * An in-memory cache that provides blocking CRUD operations such that each operation is guaranteed
 * to operate exactly after any prior started operations began, and before any future operations.
 * This class is thread-safe. Note that it's safe to execute long-running operations in lambdas
 * passed into the methods of this class.
 *
 * This cache is primarily intended to be used with immutable payloads, but mutable payloads can be
 * used if calling code takes caution to restrict all read/write access to those mutable values to
 * operations invoked by this class.
 */
class InMemoryBlockingCache<T : Any> private constructor(
  private val backgroundDispatcher: CoroutineDispatcher,
  initialValue: T?
) {
  private val cacheCommandQueue by lazy { createCacheCommandQueue(initialValue) }

  /**
   * Registers an observer that is called synchronously whenever this cache's contents are changed,
   * replacing the previously registered observer (if any).
   *
   * Note that the observer is not actually registered until the [Deferred] completes.
   *
   * **IMPORTANT**: Note also that synchronous calls to this cache from registered observers can
   * cause the internal messaging system of this cache stop working. It's recommended to hop
   * execution contexts if calls back to this cache are possible.
   */
  fun observeChangesAsync(changeObserver: suspend () -> Unit): Deferred<Unit> =
    dispatchCommandAsync(CacheCommand.RegisterObserver(changeObserver))

  /**
   * Returns a [Deferred] that, upon completion, guarantees that the cache has been recreated and
   * initialized to the specified value. The [Deferred] will be passed the most up-to-date state of
   * the cache.
   */
  fun createAsync(newValue: T): Deferred<T> =
    dispatchCommandAsync(CacheCommand.CreateCache(newValue))

  /**
   * Returns a [Deferred] that provides the most-up-to-date value of the cache, after either
   * retrieving the current state (if defined), or calling the provided generator to create a new
   * state and initialize the cache to that state. The provided function must be thread-safe and
   * should have no side effects.
   */
  fun createIfAbsentAsync(generate: suspend () -> T): Deferred<T> =
    dispatchCommandAsync(CacheCommand.CreateCacheIfAbsent(generate))

  /**
   * Returns a [Deferred] that will provide the most-up-to-date value stored in the cache, or null
   * if it's not yet initialized.
   */
  fun readAsync(): Deferred<T?> = dispatchCommandAsync(CacheCommand.ReadCache())

  /**
   * Returns a [Deferred] similar to [readAsync], except this assumes the cache to have been created
   * already otherwise an exception will be thrown.
   */
  fun readIfPresentAsync(): Deferred<T> = dispatchCommandAsync(CacheCommand.ReadCacheWhenPresent())

  /**
   * Returns a [Deferred] that provides the most-up-to-date value of the cache, after atomically
   * updating it based on the specified update function. Note that the update function provided here
   * must be thread-safe and should have no side effects. This function is safe to call regardless
   * of whether the cache has been created, meaning it can be used also to initialize the cache.
   */
  fun updateAsync(update: suspend (T?) -> T): Deferred<T> =
    dispatchCommandAsync(CacheCommand.UpdateCache(update))

  /**
   * Returns a [Deferred] in the same way as [updateAsync], excepted this update is expected to
   * occur after cache creation otherwise an exception will be thrown.
   */
  fun updateIfPresentAsync(update: suspend (T) -> T): Deferred<T> =
    dispatchCommandAsync(CacheCommand.UpdateCacheWhenPresent(update))

  /** See [updateIfPresentAsync]. Returns a custom deferred result. */
  fun <V> updateWithCustomChannelIfPresentAsync(update: suspend (T) -> Pair<T, V>): Deferred<V> =
    dispatchCommandAsync(CacheCommand.UpdateCacheWithCustomChannelWhenPresent(update))

  /**
   * Returns a [Deferred] that executes when this cache has been fully cleared, or if it's already
   * been cleared.
   */
  fun deleteAsync(): Deferred<Unit> = dispatchCommandAsync(CacheCommand.DeleteCache())

  /**
   * Returns a [Deferred] that executes when checking the specified function on whether this cache
   * should be deleted, and returns whether it was deleted.
   *
   * Note that the provided function will not be called if the cache is already cleared.
   */
  fun maybeDeleteAsync(shouldDelete: suspend (T) -> Boolean): Deferred<Boolean> =
    dispatchCommandAsync(CacheCommand.ConditionallyDeleteCacheIfPresent(shouldDelete))

  /**
   * Returns a [Deferred] in the same way as [maybeDeleteAsync], except the deletion function
   * provided is guaranteed to be called regardless of the state of the cache, and whose return
   * value will be returned in this method's [Deferred].
   */
  fun maybeForceDeleteAsync(shouldDelete: suspend (T?) -> Boolean): Deferred<Boolean> =
    dispatchCommandAsync(CacheCommand.ConditionallyDeleteCache(shouldDelete))

  private fun <R, C> dispatchCommandAsync(
    command: C
  ): Deferred<R> where C: CacheCommand<T>, C: Completable<R> {
    cacheCommandQueue.trySend(command)
    return command.resultFuture.asDeferred()
  }

  private fun createCacheCommandQueue(initialValue: T?): SendChannel<CacheCommand<T>> {
    // The value of the cache. Note that this does not require a lock since it's only ever accessed
    // via the actor which guarantees mutually exclusive access.
    val cache = CachedValue(value = initialValue)
    return CoroutineScope(backgroundDispatcher).actor(capacity = Channel.UNLIMITED) {
      for (command in channel) {
        // This cast should never fail since it's enforced in the dispatch command above.
        @Suppress("UNCHECKED_CAST")
        val completable = command as Completable<Any?>

        try {
          val result: Any? = when (command) {
            is CacheCommand.RegisterObserver -> cache.registerChangeObserver(command.changeObserver)
            is CacheCommand.CreateCache -> cache.setCache(command.newValue)
            is CacheCommand.CreateCacheIfAbsent -> cache.maybeSetCache(command.newValueGenerator)
            is CacheCommand.ReadCache -> cache.nullableValue
            is CacheCommand.ReadCacheWhenPresent -> cache.nonNullValue
            is CacheCommand.UpdateCache -> cache.maybeSetCache(command.update)
            is CacheCommand.UpdateCacheWhenPresent ->
              cache.setCache(command.update(cache.nonNullValue))
            is CacheCommand.UpdateCacheWithCustomChannelWhenPresent<T, *> ->
              command.update(cache.nonNullValue).also { (value, _) -> cache.setCache(value) }.second
            is CacheCommand.DeleteCache -> cache.clearCache()
            is CacheCommand.ConditionallyDeleteCacheIfPresent ->
              cache.maybeClearCache(command.shouldDelete)
            is CacheCommand.ConditionallyDeleteCache -> cache.maybeClearCache(command.shouldDelete)
          }
          completable.resultFuture.set(result)
        } catch (e: Exception) {
          completable.resultFuture.setException(e)
        }
      }
    }
  }

  private companion object {
    // TODO: Add doc w/ explanation for why this is needed & why it can't be part of CacheCommand (limitations in Kotlin's type inference engine).
    private interface Completable<V> {
      val resultFuture: SettableFuture<V>
    }

    // TODO: Add documentation.
    private sealed class CacheCommand<T: Any> {
      class RegisterObserver<T: Any>(
        val changeObserver: suspend () -> Unit
      ): CacheCommand<T>(), Completable<Unit> {
        override val resultFuture: SettableFuture<Unit> = SettableFuture.create()
      }

      class CreateCache<T: Any>(val newValue: T): CacheCommand<T>(), Completable<T> {
        override val resultFuture: SettableFuture<T> = SettableFuture.create()
      }

      class CreateCacheIfAbsent<T: Any>(
        val newValueGenerator: suspend () -> T
      ): CacheCommand<T>(), Completable<T> {
        override val resultFuture: SettableFuture<T> = SettableFuture.create()
      }

      class ReadCache<T: Any>: CacheCommand<T>(), Completable<T?> {
        override val resultFuture: SettableFuture<T?> = SettableFuture.create()
      }

      class ReadCacheWhenPresent<T: Any>: CacheCommand<T>(), Completable<T> {
        override val resultFuture: SettableFuture<T> = SettableFuture.create()
      }

      class UpdateCache<T: Any>(val update: suspend (T?) -> T): CacheCommand<T>(), Completable<T> {
        override val resultFuture: SettableFuture<T> = SettableFuture.create()
      }

      class UpdateCacheWhenPresent<T: Any>(
        val update: suspend (T) -> T
      ): CacheCommand<T>(), Completable<T> {
        override val resultFuture: SettableFuture<T> = SettableFuture.create()
      }

      class UpdateCacheWithCustomChannelWhenPresent<T: Any, V: Any?>(
        val update: suspend (T) -> Pair<T, V>
      ): CacheCommand<T>(), Completable<V> {
        override val resultFuture: SettableFuture<V> = SettableFuture.create()
      }

      class DeleteCache<T: Any>: CacheCommand<T>(), Completable<Unit> {
        override val resultFuture: SettableFuture<Unit> = SettableFuture.create()
      }

      class ConditionallyDeleteCacheIfPresent<T: Any>(
        val shouldDelete: suspend (T) -> Boolean
      ): CacheCommand<T>(), Completable<Boolean> {
        override val resultFuture: SettableFuture<Boolean> = SettableFuture.create()
      }

      class ConditionallyDeleteCache<T: Any>(
        val shouldDelete: suspend (T?) -> Boolean
      ): CacheCommand<T>(), Completable<Boolean> {
        override val resultFuture: SettableFuture<Boolean> = SettableFuture.create()
      }
    }

    private class CachedValue<T>(
      private var value: T?, private var changeObserver: suspend () -> Unit = {}
    ) {
      val nullableValue: T?
        get() = value
      val nonNullValue: T
        get() = checkNotNull(value) { "Expected cache value to be set." }

      fun registerChangeObserver(changeObserver: suspend () -> Unit) {
        this.changeObserver = changeObserver
      }

      suspend fun setCache(newValue: T): T {
        value = newValue
        changeObserver()
        return newValue
      }

      suspend fun maybeSetCache(generateNewValue: suspend () -> T): T =
        setCache(value ?: generateNewValue())

      suspend fun maybeSetCache(generateNewValue: suspend (T?) -> T): T =
        setCache(generateNewValue(value))

      suspend fun clearCache() {
        value = null
        changeObserver()
      }

      @JvmName("maybeClearCacheIfPresent")
      suspend fun maybeClearCache(shouldDelete: suspend (T) -> Boolean): Boolean =
        value?.let { maybeClearCacheInternal(shouldDelete(it)) } ?: false

      @JvmName("maybeClearCache")
      suspend fun maybeClearCache(shouldDelete: suspend (T?) -> Boolean): Boolean =
        maybeClearCacheInternal(shouldDelete(value))

      private suspend fun maybeClearCacheInternal(shouldClear: Boolean): Boolean =
        shouldClear.also { if (it) clearCache() }
    }
  }

  /** An injectable factory for [InMemoryBlockingCache]es. */
  @Singleton
  class Factory @Inject constructor(
    @BackgroundDispatcher private val backgroundDispatcher: CoroutineDispatcher
  ) {
    /** Returns a new [InMemoryBlockingCache] with, optionally, the specified initial value. */
    fun <T : Any> create(initialValue: T? = null): InMemoryBlockingCache<T> =
      InMemoryBlockingCache(backgroundDispatcher, initialValue)
  }
}
