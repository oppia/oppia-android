package org.oppia.android.domain.platformparameter

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
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.platformparameter.TestPlatformParameterModule
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.caching.AssetModule
import org.oppia.android.util.locale.testing.LocaleTestModule
import org.oppia.android.util.logging.LoggerModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import testing.src.main.java.org.oppia.android.testing.platformparameter.TestPlatformParameterDebugModule
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [PlatformParameterControllerDebugImpl]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(manifest = Config.NONE)
class PlatformParameterControllerDebugImplTest {

  @Inject
  lateinit var platformParameterController: PlatformParameterController

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }
  @Test
  fun justA() {
    setUpTestApplicationComponent()
    assertThat(platformParameterController).isNotNull()
  }

  private fun setUpTestApplicationComponent() {
    DaggerPlatformParameterControllerDebugImplTest_TestApplicationComponent.builder()
      .setApplication(ApplicationProvider.getApplicationContext())
      .build()
      .inject(this)
  }

  @Module
  class TestModule {
    @Provides
    @Singleton
    fun provideContext(application: Application): Context {
      return application
    }
  }

  @Singleton
  @Component(
    modules = [
      AssetModule::class,
      FakeOppiaClockModule::class,
      LocaleTestModule::class,
      LogStorageModule::class,
      LoggerModule::class,
      RobolectricModule::class,
      TestModule::class,
      TestDispatcherModule::class,
      TestLogReportingModule::class,
      TestPlatformParameterModule::class,
      TestPlatformParameterDebugModule::class
    ]
  )

  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder
      fun build(): TestApplicationComponent
    }

    fun inject(test: PlatformParameterControllerDebugImplTest)
  }
}
