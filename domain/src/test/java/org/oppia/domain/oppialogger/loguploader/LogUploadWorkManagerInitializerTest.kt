package org.oppia.domain.oppialogger.loguploader

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.WorkManagerTestInitHelper
import com.google.common.truth.Truth.assertThat
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.model.EventLog
import org.oppia.domain.oppialogger.EventLogStorageCacheSize
import org.oppia.domain.oppialogger.ExceptionLogStorageCacheSize
import org.oppia.domain.oppialogger.OppiaLogger
import org.oppia.domain.oppialogger.analytics.AnalyticsController
import org.oppia.domain.oppialogger.analytics.TEST_TIMESTAMP
import org.oppia.domain.oppialogger.analytics.TEST_TOPIC_ID
import org.oppia.domain.oppialogger.exceptions.ExceptionsController
import org.oppia.testing.FakeEventLogger
import org.oppia.testing.FakeExceptionLogger
import org.oppia.testing.TestDispatcherModule
import org.oppia.testing.TestLogReportingModule
import org.oppia.util.logging.EnableConsoleLog
import org.oppia.util.logging.EnableFileLog
import org.oppia.util.logging.GlobalLogLevel
import org.oppia.util.logging.LogLevel
import org.oppia.util.networking.NetworkConnectionUtil
import org.robolectric.annotation.Config
import javax.inject.Inject
import javax.inject.Singleton

@RunWith(AndroidJUnit4::class)
@Config(manifest = Config.NONE)
class LogUploadWorkManagerInitializerTest {

  @Inject
  lateinit var logUploadWorkerFactory: LogUploadWorkerFactory

  @Inject
  lateinit var logUploadWorkManagerInitializer: LogUploadWorkManagerInitializer

  @Inject
  lateinit var analyticsController: AnalyticsController

  @Inject
  lateinit var exceptionsController: ExceptionsController

  @Inject
  lateinit var networkConnectionUtil: NetworkConnectionUtil

  @Inject
  lateinit var fakeEventLogger: FakeEventLogger

  @Inject
  lateinit var fakeExceptionLogger: FakeExceptionLogger

  @Inject
  lateinit var oppiaLogger: OppiaLogger

  private lateinit var context: Context

  private val eventLogTopicContext = EventLog.newBuilder()
    .setActionName(EventLog.EventAction.EVENT_ACTION_UNSPECIFIED)
    .setContext(
      EventLog.Context.newBuilder()
        .setTopicContext(
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
    networkConnectionUtil = NetworkConnectionUtil(context)
  }

  @Test
  fun testWorkRequest_logEvent_withoutNetwork_callOnCreate_verifyLogToRemoteService() {
    networkConnectionUtil.setCurrentConnectionStatus(NetworkConnectionUtil.ConnectionStatus.NONE)
    analyticsController.logTransitionEvent(
      eventLogTopicContext.timestamp,
      eventLogTopicContext.actionName,
      oppiaLogger.createTopicContext(TEST_TOPIC_ID)
    )
    networkConnectionUtil.setCurrentConnectionStatus(NetworkConnectionUtil.ConnectionStatus.LOCAL)

    logUploadWorkManagerInitializer.onCreate()
    val eventLog = fakeEventLogger.getMostRecentEvent()
    assertThat(eventLog).isEqualTo(eventLogTopicContext)
  }

  @Test
  fun testWorkRequest_logExcepton_withoutNetwork_callOnCreate_verifyLogToRemoteService() {
    networkConnectionUtil.setCurrentConnectionStatus(NetworkConnectionUtil.ConnectionStatus.NONE)
    exceptionsController.logNonFatalException(exception, TEST_TIMESTAMP)

    networkConnectionUtil.setCurrentConnectionStatus(NetworkConnectionUtil.ConnectionStatus.LOCAL)
    logUploadWorkManagerInitializer.onCreate()

    val exceptionLogged = fakeExceptionLogger.getMostRecentException()
    assertThat(exceptionLogged).isEqualTo(exception)
  }

  @Test
  fun testWorkRequest_logExceptionAndEvent_withoutNetwork_callOnCreate_verifyLogToRemoteService() {
    networkConnectionUtil.setCurrentConnectionStatus(NetworkConnectionUtil.ConnectionStatus.NONE)
    analyticsController.logTransitionEvent(
      eventLogTopicContext.timestamp,
      eventLogTopicContext.actionName,
      oppiaLogger.createTopicContext(TEST_TOPIC_ID)
    )
    exceptionsController.logNonFatalException(exception, TEST_TIMESTAMP)

    networkConnectionUtil.setCurrentConnectionStatus(NetworkConnectionUtil.ConnectionStatus.LOCAL)
    logUploadWorkManagerInitializer.onCreate()

    val eventLog = fakeEventLogger.getMostRecentEvent()
    val exceptionLogged = fakeExceptionLogger.getMostRecentException()

    assertThat(eventLog).isEqualTo(eventLogTopicContext)
    assertThat(exceptionLogged).isEqualTo(exception)
  }

  @Test
  fun testWorkRequest_verifyWorkerConstraints() {
    val workerConstraints = Constraints.Builder()
      .setRequiredNetworkType(NetworkType.CONNECTED)
      .setRequiresBatteryNotLow(true)
      .build()

    val logUploadingWorkRequestConstraints =
      logUploadWorkManagerInitializer.getLogUploadWorkerConstraints()

    assertThat(logUploadingWorkRequestConstraints).isEqualTo(workerConstraints)
  }

  private fun setUpTestApplicationComponent() {
    DaggerLogUploadWorkManagerInitializerTest_TestApplicationComponent.builder()
      .setApplication(ApplicationProvider.getApplicationContext())
      .build()
      .inject(this)
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
    @EventLogStorageCacheSize
    fun provideEventLogStorageCacheSize(): Int = 2

    @Provides
    @ExceptionLogStorageCacheSize
    fun provideExceptionLogStorageSize(): Int = 2
  }

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(
    modules = [
      TestModule::class,
      TestLogReportingModule::class,
      TestLogStorageModule::class,
      TestDispatcherModule::class,
      LogUploadWorkerModule::class
    ]
  )
  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder
      fun build(): TestApplicationComponent
    }

    fun inject(logUploadWorkRequestTest: LogUploadWorkManagerInitializerTest)
  }
}
