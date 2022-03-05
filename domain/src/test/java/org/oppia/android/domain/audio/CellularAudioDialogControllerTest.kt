package org.oppia.android.domain.audio

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
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.data.DataProviderTestMonitor
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.data.DataProvidersInjector
import org.oppia.android.util.data.DataProvidersInjectorProvider
import org.oppia.android.util.locale.LocaleProdModule
import org.oppia.android.util.logging.EnableConsoleLog
import org.oppia.android.util.logging.EnableFileLog
import org.oppia.android.util.logging.GlobalLogLevel
import org.oppia.android.util.logging.LogLevel
import org.oppia.android.util.networking.NetworkConnectionUtilDebugModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode

// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = CellularAudioDialogControllerTest.TestApplication::class)
class CellularAudioDialogControllerTest {
  @Inject lateinit var cellularAudioDialogController: CellularAudioDialogController
  @Inject lateinit var testCoroutineDispatchers: TestCoroutineDispatchers
  @Inject lateinit var monitorFactory: DataProviderTestMonitor.Factory

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  @Test
  fun testController_providesInitialState_indicatesToNotHideDialogAndNotUseCellularData() {
    val cellularDataPreference = cellularAudioDialogController.getCellularDataPreference()

    val value = monitorFactory.waitForNextSuccessfulResult(cellularDataPreference)
    assertThat(value.hideDialog).isFalse()
    assertThat(value.useCellularData).isFalse()
  }

  @Test
  fun testController_setNeverUseCellularDataPref_indicatesToHideDialogAndNotUseCellularData() {
    val cellularDataPreference = cellularAudioDialogController.getCellularDataPreference()

    cellularAudioDialogController.setNeverUseCellularDataPreference()
    testCoroutineDispatchers.advanceUntilIdle()

    val value = monitorFactory.waitForNextSuccessfulResult(cellularDataPreference)
    assertThat(value.hideDialog).isTrue()
    assertThat(value.useCellularData).isFalse()
  }

  @Test
  fun testController_setAlwaysUseCellularDataPref_indicatesToHideDialogAndUseCellularData() {
    val cellularDataPreference = cellularAudioDialogController.getCellularDataPreference()

    cellularAudioDialogController.setAlwaysUseCellularDataPreference()
    testCoroutineDispatchers.advanceUntilIdle()

    val value = monitorFactory.waitForNextSuccessfulResult(cellularDataPreference)
    assertThat(value.hideDialog).isTrue()
    assertThat(value.useCellularData).isTrue()
  }

  @Test
  fun testController_setNeverUseCellDataPref_observeNewController_indicatesHideDialogNotUseCell() {
    // Pause immediate dispatching to avoid an infinite loop within the provider pipeline.
    cellularAudioDialogController.setNeverUseCellularDataPreference()
    testCoroutineDispatchers.advanceUntilIdle()

    setUpTestApplicationComponent()
    testCoroutineDispatchers.advanceUntilIdle()
    val cellularDataPreference = cellularAudioDialogController.getCellularDataPreference()

    val value = monitorFactory.waitForNextSuccessfulResult(cellularDataPreference)
    assertThat(value.hideDialog).isTrue()
    assertThat(value.useCellularData).isFalse()
  }

  @Test
  fun testController_setAlwaysUseCellDataPref_observeNewController_indicatesHideDialogAndUseCell() {
    cellularAudioDialogController.setAlwaysUseCellularDataPreference()
    testCoroutineDispatchers.advanceUntilIdle()

    setUpTestApplicationComponent()
    val cellularDataPreference = cellularAudioDialogController.getCellularDataPreference()

    val value = monitorFactory.waitForNextSuccessfulResult(cellularDataPreference)
    assertThat(value.hideDialog).isTrue()
    assertThat(value.useCellularData).isTrue()
  }

  // TODO(#89): Move this to a common test application component.
  @Module
  class TestModule {
    @Provides
    @Singleton
    fun provideContext(application: Application): Context {
      return application
    }

    // TODO(#59): Either isolate these to their own shared test module, or use the real logging
    // module in tests to avoid needing to specify these settings for tests.
    @EnableConsoleLog
    @Provides
    fun provideEnableConsoleLog(): Boolean = true

    @EnableFileLog
    @Provides
    fun provideEnableFileLog(): Boolean = false

    @GlobalLogLevel
    @Provides
    fun provideGlobalLogLevel(): LogLevel = LogLevel.VERBOSE
  }

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(
    modules = [
      LogStorageModule::class, RobolectricModule::class, TestDispatcherModule::class,
      TestModule::class, TestLogReportingModule::class, NetworkConnectionUtilDebugModule::class,
      LocaleProdModule::class, FakeOppiaClockModule::class
    ]
  )
  interface TestApplicationComponent : DataProvidersInjector {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder
      fun build(): TestApplicationComponent
    }

    fun inject(cellularDataControllerTest: CellularAudioDialogControllerTest)
  }

  class TestApplication : Application(), DataProvidersInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerCellularAudioDialogControllerTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(cellularDataControllerTest: CellularAudioDialogControllerTest) {
      component.inject(cellularDataControllerTest)
    }

    override fun getDataProvidersInjector(): DataProvidersInjector = component
  }
}
