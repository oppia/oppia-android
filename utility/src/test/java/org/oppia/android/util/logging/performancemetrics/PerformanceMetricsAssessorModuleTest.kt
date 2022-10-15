package org.oppia.android.util.logging.performancemetrics

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
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.util.data.DataProvidersInjector
import org.oppia.android.util.data.DataProvidersInjectorProvider
import org.oppia.android.util.logging.LoggerModule
import org.oppia.android.util.logging.firebase.LogReportingModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton
import org.oppia.android.util.system.OppiaClockModule

/** Tests for [PerformanceMetricsAssessorModule]. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = PerformanceMetricsAssessorModuleTest.TestApplication::class)
class PerformanceMetricsAssessorModuleTest {

  @Inject
  lateinit var performanceMetricsAssessor: PerformanceMetricsAssessor

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testInjectAssessor_injectsPerformanceMetricsAssessorImpl() {
    assertThat(performanceMetricsAssessor).isInstanceOf(PerformanceMetricsAssessorImpl::class.java)
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
      TestModule::class, PerformanceMetricsAssessorModule::class, LoggerModule::class,
      TestDispatcherModule::class, LogReportingModule::class, RobolectricModule::class,
      PerformanceMetricsConfigurationsModule::class, OppiaClockModule::class
    ]
  )
  interface TestApplicationComponent : DataProvidersInjector {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder
      fun build(): TestApplicationComponent
    }

    fun inject(test: PerformanceMetricsAssessorModuleTest)
  }

  class TestApplication : Application(), DataProvidersInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerPerformanceMetricsAssessorModuleTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(test: PerformanceMetricsAssessorModuleTest) {
      component.inject(test)
    }

    override fun getDataProvidersInjector(): DataProvidersInjector = component
  }
}
