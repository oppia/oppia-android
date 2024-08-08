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
import androidx.test.espresso.action.ViewActions.pressBack
import androidx.test.espresso.action.ViewActions.swipeUp
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.DrawerActions.close
import androidx.test.espresso.contrib.DrawerMatchers.isClosed
import androidx.test.espresso.contrib.DrawerMatchers.isOpen
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.hasTextColor
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
import com.google.android.material.navigation.NavigationView
import dagger.Component
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.not
import org.hamcrest.TypeSafeMatcher
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityComponent
import org.oppia.android.app.activity.ActivityComponentFactory
import org.oppia.android.app.activity.route.ActivityRouterModule
import org.oppia.android.app.administratorcontrols.AdministratorControlsActivity
import org.oppia.android.app.application.ApplicationComponent
import org.oppia.android.app.application.ApplicationInjector
import org.oppia.android.app.application.ApplicationInjectorProvider
import org.oppia.android.app.application.ApplicationModule
import org.oppia.android.app.application.ApplicationStartupListenerModule
import org.oppia.android.app.application.testing.TestingBuildFlavorModule
import org.oppia.android.app.devoptions.DeveloperOptionsModule
import org.oppia.android.app.drawer.NavigationDrawerItem
import org.oppia.android.app.help.HelpActivity
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.mydownloads.MyDownloadsActivity
import org.oppia.android.app.player.state.itemviewmodel.SplitScreenInteractionModule
import org.oppia.android.app.profile.ProfileChooserActivity
import org.oppia.android.app.profileprogress.ProfileProgressActivity
import org.oppia.android.app.shim.ViewBindingShimModule
import org.oppia.android.app.translation.testing.ActivityRecreatorTestModule
import org.oppia.android.app.utility.EspressoTestsMatchers.hasProtoExtra
import org.oppia.android.app.utility.OrientationChangeAction.Companion.orientationLandscape
import org.oppia.android.data.backends.gae.NetworkConfigProdModule
import org.oppia.android.data.backends.gae.NetworkModule
import org.oppia.android.domain.classify.InteractionsModule
import org.oppia.android.domain.classify.rules.algebraicexpressioninput.AlgebraicExpressionInputModule
import org.oppia.android.domain.classify.rules.continueinteraction.ContinueModule
import org.oppia.android.domain.classify.rules.dragAndDropSortInput.DragDropSortInputModule
import org.oppia.android.domain.classify.rules.fractioninput.FractionInputModule
import org.oppia.android.domain.classify.rules.imageClickInput.ImageClickInputModule
import org.oppia.android.domain.classify.rules.itemselectioninput.ItemSelectionInputModule
import org.oppia.android.domain.classify.rules.mathequationinput.MathEquationInputModule
import org.oppia.android.domain.classify.rules.multiplechoiceinput.MultipleChoiceInputModule
import org.oppia.android.domain.classify.rules.numberwithunits.NumberWithUnitsRuleModule
import org.oppia.android.domain.classify.rules.numericexpressioninput.NumericExpressionInputModule
import org.oppia.android.domain.classify.rules.numericinput.NumericInputRuleModule
import org.oppia.android.domain.classify.rules.ratioinput.RatioInputModule
import org.oppia.android.domain.classify.rules.textinput.TextInputRuleModule
import org.oppia.android.domain.exploration.ExplorationProgressModule
import org.oppia.android.domain.exploration.ExplorationStorageModule
import org.oppia.android.domain.hintsandsolution.HintsAndSolutionConfigModule
import org.oppia.android.domain.hintsandsolution.HintsAndSolutionProdModule
import org.oppia.android.domain.onboarding.ExpirationMetaDataRetrieverModule
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.domain.oppialogger.LoggingIdentifierModule
import org.oppia.android.domain.oppialogger.analytics.ApplicationLifecycleModule
import org.oppia.android.domain.oppialogger.analytics.CpuPerformanceSnapshotterModule
import org.oppia.android.domain.oppialogger.logscheduler.MetricLogSchedulerModule
import org.oppia.android.domain.oppialogger.loguploader.LogReportWorkerModule
import org.oppia.android.domain.platformparameter.PlatformParameterModule
import org.oppia.android.domain.platformparameter.PlatformParameterSingletonModule
import org.oppia.android.domain.question.QuestionModule
import org.oppia.android.domain.workmanager.WorkManagerConfigurationModule
import org.oppia.android.testing.OppiaTestRule
import org.oppia.android.testing.RunOn
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.TestPlatform
import org.oppia.android.testing.firebase.TestAuthenticationModule
import org.oppia.android.testing.junit.InitializeDefaultLocaleRule
import org.oppia.android.testing.profile.ProfileTestHelper
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.story.StoryProgressTestHelper
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClock
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.accessibility.AccessibilityTestModule
import org.oppia.android.util.caching.AssetModule
import org.oppia.android.util.caching.testing.CachingTestModule
import org.oppia.android.util.gcsresource.GcsResourceModule
import org.oppia.android.util.locale.LocaleProdModule
import org.oppia.android.util.logging.EventLoggingConfigurationModule
import org.oppia.android.util.logging.LoggerModule
import org.oppia.android.util.logging.SyncStatusModule
import org.oppia.android.util.logging.firebase.FirebaseLogUploaderModule
import org.oppia.android.util.networking.NetworkConnectionDebugUtilModule
import org.oppia.android.util.networking.NetworkConnectionUtilDebugModule
import org.oppia.android.util.parser.html.HtmlParserEntityTypeModule
import org.oppia.android.util.parser.image.GlideImageLoaderModule
import org.oppia.android.util.parser.image.ImageParsingModule
import org.oppia.android.util.profile.PROFILE_ID_INTENT_DECORATOR
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [NavigationDrawerTestActivity]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(
  application = NavigationDrawerActivityProdTest.TestApplication::class,
  qualifiers = "port-xxhdpi"
)
class NavigationDrawerActivityProdTest {
  @get:Rule
  val initializeDefaultLocaleRule = InitializeDefaultLocaleRule()

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

  @Inject
  lateinit var fakeOppiaClock: FakeOppiaClock

  private val internalProfileId = 0
  private val internalProfileId1 = 1

  @Before
  fun setUp() {
    Intents.init()
    setUpTestApplicationComponent()
    profileTestHelper.initializeProfiles()
    testCoroutineDispatchers.registerIdlingResource()
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
    storyProfileTestHelper.markCompletedRatiosStory0(
      ProfileId.newBuilder().setLoggedInInternalProfileId(
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
  fun testNavDrawer_openNavDrawer_navDrawerIsOpened() {
    launch<NavigationDrawerTestActivity>(
      createNavigationDrawerActivityIntent(internalProfileId)
    ).use {
      it.openNavigationDrawer()
      onView(withId(R.id.home_fragment_placeholder)).check(matches(isCompletelyDisplayed()))
      onView(withId(R.id.home_activity_drawer_layout)).check(matches(isOpen()))
    }
  }

  @Test
  fun testNavDrawer_openNavDrawer_configChange_navDrawerIsDisplayed() {
    launch<NavigationDrawerTestActivity>(
      createNavigationDrawerActivityIntent(internalProfileId)
    ).use {
      it.openNavigationDrawer()
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.home_activity_drawer_layout)).check(matches(isOpen()))
    }
  }

  @Test
  fun testNavDrawer_withAdminProfile_openNavDrawer_profileNameIsDisplayed() {
    launch<NavigationDrawerTestActivity>(
      createNavigationDrawerActivityIntent(internalProfileId)
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
  fun testNavDrawer_withAdminProfile_configChange_profileNameIsDisplayed() {
    launch<NavigationDrawerTestActivity>(
      createNavigationDrawerActivityIntent(internalProfileId)
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
  fun testNavDrawer_openNavDrawer_oneTopicInProgress_profileStoryProgressIsDisplayedCorrectly() {
    storyProfileTestHelper.markCompletedRatiosStory1Exp0(
      ProfileId.newBuilder().setLoggedInInternalProfileId(
        internalProfileId
      ).build(),
      timestampOlderThanOneWeek = false
    )
    launch<NavigationDrawerTestActivity>(
      createNavigationDrawerActivityIntent(internalProfileId)
    ).use {
      testCoroutineDispatchers.runCurrent()
      it.openNavigationDrawer()
      onView(
        allOf(
          withId(R.id.profile_story_progress_text_view),
          isDescendantOfA(withId(R.id.progress_linear_layout))
        )
      ).check(matches(withText("1 Story Completed")))
    }
  }

  @Test
  fun testNavDrawer_openNavDrawer_oneTopicInProgress_profileTopicProgressIsDisplayedCorrectly() {
    storyProfileTestHelper.markCompletedRatiosStory1Exp0(
      ProfileId.newBuilder().setLoggedInInternalProfileId(
        internalProfileId
      ).build(),
      timestampOlderThanOneWeek = false
    )
    launch<NavigationDrawerTestActivity>(
      createNavigationDrawerActivityIntent(internalProfileId)
    ).use {
      testCoroutineDispatchers.runCurrent()
      it.openNavigationDrawer()
      onView(
        allOf(
          withId(R.id.profile_topic_progress_text_view),
          isDescendantOfA(withId(R.id.progress_linear_layout))
        )
      ).check(matches(withText("1 Topic in Progress")))
    }
  }

  @Test
  fun testNavDrawer_withUserProfile_openNavDrawer_profileNameIsDisplayed() {
    launch<NavigationDrawerTestActivity>(
      createNavigationDrawerActivityIntent(internalProfileId1)
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
  fun testNavDrawer_clickOnHeader_opensProfileProgressActivity() {
    launch<NavigationDrawerTestActivity>(
      createNavigationDrawerActivityIntent(internalProfileId)
    ).use {
      testCoroutineDispatchers.runCurrent()
      it.openNavigationDrawer()
      onView(withId(R.id.nav_header_profile_name)).perform(click())
      testCoroutineDispatchers.runCurrent()
      val profileId = ProfileId.newBuilder().setLoggedInInternalProfileId(internalProfileId).build()
      intended(hasComponent(ProfileProgressActivity::class.java.name))
      intended(
        hasProtoExtra(
          PROFILE_ID_INTENT_DECORATOR,
          profileId
        )
      )
    }
  }

  // TODO(#2535): Unable to open NavigationDrawer multiple times on Robolectric
  @RunOn(TestPlatform.ESPRESSO)
  @Test
  fun testNavDrawer_openNavDrawer_switchProfile_cancel_homeIsSelected() {
    launch<NavigationDrawerTestActivity>(
      createNavigationDrawerActivityIntent(internalProfileId)
    ).use {
      it.openNavigationDrawer()
      onView(withText(R.string.menu_switch_profile)).perform(click())
      onView(withText(R.string.home_activity_back_dialog_cancel))
        .inRoot(isDialog())
        .perform(click())
      it.openNavigationDrawer()
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.fragment_drawer_nav_view))
        .check(matches(checkNavigationViewItemStatus(NavigationDrawerItem.HOME)))
    }
  }

  // TODO(#2535): Unable to open NavigationDrawer multiple times on Robolectric
  @RunOn(TestPlatform.ESPRESSO)
  @Test
  fun testNavDrawer_openNavDrawer_options_switchProfile_cancel_optionsIsSelected() {
    launch<NavigationDrawerTestActivity>(
      createNavigationDrawerActivityIntent(internalProfileId)
    ).use {
      it.openNavigationDrawer()
      onView(withText(R.string.menu_options)).perform(click())
      it.openNavigationDrawer()
      onView(withText(R.string.menu_switch_profile)).perform(click())
      onView(withText(R.string.home_activity_back_dialog_cancel))
        .inRoot(isDialog())
        .perform(click())
      it.openNavigationDrawer()
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.fragment_drawer_nav_view))
        .check(matches(checkNavigationViewItemStatus(NavigationDrawerItem.OPTIONS)))
    }
  }

  // TODO(#2535): Unable to open NavigationDrawer multiple times on Robolectric
  @RunOn(TestPlatform.ESPRESSO)
  @Test
  fun testNavDrawer_openNavDrawer_help_switchProfile_cancel_helpIsSelected() {
    launch<NavigationDrawerTestActivity>(
      createNavigationDrawerActivityIntent(internalProfileId)
    ).use {
      it.openNavigationDrawer()
      onView(withText(R.string.menu_help)).perform(click())
      it.openNavigationDrawer()
      onView(withText(R.string.menu_switch_profile)).perform(click())
      onView(withText(R.string.home_activity_back_dialog_cancel))
        .inRoot(isDialog())
        .perform(click())
      it.openNavigationDrawer()
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.fragment_drawer_nav_view))
        .check(matches(checkNavigationViewItemStatus(NavigationDrawerItem.HELP)))
    }
  }

  // TODO(#552): Enable this once lowfi implementation is done.
  // TODO(#2535): Unable to open NavigationDrawer multiple times on Robolectric
  @RunOn(TestPlatform.ESPRESSO)
  @Test
  @Ignore("My Downloads is removed until we have full download support.")
  fun testNavDrawer_openNavDrawer_download_switchProfile_cancel_downloadIsSelected() {
    launch<NavigationDrawerTestActivity>(
      createNavigationDrawerActivityIntent(internalProfileId)
    ).use {
      it.openNavigationDrawer()
      onView(withText(R.string.menu_my_downloads)).perform(click())
      it.openNavigationDrawer()
      onView(withText(R.string.menu_switch_profile)).perform(click())
      onView(withText(R.string.home_activity_back_dialog_cancel))
        .inRoot(isDialog())
        .perform(click())
      it.openNavigationDrawer()
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.fragment_drawer_nav_view))
        .check(matches(checkNavigationViewItemStatus(NavigationDrawerItem.DOWNLOADS)))
    }
  }

  // TODO(#2535): Unable to open NavigationDrawer multiple times on Robolectric
  @RunOn(TestPlatform.ESPRESSO)
  @Test
  fun testNavDrawer_openNavDrawer_admin_switchProfile_cancel_adminIsSelected() {
    launch<NavigationDrawerTestActivity>(
      createNavigationDrawerActivityIntent(internalProfileId)
    ).use {
      it.openNavigationDrawer()
      onView(withText(R.string.administrator_controls)).perform(click())
      it.openNavigationDrawer()
      onView(withText(R.string.menu_switch_profile)).perform(click())
      onView(withText(R.string.home_activity_back_dialog_cancel))
        .inRoot(isDialog())
        .perform(click())
      it.openNavigationDrawer()
      testCoroutineDispatchers.runCurrent()
      onView(
        allOf(
          withText(R.string.administrator_controls),
          isDescendantOfA(withId(R.id.administrator_controls_linear_layout))
        )
      ).check(matches(hasTextColor(R.color.component_color_drawer_fragment_selected_text_color)))
    }
  }

  // TODO(#2535): Unable to open NavigationDrawer multiple times on Robolectric
  @RunOn(TestPlatform.ESPRESSO)
  @Test
  fun testNavDrawer_openNavDrawer_switchProfile_cancel_configChange_homeIsSelected() {
    launch<NavigationDrawerTestActivity>(
      createNavigationDrawerActivityIntent(internalProfileId)
    ).use {
      it.openNavigationDrawer()
      onView(withText(R.string.menu_switch_profile)).perform(click())
      onView(withText(R.string.home_activity_back_dialog_cancel))
        .inRoot(isDialog())
        .perform(click())
      it.openNavigationDrawer()
      onView(isRoot()).perform(orientationLandscape())
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.fragment_drawer_nav_view))
        .check(matches(checkNavigationViewItemStatus(NavigationDrawerItem.HOME)))
    }
  }

  // TODO(#2535): Unable to open NavigationDrawer multiple times on Robolectric
  @RunOn(TestPlatform.ESPRESSO)
  @Test
  fun testNavDrawer_openNavDrawer_options_switchProfile_cancel_configChange_optionsIsSelected() {
    launch<NavigationDrawerTestActivity>(
      createNavigationDrawerActivityIntent(internalProfileId)
    ).use {
      it.openNavigationDrawer()
      onView(withText(R.string.menu_options)).perform(click())
      it.openNavigationDrawer()
      onView(withText(R.string.menu_switch_profile)).perform(click())
      onView(withText(R.string.home_activity_back_dialog_cancel))
        .inRoot(isDialog())
        .perform(click())
      it.openNavigationDrawer()
      onView(isRoot()).perform(orientationLandscape())
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.fragment_drawer_nav_view))
        .check(matches(checkNavigationViewItemStatus(NavigationDrawerItem.OPTIONS)))
    }
  }

  // TODO(#2535): Unable to open NavigationDrawer multiple times on Robolectric
  @RunOn(TestPlatform.ESPRESSO)
  @Test
  fun testNavDrawer_openNavDrawer_help_switchProfile_cancel_configChange_helpIsSelected() {
    launch<NavigationDrawerTestActivity>(
      createNavigationDrawerActivityIntent(internalProfileId)
    ).use {
      it.openNavigationDrawer()
      onView(withText(R.string.menu_help)).perform(click())
      it.openNavigationDrawer()
      onView(withText(R.string.menu_switch_profile)).perform(click())
      onView(withText(R.string.home_activity_back_dialog_cancel))
        .inRoot(isDialog())
        .perform(click())
      it.openNavigationDrawer()
      onView(isRoot()).perform(orientationLandscape())
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.fragment_drawer_nav_view))
        .check(matches(checkNavigationViewItemStatus(NavigationDrawerItem.HELP)))
    }
  }

  // TODO(#552): Enable this once lowfi implementation is done.
  // TODO(#2535): Unable to open NavigationDrawer multiple times on Robolectric
  @RunOn(TestPlatform.ESPRESSO)
  @Test
  @Ignore("My Downloads is removed until we have full download support.")
  fun testNavDrawer_openNavDrawer_download_switchProfile_cancel_configChange_downloadIsSelected() {
    launch<NavigationDrawerTestActivity>(
      createNavigationDrawerActivityIntent(internalProfileId)
    ).use {
      it.openNavigationDrawer()
      onView(withText(R.string.menu_my_downloads)).perform(click())
      it.openNavigationDrawer()
      onView(withText(R.string.menu_switch_profile)).perform(click())
      onView(withText(R.string.home_activity_back_dialog_cancel))
        .inRoot(isDialog())
        .perform(click())
      it.openNavigationDrawer()
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.fragment_drawer_nav_view))
        .check(matches(checkNavigationViewItemStatus(NavigationDrawerItem.DOWNLOADS)))
    }
  }

  // TODO(#2535): Unable to open NavigationDrawer multiple times on Robolectric
  @RunOn(TestPlatform.ESPRESSO)
  @Test
  fun testNavDrawer_openNavDrawer_admin_switchProfile_cancel_configChange_adminIsSelected() {
    launch<NavigationDrawerTestActivity>(
      createNavigationDrawerActivityIntent(internalProfileId)
    ).use {
      it.openNavigationDrawer()
      onView(withText(R.string.administrator_controls)).perform(click())
      it.openNavigationDrawer()
      onView(withText(R.string.menu_switch_profile)).perform(click())
      onView(withText(R.string.home_activity_back_dialog_cancel))
        .inRoot(isDialog())
        .perform(click())
      it.openNavigationDrawer()
      testCoroutineDispatchers.runCurrent()
      onView(isRoot()).perform(orientationLandscape())
      testCoroutineDispatchers.runCurrent()
      onView(
        allOf(
          withText(R.string.administrator_controls),
          isDescendantOfA(withId(R.id.administrator_controls_linear_layout))
        )
      ).check(matches(hasTextColor(R.color.component_color_drawer_fragment_selected_text_color)))
    }
  }

  // TODO(#2535): Unable to open NavigationDrawer multiple times on Robolectric
  @RunOn(TestPlatform.ESPRESSO)
  @Test
  fun testNavDrawer_openNavDrawer_options_pressBack_homeIsSelected() {
    launch<NavigationDrawerTestActivity>(
      createNavigationDrawerActivityIntent(internalProfileId)
    ).use {
      it.openNavigationDrawer()
      onView(withText(R.string.menu_options)).perform(click())
      onView(isRoot()).perform(pressBack())
      it.openNavigationDrawer()
      onView(withId(R.id.fragment_drawer_nav_view))
        .check(matches(checkNavigationViewItemStatus(NavigationDrawerItem.HOME)))
    }
  }

  // TODO(#2535): Unable to open NavigationDrawer multiple times on Robolectric
  @RunOn(TestPlatform.ESPRESSO)
  @Test
  fun testNavDrawer_openNavDrawer_help_pressBack_homeIsSelected() {
    launch<NavigationDrawerTestActivity>(
      createNavigationDrawerActivityIntent(internalProfileId)
    ).use {
      it.openNavigationDrawer()
      onView(withText(R.string.menu_help)).perform(click())
      onView(isRoot()).perform(pressBack())
      it.openNavigationDrawer()
      onView(withId(R.id.fragment_drawer_nav_view))
        .check(matches(checkNavigationViewItemStatus(NavigationDrawerItem.HOME)))
    }
  }

  // TODO(#552): Enable this once lowfi implementation is done.
  // TODO(#2535): Unable to open NavigationDrawer multiple times on Robolectric
  @RunOn(TestPlatform.ESPRESSO)
  @Test
  @Ignore("My Downloads is removed until we have full download support.")
  fun testNavDrawer_openNavDrawer_download_pressBack_homeIsSelected() {
    launch<NavigationDrawerTestActivity>(
      createNavigationDrawerActivityIntent(internalProfileId)
    ).use {
      it.openNavigationDrawer()
      onView(withText(R.string.menu_my_downloads)).perform(click())
      onView(isRoot()).perform(pressBack())
      it.openNavigationDrawer()
      onView(withId(R.id.fragment_drawer_nav_view))
        .check(matches(checkNavigationViewItemStatus(NavigationDrawerItem.HOME)))
    }
  }

  // TODO(#2535): Unable to open NavigationDrawer multiple times on Robolectric
  @RunOn(TestPlatform.ESPRESSO)
  @Test
  fun testNavDrawer_openNavDrawer_switchProfile_pressBack_homeIsSelected() {
    launch<NavigationDrawerTestActivity>(
      createNavigationDrawerActivityIntent(internalProfileId)
    ).use {
      it.openNavigationDrawer()
      onView(withText(R.string.menu_switch_profile)).perform(click())
      onView(isRoot()).perform(pressBack())
      it.openNavigationDrawer()
      onView(withId(R.id.fragment_drawer_nav_view))
        .check(matches(checkNavigationViewItemStatus(NavigationDrawerItem.HOME)))
    }
  }

  // TODO(#2535): Unable to open NavigationDrawer multiple times on Robolectric
  @RunOn(TestPlatform.ESPRESSO)
  @Test
  fun testNavDrawer_openNavDrawer_admin_pressBack_homeIsSelected() {
    launch<NavigationDrawerTestActivity>(
      createNavigationDrawerActivityIntent(internalProfileId)
    ).use {
      it.openNavigationDrawer()
      onView(withText(R.string.administrator_controls)).perform(click())
      onView(isRoot()).perform(pressBack())
      it.openNavigationDrawer()
      onView(withId(R.id.fragment_drawer_nav_view))
        .check(matches(checkNavigationViewItemStatus(NavigationDrawerItem.HOME)))
    }
  }

  // TODO(#2535): Unable to open NavigationDrawer multiple times on Robolectric
  @RunOn(TestPlatform.ESPRESSO)
  @Test
  fun testNavDrawer_openNavDrawer_options_pressBack_configChange_homeIsSelected() {
    launch<NavigationDrawerTestActivity>(
      createNavigationDrawerActivityIntent(internalProfileId)
    ).use {
      it.openNavigationDrawer()
      onView(withText(R.string.menu_options)).perform(click())
      onView(isRoot()).perform(pressBack())
      it.openNavigationDrawer()
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.fragment_drawer_nav_view))
        .check(matches(checkNavigationViewItemStatus(NavigationDrawerItem.HOME)))
    }
  }

  // TODO(#2535): Unable to open NavigationDrawer multiple times on Robolectric
  @RunOn(TestPlatform.ESPRESSO)
  @Test
  fun testNavDrawer_openNavDrawer_help_pressBack_configChange_homeIsSelected() {
    launch<NavigationDrawerTestActivity>(
      createNavigationDrawerActivityIntent(internalProfileId)
    ).use {
      it.openNavigationDrawer()
      onView(withText(R.string.menu_help)).perform(click())
      onView(isRoot()).perform(pressBack())
      it.openNavigationDrawer()
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.fragment_drawer_nav_view))
        .check(matches(checkNavigationViewItemStatus(NavigationDrawerItem.HOME)))
    }
  }

  // TODO(#552): Enable this once lowfi implementation is done.
  // TODO(#2535): Unable to open NavigationDrawer multiple times on Robolectric
  @RunOn(TestPlatform.ESPRESSO)
  @Test
  @Ignore("My Downloads is removed until we have full download support.")
  fun testNavDrawer_openNavDrawer_download_pressBack_configChange_homeIsSelected() {
    launch<NavigationDrawerTestActivity>(
      createNavigationDrawerActivityIntent(internalProfileId)
    ).use {
      it.openNavigationDrawer()
      onView(withText(R.string.menu_my_downloads)).perform(click())
      onView(isRoot()).perform(pressBack())
      it.openNavigationDrawer()
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.fragment_drawer_nav_view))
        .check(matches(checkNavigationViewItemStatus(NavigationDrawerItem.HOME)))
    }
  }

  // TODO(#2535): Unable to open NavigationDrawer multiple times on Robolectric
  @RunOn(TestPlatform.ESPRESSO)
  @Test
  fun testNavDrawer_openNavDrawer_switchProfile_pressBack_configChange_homeIsSelected() {
    launch<NavigationDrawerTestActivity>(
      createNavigationDrawerActivityIntent(internalProfileId)
    ).use {
      it.openNavigationDrawer()
      onView(withText(R.string.menu_switch_profile)).perform(click())
      onView(isRoot()).perform(pressBack())
      it.openNavigationDrawer()
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.fragment_drawer_nav_view))
        .check(matches(checkNavigationViewItemStatus(NavigationDrawerItem.HOME)))
    }
  }

  // TODO(#2535): Unable to open NavigationDrawer multiple times on Robolectric
  @RunOn(TestPlatform.ESPRESSO)
  @Test
  fun testNavDrawer_openNavDrawer_admin_pressBack_configChange_homeIsSelected() {
    launch<NavigationDrawerTestActivity>(
      createNavigationDrawerActivityIntent(internalProfileId)
    ).use {
      it.openNavigationDrawer()
      onView(withText(R.string.administrator_controls)).perform(click())
      onView(isRoot()).perform(pressBack())
      it.openNavigationDrawer()
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.fragment_drawer_nav_view))
        .check(matches(checkNavigationViewItemStatus(NavigationDrawerItem.HOME)))
    }
  }

  @Test
  fun testNavDrawer_inProdMode_openNavDrawer_devOptionsIsNotDisplayed() {
    launch<NavigationDrawerTestActivity>(
      createNavigationDrawerActivityIntent(internalProfileId)
    ).use {
      it.openNavigationDrawer()
      onView(withId(R.id.developer_options_linear_layout)).check(matches(not(isDisplayed())))
    }
  }

  @Test
  fun testNavDrawer_inProdMode_openNavDrawer_configChange_devOptionsIsNotDisplayed() {
    launch<NavigationDrawerTestActivity>(
      createNavigationDrawerActivityIntent(internalProfileId)
    ).use {
      it.openNavigationDrawer()
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.drawer_nested_scroll_view)).perform(swipeUp())
      onView(withId(R.id.developer_options_linear_layout)).check(matches(not(isDisplayed())))
    }
  }

  @Test
  fun testNavDrawer_withAdminProfile_openNavDrawer_adminControlsIsDisplayed() {
    launch<NavigationDrawerTestActivity>(
      createNavigationDrawerActivityIntent(internalProfileId)
    ).use {
      it.openNavigationDrawer()
      onView(withId(R.id.administrator_controls_linear_layout)).perform(nestedScrollTo())
        .check(matches(isDisplayed()))
    }
  }

  @Test
  fun testNavDrawer_withAdminProfile_configChange_adminControlsIsDisplayed() {
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
  fun testNavDrawer_withAdminProfile_adminMenu_opensAdminControlsActivity() {
    launch<NavigationDrawerTestActivity>(
      createNavigationDrawerActivityIntent(internalProfileId)
    ).use {
      it.openNavigationDrawer()
      onView(withId(R.id.administrator_controls_linear_layout)).perform(nestedScrollTo())
        .check(matches(isDisplayed())).perform(click())
      val profileId = ProfileId.newBuilder().setLoggedInInternalProfileId(0).build()
      intended(hasComponent(AdministratorControlsActivity::class.java.name))
      intended(hasProtoExtra(PROFILE_ID_INTENT_DECORATOR, profileId))
    }
  }

  @Test
  fun testNavDrawer_withUserProfile_adminControlsAreNotDisplayed() {
    launch<NavigationDrawerTestActivity>(
      createNavigationDrawerActivityIntent(internalProfileId1)
    ).use {
      it.openNavigationDrawer()
      onView(withId(R.id.administrator_controls_linear_layout))
        .check(matches(not(isDisplayed())))
    }
  }

  // TODO(#552): Enable this once lowfi implementation is done.
  @Test
  @Ignore("My Downloads is removed until we have full download support.")
  fun testNavDrawer_myDownloadsMenu_myDownloadsFragmentIsDisplayed() {
    launch<NavigationDrawerTestActivity>(
      createNavigationDrawerActivityIntent(internalProfileId)
    ).use {
      it.openNavigationDrawer()
      onView(withText(R.string.menu_my_downloads)).perform(click())
      intended(hasComponent(MyDownloadsActivity::class.java.name))
    }
  }

  @Test
  fun testNavDrawer_switchProfileMenu_exitToProfileChooserDialogIsDisplayed() {
    launch<NavigationDrawerTestActivity>(
      createNavigationDrawerActivityIntent(internalProfileId)
    ).use {
      it.openNavigationDrawer()
      onView(withText(R.string.menu_switch_profile)).perform(click())
      onView(withText(R.string.home_activity_back_dialog_message))
        .inRoot(isDialog())
        .check(matches(isDisplayed()))
    }
  }

  @Test
  fun testNavDrawer_switchProfileMenu_clickExit_opensProfileChooserActivity() {
    launch<NavigationDrawerTestActivity>(
      createNavigationDrawerActivityIntent(internalProfileId)
    ).use {
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
  fun testNavDrawer_openNavDrawerAndClose_navDrawerIsClosed() {
    launch<NavigationDrawerTestActivity>(
      createNavigationDrawerActivityIntent(internalProfileId)
    ).use {
      testCoroutineDispatchers.runCurrent()
      it.openNavigationDrawer()
      onView(withId(R.id.home_activity_drawer_layout)).perform(close())
      onView(withId(R.id.home_activity_drawer_layout)).check(matches(isClosed()))
    }
  }

  @RunOn(TestPlatform.ESPRESSO)
  @Test
  fun testNavDrawer_selectSwitchProfileMenu_clickCancel_navDrawerIsClosed() {
    launch<NavigationDrawerTestActivity>(
      createNavigationDrawerActivityIntent(internalProfileId)
    ).use {
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
  fun testNavDrawer_selectSwitchProfile_configChange_dialogIsVisible() {
    launch<NavigationDrawerTestActivity>(
      createNavigationDrawerActivityIntent(internalProfileId)
    ).use {
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
  fun testNavDrawer_openNavDrawer_selectHelpMenu_opensHelpActivity() {
    launch<NavigationDrawerTestActivity>(
      createNavigationDrawerActivityIntent(internalProfileId)
    ).use {
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

  private fun checkNavigationViewItemStatus(item: NavigationDrawerItem) =
    object : TypeSafeMatcher<View>() {
      override fun describeTo(description: Description) {
        description.appendText("NavigationViewItem is checked")
      }

      override fun matchesSafely(view: View): Boolean {
        return (view as NavigationView).menu.getItem(item.ordinal).isChecked
      }
    }

  // TODO(#59): Figure out a way to reuse modules instead of needing to re-declare them.
  @Singleton
  @Component(
    modules = [
      RobolectricModule::class,
      PlatformParameterModule::class, PlatformParameterSingletonModule::class,
      TestDispatcherModule::class, ApplicationModule::class,
      LoggerModule::class, ContinueModule::class, FractionInputModule::class,
      ItemSelectionInputModule::class, MultipleChoiceInputModule::class,
      NumberWithUnitsRuleModule::class, NumericInputRuleModule::class, TextInputRuleModule::class,
      DragDropSortInputModule::class, ImageClickInputModule::class, InteractionsModule::class,
      GcsResourceModule::class, GlideImageLoaderModule::class, ImageParsingModule::class,
      HtmlParserEntityTypeModule::class, QuestionModule::class, TestLogReportingModule::class,
      AccessibilityTestModule::class, LogStorageModule::class, CachingTestModule::class,
      ExpirationMetaDataRetrieverModule::class,
      ViewBindingShimModule::class, RatioInputModule::class, WorkManagerConfigurationModule::class,
      ApplicationStartupListenerModule::class, LogReportWorkerModule::class,
      HintsAndSolutionConfigModule::class, HintsAndSolutionProdModule::class,
      FirebaseLogUploaderModule::class, FakeOppiaClockModule::class,
      DeveloperOptionsModule::class, ExplorationStorageModule::class, NetworkModule::class,
      NetworkConnectionUtilDebugModule::class, NetworkConnectionDebugUtilModule::class,
      AssetModule::class, LocaleProdModule::class, ActivityRecreatorTestModule::class,
      NetworkConfigProdModule::class, NumericExpressionInputModule::class,
      AlgebraicExpressionInputModule::class, MathEquationInputModule::class,
      SplitScreenInteractionModule::class,
      LoggingIdentifierModule::class, ApplicationLifecycleModule::class,
      SyncStatusModule::class, MetricLogSchedulerModule::class, TestingBuildFlavorModule::class,
      EventLoggingConfigurationModule::class, ActivityRouterModule::class,
      CpuPerformanceSnapshotterModule::class, ExplorationProgressModule::class,
      TestAuthenticationModule::class
    ]
  )
  interface TestApplicationComponent : ApplicationComponent {
    @Component.Builder
    interface Builder : ApplicationComponent.Builder {
      override fun build(): TestApplicationComponent
    }

    fun inject(navigationDrawerActivityProdTest: NavigationDrawerActivityProdTest)
  }

  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerNavigationDrawerActivityProdTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    fun inject(navigationDrawerActivityProdTest: NavigationDrawerActivityProdTest) {
      return component.inject(navigationDrawerActivityProdTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }
}
