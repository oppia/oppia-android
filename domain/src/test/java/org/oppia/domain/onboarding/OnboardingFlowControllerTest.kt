package org.oppia.domain.onboarding

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
import org.oppia.app.model.OnboardingFlow
import org.oppia.data.persistence.PersistentCacheStore
import org.oppia.testing.TestCoroutineDispatchers
import org.oppia.testing.TestDispatcherModule
import org.oppia.testing.TestLogReportingModule
import org.oppia.util.data.AsyncResult
import org.oppia.util.logging.EnableConsoleLog
import org.oppia.util.logging.EnableFileLog
import org.oppia.util.logging.GlobalLogLevel
import org.oppia.util.logging.LogLevel
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [OnboardingFlowController]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(manifest = Config.NONE)
class OnboardingFlowControllerTest {
  @Rule
  @JvmField
  val mockitoRule: MockitoRule = MockitoJUnit.rule()

  @Inject
  lateinit var onboardingFlowController: OnboardingFlowController

  @Inject
  lateinit var cacheFactory: PersistentCacheStore.Factory

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @Mock
  lateinit var mockOnboardingObserver: Observer<AsyncResult<OnboardingFlow>>

  @Captor
  lateinit var onboardingResultCaptor: ArgumentCaptor<AsyncResult<OnboardingFlow>>

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  private fun setUpTestApplicationComponent() {
    DaggerOnboardingFlowControllerTest_TestApplicationComponent.builder()
      .setApplication(ApplicationProvider.getApplicationContext())
      .build()
      .inject(this)
  }

  @Test
  fun testController_providesInitialLiveData_indicatesUserHasNotOnboardedTheApp() {
    val onboarding = onboardingFlowController.getOnboardingFlow()
    onboarding.observeForever(mockOnboardingObserver)
    testCoroutineDispatchers.runCurrent()

    verify(mockOnboardingObserver, atLeastOnce()).onChanged(onboardingResultCaptor.capture())
    assertThat(onboardingResultCaptor.value.isSuccess()).isTrue()
    assertThat(onboardingResultCaptor.value.getOrThrow().alreadyOnboardedApp).isFalse()
  }

  @Test
  fun testControllerObserver_observedAfterAppOnboarded_providesLiveData_userDidNotOnboardApp() {
    val onboarding = onboardingFlowController.getOnboardingFlow()

    onboarding.observeForever(mockOnboardingObserver)
    onboardingFlowController.markOnboardingFlowCompleted()
    testCoroutineDispatchers.runCurrent()

    // The result should not indicate that the user onboarded the app because markUserOnboardedApp does not notify observers
    // of the change.
    verify(mockOnboardingObserver, atLeastOnce()).onChanged(onboardingResultCaptor.capture())
    assertThat(onboardingResultCaptor.value.isSuccess()).isTrue()
    assertThat(onboardingResultCaptor.value.getOrThrow().alreadyOnboardedApp).isFalse()
  }

  @Test
  fun testController_settingAppOnboarded_observedNewController_userOnboardedApp() {
    onboardingFlowController.markOnboardingFlowCompleted()
    testCoroutineDispatchers.runCurrent()

    // Create the controller by creating another singleton graph and injecting it (simulating the app being recreated).
    setUpTestApplicationComponent()
    val onboarding = onboardingFlowController.getOnboardingFlow()
    onboarding.observeForever(mockOnboardingObserver)
    testCoroutineDispatchers.runCurrent()

    // The app should be considered onboarded since a new LiveData instance was observed after marking the app as onboarded.
    verify(mockOnboardingObserver, atLeastOnce()).onChanged(onboardingResultCaptor.capture())
    assertThat(onboardingResultCaptor.value.isSuccess()).isTrue()
    assertThat(onboardingResultCaptor.value.getOrThrow().alreadyOnboardedApp).isTrue()
  }

  @Test
  fun testController_onboardedApp_cleared_observeNewController_userDidNotOnboardApp() {
    val onboardingFlowStore =
      cacheFactory.create("on_boarding_flow", OnboardingFlow.getDefaultInstance())
    onboardingFlowController.markOnboardingFlowCompleted()
    testCoroutineDispatchers.runCurrent()
    // Clear, then recreate another controller.
    onboardingFlowStore.clearCacheAsync()
    testCoroutineDispatchers.runCurrent()
    setUpTestApplicationComponent()
    val onboarding = onboardingFlowController.getOnboardingFlow()
    onboarding.observeForever(mockOnboardingObserver)
    testCoroutineDispatchers.runCurrent()
    // The app should be considered not yet onboarded since the previous history was cleared.
    verify(mockOnboardingObserver, atLeastOnce()).onChanged(onboardingResultCaptor.capture())
    assertThat(onboardingResultCaptor.value.isSuccess()).isTrue()
    assertThat(onboardingResultCaptor.value.getOrThrow().alreadyOnboardedApp).isFalse()
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
      TestModule::class, TestLogReportingModule::class, TestDispatcherModule::class
    ]
  )
  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun inject(onboardingFlowControllerTest: OnboardingFlowControllerTest)
  }
}
