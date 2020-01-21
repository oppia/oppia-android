package org.oppia.domain

import android.app.Application
import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.setMain
import org.junit.After
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
import org.oppia.app.model.OnBoardingFlow
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

/** Tests for [OnBoardingFlowController]. */
@RunWith(AndroidJUnit4::class)
@Config(manifest = Config.NONE)
class OnBoardingFlowControllerTest {
  @Rule
  @JvmField
  val mockitoRule: MockitoRule = MockitoJUnit.rule()

  @Rule
  @JvmField
  val executorRule = InstantTaskExecutorRule()

  @Inject lateinit var onBoardingFlowController: OnBoardingFlowController

  @Inject
  @field:TestDispatcher
  lateinit var testDispatcher: CoroutineDispatcher

  private val coroutineContext by lazy {
    EmptyCoroutineContext + testDispatcher
  }

  @Mock
  lateinit var mockOnBoardingObserver: Observer<AsyncResult<OnBoardingFlow>>

  @Captor
  lateinit var onBoardingResultCaptor: ArgumentCaptor<AsyncResult<OnBoardingFlow>>

  // https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-test/
  @ObsoleteCoroutinesApi
  private val testThread = newSingleThreadContext("TestMain")

  @Before
  @ExperimentalCoroutinesApi
  @ObsoleteCoroutinesApi
  fun setUp() {
    Dispatchers.setMain(testThread)
    setUpTestApplicationComponent()
  }

  @After
  @ExperimentalCoroutinesApi
  @ObsoleteCoroutinesApi
  fun tearDown() {
    Dispatchers.resetMain()
    testThread.close()
  }

  private fun setUpTestApplicationComponent() {
    DaggerOnBoardingFlowControllerTest_TestApplicationComponent.builder()
      .setApplication(ApplicationProvider.getApplicationContext())
      .build()
      .inject(this)
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testController_providesInitialLiveData_thatIndicatesUserHasNotOnBoardedTheApp() = runBlockingTest(coroutineContext) {
    val onBoarding = onBoardingFlowController.getOnBoardingFlow()
    advanceUntilIdle()
    onBoarding.observeForever(mockOnBoardingObserver)

    verify(mockOnBoardingObserver, atLeastOnce()).onChanged(onBoardingResultCaptor.capture())
    assertThat(onBoardingResultCaptor.value.isSuccess()).isTrue()
    assertThat(onBoardingResultCaptor.value.getOrThrow().alreadyOnBoardedApp).isFalse()
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testControllerObserver_observedAfterSettingAppOnBoarded_providesLiveData_userDidNotOnBoardApp() =
    runBlockingTest(coroutineContext) {
      val onBoarding = onBoardingFlowController.getOnBoardingFlow()

      onBoarding.observeForever(mockOnBoardingObserver)
      onBoardingFlowController.markOnBoardingFlowCompleted()
      advanceUntilIdle()

      // The result should not indicate that the user on-boarded the app because markUserOnBoardedApp does not notify observers
      // of the change.
      verify(mockOnBoardingObserver, atLeastOnce()).onChanged(onBoardingResultCaptor.capture())
      assertThat(onBoardingResultCaptor.value.isSuccess()).isTrue()
      assertThat(onBoardingResultCaptor.value.getOrThrow().alreadyOnBoardedApp).isFalse()
    }

  @Test
  @ExperimentalCoroutinesApi
  fun testController_settingAppOnBoarded_observedNewController_userOnBoardedApp() = runBlockingTest(coroutineContext) {
    onBoardingFlowController.markOnBoardingFlowCompleted()
    advanceUntilIdle()

    // Create the controller by creating another singleton graph and injecting it (simulating the app being recreated).
    setUpTestApplicationComponent()
    val onBoarding = onBoardingFlowController.getOnBoardingFlow()
    onBoarding.observeForever(mockOnBoardingObserver)
    advanceUntilIdle()

    // The app should be considered on-boarded since a new LiveData instance was observed after marking the app as on-boarded.
    verify(mockOnBoardingObserver, atLeastOnce()).onChanged(onBoardingResultCaptor.capture())
    assertThat(onBoardingResultCaptor.value.isSuccess()).isTrue()
    assertThat(onBoardingResultCaptor.value.getOrThrow().alreadyOnBoardedApp).isTrue()
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testController_onBoardedApp_cleared_observeNewController_userDidNotOnBoardApp() = runBlockingTest(coroutineContext) {
    onBoardingFlowController.markOnBoardingFlowCompleted()
    advanceUntilIdle()

    // Clear, then recreate another controller.
    onBoardingFlowController.clearOnBoardingFlow()
    setUpTestApplicationComponent()
    val onBoarding = onBoardingFlowController.getOnBoardingFlow()
    onBoarding.observeForever(mockOnBoardingObserver)
    advanceUntilIdle()

    // The app should be considered not yet on-boarded since the previous history was cleared.
    verify(mockOnBoardingObserver, atLeastOnce()).onChanged(onBoardingResultCaptor.capture())
    assertThat(onBoardingResultCaptor.value.isSuccess()).isTrue()
    assertThat(onBoardingResultCaptor.value.getOrThrow().alreadyOnBoardedApp).isFalse()
  }

  @Qualifier
  annotation class TestDispatcher

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

    fun inject(onBoardingFlowControllerTest: OnBoardingFlowControllerTest)
  }
}
