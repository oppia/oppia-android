package org.oppia.android.domain.exploration

import android.app.Application
import android.content.Context
import androidx.lifecycle.Observer
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito.atLeast
import org.mockito.Mockito.atLeastOnce
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import org.oppia.android.app.model.AnswerOutcome
import org.oppia.android.app.model.EphemeralState
import org.oppia.android.app.model.EphemeralState.StateTypeCase.COMPLETED_STATE
import org.oppia.android.app.model.EphemeralState.StateTypeCase.PENDING_STATE
import org.oppia.android.app.model.EphemeralState.StateTypeCase.TERMINAL_STATE
import org.oppia.android.app.model.Exploration
import org.oppia.android.app.model.Hint
import org.oppia.android.app.model.InteractionObject
import org.oppia.android.app.model.Solution
import org.oppia.android.app.model.UserAnswer
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
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.domain.topic.TEST_EXPLORATION_ID_0
import org.oppia.android.domain.topic.TEST_EXPLORATION_ID_1
import org.oppia.android.domain.util.toAnswerString
import org.oppia.android.testing.FakeExceptionLogger
import org.oppia.android.testing.TestCoroutineDispatchers
import org.oppia.android.testing.TestDispatcherModule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.logging.EnableConsoleLog
import org.oppia.android.util.logging.EnableFileLog
import org.oppia.android.util.logging.GlobalLogLevel
import org.oppia.android.util.logging.LogLevel
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import java.io.FileNotFoundException
import javax.inject.Inject
import javax.inject.Singleton

// For context:
// https://github.com/oppia/oppia/blob/37285a/extensions/interactions/Continue/directives/oppia-interactive-continue.directive.ts.
private const val DEFAULT_CONTINUE_INTERACTION_TEXT_ANSWER = "Please continue."

/** Tests for [ExplorationProgressController]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(manifest = Config.NONE)
class ExplorationProgressControllerTest {
  // TODO(#114): Add much more thorough tests for the integration pathway.

  // TODO(#59): Once AsyncDataSubscriptionManager can be replaced with a fake, add the following tests once careful
  //  testing timing can be controlled:
  //  - testMoveToNext_whileSubmittingAnswer_failsWithError
  //  - testGetCurrentState_whileSubmittingCorrectMultiChoiceAnswer_updatesToPending
  //  - testSubmitAnswer_whileSubmittingAnotherAnswer_failsWithError
  //  - testMoveToPrevious_whileSubmittingAnswer_failsWithError

  @Rule
  @JvmField
  val mockitoRule: MockitoRule = MockitoJUnit.rule()

  @Inject
  lateinit var explorationDataController: ExplorationDataController

  @Inject
  lateinit var explorationProgressController: ExplorationProgressController

  @Inject
  lateinit var explorationRetriever: ExplorationRetriever

  @Inject
  lateinit var fakeExceptionLogger: FakeExceptionLogger

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @Mock
  lateinit var mockCurrentStateLiveDataObserver: Observer<AsyncResult<EphemeralState>>

  @Mock
  lateinit var mockCurrentStateLiveDataObserver2: Observer<AsyncResult<EphemeralState>>

  @Mock
  lateinit var mockAsyncResultLiveDataObserver: Observer<AsyncResult<Any?>>

  @Mock
  lateinit var mockAsyncAnswerOutcomeObserver: Observer<AsyncResult<AnswerOutcome>>

  @Mock
  lateinit var mockAsyncHintObserver: Observer<AsyncResult<Hint>>

  @Mock
  lateinit var mockAsyncSolutionObserver: Observer<AsyncResult<Solution>>

  @Captor
  lateinit var currentStateResultCaptor: ArgumentCaptor<AsyncResult<EphemeralState>>

  @Captor
  lateinit var asyncResultCaptor: ArgumentCaptor<AsyncResult<Any?>>

  @Captor
  lateinit var asyncAnswerOutcomeCaptor: ArgumentCaptor<AsyncResult<AnswerOutcome>>

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testGetCurrentState_noExploration_isPending() {
    val currentStateLiveData =
      explorationProgressController.getCurrentState()

    currentStateLiveData.observeForever(mockCurrentStateLiveDataObserver)
    testCoroutineDispatchers.runCurrent()

    verify(
      mockCurrentStateLiveDataObserver,
      atLeastOnce()
    ).onChanged(currentStateResultCaptor.capture())
    assertThat(currentStateResultCaptor.value.isPending()).isTrue()
  }

  @Test
  fun testPlayExploration_invalid_returnsSuccess() {
    val resultLiveData =
      explorationDataController.startPlayingExploration("invalid_exp_id")
    resultLiveData.observeForever(mockAsyncResultLiveDataObserver)
    testCoroutineDispatchers.runCurrent()

    // An invalid exploration is not known until it's fully loaded, and that's observed via getCurrentState.
    verify(mockAsyncResultLiveDataObserver).onChanged(asyncResultCaptor.capture())
    assertThat(asyncResultCaptor.value.isSuccess()).isTrue()
  }

  @Test
  fun testGetCurrentState_playInvalidExploration_returnsFailure() {
    val currentStateLiveData =
      explorationProgressController.getCurrentState()
    currentStateLiveData.observeForever(mockCurrentStateLiveDataObserver)

    playExploration("invalid_exp_id")

    verify(
      mockCurrentStateLiveDataObserver,
      atLeastOnce()
    ).onChanged(currentStateResultCaptor.capture())
    assertThat(currentStateResultCaptor.value.isFailure()).isTrue()
    assertThat(currentStateResultCaptor.value.getErrorOrNull())
      .hasMessageThat()
      .contains("invalid_exp_id.json")
  }

  @Test
  fun testPlayExploration_valid_returnsSuccess() {
    val resultLiveData =
      explorationDataController.startPlayingExploration(TEST_EXPLORATION_ID_0)
    resultLiveData.observeForever(mockAsyncResultLiveDataObserver)
    testCoroutineDispatchers.runCurrent()

    verify(mockAsyncResultLiveDataObserver).onChanged(asyncResultCaptor.capture())
    assertThat(asyncResultCaptor.value.isSuccess()).isTrue()
  }

  @Test
  fun testGetCurrentState_playExploration_returnsPendingResultFromLoadingExploration() {
    val currentStateLiveData =
      explorationProgressController.getCurrentState()
    currentStateLiveData.observeForever(mockCurrentStateLiveDataObserver)
    testCoroutineDispatchers.runCurrent()

    playExploration(TEST_EXPLORATION_ID_0)

    // The second-to-latest result stays pending since the exploration was loading (the actual result is the fully
    // loaded exploration). This is only true if the observer begins before starting to load the exploration.
    verify(
      mockCurrentStateLiveDataObserver,
      atLeast(2)
    ).onChanged(currentStateResultCaptor.capture())
    val results = currentStateResultCaptor.allValues
    assertThat(results[results.size - 2].isPending()).isTrue()
  }

  @Test
  fun testGetCurrentState_playExploration_loaded_returnsInitialStatePending() {
    val exploration = getTestExploration5()
    playExploration(TEST_EXPLORATION_ID_0)

    val currentStateLiveData =
      explorationProgressController.getCurrentState()
    currentStateLiveData.observeForever(mockCurrentStateLiveDataObserver)
    testCoroutineDispatchers.runCurrent()

    verify(
      mockCurrentStateLiveDataObserver,
      atLeastOnce()
    ).onChanged(currentStateResultCaptor.capture())
    assertThat(currentStateResultCaptor.value.isSuccess())
      .isTrue()
    assertThat(currentStateResultCaptor.value.getOrThrow().stateTypeCase)
      .isEqualTo(PENDING_STATE)
    assertThat(currentStateResultCaptor.value.getOrThrow().hasPreviousState)
      .isFalse()
    assertThat(currentStateResultCaptor.value.getOrThrow().state.name)
      .isEqualTo(exploration.initStateName)
  }

  @Test
  fun testGetCurrentState_playInvalidExploration_thenPlayValidExp_returnsInitialPendingState() {
    val exploration = getTestExploration5()
    // Start with playing an invalid exploration.
    playExploration("invalid_exp_id")
    endExploration()

    // Then a valid one.
    playExploration(TEST_EXPLORATION_ID_0)
    val currentStateLiveData =
      explorationProgressController.getCurrentState()
    currentStateLiveData.observeForever(mockCurrentStateLiveDataObserver)
    testCoroutineDispatchers.runCurrent()

    // The latest result should correspond to the valid ID, and the progress controller should gracefully recover.
    verify(
      mockCurrentStateLiveDataObserver,
      atLeastOnce()
    ).onChanged(currentStateResultCaptor.capture())
    assertThat(currentStateResultCaptor.value.isSuccess())
      .isTrue()
    assertThat(currentStateResultCaptor.value.getOrThrow().stateTypeCase)
      .isEqualTo(PENDING_STATE)
    assertThat(currentStateResultCaptor.value.getOrThrow().hasPreviousState)
      .isFalse()
    assertThat(currentStateResultCaptor.value.getOrThrow().state.name)
      .isEqualTo(exploration.initStateName)
  }

  @Test
  fun testFinishExploration_beforePlaying_failWithError() {
    val resultLiveData = explorationDataController.stopPlayingExploration()
    resultLiveData.observeForever(mockAsyncResultLiveDataObserver)
    testCoroutineDispatchers.runCurrent()

    verify(mockAsyncResultLiveDataObserver).onChanged(asyncResultCaptor.capture())
    assertThat(asyncResultCaptor.value.isFailure()).isTrue()
    assertThat(asyncResultCaptor.value.getErrorOrNull())
      .hasMessageThat()
      .contains("Cannot finish playing an exploration that hasn't yet been started")
  }

  @Test
  fun testPlayExploration_withoutFinishingPrevious_failsWithError() {
    subscribeToCurrentStateToAllowExplorationToLoad()
    playExploration(TEST_EXPLORATION_ID_0)

    // Try playing another exploration without finishing the previous one.
    val resultLiveData =
      explorationDataController.startPlayingExploration(TEST_EXPLORATION_ID_0)
    resultLiveData.observeForever(mockAsyncResultLiveDataObserver)
    testCoroutineDispatchers.runCurrent()

    verify(mockAsyncResultLiveDataObserver).onChanged(asyncResultCaptor.capture())
    assertThat(asyncResultCaptor.value.isFailure()).isTrue()
    assertThat(asyncResultCaptor.value.getErrorOrNull())
      .hasMessageThat()
      .contains("Expected to finish previous exploration before starting a new one.")
  }

  @Test
  fun testGetCurrentState_playSecondExploration_afterFinishingPrev_loaded_returnsInitialState() {
    val currentStateLiveData =
      explorationProgressController.getCurrentState()
    currentStateLiveData.observeForever(mockCurrentStateLiveDataObserver)
    // Start with playing a valid exploration, then stop.
    playExploration(TEST_EXPLORATION_ID_0)
    endExploration()

    // Then another valid one.
    playExploration(TEST_EXPLORATION_ID_1)

    // The latest result should correspond to the valid ID, and the progress controller should gracefully recover.
    verify(
      mockCurrentStateLiveDataObserver,
      atLeastOnce()
    ).onChanged(currentStateResultCaptor.capture())
    assertThat(currentStateResultCaptor.value.isSuccess())
      .isTrue()
    assertThat(currentStateResultCaptor.value.getOrThrow().stateTypeCase)
      .isEqualTo(PENDING_STATE)
    assertThat(currentStateResultCaptor.value.getOrThrow().hasPreviousState)
      .isFalse()
    assertThat(currentStateResultCaptor.value.getOrThrow().state.name)
      .isEqualTo(getTestExploration6().initStateName)
  }

  @Test
  fun testSubmitAnswer_beforePlaying_failsWithError() {
    val result =
      explorationProgressController.submitAnswer(createMultipleChoiceAnswer(0))
    result.observeForever(mockAsyncAnswerOutcomeObserver)
    testCoroutineDispatchers.runCurrent()

    // Verify that the answer submission failed.
    verify(
      mockAsyncAnswerOutcomeObserver,
      atLeastOnce()
    ).onChanged(asyncAnswerOutcomeCaptor.capture())
    assertThat(asyncAnswerOutcomeCaptor.value.isFailure()).isTrue()
    assertThat(asyncAnswerOutcomeCaptor.value.getErrorOrNull())
      .hasMessageThat()
      .contains("Cannot submit an answer if an exploration is not being played.")
  }

  @Test
  fun testSubmitAnswer_whileLoading_failsWithError() {
    // Start playing an exploration, but don't wait for it to complete.
    subscribeToCurrentStateToAllowExplorationToLoad()
    explorationDataController.startPlayingExploration(TEST_EXPLORATION_ID_0)

    val result =
      explorationProgressController.submitAnswer(createMultipleChoiceAnswer(0))
    result.observeForever(mockAsyncAnswerOutcomeObserver)
    testCoroutineDispatchers.runCurrent()

    // Verify that the answer submission failed.
    verify(
      mockAsyncAnswerOutcomeObserver,
      atLeastOnce()
    ).onChanged(asyncAnswerOutcomeCaptor.capture())
    assertThat(asyncAnswerOutcomeCaptor.value.isFailure()).isTrue()
    assertThat(asyncAnswerOutcomeCaptor.value.getErrorOrNull())
      .hasMessageThat()
      .contains("Cannot submit an answer while the exploration is being loaded.")
  }

  @Test
  fun testSubmitAnswer_forMultipleChoice_correctAnswer_succeeds() {
    subscribeToCurrentStateToAllowExplorationToLoad()
    playExploration(TEST_EXPLORATION_ID_0)

    val result =
      explorationProgressController.submitAnswer(createMultipleChoiceAnswer(0))
    result.observeForever(mockAsyncAnswerOutcomeObserver)
    testCoroutineDispatchers.runCurrent()

    // Verify that the answer submission was successful.
    verify(
      mockAsyncAnswerOutcomeObserver,
      atLeastOnce()
    ).onChanged(asyncAnswerOutcomeCaptor.capture())
    assertThat(asyncAnswerOutcomeCaptor.value.isSuccess()).isTrue()
  }

  @Test
  fun testSubmitAnswer_forMultipleChoice_correctAnswer_returnsOutcomeWithTransition() {
    subscribeToCurrentStateToAllowExplorationToLoad()
    playExploration(TEST_EXPLORATION_ID_0)

    val result =
      explorationProgressController.submitAnswer(createMultipleChoiceAnswer(0))
    result.observeForever(mockAsyncAnswerOutcomeObserver)
    testCoroutineDispatchers.runCurrent()

    // Verify that the answer submission was successful.
    verify(
      mockAsyncAnswerOutcomeObserver,
      atLeastOnce()
    ).onChanged(asyncAnswerOutcomeCaptor.capture())
    val answerOutcome = asyncAnswerOutcomeCaptor.value.getOrThrow()
    assertThat(answerOutcome.destinationCase).isEqualTo(AnswerOutcome.DestinationCase.STATE_NAME)
    assertThat(answerOutcome.feedback.html).isEqualTo("Yes!")
  }

  @Test
  fun testSubmitAnswer_forMultipleChoice_wrongAnswer_succeeds() {
    subscribeToCurrentStateToAllowExplorationToLoad()
    playExploration(TEST_EXPLORATION_ID_0)

    val result =
      explorationProgressController.submitAnswer(createMultipleChoiceAnswer(0))
    result.observeForever(mockAsyncAnswerOutcomeObserver)
    testCoroutineDispatchers.runCurrent()

    // Verify that the answer submission was successful.
    verify(
      mockAsyncAnswerOutcomeObserver,
      atLeastOnce()
    ).onChanged(asyncAnswerOutcomeCaptor.capture())
    assertThat(asyncAnswerOutcomeCaptor.value.isSuccess()).isTrue()
  }

  @Test
  fun testSubmitAnswer_forMultipleChoice_wrongAnswer_providesDefFeedbackAndNewStateTransition() {
    subscribeToCurrentStateToAllowExplorationToLoad()
    playExploration(TEST_EXPLORATION_ID_0)

    val result =
      explorationProgressController.submitAnswer(createMultipleChoiceAnswer(1))
    result.observeForever(mockAsyncAnswerOutcomeObserver)
    testCoroutineDispatchers.runCurrent()

    // Verify that the answer submission was successful.
    verify(
      mockAsyncAnswerOutcomeObserver,
      atLeastOnce()
    ).onChanged(asyncAnswerOutcomeCaptor.capture())
    val answerOutcome = asyncAnswerOutcomeCaptor.value.getOrThrow()
    assertThat(answerOutcome.destinationCase).isEqualTo(AnswerOutcome.DestinationCase.STATE_NAME)
    assertThat(answerOutcome.feedback.html).contains("Hm, it certainly looks like it")
  }

  @Test
  fun testGetCurrentState_afterSubmittingCorrectMultiChoiceAnswer_becomesCompletedState() {
    val currentStateLiveData =
      explorationProgressController.getCurrentState()
    currentStateLiveData.observeForever(mockCurrentStateLiveDataObserver)
    playExploration(TEST_EXPLORATION_ID_0)

    submitMultipleChoiceAnswer(0)

    // Verify that the current state updates. It should stay pending, and the wrong answer should be appended.
    verify(
      mockCurrentStateLiveDataObserver,
      atLeastOnce()
    ).onChanged(currentStateResultCaptor.capture())
    assertThat(currentStateResultCaptor.value.isSuccess()).isTrue()
    val currentState = currentStateResultCaptor.value.getOrThrow()
    assertThat(currentState.stateTypeCase)
      .isEqualTo(COMPLETED_STATE)
    assertThat(currentState.completedState.answerCount)
      .isEqualTo(1)
    assertThat(currentState.completedState.getAnswer(0).userAnswer.answer.nonNegativeInt)
      .isEqualTo(0)
    assertThat(currentState.completedState.getAnswer(0).feedback.html)
      .isEqualTo("Yes!")
  }

  @Test
  fun testGetCurrentState_afterSubmittingWrongMultiChoiceAnswer_updatesPendingState() {
    val currentStateLiveData =
      explorationProgressController.getCurrentState()
    currentStateLiveData.observeForever(mockCurrentStateLiveDataObserver)
    playExploration(TEST_EXPLORATION_ID_0)

    submitMultipleChoiceAnswer(2)

    // Verify that the current state updates. It should now be completed with the correct answer.
    verify(
      mockCurrentStateLiveDataObserver,
      atLeastOnce()
    ).onChanged(currentStateResultCaptor.capture())
    assertThat(currentStateResultCaptor.value.isSuccess()).isTrue()
    val currentState = currentStateResultCaptor.value.getOrThrow()
    assertThat(currentState.stateTypeCase)
      .isEqualTo(PENDING_STATE)
    assertThat(currentState.pendingState.wrongAnswerCount)
      .isEqualTo(1)
    assertThat(currentState.pendingState.getWrongAnswer(0).userAnswer.answer.nonNegativeInt)
      .isEqualTo(
        2
      )
    assertThat(currentState.pendingState.getWrongAnswer(0).feedback.html)
      .contains("Have another go?")
  }

  @Test
  fun testGetCurrentState_afterSubmittingWrongThenRightAnswer_updatesToStateWithBothAnswers() {
    subscribeToCurrentStateToAllowExplorationToLoad()
    playExploration(TEST_EXPLORATION_ID_0)
    submitMultipleChoiceAnswer(2)

    submitMultipleChoiceAnswer(0)

    // Verify that the current state updates. It should now be completed with both the wrong and correct answers.
    verify(
      mockCurrentStateLiveDataObserver,
      atLeastOnce()
    ).onChanged(currentStateResultCaptor.capture())
    assertThat(currentStateResultCaptor.value.isSuccess()).isTrue()
    val currentState = currentStateResultCaptor.value.getOrThrow()
    assertThat(currentState.stateTypeCase)
      .isEqualTo(COMPLETED_STATE)
    assertThat(currentState.completedState.answerCount)
      .isEqualTo(2)
    assertThat(currentState.completedState.getAnswer(0).userAnswer.answer.nonNegativeInt)
      .isEqualTo(2)
    assertThat(currentState.completedState.getAnswer(0).feedback.html)
      .contains("Have another go?")
    assertThat(currentState.completedState.getAnswer(1).userAnswer.answer.nonNegativeInt)
      .isEqualTo(0)
    assertThat(currentState.completedState.getAnswer(1).feedback.html)
      .isEqualTo("Yes!")
  }

  @Test
  fun testMoveToNext_beforePlaying_failsWithError() {
    val moveToStateResult = explorationProgressController.moveToNextState()
    moveToStateResult.observeForever(mockAsyncResultLiveDataObserver)

    verify(mockAsyncResultLiveDataObserver, atLeastOnce()).onChanged(asyncResultCaptor.capture())
    assertThat(asyncResultCaptor.value.isFailure()).isTrue()
    assertThat(asyncResultCaptor.value.getErrorOrNull())
      .hasMessageThat()
      .contains("Cannot navigate to a next state if an exploration is not being played.")
  }

  @Test
  fun testMoveToNext_whileLoadingExploration_failsWithError() {
    // Start playing an exploration, but don't wait for it to complete.
    subscribeToCurrentStateToAllowExplorationToLoad()
    explorationDataController.startPlayingExploration(TEST_EXPLORATION_ID_0)

    val moveToStateResult = explorationProgressController.moveToNextState()
    moveToStateResult.observeForever(mockAsyncResultLiveDataObserver)

    verify(mockAsyncResultLiveDataObserver, atLeastOnce()).onChanged(asyncResultCaptor.capture())
    assertThat(asyncResultCaptor.value.isFailure()).isTrue()
    assertThat(asyncResultCaptor.value.getErrorOrNull())
      .hasMessageThat()
      .contains("Cannot navigate to a next state if an exploration is being loaded.")
  }

  @Test
  fun testMoveToNext_forPendingInitialState_failsWithError() {
    subscribeToCurrentStateToAllowExplorationToLoad()
    playExploration(TEST_EXPLORATION_ID_0)

    val moveToStateResult = explorationProgressController.moveToNextState()
    moveToStateResult.observeForever(mockAsyncResultLiveDataObserver)
    testCoroutineDispatchers.runCurrent()

    // Verify that we can't move ahead since the current state isn't yet completed.
    verify(mockAsyncResultLiveDataObserver, atLeastOnce()).onChanged(asyncResultCaptor.capture())
    assertThat(asyncResultCaptor.value.isFailure()).isTrue()
    assertThat(asyncResultCaptor.value.getErrorOrNull())
      .hasMessageThat()
      .contains("Cannot navigate to next state; at most recent state.")
  }

  @Test
  fun testMoveToNext_forCompletedState_succeeds() {
    subscribeToCurrentStateToAllowExplorationToLoad()
    playExploration(TEST_EXPLORATION_ID_0)
    submitMultipleChoiceAnswer(0)

    val moveToStateResult = explorationProgressController.moveToNextState()
    moveToStateResult.observeForever(mockAsyncResultLiveDataObserver)
    testCoroutineDispatchers.runCurrent()

    verify(mockAsyncResultLiveDataObserver, atLeastOnce()).onChanged(asyncResultCaptor.capture())
    assertThat(asyncResultCaptor.value.isSuccess()).isTrue()
  }

  @Test
  fun testMoveToNext_forCompletedState_movesToNextState() {
    val currentStateLiveData =
      explorationProgressController.getCurrentState()
    currentStateLiveData.observeForever(mockCurrentStateLiveDataObserver)
    playExploration(TEST_EXPLORATION_ID_0)
    submitMultipleChoiceAnswer(0)

    moveToNextState()

    verify(
      mockCurrentStateLiveDataObserver,
      atLeastOnce()
    ).onChanged(currentStateResultCaptor.capture())
    assertThat(currentStateResultCaptor.value.isSuccess()).isTrue()
    val currentState = currentStateResultCaptor.value.getOrThrow()
    assertThat(currentState.state.name).isEqualTo("What language")
    assertThat(currentState.stateTypeCase).isEqualTo(PENDING_STATE)
  }

  @Test
  fun testMoveToNext_afterMovingFromCompletedState_failsWithError() {
    subscribeToCurrentStateToAllowExplorationToLoad()
    playExploration(TEST_EXPLORATION_ID_0)
    submitMultipleChoiceAnswer(0)
    moveToNextState()

    // Try skipping past the current state.
    val moveToStateResult =
      explorationProgressController.moveToNextState()
    moveToStateResult.observeForever(mockAsyncResultLiveDataObserver)
    testCoroutineDispatchers.runCurrent()

    // Verify we can't move ahead since the new state isn't yet completed.
    verify(mockAsyncResultLiveDataObserver, atLeastOnce()).onChanged(asyncResultCaptor.capture())
    assertThat(asyncResultCaptor.value.isFailure()).isTrue()
    assertThat(asyncResultCaptor.value.getErrorOrNull())
      .hasMessageThat()
      .contains("Cannot navigate to next state; at most recent state.")
  }

  @Test
  fun testMoveToPrevious_beforePlaying_failsWithError() {
    val moveToStateResult =
      explorationProgressController.moveToPreviousState()
    moveToStateResult.observeForever(mockAsyncResultLiveDataObserver)
    testCoroutineDispatchers.runCurrent()

    verify(mockAsyncResultLiveDataObserver, atLeastOnce()).onChanged(asyncResultCaptor.capture())
    assertThat(asyncResultCaptor.value.isFailure()).isTrue()
    assertThat(asyncResultCaptor.value.getErrorOrNull())
      .hasMessageThat()
      .contains("Cannot navigate to a previous state if an exploration is not being played.")
  }

  @Test
  fun testMoveToPrevious_whileLoadingExploration_failsWithError() {
    // Start playing an exploration, but don't wait for it to complete.
    subscribeToCurrentStateToAllowExplorationToLoad()
    explorationDataController.startPlayingExploration(TEST_EXPLORATION_ID_0)

    val moveToStateResult =
      explorationProgressController.moveToPreviousState()
    moveToStateResult.observeForever(mockAsyncResultLiveDataObserver)
    testCoroutineDispatchers.runCurrent()

    verify(mockAsyncResultLiveDataObserver, atLeastOnce()).onChanged(asyncResultCaptor.capture())
    assertThat(asyncResultCaptor.value.isFailure()).isTrue()
    assertThat(asyncResultCaptor.value.getErrorOrNull())
      .hasMessageThat()
      .contains("Cannot navigate to a previous state if an exploration is being loaded.")
  }

  @Test
  fun testMoveToPrevious_onPendingInitialState_failsWithError() {
    subscribeToCurrentStateToAllowExplorationToLoad()
    playExploration(TEST_EXPLORATION_ID_0)

    val moveToStateResult =
      explorationProgressController.moveToPreviousState()
    moveToStateResult.observeForever(mockAsyncResultLiveDataObserver)
    testCoroutineDispatchers.runCurrent()

    // Verify we can't move behind since the current state is the initial exploration state.
    verify(mockAsyncResultLiveDataObserver, atLeastOnce()).onChanged(asyncResultCaptor.capture())
    assertThat(asyncResultCaptor.value.isFailure()).isTrue()
    assertThat(asyncResultCaptor.value.getErrorOrNull())
      .hasMessageThat()
      .contains("Cannot navigate to previous state; at initial state.")
  }

  @Test
  fun testMoveToPrevious_onCompletedInitialState_failsWithError() {
    subscribeToCurrentStateToAllowExplorationToLoad()
    playExploration(TEST_EXPLORATION_ID_0)
    submitMultipleChoiceAnswer(0)

    val moveToStateResult =
      explorationProgressController.moveToPreviousState()
    moveToStateResult.observeForever(mockAsyncResultLiveDataObserver)
    testCoroutineDispatchers.runCurrent()

    // Still can't navigate behind for a completed initial state since there's no previous state.
    verify(mockAsyncResultLiveDataObserver, atLeastOnce()).onChanged(asyncResultCaptor.capture())
    assertThat(asyncResultCaptor.value.isFailure()).isTrue()
    assertThat(asyncResultCaptor.value.getErrorOrNull())
      .hasMessageThat()
      .contains("Cannot navigate to previous state; at initial state.")
  }

  @Test
  fun testMoveToPrevious_forStateWithCompletedPreviousState_succeeds() {
    subscribeToCurrentStateToAllowExplorationToLoad()
    playExploration(TEST_EXPLORATION_ID_0)
    submitMultipleChoiceAnswerAndMoveToNextState(0)

    val moveToStateResult =
      explorationProgressController.moveToPreviousState()
    moveToStateResult.observeForever(mockAsyncResultLiveDataObserver)
    testCoroutineDispatchers.runCurrent()

    // Verify that we can navigate to the previous state since the current state is complete and not initial.
    verify(mockAsyncResultLiveDataObserver, atLeastOnce()).onChanged(asyncResultCaptor.capture())
    assertThat(asyncResultCaptor.value.isSuccess()).isTrue()
  }

  @Test
  fun testMoveToPrevious_forCompletedState_movesToPreviousState() {
    val currentStateLiveData =
      explorationProgressController.getCurrentState()
    currentStateLiveData.observeForever(mockCurrentStateLiveDataObserver)
    playExploration(TEST_EXPLORATION_ID_0)
    submitMultipleChoiceAnswerAndMoveToNextState(0)

    moveToPreviousState()

    // Since the answer submission and forward navigation should work (see earlier tests), verify that the move to the
    // previous state does return us back to the initial exploration state (which is now completed).
    verify(
      mockCurrentStateLiveDataObserver,
      atLeastOnce()
    ).onChanged(currentStateResultCaptor.capture())
    assertThat(currentStateResultCaptor.value.isSuccess()).isTrue()
    val currentState = currentStateResultCaptor.value.getOrThrow()
    assertThat(currentState.state.name).isEqualTo("Welcome!")
    assertThat(currentState.stateTypeCase).isEqualTo(COMPLETED_STATE)
  }

  @Test
  fun testMoveToPrevious_navigatedForwardThenBackToInitial_failsWithError() {
    subscribeToCurrentStateToAllowExplorationToLoad()
    playExploration(TEST_EXPLORATION_ID_0)
    submitMultipleChoiceAnswerAndMoveToNextState(0)
    moveToPreviousState()

    val moveToStateResult =
      explorationProgressController.moveToPreviousState()
    moveToStateResult.observeForever(mockAsyncResultLiveDataObserver)
    testCoroutineDispatchers.runCurrent()

    // The first previous navigation should succeed (see above), but the second will fail since we're back at the
    // initial state.
    verify(mockAsyncResultLiveDataObserver, atLeastOnce()).onChanged(asyncResultCaptor.capture())
    assertThat(asyncResultCaptor.value.isFailure()).isTrue()
    assertThat(asyncResultCaptor.value.getErrorOrNull())
      .hasMessageThat()
      .contains("Cannot navigate to previous state; at initial state.")
  }

  @Test
  fun testSubmitAnswer_forTextInput_correctAnswer_returnsOutcomeWithTransition() {
    subscribeToCurrentStateToAllowExplorationToLoad()
    playExploration(TEST_EXPLORATION_ID_0)
    submitMultipleChoiceAnswerAndMoveToNextState(0)

    val result =
      explorationProgressController.submitAnswer(createTextInputAnswer("Finnish"))
    result.observeForever(mockAsyncAnswerOutcomeObserver)
    testCoroutineDispatchers.runCurrent()

    // Verify that the answer submission was successful.
    verify(
      mockAsyncAnswerOutcomeObserver,
      atLeastOnce()
    ).onChanged(asyncAnswerOutcomeCaptor.capture())
    val answerOutcome = asyncAnswerOutcomeCaptor.value.getOrThrow()
    assertThat(answerOutcome.destinationCase).isEqualTo(AnswerOutcome.DestinationCase.STATE_NAME)
    assertThat(answerOutcome.feedback.html).contains("Yes! Oppia is the Finnish word for learn.")
  }

  @Test
  fun testSubmitAnswer_forTextInput_wrongAnswer_returnsDefaultOutcome() {
    subscribeToCurrentStateToAllowExplorationToLoad()
    playExploration(TEST_EXPLORATION_ID_0)
    submitMultipleChoiceAnswerAndMoveToNextState(0)

    val result =
      explorationProgressController.submitAnswer(createTextInputAnswer("Klingon"))
    result.observeForever(mockAsyncAnswerOutcomeObserver)
    testCoroutineDispatchers.runCurrent()

    // Verify that the answer was wrong, and that there's no handler for it so the default outcome is returned.
    verify(
      mockAsyncAnswerOutcomeObserver,
      atLeastOnce()
    ).onChanged(asyncAnswerOutcomeCaptor.capture())
    val answerOutcome = asyncAnswerOutcomeCaptor.value.getOrThrow()
    assertThat(answerOutcome.destinationCase).isEqualTo(AnswerOutcome.DestinationCase.SAME_STATE)
    assertThat(answerOutcome.feedback.html).contains("Sorry, nope")
  }

  @Test
  fun testSubmitAnswer_forTextInput_wrongAnswer_returnsDefaultOutcome_showHint() {
    subscribeToCurrentStateToAllowExplorationToLoad()
    playExploration(TEST_EXPLORATION_ID_0)
    submitMultipleChoiceAnswerAndMoveToNextState(0)

    val result =
      explorationProgressController.submitAnswer(createTextInputAnswer("Klingon"))
    result.observeForever(mockAsyncAnswerOutcomeObserver)
    testCoroutineDispatchers.runCurrent()

    // Verify that the current state updates. It should stay pending, and the wrong answer should be appended.
    verify(
      mockCurrentStateLiveDataObserver,
      atLeastOnce()
    ).onChanged(currentStateResultCaptor.capture())
    assertThat(currentStateResultCaptor.value.isSuccess()).isTrue()
    val currentState = currentStateResultCaptor.value.getOrThrow()
    assertThat(currentState.stateTypeCase).isEqualTo(PENDING_STATE)
    assertThat(currentState.pendingState.wrongAnswerCount).isEqualTo(1)
    val answerAndFeedback = currentState.pendingState.getWrongAnswer(0)
    assertThat(answerAndFeedback.userAnswer.answer.normalizedString).isEqualTo("Klingon")
    assertThat(answerAndFeedback.feedback.html).contains("Sorry, nope")
    val hintAndSolution = currentState.state.interaction.getHint(0)
    assertThat(hintAndSolution.hintContent.html).contains("Start by finding the denominator")
  }

  @Test
  fun testRevealHint_forWrongAnswer_showHint_returnHintIsRevealed() {
    subscribeToCurrentStateToAllowExplorationToLoad()
    playExploration(TEST_EXPLORATION_ID_0)
    submitMultipleChoiceAnswerAndMoveToNextState(0)

    // Verify that the current state updates. It should stay pending, on submission of wrong answer.
    verify(
      mockCurrentStateLiveDataObserver,
      atLeastOnce()
    ).onChanged(currentStateResultCaptor.capture())
    assertThat(currentStateResultCaptor.value.isSuccess()).isTrue()
    val currentState = currentStateResultCaptor.value.getOrThrow()

    val result = explorationProgressController.submitHintIsRevealed(
      currentState.state,
      true,
      0
    )
    result.observeForever(mockAsyncHintObserver)
    testCoroutineDispatchers.runCurrent()

    assertThat(currentState.stateTypeCase).isEqualTo(PENDING_STATE)

    val hintAndSolution = currentState.state.interaction.getHint(0)
    assertThat(hintAndSolution.hintContent.html).contains("Start by finding the denominator")

    // Verify that the current state updates. Hint revealed is true.
    verify(
      mockCurrentStateLiveDataObserver,
      atLeastOnce()
    ).onChanged(currentStateResultCaptor.capture())
    assertThat(currentStateResultCaptor.value.isSuccess()).isTrue()
    val updatedState = currentStateResultCaptor.value.getOrThrow()

    assertThat(updatedState.state.interaction.getHint(0).hintIsRevealed).isTrue()
  }

  @Test
  fun testRevealSolution_forWrongAnswer_showSolution_returnSolutionIsRevealed() {
    subscribeToCurrentStateToAllowExplorationToLoad()
    playExploration(TEST_EXPLORATION_ID_0)
    submitMultipleChoiceAnswerAndMoveToNextState(0)

    // Verify that the current state updates. It should stay pending, on submission of wrong answer.
    verify(
      mockCurrentStateLiveDataObserver,
      atLeastOnce()
    ).onChanged(currentStateResultCaptor.capture())
    assertThat(currentStateResultCaptor.value.isSuccess()).isTrue()
    val currentState = currentStateResultCaptor.value.getOrThrow()

    val result = explorationProgressController.submitSolutionIsRevealed(currentState.state)
    result.observeForever(mockAsyncSolutionObserver)
    testCoroutineDispatchers.runCurrent()

    assertThat(currentState.stateTypeCase).isEqualTo(PENDING_STATE)

    // Verify that the current state updates. Solution revealed is true.
    verify(
      mockCurrentStateLiveDataObserver,
      atLeastOnce()
    ).onChanged(currentStateResultCaptor.capture())
    assertThat(currentStateResultCaptor.value.isSuccess()).isTrue()
    val updatedState = currentStateResultCaptor.value.getOrThrow()

    assertThat(updatedState.state.interaction.solution.solutionIsRevealed).isTrue()
  }

  @Test
  fun testSubmitAnswer_forTextInput_wrongAnswer_afterAllHintsAreExhausted_showSolution() {
    subscribeToCurrentStateToAllowExplorationToLoad()
    playExploration(TEST_EXPLORATION_ID_0)
    submitMultipleChoiceAnswerAndMoveToNextState(0)

    val result =
      explorationProgressController.submitAnswer(createTextInputAnswer("Klingon"))
    result.observeForever(mockAsyncAnswerOutcomeObserver)
    testCoroutineDispatchers.runCurrent()

    // Verify that the current state updates. It should stay pending, and the wrong answer should be appended.
    verify(
      mockCurrentStateLiveDataObserver,
      atLeastOnce()
    ).onChanged(currentStateResultCaptor.capture())
    assertThat(currentStateResultCaptor.value.isSuccess()).isTrue()
    val currentState = currentStateResultCaptor.value.getOrThrow()
    assertThat(currentState.stateTypeCase).isEqualTo(PENDING_STATE)
    assertThat(currentState.pendingState.wrongAnswerCount).isEqualTo(1)

    val hint1 = currentState.state.interaction.getHint(0)
    assertThat(hint1.hintContent.html)
      .contains("Start by finding the denominator")
    val hint2 = currentState.state.interaction.getHint(1)
    assertThat(hint2.hintContent.html)
      .contains("Next, find the numerator by counting the number of selected parts.")
    val hint3 = currentState.state.interaction.getHint(2)
    assertThat(hint3.hintContent.html)
      .contains("Always be careful about what you're counting. The question will have clues!")

    val solution = currentState.state.interaction.solution
    assertThat(solution.correctAnswer.correctAnswer)
      .isEqualTo("3")
    assertThat(solution.explanation.html)
      .contains("The denominator of a fraction is the second number in the fraction.")
  }

  @Test
  fun testGetCurrentState_secondState_submitRightAnswer_pendingStateBecomesCompleted() {
    subscribeToCurrentStateToAllowExplorationToLoad()
    playExploration(TEST_EXPLORATION_ID_0)
    submitMultipleChoiceAnswerAndMoveToNextState(0)

    val result =
      explorationProgressController.submitAnswer(createTextInputAnswer("Finnish"))
    result.observeForever(mockAsyncAnswerOutcomeObserver)
    testCoroutineDispatchers.runCurrent()

    // Verify that the current state updates. It should now be completed with the correct answer.
    verify(
      mockCurrentStateLiveDataObserver,
      atLeastOnce()
    ).onChanged(currentStateResultCaptor.capture())
    assertThat(currentStateResultCaptor.value.isSuccess()).isTrue()
    val currentState = currentStateResultCaptor.value.getOrThrow()
    assertThat(currentState.stateTypeCase).isEqualTo(COMPLETED_STATE)
    assertThat(currentState.completedState.answerCount).isEqualTo(1)
    val answerAndFeedback = currentState.completedState.getAnswer(0)
    assertThat(answerAndFeedback.userAnswer.answer.normalizedString).isEqualTo("Finnish")
    assertThat(answerAndFeedback.feedback.html).contains("Yes! Oppia is the Finnish word")
  }

  @Test
  fun testSubmitAnswer_forTextInput_withSpaces_updatesStateWithVerbatimAnswer() {
    subscribeToCurrentStateToAllowExplorationToLoad()
    playExploration(TEST_EXPLORATION_ID_0)
    submitMultipleChoiceAnswerAndMoveToNextState(0)

    val result =
      explorationProgressController.submitAnswer(createTextInputAnswer("Finnish  "))
    result.observeForever(mockAsyncAnswerOutcomeObserver)
    testCoroutineDispatchers.runCurrent()

    // Verify that the current state updates. The submitted answer should have a textual version that is a verbatim
    // version of the user-submitted answer.
    verify(
      mockCurrentStateLiveDataObserver,
      atLeastOnce()
    ).onChanged(currentStateResultCaptor.capture())
    assertThat(currentStateResultCaptor.value.isSuccess()).isTrue()
    val currentState = currentStateResultCaptor.value.getOrThrow()
    assertThat(currentState.stateTypeCase)
      .isEqualTo(COMPLETED_STATE)
    assertThat(currentState.completedState.answerCount)
      .isEqualTo(1)
    val answerAndFeedback = currentState.completedState.getAnswer(0)
    assertThat(answerAndFeedback.userAnswer.textualAnswerCase)
      .isEqualTo(UserAnswer.TextualAnswerCase.PLAIN_ANSWER)
    assertThat(answerAndFeedback.userAnswer.plainAnswer)
      .isEqualTo("Finnish  ")
  }

  @Test
  fun testGetCurrentState_secondState_submitWrongAnswer_updatePendingState() {
    subscribeToCurrentStateToAllowExplorationToLoad()
    playExploration(TEST_EXPLORATION_ID_0)
    submitMultipleChoiceAnswerAndMoveToNextState(0)

    val result =
      explorationProgressController.submitAnswer(createTextInputAnswer("Klingon"))
    result.observeForever(mockAsyncAnswerOutcomeObserver)
    testCoroutineDispatchers.runCurrent()

    // Verify that the current state updates. It should stay pending, and the wrong answer should be appended.
    verify(
      mockCurrentStateLiveDataObserver,
      atLeastOnce()
    ).onChanged(currentStateResultCaptor.capture())
    assertThat(currentStateResultCaptor.value.isSuccess()).isTrue()
    val currentState = currentStateResultCaptor.value.getOrThrow()
    assertThat(currentState.stateTypeCase).isEqualTo(PENDING_STATE)
    assertThat(currentState.pendingState.wrongAnswerCount).isEqualTo(1)
    val answerAndFeedback = currentState.pendingState.getWrongAnswer(0)
    assertThat(answerAndFeedback.userAnswer.answer.normalizedString).isEqualTo("Klingon")
    assertThat(answerAndFeedback.feedback.html).contains("Sorry, nope")
  }

  @Test
  fun testGetCurrentState_afterMovePreviousAndNext_returnsCurrentState() {
    val currentStateLiveData =
      explorationProgressController.getCurrentState()
    currentStateLiveData.observeForever(mockCurrentStateLiveDataObserver)
    playExploration(TEST_EXPLORATION_ID_0)
    submitMultipleChoiceAnswerAndMoveToNextState(0)

    moveToPreviousState()
    moveToNextState()

    // The current state should stay the same.
    verify(
      mockCurrentStateLiveDataObserver,
      atLeastOnce()
    ).onChanged(currentStateResultCaptor.capture())
    assertThat(currentStateResultCaptor.value.isSuccess()).isTrue()
    val currentState = currentStateResultCaptor.value.getOrThrow()
    assertThat(currentState.state.name).isEqualTo("What language")
    assertThat(currentState.stateTypeCase).isEqualTo(PENDING_STATE)
  }

  @Test
  fun testGetCurrentState_afterMoveNextAndPrevious_returnsCurrentState() {
    val currentStateLiveData =
      explorationProgressController.getCurrentState()
    currentStateLiveData.observeForever(mockCurrentStateLiveDataObserver)
    playExploration(TEST_EXPLORATION_ID_0)
    submitMultipleChoiceAnswerAndMoveToNextState(0)
    submitTextInputAnswer("Finnish") // Submit the answer but do not proceed to the next state.

    moveToNextState()
    moveToPreviousState()

    // The current state should stay the same.
    verify(
      mockCurrentStateLiveDataObserver,
      atLeastOnce()
    ).onChanged(currentStateResultCaptor.capture())
    assertThat(currentStateResultCaptor.value.isSuccess()).isTrue()
    val currentState = currentStateResultCaptor.value.getOrThrow()
    assertThat(currentState.state.name).isEqualTo("What language")
    assertThat(currentState.stateTypeCase).isEqualTo(COMPLETED_STATE)
  }

  @Test
  fun testGetCurrentState_afterMoveToPrev_onThirdState_newObserver_receivesCompletedSecondState() {
    subscribeToCurrentStateToAllowExplorationToLoad()
    playExploration(TEST_EXPLORATION_ID_0)
    submitMultipleChoiceAnswerAndMoveToNextState(0) // First state -> second
    submitTextInputAnswerAndMoveToNextState("Finnish") // Second state -> third

    // Move to the previous state and register a new observer.
    moveToPreviousState() // Third state -> second
    val currentStateLiveData =
      explorationProgressController.getCurrentState()
    currentStateLiveData.observeForever(mockCurrentStateLiveDataObserver2)
    testCoroutineDispatchers.runCurrent()

    // The new observer should observe the completed second state since it's the current pending state.
    verify(
      mockCurrentStateLiveDataObserver2,
      atLeastOnce()
    ).onChanged(currentStateResultCaptor.capture())
    assertThat(currentStateResultCaptor.value.isSuccess()).isTrue()
    val currentState = currentStateResultCaptor.value.getOrThrow()
    assertThat(currentState.state.name).isEqualTo("What language")
    assertThat(currentState.stateTypeCase).isEqualTo(COMPLETED_STATE)
  }

  @Test
  fun testGetCurrentState_forFirstState_doesNotHaveNextState() {
    val currentStateLiveData =
      explorationProgressController.getCurrentState()
    currentStateLiveData.observeForever(mockCurrentStateLiveDataObserver)

    playExploration(TEST_EXPLORATION_ID_0)

    // The initial state should not have a next state.
    verify(
      mockCurrentStateLiveDataObserver,
      atLeastOnce()
    ).onChanged(currentStateResultCaptor.capture())
    assertThat(currentStateResultCaptor.value.isSuccess()).isTrue()
    assertThat(currentStateResultCaptor.value.getOrThrow().hasNextState).isFalse()
  }

  @Test
  fun testGetCurrentState_forFirstState_afterAnswerSubmission_doesNotHaveNextState() {
    val currentStateLiveData =
      explorationProgressController.getCurrentState()
    currentStateLiveData.observeForever(mockCurrentStateLiveDataObserver)
    playExploration(TEST_EXPLORATION_ID_0)

    submitMultipleChoiceAnswer(0)

    // Simply completing the current state should not result in there being a next state since the user hasn't proceeded
    // to the following state, yet.
    verify(
      mockCurrentStateLiveDataObserver,
      atLeastOnce()
    ).onChanged(currentStateResultCaptor.capture())
    assertThat(currentStateResultCaptor.value.isSuccess()).isTrue()
    assertThat(currentStateResultCaptor.value.getOrThrow().hasNextState).isFalse()
  }

  @Test
  fun testGetCurrentState_forSecondState_doesNotHaveNextState() {
    val currentStateLiveData =
      explorationProgressController.getCurrentState()
    currentStateLiveData.observeForever(mockCurrentStateLiveDataObserver)
    playExploration(TEST_EXPLORATION_ID_0)

    submitMultipleChoiceAnswerAndMoveToNextState(0)

    // The current state should have a previous state.
    verify(
      mockCurrentStateLiveDataObserver,
      atLeastOnce()
    ).onChanged(currentStateResultCaptor.capture())
    assertThat(currentStateResultCaptor.value.isSuccess()).isTrue()
    assertThat(currentStateResultCaptor.value.getOrThrow().hasNextState).isFalse()
  }

  @Test
  fun testGetCurrentState_forSecondState_navigateBackward_hasNextState() {
    val currentStateLiveData =
      explorationProgressController.getCurrentState()
    currentStateLiveData.observeForever(mockCurrentStateLiveDataObserver)
    playExploration(TEST_EXPLORATION_ID_0)
    submitMultipleChoiceAnswerAndMoveToNextState(0)

    moveToPreviousState()

    // The previous state should have a next state.
    verify(
      mockCurrentStateLiveDataObserver,
      atLeastOnce()
    ).onChanged(currentStateResultCaptor.capture())
    assertThat(currentStateResultCaptor.value.isSuccess()).isTrue()
    assertThat(currentStateResultCaptor.value.getOrThrow().hasNextState).isTrue()
  }

  @Test
  fun testGetCurrentState_forSecondState_navigateBackwardThenForward_doesNotHaveNextState() {
    val currentStateLiveData =
      explorationProgressController.getCurrentState()
    currentStateLiveData.observeForever(mockCurrentStateLiveDataObserver)
    playExploration(TEST_EXPLORATION_ID_0)
    submitMultipleChoiceAnswerAndMoveToNextState(0)

    moveToPreviousState()
    moveToNextState()

    // Iterating back to the current state should result in no longer having a next state.
    verify(
      mockCurrentStateLiveDataObserver,
      atLeastOnce()
    ).onChanged(currentStateResultCaptor.capture())
    assertThat(currentStateResultCaptor.value.isSuccess()).isTrue()
    assertThat(currentStateResultCaptor.value.getOrThrow().hasNextState).isFalse()
  }

  @Test
  fun testSubmitAnswer_forNumericInput_correctAnswer_returnsOutcomeWithTransition() {
    subscribeToCurrentStateToAllowExplorationToLoad()
    playExploration(TEST_EXPLORATION_ID_0)
    submitMultipleChoiceAnswerAndMoveToNextState(0)
    submitTextInputAnswerAndMoveToNextState("Finnish")

    val result = explorationProgressController.submitAnswer(
      createNumericInputAnswer(121.0)
    )
    result.observeForever(mockAsyncAnswerOutcomeObserver)
    testCoroutineDispatchers.runCurrent()

    // Verify that the answer submission was successful.
    verify(
      mockAsyncAnswerOutcomeObserver,
      atLeastOnce()
    ).onChanged(asyncAnswerOutcomeCaptor.capture())
    val answerOutcome = asyncAnswerOutcomeCaptor.value.getOrThrow()
    assertThat(answerOutcome.destinationCase).isEqualTo(AnswerOutcome.DestinationCase.STATE_NAME)
    assertThat(answerOutcome.feedback.html).contains("Yes, that's correct: 11 times 11 is 121.")
  }

  @Test
  fun testSubmitAnswer_forNumericInput_wrongAnswer_returnsOutcomeWithTransition() {
    subscribeToCurrentStateToAllowExplorationToLoad()
    playExploration(TEST_EXPLORATION_ID_0)
    submitMultipleChoiceAnswerAndMoveToNextState(0)
    submitTextInputAnswerAndMoveToNextState("Finnish")

    val result = explorationProgressController.submitAnswer(
      createNumericInputAnswer(122.0)
    )
    result.observeForever(mockAsyncAnswerOutcomeObserver)
    testCoroutineDispatchers.runCurrent()

    // Verify that the answer submission failed as expected.
    verify(
      mockAsyncAnswerOutcomeObserver,
      atLeastOnce()
    ).onChanged(asyncAnswerOutcomeCaptor.capture())
    val answerOutcome = asyncAnswerOutcomeCaptor.value.getOrThrow()
    assertThat(answerOutcome.destinationCase).isEqualTo(AnswerOutcome.DestinationCase.SAME_STATE)
    assertThat(answerOutcome.feedback.html).contains("You are actually very close.")
  }

  @Test
  fun testSubmitAnswer_forContinue_returnsOutcomeWithTransition() {
    subscribeToCurrentStateToAllowExplorationToLoad()
    playExploration(TEST_EXPLORATION_ID_0)
    submitMultipleChoiceAnswerAndMoveToNextState(0)
    submitTextInputAnswerAndMoveToNextState("Finnish")
    submitNumericInputAnswerAndMoveToNextState(121.0)

    val result = explorationProgressController.submitAnswer(
      createContinueButtonAnswer()
    )
    result.observeForever(mockAsyncAnswerOutcomeObserver)
    testCoroutineDispatchers.runCurrent()

    // Verify that the continue button succeeds by default.
    verify(
      mockAsyncAnswerOutcomeObserver,
      atLeastOnce()
    ).onChanged(asyncAnswerOutcomeCaptor.capture())
    val answerOutcome = asyncAnswerOutcomeCaptor.value.getOrThrow()
    assertThat(answerOutcome.destinationCase).isEqualTo(AnswerOutcome.DestinationCase.STATE_NAME)
    assertThat(answerOutcome.feedback.html).isEmpty()
  }

  @Test
  fun testGetCurrentState_fifthState_isTerminalState() {
    val currentStateLiveData =
      explorationProgressController.getCurrentState()
    currentStateLiveData.observeForever(mockCurrentStateLiveDataObserver)
    playExploration(TEST_EXPLORATION_ID_0)
    submitMultipleChoiceAnswerAndMoveToNextState(0)
    submitTextInputAnswerAndMoveToNextState("Finnish")
    submitNumericInputAnswerAndMoveToNextState(121.0)

    submitContinueButtonAnswerAndMoveToNextState()

    // Verify that the fifth state is terminal.
    verify(
      mockCurrentStateLiveDataObserver,
      atLeastOnce()
    ).onChanged(currentStateResultCaptor.capture())
    assertThat(currentStateResultCaptor.value.isSuccess()).isTrue()
    val currentState = currentStateResultCaptor.value.getOrThrow()
    assertThat(currentState.stateTypeCase).isEqualTo(TERMINAL_STATE)
  }

  @Test
  fun testGetCurrentState_afterMoveToPrevious_onThirdState_updatesToCompletedSecondState() {
    val currentStateLiveData =
      explorationProgressController.getCurrentState()
    currentStateLiveData.observeForever(mockCurrentStateLiveDataObserver)
    playExploration(TEST_EXPLORATION_ID_0)
    submitMultipleChoiceAnswerAndMoveToNextState(0)
    submitTextInputAnswerAndMoveToNextState("Finnish")

    moveToPreviousState()

    // Verify that the current state is the second state, and is completed. It should also have the previously submitted
    // answer, allowing learners to potentially view past answers.
    verify(
      mockCurrentStateLiveDataObserver,
      atLeastOnce()
    ).onChanged(currentStateResultCaptor.capture())
    assertThat(currentStateResultCaptor.value.isSuccess()).isTrue()
    val currentState = currentStateResultCaptor.value.getOrThrow()
    assertThat(currentState.stateTypeCase)
      .isEqualTo(COMPLETED_STATE)
    assertThat(currentState.state.name)
      .isEqualTo("What language")
    assertThat(currentState.completedState.getAnswer(0).userAnswer.answer.normalizedString)
      .isEqualTo("Finnish")
  }

  @Test
  fun testMoveToNext_onFinalState_failsWithError() {
    subscribeToCurrentStateToAllowExplorationToLoad()
    playExploration(TEST_EXPLORATION_ID_0)
    submitMultipleChoiceAnswerAndMoveToNextState(0)
    submitTextInputAnswerAndMoveToNextState("Finnish")
    submitNumericInputAnswerAndMoveToNextState(121.0)
    submitContinueButtonAnswerAndMoveToNextState()

    val moveToStateResult = explorationProgressController.moveToNextState()
    moveToStateResult.observeForever(mockAsyncResultLiveDataObserver)
    testCoroutineDispatchers.runCurrent()

    // Verify we can't navigate past the last state of the exploration.
    verify(mockAsyncResultLiveDataObserver, atLeastOnce()).onChanged(asyncResultCaptor.capture())
    assertThat(asyncResultCaptor.value.isFailure()).isTrue()
    assertThat(asyncResultCaptor.value.getErrorOrNull())
      .hasMessageThat()
      .contains("Cannot navigate to next state; at most recent state.")
  }

  @Test
  fun testGetCurrentState_afterPlayingFullSecondExploration_returnsTerminalState() {
    val currentStateLiveData =
      explorationProgressController.getCurrentState()
    currentStateLiveData.observeForever(mockCurrentStateLiveDataObserver)

    playExploration(TEST_EXPLORATION_ID_1)
    submitContinueButtonAnswerAndMoveToNextState()
    submitMultipleChoiceAnswerAndMoveToNextState(3) // Those were all the questions I had!
    submitContinueButtonAnswerAndMoveToNextState()

    // Verify that we're now on the final state.
    verify(
      mockCurrentStateLiveDataObserver,
      atLeastOnce()
    ).onChanged(currentStateResultCaptor.capture())
    assertThat(currentStateResultCaptor.value.isSuccess()).isTrue()
    val currentState = currentStateResultCaptor.value.getOrThrow()
    assertThat(currentState.stateTypeCase).isEqualTo(TERMINAL_STATE)
  }

  @Test
  fun testGetCurrentState_afterPlayingFullSecondExploration_diffPath_returnsTerminalState() {
    val currentStateLiveData =
      explorationProgressController.getCurrentState()
    currentStateLiveData.observeForever(mockCurrentStateLiveDataObserver)

    playExploration(TEST_EXPLORATION_ID_1)
    submitContinueButtonAnswerAndMoveToNextState()
    submitMultipleChoiceAnswerAndMoveToNextState(0) // How do your explorations work?
    submitTextInputAnswerAndMoveToNextState("Oppia Otter") // Can I ask your name?
    submitContinueButtonAnswerAndMoveToNextState()
    submitMultipleChoiceAnswerAndMoveToNextState(3) // Those were all the questions I had!
    submitContinueButtonAnswerAndMoveToNextState()

    // Verify that a different path can also result in reaching the end state.
    verify(
      mockCurrentStateLiveDataObserver,
      atLeastOnce()
    ).onChanged(currentStateResultCaptor.capture())
    assertThat(currentStateResultCaptor.value.isSuccess()).isTrue()
    val currentState = currentStateResultCaptor.value.getOrThrow()
    assertThat(currentState.stateTypeCase).isEqualTo(TERMINAL_STATE)
  }

  @Test
  fun testGetCurrentState_afterPlayingThroughPreviousExplorations_returnsStateFromSecondExp() {
    val currentStateLiveData =
      explorationProgressController.getCurrentState()
    currentStateLiveData.observeForever(mockCurrentStateLiveDataObserver)
    playThroughExploration5()

    playExploration(TEST_EXPLORATION_ID_1)
    submitContinueButtonAnswerAndMoveToNextState()
    // Those were all the questions I had!
    submitMultipleChoiceAnswerAndMoveToNextState(3)

    // Verify that we're on the second-to-last state of the second exploration.
    verify(
      mockCurrentStateLiveDataObserver,
      atLeastOnce()
    ).onChanged(currentStateResultCaptor.capture())
    assertThat(currentStateResultCaptor.value.isSuccess()).isTrue()
    val currentState = currentStateResultCaptor.value.getOrThrow()
    assertThat(currentState.stateTypeCase).isEqualTo(PENDING_STATE)
    // This state is not in the other test exp.
    assertThat(currentState.state.name).isEqualTo("End Card")
  }

  @Test
  fun testMoveToNext_beforePlaying_failsWithError_logsException() {
    val moveToStateResult =
      explorationProgressController.moveToNextState()
    moveToStateResult.observeForever(mockAsyncResultLiveDataObserver)
    val exception = fakeExceptionLogger.getMostRecentException()

    assertThat(exception).isInstanceOf(IllegalStateException::class.java)
    assertThat(exception).hasMessageThat()
      .contains("Cannot navigate to a next state if an exploration is not being played.")
  }

  @Test
  fun testMoveToPrevious_navigatedForwardThenBackToInitial_failsWithError_logsException() {
    subscribeToCurrentStateToAllowExplorationToLoad()
    playExploration(TEST_EXPLORATION_ID_0)
    submitMultipleChoiceAnswerAndMoveToNextState(0)
    moveToPreviousState()

    val moveToStateResult =
      explorationProgressController.moveToPreviousState()
    moveToStateResult.observeForever(mockAsyncResultLiveDataObserver)
    testCoroutineDispatchers.runCurrent()
    val exception = fakeExceptionLogger.getMostRecentException()

    assertThat(exception).isInstanceOf(IllegalStateException::class.java)
    assertThat(exception).hasMessageThat()
      .contains("Cannot navigate to previous state; at initial state.")
  }

  @Test
  fun testSubmitAnswer_beforePlaying_failsWithError_logsException() {
    val result = explorationProgressController.submitAnswer(
      createMultipleChoiceAnswer(0)
    )
    result.observeForever(mockAsyncAnswerOutcomeObserver)
    testCoroutineDispatchers.runCurrent()
    val exception = fakeExceptionLogger.getMostRecentException()

    assertThat(exception).isInstanceOf(IllegalStateException::class.java)
    assertThat(exception).hasMessageThat()
      .contains("Cannot submit an answer if an exploration is not being played.")
  }

  @Test
  fun testGetCurrentState_playInvalidExploration_returnsFailure_logsException() {
    val currentStateLiveData =
      explorationProgressController.getCurrentState()
    currentStateLiveData.observeForever(mockCurrentStateLiveDataObserver)

    playExploration("invalid_exp_id")
    val exception = fakeExceptionLogger.getMostRecentException()

    assertThat(exception).isInstanceOf(FileNotFoundException::class.java)
    assertThat(exception).hasMessageThat().contains("invalid_exp_id.json")
  }

  private fun getTestExploration5(): Exploration {
    return explorationRetriever.loadExploration(TEST_EXPLORATION_ID_0)
  }

  private fun getTestExploration6(): Exploration {
    return explorationRetriever.loadExploration(TEST_EXPLORATION_ID_1)
  }

  private fun setUpTestApplicationComponent() {
    DaggerExplorationProgressControllerTest_TestApplicationComponent.builder()
      .setApplication(ApplicationProvider.getApplicationContext())
      .build()
      .inject(this)
  }

  /**
   * Creates a blank subscription to the current state to ensure that requests to load the exploration complete,
   * otherwise post-load operations may fail. An observer is required since the current mediator live data
   * implementation will only lazily load data based on whether there's an active subscription.
   */
  private fun subscribeToCurrentStateToAllowExplorationToLoad() {
    explorationProgressController.getCurrentState().observeForever(mockCurrentStateLiveDataObserver)
  }

  private fun playExploration(explorationId: String) {
    explorationDataController.startPlayingExploration(explorationId)
    testCoroutineDispatchers.runCurrent()
  }

  private fun submitMultipleChoiceAnswer(choiceIndex: Int) {
    explorationProgressController.submitAnswer(createMultipleChoiceAnswer(choiceIndex))
    testCoroutineDispatchers.runCurrent()
  }

  private fun submitTextInputAnswer(textAnswer: String) {
    explorationProgressController.submitAnswer(createTextInputAnswer(textAnswer))
    testCoroutineDispatchers.runCurrent()
  }

  private fun submitNumericInputAnswer(numericAnswer: Double) {
    explorationProgressController.submitAnswer(createNumericInputAnswer(numericAnswer))
    testCoroutineDispatchers.runCurrent()
  }

  private fun submitContinueButtonAnswer() {
    explorationProgressController.submitAnswer(createContinueButtonAnswer())
    testCoroutineDispatchers.runCurrent()
  }

  private fun submitMultipleChoiceAnswerAndMoveToNextState(choiceIndex: Int) {
    submitMultipleChoiceAnswer(choiceIndex)
    moveToNextState()
  }

  private fun submitTextInputAnswerAndMoveToNextState(textAnswer: String) {
    submitTextInputAnswer(textAnswer)
    moveToNextState()
  }

  private fun submitNumericInputAnswerAndMoveToNextState(numericAnswer: Double) {
    submitNumericInputAnswer(numericAnswer)
    moveToNextState()
  }

  private fun submitContinueButtonAnswerAndMoveToNextState() {
    submitContinueButtonAnswer()
    moveToNextState()
  }

  private fun moveToNextState() {
    explorationProgressController.moveToNextState()
    testCoroutineDispatchers.runCurrent()
  }

  private fun moveToPreviousState() {
    explorationProgressController.moveToPreviousState()
    testCoroutineDispatchers.runCurrent()
  }

  private fun endExploration() {
    explorationDataController.stopPlayingExploration()
    testCoroutineDispatchers.runCurrent()
  }

  private fun playThroughExploration5() {
    playExploration(TEST_EXPLORATION_ID_0)
    submitMultipleChoiceAnswerAndMoveToNextState(0)
    submitTextInputAnswerAndMoveToNextState("Finnish")
    submitNumericInputAnswerAndMoveToNextState(121.0)
    submitContinueButtonAnswerAndMoveToNextState()
    endExploration()
  }

  private fun createMultipleChoiceAnswer(choiceIndex: Int): UserAnswer {
    return convertToUserAnswer(
      InteractionObject.newBuilder().setNonNegativeInt(choiceIndex).build()
    )
  }

  private fun createTextInputAnswer(textAnswer: String): UserAnswer {
    val answer = InteractionObject.newBuilder().setNormalizedString(textAnswer)
      .build()
    return UserAnswer.newBuilder().setAnswer(answer).setPlainAnswer(textAnswer).build()
  }

  private fun createNumericInputAnswer(numericAnswer: Double): UserAnswer {
    return convertToUserAnswer(InteractionObject.newBuilder().setReal(numericAnswer).build())
  }

  private fun convertToUserAnswer(answer: InteractionObject): UserAnswer {
    return UserAnswer.newBuilder().setAnswer(answer).setPlainAnswer(answer.toAnswerString()).build()
  }

  private fun createContinueButtonAnswer() =
    createTextInputAnswer(DEFAULT_CONTINUE_INTERACTION_TEXT_ANSWER)

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
      RatioInputModule::class
    ]
  )
  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun inject(explorationProgressControllerTest: ExplorationProgressControllerTest)
  }
}
