package org.oppia.app.settings.profile

import android.app.Application
import android.content.Context
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isChecked
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.FirebaseApp
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import org.hamcrest.Matchers.not
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.R
import org.oppia.app.utility.OrientationChangeAction.Companion.orientationLandscape
import org.oppia.domain.oppialogger.LogStorageModule
import org.oppia.domain.profile.ProfileManagementController
import org.oppia.domain.profile.ProfileTestHelper
import org.oppia.testing.TestLogReportingModule
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
class ProfileEditActivityTest {

  @Inject
  lateinit var context: Context

  @Inject
  lateinit var profileTestHelper: ProfileTestHelper

  @Inject
  lateinit var profileManagementController: ProfileManagementController

  @Before
  @ExperimentalCoroutinesApi
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
    DaggerProfileEditActivityTest_TestApplicationComponent.builder()
      .setApplication(ApplicationProvider.getApplicationContext())
      .build()
      .inject(this)
  }

  @Test
  fun testProfileEditActivity_startActivityWithAdminProfile_checkAdminInfoIsDisplayed() {
    ActivityScenario.launch<ProfileEditActivity>(
      ProfileEditActivity.createProfileEditActivity(
        context,
        0
      )
    ).use {
      onView(withId(R.id.action_bar)).check(matches(hasDescendant(withText("Sean"))))
      onView(withId(R.id.profile_edit_name)).check(matches(withText("Sean")))
      onView(withId(R.id.profile_edit_allow_download_container)).check(matches(not(isDisplayed())))
      onView(withId(R.id.profile_delete_button)).check(matches(not(isDisplayed())))
    }
  }

  /* ktlint-disable max-line-length */
  @Test
  fun testProfileEditActivity_configurationChange_startActivityWithAdminProfile_checkAdminInfoIsDisplayed() {
    ActivityScenario.launch<ProfileEditActivity>(
      ProfileEditActivity.createProfileEditActivity(
        context,
        0
      )
    ).use {
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.action_bar)).check(matches(hasDescendant(withText("Sean"))))
      onView(withId(R.id.profile_edit_name)).check(matches(withText("Sean")))
      onView(withId(R.id.profile_edit_allow_download_container)).check(matches(not(isDisplayed())))
      onView(withId(R.id.profile_delete_button)).check(matches(not(isDisplayed())))
    }
  }
  /* ktlint-enable max-line-length */

  @Test
  fun testProfileEditActivity_startActivityWithUserProfile_checkUserInfoIsDisplayed() {
    ActivityScenario.launch<ProfileEditActivity>(
      ProfileEditActivity.createProfileEditActivity(
        context,
        1
      )
    ).use {
      onView(withId(R.id.action_bar)).check(matches(hasDescendant(withText("Ben"))))
      onView(withId(R.id.profile_edit_name)).check(matches(withText("Ben")))
      onView(withId(R.id.profile_edit_allow_download_container)).check(matches((isDisplayed())))
      onView(withId(R.id.profile_delete_button)).check(matches((isDisplayed())))
    }
  }

  /* ktlint-disable max-line-length */
  @Test
  fun testProfileEditActivity_configurationChange_startActivityWithUserProfile_checkUserInfoIsDisplayed() {
    ActivityScenario.launch<ProfileEditActivity>(
      ProfileEditActivity.createProfileEditActivity(
        context,
        1
      )
    ).use {
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.action_bar)).check(matches(hasDescendant(withText("Ben"))))
      onView(withId(R.id.profile_edit_name)).check(matches(withText("Ben")))
      onView(withId(R.id.profile_edit_allow_download_container)).perform(scrollTo())
        .check(matches((isDisplayed())))
      onView(withId(R.id.profile_delete_button))
        .perform(scrollTo())
        .check(
          matches(
            (
              isDisplayed()
              )
          )
        )
    }
  }
  /* ktlint-enable max-line-length */

  /* ktlint-disable max-line-length */
  @Test
  fun testProfileEditActivity_startActivityWithUserProfile_clickRenameButton_checkOpensProfileRenameActivity() {
    ActivityScenario.launch<ProfileEditActivity>(
      ProfileEditActivity.createProfileEditActivity(
        context,
        1
      )
    ).use {
      onView(withId(R.id.profile_rename_button)).perform(click())
      intended(hasComponent(ProfileRenameActivity::class.java.name))
    }
  }
  /* ktlint-enable max-line-length */

  /* ktlint-disable max-line-length */
  @Test
  fun testProfileEditActivity_configurationChange_startActivityWithUserProfile_clickRenameButton_checkOpensProfileRenameActivity() {
    ActivityScenario.launch<ProfileEditActivity>(
      ProfileEditActivity.createProfileEditActivity(
        context,
        1
      )
    ).use {
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.profile_rename_button)).perform(click())
      intended(hasComponent(ProfileRenameActivity::class.java.name))
    }
  }
  /* ktlint-enable max-line-length */

  /* ktlint-disable max-line-length */
  @Test
  fun testProfileEditActivity_startActivityWithUserProfile_clickResetPin_checkOpensProfileResetPinActivity() {
    ActivityScenario.launch<ProfileEditActivity>(
      ProfileEditActivity.createProfileEditActivity(
        context,
        1
      )
    ).use {
      onView(withId(R.id.profile_reset_button)).perform(click())
      intended(hasComponent(ProfileResetPinActivity::class.java.name))
    }
  }
  /* ktlint-enable max-line-length */

  /* ktlint-disable max-line-length */
  @Test
  fun testProfileEditActivity_configurationChange_startActivityWithUserProfile_clickResetPin_checkOpensProfileResetPinActivity() {
    ActivityScenario.launch<ProfileEditActivity>(
      ProfileEditActivity.createProfileEditActivity(
        context,
        1
      )
    ).use {
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.profile_reset_button)).perform(scrollTo()).perform(click())
      intended(hasComponent(ProfileResetPinActivity::class.java.name))
    }
  }
  /* ktlint-enable max-line-length */

  /* ktlint-disable max-line-length */
  @Test
  fun testProfileEditActivity_startActivityWithUserProfile_clickProfileDeletionButton_checkOpensDeletionDialog() {
    ActivityScenario.launch<ProfileEditActivity>(
      ProfileEditActivity.createProfileEditActivity(
        context,
        1
      )
    ).use {
      onView(withId(R.id.profile_delete_button)).perform(click())
      onView(withText(R.string.profile_edit_delete_dialog_message))
        .check(
          matches(
            isDisplayed()
          )
        )
    }
  }
  /* ktlint-enable max-line-length */

  /* ktlint-disable max-line-length */
  @Test
  fun testProfileEditActivity_configurationChange_startActivityWithUserProfile_clickProfileDeletionButton_checkOpensDeletionDialog() {
    ActivityScenario.launch<ProfileEditActivity>(
      ProfileEditActivity.createProfileEditActivity(
        context,
        1
      )
    ).use {
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.profile_delete_button)).perform(scrollTo()).perform(click())
      onView(withText(R.string.profile_edit_delete_dialog_message))
        .check(
          matches(
            isDisplayed()
          )
        )
    }
  }
  /* ktlint-enable max-line-length */

  /* ktlint-disable max-line-length */
  @Test
  fun testProfileEditActivity_startActivityWithUserProfile_clickProfileDeletionButton_clickDelete_checkReturnsToProfileListActivity() {
    ActivityScenario.launch<ProfileEditActivity>(
      ProfileEditActivity.createProfileEditActivity(
        context,
        1
      )
    ).use {
      onView(withId(R.id.profile_delete_button)).perform(click())
      onView(withText(R.string.profile_edit_delete_dialog_positive)).perform(click())
      intended(hasComponent(ProfileListActivity::class.java.name))
    }
  }
  /* ktlint-enable max-line-length */

  /* ktlint-disable max-line-length */
  @Test
  fun testProfileEditActivity_configurationChange_startActivityWithUserProfile_clickProfileDeletionButton_clickDelete_checkReturnsToProfileListActivity() {
    ActivityScenario.launch<ProfileEditActivity>(
      ProfileEditActivity.createProfileEditActivity(
        context,
        1
      )
    ).use {
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.profile_delete_button)).perform(scrollTo()).perform(click())
      onView(withText(R.string.profile_edit_delete_dialog_positive)).perform(click())
      intended(hasComponent(ProfileListActivity::class.java.name))
    }
  }
  /* ktlint-enable max-line-length */

  @Test
  fun testProfileEditActivity_startActivityWithUserHasDownloadAccess_checkSwitchIsChecked() {
    profileManagementController.addProfile(
      name = "James",
      pin = "123",
      avatarImagePath = null,
      allowDownloadAccess = true,
      colorRgb = -10710042,
      isAdmin = false
    )
    ActivityScenario.launch<ProfileEditActivity>(
      ProfileEditActivity.createProfileEditActivity(
        context,
        3
      )
    ).use {
      onView(withId(R.id.profile_edit_allow_download_switch)).check(matches(isChecked()))
    }
  }

  /* ktlint-disable max-line-length */
  @Test
  fun testProfileEditActivity_configurationChange_startActivityWithUserHasDownloadAccess_checkSwitchIsChecked() {
    profileManagementController.addProfile(
      name = "James",
      pin = "123",
      avatarImagePath = null,
      allowDownloadAccess = true,
      colorRgb = -10710042,
      isAdmin = false
    )
    ActivityScenario.launch<ProfileEditActivity>(
      ProfileEditActivity.createProfileEditActivity(
        context,
        3
      )
    ).use {
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.profile_edit_allow_download_switch)).check(matches(isChecked()))
    }
  }
  /* ktlint-enable max-line-length */

  @Qualifier
  annotation class TestDispatcher

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
  @Component(modules = [TestModule::class, TestLogReportingModule::class, LogStorageModule::class])
  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun inject(profileEditActivity: ProfileEditActivityTest)
  }
}
