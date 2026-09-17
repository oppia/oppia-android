package org.oppia.android.domain.agesignals

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.play.agesignals.AgeSignalsManager
import com.google.android.play.agesignals.AgeSignalsManagerFactory
import com.google.common.truth.Truth.assertThat
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.domain.oppialogger.ApplicationStartupListener
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.data.DataProvidersInjector
import org.oppia.android.util.data.DataProvidersInjectorProvider
import org.oppia.android.util.locale.testing.LocaleTestModule
import org.oppia.android.util.logging.EnableConsoleLog
import org.oppia.android.util.logging.EnableFileLog
import org.oppia.android.util.logging.GlobalLogLevel
import org.oppia.android.util.logging.LogLevel
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [AgeSignalsModule]. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = AgeSignalsModuleTest.TestApplication::class)
class AgeSignalsModuleTest {
  @Inject lateinit var ageSignalsManager: AgeSignalsManager
  @Inject lateinit var controller: AgeSignalsController
  @Inject lateinit var startupListeners: Set<@JvmSuppressWildcards ApplicationStartupListener>

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testModule_injectsProductionImplementationOfAgeSignalsManager() {
    val application = ApplicationProvider.getApplicationContext<TestApplication>()
    val productionManager = AgeSignalsManagerFactory.create(application)
    // The production implementation is internal to the external API.
    assertThat(ageSignalsManager).isInstanceOf(productionManager.javaClass)
  }

  @Test
  fun testModule_startupListeners_containsInjectedAgeSignalsController() {
    assertThat(startupListeners).hasSize(1)
    assertThat(startupListeners.single()).isSameInstanceAs(controller)
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  @Module
  class TestModule {
    @Provides
    @Singleton
    fun provideContext(application: Application): Context = application

    @Provides
    @EnableConsoleLog
    fun provideEnableConsoleLog(): Boolean = true

    @Provides
    @EnableFileLog
    fun provideEnableFileLog(): Boolean = false

    @Provides
    @GlobalLogLevel
    fun provideGlobalLogLevel(): LogLevel = LogLevel.VERBOSE
  }

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(
    modules = [
      AgeSignalsModule::class,
      FakeOppiaClockModule::class,
      LocaleTestModule::class,
      RobolectricModule::class,
      TestDispatcherModule::class,
      TestLogReportingModule::class,
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

    fun inject(test: AgeSignalsModuleTest)
  }

  class TestApplication : Application(), DataProvidersInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerAgeSignalsModuleTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(test: AgeSignalsModuleTest) {
      component.inject(test)
    }

    override fun getDataProvidersInjector(): DataProvidersInjector = component
  }
}
