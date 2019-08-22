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
}
