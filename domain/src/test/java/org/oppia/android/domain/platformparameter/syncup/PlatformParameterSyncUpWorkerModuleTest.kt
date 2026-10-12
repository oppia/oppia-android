package org.oppia.android.domain.platformparameter.syncup

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.Binds
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.domain.oppialogger.LoggingIdentifierModule
import org.oppia.android.domain.oppialogger.analytics.ApplicationLifecycleModule
import org.oppia.android.domain.oppialogger.analytics.CpuPerformanceSnapshotterModule
import org.oppia.android.domain.platformparameter.PlatformParameterControllerInjector
import org.oppia.android.domain.platformparameter.PlatformParameterControllerInjectorProvider
import org.oppia.android.domain.platformparameter.PlatformParameterSingletonModule
import org.oppia.android.domain.workmanager.OppiaWorker
import org.oppia.android.domain.workmanager.StartupWorkerScheduleReadinessListener
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.firebase.TestAuthenticationModule
import org.oppia.android.testing.logging.SyncStatusTestModule
import org.oppia.android.testing.platformparameter.TestPlatformParameterModule
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.caching.AssetModule
import org.oppia.android.util.data.DataProvidersInjector
import org.oppia.android.util.data.DataProvidersInjectorProvider
import org.oppia.android.util.locale.LocaleProdModule
import org.oppia.android.util.logging.LoggerModule
import org.oppia.android.util.logging.performancemetrics.PerformanceMetricsConfigurationsModule
import org.oppia.android.util.networking.NetworkConnectionUtilDebugModule
import org.oppia.android.util.threading.DispatcherInjector
import org.oppia.android.util.threading.DispatcherInjectorProvider
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [PlatformParameterSyncUpWorkerModule]. */
@Suppress("FunctionName") // FunctionName: test names are conventionally named with underscores.
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = PlatformParameterSyncUpWorkerModuleTest.TestApplication::class)
class PlatformParameterSyncUpWorkerModuleTest {
  @Inject lateinit var context: Context
  @Inject
  lateinit var readinessListeners: Set<@JvmSuppressWildcards StartupWorkerScheduleReadinessListener>
  @Inject lateinit var factories: Map<String, @JvmSuppressWildcards OppiaWorker.Factory<*>>

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testInjection_bindsPlatformParameterSyncUpWorkerSchedulerIntoReadinessListenerSet() {
    // The main test is that the test suite builds.
    val platformParameterSyncUpWorkerSchedulers =
      readinessListeners.filterIsInstance<PlatformParameterSyncUpWorkerScheduler>()
    assertThat(readinessListeners).isNotEmpty()
    assertThat(platformParameterSyncUpWorkerSchedulers).hasSize(1)
  }

  @Test
  fun testInjection_bindsPlatformParameterSyncUpWorkerFactoryByWorkerName() {
    assertThat(factories).containsKey(PlatformParameterSyncUpWorker.WORKER_NAME)
    assertThat(factories[PlatformParameterSyncUpWorker.WORKER_NAME])
      .isInstanceOf(PlatformParameterSyncUpWorker.Factory::class.java)
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  // TODO(#89): Move this to a common test application component.
  @Module
  interface TestModule {
    @Binds
    fun provideContext(application: Application): Context
  }

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(
    modules = [
      TestModule::class,
      PlatformParameterSyncUpWorkerModule::class,
      LocaleProdModule::class,
      FakeOppiaClockModule::class,
      LoggerModule::class,
      RobolectricModule::class,
      TestDispatcherModule::class,
      TestPlatformParameterModule::class,
      ApplicationLifecycleModule::class,
      AssetModule::class,
      CpuPerformanceSnapshotterModule::class,
      LogStorageModule::class,
      LoggingIdentifierModule::class,
      NetworkConnectionUtilDebugModule::class,
      PerformanceMetricsConfigurationsModule::class,
      PlatformParameterSingletonModule::class,
      SyncStatusTestModule::class,
      TestLogReportingModule::class,
      TestAuthenticationModule::class
    ]
  )
  interface TestApplicationComponent :
    DataProvidersInjector, DispatcherInjector, PlatformParameterControllerInjector {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun inject(test: PlatformParameterSyncUpWorkerModuleTest)
  }

  class TestApplication :
    Application(),
    DataProvidersInjectorProvider,
    DispatcherInjectorProvider,
    PlatformParameterControllerInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerPlatformParameterSyncUpWorkerModuleTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(test: PlatformParameterSyncUpWorkerModuleTest) {
      component.inject(test)
    }

    override fun getDataProvidersInjector() = component
    override fun getPlatformParameterControllerInjector() = component
    override fun getDispatcherInjector(): DispatcherInjector = component
  }
}
