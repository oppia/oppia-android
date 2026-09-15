package org.oppia.android.domain.agesignals

import android.app.Application
import android.content.Context
import android.os.Looper
import android.util.Log
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.android.play.agesignals.AgeSignalsException
import com.google.android.play.agesignals.AgeSignalsManager
import com.google.android.play.agesignals.AgeSignalsRequest
import com.google.android.play.agesignals.AgeSignalsResult
import com.google.android.play.agesignals.model.AgeSignalsErrorCode
import com.google.android.play.agesignals.model.AgeSignalsVerificationStatus
import com.google.android.play.agesignals.testing.FakeAgeSignalsManager
import com.google.common.truth.Truth.assertThat
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.domain.oppialogger.ApplicationStartupListener
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.data.DataProvidersInjector
import org.oppia.android.util.data.DataProvidersInjectorProvider
import org.oppia.android.util.locale.testing.LocaleTestModule
import org.oppia.android.util.logging.EnableConsoleLog
import org.oppia.android.util.logging.EnableFileLog
import org.oppia.android.util.logging.GlobalLogLevel
import org.oppia.android.util.logging.LogLevel
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import org.robolectric.shadows.ShadowLog
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Tests for [AgeSignalsController] using an injected controller and Oppia's test application graph.
 *
 * [FakeAgeSignalsManager] supplies SDK responses and documented API errors. A test-only adapter
 * controls pending requests and synchronous SDK failures that the SDK fake does not represent.
 * Logging uses the real Oppia logger with test dispatchers and locale bindings. The production
 * manager module is excluded so these tests never connect to Google Play.
 */
@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = AgeSignalsControllerTest.TestApplication::class)
class AgeSignalsControllerTest {
  @Inject lateinit var controller: AgeSignalsController
  @Inject lateinit var fakeAgeSignalsManager: FakeAgeSignalsManager
  @Inject lateinit var testAgeSignalsManager: TestAgeSignalsManager
  @Inject lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
    ShadowLog.reset()
  }

  @Test
  fun testOnCreateStarted_doesNotCreateManager() {
    controller.onCreateStarted()
    runPendingCallbacks()

    assertThat(testAgeSignalsManager.creationCount).isEqualTo(0)
    assertThat(testAgeSignalsManager.requestCount).isEqualTo(0)
    assertThat(ShadowLog.getLogsForTag("AgeSignalsController")).isEmpty()
  }

  @Test
  fun testCompletedInitialization_requestsSignalsWithoutWaitingForResult() {
    val completion = TaskCompletionSource<AgeSignalsResult>()
    testAgeSignalsManager.pendingTask = completion.task

    controller.onCompletedInitialization()
    runPendingCallbacks()

    assertThat(testAgeSignalsManager.creationCount).isEqualTo(1)
    assertThat(testAgeSignalsManager.requestCount).isEqualTo(1)
    assertThat(completion.task.isComplete).isFalse()
    assertThat(ShadowLog.getLogsForTag("AgeSignalsController")).isEmpty()
  }

  @Test
  fun testCompletedInitialization_delayedSuccess_logsWhenRequestCompletes() {
    val completion = TaskCompletionSource<AgeSignalsResult>()
    testAgeSignalsManager.pendingTask = completion.task
    controller.onCompletedInitialization()

    completion.setResult(AgeSignalsResult.builder().build())
    runPendingCallbacks()

    assertOutcome(Log.DEBUG, "Successfully ingested age signals.")
    assertThat(testAgeSignalsManager.requestCount).isEqualTo(1)
  }

  @Test
  fun testCompletedInitialization_managerCreationThrows_continuesStartup() {
    testAgeSignalsManager.creationFailure = IllegalStateException("Service unavailable")

    runStartupWithFollowingListener()

    assertThat(testAgeSignalsManager.requestCount).isEqualTo(0)
    assertOutcome(Log.ERROR, "Age signals request could not start.")
  }

  @Test
  fun testCompletedInitialization_requestThrows_continuesStartup() {
    testAgeSignalsManager.requestFailure = IllegalStateException("Service unavailable")

    runStartupWithFollowingListener()

    assertThat(testAgeSignalsManager.requestCount).isEqualTo(1)
    assertOutcome(Log.ERROR, "Age signals request could not start.")
  }

  @Test
  fun testCompletedInitialization_missingSdkClass_continuesStartup() {
    testAgeSignalsManager.creationFailure = NoClassDefFoundError("SDK unavailable")

    runStartupWithFollowingListener()

    assertThat(testAgeSignalsManager.requestCount).isEqualTo(0)
    assertOutcome(Log.ERROR, "Age signals SDK unavailable.")
  }

  @Test
  fun testCompletedInitialization_requestLinkageError_continuesStartup() {
    testAgeSignalsManager.requestFailure = NoClassDefFoundError("SDK unavailable")

    runStartupWithFollowingListener()

    assertThat(testAgeSignalsManager.requestCount).isEqualTo(1)
    assertOutcome(Log.ERROR, "Age signals SDK unavailable.")
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
    fakeAgeSignalsManager.setNextAgeSignalsResult(AgeSignalsResult.builder().build())

    runStartupWithFollowingListener()

    assertOutcome(Log.DEBUG, "Successfully ingested age signals.")
    assertThat(testAgeSignalsManager.requestCount).isEqualTo(1)
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

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  private fun checkSuccessfulResponse(status: Int) {
    fakeAgeSignalsManager.setNextAgeSignalsResult(
      AgeSignalsResult.builder()
        .setUserStatus(status)
        .setAgeLower(13)
        .setAgeUpper(15)
        .setInstallId("sensitive-test-install-id")
        .setMostRecentApprovalDate(Date(123456789L))
        .build()
    )

    runStartupWithFollowingListener()

    assertOutcome(Log.DEBUG, "Successfully ingested age signals.")
    assertThat(testAgeSignalsManager.requestCount).isEqualTo(1)
  }

  private fun checkFailedResponse(errorCode: Int) {
    fakeAgeSignalsManager.setNextAgeSignalsException(AgeSignalsException(errorCode))

    runStartupWithFollowingListener()

    assertOutcome(Log.ERROR, "Failed to ingest age signals")
    assertThat(testAgeSignalsManager.requestCount).isEqualTo(1)
  }

  private fun runStartupWithFollowingListener() {
    var nextListenerCalled = false
    val nextListener = object : ApplicationStartupListener {
      override fun onCreateStarted() {}
      override fun onCompletedInitialization() {
        nextListenerCalled = true
      }
    }

    listOf(controller, nextListener).forEach { it.onCompletedInitialization() }
    runPendingCallbacks()

    assertThat(nextListenerCalled).isTrue()
  }

  private fun runPendingCallbacks() {
    shadowOf(Looper.getMainLooper()).idle()
    testCoroutineDispatchers.runCurrent()
  }

  private fun assertOutcome(level: Int, message: String) {
    val logs = ShadowLog.getLogsForTag("AgeSignalsController")
    assertThat(logs).hasSize(1)
    assertThat(logs.single().type).isEqualTo(level)
    // Error logging includes the exception on subsequent lines.
    assertThat(logs.single().msg.lineSequence().first()).isEqualTo(message)
    if (level == Log.DEBUG) {
      assertThat(logs.single().msg).isEqualTo(message)
    }
  }

  /** Adds request controls to the SDK fake without contacting Google Play. */
  @Singleton
  class TestAgeSignalsManager @Inject constructor(
    private val fakeAgeSignalsManager: FakeAgeSignalsManager
  ) : AgeSignalsManager {
    var creationCount = 0
    var requestCount = 0
      private set
    var creationFailure: Throwable? = null
    var requestFailure: Throwable? = null
    var pendingTask: Task<AgeSignalsResult>? = null

    override fun checkAgeSignals(request: AgeSignalsRequest): Task<AgeSignalsResult> {
      requestCount++
      requestFailure?.let { throw it }
      return pendingTask ?: fakeAgeSignalsManager.checkAgeSignals(request)
    }
  }

  @Module
  class TestModule {
    @Provides
    @Singleton
    fun provideContext(application: Application): Context = application

    @Provides
    @Singleton
    fun provideFakeAgeSignalsManager(): FakeAgeSignalsManager = FakeAgeSignalsManager()

    @Provides
    fun provideAgeSignalsManager(manager: TestAgeSignalsManager): AgeSignalsManager {
      manager.creationCount++
      manager.creationFailure?.let { throw it }
      return manager
    }

    @Provides
    @EnableConsoleLog
    fun provideEnableConsoleLog(): Boolean = true

    @Provides
    @EnableFileLog
    fun provideEnableFileLog(): Boolean = false

    @Provides
    @GlobalLogLevel
    fun provideGlobalLogLevel(): LogLevel = LogLevel.VERBOSE
  }

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(
    modules = [
      FakeOppiaClockModule::class,
      LocaleTestModule::class,
      RobolectricModule::class,
      TestDispatcherModule::class,
      TestLogReportingModule::class,
      TestModule::class
    ]
  )
  interface TestApplicationComponent : DataProvidersInjector {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun inject(test: AgeSignalsControllerTest)
  }

  class TestApplication : Application(), DataProvidersInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerAgeSignalsControllerTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(test: AgeSignalsControllerTest) {
      component.inject(test)
    }

    override fun getDataProvidersInjector(): DataProvidersInjector = component
  }
}
