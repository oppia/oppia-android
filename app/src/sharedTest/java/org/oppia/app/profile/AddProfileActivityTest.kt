package org.oppia.app.profile

import android.app.Activity.RESULT_OK
import android.app.Application
import android.app.Instrumentation.ActivityResult
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.net.Uri
import android.provider.MediaStore
import androidx.test.core.app.ActivityScenario
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
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isChecked
import androidx.test.espresso.matcher.ViewMatchers.isClickable
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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.not
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.R
import org.oppia.app.utility.OrientationChangeAction
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
class AddProfileActivityTest {

  @Inject lateinit var context: Context
  @Inject lateinit var profileTestHelper: ProfileTestHelper

  @Before
  @ExperimentalCoroutinesApi
  fun setUp() {
    Intents.init()
    setUpTestApplicationComponent()
  }

  @After
  fun tearDown() {
    Intents.release()
  }

  private fun setUpTestApplicationComponent() {
    DaggerAddProfileActivityTest_TestApplicationComponent.builder()
      .setApplication(ApplicationProvider.getApplicationContext())
      .build()
      .inject(this)
  }

  @Test
  fun testAddProfileActivity_inputName_clickCreate_checkOpensProfileActivity() {
    ActivityScenario.launch(AddProfileActivity::class.java).use {
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_name)))).perform(
        typeText("test"), closeSoftKeyboard()
      )
      onView(withId(R.id.create_button)).perform(scrollTo()).perform(click())
      intended(hasComponent(ProfileActivity::class.java.name))
    }
  }

  @Test
  fun testAddProfileActivity_configurationChange_inputName_clickCreate_checkOpensProfileActivity() {
    ActivityScenario.launch(AddProfileActivity::class.java).use {
      onView(ViewMatchers.isRoot()).perform(OrientationChangeAction.orientationLandscape())
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_name)))).perform(
        typeText("test"), closeSoftKeyboard()
      )
      onView(withId(R.id.create_button)).perform(scrollTo()).perform(click())
      intended(hasComponent(ProfileActivity::class.java.name))
    }
  }

  @Test
  fun testAddProfileActivity_inputName_configurationChange_checkName(){
    ActivityScenario.launch(AddProfileActivity::class.java).use {
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_name)))).perform(
        typeText("text from portrait"), closeSoftKeyboard()
      )
      onView(ViewMatchers.isRoot()).perform(OrientationChangeAction.orientationLandscape())
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_name))))
        .check(matches(withText("text from portrait")))
    }
  }

  @Test
  fun testAddProfileActivity_createPin_checkOpensProfileActivity() {
    ActivityScenario.launch(AddProfileActivity::class.java).use {
      onView(allOf(withId(R.id.checkbox_pin))).perform(click())
      onView(withId(R.id.input_pin)).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testAddProfileActivity_configurationChange_createPin_checkOpensProfileActivity() {
    ActivityScenario.launch(AddProfileActivity::class.java).use {
      onView(ViewMatchers.isRoot()).perform(OrientationChangeAction.orientationLandscape())
      onView(allOf(withId(R.id.checkbox_pin))).perform(scrollTo(),click())
      onView(withId(R.id.input_pin)).perform(scrollTo()).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testAddProfileActivity_createPin_configurationChange_checkPin_checkOpensProfileActivity() {
    ActivityScenario.launch(AddProfileActivity::class.java).use {
      onView(allOf(withId(R.id.checkbox_pin))).perform(scrollTo(),click())
      onView(ViewMatchers.isRoot()).perform(OrientationChangeAction.orientationLandscape())
      onView(allOf(withId(R.id.checkbox_pin))).check(matches(isChecked()))
      onView(withId(R.id.input_pin)).perform(scrollTo()).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testAddProfileActivity_createPinNotVisible_checkOpensProfileActivity() {
    ActivityScenario.launch(AddProfileActivity::class.java).use {
      onView(withId(R.id.input_pin)).check(matches(not(isDisplayed())))
    }
  }

  @Test
  fun testAddProfileActivity_configurationChange_createPinNotVisible_checkOpensProfileActivity() {
    ActivityScenario.launch(AddProfileActivity::class.java).use {
      onView(ViewMatchers.isRoot()).perform(OrientationChangeAction.orientationLandscape())
      onView(withId(R.id.input_pin)).check(matches(not(isDisplayed())))
    }
  }

  @Test
  fun testAddProfileActivity_inputName_inputPin_clickCreate_checkOpensProfileActivity() {
    ActivityScenario.launch(AddProfileActivity::class.java).use {
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_name)))).perform(
        typeText("test"),
        closeSoftKeyboard()
      )
      onView(withId(R.id.checkbox_pin)).perform(click())
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin)))).perform(
        typeText("123"), closeSoftKeyboard()
      )
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_confirm_pin)))).perform(scrollTo())
        .perform(
          typeText("123"), closeSoftKeyboard()
        )
      onView(withId(R.id.create_button)).perform(scrollTo()).perform(click())
      intended(hasComponent(ProfileActivity::class.java.name))
    }
  }

  @Test
  fun testAddProfileActivity_configurationChange_inputName_inputPin_clickCreate_checkOpensProfileActivity() {
    ActivityScenario.launch(AddProfileActivity::class.java).use {
      onView(ViewMatchers.isRoot()).perform(OrientationChangeAction.orientationLandscape())
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_name)))).perform(
        typeText("test"),
        closeSoftKeyboard()
      )
      onView(withId(R.id.checkbox_pin)).perform(scrollTo(),click())
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin)))).perform(
        scrollTo(), typeText("123"), closeSoftKeyboard()
      )
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_confirm_pin)))).perform(scrollTo())
        .perform(
          typeText("123"), closeSoftKeyboard()
        )
      onView(withId(R.id.create_button)).perform(scrollTo()).perform(click())
      intended(hasComponent(ProfileActivity::class.java.name))
    }
  }
  
  @Test
  fun testAddProfileActivity_inputPin_configurationChange_checkPin(){
    ActivityScenario.launch(AddProfileActivity::class.java).use {
      onView(withId(R.id.checkbox_pin)).perform(scrollTo(),click())
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin)))).perform(
        scrollTo(), typeText("123"), closeSoftKeyboard()
      )
      onView(ViewMatchers.isRoot()).perform(OrientationChangeAction.orientationLandscape())
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin))))
        .check(matches(withText("123")))
    }
  }

  @Test
  fun testAddProfileActivity_inputConfirmPin_configurationChange_checkConfirmPin(){
    ActivityScenario.launch(AddProfileActivity::class.java).use {
      onView(withId(R.id.checkbox_pin)).perform(scrollTo(),click())
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_confirm_pin)))).perform(
        scrollTo(), typeText("123"), closeSoftKeyboard()
      )
      onView(ViewMatchers.isRoot()).perform(OrientationChangeAction.orientationLandscape())
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_confirm_pin))))
        .check(matches(withText("123")))
    }
  }

  @Test
  fun testAddProfileActivity_inputName_inputPin_inputConfirmPin_configurationChange_checkName_checkPin_checkConfirmPin(){
    ActivityScenario.launch(AddProfileActivity::class.java).use {
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_name)))).perform(
        typeText("name from portrait"), closeSoftKeyboard()
      )
      onView(withId(R.id.checkbox_pin)).perform(scrollTo(),click())
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin)))).perform(
        scrollTo(), typeText("123"), closeSoftKeyboard()
      )
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_confirm_pin)))).perform(
        scrollTo(), typeText("123"), closeSoftKeyboard()
      )
      onView(ViewMatchers.isRoot()).perform(OrientationChangeAction.orientationLandscape())
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_name))))
        .check(matches(withText("name from portrait")))
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin))))
        .check(matches(withText("123")))
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_confirm_pin))))
        .check(matches(withText("123")))
    }
  }

  @Test
  fun testAddProfileActivity_clickCreate_checkNameEmptyError() {
    ActivityScenario.launch(AddProfileActivity::class.java).use {
      onView(withId(R.id.create_button)).perform(scrollTo()).perform(click())
      onView(
        allOf(
          withId(R.id.error_text),
          isDescendantOfA(withId(R.id.input_name))
        )
      ).check(matches(withText(context.getString(R.string.add_profile_error_name_empty))))
    }
  }

  @Test
  fun testAddProfileActivity_configurationChange_clickCreate_checkNameEmptyError() {
    ActivityScenario.launch(AddProfileActivity::class.java).use {
      onView(ViewMatchers.isRoot()).perform(OrientationChangeAction.orientationLandscape())
      onView(withId(R.id.create_button)).perform(scrollTo()).perform(click())
      onView(
        allOf(
          withId(R.id.error_text),
          isDescendantOfA(withId(R.id.input_name))
        )
      ).check(matches(withText(context.getString(R.string.add_profile_error_name_empty))))
    }
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testAddProfileActivity_inputNotUniqueName_clickCreate_checkNameNotUniqueError() {
    profileTestHelper.initializeProfiles()
    ActivityScenario.launch(AddProfileActivity::class.java).use {
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_name)))).perform(
        typeText("Sean"), closeSoftKeyboard()
      )
      onView(withId(R.id.create_button)).perform(scrollTo()).perform(click())
      onView(
        allOf(
          withId(R.id.error_text),
          isDescendantOfA(withId(R.id.input_name))
        )
      ).check(matches(withText(context.getString(R.string.add_profile_error_name_not_unique))))
    }
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testAddProfileActivity_configurationChange_inputNotUniqueName_clickCreate_checkNameNotUniqueError() {
    profileTestHelper.initializeProfiles()
    ActivityScenario.launch(AddProfileActivity::class.java).use {
      onView(ViewMatchers.isRoot()).perform(OrientationChangeAction.orientationLandscape())
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_name)))).perform(
        typeText("Sean"), closeSoftKeyboard()
      )
      onView(withId(R.id.create_button)).perform(scrollTo()).perform(click())
      onView(
        allOf(
          withId(R.id.error_text),
          isDescendantOfA(withId(R.id.input_name))
        )
      ).check(matches(withText(context.getString(R.string.add_profile_error_name_not_unique))))
    }
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testAddProfileActivity_inputNotUniqueName_clickCreate_inputName_checkErrorIsCleared() {
    profileTestHelper.initializeProfiles()
    ActivityScenario.launch(AddProfileActivity::class.java).use {
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_name)))).perform(
        typeText("Sean"), closeSoftKeyboard()
      )
      onView(withId(R.id.create_button)).perform(scrollTo()).perform(click())
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_name)))).perform(
        typeText(" "), closeSoftKeyboard()
      )
      onView(allOf(withId(R.id.error_text), isDescendantOfA(withId(R.id.input_name)))).check(matches(withText("")))
    }
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testAddProfileActivity_configurationChange_inputNotUniqueName_clickCreate_inputName_checkErrorIsCleared() {
    profileTestHelper.initializeProfiles()
    ActivityScenario.launch(AddProfileActivity::class.java).use {
      onView(ViewMatchers.isRoot()).perform(OrientationChangeAction.orientationLandscape())
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_name)))).perform(
        typeText("Sean"), closeSoftKeyboard()
      )
      onView(withId(R.id.create_button)).perform(scrollTo()).perform(click())
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_name)))).perform(
        typeText(" "), closeSoftKeyboard()
      )
      onView(allOf(withId(R.id.error_text), isDescendantOfA(withId(R.id.input_name)))).check(matches(withText("")))
    }
  }

  @Test
  fun testAddProfileActivity_inputNameWithNumbers_clickCreate_checkNameOnlyLettersError() {
    ActivityScenario.launch(AddProfileActivity::class.java).use {
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_name)))).perform(
        typeText("123"), closeSoftKeyboard()
      )
      onView(withId(R.id.create_button)).perform(scrollTo()).perform(click())
      onView(
        allOf(
          withId(R.id.error_text),
          isDescendantOfA(withId(R.id.input_name))
        )
      ).check(matches(withText(context.getString(R.string.add_profile_error_name_only_letters))))
    }
  }

  @Test
  fun testAddProfileActivity_configurationChange_inputNameWithNumbers_clickCreate_checkNameOnlyLettersError() {
    ActivityScenario.launch(AddProfileActivity::class.java).use {
      onView(ViewMatchers.isRoot()).perform(OrientationChangeAction.orientationLandscape())
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_name)))).perform(
        typeText("123"), closeSoftKeyboard()
      )
      onView(withId(R.id.create_button)).perform(scrollTo()).perform(click())
      onView(
        allOf(
          withId(R.id.error_text),
          isDescendantOfA(withId(R.id.input_name))
        )
      ).check(matches(withText(context.getString(R.string.add_profile_error_name_only_letters))))
    }
  }

  @Test
  fun testAddProfileActivity_inputNameWithNumbers_clickCreate_inputName_checkErrorIsCleared() {
    ActivityScenario.launch(AddProfileActivity::class.java).use {
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_name)))).perform(
        typeText("123"), closeSoftKeyboard()
      )
      onView(withId(R.id.create_button)).perform(scrollTo()).perform(click())
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_name)))).perform(
        typeText(" "), closeSoftKeyboard()
      )
      onView(allOf(withId(R.id.error_text), isDescendantOfA(withId(R.id.input_name)))).check(matches(withText("")))
    }
  }

  @Test
  fun testAddProfileActivity_configurationChange_inputNameWithNumbers_clickCreate_inputName_checkErrorIsCleared() {
    ActivityScenario.launch(AddProfileActivity::class.java).use {
      onView(ViewMatchers.isRoot()).perform(OrientationChangeAction.orientationLandscape())
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_name)))).perform(
        typeText("123"), closeSoftKeyboard()
      )
      onView(withId(R.id.create_button)).perform(scrollTo()).perform(click())
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_name)))).perform(
        typeText(" "), closeSoftKeyboard()
      )
      onView(allOf(withId(R.id.error_text), isDescendantOfA(withId(R.id.input_name)))).check(matches(withText("")))
    }
  }

  @Test
  fun testAddProfileActivity_inputShortPin_clickCreate_checkPinLengthError() {
    ActivityScenario.launch(AddProfileActivity::class.java).use {
      onView(withId(R.id.checkbox_pin)).perform(click())
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin)))).perform(
        typeText("12"), closeSoftKeyboard()
      )
      onView(withId(R.id.create_button)).perform(scrollTo()).perform(click())
      onView(
        allOf(
          withId(R.id.error_text),
          isDescendantOfA(withId(R.id.input_pin))
        )
      ).check(matches(withText(context.getString(R.string.add_profile_error_pin_length))))
    }
  }

  @Test
  fun testAddProfileActivity_configurationChange_inputShortPin_clickCreate_checkPinLengthError() {
    ActivityScenario.launch(AddProfileActivity::class.java).use {
      onView(ViewMatchers.isRoot()).perform(OrientationChangeAction.orientationLandscape())
      onView(withId(R.id.checkbox_pin)).perform(scrollTo(),click())
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin)))).perform(
        scrollTo(), typeText("12"), closeSoftKeyboard()
      )
      onView(withId(R.id.create_button)).perform(scrollTo()).perform(click())
      onView(
        allOf(
          withId(R.id.error_text),
          isDescendantOfA(withId(R.id.input_pin))
        )
      ).check(matches(withText(context.getString(R.string.add_profile_error_pin_length))))
    }
  }

  @Test
  fun testAddProfileActivity_inputShortPin_clickCreate_inputPin_checkErrorIsCleared() {
    ActivityScenario.launch(AddProfileActivity::class.java).use {
      onView(withId(R.id.checkbox_pin)).perform(click())
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin)))).perform(
        typeText("12"), closeSoftKeyboard()
      )
      onView(withId(R.id.create_button)).perform(scrollTo()).perform(click())
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin)))).perform(
        typeText("3"), closeSoftKeyboard()
      )
      onView(allOf(withId(R.id.error_text), isDescendantOfA(withId(R.id.input_pin)))).check(matches(withText("")))
    }
  }

  @Test
  fun testAddProfileActivity_configurationChange_inputShortPin_clickCreate_inputPin_checkErrorIsCleared() {
    ActivityScenario.launch(AddProfileActivity::class.java).use {
      onView(ViewMatchers.isRoot()).perform(OrientationChangeAction.orientationLandscape())
      onView(withId(R.id.checkbox_pin)).perform(scrollTo(),click())
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin)))).perform(
        scrollTo(), typeText("12"), closeSoftKeyboard()
      )
      onView(withId(R.id.create_button)).perform(scrollTo()).perform(click())
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin)))).perform(
        scrollTo(), typeText("3"), closeSoftKeyboard()
      )
      onView(allOf(withId(R.id.error_text), isDescendantOfA(withId(R.id.input_pin)))).check(matches(withText("")))
    }
  }

  @Test
  fun testAddProfileActivity_inputWrongConfirmPin_checkConfirmWrongError() {
    ActivityScenario.launch(AddProfileActivity::class.java).use {
      onView(withId(R.id.checkbox_pin)).perform(click())
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin)))).perform(
        typeText("123"), closeSoftKeyboard()
      )
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_confirm_pin)))).perform(scrollTo())
        .perform(typeText("12"))
      onView(withId(R.id.create_button)).perform(scrollTo()).perform(click())
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
  fun testAddProfileActivity_configurationChange_inputWrongConfirmPin_checkConfirmWrongError() {
    ActivityScenario.launch(AddProfileActivity::class.java).use {
      onView(ViewMatchers.isRoot()).perform(OrientationChangeAction.orientationLandscape())
      onView(withId(R.id.checkbox_pin)).perform(scrollTo(),click())
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin)))).perform(
        scrollTo(), typeText("123"), closeSoftKeyboard()
      )
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_confirm_pin)))).perform(scrollTo())
        .perform(typeText("12"), closeSoftKeyboard())
      onView(withId(R.id.create_button)).perform(scrollTo()).perform(click())
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
  fun testAddProfileActivity_inputWrongConfirmPin_inputConfirmPin_checkErrorIsCleared() {
    ActivityScenario.launch(AddProfileActivity::class.java).use {
      onView(withId(R.id.checkbox_pin)).perform(click())
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin)))).perform(
        typeText("123"), closeSoftKeyboard()
      )
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_confirm_pin)))).perform(scrollTo())
        .perform(typeText("12"))
      onView(withId(R.id.create_button)).perform(scrollTo()).perform(click())
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_confirm_pin)))).perform(
        typeText("3"), closeSoftKeyboard()
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
  fun testAddProfileActivity_configurationChange_inputWrongConfirmPin_inputConfirmPin_checkErrorIsCleared() {
    ActivityScenario.launch(AddProfileActivity::class.java).use {
      onView(ViewMatchers.isRoot()).perform(OrientationChangeAction.orientationLandscape())
      onView(withId(R.id.checkbox_pin)).perform(scrollTo(),click())
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin)))).perform(
        scrollTo(), typeText("123"), closeSoftKeyboard()
      )
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_confirm_pin)))).perform(scrollTo())
        .perform(typeText("12"), closeSoftKeyboard())
      onView(withId(R.id.create_button)).perform(scrollTo()).perform(click())
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_confirm_pin)))).perform(
        scrollTo(), typeText("3"), closeSoftKeyboard()
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
  fun testAddProfileActivity_inputPin_checkAllowDownloadNotClickable() {
    ActivityScenario.launch(AddProfileActivity::class.java).use {
      onView(withId(R.id.checkbox_pin)).perform(click())
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin)))).perform(
        typeText("123"), closeSoftKeyboard()
      )
      onView(withId(R.id.allow_download_switch)).check(matches(not(isClickable())))
    }
  }

  @Test
  fun testAddProfileActivity_configurationChange_inputPin_checkAllowDownloadNotClickable() {
    ActivityScenario.launch(AddProfileActivity::class.java).use {
      onView(ViewMatchers.isRoot()).perform(OrientationChangeAction.orientationLandscape())
      onView(withId(R.id.checkbox_pin)).perform(scrollTo(),click())
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin)))).perform(
        scrollTo(), typeText("123"), closeSoftKeyboard()
      )
      onView(withId(R.id.allow_download_switch)).check(matches(not(isClickable())))
    }
  }

  @Test
  fun testAddProfileActivity_inputPin_inputConfirmPin_checkAllowDownloadClickable() {
    ActivityScenario.launch(AddProfileActivity::class.java).use {
      onView(withId(R.id.checkbox_pin)).perform(click())
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin)))).perform(
        typeText("123"), closeSoftKeyboard()
      )
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_confirm_pin)))).perform(scrollTo())
        .perform(typeText("123"), closeSoftKeyboard())
      onView(withId(R.id.allow_download_switch)).check(matches(isClickable()))
    }
  }

  @Test
  fun testAddProfileActivity_inputPin_inputConfirmPin_clickAllowDownload_configurationChange_checkAllowDownload() {
    ActivityScenario.launch(AddProfileActivity::class.java).use {
      onView(withId(R.id.checkbox_pin)).perform(click())
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin)))).perform(
        typeText("123"), closeSoftKeyboard()
      )
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_confirm_pin)))).perform(scrollTo())
        .perform(typeText("123"), closeSoftKeyboard())
      onView(withId(R.id.allow_download_switch)).check(matches(isClickable()))
      onView(withId(R.id.allow_download_switch)).perform(click())
      onView(ViewMatchers.isRoot()).perform(OrientationChangeAction.orientationLandscape())
      onView(withId(R.id.allow_download_switch)).check(matches(isChecked()))
    }
  }

  @Test
  fun testAddProfileActivity_configurationChange_inputPin_inputConfirmPin_checkAllowDownloadClickable() {
    ActivityScenario.launch(AddProfileActivity::class.java).use {
      onView(ViewMatchers.isRoot()).perform(OrientationChangeAction.orientationLandscape())
      onView(withId(R.id.checkbox_pin)).perform(scrollTo(),click())
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin)))).perform(
        scrollTo(), typeText("123"), closeSoftKeyboard()
      )
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_confirm_pin)))).perform(scrollTo())
        .perform(typeText("123"), closeSoftKeyboard())
      onView(withId(R.id.allow_download_switch)).check(matches(isClickable()))
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
    ActivityScenario.launch(AddProfileActivity::class.java).use {
      onView(withId(R.id.upload_image_button)).perform(click())
      intended(expectedIntent)
    }
  }

  @Test
  fun testAddProfileActivity_configurationChange_imageSelectAvatar_checkGalleryIntent() {
    val expectedIntent: Matcher<Intent> = allOf(
      hasAction(Intent.ACTION_PICK),
      hasData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
    )
    val activityResult = createGalleryPickActivityResultStub()
    intending(expectedIntent).respondWith(activityResult)
    ActivityScenario.launch(AddProfileActivity::class.java).use {
      onView(ViewMatchers.isRoot()).perform(OrientationChangeAction.orientationLandscape())
      onView(withId(R.id.upload_image_button)).perform(click())
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
    ActivityScenario.launch(AddProfileActivity::class.java).use {
      onView(withId(R.id.edit_image_fab)).perform(click())
      intended(expectedIntent)
    }
  }

  @Test
  fun testAddProfileActivity_configurationChange_imageSelectEdit_checkGalleryIntent() {
    val expectedIntent: Matcher<Intent> = allOf(
      hasAction(Intent.ACTION_PICK),
      hasData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
    )
    val activityResult = createGalleryPickActivityResultStub()
    intending(expectedIntent).respondWith(activityResult)
    ActivityScenario.launch(AddProfileActivity::class.java).use {
      onView(ViewMatchers.isRoot()).perform(OrientationChangeAction.orientationLandscape())
      onView(withId(R.id.edit_image_fab)).perform(click())
      intended(expectedIntent)
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
    resultIntent.setData(imageUri)
    return ActivityResult(RESULT_OK, resultIntent)
  }

  @Qualifier annotation class TestDispatcher

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

    fun inject(addProfileActivityTest: AddProfileActivityTest)
  }
}
