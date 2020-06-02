package org.oppia.domain.question

import android.app.Application
import android.content.Context
import androidx.lifecycle.Observer
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.google.firebase.crashlytics.FirebaseCrashlytics
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
import org.oppia.app.model.AnsweredQuestionOutcome
import org.oppia.app.model.EphemeralQuestion
import org.oppia.app.model.EphemeralState.StateTypeCase.COMPLETED_STATE
import org.oppia.app.model.EphemeralState.StateTypeCase.PENDING_STATE
import org.oppia.app.model.EphemeralState.StateTypeCase.TERMINAL_STATE
import org.oppia.app.model.InteractionObject
import org.oppia.app.model.UserAnswer
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
import org.oppia.util.firebase.CrashlyticsWrapper
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
import org.oppia.domain.topic.StoryProgressControllerTest.TestFirebaseModule

/** Tests for [QuestionAssessmentProgressController]. */
@RunWith(AndroidJUnit4::class)
@Config(manifest = Config.NONE)
class QuestionAssessmentProgressControllerTest {
  private val TEST_SKILL_ID_LIST_012 = listOf(TEST_SKILL_ID_0, TEST_SKILL_ID_1, TEST_SKILL_ID_2) // questions 0, 2, 3
  private val TEST_SKILL_ID_LIST_02 = listOf(TEST_SKILL_ID_0, TEST_SKILL_ID_2) // questions 2, 1, 5
  private val TEST_SKILL_ID_LIST_01 = listOf(TEST_SKILL_ID_0, TEST_SKILL_ID_1) // questions 2, 0, 3
  private val TEST_SKILL_ID_LIST_2 = listOf(TEST_SKILL_ID_2) // questions 4, 5, 2

  @Rule
  @JvmField
  val mockitoRule: MockitoRule = MockitoJUnit.rule()

  /**
   * Returns Mockito.any() as nullable type to avoid java.lang.IllegalStateException when
   * null is returned.
   */
  fun <T> any(): T = Mockito.any<T>()
// TODO (#1233): Add a MockitoHelper class to handle nullable versions of all mockito matchers

  @Inject lateinit var questionTrainingController: QuestionTrainingController

  @Inject lateinit var questionAssessmentProgressController: QuestionAssessmentProgressController

  @ExperimentalCoroutinesApi
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

  @Captor
  lateinit var currentQuestionResultCaptor: ArgumentCaptor<AsyncResult<EphemeralQuestion>>

  @Captor
  lateinit var asyncResultCaptor: ArgumentCaptor<AsyncResult<Any>>

  @Captor
  lateinit var asyncNullableResultCaptor: ArgumentCaptor<AsyncResult<Any?>>

  @Captor
  lateinit var asyncAnswerOutcomeCaptor: ArgumentCaptor<AsyncResult<AnsweredQuestionOutcome>>

  @ExperimentalCoroutinesApi
  private val coroutineContext by lazy {
    EmptyCoroutineContext + testDispatcher
  }

  @Before
  fun setUp() {
    setUpTestApplicationWithSeed(questionSeed = 0)
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
      .hasCauseThat()
      .hasMessageThat()
      .contains("Expected at least 1 question")
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
  fun testGetCurrentQuestion_playSession_loaded_returnsInitialQuestionPending() = runBlockingTest(
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
  fun testGetCurrentQuestion_playInvalidSession_thenPlayValidExp_returnsInitialPendingQuestion() = runBlockingTest(
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
    verify(TestFirebaseModule.mockCrashlyticsWrapper, atLeastOnce()).logException(any())
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
    verify(TestFirebaseModule.mockCrashlyticsWrapper, atLeastOnce()).logException(any())
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
  fun testGetCurrentQuestion_playSecondSession_afterFinishingPrev_loaded_returnsInitialQuestion() = runBlockingTest(
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
      .contains("Cannot submit an answer if a training session has not yet begun.")
    verify(TestFirebaseModule.mockCrashlyticsWrapper, atLeastOnce()).logException(any())
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testSubmitAnswer_forMultipleChoice_correctAnswer_succeeds() = runBlockingTest(coroutineContext) {
    setUpTestApplicationWithSeed(questionSeed = 6)
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
    setUpTestApplicationWithSeed(questionSeed = 6)
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
    setUpTestApplicationWithSeed(questionSeed = 6)
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
  fun testSubmitAnswer_forMultChoice_wrongAnswer_providesDefaultFeedbackAndNewQuestionTransition() = runBlockingTest(
    coroutineContext
  ) {
    setUpTestApplicationWithSeed(questionSeed = 6)
    subscribeToCurrentQuestionToAllowSessionToLoad()
    startTrainingSession(TEST_SKILL_ID_LIST_2)

    val result = questionAssessmentProgressController.submitAnswer(createMultipleChoiceAnswer(0))
    result.observeForever(mockAsyncAnswerOutcomeObserver)
    advanceUntilIdle()

    // Verify that the answer submission was successful.
    verify(mockAsyncAnswerOutcomeObserver, atLeastOnce()).onChanged(asyncAnswerOutcomeCaptor.capture())
    val answerOutcome = asyncAnswerOutcomeCaptor.value.getOrThrow()
    assertThat(answerOutcome.feedback.html).isEmpty()
    assertThat(answerOutcome.isCorrectAnswer).isFalse()
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testGetCurrentQuestion_afterSubmittingCorrectMultiChoiceAnswer_becomesCompletedQuestion() = runBlockingTest(
    coroutineContext
  ) {
    setUpTestApplicationWithSeed(questionSeed = 6)
    val currentQuestionLiveData = questionAssessmentProgressController.getCurrentQuestion()
    currentQuestionLiveData.observeForever(mockCurrentQuestionLiveDataObserver)
    startTrainingSession(TEST_SKILL_ID_LIST_2)

    submitMultipleChoiceAnswer(1)

    // Verify that the current state updates. It should stay pending, and the wrong answer should be appended.
    verify(mockCurrentQuestionLiveDataObserver, atLeastOnce()).onChanged(currentQuestionResultCaptor.capture())
    assertThat(currentQuestionResultCaptor.value.isSuccess()).isTrue()
    val ephemeralQuestion = currentQuestionResultCaptor.value.getOrThrow()
    assertThat(ephemeralQuestion.currentQuestionIndex).isEqualTo(0)
    assertThat(ephemeralQuestion.totalQuestionCount).isEqualTo(3)
    assertThat(ephemeralQuestion.ephemeralState.stateTypeCase).isEqualTo(COMPLETED_STATE)
    val completedState = ephemeralQuestion.ephemeralState.completedState
    assertThat(completedState.getAnswer(0).userAnswer.answer.nonNegativeInt).isEqualTo(1)
    assertThat(completedState.getAnswer(0).feedback.html).contains("That's correct!")
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testGetCurrentQuestion_afterSubmittingWrongMultiChoiceAnswer_updatesPendingQuestion() = runBlockingTest(
    coroutineContext
  ) {
    setUpTestApplicationWithSeed(questionSeed = 6)
    val currentQuestionLiveData = questionAssessmentProgressController.getCurrentQuestion()
    currentQuestionLiveData.observeForever(mockCurrentQuestionLiveDataObserver)
    startTrainingSession(TEST_SKILL_ID_LIST_2)

    submitMultipleChoiceAnswer(0)

    // Verify that the current state updates. It should stay pending, and the wrong answer should be appended.
    verify(mockCurrentQuestionLiveDataObserver, atLeastOnce()).onChanged(currentQuestionResultCaptor.capture())
    assertThat(currentQuestionResultCaptor.value.isSuccess()).isTrue()
    val ephemeralQuestion = currentQuestionResultCaptor.value.getOrThrow()
    assertThat(ephemeralQuestion.currentQuestionIndex).isEqualTo(0)
    assertThat(ephemeralQuestion.totalQuestionCount).isEqualTo(3)
    assertThat(ephemeralQuestion.ephemeralState.stateTypeCase).isEqualTo(PENDING_STATE)
    val pendingState = ephemeralQuestion.ephemeralState.pendingState
    assertThat(pendingState.getWrongAnswer(0).userAnswer.answer.nonNegativeInt).isEqualTo(0)
    assertThat(pendingState.getWrongAnswer(0).feedback.html).isEmpty()
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testGetCurrentQuestion_afterSubmittingWrongThenRightAnswer_updatesToQuestionWithBothAnswers() = runBlockingTest(
    coroutineContext
  ) {
    setUpTestApplicationWithSeed(questionSeed = 6)
    subscribeToCurrentQuestionToAllowSessionToLoad()
    startTrainingSession(TEST_SKILL_ID_LIST_2)
    submitMultipleChoiceAnswer(0)

    submitMultipleChoiceAnswer(1)

    // Verify that the current state updates. It should now be completed with both the wrong and correct answers.
    verify(mockCurrentQuestionLiveDataObserver, atLeastOnce()).onChanged(currentQuestionResultCaptor.capture())
    assertThat(currentQuestionResultCaptor.value.isSuccess()).isTrue()
    val ephemeralQuestion = currentQuestionResultCaptor.value.getOrThrow()
    assertThat(ephemeralQuestion.currentQuestionIndex).isEqualTo(0)
    assertThat(ephemeralQuestion.totalQuestionCount).isEqualTo(3)
    assertThat(ephemeralQuestion.ephemeralState.stateTypeCase).isEqualTo(COMPLETED_STATE)
    val completedState = ephemeralQuestion.ephemeralState.completedState
    assertThat(completedState.getAnswer(0).userAnswer.answer.nonNegativeInt).isEqualTo(0)
    assertThat(completedState.getAnswer(0).feedback.html).isEmpty()
    assertThat(completedState.getAnswer(1).userAnswer.answer.nonNegativeInt).isEqualTo(1)
    assertThat(completedState.getAnswer(1).feedback.html).contains("That's correct!")
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testMoveToNext_beforePlaying_failsWithError() = runBlockingTest(coroutineContext) {
    val moveToStateResult = questionAssessmentProgressController.moveToNextQuestion()
    moveToStateResult.observeForever(mockAsyncNullableResultLiveDataObserver)

    verify(mockAsyncNullableResultLiveDataObserver, atLeastOnce()).onChanged(asyncNullableResultCaptor.capture())
    assertThat(asyncNullableResultCaptor.value.isFailure()).isTrue()
    assertThat(asyncNullableResultCaptor.value.getErrorOrNull())
      .hasMessageThat()
      .contains("Cannot navigate to a next question if a training session has not begun.")
    verify(TestFirebaseModule.mockCrashlyticsWrapper, atLeastOnce()).logException(any())
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testMoveToNext_forPendingInitialQuestion_failsWithError() = runBlockingTest(coroutineContext) {
    subscribeToCurrentQuestionToAllowSessionToLoad()
    startTrainingSession(TEST_SKILL_ID_LIST_2)

    val moveToStateResult = questionAssessmentProgressController.moveToNextQuestion()
    moveToStateResult.observeForever(mockAsyncNullableResultLiveDataObserver)
    advanceUntilIdle()

    // Verify that we can't move ahead since the current state isn't yet completed.
    verify(mockAsyncNullableResultLiveDataObserver, atLeastOnce()).onChanged(asyncNullableResultCaptor.capture())
    assertThat(asyncNullableResultCaptor.value.isFailure()).isTrue()
    assertThat(asyncNullableResultCaptor.value.getErrorOrNull())
      .hasMessageThat()
      .contains("Cannot navigate to next state; at most recent state.")
    verify(TestFirebaseModule.mockCrashlyticsWrapper, atLeastOnce()).logException(any())
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testMoveToNext_forCompletedQuestion_succeeds() = runBlockingTest(coroutineContext) {
    setUpTestApplicationWithSeed(questionSeed = 6)
    subscribeToCurrentQuestionToAllowSessionToLoad()
    startTrainingSession(TEST_SKILL_ID_LIST_2)
    submitMultipleChoiceAnswer(1)

    val moveToStateResult = questionAssessmentProgressController.moveToNextQuestion()
    moveToStateResult.observeForever(mockAsyncNullableResultLiveDataObserver)
    advanceUntilIdle()

    verify(mockAsyncNullableResultLiveDataObserver, atLeastOnce()).onChanged(asyncNullableResultCaptor.capture())
    assertThat(asyncNullableResultCaptor.value.isSuccess()).isTrue()
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testMoveToNext_forCompletedQuestion_movesToNextQuestion() = runBlockingTest(coroutineContext) {
    setUpTestApplicationWithSeed(questionSeed = 6)
    val currentQuestionLiveData = questionAssessmentProgressController.getCurrentQuestion()
    currentQuestionLiveData.observeForever(mockCurrentQuestionLiveDataObserver)
    startTrainingSession(TEST_SKILL_ID_LIST_2)
    submitMultipleChoiceAnswer(1)

    moveToNextQuestion()

    verify(mockCurrentQuestionLiveDataObserver, atLeastOnce()).onChanged(currentQuestionResultCaptor.capture())
    assertThat(currentQuestionResultCaptor.value.isSuccess()).isTrue()
    val currentQuestion = currentQuestionResultCaptor.value.getOrThrow()
    assertThat(currentQuestion.ephemeralState.state.content.html).contains("frac{1}{4}")
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testMoveToNext_afterMovingFromCompletedQuestion_failsWithError() = runBlockingTest(coroutineContext) {
    setUpTestApplicationWithSeed(questionSeed = 6)
    subscribeToCurrentQuestionToAllowSessionToLoad()
    startTrainingSession(TEST_SKILL_ID_LIST_2)
    submitMultipleChoiceAnswer(1)
    moveToNextQuestion()

    // Try skipping past the current state.
    val moveToStateResult = questionAssessmentProgressController.moveToNextQuestion()
    moveToStateResult.observeForever(mockAsyncNullableResultLiveDataObserver)
    advanceUntilIdle()

    // Verify we can't move ahead since the new state isn't yet completed.
    verify(mockAsyncNullableResultLiveDataObserver, atLeastOnce()).onChanged(asyncNullableResultCaptor.capture())
    assertThat(asyncNullableResultCaptor.value.isFailure()).isTrue()
    assertThat(asyncNullableResultCaptor.value.getErrorOrNull())
      .hasMessageThat()
      .contains("Cannot navigate to next state; at most recent state.")
    verify(TestFirebaseModule.mockCrashlyticsWrapper, atLeastOnce()).logException(any())
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testSubmitAnswer_forTextInput_correctAnswer_returnsOutcomeWithTransition() = runBlockingTest(coroutineContext) {
    setUpTestApplicationWithSeed(questionSeed = 2)
    subscribeToCurrentQuestionToAllowSessionToLoad()
    startTrainingSession(TEST_SKILL_ID_LIST_01)

    val result = questionAssessmentProgressController.submitAnswer(createTextInputAnswer("1/4"))
    result.observeForever(mockAsyncAnswerOutcomeObserver)
    advanceUntilIdle()

    // Verify that the answer submission was successful.
    verify(mockAsyncAnswerOutcomeObserver, atLeastOnce()).onChanged(asyncAnswerOutcomeCaptor.capture())
    val answerOutcome = asyncAnswerOutcomeCaptor.value.getOrThrow()
    assertThat(answerOutcome.isCorrectAnswer).isTrue()
    assertThat(answerOutcome.feedback.html).contains("That's correct!")
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testSubmitAnswer_forTextInput_wrongAnswer_returnsDefaultOutcome() = runBlockingTest(coroutineContext) {
    setUpTestApplicationWithSeed(questionSeed = 2)
    subscribeToCurrentQuestionToAllowSessionToLoad()
    startTrainingSession(TEST_SKILL_ID_LIST_01)

    val result = questionAssessmentProgressController.submitAnswer(createTextInputAnswer("2/4"))
    result.observeForever(mockAsyncAnswerOutcomeObserver)
    advanceUntilIdle()

    // Verify that the answer was wrong, and that there's no handler for it so the default outcome is returned.
    verify(mockAsyncAnswerOutcomeObserver, atLeastOnce()).onChanged(asyncAnswerOutcomeCaptor.capture())
    val answerOutcome = asyncAnswerOutcomeCaptor.value.getOrThrow()
    assertThat(answerOutcome.isCorrectAnswer).isFalse()
    assertThat(answerOutcome.feedback.html).isEmpty()
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testGetCurrentQuestion_secondQuestion_submitRightAnswer_pendingQuestionBecomesCompleted() = runBlockingTest(
    coroutineContext
  ) {
    subscribeToCurrentQuestionToAllowSessionToLoad()
    startTrainingSession(TEST_SKILL_ID_LIST_2)
    submitNumericInputAnswerAndMoveToNextQuestion(3.0)

    val result = questionAssessmentProgressController.submitAnswer(createNumericInputAnswer(5.0))
    result.observeForever(mockAsyncAnswerOutcomeObserver)
    advanceUntilIdle()

    // Verify that the current state updates. It should stay pending, and the wrong answer should be appended.
    verify(mockCurrentQuestionLiveDataObserver, atLeastOnce()).onChanged(currentQuestionResultCaptor.capture())
    assertThat(currentQuestionResultCaptor.value.isSuccess()).isTrue()
    val currentQuestion = currentQuestionResultCaptor.value.getOrThrow()
    assertThat(currentQuestion.currentQuestionIndex).isEqualTo(1)
    assertThat(currentQuestion.totalQuestionCount).isEqualTo(3)
    assertThat(currentQuestion.ephemeralState.stateTypeCase).isEqualTo(COMPLETED_STATE)
    val completedState = currentQuestion.ephemeralState.completedState
    assertThat(completedState.answerCount).isEqualTo(1)
    assertThat(completedState.getAnswer(0).userAnswer.answer.real).isWithin(1e-5).of(5.0)
    assertThat(completedState.getAnswer(0).feedback.html).contains("That's correct!")
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testGetCurrentQuestion_secondQuestion_submitWrongAnswer_updatePendingQuestion() = runBlockingTest(
    coroutineContext
  ) {
    subscribeToCurrentQuestionToAllowSessionToLoad()
    startTrainingSession(TEST_SKILL_ID_LIST_2)
    submitNumericInputAnswerAndMoveToNextQuestion(3.0)

    val result = questionAssessmentProgressController.submitAnswer(createNumericInputAnswer(4.0))
    result.observeForever(mockAsyncAnswerOutcomeObserver)
    advanceUntilIdle()

    // Verify that the current state updates. It should now be completed with the correct answer.
    verify(mockCurrentQuestionLiveDataObserver, atLeastOnce()).onChanged(currentQuestionResultCaptor.capture())
    assertThat(currentQuestionResultCaptor.value.isSuccess()).isTrue()
    val currentQuestion = currentQuestionResultCaptor.value.getOrThrow()
    assertThat(currentQuestion.currentQuestionIndex).isEqualTo(1)
    assertThat(currentQuestion.totalQuestionCount).isEqualTo(3)
    assertThat(currentQuestion.ephemeralState.stateTypeCase).isEqualTo(PENDING_STATE)
    val pendingState = currentQuestion.ephemeralState.pendingState
    assertThat(pendingState.wrongAnswerCount).isEqualTo(1)
    assertThat(pendingState.getWrongAnswer(0).userAnswer.answer.real).isWithin(1e-5).of(4.0)
    assertThat(pendingState.getWrongAnswer(0).feedback.html).isEmpty()
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testSubmitAnswer_forNumericInput_correctAnswer_returnsOutcomeWithTransition() = runBlockingTest(
    coroutineContext
  ) {
    subscribeToCurrentQuestionToAllowSessionToLoad()
    startTrainingSession(TEST_SKILL_ID_LIST_2)

    val result = questionAssessmentProgressController.submitAnswer(createNumericInputAnswer(3.0))
    result.observeForever(mockAsyncAnswerOutcomeObserver)
    advanceUntilIdle()

    // Verify that the answer submission was successful.
    verify(mockAsyncAnswerOutcomeObserver, atLeastOnce()).onChanged(asyncAnswerOutcomeCaptor.capture())
    val answerOutcome = asyncAnswerOutcomeCaptor.value.getOrThrow()
    assertThat(answerOutcome.isCorrectAnswer).isTrue()
    assertThat(answerOutcome.feedback.html).contains("That's correct!")
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testSubmitAnswer_forNumericInput_wrongAnswer_returnsOutcomeWithTransition() = runBlockingTest(coroutineContext) {
    subscribeToCurrentQuestionToAllowSessionToLoad()
    startTrainingSession(TEST_SKILL_ID_LIST_2)

    val result = questionAssessmentProgressController.submitAnswer(createNumericInputAnswer(2.0))
    result.observeForever(mockAsyncAnswerOutcomeObserver)
    advanceUntilIdle()

    // Verify that the answer submission failed as expected.
    verify(mockAsyncAnswerOutcomeObserver, atLeastOnce()).onChanged(asyncAnswerOutcomeCaptor.capture())
    val answerOutcome = asyncAnswerOutcomeCaptor.value.getOrThrow()
    assertThat(answerOutcome.isCorrectAnswer).isFalse()
    assertThat(answerOutcome.feedback.html).isEmpty()
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testGetCurrentQuestion_thirdQuestion_isTerminalQuestion() = runBlockingTest(coroutineContext) {
    val currentQuestionLiveData = questionAssessmentProgressController.getCurrentQuestion()
    currentQuestionLiveData.observeForever(mockCurrentQuestionLiveDataObserver)
    startTrainingSession(TEST_SKILL_ID_LIST_2)
    submitNumericInputAnswerAndMoveToNextQuestion(3.0)
    submitNumericInputAnswerAndMoveToNextQuestion(5.0)

    submitMultipleChoiceAnswerAndMoveToNextQuestion(1)

    // Verify that the third state is terminal.
    verify(mockCurrentQuestionLiveDataObserver, atLeastOnce()).onChanged(currentQuestionResultCaptor.capture())
    assertThat(currentQuestionResultCaptor.value.isSuccess()).isTrue()
    val currentQuestion = currentQuestionResultCaptor.value.getOrThrow()
    assertThat(currentQuestion.currentQuestionIndex).isEqualTo(3)
    assertThat(currentQuestion.totalQuestionCount).isEqualTo(3)
    assertThat(currentQuestion.ephemeralState.stateTypeCase).isEqualTo(TERMINAL_STATE)
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testMoveToNext_onFinalQuestion_failsWithError() = runBlockingTest(coroutineContext) {
    subscribeToCurrentQuestionToAllowSessionToLoad()
    startTrainingSession(TEST_SKILL_ID_LIST_2)
    submitNumericInputAnswerAndMoveToNextQuestion(3.0)
    submitNumericInputAnswerAndMoveToNextQuestion(5.0)
    submitMultipleChoiceAnswerAndMoveToNextQuestion(1)

    val moveToStateResult = questionAssessmentProgressController.moveToNextQuestion()
    moveToStateResult.observeForever(mockAsyncNullableResultLiveDataObserver)
    advanceUntilIdle()

    // Verify we can't navigate past the last state of the training session.
    verify(mockAsyncNullableResultLiveDataObserver, atLeastOnce()).onChanged(asyncNullableResultCaptor.capture())
    assertThat(asyncNullableResultCaptor.value.isFailure()).isTrue()
    assertThat(asyncNullableResultCaptor.value.getErrorOrNull())
      .hasMessageThat()
      .contains("Cannot navigate to next state; at most recent state.")
    verify(TestFirebaseModule.mockCrashlyticsWrapper, atLeastOnce()).logException(any())
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testGetCurrentQuestion_afterPlayingSecondSession_returnsTerminalQuestion() = runBlockingTest(coroutineContext) {
    val currentQuestionLiveData = questionAssessmentProgressController.getCurrentQuestion()
    currentQuestionLiveData.observeForever(mockCurrentQuestionLiveDataObserver)

    startTrainingSession(TEST_SKILL_ID_LIST_01)
    submitMultipleChoiceAnswerAndMoveToNextQuestion(1) // question 1
    submitNumericInputAnswerAndMoveToNextQuestion(3.0) // question 2
    submitTextInputAnswerAndMoveToNextQuestion("1/2") // question 3

    // Verify that we're now on the final state.
    verify(mockCurrentQuestionLiveDataObserver, atLeastOnce()).onChanged(currentQuestionResultCaptor.capture())
    assertThat(currentQuestionResultCaptor.value.isSuccess()).isTrue()
    val currentQuestion = currentQuestionResultCaptor.value.getOrThrow()
    assertThat(currentQuestion.ephemeralState.stateTypeCase).isEqualTo(TERMINAL_STATE)
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testGetCurrentQuestion_afterPlayingThroughPreviousSessions_returnsQuestionFromSecondSession() = runBlockingTest(
    coroutineContext
  ) {
    val currentQuestionLiveData = questionAssessmentProgressController.getCurrentQuestion()
    currentQuestionLiveData.observeForever(mockCurrentQuestionLiveDataObserver)
    playThroughSessionWithSkillList2()

    startTrainingSession(TEST_SKILL_ID_LIST_01)
    submitTextInputAnswerAndMoveToNextQuestion("1/4") // question 0
    submitMultipleChoiceAnswerAndMoveToNextQuestion(1) // question 1

    // Verify that we're on the second-to-last state of the second session.
    verify(mockCurrentQuestionLiveDataObserver, atLeastOnce()).onChanged(currentQuestionResultCaptor.capture())
    assertThat(currentQuestionResultCaptor.value.isSuccess()).isTrue()
    val currentQuestion = currentQuestionResultCaptor.value.getOrThrow()
    assertThat(currentQuestion.currentQuestionIndex).isEqualTo(2)
    assertThat(currentQuestion.totalQuestionCount).isEqualTo(3)
    assertThat(currentQuestion.ephemeralState.stateTypeCase).isEqualTo(PENDING_STATE)
    // This question is not in the other test session.
    assertThat(currentQuestion.ephemeralState.state.content.html).contains("What fraction does 'half'")
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
  private fun endTrainingSession() {
    questionTrainingController.stopQuestionTrainingSession()
    testDispatcher.advanceUntilIdle()
  }

  @ExperimentalCoroutinesApi
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
    fun provideQuestionTrainingSeed(): Long = questionSeed
  }

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(modules = [
    TestModule::class, TestQuestionModule::class, ContinueModule::class, FractionInputModule::class,
    ItemSelectionInputModule::class, MultipleChoiceInputModule::class, NumberWithUnitsRuleModule::class,
    NumericInputRuleModule::class, TextInputRuleModule::class, InteractionsModule::class, TestFirebaseModule::class
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
