package org.oppia.app.testing

import android.app.Application
import android.content.Context
import android.content.Intent
import android.view.View
import android.view.ViewParent
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.PerformException
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.swipeUp
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.DrawerActions.close
import androidx.test.espresso.contrib.DrawerActions.open
import androidx.test.espresso.contrib.DrawerMatchers.isClosed
import androidx.test.espresso.contrib.DrawerMatchers.isOpen
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withParent
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.espresso.util.HumanReadables
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.FirebaseApp
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import org.hamcrest.Matchers
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.instanceOf
import org.hamcrest.Matchers.not
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.R
import org.oppia.app.administratorcontrols.AdministratorControlsActivity
import org.oppia.app.model.ProfileId
import org.oppia.app.mydownloads.MyDownloadsActivity
import org.oppia.app.profile.ProfileActivity
import org.oppia.app.recyclerview.RecyclerViewMatcher.Companion.atPositionOnView
import org.oppia.app.utility.OrientationChangeAction.Companion.orientationLandscape
import org.oppia.domain.oppialogger.LogStorageModule
import org.oppia.domain.topic.StoryProgressTestHelper
import org.oppia.testing.TestDispatcherModule
import org.oppia.testing.TestLogReportingModule
import org.oppia.testing.profile.ProfileTestHelper
import org.oppia.util.logging.EnableConsoleLog
import org.oppia.util.logging.EnableFileLog
import org.oppia.util.logging.GlobalLogLevel
import org.oppia.util.logging.LogLevel
import org.oppia.util.system.OppiaClock
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

// Time: Wed Apr 24 2019 08:22:00
private const val MORNING_TIMESTAMP = 1556094120000

/** Tests for [NavigationDrawerTestActivity]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
class NavigationDrawerTestActivityTest {

  @Inject
  lateinit var profileTestHelper: ProfileTestHelper

  @Inject
  lateinit var storyProfileTestHelper: StoryProgressTestHelper

  @Inject
  lateinit var context: Context

  private val internalProfileId = 0
  private val internalProfileId1 = 1
  private lateinit var oppiaClock: OppiaClock

  @Before
  fun setUp() {
    Intents.init()
    setUpTestApplicationComponent()
    profileTestHelper.initializeProfiles()
    FirebaseApp.initializeApp(context)
    storyProfileTestHelper.markFullStoryPartialTopicProgressForRatios(
      ProfileId.newBuilder().setInternalId(
        internalProfileId
      ).build(),
      timestampOlderThanAWeek = false
    )
  }

  @After
  fun tearDown() {
    Intents.release()
  }

  private fun getApplicationDependencies() {
    launch(HomeInjectionActivity::class.java).use {
      it.onActivity { activity ->
        oppiaClock = activity.oppiaClock
      }
    }
  }

  private fun createNavigationDrawerActivityIntent(internalProfileId: Int): Intent {
    return NavigationDrawerTestActivity.createNavigationDrawerTestActivity(
      ApplicationProvider.getApplicationContext(),
      internalProfileId
    )
  }

  private fun setUpTestApplicationComponent() {
    DaggerNavigationDrawerTestActivityTest_TestApplicationComponent.builder()
      .setApplication(ApplicationProvider.getApplicationContext())
      .build()
      .inject(this)
  }

  @Test
  fun testNavigationDrawerTestActivity_clickNavigationDrawerHamburger_defaultProfileNameAtIndex0_displayProfileNameSuccessfully() { // ktlint-disable max-line-length
    launch<NavigationDrawerTestActivity>(
      createNavigationDrawerActivityIntent(
        internalProfileId
      )
    ).use {
      onView(withContentDescription(R.string.drawer_open_content_description)).check(
        matches(isCompletelyDisplayed())
      ).perform(click())
      onView(withId(R.id.nav_header_profile_name))
        .check(matches(withText("Sean")))
    }
  }

  @Test
  fun testNavigationDrawerTestActivity_clickNavigationDrawerHamburger_changeConfiguration_defaultProfileNameAtIndex0_displayProfileNameSuccessfully() { // ktlint-disable max-line-length
    launch<NavigationDrawerTestActivity>(
      createNavigationDrawerActivityIntent(
        internalProfileId
      )
    ).use {
      onView(withContentDescription(R.string.drawer_open_content_description))
        .check(
          matches(
            isCompletelyDisplayed()
          )
        )
        .perform(click())
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.nav_header_profile_name)).check(matches(withText("Sean")))
    }
  }

  @Test
  fun testNavigationDrawerTestActivity_clickNavigationDrawerHamburger_checkProfileProgress_displayProfileProgressSuccessfully() { // ktlint-disable max-line-length
    launch<NavigationDrawerTestActivity>(
      createNavigationDrawerActivityIntent(
        internalProfileId
      )
    ).use {
      onView(withContentDescription(R.string.drawer_open_content_description))
        .check(
          matches(
            isCompletelyDisplayed()
          )
        )
        .perform(click())
      onView(withId(R.id.profile_progress_text_view))
        .check(
          matches(
            withText(
              "1 Story Completed | 1 Topic in Progress"
            )
          )
        )
    }
  }

  @Test
  fun testNavigationDrawerTestActivity_clickNavigationDrawerHamburger_defaultProfileNameAtIndex1_displayProfileNameSuccessfully() { // ktlint-disable max-line-length
    launch<NavigationDrawerTestActivity>(
      createNavigationDrawerActivityIntent(
        internalProfileId1
      )
    ).use {
      onView(withContentDescription(R.string.drawer_open_content_description))
        .check(
          matches(
            isCompletelyDisplayed()
          )
        )
        .perform(click())
      onView(withId(R.id.nav_header_profile_name)).check(matches(withText("Ben")))
    }
  }

  @Test
  fun testNavigationDrawerTestActivity_clickNavigationDrawerHamburger_navigationDrawerIsOpenedSuccessfully() { // ktlint-disable max-line-length
    launch(NavigationDrawerTestActivity::class.java).use {
      onView(withContentDescription(R.string.drawer_open_content_description))
        .check(
          matches(
            isCompletelyDisplayed()
          )
        )
        .perform(click())
      onView(withId(R.id.home_fragment_placeholder)).check(matches(isCompletelyDisplayed()))
      onView(withId(R.id.home_activity_drawer_layout)).check(matches(isOpen()))
    }
  }

  @Test
  fun testNavigationDrawerTestActivity_clickNavigationDrawerHamburger_changeConfiguration_navigationDrawerIsOpenedSuccessfully() { // ktlint-disable max-line-length
    launch(NavigationDrawerTestActivity::class.java).use {
      onView(withContentDescription(R.string.drawer_open_content_description))
        .check(
          matches(
            isCompletelyDisplayed()
          )
        )
        .perform(click())
      onView(withId(R.id.home_fragment_placeholder)).check(matches(isCompletelyDisplayed()))
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.home_activity_drawer_layout)).check(matches(isOpen()))
    }
  }

  @Test
  fun testNavigationDrawerTestActivity_openNavigationDrawerAndRotate_navigationDrawerIsNotClosedAfterRotationIsVerifiedSuccessfully() { // ktlint-disable max-line-length
    launch(NavigationDrawerTestActivity::class.java).use {
      onView(withContentDescription(R.string.drawer_open_content_description)).perform(click())
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.home_activity_drawer_layout)).check(matches(isOpen()))
    }
  }

  @Test
  fun testNavigationDrawerTestActivity_openNavigationDrawerAndClose_closingOfNavigationDrawerIsVerifiedSuccessfully() { // ktlint-disable max-line-length
    launch(NavigationDrawerTestActivity::class.java).use {
      onView(withContentDescription(R.string.drawer_open_content_description)).perform(click())
      onView(withId(R.id.home_activity_drawer_layout)).perform(close())
      onView(withId(R.id.home_activity_drawer_layout)).check(matches(isClosed()))
    }
  }

  @Test
  fun testNavigationDrawerTestActivity_withAdminProfile_openNavigationDrawer_checkAdministratorControlsDisplayed() { // ktlint-disable max-line-length
    launch<NavigationDrawerTestActivity>(createNavigationDrawerActivityIntent(0)).use {
      onView(withContentDescription(R.string.drawer_open_content_description)).perform(click())
      onView(withId(R.id.administrator_controls_linear_layout)).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testNavigationDrawerTestActivity_withAdminProfile_openNavigationDrawer_changeConfiguration_checkAdministratorControlsDisplayed() { // ktlint-disable max-line-length
    launch<NavigationDrawerTestActivity>(createNavigationDrawerActivityIntent(0)).use {
      onView(withContentDescription(R.string.drawer_open_content_description)).perform(click())
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.drawer_nested_scroll_view)).perform(swipeUp())
      onView(withId(R.id.administrator_controls_linear_layout)).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testNavigationDrawerTestActivity_withAdminProfile_openNavigationDrawer_clickAdministratorControls_checkOpensAdministratorControlsActivity() { // ktlint-disable max-line-length
    launch<NavigationDrawerTestActivity>(
      createNavigationDrawerActivityIntent(
        internalProfileId
      )
    ).use {
      onView(withContentDescription(R.string.drawer_open_content_description)).perform(click())
      onView(withId(R.id.administrator_controls_linear_layout)).perform(nestedScrollTo())
        .check(matches(isDisplayed())).perform(click())
      intended(hasComponent(AdministratorControlsActivity::class.java.name))
      intended(hasExtra(AdministratorControlsActivity.getIntentKey(), 0))
    }
  }

  @Test
  fun testNavigationDrawerTestActivity_withUserProfile_openNavigationDrawer_checkAdministratorControlsNotDisplayed() { // ktlint-disable max-line-length
    launch<NavigationDrawerTestActivity>(
      createNavigationDrawerActivityIntent(
        internalProfileId1
      )
    ).use {
      onView(withContentDescription(R.string.drawer_open_content_description)).perform(click())
      onView(withId(R.id.administrator_controls_linear_layout))
        .check(
          matches(
            not(
              isDisplayed()
            )
          )
        )
    }
  }

  @Test
  fun testNavigationDrawerTestActivity_openNavigationDrawer_selectHelpMenuInNavigationDrawer_showsHelpFragmentSuccessfully() { // ktlint-disable max-line-length
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
  fun testNavigationDrawerTestActivity_openNavigationDrawer_selectMyDownloadsMenuInNavigationDrawer_showsMyDownloadsFragmentSuccessfully() { // ktlint-disable max-line-length
    launch(NavigationDrawerTestActivity::class.java).use {
      onView(withId(R.id.home_activity_drawer_layout)).perform(open())
      onView(withText(R.string.menu_my_downloads)).perform(click())
      intended(hasComponent(MyDownloadsActivity::class.java.name))
    }
  }

  @Test
  fun testNavigationDrawerTestActivity_openNavigationDrawer_selectSwitchProfileMenu_showsExitToProfileChooserDialog() { // ktlint-disable max-line-length
    launch(NavigationDrawerTestActivity::class.java).use {
      onView(withId(R.id.home_activity_drawer_layout)).perform(open())
      onView(withText(R.string.menu_switch_profile)).perform(click())
      onView(withText(R.string.home_activity_back_dialog_message)).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testNavigationDrawerTestActivity_openNavigationDrawer_selectSwitchProfileMenu_showsExitToProfileChooserDialog_clickExit_checkOpensProfileActivity() { // ktlint-disable max-line-length
    launch(NavigationDrawerTestActivity::class.java).use {
      onView(withId(R.id.home_activity_drawer_layout)).perform(open())
      onView(withText(R.string.menu_switch_profile)).perform(click())
      onView(withText(R.string.home_activity_back_dialog_message)).check(matches(isDisplayed()))
      onView(withText(R.string.home_activity_back_dialog_exit)).perform(click())
      intended(hasComponent(ProfileActivity::class.java.name))
    }
  }

  @Test
  fun testNavigationDrawerTestActivity_openNavigationDrawer_selectSwitchProfileMenu_showsExitToProfileChooserDialog_clickCancel_checkDrawerIsClosed() { // ktlint-disable max-line-length
    launch(NavigationDrawerTestActivity::class.java).use {
      onView(withId(R.id.home_activity_drawer_layout)).perform(open())
      onView(withText(R.string.menu_switch_profile)).perform(click())
      onView(withText(R.string.home_activity_back_dialog_message)).check(matches(isDisplayed()))
      onView(withText(R.string.home_activity_back_dialog_cancel)).perform(click())
      onView(withId(R.id.home_activity_drawer_layout)).check(matches(isClosed()))
      onView(withId(R.id.home_activity_drawer_layout)).perform(open())
      onView(
        allOf(
          instanceOf(TextView::class.java),
          withParent(withId(R.id.home_activity_toolbar))
        )
      ).check(matches(withText(R.string.menu_home)))
    }
  }

  @Test
  fun testNavigationDrawerTestActivity_openNavigationDrawer_selectHelpMenuInNavigationDrawer_clickNavigationDrawerHamburger_navigationDrawerIsOpenedAndVerifiedSuccessfully() { // ktlint-disable max-line-length
    launch(NavigationDrawerTestActivity::class.java).use {
      onView(withId(R.id.home_activity_drawer_layout)).perform(open())
      onView(withText(R.string.menu_help)).perform(click())
      onView(
        allOf(instanceOf(TextView::class.java), withParent(withId(R.id.help_activity_toolbar)))
      ).check(matches(withText(R.string.menu_help)))
      onView(withContentDescription(R.string.drawer_open_content_description))
        .check(
          matches(
            isCompletelyDisplayed()
          )
        )
        .perform(click())
      onView(withId(R.id.help_activity_drawer_layout))
    }
  }

  @Test
  fun testNavigationDrawerTestActivity_openNavigationDrawer_selectHelpMenuInNavigationDrawer_openingAndClosingOfDrawerIsVerifiedSuccessfully() { // ktlint-disable max-line-length
    launch(NavigationDrawerTestActivity::class.java).use {
      onView(withId(R.id.home_activity_drawer_layout)).perform(open())
      onView(withText(R.string.menu_help)).perform(click())
      onView(withContentDescription(R.string.drawer_open_content_description)).perform(click())
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
  fun testNavigationDrawerTestActivity_openNavigationDrawer_selectHelpMenuInNavigationDrawer_navigationDrawerClosingIsVerifiedSuccessfully() { // ktlint-disable max-line-length
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
  fun testNavigationDrawerTestActivity_openNavigationDrawer_selectHelpMenuInNavigationDrawer_selectHomeMenuInNavigationDrawer_showsHomeFragmentSuccessfully() { // ktlint-disable max-line-length
    getApplicationDependencies()
    oppiaClock.setCurrentTimeMs(MORNING_TIMESTAMP)
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
      onView(withId(R.id.home_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          0
        )
      )
      onView(
        atPositionOnView(
          R.id.home_recycler_view,
          0,
          R.id.welcome_text_view
        )
      ).check(matches(withText("Good morning,")))
    }
  }

  /** Functions nestedScrollTo() and findFirstParentLayoutOfClass() taken from: https://stackoverflow.com/a/46037284/8860848 */
  private fun nestedScrollTo(): ViewAction {
    return object : ViewAction {
      override fun getDescription(): String {
        return "View is not NestedScrollView"
      }

      override fun getConstraints(): org.hamcrest.Matcher<View> {
        return Matchers.allOf(
          isDescendantOfA(isAssignableFrom(NestedScrollView::class.java))
        )
      }

      override fun perform(uiController: UiController, view: View) {
        try {
          val nestedScrollView =
            findFirstParentLayoutOfClass(view, NestedScrollView::class.java) as NestedScrollView
          nestedScrollView.scrollTo(0, view.getTop())
        } catch (e: Exception) {
          throw PerformException.Builder()
            .withActionDescription(this.description)
            .withViewDescription(HumanReadables.describe(view))
            .withCause(e)
            .build()
        }
        uiController.loopMainThreadUntilIdle()
      }
    }
  }

  private fun findFirstParentLayoutOfClass(view: View, parentClass: Class<out View>): View {
    var parent: ViewParent = FrameLayout(view.getContext())
    lateinit var incrementView: ViewParent
    var i = 0
    while (!(parent.javaClass === parentClass)) {
      if (i == 0) {
        parent = findParent(view)
      } else {
        parent = findParent(incrementView)
      }
      incrementView = parent
      i++
    }
    return parent as View
  }

  private fun findParent(view: View): ViewParent {
    return view.getParent()
  }

  private fun findParent(view: ViewParent): ViewParent {
    return view.getParent()
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
    TestModule::class, TestLogReportingModule::class, LogStorageModule::class,
    TestDispatcherModule::class
  ])
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
