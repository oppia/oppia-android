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
import com.google.common.truth.Truth
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.extensions.proto.LiteProtoSubject
import com.google.common.truth.extensions.proto.LiteProtoTruth.assertThat
import com.google.protobuf.MessageLite
import org.oppia.android.util.data.AsyncResult

// TODO: file issue and add TODO to add tests.

class AsyncResultSubject<T>(
  failureMetadata: FailureMetadata?,
  @PublishedApi internal val actual: AsyncResult<T>
) : Subject(failureMetadata, actual) {
  fun isPending() {
    ensureActualIsType<AsyncResult.Pending<T>>()
  }

  fun isSuccess() {
    ensureActualIsType<AsyncResult.Success<T>>()
  }

  fun isFailure() {
    ensureActualIsType<AsyncResult.Failure<T>>()
  }

  fun hasSuccessValueWhere(block: T.() -> Unit) =
    ensureActualIsType<AsyncResult.Success<T>>().value.block()

  fun isSuccessThat(): Subject = assertThat(ensureActualIsType<AsyncResult.Success<T>>().value)

  /* NOTE TO DEVELOPERS: Add more subject types below, as needed. */

  inline fun <reified C: Comparable<C>> isComparableSuccessThat(): ComparableSubject<C> =
    assertThat(extractSuccessValue<C>())

  fun isStringSuccessThat(): StringSubject = assertThat(extractSuccessValue<String>())

  fun isBooleanSuccessThat(): BooleanSubject = assertThat(extractSuccessValue<Boolean>())

  fun isIntSuccessThat(): IntegerSubject = assertThat(extractSuccessValue<Int>())

  fun isLongSuccessThat(): LongSubject = assertThat(extractSuccessValue<Long>())

  fun isFloatSuccessThat(): FloatSubject = assertThat(extractSuccessValue<Float>())

  fun isDoubleSuccessThat(): DoubleSubject = assertThat(extractSuccessValue<Double>())

  fun isProtoSuccessThat(): LiteProtoSubject = assertThat(extractSuccessValue<MessageLite>())

  fun <E> isIterableSuccessThat(): IterableSubject = assertThat(extractSuccessValue<Iterable<E>>())

  fun <K, V> asMapSuccessThat(): MapSubject = assertThat(extractSuccessValue<Map<K, V>>())

  fun asThrowableSuccessThat(): ThrowableSubject = assertThat(extractSuccessValue<Throwable>())

  fun isFailureThat(): ThrowableSubject =
    assertThat(ensureActualIsType<AsyncResult.Failure<T>>().error)

  fun isNewerOrSameAgeAs(other: AsyncResult<T>) {
    assertThat(actual.isNewerThanOrSameAgeAs(other)).isTrue()
  }

  fun isOlderThan(other: AsyncResult<T>) {
    assertThat(actual.isNewerThanOrSameAgeAs(other)).isFalse()
  }

  @PublishedApi // See: https://stackoverflow.com/a/41905907/3689782.
  internal inline fun <reified T> extractSuccessValue(): T {
    return ensureActualIsType<AsyncResult.Success<T>>().value.also {
      assertThat(it).isInstanceOf(T::class.java)
    }
  }

  @PublishedApi
  internal inline fun <reified T> ensureActualIsType(): T {
    assertThat(actual).isInstanceOf(T::class.java)
    // This extra check is just to ensure Kotlin knows 'actual' is of type 'T'.
    check(actual is T) { "Error: Truth didn't correctly catch mis-typing." }
    return actual
  }

  companion object {
    fun <T> assertThat(actual: AsyncResult<T>): AsyncResultSubject<T> {
      return Truth.assertAbout(
        Factory<AsyncResultSubject<T>, AsyncResult<T>>(::AsyncResultSubject)
      ).that(actual)
    }
  }
}
