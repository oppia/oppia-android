package org.oppia.app.settings.profile

import android.app.Application
import android.content.Context
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.ExperimentalCoroutinesApi
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineDispatcher
import org.hamcrest.CoreMatchers.allOf
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.R
import org.oppia.domain.profile.ProfileTestHelper
import org.oppia.util.logging.EnableConsoleLog
import org.oppia.util.logging.EnableFileLog
import org.oppia.util.logging.GlobalLogLevel
import org.oppia.util.logging.LogLevel
import org.oppia.util.threading.BackgroundDispatcher
import org.oppia.util.threading.BlockingDispatcher
import javax.inject.Inject
import javax.inject.Qualifier
import javax.inject.Singleton

@RunWith(AndroidJUnit4::class)
class ProfileResetPinActivityTest {

  @Inject lateinit var context: Context
  @Inject lateinit var profileTestHelper: ProfileTestHelper

  @Before
  @ExperimentalCoroutinesApi
  fun setUp() {
    Intents.init()
    setUpTestApplicationComponent()
    profileTestHelper.initializeProfiles()
  }

  @After
  fun tearDown() {
    Intents.release()
  }

  private fun setUpTestApplicationComponent() {
    DaggerProfileResetPinActivityTest_TestApplicationComponent.builder()
      .setApplication(ApplicationProvider.getApplicationContext())
      .build()
      .inject(this)
  }

  @Test
  fun testProfileResetPinActivity_startActivityWithAdmin_inputPin_inputConfirmPin_clickSave_checkReturnsToProfileEditActivity() {
    ActivityScenario.launch<ProfileResetPinActivity>(ProfileResetPinActivity.createProfileResetPinActivity(context, 0, true)).use {
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin)))).perform(typeText("12345"))
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_confirm_pin)))).perform(typeText("12345"))
      onView(withId(R.id.profile_reset_save_button)).perform(click())
      intended(hasComponent(ProfileEditActivity::class.java.name))
    }
  }

  @Test
  fun testProfileResetPinActivity_startActivityWithUser_inputPin_inputConfirmPin_clickSave_checkReturnsToProfileEditActivity() {
    ActivityScenario.launch<ProfileResetPinActivity>(ProfileResetPinActivity.createProfileResetPinActivity(context, 1, false)).use {
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin)))).perform(typeText("123"))
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_confirm_pin)))).perform(typeText("123"))
      onView(withId(R.id.profile_reset_save_button)).perform(click())
      intended(hasComponent(ProfileEditActivity::class.java.name))
    }
  }

  @Test
  fun testProfileResetPinActivity_startActivityWithAdmin_inputShortPin_clickSave_checkPinLengthError() {
    ActivityScenario.launch<ProfileResetPinActivity>(ProfileResetPinActivity.createProfileResetPinActivity(context, 0, true)).use {
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin)))).perform(typeText("1234"))
      onView(withId(R.id.profile_reset_save_button)).perform(click())
      onView(allOf(withId(R.id.error_text), isDescendantOfA(withId(R.id.input_pin)))).check(matches(withText(context.getString(R.string.profile_reset_pin_error_admin_pin_length))))
    }
  }

  @Test
  fun testProfileResetPinActivity_startActivityWithAdmin_inputShortPin_clickSave_inputPin_checkErrorIsCleared() {
    ActivityScenario.launch<ProfileResetPinActivity>(ProfileResetPinActivity.createProfileResetPinActivity(context, 0, true)).use {
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin)))).perform(typeText("1234"))
      onView(withId(R.id.profile_reset_save_button)).perform(click())
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin)))).perform(typeText("5"))
      onView(allOf(withId(R.id.error_text), isDescendantOfA(withId(R.id.input_pin)))).check(matches(withText("")))
    }
  }

  @Test
  fun testProfileResetPinActivity_startActivityWithAdmin_inputWrongConfirmPin_clickSave_checkConfirmWrongError() {
    ActivityScenario.launch<ProfileResetPinActivity>(ProfileResetPinActivity.createProfileResetPinActivity(context, 0, true)).use {
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin)))).perform(typeText("12345"))
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_confirm_pin)))).perform(typeText("1234"))
      onView(withId(R.id.profile_reset_save_button)).perform(click())
      onView(allOf(withId(R.id.error_text), isDescendantOfA(withId(R.id.input_confirm_pin)))).check(matches(withText(context.getString(R.string.add_profile_error_pin_confirm_wrong))))
    }
  }

  @Test
  fun testProfileResetPinActivity_startActivityWithAdmin_inputWrongConfirmPin_clickSave_inputConfirmPin_checkErrorIsCleared() {
    ActivityScenario.launch<ProfileResetPinActivity>(ProfileResetPinActivity.createProfileResetPinActivity(context, 0, true)).use {
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin)))).perform(typeText("12345"))
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_confirm_pin)))).perform(typeText("1234"))
      onView(withId(R.id.profile_reset_save_button)).perform(click())
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_confirm_pin)))).perform(typeText("5"))
      onView(allOf(withId(R.id.error_text), isDescendantOfA(withId(R.id.input_confirm_pin)))).check(matches(withText("")))
    }
  }

  @Test
  fun testProfileResetPinActivity_startActivityWithUser_inputShortPin_clickSave_checkPinLengthError() {
    ActivityScenario.launch<ProfileResetPinActivity>(ProfileResetPinActivity.createProfileResetPinActivity(context, 1, false)).use {
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin)))).perform(typeText("12"))
      onView(withId(R.id.profile_reset_save_button)).perform(click())
      onView(allOf(withId(R.id.error_text), isDescendantOfA(withId(R.id.input_pin)))).check(matches(withText(context.getString(R.string.profile_reset_pin_error_user_pin_length))))
    }
  }

  @Test
  fun testProfileResetPinActivity_startActivityWithUser_inputShortPin_clickSave_inputPin_checkErrorIsCleared() {
    ActivityScenario.launch<ProfileResetPinActivity>(ProfileResetPinActivity.createProfileResetPinActivity(context, 1, false)).use {
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin)))).perform(typeText("12"))
      onView(withId(R.id.profile_reset_save_button)).perform(click())
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin)))).perform(typeText("3"))
      onView(allOf(withId(R.id.error_text), isDescendantOfA(withId(R.id.input_pin)))).check(matches(withText("")))
    }
  }

  @Test
  fun testProfileResetPinActivity_startActivityWithUser_inputWrongConfirmPin_clickSave_checkConfirmWrongError() {
    ActivityScenario.launch<ProfileResetPinActivity>(ProfileResetPinActivity.createProfileResetPinActivity(context, 1, false)).use {
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin)))).perform(typeText("123"))
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_confirm_pin)))).perform(typeText("12"))
      onView(withId(R.id.profile_reset_save_button)).perform(click())
      onView(allOf(withId(R.id.error_text), isDescendantOfA(withId(R.id.input_confirm_pin)))).check(matches(withText(context.getString(R.string.add_profile_error_pin_confirm_wrong))))
    }
  }

  @Test
  fun testProfileResetPinActivity_startActivityWithUser_inputWrongConfirmPin_clickSave_inputConfirmPin_checkErrorIsCleared() {
    ActivityScenario.launch<ProfileResetPinActivity>(ProfileResetPinActivity.createProfileResetPinActivity(context, 1, false)).use {
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin)))).perform(typeText("123"))
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_confirm_pin)))).perform(typeText("12"))
      onView(withId(R.id.profile_reset_save_button)).perform(click())
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_confirm_pin)))).perform(typeText("3"))
      onView(allOf(withId(R.id.error_text), isDescendantOfA(withId(R.id.input_confirm_pin)))).check(matches(withText("")))
    }
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

  @Singleton
  @Component(modules = [TestModule::class])
  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun inject(profileResetPinActivity: ProfileResetPinActivityTest)
  }
}
