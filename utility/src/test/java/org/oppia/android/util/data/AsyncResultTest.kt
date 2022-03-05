package org.oppia.android.util.data

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.BindsInstance
import dagger.Component
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.BackgroundTestDispatcher
import org.oppia.android.testing.threading.TestCoroutineDispatcher
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.testing.time.FakeSystemClock
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton
import org.oppia.android.testing.data.AsyncResultSubject.Companion.assertThat
import org.oppia.android.util.data.AsyncResult.ChainedFailureException

/** Tests for [AsyncResult]. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
class AsyncResultTest {

  @Inject
  lateinit var fakeSystemClock: FakeSystemClock

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @Inject
  @field:BackgroundTestDispatcher
  lateinit var backgroundTestDispatcher: TestCoroutineDispatcher

  private val backgroundTestDispatcherScope by lazy { CoroutineScope(backgroundTestDispatcher) }

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  /* Pending tests. */

  @Test
  fun testPendingAsyncResult_transformed_isStillPending() {
    val original = AsyncResult.Pending<String>()

    val transformed = original.transform { 0 }

    assertThat(transformed).isInstanceOf(AsyncResult.Pending::class.java)
  }

  @Test
  fun testPendingAsyncResult_transformedAsync_isStillPending() {
    val original = AsyncResult.Pending<String>()

    val transformed = original.blockingTransformAsync { AsyncResult.Success(0) }

    assertThat(transformed).isInstanceOf(AsyncResult.Pending::class.java)
  }

  @Test
  fun testPendingAsyncResult_combinedWithPending_isStillPending() {
    val result1 = AsyncResult.Pending<String>()
    val result2 = AsyncResult.Pending<String>()

    val combined = result1.combineWith(result2) { _, _ -> 0 }

    assertThat(combined).isInstanceOf(AsyncResult.Pending::class.java)
  }

  @Test
  fun testPendingAsyncResult_combinedWithFailure_isStillPending() {
    val result1 = AsyncResult.Pending<String>()
    val result2 = AsyncResult.Failure<String>(RuntimeException())

    val combined = result1.combineWith(result2) { _, _ -> 0 }

    assertThat(combined).isInstanceOf(AsyncResult.Pending::class.java)
  }

  @Test
  fun testPendingAsyncResult_combinedWithSuccess_isStillPending() {
    val result1 = AsyncResult.Pending<String>()
    val result2 = AsyncResult.Success(1.0f)

    val combined = result1.combineWith(result2) { _, _ -> 0 }

    assertThat(combined).isInstanceOf(AsyncResult.Pending::class.java)
  }

  @Test
  fun testPendingAsyncResult_combinedAsyncWithPending_isStillPending() {
    val result1 = AsyncResult.Pending<String>()
    val result2 = AsyncResult.Pending<String>()

    val combined = result1.blockingCombineWithAsync(result2) { _, _ -> AsyncResult.Success(0) }

    assertThat(combined).isInstanceOf(AsyncResult.Pending::class.java)
  }

  @Test
  fun testPendingAsyncResult_combinedAsyncWithFailure_isStillPending() {
    val result1 = AsyncResult.Pending<String>()
    val result2 = AsyncResult.Failure<String>(RuntimeException())

    val combined = result1.blockingCombineWithAsync(result2) { _, _ -> AsyncResult.Success(0) }

    assertThat(combined).isInstanceOf(AsyncResult.Pending::class.java)
  }

  @Test
  fun testPendingAsyncResult_combinedAsyncWithSuccess_isStillPending() {
    val result1 = AsyncResult.Pending<String>()
    val result2 = AsyncResult.Success(1.0f)

    val combined = result1.blockingCombineWithAsync(result2) { _, _ -> AsyncResult.Success(0) }

    assertThat(combined).isInstanceOf(AsyncResult.Pending::class.java)
  }

  @Test
  fun testPendingResult_isEqualToAnotherPendingResult() {
    val result = AsyncResult.Pending<String>()

    // Two pending results are the same regardless of their types.
    assertThat(result).isEqualTo(AsyncResult.Pending<String>())
  }

  @Test
  fun testPendingResult_isNotEqualToFailedResult() {
    val result = AsyncResult.Pending<String>()

    assertThat(result).isNotEqualTo(AsyncResult.Failure<String>(UnsupportedOperationException()))
  }

  @Test
  fun testPendingResult_isNotEqualToSucceededResult() {
    val result = AsyncResult.Pending<String>()

    assertThat(result).isNotEqualTo(AsyncResult.Success("Success"))
  }

  @Test
  fun testPendingResult_hashCode_isEqualToAnotherPendingResult() {
    val resultHash = AsyncResult.Pending<String>().hashCode()

    // Two pending results are the same regardless of their types.
    assertThat(resultHash).isEqualTo(AsyncResult.Pending<String>().hashCode())
  }

  @Test
  fun testPendingResult_hashCode_isNotEqualToSucceededResult() {
    val resultHash = AsyncResult.Pending<String>().hashCode()

    assertThat(resultHash).isNotEqualTo(AsyncResult.Success("Success").hashCode())
  }

  @Test
  fun testPendingResult_hashCode_isNotEqualToFailedResult() {
    val resultHash = AsyncResult.Pending<String>().hashCode()

    assertThat(resultHash).isNotEqualTo(
      AsyncResult.Failure<String>(UnsupportedOperationException()
      ).hashCode()
    )
  }

  @Test
  fun testPendingResult_comparedWithItself_isTheSameAge() {
    val result = AsyncResult.Pending<String>()

    val areSameAge = result.isNewerThanOrSameAgeAs(result)

    assertThat(areSameAge).isTrue()
  }

  @Test
  fun testPendingResult_comparedWithOtherPendingResult_createdAtTheSameTime_areTheSameAge() {
    val result1 = AsyncResult.Pending<String>()
    val result2 = AsyncResult.Pending<String>()

    val areSameAge = result1.isNewerThanOrSameAgeAs(result2)

    assertThat(areSameAge).isTrue()
  }

  @Test
  fun testPendingResult_comparedWithSucceededResult_createdAtTheSameTime_areTheSameAge() {
    val pendingResult = AsyncResult.Pending<String>()
    val success = AsyncResult.Success("value")

    val areSameAge = pendingResult.isNewerThanOrSameAgeAs(success)

    assertThat(areSameAge).isTrue()
  }

  @Test
  fun testPendingResult_comparedWithFailedResult_createdAtTheSameTime_areTheSameAge() {
    val pendingResult = AsyncResult.Pending<String>()
    val failure = AsyncResult.Failure<String>(RuntimeException())

    val areSameAge = pendingResult.isNewerThanOrSameAgeAs(failure)

    assertThat(areSameAge).isTrue()
  }

  @Test
  fun testPendingResult_comparedWithOlderPendingResult_isNewer() {
    val olderResult = AsyncResult.Pending<String>()
    fakeSystemClock.advanceTime(millis = 10)
    val newerResult = AsyncResult.Pending<String>()

    val isNewer = newerResult.isNewerThanOrSameAgeAs(olderResult)

    assertThat(isNewer).isTrue()
  }

  @Test
  fun testPendingResult_comparedWithNewerPendingResult_isNotNewer() {
    val olderResult = AsyncResult.Pending<String>()
    fakeSystemClock.advanceTime(millis = 10)
    val newerResult = AsyncResult.Pending<String>()

    val isNewer = olderResult.isNewerThanOrSameAgeAs(newerResult)

    assertThat(isNewer).isFalse()
  }

  /* Success tests. */

  @Test
  fun testSucceededAsyncResult_hasCorrectValue() {
    val result = AsyncResult.Success("value")

    assertThat(result.value).isEqualTo("value")
  }

  @Test
  fun testSucceededAsyncResult_transformed_hasTransformedValue() {
    val original = AsyncResult.Success("value")

    val transformed = original.transform { 0 }

    assertThat(transformed).isIntSuccessThat().isEqualTo(0)
  }

  @Test
  fun testSucceededAsyncResult_transformedAsyncPending_isPending() {
    val original = AsyncResult.Success("value")

    val transformed = original.blockingTransformAsync { AsyncResult.Pending<String>() }

    assertThat(transformed).isInstanceOf(AsyncResult.Pending::class.java)
  }

  @Test
  fun testSucceededAsyncResult_transformedAsyncSuccess_hasTransformedValue() {
    val original = AsyncResult.Success("value")

    val transformed = original.blockingTransformAsync { AsyncResult.Success(0) }

    assertThat(transformed).isIntSuccessThat().isEqualTo(0)
  }

  @Test
  fun testSucceededAsyncResult_transformedAsyncFailed_isFailure() {
    val original = AsyncResult.Success("value")

    val transformed = original.blockingTransformAsync {
      AsyncResult.Failure<String>(UnsupportedOperationException())
    }

    // Note that the failure is not chained since the transform function was responsible for
    // 'throwing' it.
    assertThat(transformed).isFailureThat().isInstanceOf(UnsupportedOperationException::class.java)
  }

  @Test
  fun testSucceededAsyncResult_combinedWithPending_isPending() {
    val result1 = AsyncResult.Success("value")
    val result2 = AsyncResult.Pending<String>()

    val combined = result1.combineWith(result2) { _, _ -> 0 }

    assertThat(combined).isInstanceOf(AsyncResult.Pending::class.java)
  }

  @Test
  fun testSucceededAsyncResult_combinedWithFailure_isFailedWithCorrectChainedFailure() {
    val result1 = AsyncResult.Success("value")
    val result2 = AsyncResult.Failure<String>(RuntimeException())

    val combined = result1.combineWith(result2) { _, _ -> 0 }

    assertThat(combined).isFailureThat().isInstanceOf(ChainedFailureException::class.java)
    assertThat(combined).isFailureThat().hasCauseThat().isInstanceOf(RuntimeException::class.java)
  }

  @Test
  fun testSucceededAsyncResult_combinedWithSuccess_hasCombinedSuccessValue() {
    val result1 = AsyncResult.Success("value")
    val result2 = AsyncResult.Success(1.0)

    val combined = result1.combineWith(result2) { v1, v2 -> v1 + v2 }

    assertThat(combined).isStringSuccessThat().contains("value")
    assertThat(combined).isStringSuccessThat().contains("1.0")
  }

  @Test
  fun testSucceededAsyncResult_combinedAsyncWithPending_isPending() {
    val result1 = AsyncResult.Success("value")
    val result2 = AsyncResult.Pending<String>()

    val combined = result1.blockingCombineWithAsync(result2) { _, _ -> AsyncResult.Success(0) }

    assertThat(combined).isInstanceOf(AsyncResult.Pending::class.java)
  }

  @Test
  fun testSucceededAsyncResult_combinedAsyncWithFailure_isFailedWithCorrectChainedFailure() {
    val result1 = AsyncResult.Success("value")
    val result2 = AsyncResult.Failure<String>(RuntimeException())

    val combined = result1.blockingCombineWithAsync(result2) { _, _ -> AsyncResult.Success(0) }

    assertThat(combined).isFailureThat().isInstanceOf(ChainedFailureException::class.java)
    assertThat(combined).isFailureThat().hasCauseThat().isInstanceOf(RuntimeException::class.java)
  }

  @Test
  fun testSucceededAsyncResult_combinedAsyncWithSuccess_resultPending_isPending() {
    val result1 = AsyncResult.Success("value")
    val result2 = AsyncResult.Success(1.0)

    val combined = result1.blockingCombineWithAsync(result2) { _, _ ->
      AsyncResult.Pending<String>()
    }

    assertThat(combined).isInstanceOf(AsyncResult.Pending::class.java)
  }

  @Test
  fun testSucceededAsyncResult_combinedAsyncWithSuccess_resultFailure_isFailed() {
    val result1 = AsyncResult.Success("value")
    val result2 = AsyncResult.Success(1.0)

    val combined = result1.blockingCombineWithAsync(result2) { _, _ ->
      AsyncResult.Failure<String>(RuntimeException())
    }

    // Note that the failure is not chained since the transform function was responsible for
    // 'throwing' it.
    assertThat(combined).isFailureThat().isInstanceOf(RuntimeException::class.java)
  }

  @Test
  fun testSucceededAsyncResult_combinedAsyncWithSuccess_resultSuccess_hasCombinedSuccessValue() {
    val result1 = AsyncResult.Success("value")
    val result2 = AsyncResult.Success(1.0)

    val combined = result1.blockingCombineWithAsync(result2) { v1, v2 ->
      AsyncResult.Success(v1 + v2)
    }

    assertThat(combined).isStringSuccessThat().contains("value")
    assertThat(combined).isStringSuccessThat().contains("1.0")
  }

  @Test
  fun testSucceededResult_isNotEqualToPendingResult() {
    val result = AsyncResult.Success("Success")

    assertThat(result).isNotEqualTo(AsyncResult.Pending<String>())
  }

  @Test
  fun testSucceededResult_isEqualToSameSucceededResult() {
    val result = AsyncResult.Success("Success")

    assertThat(result).isEqualTo(AsyncResult.Success("Success"))
  }

  @Test
  fun testSucceededResult_isNotEqualToDifferentSucceededResult() {
    val result = AsyncResult.Success("Success")

    assertThat(result).isNotEqualTo(AsyncResult.Success("Other value"))
  }

  @Test
  fun testSucceededResult_isNotEqualToDifferentTypedSucceededResult() {
    val result = AsyncResult.Success("0")

    assertThat(result).isNotEqualTo(AsyncResult.Success(0))
  }

  @Test
  fun testSucceededResult_isNotEqualToFailedResult() {
    val result = AsyncResult.Success("Success")

    assertThat(result).isNotEqualTo(AsyncResult.Failure<String>(UnsupportedOperationException()))
  }

  @Test
  fun testSucceededResult_hashCode_isNotEqualToPendingResult() {
    val resultHash = AsyncResult.Success("Success").hashCode()

    // Two pending results are the same regardless of their types.
    assertThat(resultHash).isNotEqualTo(AsyncResult.Pending<String>().hashCode())
  }

  @Test
  fun testSucceededResult_hashCode_isEqualToSameSucceededResult() {
    val resultHash = AsyncResult.Success("Success").hashCode()

    assertThat(resultHash).isEqualTo(AsyncResult.Success("Success").hashCode())
  }

  @Test
  fun testSucceededResult_hashCode_isNotEqualToDifferentSucceededResult() {
    val resultHash = AsyncResult.Success("Success").hashCode()

    assertThat(resultHash).isNotEqualTo(AsyncResult.Success("Other value").hashCode())
  }

  @Test
  fun testSucceededResult_hashCode_isNotEqualToDifferentTypedSucceededResult() {
    val resultHash = AsyncResult.Success("0").hashCode()

    assertThat(resultHash).isNotEqualTo(AsyncResult.Success(0))
  }

  @Test
  fun testSucceededResult_hashCode_isNotEqualToFailedResult() {
    val resultHash = AsyncResult.Success("Success").hashCode()

    assertThat(resultHash).isNotEqualTo(
      AsyncResult.Failure<String>(UnsupportedOperationException()).hashCode()
    )
  }

  @Test
  fun testSucceededResult_comparedWithItself_isTheSameAge() {
    val result = AsyncResult.Success("value")

    val areSameAge = result.isNewerThanOrSameAgeAs(result)

    assertThat(areSameAge).isTrue()
  }

  @Test
  fun testSucceededResult_comparedWithPendingResult_createdAtTheSameTime_areTheSameAge() {
    val pendingResult = AsyncResult.Pending<String>()
    val success = AsyncResult.Success("value")

    val areSameAge = success.isNewerThanOrSameAgeAs(pendingResult)

    assertThat(areSameAge).isTrue()
  }

  @Test
  fun testSucceededResult_comparedWithOtherSucceededResult_createdAtTheSameTime_areTheSameAge() {
    val result1 = AsyncResult.Success("value")
    val result2 = AsyncResult.Success("value")

    val areSameAge = result1.isNewerThanOrSameAgeAs(result2)

    assertThat(areSameAge).isTrue()
  }

  @Test
  fun testSucceededResult_comparedWithFailedResult_createdAtTheSameTime_areTheSameAge() {
    val success = AsyncResult.Success("value")
    val failure = AsyncResult.Failure<String>(RuntimeException())

    val areSameAge = success.isNewerThanOrSameAgeAs(failure)

    assertThat(areSameAge).isTrue()
  }

  @Test
  fun testSucceededResult_comparedWithOlderSucceededResult_isNewer() {
    val olderResult = AsyncResult.Success("value")
    fakeSystemClock.advanceTime(millis = 10)
    val newerResult = AsyncResult.Success("value")

    val isNewer = newerResult.isNewerThanOrSameAgeAs(olderResult)

    assertThat(isNewer).isTrue()
  }

  @Test
  fun testSucceededResult_comparedWithNewerSucceededResult_isNotNewer() {
    val olderResult = AsyncResult.Success("value")
    fakeSystemClock.advanceTime(millis = 10)
    val newerResult = AsyncResult.Success("value")

    val isNewer = olderResult.isNewerThanOrSameAgeAs(newerResult)

    assertThat(isNewer).isFalse()
  }

  @Test
  fun testSuccessfulResult_nullValue_canRetrieveNullValue() {
    val result = AsyncResult.Success<Any?>(null)

    assertThat(result.value).isNull()
  }

  @Test
  fun testSuccessfulResult_nullValue_transformIntoString_createsResultWithCorrectValue() {
    val result1 = AsyncResult.Success<Any?>(null)

    val result2 = result1.transform { "string" }

    assertThat(result2).isStringSuccessThat().isEqualTo("string")
  }

  @Test
  fun testSuccessfulResult_stringValue_transformIntoNull_createsResultWithCorrectValue() {
    val result1 = AsyncResult.Success("string")

    val result2: AsyncResult<Any?> = result1.transform { null }

    assertThat(result2).isSuccessThat().isNull()
  }

  @Test
  fun testSuccessfulResult_combineStringAndNullResult_createsResultWithCorrectValue() {
    val result1 = AsyncResult.Success("string")
    val result2 = AsyncResult.Success<Any?>(null)

    val combined = result1.combineWith(result2) { _, _ -> "combined" }

    assertThat(combined).isStringSuccessThat().isEqualTo("combined")
  }

  /* Failure tests. */

  @Test
  fun testFailedAsyncResult_containsFailureException() {
    val result = AsyncResult.Failure<String>(UnsupportedOperationException())

    assertThat(result.error).isInstanceOf(UnsupportedOperationException::class.java)
  }

  @Test
  fun testFailedAsyncResult_transformed_throwsChainedFailureException_withCorrectRootCause() {
    val result = AsyncResult.Failure<String>(UnsupportedOperationException())

    val transformed = result.transform { 0 }

    assertThat(transformed).isFailureThat().isInstanceOf(ChainedFailureException::class.java)
    assertThat(transformed).isFailureThat()
      .hasCauseThat()
      .isInstanceOf(UnsupportedOperationException::class.java)
  }

  @Test
  fun testFailedAsyncResult_transformedAsync_throwsChainedFailureException_withCorrectRootCause() {
    val result = AsyncResult.Failure<String>(UnsupportedOperationException())

    val transformed = result.blockingTransformAsync { AsyncResult.Success(0) }

    assertThat(transformed).isFailureThat().isInstanceOf(ChainedFailureException::class.java)
    assertThat(transformed).isFailureThat()
      .hasCauseThat()
      .isInstanceOf(UnsupportedOperationException::class.java)
  }

  @Test
  fun testFailedAsyncResult_combinedWithPending_isStillChainedFailure() {
    val result1 = AsyncResult.Failure<String>(UnsupportedOperationException())
    val result2 = AsyncResult.Pending<String>()

    val combined = result1.combineWith(result2) { _, _ -> 0 }

    assertThat(combined).isFailureThat().isInstanceOf(ChainedFailureException::class.java)
    assertThat(combined).isFailureThat()
      .hasCauseThat()
      .isInstanceOf(UnsupportedOperationException::class.java)
  }

  @Test
  fun testFailedAsyncResult_combinedWithFailure_hasFirstFailureChained() {
    val result1 = AsyncResult.Failure<String>(UnsupportedOperationException())
    val result2 = AsyncResult.Failure<String>(RuntimeException())

    val combined = result1.combineWith(result2) { _, _ -> 0 }

    assertThat(combined).isFailureThat().isInstanceOf(ChainedFailureException::class.java)
    assertThat(combined).isFailureThat()
      .hasCauseThat()
      .isInstanceOf(UnsupportedOperationException::class.java)
  }

  @Test
  fun testFailedAsyncResult_combinedWithSuccess_isStillChainedFailure() {
    val result1 = AsyncResult.Failure<String>(UnsupportedOperationException())
    val result2 = AsyncResult.Success(1.0f)

    val combined = result1.combineWith(result2) { _, _ -> 0 }

    assertThat(combined).isFailureThat().isInstanceOf(ChainedFailureException::class.java)
    assertThat(combined).isFailureThat()
      .hasCauseThat()
      .isInstanceOf(UnsupportedOperationException::class.java)
  }

  @Test
  fun testFailedAsyncResult_combinedAsyncWithPending_isStillChainedFailure() {
    val result1 = AsyncResult.Failure<String>(UnsupportedOperationException())
    val result2 = AsyncResult.Pending<String>()

    val combined = result1.blockingCombineWithAsync(result2) { _, _ -> AsyncResult.Success(0) }

    assertThat(combined).isFailureThat().isInstanceOf(ChainedFailureException::class.java)
    assertThat(combined).isFailureThat()
      .hasCauseThat()
      .isInstanceOf(UnsupportedOperationException::class.java)
  }

  @Test
  fun testFailedAsyncResult_combinedAsyncWithFailure_isStillChainedFailure() {
    val result1 = AsyncResult.Failure<String>(UnsupportedOperationException())
    val result2 = AsyncResult.Failure<String>(RuntimeException())

    val combined = result1.blockingCombineWithAsync(result2) { _, _ -> AsyncResult.Success(0) }

    assertThat(combined).isFailureThat().isInstanceOf(ChainedFailureException::class.java)
    assertThat(combined).isFailureThat()
      .hasCauseThat()
      .isInstanceOf(UnsupportedOperationException::class.java)
  }

  @Test
  fun testFailedAsyncResult_combinedAsyncWithSuccess_isStillChainedFailure() {
    val result1 = AsyncResult.Failure<String>(UnsupportedOperationException())
    val result2 = AsyncResult.Success(1.0f)

    val combined = result1.blockingCombineWithAsync(result2) { _, _ -> AsyncResult.Success(0) }

    assertThat(combined).isFailureThat().isInstanceOf(ChainedFailureException::class.java)
    assertThat(combined).isFailureThat()
      .hasCauseThat()
      .isInstanceOf(UnsupportedOperationException::class.java)
  }

  @Test
  fun testFailedResult_isNotEqualToPendingResult() {
    val result = AsyncResult.Failure<String>(UnsupportedOperationException("Reason"))

    assertThat(result).isNotEqualTo(AsyncResult.Pending<String>())
  }

  @Test
  fun testFailedResult_isNotEqualToSucceededResult() {
    val result = AsyncResult.Failure<String>(UnsupportedOperationException("Reason"))

    assertThat(result).isNotEqualTo(AsyncResult.Success("Success"))
  }

  @Test
  fun testFailedResult_isEqualToFailedResultWithSameExceptionObject() {
    val failure = UnsupportedOperationException("Reason")

    val result = AsyncResult.Failure<String>(failure)

    assertThat(result).isEqualTo(AsyncResult.Failure<String>(failure))
  }

  @Test
  fun testFailedResult_isNotEqualToFailedResultWithDifferentInstanceOfSameExceptionType() {
    val result = AsyncResult.Failure<String>(UnsupportedOperationException("Reason"))

    // Different exceptions have different stack traces, so they can't be equal despite similar
    // constructions.
    assertThat(result).isNotEqualTo(
      AsyncResult.Failure<String>(UnsupportedOperationException("Reason"))
    )
  }

  @Test
  fun testFailedResult_hashCode_isNotEqualToPendingResult() {
    val resultHash = AsyncResult.Failure<String>(UnsupportedOperationException("Reason")).hashCode()

    // Two pending results are the same regardless of their types.
    assertThat(resultHash).isNotEqualTo(AsyncResult.Pending<String>().hashCode())
  }

  @Test
  fun testFailedResult_hashCode_isNotEqualToSucceededResult() {
    val resultHash = AsyncResult.Failure<String>(UnsupportedOperationException("Reason")).hashCode()

    assertThat(resultHash).isNotEqualTo(AsyncResult.Success("Success").hashCode())
  }

  @Test
  fun testFailedResult_hashCode_isEqualToFailedResultWithSameExceptionObject() {
    val failure = UnsupportedOperationException("Reason")

    val resultHash = AsyncResult.Failure<String>(failure).hashCode()

    assertThat(resultHash).isEqualTo(AsyncResult.Failure<String>(failure).hashCode())
  }

  @Test
  fun testFailedResult_hashCode_isNotEqualToFailedResultWithDifferentInstanceOfSameExceptionType() {
    val resultHash = AsyncResult.Failure<String>(UnsupportedOperationException("Reason")).hashCode()

    // Different exceptions have different stack traces, so they can't be equal despite similar
    // constructions.
    assertThat(resultHash).isNotEqualTo(
      AsyncResult.Failure<String>(UnsupportedOperationException("Reason")).hashCode()
    )
  }

  @Test
  fun testFailedResult_comparedWithItself_isTheSameAge() {
    val result = AsyncResult.Failure<String>(RuntimeException())

    val areSameAge = result.isNewerThanOrSameAgeAs(result)

    assertThat(areSameAge).isTrue()
  }

  @Test
  fun testFailedResult_comparedWithPendingResult_createdAtTheSameTime_areTheSameAge() {
    val failure = AsyncResult.Failure<String>(RuntimeException())
    val pendingResult = AsyncResult.Pending<String>()

    val areSameAge = failure.isNewerThanOrSameAgeAs(pendingResult)

    assertThat(areSameAge).isTrue()
  }

  @Test
  fun testFailedResult_comparedWithSucceededResult_createdAtTheSameTime_areTheSameAge() {
    val failure = AsyncResult.Failure<String>(RuntimeException())
    val success = AsyncResult.Success("value")

    val areSameAge = failure.isNewerThanOrSameAgeAs(success)

    assertThat(areSameAge).isTrue()
  }

  @Test
  fun testFailedResult_comparedWithOtherFailedResult_createdAtTheSameTime_areTheSameAge() {
    val result1 = AsyncResult.Failure<String>(RuntimeException())
    val result2 = AsyncResult.Failure<String>(RuntimeException())

    val areSameAge = result1.isNewerThanOrSameAgeAs(result2)

    assertThat(areSameAge).isTrue()
  }

  @Test
  fun testFailedResult_comparedWithOlderFailedResult_isNewer() {
    val olderResult = AsyncResult.Failure<String>(RuntimeException())
    fakeSystemClock.advanceTime(millis = 10)
    val newerResult = AsyncResult.Failure<String>(RuntimeException())

    val isNewer = newerResult.isNewerThanOrSameAgeAs(olderResult)

    assertThat(isNewer).isTrue()
  }

  @Test
  fun testFailedResult_comparedWithNewerFailedResult_isNotNewer() {
    val olderResult = AsyncResult.Failure<String>(RuntimeException())
    fakeSystemClock.advanceTime(millis = 10)
    val newerResult = AsyncResult.Failure<String>(RuntimeException())

    val isNewer = olderResult.isNewerThanOrSameAgeAs(newerResult)

    assertThat(isNewer).isFalse()
  }

  private fun setUpTestApplicationComponent() {
    DaggerAsyncResultTest_TestApplicationComponent.builder()
      .setApplication(ApplicationProvider.getApplicationContext())
      .build()
      .inject(this)
  }

  @Suppress("EXPERIMENTAL_API_USAGE")
  private fun <T, O> AsyncResult<T>.blockingTransformAsync(
    transformFunction: suspend (T) -> AsyncResult<O>
  ): AsyncResult<O> {
    val deferred = backgroundTestDispatcherScope.async { transformAsync(transformFunction) }
    testCoroutineDispatchers.runCurrent()
    return deferred.getCompleted()
  }

  @Suppress("EXPERIMENTAL_API_USAGE")
  private fun <T1, T2, O> AsyncResult<T1>.blockingCombineWithAsync(
    otherResult: AsyncResult<T2>,
    combineFunction: suspend (T1, T2) -> AsyncResult<O>
  ): AsyncResult<O> {
    val deferred = backgroundTestDispatcherScope.async {
      combineWithAsync(otherResult, combineFunction)
    }
    testCoroutineDispatchers.runCurrent()
    return deferred.getCompleted()
  }

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(
    modules = [
      TestDispatcherModule::class,
      RobolectricModule::class, FakeOppiaClockModule::class
    ]
  )
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
