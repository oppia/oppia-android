package org.oppia.android.app.player.exploration

import android.app.Application
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.Component
import org.junit.After
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
import org.oppia.android.app.model.EventLog
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.OPEN_EXPLORATION_ACTIVITY
import org.oppia.android.app.model.ExplorationActivityParams
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.Spotlight
import org.oppia.android.app.player.state.itemviewmodel.SplitScreenInteractionModule
import org.oppia.android.app.shim.IntentFactoryShimModule
import org.oppia.android.app.shim.ViewBindingShimModule
import org.oppia.android.app.testing.ExplorationInjectionActivity
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
import org.oppia.android.domain.classroom.TEST_CLASSROOM_ID_0
import org.oppia.android.domain.exploration.ExplorationDataController
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
import org.oppia.android.domain.profile.ProfileManagementController
import org.oppia.android.domain.question.QuestionModule
import org.oppia.android.domain.spotlight.SpotlightStateController
import org.oppia.android.domain.topic.TEST_EXPLORATION_ID_2
import org.oppia.android.domain.topic.TEST_STORY_ID_0
import org.oppia.android.domain.topic.TEST_TOPIC_ID_0
import org.oppia.android.domain.workmanager.WorkManagerConfigurationModule
import org.oppia.android.testing.FakeAnalyticsEventLogger
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.firebase.TestAuthenticationModule
import org.oppia.android.testing.junit.InitializeDefaultLocaleRule
import org.oppia.android.testing.platformparameter.TestPlatformParameterModule
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClock
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
import org.oppia.android.util.networking.NetworkConnectionUtil
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
  application = ExplorationActivityLocalTest.TestApplication::class,
  qualifiers = "port-xxhdpi"
)
class ExplorationActivityLocalTest {
  @get:Rule
  val initializeDefaultLocaleRule = InitializeDefaultLocaleRule()

  @Inject
  lateinit var fakeAnalyticsEventLogger: FakeAnalyticsEventLogger

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @Inject
  lateinit var fakeOppiaClock: FakeOppiaClock

  @Inject
  lateinit var spotlightStateController: SpotlightStateController

  @Inject
  lateinit var profileManagementController: ProfileManagementController

  private lateinit var networkConnectionUtil: NetworkConnectionUtil
  private lateinit var explorationDataController: ExplorationDataController
  private val internalProfileId: Int = 0
  private val afternoonUtcTimestampMillis = 1556101812000

  @After
  fun tearDown() {
    testCoroutineDispatchers.unregisterIdlingResource()
  }

  @Test
  fun testExploration_onLaunch_logsEvent() {
    setUpTestApplicationComponent()
    getApplicationDependencies(
      internalProfileId,
      TEST_CLASSROOM_ID_0,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2
    )
    launch<ExplorationActivity>(
      createExplorationActivityIntent(
        internalProfileId,
        TEST_CLASSROOM_ID_0,
        TEST_TOPIC_ID_0,
        TEST_STORY_ID_0,
        TEST_EXPLORATION_ID_2
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      val event = fakeAnalyticsEventLogger.getOldestEvent()

      assertThat(event.context.activityContextCase).isEqualTo(OPEN_EXPLORATION_ACTIVITY)
      assertThat(event.priority).isEqualTo(EventLog.Priority.ESSENTIAL)
      assertThat(event.context.openExplorationActivity.explorationId).matches(TEST_EXPLORATION_ID_2)
      assertThat(event.context.openExplorationActivity.topicId).matches(TEST_TOPIC_ID_0)
      assertThat(event.context.openExplorationActivity.storyId).matches(TEST_STORY_ID_0)
    }
  }

  @Test
  fun testExplorationActivity_closeExploration_surveyGatingCriteriaMet_showsSurveyPopup() {
    setUpTestWithNpsEnabled()
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_FIXED_FAKE_TIME)
    fakeOppiaClock.setCurrentTimeMs(afternoonUtcTimestampMillis)

    getApplicationDependencies(
      internalProfileId,
      TEST_CLASSROOM_ID_0,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2
    )
    markAllSpotlightsSeen()

    launch<ExplorationActivity>(
      createExplorationActivityIntent(
        internalProfileId,
        TEST_CLASSROOM_ID_0,
        TEST_TOPIC_ID_0,
        TEST_STORY_ID_0,
        TEST_EXPLORATION_ID_2
      )
    ).use {
      explorationDataController.startPlayingNewExploration(
        internalProfileId,
        TEST_CLASSROOM_ID_0,
        TEST_TOPIC_ID_0,
        TEST_STORY_ID_0,
        TEST_EXPLORATION_ID_2
      )
      testCoroutineDispatchers.runCurrent()

      fakeOppiaClock.setCurrentTimeMs(afternoonUtcTimestampMillis + 360_000L)

      onView(withContentDescription(R.string.nav_app_bar_navigate_up_description))
        .perform(click())
      onView(withText(R.string.stop_exploration_dialog_leave_button))
        .inRoot(isDialog())
        .perform(click())
      onView(withText(R.string.stop_exploration_dialog_leave_button))
        .inRoot(isDialog())
      testCoroutineDispatchers.runCurrent()

      onView(withText(R.string.survey_onboarding_title_text))
        .inRoot(isDialog())
        .check(matches(isDisplayed()))
      onView(withText(R.string.survey_onboarding_message_text))
        .inRoot(isDialog())
        .check(matches(isDisplayed()))
    }
  }

  @Test
  fun testExplorationActivity_closeExploration_surveyGatingCriteriaNotMet_noSurveyPopup() {
    setUpTestWithNpsEnabled()
    getApplicationDependencies(
      internalProfileId,
      TEST_CLASSROOM_ID_0,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2
    )

    markAllSpotlightsSeen()

    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_FIXED_FAKE_TIME)
    fakeOppiaClock.setCurrentTimeMs(afternoonUtcTimestampMillis)

    launch<ExplorationActivity>(
      createExplorationActivityIntent(
        internalProfileId,
        TEST_CLASSROOM_ID_0,
        TEST_TOPIC_ID_0,
        TEST_STORY_ID_0,
        TEST_EXPLORATION_ID_2
      )
    ).use {
      explorationDataController.startPlayingNewExploration(
        internalProfileId,
        TEST_CLASSROOM_ID_0,
        TEST_TOPIC_ID_0,
        TEST_STORY_ID_0,
        TEST_EXPLORATION_ID_2
      )
      testCoroutineDispatchers.runCurrent()

      // Time not advanced to simulate minimum aggregate learning time not achieved.
      onView(withContentDescription(R.string.nav_app_bar_navigate_up_description))
        .perform(click())
      onView(withText(R.string.stop_exploration_dialog_leave_button))
        .inRoot(isDialog())
        .perform(click())

      onView(withText(R.string.survey_onboarding_title_text))
        .check(ViewAssertions.doesNotExist())
    }
  }

  @Test
  fun testExplorationActivity_updateGatingProvider_surveyGatingCriteriaMet_keepsSurveyDialog() {
    setUpTestWithNpsEnabled()
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_FIXED_FAKE_TIME)
    fakeOppiaClock.setCurrentTimeMs(afternoonUtcTimestampMillis)

    getApplicationDependencies(
      internalProfileId,
      TEST_CLASSROOM_ID_0,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2
    )
    markAllSpotlightsSeen()

    launch<ExplorationActivity>(
      createExplorationActivityIntent(
        internalProfileId,
        TEST_CLASSROOM_ID_0,
        TEST_TOPIC_ID_0,
        TEST_STORY_ID_0,
        TEST_EXPLORATION_ID_2
      )
    ).use {
      explorationDataController.startPlayingNewExploration(
        internalProfileId,
        TEST_CLASSROOM_ID_0,
        TEST_TOPIC_ID_0,
        TEST_STORY_ID_0,
        TEST_EXPLORATION_ID_2
      )
      testCoroutineDispatchers.runCurrent()

      fakeOppiaClock.setCurrentTimeMs(afternoonUtcTimestampMillis + 360_000L)

      onView(withContentDescription(R.string.nav_app_bar_navigate_up_description))
        .perform(click())
      onView(withText(R.string.stop_exploration_dialog_leave_button))
        .inRoot(isDialog())
        .perform(click())

      testCoroutineDispatchers.runCurrent()

      onView(withText(R.string.survey_onboarding_title_text))
        .inRoot(isDialog())
        .check(matches(isDisplayed()))
      onView(withText(R.string.survey_onboarding_message_text))
        .inRoot(isDialog())
        .check(matches(isDisplayed()))

      // Update the SurveyLastShownTimestamp to trigger an update in the data provider and notify
      // subscribers of an update.
      profileManagementController.updateSurveyLastShownTimestamp(
        ProfileId.newBuilder().setInternalId(internalProfileId).build()
      )

      onView(withText(R.string.survey_onboarding_title_text))
        .inRoot(isDialog())
        .check(matches(isDisplayed()))
      onView(withText(R.string.survey_onboarding_message_text))
        .inRoot(isDialog())
        .check(matches(isDisplayed()))
    }
  }

  private fun setUpTestWithNpsEnabled() {
    TestPlatformParameterModule.forceEnableNpsSurvey(true)
    setUpTestApplicationComponent()
  }

  private fun markAllSpotlightsSeen() {
    markSpotlightSeen(Spotlight.FeatureCase.LESSONS_BACK_BUTTON)
    markSpotlightSeen(Spotlight.FeatureCase.VOICEOVER_PLAY_ICON)
    markSpotlightSeen(Spotlight.FeatureCase.VOICEOVER_LANGUAGE_ICON)
  }

  private fun markSpotlightSeen(feature: Spotlight.FeatureCase) {
    val profileId = ProfileId.newBuilder().setInternalId(internalProfileId).build()
    spotlightStateController.markSpotlightViewed(profileId, feature)
    testCoroutineDispatchers.runCurrent()
  }

  private fun getApplicationDependencies(
    internalProfileId: Int,
    classroomId: String,
    topicId: String,
    storyId: String,
    explorationId: String
  ) {
    launch(ExplorationInjectionActivity::class.java).use {
      it.onActivity { activity ->
        networkConnectionUtil = activity.networkConnectionUtil
        explorationDataController = activity.explorationDataController
        explorationDataController.startPlayingNewExploration(
          internalProfileId,
          classroomId,
          topicId,
          storyId,
          explorationId
        )
      }
    }
  }

  private fun createExplorationActivityIntent(
    internalProfileId: Int,
    classroomId: String,
    topicId: String,
    storyId: String,
    explorationId: String
  ): Intent {
    return ExplorationActivity.createExplorationActivityIntent(
      ApplicationProvider.getApplicationContext(),
      ProfileId.newBuilder().apply { internalId = internalProfileId }.build(),
      classroomId,
      topicId,
      storyId,
      explorationId,
      parentScreen = ExplorationActivityParams.ParentScreen.PARENT_SCREEN_UNSPECIFIED,
      isCheckpointingEnabled = false
    )
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
    testCoroutineDispatchers.registerIdlingResource()
  }

  // TODO(#59): Figure out a way to reuse modules instead of needing to re-declare them.
  @Singleton
  @Component(
    modules = [
      TestDispatcherModule::class, ApplicationModule::class, RobolectricModule::class,
      TestPlatformParameterModule::class, PlatformParameterSingletonModule::class,
      LoggerModule::class, ContinueModule::class, FractionInputModule::class,
      ItemSelectionInputModule::class, MultipleChoiceInputModule::class,
      NumberWithUnitsRuleModule::class, NumericInputRuleModule::class, TextInputRuleModule::class,
      DragDropSortInputModule::class, InteractionsModule::class, GcsResourceModule::class,
      GlideImageLoaderModule::class, ImageParsingModule::class, HtmlParserEntityTypeModule::class,
      QuestionModule::class, TestLogReportingModule::class, AccessibilityTestModule::class,
      ImageClickInputModule::class, LogStorageModule::class, IntentFactoryShimModule::class,
      ViewBindingShimModule::class, CachingTestModule::class, RatioInputModule::class,
      ExpirationMetaDataRetrieverModule::class,
      ApplicationStartupListenerModule::class, LogReportWorkerModule::class,
      WorkManagerConfigurationModule::class, HintsAndSolutionConfigModule::class,
      FirebaseLogUploaderModule::class, FakeOppiaClockModule::class,
      DeveloperOptionsStarterModule::class, DeveloperOptionsModule::class,
      ExplorationStorageModule::class, NetworkModule::class, HintsAndSolutionProdModule::class,
      NetworkConnectionUtilDebugModule::class, NetworkConnectionDebugUtilModule::class,
      AssetModule::class, LocaleProdModule::class, ActivityRecreatorTestModule::class,
      NetworkConfigProdModule::class, NumericExpressionInputModule::class,
      AlgebraicExpressionInputModule::class, MathEquationInputModule::class,
      SplitScreenInteractionModule::class,
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

    fun inject(explorationActivityLocalTest: ExplorationActivityLocalTest)
  }

  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerExplorationActivityLocalTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    fun inject(explorationActivityLocalTest: ExplorationActivityLocalTest) {
      component.inject(explorationActivityLocalTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }
}
