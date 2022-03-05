package org.oppia.android.util.data

import android.os.SystemClock

// TODO: update documentation to explain how to create these, and how to check the result type.
/** Represents the result from a single asynchronous function. */
sealed class AsyncResult<T> {
  protected abstract val resultTimeMillis: Long

  /** Returns whether this result is newer than, or the same age as, the specified result of the same type. */
  fun <O> isNewerThanOrSameAgeAs(otherResult: AsyncResult<O>): Boolean {
    return resultTimeMillis >= otherResult.resultTimeMillis
  }

  /**
   * Returns a version of this result that retains its pending and failed states, but transforms its success state
   * according to the specified transformation function.
   *
   * Note that if the current result is a failure, the transformed result's failure will be a chained exception with
   * this result's failure as the root cause to preserve this transformation in the exception's stacktrace.
   *
   * Note also that the specified transformation function should have no side effects, and be non-blocking.
   */
  fun <O> transform(transformFunction: (T) -> O): AsyncResult<O> {
    return transformWithResult { value -> Success(transformFunction(value)) }
  }

  /**
   * Returns a transformed version of this result in the same way as [transform] except it supports using a blocking
   * transformation function instead of a non-blocking one. Note that the transform function is only used if the current
   * result is a success, at which case the function's result becomes the new, transformed result.
   */
  suspend fun <O> transformAsync(transformFunction: suspend (T) -> AsyncResult<O>): AsyncResult<O> {
    return transformWithResultAsync { value ->
      transformFunction(value)
    }
  }

  /**
   * Returns a version of this result that retains its pending and failed states, but combines its success state with
   * the success state of another result, according to the specified combine function.
   *
   * Note that if the other result is either pending or failed, that pending or failed state will be propagated to the
   * returned result rather than attempting to combine the two states. Only successful states are combined.
   *
   * Note that if the current result is a failure, the transformed result's failure will be a chained exception with
   * this result's failure as the root cause to preserve this combination in the exception's stacktrace.
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
   * Returns a version of this result that is combined with another result in the same way as [combineWith], except it
   * supports using a blocking combine function instead of a non-blocking one. Note that the combine function is only
   * used if both results are a success, at which case the function's result becomes the new, combined result.
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

  data class Pending<T>(
    override val resultTimeMillis: Long = SystemClock.uptimeMillis()
  ) : AsyncResult<T>()

  data class Success<T>(
    val value: T, override val resultTimeMillis: Long = SystemClock.uptimeMillis()
  ) : AsyncResult<T>() {
  }

  data class Failure<T>(
    val error: Throwable, override val resultTimeMillis: Long = SystemClock.uptimeMillis()
  ) : AsyncResult<T>()
}
