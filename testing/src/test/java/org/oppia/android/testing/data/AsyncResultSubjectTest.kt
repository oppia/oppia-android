package org.oppia.android.testing.data

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.google.protobuf.Empty
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.util.data.AsyncResult
import org.robolectric.annotation.Config
import java.io.FileNotFoundException

/** Tests for [AsyncResultSubject]. */
@RunWith(AndroidJUnit4::class)
@Config(manifest = Config.NONE)
class AsyncResultSubjectTest {
  private val pendingResult: AsyncResult<String> = AsyncResult.Pending()
  private val successResult: AsyncResult<String> = AsyncResult.Success("Some string")
  private val failureResult: AsyncResult<String> =
    AsyncResult.Failure(RuntimeException("Error message"))

  @Test
  fun testAsyncResultSubject_pendingResult_checkIsPending() {
    AsyncResultSubject.assertThat(pendingResult).isPending()
  }

  @Test
  fun testAsyncResultSubject_pendingResult_checkIsNotSuccess() {
    AsyncResultSubject.assertThat(pendingResult).isNotSuccess()
  }

  @Test
  fun testAsyncResultSubject_pendingResult_checkIsNotFailure() {
    AsyncResultSubject.assertThat(pendingResult).isNotFailure()
  }

  @Test
  fun testAsyncResultSubject_successResult_checkIsNotPending() {
    AsyncResultSubject.assertThat(successResult).isNotPending()
  }

  @Test
  fun testAsyncResultSubject_successResult_checkIsSuccess() {
    AsyncResultSubject.assertThat(successResult).isSuccess()
  }

  @Test
  fun testAsyncResultSubject_successResult_checkIsNotFailure() {
    AsyncResultSubject.assertThat(successResult).isNotFailure()
  }

  @Test
  fun testAsyncResultSubject_successResult_checkSuccessValueMatches() {
    AsyncResultSubject.assertThat(successResult).hasSuccessValueWhere {
      assertThat(this).isEqualTo("Some string")
    }
  }

  @Test
  fun testAsyncResultSubject_successResult_isStringSuccessEqualToSomeString() {
    AsyncResultSubject.assertThat(successResult)
      .isStringSuccessThat()
      .isEqualTo("Some string")
  }

  @Test
  fun testAsyncResultSubject_failureResult_checkIsFailure() {
    AsyncResultSubject.assertThat(failureResult).isFailure()
  }

  @Test
  fun testAsyncResultSubject_failureResult_checkIsNotSuccess() {
    AsyncResultSubject.assertThat(failureResult).isNotSuccess()
  }

  @Test
  fun testAsyncResultSubject_failureResult_checkIsNotPending() {
    AsyncResultSubject.assertThat(failureResult).isNotPending()
  }

  @Test
  fun testAsyncResultSubject_throwableSuccess_withValidThrowable_hasMessageContainingError() {
    val throwableResult: AsyncResult<Throwable> =
      AsyncResult.Success(RuntimeException("Error"))
    AsyncResultSubject.assertThat(throwableResult)
      .asThrowableSuccessThat()
      .hasMessageThat()
      .contains("Error")
  }

  @Test
  fun testAsyncResultSubject_failureResult_withCause_hasCauseMessageContainingCause() {
    val failureResult: AsyncResult<String> =
      AsyncResult.Failure(RuntimeException("Root error", FileNotFoundException("Cause")))
    AsyncResultSubject.assertThat(failureResult)
      .isFailureThat()
      .hasCauseThat()
      .hasMessageThat()
      .contains("Cause")
  }

  @Test
  fun testAsyncResultSubject_nullSuccessValue_withNullResult_isNull() {
    val nullSuccessResult: AsyncResult<String?> = AsyncResult.Success(null)
    AsyncResultSubject.assertThat(nullSuccessResult)
      .isSuccessThat()
      .isNull()
  }

  @Test
  fun testAsyncResultSubject_pendingAndSuccess_withDifferentStates_hasDifferentEffectiveValue() {
    val pendingResult: AsyncResult<String> = AsyncResult.Pending()
    val successResult: AsyncResult<String> = AsyncResult.Success("Value")
    AsyncResultSubject.assertThat(pendingResult)
      .hasSameEffectiveValueAs(successResult)
      .isFalse()
  }

  @Test
  fun testAsyncResultSubject_sameSuccessValue_withIdenticalValues_hasSameEffectiveValue() {
    val successResult1: AsyncResult<String> = AsyncResult.Success("Same value")
    val successResult2: AsyncResult<String> = AsyncResult.Success("Same value")
    AsyncResultSubject.assertThat(successResult1)
      .hasSameEffectiveValueAs(successResult2)
      .isTrue()
  }

  @Test
  fun testAsyncResultSubject_distinctSuccessValues_haveDifferentEffectiveValue() {
    val successResult1: AsyncResult<String> = AsyncResult.Success("First value")
    val successResult2: AsyncResult<String> = AsyncResult.Success("Second value")
    AsyncResultSubject.assertThat(successResult1)
      .hasSameEffectiveValueAs(successResult2)
      .isFalse()
  }

  @Test
  fun testAsyncResultSubject_intSuccess_withValue42_isEqualTo42() {
    val intResult: AsyncResult<Int> = AsyncResult.Success(42)
    AsyncResultSubject.assertThat(intResult)
      .isIntSuccessThat()
      .isEqualTo(42)
  }

  @Test
  fun testAsyncResultSubject_booleanSuccess_withTrueValue_isTrue() {
    val boolResult: AsyncResult<Boolean> = AsyncResult.Success(true)
    AsyncResultSubject.assertThat(boolResult)
      .isBooleanSuccessThat()
      .isTrue()
  }

  @Test
  fun testAsyncResultSubject_distinctFailureMessages_haveDifferentEffectiveValue() {
    val failureResult1: AsyncResult<String> = AsyncResult.Failure(RuntimeException("Error 1"))
    val failureResult2: AsyncResult<String> = AsyncResult.Failure(RuntimeException("Error 2"))
    AsyncResultSubject.assertThat(failureResult1)
      .hasSameEffectiveValueAs(failureResult2)
      .isFalse()
  }

  @Test
  fun testAsyncResultSubject_successResult_withStringValue_isSuccessWithSomeString() {
    AsyncResultSubject.assertThat(successResult).isSuccessThat().isEqualTo("Some string")
  }

  @Test
  fun testAsyncResultSubject_failureResult_withErrorMessage_isFailureWithMessageContainingError() {
    AsyncResultSubject.assertThat(failureResult)
      .isFailureThat()
      .hasMessageThat()
      .contains("Error message")
  }

  @Test
  fun testAsyncResultSubject_twoPendingResults_withSameState_haveSameEffectiveValue() {
    val anotherPending: AsyncResult<String> = AsyncResult.Pending()
    AsyncResultSubject.assertThat(pendingResult).hasSameEffectiveValueAs(anotherPending).isTrue()
  }

  @Test
  fun testAsyncResultSubject_failureResult_withExactErrorMessage_hasMessageEqualToErrorMessage() {
    AsyncResultSubject.assertThat(failureResult)
      .isFailureThat()
      .hasMessageThat()
      .isEqualTo("Error message")
  }

  @Test
  fun testAsyncResultSubject_longSuccessResult_withValidValue_hasLongValueEqualTo100() {
    val longResult: AsyncResult<Long> = AsyncResult.Success(100L)
    AsyncResultSubject.assertThat(longResult)
      .isLongSuccessThat()
      .isEqualTo(100L)
  }

  @Test
  fun testAsyncResultSubject_floatSuccessResult_withValidValue_hasFloatValueEqualTo3Point14() {
    val floatResult: AsyncResult<Float> = AsyncResult.Success(3.14f)
    AsyncResultSubject.assertThat(floatResult)
      .isFloatSuccessThat()
      .isEqualTo(3.14f)
  }

  @Test
  fun testAsyncResultSubject_doubleSuccessResult_withValidValue_hasDoubleValueEqualTo2Point718() {
    val doubleResult: AsyncResult<Double> = AsyncResult.Success(2.718)
    AsyncResultSubject.assertThat(doubleResult)
      .isDoubleSuccessThat()
      .isEqualTo(2.718)
  }

  @Test
  fun testAsyncResultSubject_iterableSuccessResult_withListOfStrings_hasSize3() {
    val iterableResult: AsyncResult<List<String>> = AsyncResult.Success(listOf("a", "b", "c"))
    AsyncResultSubject.assertThat(iterableResult)
      .isIterableSuccessThat<String>()
      .hasSize(3)
  }

  @Test
  fun testAsyncResultSubject_mapSuccessResult_withStringIntMap_hasEntryForKey() {
    val mapResult: AsyncResult<Map<String, Int>> = AsyncResult.Success(mapOf("key" to 42))
    AsyncResultSubject.assertThat(mapResult)
      .asMapSuccessThat<String, Int>()
      .containsEntry("key", 42)
  }

  @Test
  fun testAsyncResultSubject_extractWrongTypeFromSuccess_throwsAssertionErrorWithCorrectType() {
    val intResult: AsyncResult<Int> = AsyncResult.Success(42)
    val exception = assertThrows(AssertionError::class.java) {
      AsyncResultSubject.assertThat(intResult).isStringSuccessThat()
    }

    assertThat(exception).hasMessageThat().contains("java.lang.Integer")
  }

  @Test
  fun testAsyncResultSubject_iterableSuccessResult_withEmptyList_isEmpty() {
    val emptyIterableResult: AsyncResult<List<String>> = AsyncResult.Success(emptyList())
    AsyncResultSubject.assertThat(emptyIterableResult)
      .isIterableSuccessThat<String>()
      .isEmpty()
  }

  @Test
  fun testAsyncResultSubject_mapSuccessResult_withEmptyMap_isEmpty() {
    val emptyMapResult: AsyncResult<Map<String, Int>> = AsyncResult.Success(emptyMap())
    AsyncResultSubject.assertThat(emptyMapResult)
      .asMapSuccessThat<String, Int>()
      .isEmpty()
  }

  @Test
  fun testAsyncResultSubject_failureResult_withDifferentType_hasDifferentEffectiveValue() {
    val failureResult1: AsyncResult<String> = AsyncResult.Failure(RuntimeException("Error"))
    val failureResult2: AsyncResult<Int> = AsyncResult.Failure(RuntimeException("Error"))
    AsyncResultSubject.assertThat(failureResult1)
      .hasSameEffectiveValueAs(failureResult2)
      .isFalse()
  }

  @Test
  fun testAsyncResultSubject_successIntResult_isSuccessThat_isEqualToIntValue() {
    val intResult: AsyncResult<Int> = AsyncResult.Success(100)

    AsyncResultSubject.assertThat(intResult)
      .isSuccessThat()
      .isEqualTo(100)
  }

  @Test
  fun testAsyncResultSubject_intSuccessResult_isComparableSuccessThat_isGreaterThan99() {
    val intResult: AsyncResult<Int> = AsyncResult.Success(100)

    AsyncResultSubject.assertThat(intResult)
      .isComparableSuccessThat<Int>()
      .isGreaterThan(99)
  }

  @Test
  fun testAsyncResultSubject_stringSuccessResult_isComparableSuccessThat_isGreaterThanAbc() {
    val stringResult: AsyncResult<String> = AsyncResult.Success("abd")

    AsyncResultSubject.assertThat(stringResult)
      .isComparableSuccessThat<String>()
      .isGreaterThan("abc")
  }

  @Test
  fun testAsyncResultSubject_nonStringComparableExtraction_throwsAssertionErrorWithActualType() {
    val intResult: AsyncResult<Any> = AsyncResult.Success(100)

    val exception = assertThrows(AssertionError::class.java) {
      AsyncResultSubject.assertThat(intResult)
        .isComparableSuccessThat<String>()
    }

    assertThat(exception).hasMessageThat().contains("java.lang.Integer")
  }

  @Test
  fun testAsyncResultSubject_protoSuccessResult_isProtoSuccessThat_isEqualToExpectedProto() {
    val emptyProto = Empty.getDefaultInstance()
    val protoResult: AsyncResult<Empty> = AsyncResult.Success(emptyProto)

    AsyncResultSubject.assertThat(protoResult)
      .isProtoSuccessThat()
      .isEqualTo(emptyProto)
  }

  @Test
  fun testAsyncResultSubject_nonProtoSuccessResult_isProtoSuccessThat_throwsAssertionError() {
    val stringResult: AsyncResult<String> = AsyncResult.Success("value")

    val exception = assertThrows(AssertionError::class.java) {
      AsyncResultSubject.assertThat(stringResult)
        .isProtoSuccessThat()
    }

    assertThat(exception).hasMessageThat().contains("java.lang.String")
  }

  @Test
  fun testAsyncResultSubject_failureResult_withNestedCustomCause_hasRootCauseMessage() {
    val rootCause = IllegalStateException("Root cause")
    val wrappedCause = FileNotFoundException("Missing file").apply { initCause(rootCause) }
    val failureResult: AsyncResult<String> =
      AsyncResult.Failure(RuntimeException("Top error", wrappedCause))

    AsyncResultSubject.assertThat(failureResult)
      .isFailureThat()
      .hasCauseThat()
      .hasCauseThat()
      .hasMessageThat()
      .contains("Root cause")
  }

  @Test
  fun testAsyncResultSubject_newerResult_isNewerOrSameAgeAsOlderResult_passes() {
    val olderResult: AsyncResult<String> = AsyncResult.Success("value", resultTimeMillis = 1L)
    val newerResult: AsyncResult<String> = AsyncResult.Success("value", resultTimeMillis = 2L)

    AsyncResultSubject.assertThat(newerResult).isNewerOrSameAgeAs(olderResult)
  }

  @Test
  fun testAsyncResultSubject_olderResult_isNewerOrSameAgeAsNewerResult_throwsAssertionError() {
    val olderResult: AsyncResult<String> = AsyncResult.Success("value", resultTimeMillis = 1L)
    val newerResult: AsyncResult<String> = AsyncResult.Success("value", resultTimeMillis = 2L)

    assertThrows(AssertionError::class.java) {
      AsyncResultSubject.assertThat(olderResult).isNewerOrSameAgeAs(newerResult)
    }
  }

  @Test
  fun testAsyncResultSubject_olderResult_isOlderThanNewerResult_passes() {
    val olderResult: AsyncResult<String> = AsyncResult.Success("value", resultTimeMillis = 1L)
    val newerResult: AsyncResult<String> = AsyncResult.Success("value", resultTimeMillis = 2L)

    AsyncResultSubject.assertThat(olderResult).isOlderThan(newerResult)
  }

  @Test
  fun testAsyncResultSubject_newerResult_isOlderThanOlderResult_throwsAssertionError() {
    val olderResult: AsyncResult<String> = AsyncResult.Success("value", resultTimeMillis = 1L)
    val newerResult: AsyncResult<String> = AsyncResult.Success("value", resultTimeMillis = 2L)

    assertThrows(AssertionError::class.java) {
      AsyncResultSubject.assertThat(newerResult).isOlderThan(olderResult)
    }
  }

  @Test
  fun testAsyncResultSubject_pendingAndFailure_haveDifferentEffectiveValue() {
    val pendingResult: AsyncResult<String> = AsyncResult.Pending()
    val failureResult: AsyncResult<String> = AsyncResult.Failure(RuntimeException("Error"))

    AsyncResultSubject.assertThat(pendingResult)
      .hasSameEffectiveValueAs(failureResult)
      .isFalse()
  }

  @Test
  fun testAsyncResultSubject_nullAndNonNullSuccessValues_haveDifferentEffectiveValue() {
    val nullSuccessResult: AsyncResult<String?> = AsyncResult.Success(null)
    val nonNullSuccessResult: AsyncResult<String?> = AsyncResult.Success("value")

    AsyncResultSubject.assertThat(nullSuccessResult)
      .hasSameEffectiveValueAs(nonNullSuccessResult)
      .isFalse()
  }

  @Test
  fun testAsyncResultSubject_intSuccess_withIsBooleanSuccessThat_throwsAssertionError() {
    val intResult: AsyncResult<Int> = AsyncResult.Success(100)

    val exception = assertThrows(AssertionError::class.java) {
      AsyncResultSubject.assertThat(intResult).isBooleanSuccessThat()
    }

    assertThat(exception).hasMessageThat().contains("java.lang.Integer")
  }
}
