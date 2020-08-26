package org.oppia.domain.oppialogger

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.Configuration
import androidx.work.ListenableWorker
import androidx.work.WorkManager
import androidx.work.testing.TestWorkerBuilder
import androidx.work.testing.WorkManagerTestInitHelper
import androidx.work.workDataOf
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import org.junit.Before
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import org.oppia.app.model.EventLog
import org.oppia.domain.oppialogger.analytics.AnalyticsController
import org.oppia.domain.oppialogger.analytics.TEST_TIMESTAMP
import org.oppia.domain.oppialogger.analytics.TEST_TOPIC_ID
import org.oppia.testing.FakeEventLogger
import org.oppia.testing.TestDispatcherModule
import org.oppia.testing.TestLogReportingModule
import org.oppia.util.logging.EnableConsoleLog
import org.oppia.util.logging.EnableFileLog
import org.oppia.util.logging.GlobalLogLevel
import org.oppia.util.logging.LogLevel
import org.oppia.util.networking.NetworkConnectionUtil
import org.robolectric.annotation.Config
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import javax.inject.Inject
import javax.inject.Singleton

@RunWith(AndroidJUnit4::class)
@Config(manifest = Config.NONE)
class OppiaLogUploadWorkerTest : Configuration.Provider {

  @Rule
  @JvmField
  val mockitoRule: MockitoRule = MockitoJUnit.rule()

  @Inject
  lateinit var networkConnectionUtil: NetworkConnectionUtil

  @Inject
  lateinit var fakeEventLogger: FakeEventLogger

  @Inject
  lateinit var oppiaLogger: OppiaLogger

  @Inject
  lateinit var analyticsController: AnalyticsController

  private lateinit var context: Context
  private lateinit var executor: Executor
  private lateinit var workManager: WorkManager

  @Before
  fun setUp() {
    context = ApplicationProvider.getApplicationContext()
    executor = Executors.newSingleThreadExecutor()
    WorkManagerTestInitHelper.initializeTestWorkManager(context, workManagerConfiguration)
    setUpTestApplicationComponent()
    workManager = WorkManager.getInstance(context)
    networkConnectionUtil = NetworkConnectionUtil(context)
  }

  @Test
  fun testWorker_doWork_withEventsInputData_verifySuccess(){
    analyticsController.logTransitionEvent(
      TEST_TIMESTAMP,
      EventLog.EventAction.EVENT_ACTION_UNSPECIFIED,
      oppiaLogger.createTopicContext(TEST_TOPIC_ID)
    )

    val worker = TestWorkerBuilder<OppiaLogUploadWorker>(
      context, executor, workDataOf(
        OppiaLogUploadWorker.WORKER_CASE_KEY
          to OppiaLogUploadWorker.WorkerCase.EVENT_WORKER.toString()
      )
    ).build()

    val result = worker.doWork()

    val eventList = fakeEventLogger.noEventsPresent()
    assertThat(result).isEqualTo(ListenableWorker.Result.success())
    assertThat(eventList).isTrue()
  }

  private fun setUpTestApplicationComponent() {
    DaggerOppiaLogUploadWorkerTest_TestApplicationComponent.builder()
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
  }

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(
    modules = [
      TestModule::class,
      TestLogReportingModule::class,
      TestLogStorageModule::class,
      TestDispatcherModule::class
    ]
  )
  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder
      fun build(): TestApplicationComponent
    }

    fun inject(oppiaLogUploadWorkerTest: OppiaLogUploadWorkerTest)
  }

  override fun getWorkManagerConfiguration(): Configuration {
    return Configuration.Builder().setMinimumLoggingLevel(Log.INFO)
      .setExecutor(executor)
      .build()
  }
}
