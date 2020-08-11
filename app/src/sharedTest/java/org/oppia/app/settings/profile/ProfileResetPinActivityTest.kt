package org.oppia.app.settings.profile

import android.app.Application
import android.content.Context
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.clearText
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.isClickable
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
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
import org.hamcrest.CoreMatchers.not
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.R
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

@RunWith(AndroidJUnit4::class)
class ProfileResetPinActivityTest {

  @Inject
  lateinit var context: Context

  @Inject
  lateinit var profileTestHelper: ProfileTestHelper

  @Before
  fun setUp() {
    Intents.init()
    setUpTestApplicationComponent()
    profileTestHelper.initializeProfiles()
    FirebaseApp.initializeApp(context)
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

  /* ktlint-disable max-line-length */
  @Test
  fun testProfileResetPinActivity_startActivityWithAdmin_inputPin_inputConfirmPin_clickSave_checkReturnsToProfileEditActivity() {
    launch<ProfileResetPinActivity>(
      ProfileResetPinActivity.createProfileResetPinActivity(
        context,
        0,
        true
      )
    ).use {
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin)))).perform(
        typeText("12345"),
        closeSoftKeyboard()
      )
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_confirm_pin)))).perform(
        typeText("12345"),
        closeSoftKeyboard()
      )
      onView(withId(R.id.profile_reset_save_button)).perform(click())
      intended(hasComponent(ProfileEditActivity::class.java.name))
    }
  }
  /* ktlint-enable max-line-length */

  /* ktlint-disable max-line-length */
  @Test
  fun testProfileResetPinActivity_startActivityWithAdmin_changeConfiguration_inputPin_inputConfirmPin_clickSave_checkReturnsToProfileEditActivity() {
    launch<ProfileResetPinActivity>(
      ProfileResetPinActivity.createProfileResetPinActivity(
        context,
        0,
        true
      )
    ).use {
      onView(isRoot()).perform(orientationLandscape())
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin)))).perform(scrollTo())
        .perform(
          typeText("12345"),
          closeSoftKeyboard()
        )
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_confirm_pin)))).perform(
        scrollTo()
      ).perform(
        typeText("12345"),
        closeSoftKeyboard()
      )

      onView(withId(R.id.profile_reset_save_button)).perform(scrollTo()).perform(click())
      intended(hasComponent(ProfileEditActivity::class.java.name))
    }
  }
  /* ktlint-enable max-line-length */

  /* ktlint-disable max-line-length */
  @Test
  fun testProfileResetPinActivity_startActivityWithUser_inputPin_inputConfirmPin_clickSave_checkReturnsToProfileEditActivity() {
    launch<ProfileResetPinActivity>(
      ProfileResetPinActivity.createProfileResetPinActivity(
        context,
        1,
        false
      )
    ).use {
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin)))).perform(
        typeText("123"),
        closeSoftKeyboard()
      )
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_confirm_pin)))).perform(
        typeText("123"),
        closeSoftKeyboard()
      )
      onView(withId(R.id.profile_reset_save_button)).perform(click())
      intended(hasComponent(ProfileEditActivity::class.java.name))
    }
  }
  /* ktlint-enable max-line-length */

  /* ktlint-disable max-line-length */
  @Test
  fun testProfileResetPinActivity_startActivityWithAdmin_inputShortPin_clickSave_checkPinLengthError() {
    launch<ProfileResetPinActivity>(
      ProfileResetPinActivity.createProfileResetPinActivity(
        context,
        0,
        true
      )
    ).use {
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin)))).perform(
        typeText("1234"),
        closeSoftKeyboard()
      )
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_confirm_pin)))).perform(
        typeText("1234"),
        closeSoftKeyboard()
      )
      onView(withId(R.id.profile_reset_save_button)).perform(click())
      onView(
        allOf(
          withId(R.id.error_text),
          isDescendantOfA(withId(R.id.input_pin))
        )
      ).check(matches(withText(context.getString(R.string.profile_reset_pin_error_admin_pin_length))))
    }
  }
  /* ktlint-enable max-line-length */

  /* ktlint-disable max-line-length */
  @Test
  fun testProfileResetPinActivity_startActivityWithAdmin_inputShortPin_clickSave_changeConfiguration_checkPinLengthError() {
    launch<ProfileResetPinActivity>(
      ProfileResetPinActivity.createProfileResetPinActivity(
        context,
        0,
        true
      )
    ).use {
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin)))).perform(
        typeText("1234"),
        closeSoftKeyboard()
      )
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_confirm_pin)))).perform(
        typeText("1234"),
        closeSoftKeyboard()
      )
      onView(withId(R.id.profile_reset_save_button)).perform(scrollTo()).perform(click())
      onView(isRoot()).perform(orientationLandscape())
      onView(
        allOf(
          withId(R.id.error_text),
          isDescendantOfA(withId(R.id.input_pin))
        )
      ).perform(scrollTo()).check(
        matches(withText(context.getString(R.string.profile_reset_pin_error_admin_pin_length)))
      )
    }
  }
  /* ktlint-enable max-line-length */

  /* ktlint-disable max-line-length */
  @Test
  fun testProfileResetPinActivity_startActivityWithAdmin_inputShortPin_clickSave_inputPin_checkErrorIsCleared() {
    launch<ProfileResetPinActivity>(
      ProfileResetPinActivity.createProfileResetPinActivity(
        context,
        0,
        true
      )
    ).use {
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin)))).perform(
        typeText("1234"),
        closeSoftKeyboard()
      )
      onView(withId(R.id.profile_reset_save_button)).perform(click())
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
  /* ktlint-enable max-line-length */

  /* ktlint-disable max-line-length */
  @Test
  fun testProfileResetPinActivity_startActivityWithAdmin_inputShortPin_clickSave_inputPin_configurationChange_checkErrorIsCleared() {
    launch<ProfileResetPinActivity>(
      ProfileResetPinActivity.createProfileResetPinActivity(
        context,
        0,
        true
      )
    ).use {
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin)))).perform(
        typeText("1234"),
        closeSoftKeyboard()
      )
      onView(withId(R.id.profile_reset_save_button)).perform(click())
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
  fun testProfileResetPinActivity_startActivityWithAdmin_inputWrongConfirmPin_clickSave_checkConfirmWrongError() {
    launch<ProfileResetPinActivity>(
      ProfileResetPinActivity.createProfileResetPinActivity(
        context,
        0,
        true
      )
    ).use {
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin)))).perform(
        typeText("12345"),
        closeSoftKeyboard()
      )
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_confirm_pin)))).perform(
        typeText("1234"),
        closeSoftKeyboard()
      )
      onView(withId(R.id.profile_reset_save_button)).perform(click())
      onView(allOf(withId(R.id.error_text), isDescendantOfA(withId(R.id.input_confirm_pin)))).check(
        matches(
          withText(
            context.getString(R.string.add_profile_error_pin_confirm_wrong)
          )
        )
      )
    }
  }
  /* ktlint-enable max-line-length */

  /* ktlint-disable max-line-length */
  @Test
  fun testProfileResetPinActivity_startActivityWithAdmin_inputWrongConfirmPin_clickSave_changeConfiguration_checkConfirmWrongError() {
    launch<ProfileResetPinActivity>(
      ProfileResetPinActivity.createProfileResetPinActivity(
        context,
        0,
        true
      )
    ).use {
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin)))).perform(
        typeText("12345"),
        closeSoftKeyboard()
      )
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_confirm_pin)))).perform(
        typeText("1234"),
        closeSoftKeyboard()
      )
      onView(withId(R.id.profile_reset_save_button)).perform(click())
      onView(isRoot()).perform(orientationLandscape())
      onView(
        allOf(
          withId(R.id.error_text),
          isDescendantOfA(withId(R.id.input_confirm_pin))
        )
      ).perform(scrollTo()).check(
        matches(
          withText(
            context.getString(R.string.add_profile_error_pin_confirm_wrong)
          )
        )
      )
    }
  }
  /* ktlint-enable max-line-length */

  /* ktlint-disable max-line-length */
  @Test
  fun testProfileResetPinActivity_inputPin_inputConfirmPin_changeConfiguration_inputPinExists_confirmInputPinExists_saveButtonIsClickable() {
    launch<ProfileResetPinActivity>(
      ProfileResetPinActivity.createProfileResetPinActivity(
        context,
        0,
        true
      )
    ).use {
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin)))).perform(
        typeText("12345"),
        closeSoftKeyboard()
      )
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_confirm_pin)))).perform(
        typeText("12345"),
        closeSoftKeyboard()
      )
      onView(isRoot()).perform(orientationLandscape())
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin)))).perform(scrollTo())
        .check(matches(withText("12345")))
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.input_confirm_pin))
        )
      ).perform(scrollTo()).check(matches(withText("12345")))
      onView(withId(R.id.profile_reset_save_button)).perform(scrollTo())
        .check(matches(isClickable()))
    }
  }
  /* ktlint-enable max-line-length */

  /* ktlint-disable max-line-length */
  @Test
  fun testProfileResetPinActivity_startActivityWithAdmin_inputWrongConfirmPin_clickSave_inputConfirmPin_checkErrorIsCleared() {
    launch<ProfileResetPinActivity>(
      ProfileResetPinActivity.createProfileResetPinActivity(
        context,
        0,
        true
      )
    ).use {
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin)))).perform(
        typeText("12345"),
        closeSoftKeyboard()
      )
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_confirm_pin)))).perform(
        typeText("1234"),
        closeSoftKeyboard()
      )
      onView(withId(R.id.profile_reset_save_button)).perform(click())
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_confirm_pin)))).perform(
        typeText("5"),
        closeSoftKeyboard()
      )
      onView(
        allOf(
          withId(R.id.error_text),
          isDescendantOfA(withId(R.id.input_confirm_pin))
        )
      ).check(matches(withText("")))
    }
  }
  /* ktlint-enable max-line-length */

  /* ktlint-disable max-line-length */
  @Test
  fun testProfileResetPinActivity_startActivityWithUser_inputShortPin_clickSave_checkPinLengthError() {
    launch<ProfileResetPinActivity>(
      ProfileResetPinActivity.createProfileResetPinActivity(
        context,
        1,
        false
      )
    ).use {
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin)))).perform(
        typeText("12"),
        closeSoftKeyboard()
      )
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_confirm_pin)))).perform(
        typeText("12"),
        closeSoftKeyboard()
      )
      onView(withId(R.id.profile_reset_save_button)).perform(click())
      onView(
        allOf(
          withId(R.id.error_text),
          isDescendantOfA(withId(R.id.input_pin))
        )
      ).check(matches(withText(context.getString(R.string.profile_reset_pin_error_user_pin_length))))
    }
  }
  /* ktlint-enable max-line-length */

  /* ktlint-disable max-line-length */
  @Test
  fun testProfileResetPinActivity_startActivityWithUser_inputShortPin_clickSave_inputPin_checkErrorIsCleared() {
    launch<ProfileResetPinActivity>(
      ProfileResetPinActivity.createProfileResetPinActivity(
        context,
        1,
        false
      )
    ).use {
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin)))).perform(
        typeText("12"),
        closeSoftKeyboard()
      )
      onView(withId(R.id.profile_reset_save_button)).perform(click())
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin)))).perform(
        typeText("3"),
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
  fun testProfileResetPinActivity_startActivityWithUser_inputWrongConfirmPin_clickSave_checkConfirmWrongError() {
    launch<ProfileResetPinActivity>(
      ProfileResetPinActivity.createProfileResetPinActivity(
        context,
        1,
        false
      )
    ).use {
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin)))).perform(
        typeText("123"),
        closeSoftKeyboard()
      )
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_confirm_pin)))).perform(
        typeText("12"),
        closeSoftKeyboard()
      )
      onView(withId(R.id.profile_reset_save_button)).perform(click())
      onView(allOf(withId(R.id.error_text), isDescendantOfA(withId(R.id.input_confirm_pin)))).check(
        matches(
          withText(
            context.getString(R.string.add_profile_error_pin_confirm_wrong)
          )
        )
      )
    }
  }
  /* ktlint-enable max-line-length */

  /* ktlint-disable max-line-length */
  @Test
  fun testProfileResetPinActivity_startActivityWithUser_inputWrongConfirmPin_clickSave_inputConfirmPin_checkErrorIsCleared() {
    launch<ProfileResetPinActivity>(
      ProfileResetPinActivity.createProfileResetPinActivity(
        context,
        1,
        false
      )
    ).use {
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin)))).perform(
        typeText("123"),
        closeSoftKeyboard()
      )
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_confirm_pin)))).perform(
        typeText("12"),
        closeSoftKeyboard()
      )
      onView(withId(R.id.profile_reset_save_button)).perform(click())
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_confirm_pin)))).perform(
        typeText("3"),
        closeSoftKeyboard()
      )
      onView(
        allOf(
          withId(R.id.error_text),
          isDescendantOfA(withId(R.id.input_confirm_pin))
        )
      ).check(matches(withText("")))
    }
  }
  /* ktlint-enable max-line-length */

  @Test
  fun testProfileResetPinActivity_default_saveButtonIsNotClickable() {
    launch<ProfileResetPinActivity>(
      ProfileResetPinActivity.createProfileResetPinActivity(
        context,
        1,
        false
      )
    ).use {
      onView(withId(R.id.profile_reset_save_button)).check(matches(not(isClickable())))
    }
  }

  @Test
  fun testProfileResetPinActivity_default_changeConfiguration_saveButtonIsNotClickable() {
    launch<ProfileResetPinActivity>(
      ProfileResetPinActivity.createProfileResetPinActivity(
        context,
        1,
        false
      )
    ).use {
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.profile_reset_save_button)).perform(scrollTo())
        .check(matches(not(isClickable())))
    }
  }

  @Test
  fun testProfileResetPinActivity_inputPin_saveButtonIsNotClickable() {
    launch<ProfileResetPinActivity>(
      ProfileResetPinActivity.createProfileResetPinActivity(
        context,
        1,
        false
      )
    ).use {
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin)))).perform(
        typeText("123"),
        closeSoftKeyboard()
      )
      onView(withId(R.id.profile_reset_save_button)).check(matches(not(isClickable())))
    }
  }

  @Test
  fun testProfileResetPinActivity_inputPin_changeConfiguration_saveButtonIsNotClickable() {
    launch<ProfileResetPinActivity>(
      ProfileResetPinActivity.createProfileResetPinActivity(
        context,
        1,
        false
      )
    ).use {
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin)))).perform(
        typeText("123"),
        closeSoftKeyboard()
      )
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.profile_reset_save_button)).perform(scrollTo())
        .check(matches(not(isClickable())))
    }
  }

  @Test
  fun testProfileResetPinActivity_inputPin_inputConfirmPin_saveButtonIsClickable() {
    launch<ProfileResetPinActivity>(
      ProfileResetPinActivity.createProfileResetPinActivity(
        context,
        1,
        false
      )
    ).use {
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin)))).perform(
        typeText("123"),
        closeSoftKeyboard()
      )
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_confirm_pin)))).perform(
        typeText("12"),
        closeSoftKeyboard()
      )
      onView(withId(R.id.profile_reset_save_button)).check(matches(isClickable()))
    }
  }

  /* ktlint-disable max-line-length */
  @Test
  fun testProfileResetPinActivity_inputPin_inputConfirmPin_saveButtonIsClickable_clearInputPin_saveButtonIsNotClickable() {
    launch<ProfileResetPinActivity>(
      ProfileResetPinActivity.createProfileResetPinActivity(
        context,
        1,
        false
      )
    ).use {
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin)))).perform(
        typeText("123"),
        closeSoftKeyboard()
      )
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_confirm_pin)))).perform(
        typeText("12"),
        closeSoftKeyboard()
      )
      onView(withId(R.id.profile_reset_save_button)).check(matches(isClickable()))
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin)))).perform(
        clearText(),
        closeSoftKeyboard()
      )
      onView(withId(R.id.profile_reset_save_button)).check(matches(not(isClickable())))
    }
  }
  /* ktlint-enable max-line-length */

  /* ktlint-disable max-line-length */
  @Test
  fun testProfileResetPinActivity_inputPin_inputConfirmPin_saveButtonIsClickable_clearConfirmInputPin_saveButtonIsNotClickable() {
    launch<ProfileResetPinActivity>(
      ProfileResetPinActivity.createProfileResetPinActivity(
        context,
        1,
        false
      )
    ).use {
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin)))).perform(
        typeText("123"),
        closeSoftKeyboard()
      )
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_confirm_pin)))).perform(
        typeText("12"),
        closeSoftKeyboard()
      )
      onView(withId(R.id.profile_reset_save_button)).check(matches(isClickable()))
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_confirm_pin)))).perform(
        clearText(),
        closeSoftKeyboard()
      )
      onView(withId(R.id.profile_reset_save_button)).check(matches(not(isClickable())))
    }
  }
  /* ktlint-enable max-line-length */

  /* ktlint-disable max-line-length */
  @Test
  fun testProfileResetPinActivity_inputPin_inputConfirmPin_saveButtonIsClickable_clearConfirmInputPin_changeConfiguration_saveButtonIsNotClickable() {
    launch<ProfileResetPinActivity>(
      ProfileResetPinActivity.createProfileResetPinActivity(
        context,
        1,
        false
      )
    ).use {
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin)))).perform(
        typeText("123"),
        closeSoftKeyboard()
      )
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_confirm_pin)))).perform(
        typeText("12"),
        closeSoftKeyboard()
      )
      onView(withId(R.id.profile_reset_save_button)).check(matches(isClickable()))
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_confirm_pin)))).perform(
        clearText(),
        closeSoftKeyboard()
      )
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.profile_reset_save_button)).perform(scrollTo())
        .check(matches(not(isClickable())))
    }
  }
  /* ktlint-enable max-line-length */

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

    fun inject(profileResetPinActivity: ProfileResetPinActivityTest)
  }
}
