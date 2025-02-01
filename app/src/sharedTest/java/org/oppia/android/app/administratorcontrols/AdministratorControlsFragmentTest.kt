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
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.PerformException
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isChecked
import androidx.test.espresso.matcher.ViewMatchers.isClickable
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.util.HumanReadables
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.Component
import org.hamcrest.Matcher
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
import org.oppia.android.app.activity.route.ActivityRouterModule
import org.oppia.android.app.application.ApplicationComponent
import org.oppia.android.app.application.ApplicationInjector
import org.oppia.android.app.application.ApplicationInjectorProvider
import org.oppia.android.app.application.ApplicationModule
import org.oppia.android.app.application.ApplicationStartupListenerModule
import org.oppia.android.app.application.testing.TestingBuildFlavorModule
import org.oppia.android.app.devoptions.DeveloperOptionsModule
import org.oppia.android.app.devoptions.DeveloperOptionsStarterModule
import org.oppia.android.app.model.AdministratorControlsFragmentArguments
import org.oppia.android.app.player.state.itemviewmodel.SplitScreenInteractionModule
import org.oppia.android.app.recyclerview.RecyclerViewMatcher.Companion.atPositionOnView
import org.oppia.android.app.recyclerview.RecyclerViewMatcher.Companion.scrollToPosition
import org.oppia.android.app.recyclerview.RecyclerViewMatcher.Companion.verifyItemDisplayedOnListItem
import org.oppia.android.app.recyclerview.RecyclerViewMatcher.Companion.verifyItemDisplayedOnListItemDoesNotExist
import org.oppia.android.app.recyclerview.RecyclerViewMatcher.Companion.verifyTextOnListItemAtPosition
import org.oppia.android.app.shim.ViewBindingShimModule
import org.oppia.android.app.testing.AdministratorControlsFragmentTestActivity
import org.oppia.android.app.translation.testing.ActivityRecreatorTestModule
import org.oppia.android.app.utility.OrientationChangeAction.Companion.orientationLandscape
import org.oppia.android.app.utility.OrientationChangeAction.Companion.orientationPortrait
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
import org.oppia.android.domain.platformparameter.PlatformParameterSingletonModule
import org.oppia.android.domain.question.QuestionModule
import org.oppia.android.domain.workmanager.WorkManagerConfigurationModule
import org.oppia.android.testing.OppiaTestRule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.firebase.TestAuthenticationModule
import org.oppia.android.testing.junit.InitializeDefaultLocaleRule
import org.oppia.android.testing.platformparameter.TestPlatformParameterModule
import org.oppia.android.testing.profile.ProfileTestHelper
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.accessibility.AccessibilityTestModule
import org.oppia.android.util.caching.AssetModule
import org.oppia.android.util.caching.testing.CachingTestModule
import org.oppia.android.util.extensions.getProto
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

@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(
  application = AdministratorControlsFragmentTest.TestApplication::class,
  qualifiers = "port-xxhdpi"
)
class AdministratorControlsFragmentTest {

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

  private val administratorControlsListRecyclerViewId: Int = R.id.administrator_controls_list

  @Before
  fun setUp() {
    TestPlatformParameterModule.forceEnableEditAccountsOptionsUi(true)
    TestPlatformParameterModule.forceEnableDownloadsSupport(true)
    Intents.init()
    setUpTestApplicationComponent()
    profileTestHelper.initializeProfiles()
    testCoroutineDispatchers.registerIdlingResource()
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
    launch<AdministratorControlsFragmentTestActivity>(
      createAdministratorControlsFragmentTestActivityIntent(
        profileId = internalProfileId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()

      verifyItemDisplayedOnListItem(
        recyclerViewId = administratorControlsListRecyclerViewId,
        itemPosition = 0,
        targetView = R.id.general_text_view
      )
      verifyTextOnListItemAtPosition(
        recyclerViewId = administratorControlsListRecyclerViewId,
        itemPosition = 0,
        targetViewId = R.id.edit_account_text_view,
        stringIdToMatch = R.string.administrator_controls_edit_account
      )
      verifyItemDisplayedOnListItem(
        recyclerViewId = administratorControlsListRecyclerViewId,
        itemPosition = 1,
        targetView = R.id.profile_management_text_view
      )
      verifyTextOnListItemAtPosition(
        recyclerViewId = administratorControlsListRecyclerViewId,
        itemPosition = 1,
        targetViewId = R.id.edit_profiles_text_view,
        stringIdToMatch = R.string.administrator_controls_edit_profiles
      )
    }
  }

  @Test
  fun testAdministratorControlsFragment_downloadPermissionsAndSettingsIsDisplayed() {
    launch<AdministratorControlsFragmentTestActivity>(
      createAdministratorControlsFragmentTestActivityIntent(
        profileId = internalProfileId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      verifyTextOnListItemAtPosition(
        recyclerViewId = administratorControlsListRecyclerViewId,
        itemPosition = 2,
        targetViewId = R.id.download_permissions_text_view,
        stringIdToMatch = R.string.administrator_controls_download_permissions_label
      )
      verifyItemDisplayedOnListItem(
        recyclerViewId = administratorControlsListRecyclerViewId,
        itemPosition = 2,
        targetView = R.id.topic_update_on_wifi_constraint_layout
      )
      scrollToPosition(position = 2, recyclerViewId = administratorControlsListRecyclerViewId)
      verifyItemDisplayedOnListItem(
        recyclerViewId = administratorControlsListRecyclerViewId,
        itemPosition = 2,
        targetView = R.id.auto_update_topic_constraint_layout
      )
    }
  }

  @Test
  fun testAdministratorControlsFragment_downloadPermissionsAndSettings_autoUpdateIsNotDisplayed() {
    TestPlatformParameterModule.forceEnableDownloadsSupport(false)
    launch<AdministratorControlsFragmentTestActivity>(
      createAdministratorControlsFragmentTestActivityIntent(
        profileId = internalProfileId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 2, recyclerViewId = administratorControlsListRecyclerViewId)
      verifyItemDisplayedOnListItemDoesNotExist(
        recyclerViewId = administratorControlsListRecyclerViewId,
        itemPosition = 2,
        targetView = R.id.download_permissions_text_view
      )
      verifyItemDisplayedOnListItemDoesNotExist(
        recyclerViewId = administratorControlsListRecyclerViewId,
        itemPosition = 2,
        targetView = R.id.auto_update_topic_constraint_layout
      )
    }
  }

  @Test
  fun testAdministratorControlsFragment_applicationSettingsIsDisplayed() {
    launch<AdministratorControlsFragmentTestActivity>(
      createAdministratorControlsFragmentTestActivityIntent(
        profileId = internalProfileId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 3, recyclerViewId = administratorControlsListRecyclerViewId)
      verifyItemDisplayedOnListItem(
        recyclerViewId = administratorControlsListRecyclerViewId,
        itemPosition = 3,
        targetView = R.id.app_information_text_view
      )
      verifyTextOnListItemAtPosition(
        recyclerViewId = administratorControlsListRecyclerViewId,
        itemPosition = 3,
        targetViewId = R.id.app_version_text_view,
        stringIdToMatch = R.string.administrator_controls_app_version
      )
      verifyItemDisplayedOnListItem(
        recyclerViewId = administratorControlsListRecyclerViewId,
        itemPosition = 4,
        targetView = R.id.account_actions_text_view
      )
      verifyTextOnListItemAtPosition(
        recyclerViewId = administratorControlsListRecyclerViewId,
        itemPosition = 4,
        targetViewId = R.id.log_out_text_view,
        stringIdToMatch = R.string.administrator_controls_log_out
      )
    }
  }

  @Test
  fun testAdministratorControlsFragment_wifiSwitchIsUnchecked() {
    launch<AdministratorControlsFragmentTestActivity>(
      createAdministratorControlsFragmentTestActivityIntent(
        profileId = internalProfileId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      checkUpdateOnWifiSwitchNotChecked()
    }
  }

  @Test
  fun testAdministratorControlsFragment_autoUpdateSwitchIsUnchecked() {
    launch<AdministratorControlsFragmentTestActivity>(
      createAdministratorControlsFragmentTestActivityIntent(
        profileId = internalProfileId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 2, recyclerViewId = administratorControlsListRecyclerViewId)
      checkAutoUpdateSwitchIsUnchecked()
    }
  }

  @Test
  fun testAdministratorControlsFragment_clickWifiContainer_wifiSwitchIsChecked() {
    launch<AdministratorControlsFragmentTestActivity>(
      createAdministratorControlsFragmentTestActivityIntent(
        profileId = internalProfileId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 2, recyclerViewId = administratorControlsListRecyclerViewId)
      clickUpdateOnWifiSwitch()
      atAdminControlsItem(position = 2, viewId = R.id.topic_update_on_wifi_constraint_layout)
      testCoroutineDispatchers.runCurrent()
      checkUpdateOnWifiSwitchIsChecked()
    }
  }

  @Test
  fun testAdministratorControlsFragment_clickWifiContainer_orientationLand_wifiSwitchIsChecked() {
    launch<AdministratorControlsFragmentTestActivity>(
      createAdministratorControlsFragmentTestActivityIntent(
        profileId = internalProfileId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 2, recyclerViewId = administratorControlsListRecyclerViewId)
      clickUpdateOnWifiSwitch()
      atAdminControlsItem(position = 2, viewId = R.id.topic_update_on_wifi_constraint_layout)
      testCoroutineDispatchers.runCurrent()
      onView(isRoot()).perform(orientationLandscape())
      scrollToPosition(position = 2, recyclerViewId = administratorControlsListRecyclerViewId)
      checkUpdateOnWifiSwitchIsChecked()
    }
  }

  @Test
  fun testAdministratorControlsFragment_clickWifiContainer_configChange_wifiSwitchIsChecked() {
    launch<AdministratorControlsFragmentTestActivity>(
      createAdministratorControlsFragmentTestActivityIntent(
        profileId = internalProfileId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 2, recyclerViewId = administratorControlsListRecyclerViewId)
      clickUpdateOnWifiSwitch()
      atAdminControlsItem(position = 2, viewId = R.id.topic_update_on_wifi_constraint_layout)
      testCoroutineDispatchers.runCurrent()
      onView(isRoot()).perform(orientationLandscape())
      onView(isRoot()).perform(orientationPortrait())
      scrollToPosition(position = 2, recyclerViewId = administratorControlsListRecyclerViewId)
      checkUpdateOnWifiSwitchIsChecked()
    }
  }

  @Test
  fun testAdministratorControls_clickWifiContainer_orientationLand_autoUpdateSwitchIsChecked() {
    launch<AdministratorControlsFragmentTestActivity>(
      createAdministratorControlsFragmentTestActivityIntent(
        profileId = internalProfileId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 2, recyclerViewId = administratorControlsListRecyclerViewId)
      clickAutoUpdateTopicSwitch()
      testCoroutineDispatchers.runCurrent()
      onView(isRoot()).perform(orientationLandscape())
      scrollToPosition(position = 2, recyclerViewId = administratorControlsListRecyclerViewId)
      checkAutoUpdateTopicSwitchIsChecked()
    }
  }

  @Test
  fun testAdministratorControls_clickWifiContainer_configChange_autoUpdateSwitchIsChecked() {
    launch<AdministratorControlsFragmentTestActivity>(
      createAdministratorControlsFragmentTestActivityIntent(
        profileId = internalProfileId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 2, recyclerViewId = administratorControlsListRecyclerViewId)
      clickAutoUpdateTopicSwitch()
      testCoroutineDispatchers.runCurrent()
      onView(isRoot()).perform(orientationLandscape())
      onView(isRoot()).perform(orientationPortrait())
      scrollToPosition(position = 2, recyclerViewId = administratorControlsListRecyclerViewId)
      checkAutoUpdateTopicSwitchIsChecked()
    }
  }

  @Test
  fun testAdministratorControlsFragment_clickAutoUpdateContainer_autoUpdateSwitchIsChecked() {
    launch<AdministratorControlsFragmentTestActivity>(
      createAdministratorControlsFragmentTestActivityIntent(
        profileId = internalProfileId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 2, recyclerViewId = administratorControlsListRecyclerViewId)
      clickAutoUpdateTopicContainer()
      testCoroutineDispatchers.runCurrent()
      checkAutoUpdateTopicSwitchIsChecked()
    }
  }

  @Test
  fun testAdministratorControlsFragment_nonDownloadPermissionProfile_wifiSwitchIsNonClickable() {
    launch<AdministratorControlsFragmentTestActivity>(
      createAdministratorControlsFragmentTestActivityIntent(
        profileId = internalProfileId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 2, recyclerViewId = administratorControlsListRecyclerViewId)
      checkUpdateOnWifiSwitchNotClickable()
    }
  }

  @Test
  fun testAdministratorControlsFragment_autoUpdateSwitchIsNonClickable() {
    launch<AdministratorControlsFragmentTestActivity>(
      createAdministratorControlsFragmentTestActivityIntent(
        profileId = internalProfileId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 2, recyclerViewId = administratorControlsListRecyclerViewId)
      checkAutoUpdateTopicSwitchNotClickable()
    }
  }

  @Test
  fun testFragment_argumentsAreCorrect() {
    launch<AdministratorControlsFragmentTestActivity>(
      createAdministratorControlsFragmentTestActivityIntent(
        profileId = internalProfileId
      )
    ).use { scenario ->
      testCoroutineDispatchers.runCurrent()
      scenario.onActivity { activity ->

        val administratorControlsFragment = activity.supportFragmentManager
          .findFragmentById(R.id.administrator_controls_fragment_test_activity_fragment_container)
          as AdministratorControlsFragment
        val isMultipane = activity
          .findViewById<DrawerLayout>(R.id.administrator_controls_activity_drawer_layout) != null

        val arguments = checkNotNull(administratorControlsFragment.arguments) {
          "Expected arguments to be passed to AdministratorControlsFragment"
        }.getProto(
          ADMINISTRATOR_CONTROLS_FRAGMENT_ARGUMENTS_KEY,
          AdministratorControlsFragmentArguments.getDefaultInstance()
        )
        val receivedIsMultipane = arguments.isMultipane

        assertThat(receivedIsMultipane).isEqualTo(isMultipane)
      }
    }
  }

  private fun clickAutoUpdateTopicContainer() {
    onView(
      atPositionOnView(
        recyclerViewId = administratorControlsListRecyclerViewId,
        position = 2,
        targetViewId = R.id.auto_update_topic_constraint_layout
      )
    ).perform(click())
  }

  private fun checkUpdateOnWifiSwitchNotClickable() {
    onView(
      atPositionOnView(
        recyclerViewId = administratorControlsListRecyclerViewId,
        position = 2,
        targetViewId = R.id.topic_update_on_wifi_switch
      )
    ).check(matches(not(isClickable())))
  }

  private fun checkAutoUpdateTopicSwitchNotClickable() {
    onView(
      atPositionOnView(
        recyclerViewId = administratorControlsListRecyclerViewId,
        position = 2,
        targetViewId = R.id.auto_update_topic_switch
      )
    ).check(matches(not(isClickable())))
  }

  private fun checkUpdateOnWifiSwitchNotChecked() {
    onView(
      atPositionOnView(
        recyclerViewId = administratorControlsListRecyclerViewId,
        position = 2,
        targetViewId = R.id.topic_update_on_wifi_switch
      )
    ).check(matches(not(isChecked())))
  }

  private fun clickAutoUpdateTopicSwitch() {
    onView(
      atPositionOnView(
        recyclerViewId = administratorControlsListRecyclerViewId,
        position = 2,
        targetViewId = R.id.auto_update_topic_switch
      )
    ).perform(click())
  }

  private fun checkAutoUpdateTopicSwitchIsChecked() {
    onView(
      atPositionOnView(
        recyclerViewId = administratorControlsListRecyclerViewId,
        position = 2,
        targetViewId = R.id.auto_update_topic_switch
      )
    ).check(matches(isChecked()))
  }

  private fun checkAutoUpdateSwitchIsUnchecked() {
    onView(
      atPositionOnView(
        recyclerViewId = administratorControlsListRecyclerViewId,
        position = 2,
        targetViewId = R.id.auto_update_topic_switch
      )
    ).check(matches(not(isChecked())))
  }

  private fun checkUpdateOnWifiSwitchIsChecked() {
    onView(
      atPositionOnView(
        recyclerViewId = administratorControlsListRecyclerViewId,
        position = 2,
        targetViewId = R.id.topic_update_on_wifi_switch
      )
    ).check(matches(isChecked()))
  }

  private fun clickUpdateOnWifiSwitch() {
    onView(atAdminControlsItem(position = 2, R.id.topic_update_on_wifi_constraint_layout)).perform(
      click()
    )
  }

  private fun atAdminControlsItem(position: Int, viewId: Int): Matcher<View> {
    return atPositionOnView(
      recyclerViewId = administratorControlsListRecyclerViewId,
      position,
      viewId
    )
  }

  private fun createAdministratorControlsFragmentTestActivityIntent(profileId: Int): Intent {
    return AdministratorControlsFragmentTestActivity
      .createAdministratorControlsFragmentTestActivityIntent(
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

  @Singleton
  @Component(
    modules = [
      RobolectricModule::class,
      TestPlatformParameterModule::class, PlatformParameterSingletonModule::class,
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
      DeveloperOptionsStarterModule::class, DeveloperOptionsModule::class,
      ExplorationStorageModule::class, NetworkModule::class, NetworkConfigProdModule::class,
      NetworkConnectionUtilDebugModule::class, NetworkConnectionDebugUtilModule::class,
      AssetModule::class, LocaleProdModule::class, ActivityRecreatorTestModule::class,
      NumericExpressionInputModule::class, AlgebraicExpressionInputModule::class,
      MathEquationInputModule::class, SplitScreenInteractionModule::class,
      LoggingIdentifierModule::class, ApplicationLifecycleModule::class,
      SyncStatusModule::class, MetricLogSchedulerModule::class, TestingBuildFlavorModule::class,
      ActivityRouterModule::class,
      CpuPerformanceSnapshotterModule::class, ExplorationProgressModule::class,
      TestAuthenticationModule::class
    ]
  )
  interface TestApplicationComponent : ApplicationComponent {
    @Component.Builder
    interface Builder : ApplicationComponent.Builder {
      override fun build(): TestApplicationComponent
    }

    fun inject(administratorControlsFragmentTest: AdministratorControlsFragmentTest)
  }

  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerAdministratorControlsFragmentTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    fun inject(administratorControlsFragmentTest: AdministratorControlsFragmentTest) =
      component.inject(administratorControlsFragmentTest)

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }
}
