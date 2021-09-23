package org.oppia.android.app.translation

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import javax.inject.Singleton
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.util.data.DataProvidersInjector
import org.oppia.android.util.data.DataProvidersInjectorProvider
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode

/** Tests for [AppLanguageLocaleHandler]. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = AppLanguageLocaleHandlerTest.TestApplication::class)
class AppLanguageLocaleHandlerTest {
  // TODO: finish

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun doSomething_andPass() {
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
  }

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(
    modules = [
      TestModule::class, TestLogReportingModule::class, TestDispatcherModule::class,
      RobolectricModule::class
    ]
  )
  interface TestApplicationComponent : DataProvidersInjector {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun inject(appLanguageLocaleHandlerTest: AppLanguageLocaleHandlerTest)
  }

  class TestApplication : Application(), DataProvidersInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerAppLanguageLocaleHandlerTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(appLanguageLocaleHandlerTest: AppLanguageLocaleHandlerTest) {
      component.inject(appLanguageLocaleHandlerTest)
    }

    override fun getDataProvidersInjector(): DataProvidersInjector = component
  }
}
