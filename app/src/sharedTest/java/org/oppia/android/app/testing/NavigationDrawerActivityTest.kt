package org.oppia.android.app.testing

import android.app.Application
import android.content.Context
import android.content.Intent
import android.view.View
import android.view.ViewParent
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.NestedScrollView
import androidx.drawerlayout.widget.DrawerLayout
import androidx.test.core.app.ActivityScenario
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
import androidx.test.espresso.contrib.DrawerMatchers.isClosed
import androidx.test.espresso.contrib.DrawerMatchers.isOpen
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.espresso.util.HumanReadables
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.Component
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.not
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityComponent
import org.oppia.android.app.administratorcontrols.AdministratorControlsActivity
import org.oppia.android.app.application.ActivityComponentFactory
import org.oppia.android.app.application.ApplicationComponent
import org.oppia.android.app.application.ApplicationInjector
import org.oppia.android.app.application.ApplicationInjectorProvider
import org.oppia.android.app.application.ApplicationModule
import org.oppia.android.app.application.ApplicationStartupListenerModule
import org.oppia.android.app.help.HelpActivity
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.mydownloads.MyDownloadsActivity
import org.oppia.android.app.player.state.hintsandsolution.HintsAndSolutionConfigModule
import org.oppia.android.app.profile.ProfileChooserActivity
import org.oppia.android.app.profileprogress.ProfileProgressActivity
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
import org.oppia.android.domain.topic.StoryProgressTestHelper
import org.oppia.android.testing.OppiaTestRule
import org.oppia.android.testing.RobolectricModule
import org.oppia.android.testing.RunOn
import org.oppia.android.testing.TestAccessibilityModule
import org.oppia.android.testing.TestCoroutineDispatchers
import org.oppia.android.testing.TestDispatcherModule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.TestPlatform
import org.oppia.android.testing.profile.ProfileTestHelper
import org.oppia.android.util.caching.testing.CachingTestModule
import org.oppia.android.util.gcsresource.GcsResourceModule
import org.oppia.android.util.logging.LoggerModule
import org.oppia.android.util.logging.firebase.FirebaseLogUploaderModule
import org.oppia.android.util.parser.GlideImageLoaderModule
import org.oppia.android.util.parser.HtmlParserEntityTypeModule
import org.oppia.android.util.parser.ImageParsingModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [NavigationDrawerTestActivity]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(
  application = NavigationDrawerActivityTest.TestApplication::class,
  qualifiers = "port-xxhdpi"
)
class NavigationDrawerActivityTest {

  @get:Rule
  val oppiaTestRule = OppiaTestRule()

  @Inject
  lateinit var profileTestHelper: ProfileTestHelper

  @Inject
  lateinit var storyProfileTestHelper: StoryProgressTestHelper

  @Inject
  lateinit var context: Context

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  private val internalProfileId = 0
  private val internalProfileId1 = 1

  @Before
  fun setUp() {
    Intents.init()
    setUpTestApplicationComponent()
    profileTestHelper.initializeProfiles()
    testCoroutineDispatchers.registerIdlingResource()
    storyProfileTestHelper.markFullStoryPartialTopicProgressForRatios(
      ProfileId.newBuilder().setInternalId(
        internalProfileId
      ).build(),
      timestampOlderThanOneWeek = false
    )
  }

  @After
  fun tearDown() {
    testCoroutineDispatchers.unregisterIdlingResource()
    Intents.release()
  }

  private fun createNavigationDrawerActivityIntent(internalProfileId: Int): Intent {
    return NavigationDrawerTestActivity.createNavigationDrawerTestActivity(
      context,
      internalProfileId
    )
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  @Test
  fun testNavigationDrawerTestActivity_openNavDrawer_navDrawerIsOpened() {
    launch(NavigationDrawerTestActivity::class.java).use {
      it.openNavigationDrawer()
      onView(withId(R.id.home_fragment_placeholder)).check(matches(isCompletelyDisplayed()))
      onView(withId(R.id.home_activity_drawer_layout)).check(matches(isOpen()))
    }
  }

  @Test
  fun testNavigationDrawerTestActivity_openNavDrawer_changeConfig_navDrawerIsDisplayed() {
    launch(NavigationDrawerTestActivity::class.java).use {
      it.openNavigationDrawer()
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.home_activity_drawer_layout)).check(matches(isOpen()))
    }
  }

  @Test
  fun testNavigationDrawerTestActivity_withAdminProfile_openNavDrawer_profileNameIsDisplayed() {
    launch<NavigationDrawerTestActivity>(
      createNavigationDrawerActivityIntent(
        internalProfileId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      it.openNavigationDrawer()
      onView(
        allOf(
          withId(R.id.nav_header_profile_name),
          isDescendantOfA(withId(R.id.header_linear_layout))
        )
      ).check(matches(withText("Admin")))
    }
  }

  @Test
  fun testNavigationDrawerTestActivity_withAdminProfile_changeConfig_profileNameIsDisplayed() {
    launch<NavigationDrawerTestActivity>(
      createNavigationDrawerActivityIntent(
        internalProfileId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      it.openNavigationDrawer()
      onView(isRoot()).perform(orientationLandscape())
      onView(
        allOf(
          withId(R.id.nav_header_profile_name),
          isDescendantOfA(withId(R.id.header_linear_layout))
        )
      ).check(matches(withText("Admin")))
    }
  }

  @Test
  fun testNavigationDrawerTestActivity_openNavDrawer_profileProgressIsDisplayed() {
    launch<NavigationDrawerTestActivity>(
      createNavigationDrawerActivityIntent(
        internalProfileId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      it.openNavigationDrawer()
      onView(
        allOf(
          withId(R.id.profile_progress_text_view),
          isDescendantOfA(withId(R.id.header_linear_layout))
        )
      ).check(matches(withText("1 Story Completed | 1 Topic in Progress")))
    }
  }

  @Test
  fun testNavigationDrawerTestActivity_withUserProfile_openNavDrawer_profileNameIsDisplayed() {
    launch<NavigationDrawerTestActivity>(
      createNavigationDrawerActivityIntent(
        internalProfileId1
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      it.openNavigationDrawer()
      onView(
        allOf(
          withId(R.id.nav_header_profile_name),
          isDescendantOfA(withId(R.id.header_linear_layout))
        )
      ).check(matches(withText("Ben")))
    }
  }

  @Test
  fun testNavigationDrawerTestActivity_clickOnHeader_opensProfileProgressActivity() {
    launch<NavigationDrawerTestActivity>(
      createNavigationDrawerActivityIntent(
        internalProfileId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      it.openNavigationDrawer()
      onView(withId(R.id.nav_header_profile_name)).perform(click())
      testCoroutineDispatchers.runCurrent()
      intended(hasComponent(ProfileProgressActivity::class.java.name))
      intended(
        hasExtra(
          ProfileProgressActivity.PROFILE_PROGRESS_ACTIVITY_PROFILE_ID_KEY,
          internalProfileId
        )
      )
    }
  }

  @Test
  fun testNavigationDrawerTestActivity_withAdminProfile_openNavDrawer_adminControlsIsDisplayed() {
    launch<NavigationDrawerTestActivity>(
      createNavigationDrawerActivityIntent(internalProfileId)
    ).use {
      it.openNavigationDrawer()
      onView(withId(R.id.administrator_controls_linear_layout)).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testNavigationDrawerTestActivity_withAdminProfile_changeConfig_adminControlsIsDisplayed() {
    launch<NavigationDrawerTestActivity>(
      createNavigationDrawerActivityIntent(internalProfileId)
    ).use {
      it.openNavigationDrawer()
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.drawer_nested_scroll_view)).perform(swipeUp())
      onView(withId(R.id.administrator_controls_linear_layout)).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testNavigationDrawerTestActivity_withAdminProfile_adminMenu_opensAdminControlsActivity() {
    launch<NavigationDrawerTestActivity>(
      createNavigationDrawerActivityIntent(
        internalProfileId
      )
    ).use {
      it.openNavigationDrawer()
      onView(withId(R.id.administrator_controls_linear_layout)).perform(nestedScrollTo())
        .check(matches(isDisplayed())).perform(click())
      intended(hasComponent(AdministratorControlsActivity::class.java.name))
      intended(hasExtra(AdministratorControlsActivity.getIntentKey(), 0))
    }
  }

  @Test
  fun testNavigationDrawerTestActivity_withUserProfile_adminControlsAreNotDisplayed() {
    launch<NavigationDrawerTestActivity>(
      createNavigationDrawerActivityIntent(
        internalProfileId1
      )
    ).use {
      it.openNavigationDrawer()
      onView(withId(R.id.administrator_controls_linear_layout))
        .check(matches(not(isDisplayed())))
    }
  }

  // TODO(#1806): Enable this once lowfi implementation is done.
  @Test
  @Ignore("My Downloads is removed until we have full download support.")
  fun testNavigationDrawerTestActivity_myDownloadsMenu_myDownloadsFragmentIsDisplayed() {
    launch(NavigationDrawerTestActivity::class.java).use {
      it.openNavigationDrawer()
      onView(withText(R.string.menu_my_downloads)).perform(click())
      intended(hasComponent(MyDownloadsActivity::class.java.name))
    }
  }

  @Test
  fun testNavigationDrawerTestActivity_switchProfileMenu_showsExitToProfileChooserDialog() {
    launch(NavigationDrawerTestActivity::class.java).use {
      it.openNavigationDrawer()
      onView(withText(R.string.menu_switch_profile)).perform(click())
      onView(withText(R.string.home_activity_back_dialog_message))
        .inRoot(isDialog())
        .check(matches(isDisplayed()))
    }
  }

  @Test
  fun testNavigationDrawerTestActivity_switchProfileMenu_clickExit_opensProfileChooserActivity() {
    launch(NavigationDrawerTestActivity::class.java).use {
      it.openNavigationDrawer()
      onView(withText(R.string.menu_switch_profile)).perform(click())
      onView(withText(R.string.home_activity_back_dialog_message))
        .inRoot(isDialog())
        .check(matches(isDisplayed()))
      onView(withText(R.string.home_activity_back_dialog_exit))
        .inRoot(isDialog())
        .perform(click())
      intended(hasComponent(ProfileChooserActivity::class.java.name))
    }
  }

  @RunOn(TestPlatform.ESPRESSO)
  @Test
  fun testNavigationDrawerTestActivity_openNavDrawerAndClose_navDrawerIsClosed() {
    launch(NavigationDrawerTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      it.openNavigationDrawer()
      onView(withId(R.id.home_activity_drawer_layout)).perform(close())
      onView(withId(R.id.home_activity_drawer_layout)).check(matches(isClosed()))
    }
  }

  @RunOn(TestPlatform.ESPRESSO)
  @Test
  fun testNavigationDrawerTestActivity_selectSwitchProfileMenu_clickCancel_navDrawerIsClosed() {
    launch(NavigationDrawerTestActivity::class.java).use {
      it.openNavigationDrawer()
      onView(withText(R.string.menu_switch_profile)).perform(click())
      onView(withText(R.string.home_activity_back_dialog_message))
        .inRoot(isDialog())
        .check(matches(isDisplayed()))
      onView(withText(R.string.home_activity_back_dialog_cancel))
        .inRoot(isDialog())
        .perform(click())
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.home_activity_drawer_layout)).check(matches(isClosed()))
    }
  }

  @Test
  fun testNavigationDrawerTestActivity_selectSwitchProfile_changeConfig_dialogIsVisible() {
    launch(NavigationDrawerTestActivity::class.java).use {
      it.openNavigationDrawer()
      onView(withText(R.string.menu_switch_profile)).perform(click())
      onView(withText(R.string.home_activity_back_dialog_message))
        .inRoot(isDialog())
        .check(matches(isDisplayed()))
      onView(isRoot()).perform(orientationLandscape())
      onView(withText(R.string.home_activity_back_dialog_message)).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testNavigationDrawerTestActivity_openNavDrawer_selectHelpMenu_opensHelpActivity() {
    launch(NavigationDrawerTestActivity::class.java).use {
      it.openNavigationDrawer()
      onView(withText(R.string.menu_help)).perform(click())
      intended(hasComponent(HelpActivity::class.java.name))
    }
  }

  private fun ActivityScenario<NavigationDrawerTestActivity>.openNavigationDrawer() {
    onView(withContentDescription(R.string.drawer_open_content_description))
      .check(matches(isCompletelyDisplayed()))
      .perform(click())

    // Force the drawer animation to start. See https://github.com/oppia/oppia-android/pull/2204 for
    // background context.
    onActivity { activity ->
      val drawerLayout =
        activity.findViewById<DrawerLayout>(R.id.home_activity_drawer_layout)
      // Note that this only initiates a single computeScroll() in Robolectric. Normally, Android
      // will compute several of these across multiple draw calls, but one seems sufficient for
      // Robolectric. Note that Robolectric is also *supposed* to handle the animation loop one call
      // to this method initiates in the view choreographer class, but it seems to not actually
      // flush the choreographer per observation. In Espresso, this method is automatically called
      // during draw (and a few other situations), but it's fine to call it directly once to kick it
      // off (to avoid disparity between Espresso/Robolectric runs of the tests).
      // NOTE TO DEVELOPERS: if this ever flakes, we can probably put this in a loop with fake time
      // adjustments to simulate the render loop.
      drawerLayout.computeScroll()
    }

    // Wait for the drawer to fully open (mostly for Espresso since Robolectric should synchronously
    // stabilize the drawer layout after the previous logic completes).
    testCoroutineDispatchers.runCurrent()
  }

  /** Functions nestedScrollTo() and findFirstParentLayoutOfClass() taken from: https://stackoverflow.com/a/46037284/8860848 */
  private fun nestedScrollTo(): ViewAction {
    return object : ViewAction {
      override fun getDescription(): String {
        return "View is not NestedScrollView"
      }

      override fun getConstraints(): org.hamcrest.Matcher<View> {
        return allOf(
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

  // TODO(#59): Figure out a way to reuse modules instead of needing to re-declare them.
  // TODO(#1675): Add NetworkModule once data module is migrated off of Moshi.
  @Singleton
  @Component(
    modules = [
      RobolectricModule::class,
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

    fun inject(navigationDrawerActivityTest: NavigationDrawerActivityTest)
  }

  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerNavigationDrawerActivityTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    fun inject(navigationDrawerActivityTest: NavigationDrawerActivityTest) {
      component.inject(navigationDrawerActivityTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }
}
