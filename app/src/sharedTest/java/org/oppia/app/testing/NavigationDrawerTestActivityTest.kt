package org.oppia.app.testing

import android.app.Application
import android.content.Context
import android.content.Intent
import android.widget.TextView
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.DrawerActions.close
import androidx.test.espresso.contrib.DrawerActions.open
import androidx.test.espresso.contrib.DrawerMatchers.isClosed
import androidx.test.espresso.contrib.DrawerMatchers.isOpen
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
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
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.R
import org.oppia.app.recyclerview.RecyclerViewMatcher
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

/** Tests for [NavigationDrawerTestActivity]. */
@RunWith(AndroidJUnit4::class)
class NavigationDrawerTestActivityTest {

  @Inject lateinit var profileTestHelper: ProfileTestHelper
  @Inject lateinit var context: Context

  @Before
  @ExperimentalCoroutinesApi
  fun setUp() {
    Intents.init()
    setUpTestApplicationComponent()
    profileTestHelper.initializeProfiles()
  }

  @After
  fun tearDown() {
    Intents.release()
  }

  private fun createNavigationDrawerActivityIntent(profileId: Int): Intent {
    return NavigationDrawerTestActivity.createNavigationDrawerTestActivity(
      ApplicationProvider.getApplicationContext(),
      profileId
    )
  }

  private fun setUpTestApplicationComponent() {
    DaggerNavigationDrawerTestActivityTest_TestApplicationComponent.builder()
      .setApplication(ApplicationProvider.getApplicationContext())
      .build()
      .inject(this)
  }

  @Test
  fun testNavigationDrawerTestActivity_clickNavigationDrawerHamburger_defaultProfileNameAtIndex0_displayProfileNameSuccessfully() {
    launch<NavigationDrawerTestActivity>(createNavigationDrawerActivityIntent(0)).use {
      onView(withContentDescription(R.string.drawer_open_content_description)).check(
        matches(isCompletelyDisplayed())
      ).perform(click())
      onView(withId(R.id.nav_header_profile_name))
        .check(matches(withText("Sean")))
    }
  }
  @Test
  fun testNavigationDrawerTestActivity_clickNavigationDrawerHamburger_defaultProfileNameAtIndex1_displayProfileNameSuccessfully() {
    launch<NavigationDrawerTestActivity>(createNavigationDrawerActivityIntent(1)).use {
      onView(withContentDescription(R.string.drawer_open_content_description)).check(
        matches(isCompletelyDisplayed())
      ).perform(click())
      onView(withId(R.id.nav_header_profile_name))
        .check(matches(withText("Ben")))
    }
  }

  @Test
  fun testNavigationDrawerTestActivity_clickNavigationDrawerHamburger_navigationDrawerIsOpenedSuccessfully() {
    launch(NavigationDrawerTestActivity::class.java).use {
      onView(withContentDescription(R.string.drawer_open_content_description)).check(
        matches(isCompletelyDisplayed())
      ).perform(click())
      onView(withId(R.id.home_fragment_placeholder))
        .check(matches(isCompletelyDisplayed()))
      onView(withId(R.id.home_activity_drawer_layout)).check(matches(isOpen()))
    }
  }

  @Test
  fun testNavigationDrawerTestActivity_openNavigationDrawerAndRotate_navigationDrawerIsNotClosedAfterRotationIsVerifiedSucessfully() {
    launch(NavigationDrawerTestActivity::class.java).use {
      onView(withContentDescription(R.string.drawer_open_content_description)).perform(click())
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.home_activity_drawer_layout)).check(matches(isOpen()))
    }
  }

  @Test
  fun testNavigationDrawerTestActivity_openNavigationDrawerAndClose_closingOfNavigationDrawerIsVerifiedSucessfully() {
    launch(NavigationDrawerTestActivity::class.java).use {
      onView(withContentDescription(R.string.drawer_open_content_description)).perform(click())
      onView(withId(R.id.home_activity_drawer_layout)).perform(close())
      onView(withId(R.id.home_activity_drawer_layout)).check(matches(isClosed()))
    }
  }

  @Test
  fun testNavigationDrawerTestActivity_openNavigationDrawer_selectHelpMenuInNavigationDrawer_showsHelpFragmentSuccessfully() {
    launch(NavigationDrawerTestActivity::class.java).use {
      onView(withId(R.id.home_activity_drawer_layout)).perform(open())
      onView(withText(R.string.menu_help)).perform(click())
      onView(
        allOf(
          instanceOf(TextView::class.java),
          withParent(withId(R.id.help_activity_toolbar))
        )
      ).check(matches(withText(R.string.menu_help)))
    }
  }

  @Test
  fun testNavigationDrawerTestActivity_openNavigationDrawer_selectHelpMenuInNavigationDrawer_clickNavigationDrawerHamburger_navigationDrawerIsOpenedAndVerifiedSuccessfully() {
    launch(NavigationDrawerTestActivity::class.java).use {
      onView(withId(R.id.home_activity_drawer_layout)).perform(open())
      onView(withText(R.string.menu_help)).perform(click())
      onView(
        allOf(
          instanceOf(TextView::class.java),
          withParent(withId(R.id.help_activity_toolbar))
        )
      ).check(matches(withText(R.string.menu_help)))
      onView(withContentDescription(R.string.drawer_open_content_description)).check(
        matches(isCompletelyDisplayed())
      ).perform(click())
      onView(withId(R.id.help_activity_drawer_layout))
    }
  }

  @Test
  fun testNavigationDrawerTestActivity_openNavigationDrawer_selectHelpMenuInNavigationDrawer_openingAndClosingOfDrawerIsVerifiedSuccessfully() {
    launch(NavigationDrawerTestActivity::class.java).use {
      onView(withId(R.id.home_activity_drawer_layout)).perform(open())
      onView(withText(R.string.menu_help)).perform(click())
      onView(withContentDescription(R.string.drawer_open_content_description))
        .perform(click())
      onView(withId(R.id.help_activity_drawer_layout)).perform(close())
      onView(withId(R.id.help_activity_drawer_layout)).check(matches(isClosed()))
      onView(
        allOf(
          instanceOf(TextView::class.java),
          withParent(withId(R.id.help_activity_toolbar))
        )
      ).check(matches(withText(R.string.menu_help)))
      onView(withId(R.id.help_activity_drawer_layout)).perform(open())
      onView(withId(R.id.help_activity_drawer_layout)).check(matches(isOpen()))
    }
  }

  @Test
  fun testNavigationDrawerTestActivity_openNavigationDrawer_selectHelpMenuInNavigationDrawer_navigationDrawerClosingIsVerifiedSuccessfully() {
    launch(NavigationDrawerTestActivity::class.java).use {
      onView(withId(R.id.home_activity_drawer_layout)).perform(open())
      onView(withText(R.string.menu_help)).perform(click())
      onView(withId(R.id.help_activity_drawer_layout)).perform(open())
      onView(
        allOf(
          instanceOf(TextView::class.java),
          withParent(withId(R.id.help_activity_toolbar))
        )
      ).check(matches(withText(R.string.menu_help)))
      onView(withId(R.id.help_activity_drawer_layout)).perform(close())
      onView(withId(R.id.help_activity_drawer_layout)).check(matches(isClosed()))
    }
  }

  @Test
  fun testNavigationDrawerTestActivity_openNavigationDrawer_selectHelpMenuInNavigationDrawer_selectHomeMenuInNavigationDrawer_showsHomeFragmentSuccessfully() {
    launch(NavigationDrawerTestActivity::class.java).use {
      onView(withId(R.id.home_activity_drawer_layout)).perform(open())
      onView(withText(R.string.menu_help)).perform(click())
      onView(withId(R.id.help_activity_drawer_layout)).perform(open())
      onView(withText(R.string.menu_home)).perform(click())
      onView(
        allOf(
          instanceOf(TextView::class.java),
          withParent(withId(R.id.home_activity_toolbar))
        )
      ).check(matches(withText(R.string.menu_home)))
      onView(
        RecyclerViewMatcher.atPositionOnView(
          R.id.home_recycler_view,
          0,
          R.id.welcome_text_view
        )
      ).check(matches(withText("Welcome to Oppia!")))
    }
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

    fun inject(navigationDrawerTestActivityTest: NavigationDrawerTestActivityTest)
  }
}
