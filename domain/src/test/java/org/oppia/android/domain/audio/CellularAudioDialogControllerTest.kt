package org.oppia.android.domain.audio

import android.app.Application
import android.content.Context
import androidx.lifecycle.Observer
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito.atLeastOnce
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import org.oppia.android.app.model.CellularDataPreference
import org.oppia.android.testing.TestCoroutineDispatchers
import org.oppia.android.testing.TestDispatcherModule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.logging.EnableConsoleLog
import org.oppia.android.util.logging.EnableFileLog
import org.oppia.android.util.logging.GlobalLogLevel
import org.oppia.android.util.logging.LogLevel
import org.oppia.android.app.model.CellularDataPreference
import org.oppia.testing.TestCoroutineDispatchers
import org.oppia.testing.TestDispatcherModule
import org.oppia.testing.TestLogReportingModule
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import org.oppia.android.util.data.DataProvidersInjector
import org.oppia.android.util.data.DataProvidersInjectorProvider
import org.oppia.android.util.logging.EnableConsoleLog
import org.oppia.android.util.logging.EnableFileLog
import org.oppia.android.util.logging.GlobalLogLevel
import org.oppia.android.util.logging.LogLevel
>>>>>>> develop:domain/src/test/java/org.oppia.android.domain.audio/CellularAudioDialogControllerTest.kt
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = CellularAudioDialogControllerTest.TestApplication::class)
class CellularAudioDialogControllerTest {
  @Rule
  @JvmField
  val mockitoRule: MockitoRule = MockitoJUnit.rule()

  @Inject
  lateinit var cellularAudioDialogController: CellularAudioDialogController

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @Mock
  lateinit var mockCellularDataObserver: Observer<AsyncResult<CellularDataPreference>>

  @Captor
  lateinit var cellularDataResultCaptor: ArgumentCaptor<AsyncResult<CellularDataPreference>>

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  @Test
  fun testController_providesInitialLiveData_indicatesToNotHideDialogAndNotUseCellularData() {
    val cellularDataPreference =
      cellularAudioDialogController.getCellularDataPreference().toLiveData()
    cellularDataPreference.observeForever(mockCellularDataObserver)
    testCoroutineDispatchers.advanceUntilIdle()

    verify(mockCellularDataObserver, atLeastOnce()).onChanged(cellularDataResultCaptor.capture())
    assertThat(cellularDataResultCaptor.value.isSuccess()).isTrue()
    assertThat(cellularDataResultCaptor.value.getOrThrow().hideDialog).isFalse()
    assertThat(cellularDataResultCaptor.value.getOrThrow().useCellularData).isFalse()
  }

  @Test
  fun testController_setNeverUseCellularDataPref_providesLiveData_indicatesToHideDialogAndNotUseCellularData() { // ktlint-disable max-line-length
    val appHistory =
      cellularAudioDialogController.getCellularDataPreference().toLiveData()

    appHistory.observeForever(mockCellularDataObserver)
    cellularAudioDialogController.setNeverUseCellularDataPreference()
    testCoroutineDispatchers.advanceUntilIdle()

    verify(mockCellularDataObserver, atLeastOnce()).onChanged(cellularDataResultCaptor.capture())
    assertThat(cellularDataResultCaptor.value.isSuccess()).isTrue()
    assertThat(cellularDataResultCaptor.value.getOrThrow().hideDialog).isTrue()
    assertThat(cellularDataResultCaptor.value.getOrThrow().useCellularData).isFalse()
  }

  @Test
  fun testController_setAlwaysUseCellularDataPref_providesLiveData_indicatesToHideDialogAndUseCellularData() { // ktlint-disable max-line-length
    val appHistory =
      cellularAudioDialogController.getCellularDataPreference().toLiveData()

    appHistory.observeForever(mockCellularDataObserver)
    cellularAudioDialogController.setAlwaysUseCellularDataPreference()
    testCoroutineDispatchers.advanceUntilIdle()

    verify(mockCellularDataObserver, atLeastOnce()).onChanged(cellularDataResultCaptor.capture())
    assertThat(cellularDataResultCaptor.value.getOrThrow().hideDialog).isTrue()
    assertThat(cellularDataResultCaptor.value.getOrThrow().useCellularData).isTrue()
  }

  @Test
  fun testController_setNeverUseCellularDataPref_observedNewController_indicatesToHideDialogAndNotUseCellularData() { // ktlint-disable max-line-length
    // Pause immediate dispatching to avoid an infinite loop within the provider pipeline.
    cellularAudioDialogController.setNeverUseCellularDataPreference()
    testCoroutineDispatchers.advanceUntilIdle()

    setUpTestApplicationComponent()
    val appHistory =
      cellularAudioDialogController.getCellularDataPreference().toLiveData()
    appHistory.observeForever(mockCellularDataObserver)
    testCoroutineDispatchers.advanceUntilIdle()

    verify(mockCellularDataObserver, atLeastOnce()).onChanged(cellularDataResultCaptor.capture())
    assertThat(cellularDataResultCaptor.value.isSuccess()).isTrue()
    assertThat(cellularDataResultCaptor.value.getOrThrow().hideDialog).isTrue()
    assertThat(cellularDataResultCaptor.value.getOrThrow().useCellularData).isFalse()
  }

  @Test
  fun testController_setAlwaysUseCellularDataPref_observedNewController_indicatesToHideDialogAndUseCellularData() { // ktlint-disable max-line-length
    cellularAudioDialogController.setAlwaysUseCellularDataPreference()
    testCoroutineDispatchers.advanceUntilIdle()

    setUpTestApplicationComponent()
    val appHistory =
      cellularAudioDialogController.getCellularDataPreference().toLiveData()
    appHistory.observeForever(mockCellularDataObserver)
    testCoroutineDispatchers.advanceUntilIdle()

    verify(mockCellularDataObserver, atLeastOnce()).onChanged(cellularDataResultCaptor.capture())
    assertThat(cellularDataResultCaptor.value.isSuccess()).isTrue()
    assertThat(cellularDataResultCaptor.value.getOrThrow().hideDialog).isTrue()
    assertThat(cellularDataResultCaptor.value.getOrThrow().useCellularData).isTrue()
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
      TestDispatcherModule::class,
      TestModule::class,
      TestLogReportingModule::class
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
