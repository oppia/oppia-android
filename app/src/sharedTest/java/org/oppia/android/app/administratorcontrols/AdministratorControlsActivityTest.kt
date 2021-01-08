package org.oppia.android.app.administratorcontrols

import android.app.Application
import android.content.Context
import android.content.Intent
import android.view.View
import android.view.ViewParent
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.NestedScrollView
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.PerformException
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isChecked
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.espresso.util.HumanReadables
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
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
import org.oppia.android.app.administratorcontrols.appversion.AppVersionActivity
import org.oppia.android.app.application.ActivityComponentFactory
import org.oppia.android.app.application.ApplicationComponent
import org.oppia.android.app.application.ApplicationInjector
import org.oppia.android.app.application.ApplicationInjectorProvider
import org.oppia.android.app.application.ApplicationModule
import org.oppia.android.app.application.ApplicationStartupListenerModule
import org.oppia.android.app.player.state.hintsandsolution.HintsAndSolutionConfigModule
import org.oppia.android.app.profile.ProfileChooserActivity
import org.oppia.android.app.recyclerview.RecyclerViewMatcher.Companion.atPositionOnView
import org.oppia.android.app.settings.profile.ProfileListActivity
import org.oppia.android.app.shim.ViewBindingShimModule
import org.oppia.android.app.utility.OrientationChangeAction.Companion.orientationLandscape
import org.oppia.android.app.utility.OrientationChangeAction.Companion.orientationPortrait
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
import org.oppia.android.testing.RobolectricModule
import org.oppia.android.testing.TestAccessibilityModule
import org.oppia.android.testing.TestCoroutineDispatchers
import org.oppia.android.testing.TestDispatcherModule
import org.oppia.android.testing.TestLogReportingModule
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

/** Tests for [AdministratorControlsActivity]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(
  application = AdministratorControlsActivityTest.TestApplication::class,
  qualifiers = "port-xxhdpi"
)
class AdministratorControlsActivityTest {

  @get:Rule
  var activityTestRule: ActivityTestRule<AdministratorControlsActivity> = ActivityTestRule(
    AdministratorControlsActivity::class.java, /* initialTouchMode= */
    true, /* launchActivity= */
    false
  )

  @Inject
  lateinit var profileTestHelper: ProfileTestHelper

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @Inject
  lateinit var context: Context

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
  fun testAdministratorControlsFragment_generalAndProfileManagementIsDisplayed() {
    launch<AdministratorControlsActivity>(
      createAdministratorControlsActivityIntent(
        profileId = 0
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(
        atPositionOnView(
          R.id.administrator_controls_list,
          0, R.id.general_text_view
        )
      )
        .check(matches(isDisplayed()))
      onView(
        atPositionOnView(
          R.id.administrator_controls_list,
          0, R.id.edit_account_text_view
        )
      )
        .check(
          matches(
            withText(
              context.resources.getString(
                R.string.administrator_controls_edit_account
              )
            )
          )
        )
      onView(
        atPositionOnView(
          R.id.administrator_controls_list,
          1,
          R.id.profile_management_text_view
        )
      )
        .check(matches(isDisplayed()))
      onView(
        atPositionOnView(
          R.id.administrator_controls_list,
          1, R.id.edit_profiles_text_view
        )
      )
        .check(
          matches(
            withText(
              context.resources.getString(
                R.string.administrator_controls_edit_profiles
              )
            )
          )
        )
    }
  }

  @Test
  fun testAdministratorControlsFragment_downloadPermissionsAndSettingsIsDisplayed() {
    launch<AdministratorControlsActivity>(
      createAdministratorControlsActivityIntent(
        profileId = 0
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(
        atPositionOnView(
          R.id.administrator_controls_list,
          2,
          R.id.download_permissions_text_view
        )
      )
        .check(
          matches(
            withText(
              context.resources.getString(
                R.string.administrator_controls_download_permissions_label
              )
            )
          )
        )
      onView(
        atPositionOnView(
          R.id.administrator_controls_list,
          2,
          R.id.topic_update_on_wifi_constraint_layout
        )
      )
        .check(matches(isDisplayed()))
      onView(withId(R.id.administrator_controls_list)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          2
        )
      )
      onView(
        atPositionOnView(
          R.id.administrator_controls_list,
          2,
          R.id.auto_update_topic_constraint_layout
        )
      )
        .check(matches(isDisplayed()))
    }
  }

  @Test
  fun testAdministratorControlsFragment_applicationSettingsIsDisplayed() {
    launch<AdministratorControlsActivity>(
      createAdministratorControlsActivityIntent(
        profileId = 0
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.administrator_controls_list)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          3
        )
      )
      onView(
        atPositionOnView(
          R.id.administrator_controls_list,
          3, R.id.app_information_text_view
        )
      )
        .check(matches(isDisplayed()))
      onView(
        atPositionOnView(
          R.id.administrator_controls_list,
          3, R.id.app_version_text_view
        )
      )
        .check(
          matches(
            withText(
              context.resources.getString(
                R.string.administrator_controls_app_version
              )
            )
          )
        )
      onView(
        atPositionOnView(
          R.id.administrator_controls_list,
          4, R.id.account_actions_text_view
        )
      )
        .check(matches(isDisplayed()))
      onView(
        atPositionOnView(
          R.id.administrator_controls_list,
          4, R.id.log_out_text_view
        )
      )
        .check(
          matches(
            withText(
              context.resources.getString(
                R.string.administrator_controls_log_out
              )
            )
          )
        )
    }
  }

  @Test
  fun testAdministratorControlsFragment_wifiSwitchIsUncheck_autoUpdateSwitchIsUncheck() {
    launch<AdministratorControlsActivity>(
      createAdministratorControlsActivityIntent(
        profileId = 0
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(
        atPositionOnView(
          R.id.administrator_controls_list,
          2,
          R.id.topic_update_on_wifi_switch
        )
      )
        .check(matches(not(isChecked())))
      onView(withId(R.id.administrator_controls_list)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          2
        )
      )
      onView(
        atPositionOnView(
          R.id.administrator_controls_list,
          2, R.id.auto_update_topic_switch
        )
      )
        .check(matches(not(isChecked())))
    }
  }

  @Test
  fun testAdministratorControlsFragment_clickWifiSwitch_configChange_wifiSwitchIsChecked() {
    launch<AdministratorControlsActivity>(
      createAdministratorControlsActivityIntent(
        profileId = 0
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.administrator_controls_list)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          2
        )
      )
      onView(
        atPositionOnView(
          R.id.administrator_controls_list,
          2, R.id.topic_update_on_wifi_switch
        )
      )
        .check(matches(not(isChecked())))
      onView(
        atPositionOnView(
          R.id.administrator_controls_list,
          2, R.id.auto_update_topic_switch
        )
      )
        .check(matches(not(isChecked())))
      onView(
        atPositionOnView(
          R.id.administrator_controls_list,
          2, R.id.topic_update_on_wifi_switch
        )
      )
        .perform(click())
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.administrator_controls_list)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          2
        )
      )
      onView(
        atPositionOnView(
          R.id.administrator_controls_list,
          2, R.id.topic_update_on_wifi_switch
        )
      )
        .check(matches(isChecked()))
      onView(
        atPositionOnView(
          R.id.administrator_controls_list,
          2, R.id.auto_update_topic_switch
        )
      )
        .check(matches(not(isChecked())))
      onView(isRoot()).perform(orientationPortrait())
      onView(withId(R.id.administrator_controls_list)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          2
        )
      )
      onView(
        atPositionOnView(
          R.id.administrator_controls_list,
          2, R.id.topic_update_on_wifi_switch
        )
      )
        .check(matches(isChecked()))
      onView(
        atPositionOnView(
          R.id.administrator_controls_list,
          2, R.id.auto_update_topic_switch
        )
      )
        .check(matches(not(isChecked())))
    }
  }

  @Test
  fun testAdministratorControlsFragment_clickEditProfile_opensProfileListActivity() {
    launch<AdministratorControlsActivity>(
      createAdministratorControlsActivityIntent(
        0
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.edit_profiles_text_view)).perform(click())
      intended(hasComponent(ProfileListActivity::class.java.name))
    }
  }

  @Test
  fun testAdministratorControlsFragment_clickLogoutButton_displaysLogoutDialog() {
    launch<AdministratorControlsActivity>(
      createAdministratorControlsActivityIntent(
        profileId = 0
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.administrator_controls_list)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          4
        )
      )
      onView(withId(R.id.log_out_text_view)).perform(click())
      onView(withText(R.string.log_out_dialog_message)).inRoot(isDialog())
        .check(matches(isDisplayed()))
      onView(withText(R.string.log_out_dialog_okay_button)).inRoot(isDialog())
        .check(matches(isDisplayed()))
      onView(withText(R.string.log_out_dialog_cancel_button)).inRoot(isDialog())
        .check(matches(isDisplayed()))
    }
  }

  @Test
  fun testAdministratorControlsFragment_changeConfiguration_clickLogout_displaysLogoutDialog() {
    launch<AdministratorControlsActivity>(
      createAdministratorControlsActivityIntent(
        profileId = 0
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.administrator_controls_list)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          4
        )
      )
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.administrator_controls_list)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          4
        )
      )
      onView(withId(R.id.log_out_text_view)).perform(click())
      onView(withText(R.string.log_out_dialog_message)).inRoot(isDialog())
        .check(matches(isDisplayed()))
      onView(withText(R.string.log_out_dialog_okay_button)).inRoot(isDialog())
        .check(matches(isDisplayed()))
      onView(withText(R.string.log_out_dialog_cancel_button)).inRoot(isDialog())
        .check(matches(isDisplayed()))
    }
  }

  // TODO(#762): Replace [ProfileChooserActivity] to [LoginActivity] once it is added.
  @Test
  fun testAdministratorControlsFragment_clickOkButtonInLogoutDialog_opensProfileChooserActivity() {
    launch<AdministratorControlsActivity>(
      createAdministratorControlsActivityIntent(
        profileId = 0
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.administrator_controls_list)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          4
        )
      )
      onView(withId(R.id.log_out_text_view)).perform(click())
      onView(withText(R.string.log_out_dialog_message)).inRoot(isDialog())
        .check(matches(isDisplayed()))
      onView(withText(R.string.log_out_dialog_okay_button)).perform(click())
      intended(hasComponent(ProfileChooserActivity::class.java.name))
    }
  }

  @Test
  fun testAdministratorControlsFragment_clickCancelButtonInLogoutDialog_dialogDismissed() {
    launch<AdministratorControlsActivity>(
      createAdministratorControlsActivityIntent(
        profileId = 0
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.administrator_controls_list)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          4
        )
      )
      onView(withId(R.id.log_out_text_view)).perform(click())
      onView(withText(R.string.log_out_dialog_message)).inRoot(isDialog())
        .check(matches(isDisplayed()))
      onView(withText(R.string.log_out_dialog_cancel_button)).perform(click())
      onView(withId(R.id.log_out_text_view)).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testAdministratorControlsFragment_clickAppVersion_opensAppVersionActivity() {
    launch<AdministratorControlsActivity>(
      createAdministratorControlsActivityIntent(
        0
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.administrator_controls_list)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          3
        )
      )
      onView(withId(R.id.app_version_text_view)).perform(click())
      intended(hasComponent(AppVersionActivity::class.java.name))
    }
  }

  @Test
  fun testAdministratorControls_selectAdminNavItem_displaysAdminControls() {
    launch<AdministratorControlsActivity>(
      createAdministratorControlsActivityIntent(
        0
      )
    ).use {
      it.openNavigationDrawer()
      onView(withId(R.id.administrator_controls_linear_layout)).perform(nestedScrollTo())
        .perform(click())
      onView(withText(context.getString(R.string.administrator_controls_edit_account)))
        .check(matches(isDisplayed()))
    }
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
      context,
      profileId
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
