package org.oppia.android.testing.data

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.exceptions.verification.NeverWantedButInvoked
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.assertThrows
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.data.AsyncDataSubscriptionManager
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProvider
import org.oppia.android.util.data.DataProviders
import org.oppia.android.util.data.DataProvidersInjector
import org.oppia.android.util.data.DataProvidersInjectorProvider
import org.oppia.android.util.locale.LocaleProdModule
import org.oppia.android.util.logging.LoggerModule
import org.oppia.android.util.networking.NetworkConnectionUtilDebugModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.delay
import org.oppia.android.testing.data.AsyncResultSubject.Companion.assertThat

/** Tests for [DataProviderTestMonitor]. */
// FunctionName: test names are conventionally named with underscores.
// SameParameterValue: tests should have specific context included/excluded for readability.
// ThrowableNotThrown: exceptions are created for AsyncResults.
@Suppress("FunctionName", "SameParameterValue", "ThrowableNotThrown")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = DataProviderTestMonitorTest.TestApplication::class)
class DataProviderTestMonitorTest {
  @Inject lateinit var monitorFactory: DataProviderTestMonitor.Factory
  @Inject lateinit var dataProviders: DataProviders
  @Inject lateinit var asyncDataSubscriptionManager: AsyncDataSubscriptionManager
  @Inject lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  /* Tests for createMonitor */

  @Test
  fun testCreateMonitor_returnsNewObservingMonitor() {
    var fakeLoadMemoryCallbackCalled = false
    val fakeLoadMemoryCallback: () -> String = {
      fakeLoadMemoryCallbackCalled = true
      "test value"
    }
    val dataProvider = dataProviders.createInMemoryDataProvider("test", fakeLoadMemoryCallback)

    monitorFactory.createMonitor(dataProvider)
    testCoroutineDispatchers.runCurrent()

    // Verify that the data provider was executed (indicating that the monitor is live).
    assertThat(fakeLoadMemoryCallbackCalled).isTrue()
  }

  @Test
  fun testCreateMonitor_twice_returnsDifferentMonitors() {
    val dataProvider = dataProviders.createInMemoryDataProvider("test") { 0 }

    val monitor1 = monitorFactory.createMonitor(dataProvider)
    val monitor2 = monitorFactory.createMonitor(dataProvider)

    // Verify that the two monitors are different
    assertThat(monitor1).isNotEqualTo(monitor2)
  }

  /* Tests for waitForNextResult */

  @Test
  fun testWaitForNextResult_pendingDataProvider_returnsResult() {
    val dataProvider = dataProviders.createInMemoryDataProviderAsync<String>("test") {
      AsyncResult.Pending()
    }
    val monitor = monitorFactory.createMonitor(dataProvider)

    val result = monitor.waitForNextResult()

    assertThat(result).isPending()
  }

  @Test
  fun testWaitForNextResult_failingDataProvider_returnsResult() {
    val dataProvider = dataProviders.createInMemoryDataProviderAsync<String>("test") {
      AsyncResult.Failure(Exception("Failure"))
    }
    val monitor = monitorFactory.createMonitor(dataProvider)

    val result = monitor.waitForNextResult()

    assertThat(result).isFailureThat().hasMessageThat().contains("Failure")
  }

  @Test
  fun testWaitForNextResult_successfulDataProvider_returnsResult() {
    val dataProvider = dataProviders.createInMemoryDataProviderAsync("test") {
      AsyncResult.Success("str value")
    }
    val monitor = monitorFactory.createMonitor(dataProvider)

    val result = monitor.waitForNextResult()

    assertThat(result).isStringSuccessThat().isEqualTo("str value")
  }

  @Test
  fun testWaitForNextResult_failureThenSuccess_returnsLatest() {
    val dataProvider =
      createDataProviderWithResultsQueue(
        "test", AsyncResult.Failure(Exception("Failure")), AsyncResult.Success("str value")
      )
    val monitor = monitorFactory.createMonitor(dataProvider)
    monitor.waitForNextResult() // Wait for the first result.

    // Update the provider, then wait for the result.
    asyncDataSubscriptionManager.notifyChangeAsync("test")
    val result = monitor.waitForNextResult()

    assertThat(result).isStringSuccessThat().isEqualTo("str value")
  }

  @Test
  fun testWaitForNextResult_differentValues_notified_returnsLatest() {
    val dataProvider =
      createDataProviderWithResultsQueue(
        "test", AsyncResult.Success("first"), AsyncResult.Success("second")
      )
    val monitor = monitorFactory.createMonitor(dataProvider)
    monitor.waitForNextResult()

    asyncDataSubscriptionManager.notifyChangeAsync("test")
    val result = monitor.waitForNextResult()

    assertThat(result).isStringSuccessThat().isEqualTo("second")
  }

  /* Tests for waitForNextSuccessResult */

  @Test
  fun testWaitForNextSuccessResult_pendingDataProvider_throwsException() {
    val dataProvider = dataProviders.createInMemoryDataProviderAsync<String>("test") {
      AsyncResult.Pending()
    }
    val monitor = monitorFactory.createMonitor(dataProvider)

    val failure = assertThrows(IllegalStateException::class) { monitor.waitForNextSuccessResult() }

    assertThat(failure).hasMessageThat().contains("Expected next result to be a success")
  }

  @Test
  fun testWaitForNextSuccessResult_failingDataProvider_throwsException() {
    val dataProvider = dataProviders.createInMemoryDataProviderAsync<String>("test") {
      AsyncResult.Failure(Exception("Failure"))
    }
    val monitor = monitorFactory.createMonitor(dataProvider)

    val failure = assertThrows(IllegalStateException::class) { monitor.waitForNextSuccessResult() }

    assertThat(failure).hasMessageThat().contains("Expected next result to be a success")
  }

  @Test
  fun testWaitForNextSuccessResult_successfulDataProvider_returnsResult() {
    val dataProvider = dataProviders.createInMemoryDataProviderAsync("test") {
      AsyncResult.Success("str value")
    }
    val monitor = monitorFactory.createMonitor(dataProvider)

    val result = monitor.waitForNextSuccessResult()

    assertThat(result).isEqualTo("str value")
  }

  @Test
  fun testWaitForNextSuccessResult_failureThenSuccess_notified_returnsLatest() {
    val dataProvider =
      createDataProviderWithResultsQueue(
        "test", AsyncResult.Failure(Exception("Failure")), AsyncResult.Success("str value")
      )
    val monitor = monitorFactory.createMonitor(dataProvider)
    monitor.waitForNextResult() // Wait for the first result.

    asyncDataSubscriptionManager.notifyChangeAsync("test")
    val result = monitor.waitForNextSuccessResult()

    assertThat(result).isEqualTo("str value")
  }

  @Test
  fun testWaitForNextSuccessResult_successThenFailure_notified_throwsException() {
    val dataProvider =
      createDataProviderWithResultsQueue(
        "test", AsyncResult.Success("str value"), AsyncResult.Failure(Exception("Failure"))
      )
    val monitor = monitorFactory.createMonitor(dataProvider)
    monitor.waitForNextResult() // Wait for the first result.

    asyncDataSubscriptionManager.notifyChangeAsync("test")
    val failure = assertThrows(IllegalStateException::class) { monitor.waitForNextSuccessResult() }

    assertThat(failure).hasMessageThat().contains("Expected next result to be a success")
  }

  @Test
  fun testWaitForNextSuccessResult_differentValues_notified_returnsLatest() {
    val dataProvider =
      createDataProviderWithResultsQueue(
        "test", AsyncResult.Success("first"), AsyncResult.Success("second")
      )
    val monitor = monitorFactory.createMonitor(dataProvider)
    monitor.waitForNextResult() // Wait for the first result.

    asyncDataSubscriptionManager.notifyChangeAsync("test")
    val result = monitor.waitForNextSuccessResult()

    assertThat(result).isEqualTo("second")
  }

  /* Tests for ensureNextResultIsSuccess */

  @Test
  fun testEnsureNextResultIsSuccess_successfulDataProvider_throwsException() {
    val dataProvider = dataProviders.createInMemoryDataProviderAsync("test") {
      AsyncResult.Success("str value")
    }
    val monitor = monitorFactory.createMonitor(dataProvider)

    // Internal expectation failure since the operation hasn't completed.
    assertThrows(AssertionError::class) { monitor.ensureNextResultIsSuccess() }
  }

  @Test
  fun testEnsureNextResultIsSuccess_successfulDataProvider_wait_returnsResult() {
    val dataProvider = dataProviders.createInMemoryDataProviderAsync("test") {
      AsyncResult.Success("str value")
    }
    val monitor = monitorFactory.createMonitor(dataProvider)

    testCoroutineDispatchers.runCurrent() // Ensure the subscription is updated.
    val result = monitor.ensureNextResultIsSuccess()

    assertThat(result).isEqualTo("str value")
  }

  @Test
  fun testEnsureNextResultIsSuccess_pendingDataProvider_wait_throwsException() {
    val dataProvider = dataProviders.createInMemoryDataProviderAsync<String>("test") {
      AsyncResult.Pending()
    }
    val monitor = monitorFactory.createMonitor(dataProvider)

    testCoroutineDispatchers.runCurrent() // Ensure the subscription is updated.
    val failure = assertThrows(IllegalStateException::class) { monitor.ensureNextResultIsSuccess() }

    assertThat(failure).hasMessageThat().contains("Expected next result to be a success")
  }

  @Test
  fun testEnsureNextResultIsSuccess_failingDataProvider_wait_throwsException() {
    val dataProvider = dataProviders.createInMemoryDataProviderAsync<String>("test") {
      AsyncResult.Failure(Exception("Failure"))
    }
    val monitor = monitorFactory.createMonitor(dataProvider)

    testCoroutineDispatchers.runCurrent() // Ensure the subscription is updated.
    val failure = assertThrows(IllegalStateException::class) { monitor.ensureNextResultIsSuccess() }

    assertThat(failure).hasMessageThat().contains("Expected next result to be a success")
  }

  @Test
  fun testEnsureNextResultIsSuccess_failureThenSuccess_notified_throwsException() {
    val dataProvider =
      createDataProviderWithResultsQueue(
        "test", AsyncResult.Failure(Exception("Failure")), AsyncResult.Success("str value")
      )
    val monitor = monitorFactory.createMonitor(dataProvider)
    monitor.waitForNextResult() // Wait for the first result.

    asyncDataSubscriptionManager.notifyChangeAsync("test")
    testCoroutineDispatchers.runCurrent() // Ensure the subscription is updated.
    // Internal expectation failure since the operation hasn't completed.
    assertThrows(AssertionError::class) { monitor.ensureNextResultIsSuccess() }
  }

  @Test
  fun testEnsureNextResultIsSuccess_failureThenSuccess_notified_wait_returnsLatest() {
    val dataProvider =
      createDataProviderWithResultsQueue(
        "test", AsyncResult.Failure(Exception("Failure")), AsyncResult.Success("str value")
      )
    val monitor = monitorFactory.createMonitor(dataProvider)
    monitor.waitForNextResult() // Wait for the first result.

    asyncDataSubscriptionManager.notifyChangeAsync("test")
    testCoroutineDispatchers.runCurrent() // Ensure the subscription is updated.
    val result = monitor.ensureNextResultIsSuccess()

    assertThat(result).isEqualTo("str value")
  }

  @Test
  fun testEnsureNextResultIsSuccess_successThenFailure_notified_wait_throwsException() {
    val dataProvider =
      createDataProviderWithResultsQueue(
        "test", AsyncResult.Success("str value"), AsyncResult.Failure(Exception("Failure"))
      )
    val monitor = monitorFactory.createMonitor(dataProvider)
    monitor.waitForNextResult() // Wait for the first result.

    asyncDataSubscriptionManager.notifyChangeAsync("test")
    testCoroutineDispatchers.runCurrent() // Ensure the subscription is updated.
    val failure = assertThrows(IllegalStateException::class) { monitor.ensureNextResultIsSuccess() }

    assertThat(failure).hasMessageThat().contains("Expected next result to be a success")
  }

  @Test
  fun testEnsureNextResultIsSuccess_differentValues_notified_wait_returnsLatest() {
    val dataProvider =
      createDataProviderWithResultsQueue(
        "test", AsyncResult.Success("first"), AsyncResult.Success("second")
      )
    val monitor = monitorFactory.createMonitor(dataProvider)
    monitor.waitForNextResult() // Wait for the first result.

    asyncDataSubscriptionManager.notifyChangeAsync("test")
    testCoroutineDispatchers.runCurrent() // Ensure the subscription is updated.
    val result = monitor.ensureNextResultIsSuccess()

    assertThat(result).isEqualTo("second")
  }

  /* Tests for waitForNextFailingResult */

  @Test
  fun testWaitForNextFailingResult_pendingDataProvider_throwsException() {
    val dataProvider = dataProviders.createInMemoryDataProviderAsync<String>("test") {
      AsyncResult.Pending()
    }
    val monitor = monitorFactory.createMonitor(dataProvider)

    val failure = assertThrows(IllegalStateException::class) { monitor.waitForNextFailingResult() }

    assertThat(failure).hasMessageThat().contains("Expected next result to be a failure")
  }

  @Test
  fun testWaitForNextFailingResult_failingDataProvider_returnsResult() {
    val dataProvider = dataProviders.createInMemoryDataProviderAsync<String>("test") {
      AsyncResult.Failure(Exception("Failure"))
    }
    val monitor = monitorFactory.createMonitor(dataProvider)

    val result = monitor.waitForNextFailingResult()

    assertThat(result).hasMessageThat().contains("Failure")
  }

  @Test
  fun testWaitForNextFailingResult_successfulDataProvider_throwsException() {
    val dataProvider = dataProviders.createInMemoryDataProviderAsync("test") {
      AsyncResult.Success("str value")
    }
    val monitor = monitorFactory.createMonitor(dataProvider)

    val failure = assertThrows(IllegalStateException::class) { monitor.waitForNextFailingResult() }

    assertThat(failure).hasMessageThat().contains("Expected next result to be a failure")
  }

  @Test
  fun testWaitForNextFailingResult_successThenFailure_notified_returnsLatest() {
    val dataProvider =
      createDataProviderWithResultsQueue(
        "test", AsyncResult.Success("str value"), AsyncResult.Failure(Exception("Failure"))
      )
    val monitor = monitorFactory.createMonitor(dataProvider)
    monitor.waitForNextResult() // Wait for the first result.

    asyncDataSubscriptionManager.notifyChangeAsync("test")
    val result = monitor.waitForNextFailingResult()

    assertThat(result).hasMessageThat().contains("Failure")
  }

  @Test
  fun testWaitForNextFailingResult_failureThenSuccess_notified_throwsException() {
    val dataProvider =
      createDataProviderWithResultsQueue(
        "test", AsyncResult.Failure(Exception("Failure")), AsyncResult.Success("str value")
      )
    val monitor = monitorFactory.createMonitor(dataProvider)
    monitor.waitForNextResult() // Wait for the first result.

    asyncDataSubscriptionManager.notifyChangeAsync("test")
    val failure = assertThrows(IllegalStateException::class) { monitor.waitForNextFailingResult() }

    assertThat(failure).hasMessageThat().contains("Expected next result to be a failure")
  }

  @Test
  fun testWaitForNextFailingResult_differentValues_notified_returnsLatest() {
    val dataProvider =
      createDataProviderWithResultsQueue<String>(
        "test", AsyncResult.Failure(Exception("First")), AsyncResult.Failure(Exception("Second"))
      )
    val monitor = monitorFactory.createMonitor(dataProvider)
    monitor.waitForNextResult() // Wait for the first result.

    asyncDataSubscriptionManager.notifyChangeAsync("test")
    val result = monitor.waitForNextFailingResult()

    assertThat(result).hasMessageThat().contains("Second")
  }

  /* Tests for ensureNextResultIsFailing */

  @Test
  fun testEnsureNextResultIsFailing_failingDataProvider_throwsException() {
    val dataProvider = dataProviders.createInMemoryDataProviderAsync<String>("test") {
      AsyncResult.Failure(Exception("Failure"))
    }
    val monitor = monitorFactory.createMonitor(dataProvider)

    // Internal expectation failure since the operation hasn't completed.
    assertThrows(AssertionError::class) { monitor.ensureNextResultIsSuccess() }
  }

  @Test
  fun testEnsureNextResultIsFailing_failingDataProvider_wait_returnsResult() {
    val dataProvider = dataProviders.createInMemoryDataProviderAsync<String>("test") {
      AsyncResult.Failure(Exception("Failure"))
    }
    val monitor = monitorFactory.createMonitor(dataProvider)

    testCoroutineDispatchers.runCurrent() // Ensure the subscription is updated.
    val result = monitor.ensureNextResultIsFailing()

    assertThat(result).hasMessageThat().contains("Failure")
  }

  @Test
  fun testEnsureNextResultIsFailing_pendingDataProvider_wait_throwsException() {
    val dataProvider = dataProviders.createInMemoryDataProviderAsync<String>("test") {
      AsyncResult.Pending()
    }
    val monitor = monitorFactory.createMonitor(dataProvider)

    testCoroutineDispatchers.runCurrent() // Ensure the subscription is updated.
    val failure = assertThrows(IllegalStateException::class) { monitor.ensureNextResultIsFailing() }

    assertThat(failure).hasMessageThat().contains("Expected next result to be a failure")
  }

  @Test
  fun testEnsureNextResultIsFailing_successfulDataProvider_wait_throwsException() {
    val dataProvider = dataProviders.createInMemoryDataProviderAsync("test") {
      AsyncResult.Success("str value")
    }
    val monitor = monitorFactory.createMonitor(dataProvider)

    testCoroutineDispatchers.runCurrent() // Ensure the subscription is updated.
    val failure = assertThrows(IllegalStateException::class) { monitor.ensureNextResultIsFailing() }

    assertThat(failure).hasMessageThat().contains("Expected next result to be a failure")
  }

  @Test
  fun testEnsureNextResultIsFailing_successThenFailure_notified_throwsException() {
    val dataProvider =
      createDataProviderWithResultsQueue(
        "test", AsyncResult.Success("str value"), AsyncResult.Failure(Exception("Failure"))
      )
    val monitor = monitorFactory.createMonitor(dataProvider)
    monitor.waitForNextResult() // Wait for the first result.

    asyncDataSubscriptionManager.notifyChangeAsync("test")
    // Internal expectation failure since the operation hasn't completed.
    assertThrows(AssertionError::class) { monitor.ensureNextResultIsFailing() }
  }

  @Test
  fun testEnsureNextResultIsFailing_successThenFailure_notified_wait_returnsLatest() {
    val dataProvider =
      createDataProviderWithResultsQueue(
        "test", AsyncResult.Success("str value"), AsyncResult.Failure(Exception("Failure"))
      )
    val monitor = monitorFactory.createMonitor(dataProvider)
    monitor.waitForNextResult() // Wait for the first result.

    asyncDataSubscriptionManager.notifyChangeAsync("test")
    testCoroutineDispatchers.runCurrent() // Ensure the subscription is updated.
    val result = monitor.ensureNextResultIsFailing()

    assertThat(result).hasMessageThat().contains("Failure")
  }

  @Test
  fun testEnsureNextResultIsFailing_failureThenSuccess_notified_wait_throwsException() {
    val dataProvider =
      createDataProviderWithResultsQueue(
        "test", AsyncResult.Failure(Exception("Failure")), AsyncResult.Success("str value")
      )
    val monitor = monitorFactory.createMonitor(dataProvider)
    monitor.waitForNextResult() // Wait for the first result.

    asyncDataSubscriptionManager.notifyChangeAsync("test")
    testCoroutineDispatchers.runCurrent() // Ensure the subscription is updated.
    val failure = assertThrows(IllegalStateException::class) { monitor.ensureNextResultIsFailing() }

    assertThat(failure).hasMessageThat().contains("Expected next result to be a failure")
  }

  @Test
  fun testEnsureNextResultIsFailing_differentValues_notified_wait_returnsLatest() {
    val dataProvider =
      createDataProviderWithResultsQueue<String>(
        "test", AsyncResult.Failure(Exception("First")), AsyncResult.Failure(Exception("Second"))
      )
    val monitor = monitorFactory.createMonitor(dataProvider)
    monitor.waitForNextResult() // Wait for the first result.

    asyncDataSubscriptionManager.notifyChangeAsync("test")
    testCoroutineDispatchers.runCurrent() // Ensure the subscription is updated.
    val result = monitor.ensureNextResultIsFailing()

    assertThat(result).hasMessageThat().contains("Second")
  }

  /* Tests for verifyProviderIsNotUpdated */

  @Test
  fun testVerifyProviderIsNotUpdated_pendingDataProvider_throwsException() {
    val dataProvider = dataProviders.createInMemoryDataProviderAsync<String>("test") {
      AsyncResult.Pending()
    }
    val monitor = monitorFactory.createMonitor(dataProvider)

    // Verify that the method wsa actually called despite not being expected to have been.
    assertThrows(NeverWantedButInvoked::class) { monitor.verifyProviderIsNotUpdated() }
  }

  @Test
  fun testVerifyProviderIsNotUpdated_failingDataProvider_throwsException() {
    val dataProvider = dataProviders.createInMemoryDataProviderAsync<String>("test") {
      AsyncResult.Failure(Exception("Failure"))
    }
    val monitor = monitorFactory.createMonitor(dataProvider)

    // Verify that the method wsa actually called despite not being expected to have been.
    assertThrows(NeverWantedButInvoked::class) { monitor.verifyProviderIsNotUpdated() }
  }

  @Test
  fun testVerifyProviderIsNotUpdated_successfulDataProvider_throwsException() {
    val dataProvider = dataProviders.createInMemoryDataProviderAsync("test") {
      AsyncResult.Success("str value")
    }
    val monitor = monitorFactory.createMonitor(dataProvider)

    // Verify that the method wsa actually called despite not being expected to have been.
    assertThrows(NeverWantedButInvoked::class) { monitor.verifyProviderIsNotUpdated() }
  }

  @Test
  fun testVerifyProviderIsNotUpdated_successThenFailure_notified_throwsException() {
    val dataProvider =
      createDataProviderWithResultsQueue(
        "test", AsyncResult.Success("str value"), AsyncResult.Failure(Exception("Failure"))
      )
    val monitor = monitorFactory.createMonitor(dataProvider)
    monitor.waitForNextResult() // Wait for the first result.

    asyncDataSubscriptionManager.notifyChangeAsync("test")
    // Verify that the method wsa actually called despite not being expected to have been.
    assertThrows(NeverWantedButInvoked::class) { monitor.verifyProviderIsNotUpdated() }
  }

  @Test
  fun testVerifyProviderIsNotUpdated_failureThenSuccess_notified_throwsException() {
    val dataProvider =
      createDataProviderWithResultsQueue(
        "test", AsyncResult.Failure(Exception("Failure")), AsyncResult.Success("str value")
      )
    val monitor = monitorFactory.createMonitor(dataProvider)
    monitor.waitForNextResult() // Wait for the first result.

    // Update the provider, then wait for the result.
    asyncDataSubscriptionManager.notifyChangeAsync("test")

    // Verify that the method wsa actually called despite not being expected to have been.
    assertThrows(NeverWantedButInvoked::class) { monitor.verifyProviderIsNotUpdated() }
  }

  @Test
  fun testVerifyProviderIsNotUpdated_differentValues_notified_throwsException() {
    val dataProvider =
      createDataProviderWithResultsQueue(
        "test", AsyncResult.Success("first"), AsyncResult.Success("second")
      )
    val monitor = monitorFactory.createMonitor(dataProvider)
    monitor.waitForNextResult() // Wait for the first result.

    asyncDataSubscriptionManager.notifyChangeAsync("test")

    // Verify that the method wsa actually called despite not being expected to have been.
    assertThrows(NeverWantedButInvoked::class) { monitor.verifyProviderIsNotUpdated() }
  }

  @Test
  fun testVerifyProviderIsNotUpdated_waitForSuccess_noChanges_doesNotThrowException() {
    val dataProvider =
      createDataProviderWithResultsQueue(
        "test", AsyncResult.Success("first"), AsyncResult.Success("second")
      )
    val monitor = monitorFactory.createMonitor(dataProvider)
    monitor.waitForNextSuccessResult()

    monitor.verifyProviderIsNotUpdated()

    // The verification check doesn't throw since nothing's changed since the first result was
    // retrieved.
  }

  /* Tests for ensureDataProviderExecutes */

  @Test
  fun testEnsureDataProviderExecutes_pendingDataProvider_throwsException() {
    val dataProvider = dataProviders.createInMemoryDataProviderAsync<String>("test") {
      AsyncResult.Pending()
    }

    val failure =
      assertThrows(AssertionError::class) {
        monitorFactory.ensureDataProviderExecutes(dataProvider)
      }

    assertThat(failure).hasMessageThat().contains("not to be an instance of")
    assertThat(failure).hasMessageThat().contains("Pending")
  }

  @Test
  fun testEnsureDataProviderExecutes_unfinishedDataProvider_throwsException() {
    val dataProvider = dataProviders.createInMemoryDataProviderAsync("test") {
      delay(1000L)
      AsyncResult.Success("str value")
    }

    val failure =
      assertThrows(AssertionError::class) {
        monitorFactory.ensureDataProviderExecutes(dataProvider)
      }

    // The result will fail since the provider never even provided a result (since it required
    // advancing the test clock before a result would be available).
    assertThat(failure).hasMessageThat().contains("Wanted but not invoked")
  }

  @Test
  fun testEnsureDataProviderExecutes_failingDataProvider_doesNotThrowException() {
    val dataProvider = dataProviders.createInMemoryDataProviderAsync<String>("test") {
      AsyncResult.Failure(Exception("Failure"))
    }

    val failure = assertThrows(IllegalStateException::class) {
      monitorFactory.waitForNextSuccessfulResult(dataProvider)
    }

    assertThat(failure).hasMessageThat().contains("Expected next result to be a success")
  }

  @Test
  fun testEnsureDataProviderExecutes_successfulDataProvider_doesNotThrowException() {
    val dataProvider = dataProviders.createInMemoryDataProviderAsync("test") {
      AsyncResult.Success("str value")
    }

    val result = monitorFactory.waitForNextSuccessfulResult(dataProvider)

    assertThat(result).isEqualTo("str value")
  }

  @Test
  fun testEnsureDataProviderExecutes_failureThenSuccess_consumed_doesNotThrowException() {
    val dataProvider =
      createDataProviderWithResultsQueue(
        "test", AsyncResult.Failure(Exception("Failure")), AsyncResult.Success("str value")
      )
    monitorFactory.waitForNextFailureResult(dataProvider)

    val result = monitorFactory.waitForNextSuccessfulResult(dataProvider)

    assertThat(result).isEqualTo("str value")
  }

  @Test
  fun testEnsureDataProviderExecutes_successThenFailure_consumed_doesNotThrowException() {
    val dataProvider =
      createDataProviderWithResultsQueue(
        "test", AsyncResult.Success("str value"), AsyncResult.Failure(Exception("Failure"))
      )
    monitorFactory.waitForNextSuccessfulResult(dataProvider)

    val failure = assertThrows(IllegalStateException::class) {
      monitorFactory.waitForNextSuccessfulResult(dataProvider)
    }

    assertThat(failure).hasMessageThat().contains("Expected next result to be a success")
  }

  @Test
  fun testEnsureDataProviderExecutes_differentValues_consumed_doesNotThrowException() {
    val dataProvider =
      createDataProviderWithResultsQueue(
        "test", AsyncResult.Success("first"), AsyncResult.Success("second")
      )
    monitorFactory.waitForNextSuccessfulResult(dataProvider)

    val result = monitorFactory.waitForNextSuccessfulResult(dataProvider)

    assertThat(result).isEqualTo("second")
  }

  @Test
  fun testEnsureDataProviderExecutes_twiceForChangedProvider_doesNotThrowException() {
    val dataProvider =
      createDataProviderWithResultsQueue(
        "test", AsyncResult.Success("first"), AsyncResult.Success("second")
      )

    val firstResult = monitorFactory.waitForNextSuccessfulResult(dataProvider)
    val secondResult = monitorFactory.waitForNextSuccessfulResult(dataProvider)

    assertThat(firstResult).isEqualTo("first")
    assertThat(secondResult).isEqualTo("second")
  }

  /* Tests for waitForNextSuccessfulResult */

  @Test
  fun testWaitForNextSuccessfulResult_pendingDataProvider_throwsException() {
    val dataProvider = dataProviders.createInMemoryDataProviderAsync<String>("test") {
      AsyncResult.Pending()
    }

    val failure =
      assertThrows(IllegalStateException::class) {
        monitorFactory.waitForNextSuccessfulResult(dataProvider)
      }

    assertThat(failure).hasMessageThat().contains("Expected next result to be a success")
  }

  @Test
  fun testWaitForNextSuccessfulResult_failingDataProvider_throwsException() {
    val dataProvider = dataProviders.createInMemoryDataProviderAsync<String>("test") {
      AsyncResult.Failure(Exception("Failure"))
    }

    val failure = assertThrows(IllegalStateException::class) {
      monitorFactory.waitForNextSuccessfulResult(dataProvider)
    }

    assertThat(failure).hasMessageThat().contains("Expected next result to be a success")
  }

  @Test
  fun testWaitForNextSuccessfulResult_successfulDataProvider_returnsResult() {
    val dataProvider = dataProviders.createInMemoryDataProviderAsync("test") {
      AsyncResult.Success("str value")
    }

    val result = monitorFactory.waitForNextSuccessfulResult(dataProvider)

    assertThat(result).isEqualTo("str value")
  }

  @Test
  fun testWaitForNextSuccessfulResult_failureThenSuccess_consumed_returnsLatest() {
    val dataProvider =
      createDataProviderWithResultsQueue(
        "test", AsyncResult.Failure(Exception("Failure")), AsyncResult.Success("str value")
      )
    monitorFactory.waitForNextFailureResult(dataProvider)

    val result = monitorFactory.waitForNextSuccessfulResult(dataProvider)

    assertThat(result).isEqualTo("str value")
  }

  @Test
  fun testWaitForNextSuccessfulResult_successThenFailure_consumed_throwsException() {
    val dataProvider =
      createDataProviderWithResultsQueue(
        "test", AsyncResult.Success("str value"), AsyncResult.Failure(Exception("Failure"))
      )
    monitorFactory.waitForNextSuccessfulResult(dataProvider)

    val failure = assertThrows(IllegalStateException::class) {
      monitorFactory.waitForNextSuccessfulResult(dataProvider)
    }

    assertThat(failure).hasMessageThat().contains("Expected next result to be a success")
  }

  @Test
  fun testWaitForNextSuccessfulResult_differentValues_consumed_returnsLatest() {
    val dataProvider =
      createDataProviderWithResultsQueue(
        "test", AsyncResult.Success("first"), AsyncResult.Success("second")
      )
    monitorFactory.waitForNextSuccessfulResult(dataProvider)

    val result = monitorFactory.waitForNextSuccessfulResult(dataProvider)

    assertThat(result).isEqualTo("second")
  }

  @Test
  fun testWaitForNextSuccessfulResult_twiceForChangedProvider_returnsCorrectValues() {
    val dataProvider =
      createDataProviderWithResultsQueue(
        "test", AsyncResult.Success("first"), AsyncResult.Success("second")
      )

    val firstResult = monitorFactory.waitForNextSuccessfulResult(dataProvider)
    val secondResult = monitorFactory.waitForNextSuccessfulResult(dataProvider)

    assertThat(firstResult).isEqualTo("first")
    assertThat(secondResult).isEqualTo("second")
  }

  /* Tests for waitForNextFailureResult */

  @Test
  fun testWaitForNextFailureResult_pendingDataProvider_throwsException() {
    val dataProvider = dataProviders.createInMemoryDataProviderAsync<String>("test") {
      AsyncResult.Pending()
    }

    val failure = assertThrows(IllegalStateException::class) {
      monitorFactory.waitForNextFailureResult(dataProvider)
    }

    assertThat(failure).hasMessageThat().contains("Expected next result to be a failure")
  }

  @Test
  fun testWaitForNextFailureResult_failingDataProvider_returnsResult() {
    val dataProvider = dataProviders.createInMemoryDataProviderAsync<String>("test") {
      AsyncResult.Failure(Exception("Failure"))
    }

    val result = monitorFactory.waitForNextFailureResult(dataProvider)

    assertThat(result).hasMessageThat().contains("Failure")
  }

  @Test
  fun testWaitForNextFailureResult_successfulDataProvider_throwsException() {
    val dataProvider = dataProviders.createInMemoryDataProviderAsync("test") {
      AsyncResult.Success("str value")
    }

    val failure = assertThrows(IllegalStateException::class) {
      monitorFactory.waitForNextFailureResult(dataProvider)
    }

    assertThat(failure).hasMessageThat().contains("Expected next result to be a failure")
  }

  @Test
  fun testWaitForNextFailureResult_successThenFailure_consumed_returnsLatest() {
    val dataProvider =
      createDataProviderWithResultsQueue(
        "test", AsyncResult.Success("str value"), AsyncResult.Failure(Exception("Failure"))
      )
    monitorFactory.waitForNextSuccessfulResult(dataProvider)

    val result = monitorFactory.waitForNextFailureResult(dataProvider)

    assertThat(result).hasMessageThat().contains("Failure")
  }

  @Test
  fun testWaitForNextFailureResult_failureThenSuccess_consumed_throwsException() {
    val dataProvider =
      createDataProviderWithResultsQueue(
        "test", AsyncResult.Failure(Exception("Failure")), AsyncResult.Success("str value")
      )
    monitorFactory.waitForNextFailureResult(dataProvider)

    val failure = assertThrows(IllegalStateException::class) {
      monitorFactory.waitForNextFailureResult(dataProvider)
    }

    assertThat(failure).hasMessageThat().contains("Expected next result to be a failure")
  }

  @Test
  fun testWaitForNextFailureResult_differentValues_consumed_returnsLatest() {
    val dataProvider =
      createDataProviderWithResultsQueue<String>(
        "test", AsyncResult.Failure(Exception("First")), AsyncResult.Failure(Exception("Second"))
      )
    monitorFactory.waitForNextFailureResult(dataProvider)

    val result = monitorFactory.waitForNextFailureResult(dataProvider)

    assertThat(result).hasMessageThat().contains("Second")
  }

  @Test
  fun testWaitForNextFailureResult_twiceForChangedProvider_returnsCorrectValues() {
    val dataProvider =
      createDataProviderWithResultsQueue<String>(
        "test", AsyncResult.Failure(Exception("First")), AsyncResult.Failure(Exception("Second"))
      )

    val firstResult = monitorFactory.waitForNextFailureResult(dataProvider)
    val secondResult = monitorFactory.waitForNextFailureResult(dataProvider)

    assertThat(firstResult).hasMessageThat().contains("First")
    assertThat(secondResult).hasMessageThat().contains("Second")
  }

  private fun <T> createDataProviderWithResultsQueue(
    id: Any,
    vararg results: AsyncResult<T>
  ): DataProvider<T> {
    val resultsQueue = createResultQueue(*results)
    return dataProviders.createInMemoryDataProviderAsync(id) {
      resultsQueue.removeFirst()
    }
  }

  private fun <T> createResultQueue(vararg results: AsyncResult<T>): ArrayDeque<AsyncResult<T>> {
    return ArrayDeque(results.toList())
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  // TODO(#89): Move this to a common test application component.
  @Module
  class TestModule {
    @Provides
    @Singleton
    fun provideContext(application: Application): Context {
      return application
    }
  }

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(
    modules = [
      TestModule::class, LogStorageModule::class, NetworkConnectionUtilDebugModule::class,
      TestLogReportingModule::class, LoggerModule::class, TestDispatcherModule::class,
      LocaleProdModule::class, FakeOppiaClockModule::class, RobolectricModule::class
    ]
  )
  interface TestApplicationComponent : DataProvidersInjector {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun inject(dataProviderTestMonitorTest: DataProviderTestMonitorTest)
  }

  class TestApplication : Application(), DataProvidersInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerDataProviderTestMonitorTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(dataProviderTestMonitorTest: DataProviderTestMonitorTest) {
      component.inject(dataProviderTestMonitorTest)
    }

    override fun getDataProvidersInjector(): DataProvidersInjector = component
  }
}
