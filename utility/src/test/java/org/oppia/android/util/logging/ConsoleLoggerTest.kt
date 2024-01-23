package org.oppia.android.util.logging

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.content.pm.ApplicationInfoBuilder
import androidx.test.core.content.pm.PackageInfoBuilder
import androidx.test.ext.truth.os.BundleSubject.assertThat
import com.google.common.truth.Truth.assertThat
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.junit.OppiaParameterizedTestRunner
import org.oppia.android.testing.junit.OppiaParameterizedTestRunner.SelectRunnerPlatform
import org.oppia.android.testing.junit.ParameterizedRobolectricTestRunner
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.data.DataProvidersInjector
import org.oppia.android.util.data.DataProvidersInjectorProvider
import org.oppia.android.util.locale.testing.LocaleTestModule
import org.oppia.android.util.platformparameter.LOGGING_LEARNER_STUDY_IDS_DEFAULT_VALUE
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

@Suppress("FunctionName")
@RunWith(OppiaParameterizedTestRunner::class)
@SelectRunnerPlatform(ParameterizedRobolectricTestRunner::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(
  application = ConsoleLoggerTest.TestApplication::class
)
class ConsoleLoggerTest {
  @Inject lateinit var context: Context
  @Inject lateinit var consoleLogger: ConsoleLogger

  private val testVersionName = "1.0"
  private val testVersionCode = 1

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
    setUpApplicationForContext()
  }

  @Test
  fun dummyTest() {
    assertThat(true).isTrue()
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  private fun setUpApplicationForContext() {
    val packageManager = Shadows.shadowOf(context.packageManager)
    val applicationInfo =
      ApplicationInfoBuilder.newBuilder()
        .setPackageName(context.packageName)
        .build()
    val packageInfo =
      PackageInfoBuilder.newBuilder()
        .setPackageName(context.packageName)
        .setApplicationInfo(applicationInfo)
        .build()
    packageInfo.versionName = testVersionName
    packageInfo.versionCode = testVersionCode
    packageManager.installPackage(packageInfo)
  }

  @Module
  class TestModule {
    @Provides
    @Singleton
    fun provideContext(application: Application): Context {
      return application
    }

    @Provides
    @Singleton
    @EnableConsoleLog
    fun provideEnableConsoleLog(): Boolean = true

    @Provides
    @Singleton
    @EnableFileLog
    fun provideEnableFileLog(): Boolean = false

    @Provides
    @Singleton
    @GlobalLogLevel
    fun provideGlobalLogLevel(): LogLevel = LogLevel.ERROR
  }

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(
    modules = [
      RobolectricModule::class, TestModule::class, LocaleTestModule::class,
      TestLogReportingModule::class, TestDispatcherModule::class,
      FakeOppiaClockModule::class,
    ]
  )
  interface TestApplicationComponent : DataProvidersInjector {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun inject(test: ConsoleLoggerTest)
  }

  class TestApplication : Application(), DataProvidersInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerConsoleLoggerTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(test: ConsoleLoggerTest) {
      component.inject(test)
    }

    public override fun attachBaseContext(base: Context?) {
      super.attachBaseContext(base)
    }

    override fun getDataProvidersInjector(): DataProvidersInjector = component
  }
}
