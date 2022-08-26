package org.oppia.android.domain.exploration

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.extensions.proto.LiteProtoTruth.assertThat
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.app.model.AnswerOutcome
import org.oppia.android.app.model.CheckpointState
import org.oppia.android.app.model.ClickOnImage
import org.oppia.android.app.model.EphemeralState
import org.oppia.android.app.model.EphemeralState.StateTypeCase.COMPLETED_STATE
import org.oppia.android.app.model.EphemeralState.StateTypeCase.PENDING_STATE
import org.oppia.android.app.model.EphemeralState.StateTypeCase.TERMINAL_STATE
import org.oppia.android.app.model.ExplorationCheckpoint
import org.oppia.android.app.model.Fraction
import org.oppia.android.app.model.HelpIndex
import org.oppia.android.app.model.HelpIndex.IndexTypeCase.EVERYTHING_REVEALED
import org.oppia.android.app.model.HelpIndex.IndexTypeCase.LATEST_REVEALED_HINT_INDEX
import org.oppia.android.app.model.HelpIndex.IndexTypeCase.NEXT_AVAILABLE_HINT_INDEX
import org.oppia.android.app.model.HelpIndex.IndexTypeCase.SHOW_SOLUTION
import org.oppia.android.app.model.InteractionObject
import org.oppia.android.app.model.ListOfSetsOfTranslatableHtmlContentIds
import org.oppia.android.app.model.OppiaLanguage
import org.oppia.android.app.model.Point2d
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.RatioExpression
import org.oppia.android.app.model.SetOfTranslatableHtmlContentIds
import org.oppia.android.app.model.TranslatableHtmlContentId
import org.oppia.android.app.model.UserAnswer
import org.oppia.android.app.model.WrittenTranslationContext
import org.oppia.android.app.model.WrittenTranslationLanguageSelection
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
import org.oppia.android.domain.exploration.lightweightcheckpointing.ExplorationCheckpointController
import org.oppia.android.domain.exploration.lightweightcheckpointing.ExplorationStorageDatabaseSize
import org.oppia.android.domain.hintsandsolution.HintsAndSolutionConfigModule
import org.oppia.android.domain.hintsandsolution.HintsAndSolutionProdModule
import org.oppia.android.domain.hintsandsolution.isHintRevealed
import org.oppia.android.domain.hintsandsolution.isSolutionRevealed
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.domain.oppialogger.LoggingIdentifierModule
import org.oppia.android.domain.oppialogger.analytics.ApplicationLifecycleModule
import org.oppia.android.domain.platformparameter.PlatformParameterSingletonModule
import org.oppia.android.domain.profile.ProfileManagementController
import org.oppia.android.domain.topic.FRACTIONS_EXPLORATION_ID_0
import org.oppia.android.domain.topic.FRACTIONS_STORY_ID_0
import org.oppia.android.domain.topic.FRACTIONS_TOPIC_ID
import org.oppia.android.domain.topic.TEST_EXPLORATION_ID_13
import org.oppia.android.domain.topic.TEST_EXPLORATION_ID_2
import org.oppia.android.domain.topic.TEST_EXPLORATION_ID_4
import org.oppia.android.domain.topic.TEST_STORY_ID_0
import org.oppia.android.domain.topic.TEST_STORY_ID_2
import org.oppia.android.domain.topic.TEST_TOPIC_ID_0
import org.oppia.android.domain.topic.TEST_TOPIC_ID_1
import org.oppia.android.domain.translation.TranslationController
import org.oppia.android.domain.util.toAnswerString
import org.oppia.android.testing.BuildEnvironment
import org.oppia.android.testing.FakeAnalyticsEventLogger
import org.oppia.android.testing.FakeExceptionLogger
import org.oppia.android.testing.OppiaTestRule
import org.oppia.android.testing.RunOn
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.assertThrows
import org.oppia.android.testing.data.DataProviderTestMonitor
import org.oppia.android.testing.environment.TestEnvironmentConfig
import org.oppia.android.testing.logging.EventLogSubject
import org.oppia.android.testing.logging.EventLogSubject.Companion.assertThat
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClock
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.caching.AssetModule
import org.oppia.android.util.caching.CacheAssetsLocally
import org.oppia.android.util.caching.LoadLessonProtosFromAssets
import org.oppia.android.util.caching.TopicListToCache
import org.oppia.android.util.data.DataProvidersInjector
import org.oppia.android.util.data.DataProvidersInjectorProvider
import org.oppia.android.util.locale.LocaleProdModule
import org.oppia.android.util.logging.EnableConsoleLog
import org.oppia.android.util.logging.EnableFileLog
import org.oppia.android.util.logging.GlobalLogLevel
import org.oppia.android.util.logging.LogLevel
import org.oppia.android.util.logging.SyncStatusModule
import org.oppia.android.util.networking.NetworkConnectionUtilDebugModule
import org.oppia.android.util.platformparameter.LearnerStudyAnalytics
import org.oppia.android.util.platformparameter.PlatformParameterValue
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

// For context:
// https://github.com/oppia/oppia/blob/37285a/extensions/interactions/Continue/directives/oppia-interactive-continue.directive.ts.
private const val DEFAULT_CONTINUE_INTERACTION_TEXT_ANSWER = "Please continue."
private const val INVALID_TOPIC_ID = "invalid_topic_id"
private const val INVALID_STORY_ID = "invalid_story_id"
private const val INVALID_EXPLORATION_ID = "invalid_exp_id"

/** Tests for [ExplorationProgressController]. */
// Same parameter value: helpers reduce test context, even if they are used by 1 test.
// Function name: test names are conventionally named with underscores.
@Suppress("SameParameterValue", "FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = ExplorationProgressControllerTest.TestApplication::class)
class ExplorationProgressControllerTest {
  // TODO(#3646): Add much more thorough tests for the integration pathway.

  // TODO(#59): Once AsyncDataSubscriptionManager can be replaced with a fake, add the following
  //  tests once careful testing timing can be controlled:
  //  - testMoveToNext_whileSubmittingAnswer_failsWithError
  //  - testGetCurrentState_whileSubmittingCorrectMultiChoiceAnswer_updatesToPending
  //  - testSubmitAnswer_whileSubmittingAnotherAnswer_failsWithError
  //  - testMoveToPrevious_whileSubmittingAnswer_failsWithError

  @get:Rule val oppiaTestRule = OppiaTestRule()
  @Inject lateinit var context: Context
  @Inject lateinit var explorationDataController: ExplorationDataController
  @Inject lateinit var explorationProgressController: ExplorationProgressController
  @Inject lateinit var fakeExceptionLogger: FakeExceptionLogger
  @Inject lateinit var testCoroutineDispatchers: TestCoroutineDispatchers
  @Inject lateinit var oppiaClock: FakeOppiaClock
  @Inject lateinit var explorationCheckpointController: ExplorationCheckpointController
  @Inject lateinit var monitorFactory: DataProviderTestMonitor.Factory
  @Inject lateinit var translationController: TranslationController
  @Inject lateinit var fakeEventLogger: FakeAnalyticsEventLogger
  @Inject lateinit var profileManagementController: ProfileManagementController

  private val profileId = ProfileId.newBuilder().setInternalId(0).build()

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testGetCurrentState_noExploration_throwsException() {
    // Can't retrieve the current state until the play session is started.
    assertThrows(UninitializedPropertyAccessException::class) {
      explorationProgressController.getCurrentState()
    }
  }

  @Test
  fun testPlayExploration_invalid_returnsSuccess() {
    val resultDataProvider =
      explorationDataController.replayExploration(
        profileId.internalId, INVALID_TOPIC_ID, INVALID_STORY_ID, INVALID_EXPLORATION_ID
      )

    // An invalid exploration is not known until it's fully loaded, and that's observed via
    // getCurrentState.
    monitorFactory.waitForNextSuccessfulResult(resultDataProvider)
  }

  @Test
  fun testGetCurrentState_playInvalidExploration_returnsFailure() {
    restartExploration(INVALID_TOPIC_ID, INVALID_STORY_ID, INVALID_EXPLORATION_ID)

    val error = waitForGetCurrentStateFailureLoad()

    assertThat(error).hasMessageThat().contains("invalid_exp_id")
  }

  @Test
  fun testPlayExploration_valid_returnsSuccess() {
    val resultDataProvider =
      explorationDataController.replayExploration(
        profileId.internalId, TEST_TOPIC_ID_0, TEST_STORY_ID_0, TEST_EXPLORATION_ID_2
      )

    monitorFactory.waitForNextSuccessfulResult(resultDataProvider)
  }

  @Test
  fun testGetCurrentState_playExploration_loaded_returnsInitialStatePending() {
    restartExploration(TEST_TOPIC_ID_0, TEST_STORY_ID_0, TEST_EXPLORATION_ID_2)

    val ephemeralState = waitForGetCurrentStateSuccessfulLoad()

    assertThat(ephemeralState.stateTypeCase).isEqualTo(PENDING_STATE)
    assertThat(ephemeralState.hasPreviousState).isFalse()
    assertThat(ephemeralState.state.name).isEqualTo("Continue")
  }

  @Test
  fun testGetCurrentState_playInvalidExploration_thenPlayValidExp_returnsInitialPendingState() {
    // Start with playing an invalid exploration.
    restartExploration(INVALID_TOPIC_ID, INVALID_STORY_ID, INVALID_EXPLORATION_ID)
    endExploration()

    // Then a valid one.
    restartExploration(TEST_TOPIC_ID_0, TEST_STORY_ID_0, TEST_EXPLORATION_ID_2)

    // The latest result should correspond to the valid ID, and the progress controller should
    // gracefully recover.
    val ephemeralState = waitForGetCurrentStateSuccessfulLoad()
    assertThat(ephemeralState.stateTypeCase).isEqualTo(PENDING_STATE)
    assertThat(ephemeralState.hasPreviousState).isFalse()
    assertThat(ephemeralState.state.name).isEqualTo("Continue")
  }

  @Test
  fun testFinishExploration_beforePlaying_isFailure() {
    val resultDataProvider = explorationDataController.stopPlayingExploration(isCompletion = false)

    // The operation should be failing since the session hasn't started.
    val result = monitorFactory.waitForNextFailureResult(resultDataProvider)
    assertThat(result).isInstanceOf(IllegalStateException::class.java)
    assertThat(result).hasMessageThat().contains("Session isn't initialized yet.")
  }

  @Test
  fun testPlayExploration_withoutFinishingPrevious_succeeds() {
    restartExploration(TEST_TOPIC_ID_0, TEST_STORY_ID_0, TEST_EXPLORATION_ID_2)
    waitForGetCurrentStateSuccessfulLoad()

    // Try playing another exploration without finishing the previous one.
    val resultDataProvider =
      explorationDataController.replayExploration(
        profileId.internalId, TEST_TOPIC_ID_0, TEST_STORY_ID_0, TEST_EXPLORATION_ID_2
      )

    // The new session will overwrite the previous.
    monitorFactory.waitForNextSuccessfulResult(resultDataProvider)
  }

  @Test
  fun testGetCurrentState_playSecondExploration_afterFinishingPrev_loaded_returnsInitialState() {
    // Start with playing a valid exploration, then stop.
    restartExploration(TEST_TOPIC_ID_0, TEST_STORY_ID_0, TEST_EXPLORATION_ID_2)
    waitForGetCurrentStateSuccessfulLoad()
    endExploration()

    // Then another valid one.
    restartExploration(TEST_TOPIC_ID_1, TEST_STORY_ID_2, TEST_EXPLORATION_ID_4)

    // The latest result should correspond to the valid ID, and the progress controller should
    // gracefully recover.
    val ephemeralState = waitForGetCurrentStateSuccessfulLoad()
    assertThat(ephemeralState.stateTypeCase).isEqualTo(PENDING_STATE)
    assertThat(ephemeralState.hasPreviousState).isFalse()
    assertThat(ephemeralState.state.name).isEqualTo("DragDropSortInput")
  }

  @Test
  fun testSubmitAnswer_beforePlaying_isFailure() {
    val resultProvider = explorationProgressController.submitAnswer(createMultipleChoiceAnswer(0))

    // The operation should be failing since the session hasn't started.
    val result = monitorFactory.waitForNextFailureResult(resultProvider)
    assertThat(result).isInstanceOf(IllegalStateException::class.java)
    assertThat(result).hasMessageThat().contains("Session isn't initialized yet.")
  }

  @Test
  fun testSubmitAnswer_forMultipleChoice_correctAnswer_succeeds() {
    restartExploration(TEST_TOPIC_ID_0, TEST_STORY_ID_0, TEST_EXPLORATION_ID_2)
    waitForGetCurrentStateSuccessfulLoad()
    navigateToPrototypeMultipleChoiceState()

    val result = explorationProgressController.submitAnswer(createMultipleChoiceAnswer(2))

    // Verify that the answer submission was successful.
    monitorFactory.waitForNextSuccessfulResult(result)
  }

  @Test
  fun testSubmitAnswer_forMultipleChoice_correctAnswer_returnsOutcomeWithTransition() {
    restartExploration(TEST_TOPIC_ID_0, TEST_STORY_ID_0, TEST_EXPLORATION_ID_2)
    waitForGetCurrentStateSuccessfulLoad()
    navigateToPrototypeMultipleChoiceState()

    val result = explorationProgressController.submitAnswer(createMultipleChoiceAnswer(2))

    // Verify that the answer submission was successful.
    val answerOutcome = monitorFactory.waitForNextSuccessfulResult(result)
    assertThat(answerOutcome.destinationCase).isEqualTo(AnswerOutcome.DestinationCase.STATE_NAME)
    assertThat(answerOutcome.feedback.html).contains("Correct!")
  }

  @Test
  fun testSubmitAnswer_forMultipleChoice_wrongAnswer_succeeds() {
    restartExploration(TEST_TOPIC_ID_0, TEST_STORY_ID_0, TEST_EXPLORATION_ID_2)
    waitForGetCurrentStateSuccessfulLoad()
    navigateToPrototypeMultipleChoiceState()

    val result = explorationProgressController.submitAnswer(createMultipleChoiceAnswer(0))

    // Verify that the answer submission was successful.
    monitorFactory.waitForNextSuccessfulResult(result)
  }

  @Test
  fun testSubmitAnswer_forMultipleChoice_wrongAnswer_providesDefFeedbackAndSameStateTransition() {
    restartExploration(TEST_TOPIC_ID_0, TEST_STORY_ID_0, TEST_EXPLORATION_ID_2)
    waitForGetCurrentStateSuccessfulLoad()
    navigateToPrototypeMultipleChoiceState()

    val result = explorationProgressController.submitAnswer(createMultipleChoiceAnswer(0))

    // Verify that the answer submission was successful.
    val answerOutcome = monitorFactory.waitForNextSuccessfulResult(result)
    assertThat(answerOutcome.destinationCase).isEqualTo(AnswerOutcome.DestinationCase.SAME_STATE)
    assertThat(answerOutcome.feedback.html).contains("Try again.")
  }

  @Test
  fun testGetCurrentState_afterSubmittingCorrectMultiChoiceAnswer_becomesCompletedState() {
    restartExploration(TEST_TOPIC_ID_0, TEST_STORY_ID_0, TEST_EXPLORATION_ID_2)
    waitForGetCurrentStateSuccessfulLoad()
    navigateToPrototypeMultipleChoiceState()

    val ephemeralState = submitMultipleChoiceAnswer(2)

    // Verify that the current state updates. It should now be completed with the correct answer.
    assertThat(ephemeralState.stateTypeCase).isEqualTo(COMPLETED_STATE)
    assertThat(ephemeralState.completedState.answerCount).isEqualTo(1)
    assertThat(ephemeralState.completedState.getAnswer(0).userAnswer.answer.nonNegativeInt)
      .isEqualTo(2)
    assertThat(ephemeralState.completedState.getAnswer(0).feedback.html).contains("Correct!")
  }

  @Test
  fun testGetCurrentState_afterSubmittingWrongMultiChoiceAnswer_updatesPendingState() {
    restartExploration(TEST_TOPIC_ID_0, TEST_STORY_ID_0, TEST_EXPLORATION_ID_2)
    waitForGetCurrentStateSuccessfulLoad()
    navigateToPrototypeMultipleChoiceState()

    val ephemeralState = submitMultipleChoiceAnswer(0)

    // Verify that the current state updates. It should stay pending, and the wrong answer should be
    // appended.
    assertThat(ephemeralState.stateTypeCase).isEqualTo(PENDING_STATE)
    assertThat(ephemeralState.pendingState.wrongAnswerCount).isEqualTo(1)
    assertThat(ephemeralState.pendingState.getWrongAnswer(0).userAnswer.answer.nonNegativeInt)
      .isEqualTo(0)
    assertThat(ephemeralState.pendingState.getWrongAnswer(0).feedback.html).contains("Try again.")
  }

  @Test
  fun testGetCurrentState_afterSubmittingWrongThenRightAnswer_updatesToStateWithBothAnswers() {
    restartExploration(TEST_TOPIC_ID_0, TEST_STORY_ID_0, TEST_EXPLORATION_ID_2)
    waitForGetCurrentStateSuccessfulLoad()
    navigateToPrototypeMultipleChoiceState()
    submitMultipleChoiceAnswer(0)

    val ephemeralState = submitMultipleChoiceAnswer(2)

    // Verify that the current state updates. It should now be completed with both the wrong and
    // correct answers.
    assertThat(ephemeralState.stateTypeCase).isEqualTo(COMPLETED_STATE)
    assertThat(ephemeralState.completedState.answerCount).isEqualTo(2)
    assertThat(ephemeralState.completedState.getAnswer(0).userAnswer.answer.nonNegativeInt)
      .isEqualTo(0)
    assertThat(ephemeralState.completedState.getAnswer(0).feedback.html).contains("Try again.")
    assertThat(ephemeralState.completedState.getAnswer(1).userAnswer.answer.nonNegativeInt)
      .isEqualTo(2)
    assertThat(ephemeralState.completedState.getAnswer(1).feedback.html).contains("Correct!")
  }

  @Test
  fun testMoveToNext_beforePlaying_isFailure() {
    val moveToStateResult = explorationProgressController.moveToNextState()

    // The operation should be failing since the session hasn't started.
    val result = monitorFactory.waitForNextFailureResult(moveToStateResult)
    assertThat(result).isInstanceOf(IllegalStateException::class.java)
    assertThat(result).hasMessageThat().contains("Session isn't initialized yet.")
  }

  @Test
  fun testMoveToNext_forPendingInitialState_failsWithError() {
    restartExploration(TEST_TOPIC_ID_0, TEST_STORY_ID_0, TEST_EXPLORATION_ID_2)
    waitForGetCurrentStateSuccessfulLoad()

    val moveToStateResult = explorationProgressController.moveToNextState()

    // Verify that we can't move ahead since the current state isn't yet completed.
    val error = monitorFactory.waitForNextFailureResult(moveToStateResult)
    assertThat(error)
      .hasMessageThat()
      .contains("Cannot navigate to next state; at most recent state.")
  }

  @Test
  fun testMoveToNext_forCompletedState_succeeds() {
    restartExploration(TEST_TOPIC_ID_0, TEST_STORY_ID_0, TEST_EXPLORATION_ID_2)
    waitForGetCurrentStateSuccessfulLoad()
    submitPrototypeState1Answer()

    val moveToStateResult = explorationProgressController.moveToNextState()

    monitorFactory.waitForNextSuccessfulResult(moveToStateResult)
  }

  @Test
  fun testMoveToNext_forCompletedState_movesToNextState() {
    restartExploration(TEST_TOPIC_ID_0, TEST_STORY_ID_0, TEST_EXPLORATION_ID_2)
    waitForGetCurrentStateSuccessfulLoad()
    submitPrototypeState1Answer()

    val ephemeralState = moveToNextState()

    assertThat(ephemeralState.state.name).isEqualTo("Fractions")
    assertThat(ephemeralState.stateTypeCase).isEqualTo(PENDING_STATE)
  }

  @Test
  fun testMoveToNext_afterMovingFromCompletedState_failsWithError() {
    restartExploration(TEST_TOPIC_ID_0, TEST_STORY_ID_0, TEST_EXPLORATION_ID_2)
    waitForGetCurrentStateSuccessfulLoad()
    submitPrototypeState1Answer()
    moveToNextState()

    // Try skipping past the current state.
    val moveToStateResult = explorationProgressController.moveToNextState()

    // Verify we can't move ahead since the new state isn't yet completed.
    val error = monitorFactory.waitForNextFailureResult(moveToStateResult)
    assertThat(error)
      .hasMessageThat()
      .contains("Cannot navigate to next state; at most recent state.")
  }

  @Test
  fun testMoveToPrevious_beforePlaying_isFailure() {
    val moveToStateResult = explorationProgressController.moveToPreviousState()

    // The operation should be failing since the session hasn't started.
    val result = monitorFactory.waitForNextFailureResult(moveToStateResult)
    assertThat(result).isInstanceOf(IllegalStateException::class.java)
    assertThat(result).hasMessageThat().contains("Session isn't initialized yet.")
  }

  @Test
  fun testMoveToPrevious_onPendingInitialState_failsWithError() {
    restartExploration(TEST_TOPIC_ID_0, TEST_STORY_ID_0, TEST_EXPLORATION_ID_2)
    waitForGetCurrentStateSuccessfulLoad()

    val moveToStateResult = explorationProgressController.moveToPreviousState()

    // Verify we can't move behind since the current state is the initial exploration state.
    val error = monitorFactory.waitForNextFailureResult(moveToStateResult)
    assertThat(error)
      .hasMessageThat()
      .contains("Cannot navigate to previous state; at initial state.")
  }

  @Test
  fun testMoveToPrevious_onCompletedInitialState_failsWithError() {
    restartExploration(TEST_TOPIC_ID_0, TEST_STORY_ID_0, TEST_EXPLORATION_ID_2)
    waitForGetCurrentStateSuccessfulLoad()
    submitPrototypeState1Answer()

    val moveToStateResult = explorationProgressController.moveToPreviousState()

    // Still can't navigate behind for a completed initial state since there's no previous state.
    val error = monitorFactory.waitForNextFailureResult(moveToStateResult)
    assertThat(error)
      .hasMessageThat()
      .contains("Cannot navigate to previous state; at initial state.")
  }

  @Test
  fun testMoveToPrevious_forStateWithCompletedPreviousState_succeeds() {
    restartExploration(TEST_TOPIC_ID_0, TEST_STORY_ID_0, TEST_EXPLORATION_ID_2)
    waitForGetCurrentStateSuccessfulLoad()
    playThroughPrototypeState1AndMoveToNextState()

    val moveToStateResult = explorationProgressController.moveToPreviousState()

    // Verify that we can navigate to the previous state since the current state is complete and not
    // initial.
    monitorFactory.waitForNextSuccessfulResult(moveToStateResult)
  }

  @Test
  fun testMoveToPrevious_forCompletedState_movesToPreviousState() {
    restartExploration(TEST_TOPIC_ID_0, TEST_STORY_ID_0, TEST_EXPLORATION_ID_2)
    waitForGetCurrentStateSuccessfulLoad()
    playThroughPrototypeState1AndMoveToNextState()

    val ephemeralState = moveToPreviousState()

    // Since the answer submission and forward navigation should work (see earlier tests), verify
    // that the move to the previous state does return us back to the initial exploration state
    // (which is now completed).
    assertThat(ephemeralState.state.name).isEqualTo("Continue")
    assertThat(ephemeralState.stateTypeCase).isEqualTo(COMPLETED_STATE)
  }

  @Test
  fun testMoveToPrevious_navigatedForwardThenBackToInitial_failsWithError() {
    restartExploration(TEST_TOPIC_ID_0, TEST_STORY_ID_0, TEST_EXPLORATION_ID_2)
    waitForGetCurrentStateSuccessfulLoad()
    playThroughPrototypeState1AndMoveToNextState()
    moveToPreviousState()

    val moveToStateResult = explorationProgressController.moveToPreviousState()

    // The first previous navigation should succeed (see above), but the second will fail since
    // we're back at the initial state.
    val error = monitorFactory.waitForNextFailureResult(moveToStateResult)
    assertThat(error)
      .hasMessageThat()
      .contains("Cannot navigate to previous state; at initial state.")
  }

  @Test
  fun testSubmitAnswer_forTextInput_correctAnswer_returnsOutcomeWithTransition() {
    restartExploration(TEST_TOPIC_ID_0, TEST_STORY_ID_0, TEST_EXPLORATION_ID_2)
    waitForGetCurrentStateSuccessfulLoad()
    navigateToPrototypeTextInputState()

    val result = explorationProgressController.submitAnswer(createTextInputAnswer("Finnish"))

    // Verify that the answer submission was successful.
    val answerOutcome = monitorFactory.waitForNextSuccessfulResult(result)
    assertThat(answerOutcome.destinationCase).isEqualTo(AnswerOutcome.DestinationCase.STATE_NAME)
    assertThat(answerOutcome.feedback.html).contains("Correct!")
  }

  @Test
  fun testSubmitAnswer_forTextInput_wrongAnswer_returnsDefaultOutcome() {
    restartExploration(TEST_TOPIC_ID_0, TEST_STORY_ID_0, TEST_EXPLORATION_ID_2)
    waitForGetCurrentStateSuccessfulLoad()
    navigateToPrototypeTextInputState()

    val result = explorationProgressController.submitAnswer(createTextInputAnswer("Klingon"))

    // Verify that the answer was wrong, and that there's no handler for it so the default outcome
    // is returned.
    val answerOutcome = monitorFactory.waitForNextSuccessfulResult(result)
    assertThat(answerOutcome.destinationCase).isEqualTo(AnswerOutcome.DestinationCase.SAME_STATE)
    assertThat(answerOutcome.feedback.html).contains("Not quite.")
  }

  @Test
  fun testSubmitAnswer_forFractionInput_wrongAnswer_returnsDefaultOutcome_hasHint() {
    restartExploration(TEST_TOPIC_ID_0, TEST_STORY_ID_0, TEST_EXPLORATION_ID_2)
    waitForGetCurrentStateSuccessfulLoad()
    navigateToPrototypeFractionInputState()

    val ephemeralState = submitWrongAnswerForPrototypeState2()

    // Verify that the current state updates. It should stay pending, and the wrong answer should be
    // appended.
    assertThat(ephemeralState.stateTypeCase).isEqualTo(PENDING_STATE)
    assertThat(ephemeralState.pendingState.wrongAnswerCount).isEqualTo(1)
    val answerAndFeedback = ephemeralState.pendingState.getWrongAnswer(0)
    assertThat(answerAndFeedback.userAnswer.answer.fraction.numerator).isEqualTo(1)
    assertThat(answerAndFeedback.userAnswer.answer.fraction.denominator).isEqualTo(3)
    assertThat(answerAndFeedback.feedback.html).contains("Try again.")
    val hintAndSolution = ephemeralState.state.interaction.getHint(0)
    assertThat(hintAndSolution.hintContent.html).contains("Remember that two halves")
  }

  @Test
  fun testRevealHint_forWrongAnswers_showHint_returnHintIsRevealed() {
    restartExploration(TEST_TOPIC_ID_0, TEST_STORY_ID_0, TEST_EXPLORATION_ID_2)
    waitForGetCurrentStateSuccessfulLoad()
    navigateToPrototypeFractionInputState()
    // Submit 2 wrong answers to trigger a hint becoming available.
    submitWrongAnswerForPrototypeState2()
    submitWrongAnswerForPrototypeState2()
    monitorFactory.waitForNextSuccessfulResult(
      explorationProgressController.submitHintIsRevealed(hintIndex = 0)
    )

    // Verify that the current state updates. It should stay pending, on submission of wrong answer.
    val ephemeralState = waitForGetCurrentStateSuccessfulLoad()
    val hintAndSolution = ephemeralState.state.interaction.getHint(0)
    assertThat(ephemeralState.stateTypeCase).isEqualTo(PENDING_STATE)
    assertThat(hintAndSolution.hintContent.html).contains("Remember that two halves")
    assertThat(ephemeralState.isHintRevealed(0)).isTrue()
  }

  @Test
  fun testRevealSolution_triggeredSolution_showSolution_returnSolutionIsRevealed() {
    restartExploration(TEST_TOPIC_ID_0, TEST_STORY_ID_0, TEST_EXPLORATION_ID_2)
    waitForGetCurrentStateSuccessfulLoad()
    navigateToPrototypeFractionInputState()
    // Submit 2 wrong answers to trigger the hint.
    submitWrongAnswerForPrototypeState2()
    submitWrongAnswerForPrototypeState2()
    // Reveal the hint, then submit another wrong answer to trigger the solution.
    monitorFactory.waitForNextSuccessfulResult(
      explorationProgressController.submitHintIsRevealed(hintIndex = 0)
    )
    submitWrongAnswerForPrototypeState2()
    testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(10))

    // Verify that the current state updates. It should stay pending, on submission of wrong answer.
    waitForGetCurrentStateSuccessfulLoad()
    monitorFactory.waitForNextSuccessfulResult(
      explorationProgressController.submitSolutionIsRevealed()
    )

    // Verify that the current state updates. Solution revealed is true.
    val ephemeralState = waitForGetCurrentStateSuccessfulLoad()
    assertThat(ephemeralState.stateTypeCase).isEqualTo(PENDING_STATE)
    assertThat(ephemeralState.isSolutionRevealed()).isTrue()
  }

  @Test
  fun testHintsAndSolution_noHintVisible_checkHelpIndexIsCorrect() {
    startPlayingNewExploration(TEST_TOPIC_ID_0, TEST_STORY_ID_0, TEST_EXPLORATION_ID_2)
    waitForGetCurrentStateSuccessfulLoad()
    val ephemeralState = playThroughPrototypeState1AndMoveToNextState()

    // Verify that the helpIndex.IndexTypeCase is equal to INDEX_TYPE_NOT_SET because no hint
    // is visible yet.
    assertThat(ephemeralState.pendingState.helpIndex.indexTypeCase)
      .isEqualTo(HelpIndex.IndexTypeCase.INDEXTYPE_NOT_SET)
  }

  @Test
  fun testHintsAndSolution_wait60Seconds_unrevealedHintIsVisible_checkHelpIndexIsCorrect() {
    startPlayingNewExploration(TEST_TOPIC_ID_0, TEST_STORY_ID_0, TEST_EXPLORATION_ID_2)
    waitForGetCurrentStateSuccessfulLoad()
    playThroughPrototypeState1AndMoveToNextState()
    // Make the first hint visible by submitting two wrong answers.
    testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(60))
    testCoroutineDispatchers.runCurrent()

    // Verify that the helpIndex.IndexTypeCase is equal AVAILABLE_NEXT_HINT_HINT_INDEX because a new
    // unrevealed hint is visible.
    val ephemeralState = waitForGetCurrentStateSuccessfulLoad()
    assertThat(ephemeralState.isHintRevealed(0)).isFalse()
    assertThat(ephemeralState.pendingState.helpIndex.indexTypeCase)
      .isEqualTo(NEXT_AVAILABLE_HINT_INDEX)
    assertThat(ephemeralState.pendingState.helpIndex.nextAvailableHintIndex).isEqualTo(0)
  }

  @Test
  fun testHintsAndSolution_submitTwoWrongAnswers_unrevealedHintIsVisible_checkHelpIndexIsCorrect() {
    startPlayingNewExploration(TEST_TOPIC_ID_0, TEST_STORY_ID_0, TEST_EXPLORATION_ID_2)
    waitForGetCurrentStateSuccessfulLoad()
    playThroughPrototypeState1AndMoveToNextState()
    // Make the first hint visible by submitting two wrong answers.
    submitWrongAnswerForPrototypeState2()
    val ephemeralState = submitWrongAnswerForPrototypeState2()

    // Verify that the helpIndex.IndexTypeCase is equal AVAILABLE_NEXT_HINT_HINT_INDEX because a new
    // unrevealed hint is visible.
    assertThat(ephemeralState.isHintRevealed(0)).isFalse()
    assertThat(ephemeralState.pendingState.helpIndex.indexTypeCase)
      .isEqualTo(NEXT_AVAILABLE_HINT_INDEX)
    assertThat(ephemeralState.pendingState.helpIndex.nextAvailableHintIndex).isEqualTo(0)
  }

  @Test
  fun testHintsAndSolution_revealedHintIsVisible_checkHelpIndexIsCorrect() {
    startPlayingNewExploration(TEST_TOPIC_ID_0, TEST_STORY_ID_0, TEST_EXPLORATION_ID_2)
    waitForGetCurrentStateSuccessfulLoad()
    playThroughPrototypeState1AndMoveToNextState()
    submitWrongAnswerForPrototypeState2()
    submitWrongAnswerForPrototypeState2()

    val result = explorationProgressController.submitHintIsRevealed(hintIndex = 0)

    // Verify that the helpIndex.IndexTypeCase is equal LATEST_REVEALED_HINT_INDEX because a new
    // revealed hint is visible.
    monitorFactory.waitForNextSuccessfulResult(result)
    val ephemeralState = waitForGetCurrentStateSuccessfulLoad()
    assertThat(ephemeralState.isHintRevealed(0)).isTrue()
    assertThat(ephemeralState.isSolutionRevealed()).isFalse()
    assertThat(ephemeralState.pendingState.helpIndex.indexTypeCase)
      .isEqualTo(LATEST_REVEALED_HINT_INDEX)
    assertThat(ephemeralState.pendingState.helpIndex.latestRevealedHintIndex).isEqualTo(0)
  }

  @Test
  fun testHintsAndSolution_allHintsVisible_wait30Seconds_solutionVisible_checkHelpIndexIsCorrect() {
    startPlayingNewExploration(TEST_TOPIC_ID_0, TEST_STORY_ID_0, TEST_EXPLORATION_ID_2)
    waitForGetCurrentStateSuccessfulLoad()
    playThroughPrototypeState1AndMoveToNextState()
    submitWrongAnswerForPrototypeState2()
    submitWrongAnswerForPrototypeState2()

    explorationProgressController.submitHintIsRevealed(hintIndex = 0)
    testCoroutineDispatchers.runCurrent()

    // The solution should be visible after 30 seconds of the last hint being reveled.
    testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(30))
    testCoroutineDispatchers.runCurrent()

    // Verify that the helpIndex.IndexTypeCase is equal SHOW_SOLUTION because unrevealed solution is
    // visible.
    val ephemeralState = waitForGetCurrentStateSuccessfulLoad()
    assertThat(ephemeralState.isHintRevealed(0)).isTrue()
    assertThat(ephemeralState.isSolutionRevealed()).isFalse()
    assertThat(ephemeralState.pendingState.helpIndex.indexTypeCase)
      .isEqualTo(SHOW_SOLUTION)
  }

  @Test
  fun testHintAndSol_hintsVisible_submitWrongAns_wait10Second_solVisible_checkHelpIndexIsCorrect() {
    startPlayingNewExploration(TEST_TOPIC_ID_0, TEST_STORY_ID_0, TEST_EXPLORATION_ID_2)
    waitForGetCurrentStateSuccessfulLoad()
    playThroughPrototypeState1AndMoveToNextState()
    submitWrongAnswerForPrototypeState2()
    submitWrongAnswerForPrototypeState2()

    explorationProgressController.submitHintIsRevealed(hintIndex = 0)
    testCoroutineDispatchers.runCurrent()

    submitWrongAnswerForPrototypeState2()
    // The solution should be visible after 10 seconds because one wrong answer was submitted.
    testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(10))
    testCoroutineDispatchers.runCurrent()

    // Verify that the helpIndex.IndexTypeCase is equal SHOW_SOLUTION because unrevealed solution is
    // visible.
    val ephemeralState = waitForGetCurrentStateSuccessfulLoad()
    assertThat(ephemeralState.isHintRevealed(0)).isTrue()
    assertThat(ephemeralState.isSolutionRevealed()).isFalse()
    assertThat(ephemeralState.pendingState.helpIndex.indexTypeCase)
      .isEqualTo(SHOW_SOLUTION)
  }

  @Test
  fun testHintsAndSolution_revealedSolutionIsVisible_checkHelpIndexIsCorrect() {
    startPlayingNewExploration(TEST_TOPIC_ID_0, TEST_STORY_ID_0, TEST_EXPLORATION_ID_2)
    waitForGetCurrentStateSuccessfulLoad()
    playThroughPrototypeState1AndMoveToNextState()
    submitWrongAnswerForPrototypeState2()
    submitWrongAnswerForPrototypeState2()

    explorationProgressController.submitHintIsRevealed(hintIndex = 0)
    testCoroutineDispatchers.runCurrent()

    // The solution should be visible after 30 seconds of the last hint being reveled.
    testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(30))
    testCoroutineDispatchers.runCurrent()

    explorationProgressController.submitSolutionIsRevealed()
    testCoroutineDispatchers.runCurrent()

    // Verify that the helpIndex.IndexTypeCase is equal EVERYTHING_IS_REVEALED because a new the
    // solution has been revealed.
    val ephemeralState = waitForGetCurrentStateSuccessfulLoad()
    assertThat(ephemeralState.isHintRevealed(0)).isTrue()
    assertThat(ephemeralState.isSolutionRevealed()).isTrue()
    assertThat(ephemeralState.pendingState.helpIndex.indexTypeCase)
      .isEqualTo(EVERYTHING_REVEALED)
  }

  @Test
  fun testSubmitAnswer_forTextInput_wrongAnswer_afterAllHintsAreExhausted_showSolution() {
    restartExploration(TEST_TOPIC_ID_0, TEST_STORY_ID_0, TEST_EXPLORATION_ID_2)
    waitForGetCurrentStateSuccessfulLoad()
    navigateToPrototypeFractionInputState()

    val ephemeralState = submitWrongAnswerForPrototypeState2()

    // Verify that the current state updates. It should stay pending, and the wrong answer should be
    // appended.
    assertThat(ephemeralState.stateTypeCase).isEqualTo(PENDING_STATE)
    assertThat(ephemeralState.pendingState.wrongAnswerCount).isEqualTo(1)
    val hint = ephemeralState.state.interaction.getHint(0)
    assertThat(hint.hintContent.html).contains("Remember that two halves")
    val solution = ephemeralState.state.interaction.solution
    assertThat(solution.correctAnswer.numerator).isEqualTo(1)
    assertThat(solution.correctAnswer.denominator).isEqualTo(2)
    assertThat(solution.explanation.html)
      .contains("Half of something has one part in the numerator for every two parts")
  }

  @Test
  fun testGetCurrentState_secondState_submitRightAnswer_pendingStateBecomesCompleted() {
    restartExploration(TEST_TOPIC_ID_0, TEST_STORY_ID_0, TEST_EXPLORATION_ID_2)
    waitForGetCurrentStateSuccessfulLoad()
    navigateToPrototypeTextInputState()

    explorationProgressController.submitAnswer(createTextInputAnswer("Finnish"))
    testCoroutineDispatchers.runCurrent()

    // Verify that the current state updates. It should now be completed with the correct answer.
    val ephemeralState = waitForGetCurrentStateSuccessfulLoad()
    assertThat(ephemeralState.stateTypeCase).isEqualTo(COMPLETED_STATE)
    assertThat(ephemeralState.completedState.answerCount).isEqualTo(1)
    val answerAndFeedback = ephemeralState.completedState.getAnswer(0)
    assertThat(answerAndFeedback.userAnswer.answer.normalizedString).isEqualTo("Finnish")
    assertThat(answerAndFeedback.feedback.html).contains("Correct!")
  }

  @Test
  fun testSubmitAnswer_forTextInput_withSpaces_updatesStateWithVerbatimAnswer() {
    restartExploration(TEST_TOPIC_ID_0, TEST_STORY_ID_0, TEST_EXPLORATION_ID_2)
    waitForGetCurrentStateSuccessfulLoad()
    navigateToPrototypeTextInputState()

    explorationProgressController.submitAnswer(createTextInputAnswer("Finnish  "))
    testCoroutineDispatchers.runCurrent()

    // Verify that the current state updates. The submitted answer should have a textual version
    // that is a verbatim version of the user-submitted answer.
    val ephemeralState = waitForGetCurrentStateSuccessfulLoad()
    assertThat(ephemeralState.stateTypeCase).isEqualTo(COMPLETED_STATE)
    assertThat(ephemeralState.completedState.answerCount).isEqualTo(1)
    val answerAndFeedback = ephemeralState.completedState.getAnswer(0)
    assertThat(answerAndFeedback.userAnswer.textualAnswerCase)
      .isEqualTo(UserAnswer.TextualAnswerCase.PLAIN_ANSWER)
    assertThat(answerAndFeedback.userAnswer.plainAnswer).isEqualTo("Finnish  ")
  }

  @Test
  fun testGetCurrentState_eighthState_submitWrongAnswer_updatePendingState() {
    restartExploration(TEST_TOPIC_ID_0, TEST_STORY_ID_0, TEST_EXPLORATION_ID_2)
    waitForGetCurrentStateSuccessfulLoad()
    navigateToPrototypeTextInputState()

    explorationProgressController.submitAnswer(createTextInputAnswer("Klingon"))
    testCoroutineDispatchers.runCurrent()

    // Verify that the current state updates. It should stay pending, and the wrong answer should be
    // appended.
    val ephemeralState = waitForGetCurrentStateSuccessfulLoad()
    assertThat(ephemeralState.stateTypeCase).isEqualTo(PENDING_STATE)
    assertThat(ephemeralState.pendingState.wrongAnswerCount).isEqualTo(1)
    val answerAndFeedback = ephemeralState.pendingState.getWrongAnswer(0)
    assertThat(answerAndFeedback.userAnswer.answer.normalizedString).isEqualTo("Klingon")
    assertThat(answerAndFeedback.feedback.html).contains("Not quite.")
  }

  @Test
  fun testGetCurrentState_afterMovePreviousAndNext_returnsCurrentState() {
    restartExploration(TEST_TOPIC_ID_0, TEST_STORY_ID_0, TEST_EXPLORATION_ID_2)
    waitForGetCurrentStateSuccessfulLoad()
    playThroughPrototypeState1AndMoveToNextState()

    moveToPreviousState()
    val ephemeralState = moveToNextState()

    // The current state should stay the same.
    assertThat(ephemeralState.state.name).isEqualTo("Fractions")
    assertThat(ephemeralState.stateTypeCase).isEqualTo(PENDING_STATE)
  }

  @Test
  fun testGetCurrentState_afterMoveNextAndPrevious_returnsCurrentState() {
    restartExploration(TEST_TOPIC_ID_0, TEST_STORY_ID_0, TEST_EXPLORATION_ID_2)
    waitForGetCurrentStateSuccessfulLoad()
    playThroughPrototypeState1AndMoveToNextState()
    submitPrototypeState2Answer() // Submit the answer but do not proceed to the next state.

    moveToNextState()
    val ephemeralState = moveToPreviousState()

    // The current state should stay the same.
    assertThat(ephemeralState.state.name).isEqualTo("Fractions")
    assertThat(ephemeralState.stateTypeCase).isEqualTo(COMPLETED_STATE)
  }

  @Test
  fun testGetCurrentState_afterMoveToPrev_onThirdState_newObserver_receivesCompletedSecondState() {
    restartExploration(TEST_TOPIC_ID_0, TEST_STORY_ID_0, TEST_EXPLORATION_ID_2)
    waitForGetCurrentStateSuccessfulLoad()
    playThroughPrototypeState1AndMoveToNextState()
    playThroughPrototypeState2AndMoveToNextState()

    // Move to the previous state and register a new observer.
    val ephemeralState = moveToPreviousState() // Third state -> second

    // The new observer should observe the completed second state
    // since it's the current pending state.
    assertThat(ephemeralState.state.name).isEqualTo("Fractions")
    assertThat(ephemeralState.stateTypeCase).isEqualTo(COMPLETED_STATE)
  }

  @Test
  fun testGetCurrentState_forFirstState_doesNotHaveNextState() {
    restartExploration(TEST_TOPIC_ID_0, TEST_STORY_ID_0, TEST_EXPLORATION_ID_2)
    val ephemeralState = waitForGetCurrentStateSuccessfulLoad()

    // The initial state should not have a next state.
    assertThat(ephemeralState.hasNextState).isFalse()
  }

  @Test
  fun testGetCurrentState_forFirstState_afterAnswerSubmission_doesNotHaveNextState() {
    restartExploration(TEST_TOPIC_ID_0, TEST_STORY_ID_0, TEST_EXPLORATION_ID_2)
    waitForGetCurrentStateSuccessfulLoad()

    val ephemeralState = submitPrototypeState1Answer()

    // Simply completing the current state should not result in there being a next state since the
    // user hasn't proceeded to the following state, yet.
    assertThat(ephemeralState.hasNextState).isFalse()
  }

  @Test
  fun testGetCurrentState_forSecondState_doesNotHaveNextState() {
    restartExploration(TEST_TOPIC_ID_0, TEST_STORY_ID_0, TEST_EXPLORATION_ID_2)
    waitForGetCurrentStateSuccessfulLoad()

    val ephemeralState = playThroughPrototypeState1AndMoveToNextState()

    // The current state should have a previous state.
    assertThat(ephemeralState.hasNextState).isFalse()
  }

  @Test
  fun testGetCurrentState_forSecondState_navigateBackward_hasNextState() {
    restartExploration(TEST_TOPIC_ID_0, TEST_STORY_ID_0, TEST_EXPLORATION_ID_2)
    waitForGetCurrentStateSuccessfulLoad()
    playThroughPrototypeState1AndMoveToNextState()

    val ephemeralState = moveToPreviousState()

    // The previous state should have a next state.
    assertThat(ephemeralState.hasNextState).isTrue()
  }

  @Test
  fun testGetCurrentState_forSecondState_navigateBackwardThenForward_doesNotHaveNextState() {
    restartExploration(TEST_TOPIC_ID_0, TEST_STORY_ID_0, TEST_EXPLORATION_ID_2)
    waitForGetCurrentStateSuccessfulLoad()
    playThroughPrototypeState1AndMoveToNextState()

    moveToPreviousState()
    val ephemeralState = moveToNextState()

    // Iterating back to the current state should result in no longer having a next state.
    assertThat(ephemeralState.hasNextState).isFalse()
  }

  @Test
  fun testSubmitAnswer_forNumericInput_correctAnswer_returnsOutcomeWithTransition() {
    restartExploration(TEST_TOPIC_ID_0, TEST_STORY_ID_0, TEST_EXPLORATION_ID_2)
    waitForGetCurrentStateSuccessfulLoad()
    navigateToPrototypeNumericInputState()

    val result = explorationProgressController.submitAnswer(createNumericInputAnswer(121.0))

    // Verify that the answer submission was successful.
    val answerOutcome = monitorFactory.waitForNextSuccessfulResult(result)
    assertThat(answerOutcome.destinationCase).isEqualTo(AnswerOutcome.DestinationCase.STATE_NAME)
    assertThat(answerOutcome.feedback.html).contains("Correct!")
  }

  @Test
  fun testSubmitAnswer_forNumericInput_wrongAnswer_returnsOutcomeWithTransition() {
    restartExploration(TEST_TOPIC_ID_0, TEST_STORY_ID_0, TEST_EXPLORATION_ID_2)
    waitForGetCurrentStateSuccessfulLoad()
    navigateToPrototypeNumericInputState()

    val result = explorationProgressController.submitAnswer(createNumericInputAnswer(122.0))

    // Verify that the answer submission failed as expected.
    val answerOutcome = monitorFactory.waitForNextSuccessfulResult(result)
    assertThat(answerOutcome.destinationCase).isEqualTo(AnswerOutcome.DestinationCase.SAME_STATE)
    assertThat(answerOutcome.feedback.html).contains("It's less than that.")
  }

  @Test
  fun testSubmitAnswer_forContinue_returnsOutcomeWithTransition() {
    restartExploration(TEST_TOPIC_ID_0, TEST_STORY_ID_0, TEST_EXPLORATION_ID_2)
    waitForGetCurrentStateSuccessfulLoad()
    // The first state of the exploration is the Continue interaction.

    val result = explorationProgressController.submitAnswer(createContinueButtonAnswer())

    // Verify that the continue button succeeds by default.
    val answerOutcome = monitorFactory.waitForNextSuccessfulResult(result)
    assertThat(answerOutcome.destinationCase).isEqualTo(AnswerOutcome.DestinationCase.STATE_NAME)
    assertThat(answerOutcome.feedback.html).contains("Continuing onward")
  }

  @Test
  fun testGetCurrentState_eleventhState_isTerminalState() {
    restartExploration(TEST_TOPIC_ID_0, TEST_STORY_ID_0, TEST_EXPLORATION_ID_2)
    waitForGetCurrentStateSuccessfulLoad()

    val ephemeralState = playThroughPrototypeExploration()

    // Verify that the last state is terminal.
    assertThat(ephemeralState.stateTypeCase).isEqualTo(TERMINAL_STATE)
  }

  @Test
  fun testGetCurrentState_afterMoveToPrevious_onThirdState_updatesToCompletedSecondState() {
    restartExploration(TEST_TOPIC_ID_0, TEST_STORY_ID_0, TEST_EXPLORATION_ID_2)
    waitForGetCurrentStateSuccessfulLoad()
    playThroughPrototypeState1AndMoveToNextState()
    playThroughPrototypeState2AndMoveToNextState()

    val ephemeralState = moveToPreviousState()

    // Verify that the current state is the second state, and is completed. It should also have the
    // previously submitted answer, allowing learners to potentially view past answers.
    assertThat(ephemeralState.stateTypeCase).isEqualTo(COMPLETED_STATE)
    assertThat(ephemeralState.state.name).isEqualTo("Fractions")
    assertThat(ephemeralState.completedState.getAnswer(0).userAnswer.answer.fraction)
      .isEqualTo(
        Fraction.newBuilder().apply {
          numerator = 1
          denominator = 2
        }.build()
      )
  }

  @Test
  fun testMoveToNext_onFinalState_failsWithError() {
    restartExploration(TEST_TOPIC_ID_0, TEST_STORY_ID_0, TEST_EXPLORATION_ID_2)
    waitForGetCurrentStateSuccessfulLoad()
    playThroughPrototypeExploration()

    val moveToStateResult = explorationProgressController.moveToNextState()

    // Verify we can't navigate past the last state of the exploration.
    val error = monitorFactory.waitForNextFailureResult(moveToStateResult)
    assertThat(error)
      .hasMessageThat()
      .contains("Cannot navigate to next state; at most recent state.")
  }

  @Test
  fun testGetCurrentState_afterPlayingFullSecondExploration_returnsTerminalState() {
    restartExploration(TEST_TOPIC_ID_0, TEST_STORY_ID_0, TEST_EXPLORATION_ID_13)
    waitForGetCurrentStateSuccessfulLoad()

    submitImageRegionAnswer(clickX = 0.5f, clickY = 0.5f, clickedRegion = "Saturn")
    val ephemeralState = moveToNextState()

    // Verify that we're now on the final state.
    assertThat(ephemeralState.stateTypeCase).isEqualTo(TERMINAL_STATE)
  }

  @Test
  fun testGetCurrentState_afterPlayingFullSecondExploration_diffPath_returnsTerminalState() {
    // Click on Jupiter before Saturn to take a slightly different (valid) path through the
    // exploration. (Note that this does not include actual branching).
    restartExploration(TEST_TOPIC_ID_0, TEST_STORY_ID_0, TEST_EXPLORATION_ID_13)
    waitForGetCurrentStateSuccessfulLoad()

    submitImageRegionAnswer(clickX = 0.2f, clickY = 0.5f, clickedRegion = "Jupiter")
    submitImageRegionAnswer(clickX = 0.5f, clickY = 0.5f, clickedRegion = "Saturn")
    val ephemeralState = moveToNextState()

    // Verify that a different path can also result in reaching the end state.
    assertThat(ephemeralState.stateTypeCase).isEqualTo(TERMINAL_STATE)
  }

  @Test
  fun testGetCurrentState_afterPlayingThroughPreviousExplorations_returnsStateFromSecondExp() {
    restartExploration(TEST_TOPIC_ID_0, TEST_STORY_ID_0, TEST_EXPLORATION_ID_2)
    waitForGetCurrentStateSuccessfulLoad()
    playThroughPrototypeExploration()
    endExploration()

    restartExploration(TEST_TOPIC_ID_0, TEST_STORY_ID_0, TEST_EXPLORATION_ID_13)
    waitForGetCurrentStateSuccessfulLoad()
    val ephemeralState =
      submitImageRegionAnswer(clickX = 0.2f, clickY = 0.5f, clickedRegion = "Jupiter")

    // Verify that we're on the second-to-last state of the second exploration.
    assertThat(ephemeralState.stateTypeCase).isEqualTo(PENDING_STATE)
    // This state is not in the other test exp.
    assertThat(ephemeralState.state.name).isEqualTo("ImageClickInput")
  }

  @Test
  fun testMoveToPrevious_navigatedForwardThenBackToInitial_failsWithError_logsException() {
    restartExploration(TEST_TOPIC_ID_0, TEST_STORY_ID_0, TEST_EXPLORATION_ID_2)
    waitForGetCurrentStateSuccessfulLoad()
    playThroughPrototypeState1AndMoveToNextState()
    moveToPreviousState()

    explorationProgressController.moveToPreviousState()
    testCoroutineDispatchers.runCurrent()

    val exception = fakeExceptionLogger.getMostRecentException()
    assertThat(exception).isInstanceOf(IllegalStateException::class.java)
    assertThat(exception)
      .hasMessageThat()
      .contains("Cannot navigate to previous state; at initial state.")
  }

  @Test
  fun testGetCurrentState_playInvalidExploration_returnsFailure_logsException() {
    restartExploration(INVALID_TOPIC_ID, INVALID_STORY_ID, INVALID_EXPLORATION_ID)

    waitForGetCurrentStateFailureLoad()

    val exception = fakeExceptionLogger.getMostRecentException()
    assertThat(exception).isInstanceOf(IllegalStateException::class.java)
    assertThat(exception).hasMessageThat().contains("Asset doesn't exist: $INVALID_EXPLORATION_ID")
  }

  @Test
  fun testCheckpointing_loadExploration_checkCheckpointIsSaved() {
    startPlayingNewExploration(TEST_TOPIC_ID_0, TEST_STORY_ID_0, TEST_EXPLORATION_ID_2)
    waitForGetCurrentStateSuccessfulLoad()

    val result =
      explorationCheckpointController.retrieveExplorationCheckpoint(
        profileId, TEST_EXPLORATION_ID_2
      )

    monitorFactory.waitForNextSuccessfulResult(result)
  }

  @Test
  fun testCheckpointing_playThroughMultipleStates_verifyCheckpointHasCorrectPendingStateName() {
    startPlayingNewExploration(TEST_TOPIC_ID_0, TEST_STORY_ID_0, TEST_EXPLORATION_ID_2)
    waitForGetCurrentStateSuccessfulLoad()

    assertThat(retrieveCheckpointStateName(TEST_EXPLORATION_ID_2)).isEqualTo("Continue")

    playThroughPrototypeState1AndMoveToNextState()
    assertThat(retrieveCheckpointStateName(TEST_EXPLORATION_ID_2)).isEqualTo("Fractions")

    playThroughPrototypeState2AndMoveToNextState()
    assertThat(retrieveCheckpointStateName(TEST_EXPLORATION_ID_2)).isEqualTo("MultipleChoice")

    playThroughPrototypeState3AndMoveToNextState()
    assertThat(retrieveCheckpointStateName(TEST_EXPLORATION_ID_2)).isEqualTo("ItemSelectionMinOne")
  }

  @Test
  fun testCheckpointing_advToFourthState_backToPrevState_verifyCheckpointHasCorrectPendingState() {
    startPlayingNewExploration(TEST_TOPIC_ID_0, TEST_STORY_ID_0, TEST_EXPLORATION_ID_2)
    waitForGetCurrentStateSuccessfulLoad()

    playThroughPrototypeState1AndMoveToNextState()
    playThroughPrototypeState2AndMoveToNextState()
    playThroughPrototypeState3AndMoveToNextState()
    assertThat(retrieveCheckpointStateName(TEST_EXPLORATION_ID_2)).isEqualTo("ItemSelectionMinOne")
    moveToPreviousState()

    assertThat(retrieveCheckpointStateName(TEST_EXPLORATION_ID_2)).isEqualTo("ItemSelectionMinOne")
  }

  @Test
  fun testCheckpointing_backTwoStates_nextState_verifyCheckpointHasCorrectPendingState() {
    startPlayingNewExploration(TEST_TOPIC_ID_0, TEST_STORY_ID_0, TEST_EXPLORATION_ID_2)
    waitForGetCurrentStateSuccessfulLoad()

    playThroughPrototypeState1AndMoveToNextState()
    playThroughPrototypeState2AndMoveToNextState()
    playThroughPrototypeState3AndMoveToNextState()
    moveToPreviousState()
    moveToPreviousState()
    moveToNextState()

    assertThat(retrieveCheckpointStateName(TEST_EXPLORATION_ID_2)).isEqualTo("ItemSelectionMinOne")
  }

  @Test
  fun testCheckpointing_advanceToThirdState_submitMultipleAns_checkCheckpointIsSavedAfterEachAns() {
    startPlayingNewExploration(TEST_TOPIC_ID_0, TEST_STORY_ID_0, TEST_EXPLORATION_ID_2)
    waitForGetCurrentStateSuccessfulLoad()

    playThroughPrototypeState1AndMoveToNextState()
    playThroughPrototypeState2AndMoveToNextState()
    // option 2 is the correct answer to the third state.
    submitMultipleChoiceAnswer(choiceIndex = 0)
    testCoroutineDispatchers.runCurrent()

    assertThat(retrieveCheckpointPendingAnswerCount(TEST_EXPLORATION_ID_2)).isEqualTo(1)

    // option 2 is the correct answer to the third state.
    submitMultipleChoiceAnswer(choiceIndex = 1)
    testCoroutineDispatchers.runCurrent()

    assertThat(retrieveCheckpointPendingAnswerCount(TEST_EXPLORATION_ID_2)).isEqualTo(2)

    // option 2 is the correct answer to the third state.
    submitMultipleChoiceAnswer(choiceIndex = 2)
    testCoroutineDispatchers.runCurrent()

    // count should be equal to zero because on submission of the correct answer, the
    // pendingTopState changes.
    assertThat(retrieveCheckpointPendingAnswerCount(TEST_EXPLORATION_ID_2)).isEqualTo(0)
  }

  @Test
  fun testCheckpointing_advToThirdState_submitAns_prevState_checkCheckpointIsSavedAfterEachAns() {
    startPlayingNewExploration(TEST_TOPIC_ID_0, TEST_STORY_ID_0, TEST_EXPLORATION_ID_2)
    waitForGetCurrentStateSuccessfulLoad()

    playThroughPrototypeState1AndMoveToNextState()
    playThroughPrototypeState2AndMoveToNextState()
    // option 2 is the correct answer to the third state.
    submitMultipleChoiceAnswer(choiceIndex = 1)
    testCoroutineDispatchers.runCurrent()

    assertThat(retrieveCheckpointPendingAnswerCount(TEST_EXPLORATION_ID_2)).isEqualTo(1)

    // option 2 is the correct answer to the third state.
    submitMultipleChoiceAnswer(choiceIndex = 1)
    testCoroutineDispatchers.runCurrent()

    assertThat(retrieveCheckpointPendingAnswerCount(TEST_EXPLORATION_ID_2)).isEqualTo(2)

    moveToPreviousState()

    assertThat(retrieveCheckpointPendingAnswerCount(TEST_EXPLORATION_ID_2)).isEqualTo(2)
  }

  @Test
  fun testCheckpointing_advToThirdState_moveToPrevState_checkCheckpointHasStateIndexOfThirdState() {
    startPlayingNewExploration(TEST_TOPIC_ID_0, TEST_STORY_ID_0, TEST_EXPLORATION_ID_2)
    waitForGetCurrentStateSuccessfulLoad()

    playThroughPrototypeState1AndMoveToNextState()
    playThroughPrototypeState2AndMoveToNextState()
    moveToPreviousState()

    assertThat(retrieveCheckpointStateIndex(TEST_EXPLORATION_ID_2)).isEqualTo(2)
  }

  @Test
  fun testCheckpointing_advToThirdState_prevStates_nextState_checkCheckpointHasCorrectStateIndex() {
    startPlayingNewExploration(TEST_TOPIC_ID_0, TEST_STORY_ID_0, TEST_EXPLORATION_ID_2)
    waitForGetCurrentStateSuccessfulLoad()

    playThroughPrototypeState1AndMoveToNextState()
    playThroughPrototypeState2AndMoveToNextState()
    moveToPreviousState()
    moveToPreviousState()
    moveToNextState()

    assertThat(retrieveCheckpointStateIndex(TEST_EXPLORATION_ID_2)).isEqualTo(2)
  }

  @Test
  fun testCheckpointing_hintIsVisible_checkHintIsSavedInCheckpoint() {
    startPlayingNewExploration(TEST_TOPIC_ID_0, TEST_STORY_ID_0, TEST_EXPLORATION_ID_2)
    waitForGetCurrentStateSuccessfulLoad()

    navigateToPrototypeFractionInputState()
    // Submit 2 wrong answers to trigger the hint.
    submitWrongAnswerForPrototypeState2()
    submitWrongAnswerForPrototypeState2()

    val helpIndex = retrieveCheckpointHelpIndex(TEST_EXPLORATION_ID_2)
    assertThat(helpIndex.indexTypeCase).isEqualTo(NEXT_AVAILABLE_HINT_INDEX)
    assertThat(helpIndex.nextAvailableHintIndex).isEqualTo(0)
  }

  @Test
  fun testCheckpointing_revealHint_checkHintIsSavedInCheckpoint() {
    startPlayingNewExploration(TEST_TOPIC_ID_0, TEST_STORY_ID_0, TEST_EXPLORATION_ID_2)
    waitForGetCurrentStateSuccessfulLoad()

    navigateToPrototypeFractionInputState()
    // Submit 2 wrong answers to trigger the hint.
    submitWrongAnswerForPrototypeState2()
    submitWrongAnswerForPrototypeState2()

    monitorFactory.waitForNextSuccessfulResult(
      explorationProgressController.submitHintIsRevealed(hintIndex = 0)
    )
    val helpIndex = retrieveCheckpointHelpIndex(TEST_EXPLORATION_ID_2)
    assertThat(helpIndex.indexTypeCase).isEqualTo(LATEST_REVEALED_HINT_INDEX)
    assertThat(helpIndex.latestRevealedHintIndex).isEqualTo(0)
  }

  @Test
  fun testCheckpointing_solutionIsVisible_checkCheckpointIsSaved() {
    startPlayingNewExploration(TEST_TOPIC_ID_0, TEST_STORY_ID_0, TEST_EXPLORATION_ID_2)
    waitForGetCurrentStateSuccessfulLoad()

    navigateToPrototypeFractionInputState()
    // Submit 2 wrong answers to trigger the hint.
    submitWrongAnswerForPrototypeState2()
    submitWrongAnswerForPrototypeState2()
    // Reveal the hint, then submit another wrong answer to trigger the solution.
    monitorFactory.waitForNextSuccessfulResult(
      explorationProgressController.submitHintIsRevealed(hintIndex = 0)
    )
    submitWrongAnswerForPrototypeState2()
    testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(10))

    val helpIndex = retrieveCheckpointHelpIndex(TEST_EXPLORATION_ID_2)
    assertThat(helpIndex.indexTypeCase).isEqualTo(SHOW_SOLUTION)
    assertThat(helpIndex.showSolution).isTrue()
  }

  @Test
  fun testCheckpointing_revealSolution_checkCheckpointIsSaved() {
    startPlayingNewExploration(TEST_TOPIC_ID_0, TEST_STORY_ID_0, TEST_EXPLORATION_ID_2)
    waitForGetCurrentStateSuccessfulLoad()

    navigateToPrototypeFractionInputState()
    // Submit 2 wrong answers to trigger the hint.
    submitWrongAnswerForPrototypeState2()
    submitWrongAnswerForPrototypeState2()
    // Reveal the hint, then submit another wrong answer to trigger the solution.
    monitorFactory.waitForNextSuccessfulResult(
      explorationProgressController.submitHintIsRevealed(hintIndex = 0)
    )
    submitWrongAnswerForPrototypeState2()
    testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(10))

    monitorFactory.waitForNextSuccessfulResult(
      explorationProgressController.submitSolutionIsRevealed()
    )
    val helpIndex = retrieveCheckpointHelpIndex(TEST_EXPLORATION_ID_2)
    assertThat(helpIndex.indexTypeCase).isEqualTo(EVERYTHING_REVEALED)
    assertThat(helpIndex.everythingRevealed).isTrue()
  }

  @Test
  fun testCheckpointing_onStateWithContinueInteraction_pressContinue_correctCheckpointIsSaved() {
    startPlayingNewExploration(TEST_TOPIC_ID_0, TEST_STORY_ID_0, TEST_EXPLORATION_ID_2)
    waitForGetCurrentStateSuccessfulLoad()

    playThroughPrototypeState1AndMoveToNextState()

    // Verify that checkpoint is saved when the exploration moves to the new pendingTopState.
    assertThat(retrieveCheckpointStateName(TEST_EXPLORATION_ID_2)).isEqualTo("Fractions")
  }

  @Test
  fun testCheckpointing_noCheckpointSaved_checkCheckpointStateIsUnsaved() {
    // 'Replay' the exploration (since the only way to not have saved progress is for the lesson to
    // already be completed).
    replayExploration(TEST_TOPIC_ID_0, TEST_STORY_ID_0, TEST_EXPLORATION_ID_2)
    val ephemeralState = waitForGetCurrentStateSuccessfulLoad()

    assertThat(ephemeralState.checkpointState).isEqualTo(CheckpointState.CHECKPOINT_UNSAVED)
  }

  @Test
  fun testCheckpointing_saveCheckpoint_checkCheckpointStateIsSavedDatabaseNotExceededLimit() {
    startPlayingNewExploration(TEST_TOPIC_ID_0, TEST_STORY_ID_0, TEST_EXPLORATION_ID_2)
    val ephemeralState = waitForGetCurrentStateSuccessfulLoad()

    assertThat(ephemeralState.checkpointState)
      .isEqualTo(CheckpointState.CHECKPOINT_SAVED_DATABASE_NOT_EXCEEDED_LIMIT)
  }

  @Test
  fun testCheckpointing_saveCheckpoint_databaseFull_checkpointStateIsSavedDatabaseExceededLimit() {
    startPlayingNewExploration(TEST_TOPIC_ID_0, TEST_STORY_ID_0, TEST_EXPLORATION_ID_2)
    waitForGetCurrentStateSuccessfulLoad()
    // For testing, size limit of checkpoint database is set to 150 Bytes, this makes the database
    // exceed the allocated limit when checkpoint is saved on completing prototypeState 2.
    playThroughPrototypeState1AndMoveToNextState()
    val ephemeralState = playThroughPrototypeState2AndMoveToNextState()

    assertThat(ephemeralState.checkpointState)
      .isEqualTo(CheckpointState.CHECKPOINT_SAVED_DATABASE_EXCEEDED_LIMIT)
  }

  @Test
  fun testCheckpointing_onSecondState_resumeExploration_expResumedFromCorrectPendingState() {
    startPlayingNewExploration(TEST_TOPIC_ID_0, TEST_STORY_ID_0, TEST_EXPLORATION_ID_2)
    waitForGetCurrentStateSuccessfulLoad()

    playThroughPrototypeState1AndMoveToNextState()
    endExploration()

    val checkpoint = retrieveExplorationCheckpoint(TEST_EXPLORATION_ID_2)
    resumeExploration(TEST_TOPIC_ID_0, TEST_STORY_ID_0, TEST_EXPLORATION_ID_2, checkpoint)
    val ephemeralState = waitForGetCurrentStateSuccessfulLoad()

    // Verify that we're on the second state of the second exploration.
    assertThat(ephemeralState.stateTypeCase).isEqualTo(PENDING_STATE)
    assertThat(ephemeralState.state.name).isEqualTo("Fractions")
  }

  @Test
  fun testCheckpointing_onSecondState_navigateBack_resumeExploration_checkResumedFromSecondState() {
    startPlayingNewExploration(TEST_TOPIC_ID_0, TEST_STORY_ID_0, TEST_EXPLORATION_ID_2)
    waitForGetCurrentStateSuccessfulLoad()

    playThroughPrototypeState1AndMoveToNextState()
    moveToPreviousState()
    endExploration()

    val checkpoint = retrieveExplorationCheckpoint(TEST_EXPLORATION_ID_2)
    resumeExploration(TEST_TOPIC_ID_0, TEST_STORY_ID_0, TEST_EXPLORATION_ID_2, checkpoint)
    val ephemeralState = waitForGetCurrentStateSuccessfulLoad()

    // Verify that we're on the second state of the second exploration.
    assertThat(ephemeralState.stateTypeCase).isEqualTo(PENDING_STATE)
    assertThat(ephemeralState.state.name).isEqualTo("Fractions")
  }

  @Test
  fun testCheckpointing_onSecondState_submitWrongAns_resumeExploration_checkWrongAnswersVisible() {
    startPlayingNewExploration(TEST_TOPIC_ID_0, TEST_STORY_ID_0, TEST_EXPLORATION_ID_2)
    waitForGetCurrentStateSuccessfulLoad()

    playThroughPrototypeState1AndMoveToNextState()
    submitWrongAnswerForPrototypeState2()
    submitWrongAnswerForPrototypeState2()
    submitWrongAnswerForPrototypeState2()
    endExploration()

    val checkpoint = retrieveExplorationCheckpoint(TEST_EXPLORATION_ID_2)
    resumeExploration(TEST_TOPIC_ID_0, TEST_STORY_ID_0, TEST_EXPLORATION_ID_2, checkpoint)
    val ephemeralState = waitForGetCurrentStateSuccessfulLoad()

    // Verify that three wrong answers are visible to the user.
    assertThat(ephemeralState.stateTypeCase).isEqualTo(PENDING_STATE)
    assertThat(ephemeralState.pendingState.wrongAnswerCount).isEqualTo(3)
  }

  @Test
  fun testCheckpointing_onSecondState_submitRightAns_resumeExploration_expResumedFromCompState() {
    startPlayingNewExploration(TEST_TOPIC_ID_0, TEST_STORY_ID_0, TEST_EXPLORATION_ID_2)
    waitForGetCurrentStateSuccessfulLoad()

    playThroughPrototypeState1AndMoveToNextState()
    submitWrongAnswerForPrototypeState2()
    submitWrongAnswerForPrototypeState2()
    submitPrototypeState2Answer()
    endExploration()

    val checkpoint = retrieveExplorationCheckpoint(TEST_EXPLORATION_ID_2)
    resumeExploration(TEST_TOPIC_ID_0, TEST_STORY_ID_0, TEST_EXPLORATION_ID_2, checkpoint)
    val ephemeralState = waitForGetCurrentStateSuccessfulLoad()

    // Verify that we're on the second state of the second exploration because the continue button
    // was not pressed after submitting the correct answer.
    assertThat(ephemeralState.stateTypeCase).isEqualTo(COMPLETED_STATE)
    assertThat(ephemeralState.state.name).isEqualTo("Fractions")
  }

  @Test
  fun testCheckpointing_submitAns_moveToNextState_resumeExploration_answersVisibleOnPrevState() {
    startPlayingNewExploration(TEST_TOPIC_ID_0, TEST_STORY_ID_0, TEST_EXPLORATION_ID_2)
    waitForGetCurrentStateSuccessfulLoad()

    playThroughPrototypeState1AndMoveToNextState()
    submitWrongAnswerForPrototypeState2()
    submitWrongAnswerForPrototypeState2()
    playThroughPrototypeState2AndMoveToNextState()
    endExploration()

    val checkpoint = retrieveExplorationCheckpoint(TEST_EXPLORATION_ID_2)
    resumeExploration(TEST_TOPIC_ID_0, TEST_STORY_ID_0, TEST_EXPLORATION_ID_2, checkpoint)
    waitForGetCurrentStateSuccessfulLoad()
    val ephemeralState = moveToPreviousState()

    // Verify that we're on the second state of the second exploration because the continue button
    // was not pressed after submitting the correct answer.
    assertThat(ephemeralState.stateTypeCase).isEqualTo(COMPLETED_STATE)
    assertThat(ephemeralState.state.name).isEqualTo("Fractions")
    assertThat(ephemeralState.completedState.answerCount).isEqualTo(3)
  }

  @Test
  fun testCheckpointing_onSecondState_resumeExploration_checkPendingStateDoesNotHaveANextState() {
    startPlayingNewExploration(TEST_TOPIC_ID_0, TEST_STORY_ID_0, TEST_EXPLORATION_ID_2)
    waitForGetCurrentStateSuccessfulLoad()

    playThroughPrototypeState1AndMoveToNextState()
    endExploration()

    val checkpoint = retrieveExplorationCheckpoint(TEST_EXPLORATION_ID_2)
    resumeExploration(TEST_TOPIC_ID_0, TEST_STORY_ID_0, TEST_EXPLORATION_ID_2, checkpoint)
    val ephemeralState = waitForGetCurrentStateSuccessfulLoad()

    // Verify that we're on the second state of the second exploration because the continue button
    // was not pressed after submitting the correct answer.
    assertThat(ephemeralState.stateTypeCase).isEqualTo(PENDING_STATE)
    assertThat(ephemeralState.state.name).isEqualTo("Fractions")
    assertThat(ephemeralState.hasNextState).isFalse()
  }

  @Test
  fun testCheckpointing_onFirstState_resumeExploration_checkStateDoesNotHaveAPrevState() {
    startPlayingNewExploration(TEST_TOPIC_ID_0, TEST_STORY_ID_0, TEST_EXPLORATION_ID_2)
    waitForGetCurrentStateSuccessfulLoad()
    endExploration()

    val checkpoint = retrieveExplorationCheckpoint(TEST_EXPLORATION_ID_2)
    resumeExploration(TEST_TOPIC_ID_0, TEST_STORY_ID_0, TEST_EXPLORATION_ID_2, checkpoint)
    val ephemeralState = waitForGetCurrentStateSuccessfulLoad()

    // Verify that we're on the second state of the second exploration because the continue button
    // was not pressed after submitting the correct answer.
    assertThat(ephemeralState.stateTypeCase).isEqualTo(PENDING_STATE)
    assertThat(ephemeralState.state.name).isEqualTo("Continue")
    assertThat(ephemeralState.hasPreviousState).isFalse()
  }

  @Test
  fun testCheckpointing_onSecondState_resumeExploration_checkFirstStateHasANextState() {
    startPlayingNewExploration(TEST_TOPIC_ID_0, TEST_STORY_ID_0, TEST_EXPLORATION_ID_2)
    waitForGetCurrentStateSuccessfulLoad()

    playThroughPrototypeState1AndMoveToNextState()
    endExploration()

    val checkpoint = retrieveExplorationCheckpoint(TEST_EXPLORATION_ID_2)
    resumeExploration(TEST_TOPIC_ID_0, TEST_STORY_ID_0, TEST_EXPLORATION_ID_2, checkpoint)
    waitForGetCurrentStateSuccessfulLoad()
    val ephemeralState = moveToPreviousState()

    // Verify that we're on the second state of the second exploration because the continue button
    // was not pressed after submitting the correct answer.
    assertThat(ephemeralState.stateTypeCase).isEqualTo(COMPLETED_STATE)
    assertThat(ephemeralState.state.name).isEqualTo("Continue")
    assertThat(ephemeralState.hasNextState).isTrue()
  }

  @Test
  fun testCheckpointing_onSecondState_resumeExploration_checkFirstStateDoesNotHaveAPrevState() {
    startPlayingNewExploration(TEST_TOPIC_ID_0, TEST_STORY_ID_0, TEST_EXPLORATION_ID_2)
    waitForGetCurrentStateSuccessfulLoad()

    playThroughPrototypeState1AndMoveToNextState()
    endExploration()

    val checkpoint = retrieveExplorationCheckpoint(TEST_EXPLORATION_ID_2)
    resumeExploration(TEST_TOPIC_ID_0, TEST_STORY_ID_0, TEST_EXPLORATION_ID_2, checkpoint)
    waitForGetCurrentStateSuccessfulLoad()
    val ephemeralState = moveToPreviousState()

    // Verify that we're on the second state of the second exploration because the continue button
    // was not pressed after submitting the correct answer.
    assertThat(ephemeralState.stateTypeCase).isEqualTo(COMPLETED_STATE)
    assertThat(ephemeralState.state.name).isEqualTo("Continue")
    assertThat(ephemeralState.hasPreviousState).isFalse()
  }

  @Test
  fun testCheckpointing_onSecondState_resumeExploration_checkSecondStateHasAPrevState() {
    startPlayingNewExploration(TEST_TOPIC_ID_0, TEST_STORY_ID_0, TEST_EXPLORATION_ID_2)
    waitForGetCurrentStateSuccessfulLoad()

    playThroughPrototypeState1AndMoveToNextState()
    endExploration()

    val checkpoint = retrieveExplorationCheckpoint(TEST_EXPLORATION_ID_2)
    resumeExploration(TEST_TOPIC_ID_0, TEST_STORY_ID_0, TEST_EXPLORATION_ID_2, checkpoint)
    val ephemeralState = waitForGetCurrentStateSuccessfulLoad()

    // Verify that we're on the second state of the second exploration because the continue button
    // was not pressed after submitting the correct answer.
    assertThat(ephemeralState.stateTypeCase).isEqualTo(PENDING_STATE)
    assertThat(ephemeralState.state.name).isEqualTo("Fractions")
    assertThat(ephemeralState.hasPreviousState).isTrue()
  }

  @Test
  fun testCheckpointing_submitAns_doNotPressContinueBtn_resumeExp_pendingStateHasNoNextState() {
    startPlayingNewExploration(TEST_TOPIC_ID_0, TEST_STORY_ID_0, TEST_EXPLORATION_ID_2)
    waitForGetCurrentStateSuccessfulLoad()

    playThroughPrototypeState1AndMoveToNextState()
    submitPrototypeState2Answer()
    endExploration()

    val checkpoint = retrieveExplorationCheckpoint(TEST_EXPLORATION_ID_2)
    resumeExploration(TEST_TOPIC_ID_0, TEST_STORY_ID_0, TEST_EXPLORATION_ID_2, checkpoint)
    val ephemeralState = waitForGetCurrentStateSuccessfulLoad()

    // Verify that the current state is a completed state but has no next state because we have
    // not navigated to the latest pending state yet.
    assertThat(ephemeralState.stateTypeCase).isEqualTo(COMPLETED_STATE)
    assertThat(ephemeralState.state.name).isEqualTo("Fractions")
    assertThat(ephemeralState.hasNextState).isFalse()
  }

  @Test
  fun testCheckpointing_noHintVisible_resumeExp_notHintVisibleOnPendingState() {
    startPlayingNewExploration(TEST_TOPIC_ID_0, TEST_STORY_ID_0, TEST_EXPLORATION_ID_2)
    waitForGetCurrentStateSuccessfulLoad()

    playThroughPrototypeState1AndMoveToNextState()
    endExploration()

    val checkpoint = retrieveExplorationCheckpoint(TEST_EXPLORATION_ID_2)
    resumeExploration(TEST_TOPIC_ID_0, TEST_STORY_ID_0, TEST_EXPLORATION_ID_2, checkpoint)
    val ephemeralState = waitForGetCurrentStateSuccessfulLoad()

    // Verify that the helpIndex.IndexTypeCase is equal to INDEX_TYPE_NOT_SET because no hint
    // is visible yet.
    assertThat(ephemeralState.pendingState.helpIndex.indexTypeCase)
      .isEqualTo(HelpIndex.IndexTypeCase.INDEXTYPE_NOT_SET)
  }

  @Test
  fun testCheckpointing_unrevealedHintIsVisible_resumeExp_unrevealedHintIsVisibleOnPendingState() {
    startPlayingNewExploration(TEST_TOPIC_ID_0, TEST_STORY_ID_0, TEST_EXPLORATION_ID_2)
    waitForGetCurrentStateSuccessfulLoad()

    playThroughPrototypeState1AndMoveToNextState()
    // Make the first hint visible by submitting two wrong answers.
    submitWrongAnswerForPrototypeState2()
    submitWrongAnswerForPrototypeState2()
    endExploration()

    val checkpoint = retrieveExplorationCheckpoint(TEST_EXPLORATION_ID_2)
    resumeExploration(TEST_TOPIC_ID_0, TEST_STORY_ID_0, TEST_EXPLORATION_ID_2, checkpoint)
    val ephemeralState = waitForGetCurrentStateSuccessfulLoad()

    // Verify that the helpIndex.IndexTypeCase is equal AVAILABLE_NEXT_HINT_HINT_INDEX because a new
    // unrevealed hint is visible
    assertThat(ephemeralState.pendingState.helpIndex.indexTypeCase)
      .isEqualTo(NEXT_AVAILABLE_HINT_INDEX)
    assertThat(ephemeralState.isHintRevealed(0)).isFalse()
    assertThat(ephemeralState.pendingState.helpIndex.indexTypeCase)
      .isEqualTo(NEXT_AVAILABLE_HINT_INDEX)
    assertThat(ephemeralState.pendingState.helpIndex.nextAvailableHintIndex).isEqualTo(0)
  }

  @Test
  fun testCheckpointing_revealedHintIsVisible_resumeExp_revealedHintIsVisibleOnPendingState() {
    startPlayingNewExploration(TEST_TOPIC_ID_0, TEST_STORY_ID_0, TEST_EXPLORATION_ID_2)
    waitForGetCurrentStateSuccessfulLoad()

    playThroughPrototypeState1AndMoveToNextState()
    submitWrongAnswerForPrototypeState2()
    submitWrongAnswerForPrototypeState2()
    monitorFactory.waitForNextSuccessfulResult(
      explorationProgressController.submitHintIsRevealed(hintIndex = 0)
    )
    endExploration()

    val checkpoint = retrieveExplorationCheckpoint(TEST_EXPLORATION_ID_2)
    resumeExploration(TEST_TOPIC_ID_0, TEST_STORY_ID_0, TEST_EXPLORATION_ID_2, checkpoint)
    val ephemeralState = waitForGetCurrentStateSuccessfulLoad()

    // Verify that the helpIndex.IndexTypeCase is equal LATEST_REVEALED_HINT_INDEX because a new
    // revealed hint is visible
    assertThat(ephemeralState.pendingState.helpIndex.indexTypeCase)
      .isEqualTo(LATEST_REVEALED_HINT_INDEX)
    assertThat(ephemeralState.isHintRevealed(0)).isTrue()
  }

  @Test
  fun testCheckpointing_revealedHintIsVisible_resumeExp_wait10Seconds_solutionIsNotVisible() {
    startPlayingNewExploration(TEST_TOPIC_ID_0, TEST_STORY_ID_0, TEST_EXPLORATION_ID_2)
    waitForGetCurrentStateSuccessfulLoad()

    playThroughPrototypeState1AndMoveToNextState()
    submitWrongAnswerForPrototypeState2()
    submitWrongAnswerForPrototypeState2()
    monitorFactory.waitForNextSuccessfulResult(
      explorationProgressController.submitHintIsRevealed(hintIndex = 0)
    )
    endExploration()

    val checkpoint = retrieveExplorationCheckpoint(TEST_EXPLORATION_ID_2)
    resumeExploration(TEST_TOPIC_ID_0, TEST_STORY_ID_0, TEST_EXPLORATION_ID_2, checkpoint)
    waitForGetCurrentStateSuccessfulLoad()
    testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(10))

    // Verify that the helpIndex.IndexTypeCase is equal LATEST_REVEALED_HINT_INDEX because a new
    // revealed hint is visible
    val ephemeralState = waitForGetCurrentStateSuccessfulLoad()
    assertThat(ephemeralState.pendingState.helpIndex.indexTypeCase)
      .isEqualTo(LATEST_REVEALED_HINT_INDEX)
    assertThat(ephemeralState.isHintRevealed(0)).isTrue()
    assertThat(ephemeralState.isSolutionRevealed()).isFalse()
  }

  @Test
  fun testCheckpointing_revealedHintIsVisible_resumeExp_wait30Seconds_solutionIsNotVisible() {
    startPlayingNewExploration(TEST_TOPIC_ID_0, TEST_STORY_ID_0, TEST_EXPLORATION_ID_2)
    waitForGetCurrentStateSuccessfulLoad()

    playThroughPrototypeState1AndMoveToNextState()
    submitWrongAnswerForPrototypeState2()
    submitWrongAnswerForPrototypeState2()
    monitorFactory.waitForNextSuccessfulResult(
      explorationProgressController.submitHintIsRevealed(hintIndex = 0)
    )
    endExploration()

    val checkpoint = retrieveExplorationCheckpoint(TEST_EXPLORATION_ID_2)
    resumeExploration(TEST_TOPIC_ID_0, TEST_STORY_ID_0, TEST_EXPLORATION_ID_2, checkpoint)
    waitForGetCurrentStateSuccessfulLoad()
    testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(30))

    // Verify that the helpIndex.IndexTypeCase is equal LATEST_REVEALED_HINT_INDEX because a new
    // revealed hint is visible
    val ephemeralState = waitForGetCurrentStateSuccessfulLoad()
    assertThat(ephemeralState.pendingState.helpIndex.indexTypeCase)
      .isEqualTo(SHOW_SOLUTION)
    assertThat(ephemeralState.isSolutionRevealed()).isFalse()
  }

  @Test
  fun testCheckpointing_solutionIsVisible_resumeExp_unrevealedSolutionIsVisibleOnPendingState() {
    startPlayingNewExploration(TEST_TOPIC_ID_0, TEST_STORY_ID_0, TEST_EXPLORATION_ID_2)
    waitForGetCurrentStateSuccessfulLoad()

    playThroughPrototypeState1AndMoveToNextState()
    submitWrongAnswerForPrototypeState2()
    submitWrongAnswerForPrototypeState2()

    // Reveal the hint, then submit another wrong answer to trigger the solution.
    monitorFactory.waitForNextSuccessfulResult(
      explorationProgressController.submitHintIsRevealed(hintIndex = 0)
    )

    testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(30))
    endExploration()

    val checkpoint = retrieveExplorationCheckpoint(TEST_EXPLORATION_ID_2)
    resumeExploration(TEST_TOPIC_ID_0, TEST_STORY_ID_0, TEST_EXPLORATION_ID_2, checkpoint)
    val ephemeralState = waitForGetCurrentStateSuccessfulLoad()

    // Verify that the helpIndex.IndexTypeCase is equal EVERYTHING_IS_REVEALED because all available
    // help has been revealed.
    assertThat(ephemeralState.pendingState.helpIndex.indexTypeCase)
      .isEqualTo(SHOW_SOLUTION)
    assertThat(ephemeralState.isHintRevealed(0)).isTrue()
    assertThat(ephemeralState.isSolutionRevealed()).isFalse()
  }

  @Test
  fun testCheckpointing_revealedSolution_resumeExp_revealedSolIsVisibleOnPendingState() {
    startPlayingNewExploration(TEST_TOPIC_ID_0, TEST_STORY_ID_0, TEST_EXPLORATION_ID_2)
    waitForGetCurrentStateSuccessfulLoad()

    playThroughPrototypeState1AndMoveToNextState()
    submitWrongAnswerForPrototypeState2()
    submitWrongAnswerForPrototypeState2()
    monitorFactory.waitForNextSuccessfulResult(
      explorationProgressController.submitHintIsRevealed(hintIndex = 0)
    )

    // The solution should be visible after 30 seconds of the last hint being reveled.
    testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(30))
    testCoroutineDispatchers.runCurrent()

    explorationProgressController.submitSolutionIsRevealed()
    testCoroutineDispatchers.runCurrent()
    endExploration()

    val checkpoint = retrieveExplorationCheckpoint(TEST_EXPLORATION_ID_2)
    resumeExploration(TEST_TOPIC_ID_0, TEST_STORY_ID_0, TEST_EXPLORATION_ID_2, checkpoint)
    val ephemeralState = waitForGetCurrentStateSuccessfulLoad()

    // Verify that the helpIndex.IndexTypeCase is equal EVERYTHING_IS_REVEALED because all available
    // help has been revealed.
    assertThat(ephemeralState.pendingState.helpIndex.indexTypeCase)
      .isEqualTo(EVERYTHING_REVEALED)
    assertThat(ephemeralState.isHintRevealed(0)).isTrue()
    assertThat(ephemeralState.isSolutionRevealed()).isTrue()
  }

  @Test
  fun testCheckpointing_playSomeStates_resumeExp_playRemainingState_verifyTerminalStateReached() {
    startPlayingNewExploration(TEST_TOPIC_ID_0, TEST_STORY_ID_0, TEST_EXPLORATION_ID_2)
    waitForGetCurrentStateSuccessfulLoad()

    // Play through some states in the exploration.
    playThroughPrototypeState1AndMoveToNextState()
    playThroughPrototypeState2AndMoveToNextState()
    playThroughPrototypeState3AndMoveToNextState()
    playThroughPrototypeState4AndMoveToNextState()
    playThroughPrototypeState5AndMoveToNextState()
    endExploration()

    val checkpoint = retrieveExplorationCheckpoint(TEST_EXPLORATION_ID_2)
    resumeExploration(TEST_TOPIC_ID_0, TEST_STORY_ID_0, TEST_EXPLORATION_ID_2, checkpoint)
    waitForGetCurrentStateSuccessfulLoad()

    // Resume exploration and play through the remaining states in the exploration.
    playThroughPrototypeState6AndMoveToNextState()
    playThroughPrototypeState7AndMoveToNextState()
    playThroughPrototypeState8AndMoveToNextState()
    playThroughPrototypeState9AndMoveToNextState()
    val ephemeralState = playThroughPrototypeState10AndMoveToNextState()

    // Verify that the last state is terminal.
    assertThat(ephemeralState.stateTypeCase).isEqualTo(TERMINAL_STATE)
  }

  /* Localization-based tests. */

  @Test
  @RunOn(buildEnvironments = [BuildEnvironment.BAZEL]) // Languages unsupported in Gradle builds.
  fun testGetCurrentState_englishLocale_defaultContentLang_includesTranslationContextForEnglish() {
    forceDefaultLocale(Locale.US)
    startPlayingNewExploration(TEST_TOPIC_ID_0, TEST_STORY_ID_0, TEST_EXPLORATION_ID_2)

    val ephemeralState = waitForGetCurrentStateSuccessfulLoad()

    // The context should be the default instance for English since the default strings of the
    // lesson are expected to be in English.
    val expectedContext = WrittenTranslationContext.newBuilder().apply {
      language = OppiaLanguage.ENGLISH
    }.build()
    assertThat(ephemeralState.writtenTranslationContext).isEqualTo(expectedContext)
  }

  @Test
  @RunOn(buildEnvironments = [BuildEnvironment.BAZEL]) // Languages unsupported in Gradle builds.
  fun testGetCurrentState_arabicLocale_defaultContentLang_includesTranslationContextForArabic() {
    forceDefaultLocale(EGYPT_ARABIC_LOCALE)
    startPlayingNewExploration(TEST_TOPIC_ID_0, TEST_STORY_ID_0, TEST_EXPLORATION_ID_2)

    val ephemeralState = waitForGetCurrentStateSuccessfulLoad()

    // Arabic translations should be included per the locale.
    assertThat(ephemeralState.writtenTranslationContext.language).isEqualTo(OppiaLanguage.ARABIC)
    assertThat(ephemeralState.writtenTranslationContext.translationsMap).isNotEmpty()
  }

  @Test
  fun testGetCurrentState_turkishLocale_defaultContentLang_includesDefaultTranslationContext() {
    forceDefaultLocale(TURKEY_TURKISH_LOCALE)
    startPlayingNewExploration(TEST_TOPIC_ID_0, TEST_STORY_ID_0, TEST_EXPLORATION_ID_2)

    val ephemeralState = waitForGetCurrentStateSuccessfulLoad()

    // No translations match to an unsupported language, so default to the built-in strings.
    assertThat(ephemeralState.writtenTranslationContext).isEqualToDefaultInstance()
  }

  @Test
  @RunOn(buildEnvironments = [BuildEnvironment.BAZEL]) // Languages unsupported in Gradle builds.
  fun testGetCurrentState_englishLangProfile_includesTranslationContextForEnglish() {
    val englishProfileId = ProfileId.newBuilder().apply { internalId = 1 }.build()
    updateContentLanguage(englishProfileId, OppiaLanguage.ENGLISH)
    startPlayingNewExploration(
      TEST_TOPIC_ID_0, TEST_STORY_ID_0, TEST_EXPLORATION_ID_2, englishProfileId
    )

    val ephemeralState = waitForGetCurrentStateSuccessfulLoad()

    // English translations means only a language specification.
    val expectedContext = WrittenTranslationContext.newBuilder().apply {
      language = OppiaLanguage.ENGLISH
    }.build()
    assertThat(ephemeralState.writtenTranslationContext).isEqualTo(expectedContext)
  }

  @Test
  @RunOn(buildEnvironments = [BuildEnvironment.BAZEL]) // Languages unsupported in Gradle builds.
  fun testGetCurrentState_englishLangProfile_switchToArabic_includesTranslationContextForArabic() {
    val englishProfileId = ProfileId.newBuilder().apply { internalId = 1 }.build()
    updateContentLanguage(englishProfileId, OppiaLanguage.ENGLISH)
    startPlayingNewExploration(
      TEST_TOPIC_ID_0, TEST_STORY_ID_0, TEST_EXPLORATION_ID_2, englishProfileId
    )
    val monitor = monitorFactory.createMonitor(explorationProgressController.getCurrentState())
    monitor.waitForNextSuccessResult()

    // Update the content language & wait for the ephemeral state to update.
    updateContentLanguage(englishProfileId, OppiaLanguage.ARABIC)
    val ephemeralState = monitor.ensureNextResultIsSuccess()

    // Switching to Arabic should result in a new ephemeral state with a translation context.
    assertThat(ephemeralState.writtenTranslationContext.language).isEqualTo(OppiaLanguage.ARABIC)
    assertThat(ephemeralState.writtenTranslationContext.translationsMap).isNotEmpty()
  }

  @Test
  @RunOn(buildEnvironments = [BuildEnvironment.BAZEL]) // Languages unsupported in Gradle builds.
  fun testGetCurrentState_arabicLangProfile_includesTranslationContextForArabic() {
    val englishProfileId = ProfileId.newBuilder().apply { internalId = 1 }.build()
    val arabicProfileId = ProfileId.newBuilder().apply { internalId = 2 }.build()
    updateContentLanguage(englishProfileId, OppiaLanguage.ENGLISH)
    updateContentLanguage(arabicProfileId, OppiaLanguage.ARABIC)
    startPlayingNewExploration(
      TEST_TOPIC_ID_0, TEST_STORY_ID_0, TEST_EXPLORATION_ID_2, arabicProfileId
    )

    val ephemeralState = waitForGetCurrentStateSuccessfulLoad()

    // Selecting the profile with Arabic translations should provide a translation context.
    assertThat(ephemeralState.writtenTranslationContext.language).isEqualTo(OppiaLanguage.ARABIC)
    assertThat(ephemeralState.writtenTranslationContext.translationsMap).isNotEmpty()
  }

  @Test
  fun testPlayNewExploration_logsStartCardEvent() {
    logIntoAnalyticsReadyAdminProfile()

    startPlayingNewExploration(TEST_TOPIC_ID_0, TEST_STORY_ID_0, TEST_EXPLORATION_ID_2)
    waitForGetCurrentStateSuccessfulLoad()

    val exploration = loadExploration(TEST_EXPLORATION_ID_2)
    val eventLog = fakeEventLogger.getMostRecentEvent()
    assertThat(fakeEventLogger.getEventListCount()).isEqualTo(1)
    assertThat(eventLog).hasStartCardContextThat {
      hasExplorationDetailsThat().containsTestExp2Details()
      hasExplorationDetailsThat().hasStateNameThat().isEqualTo(exploration.initStateName)
      hasSkillIdThat().isEqualTo("test_skill_id_0")
    }
  }

  @Test
  fun testResumeExploration_logsResumeExplorationEventAndNotStartCardEvent() {
    logIntoAnalyticsReadyAdminProfile()
    val checkpoint = createTestExp2CheckpointToState6()
    fakeEventLogger.clearAllEvents()

    resumeExploration(TEST_TOPIC_ID_0, TEST_STORY_ID_0, TEST_EXPLORATION_ID_2, checkpoint)
    waitForGetCurrentStateSuccessfulLoad()

    // Resuming shouldn't log a 'start card' event.
    val eventLog = fakeEventLogger.getMostRecentEvent()
    assertThat(fakeEventLogger.getEventListCount()).isEqualTo(1)
    assertThat(eventLog).hasResumeExplorationContextThat {
      hasLearnerIdThat().isNotEmpty()
      hasInstallationIdThat().isNotEmpty()
    }
  }

  @Test
  fun testStartOverExploration_logsStartCardAndStartOverEvents() {
    logIntoAnalyticsReadyAdminProfile()
    createTestExp2CheckpointToState6()
    fakeEventLogger.clearAllEvents()

    restartExploration(TEST_TOPIC_ID_0, TEST_STORY_ID_0, TEST_EXPLORATION_ID_2)
    waitForGetCurrentStateSuccessfulLoad()

    val (eventLog1, eventLog2) = fakeEventLogger.getMostRecentEvents(count = 2)
    assertThat(fakeEventLogger.getEventListCount()).isEqualTo(2)
    assertThat(eventLog1).hasStartOverExplorationContextThat {
      hasLearnerIdThat().isNotEmpty()
      hasInstallationIdThat().isNotEmpty()
    }
    assertThat(eventLog2).hasStartCardContextThat {
      hasExplorationDetailsThat().containsTestExp2Details()
      // The exploration should have been started over.
      hasExplorationDetailsThat().hasStateNameThat().isEqualTo("Continue")
      hasSkillIdThat().isEqualTo("test_skill_id_0")
    }
  }

  @Test
  fun testPlayExplorationAgain_logsStartCardEvent() {
    logIntoAnalyticsReadyAdminProfile()
    createTestExp2CheckpointToState6()
    fakeEventLogger.clearAllEvents()

    replayExploration(TEST_TOPIC_ID_0, TEST_STORY_ID_0, TEST_EXPLORATION_ID_2)
    waitForGetCurrentStateSuccessfulLoad()

    val eventLog = fakeEventLogger.getMostRecentEvent()
    assertThat(fakeEventLogger.getEventListCount()).isEqualTo(1)
    assertThat(eventLog).hasStartCardContextThat {
      hasExplorationDetailsThat().containsTestExp2Details()
      // The exploration should have been started over.
      hasExplorationDetailsThat().hasStateNameThat().isEqualTo("Continue")
      hasSkillIdThat().isEqualTo("test_skill_id_0")
    }
  }

  @Test
  fun testSubmitAnswer_correctAnswer_logsEndCardAndSubmitAnswerEvents() {
    logIntoAnalyticsReadyAdminProfile()
    startPlayingNewExploration(TEST_TOPIC_ID_0, TEST_STORY_ID_0, TEST_EXPLORATION_ID_2)
    waitForGetCurrentStateSuccessfulLoad()
    playThroughPrototypeState1AndMoveToNextState()
    fakeEventLogger.clearAllEvents()

    submitPrototypeState2Answer()

    val (eventLog1, eventLog2) = fakeEventLogger.getMostRecentEvents(count = 2)
    assertThat(fakeEventLogger.getEventListCount()).isEqualTo(2)
    assertThat(eventLog1).hasSubmitAnswerContextThat {
      hasExplorationDetailsThat().containsTestExp2Details()
      hasExplorationDetailsThat().hasStateNameThat().isEqualTo("Fractions")
      hasAnswerCorrectValueThat().isTrue()
    }
    assertThat(eventLog2).hasEndCardContextThat {
      hasExplorationDetailsThat().containsTestExp2Details()
      hasExplorationDetailsThat().hasStateNameThat().isEqualTo("Fractions")
      hasSkillIdThat().isEqualTo("test_skill_id_0")
    }
  }

  @Test
  fun testSubmitAnswer_wrongAnswer_logsSubmitAnswerEventOnly() {
    logIntoAnalyticsReadyAdminProfile()
    startPlayingNewExploration(TEST_TOPIC_ID_0, TEST_STORY_ID_0, TEST_EXPLORATION_ID_2)
    waitForGetCurrentStateSuccessfulLoad()
    playThroughPrototypeState1AndMoveToNextState()
    fakeEventLogger.clearAllEvents()

    submitWrongAnswerForPrototypeState2()

    val eventLog = fakeEventLogger.getMostRecentEvent()
    assertThat(fakeEventLogger.getEventListCount()).isEqualTo(1)
    assertThat(eventLog).hasSubmitAnswerContextThat {
      hasExplorationDetailsThat().containsTestExp2Details()
      hasExplorationDetailsThat().hasStateNameThat().isEqualTo("Fractions")
      hasAnswerCorrectValueThat().isFalse()
    }
  }

  @Test
  fun testMoveToNextState_logsStartCardEvent() {
    logIntoAnalyticsReadyAdminProfile()
    startPlayingNewExploration(TEST_TOPIC_ID_0, TEST_STORY_ID_0, TEST_EXPLORATION_ID_2)
    waitForGetCurrentStateSuccessfulLoad()
    submitPrototypeState1Answer()
    fakeEventLogger.clearAllEvents()

    moveToNextState()

    val eventLog = fakeEventLogger.getMostRecentEvent()
    assertThat(fakeEventLogger.getEventListCount()).isEqualTo(1)
    assertThat(eventLog).hasStartCardContextThat {
      hasExplorationDetailsThat().containsTestExp2Details()
      hasExplorationDetailsThat().hasStateNameThat().isEqualTo("Fractions")
      hasSkillIdThat().isEqualTo("test_skill_id_0")
    }
  }

  @Test
  fun testHint_offered_logsHintOfferedEvent() {
    logIntoAnalyticsReadyAdminProfile()
    startPlayingNewExploration(TEST_TOPIC_ID_0, TEST_STORY_ID_0, TEST_EXPLORATION_ID_2)
    waitForGetCurrentStateSuccessfulLoad()
    playThroughPrototypeState1AndMoveToNextState()

    // Submit 2 wrong answers to trigger a hint becoming available.
    submitWrongAnswerForPrototypeState2()
    submitWrongAnswerForPrototypeState2()

    val eventLog = fakeEventLogger.getMostRecentEvent()
    assertThat(eventLog).hasHintOfferedContextThat {
      hasExplorationDetailsThat().containsTestExp2Details()
      hasExplorationDetailsThat().hasStateNameThat().isEqualTo("Fractions")
      hasHintIndexThat().isEqualTo(0)
    }
  }

  @Test
  fun testHint_offeredThenViewed_logsViewHintEvent() {
    logIntoAnalyticsReadyAdminProfile()
    startPlayingNewExploration(TEST_TOPIC_ID_0, TEST_STORY_ID_0, TEST_EXPLORATION_ID_2)
    waitForGetCurrentStateSuccessfulLoad()
    playThroughPrototypeState1AndMoveToNextState()
    // Submit 2 wrong answers to trigger a hint becoming available.
    submitWrongAnswerForPrototypeState2()
    submitWrongAnswerForPrototypeState2()

    monitorFactory.ensureDataProviderExecutes(
      explorationProgressController.submitHintIsRevealed(hintIndex = 0)
    )

    val eventLog = fakeEventLogger.getMostRecentEvent()
    assertThat(eventLog).hasAccessHintContextThat {
      hasExplorationDetailsThat().containsTestExp2Details()
      hasExplorationDetailsThat().hasStateNameThat().isEqualTo("Fractions")
      hasHintIndexThat().isEqualTo(0)
    }
  }

  @Test
  fun testHint_lastHintWithNoSolution_offered_logsHintOfferedEvent() {
    logIntoAnalyticsReadyAdminProfile()
    startPlayingNewExploration(FRACTIONS_TOPIC_ID, FRACTIONS_STORY_ID_0, FRACTIONS_EXPLORATION_ID_0)
    waitForGetCurrentStateSuccessfulLoad()
    submitContinueButtonAnswerAndContinue() // 'Introduction' -> 'A Problem'
    submitContinueButtonAnswerAndContinue() // 'A Problem' -> 'Mr. Baker'
    submitContinueButtonAnswerAndContinue() // 'Mr. Baker' -> 'Parts of a whole'

    // Submit the wrong answer twice to trigger a hint.
    submitMultipleChoiceAnswer(choiceIndex = 0)
    submitMultipleChoiceAnswer(choiceIndex = 0)

    val eventLog = fakeEventLogger.getMostRecentEvent()
    assertThat(eventLog).hasHintOfferedContextThat {
      hasExplorationDetailsThat().containsFractionsExp0Details()
      hasExplorationDetailsThat().hasStateNameThat().isEqualTo("Parts of a whole")
      hasHintIndexThat().isEqualTo(0)
    }
  }

  @Test
  fun testHint_lastHintWithNoSolution_offeredThenViewed_logsViewHintEvent() {
    logIntoAnalyticsReadyAdminProfile()
    startPlayingNewExploration(FRACTIONS_TOPIC_ID, FRACTIONS_STORY_ID_0, FRACTIONS_EXPLORATION_ID_0)
    waitForGetCurrentStateSuccessfulLoad()
    submitContinueButtonAnswerAndContinue() // 'Introduction' -> 'A Problem'
    submitContinueButtonAnswerAndContinue() // 'A Problem' -> 'Mr. Baker'
    submitContinueButtonAnswerAndContinue() // 'Mr. Baker' -> 'Parts of a whole'
    // Submit the wrong answer twice to trigger a hint.
    submitMultipleChoiceAnswer(choiceIndex = 0)
    submitMultipleChoiceAnswer(choiceIndex = 0)

    // View the hint.
    monitorFactory.ensureDataProviderExecutes(
      explorationProgressController.submitHintIsRevealed(hintIndex = 0)
    )

    val eventLog = fakeEventLogger.getMostRecentEvent()
    assertThat(eventLog).hasAccessHintContextThat {
      hasExplorationDetailsThat().containsFractionsExp0Details()
      hasExplorationDetailsThat().hasStateNameThat().isEqualTo("Parts of a whole")
      hasHintIndexThat().isEqualTo(0)
    }
  }

  @Test
  fun testSolution_offered_logsSolutionOfferedEvent() {
    logIntoAnalyticsReadyAdminProfile()
    startPlayingNewExploration(TEST_TOPIC_ID_0, TEST_STORY_ID_0, TEST_EXPLORATION_ID_2)
    waitForGetCurrentStateSuccessfulLoad()
    playThroughPrototypeState1AndMoveToNextState()
    // Submit 2 wrong answers to trigger the hint.
    submitWrongAnswerForPrototypeState2()
    submitWrongAnswerForPrototypeState2()
    monitorFactory.ensureDataProviderExecutes(
      explorationProgressController.submitHintIsRevealed(hintIndex = 0)
    )

    // Submit another wrong answer to trigger the solution.
    submitWrongAnswerForPrototypeState2()
    testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(10))

    val eventLog = fakeEventLogger.getMostRecentEvent()
    assertThat(eventLog).hasSolutionOfferedContextThat().containsTestExp2Details()
    assertThat(eventLog).hasSolutionOfferedContextThat().hasStateNameThat().isEqualTo("Fractions")
  }

  @Test
  fun testSolution_offeredThenViewed_logsViewSolutionEvent() {
    logIntoAnalyticsReadyAdminProfile()
    startPlayingNewExploration(TEST_TOPIC_ID_0, TEST_STORY_ID_0, TEST_EXPLORATION_ID_2)
    waitForGetCurrentStateSuccessfulLoad()
    playThroughPrototypeState1AndMoveToNextState()
    // Submit 2 wrong answers to trigger the hint.
    submitWrongAnswerForPrototypeState2()
    submitWrongAnswerForPrototypeState2()
    monitorFactory.ensureDataProviderExecutes(
      explorationProgressController.submitHintIsRevealed(hintIndex = 0)
    )
    // Submit another wrong answer to trigger the solution.
    submitWrongAnswerForPrototypeState2()
    testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(10))

    monitorFactory.ensureDataProviderExecutes(
      explorationProgressController.submitSolutionIsRevealed()
    )

    val eventLog = fakeEventLogger.getMostRecentEvent()
    assertThat(eventLog).hasAccessSolutionContextThat().containsTestExp2Details()
    assertThat(eventLog).hasAccessSolutionContextThat().hasStateNameThat().isEqualTo("Fractions")
  }

  @Test
  fun testEndExploration_withoutFinishing_logsExitExplorationEvent() {
    logIntoAnalyticsReadyAdminProfile()
    startPlayingNewExploration(TEST_TOPIC_ID_0, TEST_STORY_ID_0, TEST_EXPLORATION_ID_2)
    waitForGetCurrentStateSuccessfulLoad()

    endExploration(isCompletion = false)

    val eventLog = fakeEventLogger.getMostRecentEvent()
    assertThat(eventLog).hasExitExplorationContextThat().containsTestExp2Details()
    assertThat(eventLog).hasExitExplorationContextThat().hasStateNameThat().isEqualTo("Continue")
  }

  @Test
  fun testEndExploration_afterFinishing_logsFinishExplorationEvent() {
    logIntoAnalyticsReadyAdminProfile()
    startPlayingNewExploration(TEST_TOPIC_ID_0, TEST_STORY_ID_0, TEST_EXPLORATION_ID_2)
    waitForGetCurrentStateSuccessfulLoad()
    playThroughPrototypeExploration()

    endExploration(isCompletion = true)

    val eventLog = fakeEventLogger.getMostRecentEvent()
    assertThat(eventLog).hasFinishExplorationContextThat().containsTestExp2Details()
    assertThat(eventLog).hasFinishExplorationContextThat().hasStateNameThat().isEqualTo("End")
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  private fun startPlayingNewExploration(
    topicId: String,
    storyId: String,
    explorationId: String,
    profileId: ProfileId = this.profileId
  ) {
    val startPlayingProvider =
      explorationDataController.startPlayingNewExploration(
        profileId.internalId, topicId, storyId, explorationId
      )
    monitorFactory.waitForNextSuccessfulResult(startPlayingProvider)
  }

  private fun resumeExploration(
    topicId: String,
    storyId: String,
    explorationId: String,
    explorationCheckpoint: ExplorationCheckpoint,
    profileId: ProfileId = this.profileId
  ) {
    val startPlayingProvider =
      explorationDataController.resumeExploration(
        profileId.internalId, topicId, storyId, explorationId, explorationCheckpoint
      )
    monitorFactory.waitForNextSuccessfulResult(startPlayingProvider)
  }

  private fun restartExploration(
    topicId: String,
    storyId: String,
    explorationId: String,
    profileId: ProfileId = this.profileId
  ) {
    val startPlayingProvider =
      explorationDataController.restartExploration(
        profileId.internalId, topicId, storyId, explorationId
      )
    monitorFactory.waitForNextSuccessfulResult(startPlayingProvider)
  }

  private fun replayExploration(
    topicId: String,
    storyId: String,
    explorationId: String,
    profileId: ProfileId = this.profileId
  ) {
    val startPlayingProvider =
      explorationDataController.replayExploration(
        profileId.internalId, topicId, storyId, explorationId
      )
    monitorFactory.waitForNextSuccessfulResult(startPlayingProvider)
  }

  private fun retrieveExplorationCheckpoint(
    explorationId: String,
    profileId: ProfileId = this.profileId
  ): ExplorationCheckpoint {
    return monitorFactory.waitForNextSuccessfulResult(
      explorationCheckpointController.retrieveExplorationCheckpoint(profileId, explorationId)
    )
  }

  private fun waitForGetCurrentStateSuccessfulLoad(): EphemeralState {
    return monitorFactory.waitForNextSuccessfulResult(
      explorationProgressController.getCurrentState()
    )
  }

  private fun waitForGetCurrentStateFailureLoad(): Throwable {
    return monitorFactory.waitForNextFailureResult(
      explorationProgressController.getCurrentState()
    )
  }

  private fun submitContinueButtonAnswer(): EphemeralState {
    return submitAnswer(createContinueButtonAnswer())
  }

  private fun submitContinueButtonAnswerAndContinue(): EphemeralState {
    submitAnswer(createContinueButtonAnswer())
    return moveToNextState()
  }

  private fun submitFractionAnswer(fraction: Fraction): EphemeralState {
    return submitAnswer(createFractionAnswer(fraction))
  }

  private fun submitMultipleChoiceAnswer(choiceIndex: Int): EphemeralState {
    return submitAnswer(createMultipleChoiceAnswer(choiceIndex))
  }

  private fun submitItemSelectionAnswer(vararg contentIds: String): EphemeralState {
    return submitAnswer(createItemSelectionAnswer(contentIds.toList()))
  }

  private fun submitNumericInputAnswer(numericAnswer: Double): EphemeralState {
    return submitAnswer(createNumericInputAnswer(numericAnswer))
  }

  private fun submitRatioInputAnswer(ratioExpression: RatioExpression): EphemeralState {
    return submitAnswer(createRatioInputAnswer(ratioExpression))
  }

  private fun submitTextInputAnswer(textAnswer: String): EphemeralState {
    return submitAnswer(createTextInputAnswer(textAnswer))
  }

  private fun submitDragAndDropAnswer(vararg selectedChoicesLists: List<String>): EphemeralState {
    return submitAnswer(createDragAndDropAnswer(selectedChoicesLists.toList()))
  }

  private fun submitImageRegionAnswer(
    clickX: Float,
    clickY: Float,
    clickedRegion: String
  ): EphemeralState {
    return submitAnswer(createImageRegionAnswer(clickX, clickY, clickedRegion))
  }

  private fun submitAnswer(userAnswer: UserAnswer): EphemeralState {
    monitorFactory.waitForNextSuccessfulResult(
      explorationProgressController.submitAnswer(userAnswer)
    )
    return waitForGetCurrentStateSuccessfulLoad()
  }

  private fun playThroughPrototypeExploration(): EphemeralState {
    playThroughPrototypeState1AndMoveToNextState()
    playThroughPrototypeState2AndMoveToNextState()
    playThroughPrototypeState3AndMoveToNextState()
    playThroughPrototypeState4AndMoveToNextState()
    playThroughPrototypeState5AndMoveToNextState()
    playThroughPrototypeState6AndMoveToNextState()
    playThroughPrototypeState7AndMoveToNextState()
    playThroughPrototypeState8AndMoveToNextState()
    playThroughPrototypeState9AndMoveToNextState()
    return playThroughPrototypeState10AndMoveToNextState()
  }

  private fun navigateToPrototypeFractionInputState(): EphemeralState {
    // Fraction input is the second state of the exploration.
    return playThroughPrototypeState1AndMoveToNextState()
  }

  private fun navigateToPrototypeMultipleChoiceState(): EphemeralState {
    // Multiple choice is the third state of the exploration.
    playThroughPrototypeState1AndMoveToNextState()
    return playThroughPrototypeState2AndMoveToNextState()
  }

  private fun navigateToPrototypeNumericInputState(): EphemeralState {
    // Numeric input is the sixth state of the exploration.
    playThroughPrototypeState1AndMoveToNextState()
    playThroughPrototypeState2AndMoveToNextState()
    playThroughPrototypeState3AndMoveToNextState()
    playThroughPrototypeState4AndMoveToNextState()
    return playThroughPrototypeState5AndMoveToNextState()
  }

  private fun navigateToPrototypeTextInputState(): EphemeralState {
    // Text input is the eighth state of the exploration.
    playThroughPrototypeState1AndMoveToNextState()
    playThroughPrototypeState2AndMoveToNextState()
    playThroughPrototypeState3AndMoveToNextState()
    playThroughPrototypeState4AndMoveToNextState()
    playThroughPrototypeState5AndMoveToNextState()
    playThroughPrototypeState6AndMoveToNextState()
    return playThroughPrototypeState7AndMoveToNextState()
  }

  private fun submitPrototypeState1Answer(): EphemeralState {
    // First state: Continue interaction.
    return submitContinueButtonAnswer()
  }

  private fun submitPrototypeState2Answer(): EphemeralState {
    // Second state: Fraction input. Correct answer: 1/2.
    return submitFractionAnswer(
      Fraction.newBuilder().apply {
        numerator = 1
        denominator = 2
      }.build()
    )
  }

  private fun submitWrongAnswerForPrototypeState2(): EphemeralState {
    return submitFractionAnswer(
      Fraction.newBuilder().apply {
        numerator = 1
        denominator = 3
      }.build()
    )
  }

  private fun submitPrototypeState3Answer(): EphemeralState {
    // Third state: Multiple choice. Correct answer: Eagle (second third choice).
    return submitMultipleChoiceAnswer(choiceIndex = 2)
  }

  private fun submitPrototypeState4Answer(): EphemeralState {
    // Fourth state: Item selection (radio buttons). Correct answer: Green (first choice).
    return submitItemSelectionAnswer("ca_choices_0")
  }

  private fun submitPrototypeState5Answer(): EphemeralState {
    // Fifth state: Item selection (checkboxes). Correct answer: {Red, Green, Blue}.
    return submitItemSelectionAnswer("ca_choices_0", "ca_choices_3", "ca_choices_2")
  }

  private fun submitPrototypeState6Answer(): EphemeralState {
    // Sixth state: Numeric input. Correct answer: 121.
    return submitNumericInputAnswer(121.0)
  }

  private fun submitPrototypeState7Answer(): EphemeralState {
    // Seventh state: Ratio input. Correct answer: 4:5.
    return submitRatioInputAnswer(
      RatioExpression.newBuilder().apply {
        addAllRatioComponent(listOf(4, 5))
      }.build()
    )
  }

  private fun submitPrototypeState8Answer(): EphemeralState {
    // Eighth state: Text input. Correct answer: finnish.
    return submitTextInputAnswer("finnish")
  }

  private fun submitPrototypeState9Answer(): EphemeralState {
    // Ninth state: Drag Drop Sort. Initial configuration: ca_choices_0, ca_choices_1, ca_choices_2,
    // ca_choices_3. Correct answer: Move 1st item to 4th position.
    return submitDragAndDropAnswer(
      listOf("ca_choices_1"),
      listOf("ca_choices_2"),
      listOf("ca_choices_3"),
      listOf("ca_choices_0"),
    )
  }

  private fun submitPrototypeState10Answer(): EphemeralState {
    // Tenth state: Drag Drop Sort. Initial configuration: ca_choices_0, ca_choices_1, ca_choices_2,
    // ca_choices_3. Correct answer: Move 1st item to 4th position. Correct answer: Merge first two
    // then move 2nd item to 3rd position.
    return submitDragAndDropAnswer(
      listOf("ca_choices_0", "ca_choices_1"),
      listOf("ca_choices_3"),
      listOf("ca_choices_2"),
    )
  }

  private fun playThroughPrototypeState1AndMoveToNextState(): EphemeralState {
    submitPrototypeState1Answer()
    return moveToNextState()
  }

  private fun playThroughPrototypeState2AndMoveToNextState(): EphemeralState {
    submitPrototypeState2Answer()
    return moveToNextState()
  }

  private fun playThroughPrototypeState3AndMoveToNextState(): EphemeralState {
    submitPrototypeState3Answer()
    return moveToNextState()
  }

  private fun playThroughPrototypeState4AndMoveToNextState(): EphemeralState {
    submitPrototypeState4Answer()
    return moveToNextState()
  }

  private fun playThroughPrototypeState5AndMoveToNextState(): EphemeralState {
    submitPrototypeState5Answer()
    return moveToNextState()
  }

  private fun playThroughPrototypeState6AndMoveToNextState(): EphemeralState {
    submitPrototypeState6Answer()
    return moveToNextState()
  }

  private fun playThroughPrototypeState7AndMoveToNextState(): EphemeralState {
    submitPrototypeState7Answer()
    return moveToNextState()
  }

  private fun playThroughPrototypeState8AndMoveToNextState(): EphemeralState {
    submitPrototypeState8Answer()
    return moveToNextState()
  }

  private fun playThroughPrototypeState9AndMoveToNextState(): EphemeralState {
    submitPrototypeState9Answer()
    return moveToNextState()
  }

  private fun playThroughPrototypeState10AndMoveToNextState(): EphemeralState {
    submitPrototypeState10Answer()
    return moveToNextState()
  }

  private fun moveToNextState(): EphemeralState {
    monitorFactory.waitForNextSuccessfulResult(explorationProgressController.moveToNextState())
    return waitForGetCurrentStateSuccessfulLoad()
  }

  private fun moveToPreviousState(): EphemeralState {
    monitorFactory.waitForNextSuccessfulResult(explorationProgressController.moveToPreviousState())
    return waitForGetCurrentStateSuccessfulLoad()
  }

  private fun endExploration(isCompletion: Boolean = false) {
    monitorFactory.waitForNextSuccessfulResult(
      explorationDataController.stopPlayingExploration(isCompletion)
    )
  }

  private fun createContinueButtonAnswer() =
    createTextInputAnswer(DEFAULT_CONTINUE_INTERACTION_TEXT_ANSWER)

  private fun createFractionAnswer(fraction: Fraction): UserAnswer {
    return convertToUserAnswer(
      InteractionObject.newBuilder().apply {
        this.fraction = fraction
      }.build()
    )
  }

  private fun createMultipleChoiceAnswer(choiceIndex: Int): UserAnswer {
    return convertToUserAnswer(
      InteractionObject.newBuilder().apply {
        nonNegativeInt = choiceIndex
      }.build()
    )
  }

  private fun createItemSelectionAnswer(contentIds: List<String>): UserAnswer {
    return convertToUserAnswer(
      InteractionObject.newBuilder().apply {
        setOfTranslatableHtmlContentIds = SetOfTranslatableHtmlContentIds.newBuilder().apply {
          addAllContentIds(
            contentIds.map { choice ->
              TranslatableHtmlContentId.newBuilder().apply { contentId = choice }.build()
            }
          )
        }.build()
      }.build()
    )
  }

  private fun createNumericInputAnswer(numericAnswer: Double): UserAnswer {
    return convertToUserAnswer(
      InteractionObject.newBuilder().apply {
        real = numericAnswer
      }.build()
    )
  }

  private fun createRatioInputAnswer(ratioExpression: RatioExpression): UserAnswer {
    return convertToUserAnswer(
      InteractionObject.newBuilder().apply {
        this.ratioExpression = ratioExpression
      }.build()
    )
  }

  private fun createTextInputAnswer(textAnswer: String): UserAnswer {
    return convertToUserAnswer(
      InteractionObject.newBuilder().apply {
        normalizedString = textAnswer
      }.build()
    )
  }

  private fun createDragAndDropAnswer(selectedChoicesLists: List<List<String>>): UserAnswer {
    return convertToUserAnswer(
      InteractionObject.newBuilder().apply {
        listOfSetsOfTranslatableHtmlContentIds =
          ListOfSetsOfTranslatableHtmlContentIds.newBuilder().apply {
            addAllContentIdLists(
              selectedChoicesLists.map { choices ->
                SetOfTranslatableHtmlContentIds.newBuilder().apply {
                  addAllContentIds(
                    choices.map { choice ->
                      TranslatableHtmlContentId.newBuilder().apply { contentId = choice }.build()
                    }
                  )
                }.build()
              }
            )
          }.build()
      }.build()
    )
  }

  private fun createImageRegionAnswer(
    clickX: Float,
    clickY: Float,
    clickedRegion: String
  ): UserAnswer {
    return convertToUserAnswer(
      InteractionObject.newBuilder().apply {
        clickOnImage = ClickOnImage.newBuilder().apply {
          clickPosition = Point2d.newBuilder().apply {
            x = clickX
            y = clickY
          }.build()
          addClickedRegions(clickedRegion)
        }.build()
      }.build()
    )
  }

  private fun convertToUserAnswer(answer: InteractionObject): UserAnswer {
    return UserAnswer.newBuilder().setAnswer(answer).setPlainAnswer(answer.toAnswerString()).build()
  }

  private fun forceDefaultLocale(locale: Locale) {
    context.applicationContext.resources.configuration.setLocale(locale)
    Locale.setDefault(locale)
  }

  private fun updateContentLanguage(profileId: ProfileId, language: OppiaLanguage) {
    val updateProvider = translationController.updateWrittenTranslationContentLanguage(
      profileId,
      WrittenTranslationLanguageSelection.newBuilder().apply {
        selectedLanguage = language
      }.build()
    )
    monitorFactory.waitForNextSuccessfulResult(updateProvider)
  }

  private fun EphemeralState.isHintRevealed(hintIndex: Int): Boolean {
    return pendingState.helpIndex.isHintRevealed(hintIndex, state.interaction.hintList)
  }

  private fun EphemeralState.isSolutionRevealed(): Boolean =
    pendingState.helpIndex.isSolutionRevealed()

  private fun EventLogSubject.ExplorationContextSubject.containsTestExp2Details() {
    hasTopicIdThat().isEqualTo(TEST_TOPIC_ID_0)
    hasStoryIdThat().isEqualTo(TEST_STORY_ID_0)
    hasExplorationIdThat().isEqualTo(TEST_EXPLORATION_ID_2)
    hasVersionThat().isEqualTo(loadExploration(TEST_EXPLORATION_ID_2).version)
    hasSessionIdThat().isNotEmpty()
    hasLearnerDetailsThat {
      hasLearnerIdThat().isNotEmpty()
      hasInstallationIdThat().isNotEmpty()
    }
  }

  private fun EventLogSubject.ExplorationContextSubject.containsFractionsExp0Details() {
    hasTopicIdThat().isEqualTo(FRACTIONS_TOPIC_ID)
    hasStoryIdThat().isEqualTo(FRACTIONS_STORY_ID_0)
    hasExplorationIdThat().isEqualTo(FRACTIONS_EXPLORATION_ID_0)
    hasVersionThat().isEqualTo(loadExploration(FRACTIONS_EXPLORATION_ID_0).version)
    hasSessionIdThat().isNotEmpty()
    hasLearnerDetailsThat {
      hasLearnerIdThat().isNotEmpty()
      hasInstallationIdThat().isNotEmpty()
    }
  }

  private fun retrieveCheckpointPendingAnswerCount(explorationId: String) =
    retrieveExplorationCheckpoint(explorationId).pendingUserAnswersCount

  private fun retrieveCheckpointStateName(explorationId: String) =
    retrieveExplorationCheckpoint(explorationId).pendingStateName

  private fun retrieveCheckpointStateIndex(explorationId: String) =
    retrieveExplorationCheckpoint(explorationId).stateIndex

  private fun retrieveCheckpointHelpIndex(explorationId: String) =
    retrieveExplorationCheckpoint(explorationId).helpIndex

  private fun loadExploration(expId: String) =
    monitorFactory.waitForNextSuccessfulResult(explorationDataController.getExplorationById(expId))

  private fun logIntoAnalyticsReadyAdminProfile() {
    val rootProfileId = ProfileId.getDefaultInstance()
    val addProfileProvider = profileManagementController.addProfile(
      name = "Admin",
      pin = "",
      avatarImagePath = null,
      allowDownloadAccess = true,
      colorRgb = 0,
      isAdmin = true
    )
    monitorFactory.waitForNextSuccessfulResult(addProfileProvider)
    monitorFactory.waitForNextSuccessfulResult(
      profileManagementController.loginToProfile(rootProfileId)
    )
  }

  private fun createTestExp2CheckpointToState6(): ExplorationCheckpoint {
    startPlayingNewExploration(TEST_TOPIC_ID_0, TEST_STORY_ID_0, TEST_EXPLORATION_ID_2)
    waitForGetCurrentStateSuccessfulLoad()

    // Play through some states in the exploration.
    playThroughPrototypeState1AndMoveToNextState()
    playThroughPrototypeState2AndMoveToNextState()
    playThroughPrototypeState3AndMoveToNextState()
    playThroughPrototypeState4AndMoveToNextState()
    playThroughPrototypeState5AndMoveToNextState()
    endExploration()

    return retrieveExplorationCheckpoint(TEST_EXPLORATION_ID_2)
  }

  // TODO(#89): Move this to a common test application component.
  @Module
  class TestModule {
    @Provides
    @Singleton
    fun provideContext(application: Application): Context {
      return application
    }

    @EnableConsoleLog
    @Provides
    fun provideEnableConsoleLog(): Boolean = true

    @EnableFileLog
    @Provides
    fun provideEnableFileLog(): Boolean = false

    @GlobalLogLevel
    @Provides
    fun provideGlobalLogLevel(): LogLevel = LogLevel.VERBOSE

    @CacheAssetsLocally
    @Provides
    fun provideCacheAssetsLocally(): Boolean = false

    @Provides
    @TopicListToCache
    fun provideTopicListToCache(): List<String> = listOf()

    @Provides
    @LoadLessonProtosFromAssets
    fun provideLoadLessonProtosFromAssets(testEnvironmentConfig: TestEnvironmentConfig): Boolean =
      testEnvironmentConfig.isUsingBazel()

    @Provides
    @LearnerStudyAnalytics
    fun provideLearnerStudyAnalytics(): PlatformParameterValue<Boolean> {
      // Enable the study by default in tests.
      return PlatformParameterValue.createDefaultParameter(defaultValue = true)
    }
  }

  @Module
  class TestExplorationStorageModule {

    /**
     * Provides the size allocated to exploration checkpoint database.
     *
     * For testing, the current [ExplorationStorageDatabaseSize] is set to be 150 Bytes.
     *
     * The size of checkpoint for the the first state in [TEST_EXPLORATION_ID_2] is equal to
     * 150 Bytes, therefore the database will exceeded the allocated limit when the second
     * checkpoint is stored for [TEST_EXPLORATION_ID_2]
     */
    @Provides
    @ExplorationStorageDatabaseSize
    fun provideExplorationStorageDatabaseSize(): Int = 150
  }

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(
    modules = [
      TestModule::class, ContinueModule::class, FractionInputModule::class,
      ItemSelectionInputModule::class, MultipleChoiceInputModule::class,
      NumberWithUnitsRuleModule::class, NumericInputRuleModule::class, TextInputRuleModule::class,
      DragDropSortInputModule::class, InteractionsModule::class, TestLogReportingModule::class,
      ImageClickInputModule::class, LogStorageModule::class, TestDispatcherModule::class,
      RatioInputModule::class, RobolectricModule::class, FakeOppiaClockModule::class,
      TestExplorationStorageModule::class, HintsAndSolutionConfigModule::class,
      HintsAndSolutionProdModule::class, NetworkConnectionUtilDebugModule::class,
      AssetModule::class, LocaleProdModule::class, NumericExpressionInputModule::class,
      AlgebraicExpressionInputModule::class, MathEquationInputModule::class,
      LoggingIdentifierModule::class, ApplicationLifecycleModule::class,
      SyncStatusModule::class, PlatformParameterSingletonModule::class
    ]
  )
  interface TestApplicationComponent : DataProvidersInjector {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun inject(explorationProgressControllerTest: ExplorationProgressControllerTest)
  }

  class TestApplication : Application(), DataProvidersInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerExplorationProgressControllerTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(explorationProgressControllerTest: ExplorationProgressControllerTest) {
      component.inject(explorationProgressControllerTest)
    }

    override fun getDataProvidersInjector(): DataProvidersInjector = component
  }

  private companion object {
    private val EGYPT_ARABIC_LOCALE = Locale("ar", "EG")
    private val TURKEY_TURKISH_LOCALE = Locale("tr", "TR")
  }
}
