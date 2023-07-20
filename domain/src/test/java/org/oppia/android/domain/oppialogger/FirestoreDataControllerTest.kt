package org.oppia.android.domain.oppialogger

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.oppia.android.app.model.EventLog
import org.oppia.android.app.model.ProfileId
import org.oppia.android.domain.oppialogger.analytics.ApplicationLifecycleModule
import org.oppia.android.domain.oppialogger.analytics.CpuPerformanceSnapshotterModule
import org.oppia.android.domain.oppialogger.firestoreuploader.FirestoreDataController
import org.oppia.android.domain.platformparameter.PlatformParameterModule
import org.oppia.android.domain.platformparameter.PlatformParameterSingletonModule
import org.oppia.android.testing.FakeFirestoreDataUploader
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.data.DataProviderTestMonitor
import org.oppia.android.testing.logging.EventLogSubject
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClock
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.data.DataProvidersInjector
import org.oppia.android.util.data.DataProvidersInjectorProvider
import org.oppia.android.util.locale.LocaleProdModule
import org.oppia.android.util.logging.EnableConsoleLog
import org.oppia.android.util.logging.EnableFileLog
import org.oppia.android.util.logging.GlobalLogLevel
import org.oppia.android.util.logging.LogLevel
import org.oppia.android.util.logging.SyncStatusModule
import org.oppia.android.util.networking.NetworkConnectionDebugUtil
import org.oppia.android.util.networking.NetworkConnectionUtil
import org.oppia.android.util.networking.NetworkConnectionUtilDebugModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

/** Tests for [FirestoreDataController]. */
@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = FirestoreDataControllerTest.TestApplication::class)
class FirestoreDataControllerTest {
  @Inject
  lateinit var dataControllerProvider: Provider<FirestoreDataController>

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @Inject
  lateinit var oppiaClock: FakeOppiaClock

  @Inject
  lateinit var fakeFirestoreDataUploader: FakeFirestoreDataUploader

  @Inject
  lateinit var networkConnectionUtil: NetworkConnectionDebugUtil

  @Inject
  lateinit var monitorFactory: DataProviderTestMonitor.Factory

  lateinit var context: Context

  private val profileId by lazy { ProfileId.newBuilder().apply { internalId = 0 }.build() }
  private val dataController by lazy { dataControllerProvider.get() }
  private lateinit var firebaseAuth: FirebaseAuth

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
    context = InstrumentationRegistry.getInstrumentation().targetContext
    FirebaseApp.initializeApp(context)
    firebaseAuth = mock(FirebaseAuth::class.java)
    oppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_FIXED_FAKE_TIME)
  }

  @Test
  fun testController_logEvent_withOptionalSurveyQuestionContext_checkLogsEvent() {
    logOptionalSurveyResponseEvent()

    val eventLog = fakeFirestoreDataUploader.getMostRecentEvent()
    EventLogSubject.assertThat(eventLog).hasTimestampThat().isEqualTo(TEST_TIMESTAMP)
    EventLogSubject.assertThat(eventLog).isEssentialPriority()
    EventLogSubject.assertThat(eventLog).hasOptionalSurveyResponseContext()
  }

  @Test
  fun testController_logEvent_noProfile_hasNoProfileId() {
    dataController.logEvent(
      createOptionalSurveyResponseContext(
        surveyId = TEST_SURVEY_ID,
        profileId = null,
        answer = TEST_ANSWER
      ),
      profileId = null,
      TEST_TIMESTAMP
    )
    testCoroutineDispatchers.runCurrent()

    val eventLog = fakeFirestoreDataUploader.getMostRecentEvent()
    assertThat(eventLog.hasProfileId()).isFalse()
  }

  @Test
  fun testController_logEvent_withProfile_includesProfileId() {
    logOptionalSurveyResponseEvent()

    val eventLog = fakeFirestoreDataUploader.getMostRecentEvent()
    assertThat(eventLog.hasProfileId()).isTrue()
    EventLogSubject.assertThat(eventLog).hasProfileIdThat().isEqualTo(profileId)
  }

  @Test
  fun testController_logEvent_withNoNetwork_exceedLimit_checkEventLogStoreSize() {
    networkConnectionUtil.setCurrentConnectionStatus(
      NetworkConnectionUtil.ProdConnectionStatus.NONE
    )
    logFourEvents()

    val eventLogsProvider = dataController.getEventLogStore()

    val eventLogs = monitorFactory.waitForNextSuccessfulResult(eventLogsProvider)
    assertThat(eventLogs.eventLogsToUploadList).hasSize(2)
    assertThat(eventLogs.uploadedEventLogsList).isEmpty()
  }

  @Test
  fun testController_logEvents_exceedLimit_withNoNetwork_checkCorrectEventIsEvicted() {
    networkConnectionUtil.setCurrentConnectionStatus(
      NetworkConnectionUtil.ProdConnectionStatus.NONE
    )
    logFourEvents()

    val logsProvider = dataController.getEventLogStore()

    val eventLogs = monitorFactory.waitForNextSuccessfulResult(logsProvider)
    val firstEventLog = eventLogs.getEventLogsToUpload(0)
    val secondEventLog = eventLogs.getEventLogsToUpload(1)
    assertThat(eventLogs.eventLogsToUploadList).hasSize(2)

    // The pruning will be purely based on timestamp of the event as all the event logs have 
    // ESSENTIAL priority. 
    EventLogSubject.assertThat(firstEventLog).hasTimestampThat().isEqualTo(1556094120000)
    EventLogSubject.assertThat(secondEventLog).hasTimestampThat().isEqualTo(1556094100000)
  }

  private fun logFourEvents() {
    logOptionalSurveyResponseEvent(timestamp = 1556094120000)
    logOptionalSurveyResponseEvent(timestamp = 1556094110000)
    logOptionalSurveyResponseEvent(timestamp = 1556093100000)
    logOptionalSurveyResponseEvent(timestamp = 1556094100000)
  }

  private fun logOptionalSurveyResponseEvent(timestamp: Long = TEST_TIMESTAMP) {
    dataController.logEvent(
      createOptionalSurveyResponseContext(
        surveyId = TEST_SURVEY_ID,
        profileId = profileId,
        answer = TEST_ANSWER
      ),
      profileId,
      timestamp
    )
    testCoroutineDispatchers.runCurrent()
  }

  private fun createOptionalSurveyResponseContext(
    surveyId: String,
    profileId: ProfileId?,
    answer: String
  ): EventLog.Context {
    return EventLog.Context.newBuilder()
      .setOptionalResponse(
        EventLog.OptionalSurveyResponseContext.newBuilder()
          .setFeedbackAnswer(answer)
          .setSurveyDetails(
            createSurveyResponseContext(surveyId, profileId)
          )
      )
      .build()
  }

  private fun createSurveyResponseContext(
    surveyId: String,
    profileId: ProfileId?
  ): EventLog.SurveyResponseContext {
    return EventLog.SurveyResponseContext.newBuilder()
      .setProfileId(profileId?.internalId.toString())
      .setSurveyId(surveyId)
      .build()
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  private companion object {
    private const val TEST_SURVEY_ID = "test_survey_id"
    private const val TEST_ANSWER = "Some text response"
    private const val TEST_TIMESTAMP = 1556094120000
  }

  // TODO(#89): Move this to a common test application component.
  @Module
  class TestModule {
    @Provides
    @Singleton
    fun provideContext(application: Application): Context {
      return application
    }

    // TODO(#59): Either isolate these to their own shared test module, or use the real logging
    // module in tests to avoid needing to specify these settings for tests.
    @EnableConsoleLog
    @Provides
    fun provideEnableConsoleLog(): Boolean = true

    @EnableFileLog
    @Provides
    fun provideEnableFileLog(): Boolean = false

    @GlobalLogLevel
    @Provides
    fun provideGlobalLogLevel(): LogLevel = LogLevel.VERBOSE
  }

  @Module
  class TestLogStorageModule {
    @Provides
    @FirestoreLogStorageCacheSize
    fun provideFirestoreLogStorageCacheSize(): Int = 2
  }

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(
    modules = [
      TestModule::class, TestLogReportingModule::class, TestLogStorageModule::class,
      TestDispatcherModule::class, RobolectricModule::class, FakeOppiaClockModule::class,
      NetworkConnectionUtilDebugModule::class, LocaleProdModule::class,
      PlatformParameterSingletonModule::class, SyncStatusModule::class,
      ApplicationLifecycleModule::class, PlatformParameterModule::class,
      CpuPerformanceSnapshotterModule::class
    ]
  )
  interface TestApplicationComponent : DataProvidersInjector {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder
      fun build(): TestApplicationComponent
    }

    fun inject(dataControllerTest: FirestoreDataControllerTest)
  }

  class TestApplication : Application(), DataProvidersInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerFirestoreDataControllerTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(dataControllerTest: FirestoreDataControllerTest) {
      component.inject(dataControllerTest)
    }

    override fun getDataProvidersInjector(): DataProvidersInjector = component
  }
}
