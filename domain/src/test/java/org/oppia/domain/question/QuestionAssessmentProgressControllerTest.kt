package org.oppia.domain.question

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
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.atLeastOnce
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import org.oppia.app.model.AnswerOutcome
import org.oppia.app.model.AnsweredQuestionOutcome
import org.oppia.app.model.EphemeralQuestion
import org.oppia.app.model.EphemeralState.StateTypeCase.COMPLETED_STATE
import org.oppia.app.model.EphemeralState.StateTypeCase.PENDING_STATE
import org.oppia.app.model.EphemeralState.StateTypeCase.TERMINAL_STATE
import org.oppia.app.model.InteractionObject
import org.oppia.domain.classify.InteractionsModule
import org.oppia.domain.classify.rules.continueinteraction.ContinueModule
import org.oppia.domain.classify.rules.fractioninput.FractionInputModule
import org.oppia.domain.classify.rules.itemselectioninput.ItemSelectionInputModule
import org.oppia.domain.classify.rules.multiplechoiceinput.MultipleChoiceInputModule
import org.oppia.domain.classify.rules.numberwithunits.NumberWithUnitsRuleModule
import org.oppia.domain.classify.rules.numericinput.NumericInputRuleModule
import org.oppia.domain.classify.rules.textinput.TextInputRuleModule
import org.oppia.domain.topic.TEST_SKILL_ID_0
import org.oppia.domain.topic.TEST_SKILL_ID_1
import org.oppia.domain.topic.TEST_SKILL_ID_2
import org.oppia.util.data.AsyncResult
import org.oppia.util.logging.EnableConsoleLog
import org.oppia.util.logging.EnableFileLog
import org.oppia.util.logging.GlobalLogLevel
import org.oppia.util.logging.LogLevel
import org.oppia.util.threading.BackgroundDispatcher
import org.oppia.util.threading.BlockingDispatcher
import org.robolectric.annotation.Config
import javax.inject.Inject
import javax.inject.Qualifier
import javax.inject.Singleton
import kotlin.coroutines.EmptyCoroutineContext

/** Tests for [QuestionAssessmentProgressController]. */
@RunWith(AndroidJUnit4::class)
@Config(manifest = Config.NONE)
class QuestionAssessmentProgressControllerTest {
  private val TEST_SKILL_ID_LIST_012 = listOf(TEST_SKILL_ID_0, TEST_SKILL_ID_1, TEST_SKILL_ID_2) // questions 0, 2, 3
  private val TEST_SKILL_ID_LIST_02 = listOf(TEST_SKILL_ID_0, TEST_SKILL_ID_2) // questions 2, 1, 5
  private val TEST_SKILL_ID_LIST_01 = listOf(TEST_SKILL_ID_0, TEST_SKILL_ID_1) // questions 2, 0, 3
  private val TEST_SKILL_ID_LIST_2 = listOf(TEST_SKILL_ID_2) // questions 4, 5, 2

  // TODO: add tests for multiple sessions (& verify that reverting the fix for reinitializing the question data
  //  provider breaks as expected).

  @Rule
  @JvmField
  val mockitoRule: MockitoRule = MockitoJUnit.rule()

  @Inject lateinit var questionTrainingController: QuestionTrainingController

  @Inject lateinit var questionAssessmentProgressController: QuestionAssessmentProgressController

  @ExperimentalCoroutinesApi
  @Inject
  @field:TestDispatcher
  lateinit var testDispatcher: TestCoroutineDispatcher

  @Mock
  lateinit var mockCurrentQuestionLiveDataObserver: Observer<AsyncResult<EphemeralQuestion>>

  @Mock
  lateinit var mockCurrentQuestionLiveDataObserver2: Observer<AsyncResult<EphemeralQuestion>>

  @Mock
  lateinit var mockAsyncResultLiveDataObserver: Observer<AsyncResult<Any?>>

  @Mock
  lateinit var mockAsyncAnswerOutcomeObserver: Observer<AsyncResult<AnsweredQuestionOutcome>>

  @Captor
  lateinit var currentQuestionResultCaptor: ArgumentCaptor<AsyncResult<EphemeralQuestion>>

  @Captor
  lateinit var asyncResultCaptor: ArgumentCaptor<AsyncResult<Any?>>

  @Captor
  lateinit var asyncAnswerOutcomeCaptor: ArgumentCaptor<AsyncResult<AnsweredQuestionOutcome>>

  @ExperimentalCoroutinesApi
  private val coroutineContext by lazy {
    EmptyCoroutineContext + testDispatcher
  }

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testGetCurrentQuestion_noSessionStarted_returnsPendingResult() = runBlockingTest(coroutineContext) {
    val resultLiveData = questionAssessmentProgressController.getCurrentQuestion()
    resultLiveData.observeForever(mockCurrentQuestionLiveDataObserver)
    advanceUntilIdle()

    verify(mockCurrentQuestionLiveDataObserver).onChanged(currentQuestionResultCaptor.capture())
    assertThat(currentQuestionResultCaptor.value.isPending()).isTrue()
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testGetCurrentQuestion_sessionStarted_withEmptyQuestionList_fails() = runBlockingTest(coroutineContext) {
    questionTrainingController.startQuestionTrainingSession(listOf())

    val resultLiveData = questionAssessmentProgressController.getCurrentQuestion()
    resultLiveData.observeForever(mockCurrentQuestionLiveDataObserver)
    advanceUntilIdle()

    verify(mockCurrentQuestionLiveDataObserver, atLeastOnce()).onChanged(currentQuestionResultCaptor.capture())
    assertThat(currentQuestionResultCaptor.value.isFailure()).isTrue()
    assertThat(currentQuestionResultCaptor.value.getErrorOrNull())
      .hasMessageThat()
      .contains("Cannot start a training session with zero questions.")
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testStartTrainingSession_succeeds() = runBlockingTest(coroutineContext) {
    val resultLiveData = questionTrainingController.startQuestionTrainingSession(TEST_SKILL_ID_LIST_012)
    resultLiveData.observeForever(mockAsyncResultLiveDataObserver)
    advanceUntilIdle()

    verify(mockAsyncResultLiveDataObserver).onChanged(asyncResultCaptor.capture())
    assertThat(asyncResultCaptor.value.isSuccess()).isTrue()
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testGetCurrentQuestion_playSession_returnsPendingResultFromLoadingSession() = runBlockingTest(
    coroutineContext
  ) {
    val currentQuestionLiveData = questionAssessmentProgressController.getCurrentQuestion()
    currentQuestionLiveData.observeForever(mockCurrentQuestionLiveDataObserver)
    advanceUntilIdle()

    startTrainingSession(TEST_SKILL_ID_LIST_012)

    // The second-to-latest result stays pending since the session was loading (the actual result is the fully
    // loaded session). This is only true if the observer begins before starting to load the session.
    verify(mockCurrentQuestionLiveDataObserver, Mockito.atLeast(2)).onChanged(currentQuestionResultCaptor.capture())
    val results = currentQuestionResultCaptor.allValues
    assertThat(results[results.size - 2].isPending()).isTrue()
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testGetCurrentQuestion_playSession_loaded_returnsInitialStatePending() = runBlockingTest(
    coroutineContext
  ) {
    startTrainingSession(TEST_SKILL_ID_LIST_012)

    val currentQuestionLiveData = questionAssessmentProgressController.getCurrentQuestion()
    currentQuestionLiveData.observeForever(mockCurrentQuestionLiveDataObserver)
    advanceUntilIdle()

    verify(mockCurrentQuestionLiveDataObserver, atLeastOnce()).onChanged(currentQuestionResultCaptor.capture())
    assertThat(currentQuestionResultCaptor.value.isSuccess()).isTrue()
    val ephemeralQuestion = currentQuestionResultCaptor.value.getOrThrow()
    assertThat(ephemeralQuestion.currentQuestionIndex).isEqualTo(0)
    assertThat(ephemeralQuestion.totalQuestionCount).isGreaterThan(0)
    assertThat(ephemeralQuestion.ephemeralState.stateTypeCase).isEqualTo(PENDING_STATE)
    assertThat(ephemeralQuestion.ephemeralState.state.content.html).contains("What fraction does 'quarter'")
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testGetCurrentQuestion_playInvalidSession_thenPlayValidExp_returnsInitialPendingState() = runBlockingTest(
    coroutineContext
  ) {
    // Start with starting an invalid training session.
    startTrainingSession(listOf())
    endTrainingSession()

    // Then a valid one.
    startTrainingSession(TEST_SKILL_ID_LIST_012)
    val currentQuestionLiveData = questionAssessmentProgressController.getCurrentQuestion()
    currentQuestionLiveData.observeForever(mockCurrentQuestionLiveDataObserver)
    advanceUntilIdle()

    // The latest result should correspond to the valid ID, and the progress controller should gracefully recover.
    verify(mockCurrentQuestionLiveDataObserver, atLeastOnce()).onChanged(currentQuestionResultCaptor.capture())
    assertThat(currentQuestionResultCaptor.value.isSuccess()).isTrue()
    val ephemeralQuestion = currentQuestionResultCaptor.value.getOrThrow()
    assertThat(ephemeralQuestion.currentQuestionIndex).isEqualTo(0)
    assertThat(ephemeralQuestion.totalQuestionCount).isGreaterThan(0)
    assertThat(ephemeralQuestion.ephemeralState.stateTypeCase).isEqualTo(PENDING_STATE)
    assertThat(ephemeralQuestion.ephemeralState.state.content.html).contains("What fraction does 'quarter'")
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testStopTrainingSession_withoutStartingSession_fails() = runBlockingTest(coroutineContext) {
    val resultLiveData = questionTrainingController.stopQuestionTrainingSession()
    advanceUntilIdle()

    assertThat(resultLiveData.value).isNotNull()
    assertThat(resultLiveData.value!!.isFailure()).isTrue()
    assertThat(resultLiveData.value!!.getErrorOrNull())
      .hasMessageThat()
      .contains("Cannot stop a new training session which wasn't started")
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testStartTrainingSession_withoutFinishingPrevious_fails() = runBlockingTest(coroutineContext) {
    questionTrainingController.startQuestionTrainingSession(TEST_SKILL_ID_LIST_012)

    val resultLiveData = questionTrainingController.startQuestionTrainingSession(TEST_SKILL_ID_LIST_02)
    advanceUntilIdle()

    assertThat(resultLiveData.value).isNotNull()
    assertThat(resultLiveData.value!!.isFailure()).isTrue()
    assertThat(resultLiveData.value!!.getErrorOrNull())
      .hasMessageThat()
      .contains("Cannot start a new training session until the previous one is completed")
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testStopTrainingSession_afterStartingPreviousSession_succeeds() = runBlockingTest(coroutineContext) {
    questionTrainingController.startQuestionTrainingSession(TEST_SKILL_ID_LIST_012)

    val resultLiveData = questionTrainingController.stopQuestionTrainingSession()
    advanceUntilIdle()

    assertThat(resultLiveData.value).isNotNull()
    assertThat(resultLiveData.value!!.isSuccess()).isTrue()
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testGetCurrentQuestion_playSecondSession_afterFinishingPrev_loaded_returnsInitialState() = runBlockingTest(
    coroutineContext
  ) {
    val currentQuestionLiveData = questionAssessmentProgressController.getCurrentQuestion()
    currentQuestionLiveData.observeForever(mockCurrentQuestionLiveDataObserver)
    // Start with playing a valid session, then stop.
    startTrainingSession(TEST_SKILL_ID_LIST_012)
    endTrainingSession()

    // Then another valid one.
    startTrainingSession(TEST_SKILL_ID_LIST_2)

    // The latest result should correspond to the valid ID, and the progress controller should gracefully recover.
    verify(mockCurrentQuestionLiveDataObserver, atLeastOnce()).onChanged(currentQuestionResultCaptor.capture())
    assertThat(currentQuestionResultCaptor.value.isSuccess()).isTrue()
    val ephemeralQuestion = currentQuestionResultCaptor.value.getOrThrow()
    assertThat(ephemeralQuestion.currentQuestionIndex).isEqualTo(0)
    assertThat(ephemeralQuestion.totalQuestionCount).isGreaterThan(0)
    assertThat(ephemeralQuestion.ephemeralState.stateTypeCase).isEqualTo(PENDING_STATE)
    assertThat(ephemeralQuestion.ephemeralState.state.content.html).contains("of a cake, what does the 10 represent?")
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testSubmitAnswer_beforePlaying_failsWithError() = runBlockingTest(coroutineContext) {
    val result = questionAssessmentProgressController.submitAnswer(createMultipleChoiceAnswer(0))
    result.observeForever(mockAsyncAnswerOutcomeObserver)
    advanceUntilIdle()

    // Verify that the answer submission failed.
    verify(mockAsyncAnswerOutcomeObserver, atLeastOnce()).onChanged(asyncAnswerOutcomeCaptor.capture())
    assertThat(asyncAnswerOutcomeCaptor.value.isFailure()).isTrue()
    assertThat(asyncAnswerOutcomeCaptor.value.getErrorOrNull())
      .hasMessageThat()
      .contains("Cannot submit an answer if an session is not being played.")
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testSubmitAnswer_whileLoading_failsWithError() = runBlockingTest(coroutineContext) {
    // Start playing an session, but don't wait for it to complete.
    subscribeToCurrentQuestionToAllowSessionToLoad()
    startTrainingSession(TEST_SKILL_ID_LIST_2)

    val result = questionAssessmentProgressController.submitAnswer(createMultipleChoiceAnswer(0))
    result.observeForever(mockAsyncAnswerOutcomeObserver)
    advanceUntilIdle()

    // Verify that the answer submission failed.
    verify(mockAsyncAnswerOutcomeObserver, atLeastOnce()).onChanged(asyncAnswerOutcomeCaptor.capture())
    assertThat(asyncAnswerOutcomeCaptor.value.isFailure()).isTrue()
    assertThat(asyncAnswerOutcomeCaptor.value.getErrorOrNull())
      .hasMessageThat()
      .contains("Cannot submit an answer while the session is being loaded.")
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testSubmitAnswer_forMultipleChoice_correctAnswer_succeeds() = runBlockingTest(coroutineContext) {
    subscribeToCurrentQuestionToAllowSessionToLoad()
    startTrainingSession(TEST_SKILL_ID_LIST_2)

    val result = questionAssessmentProgressController.submitAnswer(createMultipleChoiceAnswer(1))
    result.observeForever(mockAsyncAnswerOutcomeObserver)
    advanceUntilIdle()

    // Verify that the answer submission was successful.
    verify(mockAsyncAnswerOutcomeObserver, atLeastOnce()).onChanged(asyncAnswerOutcomeCaptor.capture())
    assertThat(asyncAnswerOutcomeCaptor.value.isSuccess()).isTrue()
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testSubmitAnswer_forMultipleChoice_correctAnswer_returnsOutcomeWithTransition() = runBlockingTest(
    coroutineContext
  ) {
    subscribeToCurrentQuestionToAllowSessionToLoad()
    startTrainingSession(TEST_SKILL_ID_LIST_2)

    val result = questionAssessmentProgressController.submitAnswer(createMultipleChoiceAnswer(1))
    result.observeForever(mockAsyncAnswerOutcomeObserver)
    advanceUntilIdle()

    // Verify that the answer submission was successful.
    verify(mockAsyncAnswerOutcomeObserver, atLeastOnce()).onChanged(asyncAnswerOutcomeCaptor.capture())
    val answerOutcome = asyncAnswerOutcomeCaptor.value.getOrThrow()
    assertThat(answerOutcome.feedback.html).contains("That's correct!")
    assertThat(answerOutcome.isCorrectAnswer).isTrue()
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testSubmitAnswer_forMultipleChoice_wrongAnswer_succeeds() = runBlockingTest(
    coroutineContext
  ) {
    subscribeToCurrentQuestionToAllowSessionToLoad()
    startTrainingSession(TEST_SKILL_ID_LIST_2)

    val result = questionAssessmentProgressController.submitAnswer(createMultipleChoiceAnswer(0))
    result.observeForever(mockAsyncAnswerOutcomeObserver)
    advanceUntilIdle()

    // Verify that the answer submission was successful.
    verify(mockAsyncAnswerOutcomeObserver, atLeastOnce()).onChanged(asyncAnswerOutcomeCaptor.capture())
    assertThat(asyncAnswerOutcomeCaptor.value.isSuccess()).isTrue()
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testSubmitAnswer_forMultipleChoice_wrongAnswer_providesDefaultFeedbackAndNewStateTransition() = runBlockingTest(
    coroutineContext
  ) {
    subscribeToCurrentQuestionToAllowSessionToLoad()
    startTrainingSession(TEST_SKILL_ID_LIST_2)

    val result = questionAssessmentProgressController.submitAnswer(createMultipleChoiceAnswer(0))
    result.observeForever(mockAsyncAnswerOutcomeObserver)
    advanceUntilIdle()

    // Verify that the answer submission was successful.
    verify(mockAsyncAnswerOutcomeObserver, atLeastOnce()).onChanged(asyncAnswerOutcomeCaptor.capture())
    val answerOutcome = asyncAnswerOutcomeCaptor.value.getOrThrow()
    assertThat(answerOutcome.feedback.html).contains("Incorrect. Try again.")
    assertThat(answerOutcome.isCorrectAnswer).isFalse()
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testGetCurrentQuestion_afterSubmittingCorrectMultiChoiceAnswer_becomesCompletedState() = runBlockingTest(
    coroutineContext
  ) {
    val currentQuestionLiveData = questionAssessmentProgressController.getCurrentQuestion()
    currentQuestionLiveData.observeForever(mockCurrentQuestionLiveDataObserver)
    startTrainingSession(TEST_SKILL_ID_LIST_2)

    submitMultipleChoiceAnswer(1)

    // Verify that the current state updates. It should stay pending, and the wrong answer should be appended.
    verify(mockCurrentQuestionLiveDataObserver, atLeastOnce()).onChanged(currentQuestionResultCaptor.capture())
    assertThat(currentQuestionResultCaptor.value.isSuccess()).isTrue()
    val ephemeralQuestion = currentQuestionResultCaptor.value.getOrThrow()
    assertThat(ephemeralQuestion.currentQuestionIndex).isEqualTo(0)
    assertThat(ephemeralQuestion.totalQuestionCount).isEqualTo(2)
    assertThat(ephemeralQuestion.ephemeralState.stateTypeCase).isEqualTo(COMPLETED_STATE)
    val completedState = ephemeralQuestion.ephemeralState.completedState
    assertThat(completedState.getAnswer(0).userAnswer.nonNegativeInt).isEqualTo(1)
    assertThat(completedState.getAnswer(0).feedback.html).contains("That's correct!")
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testGetCurrentQuestion_afterSubmittingWrongMultiChoiceAnswer_updatesPendingState() = runBlockingTest(
    coroutineContext
  ) {
    val currentQuestionLiveData = questionAssessmentProgressController.getCurrentQuestion()
    currentQuestionLiveData.observeForever(mockCurrentQuestionLiveDataObserver)
    startTrainingSession(TEST_SKILL_ID_LIST_2)

    submitMultipleChoiceAnswer(0)

    // Verify that the current state updates. It should stay pending, and the wrong answer should be appended.
    verify(mockCurrentQuestionLiveDataObserver, atLeastOnce()).onChanged(currentQuestionResultCaptor.capture())
    assertThat(currentQuestionResultCaptor.value.isSuccess()).isTrue()
    val ephemeralQuestion = currentQuestionResultCaptor.value.getOrThrow()
    assertThat(ephemeralQuestion.currentQuestionIndex).isEqualTo(0)
    assertThat(ephemeralQuestion.totalQuestionCount).isEqualTo(2)
    assertThat(ephemeralQuestion.ephemeralState.stateTypeCase).isEqualTo(PENDING_STATE)
    val pendingState = ephemeralQuestion.ephemeralState.pendingState
    assertThat(pendingState.getWrongAnswer(0).userAnswer.nonNegativeInt).isEqualTo(0)
    assertThat(pendingState.getWrongAnswer(0).feedback.html).contains("Incorrect. Try again.")
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testGetCurrentQuestion_afterSubmittingWrongThenRightAnswer_updatesToStateWithBothAnswers() = runBlockingTest(
    coroutineContext
  ) {
    subscribeToCurrentQuestionToAllowSessionToLoad()
    startTrainingSession(TEST_SKILL_ID_LIST_2)
    submitMultipleChoiceAnswer(0)

    submitMultipleChoiceAnswer(1)

    // Verify that the current state updates. It should now be completed with both the wrong and correct answers.
    verify(mockCurrentQuestionLiveDataObserver, atLeastOnce()).onChanged(currentQuestionResultCaptor.capture())
    assertThat(currentQuestionResultCaptor.value.isSuccess()).isTrue()
    val ephemeralQuestion = currentQuestionResultCaptor.value.getOrThrow()
    assertThat(ephemeralQuestion.currentQuestionIndex).isEqualTo(0)
    assertThat(ephemeralQuestion.totalQuestionCount).isEqualTo(2)
    assertThat(ephemeralQuestion.ephemeralState.stateTypeCase).isEqualTo(COMPLETED_STATE)
    val completedState = ephemeralQuestion.ephemeralState.completedState
    assertThat(completedState.getAnswer(0).userAnswer.nonNegativeInt).isEqualTo(0)
    assertThat(completedState.getAnswer(0).feedback.html).contains("Incorrect. Try again.")
    assertThat(completedState.getAnswer(1).userAnswer.nonNegativeInt).isEqualTo(1)
    assertThat(completedState.getAnswer(1).feedback.html).contains("That's correct!")
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testMoveToNext_beforePlaying_failsWithError() = runBlockingTest(coroutineContext) {
    val moveToStateResult = questionAssessmentProgressController.moveToNextQuestion()
    moveToStateResult.observeForever(mockAsyncResultLiveDataObserver)

    verify(mockAsyncResultLiveDataObserver, atLeastOnce()).onChanged(asyncResultCaptor.capture())
    assertThat(asyncResultCaptor.value.isFailure()).isTrue()
    assertThat(asyncResultCaptor.value.getErrorOrNull())
      .hasMessageThat()
      .contains("Cannot navigate to a next state if an session is not being played.")
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testMoveToNext_whileLoadingSession_failsWithError() = runBlockingTest(coroutineContext) {
    // Start playing an session, but don't wait for it to complete.
    subscribeToCurrentQuestionToAllowSessionToLoad()
    startTrainingSession(TEST_SKILL_ID_LIST_2)

    val moveToStateResult = questionAssessmentProgressController.moveToNextQuestion()
    moveToStateResult.observeForever(mockAsyncResultLiveDataObserver)

    verify(mockAsyncResultLiveDataObserver, atLeastOnce()).onChanged(asyncResultCaptor.capture())
    assertThat(asyncResultCaptor.value.isFailure()).isTrue()
    assertThat(asyncResultCaptor.value.getErrorOrNull())
      .hasMessageThat()
      .contains("Cannot navigate to a next state if an session is being loaded.")
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testMoveToNext_forPendingInitialState_failsWithError() = runBlockingTest(coroutineContext) {
    subscribeToCurrentQuestionToAllowSessionToLoad()
    startTrainingSession(TEST_SKILL_ID_LIST_2)

    val moveToStateResult = questionAssessmentProgressController.moveToNextQuestion()
    moveToStateResult.observeForever(mockAsyncResultLiveDataObserver)
    advanceUntilIdle()

    // Verify that we can't move ahead since the current state isn't yet completed.
    verify(mockAsyncResultLiveDataObserver, atLeastOnce()).onChanged(asyncResultCaptor.capture())
    assertThat(asyncResultCaptor.value.isFailure()).isTrue()
    assertThat(asyncResultCaptor.value.getErrorOrNull())
      .hasMessageThat()
      .contains("Cannot navigate to next state; at most recent state.")
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testMoveToNext_forCompletedState_succeeds() = runBlockingTest(coroutineContext) {
    subscribeToCurrentQuestionToAllowSessionToLoad()
    startTrainingSession(TEST_SKILL_ID_LIST_2)
    submitMultipleChoiceAnswer(0)

    val moveToStateResult = questionAssessmentProgressController.moveToNextQuestion()
    moveToStateResult.observeForever(mockAsyncResultLiveDataObserver)
    advanceUntilIdle()

    verify(mockAsyncResultLiveDataObserver, atLeastOnce()).onChanged(asyncResultCaptor.capture())
    assertThat(asyncResultCaptor.value.isSuccess()).isTrue()
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testMoveToNext_forCompletedState_movesToNextState() = runBlockingTest(coroutineContext) {
    val currentQuestionLiveData = questionAssessmentProgressController.getCurrentQuestion()
    currentQuestionLiveData.observeForever(mockCurrentQuestionLiveDataObserver)
    startTrainingSession(TEST_SKILL_ID_LIST_2)
    submitMultipleChoiceAnswer(0)

    moveToNextQuestion()

    verify(mockCurrentQuestionLiveDataObserver, atLeastOnce()).onChanged(currentQuestionResultCaptor.capture())
    assertThat(currentQuestionResultCaptor.value.isSuccess()).isTrue()
    val currentQuestion = currentQuestionResultCaptor.value.getOrThrow()
    assertThat(currentQuestion.ephemeralState.state.name).isEqualTo("What language")
    assertThat(currentQuestion.ephemeralState.stateTypeCase).isEqualTo(PENDING_STATE)
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testMoveToNext_afterMovingFromCompletedState_failsWithError() = runBlockingTest(coroutineContext) {
    subscribeToCurrentQuestionToAllowSessionToLoad()
    startTrainingSession(TEST_SKILL_ID_LIST_2)
    submitMultipleChoiceAnswer(0)
    moveToNextQuestion()

    // Try skipping past the current state.
    val moveToStateResult = questionAssessmentProgressController.moveToNextQuestion()
    moveToStateResult.observeForever(mockAsyncResultLiveDataObserver)
    advanceUntilIdle()

    // Verify we can't move ahead since the new state isn't yet completed.
    verify(mockAsyncResultLiveDataObserver, atLeastOnce()).onChanged(asyncResultCaptor.capture())
    assertThat(asyncResultCaptor.value.isFailure()).isTrue()
    assertThat(asyncResultCaptor.value.getErrorOrNull())
      .hasMessageThat()
      .contains("Cannot navigate to next state; at most recent state.")
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testMoveToPrevious_beforePlaying_failsWithError() = runBlockingTest(coroutineContext) {
    val moveToStateResult = questionAssessmentProgressController.moveToPreviousQuestion()
    moveToStateResult.observeForever(mockAsyncResultLiveDataObserver)
    advanceUntilIdle()

    verify(mockAsyncResultLiveDataObserver, atLeastOnce()).onChanged(asyncResultCaptor.capture())
    assertThat(asyncResultCaptor.value.isFailure()).isTrue()
    assertThat(asyncResultCaptor.value.getErrorOrNull())
      .hasMessageThat()
      .contains("Cannot navigate to a previous state if an session is not being played.")
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testMoveToPrevious_whileLoadingSession_failsWithError() = runBlockingTest(coroutineContext) {
    // Start playing an session, but don't wait for it to complete.
    subscribeToCurrentQuestionToAllowSessionToLoad()
    startTrainingSession(TEST_SKILL_ID_LIST_2)

    val moveToStateResult = questionAssessmentProgressController.moveToPreviousQuestion()
    moveToStateResult.observeForever(mockAsyncResultLiveDataObserver)
    advanceUntilIdle()

    verify(mockAsyncResultLiveDataObserver, atLeastOnce()).onChanged(asyncResultCaptor.capture())
    assertThat(asyncResultCaptor.value.isFailure()).isTrue()
    assertThat(asyncResultCaptor.value.getErrorOrNull())
      .hasMessageThat()
      .contains("Cannot navigate to a previous state if an session is being loaded.")
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testMoveToPrevious_onPendingInitialState_failsWithError() = runBlockingTest(coroutineContext) {
    subscribeToCurrentQuestionToAllowSessionToLoad()
    startTrainingSession(TEST_SKILL_ID_LIST_2)

    val moveToStateResult = questionAssessmentProgressController.moveToPreviousQuestion()
    moveToStateResult.observeForever(mockAsyncResultLiveDataObserver)
    advanceUntilIdle()

    // Verify we can't move behind since the current state is the initial session state.
    verify(mockAsyncResultLiveDataObserver, atLeastOnce()).onChanged(asyncResultCaptor.capture())
    assertThat(asyncResultCaptor.value.isFailure()).isTrue()
    assertThat(asyncResultCaptor.value.getErrorOrNull())
      .hasMessageThat()
      .contains("Cannot navigate to previous state; at initial state.")
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testMoveToPrevious_onCompletedInitialState_failsWithError() = runBlockingTest(coroutineContext) {
    subscribeToCurrentQuestionToAllowSessionToLoad()
    startTrainingSession(TEST_SKILL_ID_LIST_2)
    submitMultipleChoiceAnswer(0)

    val moveToStateResult = questionAssessmentProgressController.moveToPreviousQuestion()
    moveToStateResult.observeForever(mockAsyncResultLiveDataObserver)
    advanceUntilIdle()

    // Still can't navigate behind for a completed initial state since there's no previous state.
    verify(mockAsyncResultLiveDataObserver, atLeastOnce()).onChanged(asyncResultCaptor.capture())
    assertThat(asyncResultCaptor.value.isFailure()).isTrue()
    assertThat(asyncResultCaptor.value.getErrorOrNull())
      .hasMessageThat()
      .contains("Cannot navigate to previous state; at initial state.")
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testMoveToPrevious_forStateWithCompletedPreviousState_succeeds() = runBlockingTest(coroutineContext) {
    subscribeToCurrentQuestionToAllowSessionToLoad()
    startTrainingSession(TEST_SKILL_ID_LIST_2)
    submitMultipleChoiceAnswerAndMoveToNextQuestion(0)

    val moveToStateResult = questionAssessmentProgressController.moveToPreviousQuestion()
    moveToStateResult.observeForever(mockAsyncResultLiveDataObserver)
    advanceUntilIdle()

    // Verify that we can navigate to the previous state since the current state is complete and not initial.
    verify(mockAsyncResultLiveDataObserver, atLeastOnce()).onChanged(asyncResultCaptor.capture())
    assertThat(asyncResultCaptor.value.isSuccess()).isTrue()
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testMoveToPrevious_forCompletedState_movesToPreviousState() = runBlockingTest(coroutineContext) {
    val currentQuestionLiveData = questionAssessmentProgressController.getCurrentQuestion()
    currentQuestionLiveData.observeForever(mockCurrentQuestionLiveDataObserver)
    startTrainingSession(TEST_SKILL_ID_LIST_2)
    submitMultipleChoiceAnswerAndMoveToNextQuestion(0)

    moveToPreviousQuestion()

    // Since the answer submission and forward navigation should work (see earlier tests), verify that the move to the
    // previous state does return us back to the initial session state (which is now completed).
    verify(mockCurrentQuestionLiveDataObserver, atLeastOnce()).onChanged(currentQuestionResultCaptor.capture())
    assertThat(currentQuestionResultCaptor.value.isSuccess()).isTrue()
    val currentQuestion = currentQuestionResultCaptor.value.getOrThrow()
    assertThat(currentQuestion.ephemeralState.state.name).isEqualTo("Welcome!")
    assertThat(currentQuestion.ephemeralState.stateTypeCase).isEqualTo(COMPLETED_STATE)
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testMoveToPrevious_navigatedForwardThenBackToInitial_failsWithError() = runBlockingTest(coroutineContext) {
    subscribeToCurrentQuestionToAllowSessionToLoad()
    startTrainingSession(TEST_SKILL_ID_LIST_2)
    submitMultipleChoiceAnswerAndMoveToNextQuestion(0)
    moveToPreviousQuestion()

    val moveToStateResult = questionAssessmentProgressController.moveToPreviousQuestion()
    moveToStateResult.observeForever(mockAsyncResultLiveDataObserver)
    advanceUntilIdle()

    // The first previous navigation should succeed (see above), but the second will fail since we're back at the
    // initial state.
    verify(mockAsyncResultLiveDataObserver, atLeastOnce()).onChanged(asyncResultCaptor.capture())
    assertThat(asyncResultCaptor.value.isFailure()).isTrue()
    assertThat(asyncResultCaptor.value.getErrorOrNull())
      .hasMessageThat()
      .contains("Cannot navigate to previous state; at initial state.")
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testSubmitAnswer_forTextInput_correctAnswer_returnsOutcomeWithTransition() = runBlockingTest(coroutineContext) {
    subscribeToCurrentQuestionToAllowSessionToLoad()
    startTrainingSession(TEST_SKILL_ID_LIST_2)
    submitMultipleChoiceAnswerAndMoveToNextQuestion(0)

    val result = questionAssessmentProgressController.submitAnswer(createTextInputAnswer("Finnish"))
    result.observeForever(mockAsyncAnswerOutcomeObserver)
    advanceUntilIdle()

    // Verify that the answer submission was successful.
    verify(mockAsyncAnswerOutcomeObserver, atLeastOnce()).onChanged(asyncAnswerOutcomeCaptor.capture())
    val answerOutcome = asyncAnswerOutcomeCaptor.value.getOrThrow()
    assertThat(answerOutcome.isCorrectAnswer).isTrue()
    assertThat(answerOutcome.feedback.html).contains("Yes! Oppia is the Finnish word for learn.")
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testSubmitAnswer_forTextInput_wrongAnswer_returnsDefaultOutcome() = runBlockingTest(coroutineContext) {
    subscribeToCurrentQuestionToAllowSessionToLoad()
    startTrainingSession(TEST_SKILL_ID_LIST_2)
    submitMultipleChoiceAnswerAndMoveToNextQuestion(0)

    val result = questionAssessmentProgressController.submitAnswer(createTextInputAnswer("Klingon"))
    result.observeForever(mockAsyncAnswerOutcomeObserver)
    advanceUntilIdle()

    // Verify that the answer was wrong, and that there's no handler for it so the default outcome is returned.
    verify(mockAsyncAnswerOutcomeObserver, atLeastOnce()).onChanged(asyncAnswerOutcomeCaptor.capture())
    val answerOutcome = asyncAnswerOutcomeCaptor.value.getOrThrow()
    assertThat(answerOutcome.isCorrectAnswer).isFalse()
    assertThat(answerOutcome.feedback.html).contains("Sorry, nope")
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testGetCurrentQuestion_secondState_submitRightAnswer_pendingStateBecomesCompleted() = runBlockingTest(
    coroutineContext
  ) {
    subscribeToCurrentQuestionToAllowSessionToLoad()
    startTrainingSession(TEST_SKILL_ID_LIST_2)
    submitMultipleChoiceAnswerAndMoveToNextQuestion(0)

    val result = questionAssessmentProgressController.submitAnswer(createTextInputAnswer("Finnish"))
    result.observeForever(mockAsyncAnswerOutcomeObserver)
    advanceUntilIdle()

    // Verify that the current state updates. It should stay pending, and the wrong answer should be appended.
    verify(mockCurrentQuestionLiveDataObserver, atLeastOnce()).onChanged(currentQuestionResultCaptor.capture())
    assertThat(currentQuestionResultCaptor.value.isSuccess()).isTrue()
    val currentQuestion = currentQuestionResultCaptor.value.getOrThrow()
    assertThat(currentQuestion.ephemeralState.stateTypeCase).isEqualTo(COMPLETED_STATE)
    val completedState = currentQuestion.ephemeralState.completedState
    assertThat(completedState.answerCount).isEqualTo(1)
    assertThat(completedState.getAnswer(0).userAnswer.normalizedString).isEqualTo("Finnish")
    assertThat(completedState.getAnswer(0).feedback.html).contains("Yes! Oppia is the Finnish word")
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testGetCurrentQuestion_secondState_submitWrongAnswer_updatePendingState() = runBlockingTest(
    coroutineContext
  ) {
    subscribeToCurrentQuestionToAllowSessionToLoad()
    startTrainingSession(TEST_SKILL_ID_LIST_2)
    submitMultipleChoiceAnswerAndMoveToNextQuestion(0)

    val result = questionAssessmentProgressController.submitAnswer(createTextInputAnswer("Klingon"))
    result.observeForever(mockAsyncAnswerOutcomeObserver)
    advanceUntilIdle()

    // Verify that the current state updates. It should now be completed with the correct answer.
    verify(mockCurrentQuestionLiveDataObserver, atLeastOnce()).onChanged(currentQuestionResultCaptor.capture())
    assertThat(currentQuestionResultCaptor.value.isSuccess()).isTrue()
    val currentQuestion = currentQuestionResultCaptor.value.getOrThrow()
    assertThat(currentQuestion.ephemeralState.stateTypeCase).isEqualTo(PENDING_STATE)
    val pendingState = currentQuestion.ephemeralState.pendingState
    assertThat(pendingState.wrongAnswerCount).isEqualTo(1)
    assertThat(pendingState.getWrongAnswer(0).userAnswer.normalizedString).isEqualTo("Klingon")
    assertThat(pendingState.getWrongAnswer(0).feedback.html).contains("Sorry, nope")
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testGetCurrentQuestion_afterMovePreviousAndNext_returnscurrentQuestion() = runBlockingTest(coroutineContext) {
    val currentQuestionLiveData = questionAssessmentProgressController.getCurrentQuestion()
    currentQuestionLiveData.observeForever(mockCurrentQuestionLiveDataObserver)
    startTrainingSession(TEST_SKILL_ID_LIST_2)
    submitMultipleChoiceAnswerAndMoveToNextQuestion(0)

    moveToPreviousQuestion()
    moveToNextQuestion()

    // The current state should stay the same.
    verify(mockCurrentQuestionLiveDataObserver, atLeastOnce()).onChanged(currentQuestionResultCaptor.capture())
    assertThat(currentQuestionResultCaptor.value.isSuccess()).isTrue()
    val currentQuestion = currentQuestionResultCaptor.value.getOrThrow()
    assertThat(currentQuestion.ephemeralState.state.name).isEqualTo("What language")
    assertThat(currentQuestion.ephemeralState.stateTypeCase).isEqualTo(PENDING_STATE)
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testGetCurrentQuestion_afterMoveNextAndPrevious_returnscurrentQuestion() = runBlockingTest(coroutineContext) {
    val currentQuestionLiveData = questionAssessmentProgressController.getCurrentQuestion()
    currentQuestionLiveData.observeForever(mockCurrentQuestionLiveDataObserver)
    startTrainingSession(TEST_SKILL_ID_LIST_2)
    submitMultipleChoiceAnswerAndMoveToNextQuestion(0)
    submitTextInputAnswer("Finnish") // Submit the answer but do not proceed to the next state.

    moveToNextQuestion()
    moveToPreviousQuestion()

    // The current state should stay the same.
    verify(mockCurrentQuestionLiveDataObserver, atLeastOnce()).onChanged(currentQuestionResultCaptor.capture())
    assertThat(currentQuestionResultCaptor.value.isSuccess()).isTrue()
    val currentQuestion = currentQuestionResultCaptor.value.getOrThrow()
    assertThat(currentQuestion.ephemeralState.state.name).isEqualTo("What language")
    assertThat(currentQuestion.ephemeralState.stateTypeCase).isEqualTo(COMPLETED_STATE)
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testGetCurrentQuestion_afterMoveToPrevious_onThirdState_newObserver_receivesCompletedSecondState() = runBlockingTest(
    coroutineContext
  ) {
    subscribeToCurrentQuestionToAllowSessionToLoad()
    startTrainingSession(TEST_SKILL_ID_LIST_2)
    submitMultipleChoiceAnswerAndMoveToNextQuestion(0) // First state -> second
    submitTextInputAnswerAndMoveToNextQuestion("Finnish") // Second state -> third

    // Move to the previous state and register a new observer.
    moveToPreviousQuestion() // Third state -> second
    val currentQuestionLiveData = questionAssessmentProgressController.getCurrentQuestion()
    currentQuestionLiveData.observeForever(mockCurrentQuestionLiveDataObserver2)
    advanceUntilIdle()

    // The new observer should observe the completed second state since it's the current pending state.
    verify(mockCurrentQuestionLiveDataObserver2, atLeastOnce()).onChanged(currentQuestionResultCaptor.capture())
    assertThat(currentQuestionResultCaptor.value.isSuccess()).isTrue()
    val currentQuestion = currentQuestionResultCaptor.value.getOrThrow()
    assertThat(currentQuestion.ephemeralState.state.name).isEqualTo("What language")
    assertThat(currentQuestion.ephemeralState.stateTypeCase).isEqualTo(COMPLETED_STATE)
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testSubmitAnswer_forNumericInput_correctAnswer_returnsOutcomeWithTransition() = runBlockingTest(
    coroutineContext
  ) {
    subscribeToCurrentQuestionToAllowSessionToLoad()
    startTrainingSession(TEST_SKILL_ID_LIST_2)
    submitMultipleChoiceAnswerAndMoveToNextQuestion(0)
    submitTextInputAnswerAndMoveToNextQuestion("Finnish")

    val result = questionAssessmentProgressController.submitAnswer(createNumericInputAnswer(121.0))
    result.observeForever(mockAsyncAnswerOutcomeObserver)
    advanceUntilIdle()

    // Verify that the answer submission was successful.
    verify(mockAsyncAnswerOutcomeObserver, atLeastOnce()).onChanged(asyncAnswerOutcomeCaptor.capture())
    val answerOutcome = asyncAnswerOutcomeCaptor.value.getOrThrow()
    assertThat(answerOutcome.isCorrectAnswer).isTrue()
    assertThat(answerOutcome.feedback.html).contains("Yes, that's correct: 11 times 11 is 121.")
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testSubmitAnswer_forNumericInput_wrongAnswer_returnsOutcomeWithTransition() = runBlockingTest(coroutineContext) {
    subscribeToCurrentQuestionToAllowSessionToLoad()
    startTrainingSession(TEST_SKILL_ID_LIST_2)
    submitMultipleChoiceAnswerAndMoveToNextQuestion(0)
    submitTextInputAnswerAndMoveToNextQuestion("Finnish")

    val result = questionAssessmentProgressController.submitAnswer(createNumericInputAnswer(122.0))
    result.observeForever(mockAsyncAnswerOutcomeObserver)
    advanceUntilIdle()

    // Verify that the answer submission failed as expected.
    verify(mockAsyncAnswerOutcomeObserver, atLeastOnce()).onChanged(asyncAnswerOutcomeCaptor.capture())
    val answerOutcome = asyncAnswerOutcomeCaptor.value.getOrThrow()
    assertThat(answerOutcome.isCorrectAnswer).isFalse()
    assertThat(answerOutcome.feedback.html).contains("You are actually very close.")
  }

//  @Test
//  @ExperimentalCoroutinesApi
//  fun testGetCurrentQuestion_fifthState_isTerminalState() = runBlockingTest(coroutineContext) {
//    val currentQuestionLiveData = questionAssessmentProgressController.getCurrentQuestion()
//    currentQuestionLiveData.observeForever(mockCurrentQuestionLiveDataObserver)
//    startTrainingSession(TEST_SKILL_ID_LIST_2)
//    submitMultipleChoiceAnswerAndMoveToNextQuestion(0)
//    submitTextInputAnswerAndMoveToNextQuestion("Finnish")
//    submitNumericInputAnswerAndMoveToNextQuestion(121.0)
//
//    submitContinueButtonAnswerAndMoveToNextQuestion()
//
//    // Verify that the fifth state is terminal.
//    verify(mockCurrentQuestionLiveDataObserver, atLeastOnce()).onChanged(currentQuestionResultCaptor.capture())
//    assertThat(currentQuestionResultCaptor.value.isSuccess()).isTrue()
//    val currentQuestion = currentQuestionResultCaptor.value.getOrThrow()
//    assertThat(currentQuestion.ephemeralState.stateTypeCase).isEqualTo(TERMINAL_STATE)
//  }

  @Test
  @ExperimentalCoroutinesApi
  fun testGetCurrentQuestion_afterMoveToPrevious_onThirdState_updatesToCompletedSecondState() = runBlockingTest(
    coroutineContext
  ) {
    val currentQuestionLiveData = questionAssessmentProgressController.getCurrentQuestion()
    currentQuestionLiveData.observeForever(mockCurrentQuestionLiveDataObserver)
    startTrainingSession(TEST_SKILL_ID_LIST_2)
    submitMultipleChoiceAnswerAndMoveToNextQuestion(0)
    submitTextInputAnswerAndMoveToNextQuestion("Finnish")

    moveToPreviousQuestion()

    // Verify that the current state is the second state, and is completed. It should also have the previously submitted
    // answer, allowing learners to potentially view past answers.
    verify(mockCurrentQuestionLiveDataObserver, atLeastOnce()).onChanged(currentQuestionResultCaptor.capture())
    assertThat(currentQuestionResultCaptor.value.isSuccess()).isTrue()
    val currentQuestion = currentQuestionResultCaptor.value.getOrThrow()
    assertThat(currentQuestion.ephemeralState.stateTypeCase).isEqualTo(COMPLETED_STATE)
    assertThat(currentQuestion.ephemeralState.state.name).isEqualTo("What language")
    val completedState = currentQuestion.ephemeralState.completedState
    assertThat(completedState.getAnswer(0).userAnswer.normalizedString).isEqualTo("Finnish")
  }

//  @Test
//  @ExperimentalCoroutinesApi
//  fun testMoveToNext_onFinalState_failsWithError() = runBlockingTest(coroutineContext) {
//    subscribeToCurrentQuestionToAllowSessionToLoad()
//    startTrainingSession(TEST_SKILL_ID_LIST_2)
//    submitMultipleChoiceAnswerAndMoveToNextQuestion(0)
//    submitTextInputAnswerAndMoveToNextQuestion("Finnish")
//    submitNumericInputAnswerAndMoveToNextQuestion(121.0)
//    submitContinueButtonAnswerAndMoveToNextQuestion()
//
//    val moveToStateResult = questionAssessmentProgressController.moveToNextQuestion()
//    moveToStateResult.observeForever(mockAsyncResultLiveDataObserver)
//    advanceUntilIdle()
//
//    // Verify we can't navigate past the last state of the training session.
//    verify(mockAsyncResultLiveDataObserver, atLeastOnce()).onChanged(asyncResultCaptor.capture())
//    assertThat(asyncResultCaptor.value.isFailure()).isTrue()
//    assertThat(asyncResultCaptor.value.getErrorOrNull())
//      .hasMessageThat()
//      .contains("Cannot navigate to next state; at most recent state.")
//  }

//  @Test
//  @ExperimentalCoroutinesApi
//  fun testGetCurrentQuestion_afterPlayingFullSecondSesson_returnsTerminalState() = runBlockingTest(coroutineContext) {
//    val currentQuestionLiveData = questionAssessmentProgressController.getCurrentQuestion()
//    currentQuestionLiveData.observeForever(mockCurrentQuestionLiveDataObserver)
//
//    startTrainingSession(TEST_SKILL_ID_LIST_01)
//    submitContinueButtonAnswerAndMoveToNextQuestion()
//    submitMultipleChoiceAnswerAndMoveToNextQuestion(3) // Those were all the questions I had!
//    submitContinueButtonAnswerAndMoveToNextQuestion()
//
//    // Verify that we're now on the final state.
//    verify(mockCurrentQuestionLiveDataObserver, atLeastOnce()).onChanged(currentQuestionResultCaptor.capture())
//    assertThat(currentQuestionResultCaptor.value.isSuccess()).isTrue()
//    val currentQuestion = currentQuestionResultCaptor.value.getOrThrow()
//    assertThat(currentQuestion.ephemeralState.stateTypeCase).isEqualTo(TERMINAL_STATE)
//  }

//  @Test
//  @ExperimentalCoroutinesApi
//  fun testGetCurrentQuestion_afterPlayingFullSecondSesson_diffPath_returnsTerminalState() = runBlockingTest(
//    coroutineContext
//  ) {
//    val currentQuestionLiveData = questionAssessmentProgressController.getCurrentQuestion()
//    currentQuestionLiveData.observeForever(mockCurrentQuestionLiveDataObserver)
//
//    startTrainingSession(TEST_SKILL_ID_LIST_01)
//    submitContinueButtonAnswerAndMoveToNextQuestion()
//    submitMultipleChoiceAnswerAndMoveToNextQuestion(0) // How do your sessions work?
//    submitTextInputAnswerAndMoveToNextQuestion("Oppia Otter") // Can I ask your name?
//    submitContinueButtonAnswerAndMoveToNextQuestion()
//    submitMultipleChoiceAnswerAndMoveToNextQuestion(3) // Those were all the questions I had!
//    submitContinueButtonAnswerAndMoveToNextQuestion()
//
//    // Verify that a different path can also result in reaching the end state.
//    verify(mockCurrentQuestionLiveDataObserver, atLeastOnce()).onChanged(currentQuestionResultCaptor.capture())
//    assertThat(currentQuestionResultCaptor.value.isSuccess()).isTrue()
//    val currentQuestion = currentQuestionResultCaptor.value.getOrThrow()
//    assertThat(currentQuestion.ephemeralState.stateTypeCase).isEqualTo(TERMINAL_STATE)
//  }

//  @Test
//  @ExperimentalCoroutinesApi
//  fun testGetCurrentQuestion_afterPlayingThroughPreviousSessions_returnsStateFromSecondSession() = runBlockingTest(
//    coroutineContext
//  ) {
//    val currentQuestionLiveData = questionAssessmentProgressController.getCurrentQuestion()
//    currentQuestionLiveData.observeForever(mockCurrentQuestionLiveDataObserver)
//    playThroughSession5()
//
//    startTrainingSession(TEST_SKILL_ID_LIST_01)
//    submitContinueButtonAnswerAndMoveToNextQuestion()
//    submitMultipleChoiceAnswerAndMoveToNextQuestion(3) // Those were all the questions I had!
//
//    // Verify that we're on the second-to-last state of the second session.
//    verify(mockCurrentQuestionLiveDataObserver, atLeastOnce()).onChanged(currentQuestionResultCaptor.capture())
//    assertThat(currentQuestionResultCaptor.value.isSuccess()).isTrue()
//    val currentQuestion = currentQuestionResultCaptor.value.getOrThrow()
//    assertThat(currentQuestion.ephemeralState.stateTypeCase).isEqualTo(PENDING_STATE)
//    // This state is not in the other test session.
//    assertThat(currentQuestion.ephemeralState.state.name).isEqualTo("End Card")
//  }

  private fun setUpTestApplicationComponent() {
    DaggerQuestionAssessmentProgressControllerTest_TestApplicationComponent.builder()
      .setApplication(ApplicationProvider.getApplicationContext())
      .build()
      .inject(this)
  }

  /**
   * Creates a blank subscription to the current state to ensure that requests to load the session complete, otherwise
   * post-load operations may fail. An observer is required since the current mediator live data implementation will
   * only lazily load data based on whether there's an active subscription.
   */
  private fun subscribeToCurrentQuestionToAllowSessionToLoad() {
    questionAssessmentProgressController.getCurrentQuestion().observeForever(mockCurrentQuestionLiveDataObserver)
  }

  @ExperimentalCoroutinesApi
  private fun startTrainingSession(skillIdList: List<String>) {
    questionTrainingController.startQuestionTrainingSession(skillIdList)
    testDispatcher.advanceUntilIdle()
  }

  @ExperimentalCoroutinesApi
  private fun submitMultipleChoiceAnswer(choiceIndex: Int) {
    questionAssessmentProgressController.submitAnswer(createMultipleChoiceAnswer(choiceIndex))
    testDispatcher.advanceUntilIdle()
  }

  @ExperimentalCoroutinesApi
  private fun submitTextInputAnswer(textAnswer: String) {
    questionAssessmentProgressController.submitAnswer(createTextInputAnswer(textAnswer))
    testDispatcher.advanceUntilIdle()
  }

  @ExperimentalCoroutinesApi
  private fun submitNumericInputAnswer(numericAnswer: Double) {
    questionAssessmentProgressController.submitAnswer(createNumericInputAnswer(numericAnswer))
    testDispatcher.advanceUntilIdle()
  }

  @ExperimentalCoroutinesApi
  private fun submitMultipleChoiceAnswerAndMoveToNextQuestion(choiceIndex: Int) {
    submitMultipleChoiceAnswer(choiceIndex)
    moveToNextQuestion()
  }

  @ExperimentalCoroutinesApi
  private fun submitTextInputAnswerAndMoveToNextQuestion(textAnswer: String) {
    submitTextInputAnswer(textAnswer)
    moveToNextQuestion()
  }

  @ExperimentalCoroutinesApi
  private fun submitNumericInputAnswerAndMoveToNextQuestion(numericAnswer: Double) {
    submitNumericInputAnswer(numericAnswer)
    moveToNextQuestion()
  }

  @ExperimentalCoroutinesApi
  private fun moveToNextQuestion() {
    questionAssessmentProgressController.moveToNextQuestion()
    testDispatcher.advanceUntilIdle()
  }

  @ExperimentalCoroutinesApi
  private fun moveToPreviousQuestion() {
    questionAssessmentProgressController.moveToPreviousQuestion()
    testDispatcher.advanceUntilIdle()
  }

  @ExperimentalCoroutinesApi
  private fun endTrainingSession() {
    questionTrainingController.stopQuestionTrainingSession()
    testDispatcher.advanceUntilIdle()
  }

  private fun createMultipleChoiceAnswer(choiceIndex: Int): InteractionObject {
    return InteractionObject.newBuilder().setNonNegativeInt(choiceIndex).build()
  }

  private fun createTextInputAnswer(textAnswer: String): InteractionObject {
    return InteractionObject.newBuilder().setNormalizedString(textAnswer).build()
  }

  private fun createNumericInputAnswer(numericAnswer: Double): InteractionObject {
    return InteractionObject.newBuilder().setReal(numericAnswer).build()
  }

  @Qualifier
  annotation class TestDispatcher

  // TODO(#89): Move this to a common test application component.
  @Module
  class TestModule {
    @Provides
    @Singleton
    fun provideContext(application: Application): Context {
      return application
    }

    @ExperimentalCoroutinesApi
    @Singleton
    @Provides
    @TestDispatcher
    fun provideTestDispatcher(): TestCoroutineDispatcher {
      return TestCoroutineDispatcher()
    }

    @ExperimentalCoroutinesApi
    @Singleton
    @Provides
    @BackgroundDispatcher
    fun provideBackgroundDispatcher(@TestDispatcher testDispatcher: TestCoroutineDispatcher): CoroutineDispatcher {
      return testDispatcher
    }

    @ExperimentalCoroutinesApi
    @Singleton
    @Provides
    @BlockingDispatcher
    fun provideBlockingDispatcher(@TestDispatcher testDispatcher: TestCoroutineDispatcher): CoroutineDispatcher {
      return testDispatcher
    }

    // TODO(#59): Either isolate these to their own shared test module, or use the real logging
    // module in tests to avoid needing to specify these settings for tests.
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

  @Module
  class TestQuestionModule {
    companion object {
      var questionSeed = 0L
    }

    @Provides
    @QuestionCountPerTrainingSession
    fun provideQuestionCountPerTrainingSession(): Int = 3

    @Provides
    @QuestionTrainingSeed
    fun provideQuestionTrainingSeed(): Long = questionSeed++
  }

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(modules = [
    TestModule::class, TestQuestionModule::class, ContinueModule::class, FractionInputModule::class,
    ItemSelectionInputModule::class, MultipleChoiceInputModule::class, NumberWithUnitsRuleModule::class,
    NumericInputRuleModule::class, TextInputRuleModule::class, InteractionsModule::class
  ])
  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun inject(questionAssessmentProgressControllerTest: QuestionAssessmentProgressControllerTest)
  }
}
