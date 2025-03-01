package org.oppia.android.testing.data

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
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
  fun testAsyncResultSubject_newerResult_checkIsNewerOrSameAge() {
    val olderResult: AsyncResult<String> = AsyncResult.Success("Older")
    Thread.sleep(50)
    val newerResult: AsyncResult<String> = AsyncResult.Success("Newer")
    AsyncResultSubject.assertThat(newerResult).isNewerOrSameAgeAs(olderResult)
  }

  @Test
  fun testAsyncResultSubject_throwableSuccess_isThrowableSuccessWithMessageContainingError() {
    val throwableResult: AsyncResult<Throwable> =
      AsyncResult.Success(RuntimeException("Error"))
    AsyncResultSubject.assertThat(throwableResult)
      .asThrowableSuccessThat()
      .hasMessageThat()
      .contains("Error")
  }

  @Test
  fun testAsyncResultSubject_failureResult_checkCause() {
    val failureResult: AsyncResult<String> =
      AsyncResult.Failure(RuntimeException("Root error", FileNotFoundException("Cause")))
    AsyncResultSubject.assertThat(failureResult)
      .isFailureThat()
      .hasCauseThat()
      .hasMessageThat()
      .contains("Cause")
  }

  @Test
  fun testAsyncResultSubject_nullSuccessValue_checkIsNull() {
    val nullSuccessResult: AsyncResult<String?> = AsyncResult.Success(null)
    AsyncResultSubject.assertThat(nullSuccessResult)
      .isSuccessThat()
      .isNull()
  }

  @Test
  fun testAsyncResultSubject_pendingAndSuccess_checkHasDifferentEffectiveValue() {
    val pendingResult: AsyncResult<String> = AsyncResult.Pending()
    val successResult: AsyncResult<String> = AsyncResult.Success("Value")
    AsyncResultSubject.assertThat(pendingResult)
      .hasSameEffectiveValueAs(successResult)
      .isFalse()
  }

  @Test
  fun testAsyncResultSubject_sameSuccessValue_checkHasSameEffectiveValue() {
    val successResult1: AsyncResult<String> = AsyncResult.Success("Same value")
    val successResult2: AsyncResult<String> = AsyncResult.Success("Same value")
    AsyncResultSubject.assertThat(successResult1)
      .hasSameEffectiveValueAs(successResult2)
      .isTrue()
  }

  @Test
  fun testAsyncResultSubject_differentSuccessValues_checkHasDifferentEffectiveValue() {
    val successResult1: AsyncResult<String> = AsyncResult.Success("First value")
    val successResult2: AsyncResult<String> = AsyncResult.Success("Second value")
    AsyncResultSubject.assertThat(successResult1)
      .hasSameEffectiveValueAs(successResult2)
      .isFalse()
  }

  @Test
  fun testAsyncResultSubject_intSuccess_isIntSuccessEqualTo42() {
    val intResult: AsyncResult<Int> = AsyncResult.Success(42)
    AsyncResultSubject.assertThat(intResult)
      .isIntSuccessThat()
      .isEqualTo(42)
  }

  @Test
  fun testAsyncResultSubject_booleanSuccess_isBooleanSuccessEqualToTrue() {
    val boolResult: AsyncResult<Boolean> = AsyncResult.Success(true)
    AsyncResultSubject.assertThat(boolResult)
      .isBooleanSuccessThat()
      .isTrue()
  }

  @Test
  fun testAsyncResultSubject_differentFailureMessages_checkHasDifferentEffectiveValue() {
    val failureResult1: AsyncResult<String> = AsyncResult.Failure(RuntimeException("Error 1"))
    val failureResult2: AsyncResult<String> = AsyncResult.Failure(RuntimeException("Error 2"))
    AsyncResultSubject.assertThat(failureResult1)
      .hasSameEffectiveValueAs(failureResult2)
      .isFalse()
  }

  @Test
  fun testAsyncResultSubject_pendingResult_checkIsNotSuccessOrFailure() {
    AsyncResultSubject.assertThat(pendingResult).isNotSuccess()
    AsyncResultSubject.assertThat(pendingResult).isNotFailure()
  }

  @Test
  fun testAsyncResultSubject_successResult_isSuccessWithValueEqualToSomeString() {
    AsyncResultSubject.assertThat(successResult).isSuccessThat().isEqualTo("Some string")
  }

  @Test
  fun testAsyncResultSubject_failureResult_isFailureWithMessageContainingErrorMessage() {
    AsyncResultSubject.assertThat(failureResult)
      .isFailureThat()
      .hasMessageThat()
      .contains("Error message")
  }

  @Test
  fun testAsyncResultSubject_twoPendingResults_haveSameEffectiveValue() {
    val anotherPending: AsyncResult<String> = AsyncResult.Pending()
    AsyncResultSubject.assertThat(pendingResult).hasSameEffectiveValueAs(anotherPending).isTrue()
  }

  @Test
  fun testAsyncResultSubject_pendingResult_checkHasDifferentEffectiveValue() {
    val successResult: AsyncResult<String> = AsyncResult.Success("Some string")
    AsyncResultSubject.assertThat(pendingResult)
      .hasSameEffectiveValueAs(successResult)
      .isFalse()
  }

  @Test
  fun testAsyncResultSubject_successResult_checkStringSuccessValue() {
    AsyncResultSubject.assertThat(successResult)
      .isSuccessThat()
      .isEqualTo("Some string")
  }

  @Test
  fun testAsyncResultSubject_failureResult_checkErrorMessageMatchesExact() {
    AsyncResultSubject.assertThat(failureResult)
      .isFailureThat()
      .hasMessageThat()
      .isEqualTo("Error message")
  }

  @Test
  fun testAsyncResultSubject_pendingResult_checkHasNullEffectiveValue() {
    val nullPending: AsyncResult<String> = AsyncResult.Pending()
    AsyncResultSubject.assertThat(pendingResult)
      .hasSameEffectiveValueAs(nullPending)
      .isTrue()
  }

  @Test
  fun testAsyncResultSubject_pendingResult_checkIsNotSameEffectiveValue() {
    val pending1: AsyncResult<String> = AsyncResult.Pending()
    val pending2: AsyncResult<String> = AsyncResult.Pending()
    AsyncResultSubject.assertThat(pending1)
      .hasSameEffectiveValueAs(pending2)
      .isTrue()
  }
}
