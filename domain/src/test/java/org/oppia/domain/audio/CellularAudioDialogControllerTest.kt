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
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
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
import org.oppia.util.data.AsyncResult
import org.oppia.util.logging.EnableConsoleLog
import org.oppia.util.logging.EnableFileLog
import org.oppia.util.logging.GlobalLogLevel
import org.oppia.util.logging.LogLevel
import org.oppia.util.threading.BackgroundDispatcher
import org.oppia.util.threading.BlockingDispatcher
import org.robolectric.annotation.Config
import javax.inject.Inject
import javax.inject.Qualifier
import javax.inject.Singleton
import kotlin.coroutines.EmptyCoroutineContext

@RunWith(AndroidJUnit4::class)
@Config(manifest = Config.NONE)
class CellularAudioDialogControllerTest {
  @Rule
  @JvmField
  val mockitoRule: MockitoRule = MockitoJUnit.rule()

  @Inject lateinit var cellularAudioDialogController: CellularAudioDialogController

  @Inject
  @field:TestDispatcher
  lateinit var testDispatcher: CoroutineDispatcher

  @Inject
  @field:TestBlockingDispatcher
  lateinit var testBlockingDispatcher: TestCoroutineDispatcher

  private val coroutineContext by lazy {
    EmptyCoroutineContext + testDispatcher
  }

  @Mock
  lateinit var mockCellularDataObserver: Observer<AsyncResult<CellularDataPreference>>

  @Captor
  lateinit var cellularDataResultCaptor: ArgumentCaptor<AsyncResult<CellularDataPreference>>

  @Before
  @ExperimentalCoroutinesApi
  @ObsoleteCoroutinesApi
  fun setUp() {
    setUpTestApplicationComponent()
    // Separate dispatcher is needed for PersistentCacheStore to behave similar to production.
    testBlockingDispatcher.pauseDispatcher()
  }

  private fun setUpTestApplicationComponent() {
    DaggerCellularAudioDialogControllerTest_TestApplicationComponent.builder()
      .setApplication(ApplicationProvider.getApplicationContext())
      .build()
      .inject(this)
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testController_providesInitialLiveData_indicatesToNotHideDialogAndNotUseCellularData()
      = runBlockingTest(coroutineContext) {
    val cellularDataPreference = cellularAudioDialogController.getCellularDataPreference()
    cellularDataPreference.observeForever(mockCellularDataObserver)
    testBlockingDispatcher.advanceUntilIdle()

    verify(mockCellularDataObserver, atLeastOnce()).onChanged(cellularDataResultCaptor.capture())
    assertThat(cellularDataResultCaptor.value.isSuccess()).isTrue()
    assertThat(cellularDataResultCaptor.value.getOrThrow().hideDialog).isFalse()
    assertThat(cellularDataResultCaptor.value.getOrThrow().useCellularData).isFalse()
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testController_setNeverUseCellularDataPref_providesLiveData_indicatesToHideDialogAndNotUseCellularData()
      = runBlockingTest(coroutineContext) {
    val appHistory = cellularAudioDialogController.getCellularDataPreference()

    appHistory.observeForever(mockCellularDataObserver)
    cellularAudioDialogController.setNeverUseCellularDataPreference()
    testBlockingDispatcher.advanceUntilIdle()

    verify(mockCellularDataObserver, atLeastOnce()).onChanged(cellularDataResultCaptor.capture())
    assertThat(cellularDataResultCaptor.value.isSuccess()).isTrue()
    assertThat(cellularDataResultCaptor.value.getOrThrow().hideDialog).isTrue()
    assertThat(cellularDataResultCaptor.value.getOrThrow().useCellularData).isFalse()
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testController_setAlwaysUseCellularDataPref_providesLiveData_indicatesToHideDialogAndUseCellularData()
      = runBlockingTest(coroutineContext) {
    val appHistory = cellularAudioDialogController.getCellularDataPreference()

    appHistory.observeForever(mockCellularDataObserver)
    cellularAudioDialogController.setAlwaysUseCellularDataPreference()
    testBlockingDispatcher.advanceUntilIdle()

    verify(mockCellularDataObserver, atLeastOnce()).onChanged(cellularDataResultCaptor.capture())
    assertThat(cellularDataResultCaptor.value.getOrThrow().hideDialog).isTrue()
    assertThat(cellularDataResultCaptor.value.getOrThrow().useCellularData).isTrue()
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testController_setNeverUseCellularDataPref_observedNewController_indicatesToHideDialogAndNotUseCellularData()
      = runBlockingTest(coroutineContext) {
    cellularAudioDialogController.setNeverUseCellularDataPreference()
    testBlockingDispatcher.advanceUntilIdle()

    setUpTestApplicationComponent()
    val appHistory = cellularAudioDialogController.getCellularDataPreference()
    appHistory.observeForever(mockCellularDataObserver)
    testBlockingDispatcher.advanceUntilIdle()

    verify(mockCellularDataObserver, atLeastOnce()).onChanged(cellularDataResultCaptor.capture())
    assertThat(cellularDataResultCaptor.value.isSuccess()).isTrue()
    assertThat(cellularDataResultCaptor.value.getOrThrow().hideDialog).isTrue()
    assertThat(cellularDataResultCaptor.value.getOrThrow().useCellularData).isFalse()
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testController_setAlwaysUseCellularDataPref_observedNewController_indicatesToHideDialogAndUseCellularData()
      = runBlockingTest(coroutineContext) {
    cellularAudioDialogController.setAlwaysUseCellularDataPreference()
    testBlockingDispatcher.advanceUntilIdle()

    setUpTestApplicationComponent()
    val appHistory = cellularAudioDialogController.getCellularDataPreference()
    appHistory.observeForever(mockCellularDataObserver)
    testBlockingDispatcher.advanceUntilIdle()

    verify(mockCellularDataObserver, atLeastOnce()).onChanged(cellularDataResultCaptor.capture())
    assertThat(cellularDataResultCaptor.value.isSuccess()).isTrue()
    assertThat(cellularDataResultCaptor.value.getOrThrow().hideDialog).isTrue()
    assertThat(cellularDataResultCaptor.value.getOrThrow().useCellularData).isTrue()
  }

  @Qualifier annotation class TestDispatcher
  @Qualifier annotation class TestBlockingDispatcher

  // TODO(#89): Move this to a common test application component.
  @Module
  class TestModule {
    @Provides
    @Singleton
    fun provideContext(application: Application): Context {
      return application
    }

    @ExperimentalCoroutinesApi
    @Singleton
    @Provides
    @TestDispatcher
    fun provideTestDispatcher(): CoroutineDispatcher {
      return TestCoroutineDispatcher()
    }

    @Singleton
    @Provides
    @BackgroundDispatcher
    fun provideBackgroundDispatcher(@TestDispatcher testDispatcher: CoroutineDispatcher): CoroutineDispatcher {
      return testDispatcher
    }

    @Singleton
    @Provides
    @BlockingDispatcher
    fun provideBlockingDispatcher(@TestBlockingDispatcher testDispatcher: TestCoroutineDispatcher): CoroutineDispatcher {
      return testDispatcher
    }

    @ExperimentalCoroutinesApi
    @Singleton
    @Provides
    @TestBlockingDispatcher
    fun provideTestBlockingDispatcher(): TestCoroutineDispatcher {
      return TestCoroutineDispatcher()
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
  @Component(modules = [TestModule::class])
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
