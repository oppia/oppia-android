package org.oppia.domain.oppialogger.loguploader

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
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
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import org.oppia.domain.oppialogger.EventLogStorageCacheSize
import org.oppia.domain.oppialogger.ExceptionLogStorageCacheSize
import org.oppia.domain.oppialogger.OppiaLogger
import org.oppia.domain.oppialogger.analytics.AnalyticsController
import org.oppia.domain.oppialogger.exceptions.ExceptionsController
import org.oppia.testing.FakeEventLogger
import org.oppia.testing.FakeExceptionLogger
import org.oppia.testing.TestCoroutineDispatchers
import org.oppia.testing.TestDispatcherModule
import org.oppia.testing.TestLogReportingModule
import org.oppia.util.data.DataProviders
import org.oppia.util.logging.EnableConsoleLog
import org.oppia.util.logging.EnableFileLog
import org.oppia.util.logging.GlobalLogLevel
import org.oppia.util.logging.LogLevel
import org.oppia.util.networking.NetworkConnectionUtil
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(manifest = Config.NONE)
class LogUploadWorkManagerInitializerTest {

  @Rule
  @JvmField
  val mockitoRule: MockitoRule = MockitoJUnit.rule()

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
  lateinit var dataProviders: DataProviders

  @Inject
  lateinit var oppiaLogger: OppiaLogger

  @Inject
  lateinit var fakeLogUploader: FakeLogUploader

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  private lateinit var context: Context

  @Before
  fun setUp() {
    networkConnectionUtil = NetworkConnectionUtil(ApplicationProvider.getApplicationContext())
    setUpTestApplicationComponent()
    context = InstrumentationRegistry.getInstrumentation().targetContext
    val config = Configuration.Builder()
      .setExecutor(SynchronousExecutor())
      .setWorkerFactory(logUploadWorkerFactory)
      .build()
    WorkManagerTestInitHelper.initializeTestWorkManager(context, config)
  }

  @Test
  fun testWorkRequest_onCreate_enqueuesRequest_verifyRequestId() {
    logUploadWorkManagerInitializer.onCreate()
    testCoroutineDispatchers.runCurrent()

    val enqueuedEventWorkRequestId = logUploadWorkManagerInitializer.getWorkRequestForEventsId()
    val enqueuedExceptionWorkRequestId =
      logUploadWorkManagerInitializer.getWorkRequestForExceptionsId()

    assertThat(fakeLogUploader.getMostRecentEventRequestId()).isEqualTo(enqueuedEventWorkRequestId)
    assertThat(fakeLogUploader.getMostRecentExceptionRequestId()).isEqualTo(
      enqueuedExceptionWorkRequestId
    )
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

  @Test
  fun testWorkRequest_verifyWorkRequestDataForEvents() {
    val workerCaseForUploadingEvents: Data = Data.Builder()
      .putString(
        LogUploadWorker.WORKER_CASE_KEY,
        LogUploadWorker.EVENT_WORKER
      )
      .build()

    assertThat(logUploadWorkManagerInitializer.getWorkRequestDataForEvents()).isEqualTo(
      workerCaseForUploadingEvents
    )
  }

  @Test
  fun testWorkRequest_verifyWorkRequestDataForExceptions() {
    val workerCaseForUploadingExceptions: Data = Data.Builder()
      .putString(
        LogUploadWorker.WORKER_CASE_KEY,
        LogUploadWorker.EXCEPTION_WORKER
      )
      .build()

    assertThat(logUploadWorkManagerInitializer.getWorkRequestDataForExceptions()).isEqualTo(
      workerCaseForUploadingExceptions
    )
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

  @Module
  interface TestLogUploaderModule {

    @Binds
    fun bindsFakeLogUploader(fakeLogUploader: FakeLogUploader): LogUploader
  }

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(
    modules = [
      TestModule::class,
      TestLogReportingModule::class,
      TestLogStorageModule::class,
      TestDispatcherModule::class,
      LogUploadWorkerModule::class,
      TestLogUploaderModule::class
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

/**  A test specific fake for the log uploader. */
@Singleton
class FakeLogUploader @Inject constructor() : LogUploader {

  private val eventRequestIdList = ArrayList<UUID>()
  private val exceptionRequestIdList = ArrayList<UUID>()

  override fun enqueueWorkRequestForEvents(
    workManager: WorkManager,
    workRequest: PeriodicWorkRequest
  ) {
    eventRequestIdList.add(workRequest.id)
  }

  override fun enqueueWorkRequestForExceptions(
    workManager: WorkManager,
    workRequest: PeriodicWorkRequest
  ) {
    exceptionRequestIdList.add(workRequest.id)
  }

  /** Returns the most recent work request id that's stored in the [eventRequestIdList]. */
  fun getMostRecentEventRequestId() = eventRequestIdList.last()

  /** Returns the most recent work request id that's stored in the [exceptionRequestIdList]. */
  fun getMostRecentExceptionRequestId() = exceptionRequestIdList.last()
}
