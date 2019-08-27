package org.oppia.util.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.async
import java.util.concurrent.Executors

/**
 * An in-memory cache that provides blocking CRUD operations such that each operation is guaranteed to operate exactly
 * after any prior started operations began, and before any future operations. This class is thread-safe. Note that it's
 * safe to execute long-running operations in lambdas passed into the methods of this class.
 */
class InMemoryBlockingCache<T: Any>(initialValue: T? = null) {
  private val blockingDispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
  private val blockingScope = CoroutineScope(blockingDispatcher)

  /**
   * The value of the cache. Note that this does not require a lock since it's only ever accessed via the blocking
   * dispatcher's single thread.
   */
  private var value: T? = initialValue

  /**
   * Returns a [Deferred] that, upon completion, guarantees that the cache has been recreated and initialized to the
   * specified value. The [Deferred] will be passed the most up-to-date state of the cache.
   */
  fun createAsync(newValue: T): Deferred<T> {
    return blockingScope.async {
      value = newValue
      newValue
    }
  }

  /**
   * Returns a [Deferred] that will provide the most-up-to-date value stored in the cache, or null if it's not yet
   * initialized.
   */
  fun readAsync(): Deferred<T?> {
    return blockingScope.async {
      value
    }
  }

  /**
   * Returns a [Deferred] similar to [readAsync], except this assumes the cache to have been created already otherwise
   * an exception will be thrown.
   */
  fun readIfPresentAsync(): Deferred<T> {
    return blockingScope.async {
      checkNotNull(value) { "Expected to read the cache only after it's been created" }
    }
  }

  /**
   * Returns a [Deferred] that provides the most-up-to-date value of the cache, after either retrieving the current
   * state (if defined), or calling the provided generator to create a new state and initialize the cache to that state.
   * The provided function must be thread-safe and should have no side effects.
   */
  fun createIfAbsentAsync(generate: suspend () -> T): Deferred<T> {
    return blockingScope.async {
      val initedValue = value ?: generate()
      value = initedValue
      initedValue
    }
  }

  /**
   * Returns a [Deferred] that provides the most-up-to-date value of the cache, after atomically updating it based on
   * the specified update function. Note that the update function provided here must be thread-safe and should have no
   * side effects. This function is safe to call regardless of whether the cache has been created, meaning it can be
   * used also to initialize the cache.
   */
  fun updateAsync(update: suspend (T?) -> T): Deferred<T> {
    return blockingScope.async {
      val updatedValue = update(value)
      value = updatedValue
      updatedValue
    }
  }

  /**
   * Returns a [Deferred] in the same way as [updateAsync], excepted this update is expected to occur after cache
   * creation otherwise an exception will be thrown.
   */
  fun updateIfPresentAsync(update: suspend (T) -> T): Deferred<T> {
    return blockingScope.async {
      val updatedValue = update(checkNotNull(value) { "Expected to update the cache only after it's been created" })
      value = updatedValue
      updatedValue
    }
  }

  /**
   * Returns a [Deferred] that executes when this cache has been fully cleared, or if it's already been cleared.
   */
  fun deleteAsync(): Deferred<Unit> {
    return blockingScope.async {
      value = null
    }
  }
}
