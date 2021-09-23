package org.oppia.android.testing

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
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode

/** Tests for [InitializeDefaultLocaleRule]. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(manifest = Config.NONE)
class InitializeDefaultLocaleRuleOmissionTest {
  // TODO: finish

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun doSomething_andPass() {
  }

  private fun setUpTestApplicationComponent() {
    DaggerInitializeDefaultLocaleRuleOmissionTest_TestApplicationComponent.builder()
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
  }

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(
    modules = [
      TestModule::class
    ]
  )
  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun inject(initializeDefaultLocaleRuleOmissionTest: InitializeDefaultLocaleRuleOmissionTest)
  }
}
