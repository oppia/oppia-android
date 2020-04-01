package org.oppia.app.profile

import android.app.Application
import android.content.Context
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.hasFocus
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestCoroutineDispatcher
import org.hamcrest.Matchers.allOf
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.R
import org.oppia.app.home.HomeActivity
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
class PinPasswordActivityTest {

  @Inject lateinit var context: Context
  @Inject lateinit var profileTestHelper: ProfileTestHelper

  private val adminPin = "12345"
  private val adminId = 0
  private val userId = 1

  @Before
  @ExperimentalCoroutinesApi
  fun setUp() {
    Intents.init()
    setUpTestApplicationComponent()
    GlobalScope.launch(Dispatchers.Main) {
      profileTestHelper.initializeProfiles()
    }
  }

  @After
  fun tearDown() {
    Intents.release()
  }

  private fun setUpTestApplicationComponent() {
    DaggerPinPasswordActivityTest_TestApplicationComponent.builder()
      .setApplication(ApplicationProvider.getApplicationContext())
      .build()
      .inject(this)
  }

  @Test
  fun testPinPasswordActivityWithAdmin_checkKeyboardIsVisibleByDefault() {
    ActivityScenario.launch<PinPasswordActivity>(
      PinPasswordActivity.createPinPasswordActivityIntent(
        context,
        adminPin,
        adminId
      )
    ).use {
      onView(withId(R.id.input_pin)).check(matches(hasFocus()))
    }
  }

  @Test
  fun testPinPasswordActivityWithAdmin_inputCorrectPin_checkOpensHomeActivity() {
    ActivityScenario.launch<PinPasswordActivity>(
      PinPasswordActivity.createPinPasswordActivityIntent(
        context,
        adminPin,
        adminId
      )
    ).use {
      onView(withId(R.id.input_pin)).perform(typeText("12345"))
      intended(hasComponent(HomeActivity::class.java.name))
    }
  }

  @Test
  fun testPinPasswordActivityWithUser_inputCorrectPin_checkOpensHomeActivity() {
    ActivityScenario.launch<PinPasswordActivity>(
      PinPasswordActivity.createPinPasswordActivityIntent(
        context,
        adminPin,
        userId
      )
    ).use {
      onView(withId(R.id.input_pin)).perform(typeText("123"))
      intended(hasComponent(HomeActivity::class.java.name))
    }
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testPinPasswordActivityWithAdmin_inputWrongPin_checkIncorrectPinShows() {
    ActivityScenario.launch<PinPasswordActivity>(
      PinPasswordActivity.createPinPasswordActivityIntent(
        context,
        adminPin,
        adminId
      )
    ).use {
      onView(withId(R.id.input_pin)).perform(typeText("54321"), closeSoftKeyboard())
      onView(withText(context.getString(R.string.pin_password_incorrect_pin))).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testPinPasswordActivityWithUser_inputWrongPin_checkIncorrectPinShows() {
    ActivityScenario.launch<PinPasswordActivity>(
      PinPasswordActivity.createPinPasswordActivityIntent(
        context,
        adminPin,
        userId
      )
    ).use {
      onView(withId(R.id.input_pin)).perform(typeText("321"), closeSoftKeyboard())
      onView(withText(context.getString(R.string.pin_password_incorrect_pin))).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testPinPasswordActivityWithAdmin_clickForgot_checkOpensAdminForgotDialog() {
    ActivityScenario.launch<PinPasswordActivity>(
      PinPasswordActivity.createPinPasswordActivityIntent(
        context,
        adminPin,
        adminId
      )
    ).use {
      onView(withId(R.id.input_pin)).perform(typeText(""), closeSoftKeyboard())
      onView(withId(R.id.forgot_pin)).perform(click())
      onView(withText(context.getString(R.string.pin_password_forgot_message))).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testPinPasswordActivityWithUser_clickForgot_inputWrongAdminPin_checkWrongAdminPinError() {
    ActivityScenario.launch<PinPasswordActivity>(
      PinPasswordActivity.createPinPasswordActivityIntent(
        context,
        adminPin,
        userId
      )
    ).use {
      onView(withId(R.id.forgot_pin)).perform(click())
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin)))).perform(
        typeText("1234"),
        closeSoftKeyboard()
      )
      onView(withText(context.getString(R.string.admin_settings_submit))).perform(click())
      onView(
        allOf(
          withId(R.id.error_text),
          isDescendantOfA(withId(R.id.input_pin))
        )
      ).check(matches(withText(context.getString(R.string.admin_settings_incorrect))))

      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin)))).perform(
        typeText("5"),
        closeSoftKeyboard()
      )
      onView(allOf(withId(R.id.error_text), isDescendantOfA(withId(R.id.input_pin)))).check(matches(withText("")))
    }
  }

  @Test
  fun testPinPasswordActivityWithUser_clickForgot_inputAdminPin_inputShortPin_checkPinLengthError() {
    ActivityScenario.launch<PinPasswordActivity>(
      PinPasswordActivity.createPinPasswordActivityIntent(
        context,
        adminPin,
        userId
      )
    ).use {
      onView(withId(R.id.forgot_pin)).perform(click())
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin)))).perform(
        typeText("12345"),
        closeSoftKeyboard()
      )
      onView(withText(context.getString(R.string.admin_settings_submit))).perform(click())
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin)))).perform(
        typeText("32"),
        closeSoftKeyboard()
      )
      onView(withText(context.getString(R.string.admin_settings_submit))).perform(click())
      onView(
        allOf(
          withId(R.id.error_text),
          isDescendantOfA(withId(R.id.input_pin))
        )
      ).check(matches(withText(context.getString(R.string.add_profile_error_pin_length))))

      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin)))).perform(
        typeText("1"),
        closeSoftKeyboard()
      )
      onView(allOf(withId(R.id.error_text), isDescendantOfA(withId(R.id.input_pin)))).check(matches(withText("")))
    }
  }

  @Test
  fun testPinPasswordActivityWithUser_clickForgot_inputAdminPin_inputNewPin_inputOldPin_checkWrongPinError() {
    ActivityScenario.launch<PinPasswordActivity>(
      PinPasswordActivity.createPinPasswordActivityIntent(
        context,
        adminPin,
        userId
      )
    ).use {
      onView(withId(R.id.input_pin)).perform(typeText(""), closeSoftKeyboard())
      onView(withId(R.id.forgot_pin)).perform(click())
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin)))).perform(
        typeText("12345"),
        closeSoftKeyboard()
      )
      onView(withText(context.getString(R.string.admin_settings_submit))).perform(click())
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin)))).perform(
        typeText("321"),
        closeSoftKeyboard()
      )
      onView(withText(context.getString(R.string.admin_settings_submit))).perform(click())
      onView(withText(context.getString(R.string.pin_password_close))).perform(click())
      onView(withId(R.id.input_pin)).perform(typeText("123"), closeSoftKeyboard())
      onView(withText(context.getString(R.string.pin_password_incorrect_pin))).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testPinPasswordActivityWithUser_clickForgot_inputAdminPin_inputNewPin_inputNewPin_checkOpensHomeActivity() {
    ActivityScenario.launch<PinPasswordActivity>(
      PinPasswordActivity.createPinPasswordActivityIntent(
        context,
        adminPin,
        userId
      )
    ).use {
      onView(withId(R.id.forgot_pin)).perform(click())
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin)))).perform(
        typeText("12345"),
        closeSoftKeyboard()
      )
      onView(withText(context.getString(R.string.admin_settings_submit))).perform(click())
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin)))).perform(
        typeText("321"),
        closeSoftKeyboard()
      )
      onView(withText(context.getString(R.string.admin_settings_submit))).perform(click())
      onView(withText(context.getString(R.string.pin_password_close))).perform(click())
      onView(withId(R.id.input_pin)).perform(typeText("321"))
      intended(hasComponent(HomeActivity::class.java.name))
    }
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

  @Singleton
  @Component(modules = [TestModule::class])
  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun inject(pinPasswordActivityTest: PinPasswordActivityTest)
  }
}
