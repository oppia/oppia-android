package org.oppia.util.data

/** Represents the result from a single asynchronous function. */
class AsyncResult<T> private constructor(
  private val status: Status,
  private val value: T? = null,
  val error: Throwable? = null
) {
  /** Represents the status of an asynchronous result. */
  enum class Status {
    /** Indicates that the asynchronous operation is not yet completed. */
    PENDING,
    /** Indicates that the asynchronous operation completed successfully and has a result. */
    SUCCEEDED,
    /** Indicates that the asynchronous operation failed and has an error. */
    FAILED
  }

  /** Returns whether this result is still pending. */
  fun isPending(): Boolean {
    return status == Status.PENDING
  }

  /** Returns whether this result has completed successfully. */
  fun isSuccess(): Boolean {
    return status == Status.SUCCEEDED
  }

  /** Returns whether this result has completed unsuccessfully. */
  fun isFailure(): Boolean {
    return status == Status.FAILED
  }

  /** Returns whether this result has completed (successfully or unsuccessfully). */
  fun isCompleted(): Boolean {
    return isSuccess() || isFailure()
  }

  /** Returns the value of the result if it succeeded, otherwise the specified default value. */
  fun getOrDefault(defaultValue: T): T {
    return if (isSuccess()) value!! else defaultValue
  }

  /**
   * Returns the value of the result if it succeeded, otherwise throws the underlying exception. Throws if this result
   * is not yet completed.
   */
  fun getOrThrow(): T {
    check(isCompleted()) { "Result is not yet completed." }
    if (isSuccess()) return value!! else throw error!!
  }

  /** Returns the underlying exception if this result failed, otherwise null. */
  fun getErrorOrNull(): Throwable? {
    return if (isFailure()) error else null
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
    return when(status) {
      Status.PENDING -> pending()
      Status.FAILED -> failed(ChainedFailureException(error!!))
      Status.SUCCEEDED -> success(transformFunction(value!!))
    }
  }

  /**
   * Returns a transformed version of this result in the same way as [transform] except it supports using a blocking
   * transformation function instead of a non-blocking one.
   */
  suspend fun <O> transformAsync(transformFunction: suspend (T) -> O): AsyncResult<O> {
    return when(status) {
      Status.PENDING -> pending()
      Status.FAILED -> failed(ChainedFailureException(error!!))
      Status.SUCCEEDED -> success(transformFunction(value!!))
    }
  }

  companion object {
    /** Returns a pending result. */
    fun <T> pending(): AsyncResult<T> {
      return AsyncResult(status = Status.PENDING)
    }

    /** Returns a successful result with the specified payload. */
    fun <T> success(value: T): AsyncResult<T> {
      return AsyncResult(status = Status.SUCCEEDED, value = value)
    }

    /** Returns a failed result with the specified error. */
    fun <T> failed(error: Throwable): AsyncResult<T> {
      return AsyncResult(status = Status.FAILED, error = error)
    }
  }

  /** A chained exception to preserve failure stacktraces for [transform] and [transformAsync]. */
  class ChainedFailureException(cause: Throwable): Exception(cause)
}
