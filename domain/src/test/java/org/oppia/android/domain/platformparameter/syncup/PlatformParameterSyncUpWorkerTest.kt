package org.oppia.android.domain.platformparameter.syncup

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.Configuration
import androidx.work.WorkInfo
import com.google.common.truth.Truth.assertThat
import dagger.Binds
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.data.backends.gae.RetrofitModule
import org.oppia.android.data.backends.gae.RetrofitServiceModule
import org.oppia.android.data.backends.gae.testing.NetworkConfigTestModule
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.domain.oppialogger.LoggingIdentifierModule
import org.oppia.android.domain.oppialogger.analytics.ApplicationLifecycleModule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.caching.AssetModule
import org.oppia.android.util.data.DataProvidersInjector
import org.oppia.android.util.data.DataProvidersInjectorProvider
import org.oppia.android.util.locale.LocaleProdModule
import org.oppia.android.util.logging.SyncStatusModule
import org.oppia.android.util.networking.NetworkConnectionDebugUtilModule
import org.oppia.android.util.networking.NetworkConnectionUtilDebugModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import org.junit.After
import org.oppia.android.data.backends.gae.testing.PlatformParameterServiceTestOrchestrator
import org.oppia.android.domain.platformparameter.PlatformParameterControllerInjector
import org.oppia.android.domain.platformparameter.PlatformParameterControllerInjectorProvider
import org.oppia.android.domain.platformparameter.PlatformParameterSingletonModule
import org.oppia.android.domain.platformparameter.syncup.PlatformParameterSyncUpWorker.Companion.WORKER_NAME
import org.oppia.android.domain.platformparameter.syncup.PlatformParameterSyncUpWorker.Operation.REFRESH_PLATFORM_PARAMETERS
import org.oppia.android.domain.workmanager.WorkManagerConfigurationModule
import org.oppia.android.domain.workmanager.testing.OppiaWorkManagerTestDriver
import org.oppia.android.testing.platformparameter.TestPlatformParameterModule
import org.oppia.android.util.logging.LoggerModule
import org.oppia.android.util.networking.NetworkConnectionDebugUtil
import org.oppia.android.util.threading.BackgroundDispatcher
import org.oppia.android.util.threading.DispatcherInjector
import org.oppia.android.util.threading.DispatcherInjectorProvider

/** Tests for [PlatformParameterSyncUpWorker]. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = PlatformParameterSyncUpWorkerTest.TestApplication::class)
class PlatformParameterSyncUpWorkerTest {
  @Inject lateinit var context: Context
  @Inject lateinit var testCoroutineDispatchers: TestCoroutineDispatchers
  @Inject lateinit var configuration: Configuration
  @Inject lateinit var testDriver: OppiaWorkManagerTestDriver
  @Inject lateinit var networkConnectionUtil: NetworkConnectionDebugUtil
  @Inject lateinit var serviceOrchestrator: PlatformParameterServiceTestOrchestrator
  @field:[Inject BackgroundDispatcher] lateinit var backgroundDispatcher: CoroutineDispatcher

  @After
  fun tearDown() {
    TestPlatformParameterModule.reset()
  }

  // TODO(#5835): Finish the tests for this suite.

  @Test
  fun testWorker_scheduleRefreshPlatformParameters_succeeds() {
    setUpTestApplicationComponent()
    initializeDependencies()
    serviceOrchestrator.setNextResponseAsServerError()

    val workInfo = testDriver.runOneOffWork(WORKER_NAME, REFRESH_PLATFORM_PARAMETERS)

    // Verify that running the job succeeds even though it doesn't actually have an effect yet. Note
    // that this is specifically verifiable because the orchestrator setup above should trigger a
    // failure in the job if it actually made an attempt to call through to the server for syncing.
    assertThat(workInfo.state).isEqualTo(WorkInfo.State.SUCCEEDED)
  }

  private fun initializeDependencies() {
    testDriver.initializeWorkManager(configuration)
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  // TODO(#89): Move this to a common test application component.
  @Module
  interface TestModule {
    @Binds
    fun bindContext(application: Application): Context
  }

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(
    modules = [
      ApplicationLifecycleModule::class,
      AssetModule::class,
      FakeOppiaClockModule::class,
      LocaleProdModule::class,
      LogStorageModule::class,
      LoggingIdentifierModule::class,
      NetworkConfigTestModule::class,
      NetworkConnectionDebugUtilModule::class,
      NetworkConnectionUtilDebugModule::class,
      RetrofitModule::class,
      RetrofitServiceModule::class,
      RobolectricModule::class,
      SyncStatusModule::class,
      TestDispatcherModule::class,
      TestLogReportingModule::class,
      TestModule::class,
      LoggerModule::class,
      PlatformParameterSingletonModule::class,
      WorkManagerConfigurationModule::class,
      TestPlatformParameterModule::class,
      PlatformParameterSyncUpWorkerModule::class
    ]
  )
  interface TestApplicationComponent : DataProvidersInjector, DispatcherInjector, PlatformParameterControllerInjector {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder
      fun build(): TestApplicationComponent
    }

    fun inject(platformParameterSyncUpWorkerTest: PlatformParameterSyncUpWorkerTest)
  }

  class TestApplication : Application(), DataProvidersInjectorProvider, DispatcherInjectorProvider, PlatformParameterControllerInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerPlatformParameterSyncUpWorkerTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(platformParameterSyncUpWorkerTest: PlatformParameterSyncUpWorkerTest) {
      component.inject(platformParameterSyncUpWorkerTest)
    }

    override fun getDataProvidersInjector() = component
    override fun getPlatformParameterControllerInjector() = component
    override fun getDispatcherInjector(): DispatcherInjector = component
  }
}
