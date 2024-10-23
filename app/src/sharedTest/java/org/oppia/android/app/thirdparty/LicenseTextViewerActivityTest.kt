package org.oppia.android.app.thirdparty

import android.app.Application
import android.content.Context
import android.content.Intent
import android.text.TextUtils
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.google.common.truth.Truth.assertThat
import dagger.Component
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
import org.oppia.android.app.help.thirdparty.LicenseListActivity
import org.oppia.android.app.help.thirdparty.LicenseTextViewerActivity
import org.oppia.android.app.model.ScreenName
import org.oppia.android.app.player.state.itemviewmodel.SplitScreenInteractionModule
import org.oppia.android.app.shim.ViewBindingShimModule
import org.oppia.android.app.translation.testing.ActivityRecreatorTestModule
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
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.firebase.TestAuthenticationModule
import org.oppia.android.testing.junit.InitializeDefaultLocaleRule
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.accessibility.AccessibilityTestModule
import org.oppia.android.util.accessibility.FakeAccessibilityService
import org.oppia.android.util.caching.AssetModule
import org.oppia.android.util.caching.testing.CachingTestModule
import org.oppia.android.util.gcsresource.GcsResourceModule
import org.oppia.android.util.locale.LocaleProdModule
import org.oppia.android.util.logging.CurrentAppScreenNameIntentDecorator.extractCurrentAppScreenName
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

/** Tests for [LicenseListActivity]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(
  application = LicenseTextViewerActivityTest.TestApplication::class,
  qualifiers = "port-xxhdpi"
)
class LicenseTextViewerActivityTest {
  @get:Rule
  val initializeDefaultLocaleRule = InitializeDefaultLocaleRule()

  @get:Rule
  val oppiaTestRule = OppiaTestRule()

  @get:Rule
  val activityTestRule: ActivityTestRule<LicenseTextViewerActivity> = ActivityTestRule(
    LicenseTextViewerActivity::class.java,
    /* initialTouchMode= */ true,
    /* launchActivity= */ false
  )

  @Inject
  lateinit var fakeAccessibilityService: FakeAccessibilityService

  @Inject
  lateinit var context: Context

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testActivity_createIntent_verifyScreenNameInIntent() {
    val currentScreenName = createLicenseTextViewerActivityIntent(0, 0)
      .extractCurrentAppScreenName()

    assertThat(currentScreenName).isEqualTo(ScreenName.LICENSE_TEXT_VIEWER_ACTIVITY)
  }

  @Test
  fun testLicenseTextViewerActivity_hasCorrectActivityLabel() {
    activityTestRule.launchActivity(
      createLicenseTextViewerActivityIntent(
        dependencyIndex = 0,
        licenseIndex = 0
      )
    )
    val title = activityTestRule.activity.title

    // Verify that the activity label is correct as a proxy to verify TalkBack will announce the
    // correct string when it's read out.
    assertThat(title).isEqualTo(
      context.getString(
        R.string.license_name_0
      )
    )
  }

  @Test
  fun testExploration_toolbarTitle_readerOff_marqueeInRtl_isDisplayedCorrectly() {
    fakeAccessibilityService.setScreenReaderEnabled(false)
    activityTestRule.launchActivity(
      createLicenseTextViewerActivityIntent(
        dependencyIndex = 0,
        licenseIndex = 0
      )
    )

    val activityToolbarTitle: TextView =
      activityTestRule.activity.findViewById(R.id.license_text_viewer_activity_toolbar_title)
    ViewCompat.setLayoutDirection(activityToolbarTitle, ViewCompat.LAYOUT_DIRECTION_RTL)

    Espresso.onView(ViewMatchers.withId(R.id.license_text_viewer_activity_toolbar_title))
      .perform(ViewActions.click())
    assertThat(activityToolbarTitle.ellipsize).isEqualTo(TextUtils.TruncateAt.MARQUEE)
    assertThat(activityToolbarTitle.isSelected).isEqualTo(true)
    assertThat(activityToolbarTitle.textAlignment).isEqualTo(View.TEXT_ALIGNMENT_VIEW_START)
  }

  @Test
  fun testExploration_toolbarTitle_readerOn_marqueeInRtl_isDisplayedCorrectly() {
    fakeAccessibilityService.setScreenReaderEnabled(true)
    activityTestRule.launchActivity(
      createLicenseTextViewerActivityIntent(
        dependencyIndex = 0,
        licenseIndex = 0
      )
    )

    val activityToolbarTitle: TextView =
      activityTestRule.activity.findViewById(R.id.license_text_viewer_activity_toolbar_title)
    ViewCompat.setLayoutDirection(activityToolbarTitle, ViewCompat.LAYOUT_DIRECTION_RTL)

    Espresso.onView(ViewMatchers.withId(R.id.license_text_viewer_activity_toolbar_title))
      .perform(ViewActions.click())
    assertThat(activityToolbarTitle.ellipsize).isEqualTo(TextUtils.TruncateAt.MARQUEE)
    assertThat(activityToolbarTitle.isSelected).isEqualTo(false)
    assertThat(activityToolbarTitle.textAlignment).isEqualTo(View.TEXT_ALIGNMENT_VIEW_START)
  }

  @Test
  fun testExploration_toolbarTitle_readerOff_marqueeInLtr_isDisplayedCorrectly() {
    fakeAccessibilityService.setScreenReaderEnabled(false)
    activityTestRule.launchActivity(
      createLicenseTextViewerActivityIntent(
        dependencyIndex = 0,
        licenseIndex = 0
      )
    )

    val activityToolbarTitle: TextView =
      activityTestRule.activity.findViewById(R.id.license_text_viewer_activity_toolbar_title)
    ViewCompat.setLayoutDirection(activityToolbarTitle, ViewCompat.LAYOUT_DIRECTION_LTR)

    Espresso.onView(ViewMatchers.withId(R.id.license_text_viewer_activity_toolbar_title))
      .perform(ViewActions.click())
    assertThat(activityToolbarTitle.ellipsize).isEqualTo(TextUtils.TruncateAt.MARQUEE)
    assertThat(activityToolbarTitle.isSelected).isEqualTo(true)
    assertThat(activityToolbarTitle.textAlignment).isEqualTo(View.TEXT_ALIGNMENT_VIEW_START)
  }

  @Test
  fun testExploration_toolbarTitle_readerOn_marqueeInLtr_isDisplayedCorrectly() {
    fakeAccessibilityService.setScreenReaderEnabled(true)
    activityTestRule.launchActivity(
      createLicenseTextViewerActivityIntent(
        dependencyIndex = 0,
        licenseIndex = 0
      )
    )

    val activityToolbarTitle: TextView =
      activityTestRule.activity.findViewById(R.id.license_text_viewer_activity_toolbar_title)
    ViewCompat.setLayoutDirection(activityToolbarTitle, ViewCompat.LAYOUT_DIRECTION_LTR)

    Espresso.onView(ViewMatchers.withId(R.id.license_text_viewer_activity_toolbar_title))
      .perform(ViewActions.click())
    assertThat(activityToolbarTitle.ellipsize).isEqualTo(TextUtils.TruncateAt.MARQUEE)
    assertThat(activityToolbarTitle.isSelected).isEqualTo(false)
    assertThat(activityToolbarTitle.textAlignment).isEqualTo(View.TEXT_ALIGNMENT_VIEW_START)
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  private fun createLicenseTextViewerActivityIntent(
    dependencyIndex: Int,
    licenseIndex: Int
  ): Intent {
    return LicenseTextViewerActivity.createLicenseTextViewerActivityIntent(
      ApplicationProvider.getApplicationContext(),
      dependencyIndex,
      licenseIndex
    )
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
      DeveloperOptionsStarterModule::class, DeveloperOptionsModule::class,
      ExplorationStorageModule::class, NetworkModule::class, NetworkConfigProdModule::class,
      NetworkConnectionUtilDebugModule::class, NetworkConnectionDebugUtilModule::class,
      AssetModule::class, LocaleProdModule::class, ActivityRecreatorTestModule::class,
      NumericExpressionInputModule::class, AlgebraicExpressionInputModule::class,
      MathEquationInputModule::class, SplitScreenInteractionModule::class,
      LoggingIdentifierModule::class, ApplicationLifecycleModule::class, SyncStatusModule::class,
      MetricLogSchedulerModule::class, TestingBuildFlavorModule::class,
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

    fun inject(licenseTextViewerActivityTest: LicenseTextViewerActivityTest)
  }

  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerLicenseTextViewerActivityTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    fun inject(licenseTextViewerActivityTest: LicenseTextViewerActivityTest) {
      component.inject(licenseTextViewerActivityTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }
}
