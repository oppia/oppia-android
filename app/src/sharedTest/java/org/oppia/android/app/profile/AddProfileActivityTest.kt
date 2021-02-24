package org.oppia.android.app.profile

import android.app.Activity.RESULT_OK
import android.app.Application
import android.app.Instrumentation.ActivityResult
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.net.Uri
import android.provider.MediaStore
import android.view.View
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.intent.matcher.IntentMatchers.hasData
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.isChecked
import androidx.test.espresso.matcher.ViewMatchers.isClickable
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.material.textfield.TextInputLayout
import dagger.Component
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.not
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
import org.oppia.android.testing.time.FakeOppiaClockModule
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
  application = AddProfileActivityTest.TestApplication::class,
  qualifiers = "port-xxhdpi"
)
class AddProfileActivityTest {

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
  fun testAddProfileActivity_inputName_opensProfileChooserActivity() {
    launch(AddProfileActivity::class.java).use {
      onView(
        allOf(
          withId(R.id.add_profile_activity_user_name_edit_text),
          isDescendantOfA(withId(R.id.add_profile_activity_user_name))
        )
      ).perform(
        editTextInputAction.appendText("test"), closeSoftKeyboard()
      )
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.add_profile_activity_create_button)).perform(scrollTo())
      onView(withId(R.id.add_profile_activity_create_button)).perform(click())
      testCoroutineDispatchers.runCurrent()
      intended(hasComponent(ProfileChooserActivity::class.java.name))
    }
  }

  @Test
  fun testAddProfileActivity_configChange_inputName_opensProfileChooserActivity() {
    launch(AddProfileActivity::class.java).use {
      onView(isRoot()).perform(orientationLandscape())
      onView(allOf(withId(R.id.add_profile_activity_pin_check_box))).perform(scrollTo())
      onView(
        allOf(
          withId(R.id.add_profile_activity_user_name_edit_text),
          isDescendantOfA(withId(R.id.add_profile_activity_user_name))
        )
      ).perform(scrollTo()).perform(
        editTextInputAction.appendText("test"), closeSoftKeyboard()
      )
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.add_profile_activity_create_button)).perform(scrollTo())
      onView(withId(R.id.add_profile_activity_create_button)).perform(click())
      testCoroutineDispatchers.runCurrent()
      intended(hasComponent(ProfileChooserActivity::class.java.name))
    }
  }

  @Test
  fun testAddProfileActivity_clickOnCheckbox_inputPinIsDisplayed() {
    launch(AddProfileActivity::class.java).use {
      onView(allOf(withId(R.id.add_profile_activity_pin_check_box))).perform(click())
      onView(withId(R.id.add_profile_activity_pin)).check(
        matches(
          isDisplayed()
        )
      )
    }
  }

  @Test
  fun testAddProfileActivity_configChange_clickOnCheckbox_inputPinIsDisplayed() {
    launch(AddProfileActivity::class.java).use {
      onView(isRoot()).perform(orientationLandscape())
      onView(allOf(withId(R.id.add_profile_activity_pin_check_box))).perform(scrollTo())
        .perform(click())
      onView(withId(R.id.add_profile_activity_pin)).perform(scrollTo())
      onView(withId(R.id.add_profile_activity_pin)).check(
        matches(
          isDisplayed()
        )
      )
    }
  }

  @Test
  fun testAddProfileActivity_inputPinIsNotDisplayed() {
    launch(AddProfileActivity::class.java).use {
      onView(withId(R.id.add_profile_activity_pin)).check(
        matches(
          not(
            isDisplayed()
          )
        )
      )
    }
  }

  @Test
  fun testAddProfileActivity_configChange_inputPinIsNotDisplayed() {
    launch(AddProfileActivity::class.java).use {
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.add_profile_activity_pin)).check(
        matches(
          not(
            isDisplayed()
          )
        )
      )
    }
  }

  @Test
  fun testAddProfileActivity_inputNameAndPin_opensProfileActivity() {
    launch(AddProfileActivity::class.java).use {
      onView(
        allOf(
          withId(R.id.add_profile_activity_user_name_edit_text),
          isDescendantOfA(withId(R.id.add_profile_activity_user_name))
        )
      ).perform(
        editTextInputAction.appendText("test"),
        closeSoftKeyboard()
      )
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.add_profile_activity_pin_check_box)).perform(click())
      onView(
        allOf(
          withId(R.id.add_profile_activity_pin_edit_text),
          isDescendantOfA(withId(R.id.add_profile_activity_pin))
        )
      ).perform(
        scrollTo(),
        editTextInputAction.appendText("123"),
        closeSoftKeyboard()
      )
      testCoroutineDispatchers.runCurrent()
      onView(
        allOf(
          withId(R.id.add_profile_activity_confirm_pin_edit_text),
          isDescendantOfA(withId(R.id.add_profile_activity_confirm_pin))
        )
      ).perform(scrollTo())
      onView(
        allOf(
          withId(R.id.add_profile_activity_confirm_pin_edit_text),
          isDescendantOfA(withId(R.id.add_profile_activity_confirm_pin))
        )
      ).perform(
        editTextInputAction.appendText("123"),
        closeSoftKeyboard()
      )
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.add_profile_activity_create_button)).perform(scrollTo())
      onView(withId(R.id.add_profile_activity_create_button)).perform(click())
      testCoroutineDispatchers.runCurrent()
      intended(hasComponent(ProfileChooserActivity::class.java.name))
    }
  }

  @Test
  fun testAddProfileActivity_configChange_inputNameAndPin_opensProfileActivity() {
    launch(AddProfileActivity::class.java).use {
      onView(isRoot()).perform(orientationLandscape())
      onView(allOf(withId(R.id.add_profile_activity_pin_check_box))).perform(scrollTo())
      onView(
        allOf(
          withId(R.id.add_profile_activity_user_name_edit_text),
          isDescendantOfA(withId(R.id.add_profile_activity_user_name))
        )
      ).perform(
        editTextInputAction.appendText("test"),
        closeSoftKeyboard()
      )
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.add_profile_activity_pin_check_box)).perform(scrollTo()).perform(click())
      onView(
        allOf(
          withId(R.id.add_profile_activity_pin_edit_text),
          isDescendantOfA(withId(R.id.add_profile_activity_pin))
        )
      ).perform(scrollTo())
      onView(
        allOf(
          withId(R.id.add_profile_activity_pin_edit_text),
          isDescendantOfA(withId(R.id.add_profile_activity_pin))
        )
      ).perform(
        editTextInputAction.appendText("123"),
        closeSoftKeyboard()
      )
      testCoroutineDispatchers.runCurrent()
      onView(
        allOf(
          withId(R.id.add_profile_activity_confirm_pin_edit_text),
          isDescendantOfA(withId(R.id.add_profile_activity_confirm_pin))
        )
      ).perform(scrollTo())
      onView(
        allOf(
          withId(R.id.add_profile_activity_confirm_pin_edit_text),
          isDescendantOfA(withId(R.id.add_profile_activity_confirm_pin))
        )
      ).perform(
        editTextInputAction.appendText("123"),
        closeSoftKeyboard()
      )
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.add_profile_activity_create_button)).perform(scrollTo())
      onView(withId(R.id.add_profile_activity_create_button)).perform(click())
      testCoroutineDispatchers.runCurrent()
      intended(hasComponent(ProfileChooserActivity::class.java.name))
    }
  }

  @Test
  fun testAddProfileActivity_createIsNotClickable() {
    launch(AddProfileActivity::class.java).use {
      onView(withId(R.id.add_profile_activity_create_button)).perform(scrollTo())
      onView(withId(R.id.add_profile_activity_create_button)).check(matches(not(isClickable())))
    }
  }

  @Test
  fun testAddProfileActivity_configChange_createIsNotClickable() {
    launch(AddProfileActivity::class.java).use {
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.add_profile_activity_create_button)).perform(scrollTo())
      onView(withId(R.id.add_profile_activity_create_button)).check(matches(not(isClickable())))
    }
  }

  @Test
  fun testAddProfileActivity_inputName_createIsClickable() {
    profileTestHelper.initializeProfiles()
    launch(AddProfileActivity::class.java).use {
      onView(
        allOf(
          withId(R.id.add_profile_activity_user_name_edit_text),
          isDescendantOfA(withId(R.id.add_profile_activity_user_name))
        )
      ).perform(
        editTextInputAction.appendText("Rajat"), closeSoftKeyboard()
      )
      onView(withId(R.id.add_profile_activity_create_button)).perform(scrollTo())
      onView(withId(R.id.add_profile_activity_create_button)).check(matches(isClickable()))
    }
  }

  @Test
  fun testAddProfileActivity_inputName_configChange_createIsClickable() {
    profileTestHelper.initializeProfiles()
    launch(AddProfileActivity::class.java).use {
      onView(
        allOf(
          withId(R.id.add_profile_activity_user_name_edit_text),
          isDescendantOfA(withId(R.id.add_profile_activity_user_name))
        )
      ).perform(
        editTextInputAction.appendText("Rajat"), closeSoftKeyboard()
      )
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.add_profile_activity_create_button)).perform(scrollTo())
        .check(matches(isClickable()))
    }
  }

  @Test
  fun testAddProfileActivity_inputNotUniqueName_create_nameNotUniqueError() {
    profileTestHelper.initializeProfiles()
    launch(AddProfileActivity::class.java).use {
      onView(
        allOf(
          withId(R.id.add_profile_activity_user_name_edit_text),
          isDescendantOfA(withId(R.id.add_profile_activity_user_name))
        )
      ).perform(
        editTextInputAction.appendText("Admin"), closeSoftKeyboard()
      )
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.add_profile_activity_create_button)).perform(scrollTo())
      onView(withId(R.id.add_profile_activity_create_button)).perform(click())
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.add_profile_activity_user_name))
        .check(matches(hasErrorText(R.string.add_profile_error_name_not_unique)))
    }
  }

  @Test
  fun testAddProfileActivity_configChange_inputNotUniqueName_create_nameNotUniqueError() {
    profileTestHelper.initializeProfiles()
    launch(AddProfileActivity::class.java).use {
      onView(isRoot()).perform(orientationLandscape())
      onView(
        allOf(
          withId(R.id.add_profile_activity_user_name_edit_text),
          isDescendantOfA(withId(R.id.add_profile_activity_user_name))
        )
      ).perform(scrollTo()).perform(
        editTextInputAction.appendText("Admin"), closeSoftKeyboard()
      )
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.add_profile_activity_create_button)).perform(scrollTo())
      onView(withId(R.id.add_profile_activity_create_button)).perform(click())
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.add_profile_activity_user_name))
        .check(matches(hasErrorText(R.string.add_profile_error_name_not_unique)))
    }
  }

  @Test
  fun testAddProfileActivity_inputNotUniqueName_create_inputName_errorIsCleared() {
    profileTestHelper.initializeProfiles()
    launch(AddProfileActivity::class.java).use {
      onView(
        allOf(
          withId(R.id.add_profile_activity_user_name_edit_text),
          isDescendantOfA(withId(R.id.add_profile_activity_user_name))
        )
      ).perform(
        editTextInputAction.appendText("Admin"), closeSoftKeyboard()
      )
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.add_profile_activity_create_button)).perform(scrollTo())
      onView(withId(R.id.add_profile_activity_create_button)).perform(click())
      testCoroutineDispatchers.runCurrent()
      onView(
        allOf(
          withId(R.id.add_profile_activity_user_name_edit_text),
          isDescendantOfA(withId(R.id.add_profile_activity_user_name))
        )
      ).perform(
        editTextInputAction.appendText(" "), closeSoftKeyboard()
      )
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.add_profile_activity_user_name))
        .check(matches(hasNoErrorText()))
    }
  }

  @Test
  fun testAddProfileActivity_configChange_inputNotUniqueName_create_inputName_errorIsCleared() {
    profileTestHelper.initializeProfiles()
    launch(AddProfileActivity::class.java).use {
      onView(isRoot()).perform(orientationLandscape())
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.add_profile_activity_pin_check_box)).perform(scrollTo())
      onView(
        allOf(
          withId(R.id.add_profile_activity_user_name_edit_text),
          isDescendantOfA(withId(R.id.add_profile_activity_user_name))
        )
      ).perform(scrollTo()).perform(
        editTextInputAction.appendText("Admin"), closeSoftKeyboard()
      )
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.add_profile_activity_create_button)).perform(scrollTo())
      onView(withId(R.id.add_profile_activity_create_button)).perform(click())
      testCoroutineDispatchers.runCurrent()
      onView(
        allOf(
          withId(R.id.add_profile_activity_user_name_edit_text),
          isDescendantOfA(withId(R.id.add_profile_activity_user_name))
        )
      ).perform(scrollTo())
      onView(
        allOf(
          withId(R.id.add_profile_activity_user_name_edit_text),
          isDescendantOfA(withId(R.id.add_profile_activity_user_name))
        )
      ).perform(
        editTextInputAction.appendText(" "), closeSoftKeyboard()
      )
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.add_profile_activity_user_name))
        .check(matches(hasNoErrorText()))
    }
  }

  @Test
  fun testAddProfileActivity_inputNameWithNumbers_create_nameOnlyLettersError() {
    launch(AddProfileActivity::class.java).use {
      onView(
        allOf(
          withId(R.id.add_profile_activity_user_name_edit_text),
          isDescendantOfA(withId(R.id.add_profile_activity_user_name))
        )
      ).perform(
        editTextInputAction.appendText("123"), closeSoftKeyboard()
      )
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.add_profile_activity_create_button)).perform(scrollTo())
      onView(withId(R.id.add_profile_activity_create_button)).perform(click())
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.add_profile_activity_user_name))
        .check(matches(hasErrorText(R.string.add_profile_error_name_only_letters)))
    }
  }

  @Test
  fun testAddProfileActivity_configChange_inputNameWithNumbers_create_nameOnlyLettersError() {
    launch(AddProfileActivity::class.java).use {
      onView(isRoot()).perform(orientationLandscape())
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.add_profile_activity_pin_check_box)).perform(scrollTo())
      onView(
        allOf(
          withId(R.id.add_profile_activity_user_name_edit_text),
          isDescendantOfA(withId(R.id.add_profile_activity_user_name))
        )
      ).perform(
        editTextInputAction.appendText("123"), closeSoftKeyboard()
      )
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.add_profile_activity_create_button)).perform(scrollTo())
      onView(withId(R.id.add_profile_activity_create_button)).perform(click())
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.add_profile_activity_user_name))
        .check(matches(hasErrorText(R.string.add_profile_error_name_only_letters)))
    }
  }

  @Test
  fun testAddProfileActivity_inputNameWithNumbers_create_inputName_errorIsCleared() {
    launch(AddProfileActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      onView(
        allOf(
          withId(R.id.add_profile_activity_user_name_edit_text),
          isDescendantOfA(withId(R.id.add_profile_activity_user_name))
        )
      ).perform(
        editTextInputAction.appendText("123"), closeSoftKeyboard()
      )
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.add_profile_activity_create_button)).perform(scrollTo())
      onView(withId(R.id.add_profile_activity_create_button)).perform(click())
      testCoroutineDispatchers.runCurrent()
      onView(
        allOf(
          withId(R.id.add_profile_activity_user_name_edit_text),
          isDescendantOfA(withId(R.id.add_profile_activity_user_name))
        )
      ).perform(
        editTextInputAction.appendText(" "), closeSoftKeyboard()
      )
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.add_profile_activity_user_name))
        .check(matches(hasNoErrorText()))
    }
  }

  @Test
  fun testAddProfileActivity_configChange_inputNameWithNumbers_create_inputName_errorIsCleared() {
    launch(AddProfileActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      onView(isRoot()).perform(orientationLandscape())
      onView(
        allOf(
          withId(R.id.add_profile_activity_user_name_edit_text),
          isDescendantOfA(withId(R.id.add_profile_activity_user_name))
        )
      ).perform(scrollTo()).perform(
        editTextInputAction.appendText("123"), closeSoftKeyboard()
      )
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.add_profile_activity_create_button)).perform(scrollTo())
      onView(withId(R.id.add_profile_activity_create_button)).perform(click())
      testCoroutineDispatchers.runCurrent()
      onView(
        allOf(
          withId(R.id.add_profile_activity_user_name_edit_text),
          isDescendantOfA(withId(R.id.add_profile_activity_user_name))
        )
      ).perform(scrollTo())
      onView(
        allOf(
          withId(R.id.add_profile_activity_user_name_edit_text),
          isDescendantOfA(withId(R.id.add_profile_activity_user_name))
        )
      ).perform(
        editTextInputAction.appendText(" "), closeSoftKeyboard()
      )
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.add_profile_activity_user_name))
        .check(matches(hasNoErrorText()))
    }
  }

  @Test
  fun testAddProfileActivity_inputShortPin_create_pinLengthError() {
    launch(AddProfileActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      onView(
        allOf(
          withId(R.id.add_profile_activity_user_name_edit_text),
          isDescendantOfA(withId(R.id.add_profile_activity_user_name))
        )
      ).perform(
        editTextInputAction.appendText("test"), closeSoftKeyboard()
      )
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.add_profile_activity_pin_check_box)).perform(click())
      onView(
        allOf(
          withId(R.id.add_profile_activity_pin_edit_text),
          isDescendantOfA(withId(R.id.add_profile_activity_pin))
        )
      ).perform(
        scrollTo(),
        editTextInputAction.appendText("12"),
        closeSoftKeyboard()
      )
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.add_profile_activity_create_button)).perform(scrollTo())
      onView(withId(R.id.add_profile_activity_create_button)).perform(click())
      onView(withId(R.id.add_profile_activity_pin))
        .check(matches(hasErrorText(R.string.add_profile_error_pin_length)))
    }
  }

  @Test
  fun testAddProfileActivity_configChange_inputShortPin_create_pinLengthError() {
    launch(AddProfileActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      onView(isRoot()).perform(orientationLandscape())
      onView(
        allOf(
          withId(R.id.add_profile_activity_user_name_edit_text),
          isDescendantOfA(withId(R.id.add_profile_activity_user_name))
        )
      ).perform(scrollTo()).perform(
        editTextInputAction.appendText("test"), closeSoftKeyboard()
      )
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.add_profile_activity_pin_check_box)).perform(scrollTo())
      onView(withId(R.id.add_profile_activity_pin_check_box)).perform(click())
      onView(
        allOf(
          withId(R.id.add_profile_activity_pin_edit_text),
          isDescendantOfA(withId(R.id.add_profile_activity_pin))
        )
      ).perform(scrollTo())
      onView(
        allOf(
          withId(R.id.add_profile_activity_pin_edit_text),
          isDescendantOfA(withId(R.id.add_profile_activity_pin))
        )
      ).perform(
        editTextInputAction.appendText("12"),
        closeSoftKeyboard()
      )
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.add_profile_activity_create_button)).perform(scrollTo())
      onView(withId(R.id.add_profile_activity_create_button)).perform(click())
      onView(withId(R.id.add_profile_activity_pin))
        .check(matches(hasErrorText(R.string.add_profile_error_pin_length)))
    }
  }

  @Test
  fun testAddProfileActivity_inputShortPin_create_inputPin_errorIsCleared() {
    launch(AddProfileActivity::class.java).use {
      onView(withId(R.id.add_profile_activity_pin_check_box)).perform(click())
      onView(
        allOf(
          withId(R.id.add_profile_activity_pin_edit_text),
          isDescendantOfA(withId(R.id.add_profile_activity_pin))
        )
      ).perform(
        scrollTo(),
        editTextInputAction.appendText("12"),
        closeSoftKeyboard()
      )
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.add_profile_activity_create_button)).perform(scrollTo())
      onView(withId(R.id.add_profile_activity_create_button)).perform(click())
      onView(
        allOf(
          withId(R.id.add_profile_activity_pin_edit_text),
          isDescendantOfA(withId(R.id.add_profile_activity_pin))
        )
      ).perform(scrollTo()).perform(
        editTextInputAction.appendText("3"), closeSoftKeyboard()
      )
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.add_profile_activity_pin))
        .check(matches(hasNoErrorText()))
    }
  }

  @Test
  fun testAddProfileActivity_configChange_inputShortPin_create_inputPin_errorIsCleared() {
    launch(AddProfileActivity::class.java).use {
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.add_profile_activity_pin_check_box)).perform(scrollTo())
      onView(withId(R.id.add_profile_activity_pin_check_box)).perform(click())
      onView(
        allOf(
          withId(R.id.add_profile_activity_pin_edit_text),
          isDescendantOfA(withId(R.id.add_profile_activity_pin))
        )
      ).perform(scrollTo()).perform(
        editTextInputAction.appendText("12"),
        closeSoftKeyboard()
      )
      onView(withId(R.id.add_profile_activity_create_button)).perform(scrollTo()).perform(click())
      onView(
        allOf(
          withId(R.id.add_profile_activity_pin_edit_text),
          isDescendantOfA(withId(R.id.add_profile_activity_pin))
        )
      ).perform(scrollTo())
      onView(
        allOf(
          withId(R.id.add_profile_activity_pin_edit_text),
          isDescendantOfA(withId(R.id.add_profile_activity_pin))
        )
      ).perform(
        editTextInputAction.appendText("3"),
        closeSoftKeyboard()
      )
      onView(withId(R.id.add_profile_activity_pin))
        .check(matches(hasNoErrorText()))
    }
  }

  @Test
  fun testAddProfileActivity_inputWrongConfirmPin_confirmWrongError() {
    launch(AddProfileActivity::class.java).use {
      onView(
        allOf(
          withId(R.id.add_profile_activity_user_name_edit_text),
          isDescendantOfA(withId(R.id.add_profile_activity_user_name))
        )
      ).perform(
        editTextInputAction.appendText("test"), closeSoftKeyboard()
      )
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.add_profile_activity_pin_check_box)).perform(click())
      onView(
        allOf(
          withId(R.id.add_profile_activity_pin_edit_text),
          isDescendantOfA(withId(R.id.add_profile_activity_pin))
        )
      ).perform(
        scrollTo(),
        editTextInputAction.appendText("123"),
        closeSoftKeyboard()
      )
      testCoroutineDispatchers.runCurrent()
      onView(
        allOf(
          withId(R.id.add_profile_activity_confirm_pin_edit_text),
          isDescendantOfA(withId(R.id.add_profile_activity_confirm_pin))
        )
      ).perform(scrollTo())
      onView(
        allOf(
          withId(R.id.add_profile_activity_confirm_pin_edit_text),
          isDescendantOfA(withId(R.id.add_profile_activity_confirm_pin))
        )
      ).perform(editTextInputAction.appendText("12"))
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.add_profile_activity_create_button)).perform(scrollTo())
      onView(withId(R.id.add_profile_activity_create_button)).perform(click())
      onView(withId(R.id.add_profile_activity_confirm_pin))
        .check(matches(hasErrorText(R.string.add_profile_error_pin_confirm_wrong)))
    }
  }

  @Test
  fun testAddProfileActivity_configChange_inputWrongConfirmPin_confirmWrongError() {
    launch(AddProfileActivity::class.java).use {
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.add_profile_activity_pin_check_box)).perform(scrollTo())
      onView(
        allOf(
          withId(R.id.add_profile_activity_user_name_edit_text),
          isDescendantOfA(withId(R.id.add_profile_activity_user_name))
        )
      ).perform(scrollTo()).perform(
        editTextInputAction.appendText("test"), closeSoftKeyboard()
      )
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.add_profile_activity_pin_check_box)).perform(scrollTo())
      onView(withId(R.id.add_profile_activity_pin_check_box)).perform(click())
      onView(
        allOf(
          withId(R.id.add_profile_activity_pin_edit_text),
          isDescendantOfA(withId(R.id.add_profile_activity_pin))
        )
      ).perform(scrollTo())
      onView(
        allOf(
          withId(R.id.add_profile_activity_pin_edit_text),
          isDescendantOfA(withId(R.id.add_profile_activity_pin))
        )
      ).perform(
        editTextInputAction.appendText("123"),
        closeSoftKeyboard()
      )
      testCoroutineDispatchers.runCurrent()
      onView(
        allOf(
          withId(R.id.add_profile_activity_confirm_pin_edit_text),
          isDescendantOfA(withId(R.id.add_profile_activity_confirm_pin))
        )
      ).perform(scrollTo())
      onView(
        allOf(
          withId(R.id.add_profile_activity_confirm_pin_edit_text),
          isDescendantOfA(withId(R.id.add_profile_activity_confirm_pin))
        )
      ).perform(
        editTextInputAction.appendText("12"),
        closeSoftKeyboard()
      )
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.add_profile_activity_create_button)).perform(scrollTo())
      onView(withId(R.id.add_profile_activity_create_button)).perform(click())
      onView(
        allOf(
          withId(R.id.add_profile_activity_confirm_pin_edit_text),
          isDescendantOfA(withId(R.id.add_profile_activity_confirm_pin))
        )
      ).perform(scrollTo())
      onView(withId(R.id.add_profile_activity_confirm_pin))
        .check(matches(hasErrorText(R.string.add_profile_error_pin_confirm_wrong)))
    }
  }

  @Test
  fun testAddProfileActivity_inputWrongConfirmPin_inputConfirmPin_errorIsCleared() {
    launch(AddProfileActivity::class.java).use {
      onView(withId(R.id.add_profile_activity_pin_check_box)).perform(scrollTo())
      onView(withId(R.id.add_profile_activity_pin_check_box)).perform(click())
      onView(
        allOf(
          withId(R.id.add_profile_activity_pin_edit_text),
          isDescendantOfA(withId(R.id.add_profile_activity_pin))
        )
      ).perform(scrollTo())
      onView(
        allOf(
          withId(R.id.add_profile_activity_pin_edit_text),
          isDescendantOfA(withId(R.id.add_profile_activity_pin))
        )
      ).perform(
        editTextInputAction.appendText("123"),
        closeSoftKeyboard()
      )
      onView(
        allOf(
          withId(R.id.add_profile_activity_confirm_pin_edit_text),
          isDescendantOfA(withId(R.id.add_profile_activity_confirm_pin))
        )
      ).perform(scrollTo())
      onView(
        allOf(
          withId(R.id.add_profile_activity_confirm_pin_edit_text),
          isDescendantOfA(withId(R.id.add_profile_activity_confirm_pin))
        )
      ).perform(
        editTextInputAction.appendText("12"),
        closeSoftKeyboard()
      )
      onView(withId(R.id.add_profile_activity_create_button)).perform(scrollTo())
      onView(withId(R.id.add_profile_activity_create_button)).perform(click())
      onView(
        allOf(
          withId(R.id.add_profile_activity_confirm_pin_edit_text),
          isDescendantOfA(withId(R.id.add_profile_activity_confirm_pin))
        )
      ).perform(scrollTo())
      onView(
        allOf(
          withId(R.id.add_profile_activity_confirm_pin_edit_text),
          isDescendantOfA(withId(R.id.add_profile_activity_confirm_pin))
        )
      ).perform(
        editTextInputAction.appendText("3"),
        closeSoftKeyboard()
      )
      onView(withId(R.id.add_profile_activity_confirm_pin))
        .check(matches(hasNoErrorText()))
    }
  }

  @Test
  fun testAddProfileActivity_configChange_inputWrongConfirmPinAndConfirmPin_errorIsCleared() {
    launch(AddProfileActivity::class.java).use {
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.add_profile_activity_pin_check_box)).perform(scrollTo())
      onView(withId(R.id.add_profile_activity_pin_check_box)).perform(click())
      onView(
        allOf(
          withId(R.id.add_profile_activity_pin_edit_text),
          isDescendantOfA(withId(R.id.add_profile_activity_pin))
        )
      ).perform(scrollTo()).perform(
        editTextInputAction.appendText("123"),
        closeSoftKeyboard()
      )
      onView(
        allOf(
          withId(R.id.add_profile_activity_confirm_pin_edit_text),
          isDescendantOfA(withId(R.id.add_profile_activity_confirm_pin))
        )
      ).perform(scrollTo()).perform(
        editTextInputAction.appendText("12"),
        closeSoftKeyboard()
      )
      onView(withId(R.id.add_profile_activity_create_button)).perform(scrollTo())
      onView(withId(R.id.add_profile_activity_create_button)).perform(click())
      onView(
        allOf(
          withId(R.id.add_profile_activity_confirm_pin_edit_text),
          isDescendantOfA(withId(R.id.add_profile_activity_confirm_pin))
        )
      ).perform(scrollTo())
      onView(
        allOf(
          withId(R.id.add_profile_activity_confirm_pin_edit_text),
          isDescendantOfA(withId(R.id.add_profile_activity_confirm_pin))
        )
      ).perform(
        editTextInputAction.appendText("3"),
        closeSoftKeyboard()
      )
      onView(withId(R.id.add_profile_activity_confirm_pin))
        .check(matches(hasNoErrorText()))
    }
  }

  @Test
  fun testAddProfileActivity_inputPin_checkAllowDownloadNotClickable() {
    launch(AddProfileActivity::class.java).use {
      onView(withId(R.id.add_profile_activity_pin_check_box)).perform(click())
      onView(
        allOf(
          withId(R.id.add_profile_activity_pin_edit_text),
          isDescendantOfA(withId(R.id.add_profile_activity_pin))
        )
      ).perform(
        scrollTo(),
        editTextInputAction.appendText("123"),
        closeSoftKeyboard()
      )
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.add_profile_activity_allow_download_switch))
        .check(
          matches(
            not(
              isClickable()
            )
          )
        )
    }
  }

  @Test
  fun testAddProfileActivity_configChange_inputPin_allowDownloadIsNotClickable() {
    launch(AddProfileActivity::class.java).use {
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.add_profile_activity_pin_check_box)).perform(scrollTo()).perform(click())
      onView(
        allOf(
          withId(R.id.add_profile_activity_pin_edit_text),
          isDescendantOfA(withId(R.id.add_profile_activity_pin))
        )
      ).perform(scrollTo()).perform(
        editTextInputAction.appendText("123"),
        closeSoftKeyboard()
      )
      onView(withId(R.id.add_profile_activity_allow_download_switch))
        .check(
          matches(
            not(
              isClickable()
            )
          )
        )
    }
  }

  @Test
  fun testAddProfileActivity_inputPin_inputConfirmPin_allowDownloadIsClickable() {
    launch(AddProfileActivity::class.java).use {
      onView(withId(R.id.add_profile_activity_pin_check_box)).perform(click())
      onView(
        allOf(
          withId(R.id.add_profile_activity_pin_edit_text),
          isDescendantOfA(withId(R.id.add_profile_activity_pin))
        )
      ).perform(
        scrollTo(),
        editTextInputAction.appendText("123"),
        closeSoftKeyboard()
      )
      testCoroutineDispatchers.runCurrent()
      onView(
        allOf(
          withId(R.id.add_profile_activity_confirm_pin_edit_text),
          isDescendantOfA(withId(R.id.add_profile_activity_confirm_pin))
        )
      ).perform(scrollTo())
      onView(
        allOf(
          withId(R.id.add_profile_activity_confirm_pin_edit_text),
          isDescendantOfA(withId(R.id.add_profile_activity_confirm_pin))
        )
      ).perform(
        editTextInputAction.appendText("123"),
        closeSoftKeyboard()
      )
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.add_profile_activity_allow_download_switch))
        .check(
          matches(
            isClickable()
          )
        )
    }
  }

  @Test
  fun testAddProfileActivity_configChange_inputPin_allowDownloadIsClickable() {
    launch(AddProfileActivity::class.java).use {
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.add_profile_activity_pin_check_box)).perform(scrollTo())
      onView(withId(R.id.add_profile_activity_pin_check_box)).perform(click())
      onView(
        allOf(
          withId(R.id.add_profile_activity_pin_edit_text),
          isDescendantOfA(withId(R.id.add_profile_activity_pin))
        )
      ).perform(
        scrollTo(),
        editTextInputAction.appendText("123"),
        closeSoftKeyboard()
      )
      testCoroutineDispatchers.runCurrent()
      onView(
        allOf(
          withId(R.id.add_profile_activity_confirm_pin_edit_text),
          isDescendantOfA(withId(R.id.add_profile_activity_confirm_pin))
        )
      ).perform(scrollTo())
      onView(
        allOf(
          withId(R.id.add_profile_activity_confirm_pin_edit_text),
          isDescendantOfA(withId(R.id.add_profile_activity_confirm_pin))
        )
      ).perform(
        editTextInputAction.appendText("123"),
        closeSoftKeyboard()
      )
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.add_profile_activity_allow_download_switch)).check(matches(isClickable()))
    }
  }

  @Test
  fun testAddProfileActivity_imageSelectAvatar_checkGalleryIntent() {
    val expectedIntent: Matcher<Intent> = allOf(
      hasAction(Intent.ACTION_PICK),
      hasData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
    )
    val activityResult = createGalleryPickActivityResultStub()
    intending(expectedIntent).respondWith(activityResult)
    launch(AddProfileActivity::class.java).use {
      onView(withId(R.id.add_profile_activity_user_image_view)).perform(click())
      intended(expectedIntent)
    }
  }

  @Test
  fun testAddProfileActivity_configChange_imageSelectAvatar_checkGalleryIntent() {
    val expectedIntent: Matcher<Intent> = allOf(
      hasAction(Intent.ACTION_PICK),
      hasData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
    )
    val activityResult = createGalleryPickActivityResultStub()
    intending(expectedIntent).respondWith(activityResult)
    launch(AddProfileActivity::class.java).use {
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.add_profile_activity_user_image_view)).perform(scrollTo())
      onView(withId(R.id.add_profile_activity_user_image_view)).perform(click())
      intended(expectedIntent)
    }
  }

  @Test
  fun testAddProfileActivity_imageSelectEdit_checkGalleryIntent() {
    val expectedIntent: Matcher<Intent> = allOf(
      hasAction(Intent.ACTION_PICK),
      hasData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
    )
    val activityResult = createGalleryPickActivityResultStub()
    intending(expectedIntent).respondWith(activityResult)
    launch(AddProfileActivity::class.java).use {
      onView(withId(R.id.add_profile_activity_edit_user_image_view)).perform(click())
      intended(expectedIntent)
    }
  }

  @Test
  fun testAddProfileActivity_configChange_imageSelectEdit_checkGalleryIntent() {
    val expectedIntent: Matcher<Intent> = allOf(
      hasAction(Intent.ACTION_PICK),
      hasData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
    )
    val activityResult = createGalleryPickActivityResultStub()
    intending(expectedIntent).respondWith(activityResult)
    launch(AddProfileActivity::class.java).use {
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.add_profile_activity_edit_user_image_view)).perform(scrollTo())
      onView(withId(R.id.add_profile_activity_edit_user_image_view)).perform(click())
      intended(expectedIntent)
    }
  }

  @Test
  fun testAddProfileActivity_inputName_configChange_nameIsDisplayed() {
    launch(AddProfileActivity::class.java).use {
      onView(
        allOf(
          withId(R.id.add_profile_activity_user_name_edit_text),
          isDescendantOfA(withId(R.id.add_profile_activity_user_name))
        )
      ).perform(
        editTextInputAction.appendText("test"),
        closeSoftKeyboard()
      )
      onView(isRoot()).perform(orientationLandscape())
      onView(
        allOf(
          withId(R.id.add_profile_activity_user_name_edit_text),
          isDescendantOfA(withId(R.id.add_profile_activity_user_name))
        )
      ).perform(scrollTo())
        .check(matches(withText("test")))
    }
  }

  @Test
  fun testAddProfileActivity_inputPin_configChange_pinIsDisplayed() {
    launch(AddProfileActivity::class.java).use {
      onView(withId(R.id.add_profile_activity_pin_check_box)).perform(click())
      onView(
        allOf(
          withId(R.id.add_profile_activity_pin_edit_text),
          isDescendantOfA(withId(R.id.add_profile_activity_pin))
        )
      ).perform(
        scrollTo(),
        editTextInputAction.appendText("123"),
        closeSoftKeyboard()
      )
      testCoroutineDispatchers.runCurrent()
      onView(isRoot()).perform(orientationLandscape())
      onView(
        allOf(
          withId(R.id.add_profile_activity_pin_edit_text),
          isDescendantOfA(withId(R.id.add_profile_activity_pin))
        )
      ).perform(scrollTo())
        .check(matches(withText("123")))
    }
  }

  @Test
  fun testAddProfileActivity_inputConfirmPin_configChange_confirmPinIsDisplayed() {
    launch(AddProfileActivity::class.java).use {
      onView(withId(R.id.add_profile_activity_pin_check_box)).perform(scrollTo())
      onView(withId(R.id.add_profile_activity_pin_check_box)).perform(click())
      onView(
        allOf(
          withId(R.id.add_profile_activity_confirm_pin_edit_text),
          isDescendantOfA(withId(R.id.add_profile_activity_confirm_pin))
        )
      ).perform(
        scrollTo(),
        editTextInputAction.appendText("123"),
        closeSoftKeyboard()
      )
      testCoroutineDispatchers.runCurrent()
      onView(isRoot()).perform(orientationLandscape())
      onView(
        allOf(
          withId(R.id.add_profile_activity_confirm_pin_edit_text),
          isDescendantOfA(withId(R.id.add_profile_activity_confirm_pin))
        )
      ).perform(scrollTo())
        .check(matches(withText("123")))
    }
  }

  @Test
  fun testAddProfileActivity_inputNameAndPin_configChange_confirmPinIsDisplayed() {
    launch(AddProfileActivity::class.java).use {
      onView(
        allOf(
          withId(R.id.add_profile_activity_user_name_edit_text),
          isDescendantOfA(withId(R.id.add_profile_activity_user_name))
        )
      ).perform(
        editTextInputAction.appendText("test"),
        closeSoftKeyboard()
      )
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.add_profile_activity_pin_check_box)).perform(click())
      onView(
        allOf(
          withId(R.id.add_profile_activity_pin_edit_text),
          isDescendantOfA(withId(R.id.add_profile_activity_pin))
        )
      ).perform(
        scrollTo(),
        editTextInputAction.appendText("123"),
        closeSoftKeyboard()
      )
      testCoroutineDispatchers.runCurrent()
      onView(
        allOf(
          withId(R.id.add_profile_activity_confirm_pin_edit_text),
          isDescendantOfA(withId(R.id.add_profile_activity_confirm_pin))
        )
      ).perform(scrollTo())
      onView(
        allOf(
          withId(R.id.add_profile_activity_confirm_pin_edit_text),
          isDescendantOfA(withId(R.id.add_profile_activity_confirm_pin))
        )
      ).perform(
        editTextInputAction.appendText("123"),
        closeSoftKeyboard()
      )
      testCoroutineDispatchers.runCurrent()
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.add_profile_activity_pin_check_box)).perform(scrollTo())
      onView(
        allOf(
          withId(R.id.add_profile_activity_user_name_edit_text),
          isDescendantOfA(withId(R.id.add_profile_activity_user_name))
        )
      ).check(matches(withText("test")))
      onView(
        allOf(
          withId(R.id.add_profile_activity_pin_edit_text),
          isDescendantOfA(withId(R.id.add_profile_activity_pin))
        )
      ).perform(scrollTo())
      onView(
        allOf(
          withId(R.id.add_profile_activity_pin_edit_text),
          isDescendantOfA(withId(R.id.add_profile_activity_pin))
        )
      ).check(matches(withText("123")))
      onView(
        withId(R.id.add_profile_activity_confirm_pin)
      ).perform(scrollTo())
      onView(
        allOf(
          withId(R.id.add_profile_activity_confirm_pin_edit_text),
          isDescendantOfA(withId(R.id.add_profile_activity_confirm_pin))
        )
      ).check(matches(withText("123")))
    }
  }

  @Test
  fun testAddProfileActivity_inputNameAndPin_deselectPIN_opensProfileActivity() {
    launch(AddProfileActivity::class.java).use {
      onView(
        allOf(
          withId(R.id.add_profile_activity_user_name_edit_text),
          isDescendantOfA(withId(R.id.add_profile_activity_user_name))
        )
      ).perform(
        editTextInputAction.appendText("test"),
        closeSoftKeyboard()
      )
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.add_profile_activity_pin_check_box)).perform(click())
      onView(
        allOf(
          withId(R.id.add_profile_activity_pin_edit_text),
          isDescendantOfA(withId(R.id.add_profile_activity_pin))
        )
      ).perform(
        scrollTo(),
        editTextInputAction.appendText("123"),
        closeSoftKeyboard()
      )
      testCoroutineDispatchers.runCurrent()
      onView(
        allOf(
          withId(R.id.add_profile_activity_confirm_pin_edit_text),
          isDescendantOfA(withId(R.id.add_profile_activity_confirm_pin))
        )
      ).perform(scrollTo())
        .perform(editTextInputAction.appendText("123"), closeSoftKeyboard())
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.add_profile_activity_pin_check_box)).perform(scrollTo())
      onView(withId(R.id.add_profile_activity_pin_check_box)).perform(click())
      onView(withId(R.id.add_profile_activity_create_button)).perform(scrollTo())
      onView(withId(R.id.add_profile_activity_create_button)).perform(click())
      testCoroutineDispatchers.runCurrent()
      intended(hasComponent(ProfileChooserActivity::class.java.name))
    }
  }

  @Test
  fun testAddProfileActivity_inputNotUniqueName_create_configChange_errorMessageIsDisplayed() {
    profileTestHelper.initializeProfiles()
    launch(AddProfileActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      onView(
        allOf(
          withId(R.id.add_profile_activity_user_name_edit_text),
          isDescendantOfA(withId(R.id.add_profile_activity_user_name))
        )
      ).perform(
        editTextInputAction.appendText("Admin"),
        closeSoftKeyboard()
      )
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.add_profile_activity_create_button)).perform(scrollTo())
      onView(withId(R.id.add_profile_activity_create_button)).perform(click())
      onView(isRoot()).perform(orientationLandscape())
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.add_profile_activity_user_name))
        .check(matches(hasErrorText(R.string.add_profile_error_name_not_unique)))
    }
  }

  @Test
  fun testAddProfileActivity_selectCheckbox_configChange_checkboxIsSelected() {
    launch(AddProfileActivity::class.java).use {
      onView(withId(R.id.add_profile_activity_pin_check_box)).perform(scrollTo())
      onView(withId(R.id.add_profile_activity_pin_check_box)).perform(click())
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.add_profile_activity_pin_check_box)).perform(scrollTo())
      onView(withId(R.id.add_profile_activity_pin_check_box)).check(matches(isChecked()))
    }
  }

  @Test
  fun testAddProfileActivity_inputPin_configChange_confirmPinIsDisplayed() {
    launch(AddProfileActivity::class.java).use {
      onView(withId(R.id.add_profile_activity_pin_check_box)).perform(click())
      onView(
        allOf(
          withId(R.id.add_profile_activity_pin_edit_text),
          isDescendantOfA(withId(R.id.add_profile_activity_pin))
        )
      ).perform(
        scrollTo(),
        editTextInputAction.appendText("123"),
        closeSoftKeyboard()
      )
      testCoroutineDispatchers.runCurrent()
      onView(
        allOf(
          withId(R.id.add_profile_activity_confirm_pin_edit_text),
          isDescendantOfA(withId(R.id.add_profile_activity_confirm_pin))
        )
      ).perform(scrollTo())
      onView(
        allOf(
          withId(R.id.add_profile_activity_confirm_pin_edit_text),
          isDescendantOfA(withId(R.id.add_profile_activity_confirm_pin))
        )
      ).perform(
        editTextInputAction.appendText("123"),
        closeSoftKeyboard()
      )
      testCoroutineDispatchers.runCurrent()
      onView(isRoot()).perform(orientationLandscape())
      onView(
        allOf(
          withId(R.id.add_profile_activity_pin_edit_text),
          isDescendantOfA(withId(R.id.add_profile_activity_pin))
        )
      ).perform(scrollTo())
      onView(
        allOf(
          withId(R.id.add_profile_activity_pin_edit_text),
          isDescendantOfA(withId(R.id.add_profile_activity_pin))
        )
      ).check(matches(withText("123")))
      onView(withId(R.id.add_profile_activity_confirm_pin)).perform(scrollTo())
      onView(
        allOf(
          withId(R.id.add_profile_activity_confirm_pin_edit_text),
          isDescendantOfA(withId(R.id.add_profile_activity_confirm_pin))
        )
      ).check(matches(withText("123")))
    }
  }

  @Test
  fun testAddProfileActivity_inputPin_create_configChange_errorMessageIsDisplayed() {
    launch(AddProfileActivity::class.java).use {
      onView(
        allOf(
          withId(R.id.add_profile_activity_user_name_edit_text),
          isDescendantOfA(withId(R.id.add_profile_activity_user_name))
        )
      ).perform(
        editTextInputAction.appendText("test"),
        closeSoftKeyboard()
      )
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.add_profile_activity_pin_check_box)).perform(click())
      onView(
        allOf(
          withId(R.id.add_profile_activity_pin_edit_text),
          isDescendantOfA(withId(R.id.add_profile_activity_pin))
        )
      ).perform(
        scrollTo(),
        editTextInputAction.appendText("123"),
        closeSoftKeyboard()
      )
      onView(
        allOf(
          withId(R.id.add_profile_activity_confirm_pin_edit_text),
          isDescendantOfA(withId(R.id.add_profile_activity_confirm_pin))
        )
      ).perform(scrollTo())
      onView(
        allOf(
          withId(R.id.add_profile_activity_confirm_pin_edit_text),
          isDescendantOfA(withId(R.id.add_profile_activity_confirm_pin))
        )
      ).perform(
        editTextInputAction.appendText("321 "),
        closeSoftKeyboard()
      )
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.add_profile_activity_create_button)).perform(scrollTo())
      onView(withId(R.id.add_profile_activity_create_button)).perform(click())
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.add_profile_activity_pin_check_box)).perform(scrollTo())
      onView(withId(R.id.add_profile_activity_confirm_pin))
        .check(matches(hasErrorText(R.string.add_profile_error_pin_confirm_wrong)))
    }
  }

  @Test
  fun testAddProfileActivity_inputPin_configChange_downloadAccessSwitchIsOn() {
    launch(AddProfileActivity::class.java).use {
      onView(withId(R.id.add_profile_activity_pin_check_box)).perform(click())
      onView(
        allOf(
          withId(R.id.add_profile_activity_pin_edit_text),
          isDescendantOfA(withId(R.id.add_profile_activity_pin))
        )
      ).perform(
        scrollTo(),
        editTextInputAction.appendText("123"),
        closeSoftKeyboard()
      )
      testCoroutineDispatchers.runCurrent()
      onView(
        allOf(
          withId(R.id.add_profile_activity_confirm_pin_edit_text),
          isDescendantOfA(withId(R.id.add_profile_activity_confirm_pin))
        )
      ).perform(scrollTo())
      onView(
        allOf(
          withId(R.id.add_profile_activity_confirm_pin_edit_text),
          isDescendantOfA(withId(R.id.add_profile_activity_confirm_pin))
        )
      ).perform(
        editTextInputAction.appendText("123"),
        closeSoftKeyboard()
      )
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.add_profile_activity_allow_download_switch)).perform(scrollTo())
      onView(withId(R.id.add_profile_activity_allow_download_switch)).perform(click())
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.add_profile_activity_allow_download_switch)).perform(scrollTo())
      onView(withId(R.id.add_profile_activity_allow_download_switch)).check(matches(isChecked()))
    }
  }

  @Test
  fun testAddProfileActivity_clickInfo_infoPopupIsDisplayed() {
    launch(AddProfileActivity::class.java).use {
      onView(withId(R.id.add_profile_activity_info_image_view)).perform(click())
      onView(withText(context.getString(R.string.add_profile_pin_info))).inRoot(isDialog())
        .check(
          matches(
            isDisplayed()
          )
        )
    }
  }

  @Test
  fun testAddProfileActivity_clickInfo_configChange_infoPopupIsDisplayed() {
    launch(AddProfileActivity::class.java).use {
      onView(withId(R.id.add_profile_activity_info_image_view)).perform(click())
      onView(isRoot()).perform(orientationLandscape())
      onView(withText(context.getString(R.string.add_profile_pin_info))).inRoot(isDialog())
        .check(
          matches(
            isDisplayed()
          )
        )
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

  private fun createGalleryPickActivityResultStub(): ActivityResult {
    val resources: Resources = context.resources
    val imageUri = Uri.parse(
      ContentResolver.SCHEME_ANDROID_RESOURCE + "://" +
        resources.getResourcePackageName(R.mipmap.ic_launcher) + '/' +
        resources.getResourceTypeName(R.mipmap.ic_launcher) + '/' +
        resources.getResourceEntryName(R.mipmap.ic_launcher)
    )
    val resultIntent = Intent()
    resultIntent.data = imageUri
    return ActivityResult(RESULT_OK, resultIntent)
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
      FirebaseLogUploaderModule::class, FakeOppiaClockModule::class
    ]
  )
  interface TestApplicationComponent : ApplicationComponent {
    @Component.Builder
    interface Builder : ApplicationComponent.Builder

    fun inject(addProfileActivityTest: AddProfileActivityTest)
  }

  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerAddProfileActivityTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    fun inject(addProfileActivityTest: AddProfileActivityTest) {
      component.inject(addProfileActivityTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }
}
