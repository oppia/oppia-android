package org.oppia.android.app.administratorcontrols

import android.app.Application
import android.content.Context
import android.content.Intent
import android.view.View
import android.view.ViewParent
import android.widget.FrameLayout
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.NestedScrollView
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.PerformException
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.espresso.util.HumanReadables
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.google.common.truth.Truth.assertThat
import dagger.Component
import org.hamcrest.Matchers
import org.hamcrest.Matchers.not
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityComponent
import org.oppia.android.app.activity.ActivityComponentFactory
import org.oppia.android.app.administratorcontrols.appversion.AppVersionActivity
import org.oppia.android.app.application.ApplicationComponent
import org.oppia.android.app.application.ApplicationInjector
import org.oppia.android.app.application.ApplicationInjectorProvider
import org.oppia.android.app.application.ApplicationModule
import org.oppia.android.app.application.ApplicationStartupListenerModule
import org.oppia.android.app.devoptions.DeveloperOptionsModule
import org.oppia.android.app.devoptions.DeveloperOptionsStarterModule
import org.oppia.android.app.player.state.itemviewmodel.SplitScreenInteractionModule
import org.oppia.android.app.profile.ProfileChooserActivity
import org.oppia.android.app.recyclerview.RecyclerViewMatcher.Companion.atPositionOnView
import org.oppia.android.app.settings.profile.ProfileListActivity
import org.oppia.android.app.shim.ViewBindingShimModule
import org.oppia.android.app.topic.PracticeTabModule
import org.oppia.android.app.translation.testing.ActivityRecreatorTestModule
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
import org.oppia.android.domain.exploration.lightweightcheckpointing.ExplorationStorageModule
import org.oppia.android.domain.hintsandsolution.HintsAndSolutionConfigModule
import org.oppia.android.domain.hintsandsolution.HintsAndSolutionProdModule
import org.oppia.android.domain.onboarding.ExpirationMetaDataRetrieverModule
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.domain.oppialogger.LoggingIdentifierModule
import org.oppia.android.domain.oppialogger.analytics.ApplicationLifecycleModule
import org.oppia.android.domain.oppialogger.loguploader.LogReportWorkerModule
import org.oppia.android.domain.platformparameter.PlatformParameterModule
import org.oppia.android.domain.platformparameter.PlatformParameterSingletonModule
import org.oppia.android.domain.question.QuestionModule
import org.oppia.android.domain.topic.PrimeTopicAssetsControllerModule
import org.oppia.android.domain.workmanager.WorkManagerConfigurationModule
import org.oppia.android.testing.OppiaTestRule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.junit.InitializeDefaultLocaleRule
import org.oppia.android.testing.profile.ProfileTestHelper
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.accessibility.AccessibilityTestModule
import org.oppia.android.util.caching.AssetModule
import org.oppia.android.util.caching.testing.CachingTestModule
import org.oppia.android.util.gcsresource.GcsResourceModule
import org.oppia.android.util.locale.LocaleProdModule
import org.oppia.android.util.logging.LoggerModule
import org.oppia.android.util.logging.SyncStatusModule
import org.oppia.android.util.logging.firebase.FirebaseLogUploaderModule
import org.oppia.android.util.networking.NetworkConnectionDebugUtilModule
import org.oppia.android.util.networking.NetworkConnectionUtilDebugModule
import org.oppia.android.util.parser.html.HtmlParserEntityTypeModule
import org.oppia.android.util.parser.image.GlideImageLoaderModule
import org.oppia.android.util.parser.image.ImageParsingModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton
import org.oppia.android.util.logging.performancemetrics.MetricLogSchedulerModule

/** Tests for [AdministratorControlsActivity]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(
  application = AdministratorControlsActivityTest.TestApplication::class,
  qualifiers = "port-xxhdpi"
)
class AdministratorControlsActivityTest {
  @get:Rule
  val initializeDefaultLocaleRule = InitializeDefaultLocaleRule()

  @get:Rule
  val oppiaTestRule = OppiaTestRule()

  private val internalProfileId = 0

  @Inject
  lateinit var profileTestHelper: ProfileTestHelper

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @Inject
  lateinit var context: Context

  @get:Rule
  val activityTestRule = ActivityTestRule(
    AdministratorControlsActivity::class.java,
    /* initialTouchMode= */ true,
    /* launchActivity= */ false
  )

  @Test
  fun testAdministratorControls_hasCorrectActivityLabel() {
    activityTestRule.launchActivity(createAdministratorControlsActivityIntent(internalProfileId))
    val title = activityTestRule.activity.title

    // Verify that the activity label is correct as a proxy to verify TalkBack will announce the
    // correct string when it's read out.
    assertThat(title).isEqualTo(context.getString(R.string.administrator_controls_title))
  }

  @Before
  fun setUp() {
    Intents.init()
    setUpTestApplicationComponent()
    testCoroutineDispatchers.registerIdlingResource()
    profileTestHelper.initializeProfiles()
  }

  @After
  fun tearDown() {
    testCoroutineDispatchers.unregisterIdlingResource()
    Intents.release()
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  @Test
  fun testAdministratorControlsFragment_clickEditProfile_opensProfileListActivity() {
    launch<AdministratorControlsActivity>(
      createAdministratorControlsActivityIntent(
        profileId = internalProfileId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.edit_profiles_text_view)).perform(click())
      intended(hasComponent(ProfileListActivity::class.java.name))
    }
  }

  // TODO(#762): Replace [ProfileChooserActivity] to [LoginActivity] once it is added.
  @Test
  fun testAdministratorControlsFragment_clickOkButtonInLogoutDialog_opensProfileChooserActivity() {
    launch<AdministratorControlsActivity>(
      createAdministratorControlsActivityIntent(
        profileId = internalProfileId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 4)
      onView(withId(R.id.log_out_text_view)).perform(click())
      verifyTextInDialog(textInDialogId = R.string.log_out_dialog_message)
      onView(withText(R.string.log_out_dialog_okay_button)).perform(click())
      intended(hasComponent(ProfileChooserActivity::class.java.name))
    }
  }

  @Test
  fun testAdministratorControlsFragment_clickAppVersion_opensAppVersionActivity() {
    launch<AdministratorControlsActivity>(
      createAdministratorControlsActivityIntent(
        profileId = internalProfileId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 3)
      onView(withId(R.id.app_version_text_view)).perform(click())
      intended(hasComponent(AppVersionActivity::class.java.name))
    }
  }

  @Test
  fun testAdministratorControls_selectAdminNavItem_adminControlsIsDisplayed() {
    launch<AdministratorControlsActivity>(
      createAdministratorControlsActivityIntent(
        profileId = internalProfileId
      )
    ).use {
      it.openNavigationDrawer()
      onView(withId(R.id.administrator_controls_linear_layout)).perform(nestedScrollTo())
        .perform(click())
      onView(withText(context.getString(R.string.administrator_controls_edit_account)))
        .check(matches(isDisplayed()))
    }
  }

  @Test
  fun testAdministratorControlsFragment_clickLogoutButton_logoutDialogIsDisplayed() {
    launch<AdministratorControlsActivity>(
      createAdministratorControlsActivityIntent(
        profileId = internalProfileId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 4)
      onView(withId(R.id.log_out_text_view)).perform(click())
      verifyTextInDialog(textInDialogId = R.string.log_out_dialog_message)
      verifyTextInDialog(textInDialogId = R.string.log_out_dialog_okay_button)
      verifyTextInDialog(textInDialogId = R.string.log_out_dialog_cancel_button)
    }
  }

  @Test
  fun testAdministratorControlsFragment_configChange_clickLogout_logoutDialogIsDisplayed() {
    launch<AdministratorControlsActivity>(
      createAdministratorControlsActivityIntent(
        profileId = internalProfileId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 4)
      onView(isRoot()).perform(orientationLandscape())
      scrollToPosition(position = 4)
      onView(withId(R.id.log_out_text_view)).perform(click())
      verifyTextInDialog(textInDialogId = R.string.log_out_dialog_message)
      verifyTextInDialog(textInDialogId = R.string.log_out_dialog_okay_button)
      verifyTextInDialog(textInDialogId = R.string.log_out_dialog_cancel_button)
    }
  }

  @Test
  fun testAdministratorControlsFragment_clickLogout_configChange_logoutDialogIsDisplayed() {
    launch<AdministratorControlsActivity>(
      createAdministratorControlsActivityIntent(
        profileId = 0
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 4)
      onView(withId(R.id.log_out_text_view)).perform(click())
      onView(isRoot()).perform(orientationLandscape())
      verifyTextInDialog(textInDialogId = R.string.log_out_dialog_message)
      verifyTextInDialog(textInDialogId = R.string.log_out_dialog_okay_button)
      verifyTextInDialog(textInDialogId = R.string.log_out_dialog_cancel_button)
    }
  }

  @Test
  fun testAdministratorControlsFragment_clickCancelButtonInLogoutDialog_dialogIsDismissed() {
    launch<AdministratorControlsActivity>(
      createAdministratorControlsActivityIntent(
        profileId = internalProfileId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 4)
      onView(withId(R.id.log_out_text_view)).perform(click())
      verifyTextInDialog(textInDialogId = R.string.log_out_dialog_message)
      onView(withText(R.string.log_out_dialog_cancel_button)).perform(click())
      onView(withId(R.id.log_out_text_view)).check(matches(isDisplayed()))
    }
  }

  @Test
  @Config(qualifiers = "sw600dp")
  fun testAdministratorControls_defaultTabletConfig_multiPaneBackButtonGone() {
    launch<AdministratorControlsActivity>(
      createAdministratorControlsActivityIntent(
        profileId = internalProfileId
      )
    ).use {
      onView(withId(R.id.administrator_controls_multipane_options_back_button))
        .check(matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.GONE)))
    }
  }

  @Test
  @Config(qualifiers = "sw600dp")
  fun testAdministratorControls_tabletConfigChange_multiPaneBackButtonGone() {
    launch<AdministratorControlsActivity>(
      createAdministratorControlsActivityIntent(
        profileId = internalProfileId
      )
    ).use {
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.administrator_controls_multipane_options_back_button))
        .check(matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.GONE)))
    }
  }

  @Test
  @Config(qualifiers = "sw600dp")
  fun testAdministratorControls_defaultTabletConfig_editProfileVisible() {
    launch<AdministratorControlsActivity>(
      createAdministratorControlsActivityIntent(
        profileId = internalProfileId
      )
    ).use {
      onView(withId(R.id.extra_controls_title))
        .check(matches(withText(R.string.administrator_controls_edit_profiles)))
    }
  }

  @Test
  @Config(qualifiers = "sw600dp")
  fun testAdministratorControls_tabletConfigChange_editProfileVisible() {
    launch<AdministratorControlsActivity>(
      createAdministratorControlsActivityIntent(
        profileId = internalProfileId
      )
    ).use {
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.extra_controls_title))
        .check(matches(withText(R.string.administrator_controls_edit_profiles)))
    }
  }

  @Test
  @Config(qualifiers = "sw600dp")
  fun testAdministratorControls_defaultTabletConfig_profileListIsDisplayed() {
    launch<AdministratorControlsActivity>(
      createAdministratorControlsActivityIntent(
        profileId = internalProfileId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      checkIsAdminProfileVisible()
      checkIsAdminTextVisible()
      onView(atPositionOnView(R.id.profile_list_recycler_view, 1, R.id.profile_list_name)).check(
        matches(withText("Ben"))
      )
    }
  }

  @Test
  @Config(qualifiers = "sw600dp")
  fun testAdministratorControls_tabletConfigChange_profileListIsDisplayed() {
    launch<AdministratorControlsActivity>(
      createAdministratorControlsActivityIntent(
        profileId = internalProfileId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(isRoot()).perform(orientationLandscape())
      testCoroutineDispatchers.runCurrent()
      checkIsAdminProfileVisible()
      checkIsAdminTextVisible()
      onView(atPositionOnView(R.id.profile_list_recycler_view, 1, R.id.profile_list_name)).check(
        matches(withText("Ben"))
      )
    }
  }

  @Test
  @Config(qualifiers = "sw600dp")
  fun testAdministratorControls_selectProfileAdmin_backButton_selectSecondProfileDisplayed() {
    launch<AdministratorControlsActivity>(
      createAdministratorControlsActivityIntent(
        profileId = internalProfileId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      clickAdminProfile()
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.extra_controls_title)).check(matches(withText("Admin")))
      onView(withId(R.id.profile_edit_name)).check(matches(withText("Admin")))
      onView(withId(R.id.administrator_controls_multipane_options_back_button)).perform(click())
      testCoroutineDispatchers.runCurrent()
      onView(atPositionOnView(R.id.profile_list_recycler_view, 1, R.id.profile_list_name))
        .check(matches(withText("Ben")))
      onView(atPositionOnView(R.id.profile_list_recycler_view, 1, R.id.profile_list_name))
        .perform(click())
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.extra_controls_title)).check(matches(withText("Ben")))
      onView(withId(R.id.profile_edit_name)).check(matches(withText("Ben")))
    }
  }

  @Test
  @Config(qualifiers = "sw600dp")
  fun testAdministratorControls_selectProfileAdmin_backPressed_selectSecondProfileDisplayed() {
    launch<AdministratorControlsActivity>(
      createAdministratorControlsActivityIntent(
        profileId = internalProfileId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      clickAdminProfile()
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.extra_controls_title)).check(matches(withText("Admin")))
      onView(withId(R.id.profile_edit_name)).check(matches(withText("Admin")))
      pressBack()
      testCoroutineDispatchers.runCurrent()
      onView(atPositionOnView(R.id.profile_list_recycler_view, 1, R.id.profile_list_name))
        .check(matches(withText("Ben")))
      onView(atPositionOnView(R.id.profile_list_recycler_view, 1, R.id.profile_list_name))
        .perform(click())
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.extra_controls_title)).check(matches(withText("Ben")))
      onView(withId(R.id.profile_edit_name)).check(matches(withText("Ben")))
    }
  }

  @Test
  @Config(qualifiers = "sw600dp")
  fun testAdministratorControls_selectProfileAdmin_tabletConfigChange_displaysProfileEdit() {
    launch<AdministratorControlsActivity>(
      createAdministratorControlsActivityIntent(
        profileId = internalProfileId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      clickAdminProfile()
      onView(isRoot()).perform(orientationLandscape())
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.extra_controls_title)).check(matches(withText("Admin")))
      onView(withId(R.id.profile_edit_name)).check(matches(withText("Admin")))
      onView(withId(R.id.profile_edit_allow_download_heading)).check(matches(not(isDisplayed())))
      onView(withId(R.id.profile_edit_allow_download_sub)).check(matches(not(isDisplayed())))
      onView(withId(R.id.profile_edit_allow_download_switch)).check(matches(not(isDisplayed())))
      onView(withId(R.id.profile_delete_button)).check(matches(not(isDisplayed())))
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.profile_edit_name)).check(matches(withText("Admin")))
    }
  }

  @Test
  @Config(qualifiers = "sw600dp")
  fun testAdministratorControls_selectProfileUser_tabletConfigChange_displaysProfileEdit() {
    launch<AdministratorControlsActivity>(
      createAdministratorControlsActivityIntent(
        profileId = internalProfileId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(atPositionOnView(R.id.profile_list_recycler_view, 1, R.id.profile_list_name)).check(
        matches(withText("Ben"))
      ).perform(click())
      onView(isRoot()).perform(orientationLandscape())
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.profile_edit_name)).check(matches(withText("Ben")))
      onView(withId(R.id.profile_edit_allow_download_heading)).check(matches(isDisplayed()))
      onView(withId(R.id.profile_edit_allow_download_sub)).check(matches(isDisplayed()))
      onView(withId(R.id.profile_edit_allow_download_switch)).check(matches(isDisplayed()))
      onView(withId(R.id.profile_delete_button)).check(matches(isDisplayed()))
    }
  }

  @Test
  @Config(qualifiers = "sw600dp")
  fun testAdministratorControlsFragment_clickProfileDeletionButton_checkOpensDeletionDialog() {
    launch<AdministratorControlsActivity>(
      createAdministratorControlsActivityIntent(
        profileId = 1
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(atPositionOnView(R.id.profile_list_recycler_view, 1, R.id.profile_list_name)).check(
        matches(withText("Ben"))
      ).perform(click())
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.profile_delete_button)).perform(click())
      onView(withText(R.string.profile_edit_delete_dialog_message))
        .inRoot(isDialog())
        .check(
          matches(
            isDisplayed()
          )
        )
    }
  }

  @Test
  @Config(qualifiers = "sw600dp")
  fun testAdministratorControlsFragment_configChange_checkOpensDeletionDialog() {
    launch<AdministratorControlsActivity>(
      createAdministratorControlsActivityIntent(
        profileId = 1
      )
    ).use {
      onView(isRoot()).perform(orientationLandscape())
      testCoroutineDispatchers.runCurrent()
      onView(atPositionOnView(R.id.profile_list_recycler_view, 1, R.id.profile_list_name)).check(
        matches(withText("Ben"))
      ).perform(click())
      onView(withId(R.id.profile_delete_button)).perform(scrollTo()).perform(click())
      testCoroutineDispatchers.runCurrent()
      onView(withText(R.string.profile_edit_delete_dialog_message))
        .inRoot(isDialog())
        .check(
          matches(
            isDisplayed()
          )
        )
    }
  }

  @Test
  @Config(qualifiers = "sw600dp")
  fun testAdministratorControlsFragment_configChange_checkDeletionDialogIsVisible() {
    launch<AdministratorControlsActivity>(
      createAdministratorControlsActivityIntent(
        profileId = 1
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(atPositionOnView(R.id.profile_list_recycler_view, 1, R.id.profile_list_name)).check(
        matches(withText("Ben"))
      ).perform(click())
      onView(withId(R.id.profile_delete_button)).perform(scrollTo()).perform(click())
      testCoroutineDispatchers.runCurrent()
      onView(isRoot()).perform(orientationLandscape())
      testCoroutineDispatchers.runCurrent()
      onView(withText(R.string.profile_edit_delete_dialog_message))
        .inRoot(isDialog())
        .check(
          matches(
            isCompletelyDisplayed()
          )
        )
    }
  }

  private fun checkIsAdminProfileVisible() {
    onView(atPositionOnView(R.id.profile_list_recycler_view, 0, R.id.profile_list_name)).check(
      matches(withText("Admin"))
    )
  }

  private fun clickAdminProfile() {
    onView(atPositionOnView(R.id.profile_list_recycler_view, 0, R.id.profile_list_name)).check(
      matches(withText("Admin"))
    ).perform(click())
  }

  private fun checkIsAdminTextVisible() {
    onView(
      atPositionOnView(
        R.id.profile_list_recycler_view,
        0,
        R.id.profile_list_admin_text
      )
    ).check(
      matches(withText(context.resources.getString(R.string.profile_chooser_admin)))
    )
  }

  private fun ActivityScenario<AdministratorControlsActivity>.openNavigationDrawer() {
    onView(withContentDescription(R.string.drawer_open_content_description))
      .check(matches(isCompletelyDisplayed()))
      .perform(click())

    // Force the drawer animation to start. See https://github.com/oppia/oppia-android/pull/2204 for
    // background context.
    onActivity { activity ->
      val drawerLayout =
        activity.findViewById<DrawerLayout>(R.id.administrator_controls_activity_drawer_layout)
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

  private fun createAdministratorControlsActivityIntent(profileId: Int): Intent {
    return AdministratorControlsActivity.createAdministratorControlsActivityIntent(
      context = context,
      profileId = profileId
    )
  }

  /** Functions nestedScrollTo() and findFirstParentLayoutOfClass() taken from: https://stackoverflow.com/a/46037284/8860848 */
  private fun nestedScrollTo(): ViewAction {
    return object : ViewAction {
      override fun getDescription(): String {
        return "View is not NestedScrollView"
      }

      override fun getConstraints(): org.hamcrest.Matcher<View> {
        return Matchers.allOf(
          ViewMatchers.isDescendantOfA(ViewMatchers.isAssignableFrom(NestedScrollView::class.java))
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

  private fun verifyItemDisplayedOnAdministratorControlListItem(
    itemPosition: Int,
    targetView: Int
  ) {
    onView(
      atPositionOnView(
        recyclerViewId = R.id.administrator_controls_list,
        position = itemPosition,
        targetViewId = targetView
      )
    ).check(matches(isDisplayed()))
  }

  private fun verifyTextOnAdministratorListItemAtPosition(
    itemPosition: Int,
    targetViewId: Int,
    @StringRes stringIdToMatch: Int
  ) {
    onView(
      atPositionOnView(
        recyclerViewId = R.id.administrator_controls_list,
        position = itemPosition,
        targetViewId = targetViewId
      )
    ).check(matches(withText(context.getString(stringIdToMatch))))
  }

  private fun scrollToPosition(position: Int) {
    onView(withId(R.id.administrator_controls_list)).perform(
      scrollToPosition<RecyclerView.ViewHolder>(
        position
      )
    )
  }

  private fun verifyTextInDialog(@StringRes textInDialogId: Int) {
    onView(withText(context.getString(textInDialogId)))
      .inRoot(isDialog())
      .check(matches(isDisplayed()))
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
      PrimeTopicAssetsControllerModule::class, ExpirationMetaDataRetrieverModule::class,
      ViewBindingShimModule::class, RatioInputModule::class, WorkManagerConfigurationModule::class,
      ApplicationStartupListenerModule::class, LogReportWorkerModule::class,
      HintsAndSolutionConfigModule::class, HintsAndSolutionProdModule::class,
      FirebaseLogUploaderModule::class, FakeOppiaClockModule::class, PracticeTabModule::class,
      DeveloperOptionsStarterModule::class, DeveloperOptionsModule::class,
      ExplorationStorageModule::class, NetworkModule::class, NetworkConfigProdModule::class,
      NetworkConnectionUtilDebugModule::class, NetworkConnectionDebugUtilModule::class,
      AssetModule::class, LocaleProdModule::class, ActivityRecreatorTestModule::class,
      NumericExpressionInputModule::class, AlgebraicExpressionInputModule::class,
      MathEquationInputModule::class, SplitScreenInteractionModule::class,
      LoggingIdentifierModule::class, ApplicationLifecycleModule::class,
      SyncStatusModule::class, MetricLogSchedulerModule::class
    ]
  )
  interface TestApplicationComponent : ApplicationComponent {
    @Component.Builder
    interface Builder : ApplicationComponent.Builder

    fun inject(administratorControlsActivityTest: AdministratorControlsActivityTest)
  }

  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerAdministratorControlsActivityTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    fun inject(administratorControlsActivityTest: AdministratorControlsActivityTest) {
      component.inject(administratorControlsActivityTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }
}
