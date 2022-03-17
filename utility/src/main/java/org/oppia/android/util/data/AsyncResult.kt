package org.oppia.android.util.data

import android.os.SystemClock

/**
 * Represents the result from a single asynchronous function.
 *
 * [AsyncResult]s exist in one of three states:
 * - [Pending] to indicate an operation that hasn't yet completed
 * - [Success] to indicate an operation that finished as expected
 * - [Failure] to indicate an operation that finished in an unexpected way
 *
 * Since this is a sealed class, each type of result can be created by constructing one of the types
 * listed above. Results can also leverage Kotlin's exhaustive ``when`` statements when checking for
 * the type of results received:
 *
 * ```kotlin
 * when (result) {
 *   is AsyncResult.Pending -> { /* Show that the operation is pending. */ }
 *   is AsyncResult.Success -> { /* Do something with result.value. */ }
 *   is AsyncResult.Failure -> { /* Do something with result.error. */ }
 * }
 * ```
 *
 * Note that the above is the suggested way to always check for a result type since it ensures that
 * all possibilities are always considered, and can help minimize bugs.
 *
 * **A note on immutability:** This class is inherently immutable, though it may contain types which
 * are not (based on [T]). It's **strongly** suggested to only ever use this class with immutable
 * [T] types since the app's entire multithreading environment assumes this class to be safe to pass
 * between threads and coroutines.
 *
 * **A note on pending:** While [AsyncResult.Pending] exists, some [DataProvider]s may instead elect
 * to simply not provide a result until one is available. Both are acceptable results so long as the
 * UI knows how to react (i.e. it's the difference between the UI showing a loading indicator upon
 * initiating the operation and stopping it when a success/error is received, versus initiating the
 * loading indicator only after a pending result is received). It's generally recommended that data
 * providers always provide a pending result by default, but it may lead to a better user experience
 * to utilize it as a signal that a long operation is underway). This API may be changed in the
 * future to make these design choices more clear-cut and deliberate when implementing data
 * providers.
 */
sealed class AsyncResult<T> {
  /**
   * The timestamp (in millis) of when this result was created.
   *
   * This value should only be used to compared results created in the same process, and should
   * never be persisted or transferred across process boundaries. This value should be stable across
   * changes to the user device's system clock or calendar settings.
   */
  protected abstract val resultTimeMillis: Long

  /**
   * Returns whether this result is newer than, or the same age as, the specified result of the same
   * type.
   */
  fun <O> isNewerThanOrSameAgeAs(otherResult: AsyncResult<O>): Boolean {
    return resultTimeMillis >= otherResult.resultTimeMillis
  }

  /**
   * Returns a version of this result that retains its pending and failed states, but transforms its
   * success state according to the specified transformation function.
   *
   * Note that if the current result is a failure, the transformed result's failure will be a
   * chained exception with this result's failure as the root cause to preserve this transformation
   * in the exception's stacktrace.
   *
   * Note also that the specified transformation function should have no side effects, and be
   * non-blocking.
   */
  fun <O> transform(transformFunction: (T) -> O): AsyncResult<O> {
    return transformWithResult { value -> Success(transformFunction(value)) }
  }

  /**
   * Returns a transformed version of this result in the same way as [transform] except it supports
   * using a blocking transformation function instead of a non-blocking one.
   *
   * Note that the transform function is only used if the current result is a success, at which case
   * the function's result becomes the new, transformed result.
   */
  suspend fun <O> transformAsync(transformFunction: suspend (T) -> AsyncResult<O>): AsyncResult<O> {
    return transformWithResultAsync { value ->
      transformFunction(value)
    }
  }

  /**
   * Returns a version of this result that retains its pending and failed states, but combines its
   * success state with the success state of another result, according to the specified combine
   * function.
   *
   * Note that if the other result is either pending or failed, that pending or failed state will be
   * propagated to the returned result rather than attempting to combine the two states. Only
   * successful states are combined.
   *
   * Note that if the current result is a failure, the transformed result's failure will be a
   * chained exception with this result's failure as the root cause to preserve this combination in
   * the exception's stacktrace.
   *
   * Note also that the specified combine function should have no side effects, and be non-blocking.
   */
  fun <O, T2> combineWith(
    otherResult: AsyncResult<T2>,
    combineFunction: (T, T2) -> O
  ): AsyncResult<O> {
    return transformWithResult { value1 ->
      otherResult.transformWithResult { value2 -> Success(combineFunction(value1, value2)) }
    }
  }

  /**
   * Returns a version of this result that is combined with another result in the same way as
   * [combineWith], except it supports using a blocking combine function instead of a non-blocking
   * one.
   *
   * Note that the combine function is only used if both results are a success, at which case the
   * function's result becomes the new, combined result.
   */
  suspend fun <O, T2> combineWithAsync(
    otherResult: AsyncResult<T2>,
    combineFunction: suspend (T, T2) -> AsyncResult<O>
  ): AsyncResult<O> {
    return transformWithResultAsync { value1 ->
      otherResult.transformWithResultAsync { value2 ->
        combineFunction(value1, value2)
      }
    }
  }

  private fun <O> transformWithResult(transformFunction: (T) -> AsyncResult<O>): AsyncResult<O> {
    return when (this) {
      is Pending -> Pending()
      is Success -> transformFunction(value)
      is Failure -> Failure(ChainedFailureException(error))
    }
  }

  private suspend fun <O> transformWithResultAsync(
    transformFunction: suspend (T) -> AsyncResult<O>
  ): AsyncResult<O> {
    return when (this) {
      is Pending -> Pending()
      is Success -> transformFunction(value)
      is Failure -> Failure(ChainedFailureException(error))
    }
  }

  /** A chained exception to preserve failure stacktraces for [transform] and [transformAsync]. */
  class ChainedFailureException(cause: Throwable) : Exception(cause)

  /** [AsyncResult] representing an operation that may be completed in the future. */
  data class Pending<T>(
    override val resultTimeMillis: Long = SystemClock.uptimeMillis()
  ) : AsyncResult<T>()

  /** [AsyncResult] representing an operation that succeeded with a specific [value]. */
  data class Success<T>(
    val value: T,
    override val resultTimeMillis: Long = SystemClock.uptimeMillis()
  ) : AsyncResult<T>()

  /** [AsyncResult] representing an operation that failed with a specific [error]. */
  data class Failure<T>(
    val error: Throwable,
    override val resultTimeMillis: Long = SystemClock.uptimeMillis()
  ) : AsyncResult<T>()
}
