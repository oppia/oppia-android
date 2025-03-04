package org.oppia.android.util.logging.firebase

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.google.firebase.FirebaseApp
import dagger.Binds
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.data.backends.gae.NetworkConfigTestModule
import org.oppia.android.data.backends.gae.NetworkModule
import org.oppia.android.domain.platformparameter.PlatformParameterController
import org.oppia.android.domain.platformparameter.testing.PlatformParameterTestInitializer
import org.oppia.android.domain.platformparameter.testing.PlatformParameterTestModule
import org.oppia.android.testing.data.DataProviderTestMonitor
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.caching.AssetModule
import org.oppia.android.util.data.DataProvidersInjector
import org.oppia.android.util.data.DataProvidersInjectorProvider
import org.oppia.android.util.locale.LocaleProdModule
import org.oppia.android.util.logging.AnalyticsEventLogger
import org.oppia.android.util.logging.LoggerModule
import org.oppia.android.util.logging.SyncStatusModule
import org.oppia.android.util.logging.performancemetrics.PerformanceMetricsEventLogger
import org.oppia.android.util.networking.NetworkConnectionUtilDebugModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [LogReportingModule]. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = LogReportingModuleTest.TestApplication::class)
class LogReportingModuleTest {

  // This initializes platform parameters and feature flags at injection, so it's unused.
  @[Inject Suppress("unused")] lateinit var flagInitializer: PlatformParameterTestInitializer

  @Inject lateinit var performanceMetricsEventLogger: PerformanceMetricsEventLogger
  @Inject lateinit var analyticsEventLogger: AnalyticsEventLogger

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testModule_injectsProductionImplementationOfEventLogger() {
    assertThat(analyticsEventLogger).isInstanceOf(FirebaseAnalyticsEventLogger::class.java)
  }

  @Test
  fun testModule_injectsProductionImplementationOfPerformanceMetricsEventLogger() {
    assertThat(performanceMetricsEventLogger).isInstanceOf(FirebaseAnalyticsEventLogger::class.java)
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
      TestModule::class, LogReportingModule::class, TestDispatcherModule::class,
      RobolectricModule::class, FakeOppiaClockModule::class,
      NetworkConnectionUtilDebugModule::class, LocaleProdModule::class,
      PlatformParameterTestModule::class, LoggerModule::class, SyncStatusModule::class,
      NetworkModule::class, AssetModule::class, NetworkConfigTestModule::class
    ]
  )
  interface TestApplicationComponent : DataProvidersInjector {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder
      fun build(): TestApplicationComponent
    }

    fun getPlatformParameterController(): PlatformParameterController

    fun getDataProviderTestMonitorFactory(): DataProviderTestMonitor.Factory

    fun inject(logReportingModuleTest: LogReportingModuleTest)
  }

  class TestApplication : Application(), DataProvidersInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerLogReportingModuleTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    override fun onCreate() {
      super.onCreate()
      FirebaseApp.initializeApp(applicationContext)
    }

    fun getPlatformParameterController() = component.getPlatformParameterController()

    fun getDataProviderTestMonitorFactory() = component.getDataProviderTestMonitorFactory()

    fun inject(test: LogReportingModuleTest) {
      component.inject(test)
    }

    override fun getDataProvidersInjector(): DataProvidersInjector = component
  }
}
