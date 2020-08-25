package org.oppia.domain.oppialogger

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.Configuration
import androidx.work.ListenableWorker
import androidx.work.WorkManager
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.testing.TestWorkerBuilder
import androidx.work.workDataOf
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import org.junit.Before
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.model.EventLog
import org.oppia.domain.oppialogger.analytics.AnalyticsController
import org.oppia.domain.oppialogger.analytics.TEST_QUESTION_ID
import org.oppia.domain.oppialogger.analytics.TEST_SKILL_LIST_ID
import org.oppia.domain.oppialogger.analytics.TEST_TIMESTAMP
import org.oppia.testing.TestDispatcherModule
import org.oppia.testing.TestLogReportingModule
import org.oppia.util.logging.EnableConsoleLog
import org.oppia.util.logging.EnableFileLog
import org.oppia.util.logging.GlobalLogLevel
import org.oppia.util.logging.LogLevel
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import javax.inject.Inject
import javax.inject.Singleton

@RunWith(AndroidJUnit4::class)
@Config(manifest = Config.NONE)
class OppiaLogUploadWorkRequestTest: Configuration.Provider {

  @Inject
  lateinit var oppiaLogger: OppiaLogger

  @Inject
  lateinit var analyticsController: AnalyticsController

  private val oppiaLogUploadWorkRequest = OppiaLogUploadWorkRequest()

  private lateinit var context: Context
  private lateinit var executor: Executor

  lateinit var workManager: WorkManager

  @Before
  fun setUp() {
    context = ApplicationProvider.getApplicationContext()
    executor = Executors.newCachedThreadPool()
    WorkManager.initialize(context, workManagerConfiguration)
    setUpTestApplicationComponent()
    workManager = WorkManager.getInstance(context)
  }

  @Test
  fun testWorkRequest_logEvents_createWorkRequest_verifyRequestResult() {
    oppiaLogger.logTransitionEvent(
      TEST_TIMESTAMP,
      EventLog.EventAction.EVENT_ACTION_UNSPECIFIED,
      oppiaLogger.createQuestionContext(
        TEST_QUESTION_ID,
        listOf(
          TEST_SKILL_LIST_ID, TEST_SKILL_LIST_ID
        )
      )
    )
    oppiaLogUploadWorkRequest.setWorkerRequestForEvents(workManager)
    val worker = TestWorkerBuilder<OppiaLogUploadWorker>(
      context,
      workManagerConfiguration.executor,
      workDataOf("worker_case_key" to OppiaLogUploadWorker.WorkerCase.EVENT_WORKER.toString())
    ).build()

    val result = worker.doWork()

    assertThat(result).isEqualTo(ListenableWorker.Result.success())
  }

  private fun setUpTestApplicationComponent() {
    DaggerOppiaLogUploadWorkRequestTest_TestApplicationComponent.builder()
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

    fun inject(oppiaLogUploadWorkRequestTest: OppiaLogUploadWorkRequestTest)
  }

  override fun getWorkManagerConfiguration(): Configuration {
    return Configuration.Builder().setMinimumLoggingLevel(Log.INFO)
      .setExecutor(executor)
      .build()
  }
}