package org.oppia.domain.onboarding

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
import org.oppia.app.model.AppStartupState
import org.oppia.app.model.AppStartupState.StartupMode
import org.oppia.app.model.AppStartupState.StartupMode.USER_IS_ONBOARDED
import org.oppia.app.model.AppStartupState.StartupMode.USER_NOT_YET_ONBOARDED
import org.oppia.app.model.OnboardingState
import org.oppia.data.persistence.PersistentCacheStore
import org.oppia.testing.TestLogReportingModule
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

/** Tests for [AppStartupStateController]. */
@RunWith(AndroidJUnit4::class)
@Config(manifest = Config.NONE)
class AppStartupStateControllerTest {
  @Rule
  @JvmField
  val mockitoRule: MockitoRule = MockitoJUnit.rule()

  @Rule
  @JvmField
  val executorRule = InstantTaskExecutorRule()

  @Inject
  lateinit var appStartupStateController: AppStartupStateController

  @Inject
  lateinit var cacheFactory: PersistentCacheStore.Factory

  @Inject
  @field:TestDispatcher
  lateinit var testDispatcher: CoroutineDispatcher

  private val coroutineContext by lazy {
    EmptyCoroutineContext + testDispatcher
  }

  @Mock
  lateinit var mockOnboardingObserver: Observer<AsyncResult<AppStartupState>>

  @Captor
  lateinit var appStartupStateCaptor: ArgumentCaptor<AsyncResult<AppStartupState>>

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
    DaggerAppStartupStateControllerTest_TestApplicationComponent.builder()
      .setApplication(ApplicationProvider.getApplicationContext())
      .build()
      .inject(this)
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testController_providesInitialLiveData_indicatesUserHasNotOnboardedTheApp() =
    runBlockingTest(coroutineContext) {
      val appStartupState = appStartupStateController.getAppStartupState()
      advanceUntilIdle()
      appStartupState.observeForever(mockOnboardingObserver)

      verify(mockOnboardingObserver, atLeastOnce()).onChanged(appStartupStateCaptor.capture())
      assertThat(appStartupStateCaptor.value.isSuccess()).isTrue()
      assertThat(appStartupStateCaptor.getStartupMode()).isEqualTo(USER_NOT_YET_ONBOARDED)
    }

  @Test
  @ExperimentalCoroutinesApi
  fun testControllerObserver_observedAfterSettingAppOnboarded_providesLiveData_userDidNotOnboardApp() = // ktlint-disable max-line-length
    runBlockingTest(coroutineContext) {
      val appStartupState = appStartupStateController.getAppStartupState()

      appStartupState.observeForever(mockOnboardingObserver)
      appStartupStateController.markOnboardingFlowCompleted()
      advanceUntilIdle()

      // The result should not indicate that the user onboarded the app because markUserOnboardedApp does not notify observers
      // of the change.
      verify(mockOnboardingObserver, atLeastOnce()).onChanged(appStartupStateCaptor.capture())
      assertThat(appStartupStateCaptor.value.isSuccess()).isTrue()
      assertThat(appStartupStateCaptor.getStartupMode()).isEqualTo(USER_NOT_YET_ONBOARDED)
    }

  @Test
  @ExperimentalCoroutinesApi
  fun testController_settingAppOnboarded_observedNewController_userOnboardedApp() =
    runBlockingTest(coroutineContext) {
      appStartupStateController.markOnboardingFlowCompleted()
      advanceUntilIdle()

      // Create the controller by creating another singleton graph and injecting it (simulating the
      // app being recreated).
      setUpTestApplicationComponent()
      val appStartupState = appStartupStateController.getAppStartupState()
      appStartupState.observeForever(mockOnboardingObserver)
      advanceUntilIdle()

      // The app should be considered onboarded since a new LiveData instance was observed after
      // marking the app as onboarded.
      verify(mockOnboardingObserver, atLeastOnce()).onChanged(appStartupStateCaptor.capture())
      assertThat(appStartupStateCaptor.value.isSuccess()).isTrue()
      assertThat(appStartupStateCaptor.getStartupMode()).isEqualTo(USER_IS_ONBOARDED)
    }

  @Test
  @ExperimentalCoroutinesApi
  fun testController_onboardedApp_cleared_observeNewController_userDidNotOnboardApp() =
    runBlockingTest(coroutineContext) {
      val onboardingFlowStore = cacheFactory.create(
        "on_boarding_flow",
        OnboardingState.getDefaultInstance()
      )
      appStartupStateController.markOnboardingFlowCompleted()
      advanceUntilIdle()
      // Clear, then recreate another controller.
      onboardingFlowStore.clearCacheAsync()
      setUpTestApplicationComponent()

      val appStartupState = appStartupStateController.getAppStartupState()
      appStartupState.observeForever(mockOnboardingObserver)
      advanceUntilIdle()

      // The app should be considered not yet onboarded since the previous history was cleared.
      verify(mockOnboardingObserver, atLeastOnce()).onChanged(appStartupStateCaptor.capture())
      assertThat(appStartupStateCaptor.value.isSuccess()).isTrue()
      assertThat(appStartupStateCaptor.getStartupMode()).isEqualTo(USER_NOT_YET_ONBOARDED)
    }

  private fun ArgumentCaptor<AsyncResult<AppStartupState>>.getStartupMode(): StartupMode {
    return value.getOrThrow().startupMode
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
    fun provideBackgroundDispatcher(
      @TestDispatcher testDispatcher: CoroutineDispatcher
    ): CoroutineDispatcher {
      return testDispatcher
    }

    @Singleton
    @Provides
    @BlockingDispatcher
    fun provideBlockingDispatcher(
      @TestDispatcher testDispatcher: CoroutineDispatcher
    ): CoroutineDispatcher {
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
  @Component(modules = [TestModule::class, TestLogReportingModule::class])
  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun inject(onboardingFlowControllerTest: AppStartupStateControllerTest)
  }
}
