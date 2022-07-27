package org.oppia.android.util.data

import com.google.common.util.concurrent.SettableFuture
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.ObsoleteCoroutinesApi
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
@OptIn(ObsoleteCoroutinesApi::class)
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
  fun updateIfPresentAsync(update: suspend (T) -> T): Deferred<T> {
    return dispatchCommandAsync(CacheCommand.UpdateCacheWhenPresent(update))
  }

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
    /**
     * Interface to augment [CacheCommand] by allowing command subclasses to be [Completable], that
     * is, to have a singular value result that can be passed back to calling classes as a
     * [Deferred].
     *
     * Note that this can't actually be part of [CacheCommand] itself due to a limitation in
     * Kotlin's type inference engine (it can't seem to understand when subclasses predefine a type
     * for the future in a ``when`` block when the original type uses a wildcard for the future.
     */
    private interface Completable<V> {
      /**
       * Stores the value of the completed [CacheCommand] (see corresponding command KDocs for
       * specifics on what this value will be for each command).
       */
      val resultFuture: SettableFuture<V>
    }

    /**
     * Defines an asynchronous command that will execute synchronously relative to other commands
     * (providing thread safety across a multi-threaded dispatcher).
     */
    private sealed class CacheCommand<T: Any> {
      /**
       * [CacheCommand] for registering a new [changeObserver] for this cache.
       *
       * [resultFuture] will store an indication of pass/failure on whether the observer was set.
       */
      class RegisterObserver<T: Any>(
        val changeObserver: suspend () -> Unit
      ): CacheCommand<T>(), Completable<Unit> {
        override val resultFuture: SettableFuture<Unit> = SettableFuture.create()
      }

      /**
       * [CacheCommand] for initializing the cache with a [newValue].
       *
       * [resultFuture] will store the latest value of the cache.
       */
      class CreateCache<T: Any>(val newValue: T): CacheCommand<T>(), Completable<T> {
        override val resultFuture: SettableFuture<T> = SettableFuture.create()
      }

      /**
       * [CacheCommand] for initializing the cache based on a [newValueGenerator] if it doesn't
       * already have a value defined.
       *
       * [resultFuture] will store the latest value of the cache.
       */
      class CreateCacheIfAbsent<T: Any>(
        val newValueGenerator: suspend () -> T
      ): CacheCommand<T>(), Completable<T> {
        override val resultFuture: SettableFuture<T> = SettableFuture.create()
      }

      /**
       * [CacheCommand] for reading the current value of the cache.
       *
       * [resultFuture] will store the read result.
       */
      class ReadCache<T: Any>: CacheCommand<T>(), Completable<T?> {
        override val resultFuture: SettableFuture<T?> = SettableFuture.create()
      }

      /**
       * [CacheCommand] for reading the cache (with an assumption that it's already inited).
       *
       * [resultFuture] will store the read value (or a failure if the cache is not initialized).
       */
      class ReadCacheWhenPresent<T: Any>: CacheCommand<T>(), Completable<T> {
        override val resultFuture: SettableFuture<T> = SettableFuture.create()
      }

      /**
       * [CacheCommand] for updating the cache using a transformation [update] function.
       *
       * [resultFuture] will store the latest value of the cache.
       */
      class UpdateCache<T: Any>(val update: suspend (T?) -> T): CacheCommand<T>(), Completable<T> {
        override val resultFuture: SettableFuture<T> = SettableFuture.create()
      }

      /**
       * [CacheCommand] for updating the cache using a transformation [update] function, but only
       * when the cache is already initialized.
       *
       * [resultFuture] will store the latest value of the cache, or a failure if the cache is not
       * already initialized.
       */
      class UpdateCacheWhenPresent<T: Any>(
        val update: suspend (T) -> T
      ): CacheCommand<T>(), Completable<T> {
        override val resultFuture: SettableFuture<T> = SettableFuture.create()
      }

      /**
       * [CacheCommand] for updating the cache using a transformation [update] function, but only
       * when the cache is already initialized.
       *
       * This differs from [UpdateCacheWhenPresent] in that a custom channel can be provided as the
       * result via [update].
       *
       * [resultFuture] will store the non-cache value returned by [update] (i.e. the extra
       * channel), or a failure if the cache is not already initialized.
       */
      class UpdateCacheWithCustomChannelWhenPresent<T: Any, V: Any?>(
        val update: suspend (T) -> Pair<T, V>
      ): CacheCommand<T>(), Completable<V> {
        override val resultFuture: SettableFuture<V> = SettableFuture.create()
      }

      /**
       * [CacheCommand] for clearing the cache.
       *
       * [resultFuture] will store the pass/fail result on whether the cache was successfully
       * cleared.
       */
      class DeleteCache<T: Any>: CacheCommand<T>(), Completable<Unit> {
        override val resultFuture: SettableFuture<Unit> = SettableFuture.create()
      }

      /**
       * [CacheCommand] for clearing the cache, but only if [shouldDelete] returns true.
       *
       * [resultFuture] will store ``true`` if the cache was successfully cleared, or ``false`` if
       * not.
       */
      class ConditionallyDeleteCache<T: Any>(
        val shouldDelete: suspend (T?) -> Boolean
      ): CacheCommand<T>(), Completable<Boolean> {
        override val resultFuture: SettableFuture<Boolean> = SettableFuture.create()
      }

      /**
       * [CacheCommand] for clearing the cache, but only if the cache is present and if
       * [shouldDelete] returns true.
       *
       * [resultFuture] will store ``true`` if the cache was successfully cleared, or ``false`` if
       * not.
       */
      class ConditionallyDeleteCacheIfPresent<T: Any>(
        val shouldDelete: suspend (T) -> Boolean
      ): CacheCommand<T>(), Completable<Boolean> {
        override val resultFuture: SettableFuture<Boolean> = SettableFuture.create()
      }
    }

    /**
     * Represents the core state of the [InMemoryBlockingCache] with various convenience functions
     * for mutating the cache's state.
     *
     * This class is not safe to access concurrently across multiple threads.
     *
     * @property value the initial value of the cache
     */
    private class CachedValue<T>(private var value: T?) {
      private var changeObserver: suspend () -> Unit = {}

      /** The current value of the cache, or null if it hasn't yet been initialized. */
      val nullableValue: T?
        get() = value

      /**
       * The current non-null value of the cache (assumes that the cache has been initialized), or
       * fails if the cache isn't initialized.
       */
      val nonNullValue: T
        get() = checkNotNull(value) { "Expected cache value to be set." }

      /**
       * Registers a new [changeObserver] that will be called whenever the cache's internal state
       * has changed.
       */
      fun registerChangeObserver(changeObserver: suspend () -> Unit) {
        this.changeObserver = changeObserver
      }

      /**
       * Updates the cache with a [newValue], returning its current state and notifying the
       * registered observer, if any (note that the observer is only called if the value has
       * actually changed).
       */
      suspend fun setCache(newValue: T): T {
        val oldValue = value
        value = newValue
        if (oldValue != newValue) {
          changeObserver()
        }
        return newValue
      }

      /**
       * Conditionally updates the cache using [generateNewValue] only if the cache isn't already
       * initialized, returning the current value of the cache and notifying the registered observer
       * (if there is any).
       */
      suspend fun maybeSetCache(generateNewValue: suspend () -> T): T =
        setCache(value ?: generateNewValue())

      /**
       * Conditionally updates the cache using [generateNewValue], returning the current value of
       * the cache and notifying the registered observer (if there is any).
       */
      suspend fun maybeSetCache(generateNewValue: suspend (T?) -> T): T =
        setCache(generateNewValue(value))

      /**
       * Resets the cache back to null, notifying the registered observer (if any is registered) and
       * only if the cache wasn't already cleared.
       */
      suspend fun clearCache() {
        val oldValue = value
        value = null
        if (oldValue != null) {
          changeObserver()
        }
      }

      /**
       * Conditionally resets the cache back to null if [shouldDelete] returns true, notifying the
       * registered observer (if any is registered), and returning whether the cache was reset.
       */
      @JvmName("maybeClearCache")
      suspend fun maybeClearCache(shouldDelete: suspend (T?) -> Boolean): Boolean =
        maybeClearCacheInternal(shouldDelete(value))

      /**
       * Conditionally resets the cache back to null if [shouldDelete] returns true, notifying the
       * registered observer (if any is registered), and returning whether the cache was reset.
       *
       * Note that this always returns false if the cache isn't yet initialized.
       */
      @JvmName("maybeClearCacheIfPresent")
      suspend fun maybeClearCache(shouldDelete: suspend (T) -> Boolean): Boolean =
        value?.let { maybeClearCacheInternal(shouldDelete(it)) } ?: false

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
