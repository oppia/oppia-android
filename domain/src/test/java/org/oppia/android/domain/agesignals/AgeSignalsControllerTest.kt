package org.oppia.android.domain.agesignals

import android.app.Application
import android.os.Looper
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.android.play.agesignals.AgeSignalsException
import com.google.android.play.agesignals.AgeSignalsManager
import com.google.android.play.agesignals.AgeSignalsRequest
import com.google.android.play.agesignals.AgeSignalsResult
import com.google.android.play.agesignals.model.AgeSignalsErrorCode
import com.google.android.play.agesignals.model.AgeSignalsVerificationStatus
import com.google.android.play.agesignals.testing.FakeAgeSignalsManager
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.Dispatchers
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.util.locale.OppiaLocale
import org.oppia.android.util.logging.ConsoleLogger
import org.oppia.android.util.logging.LogLevel
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import org.robolectric.shadows.ShadowLog

/**
 * Tests for [AgeSignalsController].
 *
 * Uses the SDK's FakeAgeSignalsManager for response and documented API error coverage. Mocks
 * control pending requests and synchronous failures, which the fake does not model. The real
 * [OppiaLogger] verifies that diagnostics contain only fixed messages, with no signal values or
 * exception details. No production Play manager is created by these tests.
 */
@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(sdk = [28])
class AgeSignalsControllerTest {
  private lateinit var oppiaLogger: OppiaLogger

  @Before
  fun setUp() {
    // Exercise Oppia's real console logging; file logging is disabled for these focused tests.
    val consoleLogger = ConsoleLogger(
      ApplicationProvider.getApplicationContext<Application>(),
      Dispatchers.Unconfined,
      true,
      false,
      LogLevel.VERBOSE,
      mock(OppiaLocale.MachineLocale::class.java)
    )
    oppiaLogger = OppiaLogger(consoleLogger)
    ShadowLog.reset()
  }

  @Test
  fun testOnCreateStarted_doesNotCreateManager() {
    var managerRequested = false
    var loggerRequested = false
    val controller = AgeSignalsController(
      {
        managerRequested = true
        mock(AgeSignalsManager::class.java)
      },
      {
        loggerRequested = true
        oppiaLogger
      }
    )

    controller.onCreateStarted()

    assertThat(managerRequested).isFalse()
    assertThat(loggerRequested).isFalse()
    assertThat(ShadowLog.getLogsForTag("AgeSignalsController")).isEmpty()
  }

  @Test
  fun testCompletedInitialization_requestsSignalsWithoutWaitingForResult() {
    val manager = mock(AgeSignalsManager::class.java)
    val completion = TaskCompletionSource<AgeSignalsResult>()
    `when`(manager.checkAgeSignals(any(AgeSignalsRequest::class.java)))
      .thenReturn(completion.task)
    val controller = AgeSignalsController({ manager }, { oppiaLogger })

    controller.onCompletedInitialization()

    verify(manager).checkAgeSignals(any(AgeSignalsRequest::class.java))
    assertThat(completion.task.isComplete).isFalse()
  }

  @Test
  fun testCompletedInitialization_failedRequest_doesNotThrow() {
    val manager = mock(AgeSignalsManager::class.java)
    val completion = TaskCompletionSource<AgeSignalsResult>()
    `when`(manager.checkAgeSignals(any(AgeSignalsRequest::class.java)))
      .thenReturn(completion.task)
    val controller = AgeSignalsController({ manager }, { oppiaLogger })
    controller.onCompletedInitialization()

    completion.setException(IllegalStateException("Service unavailable"))
    shadowOf(Looper.getMainLooper()).idle()

    assertThat(ShadowLog.getLogsForTag("AgeSignalsController").map { it.msg })
      .containsExactly("Age signals request failed.")
  }

  @Test
  fun testCompletedInitialization_managerCreationThrows_doesNotThrow() {
    val controller = AgeSignalsController(
      {
        throw IllegalStateException("Service unavailable")
      },
      { oppiaLogger }
    )

    controller.onCompletedInitialization()

    assertThat(ShadowLog.getLogsForTag("AgeSignalsController").map { it.msg })
      .containsExactly("Age signals request could not start.")
  }

  @Test
  fun testCompletedInitialization_requestThrows_doesNotThrow() {
    val manager = mock(AgeSignalsManager::class.java)
    `when`(manager.checkAgeSignals(any(AgeSignalsRequest::class.java)))
      .thenThrow(IllegalStateException("Service unavailable"))
    val controller = AgeSignalsController({ manager }, { oppiaLogger })

    controller.onCompletedInitialization()

    assertThat(ShadowLog.getLogsForTag("AgeSignalsController").map { it.msg })
      .containsExactly("Age signals request could not start.")
  }
  @Test
  fun testCompletedInitialization_verified_logsOnlyOutcome() {
    checkSuccessfulResponse(AgeSignalsVerificationStatus.VERIFIED)
  }

  @Test
  fun testCompletedInitialization_declared_logsOnlyOutcome() {
    checkSuccessfulResponse(AgeSignalsVerificationStatus.DECLARED)
  }

  @Test
  fun testCompletedInitialization_supervised_logsOnlyOutcome() {
    checkSuccessfulResponse(AgeSignalsVerificationStatus.SUPERVISED)
  }

  @Test
  fun testCompletedInitialization_supervised_approval_pending_logsOnlyOutcome() {
    checkSuccessfulResponse(AgeSignalsVerificationStatus.SUPERVISED_APPROVAL_PENDING)
  }

  @Test
  fun testCompletedInitialization_supervised_approval_denied_logsOnlyOutcome() {
    checkSuccessfulResponse(AgeSignalsVerificationStatus.SUPERVISED_APPROVAL_DENIED)
  }

  @Test
  fun testCompletedInitialization_unknown_logsOnlyOutcome() {
    checkSuccessfulResponse(AgeSignalsVerificationStatus.UNKNOWN)
  }

  @Test
  fun testCompletedInitialization_nullStatusAndFields_logsOnlyOutcome() {
    val manager = FakeAgeSignalsManager()
    manager.setNextAgeSignalsResult(AgeSignalsResult.builder().build())
    val controller = AgeSignalsController({ manager }, { oppiaLogger })

    controller.onCompletedInitialization()
    shadowOf(Looper.getMainLooper()).idle()

    assertThat(ShadowLog.getLogsForTag("AgeSignalsController").map { it.msg })
      .containsExactly("Age signals request completed.")
  }

  @Test
  fun testCompletedInitialization_api_not_available_continuesStartup() {
    checkFailedResponse(AgeSignalsErrorCode.API_NOT_AVAILABLE)
  }

  @Test
  fun testCompletedInitialization_play_store_not_found_continuesStartup() {
    checkFailedResponse(AgeSignalsErrorCode.PLAY_STORE_NOT_FOUND)
  }

  @Test
  fun testCompletedInitialization_network_error_continuesStartup() {
    checkFailedResponse(AgeSignalsErrorCode.NETWORK_ERROR)
  }

  @Test
  fun testCompletedInitialization_play_services_not_found_continuesStartup() {
    checkFailedResponse(AgeSignalsErrorCode.PLAY_SERVICES_NOT_FOUND)
  }

  @Test
  fun testCompletedInitialization_cannot_bind_to_service_continuesStartup() {
    checkFailedResponse(AgeSignalsErrorCode.CANNOT_BIND_TO_SERVICE)
  }

  @Test
  fun testCompletedInitialization_play_store_version_outdated_continuesStartup() {
    checkFailedResponse(AgeSignalsErrorCode.PLAY_STORE_VERSION_OUTDATED)
  }

  @Test
  fun testCompletedInitialization_play_services_version_outdated_continuesStartup() {
    checkFailedResponse(AgeSignalsErrorCode.PLAY_SERVICES_VERSION_OUTDATED)
  }

  @Test
  fun testCompletedInitialization_client_transient_error_continuesStartup() {
    checkFailedResponse(AgeSignalsErrorCode.CLIENT_TRANSIENT_ERROR)
  }

  @Test
  fun testCompletedInitialization_app_not_owned_continuesStartup() {
    checkFailedResponse(AgeSignalsErrorCode.APP_NOT_OWNED)
  }

  @Test
  fun testCompletedInitialization_sdk_version_outdated_continuesStartup() {
    checkFailedResponse(AgeSignalsErrorCode.SDK_VERSION_OUTDATED)
  }

  @Test
  fun testCompletedInitialization_internal_error_continuesStartup() {
    checkFailedResponse(AgeSignalsErrorCode.INTERNAL_ERROR)
  }

  @Test
  fun testCompletedInitialization_missingSdkClass_continuesStartup() {
    val controller = AgeSignalsController(
      { throw NoClassDefFoundError("Sensitive SDK detail") },
      { oppiaLogger }
    )

    controller.onCompletedInitialization()

    assertThat(ShadowLog.getLogsForTag("AgeSignalsController").map { it.msg })
      .containsExactly("Age signals SDK unavailable.")
  }

  @Test
  fun testCompletedInitialization_requestLinkageError_continuesStartup() {
    val manager = mock(AgeSignalsManager::class.java)
    `when`(manager.checkAgeSignals(any(AgeSignalsRequest::class.java)))
      .thenThrow(NoClassDefFoundError("Sensitive SDK detail"))
    val controller = AgeSignalsController({ manager }, { oppiaLogger })

    controller.onCompletedInitialization()

    assertThat(ShadowLog.getLogsForTag("AgeSignalsController").map { it.msg })
      .containsExactly("Age signals SDK unavailable.")
  }

  @Test
  fun testCompletedInitialization_successLoggerThrows_doesNotCrashCallback() {
    val manager = FakeAgeSignalsManager()
    manager.setNextAgeSignalsResult(AgeSignalsResult.builder().build())
    val controller = AgeSignalsController(
      { manager },
      { throw IllegalStateException("Logger unavailable") }
    )

    controller.onCompletedInitialization()
    shadowOf(Looper.getMainLooper()).idle()
  }

  @Test
  fun testCompletedInitialization_failureLoggerLinkageError_doesNotCrashCallback() {
    val manager = FakeAgeSignalsManager()
    manager.setNextAgeSignalsException(AgeSignalsException(AgeSignalsErrorCode.NETWORK_ERROR))
    val controller = AgeSignalsController(
      { manager },
      { throw NoClassDefFoundError("Logger unavailable") }
    )

    controller.onCompletedInitialization()
    shadowOf(Looper.getMainLooper()).idle()
  }

  private fun checkSuccessfulResponse(status: Int) {
    val manager = FakeAgeSignalsManager()
    manager.setNextAgeSignalsResult(
      AgeSignalsResult.builder()
        .setUserStatus(status)
        .setAgeLower(13)
        .setAgeUpper(15)
        .setInstallId("sensitive-test-install-id")
        .setMostRecentApprovalDate(java.util.Date(123456789L))
        .build()
    )
    val controller = AgeSignalsController({ manager }, { oppiaLogger })

    controller.onCompletedInitialization()
    shadowOf(Looper.getMainLooper()).idle()

    assertThat(ShadowLog.getLogsForTag("AgeSignalsController").map { it.msg })
      .containsExactly("Age signals request completed.")
    assertThat(ShadowLog.getLogsForTag("AgeSignalsController").map { it.type })
      .containsExactly(android.util.Log.DEBUG)
  }

  private fun checkFailedResponse(errorCode: Int) {
    val manager = FakeAgeSignalsManager()
    manager.setNextAgeSignalsException(AgeSignalsException(errorCode))
    val controller = AgeSignalsController({ manager }, { oppiaLogger })
    var nextListenerCalled = false
    val nextListener = object : org.oppia.android.domain.oppialogger.ApplicationStartupListener {
      override fun onCreateStarted() {}
      override fun onCompletedInitialization() {
        nextListenerCalled = true
      }
    }

    listOf(controller, nextListener).forEach { it.onCompletedInitialization() }
    shadowOf(Looper.getMainLooper()).idle()

    assertThat(nextListenerCalled).isTrue()
    assertThat(ShadowLog.getLogsForTag("AgeSignalsController").map { it.msg })
      .containsExactly("Age signals request failed.")
    assertThat(ShadowLog.getLogsForTag("AgeSignalsController").map { it.type })
      .containsExactly(android.util.Log.WARN)
  }
}
