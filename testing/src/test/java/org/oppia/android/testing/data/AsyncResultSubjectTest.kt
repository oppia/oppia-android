package org.oppia.android.testing.data

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.util.data.AsyncResult
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowSystemClock

@RunWith(RobolectricTestRunner::class)
@Config(shadows = [ShadowSystemClock::class])
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
  fun testAsyncResultSubject_successResult_checkIsStringSuccessThat() {
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
  fun testAsyncResultSubject_failureResult_checkErrorMessageMatches() {
    AsyncResultSubject.assertThat(failureResult)
      .isFailureThat()
      .hasMessageThat()
      .contains("Error message")
  }

  @Test
  fun testAsyncResultSubject_twoSuccessResults_checkNewerOrSameAge() {
    val successResult1: AsyncResult<String> = AsyncResult.Success("First")
    val successResult2: AsyncResult<String> = AsyncResult.Success("Second")
    AsyncResultSubject.assertThat(successResult1).isNewerOrSameAgeAs(successResult2)
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
  fun testAsyncResultSubject_intSuccess_checkIsIntSuccessThat() {
    val intResult: AsyncResult<Int> = AsyncResult.Success(42)
    AsyncResultSubject.assertThat(intResult)
      .isIntSuccessThat()
      .isEqualTo(42)
  }

  @Test
  fun testAsyncResultSubject_booleanSuccess_checkIsBooleanSuccessThat() {
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
  fun testAsyncResultSubject_successResult_checkIsSuccessThat() {
    AsyncResultSubject.assertThat(successResult).isSuccessThat().isEqualTo("Some string")
  }

  @Test
  fun testAsyncResultSubject_failureResult_checkIsFailureThatError() {
    AsyncResultSubject.assertThat(failureResult)
      .isFailureThat()
      .hasMessageThat()
      .contains("Error message")
  }

  @Test
  fun testAsyncResultSubject_pendingResult_checkHasSameEffectiveValueAs() {
    val anotherPending: AsyncResult<String> = AsyncResult.Pending()
    AsyncResultSubject.assertThat(pendingResult).hasSameEffectiveValueAs(anotherPending)
  }

  @Test
  fun testAsyncResultSubject_pendingResult_checkHasDifferentEffectiveValue() {
    val successResult: AsyncResult<String> = AsyncResult.Success("Some string")
    AsyncResultSubject.assertThat(pendingResult)
      .hasSameEffectiveValueAs(successResult)
      .isFalse()
  }

  @Test
  fun testAsyncResultSubject_isComparableSuccessThat_checkIntValue() {
    val intResult: AsyncResult<Int> = AsyncResult.Success(100)
    AsyncResultSubject.assertThat(intResult)
      .isComparableSuccessThat<Int>()
      .isEqualTo(100)
  }

  @Test
  fun testAsyncResultSubject_successResult_checkStringSuccessValue() {
    AsyncResultSubject.assertThat(successResult)
      .isSuccessThat()
      .isEqualTo("Some string")
  }
}
