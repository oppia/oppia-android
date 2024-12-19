package org.oppia.android.util.data

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.actor
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

  /**
   * The value of the cache. Note that this does not require a lock since it's only ever accessed via the blocking
   * dispatcher's single thread.
   */
  private var value: T? = initialValue
  private val scope = CoroutineScope(SupervisorJob() + blockingDispatcher)
  private var changeObserver: suspend (T?, T?) -> Unit = { _, _ -> }

  private sealed class CacheOp<T, R> {
    abstract suspend fun execute(state: State<T>): R

    data class Create<T>(val value: T) : CacheOp<T, T>() {
      override suspend fun execute(state: State<T>): T {
        state.value = value
        state.changeObserver(state.previousValue, value)
        state.previousValue = value
        return value
      }
    }

    data class CreateIfAbsent<T>(val generate: suspend () -> T) : CacheOp<T, T>() {
      override suspend fun execute(state: State<T>): T {
        if (state.value == null) {
          val newValue = generate()
          state.value = newValue
          state.changeObserver(state.previousValue, newValue)
          state.previousValue = newValue
        }
        return checkNotNull(state.value)
      }
    }

    class Read<T>(val dummy: Boolean = true) : CacheOp<T, T?>() {
      override suspend fun execute(state: State<T>): T? = state.value
    }

    class ReadIfPresent<T>(val dummy: Boolean = true) : CacheOp<T, T>() {
      override suspend fun execute(state: State<T>): T {
        return checkNotNull(state.value) {
          "Expected to read the cache only after it's been created"
        }
      }
    }

    data class Update<T>(val transform: suspend (T?) -> T) : CacheOp<T, T>() {
      override suspend fun execute(state: State<T>): T {
        val newValue = transform(state.value)
        state.value = newValue
        state.changeObserver(state.previousValue, newValue)
        state.previousValue = newValue
        return newValue
      }
    }

    data class UpdateIfPresent<T>(val transform: suspend (T) -> T) : CacheOp<T, T>() {
      override suspend fun execute(state: State<T>): T {
        val currentValue = checkNotNull(state.value) {
          "Expected to update the cache only after it's been created"
        }
        val newValue = transform(currentValue)
        state.value = newValue
        state.changeObserver(state.previousValue, newValue)
        state.previousValue = newValue
        return newValue
      }
    }

    data class UpdateWithCustom<T, V>(
      val transform: suspend (T) -> Pair<T, V>
    ) : CacheOp<T, V>() {
      override suspend fun execute(state: State<T>): V {
        val currentValue = checkNotNull(state.value) {
          "Expected to update the cache only after it's been created"
        }
        val (newValue, result) = transform(currentValue)
        state.value = newValue
        state.changeObserver(state.previousValue, newValue)
        state.previousValue = newValue
        return result
      }
    }

    class Delete<T>(val dummy: Boolean = true) : CacheOp<T, Unit>() {
      override suspend fun execute(state: State<T>) {
        state.changeObserver(state.value, null)
        state.previousValue = state.value
        state.value = null
      }
    }
  }

  private class State<T>(
    var value: T?,
    var previousValue: T?,
    var changeObserver: suspend (T?, T?) -> Unit
  )

  private val state = State(initialValue, null, changeObserver)

  private val actor = scope.actor<Pair<CacheOp<T, Any?>, Channel<Result<Any?>>>>(
    capacity = Channel.UNLIMITED
  ) {
    for ((op, resultChannel) in channel) {
      try {
        val result = op.execute(state)
        resultChannel.send(Result.success(result))
      } catch (e: Exception) {
        resultChannel.send(Result.failure(e))
      } finally {
        resultChannel.close()
      }
    }
  }

  @Suppress("UNCHECKED_CAST")
  private suspend fun <R> submitOperation(op: CacheOp<T, R>): R {
    val resultChannel = Channel<Result<Any?>>()
    actor.send(op as CacheOp<T, Any?> to resultChannel)
    val result = resultChannel.receive() as Result<R>
    return result.getOrThrow()
  }

  /** Registers an observer that is called synchronously whenever this cache's contents are changed. */
  fun observeChanges(observer: suspend (T?, T?) -> Unit) {
    state.changeObserver = observer
  }

  /**
   * Returns a [Deferred] that, upon completion, guarantees that the cache has been recreated and initialized to the
   * specified value. The [Deferred] will be passed the most up-to-date state of the cache.
   */
  fun createAsync(newValue: T): Deferred<T> = scope.async {
    submitOperation(CacheOp.Create(newValue))
  }

  /**
   * Returns a [Deferred] that provides the most-up-to-date value of the cache, after either retrieving the current
   * state (if defined), or calling the provided generator to create a new state and initialize the cache to that state.
   * The provided function must be thread-safe and should have no side effects.
   */
  fun createIfAbsentAsync(generate: suspend () -> T): Deferred<T> = scope.async {
    submitOperation(CacheOp.CreateIfAbsent(generate))
  }

  /**
   * Returns a [Deferred] that will provide the most-up-to-date value stored in the cache, or null if it's not yet
   * initialized.
   */
  fun readAsync(): Deferred<T?> = scope.async {
    submitOperation(CacheOp.Read())
  }

  /**
   * Returns a [Deferred] similar to [readAsync], except this assumes the cache to have been created already otherwise
   * an exception will be thrown.
   */
  fun readIfPresentAsync(): Deferred<T> = scope.async {
    submitOperation(CacheOp.ReadIfPresent())
  }

  /**
   * Returns a [Deferred] that provides the most-up-to-date value of the cache, after atomically updating it based on
   * the specified update function. Note that the update function provided here must be thread-safe and should have no
   * side effects. This function is safe to call regardless of whether the cache has been created, meaning it can be
   * used also to initialize the cache.
   */
  fun updateAsync(transform: suspend (T?) -> T): Deferred<T> = scope.async {
    submitOperation(CacheOp.Update(transform))
  }

  /**
   * Returns a [Deferred] in the same way as [updateAsync], excepted this update is expected to occur after cache
   * creation otherwise an exception will be thrown.
   */
  fun updateIfPresentAsync(transform: suspend (T) -> T): Deferred<T> = scope.async {
    submitOperation(CacheOp.UpdateIfPresent(transform))
  }

  /** See [updateIfPresentAsync]. Returns a custom deferred result. */
  fun <V> updateWithCustomChannelIfPresentAsync(
    transform: suspend (T) -> Pair<T, V>
  ): Deferred<V> = scope.async {
    submitOperation(CacheOp.UpdateWithCustom(transform))
  }

  /**
   * Returns a [Deferred] in the same way and for the same conditions as [updateIfPresentAsync] except the provided
   * function is expected to update the cache in-place and return a custom value to propagate to the result of the
   * [Deferred] object.
   */
  fun <O> updateInPlaceIfPresentAsync(transform: suspend (T) -> O): Deferred<O> = scope.async {
    transform(
      checkNotNull(state.value) { "Expected to update the cache only after it's been created" }
    )
  }

  /**
   * Returns a [Deferred] that executes when this cache has been fully cleared, or if it's already been cleared.
   */
  fun deleteAsync(): Deferred<Unit> = scope.async {
    submitOperation(CacheOp.Delete())
  }

  /**
   * Returns a [Deferred] that executes when checking the specified function on whether this cache should be deleted,
   * and returns whether it was deleted.
   *
   * Note that the provided function will not be called if the cache is already cleared.
   */
  fun maybeDeleteAsync(shouldDelete: suspend (T) -> Boolean): Deferred<Boolean> = scope.async {
    val valueSnapshot = state.value
    if (valueSnapshot != null && shouldDelete(valueSnapshot)) {
      submitOperation(CacheOp.Delete())
      true
    } else false
  }

  /**
   * Returns a [Deferred] in the same way as [maybeDeleteAsync], except the deletion function provided is guaranteed to
   * be called regardless of the state of the cache, and whose return value will be returned in this method's
   * [Deferred].
   */
  fun maybeForceDeleteAsync(shouldDelete: suspend (T?) -> Boolean): Deferred<Boolean> =
    scope.async {
      if (shouldDelete(state.value)) {
        submitOperation(CacheOp.Delete())
        true
      } else false
    }

  /** An injectable factory for [InMemoryBlockingCache]es. */
  @Singleton
  class Factory @Inject constructor(
    @BlockingDispatcher private val blockingDispatcher: CoroutineDispatcher
  ) {
    /** Returns a new [InMemoryBlockingCache] with, optionally, the specified initial value. */
    fun <T : Any> create(initialValue: T? = null): InMemoryBlockingCache<T> {
      return InMemoryBlockingCache(blockingDispatcher, initialValue)
    }
  }
}
