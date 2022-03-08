package org.oppia.android.testing.data

import com.google.common.truth.BooleanSubject
import com.google.common.truth.ComparableSubject
import com.google.common.truth.DoubleSubject
import com.google.common.truth.FailureMetadata
import com.google.common.truth.FloatSubject
import com.google.common.truth.IntegerSubject
import com.google.common.truth.IterableSubject
import com.google.common.truth.LongSubject
import com.google.common.truth.MapSubject
import com.google.common.truth.StringSubject
import com.google.common.truth.Subject
import com.google.common.truth.Subject.Factory
import com.google.common.truth.ThrowableSubject
import com.google.common.truth.Truth.assertAbout
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.extensions.proto.LiteProtoSubject
import com.google.common.truth.extensions.proto.LiteProtoTruth.assertThat
import com.google.protobuf.MessageLite
import org.oppia.android.util.data.AsyncResult

// TODO(#4236): Add tests for this class.

/**
 * Truth subject for verifying properties of [AsyncResult]s.
 *
 * Call [assertThat] to create the subject.
 */
class AsyncResultSubject<T>(
  failureMetadata: FailureMetadata?,
  @PublishedApi internal val actual: AsyncResult<T>
) : Subject(failureMetadata, actual) {
  /** Verifies that the [AsyncResult] under test is of type [AsyncResult.Pending]. */
  fun isPending() {
    ensureActualIsType<AsyncResult.Pending<T>>()
  }

  /** Verifies that the [AsyncResult] under test is not type [AsyncResult.Pending]. */
  fun isNotPending() {
    ensureActualIsNotType<AsyncResult.Pending<T>>()
  }

  /** Verifies that the [AsyncResult] under test is of type [AsyncResult.Success]. */
  fun isSuccess() {
    ensureActualIsType<AsyncResult.Success<T>>()
  }

  /** Verifies that the [AsyncResult] under test is not type [AsyncResult.Success]. */
  fun isNotSuccess() {
    ensureActualIsNotType<AsyncResult.Success<T>>()
  }

  /** Verifies that the [AsyncResult] under test is of type [AsyncResult.Failure]. */
  fun isFailure() {
    ensureActualIsType<AsyncResult.Failure<T>>()
  }

  /** Verifies that the [AsyncResult] under test is not type [AsyncResult.Failure]. */
  fun isNotFailure() {
    ensureActualIsNotType<AsyncResult.Failure<T>>()
  }

  /**
   * Verifies that the [AsyncResult] under test is of type [AsyncResult.Success] and then calls
   * [block] with the [AsyncResult.Success.value] result.
   *
   * Note that this does not perform type checking, so it's up to the caller to ensure that the [T]
   * type used by the [AsyncResult] is correct.
   */
  fun hasSuccessValueWhere(block: T.() -> Unit) =
    ensureActualIsType<AsyncResult.Success<T>>().value.block()

  /**
   * Returns a [Subject] that can be used to perform additional assertions about the
   * [AsyncResult.Success.value] of the result under test (this verifies that the result is a
   * success, similar to [isSuccess]).
   */
  fun isSuccessThat(): Subject = assertThat(ensureActualIsType<AsyncResult.Success<T>>().value)

  /* NOTE TO DEVELOPERS: Add more subject types below, as needed. */

  /**
   * Returns a [ComparableSubject] of type [C] using the same considerations as [isSuccessThat],
   * except this also verifies that the success value is a [Comparable] (though it can't verify
   * [C] due to type erasure).
   */
  inline fun <reified C : Comparable<C>> isComparableSuccessThat(): ComparableSubject<C> =
    assertThat(extractSuccessValue<C>())

  /**
   * Returns a [StringSubject] using the same considerations as [isSuccessThat], except this also
   * verifies that the successful value is a [String].
   */
  fun isStringSuccessThat(): StringSubject = assertThat(extractSuccessValue<String>())

  /**
   * Returns a [BooleanSubject] for the success value (as a [Boolean] version of
   * [isStringSuccessThat]).
   */
  fun isBooleanSuccessThat(): BooleanSubject = assertThat(extractSuccessValue<Boolean>())

  /**
   * Returns an [IntegerSubject] for the success value (as an [Int] version of
   * [isStringSuccessThat]).
   */
  fun isIntSuccessThat(): IntegerSubject = assertThat(extractSuccessValue<Int>())

  /**
   * Returns a [LongSubject] for the success value (as a [Long] version of [isStringSuccessThat]).
   */
  fun isLongSuccessThat(): LongSubject = assertThat(extractSuccessValue<Long>())

  /**
   * Returns a [FloatSubject] for the success value (as a [Float] version of [isStringSuccessThat]).
   */
  fun isFloatSuccessThat(): FloatSubject = assertThat(extractSuccessValue<Float>())

  /**
   * Returns a [DoubleSubject] for the success value (as a [Double] version of
   * [isStringSuccessThat]).
   */
  fun isDoubleSuccessThat(): DoubleSubject = assertThat(extractSuccessValue<Double>())

  /**
   * Returns a [LiteProtoSubject] for the success value (as a [MessageLite] version of
   * [isStringSuccessThat]).
   */
  fun isProtoSuccessThat(): LiteProtoSubject = assertThat(extractSuccessValue<MessageLite>())

  /**
   * Returns an [IterableSubject] for the success value (as an [Iterator] version of
   * [isComparableSuccessThat], including the inability to verify [E]).
   */
  fun <E> isIterableSuccessThat(): IterableSubject = assertThat(extractSuccessValue<Iterable<E>>())

  /**
   * Returns a [MapSubject] for the success value (as a [Map] version of [isComparableSuccessThat],
   * including the inability to verify [K] and [V]).
   */
  fun <K, V> asMapSuccessThat(): MapSubject = assertThat(extractSuccessValue<Map<K, V>>())

  /**
   * Returns a [ThrowableSubject] for the success value (as a [MessageLite] version of
   * [isStringSuccessThat]).
   */
  fun asThrowableSuccessThat(): ThrowableSubject = assertThat(extractSuccessValue<Throwable>())

  /**
   * Verifies that the result under test is a failure (similar to [isFailure]) and returns a
   * [ThrowableSubject] to verify details about the [AsyncResult.Failure.error].
   */
  fun isFailureThat(): ThrowableSubject =
    assertThat(ensureActualIsType<AsyncResult.Failure<T>>().error)

  /**
   * Verifies that the result under test is newer or the same age as [other] (per
   * [AsyncResult.isNewerThanOrSameAgeAs]).
   */
  fun isNewerOrSameAgeAs(other: AsyncResult<T>) {
    assertThat(actual.isNewerThanOrSameAgeAs(other)).isTrue()
  }

  /**
   * Verifies that the result under test is older than [other] (per
   * [AsyncResult.isNewerThanOrSameAgeAs]).
   */
  fun isOlderThan(other: AsyncResult<T>) {
    assertThat(actual.isNewerThanOrSameAgeAs(other)).isFalse()
  }

  /**
   * Verifies the result under test is successful (per [ensureActualIsType]) and returns its
   * [AsyncResult.Success.value] as type [T] (this method will fail if the conversion can't happen).
   *
   * Note that this is a [PublishedApi] method since it's referenced in functions inlined above, and
   * should never be called outside this class.
   */
  @PublishedApi // See: https://stackoverflow.com/a/41905907/3689782.
  internal inline fun <reified T> extractSuccessValue(): T {
    return ensureActualIsType<AsyncResult.Success<T>>().value.also {
      assertThat(it).isInstanceOf(T::class.java)
    }
  }

  /**
   * Verifies that the result under test is of type [T] (which can be useful when generally checking
   * for pending, failure, or success results), failing if it isn't.
   *
   * Note that this is a [PublishedApi] method since it's referenced in functions inlined above, and
   * should never be called outside this class.
   */
  @PublishedApi
  internal inline fun <reified T> ensureActualIsType(): T {
    assertThat(actual).isInstanceOf(T::class.java)
    // This extra check is just to ensure Kotlin knows 'actual' is of type 'T'.
    check(actual is T) { "Error: Truth didn't correctly catch mis-typing." }
    return actual
  }

  private inline fun <reified T> ensureActualIsNotType() {
    assertThat(actual).isNotInstanceOf(T::class.java)
  }

  companion object {
    /**
     * Returns a new [AsyncResultSubject] to verify aspects of the specified [AsyncResult] value.
     */
    fun <T> assertThat(actual: AsyncResult<T>): AsyncResultSubject<T> {
      return assertAbout(
        Factory<AsyncResultSubject<T>, AsyncResult<T>>(::AsyncResultSubject)
      ).that(actual)
    }
  }
}
