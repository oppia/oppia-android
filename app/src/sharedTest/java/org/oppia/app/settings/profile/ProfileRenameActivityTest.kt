package org.oppia.app.settings.profile

import android.app.Application
import android.content.Context
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.isClickable
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.isEnabled
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
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
class ProfileRenameActivityTest {

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
    DaggerProfileRenameActivityTest_TestApplicationComponent.builder()
      .setApplication(ApplicationProvider.getApplicationContext())
      .build()
      .inject(this)
  }

  @Test
  fun testProfileRenameActivity_inputNewName_clickSave_checkNameIsSaved() {
    launch<ProfileRenameActivity>(
      ProfileRenameActivity.createProfileRenameActivity(
        context,
        1
      )
    ).use {
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.input_name))
        )
      ).perform(typeText("James"))
      onView(withId(R.id.profile_rename_save_button)).perform(click())
      intended(hasComponent(ProfileEditActivity::class.java.name))
      onView(withId(R.id.profile_edit_name)).check(matches(withText("James")))
    }
  }

  @Test
  fun testProfileRenameActivity_inputNewName_configurationChange_checkSaveIsEnabled() {
    launch<ProfileRenameActivity>(
      ProfileRenameActivity.createProfileRenameActivity(
        context,
        1
      )
    ).use {
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.input_name))
        )
      ).perform(typeText("James"))
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.profile_rename_save_button)).check(matches(isEnabled()))
    }
  }

  @Test
  fun testProfileRenameActivity_inputNewName_configurationChange_inputTextExists() {
    launch<ProfileRenameActivity>(
      ProfileRenameActivity.createProfileRenameActivity(
        context,
        1
      )
    ).use {
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.input_name))
        )
      ).perform(typeText("James"))
      onView(isRoot()).perform(orientationLandscape())
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_name)))).check(
        matches(
          withText("James")
        )
      )
    }
  }

  @Test
  fun testProfileRenameActivity_inputOldName_clickSave_checkNameNotUniqueError() {
    launch<ProfileRenameActivity>(
      ProfileRenameActivity.createProfileRenameActivity(
        context,
        1
      )
    ).use {
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.input_name))
        )
      ).perform(typeText("Sean"))
      onView(withId(R.id.profile_rename_save_button)).perform(click())
      onView(
        allOf(
          withId(R.id.error_text),
          isDescendantOfA(withId(R.id.input_name))
        )
      ).check(matches(withText(context.getString(R.string.add_profile_error_name_not_unique))))
    }
  }

  @Test
  fun testProfileRenameActivity_inputOldNam_clickSave_inputName_checkErrorIsCleared() {
    launch<ProfileRenameActivity>(
      ProfileRenameActivity.createProfileRenameActivity(
        context,
        1
      )
    ).use {
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.input_name))
        )
      ).perform(typeText("Sean"))
      onView(withId(R.id.profile_rename_save_button)).perform(click())
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.input_name))
        )
      ).perform(typeText(" "))
      onView(
        allOf(
          withId(R.id.error_text),
          isDescendantOfA(withId(R.id.input_name))
        )
      ).check(matches(withText("")))
    }
  }

  @Test
  fun testProfileRenameActivity_inputNameWithNumbers_clickCreate_checkNameOnlyLettersError() {
    launch<ProfileRenameActivity>(
      ProfileRenameActivity.createProfileRenameActivity(
        context,
        1
      )
    ).use {
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.input_name))
        )
      ).perform(typeText("123"))
      onView(withId(R.id.profile_rename_save_button)).perform(click())
      onView(
        allOf(
          withId(R.id.error_text),
          isDescendantOfA(withId(R.id.input_name))
        )
      ).check(matches(withText(context.getString(R.string.add_profile_error_name_only_letters))))
    }
  }

  @Test
  fun testProfileRenameActivity_inputNameWithNumbers_clickCreate_inputName_checkErrorIsCleared() {
    launch<ProfileRenameActivity>(
      ProfileRenameActivity.createProfileRenameActivity(
        context,
        1
      )
    ).use {
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.input_name))
        )
      ).perform(typeText("123"))
      onView(withId(R.id.profile_rename_save_button)).perform(click())
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.input_name))
        )
      ).perform(typeText(" "))
      onView(
        allOf(
          withId(R.id.error_text),
          isDescendantOfA(withId(R.id.input_name))
        )
      ).check(matches(withText("")))
    }
  }

  @Test
  fun testProfileRenameActivity_inputName_changeConfiguration_checkNameIsDisplayed() {
    launch<ProfileRenameActivity>(
      ProfileRenameActivity.createProfileRenameActivity(
        context,
        1
      )
    ).use {
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_name)))).perform(
        typeText("test"),
        closeSoftKeyboard()
      )
      onView(isRoot()).perform(orientationLandscape())
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_name)))).check(
        matches(
          withText("test")
        )
      )
    }
  }

  @Test
  fun testProfileRenameActivity_inputOldName_clickSave_changeConfiguration_errorIsVisible() {
    launch<ProfileRenameActivity>(
      ProfileRenameActivity.createProfileRenameActivity(
        context,
        1
      )
    ).use {
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_name)))).perform(
        typeText("Sean"),
        closeSoftKeyboard()
      )
      onView(withId(R.id.profile_rename_save_button)).perform(click())
      onView(isRoot()).perform(orientationLandscape())
      onView(
        allOf(
          withId(R.id.error_text),
          isDescendantOfA(withId(R.id.input_name))
        )
      ).check(matches(withText(context.getString(R.string.add_profile_error_name_not_unique))))
    }
  }

  @Test
  fun testProfileRenameActivity_clickSave_changeConfiguration_saveButtonIsNotClickable() {
    launch<ProfileRenameActivity>(
      ProfileRenameActivity.createProfileRenameActivity(
        context,
        1
      )
    ).use {
      onView(withId(R.id.profile_rename_save_button)).check(matches(not(isClickable())))
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.profile_rename_save_button)).check(matches(not(isClickable())))
    }
  }

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

    fun inject(profileRenameActivity: ProfileRenameActivityTest)
  }
}
