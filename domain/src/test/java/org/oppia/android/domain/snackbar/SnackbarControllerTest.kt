package org.oppia.android.domain.snackbar

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
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.caching.AssetModule
import org.oppia.android.util.data.DataProvidersInjector
import org.oppia.android.util.data.DataProvidersInjectorProvider
import org.oppia.android.util.locale.LocaleProdModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

private const val STRING_ID: Int = 1

@Suppress("FunctionName", "SameParameterValue")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = SnackbarControllerTest.TestApplication::class)
class SnackbarControllerTest {

  @Inject
  lateinit var snackbarController: SnackbarController

  var request = SnackbarController.SnackbarRequest.ShowSnackbar(
    messageStringId = STRING_ID,
    duration = SnackbarController.SnackbarDuration.SHORT
  )

  var request2 = SnackbarController.SnackbarRequest.ShowSnackbar(
    messageStringId = STRING_ID,
    duration = SnackbarController.SnackbarDuration.LONG
  )

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testEnqueueSnackbarRequest_returns_requestInTheQueue() {
    snackbarController.enqueueSnackbar(request)
    assertThat(snackbarController.snackbarRequestQueue).contains(request)
  }

  @Test
  fun testEnqueueAndDismissSnackbarRequest_returns_requestNotInTheQueue() {
    snackbarController.enqueueSnackbar(request)
    snackbarController.dismissCurrentSnackbar()
    assertThat(snackbarController.snackbarRequestQueue).doesNotContain(request)
  }

  @Test
  fun testEnqueueTwoRequests_returns_FirstRequestInTheQueue() {
    snackbarController.enqueueSnackbar(request)
    snackbarController.enqueueSnackbar(request2)
    assertThat(snackbarController.snackbarRequestQueue.peek()).isEqualTo(request)
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
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
      TestModule::class, AssetModule::class, LocaleProdModule::class, FakeOppiaClockModule::class,
      TestLogReportingModule::class, TestDispatcherModule::class, RobolectricModule::class,
    ]
  )
  interface TestApplicationComponent : DataProvidersInjector {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun inject(snackbarControllerTest: SnackbarControllerTest)
  }

  class TestApplication : Application(), DataProvidersInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerSnackbarControllerTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(snackbarControllerTest: SnackbarControllerTest) {
      component.inject(snackbarControllerTest)
    }

    override fun getDataProvidersInjector(): DataProvidersInjector = component
  }
}
