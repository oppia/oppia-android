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
import androidx.test.espresso.matcher.ViewMatchers.isChecked
import androidx.test.espresso.matcher.ViewMatchers.isClickable
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isRoot
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

@RunWith(AndroidJUnit4::class)
class AddProfileActivityTest {

  @Inject lateinit var context: Context
  @Inject lateinit var profileTestHelper: ProfileTestHelper

  @Before
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
    launch(AddProfileActivity::class.java).use {
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_user_name_profile_input_view))
        )
      ).perform(
        typeText("test"), closeSoftKeyboard()
      )
      onView(withId(R.id.add_profile_activity_create_button)).perform(scrollTo())
      onView(withId(R.id.add_profile_activity_create_button)).perform(click())
      intended(hasComponent(ProfileActivity::class.java.name))
    }
  }

  @Test
  fun testAddProfileActivity_changeConfiguration_inputName_clickCreate_checkOpensProfileActivity() {
    launch(AddProfileActivity::class.java).use {
      onView(isRoot()).perform(orientationLandscape())
      onView(allOf(withId(R.id.add_profile_activity_pin_check_box))).perform(scrollTo())
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_user_name_profile_input_view))
        )
      ).perform(scrollTo()).perform(
        typeText("test"), closeSoftKeyboard()
      )
      onView(withId(R.id.add_profile_activity_create_button)).perform(scrollTo())
      onView(withId(R.id.add_profile_activity_create_button)).perform(click())
      intended(hasComponent(ProfileActivity::class.java.name))
    }
  }

  @Test
  fun testAddProfileActivity_clickOnCheckbox_createPin_checkIsDisplayed() {
    launch(AddProfileActivity::class.java).use {
      onView(allOf(withId(R.id.add_profile_activity_pin_check_box))).perform(click())
      onView(withId(R.id.add_profile_activity_input_pin_profile_input_view)).check(
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
      onView(withId(R.id.add_profile_activity_input_pin_profile_input_view)).perform(scrollTo())
      onView(withId(R.id.add_profile_activity_input_pin_profile_input_view)).check(
        matches(
          isDisplayed()
        )
      )
    }
  }

  @Test
  fun testAddProfileActivity_createPin_checkNotVisible() {
    launch(AddProfileActivity::class.java).use {
      onView(withId(R.id.add_profile_activity_input_pin_profile_input_view)).check(
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
      onView(withId(R.id.add_profile_activity_input_pin_profile_input_view)).check(
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
        typeText("test"),
        closeSoftKeyboard()
      )
      onView(withId(R.id.add_profile_activity_pin_check_box)).perform(click())
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_input_pin_profile_input_view))
        )
      ).perform(
        typeText("123"),
        closeSoftKeyboard()
      )
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_input_confirm_pin_profile_input_view))
        )
      ).perform(scrollTo())
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_input_confirm_pin_profile_input_view))
        )
      ).perform(
        typeText("123"),
        closeSoftKeyboard()
      )
      onView(withId(R.id.add_profile_activity_create_button)).perform(scrollTo())
      onView(withId(R.id.add_profile_activity_create_button)).perform(click())
      intended(hasComponent(ProfileActivity::class.java.name))
    }
  }

  @Test
  fun testAddProfileActivity_changeConfiguration_inputName_inputPin_clickCreate_checkOpensProfileActivity() {
    launch(AddProfileActivity::class.java).use {
      onView(isRoot()).perform(orientationLandscape())
      onView(allOf(withId(R.id.add_profile_activity_pin_check_box))).perform(scrollTo())
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_user_name_profile_input_view))
        )
      ).perform(
        typeText("test"),
        closeSoftKeyboard()
      )
      onView(withId(R.id.add_profile_activity_pin_check_box)).perform(scrollTo()).perform(click())
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_input_pin_profile_input_view))
        )
      ).perform(scrollTo())
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_input_pin_profile_input_view))
        )
      ).perform(
        typeText("123"),
        closeSoftKeyboard()
      )
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_input_confirm_pin_profile_input_view))
        )
      ).perform(scrollTo())
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_input_confirm_pin_profile_input_view))
        )
      ).perform(
        typeText("123"),
        closeSoftKeyboard()
      )
      onView(withId(R.id.add_profile_activity_create_button)).perform(scrollTo())
      onView(withId(R.id.add_profile_activity_create_button)).perform(click())
      intended(hasComponent(ProfileActivity::class.java.name))
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
  @ExperimentalCoroutinesApi
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
  @ExperimentalCoroutinesApi
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
  @ExperimentalCoroutinesApi
  fun testAddProfileActivity_inputNotUniqueName_clickCreate_checkNameNotUniqueError() {
    profileTestHelper.initializeProfiles()
    launch(AddProfileActivity::class.java).use {
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_user_name_profile_input_view))
        )
      ).perform(
        typeText("Sean"), closeSoftKeyboard()
      )
      onView(withId(R.id.add_profile_activity_create_button)).perform(scrollTo())
      onView(withId(R.id.add_profile_activity_create_button)).perform(click())
      onView(
        allOf(
          withId(R.id.error_text),
          isDescendantOfA(withId(R.id.add_profile_activity_user_name_profile_input_view))
        )
      ).check(matches(withText(context.getString(R.string.add_profile_error_name_not_unique))))
    }
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testAddProfileActivity_changeConfiguration_inputNotUniqueName_clickCreate_checkNameNotUniqueError() {
    profileTestHelper.initializeProfiles()
    launch(AddProfileActivity::class.java).use {
      onView(isRoot()).perform(orientationLandscape())
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_user_name_profile_input_view))
        )
      ).perform(scrollTo()).perform(
        typeText("Sean"), closeSoftKeyboard()
      )
      onView(withId(R.id.add_profile_activity_create_button)).perform(scrollTo())
      onView(withId(R.id.add_profile_activity_create_button)).perform(click())
      onView(
        allOf(
          withId(R.id.error_text),
          isDescendantOfA(withId(R.id.add_profile_activity_user_name_profile_input_view))
        )
      ).check(matches(withText(context.getString(R.string.add_profile_error_name_not_unique))))
    }
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testAddProfileActivity_inputNotUniqueName_clickCreate_inputName_checkErrorIsCleared() {
    profileTestHelper.initializeProfiles()
    launch(AddProfileActivity::class.java).use {
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_user_name_profile_input_view))
        )
      ).perform(
        typeText("Sean"), closeSoftKeyboard()
      )
      onView(withId(R.id.add_profile_activity_create_button)).perform(scrollTo())
      onView(withId(R.id.add_profile_activity_create_button)).perform(click())
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_user_name_profile_input_view))
        )
      ).perform(
        typeText(" "), closeSoftKeyboard()
      )
      onView(
        allOf(
          withId(R.id.error_text),
          isDescendantOfA(withId(R.id.add_profile_activity_user_name_profile_input_view))
        )
      ).check(matches(withText("")))
    }
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testAddProfileActivity_changeConfiguration_inputNotUniqueName_clickCreate_inputName_checkErrorIsCleared() {
    profileTestHelper.initializeProfiles()
    launch(AddProfileActivity::class.java).use {
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.add_profile_activity_pin_check_box)).perform(scrollTo())
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_user_name_profile_input_view))
        )
      ).perform(scrollTo()).perform(
        typeText("Sean"), closeSoftKeyboard()
      )
      onView(withId(R.id.add_profile_activity_create_button)).perform(scrollTo())
      onView(withId(R.id.add_profile_activity_create_button)).perform(click())
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
        typeText(" "), closeSoftKeyboard()
      )
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
      onView(withId(R.id.add_profile_activity_create_button)).perform(scrollTo())
      onView(withId(R.id.add_profile_activity_create_button)).perform(click())
      onView(
        allOf(
          withId(R.id.error_text),
          isDescendantOfA(withId(R.id.add_profile_activity_user_name_profile_input_view))
        )
      ).check(matches(withText(context.getString(R.string.add_profile_error_name_only_letters))))
    }
  }

  @Test
  fun testAddProfileActivity_changeConfiguration_inputNameWithNumbers_clickCreate_checkNameOnlyLettersError() {
    launch(AddProfileActivity::class.java).use {
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.add_profile_activity_pin_check_box)).perform(scrollTo())
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_user_name_profile_input_view))
        )
      ).perform(
        typeText("123"), closeSoftKeyboard()
      )
      onView(withId(R.id.add_profile_activity_create_button)).perform(scrollTo())
      onView(withId(R.id.add_profile_activity_create_button)).perform(click())
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
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_user_name_profile_input_view))
        )
      ).perform(
        typeText("123"), closeSoftKeyboard()
      )
      onView(withId(R.id.add_profile_activity_create_button)).perform(scrollTo())
      onView(withId(R.id.add_profile_activity_create_button)).perform(click())
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_user_name_profile_input_view))
        )
      ).perform(
        typeText(" "), closeSoftKeyboard()
      )
      onView(
        allOf(
          withId(R.id.error_text),
          isDescendantOfA(withId(R.id.add_profile_activity_user_name_profile_input_view))
        )
      ).check(matches(withText("")))
    }
  }

  @Test
  fun testAddProfileActivity_changeConfiguration_inputNameWithNumbers_clickCreate_inputName_checkErrorIsCleared() {
    launch(AddProfileActivity::class.java).use {
      onView(isRoot()).perform(orientationLandscape())
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_user_name_profile_input_view))
        )
      ).perform(scrollTo()).perform(
        typeText("123"), closeSoftKeyboard()
      )
      onView(withId(R.id.add_profile_activity_create_button)).perform(scrollTo())
      onView(withId(R.id.add_profile_activity_create_button)).perform(click())
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
        typeText(" "), closeSoftKeyboard()
      )
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
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_user_name_profile_input_view))
        )
      ).perform(
        typeText("test"), closeSoftKeyboard()
      )
      onView(withId(R.id.add_profile_activity_pin_check_box)).perform(click())
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_input_pin_profile_input_view))
        )
      ).perform(
        typeText("12"), closeSoftKeyboard()
      )
      onView(withId(R.id.add_profile_activity_create_button)).perform(scrollTo())
      onView(withId(R.id.add_profile_activity_create_button)).perform(click())
      onView(
        allOf(
          withId(R.id.error_text),
          isDescendantOfA(withId(R.id.add_profile_activity_input_pin_profile_input_view))
        )
      ).check(matches(withText(context.getString(R.string.add_profile_error_pin_length))))
    }
  }

  @Test
  fun testAddProfileActivity_changeConfiguration_inputShortPin_clickCreate_checkPinLengthError() {
    launch(AddProfileActivity::class.java).use {
      onView(isRoot()).perform(orientationLandscape())
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_user_name_profile_input_view))
        )
      ).perform(scrollTo()).perform(
        typeText("test"), closeSoftKeyboard()
      )
      onView(withId(R.id.add_profile_activity_pin_check_box)).perform(scrollTo())
      onView(withId(R.id.add_profile_activity_pin_check_box)).perform(click())
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_input_pin_profile_input_view))
        )
      ).perform(scrollTo())
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_input_pin_profile_input_view))
        )
      ).perform(
        typeText("12"),
        closeSoftKeyboard()
      )
      onView(withId(R.id.add_profile_activity_create_button)).perform(scrollTo())
      onView(withId(R.id.add_profile_activity_create_button)).perform(click())
      onView(
        allOf(
          withId(R.id.error_text),
          isDescendantOfA(withId(R.id.add_profile_activity_input_pin_profile_input_view))
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
          isDescendantOfA(withId(R.id.add_profile_activity_input_pin_profile_input_view))
        )
      ).perform(
        typeText("12"), closeSoftKeyboard()
      )
      onView(withId(R.id.add_profile_activity_create_button)).perform(scrollTo())
      onView(withId(R.id.add_profile_activity_create_button)).perform(click())
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_input_pin_profile_input_view))
        )
      ).perform(scrollTo()).perform(
        typeText("3"), closeSoftKeyboard()
      )
      onView(
        allOf(
          withId(R.id.error_text),
          isDescendantOfA(withId(R.id.add_profile_activity_input_pin_profile_input_view))
        )
      ).check(
        matches(
          withText("")
        )
      )
    }
  }

  @Test
  fun testAddProfileActivity_changeConfiguration_inputShortPin_clickCreate_inputPin_checkErrorIsCleared() {
    launch(AddProfileActivity::class.java).use {
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.add_profile_activity_pin_check_box)).perform(scrollTo())
      onView(withId(R.id.add_profile_activity_pin_check_box)).perform(click())
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_input_pin_profile_input_view))
        )
      ).perform(scrollTo()).perform(
        typeText("12"),
        closeSoftKeyboard()
      )
      onView(withId(R.id.add_profile_activity_create_button)).perform(scrollTo()).perform(click())
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_input_pin_profile_input_view))
        )
      ).perform(scrollTo())
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_input_pin_profile_input_view))
        )
      ).perform(
        typeText("3"),
        closeSoftKeyboard()
      )
      onView(
        allOf(
          withId(R.id.error_text),
          isDescendantOfA(withId(R.id.add_profile_activity_input_pin_profile_input_view))
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
        typeText("test"), closeSoftKeyboard()
      )
      onView(withId(R.id.add_profile_activity_pin_check_box)).perform(click())
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_input_pin_profile_input_view))
        )
      ).perform(
        typeText("123"), closeSoftKeyboard()
      )
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_input_confirm_pin_profile_input_view))
        )
      ).perform(scrollTo())
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_input_confirm_pin_profile_input_view))
        )
      ).perform(typeText("12"))
      onView(withId(R.id.add_profile_activity_create_button)).perform(scrollTo())
      onView(withId(R.id.add_profile_activity_create_button)).perform(click())
      onView(
        allOf(
          withId(R.id.error_text),
          isDescendantOfA(withId(R.id.add_profile_activity_input_confirm_pin_profile_input_view))
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
        typeText("test"), closeSoftKeyboard()
      )
      onView(withId(R.id.add_profile_activity_pin_check_box)).perform(scrollTo())
      onView(withId(R.id.add_profile_activity_pin_check_box)).perform(click())
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_input_pin_profile_input_view))
        )
      ).perform(scrollTo())
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_input_pin_profile_input_view))
        )
      ).perform(
        typeText("123"),
        closeSoftKeyboard()
      )
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_input_confirm_pin_profile_input_view))
        )
      ).perform(scrollTo())
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_input_confirm_pin_profile_input_view))
        )
      ).perform(
        typeText("12"),
        closeSoftKeyboard()
      )
      onView(withId(R.id.add_profile_activity_create_button)).perform(scrollTo())
      onView(withId(R.id.add_profile_activity_create_button)).perform(click())
      onView(
        allOf(
          withId(R.id.error_text),
          isDescendantOfA(withId(R.id.add_profile_activity_input_confirm_pin_profile_input_view))
        )
      ).perform(scrollTo())
      onView(
        allOf(
          withId(R.id.error_text),
          isDescendantOfA(withId(R.id.add_profile_activity_input_confirm_pin_profile_input_view))
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
          isDescendantOfA(withId(R.id.add_profile_activity_input_pin_profile_input_view))
        )
      ).perform(scrollTo())
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_input_pin_profile_input_view))
        )
      ).perform(
        typeText("123"),
        closeSoftKeyboard()
      )
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_input_confirm_pin_profile_input_view))
        )
      ).perform(scrollTo())
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_input_confirm_pin_profile_input_view))
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
          isDescendantOfA(withId(R.id.add_profile_activity_input_confirm_pin_profile_input_view))
        )
      ).perform(scrollTo())
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_input_confirm_pin_profile_input_view))
        )
      ).perform(
        typeText("3"),
        closeSoftKeyboard()
      )
      onView(
        allOf(
          withId(R.id.error_text),
          isDescendantOfA(withId(R.id.add_profile_activity_input_confirm_pin_profile_input_view))
        )
      ).check(matches(withText("")))
    }
  }

  @Test
  fun testAddProfileActivity_changeConfiguration_inputWrongConfirmPin_inputConfirmPin_checkErrorIsCleared() {
    launch(AddProfileActivity::class.java).use {
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.add_profile_activity_pin_check_box)).perform(scrollTo())
      onView(withId(R.id.add_profile_activity_pin_check_box)).perform(click())
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_input_pin_profile_input_view))
        )
      ).perform(scrollTo()).perform(
        typeText("123"),
        closeSoftKeyboard()
      )
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_input_confirm_pin_profile_input_view))
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
          isDescendantOfA(withId(R.id.add_profile_activity_input_confirm_pin_profile_input_view))
        )
      ).perform(scrollTo())
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_input_confirm_pin_profile_input_view))
        )
      ).perform(
        typeText("3"),
        closeSoftKeyboard()
      )
      onView(
        allOf(
          withId(R.id.error_text),
          isDescendantOfA(withId(R.id.add_profile_activity_input_confirm_pin_profile_input_view))
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
          isDescendantOfA(withId(R.id.add_profile_activity_input_pin_profile_input_view))
        )
      ).perform(
        typeText("123"), closeSoftKeyboard()
      )
      onView(withId(R.id.add_profile_activity_allow_download_switch)).check(matches(not(isClickable())))
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
          isDescendantOfA(withId(R.id.add_profile_activity_input_pin_profile_input_view))
        )
      ).perform(scrollTo()).perform(
        typeText("123"),
        closeSoftKeyboard()
      )
      onView(withId(R.id.add_profile_activity_allow_download_switch)).check(matches(not(isClickable())))
    }
  }

  @Test
  fun testAddProfileActivity_inputPin_inputConfirmPin_checkAllowDownloadClickable() {
    launch(AddProfileActivity::class.java).use {
      onView(withId(R.id.add_profile_activity_pin_check_box)).perform(click())
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_input_pin_profile_input_view))
        )
      ).perform(
        typeText("123"),
        closeSoftKeyboard()
      )
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_input_confirm_pin_profile_input_view))
        )
      ).perform(scrollTo())
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_input_confirm_pin_profile_input_view))
        )
      ).perform(
        typeText("123"),
        closeSoftKeyboard()
      )
      onView(withId(R.id.add_profile_activity_allow_download_switch)).check(matches(isClickable()))
    }
  }

  @Test
  fun testAddProfileActivity_changeConfiguration_inputPin_inputConfirmPin_checkAllowDownloadClickable() {
    launch(AddProfileActivity::class.java).use {
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.add_profile_activity_pin_check_box)).perform(scrollTo())
      onView(withId(R.id.add_profile_activity_pin_check_box)).perform(click())
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_input_pin_profile_input_view))
        )
      ).perform(scrollTo())
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_input_pin_profile_input_view))
        )
      ).perform(
        typeText("123"),
        closeSoftKeyboard()
      )
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_input_confirm_pin_profile_input_view))
        )
      ).perform(scrollTo())
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_input_confirm_pin_profile_input_view))
        )
      ).perform(
        typeText("123"),
        closeSoftKeyboard()
      )
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
          isDescendantOfA(withId(R.id.add_profile_activity_input_pin_profile_input_view))
        )
      ).perform(
        typeText("123"),
        closeSoftKeyboard()
      )
      onView(isRoot()).perform(orientationLandscape())
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_input_pin_profile_input_view))
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
          isDescendantOfA(withId(R.id.add_profile_activity_input_confirm_pin_profile_input_view))
        )
      ).perform(scrollTo()).perform(
        typeText("123"),
        closeSoftKeyboard()
      )
      onView(isRoot()).perform(orientationLandscape())
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_input_confirm_pin_profile_input_view))
        )
      ).perform(scrollTo())
        .check(matches(withText("123")))
    }
  }

  @Test
  fun testAddProfileActivity_inputName_inputPin_inputConfirmPin_changeConfiguration_checkName_checkPin_checkConfirmPin_IsDisplayed() {
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
      onView(withId(R.id.add_profile_activity_pin_check_box)).perform(click())
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_input_pin_profile_input_view))
        )
      ).perform(
        typeText("123"), closeSoftKeyboard()
      )
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_input_confirm_pin_profile_input_view))
        )
      ).perform(scrollTo())
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_input_confirm_pin_profile_input_view))
        )
      ).perform(
        typeText("123"),
        closeSoftKeyboard()
      )
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
          isDescendantOfA(withId(R.id.add_profile_activity_input_pin_profile_input_view))
        )
      ).perform(scrollTo())
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_input_pin_profile_input_view))
        )
      ).check(matches(withText("123")))
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_input_confirm_pin_profile_input_view))
        )
      ).perform(scrollTo())
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_input_confirm_pin_profile_input_view))
        )
      ).check(matches(withText("123")))
    }
  }

  @Test
  fun testAddProfileActivity_inputName_inputPin_inputConfirmPin_deselectPIN_clickCreate_checkOpensProfileActivity() {
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
      onView(withId(R.id.add_profile_activity_pin_check_box)).perform(click())
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_input_pin_profile_input_view))
        )
      ).perform(
        typeText("123"),
        closeSoftKeyboard()
      )
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_input_confirm_pin_profile_input_view))
        )
      ).perform(scrollTo())
        .perform(typeText("123"), closeSoftKeyboard())
      onView(withId(R.id.add_profile_activity_pin_check_box)).perform(scrollTo())
      onView(withId(R.id.add_profile_activity_pin_check_box)).perform(click())
      onView(withId(R.id.add_profile_activity_create_button)).perform(scrollTo())
      onView(withId(R.id.add_profile_activity_create_button)).perform(click())
      intended(hasComponent(ProfileActivity::class.java.name))
    }
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testAddProfileActivity_inputNotUniqueName_clickCreate_changeConfiguration_checkErrorMessageDisplayed() {
    profileTestHelper.initializeProfiles()
    launch(AddProfileActivity::class.java).use {
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_user_name_profile_input_view))
        )
      ).perform(
        typeText("Sean"),
        closeSoftKeyboard()
      )
      onView(withId(R.id.add_profile_activity_create_button)).perform(scrollTo())
      onView(withId(R.id.add_profile_activity_create_button)).perform(click())
      onView(isRoot()).perform(orientationLandscape())
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
  fun testAddProfileActivity_inputPin_inputConfirmPin_changeConfiguration_checkPin_checkConfirmPin_IsDisplayed() {
    launch(AddProfileActivity::class.java).use {
      onView(withId(R.id.add_profile_activity_pin_check_box)).perform(click())
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_input_pin_profile_input_view))
        )
      ).perform(
        typeText("123"),
        closeSoftKeyboard()
      )
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_input_confirm_pin_profile_input_view))
        )
      ).perform(scrollTo())
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_input_confirm_pin_profile_input_view))
        )
      ).perform(
        typeText("123"),
        closeSoftKeyboard()
      )
      onView(isRoot()).perform(orientationLandscape())
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_input_pin_profile_input_view))
        )
      ).perform(scrollTo())
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_input_pin_profile_input_view))
        )
      ).check(matches(withText("123")))
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_input_confirm_pin_profile_input_view))
        )
      ).perform(scrollTo())
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_input_confirm_pin_profile_input_view))
        )
      ).check(matches(withText("123")))
    }
  }

  @Test
  fun testAddProfileActivity_inputPin_inputDifferentConfirmPin_clickCreate_changeConfiguration_checkErrorMessageDisplayed() {
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
      onView(withId(R.id.add_profile_activity_pin_check_box)).perform(click())
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_input_pin_profile_input_view))
        )
      ).perform(
        typeText("123"),
        closeSoftKeyboard()
      )
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_input_confirm_pin_profile_input_view))
        )
      ).perform(scrollTo())
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_input_confirm_pin_profile_input_view))
        )
      ).perform(
        typeText("321"),
        closeSoftKeyboard()
      )
      onView(withId(R.id.add_profile_activity_create_button)).perform(scrollTo())
      onView(withId(R.id.add_profile_activity_create_button)).perform(click())
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.add_profile_activity_pin_check_box)).perform(scrollTo())
      onView(
        allOf(
          withId(R.id.error_text),
          isDescendantOfA(withId(R.id.add_profile_activity_input_confirm_pin_profile_input_view))
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
  fun testAddProfileActivity_inputPin_inputConfirmPin_turnOnDownloadAccessSwitch_changeConfiguration_checkDownloadAccessSwitchIsOn() {
    launch(AddProfileActivity::class.java).use {
      onView(withId(R.id.add_profile_activity_pin_check_box)).perform(click())
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_input_pin_profile_input_view))
        )
      ).perform(
        typeText("123"),
        closeSoftKeyboard()
      )
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_input_confirm_pin_profile_input_view))
        )
      ).perform(scrollTo())
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.add_profile_activity_input_confirm_pin_profile_input_view))
        )
      ).perform(
        typeText("123"),
        closeSoftKeyboard()
      )
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
      onView(withText(context.getString(R.string.add_profile_pin_info))).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testAddProfileActivity_clickInfo_changeConfiguration_checkInfoPopupIsDisplayed() {
    launch(AddProfileActivity::class.java).use {
      onView(withId(R.id.add_profile_activity_info_image_view)).perform(click())
      onView(isRoot()).perform(orientationLandscape())
      onView(withText(context.getString(R.string.add_profile_pin_info))).check(matches(isDisplayed()))
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
