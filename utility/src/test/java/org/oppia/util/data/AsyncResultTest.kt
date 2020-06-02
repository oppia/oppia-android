package org.oppia.util.data

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.BindsInstance
import dagger.Component
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.test.assertFailsWith
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.testing.FakeSystemClock
import org.robolectric.annotation.LooperMode

/** Tests for [AsyncResult]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
class AsyncResultTest {

  @Inject
  lateinit var fakeSystemClock: FakeSystemClock

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  /* Pending tests. */

  @Test
  fun testPendingAsyncResult_isPending() {
    val result = AsyncResult.pending<String>()

    assertThat(result.isPending()).isTrue()
  }

  @Test
  fun testPendingAsyncResult_isNotSuccess() {
    val result = AsyncResult.pending<String>()

    assertThat(result.isSuccess()).isFalse()
  }

  @Test
  fun testPendingAsyncResult_isNotFailure() {
    val result = AsyncResult.pending<String>()

    assertThat(result.isFailure()).isFalse()
  }

  @Test
  fun testPendingAsyncResult_isNotCompleted() {
    val result = AsyncResult.pending<String>()

    assertThat(result.isCompleted()).isFalse()
  }

  @Test
  fun testPendingAsyncResult_getOrDefault_returnsDefault() {
    val result = AsyncResult.pending<String>()

    assertThat(result.getOrDefault("default")).isEqualTo("default")
  }

  @Test
  fun testPendingAsyncResult_getOrThrow_throwsIllegalStateExceptionDueToIncompletion() {
    val result = AsyncResult.pending<String>()

    assertFailsWith<IllegalStateException> { result.getOrThrow() }
  }

  @Test
  fun testPendingAsyncResult_getErrorOrNull_returnsNull() {
    val result = AsyncResult.pending<String>()

    assertThat(result.getErrorOrNull()).isNull()
  }

  @Test
  fun testPendingAsyncResult_transformed_isStillPending() {
    val original = AsyncResult.pending<String>()

    val transformed = original.transform { 0 }

    assertThat(transformed.isPending()).isTrue()
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testPendingAsyncResult_transformedAsync_isStillPending() = runBlockingTest {
    val original = AsyncResult.pending<String>()

    val transformed = original.transformAsync { AsyncResult.success(0) }

    assertThat(transformed.isPending()).isTrue()
  }

  @Test
  fun testPendingAsyncResult_combinedWithPending_isStillPending() {
    val result1 = AsyncResult.pending<String>()
    val result2 = AsyncResult.pending<Float>()

    val combined = result1.combineWith(result2) { _, _ -> 0 }

    assertThat(combined.isPending()).isTrue()
  }

  @Test
  fun testPendingAsyncResult_combinedWithFailure_isStillPending() {
    val result1 = AsyncResult.pending<String>()
    val result2 = AsyncResult.failed<Float>(RuntimeException())

    val combined = result1.combineWith(result2) { _, _ -> 0 }

    assertThat(combined.isPending()).isTrue()
  }

  @Test
  fun testPendingAsyncResult_combinedWithSuccess_isStillPending() {
    val result1 = AsyncResult.pending<String>()
    val result2 = AsyncResult.success(1.0f)

    val combined = result1.combineWith(result2) { _, _ -> 0 }

    assertThat(combined.isPending()).isTrue()
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testPendingAsyncResult_combinedAsyncWithPending_isStillPending() = runBlockingTest {
    val result1 = AsyncResult.pending<String>()
    val result2 = AsyncResult.pending<Float>()

    val combined = result1.combineWithAsync(result2) { _, _ -> AsyncResult.success(0) }

    assertThat(combined.isPending()).isTrue()
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testPendingAsyncResult_combinedAsyncWithFailure_isStillPending() = runBlockingTest {
    val result1 = AsyncResult.pending<String>()
    val result2 = AsyncResult.failed<Float>(RuntimeException())

    val combined = result1.combineWithAsync(result2) { _, _ -> AsyncResult.success(0) }

    assertThat(combined.isPending()).isTrue()
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testPendingAsyncResult_combinedAsyncWithSuccess_isStillPending() = runBlockingTest {
    val result1 = AsyncResult.pending<String>()
    val result2 = AsyncResult.success(1.0f)

    val combined = result1.combineWithAsync(result2) { _, _ -> AsyncResult.success(0) }

    assertThat(combined.isPending()).isTrue()
  }

  @Test
  fun testPendingResult_isEqualToAnotherPendingResult() {
    val result = AsyncResult.pending<String>()

    // Two pending results are the same regardless of their types.
    assertThat(result).isEqualTo(AsyncResult.pending<Int>())
  }

  @Test
  fun testPendingResult_isNotEqualToFailedResult() {
    val result = AsyncResult.pending<String>()

    assertThat(result).isNotEqualTo(AsyncResult.failed<String>(UnsupportedOperationException()))
  }

  @Test
  fun testPendingResult_isNotEqualToSucceededResult() {
    val result = AsyncResult.pending<String>()

    assertThat(result).isNotEqualTo(AsyncResult.success("Success"))
  }

  @Test
  fun testPendingResult_hashCode_isEqualToAnotherPendingResult() {
    val resultHash = AsyncResult.pending<String>().hashCode()

    // Two pending results are the same regardless of their types.
    assertThat(resultHash).isEqualTo(AsyncResult.pending<Int>().hashCode())
  }

  @Test
  fun testPendingResult_hashCode_isNotEqualToSucceededResult() {
    val resultHash = AsyncResult.pending<String>().hashCode()

    assertThat(resultHash).isNotEqualTo(AsyncResult.success("Success").hashCode())
  }

  @Test
  fun testPendingResult_hashCode_isNotEqualToFailedResult() {
    val resultHash = AsyncResult.pending<String>().hashCode()

    assertThat(resultHash).isNotEqualTo(
      AsyncResult.failed<String>(UnsupportedOperationException()).hashCode()
    )
  }

  @Test
  fun testPendingResult_comparedWithItself_isTheSameAge() {
    val result = AsyncResult.pending<String>()

    val areSameAge = result.isNewerThanOrSameAgeAs(result)

    assertThat(areSameAge).isTrue()
  }

  @Test
  fun testPendingResult_comparedWithOtherPendingResult_createdAtTheSameTime_areTheSameAge() {
    val result1 = AsyncResult.pending<String>()
    val result2 = AsyncResult.pending<String>()

    val areSameAge = result1.isNewerThanOrSameAgeAs(result2)

    assertThat(areSameAge).isTrue()
  }

  @Test
  fun testPendingResult_comparedWithSucceededResult_createdAtTheSameTime_areTheSameAge() {
    val pendingResult = AsyncResult.pending<String>()
    val success = AsyncResult.success("value")

    val areSameAge = pendingResult.isNewerThanOrSameAgeAs(success)

    assertThat(areSameAge).isTrue()
  }

  @Test
  fun testPendingResult_comparedWithFailedResult_createdAtTheSameTime_areTheSameAge() {
    val pendingResult = AsyncResult.pending<String>()
    val failure = AsyncResult.failed<Float>(RuntimeException())

    val areSameAge = pendingResult.isNewerThanOrSameAgeAs(failure)

    assertThat(areSameAge).isTrue()
  }

  @Test
  fun testPendingResult_comparedWithOlderPendingResult_isNewer() {
    val olderResult = AsyncResult.pending<String>()
    fakeSystemClock.advanceTime(millis = 10)
    val newerResult = AsyncResult.pending<String>()

    val isNewer = newerResult.isNewerThanOrSameAgeAs(olderResult)

    assertThat(isNewer).isTrue()
  }

  @Test
  fun testPendingResult_comparedWithNewerPendingResult_isNotNewer() {
    val olderResult = AsyncResult.pending<String>()
    fakeSystemClock.advanceTime(millis = 10)
    val newerResult = AsyncResult.pending<String>()

    val isNewer = olderResult.isNewerThanOrSameAgeAs(newerResult)

    assertThat(isNewer).isFalse()
  }

  /* Success tests. */

  @Test
  fun testSucceededAsyncResult_isNotPending() {
    val result = AsyncResult.success("value")

    assertThat(result.isPending()).isFalse()
  }

  @Test
  fun testSucceededAsyncResult_isSuccess() {
    val result = AsyncResult.success("value")

    assertThat(result.isSuccess()).isTrue()
  }

  @Test
  fun testSucceededAsyncResult_isNotFailure() {
    val result = AsyncResult.success("value")

    assertThat(result.isFailure()).isFalse()
  }

  @Test
  fun testSucceededAsyncResult_isCompleted() {
    val result = AsyncResult.success("value")

    assertThat(result.isCompleted()).isTrue()
  }

  @Test
  fun testSucceededAsyncResult_getOrDefault_returnsValue() {
    val result = AsyncResult.success("value")

    assertThat(result.getOrDefault("default")).isEqualTo("value")
  }

  @Test
  fun testSucceededAsyncResult_getOrThrow_returnsValue() {
    val result = AsyncResult.success("value")

    assertThat(result.getOrThrow()).isEqualTo("value")
  }

  @Test
  fun testSucceededAsyncResult_getErrorOrNull_returnsNull() {
    val result = AsyncResult.success("value")

    assertThat(result.getErrorOrNull()).isNull()
  }

  @Test
  fun testSucceededAsyncResult_transformed_hasTransformedValue() {
    val original = AsyncResult.success("value")

    val transformed = original.transform { 0 }

    assertThat(transformed.getOrThrow()).isEqualTo(0)
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testSucceededAsyncResult_transformedAsyncPending_isPending() = runBlockingTest {
    val original = AsyncResult.success("value")

    val transformed = original.transformAsync { AsyncResult.pending<Int>() }

    assertThat(transformed.isPending()).isTrue()
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testSucceededAsyncResult_transformedAsyncSuccess_hasTransformedValue() = runBlockingTest {
    val original = AsyncResult.success("value")

    val transformed = original.transformAsync { AsyncResult.success(0) }

    assertThat(transformed.getOrThrow()).isEqualTo(0)
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testSucceededAsyncResult_transformedAsyncFailed_isFailure() = runBlockingTest {
    val original = AsyncResult.success("value")

    val transformed = original.transformAsync {
      AsyncResult.failed<Int>(UnsupportedOperationException())
    }

    // Note that the failure is not chained since the transform function was responsible for 'throwing' it.
    assertThat(transformed.getErrorOrNull()).isInstanceOf(UnsupportedOperationException::class.java)
  }

  @Test
  fun testSucceededAsyncResult_combinedWithPending_isPending() {
    val result1 = AsyncResult.success("value")
    val result2 = AsyncResult.pending<Float>()

    val combined = result1.combineWith(result2) { _, _ -> 0 }

    assertThat(combined.isPending()).isTrue()
  }

  @Test
  fun testSucceededAsyncResult_combinedWithFailure_isFailedWithCorrectChainedFailure() {
    val result1 = AsyncResult.success("value")
    val result2 = AsyncResult.failed<Float>(RuntimeException())

    val combined = result1.combineWith(result2) { _, _ -> 0 }

    assertThat(combined.isFailure()).isTrue()
    assertThat(combined.getErrorOrNull()).isInstanceOf(
      AsyncResult.ChainedFailureException::class.java
    )
    assertThat(combined.getErrorOrNull()).hasCauseThat().isInstanceOf(RuntimeException::class.java)
  }

  @Test
  fun testSucceededAsyncResult_combinedWithSuccess_hasCombinedSuccessValue() {
    val result1 = AsyncResult.success("value")
    val result2 = AsyncResult.success(1.0)

    val combined = result1.combineWith(result2) { v1, v2 -> v1 + v2 }

    assertThat(combined.getOrThrow()).contains("value")
    assertThat(combined.getOrThrow()).contains("1.0")
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testSucceededAsyncResult_combinedAsyncWithPending_isPending() = runBlockingTest {
    val result1 = AsyncResult.success("value")
    val result2 = AsyncResult.pending<Float>()

    val combined = result1.combineWithAsync(result2) { _, _ -> AsyncResult.success(0) }

    assertThat(combined.isPending()).isTrue()
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testSucceededAsyncResult_combinedAsyncWithFailure_isFailedWithCorrectChainedFailure() =
    runBlockingTest {
      val result1 = AsyncResult.success("value")
      val result2 = AsyncResult.failed<Float>(RuntimeException())

      val combined = result1.combineWithAsync(result2) { _, _ -> AsyncResult.success(0) }

      assertThat(combined.isFailure()).isTrue()
      assertThat(combined.getErrorOrNull()).isInstanceOf(
        AsyncResult.ChainedFailureException::class.java
      )
      assertThat(combined.getErrorOrNull()).hasCauseThat()
        .isInstanceOf(RuntimeException::class.java)
    }

  @Test
  @ExperimentalCoroutinesApi
  fun testSucceededAsyncResult_combinedAsyncWithSuccess_resultPending_isPending() =
    runBlockingTest {
      val result1 = AsyncResult.success("value")
      val result2 = AsyncResult.success(1.0)

      val combined = result1.combineWithAsync(result2) { _, _ -> AsyncResult.pending<Int>() }

      assertThat(combined.isPending()).isTrue()
    }

  @Test
  @ExperimentalCoroutinesApi
  fun testSucceededAsyncResult_combinedAsyncWithSuccess_resultFailure_isFailed() =
    runBlockingTest {
      val result1 = AsyncResult.success("value")
      val result2 = AsyncResult.success(1.0)

      val combined = result1.combineWithAsync(
        result2
      ) { _, _ -> AsyncResult.failed<Int>(RuntimeException()) }

      // Note that the failure is not chained since the transform function was responsible for 'throwing' it.
      assertThat(combined.isFailure()).isTrue()
      assertThat(combined.getErrorOrNull()).isInstanceOf(RuntimeException::class.java)
    }

  @Test
  @ExperimentalCoroutinesApi
  fun testSucceededAsyncResult_combinedAsyncWithSuccess_resultSuccess_hasCombinedSuccessValue() =
    runBlockingTest {
      val result1 = AsyncResult.success("value")
      val result2 = AsyncResult.success(1.0)

      val combined = result1.combineWithAsync(result2) { v1, v2 -> AsyncResult.success(v1 + v2) }

      assertThat(combined.getOrThrow()).contains("value")
      assertThat(combined.getOrThrow()).contains("1.0")
    }

  @Test
  fun testSucceededResult_isNotEqualToPendingResult() {
    val result = AsyncResult.success("Success")

    assertThat(result).isNotEqualTo(AsyncResult.pending<Int>())
  }

  @Test
  fun testSucceededResult_isEqualToSameSucceededResult() {
    val result = AsyncResult.success("Success")

    assertThat(result).isEqualTo(AsyncResult.success("Success"))
  }

  @Test
  fun testSucceededResult_isNotEqualToDifferentSucceededResult() {
    val result = AsyncResult.success("Success")

    assertThat(result).isNotEqualTo(AsyncResult.success("Other value"))
  }

  @Test
  fun testSucceededResult_isNotEqualToDifferentTypedSucceededResult() {
    val result = AsyncResult.success("0")

    assertThat(result).isNotEqualTo(AsyncResult.success(0))
  }

  @Test
  fun testSucceededResult_isNotEqualToFailedResult() {
    val result = AsyncResult.success("Success")

    assertThat(result).isNotEqualTo(AsyncResult.failed<String>(UnsupportedOperationException()))
  }

  @Test
  fun testSucceededResult_hashCode_isNotEqualToPendingResult() {
    val resultHash = AsyncResult.success("Success").hashCode()

    // Two pending results are the same regardless of their types.
    assertThat(resultHash).isNotEqualTo(AsyncResult.pending<Int>().hashCode())
  }

  @Test
  fun testSucceededResult_hashCode_isEqualToSameSucceededResult() {
    val resultHash = AsyncResult.success("Success").hashCode()

    assertThat(resultHash).isEqualTo(AsyncResult.success("Success").hashCode())
  }

  @Test
  fun testSucceededResult_hashCode_isNotEqualToDifferentSucceededResult() {
    val resultHash = AsyncResult.success("Success").hashCode()

    assertThat(resultHash).isNotEqualTo(AsyncResult.success("Other value").hashCode())
  }

  @Test
  fun testSucceededResult_hashCode_isNotEqualToDifferentTypedSucceededResult() {
    val resultHash = AsyncResult.success("0").hashCode()

    assertThat(resultHash).isNotEqualTo(AsyncResult.success(0))
  }

  @Test
  fun testSucceededResult_hashCode_isNotEqualToFailedResult() {
    val resultHash = AsyncResult.success("Success").hashCode()

    assertThat(resultHash).isNotEqualTo(
      AsyncResult.failed<String>(
        UnsupportedOperationException()
      ).hashCode()
    )
  }

  @Test
  fun testSucceededResult_comparedWithItself_isTheSameAge() {
    val result = AsyncResult.success("value")

    val areSameAge = result.isNewerThanOrSameAgeAs(result)

    assertThat(areSameAge).isTrue()
  }

  @Test
  fun testSucceededResult_comparedWithPendingResult_createdAtTheSameTime_areTheSameAge() {
    val pendingResult = AsyncResult.pending<String>()
    val success = AsyncResult.success("value")

    val areSameAge = success.isNewerThanOrSameAgeAs(pendingResult)

    assertThat(areSameAge).isTrue()
  }

  @Test
  fun testSucceededResult_comparedWithOtherSucceededResult_createdAtTheSameTime_areTheSameAge() {
    val result1 = AsyncResult.success("value")
    val result2 = AsyncResult.success("value")

    val areSameAge = result1.isNewerThanOrSameAgeAs(result2)

    assertThat(areSameAge).isTrue()
  }

  @Test
  fun testSucceededResult_comparedWithFailedResult_createdAtTheSameTime_areTheSameAge() {
    val success = AsyncResult.success("value")
    val failure = AsyncResult.failed<Float>(RuntimeException())

    val areSameAge = success.isNewerThanOrSameAgeAs(failure)

    assertThat(areSameAge).isTrue()
  }

  @Test
  fun testSucceededResult_comparedWithOlderSucceededResult_isNewer() {
    val olderResult = AsyncResult.success("value")
    fakeSystemClock.advanceTime(millis = 10)
    val newerResult = AsyncResult.success("value")

    val isNewer = newerResult.isNewerThanOrSameAgeAs(olderResult)

    assertThat(isNewer).isTrue()
  }

  @Test
  fun testSucceededResult_comparedWithNewerSucceededResult_isNotNewer() {
    val olderResult = AsyncResult.success("value")
    fakeSystemClock.advanceTime(millis = 10)
    val newerResult = AsyncResult.success("value")

    val isNewer = olderResult.isNewerThanOrSameAgeAs(newerResult)

    assertThat(isNewer).isFalse()
  }

  /* Failure tests. */

  @Test
  fun testFailedAsyncResult_isNotPending() {
    val result = AsyncResult.failed<String>(UnsupportedOperationException())

    assertThat(result.isPending()).isFalse()
  }

  @Test
  fun testFailedAsyncResult_isNotSuccess() {
    val result = AsyncResult.failed<String>(UnsupportedOperationException())

    assertThat(result.isSuccess()).isFalse()
  }

  @Test
  fun testFailedAsyncResult_isFailure() {
    val result = AsyncResult.failed<String>(UnsupportedOperationException())

    assertThat(result.isFailure()).isTrue()
  }

  @Test
  fun testFailedAsyncResult_isCompleted() {
    val result = AsyncResult.failed<String>(UnsupportedOperationException())

    assertThat(result.isCompleted()).isTrue()
  }

  @Test
  fun testFailedAsyncResult_getOrDefault_returnsDefault() {
    val result = AsyncResult.failed<String>(UnsupportedOperationException())

    assertThat(result.getOrDefault("default")).isEqualTo("default")
  }

  @Test
  fun testFailedAsyncResult_getOrThrow_throwsFailureException() {
    val result = AsyncResult.failed<String>(UnsupportedOperationException())

    assertFailsWith<UnsupportedOperationException> { result.getOrThrow() }
  }

  @Test
  fun testFailedAsyncResult_getErrorOrNull_returnsFailureException() {
    val result = AsyncResult.failed<String>(UnsupportedOperationException())

    assertThat(result.getErrorOrNull()).isInstanceOf(UnsupportedOperationException::class.java)
  }

  @Test
  fun testFailedAsyncResult_transformed_throwsChainedFailureException_withCorrectRootCause() {
    val result = AsyncResult.failed<String>(UnsupportedOperationException())

    val transformed = result.transform { 0 }

    assertThat(transformed.getErrorOrNull()).isInstanceOf(
      AsyncResult.ChainedFailureException::class.java
    )
    assertThat(transformed.getErrorOrNull()).hasCauseThat().isInstanceOf(
      UnsupportedOperationException::class.java
    )
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testFailedAsyncResult_transformedAsync_throwsChainedFailureException_withCorrectRootCause() =
    runBlockingTest {
      val result = AsyncResult.failed<String>(UnsupportedOperationException())

      val transformed = result.transformAsync { AsyncResult.success(0) }

      assertThat(transformed.getErrorOrNull()).isInstanceOf(
        AsyncResult.ChainedFailureException::class.java
      )
      assertThat(transformed.getErrorOrNull()).hasCauseThat().isInstanceOf(
        UnsupportedOperationException::class.java
      )
    }

  @Test
  fun testFailedAsyncResult_combinedWithPending_isStillChainedFailure() {
    val result1 = AsyncResult.failed<String>(UnsupportedOperationException())
    val result2 = AsyncResult.pending<Float>()

    val combined = result1.combineWith(result2) { _, _ -> 0 }

    assertThat(combined.getErrorOrNull()).isInstanceOf(
      AsyncResult.ChainedFailureException::class.java
    )
    assertThat(combined.getErrorOrNull()).hasCauseThat().isInstanceOf(
      UnsupportedOperationException::class.java
    )
  }

  @Test
  fun testFailedAsyncResult_combinedWithFailure_hasFirstFailureChained() {
    val result1 = AsyncResult.failed<String>(UnsupportedOperationException())
    val result2 = AsyncResult.failed<Float>(RuntimeException())

    val combined = result1.combineWith(result2) { _, _ -> 0 }

    assertThat(combined.getErrorOrNull()).isInstanceOf(
      AsyncResult.ChainedFailureException::class.java
    )
    assertThat(combined.getErrorOrNull()).hasCauseThat().isInstanceOf(
      UnsupportedOperationException::class.java
    )
  }

  @Test
  fun testFailedAsyncResult_combinedWithSuccess_isStillChainedFailure() {
    val result1 = AsyncResult.failed<String>(UnsupportedOperationException())
    val result2 = AsyncResult.success(1.0f)

    val combined = result1.combineWith(result2) { _, _ -> 0 }

    assertThat(combined.getErrorOrNull()).isInstanceOf(
      AsyncResult.ChainedFailureException::class.java
    )
    assertThat(combined.getErrorOrNull()).hasCauseThat().isInstanceOf(
      UnsupportedOperationException::class.java
    )
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testFailedAsyncResult_combinedAsyncWithPending_isStillChainedFailure() = runBlockingTest {
    val result1 = AsyncResult.failed<String>(UnsupportedOperationException())
    val result2 = AsyncResult.pending<Float>()

    val combined = result1.combineWithAsync(result2) { _, _ -> AsyncResult.success(0) }

    assertThat(combined.getErrorOrNull()).isInstanceOf(
      AsyncResult.ChainedFailureException::class.java)
    assertThat(combined.getErrorOrNull()).hasCauseThat()
      .isInstanceOf(UnsupportedOperationException::class.java)
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testFailedAsyncResult_combinedAsyncWithFailure_isStillChainedFailure() = runBlockingTest {
    val result1 = AsyncResult.failed<String>(UnsupportedOperationException())
    val result2 = AsyncResult.failed<Float>(RuntimeException())

    val combined = result1.combineWithAsync(result2) { _, _ -> AsyncResult.success(0) }

    assertThat(combined.getErrorOrNull()).isInstanceOf(
      AsyncResult.ChainedFailureException::class.java)
    assertThat(combined.getErrorOrNull()).hasCauseThat()
      .isInstanceOf(UnsupportedOperationException::class.java)
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testFailedAsyncResult_combinedAsyncWithSuccess_isStillChainedFailure() = runBlockingTest {
    val result1 = AsyncResult.failed<String>(UnsupportedOperationException())
    val result2 = AsyncResult.success(1.0f)

    val combined = result1.combineWithAsync(result2) { _, _ -> AsyncResult.success(0) }

    assertThat(combined.getErrorOrNull()).isInstanceOf(
      AsyncResult.ChainedFailureException::class.java)
    assertThat(combined.getErrorOrNull()).hasCauseThat()
      .isInstanceOf(UnsupportedOperationException::class.java)
  }

  @Test
  fun testFailedResult_isNotEqualToPendingResult() {
    val result = AsyncResult.failed<String>(UnsupportedOperationException("Reason"))

    assertThat(result).isNotEqualTo(AsyncResult.pending<Int>())
  }

  @Test
  fun testFailedResult_isNotEqualToSucceededResult() {
    val result = AsyncResult.failed<String>(UnsupportedOperationException("Reason"))

    assertThat(result).isNotEqualTo(AsyncResult.success("Success"))
  }

  @Test
  fun testFailedResult_isEqualToFailedResultWithSameExceptionObject() {
    val failure = UnsupportedOperationException("Reason")

    val result = AsyncResult.failed<String>(failure)

    assertThat(result).isEqualTo(AsyncResult.failed<String>(failure))
  }

  @Test
  fun testFailedResult_isNotEqualToFailedResultWithDifferentInstanceOfSameExceptionType() {
    val result = AsyncResult.failed<String>(UnsupportedOperationException("Reason"))

    // Different exceptions have different stack traces, so they can't be equal despite similar constructions.
    assertThat(result).isNotEqualTo(AsyncResult.failed<String>(UnsupportedOperationException("Reason")))
  }

  @Test
  fun testFailedResult_hashCode_isNotEqualToPendingResult() {
    val resultHash = AsyncResult.failed<String>(UnsupportedOperationException("Reason")).hashCode()

    // Two pending results are the same regardless of their types.
    assertThat(resultHash).isNotEqualTo(AsyncResult.pending<Int>().hashCode())
  }

  @Test
  fun testFailedResult_hashCode_isNotEqualToSucceededResult() {
    val resultHash = AsyncResult.failed<String>(UnsupportedOperationException("Reason")).hashCode()

    assertThat(resultHash).isNotEqualTo(AsyncResult.success("Success").hashCode())
  }

  @Test
  fun testFailedResult_hashCode_isEqualToFailedResultWithSameExceptionObject() {
    val failure = UnsupportedOperationException("Reason")

    val resultHash = AsyncResult.failed<String>(failure).hashCode()

    assertThat(resultHash).isEqualTo(AsyncResult.failed<String>(failure).hashCode())
  }

  @Test
  fun testFailedResult_hashCode_isNotEqualToFailedResultWithDifferentInstanceOfSameExceptionType() {
    val resultHash = AsyncResult.failed<String>(UnsupportedOperationException("Reason")).hashCode()

    // Different exceptions have different stack traces, so they can't be equal despite similar constructions.
    assertThat(resultHash).isNotEqualTo(
      AsyncResult.failed<String>(UnsupportedOperationException("Reason")).hashCode()
    )
  }

  @Test
  fun testFailedResult_comparedWithItself_isTheSameAge() {
    val result = AsyncResult.failed<Float>(RuntimeException())

    val areSameAge = result.isNewerThanOrSameAgeAs(result)

    assertThat(areSameAge).isTrue()
  }

  @Test
  fun testFailedResult_comparedWithPendingResult_createdAtTheSameTime_areTheSameAge() {
    val failure = AsyncResult.failed<Float>(RuntimeException())
    val pendingResult = AsyncResult.pending<String>()

    val areSameAge = failure.isNewerThanOrSameAgeAs(pendingResult)

    assertThat(areSameAge).isTrue()
  }

  @Test
  fun testFailedResult_comparedWithSucceededResult_createdAtTheSameTime_areTheSameAge() {
    val failure = AsyncResult.failed<Float>(RuntimeException())
    val success = AsyncResult.success("value")

    val areSameAge = failure.isNewerThanOrSameAgeAs(success)

    assertThat(areSameAge).isTrue()
  }

  @Test
  fun testFailedResult_comparedWithOtherFailedResult_createdAtTheSameTime_areTheSameAge() {
    val result1 = AsyncResult.failed<Float>(RuntimeException())
    val result2 = AsyncResult.failed<Float>(RuntimeException())

    val areSameAge = result1.isNewerThanOrSameAgeAs(result2)

    assertThat(areSameAge).isTrue()
  }

  @Test
  fun testFailedResult_comparedWithOlderFailedResult_isNewer() {
    val olderResult = AsyncResult.failed<Float>(RuntimeException())
    fakeSystemClock.advanceTime(millis = 10)
    val newerResult = AsyncResult.failed<Float>(RuntimeException())

    val isNewer = newerResult.isNewerThanOrSameAgeAs(olderResult)

    assertThat(isNewer).isTrue()
  }

  @Test
  fun testFailedResult_comparedWithNewerFailedResult_isNotNewer() {
    val olderResult = AsyncResult.failed<Float>(RuntimeException())
    fakeSystemClock.advanceTime(millis = 10)
    val newerResult = AsyncResult.failed<Float>(RuntimeException())

    val isNewer = olderResult.isNewerThanOrSameAgeAs(newerResult)

    assertThat(isNewer).isFalse()
  }

  private fun setUpTestApplicationComponent() {
    DaggerAsyncResultTest_TestApplicationComponent.builder()
      .setApplication(ApplicationProvider.getApplicationContext())
      .build()
      .inject(this)
  }

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(modules = [])
  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder
      fun build(): TestApplicationComponent
    }

    fun inject(asyncResultTest: AsyncResultTest)
  }
}
