package org.oppia.android.domain.devoptions

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
import org.oppia.android.domain.oppialogger.LoggingIdentifierModule
import org.oppia.android.domain.oppialogger.analytics.ApplicationLifecycleModule
import org.oppia.android.domain.platformparameter.PlatformParameterModule
import org.oppia.android.domain.platformparameter.PlatformParameterSingletonModule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.data.DataProviderTestMonitor
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.caching.AssetModule
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.locale.LocaleProdModule
import org.oppia.android.util.logging.SyncStatusModule
import org.oppia.android.util.networking.NetworkConnectionUtilDebugModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [ForceDownloadRemoteParametersController]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = ForceDownloadRemoteParametersControllerTest.TestApplication::class)
class ForceDownloadRemoteParametersControllerTest {
  @Inject
  lateinit var forceDownloadRemoteParametersController: ForceDownloadRemoteParametersController
  @Inject lateinit var monitorFactory: DataProviderTestMonitor.Factory

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testDownloadRemoteParameters_returnsAsyncResultSuccess() {
    setUpTestApplicationComponent()
    val downloadProvider = forceDownloadRemoteParametersController.downloadRemoteParameters()
    val downloadMonitor = monitorFactory.createMonitor(downloadProvider)
    val downloadResult = downloadMonitor.waitForNextResult()
    assertThat(downloadResult).isInstanceOf(AsyncResult.Success::class.java)
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  // TODO(#89): Move this to a common test application component.
  @Module
  class TestModule {
    @Provides
    fun provideContext(application: Application): Context = application
  }

  @Singleton
  @Component(
    modules = [
      ApplicationLifecycleModule::class,
      AssetModule::class,
      FakeOppiaClockModule::class,
      LocaleProdModule::class,
      LogStorageModule::class,
      LoggingIdentifierModule::class,
      NetworkConnectionUtilDebugModule::class,
      PlatformParameterModule::class,
      PlatformParameterSingletonModule::class,
      RobolectricModule::class,
      SyncStatusModule::class,
      TestDispatcherModule::class,
      TestLogReportingModule::class,
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

    fun inject(
      forceDownloadRemoteParametersControllerTest: ForceDownloadRemoteParametersControllerTest
    )
  }

  class TestApplication : Application() {
    private val component: TestApplicationComponent by lazy {
      DaggerForceDownloadRemoteParametersControllerTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    fun inject(
      forceDownloadRemoteParametersControllerTest: ForceDownloadRemoteParametersControllerTest
    ) {
      component.inject(forceDownloadRemoteParametersControllerTest)
    }
  }
}
