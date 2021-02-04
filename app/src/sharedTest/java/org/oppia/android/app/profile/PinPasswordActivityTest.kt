package org.oppia.android.app.profile

import android.app.Application
import android.content.Context
import android.view.View
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.hasFocus
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withInputType
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.chaos.view.PinView
import com.google.android.material.textfield.TextInputLayout
import dagger.Component
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.hamcrest.TypeSafeMatcher
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityComponent
import org.oppia.android.app.application.ActivityComponentFactory
import org.oppia.android.app.application.ApplicationComponent
import org.oppia.android.app.application.ApplicationInjector
import org.oppia.android.app.application.ApplicationInjectorProvider
import org.oppia.android.app.application.ApplicationModule
import org.oppia.android.app.application.ApplicationStartupListenerModule
import org.oppia.android.app.home.HomeActivity
import org.oppia.android.app.player.state.hintsandsolution.HintsAndSolutionConfigModule
import org.oppia.android.app.shim.ViewBindingShimModule
import org.oppia.android.app.utility.EspressoTestsMatchers.withDrawable
import org.oppia.android.app.utility.OrientationChangeAction.Companion.orientationLandscape
import org.oppia.android.domain.classify.InteractionsModule
import org.oppia.android.domain.classify.rules.continueinteraction.ContinueModule
import org.oppia.android.domain.classify.rules.dragAndDropSortInput.DragDropSortInputModule
import org.oppia.android.domain.classify.rules.fractioninput.FractionInputModule
import org.oppia.android.domain.classify.rules.imageClickInput.ImageClickInputModule
import org.oppia.android.domain.classify.rules.itemselectioninput.ItemSelectionInputModule
import org.oppia.android.domain.classify.rules.multiplechoiceinput.MultipleChoiceInputModule
import org.oppia.android.domain.classify.rules.numberwithunits.NumberWithUnitsRuleModule
import org.oppia.android.domain.classify.rules.numericinput.NumericInputRuleModule
import org.oppia.android.domain.classify.rules.ratioinput.RatioInputModule
import org.oppia.android.domain.classify.rules.textinput.TextInputRuleModule
import org.oppia.android.domain.onboarding.ExpirationMetaDataRetrieverModule
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.domain.oppialogger.loguploader.LogUploadWorkerModule
import org.oppia.android.domain.oppialogger.loguploader.WorkManagerConfigurationModule
import org.oppia.android.domain.question.QuestionModule
import org.oppia.android.domain.topic.PrimeTopicAssetsControllerModule
import org.oppia.android.testing.EditTextInputAction
import org.oppia.android.testing.RobolectricModule
import org.oppia.android.testing.TestAccessibilityModule
import org.oppia.android.testing.TestCoroutineDispatchers
import org.oppia.android.testing.TestDispatcherModule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.profile.ProfileTestHelper
import org.oppia.android.util.caching.testing.CachingTestModule
import org.oppia.android.util.gcsresource.GcsResourceModule
import org.oppia.android.util.logging.LoggerModule
import org.oppia.android.util.logging.firebase.FirebaseLogUploaderModule
import org.oppia.android.util.parser.GlideImageLoaderModule
import org.oppia.android.util.parser.HtmlParserEntityTypeModule
import org.oppia.android.util.parser.ImageParsingModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(
  application = PinPasswordActivityTest.TestApplication::class,
  qualifiers = "port-xxhdpi"
)
class PinPasswordActivityTest {

  @Inject
  lateinit var context: Context

  @Inject
  lateinit var profileTestHelper: ProfileTestHelper

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @Inject
  lateinit var editTextInputAction: EditTextInputAction

  private val adminPin = "12345"
  private val adminId = 0
  private val userId = 1

  @Before
  fun setUp() {
    Intents.init()
    setUpTestApplicationComponent()
    profileTestHelper.initializeProfiles()
    testCoroutineDispatchers.registerIdlingResource()
  }

  @After
  fun tearDown() {
    testCoroutineDispatchers.unregisterIdlingResource()
    Intents.release()
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  @Test
  fun testPinPassword_withAdmin_keyboardIsVisibleByDefault() {
    ActivityScenario.launch<PinPasswordActivity>(
      PinPasswordActivity.createPinPasswordActivityIntent(
        context = context,
        adminPin = adminPin,
        profileId = adminId
      )
    ).use {
      onView(withId(R.id.input_pin)).check(matches(hasFocus()))
    }
  }

  @Test
  fun testPinPassword_withAdmin_inputCorrectPin_opensHomeActivity() {
    ActivityScenario.launch<PinPasswordActivity>(
      PinPasswordActivity.createPinPasswordActivityIntent(
        context = context,
        adminPin = adminPin,
        profileId = adminId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.input_pin)).perform(editTextInputAction.appendText("12345"))
      testCoroutineDispatchers.runCurrent()
      intended(hasComponent(HomeActivity::class.java.name))
    }
  }

  @Test
  fun testPinPassword_withUser_inputCorrectPin_opensHomeActivity() {
    ActivityScenario.launch<PinPasswordActivity>(
      PinPasswordActivity.createPinPasswordActivityIntent(
        context = context,
        adminPin = adminPin,
        profileId = userId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.input_pin)).perform(editTextInputAction.appendText("123"))
      testCoroutineDispatchers.runCurrent()
      intended(hasComponent(HomeActivity::class.java.name))
    }
  }

  @Test
  fun testPinPassword_withAdmin_inputWrongPin_incorrectPinShows() {
    ActivityScenario.launch<PinPasswordActivity>(
      PinPasswordActivity.createPinPasswordActivityIntent(
        context = context,
        adminPin = adminPin,
        profileId = adminId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      closeSoftKeyboard()
      onView(withId(R.id.input_pin)).perform(closeSoftKeyboard())
        .perform(editTextInputAction.appendText("54321"), closeSoftKeyboard())
      onView(withText(context.getString(R.string.pin_password_incorrect_pin))).check(
        matches(
          isDisplayed()
        )
      )
    }
  }

  @Test
  fun testPinPassword_withUser_inputWrongPin_incorrectPinShows() {
    ActivityScenario.launch<PinPasswordActivity>(
      PinPasswordActivity.createPinPasswordActivityIntent(
        context = context,
        adminPin = adminPin,
        profileId = userId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.input_pin)).perform(
        editTextInputAction.appendText("321"), closeSoftKeyboard()
      )
      onView(withText(context.getString(R.string.pin_password_incorrect_pin))).check(
        matches(
          isDisplayed()
        )
      )
    }
  }

  @Test
  fun testPinPassword_withAdmin_forgot_opensAdminForgotDialog() {
    ActivityScenario.launch<PinPasswordActivity>(
      PinPasswordActivity.createPinPasswordActivityIntent(
        context = context,
        adminPin = adminPin,
        profileId = adminId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.input_pin)).perform(
        editTextInputAction.appendText(""),
        closeSoftKeyboard()
      )
      onView(withId(R.id.forgot_pin)).perform(click())
      onView(withText(context.getString(R.string.pin_password_forgot_message)))
        .inRoot(isDialog())
        .check(matches(isDisplayed()))
    }
  }

  @Test
  fun testPinPassword_withUser_forgot_inputWrongAdminPin_wrongAdminPinError() {
    ActivityScenario.launch<PinPasswordActivity>(
      PinPasswordActivity.createPinPasswordActivityIntent(
        context = context,
        adminPin = adminPin,
        profileId = userId
      )
    ).use {
      onView(withId(R.id.forgot_pin)).perform(click())
      testCoroutineDispatchers.runCurrent()
      onView(
        allOf(
          withId(R.id.admin_settings_input_pin_edit_text),
          isDescendantOfA(withId(R.id.admin_settings_input_pin))
        )
      ).inRoot(isDialog())
        .perform(editTextInputAction.appendText("1234"), closeSoftKeyboard())

      onView(withText(context.getString(R.string.admin_settings_submit)))
        .inRoot(isDialog())
        .perform(click())
      onView(withId(R.id.admin_settings_input_pin))
        .check(matches(hasErrorText(R.string.admin_settings_incorrect)))
      onView(
        allOf(
          withId(R.id.admin_settings_input_pin_edit_text),
          isDescendantOfA(withId(R.id.admin_settings_input_pin))
        )
      ).perform(
        editTextInputAction.appendText("5"),
        closeSoftKeyboard()
      )
      onView(withId(R.id.admin_settings_input_pin))
        .check(matches(hasNoErrorText()))
    }
  }

  @Test
  fun testPinPassword_withUser_forgot_inputAdminPinAndShortPin_pinLengthError() {
    ActivityScenario.launch<PinPasswordActivity>(
      PinPasswordActivity.createPinPasswordActivityIntent(
        context = context,
        adminPin = adminPin,
        profileId = userId
      )
    ).use {
      onView(withId(R.id.input_pin))
        .perform(editTextInputAction.appendText(""), closeSoftKeyboard())
      onView(withId(R.id.forgot_pin)).perform(click())
      testCoroutineDispatchers.runCurrent()
      onView(
        allOf(
          withId(R.id.admin_settings_input_pin_edit_text),
          isDescendantOfA(withId(R.id.admin_settings_input_pin))
        )
      ).inRoot(isDialog())
        .perform(editTextInputAction.appendText("12345"), closeSoftKeyboard())

      onView(withText(context.getString(R.string.admin_settings_submit)))
        .inRoot(isDialog())
        .perform(click())

      onView(
        allOf(
          withId(R.id.reset_pin_input_pin_edit_text),
          isDescendantOfA(withId(R.id.reset_pin_input_pin))
        )
      ).inRoot(isDialog())
        .perform(editTextInputAction.appendText("32"), closeSoftKeyboard())

      onView(withText(context.getString(R.string.admin_settings_submit)))
        .inRoot(isDialog())
        .perform(click())
      onView(withId(R.id.reset_pin_input_pin))
        .check(matches(hasErrorText(R.string.add_profile_error_pin_length)))
      onView(
        allOf(
          withId(R.id.reset_pin_input_pin_edit_text),
          isDescendantOfA(withId(R.id.reset_pin_input_pin))
        )
      ).perform(
        editTextInputAction.appendText("1"),
        closeSoftKeyboard()
      )
      onView(withId(R.id.reset_pin_input_pin))
        .check(matches(hasNoErrorText()))
    }
  }

  @Test
  fun testPinPassword_withUser_forgot_inputAdminPinAndNewPinAndOldPin_wrongPinError() {
    ActivityScenario.launch<PinPasswordActivity>(
      PinPasswordActivity.createPinPasswordActivityIntent(
        context = context,
        adminPin = adminPin,
        profileId = userId
      )
    ).use {
      onView(withId(R.id.input_pin))
        .perform(editTextInputAction.appendText(""), closeSoftKeyboard())
      onView(withId(R.id.forgot_pin)).perform(click())
      onView(
        allOf(
          withId(R.id.admin_settings_input_pin_edit_text),
          isDescendantOfA(withId(R.id.admin_settings_input_pin))
        )
      ).inRoot(isDialog())
        .perform(editTextInputAction.appendText("12345"), closeSoftKeyboard())

      onView(withText(context.getString(R.string.admin_settings_submit)))
        .inRoot(isDialog())
        .perform(click())
      onView(
        allOf(
          withId(R.id.reset_pin_input_pin_edit_text),
          isDescendantOfA(withId(R.id.reset_pin_input_pin))
        )
      ).inRoot(isDialog())
        .perform(editTextInputAction.appendText("321"), closeSoftKeyboard())

      onView(withText(context.getString(R.string.admin_settings_submit)))
        .inRoot(isDialog())
        .perform(click())

      testCoroutineDispatchers.runCurrent()
      onView(withText(context.getString(R.string.pin_password_close)))
        .inRoot(isDialog())
        .perform(click())
      onView(withId(R.id.input_pin)).perform(
        editTextInputAction.appendText("123"),
        closeSoftKeyboard()
      )
      onView(withText(context.getString(R.string.pin_password_incorrect_pin)))
        .check(matches(isDisplayed()))
    }
  }

  @Test
  fun testPinPassword_withUser_forgot_inputAdminPinAndNewPin_opensHomeActivity() {
    ActivityScenario.launch<PinPasswordActivity>(
      PinPasswordActivity.createPinPasswordActivityIntent(
        context = context,
        adminPin = adminPin,
        profileId = userId
      )
    ).use {
      onView(withId(R.id.input_pin))
        .perform(editTextInputAction.appendText(""), closeSoftKeyboard())
      onView(withId(R.id.forgot_pin)).perform(click())
      onView(
        allOf(
          withId(R.id.admin_settings_input_pin_edit_text),
          isDescendantOfA(withId(R.id.admin_settings_input_pin))
        )
      ).inRoot(isDialog())
        .perform(editTextInputAction.appendText("12345"), closeSoftKeyboard())

      onView(withText(context.getString(R.string.admin_settings_submit)))
        .inRoot(isDialog())
        .perform(click())
      onView(
        allOf(
          withId(R.id.reset_pin_input_pin_edit_text),
          isDescendantOfA(withId(R.id.reset_pin_input_pin))
        )
      )
        .inRoot(isDialog())
        .perform(editTextInputAction.appendText("321"), closeSoftKeyboard())
      onView(withText(context.getString(R.string.admin_settings_submit)))
        .inRoot(isDialog())
        .perform(click())

      testCoroutineDispatchers.runCurrent()
      onView(withText(context.getString(R.string.pin_password_close)))
        .inRoot(isDialog())
        .perform(click())
      onView(withId(R.id.input_pin)).perform(editTextInputAction.appendText("321"))
      testCoroutineDispatchers.runCurrent()
      intended(hasComponent(HomeActivity::class.java.name))
    }
  }

  @Test
  fun testPinPassword_withUser_forgot_inputAdminPin_configChange_inputPinIsPresent() {
    ActivityScenario.launch<PinPasswordActivity>(
      PinPasswordActivity.createPinPasswordActivityIntent(
        context = context,
        adminPin = adminPin,
        profileId = userId
      )
    ).use {
      onView(withId(R.id.input_pin))
        .perform(editTextInputAction.appendText(""), closeSoftKeyboard())
      onView(withId(R.id.forgot_pin)).perform(click())
      onView(
        allOf(
          withId(R.id.admin_settings_input_pin_edit_text),
          isDescendantOfA(withId(R.id.admin_settings_input_pin))
        )
      ).inRoot(isDialog())
        .perform(editTextInputAction.appendText("1234"), closeSoftKeyboard())
      onView(isRoot()).perform(orientationLandscape())
      onView(
        allOf(
          withId(R.id.admin_settings_input_pin_edit_text),
          isDescendantOfA(withId(R.id.admin_settings_input_pin))
        )
      ).inRoot(isDialog())
        .check(matches(withText("1234")))
    }
  }

  @Test
  fun testPinPassword_withUser_forgot_inputAdminPin_submit_configChange_resetPinDisplayed() {
    ActivityScenario.launch<PinPasswordActivity>(
      PinPasswordActivity.createPinPasswordActivityIntent(
        context = context,
        adminPin = adminPin,
        profileId = userId
      )
    ).use {
      onView(withId(R.id.input_pin))
        .perform(editTextInputAction.appendText(""), closeSoftKeyboard())
      onView(withId(R.id.forgot_pin)).perform(click())
      onView(
        allOf(
          withId(R.id.admin_settings_input_pin_edit_text),
          isDescendantOfA(withId(R.id.admin_settings_input_pin))
        )
      ).inRoot(isDialog())
        .perform(editTextInputAction.appendText("12345"), closeSoftKeyboard())
      onView(withText(context.getString(R.string.admin_settings_submit)))
        .inRoot(isDialog())
        .perform(click())
      onView(isRoot()).perform(orientationLandscape())
      onView(withText(context.getString(R.string.reset_pin_enter)))
        .inRoot(isDialog())
        .check(matches(isDisplayed()))
    }
  }

  @Test
  fun testPinPassword_withUser_forgot_inputAdminPin_submit_inputNewPin_pinChanged() {
    ActivityScenario.launch<PinPasswordActivity>(
      PinPasswordActivity.createPinPasswordActivityIntent(
        context = context,
        adminPin = adminPin,
        profileId = userId
      )
    ).use {
      onView(withId(R.id.input_pin))
        .perform(editTextInputAction.appendText(""), closeSoftKeyboard())
      onView(withId(R.id.forgot_pin)).perform(click())
      onView(
        allOf(
          withId(R.id.admin_settings_input_pin_edit_text),
          isDescendantOfA(withId(R.id.admin_settings_input_pin))
        )
      ).inRoot(isDialog())
        .perform(editTextInputAction.appendText("12345"), closeSoftKeyboard())

      onView(withText(context.getString(R.string.admin_settings_submit)))
        .inRoot(isDialog())
        .perform(click())
      onView(
        allOf(
          withId(R.id.reset_pin_input_pin_edit_text),
          isDescendantOfA(withId(R.id.reset_pin_input_pin))
        )
      )
        .inRoot(isDialog())
        .perform(editTextInputAction.appendText("123"), closeSoftKeyboard())
      onView(isRoot()).perform(orientationLandscape())
      onView(withText(context.getString(R.string.admin_settings_submit)))
        .inRoot(isDialog())
        .perform(click())
      testCoroutineDispatchers.runCurrent()
      onView(
        withText(context.getString(R.string.pin_password_success))
      ).inRoot(isDialog()).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testPinPassword_withAdmin_forgot_configChange_opensAdminForgotDialog() {
    ActivityScenario.launch<PinPasswordActivity>(
      PinPasswordActivity.createPinPasswordActivityIntent(
        context = context,
        adminPin = adminPin,
        profileId = adminId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      closeSoftKeyboard()
      onView(withId(R.id.forgot_pin)).perform(click())
      onView(isRoot()).perform(orientationLandscape())
      onView(withText(context.getString(R.string.pin_password_forgot_message)))
        .inRoot(isDialog())
        .check(matches(isDisplayed()))
    }
  }

  @Test
  fun testPinPassword_withUser_forgot_inputWrongAdminPin_configChange_wrongAdminPinError() {
    ActivityScenario.launch<PinPasswordActivity>(
      PinPasswordActivity.createPinPasswordActivityIntent(
        context = context,
        adminPin = adminPin,
        profileId = userId
      )
    ).use {
      onView(withId(R.id.input_pin))
        .perform(editTextInputAction.appendText(""), closeSoftKeyboard())
      onView(withId(R.id.forgot_pin)).perform(click())
      onView(
        allOf(
          withId(R.id.admin_settings_input_pin_edit_text),
          isDescendantOfA(withId(R.id.admin_settings_input_pin))
        )
      ).inRoot(isDialog())
        .perform(editTextInputAction.appendText("1234"), closeSoftKeyboard())
      onView(withText(context.getString(R.string.admin_settings_submit)))
        .inRoot(isDialog())
        .perform(click())
      onView(withId(R.id.admin_settings_input_pin))
        .check(matches(hasErrorText(R.string.admin_settings_incorrect)))
      onView(
        allOf(
          withId(R.id.admin_settings_input_pin_edit_text),
          isDescendantOfA(withId(R.id.admin_settings_input_pin))
        )
      ).inRoot(isDialog())
        .perform(editTextInputAction.appendText("5"), closeSoftKeyboard())
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.admin_settings_input_pin))
        .check(matches(hasNoErrorText()))
    }
  }

  @Test
  fun testPinPassword_withUser_forgot_inputAdminPinAndIncorrectPin_errorIsDisplayed() {
    ActivityScenario.launch<PinPasswordActivity>(
      PinPasswordActivity.createPinPasswordActivityIntent(
        context = context,
        adminPin = adminPin,
        profileId = userId
      )
    ).use {
      onView(withId(R.id.input_pin))
        .perform(editTextInputAction.appendText(""), closeSoftKeyboard())
      onView(withId(R.id.forgot_pin)).perform(click())
      onView(
        allOf(
          withId(R.id.admin_settings_input_pin_edit_text),
          isDescendantOfA(withId(R.id.admin_settings_input_pin))
        )
      ).inRoot(isDialog())
        .perform(editTextInputAction.appendText("1234"), closeSoftKeyboard())
      onView(withText(context.getString(R.string.admin_settings_submit)))
        .inRoot(isDialog())
        .perform(click())
      onView(withId(R.id.admin_settings_input_pin))
        .check(matches(hasErrorText(R.string.admin_settings_incorrect)))
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.admin_settings_input_pin))
        .check(matches(hasErrorText(R.string.admin_settings_incorrect)))
    }
  }

  @Test
  fun testPinPassword_withUser_forgot_inputAdminPinAndInvalidPin_errorIsDisplayed() {
    ActivityScenario.launch<PinPasswordActivity>(
      PinPasswordActivity.createPinPasswordActivityIntent(
        context = context,
        adminPin = adminPin,
        profileId = userId
      )
    ).use {
      onView(withId(R.id.input_pin))
        .perform(editTextInputAction.appendText(""), closeSoftKeyboard())
      onView(withId(R.id.forgot_pin)).perform(click())
      onView(
        allOf(
          withId(R.id.admin_settings_input_pin_edit_text),
          isDescendantOfA(withId(R.id.admin_settings_input_pin))
        )
      ).inRoot(isDialog())
        .perform(editTextInputAction.appendText("12345"), closeSoftKeyboard())

      onView(withText(context.getString(R.string.admin_settings_submit)))
        .inRoot(isDialog())
        .perform(click())
      onView(
        allOf(
          withId(R.id.reset_pin_input_pin_edit_text),
          isDescendantOfA(withId(R.id.reset_pin_input_pin))
        )
      ).inRoot(isDialog())
        .perform(editTextInputAction.appendText("11"), closeSoftKeyboard())
      onView(withText(context.getString(R.string.admin_settings_submit)))
        .inRoot(isDialog())
        .perform(click())
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.reset_pin_input_pin))
        .check(matches(hasErrorText(R.string.add_profile_error_pin_length)))
    }
  }

  @Test
  fun testPinPassword_withAdmin_inputWrongPin_configChange_incorrectPinIsDisplayed() {
    ActivityScenario.launch<PinPasswordActivity>(
      PinPasswordActivity.createPinPasswordActivityIntent(
        context = context,
        adminPin = adminPin,
        profileId = adminId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      closeSoftKeyboard()
      onView(withId(R.id.input_pin)).perform(
        editTextInputAction.appendText("54321"),
        closeSoftKeyboard()
      )
      onView(isRoot()).perform(orientationLandscape())
      onView(withText(context.getString(R.string.pin_password_incorrect_pin))).check(
        matches(
          isDisplayed()
        )
      )
    }
  }

  @Test
  fun testPinPassword_withAdmin_checkShowHidePassword_defaultText() {
    ActivityScenario.launch<PinPasswordActivity>(
      PinPasswordActivity.createPinPasswordActivityIntent(
        context = context,
        adminPin = adminPin,
        profileId = adminId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      closeSoftKeyboard()
      onView(withText(context.getString(R.string.pin_password_show))).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testPinPassword_withAdmin_checkShowHidePassword_defaultImage() {
    ActivityScenario.launch<PinPasswordActivity>(
      PinPasswordActivity.createPinPasswordActivityIntent(
        context = context,
        adminPin = adminPin,
        profileId = adminId
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

  @Test
  fun testPinPassword_withAdmin_showHidePassword_textChangesToHide() {
    ActivityScenario.launch<PinPasswordActivity>(
      PinPasswordActivity.createPinPasswordActivityIntent(
        context = context,
        adminPin = adminPin,
        profileId = adminId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      closeSoftKeyboard()
      onView(withId(R.id.show_pin)).perform(click())
      onView(withText(context.getString(R.string.pin_password_hide))).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testPinPassword_withAdmin_showHidePassword_imageChangesToHide() {
    ActivityScenario.launch<PinPasswordActivity>(
      PinPasswordActivity.createPinPasswordActivityIntent(
        context = context,
        adminPin = adminPin,
        profileId = adminId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      closeSoftKeyboard()
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

  @Test
  fun testPinPassword_withAdmin_showHidePassword_configChange_hideViewIsShown() {
    ActivityScenario.launch<PinPasswordActivity>(
      PinPasswordActivity.createPinPasswordActivityIntent(
        context = context,
        adminPin = adminPin,
        profileId = adminId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      closeSoftKeyboard()
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

  @Test
  fun testPinPassword_checkInputType_showHidePassword_inputTypeIsSame() {
    ActivityScenario.launch<PinPasswordActivity>(
      PinPasswordActivity.createPinPasswordActivityIntent(
        context = context,
        adminPin = adminPin,
        profileId = adminId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()

      var inputType: Int = 0
      it.onActivity {
        inputType = it.findViewById<PinView>(R.id.input_pin).inputType
      }
      onView(withId(R.id.input_pin))
        .check(matches(withInputType(inputType)))
      onView(withId(R.id.show_pin)).perform(click())
      onView(withId(R.id.input_pin))
        .check(matches(withInputType(inputType)))
    }
  }

  private fun hasErrorText(@StringRes expectedErrorTextId: Int): Matcher<View> {
    return object : TypeSafeMatcher<View>() {
      override fun matchesSafely(view: View): Boolean {
        val expectedErrorText = context.resources.getString(expectedErrorTextId)
        return (view as TextInputLayout).error == expectedErrorText
      }

      override fun describeTo(description: Description) {
        description.appendText("TextInputLayout's error")
      }
    }
  }

  private fun hasNoErrorText(): Matcher<View> {
    return object : TypeSafeMatcher<View>() {
      override fun matchesSafely(view: View): Boolean {
        return (view as TextInputLayout).error.isNullOrEmpty()
      }

      override fun describeTo(description: Description) {
        description.appendText("")
      }
    }
  }

  // TODO(#59): Figure out a way to reuse modules instead of needing to re-declare them.
  // TODO(#1675): Add NetworkModule once data module is migrated off of Moshi.
  @Singleton
  @Component(
    modules = [
      RobolectricModule::class, TestDispatcherModule::class, ApplicationModule::class,
      LoggerModule::class, ContinueModule::class, FractionInputModule::class,
      ItemSelectionInputModule::class, MultipleChoiceInputModule::class,
      NumberWithUnitsRuleModule::class, NumericInputRuleModule::class, TextInputRuleModule::class,
      DragDropSortInputModule::class, ImageClickInputModule::class, InteractionsModule::class,
      GcsResourceModule::class, GlideImageLoaderModule::class, ImageParsingModule::class,
      HtmlParserEntityTypeModule::class, QuestionModule::class, TestLogReportingModule::class,
      TestAccessibilityModule::class, LogStorageModule::class, CachingTestModule::class,
      PrimeTopicAssetsControllerModule::class, ExpirationMetaDataRetrieverModule::class,
      ViewBindingShimModule::class, RatioInputModule::class,
      ApplicationStartupListenerModule::class, LogUploadWorkerModule::class,
      WorkManagerConfigurationModule::class, HintsAndSolutionConfigModule::class,
      FirebaseLogUploaderModule::class
    ]
  )
  interface TestApplicationComponent : ApplicationComponent {
    @Component.Builder
    interface Builder : ApplicationComponent.Builder

    fun inject(pinPasswordActivityTest: PinPasswordActivityTest)
  }

  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerPinPasswordActivityTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    fun inject(pinPasswordActivityTest: PinPasswordActivityTest) {
      component.inject(pinPasswordActivityTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }
}
