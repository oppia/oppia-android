package org.oppia.domain.audio

import org.oppia.domain.UserAppHistoryController
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

/** Tests for [UserAppHistoryController]. */
@RunWith(AndroidJUnit4::class)
@Config(manifest = Config.NONE)
class CellularDialogControllerTest {
  @Rule
  @JvmField
  val mockitoRule: MockitoRule = MockitoJUnit.rule()

  @Inject
  lateinit var cellularDialogController: CellularDialogController

  @Inject
  @field:TestDispatcher
  lateinit var testDispatcher: CoroutineDispatcher

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
  }


  private fun setUpTestApplicationComponent() {
    DaggerCellularDialogControllerTest_TestApplicationComponent.builder()
      .setApplication(ApplicationProvider.getApplicationContext())
      .build()
      .inject(this)
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testController_providesInitialLiveData_thatIndicatesShowDialog()
      = runBlockingTest(coroutineContext) {
    val cellularDataPreference = cellularDialogController.getCellularDataPreference()
    advanceUntilIdle()
    cellularDataPreference.observeForever(mockCellularDataObserver)

    verify(mockCellularDataObserver, atLeastOnce()).onChanged(cellularDataResultCaptor.capture())
    assertThat(cellularDataResultCaptor.value.isSuccess()).isTrue()
    assertThat(cellularDataResultCaptor.value.getOrThrow().showDialog).isFalse()
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testControllerObserver_observedAfterSetPreference_providesLiveData_userSetShowDialogTrue()
      = runBlockingTest(coroutineContext) {
    val appHistory = cellularDialogController.getCellularDataPreference()

    appHistory.observeForever(mockCellularDataObserver)
    cellularDialogController.setShowDialogPreference(true)
    cellularDialogController.setShowDialogPreference(true)
    advanceUntilIdle()

    verify(mockCellularDataObserver, atLeastOnce()).onChanged(cellularDataResultCaptor.capture())
    assertThat(cellularDataResultCaptor.value.isSuccess()).isTrue()
    assertThat(cellularDataResultCaptor.value.getOrThrow().showDialog).isTrue()
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testControllerObserver_observedAfterSetPreference_providesLiveData_userSetShowDialogFalse()
      = runBlockingTest(coroutineContext) {
    val appHistory = cellularDialogController.getCellularDataPreference()

    appHistory.observeForever(mockCellularDataObserver)
    cellularDialogController.setShowDialogPreference(true)
    cellularDialogController.setShowDialogPreference(false)
    cellularDialogController.setShowDialogPreference(false)
    advanceUntilIdle()

    verify(mockCellularDataObserver, atLeastOnce()).onChanged(cellularDataResultCaptor.capture())
    assertThat(cellularDataResultCaptor.value.isSuccess()).isTrue()
    assertThat(cellularDataResultCaptor.value.getOrThrow().showDialog).isFalse()
  }

  @Qualifier annotation class TestDispatcher

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
    fun provideBlockingDispatcher(@TestDispatcher testDispatcher: CoroutineDispatcher): CoroutineDispatcher {
      return testDispatcher
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

    fun inject(cellularDataControllerTest: CellularDialogControllerTest)
  }
}
