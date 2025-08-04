package org.oppia.android.domain.platformparameter.syncup

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.Configuration
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
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
import org.oppia.android.data.backends.gae.RetrofitModule
import org.oppia.android.data.backends.gae.RetrofitServiceModule
import org.oppia.android.data.backends.gae.testing.NetworkConfigTestModule
import org.oppia.android.data.backends.gae.testing.PlatformParameterServiceTestOrchestrator
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.domain.oppialogger.LoggingIdentifierModule
import org.oppia.android.domain.oppialogger.analytics.ApplicationLifecycleModule
import org.oppia.android.domain.platformparameter.PlatformParameterProdModule
import org.oppia.android.testing.FakeExceptionLogger
import org.oppia.android.testing.LogReportingTestModule
import org.oppia.android.testing.data.DataProviderTestMonitor
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.DispatcherTestModule
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.caching.AssetModule
import org.oppia.android.util.data.DataProvidersInjector
import org.oppia.android.util.data.DataProvidersInjectorProvider
import org.oppia.android.util.locale.LocaleProdModule
import org.oppia.android.util.logging.EnableConsoleLog
import org.oppia.android.util.logging.EnableFileLog
import org.oppia.android.util.logging.GlobalLogLevel
import org.oppia.android.util.logging.LogLevel
import org.oppia.android.util.logging.SyncStatusProdModule
import org.oppia.android.util.logging.performancemetrics.testing.PerformanceMetricsAssessorTestModule
import org.oppia.android.util.networking.NetworkConnectionDebugUtilModule
import org.oppia.android.util.networking.NetworkConnectionUtilDebugModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [PlatformParameterSyncUpWorker]. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(
  application = PlatformParameterSyncUpWorkerTest.TestApplication::class,
  manifest = Config.NONE
)
class PlatformParameterSyncUpWorkerTest {
  @Inject lateinit var platformParameterSyncUpWorkerFactory: PlatformParameterSyncUpWorkerFactory
  @Inject lateinit var testCoroutineDispatchers: TestCoroutineDispatchers
  @Inject lateinit var context: Context
  @Inject lateinit var fakeExceptionLogger: FakeExceptionLogger
  @Inject lateinit var monitorFactory: DataProviderTestMonitor.Factory
  @Inject lateinit var serviceOrchestrator: PlatformParameterServiceTestOrchestrator

  @Before
  fun setup() {
    setUpTestApplicationComponent()
    val config = Configuration.Builder()
      .setExecutor(SynchronousExecutor())
      .setWorkerFactory(platformParameterSyncUpWorkerFactory)
      .build()
    WorkManagerTestInitHelper.initializeTestWorkManager(context, config)
  }

  // TODO(#5835): Fix and finish the tests for this suite.

  @Test
  fun testSyncUpWorker_databaseIsEmpty_failsToLoadParameters() {
    serviceOrchestrator.setNextResponseAsSuccess()

    val workManager = WorkManager.getInstance(context)

    val inputData = Data.Builder().putString(
      PlatformParameterSyncUpWorker.WORKER_TYPE_KEY,
      PlatformParameterSyncUpWorker.PLATFORM_PARAMETER_WORKER
    ).build()

    val request: OneTimeWorkRequest = OneTimeWorkRequestBuilder<PlatformParameterSyncUpWorker>()
      .setInputData(inputData)
      .build()

    // Enqueue the Work Request to fetch and cache the Platform Parameters from Remote Service.
    workManager.enqueue(request)
    testCoroutineDispatchers.runCurrent()

    // Note that this fails because the test isn't yet set up correctly.
    val workInfo = workManager.getWorkInfoById(request.id)
    assertThat(workInfo.get().state).isEqualTo(WorkInfo.State.FAILED)
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
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

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(
    modules = [
      ApplicationLifecycleModule::class,
      AssetModule::class,
      DispatcherTestModule::class,
      FakeOppiaClockModule::class,
      LocaleProdModule::class,
      LogReportingTestModule::class,
      LogStorageModule::class,
      LoggingIdentifierModule::class,
      NetworkConfigTestModule::class,
      NetworkConnectionDebugUtilModule::class,
      NetworkConnectionUtilDebugModule::class,
      PerformanceMetricsAssessorTestModule::class,
      PlatformParameterProdModule::class,
      RetrofitModule::class,
      RetrofitServiceModule::class,
      RobolectricModule::class,
      SyncStatusProdModule::class,
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

    fun inject(platformParameterSyncUpWorkerTest: PlatformParameterSyncUpWorkerTest)
  }

  class TestApplication : Application(), DataProvidersInjectorProvider {
    private val component: PlatformParameterSyncUpWorkerTest.TestApplicationComponent by lazy {
      DaggerPlatformParameterSyncUpWorkerTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(platformParameterSyncUpWorkerTest: PlatformParameterSyncUpWorkerTest) {
      component.inject(platformParameterSyncUpWorkerTest)
    }

    public override fun attachBaseContext(base: Context?) {
      super.attachBaseContext(base)
    }

    override fun getDataProvidersInjector(): DataProvidersInjector = component
  }
}
