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
import kotlinx.coroutines.test.TestCoroutineDispatcher
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
import org.oppia.app.model.AnsweredQuestionOutcome
import org.oppia.app.model.EphemeralQuestion
import org.oppia.app.model.EphemeralState.StateTypeCase.COMPLETED_STATE
import org.oppia.app.model.EphemeralState.StateTypeCase.PENDING_STATE
import org.oppia.app.model.EphemeralState.StateTypeCase.TERMINAL_STATE
import org.oppia.app.model.Hint
import org.oppia.app.model.InteractionObject
import org.oppia.app.model.Solution
import org.oppia.app.model.UserAnswer
import org.oppia.domain.classify.InteractionsModule
import org.oppia.domain.classify.rules.continueinteraction.ContinueModule
import org.oppia.domain.classify.rules.dragAndDropSortInput.DragDropSortInputModule
import org.oppia.domain.classify.rules.fractioninput.FractionInputModule
import org.oppia.domain.classify.rules.imageClickInput.ImageClickInputModule
import org.oppia.domain.classify.rules.itemselectioninput.ItemSelectionInputModule
import org.oppia.domain.classify.rules.multiplechoiceinput.MultipleChoiceInputModule
import org.oppia.domain.classify.rules.numberwithunits.NumberWithUnitsRuleModule
import org.oppia.domain.classify.rules.numericinput.NumericInputRuleModule
import org.oppia.domain.classify.rules.textinput.TextInputRuleModule
import org.oppia.domain.oppialogger.LogStorageModule
import org.oppia.domain.topic.TEST_SKILL_ID_0
import org.oppia.domain.topic.TEST_SKILL_ID_1
import org.oppia.domain.topic.TEST_SKILL_ID_2
import org.oppia.testing.FakeExceptionLogger
import org.oppia.testing.TestLogReportingModule
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
import kotlinx.coroutines.ExperimentalCoroutinesApi as ExperimentalCoroutinesApi1
import kotlinx.coroutines.test.runBlockingTest as runBlockingTest1

/** Tests for [QuestionAssessmentProgressController]. */
@RunWith(AndroidJUnit4::class)
@Config(manifest = Config.NONE)
class QuestionAssessmentProgressControllerTest {
  private val TEST_SKILL_ID_LIST_012 =
    listOf(TEST_SKILL_ID_0, TEST_SKILL_ID_1, TEST_SKILL_ID_2) // questions 0, 1, 2, 3, 4, 5
  private val TEST_SKILL_ID_LIST_02 =
    listOf(TEST_SKILL_ID_0, TEST_SKILL_ID_2) // questions 0, 1, 2, 4, 5
  private val TEST_SKILL_ID_LIST_01 =
    listOf(TEST_SKILL_ID_0, TEST_SKILL_ID_1) // questions 0, 1, 2, 3
  private val TEST_SKILL_ID_LIST_2 = listOf(TEST_SKILL_ID_2) // questions 2, 4, 5

  @Rule
  @JvmField
  val mockitoRule: MockitoRule = MockitoJUnit.rule()

  @Inject
  lateinit var questionTrainingController: QuestionTrainingController

  @Inject
  lateinit var questionAssessmentProgressController: QuestionAssessmentProgressController

  @Inject
  lateinit var fakeExceptionLogger: FakeExceptionLogger

  @ExperimentalCoroutinesApi1
  @Inject
  @field:TestDispatcher
  lateinit var testDispatcher: TestCoroutineDispatcher

  @Mock
  lateinit var mockCurrentQuestionLiveDataObserver: Observer<AsyncResult<EphemeralQuestion>>

  @Mock
  lateinit var mockAsyncResultLiveDataObserver: Observer<AsyncResult<Any>>

  @Mock
  lateinit var mockAsyncNullableResultLiveDataObserver: Observer<AsyncResult<Any?>>

  @Mock
  lateinit var mockAsyncAnswerOutcomeObserver: Observer<AsyncResult<AnsweredQuestionOutcome>>

  @Mock
  lateinit var mockAsyncHintObserver: Observer<AsyncResult<Hint>>

  @Mock
  lateinit var mockAsyncSolutionObserver: Observer<AsyncResult<Solution>>

  @Captor
  lateinit var currentQuestionResultCaptor: ArgumentCaptor<AsyncResult<EphemeralQuestion>>

  @Captor
  lateinit var asyncResultCaptor: ArgumentCaptor<AsyncResult<Any>>

  @Captor
  lateinit var asyncNullableResultCaptor: ArgumentCaptor<AsyncResult<Any?>>

  @Captor
  lateinit var asyncAnswerOutcomeCaptor: ArgumentCaptor<AsyncResult<AnsweredQuestionOutcome>>

  @ExperimentalCoroutinesApi1
  private val coroutineContext by lazy {
    EmptyCoroutineContext + testDispatcher
  }

  @Before
  fun setUp() {
    setUpTestApplicationWithSeed(questionSeed = 0)
  }

  @Test
  @ExperimentalCoroutinesApi1
  fun testGetCurrentQuestion_noSessionStarted_returnsPendingResult() =
    runBlockingTest1(coroutineContext) {
      val resultLiveData =
        questionAssessmentProgressController.getCurrentQuestion()
      resultLiveData.observeForever(mockCurrentQuestionLiveDataObserver)
      advanceUntilIdle()

      verify(mockCurrentQuestionLiveDataObserver).onChanged(currentQuestionResultCaptor.capture())
      assertThat(currentQuestionResultCaptor.value.isPending()).isTrue()
    }

  @Test
  @ExperimentalCoroutinesApi1
  fun testGetCurrentQuestion_sessionStarted_withEmptyQuestionList_fails() =
    runBlockingTest1(coroutineContext) {
      questionTrainingController.startQuestionTrainingSession(listOf())

      val resultLiveData =
        questionAssessmentProgressController.getCurrentQuestion()
      resultLiveData.observeForever(mockCurrentQuestionLiveDataObserver)
      advanceUntilIdle()

      verify(mockCurrentQuestionLiveDataObserver, atLeastOnce()).onChanged(
        currentQuestionResultCaptor.capture()
      )
      assertThat(currentQuestionResultCaptor.value.isFailure()).isTrue()
      assertThat(currentQuestionResultCaptor.value.getErrorOrNull())
        .hasCauseThat()
        .hasMessageThat()
        .contains("Expected at least 1 question")
    }

  @Test
  @ExperimentalCoroutinesApi1
  fun testStartTrainingSession_succeeds() = runBlockingTest1(coroutineContext) {
    val resultLiveData =
      questionTrainingController.startQuestionTrainingSession(TEST_SKILL_ID_LIST_012)
    resultLiveData.observeForever(mockAsyncResultLiveDataObserver)
    advanceUntilIdle()

    verify(mockAsyncResultLiveDataObserver).onChanged(asyncResultCaptor.capture())
    assertThat(asyncResultCaptor.value.isSuccess()).isTrue()
  }

  @Test
  @ExperimentalCoroutinesApi1
  fun testGetCurrentQuestion_playSession_returnsPendingResultFromLoadingSession() =
    runBlockingTest1(
      coroutineContext
    ) {
      val currentQuestionLiveData =
        questionAssessmentProgressController.getCurrentQuestion()
      currentQuestionLiveData.observeForever(mockCurrentQuestionLiveDataObserver)
      advanceUntilIdle()

      startTrainingSession(TEST_SKILL_ID_LIST_012)

      // The second-to-latest result stays pending since the session was loading (the actual result is the fully
      // loaded session). This is only true if the observer begins before starting to load the session.
      verify(mockCurrentQuestionLiveDataObserver, Mockito.atLeast(2)).onChanged(
        currentQuestionResultCaptor.capture()
      )
      val results = currentQuestionResultCaptor.allValues
      assertThat(results[results.size - 2].isPending()).isTrue()
    }

  @Test
  @ExperimentalCoroutinesApi1
  fun testGetCurrentQuestion_playSession_loaded_returnsInitialQuestionPending() = runBlockingTest1(
    coroutineContext
  ) {
    startTrainingSession(TEST_SKILL_ID_LIST_012)

    val currentQuestionLiveData =
      questionAssessmentProgressController.getCurrentQuestion()
    currentQuestionLiveData.observeForever(mockCurrentQuestionLiveDataObserver)
    advanceUntilIdle()

    verify(
      mockCurrentQuestionLiveDataObserver,
      atLeastOnce()
    ).onChanged(currentQuestionResultCaptor.capture())
    assertThat(currentQuestionResultCaptor.value.isSuccess()).isTrue()
    val ephemeralQuestion = currentQuestionResultCaptor.value.getOrThrow()
    assertThat(ephemeralQuestion.currentQuestionIndex).isEqualTo(0)
    assertThat(ephemeralQuestion.totalQuestionCount).isGreaterThan(0)
    assertThat(ephemeralQuestion.ephemeralState.stateTypeCase).isEqualTo(PENDING_STATE)
    assertThat(ephemeralQuestion.ephemeralState.state.content.html)
      .contains("What fraction does 'quarter'")
  }

  @Test
  @ExperimentalCoroutinesApi1
  fun testGetCurrentQuestion_playInvalidSession_thenPlayValidExp_returnsInitialPendingQuestion() =
    runBlockingTest1(
      coroutineContext
    ) {
      // Start with starting an invalid training session.
      startTrainingSession(listOf())
      endTrainingSession()

      // Then a valid one.
      startTrainingSession(TEST_SKILL_ID_LIST_012)
      val currentQuestionLiveData =
        questionAssessmentProgressController.getCurrentQuestion()
      currentQuestionLiveData.observeForever(mockCurrentQuestionLiveDataObserver)
      advanceUntilIdle()

      // The latest result should correspond to the valid ID, and the progress controller should gracefully recover.
      verify(mockCurrentQuestionLiveDataObserver, atLeastOnce()).onChanged(
        currentQuestionResultCaptor.capture()
      )
      assertThat(currentQuestionResultCaptor.value.isSuccess()).isTrue()
      val ephemeralQuestion = currentQuestionResultCaptor.value.getOrThrow()
      assertThat(ephemeralQuestion.currentQuestionIndex).isEqualTo(0)
      assertThat(ephemeralQuestion.totalQuestionCount).isGreaterThan(0)
      assertThat(ephemeralQuestion.ephemeralState.stateTypeCase).isEqualTo(PENDING_STATE)
      assertThat(ephemeralQuestion.ephemeralState.state.content.html)
        .contains("What fraction does 'quarter'")
    }

  @Test
  @ExperimentalCoroutinesApi1
  fun testStopTrainingSession_withoutStartingSession_fails() = runBlockingTest1(coroutineContext) {
    val resultLiveData =
      questionTrainingController.stopQuestionTrainingSession()
    advanceUntilIdle()

    assertThat(resultLiveData.value).isNotNull()
    assertThat(resultLiveData.value!!.isFailure()).isTrue()
    assertThat(resultLiveData.value!!.getErrorOrNull())
      .hasMessageThat()
      .contains("Cannot stop a new training session which wasn't started")
  }

  @Test
  @ExperimentalCoroutinesApi1
  fun testStartTrainingSession_withoutFinishingPrevious_fails() =
    runBlockingTest1(coroutineContext) {
      questionTrainingController.startQuestionTrainingSession(TEST_SKILL_ID_LIST_012)

      val resultLiveData =
        questionTrainingController.startQuestionTrainingSession(TEST_SKILL_ID_LIST_02)
      advanceUntilIdle()

      assertThat(resultLiveData.value).isNotNull()
      assertThat(resultLiveData.value!!.isFailure()).isTrue()
      assertThat(resultLiveData.value!!.getErrorOrNull())
        .hasMessageThat()
        .contains("Cannot start a new training session until the previous one is completed")
    }

  @Test
  @ExperimentalCoroutinesApi1
  fun testStopTrainingSession_afterStartingPreviousSession_succeeds() =
    runBlockingTest1(coroutineContext) {
      questionTrainingController.startQuestionTrainingSession(TEST_SKILL_ID_LIST_012)

      val resultLiveData =
        questionTrainingController.stopQuestionTrainingSession()
      advanceUntilIdle()

      assertThat(resultLiveData.value).isNotNull()
      assertThat(resultLiveData.value!!.isSuccess()).isTrue()
    }

  @Test
  @ExperimentalCoroutinesApi1
  fun testGetCurrentQuestion_playSecondSession_afterFinishingPrev_loaded_returnsInitialQuestion() =
    runBlockingTest1(
      coroutineContext
    ) {
      val currentQuestionLiveData =
        questionAssessmentProgressController.getCurrentQuestion()
      currentQuestionLiveData.observeForever(mockCurrentQuestionLiveDataObserver)
      // Start with playing a valid session, then stop.
      startTrainingSession(TEST_SKILL_ID_LIST_012)
      endTrainingSession()

      // Then another valid one.
      startTrainingSession(TEST_SKILL_ID_LIST_2)

      // The latest result should correspond to the valid ID, and the progress controller should gracefully recover.
      verify(mockCurrentQuestionLiveDataObserver, atLeastOnce()).onChanged(
        currentQuestionResultCaptor.capture()
      )
      assertThat(currentQuestionResultCaptor.value.isSuccess()).isTrue()
      val ephemeralQuestion = currentQuestionResultCaptor.value.getOrThrow()
      assertThat(ephemeralQuestion.currentQuestionIndex).isEqualTo(0)
      assertThat(ephemeralQuestion.totalQuestionCount).isGreaterThan(0)
      assertThat(ephemeralQuestion.ephemeralState.stateTypeCase).isEqualTo(PENDING_STATE)
      assertThat(ephemeralQuestion.ephemeralState.state.content.html)
        .contains("of a cake, what does the 10 represent?")
    }

  @Test
  @ExperimentalCoroutinesApi1
  fun testSubmitAnswer_beforePlaying_failsWithError() = runBlockingTest1(coroutineContext) {
    val result =
      questionAssessmentProgressController.submitAnswer(createMultipleChoiceAnswer(0))
    result.observeForever(mockAsyncAnswerOutcomeObserver)
    advanceUntilIdle()

    // Verify that the answer submission failed.
    verify(
      mockAsyncAnswerOutcomeObserver,
      atLeastOnce()
    ).onChanged(asyncAnswerOutcomeCaptor.capture())
    assertThat(asyncAnswerOutcomeCaptor.value.isFailure()).isTrue()
    assertThat(asyncAnswerOutcomeCaptor.value.getErrorOrNull())
      .hasMessageThat()
      .contains("Cannot submit an answer if a training session has not yet begun.")
  }

  @Test
  @ExperimentalCoroutinesApi1
  fun testSubmitAnswer_forMultipleChoice_correctAnswer_succeeds() =
    runBlockingTest1(coroutineContext) {
      setUpTestApplicationWithSeed(questionSeed = 6)
      subscribeToCurrentQuestionToAllowSessionToLoad()
      startTrainingSession(TEST_SKILL_ID_LIST_2)

      val result =
        questionAssessmentProgressController.submitAnswer(createMultipleChoiceAnswer(1))
      result.observeForever(mockAsyncAnswerOutcomeObserver)
      advanceUntilIdle()

      // Verify that the answer submission was successful.
      verify(
        mockAsyncAnswerOutcomeObserver,
        atLeastOnce()
      ).onChanged(asyncAnswerOutcomeCaptor.capture())
      assertThat(asyncAnswerOutcomeCaptor.value.isSuccess()).isTrue()
    }

  @Test
  @ExperimentalCoroutinesApi1
  fun testSubmitAnswer_forMultipleChoice_correctAnswer_returnsOutcomeWithTransition() =
    runBlockingTest1(
      coroutineContext
    ) {
      setUpTestApplicationWithSeed(questionSeed = 6)
      subscribeToCurrentQuestionToAllowSessionToLoad()
      startTrainingSession(TEST_SKILL_ID_LIST_2)

      val result =
        questionAssessmentProgressController.submitAnswer(createMultipleChoiceAnswer(1))
      result.observeForever(mockAsyncAnswerOutcomeObserver)
      advanceUntilIdle()

      // Verify that the answer submission was successful.
      verify(
        mockAsyncAnswerOutcomeObserver,
        atLeastOnce()
      ).onChanged(asyncAnswerOutcomeCaptor.capture())
      val answerOutcome = asyncAnswerOutcomeCaptor.value.getOrThrow()
      assertThat(answerOutcome.feedback.html).contains("That's correct!")
      assertThat(answerOutcome.isCorrectAnswer).isTrue()
    }

  @Test
  @ExperimentalCoroutinesApi1
  fun testSubmitAnswer_forMultipleChoice_wrongAnswer_succeeds() = runBlockingTest1(
    coroutineContext
  ) {
    setUpTestApplicationWithSeed(questionSeed = 6)
    subscribeToCurrentQuestionToAllowSessionToLoad()
    startTrainingSession(TEST_SKILL_ID_LIST_2)

    val result =
      questionAssessmentProgressController.submitAnswer(createMultipleChoiceAnswer(0))
    result.observeForever(mockAsyncAnswerOutcomeObserver)
    advanceUntilIdle()

    // Verify that the answer submission was successful.
    verify(
      mockAsyncAnswerOutcomeObserver,
      atLeastOnce()
    ).onChanged(asyncAnswerOutcomeCaptor.capture())
    assertThat(asyncAnswerOutcomeCaptor.value.isSuccess()).isTrue()
  }

  @Test
  @ExperimentalCoroutinesApi1
  fun testSubmitAnswer_forMultChoice_wrongAnswer_providesDefaultFeedbackAndNewQuestionTransition() =
    runBlockingTest1(
      coroutineContext
    ) {
      setUpTestApplicationWithSeed(questionSeed = 6)
      subscribeToCurrentQuestionToAllowSessionToLoad()
      startTrainingSession(TEST_SKILL_ID_LIST_2)

      val result =
        questionAssessmentProgressController.submitAnswer(createMultipleChoiceAnswer(0))
      result.observeForever(mockAsyncAnswerOutcomeObserver)
      advanceUntilIdle()

      // Verify that the answer submission was successful.
      verify(
        mockAsyncAnswerOutcomeObserver,
        atLeastOnce()
      ).onChanged(asyncAnswerOutcomeCaptor.capture())
      val answerOutcome = asyncAnswerOutcomeCaptor.value.getOrThrow()
      assertThat(answerOutcome.feedback.html).isEmpty()
      assertThat(answerOutcome.isCorrectAnswer).isFalse()
    }

  @Test
  @ExperimentalCoroutinesApi1
  fun testGetCurrentQuestion_afterSubmittingCorrectMultiChoiceAnswer_becomesCompletedQuestion() =
    runBlockingTest1(
      coroutineContext
    ) {
      setUpTestApplicationWithSeed(questionSeed = 6)
      val currentQuestionLiveData =
        questionAssessmentProgressController.getCurrentQuestion()
      currentQuestionLiveData.observeForever(mockCurrentQuestionLiveDataObserver)
      startTrainingSession(TEST_SKILL_ID_LIST_2)

      submitMultipleChoiceAnswer(1)

      // Verify that the current state updates. It should stay pending, and the wrong answer should be appended.
      verify(mockCurrentQuestionLiveDataObserver, atLeastOnce()).onChanged(
        currentQuestionResultCaptor.capture()
      )
      assertThat(currentQuestionResultCaptor.value.isSuccess()).isTrue()
      val ephemeralQuestion = currentQuestionResultCaptor.value.getOrThrow()
      assertThat(ephemeralQuestion.currentQuestionIndex)
        .isEqualTo(0)
      assertThat(ephemeralQuestion.totalQuestionCount)
        .isEqualTo(3)
      assertThat(ephemeralQuestion.ephemeralState.stateTypeCase)
        .isEqualTo(COMPLETED_STATE)
      val completedState = ephemeralQuestion.ephemeralState.completedState
      assertThat(completedState.getAnswer(0).userAnswer.answer.nonNegativeInt)
        .isEqualTo(1)
      assertThat(completedState.getAnswer(0).feedback.html)
        .contains("That's correct!")
    }

  @Test
  @ExperimentalCoroutinesApi1
  fun testGetCurrentQuestion_afterSubmittingWrongMultiChoiceAnswer_updatesPendingQuestion() =
    runBlockingTest1(
      coroutineContext
    ) {
      setUpTestApplicationWithSeed(questionSeed = 6)
      val currentQuestionLiveData =
        questionAssessmentProgressController.getCurrentQuestion()
      currentQuestionLiveData.observeForever(mockCurrentQuestionLiveDataObserver)
      startTrainingSession(TEST_SKILL_ID_LIST_2)

      submitMultipleChoiceAnswer(0)

      // Verify that the current state updates. It should stay pending, and the wrong answer should be appended.
      verify(mockCurrentQuestionLiveDataObserver, atLeastOnce()).onChanged(
        currentQuestionResultCaptor.capture()
      )
      assertThat(currentQuestionResultCaptor.value.isSuccess()).isTrue()
      val ephemeralQuestion = currentQuestionResultCaptor.value.getOrThrow()
      assertThat(ephemeralQuestion.currentQuestionIndex)
        .isEqualTo(0)
      assertThat(ephemeralQuestion.totalQuestionCount)
        .isEqualTo(3)
      assertThat(ephemeralQuestion.ephemeralState.stateTypeCase)
        .isEqualTo(PENDING_STATE)
      val pendingState = ephemeralQuestion.ephemeralState.pendingState
      assertThat(pendingState.getWrongAnswer(0).userAnswer.answer.nonNegativeInt)
        .isEqualTo(0)
      assertThat(pendingState.getWrongAnswer(0).feedback.html)
        .isEmpty()
    }

  @Test
  @ExperimentalCoroutinesApi1
  fun testGetCurrentQuestion_afterSubmittingWrongThenRightAnswer_updatesToQuestionWithBothAnswers() = // ktlint-disable max-line-length
    runBlockingTest1(
      coroutineContext
    ) {
      setUpTestApplicationWithSeed(questionSeed = 6)
      subscribeToCurrentQuestionToAllowSessionToLoad()
      startTrainingSession(TEST_SKILL_ID_LIST_2)
      submitMultipleChoiceAnswer(0)

      submitMultipleChoiceAnswer(1)

      // Verify that the current state updates. It should now be completed with both the wrong and correct answers.
      verify(mockCurrentQuestionLiveDataObserver, atLeastOnce()).onChanged(
        currentQuestionResultCaptor.capture()
      )
      assertThat(currentQuestionResultCaptor.value.isSuccess()).isTrue()
      val ephemeralQuestion = currentQuestionResultCaptor.value.getOrThrow()
      assertThat(ephemeralQuestion.currentQuestionIndex)
        .isEqualTo(0)
      assertThat(ephemeralQuestion.totalQuestionCount)
        .isEqualTo(3)
      assertThat(ephemeralQuestion.ephemeralState.stateTypeCase)
        .isEqualTo(COMPLETED_STATE)
      val completedState = ephemeralQuestion.ephemeralState.completedState
      assertThat(completedState.getAnswer(0).userAnswer.answer.nonNegativeInt)
        .isEqualTo(0)
      assertThat(completedState.getAnswer(0).feedback.html)
        .isEmpty()
      assertThat(completedState.getAnswer(1).userAnswer.answer.nonNegativeInt)
        .isEqualTo(1)
      assertThat(completedState.getAnswer(1).feedback.html)
        .contains("That's correct!")
    }

  @Test
  @ExperimentalCoroutinesApi1
  fun testMoveToNext_beforePlaying_failsWithError() = runBlockingTest1(coroutineContext) {
    val moveToStateResult =
      questionAssessmentProgressController.moveToNextQuestion()
    moveToStateResult.observeForever(mockAsyncNullableResultLiveDataObserver)

    verify(mockAsyncNullableResultLiveDataObserver, atLeastOnce()).onChanged(
      asyncNullableResultCaptor.capture()
    )
    assertThat(asyncNullableResultCaptor.value.isFailure()).isTrue()
    assertThat(asyncNullableResultCaptor.value.getErrorOrNull())
      .hasMessageThat()
      .contains("Cannot navigate to a next question if a training session has not begun.")
  }

  @Test
  @ExperimentalCoroutinesApi1
  fun testMoveToNext_forPendingInitialQuestion_failsWithError() =
    runBlockingTest1(coroutineContext) {
      subscribeToCurrentQuestionToAllowSessionToLoad()
      startTrainingSession(TEST_SKILL_ID_LIST_2)

      val moveToStateResult =
        questionAssessmentProgressController.moveToNextQuestion()
      moveToStateResult.observeForever(mockAsyncNullableResultLiveDataObserver)
      advanceUntilIdle()

      // Verify that we can't move ahead since the current state isn't yet completed.
      verify(mockAsyncNullableResultLiveDataObserver, atLeastOnce()).onChanged(
        asyncNullableResultCaptor.capture()
      )
      assertThat(asyncNullableResultCaptor.value.isFailure()).isTrue()
      assertThat(asyncNullableResultCaptor.value.getErrorOrNull())
        .hasMessageThat()
        .contains("Cannot navigate to next state; at most recent state.")
    }

  @Test
  @ExperimentalCoroutinesApi1
  fun testMoveToNext_forCompletedQuestion_succeeds() = runBlockingTest1(coroutineContext) {
    setUpTestApplicationWithSeed(questionSeed = 6)
    subscribeToCurrentQuestionToAllowSessionToLoad()
    startTrainingSession(TEST_SKILL_ID_LIST_2)
    submitMultipleChoiceAnswer(1)

    val moveToStateResult =
      questionAssessmentProgressController.moveToNextQuestion()
    moveToStateResult.observeForever(mockAsyncNullableResultLiveDataObserver)
    advanceUntilIdle()

    verify(mockAsyncNullableResultLiveDataObserver, atLeastOnce()).onChanged(
      asyncNullableResultCaptor.capture()
    )
    assertThat(asyncNullableResultCaptor.value.isSuccess()).isTrue()
  }

  @Test
  @ExperimentalCoroutinesApi1
  fun testMoveToNext_forCompletedQuestion_movesToNextQuestion() =
    runBlockingTest1(coroutineContext) {
      setUpTestApplicationWithSeed(questionSeed = 6)
      val currentQuestionLiveData =
        questionAssessmentProgressController.getCurrentQuestion()
      currentQuestionLiveData.observeForever(mockCurrentQuestionLiveDataObserver)
      startTrainingSession(TEST_SKILL_ID_LIST_2)
      submitMultipleChoiceAnswer(1)

      moveToNextQuestion()

      verify(mockCurrentQuestionLiveDataObserver, atLeastOnce()).onChanged(
        currentQuestionResultCaptor.capture()
      )
      assertThat(currentQuestionResultCaptor.value.isSuccess()).isTrue()
      val currentQuestion = currentQuestionResultCaptor.value.getOrThrow()
      assertThat(currentQuestion.ephemeralState.state.content.html).contains("1/2 + 1/4")
    }

  @Test
  @ExperimentalCoroutinesApi1
  fun testMoveToNext_afterMovingFromCompletedQuestion_failsWithError() =
    runBlockingTest1(coroutineContext) {
      setUpTestApplicationWithSeed(questionSeed = 6)
      subscribeToCurrentQuestionToAllowSessionToLoad()
      startTrainingSession(TEST_SKILL_ID_LIST_2)
      submitMultipleChoiceAnswer(1)
      moveToNextQuestion()

      // Try skipping past the current state.
      val moveToStateResult =
        questionAssessmentProgressController.moveToNextQuestion()
      moveToStateResult.observeForever(mockAsyncNullableResultLiveDataObserver)
      advanceUntilIdle()

      // Verify we can't move ahead since the new state isn't yet completed.
      verify(mockAsyncNullableResultLiveDataObserver, atLeastOnce()).onChanged(
        asyncNullableResultCaptor.capture()
      )
      assertThat(asyncNullableResultCaptor.value.isFailure()).isTrue()
      assertThat(asyncNullableResultCaptor.value.getErrorOrNull())
        .hasMessageThat()
        .contains("Cannot navigate to next state; at most recent state.")
    }

  @Test
  @ExperimentalCoroutinesApi1
  fun testSubmitAnswer_forTextInput_correctAnswer_returnsOutcomeWithTransition() =
    runBlockingTest1(coroutineContext) {
      setUpTestApplicationWithSeed(questionSeed = 2)
      subscribeToCurrentQuestionToAllowSessionToLoad()
      startTrainingSession(TEST_SKILL_ID_LIST_01)

      val result =
        questionAssessmentProgressController.submitAnswer(createTextInputAnswer("1/4"))
      result.observeForever(mockAsyncAnswerOutcomeObserver)
      advanceUntilIdle()

      // Verify that the answer submission was successful.
      verify(
        mockAsyncAnswerOutcomeObserver,
        atLeastOnce()
      ).onChanged(asyncAnswerOutcomeCaptor.capture())
      val answerOutcome = asyncAnswerOutcomeCaptor.value.getOrThrow()
      assertThat(answerOutcome.isCorrectAnswer).isTrue()
      assertThat(answerOutcome.feedback.html).contains("That's correct!")
    }

  @Test
  @ExperimentalCoroutinesApi1
  fun testSubmitAnswer_forTextInput_wrongAnswer_returnsDefaultOutcome() =
    runBlockingTest1(coroutineContext) {
      setUpTestApplicationWithSeed(questionSeed = 2)
      subscribeToCurrentQuestionToAllowSessionToLoad()
      startTrainingSession(TEST_SKILL_ID_LIST_01)

      val result =
        questionAssessmentProgressController.submitAnswer(createTextInputAnswer("2/4"))
      result.observeForever(mockAsyncAnswerOutcomeObserver)
      advanceUntilIdle()

      // Verify that the answer was wrong, and that there's no handler for it so the default outcome is returned.
      verify(
        mockAsyncAnswerOutcomeObserver,
        atLeastOnce()
      ).onChanged(asyncAnswerOutcomeCaptor.capture())
      val answerOutcome = asyncAnswerOutcomeCaptor.value.getOrThrow()
      assertThat(answerOutcome.isCorrectAnswer).isFalse()
      assertThat(answerOutcome.feedback.html).isEmpty()
    }

  @Test
  @ExperimentalCoroutinesApi1
  fun testGetCurrentQuestion_secondQuestion_submitRightAnswer_pendingQuestionBecomesCompleted() =
    runBlockingTest1(
      coroutineContext
    ) {
      subscribeToCurrentQuestionToAllowSessionToLoad()
      startTrainingSession(TEST_SKILL_ID_LIST_2)
      submitNumericInputAnswerAndMoveToNextQuestion(3.0)

      val result =
        questionAssessmentProgressController.submitAnswer(createNumericInputAnswer(5.0))
      result.observeForever(mockAsyncAnswerOutcomeObserver)
      advanceUntilIdle()

      // Verify that the current state updates. It should stay pending, and the wrong answer should be appended.
      verify(mockCurrentQuestionLiveDataObserver, atLeastOnce()).onChanged(
        currentQuestionResultCaptor.capture()
      )
      assertThat(currentQuestionResultCaptor.value.isSuccess()).isTrue()
      val currentQuestion = currentQuestionResultCaptor.value.getOrThrow()
      assertThat(currentQuestion.currentQuestionIndex).isEqualTo(1)
      assertThat(currentQuestion.totalQuestionCount).isEqualTo(3)
      assertThat(currentQuestion.ephemeralState.stateTypeCase).isEqualTo(COMPLETED_STATE)
      val completedState = currentQuestion.ephemeralState.completedState
      assertThat(completedState.answerCount).isEqualTo(1)
      assertThat(completedState.getAnswer(0).userAnswer.answer.real)
        .isWithin(1e-5).of(5.0)
      assertThat(completedState.getAnswer(0).feedback.html).contains("That's correct!")
    }

  @Test
  @ExperimentalCoroutinesApi1
  fun testGetCurrentQuestion_secondQuestion_submitWrongAnswer_updatePendingQuestion() =
    runBlockingTest1(
      coroutineContext
    ) {
      subscribeToCurrentQuestionToAllowSessionToLoad()
      startTrainingSession(TEST_SKILL_ID_LIST_2)
      submitNumericInputAnswerAndMoveToNextQuestion(3.0)

      val result =
        questionAssessmentProgressController.submitAnswer(createNumericInputAnswer(4.0))
      result.observeForever(mockAsyncAnswerOutcomeObserver)
      advanceUntilIdle()

      // Verify that the current state updates. It should now be completed with the correct answer.
      verify(mockCurrentQuestionLiveDataObserver, atLeastOnce()).onChanged(
        currentQuestionResultCaptor.capture()
      )
      assertThat(currentQuestionResultCaptor.value.isSuccess()).isTrue()
      val currentQuestion = currentQuestionResultCaptor.value.getOrThrow()
      assertThat(currentQuestion.currentQuestionIndex).isEqualTo(1)
      assertThat(currentQuestion.totalQuestionCount).isEqualTo(3)
      assertThat(currentQuestion.ephemeralState.stateTypeCase).isEqualTo(PENDING_STATE)
      val pendingState = currentQuestion.ephemeralState.pendingState
      assertThat(pendingState.wrongAnswerCount).isEqualTo(1)
      assertThat(pendingState.getWrongAnswer(0).userAnswer.answer.real)
        .isWithin(1e-5).of(4.0)
      assertThat(pendingState.getWrongAnswer(0).feedback.html).isEmpty()
    }

  @Test
  @ExperimentalCoroutinesApi1
  fun testSubmitAnswer_forNumericInput_correctAnswer_returnsOutcomeWithTransition() =
    runBlockingTest1(
      coroutineContext
    ) {
      subscribeToCurrentQuestionToAllowSessionToLoad()
      startTrainingSession(TEST_SKILL_ID_LIST_2)

      val result =
        questionAssessmentProgressController.submitAnswer(createNumericInputAnswer(3.0)) // ktlint-disable max-line-length
      result.observeForever(mockAsyncAnswerOutcomeObserver)
      advanceUntilIdle()

      // Verify that the answer submission was successful.
      verify(
        mockAsyncAnswerOutcomeObserver,
        atLeastOnce()
      ).onChanged(asyncAnswerOutcomeCaptor.capture())
      val answerOutcome = asyncAnswerOutcomeCaptor.value.getOrThrow()
      assertThat(answerOutcome.isCorrectAnswer).isTrue()
      assertThat(answerOutcome.feedback.html).contains("That's correct!")
    }

  @Test
  @ExperimentalCoroutinesApi1
  fun testSubmitAnswer_forNumericInput_wrongAnswer_returnsOutcomeWithTransition() =
    runBlockingTest1(coroutineContext) {
      subscribeToCurrentQuestionToAllowSessionToLoad()
      startTrainingSession(TEST_SKILL_ID_LIST_2)

      val result =
        questionAssessmentProgressController.submitAnswer(createNumericInputAnswer(2.0)) // ktlint-disable max-line-length
      result.observeForever(mockAsyncAnswerOutcomeObserver)
      advanceUntilIdle()

      // Verify that the answer submission failed as expected.
      verify(
        mockAsyncAnswerOutcomeObserver,
        atLeastOnce()
      ).onChanged(asyncAnswerOutcomeCaptor.capture())
      val answerOutcome = asyncAnswerOutcomeCaptor.value.getOrThrow()
      assertThat(answerOutcome.isCorrectAnswer).isFalse()
      assertThat(answerOutcome.feedback.html).isEmpty()
    }

  @Test
  @ExperimentalCoroutinesApi1
  fun testGetCurrentQuestion_thirdQuestion_isTerminalQuestion() =
    runBlockingTest1(coroutineContext) {
      val currentQuestionLiveData =
        questionAssessmentProgressController.getCurrentQuestion()
      currentQuestionLiveData.observeForever(mockCurrentQuestionLiveDataObserver)
      startTrainingSession(TEST_SKILL_ID_LIST_2)
      submitNumericInputAnswerAndMoveToNextQuestion(3.0)
      submitNumericInputAnswerAndMoveToNextQuestion(5.0)

      submitMultipleChoiceAnswerAndMoveToNextQuestion(1)

      // Verify that the third state is terminal.
      verify(mockCurrentQuestionLiveDataObserver, atLeastOnce()).onChanged(
        currentQuestionResultCaptor.capture()
      )
      assertThat(currentQuestionResultCaptor.value.isSuccess()).isTrue()
      val currentQuestion = currentQuestionResultCaptor.value.getOrThrow()
      assertThat(currentQuestion.currentQuestionIndex).isEqualTo(3)
      assertThat(currentQuestion.totalQuestionCount).isEqualTo(3)
      assertThat(currentQuestion.ephemeralState.stateTypeCase).isEqualTo(TERMINAL_STATE)
    }

  @Test
  @ExperimentalCoroutinesApi1
  fun testMoveToNext_onFinalQuestion_failsWithError() = runBlockingTest1(coroutineContext) {
    subscribeToCurrentQuestionToAllowSessionToLoad()
    startTrainingSession(TEST_SKILL_ID_LIST_2)
    submitNumericInputAnswerAndMoveToNextQuestion(3.0)
    submitNumericInputAnswerAndMoveToNextQuestion(5.0)
    submitMultipleChoiceAnswerAndMoveToNextQuestion(1)

    val moveToStateResult =
      questionAssessmentProgressController.moveToNextQuestion()
    moveToStateResult.observeForever(mockAsyncNullableResultLiveDataObserver)
    advanceUntilIdle()

    // Verify we can't navigate past the last state of the training session.
    verify(mockAsyncNullableResultLiveDataObserver, atLeastOnce()).onChanged(
      asyncNullableResultCaptor.capture()
    )
    assertThat(asyncNullableResultCaptor.value.isFailure()).isTrue()
    assertThat(asyncNullableResultCaptor.value.getErrorOrNull())
      .hasMessageThat()
      .contains("Cannot navigate to next state; at most recent state.")
  }

  @Test
  @ExperimentalCoroutinesApi1
  fun testGetCurrentQuestion_afterPlayingSecondSession_returnsTerminalQuestion() =
    runBlockingTest1(coroutineContext) {
      val currentQuestionLiveData =
        questionAssessmentProgressController.getCurrentQuestion()
      currentQuestionLiveData.observeForever(mockCurrentQuestionLiveDataObserver)

      startTrainingSession(TEST_SKILL_ID_LIST_01)
      submitMultipleChoiceAnswerAndMoveToNextQuestion(1) // question 1
      submitNumericInputAnswerAndMoveToNextQuestion(3.0) // question 2
      submitTextInputAnswerAndMoveToNextQuestion("1/2") // question 3

      // Verify that we're now on the final state.
      verify(mockCurrentQuestionLiveDataObserver, atLeastOnce()).onChanged(
        currentQuestionResultCaptor.capture()
      )
      assertThat(currentQuestionResultCaptor.value.isSuccess()).isTrue()
      val currentQuestion = currentQuestionResultCaptor.value.getOrThrow()
      assertThat(currentQuestion.ephemeralState.stateTypeCase).isEqualTo(TERMINAL_STATE)
    }

  @Test
  @ExperimentalCoroutinesApi1
  fun testGetCurrentQuestion_afterPlayingThroughPreviousSessions_returnsQuestionFromSecondSession() = // ktlint-disable max-line-length
    runBlockingTest1(
      coroutineContext
    ) {
      val currentQuestionLiveData =
        questionAssessmentProgressController.getCurrentQuestion()
      currentQuestionLiveData.observeForever(mockCurrentQuestionLiveDataObserver)
      playThroughSessionWithSkillList2()

      startTrainingSession(TEST_SKILL_ID_LIST_01)
      submitTextInputAnswerAndMoveToNextQuestion("1/4") // question 0
      submitMultipleChoiceAnswerAndMoveToNextQuestion(1) // question 1

      // Verify that we're on the second-to-last state of the second session.
      verify(mockCurrentQuestionLiveDataObserver, atLeastOnce()).onChanged(
        currentQuestionResultCaptor.capture()
      )
      assertThat(currentQuestionResultCaptor.value.isSuccess()).isTrue()
      val currentQuestion = currentQuestionResultCaptor.value.getOrThrow()
      assertThat(currentQuestion.currentQuestionIndex).isEqualTo(2)
      assertThat(currentQuestion.totalQuestionCount).isEqualTo(3)
      assertThat(currentQuestion.ephemeralState.stateTypeCase).isEqualTo(PENDING_STATE)
      // This question is not in the other test session.
      assertThat(currentQuestion.ephemeralState.state.content.html)
        .contains("What fraction does 'half'")
    }

  @Test
  @ExperimentalCoroutinesApi1
  fun testMoveToNext_onFinalQuestion_failsWithError_logsException() =
    runBlockingTest1(coroutineContext) {
      subscribeToCurrentQuestionToAllowSessionToLoad()
      startTrainingSession(TEST_SKILL_ID_LIST_2)
      submitNumericInputAnswerAndMoveToNextQuestion(3.0)
      submitNumericInputAnswerAndMoveToNextQuestion(5.0)
      submitMultipleChoiceAnswerAndMoveToNextQuestion(1)

      val moveToStateResult =
        questionAssessmentProgressController.moveToNextQuestion()
      moveToStateResult.observeForever(mockAsyncNullableResultLiveDataObserver)
      advanceUntilIdle()
      val exception = fakeExceptionLogger.getMostRecentException()

      assertThat(exception).isInstanceOf(IllegalStateException::class.java)
      assertThat(exception).hasMessageThat()
        .contains("Cannot navigate to next state; at most recent state.")
    }

  @Test
  @ExperimentalCoroutinesApi1
  fun testSubmitAnswer_beforePlaying_failsWithError_logsException() =
    runBlockingTest1(coroutineContext) {
      val result =
        questionAssessmentProgressController.submitAnswer(createMultipleChoiceAnswer(0))
      result.observeForever(mockAsyncAnswerOutcomeObserver)
      advanceUntilIdle()
      val exception = fakeExceptionLogger.getMostRecentException()

      assertThat(exception).isInstanceOf(IllegalStateException::class.java)
      assertThat(exception)
        .hasMessageThat()
        .contains("Cannot submit an answer if a training session has not yet begun.")
    }

  @Test
  @ExperimentalCoroutinesApi1
  fun testSubmitAnswer_forTextInput_wrongAnswer_returnsDefaultOutcome_showHint() = runBlockingTest1(
    coroutineContext
  ) {
    subscribeToCurrentQuestionToAllowSessionToLoad()
    startTrainingSession(TEST_SKILL_ID_LIST_2)

    val result =
      questionAssessmentProgressController.submitAnswer(createNumericInputAnswer(2.0))
    result.observeForever(mockAsyncAnswerOutcomeObserver)
    advanceUntilIdle()

    // Verify that the answer submission failed as expected.
    verify(
      mockAsyncAnswerOutcomeObserver,
      atLeastOnce()
    ).onChanged(asyncAnswerOutcomeCaptor.capture())
    val answerOutcome = asyncAnswerOutcomeCaptor.value.getOrThrow()
    assertThat(answerOutcome.isCorrectAnswer).isFalse()
    assertThat(answerOutcome.feedback.html).isEmpty()

    val currentQuestionLiveData =
      questionAssessmentProgressController.getCurrentQuestion()
    currentQuestionLiveData.observeForever(mockCurrentQuestionLiveDataObserver)
    advanceUntilIdle()

    verify(
      mockCurrentQuestionLiveDataObserver,
      atLeastOnce()
    ).onChanged(currentQuestionResultCaptor.capture())
    assertThat(currentQuestionResultCaptor.value.isSuccess()).isTrue()
    val ephemeralQuestion = currentQuestionResultCaptor.value.getOrThrow()

    assertThat(ephemeralQuestion.ephemeralState.stateTypeCase).isEqualTo(PENDING_STATE)
    assertThat(ephemeralQuestion.ephemeralState.pendingState.wrongAnswerCount).isEqualTo(1)

    val hintAndSolution = ephemeralQuestion.ephemeralState.state.interaction.getHint(0)
    assertThat(hintAndSolution.hintContent.html).contains("Hint text will appear here")
  }

  @Test
  @ExperimentalCoroutinesApi1
  fun testRevealHint_forWrongAnswer_showHint_returnHintIsRevealed() = runBlockingTest1(
    coroutineContext
  ) {
    val currentQuestionLiveData =
      questionAssessmentProgressController.getCurrentQuestion()
    currentQuestionLiveData.observeForever(mockCurrentQuestionLiveDataObserver)
    playThroughSessionWithSkillList2()

    startTrainingSession(TEST_SKILL_ID_LIST_01)
    submitTextInputAnswerAndMoveToNextQuestion("1/4") // question 0
    submitMultipleChoiceAnswerAndMoveToNextQuestion(2) // question 1

    // Verify that we're on the second-to-last state of the second session.
    verify(mockCurrentQuestionLiveDataObserver, atLeastOnce()).onChanged(
      currentQuestionResultCaptor.capture()
    )
    assertThat(currentQuestionResultCaptor.value.isSuccess()).isTrue()
    val currentQuestion = currentQuestionResultCaptor.value.getOrThrow()
    assertThat(currentQuestion.currentQuestionIndex).isEqualTo(1)
    assertThat(currentQuestion.ephemeralState.stateTypeCase).isEqualTo(PENDING_STATE)
    // This question is not in the other test session.
    assertThat(currentQuestion.ephemeralState.state.content.html)
      .contains("If we talk about wanting")

    verify(
      mockCurrentQuestionLiveDataObserver,
      atLeastOnce()
    ).onChanged(currentQuestionResultCaptor.capture())
    assertThat(currentQuestionResultCaptor.value.isSuccess()).isTrue()
    val ephemeralQuestion = currentQuestionResultCaptor.value.getOrThrow()

    assertThat(ephemeralQuestion.ephemeralState.stateTypeCase).isEqualTo(PENDING_STATE)
    assertThat(ephemeralQuestion.ephemeralState.pendingState.wrongAnswerCount)
      .isEqualTo(1)

    val hintAndSolution = ephemeralQuestion.ephemeralState.state.interaction.getHint(0)
    assertThat(hintAndSolution.hintContent.html).contains("Hint text will appear here")

    val result = questionAssessmentProgressController.submitHintIsRevealed(
      ephemeralQuestion.ephemeralState.state,
      /* hintIsRevealed= */ true,
      /* hintIndex= */ 0
    )
    result.observeForever(mockAsyncHintObserver)
    advanceUntilIdle()

    // Verify that the current state updates. Hint revealed is true.
    verify(
      mockCurrentQuestionLiveDataObserver,
      atLeastOnce()
    ).onChanged(currentQuestionResultCaptor.capture())
    assertThat(currentQuestionResultCaptor.value.isSuccess()).isTrue()
    val updatedState = currentQuestionResultCaptor.value.getOrThrow()
    assertThat(updatedState.ephemeralState.state.interaction.getHint(0).hintIsRevealed)
      .isTrue()
  }

  @Test
  @ExperimentalCoroutinesApi1
  fun testRevealSolution_forWrongAnswer_showSolution_returnSolutionIsRevealed() = runBlockingTest1(
    coroutineContext
  ) {
    val currentQuestionLiveData =
      questionAssessmentProgressController.getCurrentQuestion()
    currentQuestionLiveData.observeForever(mockCurrentQuestionLiveDataObserver)
    playThroughSessionWithSkillList2()

    startTrainingSession(TEST_SKILL_ID_LIST_01)
    submitTextInputAnswerAndMoveToNextQuestion("1/4") // question 0
    submitMultipleChoiceAnswerAndMoveToNextQuestion(2) // question 1

    // Verify that we're on the second-to-last state of the second session.
    verify(mockCurrentQuestionLiveDataObserver, atLeastOnce()).onChanged(
      currentQuestionResultCaptor.capture()
    )
    assertThat(currentQuestionResultCaptor.value.isSuccess()).isTrue()
    val currentQuestion = currentQuestionResultCaptor.value.getOrThrow()
    assertThat(currentQuestion.currentQuestionIndex).isEqualTo(1)
    assertThat(currentQuestion.ephemeralState.stateTypeCase).isEqualTo(PENDING_STATE)
    // This question is not in the other test session.
    assertThat(currentQuestion.ephemeralState.state.content.html)
      .contains("If we talk about wanting")

    verify(
      mockCurrentQuestionLiveDataObserver,
      atLeastOnce()
    ).onChanged(currentQuestionResultCaptor.capture())
    assertThat(currentQuestionResultCaptor.value.isSuccess()).isTrue()
    val ephemeralQuestion = currentQuestionResultCaptor.value.getOrThrow()

    assertThat(ephemeralQuestion.ephemeralState.stateTypeCase).isEqualTo(PENDING_STATE)
    assertThat(ephemeralQuestion.ephemeralState.pendingState.wrongAnswerCount)
      .isEqualTo(1)

    val hintAndSolution = ephemeralQuestion.ephemeralState.state.interaction.solution
    assertThat(hintAndSolution.correctAnswer.correctAnswer)
      .contains("<p>The number of pieces of cake I want.</p>")

    val result = questionAssessmentProgressController.submitSolutionIsRevealed(
      ephemeralQuestion.ephemeralState.state,
      true
    )
    result.observeForever(mockAsyncSolutionObserver)
    advanceUntilIdle()

    // Verify that the current state updates. Hint revealed is true.
    verify(
      mockCurrentQuestionLiveDataObserver,
      atLeastOnce()
    ).onChanged(currentQuestionResultCaptor.capture())
    assertThat(currentQuestionResultCaptor.value.isSuccess()).isTrue()
    val updatedState = currentQuestionResultCaptor.value.getOrThrow()
    assertThat(updatedState.ephemeralState.state.interaction.solution.solutionIsRevealed).isTrue()
  }

  private fun setUpTestApplicationWithSeed(questionSeed: Long) {
    TestQuestionModule.questionSeed = questionSeed
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
    questionAssessmentProgressController.getCurrentQuestion()
      .observeForever(mockCurrentQuestionLiveDataObserver)
  }

  @ExperimentalCoroutinesApi1
  private fun startTrainingSession(skillIdList: List<String>) {
    questionTrainingController.startQuestionTrainingSession(skillIdList)
    testDispatcher.advanceUntilIdle()
  }

  @ExperimentalCoroutinesApi1
  private fun submitMultipleChoiceAnswer(choiceIndex: Int) {
    questionAssessmentProgressController.submitAnswer(createMultipleChoiceAnswer(choiceIndex))
    testDispatcher.advanceUntilIdle()
  }

  @ExperimentalCoroutinesApi1
  private fun submitTextInputAnswer(textAnswer: String) {
    questionAssessmentProgressController.submitAnswer(createTextInputAnswer(textAnswer))
    testDispatcher.advanceUntilIdle()
  }

  @ExperimentalCoroutinesApi1
  private fun submitNumericInputAnswer(numericAnswer: Double) {
    questionAssessmentProgressController.submitAnswer(createNumericInputAnswer(numericAnswer))
    testDispatcher.advanceUntilIdle()
  }

  @ExperimentalCoroutinesApi1
  private fun submitMultipleChoiceAnswerAndMoveToNextQuestion(choiceIndex: Int) {
    submitMultipleChoiceAnswer(choiceIndex)
    moveToNextQuestion()
  }

  @ExperimentalCoroutinesApi1
  private fun submitTextInputAnswerAndMoveToNextQuestion(textAnswer: String) {
    submitTextInputAnswer(textAnswer)
    moveToNextQuestion()
  }

  @ExperimentalCoroutinesApi1
  private fun submitNumericInputAnswerAndMoveToNextQuestion(numericAnswer: Double) {
    submitNumericInputAnswer(numericAnswer)
    moveToNextQuestion()
  }

  @ExperimentalCoroutinesApi1
  private fun moveToNextQuestion() {
    questionAssessmentProgressController.moveToNextQuestion()
    testDispatcher.advanceUntilIdle()
  }

  @ExperimentalCoroutinesApi1
  private fun endTrainingSession() {
    questionTrainingController.stopQuestionTrainingSession()
    testDispatcher.advanceUntilIdle()
  }

  @ExperimentalCoroutinesApi1
  private fun playThroughSessionWithSkillList2() {
    startTrainingSession(TEST_SKILL_ID_LIST_2)
    submitNumericInputAnswerAndMoveToNextQuestion(3.0)
    submitNumericInputAnswerAndMoveToNextQuestion(5.0)
    submitMultipleChoiceAnswerAndMoveToNextQuestion(1)
    endTrainingSession()
  }

  private fun createMultipleChoiceAnswer(choiceIndex: Int): UserAnswer {
    return UserAnswer.newBuilder()
      .setAnswer(InteractionObject.newBuilder().setNonNegativeInt(choiceIndex))
      .setPlainAnswer(choiceIndex.toString())
      .build()
  }

  private fun createTextInputAnswer(textAnswer: String): UserAnswer {
    return UserAnswer.newBuilder()
      .setAnswer(InteractionObject.newBuilder().setNormalizedString(textAnswer).build())
      .setPlainAnswer(textAnswer)
      .build()
  }

  private fun createNumericInputAnswer(numericAnswer: Double): UserAnswer {
    return UserAnswer.newBuilder()
      .setAnswer(InteractionObject.newBuilder().setReal(numericAnswer).build())
      .setPlainAnswer(numericAnswer.toString())
      .build()
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

    @ExperimentalCoroutinesApi1
    @Singleton
    @Provides
    @TestDispatcher
    fun provideTestDispatcher(): TestCoroutineDispatcher {
      return TestCoroutineDispatcher()
    }

    @ExperimentalCoroutinesApi1
    @Singleton
    @Provides
    @BackgroundDispatcher
    fun provideBackgroundDispatcher(
      @TestDispatcher testDispatcher: TestCoroutineDispatcher
    ): CoroutineDispatcher {
      return testDispatcher
    }

    @ExperimentalCoroutinesApi1
    @Singleton
    @Provides
    @BlockingDispatcher
    fun provideBlockingDispatcher(
      @TestDispatcher testDispatcher: TestCoroutineDispatcher
    ): CoroutineDispatcher {
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
    fun provideQuestionTrainingSeed(): Long = questionSeed
  }

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(
    modules = [
      TestModule::class, TestQuestionModule::class,
      ContinueModule::class, FractionInputModule::class,
      ItemSelectionInputModule::class, MultipleChoiceInputModule::class,
      NumberWithUnitsRuleModule::class, NumericInputRuleModule::class,
      TextInputRuleModule::class, InteractionsModule::class,
      DragDropSortInputModule::class, TestLogReportingModule::class,
      ImageClickInputModule::class, LogStorageModule::class
    ]
  )
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
