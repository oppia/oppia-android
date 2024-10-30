package org.oppia.android.app.survey

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isChecked
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isEnabled
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.google.common.truth.Truth.assertThat
import dagger.Component
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.not
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
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.ScreenName
import org.oppia.android.app.model.SurveyFragmentArguments
import org.oppia.android.app.model.SurveyQuestionName
import org.oppia.android.app.player.state.itemviewmodel.SplitScreenInteractionModule
import org.oppia.android.app.recyclerview.RecyclerViewMatcher.Companion.atPositionOnView
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
import org.oppia.android.domain.platformparameter.PlatformParameterSingletonModule
import org.oppia.android.domain.question.QuestionModule
import org.oppia.android.domain.survey.SurveyController
import org.oppia.android.domain.topic.TEST_EXPLORATION_ID_2
import org.oppia.android.domain.topic.TEST_TOPIC_ID_0
import org.oppia.android.domain.workmanager.WorkManagerConfigurationModule
import org.oppia.android.testing.FakeAnalyticsEventLogger
import org.oppia.android.testing.OppiaTestRule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.firebase.TestAuthenticationModule
import org.oppia.android.testing.junit.InitializeDefaultLocaleRule
import org.oppia.android.testing.logging.EventLogSubject
import org.oppia.android.testing.platformparameter.TestPlatformParameterModule
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClock
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.accessibility.AccessibilityTestModule
import org.oppia.android.util.caching.AssetModule
import org.oppia.android.util.caching.testing.CachingTestModule
import org.oppia.android.util.extensions.getProto
import org.oppia.android.util.gcsresource.GcsResourceModule
import org.oppia.android.util.locale.LocaleProdModule
import org.oppia.android.util.logging.CurrentAppScreenNameIntentDecorator.extractCurrentAppScreenName
import org.oppia.android.util.logging.EventLoggingConfigurationModule
import org.oppia.android.util.logging.LoggerModule
import org.oppia.android.util.logging.SyncStatusModule
import org.oppia.android.util.logging.firebase.FirebaseLogUploaderModule
import org.oppia.android.util.networking.NetworkConnectionDebugUtilModule
import org.oppia.android.util.networking.NetworkConnectionUtilDebugModule
import org.oppia.android.util.parser.html.HtmlParserEntityTypeModule
import org.oppia.android.util.parser.image.GlideImageLoaderModule
import org.oppia.android.util.parser.image.ImageParsingModule
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.extractCurrentUserProfileId
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [SurveyFragment]. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(
  application = SurveyFragmentTest.TestApplication::class,
  qualifiers = "port-xxhdpi"
)
class SurveyFragmentTest {
  @get:Rule
  val initializeDefaultLocaleRule = InitializeDefaultLocaleRule()

  @get:Rule
  val oppiaTestRule = OppiaTestRule()

  @get:Rule
  var activityTestRule: ActivityTestRule<SurveyActivity> = ActivityTestRule(
    SurveyActivity::class.java, /* initialTouchMode= */ true, /* launchActivity= */ false
  )

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @Inject
  lateinit var fakeOppiaClock: FakeOppiaClock

  @Inject
  lateinit var fakeAnalyticsEventLogger: FakeAnalyticsEventLogger

  @Inject
  lateinit var context: Context

  @Inject
  lateinit var surveyController: SurveyController

  private val profileId = ProfileId.newBuilder().setInternalId(0).build()

  @Before
  fun setup() {
    Intents.init()
    setUpTestApplicationComponent()
    testCoroutineDispatchers.registerIdlingResource()
  }

  @After
  fun tearDown() {
    testCoroutineDispatchers.unregisterIdlingResource()
    Intents.release()
  }

  @Test
  fun testSurveyActivity_createIntent_verifyScreenNameInIntent() {
    val screenName = createSurveyActivityIntent()
      .extractCurrentAppScreenName()

    assertThat(screenName).isEqualTo(ScreenName.SURVEY_ACTIVITY)
  }

  @Test
  fun testSurveyFragment_closeButtonIsDisplayed() {
    launch<SurveyActivity>(
      createSurveyActivityIntent()
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(withContentDescription(R.string.survey_exit_button_description))
        .check(
          matches(
            withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)
          )
        )
    }
  }

  @Test
  fun testSurveyFragment_progressBarIsDisplayed() {
    launch<SurveyActivity>(
      createSurveyActivityIntent()
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.survey_progress_bar))
        .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
    }
  }

  @Test
  fun testSurveyFragment_progressTextIsDisplayed() {
    launch<SurveyActivity>(
      createSurveyActivityIntent()
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.survey_progress_text))
        .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
      onView(withId(R.id.survey_progress_text))
        .check(matches(withText("25%")))
    }
  }

  @Test
  fun testSurveyFragment_navigationContainerIsDisplayed() {
    launch<SurveyActivity>(
      createSurveyActivityIntent()
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.survey_navigation_buttons_container))
        .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
    }
  }

  @Test
  fun testSurveyFragment_beginSurvey_initialQuestionIsDisplayed() {
    startSurveySession()
    launch<SurveyActivity>(
      createSurveyActivityIntent()
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(withText(R.string.user_type_question))
        .check(matches(isDisplayed()))
      onView(withId(R.id.survey_next_button))
        .check(matches(isDisplayed()))
      onView(withId(R.id.survey_previous_button))
        .check(matches(not(isDisplayed())))
    }
  }

  @Test
  fun testSurveyFragment_beginSurvey_initialQuestion_correctOptionsDisplayed() {
    startSurveySession()
    launch<SurveyActivity>(
      createSurveyActivityIntent()
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.survey_answers_recycler_view)).perform(
        RecyclerViewActions.scrollToPosition<RecyclerView.ViewHolder>(
          0
        )
      ).check(matches(hasDescendant(withText(R.string.user_type_answer_learner))))
    }
  }

  @Test
  fun testSurveyFragment_beginSurvey_closeButtonClicked_exitConfirmationDialogDisplayed() {
    startSurveySession()
    launch<SurveyActivity>(
      createSurveyActivityIntent()
    ).use {
      onView(withContentDescription(R.string.navigate_up)).perform(click())
      onView(withText(context.getString(R.string.survey_exit_confirmation_text)))
        .inRoot(isDialog())
        .check(matches(isDisplayed()))
    }
  }

  @Test
  fun testSurveyFragment_nextButtonClicked_marketFitQuestionIsDisplayedWithCorrectOptions() {
    startSurveySession()
    launch<SurveyActivity>(
      createSurveyActivityIntent()
    ).use {
      testCoroutineDispatchers.runCurrent()

      selectMultiChoiceAnswerAndMoveToNextQuestion(0)

      onView(withText("How would you feel if you could no longer use Oppia?"))
        .check(matches(isDisplayed()))
      onView(withId(R.id.survey_next_button))
        .check(matches(isDisplayed()))
      onView(withId(R.id.survey_previous_button))
        .check(matches(isDisplayed()))

      onView(withId(R.id.survey_answers_recycler_view)).perform(
        RecyclerViewActions.scrollToPosition<RecyclerView.ViewHolder>(
          0
        )
      ).check(matches(hasDescendant(withText(R.string.market_fit_answer_very_disappointed))))
    }
  }

  @Test
  fun testSurveyNavigation_submitMarketFitAnswer_NpsQuestionIsDisplayed() {
    startSurveySession()
    launch<SurveyActivity>(
      createSurveyActivityIntent()
    ).use {
      testCoroutineDispatchers.runCurrent()

      // Select and submit userTypeAnswer
      selectMultiChoiceAnswerAndMoveToNextQuestion(0)

      // Select and submit marketFitAnswer
      selectMultiChoiceAnswerAndMoveToNextQuestion(0)

      onView(
        withText(
          "On a scale from 0–10, how likely are you to recommend Oppia to a friend" +
            " or colleague?"
        )
      )
        .check(matches(isDisplayed()))

      onView(withId(R.id.survey_answers_recycler_view))
        .check(matches(hasDescendant(withText("0"))))
      onView(withId(R.id.survey_answers_recycler_view))
        .check(matches(hasDescendant(withText("5"))))
      onView(withId(R.id.survey_answers_recycler_view))
        .check(matches(hasDescendant(withText("6"))))
      onView(withId(R.id.survey_answers_recycler_view))
        .check(matches(hasDescendant(withText("10"))))
    }
  }

  @Test
  fun testSurveyNavigation_submitNpsScoreOf3_detractorFeedbackQuestionIsDisplayed() {
    startSurveySession()
    launch<SurveyActivity>(
      createSurveyActivityIntent()
    ).use {
      testCoroutineDispatchers.runCurrent()

      // Select and submit userTypeAnswer
      selectMultiChoiceAnswerAndMoveToNextQuestion(0)

      // Select and submit marketFitAnswer
      selectMultiChoiceAnswerAndMoveToNextQuestion(0)

      // Select and submit NpsAnswer
      selectNpsAnswerAndMoveToNextQuestion(3)

      onView(withText(R.string.nps_detractor_feedback_question))
        .check(matches(isDisplayed()))
      onView(withId(R.id.submit_button))
        .check(matches(isDisplayed()))
      onView(withId(R.id.survey_previous_button))
        .check(matches(isDisplayed()))
      onView(withId(R.id.survey_next_button))
        .check(matches(not(isDisplayed())))
    }
  }

  @Test
  fun testSurveyNavigation_submitNpsScoreOf8_passiveFeedbackQuestionIsDisplayed() {
    startSurveySession()
    launch<SurveyActivity>(
      createSurveyActivityIntent()
    ).use {
      testCoroutineDispatchers.runCurrent()

      // Select and submit userTypeAnswer
      selectMultiChoiceAnswerAndMoveToNextQuestion(0)

      // Select and submit marketFitAnswer
      selectMultiChoiceAnswerAndMoveToNextQuestion(0)

      // Select and submit NpsAnswer
      selectNpsAnswerAndMoveToNextQuestion(8)

      onView(withText(R.string.nps_passive_feedback_question))
        .check(matches(isDisplayed()))
      onView(withId(R.id.submit_button))
        .check(matches(isDisplayed()))
      onView(withId(R.id.survey_previous_button))
        .check(matches(isDisplayed()))
      onView(withId(R.id.survey_next_button))
        .check(matches(not(isDisplayed())))
    }
  }

  @Test
  fun testSurveyNavigation_submitNpsScoreOf10_promoterFeedbackQuestionIsDisplayed() {
    startSurveySession()
    launch<SurveyActivity>(
      createSurveyActivityIntent()
    ).use {
      testCoroutineDispatchers.runCurrent()

      // Select and submit userTypeAnswer
      selectMultiChoiceAnswerAndMoveToNextQuestion(0)

      // Select and submit marketFitAnswer
      selectMultiChoiceAnswerAndMoveToNextQuestion(0)

      // Select and submit NpsAnswer
      selectNpsAnswerAndMoveToNextQuestion(10)

      onView(
        withText(
          "We are glad you have enjoyed your experience with Oppia. Please share " +
            "what helped you the most:"
        )
      ).check(matches(isDisplayed()))

      onView(withId(R.id.submit_button)).check(matches(isDisplayed()))
      onView(withId(R.id.survey_previous_button)).check(matches(isDisplayed()))
      onView(withId(R.id.survey_next_button)).check(matches(not(isDisplayed())))
    }
  }

  @Test
  fun testNavigation_moveToNextQuestion_thenMoveToPreviousQuestion_previousSelectionIsRestored() {
    startSurveySession()
    launch<SurveyActivity>(
      createSurveyActivityIntent()
    ).use {
      testCoroutineDispatchers.runCurrent()

      // Select and submit userTypeAnswer
      // Index 0 corresponds to "I am a learner"
      selectMultiChoiceAnswerAndMoveToNextQuestion(0)

      // Move back to previous question
      moveToPreviousQuestion()

      // Next button is enabled if an answer is available
      onView(withId(R.id.survey_next_button)).check(matches(isEnabled()))

      onView(
        allOf(
          withId(R.id.multiple_choice_radio_button),
          atPositionOnView(
            recyclerViewId = R.id.survey_answers_recycler_view,
            position = 0,
            targetViewId = R.id.multiple_choice_radio_button
          )
        )
      ).check(matches(isChecked()))

      onView(
        atPositionOnView(
          recyclerViewId = R.id.survey_answers_recycler_view,
          position = 0,
          targetViewId = R.id.multiple_choice_content_text_view
        )
      ).check(matches(withText(R.string.user_type_answer_learner)))
    }
  }

  @Test
  fun testNavigation_moveTwoQuestionsAhead_thenMoveToInitialQuestion_previousSelectionIsRestored() {
    startSurveySession()
    launch<SurveyActivity>(
      createSurveyActivityIntent()
    ).use {
      testCoroutineDispatchers.runCurrent()

      // Select and submit userTypeAnswer
      // Index 0 corresponds to "I am a learner"
      selectMultiChoiceAnswerAndMoveToNextQuestion(0)

      // Submit marketFitAnswer and move to question 3
      // Index 0 corresponds to "Very Disappointed"
      selectMultiChoiceAnswerAndMoveToNextQuestion(0)

      // Move back to marketFit question
      moveToPreviousQuestion()

      // Assert marketFit answer selection is restored
      onView(
        allOf(
          withId(R.id.multiple_choice_radio_button),
          atPositionOnView(
            recyclerViewId = R.id.survey_answers_recycler_view,
            position = 0,
            targetViewId = R.id.multiple_choice_radio_button
          )
        )
      ).check(matches(isChecked()))

      onView(
        atPositionOnView(
          recyclerViewId = R.id.survey_answers_recycler_view,
          position = 0,
          targetViewId = R.id.multiple_choice_content_text_view
        )
      ).check(matches(withText(R.string.market_fit_answer_very_disappointed)))

      // Move back to UserType question
      moveToPreviousQuestion()

      // Assert UserType answer selection is restored
      onView(
        allOf(
          withId(R.id.multiple_choice_radio_button),
          atPositionOnView(
            recyclerViewId = R.id.survey_answers_recycler_view,
            position = 0,
            targetViewId = R.id.multiple_choice_radio_button
          )
        )
      ).check(matches(isChecked()))

      onView(
        atPositionOnView(
          recyclerViewId = R.id.survey_answers_recycler_view,
          position = 0,
          targetViewId = R.id.multiple_choice_content_text_view
        )
      ).check(matches(withText(R.string.user_type_answer_learner)))
    }
  }

  @Test
  fun testSurveyFragment_beginSurvey_logsBeginSurveyEvent() {
    startSurveySession()
    launch<SurveyActivity>(
      createSurveyActivityIntent()
    ).use {
      testCoroutineDispatchers.runCurrent()

      // Verify that the "begin survey" event was logged, and with the correct values.
      val event = fakeAnalyticsEventLogger.getMostRecentEvent()
      EventLogSubject.assertThat(event).hasBeginSurveyContextThat {
        hasExplorationIdThat().isEqualTo(TEST_EXPLORATION_ID_2)
        hasTopicIdThat().isEqualTo(TEST_TOPIC_ID_0)
      }
    }
  }

  @Test
  fun testFragment_fragmentLoaded_verifyCorrectArgumentsPassed() {
    launch<SurveyActivity>(
      createSurveyActivityIntent()
    ).use { scenario ->
      testCoroutineDispatchers.runCurrent()
      scenario.onActivity { activity ->

        val surveyFragment = activity.supportFragmentManager
          .findFragmentById(R.id.survey_fragment_placeholder) as SurveyFragment
        val args = surveyFragment.arguments!!.getProto(
          SurveyFragment.SURVEY_FRAGMENT_ARGUMENTS_KEY,
          SurveyFragmentArguments.getDefaultInstance()
        )
        val receivedInternalProfileId = surveyFragment.arguments!!
          .extractCurrentUserProfileId().internalId
        val receivedTopicId = args.topicId!!
        val receivedExplorationId = args.explorationId!!

        assertThat(receivedInternalProfileId).isEqualTo(0)
        assertThat(receivedTopicId).isEqualTo(TEST_TOPIC_ID_0)
        assertThat(receivedExplorationId).isEqualTo(TEST_EXPLORATION_ID_2)
      }
    }
  }

  private fun selectNpsAnswerAndMoveToNextQuestion(npsScore: Int) {
    onView(
      allOf(
        withText(npsScore.toString()),
        isDescendantOfA(withId(R.id.survey_answers_recycler_view))
      )
    ).perform(click())
    testCoroutineDispatchers.runCurrent()

    onView(withId(R.id.survey_next_button)).perform(click())
    testCoroutineDispatchers.runCurrent()
  }

  private fun selectMultiChoiceAnswerAndMoveToNextQuestion(choiceIndex: Int) {
    onView(
      atPositionOnView(
        recyclerViewId = R.id.survey_answers_recycler_view,
        position = choiceIndex,
        targetViewId = R.id.multiple_choice_radio_button
      )
    ).perform(click())
    testCoroutineDispatchers.runCurrent()

    onView(withId(R.id.survey_next_button)).perform(click())
    testCoroutineDispatchers.runCurrent()
  }

  private fun moveToPreviousQuestion() {
    onView(withId(R.id.survey_previous_button)).perform(click())
    testCoroutineDispatchers.runCurrent()
  }

  private fun startSurveySession() {
    val questions = listOf(
      SurveyQuestionName.USER_TYPE,
      SurveyQuestionName.MARKET_FIT,
      SurveyQuestionName.NPS
    )
    val profileId = ProfileId.newBuilder().setInternalId(1).build()
    surveyController.startSurveySession(questions, profileId = profileId)
    testCoroutineDispatchers.runCurrent()
  }

  private fun createSurveyActivityIntent(): Intent {
    return SurveyActivity.createSurveyActivityIntent(
      context = context,
      profileId = profileId,
      TEST_TOPIC_ID_0,
      TEST_EXPLORATION_ID_2
    )
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  // TODO(#59): Figure out a way to reuse modules instead of needing to re-declare them.
  @Singleton
  @Component(
    modules = [
      TestPlatformParameterModule::class, RobolectricModule::class,
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
      PlatformParameterSingletonModule::class,
      NumericExpressionInputModule::class, AlgebraicExpressionInputModule::class,
      MathEquationInputModule::class, SplitScreenInteractionModule::class,
      LoggingIdentifierModule::class, ApplicationLifecycleModule::class,
      SyncStatusModule::class, MetricLogSchedulerModule::class, TestingBuildFlavorModule::class,
      EventLoggingConfigurationModule::class, ActivityRouterModule::class,
      CpuPerformanceSnapshotterModule::class, ExplorationProgressModule::class,
      TestAuthenticationModule::class
    ]
  )
  interface TestApplicationComponent : ApplicationComponent {
    @Component.Builder
    interface Builder : ApplicationComponent.Builder

    fun inject(surveyFragmentTest: SurveyFragmentTest)
  }

  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerSurveyFragmentTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    fun inject(surveyFragmentTest: SurveyFragmentTest) {
      component.inject(surveyFragmentTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }
}
