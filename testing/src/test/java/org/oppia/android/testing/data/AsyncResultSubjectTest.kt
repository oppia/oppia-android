package org.oppia.android.testing.data

import com.google.common.truth.FailureMetadata
import com.google.common.truth.StringSubject
import com.google.common.truth.Subject
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.testing.data.AsyncResultSubject.Companion.assertThat
import org.oppia.android.util.data.AsyncResult
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import org.robolectric.junit4.RobolectricTestRunner

/** Tests for [AsyncResultSubject]. */
@RunWith(RobolectricTestRunner::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(manifest = Config.NONE)
class AsyncResultSubjectTest {

  @Test
  fun testIsPending_withPendingResult_succeeds() {
    val result = AsyncResult.Pending<String>()

    assertThat(result).isPending()
  }

  @Test
  fun testIsPending_withNonPendingResult_fails() {
    val result = AsyncResult.Success("value")

    val failure = assertThrows(AssertionError::class) { assertThat(result).isPending() }

    assertThat(failure).hasMessageThat().contains("expected to be an instance of")
  }

  @Test
  fun testIsNotPending_withPendingResult_fails() {
    val result = AsyncResult.Pending<String>()

    val failure = assertThrows(AssertionError::class) { assertThat(result).isNotPending() }

    assertThat(failure).hasMessageThat().contains("expected not to be an instance of")
  }

  @Test
  fun testIsNotPending_withNonPendingResult_succeeds() {
    val result = AsyncResult.Success("value")

    assertThat(result).isNotPending()
  }

  @Test
  fun testIsSuccess_withSuccessResult_succeeds() {
    val result = AsyncResult.Success("value")

    assertThat(result).isSuccess()
  }

  @Test
  fun testIsSuccess_withNonSuccessResult_fails() {
    val result = AsyncResult.Pending<String>()

    val failure = assertThrows(AssertionError::class) { assertThat(result).isSuccess() }

    assertThat(failure).hasMessageThat().contains("expected to be an instance of")
  }

  @Test
  fun testIsNotSuccess_withSuccessResult_fails() {
    val result = AsyncResult.Success("value")

    val failure = assertThrows(AssertionError::class) { assertThat(result).isNotSuccess() }

    assertThat(failure).hasMessageThat().contains("expected not to be an instance of")
  }

  @Test
  fun testIsNotSuccess_withNonSuccessResult_succeeds() {
    val result = AsyncResult.Pending<String>()

    assertThat(result).isNotSuccess()
  }

  @Test
  fun testIsFailure_withFailureResult_succeeds() {
    val result = AsyncResult.Failure<String>(RuntimeException())

    assertThat(result).isFailure()
  }

  @Test
  fun testIsFailure_withNonFailureResult_fails() {
    val result = AsyncResult.Pending<String>()

    val failure = assertThrows(AssertionError::class) { assertThat(result).isFailure() }

    assertThat(failure).hasMessageThat().contains("expected to be an instance of")
  }

  @Test
  fun testIsNotFailure_withFailureResult_fails() {
    val result = AsyncResult.Failure<String>(RuntimeException())

    val failure = assertThrows(AssertionError::class) { assertThat(result).isNotFailure() }

    assertThat(failure).hasMessageThat().contains("expected not to be an instance of")
  }

  @Test
  fun testIsNotFailure_withNonFailureResult_succeeds() {
    val result = AsyncResult.Pending<String>()

    assertThat(result).isNotFailure()
  }

  @Test
  fun testHasSuccessValueWhere_withSuccessResult_succeeds() {
    val result = AsyncResult.Success("value")

    assertThat(result).hasSuccessValueWhere { assertThat(this).isEqualTo("value") }
  }

  @Test
  fun testHasSuccessValueWhere_withNonSuccessResult_fails() {
    val result = AsyncResult.Pending<String>()

    val failure = assertThrows(AssertionError::class) {
      assertThat(result).hasSuccessValueWhere { assertThat(this).isEqualTo("value") }
    }

    assertThat(failure).hasMessageThat().contains("expected to be an instance of")
  }

  @Test
  fun testIsSuccessThat_withSuccessResult_succeeds() {
    val result = AsyncResult.Success("value")

    assertThat(result).isSuccessThat().isEqualTo("value")
  }

  @Test
  fun testIsSuccessThat_withNonSuccessResult_fails() {
    val result = AsyncResult.Pending<String>()

    val failure = assertThrows(AssertionError::class) { assertThat(result).isSuccessThat() }

    assertThat(failure).hasMessageThat().contains("expected to be an instance of")
  }

  @Test
  fun testIsStringSuccessThat_withStringSuccessResult_succeeds() {
    val result = AsyncResult.Success("value")

    assertThat(result).isStringSuccessThat().isEqualTo("value")
  }

  @Test
  fun testIsStringSuccessThat_withNonStringSuccessResult_fails() {
    val result = AsyncResult.Success(123)

    val failure = assertThrows(AssertionError::class) { assertThat(result).isStringSuccessThat() }

    assertThat(failure).hasMessageThat().contains("expected to be an instance of")
  }

  @Test
  fun testIsBooleanSuccessThat_withBooleanSuccessResult_succeeds() {
    val result = AsyncResult.Success(true)

    assertThat(result).isBooleanSuccessThat().isTrue()
  }

  @Test
  fun testIsBooleanSuccessThat_withNonBooleanSuccessResult_fails() {
    val result = AsyncResult.Success(123)

    val failure = assertThrows(AssertionError::class) { assertThat(result).isBooleanSuccessThat() }

    assertThat(failure).hasMessageThat().contains("expected to be an instance of")
  }

  @Test
  fun testIsIntSuccessThat_withIntSuccessResult_succeeds() {
    val result = AsyncResult.Success(123)

    assertThat(result).isIntSuccessThat().isEqualTo(123)
  }

  @Test
  fun testIsIntSuccessThat_withNonIntSuccessResult_fails() {
    val result = AsyncResult.Success("value")

    val failure = assertThrows(AssertionError::class) { assertThat(result).isIntSuccessThat() }

    assertThat(failure).hasMessageThat().contains("expected to be an instance of")
  }

  @Test
  fun testIsLongSuccessThat_withLongSuccessResult_succeeds() {
    val result = AsyncResult.Success(123L)

    assertThat(result).isLongSuccessThat().isEqualTo(123L)
  }

  @Test
  fun testIsLongSuccessThat_withNonLongSuccessResult_fails() {
    val result = AsyncResult.Success("value")

    val failure = assertThrows(AssertionError::class) { assertThat(result).isLongSuccessThat() }

    assertThat(failure).hasMessageThat().contains("expected to be an instance of")
  }

  @Test
  fun testIsFloatSuccessThat_withFloatSuccessResult_succeeds() {
    val result = AsyncResult.Success(123.0f)

    assertThat(result).isFloatSuccessThat().isEqualTo(123.0f)
  }

  @Test
  fun testIsFloatSuccessThat_withNonFloatSuccessResult_fails() {
    val result = AsyncResult.Success("value")

    val failure = assertThrows(AssertionError::class) { assertThat(result).isFloatSuccessThat() }

    assertThat(failure).hasMessageThat().contains("expected to be an instance of")
  }

  @Test
  fun testIsDoubleSuccessThat_withDoubleSuccessResult_succeeds() {
    val result = AsyncResult.Success(123.0)

    assertThat(result).isDoubleSuccessThat().isEqualTo(123.0)
  }

  @Test
  fun testIsDoubleSuccessThat_withNonDoubleSuccessResult_fails() {
    val result = AsyncResult.Success("value")

    val failure = assertThrows(AssertionError::class) { assertThat(result).isDoubleSuccessThat() }

    assertThat(failure).hasMessageThat().contains("expected to be an instance of")
  }

  @Test
  fun testIsProtoSuccessThat_withProtoSuccessResult_succeeds() {
    val result = AsyncResult.Success(TestProto.getDefaultInstance())

    assertThat(result).isProtoSuccessThat().isEqualTo(TestProto.getDefaultInstance())
  }

  @Test
  fun testIsProtoSuccessThat_withNonProtoSuccessResult_fails() {
    val result = AsyncResult.Success("value")

    val failure = assertThrows(AssertionError::class) { assertThat(result).isProtoSuccessThat() }

    assertThat(failure).hasMessageThat().contains("expected to be an instance of")
  }

  @Test
  fun testIsIterableSuccessThat_withIterableSuccessResult_succeeds() {
    val result = AsyncResult.Success(listOf("value"))

    assertThat(result).isIterableSuccessThat<String>().containsExactly("value")
  }

  @Test
  fun testIsIterableSuccessThat_withNonIterableSuccessResult_fails() {
    val result = AsyncResult.Success("value")

    val failure = assertThrows(AssertionError::class) { assertThat(result).isIterableSuccessThat<String>() }

    assertThat(failure).hasMessageThat().contains("expected to be an instance of")
  }

  @Test
  fun testAsMapSuccessThat_withMapSuccessResult_succeeds() {
    val result = AsyncResult.Success(mapOf("key" to "value"))

    assertThat(result).asMapSuccessThat<String, String>().containsExactly("key", "value")
  }

  @Test
  fun testAsMapSuccessThat_withNonMapSuccessResult_fails() {
    val result = AsyncResult.Success("value")

    val failure = assertThrows(AssertionError::class) { assertThat(result).asMapSuccessThat<String, String>() }

    assertThat(failure).hasMessageThat().contains("expected to be an instance of")
  }

  @Test
  fun testAsThrowableSuccessThat_withThrowableSuccessResult_succeeds() {
    val result = AsyncResult.Success(RuntimeException("error"))

    assertThat(result).asThrowableSuccessThat().hasMessageThat().contains("error")
  }

  @Test
  fun testAsThrowableSuccessThat_withNonThrowableSuccessResult_fails() {
    val result = AsyncResult.Success("value")

    val failure = assertThrows(AssertionError::class) { assertThat(result).asThrowableSuccessThat() }

    assertThat(failure).hasMessageThat().contains("expected to be an instance of")
  }

  @Test
  fun testIsFailureThat_withFailureResult_succeeds() {
    val result = AsyncResult.Failure<String>(RuntimeException("error"))

    assertThat(result).isFailureThat().hasMessageThat().contains("error")
  }

  @Test
  fun testIsFailureThat_withNonFailureResult_fails() {
    val result = AsyncResult.Success("value")

    val failure = assertThrows(AssertionError::class) { assertThat(result).isFailureThat() }

    assertThat(failure).hasMessageThat().contains("expected to be an instance of")
  }

  @Test
  fun testIsNewerOrSameAgeAs_withNewerResult_succeeds() {
    val result1 = AsyncResult.Success("value")
    Thread.sleep(10)
    val result2 = AsyncResult.Success("value")

    assertThat(result2).isNewerOrSameAgeAs(result1)
  }

  @Test
  fun testIsNewerOrSameAgeAs_withOlderResult_fails() {
    val result1 = AsyncResult.Success("value")
    Thread.sleep(10)
    val result2 = AsyncResult.Success("value")

    val failure = assertThrows(AssertionError::class) { assertThat(result1).isNewerOrSameAgeAs(result2) }

    assertThat(failure).hasMessageThat().contains("expected to be true")
  }

  @Test
  fun testIsOlderThan_withOlderResult_succeeds() {
    val result1 = AsyncResult.Success("value")
    Thread.sleep(10)
    val result2 = AsyncResult.Success("value")

    assertThat(result1).isOlderThan(result2)
  }

  @Test
  fun testIsOlderThan_withNewerResult_fails() {
    val result1 = AsyncResult.Success("value")
    Thread.sleep(10)
    val result2 = AsyncResult.Success("value")

    val failure = assertThrows(AssertionError::class) { assertThat(result2).isOlderThan(result1) }

    assertThat(failure).hasMessageThat().contains("expected to be false")
  }

  @Test
  fun testHasSameEffectiveValueAs_withSameEffectiveValue_succeeds() {
    val result1 = AsyncResult.Success("value")
    val result2 = AsyncResult.Success("value")

    assertThat(result1).hasSameEffectiveValueAs(result2)
  }

  @Test
  fun testHasSameEffectiveValueAs_withDifferentEffectiveValue_fails() {
    val result1 = AsyncResult.Success("value")
    val result2 = AsyncResult.Success("different value")

    val failure = assertThrows(AssertionError::class) { assertThat(result1).hasSameEffectiveValueAs(result2) }

    assertThat(failure).hasMessageThat().contains("expected to be true")
  }
}
