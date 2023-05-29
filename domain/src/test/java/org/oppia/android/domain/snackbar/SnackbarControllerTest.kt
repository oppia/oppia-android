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
import javax.inject.Inject
import javax.inject.Singleton
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.data.DataProviderTestMonitor
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.caching.AssetModule
import org.oppia.android.util.data.DataProvidersInjector
import org.oppia.android.util.data.DataProvidersInjectorProvider
import org.oppia.android.util.locale.LocaleProdModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode

const val test_string: Int = 1

@Suppress("FunctionName", "SameParameterValue")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = SnackbarControllerTest.TestApplication::class)
class SnackbarControllerTest {

  @Inject
  lateinit var context: Context

  @Inject
  lateinit var snackbarController: SnackbarController

  @Inject
  lateinit var monitorFactory: DataProviderTestMonitor.Factory

  lateinit var request: SnackbarController.SnackbarRequest.ShowSnackbar
  lateinit var request2: SnackbarController.SnackbarRequest.ShowSnackbar


  @Before
  fun setUp() {
    request = SnackbarController.SnackbarRequest.ShowSnackbar(
      messageStringId = test_string,
      duration = SnackbarController.SnackbarDuration.SHORT
    )
    request2 = SnackbarController.SnackbarRequest.ShowSnackbar(
      messageStringId = test_string,
      duration = SnackbarController.SnackbarDuration.LONG
    )
    setUpTestApplicationComponent()
  }

  @Test
  fun testAddSnackbarRequestReturnsElementInTheQueue() {
    snackbarController.enqueueSnackbar(request)

    assertThat(snackbarController.snackbarRequestQueue).contains(request)
  }

  @Test
  fun testAddAndRemoveSnackbarRequestReturnsElementNotInTheQueue() {
    snackbarController.enqueueSnackbar(request)
    snackbarController.dismissCurrentSnackbar()

    assertThat(snackbarController.snackbarRequestQueue).doesNotContain(request)
  }

  @Test
  fun testAddTwoRequestsReturnsEarliestRequestInTheQueue(){
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