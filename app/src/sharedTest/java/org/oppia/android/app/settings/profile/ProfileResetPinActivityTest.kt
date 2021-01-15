package org.oppia.android.app.settings.profile

import android.app.Application
import android.content.Context
import android.view.View
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.clearText
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.pressImeActionButton
import androidx.test.espresso.action.ViewActions.scrollTo
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
import com.google.android.material.textfield.TextInputLayout
import dagger.Component
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.not
import org.hamcrest.Description
import org.hamcrest.Matcher
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
import org.oppia.android.app.player.state.hintsandsolution.HintsAndSolutionConfigModule
import org.oppia.android.app.shim.ViewBindingShimModule
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
  application = ProfileResetPinActivityTest.TestApplication::class,
  qualifiers = "port-xxhdpi"
)
class ProfileResetPinActivityTest {

  @Inject
  lateinit var context: Context

  @Inject
  lateinit var profileTestHelper: ProfileTestHelper

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @Inject
  lateinit var editTextInputAction: EditTextInputAction

  @Before
  fun setUp() {
    Intents.init()
    setUpTestApplicationComponent()
    testCoroutineDispatchers.registerIdlingResource()
    profileTestHelper.initializeProfiles()
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
  fun testProfileResetPinActivity_startActivityWithAdmin_inputPin_inputConfirmPin_clickSave_checkReturnsToProfileEditActivity() { // ktlint-disable max-line-length
    launch<ProfileResetPinActivity>(
      ProfileResetPinActivity.createProfileResetPinActivity(
        context = context,
        profileId = 0,
        isAdmin = true
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(
        allOf(
          withId(R.id.profile_reset_input_pin_edit_text),
          isDescendantOfA(withId(R.id.profile_reset_input_pin))
        )
      ).perform(
        editTextInputAction.appendText("12345"),
        closeSoftKeyboard()
      )
      onView(
        allOf(
          withId(R.id.profile_reset_input_confirm_pin_edit_text),
          isDescendantOfA(withId(R.id.profile_reset_input_confirm_pin))
        )
      ).perform(
        editTextInputAction.appendText("12345"),
        closeSoftKeyboard()
      )
      onView(withId(R.id.profile_reset_save_button)).perform(click())
      testCoroutineDispatchers.runCurrent()
      intended(hasComponent(ProfileEditActivity::class.java.name))
    }
  }

  @Test
  fun testResetPin_startWithAdmin_inputPinAndConfirmPin_pressImeAction_checkReturnsToProfileEdit() {
    launch<ProfileResetPinActivity>(
      ProfileResetPinActivity.createProfileResetPinActivity(
        context = context,
        profileId = 0,
        isAdmin = true
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(
        allOf(
          withId(R.id.profile_reset_input_pin_edit_text),
          isDescendantOfA(withId(R.id.profile_reset_input_pin))
        )
      ).perform(
        editTextInputAction.appendText("12345"),
        closeSoftKeyboard()
      )
      onView(
        allOf(
          withId(R.id.profile_reset_input_confirm_pin_edit_text),
          isDescendantOfA(withId(R.id.profile_reset_input_confirm_pin))
        )
      ).perform(
        editTextInputAction.appendText("12345"),
        pressImeActionButton()
      )
      testCoroutineDispatchers.runCurrent()
      intended(hasComponent(ProfileEditActivity::class.java.name))
    }
  }

  @Test
  fun testProfileResetPinActivity_startActivityWithAdmin_changeConfiguration_inputPin_inputConfirmPin_clickSave_checkReturnsToProfileEditActivity() { // ktlint-disable max-line-length
    launch<ProfileResetPinActivity>(
      ProfileResetPinActivity.createProfileResetPinActivity(
        context = context,
        profileId = 0,
        isAdmin = true
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(isRoot()).perform(orientationLandscape())
      onView(
        allOf(
          withId(R.id.profile_reset_input_pin_edit_text),
          isDescendantOfA(withId(R.id.profile_reset_input_pin))
        )
      ).perform(scrollTo())
        .perform(
          editTextInputAction.appendText("12345"),
          closeSoftKeyboard()
        )
      onView(
        allOf(
          withId(R.id.profile_reset_input_confirm_pin_edit_text),
          isDescendantOfA(withId(R.id.profile_reset_input_confirm_pin))
        )
      ).perform(
        scrollTo()
      ).perform(
        editTextInputAction.appendText("12345"),
        closeSoftKeyboard()
      )

      onView(withId(R.id.profile_reset_save_button)).perform(scrollTo()).perform(click())
      testCoroutineDispatchers.runCurrent()
      intended(hasComponent(ProfileEditActivity::class.java.name))
    }
  }

  @Test
  fun testProfileResetPinActivity_startActivityWithUser_inputPin_inputConfirmPin_clickSave_checkReturnsToProfileEditActivity() { // ktlint-disable max-line-length
    launch<ProfileResetPinActivity>(
      ProfileResetPinActivity.createProfileResetPinActivity(
        context = context,
        profileId = 1,
        isAdmin = false
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(
        allOf(
          withId(R.id.profile_reset_input_pin_edit_text),
          isDescendantOfA(withId(R.id.profile_reset_input_pin))
        )
      ).perform(
        editTextInputAction.appendText("123"),
        closeSoftKeyboard()
      )
      onView(
        allOf(
          withId(R.id.profile_reset_input_confirm_pin_edit_text),
          isDescendantOfA(withId(R.id.profile_reset_input_confirm_pin))
        )
      ).perform(
        editTextInputAction.appendText("123"),
        pressImeActionButton()
      )
      testCoroutineDispatchers.runCurrent()
      intended(hasComponent(ProfileEditActivity::class.java.name))
    }
  }

  @Test
  fun testProfileResetPinActivity_startActivityWithAdmin_inputShortPin_clickSave_checkPinLengthError() { // ktlint-disable max-line-length
    launch<ProfileResetPinActivity>(
      ProfileResetPinActivity.createProfileResetPinActivity(
        context = context,
        profileId = 0,
        isAdmin = true
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(
        allOf(
          withId(R.id.profile_reset_input_pin_edit_text),
          isDescendantOfA(withId(R.id.profile_reset_input_pin))
        )
      ).perform(
        editTextInputAction.appendText("1234"),
        closeSoftKeyboard()
      )
      onView(
        allOf(
          withId(R.id.profile_reset_input_confirm_pin_edit_text),
          isDescendantOfA(withId(R.id.profile_reset_input_confirm_pin))
        )
      ).perform(
        editTextInputAction.appendText("1234"),
        closeSoftKeyboard()
      )
      onView(withId(R.id.profile_reset_save_button)).perform(click())
      onView(withId(R.id.profile_reset_input_pin))
        .check(matches(hasErrorText(R.string.profile_reset_pin_error_admin_pin_length)))
    }
  }

  @Test
  fun testProfileResetPinActivity_startActivityWithAdmin_inputShortPin_clickSave_changeConfiguration_checkPinLengthError() { // ktlint-disable max-line-length
    launch<ProfileResetPinActivity>(
      ProfileResetPinActivity.createProfileResetPinActivity(
        context = context,
        profileId = 0,
        isAdmin = true
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(
        allOf(
          withId(R.id.profile_reset_input_pin_edit_text),
          isDescendantOfA(withId(R.id.profile_reset_input_pin))
        )
      ).perform(
        editTextInputAction.appendText("1234"),
        closeSoftKeyboard()
      )
      onView(
        allOf(
          withId(R.id.profile_reset_input_confirm_pin_edit_text),
          isDescendantOfA(withId(R.id.profile_reset_input_confirm_pin))
        )
      ).perform(
        editTextInputAction.appendText("1234"),
        closeSoftKeyboard()
      )
      onView(withId(R.id.profile_reset_save_button)).perform(scrollTo()).perform(click())
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.profile_reset_input_pin))
        .check(matches(hasErrorText(R.string.profile_reset_pin_error_admin_pin_length)))
    }
  }

  @Test
  fun testProfileResetPinActivity_startActivityWithAdmin_inputShortPin_clickSave_inputPin_checkErrorIsCleared() { // ktlint-disable max-line-length
    launch<ProfileResetPinActivity>(
      ProfileResetPinActivity.createProfileResetPinActivity(
        context = context,
        profileId = 0,
        isAdmin = true
      )
    ).use {
      onView(
        allOf(
          withId(R.id.profile_reset_input_pin_edit_text),
          isDescendantOfA(withId(R.id.profile_reset_input_pin))
        )
      ).perform(
        editTextInputAction.appendText("1234"),
        closeSoftKeyboard()
      )
      onView(withId(R.id.profile_reset_save_button)).perform(click())
      onView(
        allOf(
          withId(R.id.profile_reset_input_pin_edit_text),
          isDescendantOfA(withId(R.id.profile_reset_input_pin))
        )
      ).perform(
        editTextInputAction.appendText("5"),
        closeSoftKeyboard()
      )
      onView(withId(R.id.profile_reset_input_pin))
        .check(matches(hasNoErrorText()))
    }
  }

  @Test
  fun testProfileResetPinActivity_startActivityWithAdmin_inputShortPin_clickSave_inputPin_configurationChange_checkErrorIsCleared() { // ktlint-disable max-line-length
    launch<ProfileResetPinActivity>(
      ProfileResetPinActivity.createProfileResetPinActivity(
        context = context,
        profileId = 0,
        isAdmin = true
      )
    ).use {
      onView(
        allOf(
          withId(R.id.profile_reset_input_pin_edit_text),
          isDescendantOfA(withId(R.id.profile_reset_input_pin))
        )
      ).perform(
        editTextInputAction.appendText("1234"),
        closeSoftKeyboard()
      )
      onView(withId(R.id.profile_reset_save_button)).perform(click())
      onView(
        allOf(
          withId(R.id.profile_reset_input_pin_edit_text),
          isDescendantOfA(withId(R.id.profile_reset_input_pin))
        )
      ).perform(
        editTextInputAction.appendText("5"),
        closeSoftKeyboard()
      )
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.profile_reset_input_pin))
        .check(matches(hasNoErrorText()))
    }
  }

  @Test
  fun testProfileResetPinActivity_startActivityWithAdmin_inputWrongConfirmPin_clickSave_checkConfirmWrongError() { // ktlint-disable max-line-length
    launch<ProfileResetPinActivity>(
      ProfileResetPinActivity.createProfileResetPinActivity(
        context = context,
        profileId = 0,
        isAdmin = true
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(
        allOf(
          withId(R.id.profile_reset_input_pin_edit_text),
          isDescendantOfA(withId(R.id.profile_reset_input_pin))
        )
      ).perform(
        editTextInputAction.appendText("12345"),
        closeSoftKeyboard()
      )
      onView(
        allOf(
          withId(R.id.profile_reset_input_confirm_pin_edit_text),
          isDescendantOfA(withId(R.id.profile_reset_input_confirm_pin))
        )
      ).perform(
        editTextInputAction.appendText("1234"),
        closeSoftKeyboard()
      )
      onView(withId(R.id.profile_reset_save_button)).perform(click())
      onView(withId(R.id.profile_reset_input_confirm_pin))
        .check(matches(hasErrorText(R.string.add_profile_error_pin_confirm_wrong)))
    }
  }

  @Test
  fun testProfileResetPinActivity_startActivityWithAdmin_inputWrongConfirmPin_clickSave_changeConfiguration_checkConfirmWrongError() { // ktlint-disable max-line-length
    launch<ProfileResetPinActivity>(
      ProfileResetPinActivity.createProfileResetPinActivity(
        context = context,
        profileId = 0,
        isAdmin = true
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(
        allOf(
          withId(R.id.profile_reset_input_pin_edit_text),
          isDescendantOfA(withId(R.id.profile_reset_input_pin))
        )
      ).perform(
        editTextInputAction.appendText("12345"),
        closeSoftKeyboard()
      )
      onView(
        allOf(
          withId(R.id.profile_reset_input_confirm_pin_edit_text),
          isDescendantOfA(withId(R.id.profile_reset_input_confirm_pin))
        )
      ).perform(
        editTextInputAction.appendText("1234"),
        closeSoftKeyboard()
      )
      onView(withId(R.id.profile_reset_save_button)).perform(click())
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.profile_reset_input_confirm_pin))
        .perform(scrollTo())
        .check(matches(hasErrorText(R.string.add_profile_error_pin_confirm_wrong)))
    }
  }

  @Test
  fun testProfileResetPinActivity_inputPin_inputConfirmPin_changeConfiguration_inputPinExists_confirmInputPinExists_saveButtonIsClickable() { // ktlint-disable max-line-length
    launch<ProfileResetPinActivity>(
      ProfileResetPinActivity.createProfileResetPinActivity(
        context = context,
        profileId = 0,
        isAdmin = true
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(
        allOf(
          withId(R.id.profile_reset_input_pin_edit_text),
          isDescendantOfA(withId(R.id.profile_reset_input_pin))
        )
      ).perform(
        editTextInputAction.appendText("12345"),
        closeSoftKeyboard()
      )
      onView(
        allOf(
          withId(R.id.profile_reset_input_confirm_pin_edit_text),
          isDescendantOfA(withId(R.id.profile_reset_input_confirm_pin))
        )
      ).perform(
        editTextInputAction.appendText("12345"),
        closeSoftKeyboard()
      )
      onView(isRoot()).perform(orientationLandscape())
      onView(
        allOf(
          withId(R.id.profile_reset_input_pin_edit_text),
          isDescendantOfA(withId(R.id.profile_reset_input_pin))
        )
      ).perform(scrollTo())
        .check(matches(withText("12345")))
      onView(
        allOf(
          withId(R.id.profile_reset_input_confirm_pin_edit_text),
          isDescendantOfA(withId(R.id.profile_reset_input_confirm_pin))
        )
      ).perform(scrollTo()).check(matches(withText("12345")))
      onView(withId(R.id.profile_reset_save_button)).perform(scrollTo())
        .check(matches(isClickable()))
    }
  }

  @Test
  fun testProfileResetPinActivity_startActivityWithAdmin_inputWrongConfirmPin_clickSave_inputConfirmPin_checkErrorIsCleared() { // ktlint-disable max-line-length
    launch<ProfileResetPinActivity>(
      ProfileResetPinActivity.createProfileResetPinActivity(
        context = context,
        profileId = 0,
        isAdmin = true
      )
    ).use {
      onView(
        allOf(
          withId(R.id.profile_reset_input_pin_edit_text),
          isDescendantOfA(withId(R.id.profile_reset_input_pin))
        )
      ).perform(
        editTextInputAction.appendText("12345"),
        closeSoftKeyboard()
      )
      onView(
        allOf(
          withId(R.id.profile_reset_input_confirm_pin_edit_text),
          isDescendantOfA(withId(R.id.profile_reset_input_confirm_pin))
        )
      ).perform(
        editTextInputAction.appendText("1234"),
        closeSoftKeyboard()
      )
      onView(withId(R.id.profile_reset_save_button)).perform(click())
      onView(
        allOf(
          withId(R.id.profile_reset_input_confirm_pin_edit_text),
          isDescendantOfA(withId(R.id.profile_reset_input_confirm_pin))
        )
      ).perform(
        editTextInputAction.appendText("5"),
        closeSoftKeyboard()
      )
      onView(withId(R.id.profile_reset_input_confirm_pin))
        .check(matches(hasNoErrorText()))
    }
  }

  @Test
  fun testProfileResetPinActivity_startActivityWithUser_inputShortPin_clickSave_checkPinLengthError() { // ktlint-disable max-line-length
    launch<ProfileResetPinActivity>(
      ProfileResetPinActivity.createProfileResetPinActivity(
        context = context,
        profileId = 1,
        isAdmin = false
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(
        allOf(
          withId(R.id.profile_reset_input_pin_edit_text),
          isDescendantOfA(withId(R.id.profile_reset_input_pin))
        )
      ).perform(
        editTextInputAction.appendText("12"),
        closeSoftKeyboard()
      )
      onView(
        allOf(
          withId(R.id.profile_reset_input_confirm_pin_edit_text),
          isDescendantOfA(withId(R.id.profile_reset_input_confirm_pin))
        )
      ).perform(
        editTextInputAction.appendText("12"),
        closeSoftKeyboard()
      )
      onView(withId(R.id.profile_reset_save_button)).perform(click())
      onView(withId(R.id.profile_reset_input_pin))
        .check(matches(hasErrorText(R.string.profile_reset_pin_error_user_pin_length)))
    }
  }

  @Test
  fun testProfileResetPinActivity_startActivityWithUser_inputShortPin_clickSave_inputPin_checkErrorIsCleared() { // ktlint-disable max-line-length
    launch<ProfileResetPinActivity>(
      ProfileResetPinActivity.createProfileResetPinActivity(
        context = context,
        profileId = 1,
        isAdmin = false
      )
    ).use {
      onView(
        allOf(
          withId(R.id.profile_reset_input_pin_edit_text),
          isDescendantOfA(withId(R.id.profile_reset_input_pin))
        )
      ).perform(
        editTextInputAction.appendText("12"),
        closeSoftKeyboard()
      )
      onView(withId(R.id.profile_reset_save_button)).perform(click())
      onView(
        allOf(
          withId(R.id.profile_reset_input_pin_edit_text),
          isDescendantOfA(withId(R.id.profile_reset_input_pin))
        )
      ).perform(
        editTextInputAction.appendText("3"),
        closeSoftKeyboard()
      )
      onView(withId(R.id.profile_reset_input_pin))
        .check(matches(hasNoErrorText()))
    }
  }

  @Test
  fun testProfileResetPinActivity_startActivityWithUser_inputWrongConfirmPin_clickSave_checkConfirmWrongError() { // ktlint-disable max-line-length
    launch<ProfileResetPinActivity>(
      ProfileResetPinActivity.createProfileResetPinActivity(
        context = context,
        profileId = 1,
        isAdmin = false
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(
        allOf(
          withId(R.id.profile_reset_input_pin_edit_text),
          isDescendantOfA(withId(R.id.profile_reset_input_pin))
        )
      ).perform(
        editTextInputAction.appendText("123"),
        closeSoftKeyboard()
      )
      onView(
        allOf(
          withId(R.id.profile_reset_input_confirm_pin_edit_text),
          isDescendantOfA(withId(R.id.profile_reset_input_confirm_pin))
        )
      ).perform(
        editTextInputAction.appendText("12"),
        closeSoftKeyboard()
      )
      onView(withId(R.id.profile_reset_save_button)).perform(click())
      onView(withId(R.id.profile_reset_input_confirm_pin))
        .check(matches(hasErrorText(R.string.add_profile_error_pin_confirm_wrong)))
    }
  }

  @Test
  fun testProfileResetPinActivity_startActivityWithUser_inputWrongConfirmPin_clickSave_inputConfirmPin_checkErrorIsCleared() { // ktlint-disable max-line-length
    launch<ProfileResetPinActivity>(
      ProfileResetPinActivity.createProfileResetPinActivity(
        context = context,
        profileId = 1,
        isAdmin = false
      )
    ).use {
      onView(
        allOf(
          withId(R.id.profile_reset_input_pin_edit_text),
          isDescendantOfA(withId(R.id.profile_reset_input_pin))
        )
      ).perform(
        editTextInputAction.appendText("123"),
        closeSoftKeyboard()
      )
      onView(
        allOf(
          withId(R.id.profile_reset_input_confirm_pin_edit_text),
          isDescendantOfA(withId(R.id.profile_reset_input_confirm_pin))
        )
      ).perform(
        editTextInputAction.appendText("12"),
        closeSoftKeyboard()
      )
      onView(withId(R.id.profile_reset_save_button)).perform(click())
      onView(
        allOf(
          withId(R.id.profile_reset_input_confirm_pin_edit_text),
          isDescendantOfA(withId(R.id.profile_reset_input_confirm_pin))
        )
      ).perform(
        editTextInputAction.appendText("3"),
        closeSoftKeyboard()
      )
      onView(withId(R.id.profile_reset_input_confirm_pin))
        .check(matches(hasNoErrorText()))
    }
  }

  @Test
  fun testProfileResetPinActivity_default_saveButtonIsNotClickable() {
    launch<ProfileResetPinActivity>(
      ProfileResetPinActivity.createProfileResetPinActivity(
        context = context,
        profileId = 1,
        isAdmin = false
      )
    ).use {
      onView(withId(R.id.profile_reset_save_button)).check(matches(not(isClickable())))
    }
  }

  @Test
  fun testProfileResetPinActivity_default_changeConfiguration_saveButtonIsNotClickable() {
    launch<ProfileResetPinActivity>(
      ProfileResetPinActivity.createProfileResetPinActivity(
        context = context,
        profileId = 1,
        isAdmin = false
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
        context = context,
        profileId = 1,
        isAdmin = false
      )
    ).use {
      onView(
        allOf(
          withId(R.id.profile_reset_input_pin_edit_text),
          isDescendantOfA(withId(R.id.profile_reset_input_pin))
        )
      ).perform(
        editTextInputAction.appendText("123"),
        closeSoftKeyboard()
      )
      onView(withId(R.id.profile_reset_save_button)).check(matches(not(isClickable())))
    }
  }

  @Test
  fun testProfileResetPinActivity_inputPin_changeConfiguration_saveButtonIsNotClickable() {
    launch<ProfileResetPinActivity>(
      ProfileResetPinActivity.createProfileResetPinActivity(
        context = context,
        profileId = 1,
        isAdmin = false
      )
    ).use {
      onView(
        allOf(
          withId(R.id.profile_reset_input_pin_edit_text),
          isDescendantOfA(withId(R.id.profile_reset_input_pin))
        )
      ).perform(
        editTextInputAction.appendText("123"),
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
        context = context,
        profileId = 1,
        isAdmin = false
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(
        allOf(
          withId(R.id.profile_reset_input_pin_edit_text),
          isDescendantOfA(withId(R.id.profile_reset_input_pin))
        )
      ).perform(
        editTextInputAction.appendText("123"),
        closeSoftKeyboard()
      )
      onView(
        allOf(
          withId(R.id.profile_reset_input_confirm_pin_edit_text),
          isDescendantOfA(withId(R.id.profile_reset_input_confirm_pin))
        )
      ).perform(
        editTextInputAction.appendText("12"),
        closeSoftKeyboard()
      )
      onView(withId(R.id.profile_reset_save_button)).check(matches(isClickable()))
    }
  }

  @Test
  fun testProfileResetPinActivity_inputPin_inputConfirmPin_saveButtonIsClickable_clearInputPin_saveButtonIsNotClickable() { // ktlint-disable max-line-length
    launch<ProfileResetPinActivity>(
      ProfileResetPinActivity.createProfileResetPinActivity(
        context = context,
        profileId = 1,
        isAdmin = false
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(
        allOf(
          withId(R.id.profile_reset_input_pin_edit_text),
          isDescendantOfA(withId(R.id.profile_reset_input_pin))
        )
      ).perform(
        editTextInputAction.appendText("123"),
        closeSoftKeyboard()
      )
      onView(
        allOf(
          withId(R.id.profile_reset_input_confirm_pin_edit_text),
          isDescendantOfA(withId(R.id.profile_reset_input_confirm_pin))
        )
      ).perform(
        editTextInputAction.appendText("12"),
        closeSoftKeyboard()
      )
      onView(withId(R.id.profile_reset_save_button)).check(matches(isClickable()))
      onView(
        allOf(
          withId(R.id.profile_reset_input_pin_edit_text),
          isDescendantOfA(withId(R.id.profile_reset_input_pin))
        )
      ).perform(
        clearText(),
        closeSoftKeyboard()
      )
      onView(withId(R.id.profile_reset_save_button)).check(matches(not(isClickable())))
    }
  }

  @Test
  fun testProfileResetPinActivity_inputPin_inputConfirmPin_saveButtonIsClickable_clearConfirmInputPin_saveButtonIsNotClickable() { // ktlint-disable max-line-length
    launch<ProfileResetPinActivity>(
      ProfileResetPinActivity.createProfileResetPinActivity(
        context = context,
        profileId = 1,
        isAdmin = false
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(
        allOf(
          withId(R.id.profile_reset_input_pin_edit_text),
          isDescendantOfA(withId(R.id.profile_reset_input_pin))
        )
      ).perform(
        editTextInputAction.appendText("123"),
        closeSoftKeyboard()
      )
      onView(
        allOf(
          withId(R.id.profile_reset_input_confirm_pin_edit_text),
          isDescendantOfA(withId(R.id.profile_reset_input_confirm_pin))
        )
      ).perform(
        editTextInputAction.appendText("12"),
        closeSoftKeyboard()
      )
      onView(withId(R.id.profile_reset_save_button)).check(matches(isClickable()))
      onView(
        allOf(
          withId(R.id.profile_reset_input_confirm_pin_edit_text),
          isDescendantOfA(withId(R.id.profile_reset_input_confirm_pin))
        )
      ).perform(
        clearText(),
        closeSoftKeyboard()
      )
      onView(withId(R.id.profile_reset_save_button)).check(matches(not(isClickable())))
    }
  }

  @Test
  fun testProfileResetPinActivity_inputPin_inputConfirmPin_saveButtonIsClickable_clearConfirmInputPin_changeConfiguration_saveButtonIsNotClickable() { // ktlint-disable max-line-length
    launch<ProfileResetPinActivity>(
      ProfileResetPinActivity.createProfileResetPinActivity(
        context = context,
        profileId = 1,
        isAdmin = false
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(
        allOf(
          withId(R.id.profile_reset_input_pin_edit_text),
          isDescendantOfA(withId(R.id.profile_reset_input_pin))
        )
      ).perform(
        editTextInputAction.appendText("123"),
        closeSoftKeyboard()
      )
      onView(
        allOf(
          withId(R.id.profile_reset_input_confirm_pin_edit_text),
          isDescendantOfA(withId(R.id.profile_reset_input_confirm_pin))
        )
      ).perform(
        editTextInputAction.appendText("12"),
        closeSoftKeyboard()
      )
      onView(withId(R.id.profile_reset_save_button)).check(matches(isClickable()))
      onView(
        allOf(
          withId(R.id.profile_reset_input_confirm_pin_edit_text),
          isDescendantOfA(withId(R.id.profile_reset_input_confirm_pin))
        )
      ).perform(
        clearText(),
        closeSoftKeyboard()
      )
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.profile_reset_save_button)).perform(scrollTo())
        .check(matches(not(isClickable())))
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

    fun inject(profileResetPinActivityTest: ProfileResetPinActivityTest)
  }

  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerProfileResetPinActivityTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    fun inject(profileResetPinActivityTest: ProfileResetPinActivityTest) {
      component.inject(profileResetPinActivityTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }
}
