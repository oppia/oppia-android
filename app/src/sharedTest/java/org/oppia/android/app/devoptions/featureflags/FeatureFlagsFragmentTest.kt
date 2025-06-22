package org.oppia.android.app.devoptions.featureflags

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isChecked
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.Component
import org.hamcrest.Matchers.not
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
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
import org.oppia.android.app.devoptions.featureflags.testing.FeatureFlagTestActivity
import org.oppia.android.app.model.EphemeralFeatureFlag
import org.oppia.android.app.model.SyncStatus
import org.oppia.android.app.player.state.itemviewmodel.SplitScreenInteractionModule
import org.oppia.android.app.recyclerview.RecyclerViewMatcher.Companion.atPositionOnView
import org.oppia.android.app.shim.ViewBindingShimModule
import org.oppia.android.app.test.R
import org.oppia.android.app.translation.testing.ActivityRecreatorTestModule
import org.oppia.android.app.utility.OrientationChangeAction.Companion.orientationLandscape
import org.oppia.android.data.backends.gae.NetworkConfigProdModule
import org.oppia.android.data.backends.gae.RetrofitModule
import org.oppia.android.data.backends.gae.RetrofitServiceModule
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
import org.oppia.android.domain.platformparameter.PlatformParameterControllerDebugImpl
import org.oppia.android.domain.platformparameter.PlatformParameterSingletonModule
import org.oppia.android.domain.question.QuestionModule
import org.oppia.android.domain.workmanager.WorkManagerConfigurationModule
import org.oppia.android.testing.OppiaTestRule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.data.DataProviderTestMonitor
import org.oppia.android.testing.firebase.TestAuthenticationModule
import org.oppia.android.testing.junit.InitializeDefaultLocaleRule
import org.oppia.android.testing.platformparameter.TestPlatformParameterModule
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

/** Tests for [FeatureFlagsFragment]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(
  application = FeatureFlagsFragmentTest.TestApplication::class
)
class FeatureFlagsFragmentTest {
  @get:Rule
  val initializeDefaultLocaleRule = InitializeDefaultLocaleRule()

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @Inject
  lateinit var context: Context

  @Inject
  lateinit var platformParameterControllerDebugImpl: PlatformParameterControllerDebugImpl

  @Inject
  lateinit var monitorFactory: DataProviderTestMonitor.Factory

  @get:Rule
  val oppiaTestRule = OppiaTestRule()

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
    testCoroutineDispatchers.registerIdlingResource()
  }

  @Test
  fun testFeatureFlagsFragment_allFeatureFlagsAreCorrectlyDisplayed() {
    launch(FeatureFlagTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()

      scrollToPosition(position = 0)
      verifyTextOnFeatureFlagListItemAtPosition(
        itemPosition = 0,
        stringToMatch = "Downloads Support"
      )

      scrollToPosition(position = 1)
      verifyTextOnFeatureFlagListItemAtPosition(
        itemPosition = 1,
        stringToMatch = "Extra Topic Tabs Ui"
      )

      scrollToPosition(position = 2)
      verifyTextOnFeatureFlagListItemAtPosition(
        itemPosition = 2,
        stringToMatch = "Learner Study Analytics"
      )

      scrollToPosition(position = 3)
      verifyTextOnFeatureFlagListItemAtPosition(
        itemPosition = 3,
        stringToMatch = "Fast Language Switching In Lesson"
      )

      scrollToPosition(position = 4)
      verifyTextOnFeatureFlagListItemAtPosition(
        itemPosition = 4,
        stringToMatch = "Logging Learner Study Ids"
      )

      scrollToPosition(position = 5)
      verifyTextOnFeatureFlagListItemAtPosition(
        itemPosition = 5,
        stringToMatch = "Edit Accounts Options Ui"
      )

      scrollToPosition(position = 6)
      verifyTextOnFeatureFlagListItemAtPosition(
        itemPosition = 6,
        stringToMatch = "Performance Metrics Collection"
      )

      scrollToPosition(position = 7)
      verifyTextOnFeatureFlagListItemAtPosition(
        itemPosition = 7,
        stringToMatch = "Spotlight Ui"
      )

      scrollToPosition(position = 8)
      verifyTextOnFeatureFlagListItemAtPosition(
        itemPosition = 8,
        stringToMatch = "Interaction Config Change State Retention"
      )

      scrollToPosition(position = 9)
      verifyTextOnFeatureFlagListItemAtPosition(
        itemPosition = 9,
        stringToMatch = "App And Os Deprecation"
      )

      scrollToPosition(position = 10)
      verifyTextOnFeatureFlagListItemAtPosition(
        itemPosition = 10,
        stringToMatch = "Nps Survey"
      )

      scrollToPosition(position = 11)
      verifyTextOnFeatureFlagListItemAtPosition(
        itemPosition = 11,
        stringToMatch = "Onboarding Flow V2"
      )

      scrollToPosition(position = 12)
      verifyTextOnFeatureFlagListItemAtPosition(
        itemPosition = 12,
        stringToMatch = "Multiple Classrooms"
      )

      scrollToPosition(position = 13)
      verifyTextOnFeatureFlagListItemAtPosition(
        itemPosition = 13,
        stringToMatch = "Flashback Support"
      )
    }
  }

  @Test
  fun testFeatureFlagsFragment_configChange_allFeatureFlagsAreCorrectlyDisplayed() {
    launch(FeatureFlagTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      onView(ViewMatchers.isRoot()).perform(orientationLandscape())
      scrollToPosition(position = 0)
      verifyTextOnFeatureFlagListItemAtPosition(
        itemPosition = 0,
        stringToMatch = "Downloads Support"
      )

      scrollToPosition(position = 1)
      verifyTextOnFeatureFlagListItemAtPosition(
        itemPosition = 1,
        stringToMatch = "Extra Topic Tabs Ui"
      )

      scrollToPosition(position = 2)
      verifyTextOnFeatureFlagListItemAtPosition(
        itemPosition = 2,
        stringToMatch = "Learner Study Analytics"
      )

      scrollToPosition(position = 3)
      verifyTextOnFeatureFlagListItemAtPosition(
        itemPosition = 3,
        stringToMatch = "Fast Language Switching In Lesson"
      )

      scrollToPosition(position = 4)
      verifyTextOnFeatureFlagListItemAtPosition(
        itemPosition = 4,
        stringToMatch = "Logging Learner Study Ids"
      )

      scrollToPosition(position = 5)
      verifyTextOnFeatureFlagListItemAtPosition(
        itemPosition = 5,
        stringToMatch = "Edit Accounts Options Ui"
      )

      scrollToPosition(position = 6)
      verifyTextOnFeatureFlagListItemAtPosition(
        itemPosition = 6,
        stringToMatch = "Performance Metrics Collection"
      )

      scrollToPosition(position = 7)
      verifyTextOnFeatureFlagListItemAtPosition(
        itemPosition = 7,
        stringToMatch = "Spotlight Ui"
      )

      scrollToPosition(position = 8)
      verifyTextOnFeatureFlagListItemAtPosition(
        itemPosition = 8,
        stringToMatch = "Interaction Config Change State Retention"
      )

      scrollToPosition(position = 9)
      verifyTextOnFeatureFlagListItemAtPosition(
        itemPosition = 9,
        stringToMatch = "App And Os Deprecation"
      )

      scrollToPosition(position = 10)
      verifyTextOnFeatureFlagListItemAtPosition(
        itemPosition = 10,
        stringToMatch = "Nps Survey"
      )

      scrollToPosition(position = 11)
      verifyTextOnFeatureFlagListItemAtPosition(
        itemPosition = 11,
        stringToMatch = "Onboarding Flow V2"
      )

      scrollToPosition(position = 12)
      verifyTextOnFeatureFlagListItemAtPosition(
        itemPosition = 12,
        stringToMatch = "Multiple Classrooms"
      )

      scrollToPosition(position = 13)
      verifyTextOnFeatureFlagListItemAtPosition(
        itemPosition = 13,
        stringToMatch = "Flashback Support"
      )
    }
  }
  @Test
  fun testFeatureFlagsFragment_syncStatusIsCorrectlyDisplayed() {
    launch(FeatureFlagTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()

      scrollToPosition(position = 0)
      verifyTextOnFeatureFlagSyncStatusLabelAtPosition(
        itemPosition = 0,
        stringToMatch = getSyncStatusText(getFeatureFlagAtPosition(position = 0).syncStatus)
      )

      scrollToPosition(position = 1)
      verifyTextOnFeatureFlagSyncStatusLabelAtPosition(
        itemPosition = 1,
        stringToMatch = getSyncStatusText(getFeatureFlagAtPosition(position = 1).syncStatus)
      )

      scrollToPosition(position = 2)
      verifyTextOnFeatureFlagSyncStatusLabelAtPosition(
        itemPosition = 2,
        stringToMatch = getSyncStatusText(getFeatureFlagAtPosition(position = 2).syncStatus)
      )

      scrollToPosition(position = 3)
      verifyTextOnFeatureFlagSyncStatusLabelAtPosition(
        itemPosition = 3,
        stringToMatch = getSyncStatusText(getFeatureFlagAtPosition(position = 3).syncStatus)
      )

      scrollToPosition(position = 4)
      verifyTextOnFeatureFlagSyncStatusLabelAtPosition(
        itemPosition = 4,
        stringToMatch = getSyncStatusText(getFeatureFlagAtPosition(position = 4).syncStatus)
      )

      scrollToPosition(position = 5)
      verifyTextOnFeatureFlagSyncStatusLabelAtPosition(
        itemPosition = 5,
        stringToMatch = getSyncStatusText(getFeatureFlagAtPosition(position = 5).syncStatus)
      )

      scrollToPosition(position = 6)
      verifyTextOnFeatureFlagSyncStatusLabelAtPosition(
        itemPosition = 6,
        stringToMatch = getSyncStatusText(getFeatureFlagAtPosition(position = 6).syncStatus)
      )

      scrollToPosition(position = 7)
      verifyTextOnFeatureFlagSyncStatusLabelAtPosition(
        itemPosition = 7,
        stringToMatch = getSyncStatusText(getFeatureFlagAtPosition(position = 7).syncStatus)
      )

      scrollToPosition(position = 8)
      verifyTextOnFeatureFlagSyncStatusLabelAtPosition(
        itemPosition = 8,
        stringToMatch = getSyncStatusText(getFeatureFlagAtPosition(position = 8).syncStatus)
      )

      scrollToPosition(position = 9)
      verifyTextOnFeatureFlagSyncStatusLabelAtPosition(
        itemPosition = 9,
        stringToMatch = getSyncStatusText(getFeatureFlagAtPosition(position = 9).syncStatus)
      )

      scrollToPosition(position = 10)
      verifyTextOnFeatureFlagSyncStatusLabelAtPosition(
        itemPosition = 10,
        stringToMatch = getSyncStatusText(getFeatureFlagAtPosition(position = 10).syncStatus)
      )

      scrollToPosition(position = 11)
      verifyTextOnFeatureFlagSyncStatusLabelAtPosition(
        itemPosition = 11,
        stringToMatch = getSyncStatusText(getFeatureFlagAtPosition(position = 11).syncStatus)
      )

      scrollToPosition(position = 12)
      verifyTextOnFeatureFlagSyncStatusLabelAtPosition(
        itemPosition = 12,
        stringToMatch = getSyncStatusText(getFeatureFlagAtPosition(position = 12).syncStatus)
      )

      scrollToPosition(position = 13)
      verifyTextOnFeatureFlagSyncStatusLabelAtPosition(
        itemPosition = 13,
        stringToMatch = getSyncStatusText(getFeatureFlagAtPosition(position = 13).syncStatus)
      )
    }
  }

  @Test
  fun testFeatureFlagsFragment_overrideFeatureFlag_configChange_changePersists() {
    launch(FeatureFlagTestActivity::class.java).use {
      scrollToPosition(position = 0)

      val initialValue = getFeatureFlagAtPosition(position = 0).currentValue
      onView(
        atPositionOnView(
          recyclerViewId = R.id.feature_flags_recycler_view,
          position = 0,
          targetViewId = R.id.feature_flag_switch
        )
      ).perform(click())

      onView(isRoot()).perform(orientationLandscape())
      testCoroutineDispatchers.runCurrent()

      val expectedValue = !initialValue
      onView(
        atPositionOnView(
          recyclerViewId = R.id.feature_flags_recycler_view,
          position = 0,
          targetViewId = R.id.feature_flag_switch
        )
      ).check(matches(if (expectedValue) isChecked() else not(isChecked())))
    }
  }

  private fun scrollToPosition(position: Int) {
    onView(withId(R.id.feature_flags_recycler_view)).perform(
      scrollToPosition<RecyclerView.ViewHolder>(position)
    )
  }

  private fun verifyTextOnFeatureFlagListItemAtPosition(
    itemPosition: Int,
    stringToMatch: String
  ) {
    onView(
      atPositionOnView(
        recyclerViewId = R.id.feature_flags_recycler_view,
        position = itemPosition,
        targetViewId = R.id.feature_flag_label_text_view
      )
    ).check(matches(withText(stringToMatch)))
  }
  private fun verifyTextOnFeatureFlagSyncStatusLabelAtPosition(
    itemPosition: Int,
    stringToMatch: String
  ) {
    onView(
      atPositionOnView(
        recyclerViewId = R.id.feature_flags_recycler_view,
        position = itemPosition,
        targetViewId = R.id.sync_status_value_text_view
      )
    ).check(matches(withText(stringToMatch)))
  }

  private fun getSyncStatusText(syncStatus: SyncStatus): String {
    return when (syncStatus) {
      SyncStatus.SYNC_STATUS_UNSPECIFIED -> "Unknown"
      SyncStatus.NOT_SYNCED_FROM_SERVER -> "Default"
      SyncStatus.SYNCED_FROM_SERVER -> "Server"
      else -> "Unknown"
    }
  }
  @After
  fun tearDown() {
    testCoroutineDispatchers.unregisterIdlingResource()
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  private fun getFeatureFlagAtPosition(position: Int): EphemeralFeatureFlag {
    val provider = platformParameterControllerDebugImpl.loadEphemeralFeatureFlags()
    return monitorFactory.waitForNextSuccessfulResult(provider)[position]
  }

  // TODO(#59): Figure out a way to reuse modules instead of needing to re-declare them.
  @Singleton
  @Component(
    modules = [
      AccessibilityTestModule::class,
      ActivityRecreatorTestModule::class,
      ActivityRouterModule::class,
      AlgebraicExpressionInputModule::class,
      ApplicationLifecycleModule::class,
      ApplicationModule::class,
      ApplicationStartupListenerModule::class,
      AssetModule::class,
      CachingTestModule::class,
      ContinueModule::class,
      CpuPerformanceSnapshotterModule::class,
      DeveloperOptionsModule::class,
      DeveloperOptionsStarterModule::class,
      DragDropSortInputModule::class,
      ExpirationMetaDataRetrieverModule::class,
      ExplorationProgressModule::class,
      ExplorationStorageModule::class,
      FakeOppiaClockModule::class,
      FirebaseLogUploaderModule::class,
      FractionInputModule::class,
      GcsResourceModule::class,
      GlideImageLoaderModule::class,
      HintsAndSolutionConfigModule::class,
      HintsAndSolutionProdModule::class,
      HtmlParserEntityTypeModule::class,
      ImageClickInputModule::class,
      ImageParsingModule::class,
      InteractionsModule::class,
      ItemSelectionInputModule::class,
      LocaleProdModule::class,
      LogReportWorkerModule::class,
      LogStorageModule::class,
      LoggerModule::class,
      LoggingIdentifierModule::class,
      MathEquationInputModule::class,
      MetricLogSchedulerModule::class,
      MultipleChoiceInputModule::class,
      NetworkConfigProdModule::class,
      NetworkConnectionDebugUtilModule::class,
      NetworkConnectionUtilDebugModule::class,
      NumberWithUnitsRuleModule::class,
      NumericExpressionInputModule::class,
      NumericInputRuleModule::class,
      PlatformParameterSingletonModule::class,
      QuestionModule::class,
      RatioInputModule::class,
      RetrofitModule::class,
      RetrofitServiceModule::class,
      RobolectricModule::class,
      SplitScreenInteractionModule::class,
      SyncStatusModule::class,
      TestAuthenticationModule::class,
      TestDispatcherModule::class,
      TestLogReportingModule::class,
      TestPlatformParameterModule::class,
      TestingBuildFlavorModule::class,
      TextInputRuleModule::class,
      ViewBindingShimModule::class,
      WorkManagerConfigurationModule::class,
      TestPlatformParameterModule::class
    ]
  )
  /** [ApplicationComponent] for [FeatureFlagsFragmentTest]. */
  interface TestApplicationComponent : ApplicationComponent {
    /** [ApplicationComponent.Builder] for [TestApplicationComponent]. */
    @Component.Builder
    interface Builder : ApplicationComponent.Builder {
      override fun build(): TestApplicationComponent
    }

    /**
     * Injects [TestApplicationComponent] to [FeatureFlagsFragmentTest] providing the required
     * dagger modules.
     */
    fun inject(featureFlagFragmentTest: FeatureFlagsFragmentTest)
  }

  /** [Application] class for [FeatureFlagsFragmentTest]. */
  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerFeatureFlagsFragmentTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    /** Called when setting up [TestApplication]. */
    fun inject(featureFlagFragmentTest: FeatureFlagsFragmentTest) {
      component.inject(featureFlagFragmentTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }
}
