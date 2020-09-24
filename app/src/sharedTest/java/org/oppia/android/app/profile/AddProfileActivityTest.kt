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
import androidx.appcompat.app.AppCompatActivity
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.action.ViewActions.typeText
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
import com.google.firebase.FirebaseApp
import dagger.Component
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.not
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
<<<<<<< HEAD:app/src/sharedTest/java/org/oppia/android/app/profile/AddProfileActivityTest.kt
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
=======
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
>>>>>>> develop:app/src/sharedTest/java/org/oppia/app/profile/AddProfileActivityTest.kt
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
  fun testAddProfileActivity_inputName_clickCreate_checkOpensProfileChooserActivity() {
    launch(AddProfileActivity::class.java).use {
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_user_name_profile_input_view))
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
  fun testAddProfileActivity_changeConfiguration_inputName_clickCreate_checkOpensProfileChooserActivity() { // ktlint-disable max-line-length
    launch(AddProfileActivity::class.java).use {
      onView(isRoot()).perform(orientationLandscape())
      onView(allOf(withId(R.id.add_profile_activity_pin_check_box))).perform(scrollTo())
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_user_name_profile_input_view))
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
  fun testAddProfileActivity_clickOnCheckbox_createPin_checkIsDisplayed() {
    launch(AddProfileActivity::class.java).use {
      onView(allOf(withId(R.id.add_profile_activity_pin_check_box))).perform(click())
      onView(withId(R.id.add_profile_activity_pin_profile_input_view)).check(
        matches(
          isDisplayed()
        )
      )
    }
  }

  @Test
  fun testAddProfileActivity_changeConfiguration_clickOnCheckbox_createPin_checkIsDisplayed() {
    launch(AddProfileActivity::class.java).use {
      onView(isRoot()).perform(orientationLandscape())
      onView(allOf(withId(R.id.add_profile_activity_pin_check_box))).perform(scrollTo())
        .perform(click())
      onView(withId(R.id.add_profile_activity_pin_profile_input_view)).perform(scrollTo())
      onView(withId(R.id.add_profile_activity_pin_profile_input_view)).check(
        matches(
          isDisplayed()
        )
      )
    }
  }

  @Test
  fun testAddProfileActivity_createPin_checkNotVisible() {
    launch(AddProfileActivity::class.java).use {
      onView(withId(R.id.add_profile_activity_pin_profile_input_view)).check(
        matches(
          not(
            isDisplayed()
          )
        )
      )
    }
  }

  @Test
  fun testAddProfileActivity_changeConfiguration_createPin_checkNotVisible() {
    launch(AddProfileActivity::class.java).use {
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.add_profile_activity_pin_profile_input_view)).check(
        matches(
          not(
            isDisplayed()
          )
        )
      )
    }
  }

  @Test
  fun testAddProfileActivity_inputName_inputPin_clickCreate_checkOpensProfileActivity() {
    launch(AddProfileActivity::class.java).use {
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_user_name_profile_input_view))
        )
      ).perform(
        editTextInputAction.appendText("test"),
        closeSoftKeyboard()
      )
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.add_profile_activity_pin_check_box)).perform(click())
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_pin_profile_input_view))
        )
      ).perform(
        scrollTo(),
        editTextInputAction.appendText("123"),
        closeSoftKeyboard()
      )
      testCoroutineDispatchers.runCurrent()
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_confirm_pin_profile_input_view))
        )
      ).perform(scrollTo())
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_confirm_pin_profile_input_view))
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
  fun testAddProfileActivity_changeConfiguration_inputName_inputPin_clickCreate_checkOpensProfileActivity() { // ktlint-disable max-line-length
    launch(AddProfileActivity::class.java).use {
      onView(isRoot()).perform(orientationLandscape())
      onView(allOf(withId(R.id.add_profile_activity_pin_check_box))).perform(scrollTo())
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_user_name_profile_input_view))
        )
      ).perform(
        editTextInputAction.appendText("test"),
        closeSoftKeyboard()
      )
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.add_profile_activity_pin_check_box)).perform(scrollTo()).perform(click())
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_pin_profile_input_view))
        )
      ).perform(scrollTo())
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_pin_profile_input_view))
        )
      ).perform(
        editTextInputAction.appendText("123"),
        closeSoftKeyboard()
      )
      testCoroutineDispatchers.runCurrent()
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_confirm_pin_profile_input_view))
        )
      ).perform(scrollTo())
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_confirm_pin_profile_input_view))
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
  fun testAddProfileActivity_checkCreateIsNotClickable() {
    launch(AddProfileActivity::class.java).use {
      onView(withId(R.id.add_profile_activity_create_button)).perform(scrollTo())
      onView(withId(R.id.add_profile_activity_create_button)).check(matches(not(isClickable())))
    }
  }

  @Test
  fun testAddProfileActivity_changeConfiguration_checkCreateIsNotClickable() {
    launch(AddProfileActivity::class.java).use {
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.add_profile_activity_create_button)).perform(scrollTo())
      onView(withId(R.id.add_profile_activity_create_button)).check(matches(not(isClickable())))
    }
  }

  @Test
  fun testAddProfileActivity_inputName_checkCreateIsClickable() {
    profileTestHelper.initializeProfiles()
    launch(AddProfileActivity::class.java).use {
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_user_name_profile_input_view))
        )
      ).perform(
        typeText("Rajat"), closeSoftKeyboard()
      )
      onView(withId(R.id.add_profile_activity_create_button)).perform(scrollTo())
      onView(withId(R.id.add_profile_activity_create_button)).check(matches(isClickable()))
    }
  }

  @Test
  fun testAddProfileActivity_inputName_changeConfiguration_checkCreateIsClickable() {
    profileTestHelper.initializeProfiles()
    launch(AddProfileActivity::class.java).use {
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_user_name_profile_input_view))
        )
      ).perform(
        typeText("Rajat"), closeSoftKeyboard()
      )
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.add_profile_activity_create_button)).perform(scrollTo())
        .check(matches(isClickable()))
    }
  }

  @Test
  fun testAddProfileActivity_inputNotUniqueName_clickCreate_checkNameNotUniqueError() {
    profileTestHelper.initializeProfiles()
    launch(AddProfileActivity::class.java).use {
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_user_name_profile_input_view))
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
          withId(R.id.error_text),
          isDescendantOfA(withId(R.id.add_profile_activity_user_name_profile_input_view))
        )
      ).check(matches(withText(context.getString(R.string.add_profile_error_name_not_unique))))
    }
  }

  @Test
  fun testAddProfileActivity_changeConfiguration_inputNotUniqueName_clickCreate_checkNameNotUniqueError() { // ktlint-disable max-line-length
    profileTestHelper.initializeProfiles()
    launch(AddProfileActivity::class.java).use {
      onView(isRoot()).perform(orientationLandscape())
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_user_name_profile_input_view))
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
          withId(R.id.error_text),
          isDescendantOfA(withId(R.id.add_profile_activity_user_name_profile_input_view))
        )
      ).check(matches(withText(context.getString(R.string.add_profile_error_name_not_unique))))
    }
  }

  @Test
  fun testAddProfileActivity_inputNotUniqueName_clickCreate_inputName_checkErrorIsCleared() {
    profileTestHelper.initializeProfiles()
    launch(AddProfileActivity::class.java).use {
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_user_name_profile_input_view))
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
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_user_name_profile_input_view))
        )
      ).perform(
        editTextInputAction.appendText(" "), closeSoftKeyboard()
      )
      testCoroutineDispatchers.runCurrent()
      onView(
        allOf(
          withId(R.id.error_text),
          isDescendantOfA(withId(R.id.add_profile_activity_user_name_profile_input_view))
        )
      ).check(matches(withText("")))
    }
  }

  @Test
  fun testAddProfileActivity_changeConfiguration_inputNotUniqueName_clickCreate_inputName_checkErrorIsCleared() { // ktlint-disable max-line-length
    profileTestHelper.initializeProfiles()
    launch(AddProfileActivity::class.java).use {
      onView(isRoot()).perform(orientationLandscape())
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.add_profile_activity_pin_check_box)).perform(scrollTo())
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_user_name_profile_input_view))
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
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_user_name_profile_input_view))
        )
      ).perform(scrollTo())
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_user_name_profile_input_view))
        )
      ).perform(
        editTextInputAction.appendText(" "), closeSoftKeyboard()
      )
      testCoroutineDispatchers.runCurrent()
      onView(
        allOf(
          withId(R.id.error_text),
          isDescendantOfA(withId(R.id.add_profile_activity_user_name_profile_input_view))
        )
      ).check(matches(withText("")))
    }
  }

  @Test
  fun testAddProfileActivity_inputNameWithNumbers_clickCreate_checkNameOnlyLettersError() {
    launch(AddProfileActivity::class.java).use {
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_user_name_profile_input_view))
        )
      ).perform(
        typeText("123"), closeSoftKeyboard()
      )
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.add_profile_activity_create_button)).perform(scrollTo())
      onView(withId(R.id.add_profile_activity_create_button)).perform(click())
      testCoroutineDispatchers.runCurrent()
      onView(
        allOf(
          withId(R.id.error_text),
          isDescendantOfA(withId(R.id.add_profile_activity_user_name_profile_input_view))
        )
      ).check(matches(withText(context.getString(R.string.add_profile_error_name_only_letters))))
    }
  }

  @Test
  fun testAddProfileActivity_changeConfiguration_inputNameWithNumbers_clickCreate_checkNameOnlyLettersError() { // ktlint-disable max-line-length
    launch(AddProfileActivity::class.java).use {
      onView(isRoot()).perform(orientationLandscape())
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.add_profile_activity_pin_check_box)).perform(scrollTo())
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_user_name_profile_input_view))
        )
      ).perform(
        typeText("123"), closeSoftKeyboard()
      )
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.add_profile_activity_create_button)).perform(scrollTo())
      onView(withId(R.id.add_profile_activity_create_button)).perform(click())
      testCoroutineDispatchers.runCurrent()
      onView(
        allOf(
          withId(R.id.error_text),
          isDescendantOfA(withId(R.id.add_profile_activity_user_name_profile_input_view))
        )
      ).check(matches(withText(context.getString(R.string.add_profile_error_name_only_letters))))
    }
  }

  @Test
  fun testAddProfileActivity_inputNameWithNumbers_clickCreate_inputName_checkErrorIsCleared() {
    launch(AddProfileActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_user_name_profile_input_view))
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
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_user_name_profile_input_view))
        )
      ).perform(
        editTextInputAction.appendText(" "), closeSoftKeyboard()
      )
      testCoroutineDispatchers.runCurrent()
      onView(
        allOf(
          withId(R.id.error_text),
          isDescendantOfA(withId(R.id.add_profile_activity_user_name_profile_input_view))
        )
      ).check(matches(withText("")))
    }
  }

  @Test
  fun testAddProfileActivity_changeConfiguration_inputNameWithNumbers_clickCreate_inputName_checkErrorIsCleared() { // ktlint-disable max-line-length
    launch(AddProfileActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      onView(isRoot()).perform(orientationLandscape())
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_user_name_profile_input_view))
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
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_user_name_profile_input_view))
        )
      ).perform(scrollTo())
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_user_name_profile_input_view))
        )
      ).perform(
        editTextInputAction.appendText(" "), closeSoftKeyboard()
      )
      testCoroutineDispatchers.runCurrent()
      onView(
        allOf(
          withId(R.id.error_text),
          isDescendantOfA(withId(R.id.add_profile_activity_user_name_profile_input_view))
        )
      ).check(matches(withText("")))
    }
  }

  @Test
  fun testAddProfileActivity_inputShortPin_clickCreate_checkPinLengthError() {
    launch(AddProfileActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_user_name_profile_input_view))
        )
      ).perform(
        editTextInputAction.appendText("test"), closeSoftKeyboard()
      )
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.add_profile_activity_pin_check_box)).perform(click())
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_pin_profile_input_view))
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
          withId(R.id.error_text),
          isDescendantOfA(withId(R.id.add_profile_activity_pin_profile_input_view))
        )
      ).check(matches(withText(context.getString(R.string.add_profile_error_pin_length))))
    }
  }

  @Test
  fun testAddProfileActivity_changeConfiguration_inputShortPin_clickCreate_checkPinLengthError() { // ktlint-disable max-line-length
    launch(AddProfileActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      onView(isRoot()).perform(orientationLandscape())
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_user_name_profile_input_view))
        )
      ).perform(scrollTo()).perform(
        editTextInputAction.appendText("test"), closeSoftKeyboard()
      )
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.add_profile_activity_pin_check_box)).perform(scrollTo())
      onView(withId(R.id.add_profile_activity_pin_check_box)).perform(click())
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_pin_profile_input_view))
        )
      ).perform(scrollTo())
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_pin_profile_input_view))
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
          withId(R.id.error_text),
          isDescendantOfA(withId(R.id.add_profile_activity_pin_profile_input_view))
        )
      ).check(matches(withText(context.getString(R.string.add_profile_error_pin_length))))
    }
  }

  @Test
  fun testAddProfileActivity_inputShortPin_clickCreate_inputPin_checkErrorIsCleared() {
    launch(AddProfileActivity::class.java).use {
      onView(withId(R.id.add_profile_activity_pin_check_box)).perform(click())
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_pin_profile_input_view))
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
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_pin_profile_input_view))
        )
      ).perform(scrollTo()).perform(
        editTextInputAction.appendText("3"), closeSoftKeyboard()
      )
      testCoroutineDispatchers.runCurrent()
      onView(
        allOf(
          withId(R.id.error_text),
          isDescendantOfA(withId(R.id.add_profile_activity_pin_profile_input_view))
        )
      ).check(
        matches(
          withText("")
        )
      )
    }
  }

  @Test
  fun testAddProfileActivity_changeConfiguration_inputShortPin_clickCreate_inputPin_checkErrorIsCleared() { // ktlint-disable max-line-length
    launch(AddProfileActivity::class.java).use {
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.add_profile_activity_pin_check_box)).perform(scrollTo())
      onView(withId(R.id.add_profile_activity_pin_check_box)).perform(click())
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_pin_profile_input_view))
        )
      ).perform(scrollTo()).perform(
        typeText("12"),
        closeSoftKeyboard()
      )
      onView(withId(R.id.add_profile_activity_create_button)).perform(scrollTo()).perform(click())
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_pin_profile_input_view))
        )
      ).perform(scrollTo())
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_pin_profile_input_view))
        )
      ).perform(
        typeText("3"),
        closeSoftKeyboard()
      )
      onView(
        allOf(
          withId(R.id.error_text),
          isDescendantOfA(withId(R.id.add_profile_activity_pin_profile_input_view))
        )
      ).check(
        matches(
          withText("")
        )
      )
    }
  }

  @Test
  fun testAddProfileActivity_inputWrongConfirmPin_checkConfirmWrongError() {
    launch(AddProfileActivity::class.java).use {
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_user_name_profile_input_view))
        )
      ).perform(
        editTextInputAction.appendText("test"), closeSoftKeyboard()
      )
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.add_profile_activity_pin_check_box)).perform(click())
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_pin_profile_input_view))
        )
      ).perform(
        scrollTo(),
        editTextInputAction.appendText("123"),
        closeSoftKeyboard()
      )
      testCoroutineDispatchers.runCurrent()
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_confirm_pin_profile_input_view))
        )
      ).perform(scrollTo())
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_confirm_pin_profile_input_view))
        )
      ).perform(editTextInputAction.appendText("12"))
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.add_profile_activity_create_button)).perform(scrollTo())
      onView(withId(R.id.add_profile_activity_create_button)).perform(click())
      onView(
        allOf(
          withId(R.id.error_text),
          isDescendantOfA(withId(R.id.add_profile_activity_confirm_pin_profile_input_view))
        )
      ).check(
        matches(
          withText(
            context.getString(R.string.add_profile_error_pin_confirm_wrong)
          )
        )
      )
    }
  }

  @Test
  fun testAddProfileActivity_changeConfiguration_inputWrongConfirmPin_checkConfirmWrongError() {
    launch(AddProfileActivity::class.java).use {
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.add_profile_activity_pin_check_box)).perform(scrollTo())
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_user_name_profile_input_view))
        )
      ).perform(scrollTo()).perform(
        editTextInputAction.appendText("test"), closeSoftKeyboard()
      )
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.add_profile_activity_pin_check_box)).perform(scrollTo())
      onView(withId(R.id.add_profile_activity_pin_check_box)).perform(click())
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_pin_profile_input_view))
        )
      ).perform(scrollTo())
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_pin_profile_input_view))
        )
      ).perform(
        editTextInputAction.appendText("123"),
        closeSoftKeyboard()
      )
      testCoroutineDispatchers.runCurrent()
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_confirm_pin_profile_input_view))
        )
      ).perform(scrollTo())
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_confirm_pin_profile_input_view))
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
          withId(R.id.error_text),
          isDescendantOfA(withId(R.id.add_profile_activity_confirm_pin_profile_input_view))
        )
      ).perform(scrollTo())
      onView(
        allOf(
          withId(R.id.error_text),
          isDescendantOfA(withId(R.id.add_profile_activity_confirm_pin_profile_input_view))
        )
      ).check(
        matches(
          withText(
            context.getString(R.string.add_profile_error_pin_confirm_wrong)
          )
        )
      )
    }
  }

  @Test
  fun testAddProfileActivity_inputWrongConfirmPin_inputConfirmPin_checkErrorIsCleared() {
    launch(AddProfileActivity::class.java).use {
      onView(withId(R.id.add_profile_activity_pin_check_box)).perform(scrollTo())
      onView(withId(R.id.add_profile_activity_pin_check_box)).perform(click())
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_pin_profile_input_view))
        )
      ).perform(scrollTo())
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_pin_profile_input_view))
        )
      ).perform(
        typeText("123"),
        closeSoftKeyboard()
      )
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_confirm_pin_profile_input_view))
        )
      ).perform(scrollTo())
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_confirm_pin_profile_input_view))
        )
      ).perform(
        typeText("12"),
        closeSoftKeyboard()
      )
      onView(withId(R.id.add_profile_activity_create_button)).perform(scrollTo())
      onView(withId(R.id.add_profile_activity_create_button)).perform(click())
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_confirm_pin_profile_input_view))
        )
      ).perform(scrollTo())
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_confirm_pin_profile_input_view))
        )
      ).perform(
        typeText("3"),
        closeSoftKeyboard()
      )
      onView(
        allOf(
          withId(R.id.error_text),
          isDescendantOfA(withId(R.id.add_profile_activity_confirm_pin_profile_input_view))
        )
      ).check(matches(withText("")))
    }
  }

  @Test
  fun testAddProfileActivity_changeConfiguration_inputWrongConfirmPin_inputConfirmPin_checkErrorIsCleared() { // ktlint-disable max-line-length
    launch(AddProfileActivity::class.java).use {
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.add_profile_activity_pin_check_box)).perform(scrollTo())
      onView(withId(R.id.add_profile_activity_pin_check_box)).perform(click())
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_pin_profile_input_view))
        )
      ).perform(scrollTo()).perform(
        typeText("123"),
        closeSoftKeyboard()
      )
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_confirm_pin_profile_input_view))
        )
      ).perform(scrollTo()).perform(
        typeText("12"),
        closeSoftKeyboard()
      )
      onView(withId(R.id.add_profile_activity_create_button)).perform(scrollTo())
      onView(withId(R.id.add_profile_activity_create_button)).perform(click())
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_confirm_pin_profile_input_view))
        )
      ).perform(scrollTo())
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_confirm_pin_profile_input_view))
        )
      ).perform(
        typeText("3"),
        closeSoftKeyboard()
      )
      onView(
        allOf(
          withId(R.id.error_text),
          isDescendantOfA(withId(R.id.add_profile_activity_confirm_pin_profile_input_view))
        )
      ).check(matches(withText("")))
    }
  }

  @Test
  fun testAddProfileActivity_inputPin_checkAllowDownloadNotClickable() {
    launch(AddProfileActivity::class.java).use {
      onView(withId(R.id.add_profile_activity_pin_check_box)).perform(click())
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_pin_profile_input_view))
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
  fun testAddProfileActivity_changeConfiguration_inputPin_checkAllowDownloadNotClickable() {
    launch(AddProfileActivity::class.java).use {
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.add_profile_activity_pin_check_box)).perform(scrollTo()).perform(click())
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_pin_profile_input_view))
        )
      ).perform(scrollTo()).perform(
        typeText("123"),
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
  fun testAddProfileActivity_inputPin_inputConfirmPin_checkAllowDownloadClickable() {
    launch(AddProfileActivity::class.java).use {
      onView(withId(R.id.add_profile_activity_pin_check_box)).perform(click())
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_pin_profile_input_view))
        )
      ).perform(
        scrollTo(),
        editTextInputAction.appendText("123"),
        closeSoftKeyboard()
      )
      testCoroutineDispatchers.runCurrent()
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_confirm_pin_profile_input_view))
        )
      ).perform(scrollTo())
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_confirm_pin_profile_input_view))
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
  fun testAddProfileActivity_changeConfiguration_inputPin_inputConfirmPin_checkAllowDownloadClickable() { // ktlint-disable max-line-length
    launch(AddProfileActivity::class.java).use {
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.add_profile_activity_pin_check_box)).perform(scrollTo())
      onView(withId(R.id.add_profile_activity_pin_check_box)).perform(click())
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_pin_profile_input_view))
        )
      ).perform(
        scrollTo(),
        editTextInputAction.appendText("123"),
        closeSoftKeyboard()
      )
      testCoroutineDispatchers.runCurrent()
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_confirm_pin_profile_input_view))
        )
      ).perform(scrollTo())
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_confirm_pin_profile_input_view))
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
  fun testAddProfileActivity_changeConfiguration_imageSelectAvatar_checkGalleryIntent() {
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
  fun testAddProfileActivity_changeConfiguration_imageSelectEdit_checkGalleryIntent() {
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
  fun testAddProfileActivity_inputName_changeConfiguration_checkNameIsDisplayed() {
    launch(AddProfileActivity::class.java).use {
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_user_name_profile_input_view))
        )
      ).perform(
        typeText("test"),
        closeSoftKeyboard()
      )
      onView(isRoot()).perform(orientationLandscape())
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_user_name_profile_input_view))
        )
      ).perform(scrollTo())
        .check(matches(withText("test")))
    }
  }

  @Test
  fun testAddProfileActivity_inputPin_changeConfiguration_checkPinIsDisplayed() {
    launch(AddProfileActivity::class.java).use {
      onView(withId(R.id.add_profile_activity_pin_check_box)).perform(click())
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_pin_profile_input_view))
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
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_pin_profile_input_view))
        )
      ).perform(scrollTo())
        .check(matches(withText("123")))
    }
  }

  @Test
  fun testAddProfileActivity_inputConfirmPin_changeConfiguration_checkConfirmPinIsDisplayed() {
    launch(AddProfileActivity::class.java).use {
      onView(withId(R.id.add_profile_activity_pin_check_box)).perform(scrollTo())
      onView(withId(R.id.add_profile_activity_pin_check_box)).perform(click())
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_confirm_pin_profile_input_view))
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
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_confirm_pin_profile_input_view))
        )
      ).perform(scrollTo())
        .check(matches(withText("123")))
    }
  }

  @Test
  fun testAddProfileActivity_inputName_inputPin_inputConfirmPin_changeConfiguration_checkName_checkPin_checkConfirmPin_IsDisplayed() { // ktlint-disable max-line-length
    launch(AddProfileActivity::class.java).use {
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_user_name_profile_input_view))
        )
      ).perform(
        editTextInputAction.appendText("test"),
        closeSoftKeyboard()
      )
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.add_profile_activity_pin_check_box)).perform(click())
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_pin_profile_input_view))
        )
      ).perform(
        scrollTo(),
        editTextInputAction.appendText("123"),
        closeSoftKeyboard()
      )
      testCoroutineDispatchers.runCurrent()
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_confirm_pin_profile_input_view))
        )
      ).perform(scrollTo())
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_confirm_pin_profile_input_view))
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
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_user_name_profile_input_view))
        )
      ).check(matches(withText("test")))
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_pin_profile_input_view))
        )
      ).perform(scrollTo())
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_pin_profile_input_view))
        )
      ).check(matches(withText("123")))
      onView(
        withId(R.id.add_profile_activity_confirm_pin_profile_input_view)
      ).perform(scrollTo())
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_confirm_pin_profile_input_view))
        )
      ).check(matches(withText("123")))
    }
  }

  @Test
  fun testAddProfileActivity_inputName_inputPin_inputConfirmPin_deselectPIN_clickCreate_checkOpensProfileActivity() { // ktlint-disable max-line-length
    launch(AddProfileActivity::class.java).use {
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_user_name_profile_input_view))
        )
      ).perform(
        editTextInputAction.appendText("test"),
        closeSoftKeyboard()
      )
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.add_profile_activity_pin_check_box)).perform(click())
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_pin_profile_input_view))
        )
      ).perform(
        scrollTo(),
        editTextInputAction.appendText("123"),
        closeSoftKeyboard()
      )
      testCoroutineDispatchers.runCurrent()
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_confirm_pin_profile_input_view))
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
  fun testAddProfileActivity_inputNotUniqueName_clickCreate_changeConfiguration_checkErrorMessageDisplayed() { // ktlint-disable max-line-length
    profileTestHelper.initializeProfiles()
    launch(AddProfileActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_user_name_profile_input_view))
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
      onView(
        allOf(
          withId(R.id.error_text),
          isDescendantOfA(withId(R.id.add_profile_activity_user_name_profile_input_view))
        )
      ).check(matches(withText(context.getString(R.string.add_profile_error_name_not_unique))))
    }
  }

  @Test
  fun testAddProfileActivity_selectCheckbox_changeConfiguration_checkboxIsSelected() {
    launch(AddProfileActivity::class.java).use {
      onView(withId(R.id.add_profile_activity_pin_check_box)).perform(scrollTo())
      onView(withId(R.id.add_profile_activity_pin_check_box)).perform(click())
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.add_profile_activity_pin_check_box)).perform(scrollTo())
      onView(withId(R.id.add_profile_activity_pin_check_box)).check(matches(isChecked()))
    }
  }

  @Test
  fun testAddProfileActivity_inputPin_inputConfirmPin_changeConfiguration_checkPin_checkConfirmPin_IsDisplayed() { // ktlint-disable max-line-length
    launch(AddProfileActivity::class.java).use {
      onView(withId(R.id.add_profile_activity_pin_check_box)).perform(click())
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_pin_profile_input_view))
        )
      ).perform(
        scrollTo(),
        editTextInputAction.appendText("123"),
        closeSoftKeyboard()
      )
      testCoroutineDispatchers.runCurrent()
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_confirm_pin_profile_input_view))
        )
      ).perform(scrollTo())
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_confirm_pin_profile_input_view))
        )
      ).perform(
        editTextInputAction.appendText("123"),
        closeSoftKeyboard()
      )
      testCoroutineDispatchers.runCurrent()
      onView(isRoot()).perform(orientationLandscape())
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_pin_profile_input_view))
        )
      ).perform(scrollTo())
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_pin_profile_input_view))
        )
      ).check(matches(withText("123")))
      onView(withId(R.id.add_profile_activity_confirm_pin_profile_input_view)).perform(scrollTo())
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_confirm_pin_profile_input_view))
        )
      ).check(matches(withText("123")))
    }
  }

  @Test
  fun testAddProfileActivity_inputPin_inputDifferentConfirmPin_clickCreate_changeConfiguration_checkErrorMessageDisplayed() { // ktlint-disable max-line-length
    launch(AddProfileActivity::class.java).use {
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_user_name_profile_input_view))
        )
      ).perform(
        editTextInputAction.appendText("test"),
        closeSoftKeyboard()
      )
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.add_profile_activity_pin_check_box)).perform(click())
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_pin_profile_input_view))
        )
      ).perform(
        scrollTo(),
        editTextInputAction.appendText("123"),
        closeSoftKeyboard()
      )
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_confirm_pin_profile_input_view))
        )
      ).perform(scrollTo())
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_confirm_pin_profile_input_view))
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
      onView(
        allOf(
          withId(R.id.error_text),
          isDescendantOfA(withId(R.id.add_profile_activity_confirm_pin_profile_input_view))
        )
      ).check(
        matches(
          withText(
            context.getString(R.string.add_profile_error_pin_confirm_wrong)
          )
        )
      )
    }
  }

  @Test
  fun testAddProfileActivity_inputPin_inputConfirmPin_turnOnDownloadAccessSwitch_changeConfiguration_checkDownloadAccessSwitchIsOn() { // ktlint-disable max-line-length
    launch(AddProfileActivity::class.java).use {
      onView(withId(R.id.add_profile_activity_pin_check_box)).perform(click())
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_pin_profile_input_view))
        )
      ).perform(
        scrollTo(),
        editTextInputAction.appendText("123"),
        closeSoftKeyboard()
      )
      testCoroutineDispatchers.runCurrent()
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_confirm_pin_profile_input_view))
        )
      ).perform(scrollTo())
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_confirm_pin_profile_input_view))
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
  fun testAddProfileActivity_clickInfo_checkInfoPopupIsDisplayed() {
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
  fun testAddProfileActivity_clickInfo_changeConfiguration_checkInfoPopupIsDisplayed() {
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
