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
import org.oppia.app.recyclerview.RecyclerViewMatcher.Companion.atPosition
import org.oppia.app.recyclerview.RecyclerViewMatcher.Companion.atPositionOnView
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
class ProfileListActivityTest {

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
    DaggerProfileListActivityTest_TestApplicationComponent.builder()
      .setApplication(ApplicationProvider.getApplicationContext())
      .build()
      .inject(this)
  }

  @Test
  fun testProfileListActivity_initializeProfiles_checkProfilesAreShown() {
    profileTestHelper.initializeProfiles()
    launch(ProfileListActivity::class.java).use {
      onView(withId(R.id.profile_list_recycler_view)).perform(scrollToPosition<RecyclerView.ViewHolder>(0))
      onView(
        atPositionOnView(R.id.profile_list_recycler_view, 0, R.id.profile_list_name)
      ).check(
        matches(withText("Sean"))
      )
      onView(
        atPositionOnView(R.id.profile_list_recycler_view, 0, R.id.profile_list_admin_text)
      ).check(
        matches(withText(context.getString(R.string.profile_chooser_admin)))
      )
      onView(withId(R.id.profile_list_recycler_view)).perform(scrollToPosition<RecyclerView.ViewHolder>(1))
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
  fun testProfileListActivity_initializeProfiles_changeConfiguration_checkProfilesAreShown() {
    profileTestHelper.initializeProfiles()
    launch(ProfileListActivity::class.java).use {
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.profile_list_recycler_view)).perform(scrollToPosition<RecyclerView.ViewHolder>(0))
      onView(
        atPositionOnView(R.id.profile_list_recycler_view, 0, R.id.profile_list_name)
      ).check(
        matches(withText("Sean"))
      )
      onView(
        atPositionOnView(R.id.profile_list_recycler_view, 0, R.id.profile_list_admin_text)
      ).check(
        matches(withText(context.getString(R.string.profile_chooser_admin)))
      )
      onView(withId(R.id.profile_list_recycler_view)).perform(scrollToPosition<RecyclerView.ViewHolder>(1))
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
  fun testProfileListActivity_addManyProfiles_checkProfilesAreSorted() {
    profileTestHelper.initializeProfiles()
    profileTestHelper.addMoreProfiles(5)
    launch(ProfileListActivity::class.java).use {
      onView(withId(R.id.profile_list_recycler_view)).perform(scrollToPosition<RecyclerView.ViewHolder>(0))
      onView(
        atPositionOnView(R.id.profile_list_recycler_view, 0, R.id.profile_list_name)
      ).check(
        matches(withText("Sean"))
      )
      onView(withId(R.id.profile_list_recycler_view)).perform(scrollToPosition<RecyclerView.ViewHolder>(1))
      onView(
        atPositionOnView(R.id.profile_list_recycler_view, 1, R.id.profile_list_name)
      ).check(
        matches(withText("A"))
      )
      onView(withId(R.id.profile_list_recycler_view)).perform(scrollToPosition<RecyclerView.ViewHolder>(2))
      onView(
        atPositionOnView(R.id.profile_list_recycler_view, 2, R.id.profile_list_name)
      ).check(
        matches(withText("B"))
      )
      onView(withId(R.id.profile_list_recycler_view)).perform(scrollToPosition<RecyclerView.ViewHolder>(3))
      onView(
        atPositionOnView(R.id.profile_list_recycler_view, 3, R.id.profile_list_name)
      ).check(
        matches(withText("Ben"))
      )
      onView(withId(R.id.profile_list_recycler_view)).perform(scrollToPosition<RecyclerView.ViewHolder>(4))
      onView(
        atPositionOnView(R.id.profile_list_recycler_view, 4, R.id.profile_list_name)
      ).check(
        matches(withText("C"))
      )
      onView(withId(R.id.profile_list_recycler_view)).perform(scrollToPosition<RecyclerView.ViewHolder>(5))
      onView(
        atPositionOnView(R.id.profile_list_recycler_view, 5, R.id.profile_list_name)
      ).check(
        matches(withText("D"))
      )
      onView(withId(R.id.profile_list_recycler_view)).perform(scrollToPosition<RecyclerView.ViewHolder>(6))
      onView(
        atPositionOnView(R.id.profile_list_recycler_view, 6, R.id.profile_list_name)
      ).check(
        matches(withText("E"))
      )
    }
  }

  @Test
  fun testProfileListActivity_initializeProfile_clickProfile_checkOpensProfileEditActivity() {
    profileTestHelper.initializeProfiles()
    launch(ProfileListActivity::class.java).use {
      onView(atPosition(R.id.profile_list_recycler_view, 0)).perform(click())
      intended(hasComponent(ProfileEditActivity::class.java.name))
    }
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

    fun inject(profileListActivityTest: ProfileListActivityTest)
  }
}
