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
import org.oppia.android.testing.FakeExceptionLogger
import org.oppia.android.testing.OppiaTestRule
import org.oppia.android.testing.RunOn
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.assertThrows
import org.oppia.android.testing.data.AsyncResultSubject.Companion.assertThat
import org.oppia.android.testing.data.DataProviderTestMonitor
import org.oppia.android.testing.environment.TestEnvironmentConfig
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
import org.oppia.android.util.networking.NetworkConnectionUtilDebugModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import org.oppia.android.domain.oppialogger.LoggingIdentifierModule
import org.oppia.android.domain.oppialogger.analytics.ApplicationLifecycleModule
import org.oppia.android.domain.platformparameter.PlatformParameterModule
import org.oppia.android.domain.platformparameter.PlatformParameterSingletonModule
import org.oppia.android.util.logging.SyncStatusModule
import org.oppia.android.util.system.UserIdProdModule

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
      explorationDataController.startPlayingExploration(
        profileId.internalId,
        INVALID_TOPIC_ID,
        INVALID_STORY_ID,
        INVALID_EXPLORATION_ID,
        shouldSavePartialProgress = false,
        explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
      )

    // An invalid exploration is not known until it's fully loaded, and that's observed via
    // getCurrentState.
    monitorFactory.waitForNextSuccessfulResult(resultDataProvider)
  }

  @Test
  fun testGetCurrentState_playInvalidExploration_returnsFailure() {
    playExploration(
      profileId.internalId,
      INVALID_TOPIC_ID,
      INVALID_STORY_ID,
      INVALID_EXPLORATION_ID,
      shouldSavePartialProgress = false,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )

    val error = waitForGetCurrentStateFailureLoad()

    assertThat(error).hasMessageThat().contains("invalid_exp_id")
  }

  @Test
  fun testPlayExploration_valid_returnsSuccess() {
    val resultDataProvider =
      explorationDataController.startPlayingExploration(
        profileId.internalId,
        TEST_TOPIC_ID_0,
        TEST_STORY_ID_0,
        TEST_EXPLORATION_ID_2,
        shouldSavePartialProgress = false,
        explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
      )

    monitorFactory.waitForNextSuccessfulResult(resultDataProvider)
  }

  @Test
  fun testGetCurrentState_playExploration_loaded_returnsInitialStatePending() {
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = false,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )

    val ephemeralState = waitForGetCurrentStateSuccessfulLoad()

    assertThat(ephemeralState.stateTypeCase).isEqualTo(PENDING_STATE)
    assertThat(ephemeralState.hasPreviousState).isFalse()
    assertThat(ephemeralState.state.name).isEqualTo("Continue")
  }

  @Test
  fun testGetCurrentState_playInvalidExploration_thenPlayValidExp_returnsInitialPendingState() {
    // Start with playing an invalid exploration.
    playExploration(
      profileId.internalId,
      INVALID_TOPIC_ID,
      INVALID_STORY_ID,
      INVALID_EXPLORATION_ID,
      shouldSavePartialProgress = false,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )
    endExploration()

    // Then a valid one.
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = false,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )

    // The latest result should correspond to the valid ID, and the progress controller should
    // gracefully recover.
    val ephemeralState = waitForGetCurrentStateSuccessfulLoad()
    assertThat(ephemeralState.stateTypeCase).isEqualTo(PENDING_STATE)
    assertThat(ephemeralState.hasPreviousState).isFalse()
    assertThat(ephemeralState.state.name).isEqualTo("Continue")
  }

  @Test
  fun testFinishExploration_beforePlaying_isPending() {
    val resultDataProvider = explorationDataController.stopPlayingExploration()
    val monitor = monitorFactory.createMonitor(resultDataProvider)

    // The operation should be pending since the session hasn't started.
    val result = monitor.waitForNextResult()
    assertThat(result).isPending()
  }

  @Test
  fun testPlayExploration_withoutFinishingPrevious_succeeds() {
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = false,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )
    waitForGetCurrentStateSuccessfulLoad()

    // Try playing another exploration without finishing the previous one.
    val resultDataProvider =
      explorationDataController.startPlayingExploration(
        profileId.internalId,
        TEST_TOPIC_ID_0,
        TEST_STORY_ID_0,
        TEST_EXPLORATION_ID_2,
        shouldSavePartialProgress = false,
        explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
      )

    // The new session will overwrite the previous.
    monitorFactory.waitForNextSuccessfulResult(resultDataProvider)
  }

  @Test
  fun testGetCurrentState_playSecondExploration_afterFinishingPrev_loaded_returnsInitialState() {
    // Start with playing a valid exploration, then stop.
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = false,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )
    waitForGetCurrentStateSuccessfulLoad()
    endExploration()

    // Then another valid one.
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_1,
      TEST_STORY_ID_2,
      TEST_EXPLORATION_ID_4,
      shouldSavePartialProgress = false,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )

    // The latest result should correspond to the valid ID, and the progress controller should
    // gracefully recover.
    val ephemeralState = waitForGetCurrentStateSuccessfulLoad()
    assertThat(ephemeralState.stateTypeCase).isEqualTo(PENDING_STATE)
    assertThat(ephemeralState.hasPreviousState).isFalse()
    assertThat(ephemeralState.state.name).isEqualTo("DragDropSortInput")
  }

  @Test
  fun testSubmitAnswer_beforePlaying_isPending() {
    val resultProvider = explorationProgressController.submitAnswer(createMultipleChoiceAnswer(0))
    val monitor = monitorFactory.createMonitor(resultProvider)

    // The operation should be pending since the session hasn't started.
    val result = monitor.waitForNextResult()
    assertThat(result).isPending()
  }

  @Test
  fun testSubmitAnswer_forMultipleChoice_correctAnswer_succeeds() {
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = false,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )
    waitForGetCurrentStateSuccessfulLoad()
    navigateToPrototypeMultipleChoiceState()

    val result = explorationProgressController.submitAnswer(createMultipleChoiceAnswer(2))

    // Verify that the answer submission was successful.
    monitorFactory.waitForNextSuccessfulResult(result)
  }

  @Test
  fun testSubmitAnswer_forMultipleChoice_correctAnswer_returnsOutcomeWithTransition() {
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = false,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )
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
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = false,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )
    waitForGetCurrentStateSuccessfulLoad()
    navigateToPrototypeMultipleChoiceState()

    val result = explorationProgressController.submitAnswer(createMultipleChoiceAnswer(0))

    // Verify that the answer submission was successful.
    monitorFactory.waitForNextSuccessfulResult(result)
  }

  @Test
  fun testSubmitAnswer_forMultipleChoice_wrongAnswer_providesDefFeedbackAndSameStateTransition() {
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = false,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )
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
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = false,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )
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
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = false,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )
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
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = false,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )
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
  fun testMoveToNext_beforePlaying_isPending() {
    val moveToStateResult = explorationProgressController.moveToNextState()
    val monitor = monitorFactory.createMonitor(moveToStateResult)

    // The operation should be pending since the session hasn't started.
    val result = monitor.waitForNextResult()
    assertThat(result).isPending()
  }

  @Test
  fun testMoveToNext_forPendingInitialState_failsWithError() {
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = false,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )
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
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = false,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )
    waitForGetCurrentStateSuccessfulLoad()
    submitPrototypeState1Answer()

    val moveToStateResult = explorationProgressController.moveToNextState()

    monitorFactory.waitForNextSuccessfulResult(moveToStateResult)
  }

  @Test
  fun testMoveToNext_forCompletedState_movesToNextState() {
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = false,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )
    waitForGetCurrentStateSuccessfulLoad()
    submitPrototypeState1Answer()

    val ephemeralState = moveToNextState()

    assertThat(ephemeralState.state.name).isEqualTo("Fractions")
    assertThat(ephemeralState.stateTypeCase).isEqualTo(PENDING_STATE)
  }

  @Test
  fun testMoveToNext_afterMovingFromCompletedState_failsWithError() {
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = false,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )
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
  fun testMoveToPrevious_beforePlaying_isPending() {
    val moveToStateResult = explorationProgressController.moveToPreviousState()
    val monitor = monitorFactory.createMonitor(moveToStateResult)

    // The operation should be pending since the session hasn't started.
    val result = monitor.waitForNextResult()
    assertThat(result).isPending()
  }

  @Test
  fun testMoveToPrevious_onPendingInitialState_failsWithError() {
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = false,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )
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
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = false,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )
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
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = false,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )
    waitForGetCurrentStateSuccessfulLoad()
    playThroughPrototypeState1AndMoveToNextState()

    val moveToStateResult = explorationProgressController.moveToPreviousState()

    // Verify that we can navigate to the previous state since the current state is complete and not
    // initial.
    monitorFactory.waitForNextSuccessfulResult(moveToStateResult)
  }

  @Test
  fun testMoveToPrevious_forCompletedState_movesToPreviousState() {
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = false,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )
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
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = false,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )
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
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = false,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )
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
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = false,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )
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
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = false,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )
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
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = false,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )
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
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = false,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )
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
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = true,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )
    waitForGetCurrentStateSuccessfulLoad()
    val ephemeralState = playThroughPrototypeState1AndMoveToNextState()

    // Verify that the helpIndex.IndexTypeCase is equal to INDEX_TYPE_NOT_SET because no hint
    // is visible yet.
    assertThat(ephemeralState.pendingState.helpIndex.indexTypeCase)
      .isEqualTo(HelpIndex.IndexTypeCase.INDEXTYPE_NOT_SET)
  }

  @Test
  fun testHintsAndSolution_wait60Seconds_unrevealedHintIsVisible_checkHelpIndexIsCorrect() {
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = true,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )
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
      .isEqualTo(HelpIndex.IndexTypeCase.NEXT_AVAILABLE_HINT_INDEX)
    assertThat(ephemeralState.pendingState.helpIndex.nextAvailableHintIndex).isEqualTo(0)
  }

  @Test
  fun testHintsAndSolution_submitTwoWrongAnswers_unrevealedHintIsVisible_checkHelpIndexIsCorrect() {
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = true,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )
    waitForGetCurrentStateSuccessfulLoad()
    playThroughPrototypeState1AndMoveToNextState()
    // Make the first hint visible by submitting two wrong answers.
    submitWrongAnswerForPrototypeState2()
    val ephemeralState = submitWrongAnswerForPrototypeState2()

    // Verify that the helpIndex.IndexTypeCase is equal AVAILABLE_NEXT_HINT_HINT_INDEX because a new
    // unrevealed hint is visible.
    assertThat(ephemeralState.isHintRevealed(0)).isFalse()
    assertThat(ephemeralState.pendingState.helpIndex.indexTypeCase)
      .isEqualTo(HelpIndex.IndexTypeCase.NEXT_AVAILABLE_HINT_INDEX)
    assertThat(ephemeralState.pendingState.helpIndex.nextAvailableHintIndex).isEqualTo(0)
  }

  @Test
  fun testHintsAndSolution_revealedHintIsVisible_checkHelpIndexIsCorrect() {
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = true,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )
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
      .isEqualTo(HelpIndex.IndexTypeCase.LATEST_REVEALED_HINT_INDEX)
    assertThat(ephemeralState.pendingState.helpIndex.latestRevealedHintIndex).isEqualTo(0)
  }

  @Test
  fun testHintsAndSolution_allHintsVisible_wait30Seconds_solutionVisible_checkHelpIndexIsCorrect() {
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = true,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )
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
      .isEqualTo(HelpIndex.IndexTypeCase.SHOW_SOLUTION)
  }

  @Test
  fun testHintAndSol_hintsVisible_submitWrongAns_wait10Second_solVisible_checkHelpIndexIsCorrect() {
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = true,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )
    waitForGetCurrentStateSuccessfulLoad()
    playThroughPrototypeState1AndMoveToNextState()
    submitWrongAnswerForPrototypeState2()
    submitWrongAnswerForPrototypeState2()

    explorationProgressController.submitHintIsRevealed(hintIndex = 0)
    testCoroutineDispatchers.runCurrent()

    submitWrongAnswerForPrototypeState2()
    // The solution should be visible after 10 seconds becuase one wrong answer was submitted.
    testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(10))
    testCoroutineDispatchers.runCurrent()

    // Verify that the helpIndex.IndexTypeCase is equal SHOW_SOLUTION because unrevealed solution is
    // visible.
    val ephemeralState = waitForGetCurrentStateSuccessfulLoad()
    assertThat(ephemeralState.isHintRevealed(0)).isTrue()
    assertThat(ephemeralState.isSolutionRevealed()).isFalse()
    assertThat(ephemeralState.pendingState.helpIndex.indexTypeCase)
      .isEqualTo(HelpIndex.IndexTypeCase.SHOW_SOLUTION)
  }

  @Test
  fun testHintsAndSolution_revealedSolutionIsVisible_checkHelpIndexIsCorrect() {
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = true,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )
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
      .isEqualTo(HelpIndex.IndexTypeCase.EVERYTHING_REVEALED)
  }

  @Test
  fun testSubmitAnswer_forTextInput_wrongAnswer_afterAllHintsAreExhausted_showSolution() {
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = false,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )
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
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = false,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )
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
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = false,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )
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
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = false,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )
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
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = false,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )
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
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = false,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )
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
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = false,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )
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
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = false,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )
    val ephemeralState = waitForGetCurrentStateSuccessfulLoad()

    // The initial state should not have a next state.
    assertThat(ephemeralState.hasNextState).isFalse()
  }

  @Test
  fun testGetCurrentState_forFirstState_afterAnswerSubmission_doesNotHaveNextState() {
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = false,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )
    waitForGetCurrentStateSuccessfulLoad()

    val ephemeralState = submitPrototypeState1Answer()

    // Simply completing the current state should not result in there being a next state since the
    // user hasn't proceeded to the following state, yet.
    assertThat(ephemeralState.hasNextState).isFalse()
  }

  @Test
  fun testGetCurrentState_forSecondState_doesNotHaveNextState() {
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = false,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )
    waitForGetCurrentStateSuccessfulLoad()

    val ephemeralState = playThroughPrototypeState1AndMoveToNextState()

    // The current state should have a previous state.
    assertThat(ephemeralState.hasNextState).isFalse()
  }

  @Test
  fun testGetCurrentState_forSecondState_navigateBackward_hasNextState() {
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = false,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )
    waitForGetCurrentStateSuccessfulLoad()
    playThroughPrototypeState1AndMoveToNextState()

    val ephemeralState = moveToPreviousState()

    // The previous state should have a next state.
    assertThat(ephemeralState.hasNextState).isTrue()
  }

  @Test
  fun testGetCurrentState_forSecondState_navigateBackwardThenForward_doesNotHaveNextState() {
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = false,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )
    waitForGetCurrentStateSuccessfulLoad()
    playThroughPrototypeState1AndMoveToNextState()

    moveToPreviousState()
    val ephemeralState = moveToNextState()

    // Iterating back to the current state should result in no longer having a next state.
    assertThat(ephemeralState.hasNextState).isFalse()
  }

  @Test
  fun testSubmitAnswer_forNumericInput_correctAnswer_returnsOutcomeWithTransition() {
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = false,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )
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
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = false,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )
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
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = false,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )
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
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = false,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )
    waitForGetCurrentStateSuccessfulLoad()

    val ephemeralState = playThroughPrototypeExploration()

    // Verify that the last state is terminal.
    assertThat(ephemeralState.stateTypeCase).isEqualTo(TERMINAL_STATE)
  }

  @Test
  fun testGetCurrentState_afterMoveToPrevious_onThirdState_updatesToCompletedSecondState() {
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = false,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )
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
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = false,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )
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
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_13,
      shouldSavePartialProgress = false,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )
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
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_13,
      shouldSavePartialProgress = false,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )
    waitForGetCurrentStateSuccessfulLoad()

    submitImageRegionAnswer(clickX = 0.2f, clickY = 0.5f, clickedRegion = "Jupiter")
    submitImageRegionAnswer(clickX = 0.5f, clickY = 0.5f, clickedRegion = "Saturn")
    val ephemeralState = moveToNextState()

    // Verify that a different path can also result in reaching the end state.
    assertThat(ephemeralState.stateTypeCase).isEqualTo(TERMINAL_STATE)
  }

  @Test
  fun testGetCurrentState_afterPlayingThroughPreviousExplorations_returnsStateFromSecondExp() {
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = false,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )
    waitForGetCurrentStateSuccessfulLoad()
    playThroughPrototypeExploration()
    endExploration()

    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_13,
      shouldSavePartialProgress = false,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )
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
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = false,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )
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
    playExploration(
      profileId.internalId,
      INVALID_TOPIC_ID,
      INVALID_STORY_ID,
      INVALID_EXPLORATION_ID,
      shouldSavePartialProgress = false,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )

    waitForGetCurrentStateFailureLoad()

    val exception = fakeExceptionLogger.getMostRecentException()
    assertThat(exception).isInstanceOf(IllegalStateException::class.java)
    assertThat(exception).hasMessageThat().contains("Asset doesn't exist: $INVALID_EXPLORATION_ID")
  }

  @Test
  fun testCheckpointing_loadExploration_checkCheckpointIsSaved() {
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = true,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )
    waitForGetCurrentStateSuccessfulLoad()

    val result =
      explorationCheckpointController.retrieveExplorationCheckpoint(
        profileId, TEST_EXPLORATION_ID_2
      )

    monitorFactory.waitForNextSuccessfulResult(result)
  }

  @Test
  fun testCheckpointing_playThroughMultipleStates_verifyCheckpointHasCorrectPendingStateName() {
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = true,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )
    waitForGetCurrentStateSuccessfulLoad()

    verifyCheckpointHasCorrectPendingStateName(
      profileId,
      TEST_EXPLORATION_ID_2,
      pendingStateName = "Continue"
    )

    playThroughPrototypeState1AndMoveToNextState()
    verifyCheckpointHasCorrectPendingStateName(
      profileId,
      TEST_EXPLORATION_ID_2,
      pendingStateName = "Fractions",
    )

    playThroughPrototypeState2AndMoveToNextState()
    verifyCheckpointHasCorrectPendingStateName(
      profileId,
      TEST_EXPLORATION_ID_2,
      pendingStateName = "MultipleChoice",
    )

    playThroughPrototypeState3AndMoveToNextState()
    verifyCheckpointHasCorrectPendingStateName(
      profileId,
      TEST_EXPLORATION_ID_2,
      pendingStateName = "ItemSelectionMinOne",
    )
  }

  @Test
  fun testCheckpointing_advToFourthState_backToPrevState_verifyCheckpointHasCorrectPendingState() {
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = true,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )
    waitForGetCurrentStateSuccessfulLoad()

    playThroughPrototypeState1AndMoveToNextState()
    playThroughPrototypeState2AndMoveToNextState()
    playThroughPrototypeState3AndMoveToNextState()
    verifyCheckpointHasCorrectPendingStateName(
      profileId,
      TEST_EXPLORATION_ID_2,
      pendingStateName = "ItemSelectionMinOne",
    )
    moveToPreviousState()

    verifyCheckpointHasCorrectPendingStateName(
      profileId,
      TEST_EXPLORATION_ID_2,
      pendingStateName = "ItemSelectionMinOne",
    )
  }

  @Test
  fun testCheckpointing_backTwoStates_nextState_verifyCheckpointHasCorrectPendingState() {
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = true,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )
    waitForGetCurrentStateSuccessfulLoad()

    playThroughPrototypeState1AndMoveToNextState()
    playThroughPrototypeState2AndMoveToNextState()
    playThroughPrototypeState3AndMoveToNextState()
    moveToPreviousState()
    moveToPreviousState()
    moveToNextState()

    verifyCheckpointHasCorrectPendingStateName(
      profileId,
      TEST_EXPLORATION_ID_2,
      pendingStateName = "ItemSelectionMinOne",
    )
  }

  @Test
  fun testCheckpointing_advanceToThirdState_submitMultipleAns_checkCheckpointIsSavedAfterEachAns() {
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = true,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )
    waitForGetCurrentStateSuccessfulLoad()

    playThroughPrototypeState1AndMoveToNextState()
    playThroughPrototypeState2AndMoveToNextState()
    // option 2 is the correct answer to the third state.
    submitMultipleChoiceAnswer(choiceIndex = 0)
    testCoroutineDispatchers.runCurrent()

    verifyCheckpointHasCorrectCountOfAnswers(
      profileId,
      TEST_EXPLORATION_ID_2,
      countOfAnswers = 1
    )

    // option 2 is the correct answer to the third state.
    submitMultipleChoiceAnswer(choiceIndex = 1)
    testCoroutineDispatchers.runCurrent()

    verifyCheckpointHasCorrectCountOfAnswers(
      profileId,
      TEST_EXPLORATION_ID_2,
      countOfAnswers = 2
    )

    // option 2 is the correct answer to the third state.
    submitMultipleChoiceAnswer(choiceIndex = 2)
    testCoroutineDispatchers.runCurrent()

    // count should be equal to zero because on submission of the correct answer, the
    // pendingTopState changes.
    verifyCheckpointHasCorrectCountOfAnswers(
      profileId,
      TEST_EXPLORATION_ID_2,
      countOfAnswers = 0
    )
  }

  @Test
  fun testCheckpointing_advToThirdState_submitAns_prevState_checkCheckpointIsSavedAfterEachAns() {
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = true,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )
    waitForGetCurrentStateSuccessfulLoad()

    playThroughPrototypeState1AndMoveToNextState()
    playThroughPrototypeState2AndMoveToNextState()
    // option 2 is the correct answer to the third state.
    submitMultipleChoiceAnswer(choiceIndex = 1)
    testCoroutineDispatchers.runCurrent()

    verifyCheckpointHasCorrectCountOfAnswers(
      profileId,
      TEST_EXPLORATION_ID_2,
      countOfAnswers = 1
    )

    // option 2 is the correct answer to the third state.
    submitMultipleChoiceAnswer(choiceIndex = 1)
    testCoroutineDispatchers.runCurrent()

    verifyCheckpointHasCorrectCountOfAnswers(
      profileId,
      TEST_EXPLORATION_ID_2,
      countOfAnswers = 2
    )

    moveToPreviousState()

    verifyCheckpointHasCorrectCountOfAnswers(
      profileId,
      TEST_EXPLORATION_ID_2,
      countOfAnswers = 2
    )
  }

  @Test
  fun testCheckpointing_advToThirdState_moveToPrevState_checkCheckpointHasStateIndexOfThirdState() {
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = true,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )
    waitForGetCurrentStateSuccessfulLoad()

    playThroughPrototypeState1AndMoveToNextState()
    playThroughPrototypeState2AndMoveToNextState()
    moveToPreviousState()

    verifyCheckpointHasCorrectStateIndex(
      profileId,
      TEST_EXPLORATION_ID_2,
      stateIndex = 2
    )
  }

  @Test
  fun testCheckpointing_advToThirdState_prevStates_nextState_checkCheckpointHasCorrectStateIndex() {
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = true,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )
    waitForGetCurrentStateSuccessfulLoad()

    playThroughPrototypeState1AndMoveToNextState()
    playThroughPrototypeState2AndMoveToNextState()
    moveToPreviousState()
    moveToPreviousState()
    moveToNextState()

    verifyCheckpointHasCorrectStateIndex(
      profileId,
      TEST_EXPLORATION_ID_2,
      stateIndex = 2
    )
  }

  @Test
  fun testCheckpointing_hintIsVisible_checkHintIsSavedInCheckpoint() {
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = true,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )
    waitForGetCurrentStateSuccessfulLoad()

    navigateToPrototypeFractionInputState()
    // Submit 2 wrong answers to trigger the hint.
    submitWrongAnswerForPrototypeState2()
    submitWrongAnswerForPrototypeState2()

    verifyCheckpointHasCorrectHelpIndex(
      profileId,
      TEST_EXPLORATION_ID_2,
      helpIndex = HelpIndex.newBuilder().apply {
        nextAvailableHintIndex = 0
      }.build()
    )
  }

  @Test
  fun testCheckpointing_revealHint_checkHintIsSavedInCheckpoint() {
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = true,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )
    waitForGetCurrentStateSuccessfulLoad()

    navigateToPrototypeFractionInputState()
    // Submit 2 wrong answers to trigger the hint.
    submitWrongAnswerForPrototypeState2()
    submitWrongAnswerForPrototypeState2()

    monitorFactory.waitForNextSuccessfulResult(
      explorationProgressController.submitHintIsRevealed(hintIndex = 0)
    )
    verifyCheckpointHasCorrectHelpIndex(
      profileId,
      TEST_EXPLORATION_ID_2,
      helpIndex = HelpIndex.newBuilder().apply {
        latestRevealedHintIndex = 0
      }.build()
    )
  }

  @Test
  fun testCheckpointing_solutionIsVisible_checkCheckpointIsSaved() {
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = true,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )
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

    verifyCheckpointHasCorrectHelpIndex(
      profileId,
      TEST_EXPLORATION_ID_2,
      helpIndex = HelpIndex.newBuilder().apply {
        showSolution = true
      }.build()
    )
  }

  @Test
  fun testCheckpointing_revealSolution_checkCheckpointIsSaved() {
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = true,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )
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
    verifyCheckpointHasCorrectHelpIndex(
      profileId,
      TEST_EXPLORATION_ID_2,
      helpIndex = HelpIndex.newBuilder().apply {
        everythingRevealed = true
      }.build()
    )
  }

  @Test
  fun testCheckpointing_onStateWithContinueInteraction_pressContinue_correctCheckpointIsSaved() {
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = true,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )
    waitForGetCurrentStateSuccessfulLoad()

    playThroughPrototypeState1AndMoveToNextState()

    // Verify that checkpoint is saved when the exploration moves to the new pendingTopState.
    verifyCheckpointHasCorrectPendingStateName(
      profileId,
      TEST_EXPLORATION_ID_2,
      pendingStateName = "Fractions"
    )
  }

  @Test
  fun testCheckpointing_noCheckpointSaved_checkCheckpointStateIsUnsaved() {
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = false,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )
    val ephemeralState = waitForGetCurrentStateSuccessfulLoad()

    assertThat(ephemeralState.checkpointState).isEqualTo(CheckpointState.CHECKPOINT_UNSAVED)
  }

  @Test
  fun testCheckpointing_saveCheckpoint_checkCheckpointStateIsSavedDatabaseNotExceededLimit() {
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = true,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )
    val ephemeralState = waitForGetCurrentStateSuccessfulLoad()

    assertThat(ephemeralState.checkpointState)
      .isEqualTo(CheckpointState.CHECKPOINT_SAVED_DATABASE_NOT_EXCEEDED_LIMIT)
  }

  @Test
  fun testCheckpointing_saveCheckpoint_databaseFull_checkpointStateIsSavedDatabaseExceededLimit() {
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = true,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )
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
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = true,
      ExplorationCheckpoint.getDefaultInstance()
    )
    waitForGetCurrentStateSuccessfulLoad()

    playThroughPrototypeState1AndMoveToNextState()
    endExploration()

    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = true,
      retrieveExplorationCheckpoint(profileId, TEST_EXPLORATION_ID_2)
    )
    val ephemeralState = waitForGetCurrentStateSuccessfulLoad()

    // Verify that we're on the second state of the second exploration.
    assertThat(ephemeralState.stateTypeCase).isEqualTo(PENDING_STATE)
    assertThat(ephemeralState.state.name).isEqualTo("Fractions")
  }

  @Test
  fun testCheckpointing_onSecondState_navigateBack_resumeExploration_checkResumedFromSecondState() {
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = true,
      ExplorationCheckpoint.getDefaultInstance()
    )
    waitForGetCurrentStateSuccessfulLoad()

    playThroughPrototypeState1AndMoveToNextState()
    moveToPreviousState()
    endExploration()

    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = true,
      retrieveExplorationCheckpoint(profileId, TEST_EXPLORATION_ID_2)
    )
    val ephemeralState = waitForGetCurrentStateSuccessfulLoad()

    // Verify that we're on the second state of the second exploration.
    assertThat(ephemeralState.stateTypeCase).isEqualTo(PENDING_STATE)
    assertThat(ephemeralState.state.name).isEqualTo("Fractions")
  }

  @Test
  fun testCheckpointing_onSecondState_submitWrongAns_resumeExploration_checkWrongAnswersVisible() {
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = true,
      ExplorationCheckpoint.getDefaultInstance()
    )
    waitForGetCurrentStateSuccessfulLoad()

    playThroughPrototypeState1AndMoveToNextState()
    submitWrongAnswerForPrototypeState2()
    submitWrongAnswerForPrototypeState2()
    submitWrongAnswerForPrototypeState2()
    endExploration()

    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = true,
      retrieveExplorationCheckpoint(profileId, TEST_EXPLORATION_ID_2)
    )
    val ephemeralState = waitForGetCurrentStateSuccessfulLoad()

    // Verify that three wrong answers are visible to the user.
    assertThat(ephemeralState.stateTypeCase).isEqualTo(PENDING_STATE)
    assertThat(ephemeralState.pendingState.wrongAnswerCount).isEqualTo(3)
  }

  @Test
  fun testCheckpointing_onSecondState_submitRightAns_resumeExploration_expResumedFromCompState() {
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = true,
      ExplorationCheckpoint.getDefaultInstance()
    )
    waitForGetCurrentStateSuccessfulLoad()

    playThroughPrototypeState1AndMoveToNextState()
    submitWrongAnswerForPrototypeState2()
    submitWrongAnswerForPrototypeState2()
    submitPrototypeState2Answer()
    endExploration()

    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = true,
      retrieveExplorationCheckpoint(profileId, TEST_EXPLORATION_ID_2)
    )
    val ephemeralState = waitForGetCurrentStateSuccessfulLoad()

    // Verify that we're on the second state of the second exploration because the continue button
    // was not pressed after submitting the correct answer.
    assertThat(ephemeralState.stateTypeCase).isEqualTo(COMPLETED_STATE)
    assertThat(ephemeralState.state.name).isEqualTo("Fractions")
  }

  @Test
  fun testCheckpointing_submitAns_moveToNextState_resumeExploration_answersVisibleOnPrevState() {
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = true,
      ExplorationCheckpoint.getDefaultInstance()
    )
    waitForGetCurrentStateSuccessfulLoad()

    playThroughPrototypeState1AndMoveToNextState()
    submitWrongAnswerForPrototypeState2()
    submitWrongAnswerForPrototypeState2()
    playThroughPrototypeState2AndMoveToNextState()
    endExploration()

    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = true,
      retrieveExplorationCheckpoint(profileId, TEST_EXPLORATION_ID_2)
    )
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
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = true,
      ExplorationCheckpoint.getDefaultInstance()
    )
    waitForGetCurrentStateSuccessfulLoad()

    playThroughPrototypeState1AndMoveToNextState()
    endExploration()

    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = true,
      retrieveExplorationCheckpoint(profileId, TEST_EXPLORATION_ID_2)
    )
    val ephemeralState = waitForGetCurrentStateSuccessfulLoad()

    // Verify that we're on the second state of the second exploration because the continue button
    // was not pressed after submitting the correct answer.
    assertThat(ephemeralState.stateTypeCase).isEqualTo(PENDING_STATE)
    assertThat(ephemeralState.state.name).isEqualTo("Fractions")
    assertThat(ephemeralState.hasNextState).isFalse()
  }

  @Test
  fun testCheckpointing_onFirstState_resumeExploration_checkStateDoesNotHaveAPrevState() {
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = true,
      ExplorationCheckpoint.getDefaultInstance()
    )
    waitForGetCurrentStateSuccessfulLoad()
    endExploration()

    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = true,
      retrieveExplorationCheckpoint(profileId, TEST_EXPLORATION_ID_2)
    )
    val ephemeralState = waitForGetCurrentStateSuccessfulLoad()

    // Verify that we're on the second state of the second exploration because the continue button
    // was not pressed after submitting the correct answer.
    assertThat(ephemeralState.stateTypeCase).isEqualTo(PENDING_STATE)
    assertThat(ephemeralState.state.name).isEqualTo("Continue")
    assertThat(ephemeralState.hasPreviousState).isFalse()
  }

  @Test
  fun testCheckpointing_onSecondState_resumeExploration_checkFirstStateHasANextState() {
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = true,
      ExplorationCheckpoint.getDefaultInstance()
    )
    waitForGetCurrentStateSuccessfulLoad()

    playThroughPrototypeState1AndMoveToNextState()
    endExploration()

    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = true,
      retrieveExplorationCheckpoint(profileId, TEST_EXPLORATION_ID_2)
    )
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
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = true,
      ExplorationCheckpoint.getDefaultInstance()
    )
    waitForGetCurrentStateSuccessfulLoad()

    playThroughPrototypeState1AndMoveToNextState()
    endExploration()

    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = true,
      retrieveExplorationCheckpoint(profileId, TEST_EXPLORATION_ID_2)
    )
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
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = true,
      ExplorationCheckpoint.getDefaultInstance()
    )
    waitForGetCurrentStateSuccessfulLoad()

    playThroughPrototypeState1AndMoveToNextState()
    endExploration()

    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = true,
      retrieveExplorationCheckpoint(profileId, TEST_EXPLORATION_ID_2)
    )
    val ephemeralState = waitForGetCurrentStateSuccessfulLoad()

    // Verify that we're on the second state of the second exploration because the continue button
    // was not pressed after submitting the correct answer.
    assertThat(ephemeralState.stateTypeCase).isEqualTo(PENDING_STATE)
    assertThat(ephemeralState.state.name).isEqualTo("Fractions")
    assertThat(ephemeralState.hasPreviousState).isTrue()
  }

  @Test
  fun testCheckpointing_submitAns_doNotPressContinueBtn_resumeExp_pendingStateHasNoNextState() {
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = true,
      ExplorationCheckpoint.getDefaultInstance()
    )
    waitForGetCurrentStateSuccessfulLoad()

    playThroughPrototypeState1AndMoveToNextState()
    submitPrototypeState2Answer()
    endExploration()

    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = true,
      retrieveExplorationCheckpoint(profileId, TEST_EXPLORATION_ID_2)
    )
    val ephemeralState = waitForGetCurrentStateSuccessfulLoad()

    // Verify that the current state is a completed state but has no next state because we have
    // not navigated to the latest pending state yet.
    assertThat(ephemeralState.stateTypeCase).isEqualTo(COMPLETED_STATE)
    assertThat(ephemeralState.state.name).isEqualTo("Fractions")
    assertThat(ephemeralState.hasNextState).isFalse()
  }

  @Test
  fun testCheckpointing_noHintVisible_resumeExp_notHintVisibleOnPendingState() {
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = true,
      ExplorationCheckpoint.getDefaultInstance()
    )
    waitForGetCurrentStateSuccessfulLoad()

    playThroughPrototypeState1AndMoveToNextState()
    endExploration()

    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = true,
      retrieveExplorationCheckpoint(profileId, TEST_EXPLORATION_ID_2)
    )
    val ephemeralState = waitForGetCurrentStateSuccessfulLoad()

    // Verify that the helpIndex.IndexTypeCase is equal to INDEX_TYPE_NOT_SET because no hint
    // is visible yet.
    assertThat(ephemeralState.pendingState.helpIndex.indexTypeCase)
      .isEqualTo(HelpIndex.IndexTypeCase.INDEXTYPE_NOT_SET)
  }

  @Test
  fun testCheckpointing_unrevealedHintIsVisible_resumeExp_unrevealedHintIsVisibleOnPendingState() {
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = true,
      ExplorationCheckpoint.getDefaultInstance()
    )
    waitForGetCurrentStateSuccessfulLoad()

    playThroughPrototypeState1AndMoveToNextState()
    // Make the first hint visible by submitting two wrong answers.
    submitWrongAnswerForPrototypeState2()
    submitWrongAnswerForPrototypeState2()
    endExploration()

    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = true,
      retrieveExplorationCheckpoint(profileId, TEST_EXPLORATION_ID_2)
    )
    val ephemeralState = waitForGetCurrentStateSuccessfulLoad()

    // Verify that the helpIndex.IndexTypeCase is equal AVAILABLE_NEXT_HINT_HINT_INDEX because a new
    // unrevealed hint is visible
    assertThat(ephemeralState.pendingState.helpIndex.indexTypeCase)
      .isEqualTo(HelpIndex.IndexTypeCase.NEXT_AVAILABLE_HINT_INDEX)
    assertThat(ephemeralState.isHintRevealed(0)).isFalse()
    assertThat(ephemeralState.pendingState.helpIndex.indexTypeCase)
      .isEqualTo(HelpIndex.IndexTypeCase.NEXT_AVAILABLE_HINT_INDEX)
    assertThat(ephemeralState.pendingState.helpIndex.nextAvailableHintIndex).isEqualTo(0)
  }

  @Test
  fun testCheckpointing_revealedHintIsVisible_resumeExp_revealedHintIsVisibleOnPendingState() {
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = true,
      ExplorationCheckpoint.getDefaultInstance()
    )
    waitForGetCurrentStateSuccessfulLoad()

    playThroughPrototypeState1AndMoveToNextState()
    submitWrongAnswerForPrototypeState2()
    submitWrongAnswerForPrototypeState2()
    monitorFactory.waitForNextSuccessfulResult(
      explorationProgressController.submitHintIsRevealed(hintIndex = 0)
    )
    endExploration()

    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = true,
      retrieveExplorationCheckpoint(profileId, TEST_EXPLORATION_ID_2)
    )
    val ephemeralState = waitForGetCurrentStateSuccessfulLoad()

    // Verify that the helpIndex.IndexTypeCase is equal LATEST_REVEALED_HINT_INDEX because a new
    // revealed hint is visible
    assertThat(ephemeralState.pendingState.helpIndex.indexTypeCase)
      .isEqualTo(HelpIndex.IndexTypeCase.LATEST_REVEALED_HINT_INDEX)
    assertThat(ephemeralState.isHintRevealed(0)).isTrue()
  }

  @Test
  fun testCheckpointing_revealedHintIsVisible_resumeExp_wait10Seconds_solutionIsNotVisible() {
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = true,
      ExplorationCheckpoint.getDefaultInstance()
    )
    waitForGetCurrentStateSuccessfulLoad()

    playThroughPrototypeState1AndMoveToNextState()
    submitWrongAnswerForPrototypeState2()
    submitWrongAnswerForPrototypeState2()
    monitorFactory.waitForNextSuccessfulResult(
      explorationProgressController.submitHintIsRevealed(hintIndex = 0)
    )
    endExploration()

    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = true,
      retrieveExplorationCheckpoint(profileId, TEST_EXPLORATION_ID_2)
    )
    waitForGetCurrentStateSuccessfulLoad()
    testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(10))

    // Verify that the helpIndex.IndexTypeCase is equal LATEST_REVEALED_HINT_INDEX because a new
    // revealed hint is visible
    val ephemeralState = waitForGetCurrentStateSuccessfulLoad()
    assertThat(ephemeralState.pendingState.helpIndex.indexTypeCase)
      .isEqualTo(HelpIndex.IndexTypeCase.LATEST_REVEALED_HINT_INDEX)
    assertThat(ephemeralState.isHintRevealed(0)).isTrue()
    assertThat(ephemeralState.isSolutionRevealed()).isFalse()
  }

  @Test
  fun testCheckpointing_revealedHintIsVisible_resumeExp_wait30Seconds_solutionIsNotVisible() {
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = true,
      ExplorationCheckpoint.getDefaultInstance()
    )
    waitForGetCurrentStateSuccessfulLoad()

    playThroughPrototypeState1AndMoveToNextState()
    submitWrongAnswerForPrototypeState2()
    submitWrongAnswerForPrototypeState2()
    monitorFactory.waitForNextSuccessfulResult(
      explorationProgressController.submitHintIsRevealed(hintIndex = 0)
    )
    endExploration()

    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = true,
      retrieveExplorationCheckpoint(profileId, TEST_EXPLORATION_ID_2)
    )
    waitForGetCurrentStateSuccessfulLoad()
    testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(30))

    // Verify that the helpIndex.IndexTypeCase is equal LATEST_REVEALED_HINT_INDEX because a new
    // revealed hint is visible
    val ephemeralState = waitForGetCurrentStateSuccessfulLoad()
    assertThat(ephemeralState.pendingState.helpIndex.indexTypeCase)
      .isEqualTo(HelpIndex.IndexTypeCase.SHOW_SOLUTION)
    assertThat(ephemeralState.isSolutionRevealed()).isFalse()
  }

  @Test
  fun testCheckpointing_solutionIsVisible_resumeExp_unrevealedSolutionIsVisibleOnPendingState() {
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = true,
      ExplorationCheckpoint.getDefaultInstance()
    )
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

    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = true,
      explorationCheckpoint = retrieveExplorationCheckpoint(profileId, TEST_EXPLORATION_ID_2)
    )
    val ephemeralState = waitForGetCurrentStateSuccessfulLoad()

    // Verify that the helpIndex.IndexTypeCase is equal EVERYTHING_IS_REVEALED because all available
    // help has been revealed.
    assertThat(ephemeralState.pendingState.helpIndex.indexTypeCase)
      .isEqualTo(HelpIndex.IndexTypeCase.SHOW_SOLUTION)
    assertThat(ephemeralState.isHintRevealed(0)).isTrue()
    assertThat(ephemeralState.isSolutionRevealed()).isFalse()
  }

  @Test
  fun testCheckpointing_revealedSolution_resumeExp_revealedSolIsVisibleOnPendingState() {
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = true,
      ExplorationCheckpoint.getDefaultInstance()
    )
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

    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = true,
      retrieveExplorationCheckpoint(profileId, TEST_EXPLORATION_ID_2)
    )
    val ephemeralState = waitForGetCurrentStateSuccessfulLoad()

    // Verify that the helpIndex.IndexTypeCase is equal EVERYTHING_IS_REVEALED because all available
    // help has been revealed.
    assertThat(ephemeralState.pendingState.helpIndex.indexTypeCase)
      .isEqualTo(HelpIndex.IndexTypeCase.EVERYTHING_REVEALED)
    assertThat(ephemeralState.isHintRevealed(0)).isTrue()
    assertThat(ephemeralState.isSolutionRevealed()).isTrue()
  }

  @Test
  fun testCheckpointing_playSomeStates_resumeExp_playRemainingState_verifyTerminalStateReached() {
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = true,
      ExplorationCheckpoint.getDefaultInstance()
    )
    waitForGetCurrentStateSuccessfulLoad()

    // Play through some states in the exploration.
    playThroughPrototypeState1AndMoveToNextState()
    playThroughPrototypeState2AndMoveToNextState()
    playThroughPrototypeState3AndMoveToNextState()
    playThroughPrototypeState4AndMoveToNextState()
    playThroughPrototypeState5AndMoveToNextState()
    endExploration()

    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = true,
      retrieveExplorationCheckpoint(profileId, TEST_EXPLORATION_ID_2)
    )
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
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = true,
      ExplorationCheckpoint.getDefaultInstance()
    )

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
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = true,
      ExplorationCheckpoint.getDefaultInstance()
    )

    val ephemeralState = waitForGetCurrentStateSuccessfulLoad()

    // Arabic translations should be included per the locale.
    assertThat(ephemeralState.writtenTranslationContext.language).isEqualTo(OppiaLanguage.ARABIC)
    assertThat(ephemeralState.writtenTranslationContext.translationsMap).isNotEmpty()
  }

  @Test
  fun testGetCurrentState_turkishLocale_defaultContentLang_includesDefaultTranslationContext() {
    forceDefaultLocale(TURKEY_TURKISH_LOCALE)
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = true,
      ExplorationCheckpoint.getDefaultInstance()
    )

    val ephemeralState = waitForGetCurrentStateSuccessfulLoad()

    // No translations match to an unsupported language, so default to the built-in strings.
    assertThat(ephemeralState.writtenTranslationContext).isEqualToDefaultInstance()
  }

  @Test
  @RunOn(buildEnvironments = [BuildEnvironment.BAZEL]) // Languages unsupported in Gradle builds.
  fun testGetCurrentState_englishLangProfile_includesTranslationContextForEnglish() {
    val englishProfileId = ProfileId.newBuilder().apply { internalId = 1 }.build()
    updateContentLanguage(englishProfileId, OppiaLanguage.ENGLISH)
    playExploration(
      englishProfileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = true,
      ExplorationCheckpoint.getDefaultInstance()
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
    playExploration(
      englishProfileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = true,
      ExplorationCheckpoint.getDefaultInstance()
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
    playExploration(
      arabicProfileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = true,
      ExplorationCheckpoint.getDefaultInstance()
    )

    val ephemeralState = waitForGetCurrentStateSuccessfulLoad()

    // Selecting the profile with Arabic translations should provide a translation context.
    assertThat(ephemeralState.writtenTranslationContext.language).isEqualTo(OppiaLanguage.ARABIC)
    assertThat(ephemeralState.writtenTranslationContext.translationsMap).isNotEmpty()
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  private fun retrieveExplorationCheckpoint(
    profileId: ProfileId,
    explorationId: String
  ): ExplorationCheckpoint {
    val explorationCheckpointDataProvider =
      explorationCheckpointController.retrieveExplorationCheckpoint(profileId, explorationId)
    return monitorFactory.waitForNextSuccessfulResult(explorationCheckpointDataProvider)
  }

  private fun playExploration(
    internalProfileId: Int,
    topicId: String,
    storyId: String,
    explorationId: String,
    shouldSavePartialProgress: Boolean,
    explorationCheckpoint: ExplorationCheckpoint
  ) {
    monitorFactory.waitForNextSuccessfulResult(
      explorationDataController.startPlayingExploration(
        internalProfileId,
        topicId,
        storyId,
        explorationId,
        shouldSavePartialProgress,
        explorationCheckpoint
      )
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

  private fun endExploration() {
    monitorFactory.waitForNextSuccessfulResult(explorationDataController.stopPlayingExploration())
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

  private fun verifyCheckpointHasCorrectPendingStateName(
    profileId: ProfileId,
    explorationId: String,
    pendingStateName: String
  ) {
    val checkpoint = retrieveExplorationCheckpoint(profileId, explorationId)
    assertThat(checkpoint.pendingStateName).isEqualTo(pendingStateName)
  }

  private fun verifyCheckpointHasCorrectCountOfAnswers(
    profileId: ProfileId,
    explorationId: String,
    countOfAnswers: Int
  ) {
    val checkpoint = retrieveExplorationCheckpoint(profileId, explorationId)
    assertThat(checkpoint.pendingUserAnswersCount).isEqualTo(countOfAnswers)
  }

  private fun verifyCheckpointHasCorrectStateIndex(
    profileId: ProfileId,
    explorationId: String,
    stateIndex: Int
  ) {
    val checkpoint = retrieveExplorationCheckpoint(profileId, explorationId)
    assertThat(checkpoint.stateIndex).isEqualTo(stateIndex)
  }

  private fun verifyCheckpointHasCorrectHelpIndex(
    profileId: ProfileId,
    explorationId: String,
    helpIndex: HelpIndex
  ) {
    val checkpoint = retrieveExplorationCheckpoint(profileId, explorationId)
    assertThat(checkpoint.helpIndex).isEqualTo(helpIndex)
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
      SyncStatusModule::class, UserIdProdModule::class, PlatformParameterModule::class,
      PlatformParameterSingletonModule::class
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
