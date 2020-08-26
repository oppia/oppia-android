package org.oppia.app.settings.profile

import android.app.Application
import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
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
import org.hamcrest.Matchers.not
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.R
import org.oppia.app.recyclerview.RecyclerViewMatcher.Companion.atPosition
import org.oppia.app.recyclerview.RecyclerViewMatcher.Companion.atPositionOnView
import org.oppia.app.utility.OrientationChangeAction.Companion.orientationLandscape
import org.oppia.domain.oppialogger.LogStorageModule
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
class ProfileListFragmentTest {

  @Inject
  lateinit var context: Context

  @Inject
  lateinit var profileTestHelper: ProfileTestHelper

  @Before
  fun setUp() {
    Intents.init()
    setUpTestApplicationComponent()
    FirebaseApp.initializeApp(context)
  }

  @After
  fun tearDown() {
    Intents.release()
  }

  private fun setUpTestApplicationComponent() {
    DaggerProfileListFragmentTest_TestApplicationComponent.builder()
      .setApplication(ApplicationProvider.getApplicationContext())
      .build()
      .inject(this)
  }

  @Test
  fun testProfileListFragment_initializeProfiles_checkProfilesAreShown() {
    profileTestHelper.initializeProfiles()
    launch(ProfileListActivity::class.java).use {
      onView(withId(R.id.profile_list_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          0
        )
      )
      onView(
        atPositionOnView(R.id.profile_list_recycler_view, 0, R.id.profile_list_name)
      ).check(
        matches(withText("Admin"))
      )
      onView(
        atPositionOnView(R.id.profile_list_recycler_view, 0, R.id.profile_list_admin_text)
      ).check(
        matches(withText(context.getString(R.string.profile_chooser_admin)))
      )
      onView(withId(R.id.profile_list_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          1
        )
      )
      onView(
        atPositionOnView(R.id.profile_list_recycler_view, 1, R.id.profile_list_name)
      ).check(
        matches(withText("Ben"))
      )
      onView(
        atPositionOnView(R.id.profile_list_recycler_view, 1, R.id.profile_list_admin_text)
      ).check(
        matches(not(isDisplayed()))
      )
    }
  }

  @Test
  fun testProfileListFragment_initializeProfiles_changeConfiguration_checkProfilesAreShown() {
    profileTestHelper.initializeProfiles()
    launch(ProfileListActivity::class.java).use {
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.profile_list_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          0
        )
      )
      onView(
        atPositionOnView(R.id.profile_list_recycler_view, 0, R.id.profile_list_name)
      ).check(
        matches(withText("Admin"))
      )
      onView(
        atPositionOnView(R.id.profile_list_recycler_view, 0, R.id.profile_list_admin_text)
      ).check(
        matches(withText(context.getString(R.string.profile_chooser_admin)))
      )
      onView(withId(R.id.profile_list_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          1
        )
      )
      onView(
        atPositionOnView(R.id.profile_list_recycler_view, 1, R.id.profile_list_name)
      ).check(
        matches(withText("Ben"))
      )
      onView(
        atPositionOnView(R.id.profile_list_recycler_view, 1, R.id.profile_list_admin_text)
      ).check(
        matches(not(isDisplayed()))
      )
    }
  }

  @Test
  fun testProfileListFragment_addManyProfiles_checkProfilesAreSorted() {
    profileTestHelper.initializeProfiles()
    profileTestHelper.addMoreProfiles(5)
    launch(ProfileListActivity::class.java).use {
      onView(withId(R.id.profile_list_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          0
        )
      )
      onView(
        atPositionOnView(R.id.profile_list_recycler_view, 0, R.id.profile_list_name)
      ).check(
        matches(withText("Admin"))
      )
      onView(withId(R.id.profile_list_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          1
        )
      )
      onView(
        atPositionOnView(R.id.profile_list_recycler_view, 1, R.id.profile_list_name)
      ).check(
        matches(withText("A"))
      )
      onView(withId(R.id.profile_list_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          2
        )
      )
      onView(
        atPositionOnView(R.id.profile_list_recycler_view, 2, R.id.profile_list_name)
      ).check(
        matches(withText("B"))
      )
      onView(withId(R.id.profile_list_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          3
        )
      )
      onView(
        atPositionOnView(R.id.profile_list_recycler_view, 3, R.id.profile_list_name)
      ).check(
        matches(withText("Ben"))
      )
      onView(withId(R.id.profile_list_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          4
        )
      )
      onView(
        atPositionOnView(R.id.profile_list_recycler_view, 4, R.id.profile_list_name)
      ).check(
        matches(withText("C"))
      )
      onView(withId(R.id.profile_list_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          5
        )
      )
      onView(
        atPositionOnView(R.id.profile_list_recycler_view, 5, R.id.profile_list_name)
      ).check(
        matches(withText("D"))
      )
      onView(withId(R.id.profile_list_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          6
        )
      )
      onView(
        atPositionOnView(R.id.profile_list_recycler_view, 6, R.id.profile_list_name)
      ).check(
        matches(withText("E"))
      )
    }
  }

  @Test
  fun testProfileListFragment_initializeProfile_clickProfile_checkOpensProfileEditActivity() {
    profileTestHelper.initializeProfiles()
    launch(ProfileListActivity::class.java).use {
      onView(atPosition(R.id.profile_list_recycler_view, 0)).perform(click())
      intended(hasComponent(ProfileEditActivity::class.java.name))
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
  @Component(
    modules = [
      TestModule::class, TestLogReportingModule::class, LogStorageModule::class,
      TestDispatcherModule::class
    ]
  )
  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun inject(profileListFragmentTest: ProfileListFragmentTest)
  }
}
