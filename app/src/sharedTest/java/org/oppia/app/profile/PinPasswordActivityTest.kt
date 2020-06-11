package org.oppia.app.profile

import android.app.Activity
import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.content.Context.ACTIVITY_SERVICE
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
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage
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
import org.oppia.app.utility.EspressoTestsMatchers.withDrawable
import org.oppia.app.utility.OrientationChangeAction.Companion.orientationLandscape
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

private const val TIMEOUT = 1000L
private const val CONDITION_CHECK_INTERVAL = 100L

@RunWith(AndroidJUnit4::class)
class PinPasswordActivityTest {

  @Inject
  lateinit var context: Context

  @Inject
  lateinit var profileTestHelper: ProfileTestHelper

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
      closeSoftKeyboard()
      onView(withId(R.id.input_pin)).perform(typeText("12345"), closeSoftKeyboard())
      waitUntilActivityVisible<HomeActivity>()
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
      waitUntilActivityVisible<HomeActivity>()
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
      closeSoftKeyboard()
      onView(withId(R.id.input_pin)).perform(closeSoftKeyboard())
        .perform(typeText("54321"), closeSoftKeyboard())
      onView(withText(context.getString(R.string.pin_password_incorrect_pin))).check(
        matches(
          isDisplayed()
        )
      )
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
      onView(withText(context.getString(R.string.pin_password_incorrect_pin))).check(
        matches(
          isDisplayed()
        )
      )
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
      onView(withText(context.getString(R.string.pin_password_forgot_message))).check(
        matches(
          isDisplayed()
        )
      )
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
      onView(withId(R.id.input_pin)).perform(typeText(""), closeSoftKeyboard())
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
      onView(allOf(withId(R.id.error_text), isDescendantOfA(withId(R.id.input_pin)))).check(
        matches(
          withText("")
        )
      )
    }
  }

  /* ktlint-disable max-line-length */
  @Test
  fun testPinPasswordActivityWithUser_clickForgot_inputAdminPin_inputShortPin_checkPinLengthError() {
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
      onView(allOf(withId(R.id.error_text), isDescendantOfA(withId(R.id.input_pin)))).check(
        matches(
          withText("")
        )
      )
    }
  }
  /* ktlint-enable max-line-length */

  /* ktlint-disable max-line-length */
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
      onView(withText(context.getString(R.string.pin_password_incorrect_pin))).check(
        matches(
          isDisplayed()
        )
      )
    }
  }
  /* ktlint-enable max-line-length */

  /* ktlint-disable max-line-length */
  @Test
  fun testPinPasswordActivityWithUser_clickForgot_inputAdminPin_inputNewPin_inputNewPin_checkOpensHomeActivity() {
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
      onView(withId(R.id.input_pin)).perform(typeText("321"))
      waitUntilActivityVisible<HomeActivity>()
      intended(hasComponent(HomeActivity::class.java.name))
    }
  }
  /* ktlint-enable max-line-length */

  /* ktlint-disable max-line-length */
  @Test
  fun testPinPasswordActivityWithUser_clickForgot_inputAdminPin_changeConfiguration_checkInputPinIsPresent() {
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
        typeText("1234"),
        closeSoftKeyboard()
      )
      onView(isRoot()).perform(orientationLandscape())
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin)))).check(
        matches(
          withText("1234")
        )
      )
    }
  }
  /* ktlint-enable max-line-length */

  /* ktlint-disable max-line-length */
  @Test
  fun testPinPasswordActivityWithUser_clickForgot_inputAdminPin_clickSubmit_changeConfiguration_restPinDialogIsDisplayed() {
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
      onView(isRoot()).perform(orientationLandscape())
      onView(withText(context.getString(R.string.reset_pin_enter))).check(matches(isDisplayed()))
    }
  }
  /* ktlint-enable max-line-length */

  /* ktlint-disable max-line-length */
  @Test
  fun testPinPasswordActivityWithUser_clickForgot_inputAdminPin_clickSubmit_inputNewPin_changeConfiguration_clickSubmit_pinChangeIsSuccessful() {
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
        typeText("123"),
        closeSoftKeyboard()
      )
      onView(isRoot()).perform(orientationLandscape())
      onView(withText(context.getString(R.string.admin_settings_submit))).perform(click())
      onView(withText(context.getString(R.string.pin_password_success))).check(matches(isDisplayed()))
    }
  }
  /* ktlint-enable max-line-length */

  /* ktlint-disable max-line-length */
  @Test
  fun testPinPasswordActivityWithAdmin_clickForgot_changeConfiguration_checkOpensAdminForgotDialog() {
    ActivityScenario.launch<PinPasswordActivity>(
      PinPasswordActivity.createPinPasswordActivityIntent(
        context,
        adminPin,
        adminId
      )
    ).use {
      onView(withId(R.id.input_pin)).perform(typeText(""), closeSoftKeyboard())
      onView(withId(R.id.forgot_pin)).perform(click())
      onView(isRoot()).perform(orientationLandscape())
      onView(withText(context.getString(R.string.pin_password_forgot_message))).check(
        matches(
          isDisplayed()
        )
      )
    }
  }
  /* ktlint-enable max-line-length */

  /* ktlint-disable max-line-length */
  @Test
  fun testPinPasswordActivityWithUser_clickForgot_inputWrongAdminPin_changeConfiguration_checkWrongAdminPinError() {
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
      onView(isRoot()).perform(orientationLandscape())
      onView(allOf(withId(R.id.error_text), isDescendantOfA(withId(R.id.input_pin)))).check(
        matches(
          withText("")
        )
      )
    }
  }
  /* ktlint-enable max-line-length */

  /* ktlint-disable max-line-length */
  @Test
  fun testPinPasswordActivityWithUser_clickForgot_inputAdminPin_inputIncorrectPin_clickSubmit_changeConfiguration_errorIsDisplayed() {
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
        typeText("11"),
        closeSoftKeyboard()
      )
      onView(withText(context.getString(R.string.admin_settings_submit))).perform(click())
      onView(isRoot()).perform(orientationLandscape())
      onView(
        allOf(
          withId(R.id.error_text),
          isDescendantOfA(withId(R.id.input_pin))
        )
      ).check(matches(withText(R.string.add_profile_error_pin_length)))
    }
  }
  /* ktlint-enable max-line-length */

  @Test
  @ExperimentalCoroutinesApi
  fun testPinPasswordActivityWithAdmin_inputWrongPin_changeConfiguration_checkIncorrectPinShows() {
    ActivityScenario.launch<PinPasswordActivity>(
      PinPasswordActivity.createPinPasswordActivityIntent(
        context,
        adminPin,
        adminId
      )
    ).use {
      closeSoftKeyboard()
      closeSoftKeyboard()
      onView(withId(R.id.input_pin)).perform(typeText("54321"), closeSoftKeyboard())
      onView(isRoot()).perform(orientationLandscape())
      onView(withText(context.getString(R.string.pin_password_incorrect_pin))).check(
        matches(
          isDisplayed()
        )
      )
    }
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testPinPasswordActivityWithAdmin_checkShowHidePassword_defaultText() {
    ActivityScenario.launch<PinPasswordActivity>(
      PinPasswordActivity.createPinPasswordActivityIntent(
        context,
        adminPin,
        adminId
      )
    ).use {
      onView(withId(R.id.input_pin)).perform(typeText(""), closeSoftKeyboard())
      onView(withText(context.getString(R.string.pin_password_show))).check(matches(isDisplayed()))
    }
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testPinPasswordActivityWithAdmin_checkShowHidePassword_defaultImage() {
    ActivityScenario.launch<PinPasswordActivity>(
      PinPasswordActivity.createPinPasswordActivityIntent(
        context,
        adminPin,
        adminId
      )
    ).use {
      onView(withId(R.id.show_hide_password_image_view))
        .check(
          matches(
            withDrawable(
              R.drawable.ic_show_eye_icon
            )
          )
        )
    }
  }

  /* ktlint-disable max-line-length */
  @Test
  @ExperimentalCoroutinesApi
  fun testPinPasswordActivityWithAdmin_checkShowHidePassword_clickShowHidePassword_textChangesToHide() {
    ActivityScenario.launch<PinPasswordActivity>(
      PinPasswordActivity.createPinPasswordActivityIntent(
        context,
        adminPin,
        adminId
      )
    ).use {
      onView(withId(R.id.input_pin)).perform(typeText(""), closeSoftKeyboard())
      onView(withId(R.id.show_pin)).perform(click())
      onView(withText(context.getString(R.string.pin_password_hide))).check(matches(isDisplayed()))
    }
  }
  /* ktlint-enable max-line-length */

  /* ktlint-disable max-line-length */
  @Test
  @ExperimentalCoroutinesApi
  fun testPinPasswordActivityWithAdmin_checkShowHidePassword_clickShowHidePassword_imageChangesToHide() {
    ActivityScenario.launch<PinPasswordActivity>(
      PinPasswordActivity.createPinPasswordActivityIntent(
        context,
        adminPin,
        adminId
      )
    ).use {
      onView(withId(R.id.input_pin)).perform(typeText(""), closeSoftKeyboard())
      onView(withId(R.id.show_pin)).perform(click())
      onView(withId(R.id.show_hide_password_image_view))
        .check(
          matches(
            withDrawable(
              R.drawable.ic_hide_eye_icon
            )
          )
        )
    }
  }
  /* ktlint-enable max-line-length */

  /* ktlint-disable max-line-length */
  @Test
  @ExperimentalCoroutinesApi
  fun testPinPasswordActivityWithAdmin_checkShowHidePassword_clickShowHidePassword_changeConfiguration_hideViewIsShown() {
    ActivityScenario.launch<PinPasswordActivity>(
      PinPasswordActivity.createPinPasswordActivityIntent(
        context,
        adminPin,
        adminId
      )
    ).use {
      closeSoftKeyboard()
      onView(withId(R.id.input_pin)).perform(typeText(""), closeSoftKeyboard())
      onView(withId(R.id.show_pin)).perform(click())
      onView(isRoot()).perform(orientationLandscape())
      onView(withText(context.getString(R.string.pin_password_hide))).check(matches(isDisplayed()))
      onView(withId(R.id.show_hide_password_image_view))
        .check(
          matches(
            withDrawable(
              R.drawable.ic_hide_eye_icon
            )
          )
        )
    }
  }
  /* ktlint-enable max-line-length */

  private fun getCurrentActivity(): Activity? {
    var currentActivity: Activity? = null
    getInstrumentation().runOnMainSync {
      run {
        currentActivity = ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(
          Stage.RESUMED
        ).elementAtOrNull(0)
      }
    }
    return currentActivity
  }

  private inline fun <reified T : Activity> isVisible(): Boolean {
    val am =
      InstrumentationRegistry.getInstrumentation().targetContext.getSystemService(
        ACTIVITY_SERVICE
      ) as ActivityManager
    val visibleActivityName = this.getCurrentActivity()!!::class.java.name
    return visibleActivityName == T::class.java.name
  }

  private inline fun <reified T : Activity> waitUntilActivityVisible() {
    val startTime = System.currentTimeMillis()
    while (!isVisible<T>()) {
      Thread.sleep(CONDITION_CHECK_INTERVAL)
      if (System.currentTimeMillis() - startTime >= TIMEOUT) {
        throw AssertionError(
          "Activity ${T::class.java.simpleName} not visible after $TIMEOUT milliseconds"
        )
      }
    }
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
