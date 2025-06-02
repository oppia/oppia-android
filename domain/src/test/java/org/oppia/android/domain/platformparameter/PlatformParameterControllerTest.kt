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
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.data.DataProviderTestMonitor
import org.oppia.android.testing.platformparameter.TestPlatformParameterModule
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.caching.AssetModule
import org.oppia.android.util.data.DataProvidersInjector
import org.oppia.android.util.data.DataProvidersInjectorProvider
import org.oppia.android.util.locale.testing.LocaleTestModule
import org.oppia.android.util.logging.LoggerModule
import org.oppia.android.util.networking.NetworkConnectionUtilDebugModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [PlatformParameterController]. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = PlatformParameterControllerTest.TestApplication::class)
class PlatformParameterControllerTest {
  @Inject lateinit var platformParameterController: PlatformParameterController
  @Inject lateinit var monitorFactory: DataProviderTestMonitor.Factory

  // TODO(#5835): Finish these tests.

  @Test
  fun testGetParameterInitializationStatus_initialState_isFalse() {
    setUpTestApplicationComponent()

    val initStatusProvider = platformParameterController.getParameterInitializationStatus()

    val isInited = monitorFactory.waitForNextSuccessfulResult(initStatusProvider)
    assertThat(isInited).isTrue()
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
      AssetModule::class,
      FakeOppiaClockModule::class,
      LocaleTestModule::class,
      LogStorageModule::class,
      LoggerModule::class,
      NetworkConnectionUtilDebugModule::class,
      RobolectricModule::class,
      TestDispatcherModule::class,
      TestLogReportingModule::class,
      TestModule::class,
      TestPlatformParameterModule::class
    ]
  )
  interface TestApplicationComponent : DataProvidersInjector {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder
      fun build(): TestApplicationComponent
    }

    fun inject(platformParameterControllerTest: PlatformParameterControllerTest)
  }

  class TestApplication : Application(), DataProvidersInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerPlatformParameterControllerTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(platformParameterControllerTest: PlatformParameterControllerTest) {
      component.inject(platformParameterControllerTest)
    }

    public override fun attachBaseContext(base: Context?) {
      super.attachBaseContext(base)
    }

    override fun getDataProvidersInjector(): DataProvidersInjector = component
  }
}
