package org.oppia.app.settings.profile

import android.app.Application
import android.content.Context
import android.os.Build
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
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
import dagger.Component
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.not
import org.hamcrest.Matcher
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.R
import org.oppia.app.activity.ActivityComponent
import org.oppia.app.application.ActivityComponentFactory
import org.oppia.app.application.ApplicationComponent
import org.oppia.app.application.ApplicationInjector
import org.oppia.app.application.ApplicationInjectorProvider
import org.oppia.app.application.ApplicationModule
import org.oppia.app.application.ApplicationStartupListenerModule
import org.oppia.app.player.state.hintsandsolution.HintsAndSolutionConfigModule
import org.oppia.app.shim.ViewBindingShimModule
import org.oppia.app.utility.OrientationChangeAction.Companion.orientationLandscape
import org.oppia.domain.classify.InteractionsModule
import org.oppia.domain.classify.rules.continueinteraction.ContinueModule
import org.oppia.domain.classify.rules.dragAndDropSortInput.DragDropSortInputModule
import org.oppia.domain.classify.rules.fractioninput.FractionInputModule
import org.oppia.domain.classify.rules.imageClickInput.ImageClickInputModule
import org.oppia.domain.classify.rules.itemselectioninput.ItemSelectionInputModule
import org.oppia.domain.classify.rules.multiplechoiceinput.MultipleChoiceInputModule
import org.oppia.domain.classify.rules.numberwithunits.NumberWithUnitsRuleModule
import org.oppia.domain.classify.rules.numericinput.NumericInputRuleModule
import org.oppia.domain.classify.rules.ratioinput.RatioInputModule
import org.oppia.domain.classify.rules.textinput.TextInputRuleModule
import org.oppia.domain.onboarding.ExpirationMetaDataRetrieverModule
import org.oppia.domain.oppialogger.LogStorageModule
import org.oppia.domain.oppialogger.loguploader.LogUploadWorkerModule
import org.oppia.domain.oppialogger.loguploader.WorkManagerConfigurationModule
import org.oppia.domain.question.QuestionModule
import org.oppia.domain.topic.PrimeTopicAssetsControllerModule
import org.oppia.testing.EditTextInputAction
import org.oppia.testing.TestAccessibilityModule
import org.oppia.testing.TestCoroutineDispatchers
import org.oppia.testing.TestDispatcherModule
import org.oppia.testing.TestLogReportingModule
import org.oppia.testing.profile.ProfileTestHelper
import org.oppia.util.caching.testing.CachingTestModule
import org.oppia.util.gcsresource.GcsResourceModule
import org.oppia.util.logging.LoggerModule
import org.oppia.util.logging.firebase.FirebaseLogUploaderModule
import org.oppia.util.parser.GlideImageLoaderModule
import org.oppia.util.parser.HtmlParserEntityTypeModule
import org.oppia.util.parser.ImageParsingModule
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
    FirebaseApp.initializeApp(context)
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
        context,
        0,
        true
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin)))).perform(
        editTextInputAction.appendText("12345"),
        closeSoftKeyboard()
      )
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_confirm_pin)))).perform(
        editTextInputAction.appendText("12345"),
        closeSoftKeyboard()
      )
      onView(withId(R.id.profile_reset_save_button)).perform(click())
      testCoroutineDispatchers.runCurrent()
      intended(hasComponent(ProfileEditActivity::class.java.name))
    }
  }

  @Test
  fun testProfileResetPinActivity_startActivityWithAdmin_changeConfiguration_inputPin_inputConfirmPin_clickSave_checkReturnsToProfileEditActivity() { // ktlint-disable max-line-length
    launch<ProfileResetPinActivity>(
      ProfileResetPinActivity.createProfileResetPinActivity(
        context,
        0,
        true
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(isRoot()).perform(orientationLandscape())
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin)))).perform(scrollTo())
        .perform(
          editTextInputAction.appendText("12345"),
          closeSoftKeyboard()
        )
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_confirm_pin)))).perform(
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
        context,
        1,
        false
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin)))).perform(
        editTextInputAction.appendText("123"),
        closeSoftKeyboard()
      )
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_confirm_pin)))).perform(
        editTextInputAction.appendText("123"),
        closeSoftKeyboard()
      )
      onView(withId(R.id.profile_reset_save_button)).perform(click())
      testCoroutineDispatchers.runCurrent()
      intended(hasComponent(ProfileEditActivity::class.java.name))
    }
  }

  @Test
  fun testProfileResetPinActivity_startActivityWithAdmin_inputShortPin_clickSave_checkPinLengthError() { // ktlint-disable max-line-length
    launch<ProfileResetPinActivity>(
      ProfileResetPinActivity.createProfileResetPinActivity(
        context,
        0,
        true
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin)))).perform(
        editTextInputAction.appendText("1234"),
        closeSoftKeyboard()
      )
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_confirm_pin)))).perform(
        editTextInputAction.appendText("1234"),
        closeSoftKeyboard()
      )
      onView(withId(R.id.profile_reset_save_button)).perform(click())
      onView(
        allOf(
          withId(R.id.error_text),
          isDescendantOfA(withId(R.id.input_pin))
        )
      ).check(
        matches(withText(context.getString(R.string.profile_reset_pin_error_admin_pin_length)))
      )
    }
  }

  @Test
  fun testProfileResetPinActivity_startActivityWithAdmin_inputShortPin_clickSave_changeConfiguration_checkPinLengthError() { // ktlint-disable max-line-length
    launch<ProfileResetPinActivity>(
      ProfileResetPinActivity.createProfileResetPinActivity(
        context,
        0,
        true
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin)))).perform(
        editTextInputAction.appendText("1234"),
        closeSoftKeyboard()
      )
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_confirm_pin)))).perform(
        editTextInputAction.appendText("1234"),
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

  @Test
  fun testProfileResetPinActivity_startActivityWithAdmin_inputShortPin_clickSave_inputPin_checkErrorIsCleared() { // ktlint-disable max-line-length
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

  @Test
  fun testProfileResetPinActivity_startActivityWithAdmin_inputShortPin_clickSave_inputPin_configurationChange_checkErrorIsCleared() { // ktlint-disable max-line-length
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

  @Test
  fun testProfileResetPinActivity_startActivityWithAdmin_inputWrongConfirmPin_clickSave_checkConfirmWrongError() { // ktlint-disable max-line-length
    launch<ProfileResetPinActivity>(
      ProfileResetPinActivity.createProfileResetPinActivity(
        context,
        0,
        true
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin)))).perform(
        editTextInputAction.appendText("12345"),
        closeSoftKeyboard()
      )
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_confirm_pin)))).perform(
        editTextInputAction.appendText("1234"),
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

  @Test
  fun testProfileResetPinActivity_startActivityWithAdmin_inputWrongConfirmPin_clickSave_changeConfiguration_checkConfirmWrongError() { // ktlint-disable max-line-length
    launch<ProfileResetPinActivity>(
      ProfileResetPinActivity.createProfileResetPinActivity(
        context,
        0,
        true
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin)))).perform(
        editTextInputAction.appendText("12345"),
        closeSoftKeyboard()
      )
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_confirm_pin)))).perform(
        editTextInputAction.appendText("1234"),
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

  @Test
  fun testProfileResetPinActivity_inputPin_inputConfirmPin_changeConfiguration_inputPinExists_confirmInputPinExists_saveButtonIsClickable() { // ktlint-disable max-line-length
    launch<ProfileResetPinActivity>(
      ProfileResetPinActivity.createProfileResetPinActivity(
        context,
        0,
        true
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin)))).perform(
        editTextInputAction.appendText("12345"),
        closeSoftKeyboard()
      )
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_confirm_pin)))).perform(
        editTextInputAction.appendText("12345"),
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

  @Test
  fun testProfileResetPinActivity_startActivityWithAdmin_inputWrongConfirmPin_clickSave_inputConfirmPin_checkErrorIsCleared() { // ktlint-disable max-line-length
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

  @Test
  fun testProfileResetPinActivity_startActivityWithUser_inputShortPin_clickSave_checkPinLengthError() { // ktlint-disable max-line-length
    launch<ProfileResetPinActivity>(
      ProfileResetPinActivity.createProfileResetPinActivity(
        context,
        1,
        false
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin)))).perform(
        editTextInputAction.appendText("12"),
        closeSoftKeyboard()
      )
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_confirm_pin)))).perform(
        editTextInputAction.appendText("12"),
        closeSoftKeyboard()
      )
      onView(withId(R.id.profile_reset_save_button)).perform(click())
      onView(
        allOf(
          withId(R.id.error_text),
          isDescendantOfA(withId(R.id.input_pin))
        )
      ).check(
        matches(withText(context.getString(R.string.profile_reset_pin_error_user_pin_length)))
      )
    }
  }

  @Test
  fun testProfileResetPinActivity_startActivityWithUser_inputShortPin_clickSave_inputPin_checkErrorIsCleared() { // ktlint-disable max-line-length
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

  @Test
  fun testProfileResetPinActivity_startActivityWithUser_inputWrongConfirmPin_clickSave_checkConfirmWrongError() { // ktlint-disable max-line-length
    launch<ProfileResetPinActivity>(
      ProfileResetPinActivity.createProfileResetPinActivity(
        context,
        1,
        false
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin)))).perform(
        editTextInputAction.appendText("123"),
        closeSoftKeyboard()
      )
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_confirm_pin)))).perform(
        editTextInputAction.appendText("12"),
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

  @Test
  fun testProfileResetPinActivity_startActivityWithUser_inputWrongConfirmPin_clickSave_inputConfirmPin_checkErrorIsCleared() { // ktlint-disable max-line-length
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
      testCoroutineDispatchers.runCurrent()
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin)))).perform(
        editTextInputAction.appendText("123"),
        closeSoftKeyboard()
      )
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_confirm_pin)))).perform(
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
        context,
        1,
        false
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin)))).perform(
        editTextInputAction.appendText("123"),
        closeSoftKeyboard()
      )
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_confirm_pin)))).perform(
        editTextInputAction.appendText("12"),
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

  @Test
  fun testProfileResetPinActivity_inputPin_inputConfirmPin_saveButtonIsClickable_clearConfirmInputPin_saveButtonIsNotClickable() { // ktlint-disable max-line-length
    launch<ProfileResetPinActivity>(
      ProfileResetPinActivity.createProfileResetPinActivity(
        context,
        1,
        false
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin)))).perform(
        editTextInputAction.appendText("123"),
        closeSoftKeyboard()
      )
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_confirm_pin)))).perform(
        editTextInputAction.appendText("12"),
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

  @Test
  fun testProfileResetPinActivity_inputPin_inputConfirmPin_saveButtonIsClickable_clearConfirmInputPin_changeConfiguration_saveButtonIsNotClickable() { // ktlint-disable max-line-length
    launch<ProfileResetPinActivity>(
      ProfileResetPinActivity.createProfileResetPinActivity(
        context,
        1,
        false
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin)))).perform(
        editTextInputAction.appendText("123"),
        closeSoftKeyboard()
      )
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_confirm_pin)))).perform(
        editTextInputAction.appendText("12"),
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

  /**
   * Appends the specified text to a view. This is needed because Robolectric doesn't seem to
   * properly input digits for text views using 'android:digits'. See
   * https://github.com/robolectric/robolectric/issues/5110 for specifics.
   */
  private fun appendText(text: String): ViewAction {
    val typeTextViewAction = typeText(text)
    return object : ViewAction {
      override fun getDescription(): String = typeTextViewAction.description

      override fun getConstraints(): Matcher<View> = typeTextViewAction.constraints

      override fun perform(uiController: UiController?, view: View?) {
        // Appending text only works on Robolectric, whereas Espresso needs to use typeText().
        if (Build.FINGERPRINT.contains("robolectric", ignoreCase = true)) {
          (view as? EditText)?.append(text)
          testCoroutineDispatchers.runCurrent()
        } else {
          typeTextViewAction.perform(uiController, view)
        }
      }
    }
  }

  // TODO(#59): Figure out a way to reuse modules instead of needing to re-declare them.
  // TODO(#1675): Add NetworkModule once data module is migrated off of Moshi.
  @Singleton
  @Component(
    modules = [
      TestDispatcherModule::class, ApplicationModule::class,
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
  interface TestApplicationComponent : ApplicationComponent, ApplicationInjector {
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
