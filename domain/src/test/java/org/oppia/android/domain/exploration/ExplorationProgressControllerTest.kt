package org.oppia.android.domain.exploration

import android.app.Application
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import java.io.FileNotFoundException
import javax.inject.Inject
import javax.inject.Singleton
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito.atLeast
import org.mockito.Mockito.atLeastOnce
import org.mockito.Mockito.reset
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import org.oppia.android.app.model.AnswerOutcome
import org.oppia.android.app.model.ClickOnImage
import org.oppia.android.app.model.EphemeralState
import org.oppia.android.app.model.EphemeralState.StateTypeCase.COMPLETED_STATE
import org.oppia.android.app.model.EphemeralState.StateTypeCase.PENDING_STATE
import org.oppia.android.app.model.EphemeralState.StateTypeCase.TERMINAL_STATE
import org.oppia.android.app.model.Fraction
import org.oppia.android.app.model.Hint
import org.oppia.android.app.model.InteractionObject
import org.oppia.android.app.model.ListOfSetsOfTranslatableHtmlContentIds
import org.oppia.android.app.model.Point2d
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.RatioExpression
import org.oppia.android.app.model.SetOfTranslatableHtmlContentIds
import org.oppia.android.app.model.Solution
import org.oppia.android.app.model.TranslatableHtmlContentId
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
import org.oppia.android.domain.exploration.lightweightcheckpointing.ExplorationCheckpointState
import org.oppia.android.domain.exploration.lightweightcheckpointing.ExplorationStorageDatabaseSize
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.domain.topic.TEST_EXPLORATION_ID_2
import org.oppia.android.domain.topic.TEST_EXPLORATION_ID_4
import org.oppia.android.domain.topic.TEST_EXPLORATION_ID_5
import org.oppia.android.domain.topic.TEST_STORY_ID_2
import org.oppia.android.domain.topic.UPCOMING_TOPIC_ID_1
import org.oppia.android.domain.util.toAnswerString
import org.oppia.android.testing.FakeExceptionLogger
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.environment.TestEnvironmentConfig
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClock
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.caching.CacheAssetsLocally
import org.oppia.android.util.caching.LoadLessonProtosFromAssets
import org.oppia.android.util.caching.TopicListToCache
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import org.oppia.android.util.data.DataProvidersInjector
import org.oppia.android.util.data.DataProvidersInjectorProvider
import org.oppia.android.util.logging.EnableConsoleLog
import org.oppia.android.util.logging.EnableFileLog
import org.oppia.android.util.logging.GlobalLogLevel
import org.oppia.android.util.logging.LogLevel
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode

// For context:
// https://github.com/oppia/oppia/blob/37285a/extensions/interactions/Continue/directives/oppia-interactive-continue.directive.ts.
private const val DEFAULT_CONTINUE_INTERACTION_TEXT_ANSWER = "Please continue."

/** Tests for [ExplorationProgressController]. */
@Suppress("SameParameterValue") // To avoid ignorable warnings for test helper methods.
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = ExplorationProgressControllerTest.TestApplication::class)
class ExplorationProgressControllerTest {
  // TODO(#114): Add much more thorough tests for the integration pathway.

  // TODO(#59): Once AsyncDataSubscriptionManager can be replaced with a fake, add the following
  //  tests once careful testing timing can be controlled:
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
  lateinit var fakeExceptionLogger: FakeExceptionLogger

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @Inject
  lateinit var oppiaClock: FakeOppiaClock

  @Mock
  lateinit var mockCurrentStateLiveDataObserver: Observer<AsyncResult<EphemeralState>>

  @Mock
  lateinit var mockCurrentStateLiveDataObserver2: Observer<AsyncResult<EphemeralState>>

  @Mock
  lateinit var mockAsyncResultLiveDataObserver: Observer<AsyncResult<*>>

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

  private val profileId = ProfileId.newBuilder().setInternalId(0).build()

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testGetCurrentState_noExploration_isPending() {
    val currentStateLiveData =
      explorationProgressController.getCurrentState().toLiveData()

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

    // An invalid exploration is not known until it's fully loaded, and that's observed via
    // getCurrentState.
    verify(mockAsyncResultLiveDataObserver).onChanged(asyncResultCaptor.capture())
    assertThat(asyncResultCaptor.value.isSuccess()).isTrue()
  }

  @Test
  fun testGetCurrentState_playInvalidExploration_returnsFailure() {
    val currentStateLiveData =
      explorationProgressController.getCurrentState().toLiveData()
    currentStateLiveData.observeForever(mockCurrentStateLiveDataObserver)

    playExploration("invalid_exp_id")

    verify(
      mockCurrentStateLiveDataObserver,
      atLeastOnce()
    ).onChanged(currentStateResultCaptor.capture())
    assertThat(currentStateResultCaptor.value.isFailure()).isTrue()
    assertThat(currentStateResultCaptor.value.getErrorOrNull())
      .hasMessageThat()
      .contains("invalid_exp_id")
  }

  @Test
  fun testPlayExploration_valid_returnsSuccess() {
    val resultLiveData =
      explorationDataController.startPlayingExploration(TEST_EXPLORATION_ID_2)
    resultLiveData.observeForever(mockAsyncResultLiveDataObserver)
    testCoroutineDispatchers.runCurrent()

    verify(mockAsyncResultLiveDataObserver).onChanged(asyncResultCaptor.capture())
    assertThat(asyncResultCaptor.value.isSuccess()).isTrue()
  }

  @Test
  fun testGetCurrentState_playExploration_returnsPendingResultFromLoadingExploration() {
    val currentStateLiveData =
      explorationProgressController.getCurrentState().toLiveData()
    currentStateLiveData.observeForever(mockCurrentStateLiveDataObserver)
    testCoroutineDispatchers.runCurrent()

    playExploration(TEST_EXPLORATION_ID_2)

    // The second-to-latest result stays pending since the exploration was loading (the actual
    // result is the fully loaded exploration). This is only true if the observer begins before
    // starting to load the exploration.
    verify(
      mockCurrentStateLiveDataObserver,
      atLeast(2)
    ).onChanged(currentStateResultCaptor.capture())
    val results = currentStateResultCaptor.allValues
    assertThat(results[results.size - 2].isPending()).isTrue()
  }

  @Test
  fun testGetCurrentState_playExploration_loaded_returnsInitialStatePending() {
    playExploration(TEST_EXPLORATION_ID_2)

    val currentStateLiveData =
      explorationProgressController.getCurrentState().toLiveData()
    currentStateLiveData.observeForever(mockCurrentStateLiveDataObserver)
    testCoroutineDispatchers.runCurrent()

    verify(
      mockCurrentStateLiveDataObserver,
      atLeastOnce()
    ).onChanged(currentStateResultCaptor.capture())
    assertThat(currentStateResultCaptor.value.isSuccess()).isTrue()
    assertThat(currentStateResultCaptor.value.getOrThrow().stateTypeCase).isEqualTo(PENDING_STATE)
    assertThat(currentStateResultCaptor.value.getOrThrow().hasPreviousState).isFalse()
    assertThat(currentStateResultCaptor.value.getOrThrow().state.name).isEqualTo("Continue")
  }

  @Test
  fun testGetCurrentState_playInvalidExploration_thenPlayValidExp_returnsInitialPendingState() {
    // Start with playing an invalid exploration.
    playExploration("invalid_exp_id")
    endExploration()

    // Then a valid one.
    playExploration(TEST_EXPLORATION_ID_2)
    val currentStateLiveData =
      explorationProgressController.getCurrentState().toLiveData()
    currentStateLiveData.observeForever(mockCurrentStateLiveDataObserver)
    testCoroutineDispatchers.runCurrent()

    // The latest result should correspond to the valid ID, and the progress controller should
    // gracefully recover.
    verify(
      mockCurrentStateLiveDataObserver,
      atLeastOnce()
    ).onChanged(currentStateResultCaptor.capture())
    assertThat(currentStateResultCaptor.value.isSuccess()).isTrue()
    assertThat(currentStateResultCaptor.value.getOrThrow().stateTypeCase).isEqualTo(PENDING_STATE)
    assertThat(currentStateResultCaptor.value.getOrThrow().hasPreviousState).isFalse()
    assertThat(currentStateResultCaptor.value.getOrThrow().state.name).isEqualTo("Continue")
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
    playExploration(TEST_EXPLORATION_ID_2)

    // Try playing another exploration without finishing the previous one.
    val resultLiveData =
      explorationDataController.startPlayingExploration(TEST_EXPLORATION_ID_2)
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
      explorationProgressController.getCurrentState().toLiveData()
    currentStateLiveData.observeForever(mockCurrentStateLiveDataObserver)
    // Start with playing a valid exploration, then stop.
    playExploration(TEST_EXPLORATION_ID_2)
    endExploration()

    // Then another valid one.
    playExploration(TEST_EXPLORATION_ID_4)

    // The latest result should correspond to the valid ID, and the progress controller should
    // gracefully recover.
    verify(
      mockCurrentStateLiveDataObserver,
      atLeastOnce()
    ).onChanged(currentStateResultCaptor.capture())
    assertThat(currentStateResultCaptor.value.isSuccess()).isTrue()
    assertThat(currentStateResultCaptor.value.getOrThrow().stateTypeCase).isEqualTo(PENDING_STATE)
    assertThat(currentStateResultCaptor.value.getOrThrow().hasPreviousState).isFalse()
    assertThat(currentStateResultCaptor.value.getOrThrow().state.name)
      .isEqualTo("DragDropSortInput")
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
    explorationDataController.startPlayingExploration(TEST_EXPLORATION_ID_2)

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
    playExploration(TEST_EXPLORATION_ID_2)
    navigateToPrototypeMultipleChoiceState()

    val result =
      explorationProgressController.submitAnswer(createMultipleChoiceAnswer(2))
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
    playExploration(TEST_EXPLORATION_ID_2)
    navigateToPrototypeMultipleChoiceState()

    val result =
      explorationProgressController.submitAnswer(createMultipleChoiceAnswer(2))
    result.observeForever(mockAsyncAnswerOutcomeObserver)
    testCoroutineDispatchers.runCurrent()

    // Verify that the answer submission was successful.
    verify(
      mockAsyncAnswerOutcomeObserver,
      atLeastOnce()
    ).onChanged(asyncAnswerOutcomeCaptor.capture())
    val answerOutcome = asyncAnswerOutcomeCaptor.value.getOrThrow()
    assertThat(answerOutcome.destinationCase).isEqualTo(AnswerOutcome.DestinationCase.STATE_NAME)
    assertThat(answerOutcome.feedback.html).contains("Correct!")
  }

  @Test
  fun testSubmitAnswer_forMultipleChoice_wrongAnswer_succeeds() {
    subscribeToCurrentStateToAllowExplorationToLoad()
    playExploration(TEST_EXPLORATION_ID_2)
    navigateToPrototypeMultipleChoiceState()

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
  fun testSubmitAnswer_forMultipleChoice_wrongAnswer_providesDefFeedbackAndSameStateTransition() {
    subscribeToCurrentStateToAllowExplorationToLoad()
    playExploration(TEST_EXPLORATION_ID_2)
    navigateToPrototypeMultipleChoiceState()

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
    assertThat(answerOutcome.destinationCase).isEqualTo(AnswerOutcome.DestinationCase.SAME_STATE)
    assertThat(answerOutcome.feedback.html).contains("Try again.")
  }

  @Test
  fun testGetCurrentState_afterSubmittingCorrectMultiChoiceAnswer_becomesCompletedState() {
    val currentStateLiveData =
      explorationProgressController.getCurrentState().toLiveData()
    currentStateLiveData.observeForever(mockCurrentStateLiveDataObserver)
    playExploration(TEST_EXPLORATION_ID_2)
    navigateToPrototypeMultipleChoiceState()

    submitMultipleChoiceAnswer(2)

    // Verify that the current state updates. It should now be completed with the correct answer.
    verify(
      mockCurrentStateLiveDataObserver,
      atLeastOnce()
    ).onChanged(currentStateResultCaptor.capture())
    assertThat(currentStateResultCaptor.value.isSuccess()).isTrue()
    val currentState = currentStateResultCaptor.value.getOrThrow()
    assertThat(currentState.stateTypeCase).isEqualTo(COMPLETED_STATE)
    assertThat(currentState.completedState.answerCount).isEqualTo(1)
    assertThat(currentState.completedState.getAnswer(0).userAnswer.answer.nonNegativeInt)
      .isEqualTo(2)
    assertThat(currentState.completedState.getAnswer(0).feedback.html).contains("Correct!")
  }

  @Test
  fun testGetCurrentState_afterSubmittingWrongMultiChoiceAnswer_updatesPendingState() {
    val currentStateLiveData =
      explorationProgressController.getCurrentState().toLiveData()
    currentStateLiveData.observeForever(mockCurrentStateLiveDataObserver)
    playExploration(TEST_EXPLORATION_ID_2)
    navigateToPrototypeMultipleChoiceState()

    submitMultipleChoiceAnswer(0)

    // Verify that the current state updates. It should stay pending, and the wrong answer should be
    // appended.
    verify(
      mockCurrentStateLiveDataObserver,
      atLeastOnce()
    ).onChanged(currentStateResultCaptor.capture())
    assertThat(currentStateResultCaptor.value.isSuccess()).isTrue()
    val currentState = currentStateResultCaptor.value.getOrThrow()
    assertThat(currentState.stateTypeCase).isEqualTo(PENDING_STATE)
    assertThat(currentState.pendingState.wrongAnswerCount).isEqualTo(1)
    assertThat(currentState.pendingState.getWrongAnswer(0).userAnswer.answer.nonNegativeInt)
      .isEqualTo(0)
    assertThat(currentState.pendingState.getWrongAnswer(0).feedback.html).contains("Try again.")
  }

  @Test
  fun testGetCurrentState_afterSubmittingWrongThenRightAnswer_updatesToStateWithBothAnswers() {
    subscribeToCurrentStateToAllowExplorationToLoad()
    playExploration(TEST_EXPLORATION_ID_2)
    navigateToPrototypeMultipleChoiceState()
    submitMultipleChoiceAnswer(0)

    submitMultipleChoiceAnswer(2)

    // Verify that the current state updates. It should now be completed with both the wrong and
    // correct answers.
    verify(
      mockCurrentStateLiveDataObserver,
      atLeastOnce()
    ).onChanged(currentStateResultCaptor.capture())
    assertThat(currentStateResultCaptor.value.isSuccess()).isTrue()
    val currentState = currentStateResultCaptor.value.getOrThrow()
    assertThat(currentState.stateTypeCase).isEqualTo(COMPLETED_STATE)
    assertThat(currentState.completedState.answerCount).isEqualTo(2)
    assertThat(currentState.completedState.getAnswer(0).userAnswer.answer.nonNegativeInt)
      .isEqualTo(0)
    assertThat(currentState.completedState.getAnswer(0).feedback.html).contains("Try again.")
    assertThat(currentState.completedState.getAnswer(1).userAnswer.answer.nonNegativeInt)
      .isEqualTo(2)
    assertThat(currentState.completedState.getAnswer(1).feedback.html).contains("Correct!")
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
    explorationDataController.startPlayingExploration(TEST_EXPLORATION_ID_2)

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
    playExploration(TEST_EXPLORATION_ID_2)

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
    playExploration(TEST_EXPLORATION_ID_2)
    submitPrototypeState1Answer()

    val moveToStateResult = explorationProgressController.moveToNextState()
    moveToStateResult.observeForever(mockAsyncResultLiveDataObserver)
    testCoroutineDispatchers.runCurrent()

    verify(mockAsyncResultLiveDataObserver, atLeastOnce()).onChanged(asyncResultCaptor.capture())
    assertThat(asyncResultCaptor.value.isSuccess()).isTrue()
  }

  @Test
  fun testMoveToNext_forCompletedState_movesToNextState() {
    val currentStateLiveData =
      explorationProgressController.getCurrentState().toLiveData()
    currentStateLiveData.observeForever(mockCurrentStateLiveDataObserver)
    playExploration(TEST_EXPLORATION_ID_2)
    submitPrototypeState1Answer()

    moveToNextState()

    verify(
      mockCurrentStateLiveDataObserver,
      atLeastOnce()
    ).onChanged(currentStateResultCaptor.capture())
    assertThat(currentStateResultCaptor.value.isSuccess()).isTrue()
    val currentState = currentStateResultCaptor.value.getOrThrow()
    assertThat(currentState.state.name).isEqualTo("Fractions")
    assertThat(currentState.stateTypeCase).isEqualTo(PENDING_STATE)
  }

  @Test
  fun testMoveToNext_afterMovingFromCompletedState_failsWithError() {
    subscribeToCurrentStateToAllowExplorationToLoad()
    playExploration(TEST_EXPLORATION_ID_2)
    submitPrototypeState1Answer()
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
    explorationDataController.startPlayingExploration(TEST_EXPLORATION_ID_2)

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
    playExploration(TEST_EXPLORATION_ID_2)

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
    playExploration(TEST_EXPLORATION_ID_2)
    submitPrototypeState1Answer()

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
    playExploration(TEST_EXPLORATION_ID_2)
    playThroughPrototypeState1AndMoveToNextState()

    val moveToStateResult =
      explorationProgressController.moveToPreviousState()
    moveToStateResult.observeForever(mockAsyncResultLiveDataObserver)
    testCoroutineDispatchers.runCurrent()

    // Verify that we can navigate to the previous state since the current state is complete and not
    // initial.
    verify(mockAsyncResultLiveDataObserver, atLeastOnce()).onChanged(asyncResultCaptor.capture())
    assertThat(asyncResultCaptor.value.isSuccess()).isTrue()
  }

  @Test
  fun testMoveToPrevious_forCompletedState_movesToPreviousState() {
    val currentStateLiveData =
      explorationProgressController.getCurrentState().toLiveData()
    currentStateLiveData.observeForever(mockCurrentStateLiveDataObserver)
    playExploration(TEST_EXPLORATION_ID_2)
    playThroughPrototypeState1AndMoveToNextState()

    moveToPreviousState()

    // Since the answer submission and forward navigation should work (see earlier tests), verify
    // that the move to the previous state does return us back to the initial exploration state
    // (which is now completed).
    verify(
      mockCurrentStateLiveDataObserver,
      atLeastOnce()
    ).onChanged(currentStateResultCaptor.capture())
    assertThat(currentStateResultCaptor.value.isSuccess()).isTrue()
    val currentState = currentStateResultCaptor.value.getOrThrow()
    assertThat(currentState.state.name).isEqualTo("Continue")
    assertThat(currentState.stateTypeCase).isEqualTo(COMPLETED_STATE)
  }

  @Test
  fun testMoveToPrevious_navigatedForwardThenBackToInitial_failsWithError() {
    subscribeToCurrentStateToAllowExplorationToLoad()
    playExploration(TEST_EXPLORATION_ID_2)
    playThroughPrototypeState1AndMoveToNextState()
    moveToPreviousState()

    val moveToStateResult =
      explorationProgressController.moveToPreviousState()
    moveToStateResult.observeForever(mockAsyncResultLiveDataObserver)
    testCoroutineDispatchers.runCurrent()

    // The first previous navigation should succeed (see above), but the second will fail since
    // we're back at the initial state.
    verify(mockAsyncResultLiveDataObserver, atLeastOnce()).onChanged(asyncResultCaptor.capture())
    assertThat(asyncResultCaptor.value.isFailure()).isTrue()
    assertThat(asyncResultCaptor.value.getErrorOrNull())
      .hasMessageThat()
      .contains("Cannot navigate to previous state; at initial state.")
  }

  @Test
  fun testSubmitAnswer_forTextInput_correctAnswer_returnsOutcomeWithTransition() {
    subscribeToCurrentStateToAllowExplorationToLoad()
    playExploration(TEST_EXPLORATION_ID_2)
    navigateToPrototypeTextInputState()

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
    assertThat(answerOutcome.feedback.html).contains("Correct!")
  }

  @Test
  fun testSubmitAnswer_forTextInput_wrongAnswer_returnsDefaultOutcome() {
    subscribeToCurrentStateToAllowExplorationToLoad()
    playExploration(TEST_EXPLORATION_ID_2)
    navigateToPrototypeTextInputState()

    val result =
      explorationProgressController.submitAnswer(createTextInputAnswer("Klingon"))
    result.observeForever(mockAsyncAnswerOutcomeObserver)
    testCoroutineDispatchers.runCurrent()

    // Verify that the answer was wrong, and that there's no handler for it so the default outcome
    // is returned.
    verify(
      mockAsyncAnswerOutcomeObserver,
      atLeastOnce()
    ).onChanged(asyncAnswerOutcomeCaptor.capture())
    val answerOutcome = asyncAnswerOutcomeCaptor.value.getOrThrow()
    assertThat(answerOutcome.destinationCase).isEqualTo(AnswerOutcome.DestinationCase.SAME_STATE)
    assertThat(answerOutcome.feedback.html).contains("Not quite.")
  }

  @Test
  fun testSubmitAnswer_forFractionInput_wrongAnswer_returnsDefaultOutcome_hasHint() {
    subscribeToCurrentStateToAllowExplorationToLoad()
    playExploration(TEST_EXPLORATION_ID_2)
    navigateToPrototypeFractionInputState()

    submitWrongAnswerForPrototypeState2()

    // Verify that the current state updates. It should stay pending, and the wrong answer should be
    // appended.
    verify(
      mockCurrentStateLiveDataObserver,
      atLeastOnce()
    ).onChanged(currentStateResultCaptor.capture())
    assertThat(currentStateResultCaptor.value.isSuccess()).isTrue()
    val currentState = currentStateResultCaptor.value.getOrThrow()
    assertThat(currentState.stateTypeCase).isEqualTo(PENDING_STATE)
    assertThat(currentState.pendingState.wrongAnswerCount).isEqualTo(1)
    val answerAndFeedback = currentState.pendingState.getWrongAnswer(0)
    assertThat(answerAndFeedback.userAnswer.answer.fraction.numerator).isEqualTo(1)
    assertThat(answerAndFeedback.userAnswer.answer.fraction.denominator).isEqualTo(3)
    assertThat(answerAndFeedback.feedback.html).contains("Try again.")
    val hintAndSolution = currentState.state.interaction.getHint(0)
    assertThat(hintAndSolution.hintContent.html).contains("Remember that two halves")
  }

  @Test
  fun testRevealHint_forWrongAnswer_showHint_returnHintIsRevealed() {
    subscribeToCurrentStateToAllowExplorationToLoad()
    playExploration(TEST_EXPLORATION_ID_2)
    navigateToPrototypeFractionInputState()
    submitWrongAnswerForPrototypeState2()

    // Verify that the current state updates. It should stay pending, on submission of wrong answer.
    verify(
      mockCurrentStateLiveDataObserver,
      atLeastOnce()
    ).onChanged(currentStateResultCaptor.capture())
    assertThat(currentStateResultCaptor.value.isSuccess()).isTrue()
    val currentState = currentStateResultCaptor.value.getOrThrow()

    val result = explorationProgressController.submitHintIsRevealed(
      state = currentState.state,
      hintIsRevealed = true,
      hintIndex = 0,
    )
    result.observeForever(mockAsyncHintObserver)
    testCoroutineDispatchers.runCurrent()

    assertThat(currentState.stateTypeCase).isEqualTo(PENDING_STATE)

    val hintAndSolution = currentState.state.interaction.getHint(0)
    assertThat(hintAndSolution.hintContent.html).contains("Remember that two halves")

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
    playExploration(TEST_EXPLORATION_ID_2)
    navigateToPrototypeFractionInputState()
    submitWrongAnswerForPrototypeState2()

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
    playExploration(TEST_EXPLORATION_ID_2)
    navigateToPrototypeFractionInputState()

    submitWrongAnswerForPrototypeState2()

    // Verify that the current state updates. It should stay pending, and the wrong answer should be
    // appended.
    verify(
      mockCurrentStateLiveDataObserver,
      atLeastOnce()
    ).onChanged(currentStateResultCaptor.capture())
    assertThat(currentStateResultCaptor.value.isSuccess()).isTrue()
    val currentState = currentStateResultCaptor.value.getOrThrow()
    assertThat(currentState.stateTypeCase).isEqualTo(PENDING_STATE)
    assertThat(currentState.pendingState.wrongAnswerCount).isEqualTo(1)

    val hint = currentState.state.interaction.getHint(0)
    assertThat(hint.hintContent.html).contains("Remember that two halves")

    val solution = currentState.state.interaction.solution
    assertThat(solution.correctAnswer.numerator).isEqualTo(1)
    assertThat(solution.correctAnswer.denominator).isEqualTo(2)
    assertThat(solution.explanation.html)
      .contains("Half of something has one part in the numerator for every two parts")
  }

  @Test
  fun testGetCurrentState_secondState_submitRightAnswer_pendingStateBecomesCompleted() {
    subscribeToCurrentStateToAllowExplorationToLoad()
    playExploration(TEST_EXPLORATION_ID_2)
    navigateToPrototypeTextInputState()

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
    assertThat(answerAndFeedback.feedback.html).contains("Correct!")
  }

  @Test
  fun testSubmitAnswer_forTextInput_withSpaces_updatesStateWithVerbatimAnswer() {
    subscribeToCurrentStateToAllowExplorationToLoad()
    playExploration(TEST_EXPLORATION_ID_2)
    navigateToPrototypeTextInputState()

    val result =
      explorationProgressController.submitAnswer(createTextInputAnswer("Finnish  "))
    result.observeForever(mockAsyncAnswerOutcomeObserver)
    testCoroutineDispatchers.runCurrent()

    // Verify that the current state updates. The submitted answer should have a textual version
    // that is a verbatim version of the user-submitted answer.
    verify(
      mockCurrentStateLiveDataObserver,
      atLeastOnce()
    ).onChanged(currentStateResultCaptor.capture())
    assertThat(currentStateResultCaptor.value.isSuccess()).isTrue()
    val currentState = currentStateResultCaptor.value.getOrThrow()
    assertThat(currentState.stateTypeCase).isEqualTo(COMPLETED_STATE)
    assertThat(currentState.completedState.answerCount).isEqualTo(1)
    val answerAndFeedback = currentState.completedState.getAnswer(0)
    assertThat(answerAndFeedback.userAnswer.textualAnswerCase)
      .isEqualTo(UserAnswer.TextualAnswerCase.PLAIN_ANSWER)
    assertThat(answerAndFeedback.userAnswer.plainAnswer).isEqualTo("Finnish  ")
  }

  @Test
  fun testGetCurrentState_eighthState_submitWrongAnswer_updatePendingState() {
    subscribeToCurrentStateToAllowExplorationToLoad()
    playExploration(TEST_EXPLORATION_ID_2)
    navigateToPrototypeTextInputState()

    val result =
      explorationProgressController.submitAnswer(createTextInputAnswer("Klingon"))
    result.observeForever(mockAsyncAnswerOutcomeObserver)
    testCoroutineDispatchers.runCurrent()

    // Verify that the current state updates. It should stay pending, and the wrong answer should be
    // appended.
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
    assertThat(answerAndFeedback.feedback.html).contains("Not quite.")
  }

  @Test
  fun testGetCurrentState_afterMovePreviousAndNext_returnsCurrentState() {
    val currentStateLiveData =
      explorationProgressController.getCurrentState().toLiveData()
    currentStateLiveData.observeForever(mockCurrentStateLiveDataObserver)
    playExploration(TEST_EXPLORATION_ID_2)
    playThroughPrototypeState1AndMoveToNextState()

    moveToPreviousState()
    moveToNextState()

    // The current state should stay the same.
    verify(
      mockCurrentStateLiveDataObserver,
      atLeastOnce()
    ).onChanged(currentStateResultCaptor.capture())
    assertThat(currentStateResultCaptor.value.isSuccess()).isTrue()
    val currentState = currentStateResultCaptor.value.getOrThrow()
    assertThat(currentState.state.name).isEqualTo("Fractions")
    assertThat(currentState.stateTypeCase).isEqualTo(PENDING_STATE)
  }

  @Test
  fun testGetCurrentState_afterMoveNextAndPrevious_returnsCurrentState() {
    val currentStateLiveData =
      explorationProgressController.getCurrentState().toLiveData()
    currentStateLiveData.observeForever(mockCurrentStateLiveDataObserver)
    playExploration(TEST_EXPLORATION_ID_2)
    playThroughPrototypeState1AndMoveToNextState()
    submitPrototypeState2Answer() // Submit the answer but do not proceed to the next state.

    moveToNextState()
    moveToPreviousState()

    // The current state should stay the same.
    verify(
      mockCurrentStateLiveDataObserver,
      atLeastOnce()
    ).onChanged(currentStateResultCaptor.capture())
    assertThat(currentStateResultCaptor.value.isSuccess()).isTrue()
    val currentState = currentStateResultCaptor.value.getOrThrow()
    assertThat(currentState.state.name).isEqualTo("Fractions")
    assertThat(currentState.stateTypeCase).isEqualTo(COMPLETED_STATE)
  }

  @Test
  fun testGetCurrentState_afterMoveToPrev_onThirdState_newObserver_receivesCompletedSecondState() {
    subscribeToCurrentStateToAllowExplorationToLoad()
    playExploration(TEST_EXPLORATION_ID_2)
    playThroughPrototypeState1AndMoveToNextState()
    playThroughPrototypeState2AndMoveToNextState()

    // Move to the previous state and register a new observer.
    moveToPreviousState() // Third state -> second
    val currentStateLiveData =
      explorationProgressController.getCurrentState().toLiveData()
    currentStateLiveData.observeForever(mockCurrentStateLiveDataObserver2)
    testCoroutineDispatchers.runCurrent()

    // The new observer should observe the completed second state since it's the current pending
    // state.
    verify(
      mockCurrentStateLiveDataObserver2,
      atLeastOnce()
    ).onChanged(currentStateResultCaptor.capture())
    assertThat(currentStateResultCaptor.value.isSuccess()).isTrue()
    val currentState = currentStateResultCaptor.value.getOrThrow()
    assertThat(currentState.state.name).isEqualTo("Fractions")
    assertThat(currentState.stateTypeCase).isEqualTo(COMPLETED_STATE)
  }

  @Test
  fun testGetCurrentState_forFirstState_doesNotHaveNextState() {
    val currentStateLiveData =
      explorationProgressController.getCurrentState().toLiveData()
    currentStateLiveData.observeForever(mockCurrentStateLiveDataObserver)

    playExploration(TEST_EXPLORATION_ID_2)

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
      explorationProgressController.getCurrentState().toLiveData()
    currentStateLiveData.observeForever(mockCurrentStateLiveDataObserver)
    playExploration(TEST_EXPLORATION_ID_2)

    submitPrototypeState1Answer()

    // Simply completing the current state should not result in there being a next state since the
    // user hasn't proceeded to the following state, yet.
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
      explorationProgressController.getCurrentState().toLiveData()
    currentStateLiveData.observeForever(mockCurrentStateLiveDataObserver)
    playExploration(TEST_EXPLORATION_ID_2)

    playThroughPrototypeState1AndMoveToNextState()

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
      explorationProgressController.getCurrentState().toLiveData()
    currentStateLiveData.observeForever(mockCurrentStateLiveDataObserver)
    playExploration(TEST_EXPLORATION_ID_2)
    playThroughPrototypeState1AndMoveToNextState()

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
      explorationProgressController.getCurrentState().toLiveData()
    currentStateLiveData.observeForever(mockCurrentStateLiveDataObserver)
    playExploration(TEST_EXPLORATION_ID_2)
    playThroughPrototypeState1AndMoveToNextState()

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
    playExploration(TEST_EXPLORATION_ID_2)
    navigateToPrototypeNumericInputState()

    val result = explorationProgressController.submitAnswer(createNumericInputAnswer(121.0))
    result.observeForever(mockAsyncAnswerOutcomeObserver)
    testCoroutineDispatchers.runCurrent()

    // Verify that the answer submission was successful.
    verify(
      mockAsyncAnswerOutcomeObserver,
      atLeastOnce()
    ).onChanged(asyncAnswerOutcomeCaptor.capture())
    val answerOutcome = asyncAnswerOutcomeCaptor.value.getOrThrow()
    assertThat(answerOutcome.destinationCase).isEqualTo(AnswerOutcome.DestinationCase.STATE_NAME)
    assertThat(answerOutcome.feedback.html).contains("Correct!")
  }

  @Test
  fun testSubmitAnswer_forNumericInput_wrongAnswer_returnsOutcomeWithTransition() {
    subscribeToCurrentStateToAllowExplorationToLoad()
    playExploration(TEST_EXPLORATION_ID_2)
    navigateToPrototypeNumericInputState()

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
    assertThat(answerOutcome.feedback.html).contains("It's less than that.")
  }

  @Test
  fun testSubmitAnswer_forContinue_returnsOutcomeWithTransition() {
    subscribeToCurrentStateToAllowExplorationToLoad()
    playExploration(TEST_EXPLORATION_ID_2)
    // The first state of the exploration is the Continue interaction.

    val result = explorationProgressController.submitAnswer(createContinueButtonAnswer())
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
  fun testGetCurrentState_eleventhState_isTerminalState() {
    val currentStateLiveData =
      explorationProgressController.getCurrentState().toLiveData()
    currentStateLiveData.observeForever(mockCurrentStateLiveDataObserver)
    playExploration(TEST_EXPLORATION_ID_2)

    playThroughPrototypeExploration()

    // Verify that the last state is terminal.
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
      explorationProgressController.getCurrentState().toLiveData()
    currentStateLiveData.observeForever(mockCurrentStateLiveDataObserver)
    playExploration(TEST_EXPLORATION_ID_2)
    playThroughPrototypeState1AndMoveToNextState()
    playThroughPrototypeState2AndMoveToNextState()

    moveToPreviousState()

    // Verify that the current state is the second state, and is completed. It should also have the
    // previously submitted answer, allowing learners to potentially view past answers.
    verify(
      mockCurrentStateLiveDataObserver,
      atLeastOnce()
    ).onChanged(currentStateResultCaptor.capture())
    assertThat(currentStateResultCaptor.value.isSuccess()).isTrue()
    val currentState = currentStateResultCaptor.value.getOrThrow()
    assertThat(currentState.stateTypeCase).isEqualTo(COMPLETED_STATE)
    assertThat(currentState.state.name).isEqualTo("Fractions")
    assertThat(currentState.completedState.getAnswer(0).userAnswer.answer.fraction)
      .isEqualTo(
        Fraction.newBuilder().apply {
          numerator = 1
          denominator = 2
        }.build()
      )
  }

  @Test
  fun testMoveToNext_onFinalState_failsWithError() {
    subscribeToCurrentStateToAllowExplorationToLoad()
    playExploration(TEST_EXPLORATION_ID_2)
    playThroughPrototypeExploration()

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
      explorationProgressController.getCurrentState().toLiveData()
    currentStateLiveData.observeForever(mockCurrentStateLiveDataObserver)

    playExploration(TEST_EXPLORATION_ID_5)
    submitImageRegionAnswer(clickX = 0.5f, clickY = 0.5f, clickedRegion = "Saturn")
    moveToNextState()

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
      explorationProgressController.getCurrentState().toLiveData()
    currentStateLiveData.observeForever(mockCurrentStateLiveDataObserver)

    // Click on Jupiter before Saturn to take a slightly different (valid) path through the
    // exploration. (Note that this does not include actual branching).
    playExploration(TEST_EXPLORATION_ID_5)
    submitImageRegionAnswer(clickX = 0.2f, clickY = 0.5f, clickedRegion = "Jupiter")
    submitImageRegionAnswer(clickX = 0.5f, clickY = 0.5f, clickedRegion = "Saturn")
    moveToNextState()

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
      explorationProgressController.getCurrentState().toLiveData()
    currentStateLiveData.observeForever(mockCurrentStateLiveDataObserver)
    playExploration(TEST_EXPLORATION_ID_2)
    playThroughPrototypeExploration()
    endExploration()

    playExploration(TEST_EXPLORATION_ID_5)
    submitImageRegionAnswer(clickX = 0.2f, clickY = 0.5f, clickedRegion = "Jupiter")

    // Verify that we're on the second-to-last state of the second exploration.
    verify(
      mockCurrentStateLiveDataObserver,
      atLeastOnce()
    ).onChanged(currentStateResultCaptor.capture())
    assertThat(currentStateResultCaptor.value.isSuccess()).isTrue()
    val currentState = currentStateResultCaptor.value.getOrThrow()
    assertThat(currentState.stateTypeCase).isEqualTo(PENDING_STATE)
    // This state is not in the other test exp.
    assertThat(currentState.state.name).isEqualTo("ImageClickInput")
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
    playExploration(TEST_EXPLORATION_ID_2)
    playThroughPrototypeState1AndMoveToNextState()
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
      explorationProgressController.getCurrentState().toLiveData()
    currentStateLiveData.observeForever(mockCurrentStateLiveDataObserver)

    playExploration("invalid_exp_id")
    val exception = fakeExceptionLogger.getMostRecentException()

    assertThat(exception).isInstanceOf(FileNotFoundException::class.java)
    assertThat(exception).hasMessageThat().contains("invalid_exp_id")
  }

  @Test
  fun testSaveExplorationCheckpoint_savesCheckpointWithoutError() {
    subscribeToCurrentStateToAllowExplorationToLoad()
    playExploration(TEST_EXPLORATION_ID_2)
    playThroughPrototypeState1AndMoveToNextState()
    val saveCheckpointLiveData =
      explorationProgressController.saveExplorationCheckpoint(profileId).toLiveData()
    verifyOperationSucceeds(saveCheckpointLiveData)
  }

  @Test
  fun testProcessSaveCheckpointResult_saveCheckpoint_correctCheckpointState() {
    subscribeToCurrentStateToAllowExplorationToLoad()
    playExploration(TEST_EXPLORATION_ID_2)
    playThroughPrototypeState1AndMoveToNextState()
    val saveCheckpointLiveData =
      explorationProgressController.saveExplorationCheckpoint(profileId).toLiveData()
    val checkpointState = processSaveCheckpointResult(saveCheckpointLiveData)
    assertThat(checkpointState).isEqualTo(
      ExplorationCheckpointState.CHECKPOINT_SAVED_DATABASE_NOT_EXCEEDED_LIMIT
    )
  }

  @Test
  fun testProcessSaveCheckpointResult_saveMultipleCheckpoints_correctCheckpointState() {
    subscribeToCurrentStateToAllowExplorationToLoad()
    playExploration(TEST_EXPLORATION_ID_2)
    playThroughPrototypeState1AndMoveToNextState()
    var saveCheckpointLiveData =
      explorationProgressController.saveExplorationCheckpoint(profileId).toLiveData()
    var checkpointState = processSaveCheckpointResult(saveCheckpointLiveData)
    assertThat(checkpointState).isEqualTo(
      ExplorationCheckpointState.CHECKPOINT_SAVED_DATABASE_NOT_EXCEEDED_LIMIT
    )
    testCoroutineDispatchers.runCurrent()
    playThroughPrototypeState2AndMoveToNextState()
    saveCheckpointLiveData =
      explorationProgressController.saveExplorationCheckpoint(profileId).toLiveData()
    checkpointState = processSaveCheckpointResult(saveCheckpointLiveData)
    assertThat(checkpointState).isEqualTo(
      ExplorationCheckpointState.CHECKPOINT_SAVED_DATABASE_EXCEEDED_LIMIT
    )
  }

  @Test
  fun testFinishExplosionWithCheckpointing_progressSaved_databaseLimitNotExceeded_isSuccessful() {
    subscribeToCurrentStateToAllowExplorationToLoad()
    playExploration(TEST_EXPLORATION_ID_2)
    playThroughPrototypeState1AndMoveToNextState()
    val saveCheckpointLiveData =
      explorationProgressController.saveExplorationCheckpoint(profileId).toLiveData()
    processSaveCheckpointResult(saveCheckpointLiveData)
    testCoroutineDispatchers.runCurrent()
    val finishExplorationWithCheckpointingLiveData =
      explorationProgressController.checkCheckpointStateToExitExploration()
    verifyOperationSucceeds(finishExplorationWithCheckpointingLiveData)
  }

  @Test
  fun testCheckCheckpointStateToExitExploration_databaseLimitExceeded_isFailureWithException() {
    subscribeToCurrentStateToAllowExplorationToLoad()
    playExploration(TEST_EXPLORATION_ID_2)
    playThroughPrototypeState1AndMoveToNextState()

    var saveCheckpointLiveData =
      explorationProgressController.saveExplorationCheckpoint(profileId).toLiveData()
    processSaveCheckpointResult(saveCheckpointLiveData)

    playThroughPrototypeState2AndMoveToNextState()
    saveCheckpointLiveData =
      explorationProgressController.saveExplorationCheckpoint(profileId).toLiveData()
    processSaveCheckpointResult(saveCheckpointLiveData)
    testCoroutineDispatchers.runCurrent()

    val checkCheckpointStateToExitExploration =
      explorationProgressController.checkCheckpointStateToExitExploration()

    verifyOperationFails(checkCheckpointStateToExitExploration)

    assertThat(asyncResultCaptor.value.getErrorOrNull()).isInstanceOf(
      ExplorationProgressController.CheckpointDatabaseOverflowException::class.java
    )
  }

  @Test
  fun testCheckCheckpointStateToExitExploration_unsavedProgress_isFailureWithException() {
    subscribeToCurrentStateToAllowExplorationToLoad()
    playExploration(TEST_EXPLORATION_ID_2)
    playThroughPrototypeState1AndMoveToNextState()

    // Every exploration is marked as unsaved until a save operation changes the checkpoint state to
    // some other state.

    val checkpointStateToExitExplorationLiveData =
      explorationProgressController.checkCheckpointStateToExitExploration()

    verifyOperationFails(checkpointStateToExitExplorationLiveData)

    assertThat(asyncResultCaptor.value.getErrorOrNull()).isInstanceOf(
      ExplorationProgressController.ProgressNotSavedException::class.java
    )
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  /**
   * Creates a blank subscription to the current state to ensure that requests to load the
   * exploration complete, otherwise post-load operations may fail. An observer is required since
   * the current mediator live data implementation will only lazily load data based on whether
   * there's an active subscription.
   */
  private fun subscribeToCurrentStateToAllowExplorationToLoad() {
    explorationProgressController.getCurrentState()
      .toLiveData()
      .observeForever(mockCurrentStateLiveDataObserver)
  }

  private fun playExploration(explorationId: String) {
    verifyOperationSucceeds(explorationDataController.startPlayingExploration(explorationId))
  }

  private fun submitContinueButtonAnswer() {
    verifyOperationSucceeds(
      explorationProgressController.submitAnswer(createContinueButtonAnswer())
    )
  }

  private fun submitFractionAnswer(fraction: Fraction) {
    verifyOperationSucceeds(
      explorationProgressController.submitAnswer(createFractionAnswer(fraction))
    )
  }

  private fun submitMultipleChoiceAnswer(choiceIndex: Int) {
    verifyOperationSucceeds(
      explorationProgressController.submitAnswer(createMultipleChoiceAnswer(choiceIndex))
    )
  }

  private fun submitItemSelectionAnswer(vararg contentIds: String) {
    verifyOperationSucceeds(
      explorationProgressController.submitAnswer(createItemSelectionAnswer(contentIds.toList()))
    )
  }

  private fun submitNumericInputAnswer(numericAnswer: Double) {
    verifyOperationSucceeds(
      explorationProgressController.submitAnswer(createNumericInputAnswer(numericAnswer))
    )
  }

  private fun submitRatioInputAnswer(ratioExpression: RatioExpression) {
    verifyOperationSucceeds(
      explorationProgressController.submitAnswer(createRatioInputAnswer(ratioExpression))
    )
  }

  private fun submitTextInputAnswer(@Suppress("SameParameterValue") textAnswer: String) {
    verifyOperationSucceeds(
      explorationProgressController.submitAnswer(createTextInputAnswer(textAnswer))
    )
  }

  private fun submitDragAndDropAnswer(vararg selectedChoicesLists: List<String>) {
    verifyOperationSucceeds(
      explorationProgressController.submitAnswer(
        createDragAndDropAnswer(selectedChoicesLists.toList())
      )
    )
  }

  private fun submitImageRegionAnswer(clickX: Float, clickY: Float, clickedRegion: String) {
    verifyOperationSucceeds(
      explorationProgressController.submitAnswer(
        createImageRegionAnswer(clickX, clickY, clickedRegion)
      )
    )
  }

  private fun playThroughPrototypeExploration() {
    playThroughPrototypeState1AndMoveToNextState()
    playThroughPrototypeState2AndMoveToNextState()
    playThroughPrototypeState3AndMoveToNextState()
    playThroughPrototypeState4AndMoveToNextState()
    playThroughPrototypeState5AndMoveToNextState()
    playThroughPrototypeState6AndMoveToNextState()
    playThroughPrototypeState7AndMoveToNextState()
    playThroughPrototypeState8AndMoveToNextState()
    playThroughPrototypeState9AndMoveToNextState()
    playThroughPrototypeState10AndMoveToNextState()
  }

  private fun navigateToPrototypeFractionInputState() {
    // Fraction input is the second state of the exploration.
    playThroughPrototypeState1AndMoveToNextState()
  }

  private fun navigateToPrototypeMultipleChoiceState() {
    // Multiple choice is the third state of the exploration.
    playThroughPrototypeState1AndMoveToNextState()
    playThroughPrototypeState2AndMoveToNextState()
  }

  private fun navigateToPrototypeNumericInputState() {
    // Numeric input is the sixth state of the exploration.
    playThroughPrototypeState1AndMoveToNextState()
    playThroughPrototypeState2AndMoveToNextState()
    playThroughPrototypeState3AndMoveToNextState()
    playThroughPrototypeState4AndMoveToNextState()
    playThroughPrototypeState5AndMoveToNextState()
  }

  private fun navigateToPrototypeTextInputState() {
    // Text input is the eighth state of the exploration.
    playThroughPrototypeState1AndMoveToNextState()
    playThroughPrototypeState2AndMoveToNextState()
    playThroughPrototypeState3AndMoveToNextState()
    playThroughPrototypeState4AndMoveToNextState()
    playThroughPrototypeState5AndMoveToNextState()
    playThroughPrototypeState6AndMoveToNextState()
    playThroughPrototypeState7AndMoveToNextState()
  }

  private fun submitPrototypeState1Answer() {
    // First state: Continue interaction.
    submitContinueButtonAnswer()
  }

  private fun submitPrototypeState2Answer() {
    // Second state: Fraction input. Correct answer: 1/2.
    submitFractionAnswer(
      Fraction.newBuilder().apply {
        numerator = 1
        denominator = 2
      }.build()
    )
  }

  private fun submitWrongAnswerForPrototypeState2() {
    submitFractionAnswer(
      Fraction.newBuilder().apply {
        numerator = 1
        denominator = 3
      }.build()
    )
  }

  private fun submitPrototypeState3Answer() {
    // Third state: Multiple choice. Correct answer: Eagle (second third choice).
    submitMultipleChoiceAnswer(choiceIndex = 2)
  }

  private fun submitPrototypeState4Answer() {
    // Fourth state: Item selection (radio buttons). Correct answer: Green (first choice).
    submitItemSelectionAnswer("ca_choices_0")
  }

  private fun submitPrototypeState5Answer() {
    // Fifth state: Item selection (checkboxes). Correct answer: {Red, Green, Blue}.
    submitItemSelectionAnswer("ca_choices_0", "ca_choices_3", "ca_choices_2")
  }

  private fun submitPrototypeState6Answer() {
    // Sixth state: Numeric input. Correct answer: 121.
    submitNumericInputAnswer(121.0)
  }

  private fun submitPrototypeState7Answer() {
    // Seventh state: Ratio input. Correct answer: 4:5.
    submitRatioInputAnswer(
      RatioExpression.newBuilder().apply {
        addAllRatioComponent(listOf(4, 5))
      }.build()
    )
  }

  private fun submitPrototypeState8Answer() {
    // Eighth state: Text input. Correct answer: finnish.
    submitTextInputAnswer("finnish")
  }

  private fun submitPrototypeState9Answer() {
    // Ninth state: Drag Drop Sort. Initial configuration: ca_choices_0, ca_choices_1, ca_choices_2,
    // ca_choices_3. Correct answer: Move 1st item to 4th position.
    submitDragAndDropAnswer(
      listOf("ca_choices_1"),
      listOf("ca_choices_2"),
      listOf("ca_choices_3"),
      listOf("ca_choices_0"),
    )
  }

  private fun submitPrototypeState10Answer() {
    // Tenth state: Drag Drop Sort. Initial configuration: ca_choices_0, ca_choices_1, ca_choices_2,
    // ca_choices_3. Correct answer: Move 1st item to 4th position. Correct answer: Merge first two
    // then move 2nd item to 3rd position.
    submitDragAndDropAnswer(
      listOf("ca_choices_0", "ca_choices_1"),
      listOf("ca_choices_3"),
      listOf("ca_choices_2"),
    )
  }

  private fun playThroughPrototypeState1AndMoveToNextState() {
    submitPrototypeState1Answer()
    moveToNextState()
  }

  private fun playThroughPrototypeState2AndMoveToNextState() {
    submitPrototypeState2Answer()
    moveToNextState()
  }

  private fun playThroughPrototypeState3AndMoveToNextState() {
    submitPrototypeState3Answer()
    moveToNextState()
  }

  private fun playThroughPrototypeState4AndMoveToNextState() {
    submitPrototypeState4Answer()
    moveToNextState()
  }

  private fun playThroughPrototypeState5AndMoveToNextState() {
    submitPrototypeState5Answer()
    moveToNextState()
  }

  private fun playThroughPrototypeState6AndMoveToNextState() {
    submitPrototypeState6Answer()
    moveToNextState()
  }

  private fun playThroughPrototypeState7AndMoveToNextState() {
    submitPrototypeState7Answer()
    moveToNextState()
  }

  private fun playThroughPrototypeState8AndMoveToNextState() {
    submitPrototypeState8Answer()
    moveToNextState()
  }

  private fun playThroughPrototypeState9AndMoveToNextState() {
    submitPrototypeState9Answer()
    moveToNextState()
  }

  private fun playThroughPrototypeState10AndMoveToNextState() {
    submitPrototypeState10Answer()
    moveToNextState()
  }

  private fun moveToNextState() {
    verifyOperationSucceeds(explorationProgressController.moveToNextState())
  }

  private fun moveToPreviousState() {
    verifyOperationSucceeds(explorationProgressController.moveToPreviousState())
  }

  private fun endExploration() {
    verifyOperationSucceeds(explorationDataController.stopPlayingExploration())
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

  /**
   * Verifies that the specified live data provides at least one successful operation. This will
   * change test-wide mock state, and synchronizes background execution.
   */
  private fun <T : Any?> verifyOperationSucceeds(liveData: LiveData<AsyncResult<T>>) {
    reset(mockAsyncResultLiveDataObserver)
    liveData.observeForever(mockAsyncResultLiveDataObserver)
    testCoroutineDispatchers.runCurrent()
    verify(mockAsyncResultLiveDataObserver).onChanged(asyncResultCaptor.capture())
    asyncResultCaptor.value.apply {
      // This bit of conditional logic is used to add better error reporting when failures occur.
      if (isFailure()) {
        throw AssertionError("Operation failed", getErrorOrNull())
      }
      assertThat(isSuccess()).isTrue()
    }
    reset(mockAsyncResultLiveDataObserver)
  }

  /**
   * Verifies that the specified live data provides a failure result. This will change test-wide
   * mock state, and synchronizes background execution.
   */
  private fun <T : Any?> verifyOperationFails(liveData: LiveData<AsyncResult<T>>) {
    reset(mockAsyncResultLiveDataObserver)
    liveData.observeForever(mockAsyncResultLiveDataObserver)
    testCoroutineDispatchers.runCurrent()
    verify(mockAsyncResultLiveDataObserver).onChanged(asyncResultCaptor.capture())
    asyncResultCaptor.value.apply {
      // This bit of conditional logic is used to add better error reporting when failures occur.
      assertThat(isFailure()).isTrue()
    }
    reset(mockAsyncResultLiveDataObserver)
  }

  /**
   * updates the checkpoint state for an exploration depending upon the result of the last save
   * operation.
   *
   * @return the [ExplorationCheckpointState] to which the latest checkpoint state has transitioned.
   */
  private fun <T : Any?> processSaveCheckpointResult(
    liveData: LiveData<AsyncResult<T>>
  ): ExplorationCheckpointState {
    reset(mockAsyncResultLiveDataObserver)
    liveData.observeForever(mockAsyncResultLiveDataObserver)
    testCoroutineDispatchers.runCurrent()
    verify(mockAsyncResultLiveDataObserver).onChanged(asyncResultCaptor.capture())
    asyncResultCaptor.value.apply {
      // This bit of conditional logic is used to add better error reporting when failures occur.
      if (isFailure()) {
        throw AssertionError("Operation failed", getErrorOrNull())
      }
      assertThat(isSuccess()).isTrue()
    }
    reset(mockAsyncResultLiveDataObserver)
    val checkpointState = asyncResultCaptor.value.getOrThrow() as ExplorationCheckpointState
    explorationProgressController.processSaveCheckpointResult(
      profileId,
      UPCOMING_TOPIC_ID_1,
      TEST_STORY_ID_2,
      TEST_EXPLORATION_ID_2,
      oppiaClock.getCurrentTimeMs(),
      checkpointState
    )
    return checkpointState
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
      TestExplorationStorageModule::class
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
}
