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
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.Mockito.reset
import org.oppia.android.app.model.EventLog
import org.oppia.android.domain.oppialogger.EventLogStorageCacheSize
import org.oppia.android.domain.oppialogger.ExceptionLogStorageCacheSize
import org.oppia.android.domain.oppialogger.LoggingIdentifierModule
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.oppialogger.analytics.AnalyticsController
import org.oppia.android.domain.oppialogger.analytics.ApplicationLifecycleModule
import org.oppia.android.domain.oppialogger.exceptions.ExceptionsController
import org.oppia.android.domain.platformparameter.PlatformParameterModule
import org.oppia.android.domain.platformparameter.PlatformParameterSingletonModule
import org.oppia.android.domain.testing.oppialogger.loguploader.FakeLogUploader
import org.oppia.android.testing.FakeAnalyticsEventLogger
import org.oppia.android.testing.FakeExceptionLogger
import org.oppia.android.testing.logging.FakeSyncStatusManager
import org.oppia.android.testing.logging.SyncStatusTestModule
import org.oppia.android.testing.mockito.anyOrNull
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
import org.oppia.android.util.logging.SyncStatusManager.SyncStatus.NETWORK_ERROR
import org.oppia.android.util.logging.SyncStatusManager.SyncStatus.NO_CONNECTIVITY
import org.oppia.android.util.networking.NetworkConnectionDebugUtil
import org.oppia.android.util.networking.NetworkConnectionUtil.ProdConnectionStatus.NONE
import org.oppia.android.util.networking.NetworkConnectionUtilDebugModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Qualifier
import javax.inject.Singleton

private const val TEST_TIMESTAMP = 1556094120000
private const val TEST_TOPIC_ID = "test_topicId"

/** Tests for [LogUploadWorker]. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = LogUploadWorkerTest.TestApplication::class)
class LogUploadWorkerTest {
  @Inject lateinit var networkConnectionUtil: NetworkConnectionDebugUtil
  @Inject lateinit var fakeEventLogger: FakeAnalyticsEventLogger
  @Inject lateinit var fakeExceptionLogger: FakeExceptionLogger
  @Inject lateinit var oppiaLogger: OppiaLogger
  @Inject lateinit var analyticsController: AnalyticsController
  @Inject lateinit var exceptionsController: ExceptionsController
  @Inject lateinit var logUploadWorkerFactory: LogUploadWorkerFactory
  @Inject lateinit var dataProviders: DataProviders
  @Inject lateinit var testCoroutineDispatchers: TestCoroutineDispatchers
  @Inject lateinit var fakeSyncStatusManager: FakeSyncStatusManager
  @field:[Inject MockAnalyticsEventLogger]
  lateinit var mockAnalyticsEventLogger: AnalyticsEventLogger

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

  private val exception = Exception("TEST")

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
    context = InstrumentationRegistry.getInstrumentation().targetContext
    val config = Configuration.Builder()
      .setExecutor(SynchronousExecutor())
      .setWorkerFactory(logUploadWorkerFactory)
      .build()
    WorkManagerTestInitHelper.initializeTestWorkManager(context, config)
  }

  @Test
  fun testWorker_logEvent_withoutNetwork_enqueueRequest_verifySuccess() {
    networkConnectionUtil.setCurrentConnectionStatus(NONE)
    analyticsController.logImportantEvent(
      eventLogTopicContext.timestamp,
      oppiaLogger.createOpenInfoTabContext(TEST_TOPIC_ID)
    )

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
    assertThat(fakeEventLogger.getMostRecentEvent()).isEqualTo(eventLogTopicContext)
  }

  @Test
  fun testWorker_logEvent_withoutNetwork_enqueueRequest_writeFails_verifyFailure() {
    networkConnectionUtil.setCurrentConnectionStatus(NONE)
    analyticsController.logImportantEvent(
      eventLogTopicContext.timestamp,
      oppiaLogger.createOpenInfoTabContext(TEST_TOPIC_ID)
    )

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
    assertThat(fakeEventLogger.noEventsPresent()).isTrue()
  }

  @Test
  fun testWorker_logException_withoutNetwork_enqueueRequest_verifySuccess() {
    networkConnectionUtil.setCurrentConnectionStatus(NONE)
    exceptionsController.logNonFatalException(exception, TEST_TIMESTAMP)

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
  fun testWorker_logEvent_withoutNetwork_enqueueRequest_verifyCorrectSyncStatusSequence() {
    networkConnectionUtil.setCurrentConnectionStatus(NONE)
    analyticsController.logImportantEvent(
      eventLogTopicContext.timestamp,
      oppiaLogger.createOpenInfoTabContext(TEST_TOPIC_ID)
    )

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

    val statusList = fakeSyncStatusManager.getSyncStatuses()
    assertThat(statusList).containsExactly(NO_CONNECTIVITY, DATA_UPLOADING, DATA_UPLOADED).inOrder()
  }

  @Test
  fun testWorker_logEvent_withoutNetwork_enqueueRequest_writeFails_verifySyncStatusIsFailed() {
    networkConnectionUtil.setCurrentConnectionStatus(NONE)
    analyticsController.logImportantEvent(
      eventLogTopicContext.timestamp,
      oppiaLogger.createOpenInfoTabContext(TEST_TOPIC_ID)
    )

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

    val statusList = fakeSyncStatusManager.getSyncStatuses()
    assertThat(statusList).containsExactly(NO_CONNECTIVITY, DATA_UPLOADING, NETWORK_ERROR).inOrder()
  }

  private fun setUpEventLoggerToFail() {
    // Simulate the log attempt itself failing during the job. Note that the reset is necessary here
    // to remove the default stubbing for the mock so that it can properly trigger a failure.
    reset(mockAnalyticsEventLogger)
    `when`(mockAnalyticsEventLogger.logEvent(anyOrNull())).thenThrow(IllegalStateException("Failure."))
  }

  /**
   * Returns a list of lists of each relevant element of a [StackTraceElement] to be used for
   * comparison in a way that's consistent across JDK versions.
   */
  private fun Array<StackTraceElement>.extractRelevantDetails(): List<List<Any>> {
    return this.map { element ->
      return@map listOf(
        element.fileName,
        element.methodName,
        element.lineNumber,
        element.className
      )
    }
  }

  private fun setUpTestApplicationComponent() {
    DaggerLogUploadWorkerTest_TestApplicationComponent.builder()
      .setApplication(ApplicationProvider.getApplicationContext())
      .build()
      .inject(this)
  }

  @Qualifier annotation class MockAnalyticsEventLogger

  // TODO(#89): Move this to a common test application component.
  @Module
  class TestModule {
    @Provides
    fun provideContext(application: Application): Context = application

    @Provides
    @Singleton
    @MockAnalyticsEventLogger
    fun bindMockEventLogger(fakeLogger: FakeAnalyticsEventLogger): AnalyticsEventLogger {
      return mock(AnalyticsEventLogger::class.java).also {
        `when`(it.logEvent(anyOrNull())).then { answer ->
          fakeLogger.logEvent(answer.getArgument(/* index= */ 0, /* clazz= */ EventLog::class.java))
          return@then null
        }
      }
    }

    @Provides
    fun bindFakeEventLogger(
      @MockAnalyticsEventLogger delegate: AnalyticsEventLogger
    ): AnalyticsEventLogger = delegate

    @Provides
    fun bindFakeExceptionLogger(fakeLogger: FakeExceptionLogger): ExceptionLogger = fakeLogger
  }

  @Module
  class TestLogStorageModule {

    @Provides
    @EventLogStorageCacheSize
    fun provideEventLogStorageCacheSize(): Int = 2

    @Provides
    @ExceptionLogStorageCacheSize
    fun provideExceptionLogStorageSize(): Int = 2
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
      TestDispatcherModule::class, LogUploadWorkerModule::class,
      TestFirebaseLogUploaderModule::class, FakeOppiaClockModule::class,
      NetworkConnectionUtilDebugModule::class, LocaleProdModule::class, LoggerModule::class,
      AssetModule::class, PlatformParameterModule::class, PlatformParameterSingletonModule::class,
      LoggingIdentifierModule::class, SyncStatusTestModule::class, ApplicationLifecycleModule::class
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
