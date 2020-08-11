package org.oppia.app.walkthrough

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.FirebaseApp
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import org.hamcrest.CoreMatchers.allOf
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.R
import org.oppia.app.model.ProfileId
import org.oppia.app.onboarding.OnboardingActivity
import org.oppia.app.utility.OrientationChangeAction.Companion.orientationLandscape
import org.oppia.testing.TestDispatcherModule
import org.oppia.testing.TestLogReportingModule
import org.oppia.testing.profile.ProfileTestHelper
import org.oppia.util.logging.EnableConsoleLog
import org.oppia.util.logging.EnableFileLog
import org.oppia.util.logging.GlobalLogLevel
import org.oppia.util.logging.LogLevel
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [WalkthroughWelcomeFragment]. */
@RunWith(AndroidJUnit4::class)
class WalkthroughWelcomeFragmentTest {

  @Inject
  lateinit var profileTestHelper: ProfileTestHelper

  @Inject
  lateinit var context: Context
  private val internalProfileId = 0
  private lateinit var profileId: ProfileId

  @Before
  fun setUp() {
    Intents.init()
    setUpTestApplicationComponent()
    profileTestHelper.initializeProfiles()
    profileId = ProfileId.newBuilder().setInternalId(internalProfileId).build()
    FirebaseApp.initializeApp(context)
  }

  @After
  fun tearDown() {
    Intents.release()
  }

  private fun setUpTestApplicationComponent() {
    DaggerWalkthroughWelcomeFragmentTest_TestApplicationComponent.builder()
      .setApplication(ApplicationProvider.getApplicationContext())
      .build()
      .inject(this)
  }

  private fun createWalkthroughActivityIntent(profileId: Int): Intent {
    return WalkthroughActivity.createWalkthroughActivityIntent(
      ApplicationProvider.getApplicationContext(),
      profileId
    )
  }

  @Test
  fun testWalkthroughWelcomeFragment_checkDescription_isCorrect() {
    launch<OnboardingActivity>(createWalkthroughActivityIntent(0)).use {
      onView(
        allOf(
          withId(R.id.walkthrough_welcome_description_text_view),
          isCompletelyDisplayed()
        )
      ).check(matches(withText(R.string.walkthrough_welcome_description)))
    }
  }

  @Test
  fun testWalkthroughWelcomeFragment_checkProfileName_isCorrect() {
    launch<OnboardingActivity>(createWalkthroughActivityIntent(0)).use {
      onView(
        allOf(
          withId(R.id.walkthrough_welcome_title_text_view),
          isCompletelyDisplayed()
        )
      ).check(matches(withText("Welcome Sean!")))
    }
  }

  @Test
  fun testWalkthroughWelcomeFragment_checkProfileName_configurationChanged_isCorrect() {
    launch<OnboardingActivity>(createWalkthroughActivityIntent(0)).use {
      onView(
        allOf(
          withId(R.id.walkthrough_welcome_title_text_view),
          isCompletelyDisplayed()
        )
      ).check(matches(withText("Welcome Sean!")))
      onView(isRoot()).perform(orientationLandscape())
      onView(
        allOf(
          withId(R.id.walkthrough_welcome_title_text_view),
          isCompletelyDisplayed()
        )
      ).check(matches(withText("Welcome Sean!")))
    }
  }

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

  @Singleton
  @Component(modules = [
    TestModule::class, TestLogReportingModule::class, TestDispatcherModule::class
  ])
  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun inject(walkthroughWelcomeFragmentTest: WalkthroughWelcomeFragmentTest)
  }
}
