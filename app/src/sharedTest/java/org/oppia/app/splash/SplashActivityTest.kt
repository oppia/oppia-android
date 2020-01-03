package org.oppia.app.splash

import android.app.Application
import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onIdle
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.idling.CountingIdlingResource
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineDispatcher
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.R
import org.oppia.app.onBoarding.OnBoardingingActivity
import org.oppia.app.profile.ProfileActivity
import org.oppia.domain.OnboardingingFlowController
import org.oppia.util.logging.EnableConsoleLog
import org.oppia.util.logging.EnableFileLog
import org.oppia.util.logging.GlobalLogLevel
import org.oppia.util.logging.LogLevel
import org.oppia.util.threading.BackgroundDispatcher
import org.oppia.util.threading.BlockingDispatcher
import java.util.concurrent.AbstractExecutorService
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

/**
 * Tests for [SplashActivity]. For context on the activity test rule setup see:
 * https://jabknowsnothing.wordpress.com/2015/11/05/activitytestrule-espressos-test-lifecycle/.
 */
@RunWith(AndroidJUnit4::class)
class SplashActivityTest {
  @Before
  fun setUp() {
    Intents.init()
    IdlingRegistry.getInstance().register(MainThreadExecutor.countingResource)
    simulateNewAppInstance()
  }

  @After
  fun tearDown() {
    IdlingRegistry.getInstance().unregister(MainThreadExecutor.countingResource)
    Intents.release()
  }

  // The initialTouchMode enables the activity to be launched in touch mode. The launchActivity is
  // disabled to launch Activity explicitly within each test case.
  @get:Rule
  var activityTestRule: ActivityTestRule<SplashActivity> = ActivityTestRule(
    SplashActivity::class.java, /* initialTouchMode= */ true, /* launchActivity= */ false
  )

  @Test
  fun testSplashActivity_initialOpen_routesToOnboardingingActivity() {
    activityTestRule.launchActivity(null)
    intended(hasComponent(OnBoardingingActivity::class.java.name))
  }

  @Test
  fun testSplashActivity_secondOpen_routesToChooseProfileActivity() {
    simulateAppAlreadyOpened()
    launch(SplashActivity::class.java).use {
      intended(hasComponent(ProfileActivity::class.java.name))
    }
  }

  @Test
  fun testSplashActivity_secondOpen_profileChooserScreenContentVerified() {
    simulateAppAlreadyOpened()
    launch(SplashActivity::class.java).use {
      // Wait until the expected text appears on the screen, and ensure it's for the welcome text view.
      onView(withId(R.id.profile_select_text)).check(matches(withText(R.string.profile_chooser_select)))
    }
  }

  private fun simulateNewAppInstance() {
    // Simulate a fresh app install by clearing any potential on-disk caches using an isolated app history controller.
    createTestRootComponent().getOnboardingingFlowController().clearOnboardingingFlow()
    onIdle()
  }

  private fun simulateAppAlreadyOpened() {
    // Simulate the app was already opened by creating an isolated app history controller and saving the opened status
    // on the system before the activity is opened.
    createTestRootComponent().getOnboardingingFlowController().markOnboardingingFlowCompleted()
    onIdle()
  }

  private fun createTestRootComponent(): TestApplicationComponent {
    return DaggerSplashActivityTest_TestApplicationComponent.builder()
      .setApplication(ApplicationProvider.getApplicationContext())
      .build()
  }

  @Qualifier
  annotation class TestDispatcher

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

    @Singleton
    @Provides
    @BlockingDispatcher
    fun provideBlockingDispatcher(): CoroutineDispatcher {
      return MainThreadExecutor.asCoroutineDispatcher()
    }
  }

  @Singleton
  @Component(modules = [TestModule::class])
  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun getOnboardingingFlowController(): OnboardingingFlowController
    fun inject(splashActivityTest: SplashActivityTest)
  }

  // TODO(#59): Move this to a general-purpose testing library that replaces all CoroutineExecutors with an
  //  Espresso-enabled executor service. This service should also allow for background threads to run in both Espresso
  //  and Robolectric to help catch potential race conditions, rather than forcing parallel execution to be sequential
  //  and immediate.
  //  NB: This also blocks on #59 to be able to actually create a test-only library.
  /**
   * An executor service that schedules all [Runnable]s to run asynchronously on the main thread. This is based on:
   * https://android.googlesource.com/platform/packages/apps/TV/+/android-live-tv/src/com/android/tv/util/MainThreadExecutor.java.
   */
  private object MainThreadExecutor : AbstractExecutorService() {
    override fun isTerminated(): Boolean = false

    private val handler = Handler(Looper.getMainLooper())
    val countingResource = CountingIdlingResource("main_thread_executor_counting_idling_resource")

    override fun execute(command: Runnable?) {
      countingResource.increment()
      handler.post {
        try {
          command?.run()
        } finally {
          countingResource.decrement()
        }
      }
    }

    override fun shutdown() {
      throw UnsupportedOperationException()
    }

    override fun shutdownNow(): MutableList<Runnable> {
      throw UnsupportedOperationException()
    }

    override fun isShutdown(): Boolean = false

    override fun awaitTermination(timeout: Long, unit: TimeUnit?): Boolean {
      throw UnsupportedOperationException()
    }
  }
}
