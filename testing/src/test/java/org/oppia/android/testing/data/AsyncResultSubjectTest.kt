package org.oppia.android.testing.data

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.oppia.android.util.data.AsyncResult

@RunWith(RobolectricTestRunner::class)
class AsyncResultSubjectTest {

  @Test
  fun testAsyncResultPending_isPending() {
    val pendingResult: AsyncResult<String> = AsyncResult.Pending()

    AsyncResultSubject.assertThat(pendingResult).isPending()
  }

  @Test
  fun testAsyncResultPending_isNotSuccess() {
    val pendingResult: AsyncResult<String> = AsyncResult.Pending()

    AsyncResultSubject.assertThat(pendingResult).isNotSuccess()
  }

  @Test
  fun testAsyncResultSuccess_isSuccess() {
    val successResult: AsyncResult<String> = AsyncResult.Success("Success value")

    AsyncResultSubject.assertThat(successResult).isSuccess()
  }

  @Test
  fun testAsyncResultSuccess_hasSuccessValueWhere_matchesExpected() {
    val successResult: AsyncResult<String> = AsyncResult.Success("Success value")

    AsyncResultSubject.assertThat(successResult).hasSuccessValueWhere {
      // Here we are verifying that the value is "Success value"
      assertThat(this).isEqualTo("Success value")
    }
  }

  @Test
  fun testAsyncResultSuccess_isStringSuccessThat() {
    val successResult: AsyncResult<String> = AsyncResult.Success("Success value")

    AsyncResultSubject.assertThat(successResult).isStringSuccessThat().isEqualTo("Success value")
  }

  @Test
  fun testAsyncResultFailure_isFailure() {
    val failureResult: AsyncResult<String> = AsyncResult.Failure(Throwable("Error"))

    AsyncResultSubject.assertThat(failureResult).isFailure()
  }

  @Test
  fun testAsyncResultFailure_isFailureThat_matchesExpectedError() {
    val failureResult: AsyncResult<String> = AsyncResult.Failure(Throwable("Error"))

    AsyncResultSubject.assertThat(failureResult).isFailureThat().hasMessageThat().contains("Error")
  }

  @Test
  fun testAsyncResultSuccess_isNewerOrSameAgeAs() {
    val successResult1: AsyncResult<String> = AsyncResult.Success("First")
    val successResult2: AsyncResult<String> = AsyncResult.Success("Second")

    AsyncResultSubject.assertThat(successResult1).isNewerOrSameAgeAs(successResult2)
  }

  @Test
  fun testAsyncResultFailure_hasSameEffectiveValueAs_differentError() {
    val failureResult1: AsyncResult<String> = AsyncResult.Failure(Throwable("Error"))
    val failureResult2: AsyncResult<String> = AsyncResult.Failure(Throwable("Different Error"))

    AsyncResultSubject.assertThat(failureResult1).hasSameEffectiveValueAs(failureResult2).isFalse()
  }
}
