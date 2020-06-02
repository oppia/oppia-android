package org.oppia.domain.audio

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
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
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
import org.oppia.app.model.CellularDataPreference
import org.oppia.domain.topic.StoryProgressControllerTest.TestFirebaseModule
import org.oppia.testing.TestCoroutineDispatchers
import org.oppia.testing.TestDispatcherModule
import org.oppia.util.data.AsyncResult
import org.oppia.util.logging.EnableConsoleLog
import org.oppia.util.logging.EnableFileLog
import org.oppia.util.logging.GlobalLogLevel
import org.oppia.util.logging.LogLevel
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode

@RunWith(AndroidJUnit4::class)
@Config(manifest = Config.NONE)
@LooperMode(LooperMode.Mode.PAUSED)
class CellularAudioDialogControllerTest {
  @Rule
  @JvmField
  val mockitoRule: MockitoRule = MockitoJUnit.rule()

  @Inject lateinit var cellularAudioDialogController: CellularAudioDialogController

  @Inject
  @InternalCoroutinesApi
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @Mock
  lateinit var mockCellularDataObserver: Observer<AsyncResult<CellularDataPreference>>

  @Captor
  lateinit var cellularDataResultCaptor: ArgumentCaptor<AsyncResult<CellularDataPreference>>

  @Before
  @ExperimentalCoroutinesApi
  @ObsoleteCoroutinesApi
  fun setUp() {
    setUpTestApplicationComponent()
  }

  private fun setUpTestApplicationComponent() {
    DaggerCellularAudioDialogControllerTest_TestApplicationComponent.builder()
      .setApplication(ApplicationProvider.getApplicationContext())
      .build()
      .inject(this)
  }

  @Test
  @ExperimentalCoroutinesApi
  @InternalCoroutinesApi
  fun testController_providesInitialLiveData_indicatesToNotHideDialogAndNotUseCellularData() {
    val cellularDataPreference = cellularAudioDialogController.getCellularDataPreference()
    cellularDataPreference.observeForever(mockCellularDataObserver)
    testCoroutineDispatchers.advanceUntilIdle()

    verify(mockCellularDataObserver, atLeastOnce()).onChanged(cellularDataResultCaptor.capture())
    assertThat(cellularDataResultCaptor.value.isSuccess()).isTrue()
    assertThat(cellularDataResultCaptor.value.getOrThrow().hideDialog).isFalse()
    assertThat(cellularDataResultCaptor.value.getOrThrow().useCellularData).isFalse()
  }

  @Test
  @ExperimentalCoroutinesApi
  @InternalCoroutinesApi
  fun testController_setNeverUseCellularDataPref_providesLiveData_indicatesToHideDialogAndNotUseCellularData() {
    val appHistory = cellularAudioDialogController.getCellularDataPreference()

    appHistory.observeForever(mockCellularDataObserver)
    cellularAudioDialogController.setNeverUseCellularDataPreference()
    testCoroutineDispatchers.advanceUntilIdle()

    verify(mockCellularDataObserver, atLeastOnce()).onChanged(cellularDataResultCaptor.capture())
    assertThat(cellularDataResultCaptor.value.isSuccess()).isTrue()
    assertThat(cellularDataResultCaptor.value.getOrThrow().hideDialog).isTrue()
    assertThat(cellularDataResultCaptor.value.getOrThrow().useCellularData).isFalse()
  }

  @Test
  @ExperimentalCoroutinesApi
  @InternalCoroutinesApi
  fun testController_setAlwaysUseCellularDataPref_providesLiveData_indicatesToHideDialogAndUseCellularData() {
    val appHistory = cellularAudioDialogController.getCellularDataPreference()

    appHistory.observeForever(mockCellularDataObserver)
    cellularAudioDialogController.setAlwaysUseCellularDataPreference()
    testCoroutineDispatchers.advanceUntilIdle()

    verify(mockCellularDataObserver, atLeastOnce()).onChanged(cellularDataResultCaptor.capture())
    assertThat(cellularDataResultCaptor.value.getOrThrow().hideDialog).isTrue()
    assertThat(cellularDataResultCaptor.value.getOrThrow().useCellularData).isTrue()
  }

  @Test
  @ExperimentalCoroutinesApi
  @InternalCoroutinesApi
  fun testController_setNeverUseCellularDataPref_observedNewController_indicatesToHideDialogAndNotUseCellularData() {
    // Pause immediate dispatching to avoid an infinite loop within the provider pipeline.
    cellularAudioDialogController.setNeverUseCellularDataPreference()
    testCoroutineDispatchers.advanceUntilIdle()

    setUpTestApplicationComponent()
    val appHistory = cellularAudioDialogController.getCellularDataPreference()
    appHistory.observeForever(mockCellularDataObserver)
    testCoroutineDispatchers.advanceUntilIdle()

    verify(mockCellularDataObserver, atLeastOnce()).onChanged(cellularDataResultCaptor.capture())
    assertThat(cellularDataResultCaptor.value.isSuccess()).isTrue()
    assertThat(cellularDataResultCaptor.value.getOrThrow().hideDialog).isTrue()
    assertThat(cellularDataResultCaptor.value.getOrThrow().useCellularData).isFalse()
  }

  @Test
  @ExperimentalCoroutinesApi
  @InternalCoroutinesApi
  fun testController_setAlwaysUseCellularDataPref_observedNewController_indicatesToHideDialogAndUseCellularData() {
    cellularAudioDialogController.setAlwaysUseCellularDataPreference()
    testCoroutineDispatchers.advanceUntilIdle()

    setUpTestApplicationComponent()
    val appHistory = cellularAudioDialogController.getCellularDataPreference()
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
  @Component(modules = [TestDispatcherModule::class, TestModule::class, TestFirebaseModule::class])
  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder
      fun build(): TestApplicationComponent
    }

    fun inject(cellularDataControllerTest: CellularAudioDialogControllerTest)
  }
}
