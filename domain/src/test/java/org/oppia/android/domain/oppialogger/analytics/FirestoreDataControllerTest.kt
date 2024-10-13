package org.oppia.android.domain.oppialogger.analytics

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.app.model.EventLog
import org.oppia.android.app.model.OppiaEventLogs
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.SurveyQuestionName
import org.oppia.android.data.persistence.PersistentCacheStore
import org.oppia.android.domain.oppialogger.FirestoreLogStorageCacheSize
import org.oppia.android.domain.platformparameter.PlatformParameterModule
import org.oppia.android.domain.platformparameter.PlatformParameterSingletonModule
import org.oppia.android.testing.FakeFirestoreEventLogger
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.data.DataProviderTestMonitor
import org.oppia.android.testing.firebase.TestAuthenticationModule
import org.oppia.android.testing.logging.EventLogSubject.Companion.assertThat
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClock
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.data.AsyncResult
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
import org.oppia.android.util.threading.BackgroundDispatcher
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Suppress("FunctionName", "SameParameterValue")
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
  lateinit var fakeFirestoreEventLogger: FakeFirestoreEventLogger

  @Inject
  lateinit var networkConnectionUtil: NetworkConnectionDebugUtil

  @Inject
  lateinit var monitorFactory: DataProviderTestMonitor.Factory

  @field:[Inject BackgroundDispatcher]
  lateinit var backgroundDispatcher: CoroutineDispatcher

  @Inject
  lateinit var persistentCacheStoryFactory: PersistentCacheStore.Factory

  private val profileId by lazy {
    ProfileId.newBuilder().apply { loggedInInternalProfileId = 0 }.build()
  }

  private val dataController by lazy { dataControllerProvider.get() }

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testController_logEvent_withOptionalSurveyQuestionContext_checkLogsEvent() {
    logOptionalSurveyResponseEvent()

    val eventLog = fakeFirestoreEventLogger.getMostRecentEvent()

    assertThat(eventLog).hasTimestampThat().isEqualTo(TEST_TIMESTAMP)
    assertThat(eventLog).isEssentialPriority()
    assertThat(eventLog).hasOptionalSurveyResponseContext()
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

    val eventLog = fakeFirestoreEventLogger.getMostRecentEvent()
    assertThat(eventLog.hasProfileId()).isFalse()
  }

  @Test
  fun testController_logEvent_withProfile_includesProfileId() {
    logOptionalSurveyResponseEvent()

    val eventLog = fakeFirestoreEventLogger.getMostRecentEvent()
    assertThat(eventLog.hasProfileId()).isTrue()
    assertThat(eventLog).hasProfileIdThat().isEqualTo(profileId)
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
    assertThat(firstEventLog).hasTimestampThat().isEqualTo(1556094120000)
    assertThat(secondEventLog).hasTimestampThat().isEqualTo(1556094100000)
  }

  @Test
  fun testController_uploadEventLogs_noLogs_cacheUnchanged() {
    setUpTestApplicationComponent()
    val monitor = monitorFactory.createMonitor(dataController.getEventLogStore())

    runSynchronously { dataController.uploadData() }

    val logs = monitor.ensureNextResultIsSuccess()
    assertThat(logs.eventLogsToUploadList).isEmpty()
    assertThat(logs.uploadedEventLogsList).isEmpty()
  }

  @Test
  fun testController_uploadEventLogs_withPreviousLogs_recordsEventsAsUploaded() {
    setUpTestApplicationComponent()
    logTwoEvents()

    runSynchronously { dataController.uploadData() }

    assertThat(fakeFirestoreEventLogger.getEventListCount()).isEqualTo(2)
  }

  @Test
  fun testController_uploadEventLogs_withLogs_recordsEventsAsUploaded() {
    setUpTestApplicationComponent()
    logTwoEventsOffline()

    runSynchronously { dataController.uploadData() }

    assertThat(fakeFirestoreEventLogger.getEventListCount()).isEqualTo(2)
  }

  @Test
  fun testController_uploadEventLogsAndWait_noLogs_cacheUnchanged() {
    setUpTestApplicationComponent()
    val monitor = monitorFactory.createMonitor(dataController.getEventLogStore())

    runSynchronously { dataController.uploadData() }

    val logs = monitor.ensureNextResultIsSuccess()
    assertThat(logs.eventLogsToUploadList).isEmpty()
    assertThat(logs.uploadedEventLogsList).isEmpty()
  }

  @Test
  fun testController_cachedEventsFromLastAppInstance_logNewEvent_thenForceSync_everythingUploads() {
    // Simulate events being logged in a previous instance of the app.
    logTwoCachedEventsDirectlyOnDisk()

    dataController.logEvent(
      createAbandonSurveyContext(
        TEST_SURVEY_ID,
        profileId,
        SurveyQuestionName.MARKET_FIT
      ),
      profileId = profileId
    )

    runSynchronously { dataController.uploadData() }

    testCoroutineDispatchers.runCurrent()

    // The force sync should ensure everything is uploaded. NOTE TO DEVELOPER: If this test is
    // failing, it may be due to FirestoreDataController being created before
    // logTwoCachedEventsDirectlyOnDisk is called above. If that's the case, use the indirect
    // injection pattern at the top of the test suite (for FirestoreDataController itself) to ensure
    // whichever dependency is injecting FirestoreDataController is also only injected when needed
    // (i.e. using a Provider).
    assertThat(fakeFirestoreEventLogger.getEventListCount()).isEqualTo(3)
  }

  @Test
  fun testController_uploadEventLogs_onNetworkRestore_removesAllEventLogsFromStore() {
    setUpTestApplicationComponent()

    logTwoEventsOffline()

    runSynchronously { dataController.uploadData() }

    val logsProvider = dataController.getEventLogStore()

    val eventLogs = monitorFactory.waitForNextSuccessfulResult(logsProvider)

    assertThat(eventLogs.eventLogsToUploadList).hasSize(0)
  }

  @Test
  fun testController_uploadEventLogs_withNetworkConnection_removesAllEventLogsFromStore() {
    setUpTestApplicationComponent()

    logTwoEvents()

    runSynchronously { dataController.uploadData() }

    val logsProvider = dataController.getEventLogStore()

    val eventLogs = monitorFactory.waitForNextSuccessfulResult(logsProvider)

    assertThat(eventLogs.eventLogsToUploadList).hasSize(0)
  }

  private fun createAbandonSurveyContext(
    surveyId: String,
    profileId: ProfileId,
    questionName: SurveyQuestionName
  ): EventLog.Context {
    return EventLog.Context.newBuilder()
      .setAbandonSurvey(
        EventLog.AbandonSurveyContext.newBuilder()
          .setQuestionName(questionName)
          .setSurveyDetails(
            createSurveyResponseContext(surveyId, profileId)
          )
      )
      .build()
  }

  private fun runSynchronously(operation: suspend () -> Unit) =
    CoroutineScope(backgroundDispatcher).async { operation() }.waitForSuccessfulResult()

  private fun logTwoEvents() {
    logOptionalSurveyResponseEvent()
    logOptionalSurveyResponseEvent(timestamp = 1556094110000)
  }

  private fun logTwoEventsOffline() {
    networkConnectionUtil.setCurrentConnectionStatus(
      NetworkConnectionUtil.ProdConnectionStatus.NONE
    )
    logTwoEvents()
    networkConnectionUtil.setCurrentConnectionStatus(
      NetworkConnectionUtil.ProdConnectionStatus.LOCAL
    )
  }

  private fun logTwoCachedEventsDirectlyOnDisk() {
    persistentCacheStoryFactory.create(
      "firestore_data", OppiaEventLogs.getDefaultInstance()
    ).storeDataAsync {
      OppiaEventLogs.newBuilder().apply {
        addEventLogsToUpload(
          createEventLog(
            context = createOptionalSurveyResponseContext(
              surveyId = TEST_SURVEY_ID,
              profileId = profileId,
              answer = TEST_ANSWER
            )
          )
        )
        addEventLogsToUpload(
          createEventLog(
            context = createOptionalSurveyResponseContext(
              surveyId = TEST_SURVEY_ID,
              profileId = profileId,
              answer = TEST_ANSWER
            )
          )
        )
      }.build()
    }.waitForSuccessfulResult()
  }

  private fun <T> Deferred<T>.waitForSuccessfulResult() {
    return when (val result = waitForResult()) {
      is AsyncResult.Pending -> error("Deferred never finished.")
      is AsyncResult.Success -> {} // Nothing to do; the result succeeded.
      is AsyncResult.Failure -> throw IllegalStateException("Deferred failed", result.error)
    }
  }

  private fun <T> Deferred<T>.waitForResult() = toStateFlow().waitForLatestValue()

  private fun <T> Deferred<T>.toStateFlow(): StateFlow<AsyncResult<T>> {
    val deferred = this
    return MutableStateFlow<AsyncResult<T>>(value = AsyncResult.Pending()).also { flow ->
      CoroutineScope(backgroundDispatcher).async {
        flow.emit(AsyncResult.Success(deferred.await()))
      }.invokeOnCompletion {
        it?.let { flow.tryEmit(AsyncResult.Failure(it)) }
      }
    }
  }

  private fun <T> StateFlow<T>.waitForLatestValue(): T =
    also { testCoroutineDispatchers.runCurrent() }.value

  private fun createEventLog(
    context: EventLog.Context,
    priority: EventLog.Priority = EventLog.Priority.ESSENTIAL,
    timestamp: Long = oppiaClock.getCurrentTimeMs()
  ) = EventLog.newBuilder().apply {
    this.timestamp = timestamp
    this.priority = priority
    this.context = context
  }.build()

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
      .setProfileId(profileId?.loggedInInternalProfileId.toString())
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
      CpuPerformanceSnapshotterModule::class, TestAuthenticationModule::class,
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
