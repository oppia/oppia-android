package org.oppia.android.domain.oppialogger.loguploader

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.Configuration
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.WorkManagerTestInitHelper
import com.google.common.truth.Truth.assertThat
import dagger.Binds
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.Mockito.reset
import org.oppia.android.app.model.EventLog
import org.oppia.android.app.model.OppiaMetricLog
import org.oppia.android.app.model.ScreenName.SCREEN_NAME_UNSPECIFIED
import org.oppia.android.domain.oppialogger.EventLogStorageCacheSize
import org.oppia.android.domain.oppialogger.ExceptionLogStorageCacheSize
import org.oppia.android.domain.oppialogger.FirestoreLogStorageCacheSize
import org.oppia.android.domain.oppialogger.LoggingIdentifierModule
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.oppialogger.PerformanceMetricsLogStorageCacheSize
import org.oppia.android.domain.oppialogger.analytics.AnalyticsController
import org.oppia.android.domain.oppialogger.analytics.ApplicationLifecycleModule
import org.oppia.android.domain.oppialogger.analytics.FirestoreDataController
import org.oppia.android.domain.oppialogger.analytics.PerformanceMetricsController
import org.oppia.android.domain.oppialogger.exceptions.ExceptionsController
import org.oppia.android.domain.platformparameter.PlatformParameterSingletonModule
import org.oppia.android.domain.testing.oppialogger.loguploader.FakeLogUploader
import org.oppia.android.testing.FakeAnalyticsEventLogger
import org.oppia.android.testing.FakeExceptionLogger
import org.oppia.android.testing.FakeFirestoreEventLogger
import org.oppia.android.testing.FakePerformanceMetricsEventLogger
import org.oppia.android.testing.data.DataProviderTestMonitor
import org.oppia.android.testing.firebase.TestAuthenticationModule
import org.oppia.android.testing.logging.SyncStatusTestModule
import org.oppia.android.testing.logging.TestSyncStatusManager
import org.oppia.android.testing.mockito.anyOrNull
import org.oppia.android.testing.platformparameter.TestPlatformParameterModule
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.caching.AssetModule
import org.oppia.android.util.data.DataProviders
import org.oppia.android.util.data.DataProvidersInjector
import org.oppia.android.util.data.DataProvidersInjectorProvider
import org.oppia.android.util.locale.LocaleProdModule
import org.oppia.android.util.logging.AnalyticsEventLogger
import org.oppia.android.util.logging.ExceptionLogger
import org.oppia.android.util.logging.LogUploader
import org.oppia.android.util.logging.LoggerModule
import org.oppia.android.util.logging.SyncStatusManager.SyncStatus.DATA_UPLOADED
import org.oppia.android.util.logging.SyncStatusManager.SyncStatus.DATA_UPLOADING
import org.oppia.android.util.logging.SyncStatusManager.SyncStatus.INITIAL_UNKNOWN
import org.oppia.android.util.logging.SyncStatusManager.SyncStatus.NO_CONNECTIVITY
import org.oppia.android.util.logging.SyncStatusManager.SyncStatus.UPLOAD_ERROR
import org.oppia.android.util.logging.firebase.FirestoreEventLogger
import org.oppia.android.util.logging.performancemetrics.PerformanceMetricsAssessorModule
import org.oppia.android.util.logging.performancemetrics.PerformanceMetricsConfigurationsModule
import org.oppia.android.util.logging.performancemetrics.PerformanceMetricsEventLogger
import org.oppia.android.util.networking.NetworkConnectionDebugUtil
import org.oppia.android.util.networking.NetworkConnectionUtil.ProdConnectionStatus.LOCAL
import org.oppia.android.util.networking.NetworkConnectionUtil.ProdConnectionStatus.NONE
import org.oppia.android.util.networking.NetworkConnectionUtilDebugModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Qualifier
import javax.inject.Singleton

private const val TEST_TIMESTAMP = 1556094120000
private const val TEST_TOPIC_ID = "test_topicId"
private const val TEST_APK_SIZE = Long.MAX_VALUE

/** Tests for [LogUploadWorker]. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = LogUploadWorkerTest.TestApplication::class)
class LogUploadWorkerTest {
  @Inject lateinit var networkConnectionUtil: NetworkConnectionDebugUtil
  @Inject lateinit var fakeAnalyticsEventLogger: FakeAnalyticsEventLogger
  @Inject lateinit var fakeExceptionLogger: FakeExceptionLogger
  @Inject lateinit var fakePerformanceMetricsEventLogger: FakePerformanceMetricsEventLogger
  @Inject lateinit var fakeFirestoreEventLogger: FakeFirestoreEventLogger
  @Inject lateinit var oppiaLogger: OppiaLogger
  @Inject lateinit var analyticsController: AnalyticsController
  @Inject lateinit var dataController: FirestoreDataController
  @Inject lateinit var exceptionsController: ExceptionsController
  @Inject lateinit var performanceMetricsController: PerformanceMetricsController
  @Inject lateinit var logUploadWorkerFactory: LogUploadWorkerFactory
  @Inject lateinit var dataProviders: DataProviders
  @Inject lateinit var testCoroutineDispatchers: TestCoroutineDispatchers
  @Inject lateinit var testSyncStatusManager: TestSyncStatusManager
  @Inject lateinit var monitorFactory: DataProviderTestMonitor.Factory
  @field:[Inject MockEventLogger] lateinit var mockAnalyticsEventLogger: AnalyticsEventLogger
  @field:[Inject MockFirestoreEventLogger]
  lateinit var mockFirestoreEventLogger: FirestoreEventLogger

  private lateinit var context: Context

  private val eventLogTopicContext = EventLog.newBuilder()
    .setContext(
      EventLog.Context.newBuilder()
        .setOpenInfoTab(
          EventLog.TopicContext.newBuilder()
            .setTopicId(TEST_TOPIC_ID)
            .build()
        )
        .build()
    )
    .setPriority(EventLog.Priority.ESSENTIAL)
    .setTimestamp(TEST_TIMESTAMP)
    .build()

  private val apkSizeTestLoggableMetric = OppiaMetricLog.LoggableMetric.newBuilder()
    .setApkSizeMetric(
      OppiaMetricLog.ApkSizeMetric.newBuilder()
        .setApkSizeBytes(TEST_APK_SIZE)
        .build()
    ).build()

  private val exception = Exception("TEST")

  @Test
  fun testWorker_logEvent_withoutNetwork_enqueueRequest_verifyFailed() {
    setUpTestApplicationComponent()
    networkConnectionUtil.setCurrentConnectionStatus(NONE)
    analyticsController.logImportantEvent(
      oppiaLogger.createOpenInfoTabContext(TEST_TOPIC_ID),
      profileId = null,
      eventLogTopicContext.timestamp
    )
    testCoroutineDispatchers.runCurrent()

    val workManager = WorkManager.getInstance(ApplicationProvider.getApplicationContext())

    val inputData = Data.Builder().putString(
      LogUploadWorker.WORKER_CASE_KEY,
      LogUploadWorker.EVENT_WORKER
    ).build()

    val request: OneTimeWorkRequest = OneTimeWorkRequestBuilder<LogUploadWorker>()
      .setInputData(inputData)
      .build()

    workManager.enqueue(request)
    testCoroutineDispatchers.runCurrent()
    val workInfo = workManager.getWorkInfoById(request.id)

    // The enqueue should fail since the worker shouldn't be running when there's no network
    // connectivity.
    assertThat(workInfo.get().state).isEqualTo(WorkInfo.State.FAILED)
  }

  @Test
  fun testWorker_logEvent_withNetwork_enqueueRequest_verifySuccess() {
    setUpTestApplicationComponent()
    networkConnectionUtil.setCurrentConnectionStatus(NONE)
    analyticsController.logImportantEvent(
      oppiaLogger.createOpenInfoTabContext(TEST_TOPIC_ID),
      profileId = null,
      eventLogTopicContext.timestamp
    )
    networkConnectionUtil.setCurrentConnectionStatus(LOCAL)
    testCoroutineDispatchers.runCurrent()

    val workManager = WorkManager.getInstance(ApplicationProvider.getApplicationContext())

    val inputData = Data.Builder().putString(
      LogUploadWorker.WORKER_CASE_KEY,
      LogUploadWorker.EVENT_WORKER
    ).build()

    val request: OneTimeWorkRequest = OneTimeWorkRequestBuilder<LogUploadWorker>()
      .setInputData(inputData)
      .build()

    workManager.enqueue(request)
    testCoroutineDispatchers.runCurrent()
    val workInfo = workManager.getWorkInfoById(request.id)

    assertThat(workInfo.get().state).isEqualTo(WorkInfo.State.SUCCEEDED)
    assertThat(fakeAnalyticsEventLogger.getMostRecentEvent()).isEqualTo(eventLogTopicContext)
  }

  @Test
  fun testWorker_logEvent_withoutNetwork_enqueueRequest_writeFails_verifyFailure() {
    setUpTestApplicationComponent()
    networkConnectionUtil.setCurrentConnectionStatus(NONE)
    analyticsController.logImportantEvent(
      oppiaLogger.createOpenInfoTabContext(TEST_TOPIC_ID),
      profileId = null,
      eventLogTopicContext.timestamp
    )
    testCoroutineDispatchers.runCurrent()

    val workManager = WorkManager.getInstance(ApplicationProvider.getApplicationContext())

    val inputData = Data.Builder().putString(
      LogUploadWorker.WORKER_CASE_KEY,
      LogUploadWorker.EVENT_WORKER
    ).build()

    val request: OneTimeWorkRequest = OneTimeWorkRequestBuilder<LogUploadWorker>()
      .setInputData(inputData)
      .build()

    setUpEventLoggerToFail()
    workManager.enqueue(request)
    testCoroutineDispatchers.runCurrent()
    val workInfo = workManager.getWorkInfoById(request.id)

    assertThat(workInfo.get().state).isEqualTo(WorkInfo.State.FAILED)
    assertThat(fakeAnalyticsEventLogger.noEventsPresent()).isTrue()
  }

  @Test
  fun testWorker_logException_withoutNetwork_enqueueRequest_verifySuccess() {
    setUpTestApplicationComponent()
    networkConnectionUtil.setCurrentConnectionStatus(NONE)
    exceptionsController.logNonFatalException(exception, TEST_TIMESTAMP)
    testCoroutineDispatchers.runCurrent()

    val workManager = WorkManager.getInstance(ApplicationProvider.getApplicationContext())

    val inputData = Data.Builder().putString(
      LogUploadWorker.WORKER_CASE_KEY,
      LogUploadWorker.EXCEPTION_WORKER
    ).build()

    val request: OneTimeWorkRequest = OneTimeWorkRequestBuilder<LogUploadWorker>()
      .setInputData(inputData)
      .build()
    workManager.enqueue(request)
    testCoroutineDispatchers.runCurrent()

    val workInfo = workManager.getWorkInfoById(request.id)
    val loggedException = fakeExceptionLogger.getMostRecentException()
    val loggedExceptionStackTraceElems = loggedException.stackTrace.extractRelevantDetails()
    val expectedExceptionStackTraceElems = exception.stackTrace.extractRelevantDetails()
    assertThat(workInfo.get().state).isEqualTo(WorkInfo.State.SUCCEEDED)
    assertThat(loggedException.message).isEqualTo("TEST")
    assertThat(loggedException.cause).isEqualTo(null)
    // The following can't be an exact match for the stack trace since new properties are added to
    // stack trace elements in newer versions of Java (such as module name).
    assertThat(loggedExceptionStackTraceElems).isEqualTo(expectedExceptionStackTraceElems)
  }

  @Test
  fun testWorker_logPerformanceMetric_withoutNetwork_enqueueRequest_verifySuccess() {
    setUpTestApplicationComponent()
    networkConnectionUtil.setCurrentConnectionStatus(NONE)
    performanceMetricsController.logPerformanceMetricsEvent(
      TEST_TIMESTAMP,
      SCREEN_NAME_UNSPECIFIED,
      apkSizeTestLoggableMetric,
      OppiaMetricLog.Priority.LOW_PRIORITY
    )
    testCoroutineDispatchers.runCurrent()

    val workManager = WorkManager.getInstance(ApplicationProvider.getApplicationContext())

    val inputData = Data.Builder().putString(
      LogUploadWorker.WORKER_CASE_KEY,
      LogUploadWorker.PERFORMANCE_METRICS_WORKER
    ).build()

    val request: OneTimeWorkRequest = OneTimeWorkRequestBuilder<LogUploadWorker>()
      .setInputData(inputData)
      .build()
    workManager.enqueue(request)
    testCoroutineDispatchers.runCurrent()

    val workInfo = workManager.getWorkInfoById(request.id)
    val loggedPerformanceMetric =
      fakePerformanceMetricsEventLogger.getMostRecentPerformanceMetricsEvent()
    assertThat(workInfo.get().state).isEqualTo(WorkInfo.State.SUCCEEDED)
    assertThat(loggedPerformanceMetric.loggableMetric.loggableMetricTypeCase).isEqualTo(
      OppiaMetricLog.LoggableMetric.LoggableMetricTypeCase.APK_SIZE_METRIC
    )
    assertThat(loggedPerformanceMetric.currentScreen).isEqualTo(
      SCREEN_NAME_UNSPECIFIED
    )
    assertThat(loggedPerformanceMetric.priority).isEqualTo(OppiaMetricLog.Priority.LOW_PRIORITY)
    assertThat(loggedPerformanceMetric.timestampMillis).isEqualTo(TEST_TIMESTAMP)
    assertThat(loggedPerformanceMetric.loggableMetric.apkSizeMetric.apkSizeBytes).isEqualTo(
      TEST_APK_SIZE
    )
  }

  @Test
  fun testWorker_logEvent_withNetwork_enqueueRequest_studyOn_verifySyncStatusesHasSuccess() {
    setUpTestApplicationComponent(enableLearnerStudyAnalytics = true)
    networkConnectionUtil.setCurrentConnectionStatus(NONE)
    analyticsController.logImportantEvent(
      oppiaLogger.createOpenInfoTabContext(TEST_TOPIC_ID),
      profileId = null,
      eventLogTopicContext.timestamp
    )
    networkConnectionUtil.setCurrentConnectionStatus(LOCAL)
    testCoroutineDispatchers.runCurrent()

    val workManager = WorkManager.getInstance(ApplicationProvider.getApplicationContext())

    val inputData = Data.Builder().putString(
      LogUploadWorker.WORKER_CASE_KEY,
      LogUploadWorker.EVENT_WORKER
    ).build()

    val request: OneTimeWorkRequest = OneTimeWorkRequestBuilder<LogUploadWorker>()
      .setInputData(inputData)
      .build()

    workManager.enqueue(request)
    testCoroutineDispatchers.runCurrent()

    val currentStatus =
      monitorFactory.waitForNextSuccessfulResult(testSyncStatusManager.getSyncStatus())
    val statusList = testSyncStatusManager.getSyncStatuses()
    assertThat(statusList)
      .containsAtLeast(INITIAL_UNKNOWN, DATA_UPLOADED, DATA_UPLOADING, DATA_UPLOADED)
      .inOrder()
    assertThat(currentStatus).isEqualTo(DATA_UPLOADED)
  }

  @Test
  fun testWorker_logEvent_withoutNetwork_enqueueRequest_studyOn_verifySyncStatusesHasFailed() {
    setUpTestApplicationComponent(enableLearnerStudyAnalytics = true)
    networkConnectionUtil.setCurrentConnectionStatus(NONE)
    analyticsController.logImportantEvent(
      oppiaLogger.createOpenInfoTabContext(TEST_TOPIC_ID),
      profileId = null,
      eventLogTopicContext.timestamp
    )
    testCoroutineDispatchers.runCurrent()

    val workManager = WorkManager.getInstance(ApplicationProvider.getApplicationContext())

    val inputData = Data.Builder().putString(
      LogUploadWorker.WORKER_CASE_KEY,
      LogUploadWorker.EVENT_WORKER
    ).build()

    val request: OneTimeWorkRequest = OneTimeWorkRequestBuilder<LogUploadWorker>()
      .setInputData(inputData)
      .build()

    workManager.enqueue(request)
    testCoroutineDispatchers.runCurrent()

    val currentStatus =
      monitorFactory.waitForNextSuccessfulResult(testSyncStatusManager.getSyncStatus())
    val statusList = testSyncStatusManager.getSyncStatuses()
    // The operation should fail since there's no internet connectivity with which to upload the
    // events. It's not valid to try.
    assertThat(statusList)
      .containsAtLeast(INITIAL_UNKNOWN, DATA_UPLOADING, UPLOAD_ERROR, NO_CONNECTIVITY)
      .inOrder()
    assertThat(currentStatus).isEqualTo(NO_CONNECTIVITY)
  }

  @Test
  fun testWorker_logEvent_noNetwork_enqueueRequest_writeFails_studyOn_verifyHasFailedSyncStatus() {
    setUpTestApplicationComponent(enableLearnerStudyAnalytics = true)
    networkConnectionUtil.setCurrentConnectionStatus(NONE)
    analyticsController.logImportantEvent(
      oppiaLogger.createOpenInfoTabContext(TEST_TOPIC_ID),
      profileId = null,
      eventLogTopicContext.timestamp
    )
    testCoroutineDispatchers.runCurrent()

    val workManager = WorkManager.getInstance(ApplicationProvider.getApplicationContext())

    val inputData = Data.Builder().putString(
      LogUploadWorker.WORKER_CASE_KEY,
      LogUploadWorker.EVENT_WORKER
    ).build()

    val request: OneTimeWorkRequest = OneTimeWorkRequestBuilder<LogUploadWorker>()
      .setInputData(inputData)
      .build()

    setUpEventLoggerToFail()
    workManager.enqueue(request)
    testCoroutineDispatchers.runCurrent()

    // Note that "no connectivity" is the last item because it takes priority over an error.
    val statusList = testSyncStatusManager.getSyncStatuses()
    val currentStatus =
      monitorFactory.waitForNextSuccessfulResult(testSyncStatusManager.getSyncStatus())
    assertThat(statusList)
      .containsAtLeast(INITIAL_UNKNOWN, DATA_UPLOADING, UPLOAD_ERROR, NO_CONNECTIVITY)
      .inOrder()
    assertThat(currentStatus).isEqualTo(NO_CONNECTIVITY)
  }

  @Test
  fun testWorker_logFirestoreEvent_withNetwork_enqueueRequest_verifySuccess() {
    setUpTestApplicationComponent()
    networkConnectionUtil.setCurrentConnectionStatus(NONE)
    dataController.logEvent(
      createOptionalSurveyResponseContext(),
      profileId = null,
      1556094120000
    )
    networkConnectionUtil.setCurrentConnectionStatus(LOCAL)
    testCoroutineDispatchers.runCurrent()

    val workManager = WorkManager.getInstance(ApplicationProvider.getApplicationContext())

    val inputData = Data.Builder().putString(
      LogUploadWorker.WORKER_CASE_KEY,
      LogUploadWorker.FIRESTORE_WORKER
    ).build()

    val request: OneTimeWorkRequest = OneTimeWorkRequestBuilder<LogUploadWorker>()
      .setInputData(inputData)
      .build()

    workManager.enqueue(request)
    testCoroutineDispatchers.runCurrent()
    val workInfo = workManager.getWorkInfoById(request.id)

    assertThat(workInfo.get().state).isEqualTo(WorkInfo.State.SUCCEEDED)
    assertThat(fakeFirestoreEventLogger.getMostRecentEvent()).isEqualTo(
      optionalSurveyResponseEventLog
    )
  }

  @Test
  fun testWorker_logFirestoreEvent_withoutNetwork_enqueueRequest_writeFails_verifyFailure() {
    setUpTestApplicationComponent()
    networkConnectionUtil.setCurrentConnectionStatus(NONE)
    dataController.logEvent(
      createOptionalSurveyResponseContext(),
      profileId = null,
      1556094120000
    )
    testCoroutineDispatchers.runCurrent()

    val workManager = WorkManager.getInstance(ApplicationProvider.getApplicationContext())

    val inputData = Data.Builder().putString(
      LogUploadWorker.WORKER_CASE_KEY,
      LogUploadWorker.FIRESTORE_WORKER
    ).build()

    val request: OneTimeWorkRequest = OneTimeWorkRequestBuilder<LogUploadWorker>()
      .setInputData(inputData)
      .build()

    setUpFirestoreEventLoggerToFail()
    workManager.enqueue(request)
    testCoroutineDispatchers.runCurrent()
    val workInfo = workManager.getWorkInfoById(request.id)

    assertThat(workInfo.get().state).isEqualTo(WorkInfo.State.FAILED)
    assertThat(fakeFirestoreEventLogger.noEventsPresent()).isTrue()
  }

  private val optionalSurveyResponseEventLog = EventLog.newBuilder().apply {
    this.context = createOptionalSurveyResponseContext()
    this.timestamp = TEST_TIMESTAMP
    this.priority = EventLog.Priority.ESSENTIAL
  }
    .build()

  private fun createOptionalSurveyResponseContext(): EventLog.Context {
    return EventLog.Context.newBuilder()
      .setOptionalResponse(
        EventLog.OptionalSurveyResponseContext.newBuilder()
          .setFeedbackAnswer("answer")
          .setSurveyDetails(
            createSurveyResponseContext()
          )
      )
      .build()
  }

  private fun createSurveyResponseContext(): EventLog.SurveyResponseContext {
    return EventLog.SurveyResponseContext.newBuilder()
      .setSurveyId("test_survey_id")
      .build()
  }

  private fun setUpEventLoggerToFail() {
    // Simulate the log attempt itself failing during the job. Note that the reset is necessary here
    // to remove the default stubbing for the mock so that it can properly trigger a failure.
    reset(mockAnalyticsEventLogger)
    `when`(mockAnalyticsEventLogger.logEvent(anyOrNull()))
      .thenThrow(IllegalStateException("Failure."))
  }

  private fun setUpFirestoreEventLoggerToFail() {
    // Simulate the log attempt itself failing during the job. Note that the reset is necessary here
    // to remove the default stubbing for the mock so that it can properly trigger a failure.
    reset(mockFirestoreEventLogger)
    `when`(mockFirestoreEventLogger.uploadEvent(anyOrNull()))
      .thenThrow(IllegalStateException("Failure."))
  }

  /**
   * Returns a list of lists of each relevant element of a [StackTraceElement] to be used for
   * comparison in a way that's consistent across JDK versions.
   */
  private fun Array<StackTraceElement>.extractRelevantDetails(): List<List<Any>> =
    map { elem -> listOf(elem.fileName, elem.methodName, elem.lineNumber, elem.className) }

  private fun setUpTestApplicationComponent(enableLearnerStudyAnalytics: Boolean = false) {
    TestPlatformParameterModule.forceEnableLearnerStudyAnalytics(enableLearnerStudyAnalytics)
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
    context = InstrumentationRegistry.getInstrumentation().targetContext
    val config = Configuration.Builder()
      .setExecutor(SynchronousExecutor())
      .setWorkerFactory(logUploadWorkerFactory)
      .build()
    WorkManagerTestInitHelper.initializeTestWorkManager(context, config)
  }

  @Qualifier
  annotation class MockEventLogger

  @Qualifier
  annotation class MockFirestoreEventLogger

  // TODO(#89): Move this to a common test application component.
  @Module
  class TestModule {
    @Provides
    fun provideContext(application: Application): Context = application

    @Provides
    @Singleton
    @MockEventLogger
    fun bindMockEventLogger(fakeAnalyticsLogger: FakeAnalyticsEventLogger): AnalyticsEventLogger {
      return mock(AnalyticsEventLogger::class.java).also {
        `when`(it.logEvent(anyOrNull())).then { answer ->
          fakeAnalyticsLogger.logEvent(
            answer.getArgument(/* index= */ 0, /* clazz= */ EventLog::class.java)
          )
          return@then null
        }
      }
    }

    @Provides
    @Singleton
    @MockFirestoreEventLogger
    fun bindMockFirestoreEventLogger(fakeFirestoreLogger: FakeFirestoreEventLogger):
      FirestoreEventLogger {
        return mock(FirestoreEventLogger::class.java).also {
          `when`(it.uploadEvent(anyOrNull())).then { answer ->
            fakeFirestoreLogger.uploadEvent(
              answer.getArgument(/* index= */ 0, /* clazz= */ EventLog::class.java)
            )
            return@then null
          }
        }
      }

    @Provides
    fun bindFakeEventLogger(@MockEventLogger delegate: AnalyticsEventLogger):
      AnalyticsEventLogger = delegate

    @Provides
    fun bindFakeExceptionLogger(fakeLogger: FakeExceptionLogger): ExceptionLogger = fakeLogger

    @Provides
    fun bindFakePerformanceMetricsLogger(
      fakePerformanceMetricsEventLogger: FakePerformanceMetricsEventLogger
    ): PerformanceMetricsEventLogger = fakePerformanceMetricsEventLogger

    @Provides
    fun bindFakeFirestoreEventLogger(
      @MockFirestoreEventLogger delegate: FirestoreEventLogger
    ): FirestoreEventLogger = delegate
  }

  @Module
  class TestLogStorageModule {

    @Provides
    @EventLogStorageCacheSize
    fun provideEventLogStorageCacheSize(): Int = 2

    @Provides
    @ExceptionLogStorageCacheSize
    fun provideExceptionLogStorageSize(): Int = 2

    @Provides
    @PerformanceMetricsLogStorageCacheSize
    fun providePerformanceMetricsLogStorageCacheSize(): Int = 2

    @Provides
    @FirestoreLogStorageCacheSize
    fun provideFirestoreLogStorageCacheSize(): Int = 2
  }

  @Module
  interface TestFirebaseLogUploaderModule {

    @Binds
    fun bindsFakeLogUploader(fakeLogUploader: FakeLogUploader): LogUploader
  }

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(
    modules = [
      TestModule::class, RobolectricModule::class, TestLogStorageModule::class,
      TestDispatcherModule::class, LogReportWorkerModule::class,
      TestFirebaseLogUploaderModule::class, FakeOppiaClockModule::class,
      NetworkConnectionUtilDebugModule::class, LocaleProdModule::class, LoggerModule::class,
      AssetModule::class, TestPlatformParameterModule::class,
      PlatformParameterSingletonModule::class, LoggingIdentifierModule::class,
      SyncStatusTestModule::class, PerformanceMetricsAssessorModule::class,
      ApplicationLifecycleModule::class, PerformanceMetricsConfigurationsModule::class,
      TestAuthenticationModule::class,
    ]
  )
  interface TestApplicationComponent : DataProvidersInjector {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder
      fun build(): TestApplicationComponent
    }

    fun inject(logUploadWorkerTest: LogUploadWorkerTest)
  }

  class TestApplication : Application(), DataProvidersInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerLogUploadWorkerTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(logUploadWorkerTest: LogUploadWorkerTest) {
      component.inject(logUploadWorkerTest)
    }

    override fun getDataProvidersInjector(): DataProvidersInjector = component
  }
}
