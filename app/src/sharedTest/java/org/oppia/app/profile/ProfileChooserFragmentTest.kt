package org.oppia.app.profile

import android.app.Application
import android.content.Context
import android.content.Intent
import android.widget.TextView
import android.content.res.Resources
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.pressBack
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withParent
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.instanceOf
import org.hamcrest.Matchers.not
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.R
import org.oppia.app.databinding.getTimeAgo
import org.oppia.app.model.ProfileId
import org.oppia.app.administratorcontrols.AdministratorControlsActivity
import org.oppia.app.model.AppLanguage
import org.oppia.app.model.AudioLanguage
import org.oppia.app.model.StoryTextSize
import org.oppia.app.recyclerview.RecyclerViewMatcher.Companion.atPosition
import org.oppia.domain.profile.ProfileTestHelper
import org.oppia.app.recyclerview.RecyclerViewMatcher.Companion.atPositionOnView
import org.oppia.domain.profile.ProfileManagementController
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
class ProfileChooserFragmentTest {

  @Inject lateinit var profileTestHelper: ProfileTestHelper
  @Inject lateinit var profileManagementController: ProfileManagementController
  @Inject lateinit var context: Context

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
    DaggerProfileChooserFragmentTest_TestApplicationComponent.builder()
      .setApplication(ApplicationProvider.getApplicationContext())
      .build()
      .inject(this)
  }

  @Test
  fun testProfileChooserFragment_initializeProfiles_checkProfilesAreShown() {
    profileTestHelper.initializeProfiles()
    ActivityScenario.launch(ProfileActivity::class.java).use {
      onView(withId(R.id.profile_recycler_view)).perform(scrollToPosition<RecyclerView.ViewHolder>(0))
      onView(atPositionOnView(R.id.profile_recycler_view, 0, R.id.profile_name_text)).check(matches(withText("Sean")))
      onView(atPositionOnView(R.id.profile_recycler_view, 0, R.id.profile_is_admin_text)).check(matches(withText(context.getString(R.string.profile_chooser_admin))))
      onView(withId(R.id.profile_recycler_view)).perform(scrollToPosition<RecyclerView.ViewHolder>(1))
      onView(atPositionOnView(R.id.profile_recycler_view, 1, R.id.profile_name_text)).check(matches(withText("Ben")))
      onView(atPositionOnView(R.id.profile_recycler_view, 1, R.id.profile_is_admin_text)).check(matches(not(isDisplayed())))
      onView(withId(R.id.profile_recycler_view)).perform(scrollToPosition<RecyclerView.ViewHolder>(2))
      onView(atPositionOnView(R.id.profile_recycler_view, 2, R.id.add_profile_text)).check(matches(withText(context.getString(R.string.profile_chooser_add))))
    }
  }

  @Test
  fun testProfileChooserFragment_initializeProfiles_checkProfilesLastVistedTimeIsShown() {
    profileTestHelper.initializeProfiles()
    ActivityScenario.launch<ProfileActivity>(createProfileActivityIntent()) .use {
      onView(atPosition(R.id.profile_recycler_view, 0)).perform(click())
      intended(hasComponent(AdminAuthActivity::class.java.name))
      intended(hasExtra(AdminAuthActivity.getIntentKey(), 1))
      onView(allOf(instanceOf(TextView::class.java), withParent(withId(R.id.admin_auth_toolbar))))
        .check(matches(withText(context.resources.getString(R.string.add_profile_title))))
      onView(withText(context.resources.getString(R.string.admin_auth_heading))).check(matches(isDisplayed()))
      onView(withText(context.resources.getString(R.string.admin_auth_sub))).check(matches(isDisplayed()))
      onView(isRoot()).perform(pressBack())
      onView(withId(R.id.administrator_controls_linear_layout)).check(matches(isDisplayed()))

      onView(withId(R.id.profile_recycler_view)).perform(scrollToPosition<RecyclerView.ViewHolder>(0))

      onView(atPositionOnView(R.id.profile_recycler_view, 0, R.id.profile_last_visited)).check(matches(
        isDisplayed()))

      onView(atPositionOnView(R.id.profile_recycler_view, 0, R.id.profile_last_visited)).check(matches(withText( String.format(
        getResources().getString(R.string.profile_last_used) + " " + getTimeAgo(
          profileManagementController.getUpdateLastLoggedInTimeAsyncForTest(
            ProfileId.newBuilder().setInternalId(0).build(),1579677300000),
          ApplicationProvider.getApplicationContext<Context>()
        )
      ))))
    }
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testProfileChooserFragment_addManyProfiles_checkProfilesSortedAndNoAddProfile() {
    profileTestHelper.initializeProfiles()
    profileTestHelper.addMoreProfiles(8)
    ActivityScenario.launch(ProfileActivity::class.java).use {
      onView(withId(R.id.profile_recycler_view)).perform(scrollToPosition<RecyclerView.ViewHolder>(0))
      onView(atPositionOnView(R.id.profile_recycler_view, 0, R.id.profile_name_text)).check(matches(withText("Sean")))
      onView(withId(R.id.profile_recycler_view)).perform(scrollToPosition<RecyclerView.ViewHolder>(1))
      onView(atPositionOnView(R.id.profile_recycler_view, 1, R.id.profile_name_text)).check(matches(withText("A")))
      onView(withId(R.id.profile_recycler_view)).perform(scrollToPosition<RecyclerView.ViewHolder>(2))
      onView(atPositionOnView(R.id.profile_recycler_view, 2, R.id.profile_name_text)).check(matches(withText("B")))
      onView(withId(R.id.profile_recycler_view)).perform(scrollToPosition<RecyclerView.ViewHolder>(3))
      onView(atPositionOnView(R.id.profile_recycler_view, 3, R.id.profile_name_text)).check(matches(withText("Ben")))
      onView(withId(R.id.profile_recycler_view)).perform(scrollToPosition<RecyclerView.ViewHolder>(4))
      onView(atPositionOnView(R.id.profile_recycler_view, 4, R.id.profile_name_text)).check(matches(withText("C")))
      onView(withId(R.id.profile_recycler_view)).perform(scrollToPosition<RecyclerView.ViewHolder>(5))
      onView(atPositionOnView(R.id.profile_recycler_view, 5, R.id.profile_name_text)).check(matches(withText("D")))
      onView(withId(R.id.profile_recycler_view)).perform(scrollToPosition<RecyclerView.ViewHolder>(6))
      onView(atPositionOnView(R.id.profile_recycler_view, 6, R.id.profile_name_text)).check(matches(withText("E")))
      onView(withId(R.id.profile_recycler_view)).perform(scrollToPosition<RecyclerView.ViewHolder>(7))
      onView(atPositionOnView(R.id.profile_recycler_view, 7, R.id.profile_name_text)).check(matches(withText("F")))
      onView(withId(R.id.profile_recycler_view)).perform(scrollToPosition<RecyclerView.ViewHolder>(8))
      onView(atPositionOnView(R.id.profile_recycler_view, 8, R.id.profile_name_text)).check(matches(withText("G")))
      onView(withId(R.id.profile_recycler_view)).perform(scrollToPosition<RecyclerView.ViewHolder>(9))
      onView(atPositionOnView(R.id.profile_recycler_view, 9, R.id.profile_name_text)).check(matches(withText("H")))
    }
  }

  @Test
  fun testProfileChooserFragment_clickProfile_checkOpensPinPasswordActivity() {
    profileTestHelper.initializeProfiles()
    ActivityScenario.launch(ProfileActivity::class.java).use {
      onView(atPosition(R.id.profile_recycler_view, 0)).perform(click())
      intended(hasComponent(PinPasswordActivity::class.java.name))
    }
  }

  @Test
  fun testProfileChooserFragment_clickAddProfile_checkOpensAdminAuthActivity_onBackButton_opensProfileChooserFragment() {
    profileTestHelper.initializeProfiles()
    ActivityScenario.launch<ProfileActivity>(createProfileActivityIntent()) .use {
      onView(atPosition(R.id.profile_recycler_view, 2)).perform(click())
      intended(hasComponent(AdminAuthActivity::class.java.name))
      intended(hasExtra(AdminAuthActivity.getIntentKey(), 1))
      onView(allOf(instanceOf(TextView::class.java), withParent(withId(R.id.admin_auth_toolbar))))
        .check(matches(withText(context.resources.getString(R.string.add_profile_title))))
      onView(withText(context.resources.getString(R.string.admin_auth_heading))).check(matches(isDisplayed()))
      onView(withText(context.resources.getString(R.string.admin_auth_sub))).check(matches(isDisplayed()))
      onView(isRoot()).perform(pressBack())
      onView(withId(R.id.administrator_controls_linear_layout)).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testProfileChooserFragment_clickAdminControls_checkOpensAdminAuthActivity_onBackButton_opensProfileChooserFragment() {
    profileTestHelper.initializeProfiles()
    ActivityScenario.launch<ProfileActivity>(createProfileActivityIntent()) .use {
      onView(withId(R.id.administrator_controls_linear_layout)).perform(click())
      intended(hasComponent(AdminAuthActivity::class.java.name))
      intended(hasExtra(AdminAuthActivity.getIntentKey(), 0))
      onView(allOf(instanceOf(TextView::class.java), withParent(withId(R.id.admin_auth_toolbar))))
        .check(matches(withText(context.resources.getString(R.string.administrator_controls))))
      onView(withText(context.resources.getString(R.string.admin_auth_heading))).check(matches(isDisplayed()))
      onView(withText(context.resources.getString(R.string.admin_auth_admin_controls_sub))).check(matches(isDisplayed()))
      onView(isRoot()).perform(pressBack())
      onView(withId(R.id.administrator_controls_linear_layout)).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testProfileChooserFragment_clickAdminProfileWithNoPin_checkOpensAdminPinActivity() {
    profileManagementController.addProfile("Sean", "", null, true, -10710042, true, StoryTextSize.SMALL_TEXT_SIZE, AppLanguage.ENGLISH_APP_LANGUAGE, AudioLanguage.NO_AUDIO)
    ActivityScenario.launch<ProfileActivity>(createProfileActivityIntent()) .use {
      onView(atPosition(R.id.profile_recycler_view, 1)).perform(click())
      intended(hasComponent(AdminPinActivity::class.java.name))
    }
  }

  @Test
  fun testProfileChooserFragment_clickAdminControlsWithNoPin_checkOpensAdminPinActivity() {
    profileManagementController.addProfile("Sean", "", null, true, -10710042, true, StoryTextSize.SMALL_TEXT_SIZE, AppLanguage.ENGLISH_APP_LANGUAGE, AudioLanguage.NO_AUDIO)
    ActivityScenario.launch<ProfileActivity>(createProfileActivityIntent()) .use {
      onView(withId(R.id.administrator_controls_linear_layout)).perform(click())
      intended(hasComponent(AdminPinActivity::class.java.name))
    }
  }

  private fun createProfileActivityIntent(): Intent {
    return ProfileActivity.createProfileActivity(
      ApplicationProvider.getApplicationContext())
  }

  private fun getResources(): Resources {
    return ApplicationProvider.getApplicationContext<Context>().resources
  }

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

    fun inject(profileChooserFragmentTest: ProfileChooserFragmentTest)
  }
}
