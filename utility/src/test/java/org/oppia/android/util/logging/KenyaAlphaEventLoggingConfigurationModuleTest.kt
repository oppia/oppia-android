package org.oppia.android.util.logging

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [KenyaAlphaEventLoggingConfigurationModule]. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(manifest = Config.NONE)
class KenyaAlphaEventLoggingConfigurationModuleTest {
  @Inject lateinit var eventTypeToHumanReadableNameConverter: EventTypeToHumanReadableNameConverter

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testModule_injectedEventTypeToHumanReadableNameConverter_isStudySpecificImplementation() {
    assertThat(eventTypeToHumanReadableNameConverter)
      .isInstanceOf(KenyaAlphaEventTypeToHumanReadableNameConverterImpl::class.java)
  }

  private fun setUpTestApplicationComponent() {
    DaggerKenyaAlphaEventLoggingConfigurationModuleTest_TestApplicationComponent
      .builder()
      .setApplication(ApplicationProvider.getApplicationContext()).build().inject(this)
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
  @Component(modules = [KenyaAlphaEventLoggingConfigurationModule::class])
  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun inject(test: KenyaAlphaEventLoggingConfigurationModuleTest)
  }
}
