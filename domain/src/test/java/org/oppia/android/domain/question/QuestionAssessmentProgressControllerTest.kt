package org.oppia.android.domain.question

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
import org.oppia.android.app.model.EphemeralQuestion
import org.oppia.android.app.model.EphemeralState
import org.oppia.android.app.model.EphemeralState.StateTypeCase.COMPLETED_STATE
import org.oppia.android.app.model.EphemeralState.StateTypeCase.PENDING_STATE
import org.oppia.android.app.model.EphemeralState.StateTypeCase.TERMINAL_STATE
import org.oppia.android.app.model.FractionGrade
import org.oppia.android.app.model.InteractionObject
import org.oppia.android.app.model.OppiaLanguage
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.UserAnswer
import org.oppia.android.app.model.UserAssessmentPerformance
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
import org.oppia.android.domain.hintsandsolution.HintsAndSolutionConfigModule
import org.oppia.android.domain.hintsandsolution.HintsAndSolutionProdModule
import org.oppia.android.domain.hintsandsolution.isHintRevealed
import org.oppia.android.domain.hintsandsolution.isSolutionRevealed
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.domain.oppialogger.LoggingIdentifierModule
import org.oppia.android.domain.oppialogger.analytics.ApplicationLifecycleModule
import org.oppia.android.domain.platformparameter.PlatformParameterModule
import org.oppia.android.domain.platformparameter.PlatformParameterSingletonModule
import org.oppia.android.domain.topic.TEST_SKILL_ID_0
import org.oppia.android.domain.topic.TEST_SKILL_ID_1
import org.oppia.android.domain.topic.TEST_SKILL_ID_2
import org.oppia.android.domain.translation.TranslationController
import org.oppia.android.testing.BuildEnvironment
import org.oppia.android.testing.FakeExceptionLogger
import org.oppia.android.testing.OppiaTestRule
import org.oppia.android.testing.RunOn
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.assertThrows
import org.oppia.android.testing.data.DataProviderTestMonitor
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.caching.AssetModule
import org.oppia.android.util.caching.testing.CachingTestModule
import org.oppia.android.util.data.DataProvidersInjector
import org.oppia.android.util.data.DataProvidersInjectorProvider
import org.oppia.android.util.locale.LocaleProdModule
import org.oppia.android.util.logging.EnableConsoleLog
import org.oppia.android.util.logging.EnableFileLog
import org.oppia.android.util.logging.GlobalLogLevel
import org.oppia.android.util.logging.LogLevel
import org.oppia.android.util.logging.SyncStatusModule
import org.oppia.android.util.networking.NetworkConnectionUtilDebugModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

private const val TOLERANCE = 1e-5

/** Tests for [QuestionAssessmentProgressController]. */
// FunctionName: test names are conventionally named with underscores.
// SameParameterValue: tests should have specific context included/excluded for readability.
@Suppress("FunctionName", "SameParameterValue")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = QuestionAssessmentProgressControllerTest.TestApplication::class)
class QuestionAssessmentProgressControllerTest {
  @get:Rule val oppiaTestRule = OppiaTestRule()

  @Inject lateinit var context: Context
  @Inject lateinit var questionTrainingController: QuestionTrainingController
  @Inject lateinit var questionAssessmentProgressController: QuestionAssessmentProgressController
  @Inject lateinit var fakeExceptionLogger: FakeExceptionLogger
  @Inject lateinit var testCoroutineDispatchers: TestCoroutineDispatchers
  @Inject lateinit var monitorFactory: DataProviderTestMonitor.Factory
  @Inject lateinit var translationController: TranslationController

  private lateinit var profileId1: ProfileId

  @Before
  fun setUp() {
    profileId1 = ProfileId.newBuilder().setLoggedInInternalProfileId(1).build()
  }

  @Test
  fun testEphemeralState_playSession_shouldIndicateNoButtonAnimation() {
    setUpTestApplicationWithSeed(questionSeed = 0)
    startSuccessfulTrainingSession(TEST_SKILL_ID_LIST_012)
    val ephemeralQuestion = waitForGetCurrentQuestionSuccessfulLoad()
    assertThat(ephemeralQuestion.ephemeralState.showContinueButtonAnimation).isFalse()
  }

  @Test
  fun testEphemeralState_moveToNextState_shouldIndicateNoButtonAnimation() {
    setUpTestApplicationWithSeed(questionSeed = 0)
    startSuccessfulTrainingSession(TEST_SKILL_ID_LIST_012)
    waitForGetCurrentQuestionSuccessfulLoad()
    submitCorrectAnswerForQuestion0()
    moveToNextQuestion()
    val ephemeralQuestion = waitForGetCurrentQuestionSuccessfulLoad()
    assertThat(ephemeralQuestion.ephemeralState.showContinueButtonAnimation).isFalse()
  }

  @Test
  fun testGetCurrentQuestion_noSessionStarted_throwsException() {
    setUpTestApplicationWithSeed(questionSeed = 0)

    // Can't retrieve the current question until the training session is started.
    assertThrows<UninitializedPropertyAccessException>() {
      questionAssessmentProgressController.getCurrentQuestion()
    }
  }

  @Test
  fun testStartTrainingSession_withEmptyQuestionList_fails() {
    setUpTestApplicationWithSeed(questionSeed = 0)

    val error = startFailureTrainingSession(skillIdList = listOf())

    assertThat(error).hasCauseThat().hasMessageThat().contains("Expected at least 1 question")
  }

  @Test
  fun testGetCurrentQuestion_sessionStarted_withEmptyQuestionList_fails() {
    setUpTestApplicationWithSeed(questionSeed = 0)
    startFailureTrainingSession(skillIdList = listOf())

    val questionDataProvider = questionAssessmentProgressController.getCurrentQuestion()

    val error = monitorFactory.waitForNextFailureResult(questionDataProvider)
    assertThat(error).hasCauseThat().hasMessageThat().contains("Expected at least 1 question")
  }

  @Test
  fun testStartTrainingSession_succeeds() {
    setUpTestApplicationWithSeed(questionSeed = 0)

    val initiationDataProvider =
      questionTrainingController.startQuestionTrainingSession(profileId1, TEST_SKILL_ID_LIST_012)

    monitorFactory.waitForNextSuccessfulResult(initiationDataProvider)
  }

  @Test
  fun testGetCurrentQuestion_playSession_loaded_returnsInitialQuestionPending() {
    setUpTestApplicationWithSeed(questionSeed = 0)
    startSuccessfulTrainingSession(TEST_SKILL_ID_LIST_012)

    val ephemeralQuestion = waitForGetCurrentQuestionSuccessfulLoad()

    assertThat(ephemeralQuestion.currentQuestionIndex).isEqualTo(0)
    assertThat(ephemeralQuestion.totalQuestionCount).isGreaterThan(0)
    assertThat(ephemeralQuestion.ephemeralState.stateTypeCase).isEqualTo(PENDING_STATE)
    assertThat(ephemeralQuestion.ephemeralState.state.content.html)
      .contains("What fraction does 'quarter'")
  }

  @Test
  fun testGetCurrentQuestion_playInvalidSession_thenPlayValidExp_returnsInitialPendingQuestion() {
    setUpTestApplicationWithSeed(questionSeed = 0)
    // Start with starting an invalid training session.
    startFailureTrainingSession(listOf())
    endTrainingSession()

    // Then a valid one.
    startSuccessfulTrainingSession(TEST_SKILL_ID_LIST_012)
    val ephemeralQuestion = waitForGetCurrentQuestionSuccessfulLoad()

    // The latest result should correspond to the valid ID, and the progress controller should
    // gracefully recover.
    assertThat(ephemeralQuestion.currentQuestionIndex).isEqualTo(0)
    assertThat(ephemeralQuestion.totalQuestionCount).isGreaterThan(0)
    assertThat(ephemeralQuestion.ephemeralState.stateTypeCase).isEqualTo(PENDING_STATE)
    assertThat(ephemeralQuestion.ephemeralState.state.content.html)
      .contains("What fraction does 'quarter'")
  }

  @Test
  fun testStopTrainingSession_withoutStartingSession_isFailure() {
    setUpTestApplicationWithSeed(questionSeed = 0)

    val stopDataProvider = questionTrainingController.stopQuestionTrainingSession()

    // The operation should be failing since the session hasn't started.
    val result = monitorFactory.waitForNextFailureResult(stopDataProvider)
    assertThat(result).isInstanceOf(IllegalStateException::class.java)
    assertThat(result).hasMessageThat().contains("Session isn't initialized yet.")
  }

  @Test
  fun testStopTrainingSession_afterStartingPreviousSession_succeeds() {
    setUpTestApplicationWithSeed(questionSeed = 0)
    questionTrainingController.startQuestionTrainingSession(profileId1, TEST_SKILL_ID_LIST_012)

    val stopDataProvider = questionTrainingController.stopQuestionTrainingSession()

    monitorFactory.waitForNextSuccessfulResult(stopDataProvider)
  }

  @Test
  fun testGetCurrentQuestion_playSecondSession_afterFinishingPrev_loaded_returnsInitialQuestion() {
    setUpTestApplicationWithSeed(questionSeed = 0)
    // Start with playing a valid session, then stop.
    startSuccessfulTrainingSession(TEST_SKILL_ID_LIST_012)
    endTrainingSession()

    // Then another valid one.
    startSuccessfulTrainingSession(TEST_SKILL_ID_LIST_2)
    val ephemeralQuestion = waitForGetCurrentQuestionSuccessfulLoad()

    // The latest result should correspond to the valid ID, and the progress controller should
    // gracefully recover.
    assertThat(ephemeralQuestion.currentQuestionIndex).isEqualTo(0)
    assertThat(ephemeralQuestion.totalQuestionCount).isGreaterThan(0)
    assertThat(ephemeralQuestion.ephemeralState.stateTypeCase).isEqualTo(PENDING_STATE)
    assertThat(ephemeralQuestion.ephemeralState.state.content.html)
      .contains("of a cake, what does the 10 represent?")
  }

  @Test
  fun testSubmitAnswer_beforePlaying_isFailure() {
    setUpTestApplicationWithSeed(questionSeed = 0)

    val submitAnswerProvider =
      questionAssessmentProgressController.submitAnswer(createMultipleChoiceAnswer(0))

    // The operation should be failing since the session hasn't started.
    val result = monitorFactory.waitForNextFailureResult(submitAnswerProvider)
    assertThat(result).isInstanceOf(IllegalStateException::class.java)
    assertThat(result).hasMessageThat().contains("Session isn't initialized yet.")
  }

  @Test
  fun testSubmitAnswer_forMultipleChoice_correctAnswer_succeeds() {
    setUpTestApplicationWithSeed(questionSeed = 6)
    startSuccessfulTrainingSession(TEST_SKILL_ID_LIST_2)
    waitForGetCurrentQuestionSuccessfulLoad()

    val result = questionAssessmentProgressController.submitAnswer(createMultipleChoiceAnswer(1))

    // Verify that the answer submission was successful.
    monitorFactory.waitForNextSuccessfulResult(result)
  }

  @Test
  fun testSubmitAnswer_forMultipleChoice_correctAnswer_returnsOutcomeWithTransition() {
    setUpTestApplicationWithSeed(questionSeed = 6)
    startSuccessfulTrainingSession(TEST_SKILL_ID_LIST_2)
    waitForGetCurrentQuestionSuccessfulLoad()

    val result = questionAssessmentProgressController.submitAnswer(createMultipleChoiceAnswer(1))

    // Verify that the answer submission was successful.
    val answerOutcome = monitorFactory.waitForNextSuccessfulResult(result)
    assertThat(answerOutcome.feedback.html).contains("That's correct!")
    assertThat(answerOutcome.isCorrectAnswer).isTrue()
  }

  @Test
  fun testSubmitAnswer_forMultipleChoice_wrongAnswer_succeeds() {
    setUpTestApplicationWithSeed(questionSeed = 6)
    startSuccessfulTrainingSession(TEST_SKILL_ID_LIST_2)
    waitForGetCurrentQuestionSuccessfulLoad()

    val result = questionAssessmentProgressController.submitAnswer(createMultipleChoiceAnswer(0))

    // Verify that the answer submission was successful.
    monitorFactory.waitForNextSuccessfulResult(result)
  }

  @Test
  fun testSubmitAnswer_forMultChoice_wrongAnswer_providesDefaultFeedbackAndNewQuestionTransition() {
    setUpTestApplicationWithSeed(questionSeed = 6)
    startSuccessfulTrainingSession(TEST_SKILL_ID_LIST_2)
    waitForGetCurrentQuestionSuccessfulLoad()

    val result = questionAssessmentProgressController.submitAnswer(createMultipleChoiceAnswer(0))

    // Verify that the answer submission was successful.
    val answerOutcome = monitorFactory.waitForNextSuccessfulResult(result)
    assertThat(answerOutcome.feedback.html).contains("Incorrect. Try again.")
    assertThat(answerOutcome.isCorrectAnswer).isFalse()
  }

  @Test
  fun testGetCurrentQuestion_afterSubmittingCorrectMultiChoiceAnswer_becomesCompletedQuestion() {
    setUpTestApplicationWithSeed(questionSeed = 6)
    startSuccessfulTrainingSession(TEST_SKILL_ID_LIST_2)
    waitForGetCurrentQuestionSuccessfulLoad()

    val ephemeralQuestion = submitMultipleChoiceAnswer(1)

    // Verify that the current state updates. It should stay pending, and the wrong answer should be
    // appended.
    assertThat(ephemeralQuestion.currentQuestionIndex).isEqualTo(0)
    assertThat(ephemeralQuestion.totalQuestionCount).isEqualTo(3)
    assertThat(ephemeralQuestion.ephemeralState.stateTypeCase).isEqualTo(COMPLETED_STATE)
    val completedState = ephemeralQuestion.ephemeralState.completedState
    assertThat(completedState.getAnswer(0).userAnswer.answer.nonNegativeInt).isEqualTo(1)
    assertThat(completedState.getAnswer(0).feedback.html).contains("That's correct!")
  }

  @Test
  fun testGetCurrentQuestion_afterSubmittingWrongMultiChoiceAnswer_updatesPendingQuestion() {
    setUpTestApplicationWithSeed(questionSeed = 6)
    startSuccessfulTrainingSession(TEST_SKILL_ID_LIST_2)
    waitForGetCurrentQuestionSuccessfulLoad()

    val ephemeralQuestion = submitMultipleChoiceAnswer(0)

    // Verify that the current state updates. It should stay pending, and the wrong answer should be
    // appended.
    assertThat(ephemeralQuestion.currentQuestionIndex).isEqualTo(0)
    assertThat(ephemeralQuestion.totalQuestionCount).isEqualTo(3)
    assertThat(ephemeralQuestion.ephemeralState.stateTypeCase).isEqualTo(PENDING_STATE)
    val pendingState = ephemeralQuestion.ephemeralState.pendingState
    assertThat(pendingState.getWrongAnswer(0).userAnswer.answer.nonNegativeInt).isEqualTo(0)
    assertThat(pendingState.getWrongAnswer(0).feedback.html).contains("Incorrect. Try again.")
  }

  @Test
  fun testGetCurrentQuestion_afterSubmitWrongThenRightAnswer_updatesToQuestionWithBothAnswers() {
    setUpTestApplicationWithSeed(questionSeed = 6)
    startSuccessfulTrainingSession(TEST_SKILL_ID_LIST_2)
    waitForGetCurrentQuestionSuccessfulLoad()
    submitMultipleChoiceAnswer(0)

    val ephemeralQuestion = submitMultipleChoiceAnswer(1)

    // Verify that the current state updates. It should now be completed with both the wrong and
    // correct answers.
    assertThat(ephemeralQuestion.currentQuestionIndex).isEqualTo(0)
    assertThat(ephemeralQuestion.totalQuestionCount).isEqualTo(3)
    assertThat(ephemeralQuestion.ephemeralState.stateTypeCase).isEqualTo(COMPLETED_STATE)
    val completedState = ephemeralQuestion.ephemeralState.completedState
    assertThat(completedState.getAnswer(0).userAnswer.answer.nonNegativeInt).isEqualTo(0)
    assertThat(completedState.getAnswer(0).feedback.html).contains("Incorrect. Try again.")
    assertThat(completedState.getAnswer(1).userAnswer.answer.nonNegativeInt).isEqualTo(1)
    assertThat(completedState.getAnswer(1).feedback.html).contains("That's correct!")
  }

  @Test
  fun testMoveToNext_beforePlaying_isFailure() {
    setUpTestApplicationWithSeed(questionSeed = 0)

    val moveToQuestionResult = questionAssessmentProgressController.moveToNextQuestion()

    // The operation should be failing since the session hasn't started.
    val result = monitorFactory.waitForNextFailureResult(moveToQuestionResult)
    assertThat(result).isInstanceOf(IllegalStateException::class.java)
    assertThat(result).hasMessageThat().contains("Session isn't initialized yet.")
  }

  @Test
  fun testMoveToNext_forPendingInitialQuestion_failsWithError() {
    setUpTestApplicationWithSeed(questionSeed = 0)
    startSuccessfulTrainingSession(TEST_SKILL_ID_LIST_2)
    waitForGetCurrentQuestionSuccessfulLoad()

    val moveToQuestionResult = questionAssessmentProgressController.moveToNextQuestion()

    // Verify that we can't move ahead since the current state isn't yet completed.
    val error = monitorFactory.waitForNextFailureResult(moveToQuestionResult)
    assertThat(error)
      .hasMessageThat()
      .contains("Cannot navigate to next state; at most recent state.")
  }

  @Test
  fun testMoveToNext_forCompletedQuestion_succeeds() {
    setUpTestApplicationWithSeed(questionSeed = 6)
    startSuccessfulTrainingSession(TEST_SKILL_ID_LIST_2)
    waitForGetCurrentQuestionSuccessfulLoad()
    submitMultipleChoiceAnswer(1)

    val moveToQuestionResult = questionAssessmentProgressController.moveToNextQuestion()

    monitorFactory.waitForNextSuccessfulResult(moveToQuestionResult)
  }

  @Test
  fun testMoveToNext_forCompletedQuestion_movesToNextQuestion() {
    setUpTestApplicationWithSeed(questionSeed = 6)
    startSuccessfulTrainingSession(TEST_SKILL_ID_LIST_2)
    waitForGetCurrentQuestionSuccessfulLoad()
    submitMultipleChoiceAnswer(1)

    val ephemeralQuestion = moveToNextQuestion()

    assertThat(ephemeralQuestion.ephemeralState.state.content.html).contains("1/2 + 1/4")
  }

  @Test
  fun testMoveToNext_afterMovingFromCompletedQuestion_failsWithError() {
    setUpTestApplicationWithSeed(questionSeed = 6)
    startSuccessfulTrainingSession(TEST_SKILL_ID_LIST_2)
    waitForGetCurrentQuestionSuccessfulLoad()
    submitMultipleChoiceAnswer(1)
    moveToNextQuestion()

    // Try skipping past the current state.
    val moveToQuestionResult = questionAssessmentProgressController.moveToNextQuestion()

    // Verify we can't move ahead since the new state isn't yet completed.
    val error = monitorFactory.waitForNextFailureResult(moveToQuestionResult)
    assertThat(error)
      .hasMessageThat()
      .contains("Cannot navigate to next state; at most recent state.")
  }

  @Test
  fun testSubmitAnswer_forTextInput_correctAnswer_returnsOutcomeWithTransition() {
    setUpTestApplicationWithSeed(questionSeed = 2)
    startSuccessfulTrainingSession(TEST_SKILL_ID_LIST_01)
    waitForGetCurrentQuestionSuccessfulLoad()

    val result = questionAssessmentProgressController.submitAnswer(createTextInputAnswer("1/4"))

    // Verify that the answer submission was successful.
    val answerOutcome = monitorFactory.waitForNextSuccessfulResult(result)
    assertThat(answerOutcome.isCorrectAnswer).isTrue()
    assertThat(answerOutcome.feedback.html).contains("That's correct!")
  }

  @Test
  fun testSubmitAnswer_forTextInput_wrongAnswer_returnsDefaultOutcome() {
    setUpTestApplicationWithSeed(questionSeed = 2)
    startSuccessfulTrainingSession(TEST_SKILL_ID_LIST_01)
    waitForGetCurrentQuestionSuccessfulLoad()

    val result = questionAssessmentProgressController.submitAnswer(createTextInputAnswer("2/4"))

    // Verify that the answer was wrong, and that there's no handler for it so the default outcome
    // is returned.
    val answerOutcome = monitorFactory.waitForNextSuccessfulResult(result)
    assertThat(answerOutcome.isCorrectAnswer).isFalse()
    assertThat(answerOutcome.feedback.html).isEmpty()
  }

  @Test
  fun testGetCurrentQuestion_secondQuestion_submitRightAnswer_pendingQuestionBecomesCompleted() {
    setUpTestApplicationWithSeed(questionSeed = 0)
    startSuccessfulTrainingSession(TEST_SKILL_ID_LIST_2)
    waitForGetCurrentQuestionSuccessfulLoad()
    submitNumericInputAnswerAndMoveToNextQuestion(3.0)

    questionAssessmentProgressController.submitAnswer(createNumericInputAnswer(5.0))
    testCoroutineDispatchers.runCurrent()

    // Verify that the current state updates. It should stay pending, and the wrong answer should be
    // appended.
    val currentQuestion = waitForGetCurrentQuestionSuccessfulLoad()
    assertThat(currentQuestion.currentQuestionIndex).isEqualTo(1)
    assertThat(currentQuestion.totalQuestionCount).isEqualTo(3)
    assertThat(currentQuestion.ephemeralState.stateTypeCase).isEqualTo(COMPLETED_STATE)
    val completedState = currentQuestion.ephemeralState.completedState
    assertThat(completedState.answerCount).isEqualTo(1)
    assertThat(completedState.getAnswer(0).userAnswer.answer.real).isWithin(TOLERANCE).of(5.0)
    assertThat(completedState.getAnswer(0).feedback.html).contains("That's correct!")
  }

  @Test
  fun testGetCurrentQuestion_secondQuestion_submitWrongAnswer_updatePendingQuestion() {
    setUpTestApplicationWithSeed(questionSeed = 0)
    startSuccessfulTrainingSession(TEST_SKILL_ID_LIST_2)
    waitForGetCurrentQuestionSuccessfulLoad()
    submitNumericInputAnswerAndMoveToNextQuestion(3.0)

    questionAssessmentProgressController.submitAnswer(createNumericInputAnswer(4.0))
    testCoroutineDispatchers.runCurrent()

    // Verify that the current state updates. It should now be completed with the correct answer.
    val currentQuestion = waitForGetCurrentQuestionSuccessfulLoad()
    assertThat(currentQuestion.currentQuestionIndex).isEqualTo(1)
    assertThat(currentQuestion.totalQuestionCount).isEqualTo(3)
    assertThat(currentQuestion.ephemeralState.stateTypeCase).isEqualTo(PENDING_STATE)
    val pendingState = currentQuestion.ephemeralState.pendingState
    assertThat(pendingState.wrongAnswerCount).isEqualTo(1)
    assertThat(pendingState.getWrongAnswer(0).userAnswer.answer.real).isWithin(TOLERANCE).of(4.0)
    assertThat(pendingState.getWrongAnswer(0).feedback.html).isEmpty()
  }

  @Test
  fun testSubmitAnswer_forNumericInput_correctAnswer_returnsOutcomeWithTransition() {
    setUpTestApplicationWithSeed(questionSeed = 0)
    startSuccessfulTrainingSession(TEST_SKILL_ID_LIST_2)
    waitForGetCurrentQuestionSuccessfulLoad()

    val result = questionAssessmentProgressController.submitAnswer(createNumericInputAnswer(3.0))

    // Verify that the answer submission was successful.
    val answerOutcome = monitorFactory.waitForNextSuccessfulResult(result)
    assertThat(answerOutcome.isCorrectAnswer).isTrue()
    assertThat(answerOutcome.feedback.html).contains("That's correct!")
  }

  @Test
  fun testSubmitAnswer_forNumericInput_wrongAnswer_returnsOutcomeWithTransition() {
    setUpTestApplicationWithSeed(questionSeed = 0)
    startSuccessfulTrainingSession(TEST_SKILL_ID_LIST_2)
    waitForGetCurrentQuestionSuccessfulLoad()

    val result = questionAssessmentProgressController.submitAnswer(createNumericInputAnswer(2.0))

    // Verify that the answer submission failed as expected.
    val answerOutcome = monitorFactory.waitForNextSuccessfulResult(result)
    assertThat(answerOutcome.isCorrectAnswer).isFalse()
    assertThat(answerOutcome.feedback.html).isEmpty()
  }

  @Test
  fun testGetCurrentQuestion_thirdQuestion_isTerminalQuestion() {
    setUpTestApplicationWithSeed(questionSeed = 0)
    startSuccessfulTrainingSession(TEST_SKILL_ID_LIST_2)
    waitForGetCurrentQuestionSuccessfulLoad()
    submitNumericInputAnswerAndMoveToNextQuestion(3.0)
    submitNumericInputAnswerAndMoveToNextQuestion(5.0)

    val currentQuestion = submitMultipleChoiceAnswerAndMoveToNextQuestion(1)

    // Verify that the third state is terminal.
    assertThat(currentQuestion.currentQuestionIndex).isEqualTo(3)
    assertThat(currentQuestion.totalQuestionCount).isEqualTo(3)
    assertThat(currentQuestion.ephemeralState.stateTypeCase).isEqualTo(TERMINAL_STATE)
  }

  @Test
  fun testMoveToNext_onFinalQuestion_failsWithError() {
    setUpTestApplicationWithSeed(questionSeed = 0)
    startSuccessfulTrainingSession(TEST_SKILL_ID_LIST_2)
    waitForGetCurrentQuestionSuccessfulLoad()
    submitNumericInputAnswerAndMoveToNextQuestion(3.0)
    submitNumericInputAnswerAndMoveToNextQuestion(5.0)
    submitMultipleChoiceAnswerAndMoveToNextQuestion(1)

    val moveToQuestionResult = questionAssessmentProgressController.moveToNextQuestion()

    // Verify we can't navigate past the last state of the training session.
    val error = monitorFactory.waitForNextFailureResult(moveToQuestionResult)
    assertThat(error)
      .hasMessageThat()
      .contains("Cannot navigate to next state; at most recent state.")
  }

  @Test
  fun testGetCurrentQuestion_afterPlayingSecondSession_returnsTerminalQuestion() {
    setUpTestApplicationWithSeed(questionSeed = 0)

    startSuccessfulTrainingSession(TEST_SKILL_ID_LIST_01)
    waitForGetCurrentQuestionSuccessfulLoad()
    submitMultipleChoiceAnswerAndMoveToNextQuestion(1) // question 1
    submitNumericInputAnswerAndMoveToNextQuestion(3.0) // question 2
    val ephemeralQuestion = submitTextInputAnswerAndMoveToNextQuestion("1/2") // question 3

    // Verify that we're now on the final state.
    assertThat(ephemeralQuestion.ephemeralState.stateTypeCase).isEqualTo(TERMINAL_STATE)
  }

  @Test
  fun testGetCurrentQuestion_afterPlayingThroughPrevSessions_returnsQuestionFromSecondSession() {
    setUpTestApplicationWithSeed(questionSeed = 0)
    playThroughSessionWithSkillList2()

    startSuccessfulTrainingSession(TEST_SKILL_ID_LIST_01)
    waitForGetCurrentQuestionSuccessfulLoad()
    submitTextInputAnswerAndMoveToNextQuestion("1/4") // question 0
    val ephemeralQuestion = submitMultipleChoiceAnswerAndMoveToNextQuestion(1) // question 1

    // Verify that we're on the second-to-last state of the second session.
    assertThat(ephemeralQuestion.currentQuestionIndex).isEqualTo(2)
    assertThat(ephemeralQuestion.totalQuestionCount).isEqualTo(3)
    assertThat(ephemeralQuestion.ephemeralState.stateTypeCase).isEqualTo(PENDING_STATE)
    // This question is not in the other test session.
    assertThat(ephemeralQuestion.ephemeralState.state.content.html)
      .contains("What fraction does 'half'")
  }

  @Test
  fun testMoveToNext_onFinalQuestion_failsWithError_logsException() {
    setUpTestApplicationWithSeed(questionSeed = 0)
    startSuccessfulTrainingSession(TEST_SKILL_ID_LIST_2)
    waitForGetCurrentQuestionSuccessfulLoad()
    submitNumericInputAnswerAndMoveToNextQuestion(3.0)
    submitNumericInputAnswerAndMoveToNextQuestion(5.0)
    submitMultipleChoiceAnswerAndMoveToNextQuestion(1)

    questionAssessmentProgressController.moveToNextQuestion()
    testCoroutineDispatchers.runCurrent()

    val exception = fakeExceptionLogger.getMostRecentException()
    assertThat(exception).isInstanceOf(IllegalStateException::class.java)
    assertThat(exception).hasMessageThat()
      .contains("Cannot navigate to next state; at most recent state.")
  }

  @Test
  fun testSubmitAnswer_forTextInput_wrongAnswer_returnsDefaultOutcome_showHint() {
    setUpTestApplicationWithSeed(questionSeed = 0)
    startSuccessfulTrainingSession(TEST_SKILL_ID_LIST_2)
    waitForGetCurrentQuestionSuccessfulLoad()

    val result = questionAssessmentProgressController.submitAnswer(createNumericInputAnswer(2.0))

    // Verify that the answer submission failed as expected.
    val answerOutcome = monitorFactory.waitForNextSuccessfulResult(result)
    assertThat(answerOutcome.isCorrectAnswer).isFalse()
    assertThat(answerOutcome.feedback.html).isEmpty()

    val ephemeralQuestion = waitForGetCurrentQuestionSuccessfulLoad()
    assertThat(ephemeralQuestion.ephemeralState.stateTypeCase).isEqualTo(PENDING_STATE)
    assertThat(ephemeralQuestion.ephemeralState.pendingState.wrongAnswerCount).isEqualTo(1)
    val hintAndSolution = ephemeralQuestion.ephemeralState.state.interaction.getHint(0)
    assertThat(hintAndSolution.hintContent.html).contains("Hint text will appear here")
  }

  @Test
  fun testRevealHint_forWrongAnswer_showHint_returnHintIsRevealed() {
    setUpTestApplicationWithSeed(questionSeed = 0)
    playThroughSessionWithSkillList2()

    startSuccessfulTrainingSession(TEST_SKILL_ID_LIST_01)
    waitForGetCurrentQuestionSuccessfulLoad()
    submitTextInputAnswerAndMoveToNextQuestion("1/4") // question 0
    submitMultipleChoiceAnswerAndMoveToNextQuestion(2) // question 1
    // question 1 (again--second wrong answer)
    val currentQuestion = submitMultipleChoiceAnswerAndMoveToNextQuestion(2)

    // Verify that we're on the second-to-last state of the second session.
    assertThat(currentQuestion.currentQuestionIndex).isEqualTo(1)
    assertThat(currentQuestion.ephemeralState.stateTypeCase).isEqualTo(PENDING_STATE)
    // This question is not in the other test session.
    assertThat(currentQuestion.ephemeralState.state.content.html)
      .contains("If we talk about wanting")

    assertThat(currentQuestion.ephemeralState.stateTypeCase).isEqualTo(PENDING_STATE)
    assertThat(currentQuestion.ephemeralState.pendingState.wrongAnswerCount)
      .isEqualTo(2)
    val hintAndSolution = currentQuestion.ephemeralState.state.interaction.getHint(0)
    assertThat(hintAndSolution.hintContent.html).contains("Hint text will appear here")
    monitorFactory.waitForNextSuccessfulResult(
      questionAssessmentProgressController.submitHintIsRevealed(hintIndex = 0)
    )

    // Verify that the current state updates. Hint revealed is true.
    val updatedState = waitForGetCurrentQuestionSuccessfulLoad()
    assertThat(updatedState.ephemeralState.isHintRevealed(0)).isTrue()
  }

  @Test
  fun testRevealSolution_forWrongAnswer_showSolution_returnSolutionIsRevealed() {
    setUpTestApplicationWithSeed(questionSeed = 0)
    playThroughSessionWithSkillList2()

    startSuccessfulTrainingSession(TEST_SKILL_ID_LIST_01)
    waitForGetCurrentQuestionSuccessfulLoad()
    submitTextInputAnswerAndMoveToNextQuestion("1/3") // question 0 (wrong answer)
    submitTextInputAnswerAndMoveToNextQuestion("1/3") // question 0 (wrong answer)
    // The actual reveal will fail due to it being invalid.
    monitorFactory.ensureDataProviderExecutes(
      questionAssessmentProgressController.submitHintIsRevealed(hintIndex = 0)
    )
    submitTextInputAnswerAndMoveToNextQuestion("1/3") // question 0 (wrong answer)
    testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(10))

    val currentQuestion = waitForGetCurrentQuestionSuccessfulLoad()
    assertThat(currentQuestion.ephemeralState.stateTypeCase).isEqualTo(PENDING_STATE)
    assertThat(currentQuestion.ephemeralState.pendingState.wrongAnswerCount).isEqualTo(3)

    val hintAndSolution = currentQuestion.ephemeralState.state.interaction.solution
    assertThat(hintAndSolution.correctAnswer.normalizedString).contains("1/4")

    monitorFactory.waitForNextSuccessfulResult(
      questionAssessmentProgressController.submitSolutionIsRevealed()
    )

    // Verify that the current state updates. Hint revealed is true.
    val updatedState = waitForGetCurrentQuestionSuccessfulLoad()
    assertThat(updatedState.ephemeralState.isSolutionRevealed()).isTrue()
  }

  @Test
  fun testRevealedSolution_forWrongAnswer_returnScore2OutOf3() {
    setUpTestApplicationWithSeed(questionSeed = 0)
    startSuccessfulTrainingSession(TEST_SKILL_ID_LIST_2)
    waitForGetCurrentQuestionSuccessfulLoad()

    // Question 2
    // Submit question 2 wrong answer
    submitIncorrectAnswerForQuestion2(4.0)
    viewHintForQuestion2(submitIncorrectAnswerForQuestion2(4.0))
    submitIncorrectAnswerForQuestion2(4.0)
    testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(10))
    viewSolutionForQuestion2(waitForGetCurrentQuestionSuccessfulLoad())
    submitCorrectAnswerForQuestion2()

    // Question 5
    submitCorrectAnswerForQuestion5()

    // Question 4
    submitCorrectAnswerForQuestion4()

    val userAssessmentPerformance = getExpectedGrade(TEST_SKILL_ID_LIST_2)
    val grade = FractionGrade.newBuilder().apply {
      pointsReceived = 2.0
      totalPointsAvailable = 3.0
    }.build()
    assertThat(userAssessmentPerformance.totalFractionScore).isEqualTo(grade)
    assertThat(userAssessmentPerformance.fractionScorePerSkillMappingCount).isEqualTo(1)
    assertThat(userAssessmentPerformance.getFractionScorePerSkillMappingOrThrow(TEST_SKILL_ID_2))
      .isEqualTo(grade)
  }

  @Test
  fun testRevealedHintAndSolution_forWrongAnswer_returnScore2OutOf3() {
    setUpTestApplicationWithSeed(questionSeed = 0)
    startSuccessfulTrainingSession(TEST_SKILL_ID_LIST_2)
    waitForGetCurrentQuestionSuccessfulLoad()

    // Question 2
    // Submit question 2 wrong answer
    submitIncorrectAnswerForQuestion2(4.0)
    viewHintForQuestion2(submitIncorrectAnswerForQuestion2(4.0))
    submitIncorrectAnswerForQuestion2(4.0)
    testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(10))
    viewSolutionForQuestion2(waitForGetCurrentQuestionSuccessfulLoad())
    submitCorrectAnswerForQuestion2()

    // Question 5
    submitCorrectAnswerForQuestion5()

    // Question 4
    submitCorrectAnswerForQuestion4()

    val userAssessmentPerformance = getExpectedGrade(TEST_SKILL_ID_LIST_2)
    val grade = FractionGrade.newBuilder().apply {
      pointsReceived = 2.0
      totalPointsAvailable = 3.0
    }.build()
    assertThat(userAssessmentPerformance.totalFractionScore).isEqualTo(grade)
    assertThat(userAssessmentPerformance.fractionScorePerSkillMappingCount).isEqualTo(1)
    assertThat(userAssessmentPerformance.getFractionScorePerSkillMappingOrThrow(TEST_SKILL_ID_2))
      .isEqualTo(grade)
  }

  @Test
  fun testRevealedHint_for5WrongAnswers_returnScore2Point4OutOf3() {
    setUpTestApplicationWithSeed(questionSeed = 0)
    startSuccessfulTrainingSession(TEST_SKILL_ID_LIST_2)
    waitForGetCurrentQuestionSuccessfulLoad()

    // Question 2
    // Submit question 2 wrong answers
    submitIncorrectAnswerForQuestion2(4.0)
    submitIncorrectAnswerForQuestion2(4.0)
    submitIncorrectAnswerForQuestion2(4.0)
    submitIncorrectAnswerForQuestion2(4.0)
    viewHintForQuestion2(submitIncorrectAnswerForQuestion2(4.0))
    submitCorrectAnswerForQuestion2()

    // Question 5
    submitCorrectAnswerForQuestion5()

    // Question 4
    submitCorrectAnswerForQuestion4()

    val userAssessmentPerformance = getExpectedGrade(TEST_SKILL_ID_LIST_2)
    val grade = FractionGrade.newBuilder().apply {
      pointsReceived = 2.4
      totalPointsAvailable = 3.0
    }.build()
    assertThat(userAssessmentPerformance.totalFractionScore).isEqualTo(grade)
    assertThat(userAssessmentPerformance.fractionScorePerSkillMappingCount).isEqualTo(1)
    assertThat(userAssessmentPerformance.getFractionScorePerSkillMappingOrThrow(TEST_SKILL_ID_2))
      .isEqualTo(grade)
  }

  @Test
  fun noHints_noWrongAnswers_noSolutionsViewed_returnPerfectScore() {
    setUpTestApplicationWithSeed(questionSeed = 0)
    startSuccessfulTrainingSession(TEST_SKILL_ID_LIST_2)
    waitForGetCurrentQuestionSuccessfulLoad()

    // Question 2
    submitCorrectAnswerForQuestion2()

    // Question 5
    submitCorrectAnswerForQuestion5()

    // Question 4
    submitCorrectAnswerForQuestion4()

    val userAssessmentPerformance = getExpectedGrade(TEST_SKILL_ID_LIST_2)
    val grade = FractionGrade.newBuilder().apply {
      pointsReceived = 3.0
      totalPointsAvailable = 3.0
    }.build()
    assertThat(userAssessmentPerformance.totalFractionScore).isEqualTo(grade)
    assertThat(userAssessmentPerformance.fractionScorePerSkillMappingCount).isEqualTo(1)
    assertThat(userAssessmentPerformance.getFractionScorePerSkillMappingOrThrow(TEST_SKILL_ID_2))
      .isEqualTo(grade)
  }

  @Test
  fun hintViewed_solutionViewed_wrongAnswersSubmitted_for2Skills_returnDifferingSkillScores() {
    setUpTestApplicationWithSeed(questionSeed = 0)
    // This will generate question 1 (skill 0), question 2 (skill 0), and question 3 (skill 1)
    startSuccessfulTrainingSession(TEST_SKILL_ID_LIST_01)
    waitForGetCurrentQuestionSuccessfulLoad()

    // Question 1
    // Submit question 1 wrong answers
    submitIncorrectAnswerForQuestion1(2)
    submitIncorrectAnswerForQuestion1(2)
    submitIncorrectAnswerForQuestion1(2)
    submitCorrectAnswerForQuestion1()

    // Question 2
    // Submit question 2 wrong answer
    submitIncorrectAnswerForQuestion2(4.0)
    viewHintForQuestion2(submitIncorrectAnswerForQuestion2(4.0))
    submitCorrectAnswerForQuestion2()

    // Question 3
    // Submit question 3 wrong answer
    submitIncorrectAnswerForQuestion3("3/4")
    submitIncorrectAnswerForQuestion3("3/4")
    monitorFactory.waitForNextSuccessfulResult(
      questionAssessmentProgressController.submitHintIsRevealed(hintIndex = 0)
    )
    submitIncorrectAnswerForQuestion3("3/4")
    testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(10))
    viewSolutionForQuestion3(waitForGetCurrentQuestionSuccessfulLoad())
    submitCorrectAnswerForQuestion3()

    val userAssessmentPerformance = getExpectedGrade(TEST_SKILL_ID_LIST_01)
    val totalScore = FractionGrade.newBuilder().apply {
      pointsReceived = 1.4
      totalPointsAvailable = 3.0
    }.build()
    val skill0Score = FractionGrade.newBuilder().apply {
      pointsReceived = 1.4
      totalPointsAvailable = 2.0
    }.build()
    val skill1Score = FractionGrade.newBuilder().apply {
      pointsReceived = 0.0
      totalPointsAvailable = 1.0
    }.build()
    assertThat(userAssessmentPerformance.totalFractionScore).isEqualTo(totalScore)
    assertThat(userAssessmentPerformance.fractionScorePerSkillMappingCount).isEqualTo(2)
    assertThat(userAssessmentPerformance.getFractionScorePerSkillMappingOrThrow(TEST_SKILL_ID_0))
      .isEqualTo(skill0Score)
    assertThat(userAssessmentPerformance.getFractionScorePerSkillMappingOrThrow(TEST_SKILL_ID_1))
      .isEqualTo(skill1Score)
  }

  @Test
  fun solutionViewedForAllQuestions_returnZeroScore() {
    setUpTestApplicationWithSeed(questionSeed = 0)
    // This will generate question 1 (skill 0), question 2 (skill 0), and question 3 (skill 1)
    startSuccessfulTrainingSession(TEST_SKILL_ID_LIST_01)
    waitForGetCurrentQuestionSuccessfulLoad()

    // Question 1
    // Submit question 1 wrong answer (a few extra wrong answers are added to reduce points).
    submitIncorrectAnswerForQuestion1(2)
    submitIncorrectAnswerForQuestion1(2)
    submitIncorrectAnswerForQuestion1(2)
    submitIncorrectAnswerForQuestion1(2)
    submitIncorrectAnswerForQuestion1(2)
    submitIncorrectAnswerForQuestion1(2)
    viewHintForQuestion1(submitIncorrectAnswerForQuestion1(2), index = 0)
    submitIncorrectAnswerForQuestion1(2)
    testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(10))
    viewHintForQuestion1(waitForGetCurrentQuestionSuccessfulLoad(), index = 1)
    submitCorrectAnswerForQuestion1()

    // Question 2
    // Submit question 2 wrong answer
    submitIncorrectAnswerForQuestion2(4.0)
    viewHintForQuestion2(submitIncorrectAnswerForQuestion2(4.0))
    submitIncorrectAnswerForQuestion2(4.0)
    testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(10))
    viewSolutionForQuestion2(waitForGetCurrentQuestionSuccessfulLoad())
    submitCorrectAnswerForQuestion2()

    // Question 3
    // Submit question 3 wrong answer
    submitIncorrectAnswerForQuestion3("3/4")
    submitIncorrectAnswerForQuestion3("3/4")
    monitorFactory.waitForNextSuccessfulResult(
      questionAssessmentProgressController.submitHintIsRevealed(hintIndex = 0)
    )
    submitIncorrectAnswerForQuestion3("3/4")
    testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(10))
    viewSolutionForQuestion3(waitForGetCurrentQuestionSuccessfulLoad())
    submitCorrectAnswerForQuestion3()

    val userAssessmentPerformance = getExpectedGrade(TEST_SKILL_ID_LIST_01)
    val totalScore = FractionGrade.newBuilder().apply {
      pointsReceived = 0.0
      totalPointsAvailable = 3.0
    }.build()
    val skill0Score = FractionGrade.newBuilder().apply {
      pointsReceived = 0.0
      totalPointsAvailable = 2.0
    }.build()
    val skill1Score = FractionGrade.newBuilder().apply {
      pointsReceived = 0.0
      totalPointsAvailable = 1.0
    }.build()
    assertThat(userAssessmentPerformance.totalFractionScore).isEqualTo(totalScore)
    assertThat(userAssessmentPerformance.fractionScorePerSkillMappingCount).isEqualTo(2)
    assertThat(userAssessmentPerformance.getFractionScorePerSkillMappingOrThrow(TEST_SKILL_ID_0))
      .isEqualTo(skill0Score)
    assertThat(userAssessmentPerformance.getFractionScorePerSkillMappingOrThrow(TEST_SKILL_ID_1))
      .isEqualTo(skill1Score)
  }

  @Test
  fun hintViewed_for2QuestionsWithWrongAnswer_returnScore2Point4Outof3() {
    setUpTestApplicationWithSeed(questionSeed = 0)
    // This will generate question 1 (skill 0), question 2 (skill 0), and question 3 (skill 1)
    startSuccessfulTrainingSession(TEST_SKILL_ID_LIST_01)
    waitForGetCurrentQuestionSuccessfulLoad()

    // Question 1
    // Submit question 1 wrong answer
    submitIncorrectAnswerForQuestion1(2)
    viewHintForQuestion1(submitIncorrectAnswerForQuestion1(2), index = 0)
    submitCorrectAnswerForQuestion1()

    // Question 2
    // Submit question 2 wrong answer
    submitIncorrectAnswerForQuestion2(4.0)
    submitIncorrectAnswerForQuestion2(4.0)
    viewHintForQuestion2(waitForGetCurrentQuestionSuccessfulLoad())
    submitCorrectAnswerForQuestion2()

    // Question 3
    submitCorrectAnswerForQuestion3()

    val userAssessmentPerformance = getExpectedGrade(TEST_SKILL_ID_LIST_01)
    val totalScore = FractionGrade.newBuilder().apply {
      pointsReceived = 2.4
      totalPointsAvailable = 3.0
    }.build()
    val skill0Score = FractionGrade.newBuilder().apply {
      pointsReceived = 1.4
      totalPointsAvailable = 2.0
    }.build()
    val skill1Score = FractionGrade.newBuilder().apply {
      pointsReceived = 1.0
      totalPointsAvailable = 1.0
    }.build()
    assertThat(userAssessmentPerformance.totalFractionScore).isEqualTo(totalScore)
    assertThat(userAssessmentPerformance.fractionScorePerSkillMappingCount).isEqualTo(2)
    assertThat(userAssessmentPerformance.getFractionScorePerSkillMappingOrThrow(TEST_SKILL_ID_0))
      .isEqualTo(skill0Score)
    assertThat(userAssessmentPerformance.getFractionScorePerSkillMappingOrThrow(TEST_SKILL_ID_1))
      .isEqualTo(skill1Score)
  }

  @Test
  fun multipleHintsViewed_forQuestionsWithWrongAnswer_returnScore2Point5Outof3() {
    setUpTestApplicationWithSeed(questionSeed = 0)
    // This will generate question 1 (skill 0), question 2 (skill 0), and question 3 (skill 1)
    startSuccessfulTrainingSession(TEST_SKILL_ID_LIST_01)
    waitForGetCurrentQuestionSuccessfulLoad()

    // Question 1
    // Submit question 1 wrong answer
    submitIncorrectAnswerForQuestion1(2)
    viewHintForQuestion1(submitIncorrectAnswerForQuestion1(2), index = 0)
    submitIncorrectAnswerForQuestion1(2)
    testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(10))
    viewHintForQuestion1(waitForGetCurrentQuestionSuccessfulLoad(), index = 1)
    submitCorrectAnswerForQuestion1()

    // Question 2
    submitCorrectAnswerForQuestion2()

    // Question 3
    submitCorrectAnswerForQuestion3()

    val userAssessmentPerformance = getExpectedGrade(TEST_SKILL_ID_LIST_01)
    val totalScore = FractionGrade.newBuilder().apply {
      pointsReceived = 2.5
      totalPointsAvailable = 3.0
    }.build()
    val skill0Score = FractionGrade.newBuilder().apply {
      pointsReceived = 1.5
      totalPointsAvailable = 2.0
    }.build()
    val skill1Score = FractionGrade.newBuilder().apply {
      pointsReceived = 1.0
      totalPointsAvailable = 1.0
    }.build()
    assertThat(userAssessmentPerformance.totalFractionScore).isEqualTo(totalScore)
    assertThat(userAssessmentPerformance.fractionScorePerSkillMappingCount).isEqualTo(2)
    assertThat(userAssessmentPerformance.getFractionScorePerSkillMappingOrThrow(TEST_SKILL_ID_0))
      .isEqualTo(skill0Score)
    assertThat(userAssessmentPerformance.getFractionScorePerSkillMappingOrThrow(TEST_SKILL_ID_1))
      .isEqualTo(skill1Score)
  }

  @Test
  fun solutionViewedForAllQuestions_returnMaxMasteryLossPerQuestion() {
    setUpTestApplicationWithSeed(questionSeed = 0)
    // This will generate question 1 (skill 0), question 2 (skill 0), and question 3 (skill 1)
    startSuccessfulTrainingSession(TEST_SKILL_ID_LIST_01)
    waitForGetCurrentQuestionSuccessfulLoad()

    // Question 1
    // Submit question 1 wrong answer
    submitIncorrectAnswerForQuestion1(2)
    viewHintForQuestion1(submitIncorrectAnswerForQuestion1(2), index = 0)
    submitIncorrectAnswerForQuestion1(2)
    testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(10))
    viewHintForQuestion1(waitForGetCurrentQuestionSuccessfulLoad(), index = 1)
    submitCorrectAnswerForQuestion1()

    // Question 2
    // Submit question 2 wrong answer
    submitIncorrectAnswerForQuestion2(4.0)
    viewHintForQuestion2(submitIncorrectAnswerForQuestion2(4.0))
    submitIncorrectAnswerForQuestion2(4.0)
    testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(10))
    viewSolutionForQuestion2(waitForGetCurrentQuestionSuccessfulLoad())
    submitCorrectAnswerForQuestion2()

    // Question 3
    // Submit question 3 wrong answer
    submitIncorrectAnswerForQuestion3("3/4")
    submitIncorrectAnswerForQuestion3("3/4")
    monitorFactory.waitForNextSuccessfulResult(
      questionAssessmentProgressController.submitHintIsRevealed(hintIndex = 0)
    )
    submitIncorrectAnswerForQuestion3("3/4")
    testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(10))
    viewSolutionForQuestion3(waitForGetCurrentQuestionSuccessfulLoad())
    submitCorrectAnswerForQuestion3()

    val userAssessmentPerformance = getExpectedGrade(TEST_SKILL_ID_LIST_01)
    val skill0Mastery = -0.19
    val skill1Mastery = -0.1
    assertThat(userAssessmentPerformance.masteryPerSkillMappingCount).isEqualTo(2)
    assertThat(userAssessmentPerformance.getMasteryPerSkillMappingOrThrow(TEST_SKILL_ID_0))
      .isWithin(TOLERANCE).of(skill0Mastery)
    assertThat(userAssessmentPerformance.getMasteryPerSkillMappingOrThrow(TEST_SKILL_ID_1))
      .isWithin(TOLERANCE).of(skill1Mastery)
  }

  @Test
  fun correctAnswerOnFirstTryForAllQuestions_returnMaxMasteryGainPerQuestion() {
    setUpTestApplicationWithSeed(questionSeed = 0)
    // This will generate question 1 (skill 0), question 2 (skill 0), and question 3 (skill 1)
    startSuccessfulTrainingSession(TEST_SKILL_ID_LIST_01)
    waitForGetCurrentQuestionSuccessfulLoad()

    // Question 1
    submitCorrectAnswerForQuestion1()

    // Question 2
    submitCorrectAnswerForQuestion2()

    // Question 3
    submitCorrectAnswerForQuestion3()

    val userAssessmentPerformance = getExpectedGrade(TEST_SKILL_ID_LIST_01)
    val skill0Mastery = 0.2
    val skill1Mastery = 0.1
    assertThat(userAssessmentPerformance.masteryPerSkillMappingCount).isEqualTo(2)
    assertThat(userAssessmentPerformance.getMasteryPerSkillMappingOrThrow(TEST_SKILL_ID_0))
      .isWithin(TOLERANCE).of(skill0Mastery)
    assertThat(userAssessmentPerformance.getMasteryPerSkillMappingOrThrow(TEST_SKILL_ID_1))
      .isWithin(TOLERANCE).of(skill1Mastery)
  }

  @Test
  fun hintsAndSolutionsViewedWithWrongAnswers_noMisconceptions_returnDifferingMasteryDegrees() {
    setUpTestApplicationWithSeed(questionSeed = 0)
    // This will generate question 1 (skill 0), question 2 (skill 0), and question 3 (skill 1)
    startSuccessfulTrainingSession(TEST_SKILL_ID_LIST_01)
    waitForGetCurrentQuestionSuccessfulLoad()

    // Question 1
    // Submit question 1 wrong answers
    submitIncorrectAnswerForQuestion1(2)
    submitIncorrectAnswerForQuestion1(2)
    submitCorrectAnswerForQuestion1()

    // Question 2
    // Submit question 2 wrong answer
    submitIncorrectAnswerForQuestion2(4.0)
    viewHintForQuestion2(submitIncorrectAnswerForQuestion2(4.0))
    submitCorrectAnswerForQuestion2()

    // Question 3
    // Submit question 3 wrong answer
    submitIncorrectAnswerForQuestion3("3/4")
    submitIncorrectAnswerForQuestion3("3/4")
    monitorFactory.waitForNextSuccessfulResult(
      questionAssessmentProgressController.submitHintIsRevealed(hintIndex = 0)
    )
    submitIncorrectAnswerForQuestion3("3/4")
    testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(10))
    viewSolutionForQuestion3(waitForGetCurrentQuestionSuccessfulLoad())
    submitCorrectAnswerForQuestion3()

    val userAssessmentPerformance = getExpectedGrade(TEST_SKILL_ID_LIST_01)
    val skill0Mastery = -0.02
    val skill1Mastery = -0.1
    assertThat(userAssessmentPerformance.masteryPerSkillMappingCount).isEqualTo(2)
    assertThat(userAssessmentPerformance.getMasteryPerSkillMappingOrThrow(TEST_SKILL_ID_0))
      .isWithin(TOLERANCE).of(skill0Mastery)
    assertThat(userAssessmentPerformance.getMasteryPerSkillMappingOrThrow(TEST_SKILL_ID_1))
      .isWithin(TOLERANCE).of(skill1Mastery)
  }

  @Test
  fun maxMasteryLossPerQuestionSurpassed_returnMaxMasteryLossForQuestion() {
    setUpTestApplicationWithSeed(questionSeed = 0)
    // This will generate question 1 (skill 0), question 2 (skill 0), and question 0 (skill 1)
    startSuccessfulTrainingSession(TEST_SKILL_ID_LIST_01)
    waitForGetCurrentQuestionSuccessfulLoad()

    // Question 1
    submitCorrectAnswerForQuestion1()

    // Question 2
    submitCorrectAnswerForQuestion2()

    // Question 3
    // Submit question 3 wrong answers (surpass max mastery loss lower bound for this question)
    submitIncorrectAnswerForQuestion3("3/4")
    submitIncorrectAnswerForQuestion3("3/4")
    submitIncorrectAnswerForQuestion3("3/4")
    submitIncorrectAnswerForQuestion3("3/4")
    submitIncorrectAnswerForQuestion3("3/4")
    submitCorrectAnswerForQuestion3()

    val userAssessmentPerformance = getExpectedGrade(TEST_SKILL_ID_LIST_01)
    val skill0Mastery = 0.2
    val skill1Mastery = -0.1
    assertThat(userAssessmentPerformance.masteryPerSkillMappingCount).isEqualTo(2)
    assertThat(userAssessmentPerformance.getMasteryPerSkillMappingOrThrow(TEST_SKILL_ID_0))
      .isWithin(TOLERANCE).of(skill0Mastery)
    assertThat(userAssessmentPerformance.getMasteryPerSkillMappingOrThrow(TEST_SKILL_ID_1))
      .isWithin(TOLERANCE).of(skill1Mastery)
  }

  @Test
  fun multipleHintsViewed_forQuestionWithWrongAnswer_returnMastery0Point01ForLinkedSkill() {
    setUpTestApplicationWithSeed(questionSeed = 0)
    // This will generate question 1 (skill 0), question 2 (skill 0), and question 3 (skill 1)
    startSuccessfulTrainingSession(TEST_SKILL_ID_LIST_01)
    waitForGetCurrentQuestionSuccessfulLoad()

    // Question 1
    // Submit question 1 wrong answer
    submitIncorrectAnswerForQuestion1(2)
    viewHintForQuestion1(submitIncorrectAnswerForQuestion1(2), index = 0)
    submitIncorrectAnswerForQuestion1(2)
    testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(10))
    viewHintForQuestion1(waitForGetCurrentQuestionSuccessfulLoad(), index = 1)
    submitCorrectAnswerForQuestion1()

    // Question 2
    submitCorrectAnswerForQuestion2()

    // Question 3
    submitCorrectAnswerForQuestion3()

    val userAssessmentPerformance = getExpectedGrade(TEST_SKILL_ID_LIST_01)
    val skill0Mastery = 0.01
    val skill1Mastery = 0.1
    assertThat(userAssessmentPerformance.masteryPerSkillMappingCount).isEqualTo(2)
    assertThat(userAssessmentPerformance.getMasteryPerSkillMappingOrThrow(TEST_SKILL_ID_0))
      .isWithin(TOLERANCE).of(skill0Mastery)
    assertThat(userAssessmentPerformance.getMasteryPerSkillMappingOrThrow(TEST_SKILL_ID_1))
      .isWithin(TOLERANCE).of(skill1Mastery)
  }

  @Test
  fun wrongAnswersAllSubmittedWithMisconception_onlyMisconceptionSkillIdMasteryDegreesAffected() {
    setUpTestApplicationWithSeed(questionSeed = 1)
    // This will generate question 1 (skill 0), question 2 (skill 0), and question 0 (skill 0, 1)
    startSuccessfulTrainingSession(TEST_SKILL_ID_LIST_01)
    waitForGetCurrentQuestionSuccessfulLoad()

    // Question 1
    submitCorrectAnswerForQuestion1()

    // Question 2
    submitCorrectAnswerForQuestion2()

    // Question 0
    // Submit question 0 wrong answers
    submitIncorrectAnswerForQuestion0("123/456")
    submitIncorrectAnswerForQuestion0("123/456")
    submitCorrectAnswerForQuestion0()

    val userAssessmentPerformance = getExpectedGrade(TEST_SKILL_ID_LIST_01)
    val skill0Mastery = 0.3
    val skill1Mastery = 0.0
    assertThat(userAssessmentPerformance.masteryPerSkillMappingCount).isEqualTo(2)
    assertThat(userAssessmentPerformance.getMasteryPerSkillMappingOrThrow(TEST_SKILL_ID_0))
      .isWithin(TOLERANCE).of(skill0Mastery)
    assertThat(userAssessmentPerformance.getMasteryPerSkillMappingOrThrow(TEST_SKILL_ID_1))
      .isWithin(TOLERANCE).of(skill1Mastery)
  }

  @Test
  fun someWrongAnswersSubmittedWithTaggedMisconceptionSkillId() {
    setUpTestApplicationWithSeed(questionSeed = 1)
    // This will generate question 1 (skill 0), question 2 (skill 0), and question 0 (skill 0, 1)
    startSuccessfulTrainingSession(TEST_SKILL_ID_LIST_01)
    waitForGetCurrentQuestionSuccessfulLoad()

    // Question 1
    submitCorrectAnswerForQuestion1()

    // Question 2
    submitCorrectAnswerForQuestion2()

    // Question 0
    // Submit question 0 wrong answers
    submitIncorrectAnswerForQuestion0("4/5")
    submitIncorrectAnswerForQuestion0("123/456")
    submitCorrectAnswerForQuestion0()

    val userAssessmentPerformance = getExpectedGrade(TEST_SKILL_ID_LIST_01)
    val skill0Mastery = 0.25
    val skill1Mastery = 0.0
    assertThat(userAssessmentPerformance.masteryPerSkillMappingCount).isEqualTo(2)
    assertThat(userAssessmentPerformance.getMasteryPerSkillMappingOrThrow(TEST_SKILL_ID_0))
      .isWithin(TOLERANCE).of(skill0Mastery)
    assertThat(userAssessmentPerformance.getMasteryPerSkillMappingOrThrow(TEST_SKILL_ID_1))
      .isWithin(TOLERANCE).of(skill1Mastery)
  }

  /* Localization-based tests. */

  @Test
  @RunOn(buildEnvironments = [BuildEnvironment.BAZEL]) // Languages unsupported in Gradle builds.
  fun testGetCurrentState_englishLocale_defaultContentLang_includesTranslationContextForEnglish() {
    setUpTestApplicationWithSeed(questionSeed = 1)
    forceDefaultLocale(Locale.US)
    startSuccessfulTrainingSession(profileId1, TEST_SKILL_ID_LIST_01)

    val ephemeralState = waitForGetCurrentQuestionSuccessfulLoad().ephemeralState

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
    setUpTestApplicationWithSeed(questionSeed = 1)
    forceDefaultLocale(EGYPT_ARABIC_LOCALE)
    startSuccessfulTrainingSession(profileId1, TEST_SKILL_ID_LIST_01)

    val ephemeralState = waitForGetCurrentQuestionSuccessfulLoad().ephemeralState

    // Arabic translations should be included per the locale.
    assertThat(ephemeralState.writtenTranslationContext.language).isEqualTo(OppiaLanguage.ARABIC)
    assertThat(ephemeralState.writtenTranslationContext.translationsMap).isNotEmpty()
  }

  @Test
  fun testGetCurrentState_turkishLocale_defaultContentLang_includesDefaultTranslationContext() {
    setUpTestApplicationWithSeed(questionSeed = 1)
    forceDefaultLocale(TURKEY_TURKISH_LOCALE)
    startSuccessfulTrainingSession(profileId1, TEST_SKILL_ID_LIST_01)

    val ephemeralState = waitForGetCurrentQuestionSuccessfulLoad().ephemeralState

    // No translations match to an unsupported language, so default to the built-in strings.
    assertThat(ephemeralState.writtenTranslationContext).isEqualToDefaultInstance()
  }

  @Test
  @RunOn(buildEnvironments = [BuildEnvironment.BAZEL]) // Languages unsupported in Gradle builds.
  fun testGetCurrentState_englishLangProfile_includesTranslationContextForEnglish() {
    setUpTestApplicationWithSeed(questionSeed = 1)
    val englishProfileId = ProfileId.newBuilder().apply { loggedInInternalProfileId = 2 }.build()
    updateContentLanguage(englishProfileId, OppiaLanguage.ENGLISH)
    startSuccessfulTrainingSession(englishProfileId, TEST_SKILL_ID_LIST_01)

    val ephemeralState = waitForGetCurrentQuestionSuccessfulLoad().ephemeralState

    // English translations means only a language specification.
    val expectedContext = WrittenTranslationContext.newBuilder().apply {
      language = OppiaLanguage.ENGLISH
    }.build()
    assertThat(ephemeralState.writtenTranslationContext).isEqualTo(expectedContext)
  }

  @Test
  @RunOn(buildEnvironments = [BuildEnvironment.BAZEL]) // Languages unsupported in Gradle builds.
  fun testGetCurrentState_englishLangProfile_switchToArabic_includesTranslationContextForArabic() {
    setUpTestApplicationWithSeed(questionSeed = 1)
    val englishProfileId = ProfileId.newBuilder().apply { loggedInInternalProfileId = 2 }.build()
    updateContentLanguage(englishProfileId, OppiaLanguage.ENGLISH)
    startSuccessfulTrainingSession(englishProfileId, TEST_SKILL_ID_LIST_01)
    val monitor =
      monitorFactory.createMonitor(questionAssessmentProgressController.getCurrentQuestion())
    monitor.waitForNextSuccessResult()

    // Update the content language & wait for the ephemeral state to update.
    updateContentLanguage(englishProfileId, OppiaLanguage.ARABIC)
    val ephemeralState = monitor.ensureNextResultIsSuccess().ephemeralState

    // Switching to Arabic should result in a new ephemeral state with a translation context.
    assertThat(ephemeralState.writtenTranslationContext.language).isEqualTo(OppiaLanguage.ARABIC)
    assertThat(ephemeralState.writtenTranslationContext.translationsMap).isNotEmpty()
  }

  @Test
  @RunOn(buildEnvironments = [BuildEnvironment.BAZEL]) // Languages unsupported in Gradle builds.
  fun testGetCurrentState_arabicLangProfile_includesTranslationContextForArabic() {
    setUpTestApplicationWithSeed(questionSeed = 1)
    val englishProfileId = ProfileId.newBuilder().apply { loggedInInternalProfileId = 2 }.build()
    val arabicProfileId = ProfileId.newBuilder().apply { loggedInInternalProfileId = 3 }.build()
    updateContentLanguage(englishProfileId, OppiaLanguage.ENGLISH)
    updateContentLanguage(arabicProfileId, OppiaLanguage.ARABIC)
    startSuccessfulTrainingSession(arabicProfileId, TEST_SKILL_ID_LIST_01)

    val ephemeralState = waitForGetCurrentQuestionSuccessfulLoad().ephemeralState

    // Selecting the profile with Arabic translations should provide a translation context.
    assertThat(ephemeralState.writtenTranslationContext.language).isEqualTo(OppiaLanguage.ARABIC)
    assertThat(ephemeralState.writtenTranslationContext.translationsMap).isNotEmpty()
  }

  private fun setUpTestApplicationWithSeed(questionSeed: Long) {
    TestQuestionModule.questionSeed = questionSeed
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  private fun waitForGetCurrentQuestionSuccessfulLoad(): EphemeralQuestion {
    return monitorFactory.waitForNextSuccessfulResult(
      questionAssessmentProgressController.getCurrentQuestion()
    )
  }

  private fun startSuccessfulTrainingSession(skillIdList: List<String>) {
    startSuccessfulTrainingSession(profileId1, skillIdList)
  }

  private fun startSuccessfulTrainingSession(profileId: ProfileId, skillIdList: List<String>) {
    monitorFactory.waitForNextSuccessfulResult(
      questionTrainingController.startQuestionTrainingSession(profileId, skillIdList)
    )
  }

  private fun startFailureTrainingSession(skillIdList: List<String>): Throwable {
    return monitorFactory.waitForNextFailureResult(
      questionTrainingController.startQuestionTrainingSession(profileId1, skillIdList)
    )
  }

  private fun submitMultipleChoiceAnswer(choiceIndex: Int): EphemeralQuestion {
    return submitAnswer(createMultipleChoiceAnswer(choiceIndex))
  }

  private fun submitTextInputAnswer(textAnswer: String): EphemeralQuestion {
    return submitAnswer(createTextInputAnswer(textAnswer))
  }

  private fun submitNumericInputAnswer(numericAnswer: Double): EphemeralQuestion {
    return submitAnswer(createNumericInputAnswer(numericAnswer))
  }

  private fun submitAnswer(answer: UserAnswer): EphemeralQuestion {
    monitorFactory.waitForNextSuccessfulResult(
      questionAssessmentProgressController.submitAnswer(answer)
    )
    return waitForGetCurrentQuestionSuccessfulLoad()
  }

  private fun submitMultipleChoiceAnswerAndMoveToNextQuestion(choiceIndex: Int): EphemeralQuestion {
    submitMultipleChoiceAnswer(choiceIndex)
    return moveToNextQuestion()
  }

  private fun submitTextInputAnswerAndMoveToNextQuestion(textAnswer: String): EphemeralQuestion {
    submitTextInputAnswer(textAnswer)
    return moveToNextQuestion()
  }

  private fun submitNumericInputAnswerAndMoveToNextQuestion(
    numericAnswer: Double
  ): EphemeralQuestion {
    submitNumericInputAnswer(numericAnswer)
    return moveToNextQuestion()
  }

  private fun moveToNextQuestion(): EphemeralQuestion {
    // This operation might fail for some tests.
    monitorFactory.ensureDataProviderExecutes(
      questionAssessmentProgressController.moveToNextQuestion()
    )
    return waitForGetCurrentQuestionSuccessfulLoad()
  }

  private fun endTrainingSession() {
    val stopDataProvider = questionTrainingController.stopQuestionTrainingSession()
    monitorFactory.waitForNextSuccessfulResult(stopDataProvider)
  }

  private fun playThroughSessionWithSkillList2() {
    startSuccessfulTrainingSession(TEST_SKILL_ID_LIST_2)
    waitForGetCurrentQuestionSuccessfulLoad()
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

  private fun submitCorrectAnswerForQuestion0(): EphemeralQuestion {
    return submitTextInputAnswerAndMoveToNextQuestion("1/2")
  }

  private fun submitIncorrectAnswerForQuestion0(answer: String): EphemeralQuestion {
    return submitTextInputAnswerAndMoveToNextQuestion(answer)
  }

  private fun submitCorrectAnswerForQuestion1(): EphemeralQuestion {
    return submitMultipleChoiceAnswerAndMoveToNextQuestion(1)
  }

  private fun submitIncorrectAnswerForQuestion1(answer: Int): EphemeralQuestion {
    return submitMultipleChoiceAnswerAndMoveToNextQuestion(answer)
  }

  private fun viewHintForQuestion1(ephemeralQuestion: EphemeralQuestion, index: Int) {
    val hint = ephemeralQuestion.ephemeralState.state.interaction.getHint(index)
    if (index == 0) {
      assertThat(hint.hintContent.html).contains("<p>Hint text will appear here</p>")
    } else if (index == 1) {
      assertThat(hint.hintContent.html).contains("<p>Second hint text will appear here</p>")
    }
    monitorFactory.waitForNextSuccessfulResult(
      questionAssessmentProgressController.submitHintIsRevealed(hintIndex = index)
    )
  }

  private fun submitCorrectAnswerForQuestion2(): EphemeralQuestion {
    return submitNumericInputAnswerAndMoveToNextQuestion(3.0)
  }

  private fun submitIncorrectAnswerForQuestion2(answer: Double): EphemeralQuestion {
    return submitNumericInputAnswerAndMoveToNextQuestion(answer)
  }

  private fun viewHintForQuestion2(ephemeralQuestion: EphemeralQuestion) {
    val hint = ephemeralQuestion.ephemeralState.state.interaction.getHint(0)
    assertThat(hint.hintContent.html).contains("<p>Hint text will appear here</p>")
    monitorFactory.waitForNextSuccessfulResult(
      questionAssessmentProgressController.submitHintIsRevealed(hintIndex = 0)
    )
  }

  private fun viewSolutionForQuestion2(ephemeralQuestion: EphemeralQuestion) {
    val solution = ephemeralQuestion.ephemeralState.state.interaction.solution
    assertThat(solution.correctAnswer.real).isWithin(1e-5).of(3.0)
    monitorFactory.waitForNextSuccessfulResult(
      questionAssessmentProgressController.submitSolutionIsRevealed()
    )
  }

  private fun submitCorrectAnswerForQuestion3(): EphemeralQuestion {
    return submitTextInputAnswerAndMoveToNextQuestion("1/2")
  }

  private fun submitIncorrectAnswerForQuestion3(answer: String): EphemeralQuestion {
    return submitTextInputAnswerAndMoveToNextQuestion(answer)
  }

  private fun viewSolutionForQuestion3(ephemeralQuestion: EphemeralQuestion) {
    val solution = ephemeralQuestion.ephemeralState.state.interaction.solution
    assertThat(solution.correctAnswer.normalizedString).isEqualTo("1/2")
    monitorFactory.waitForNextSuccessfulResult(
      questionAssessmentProgressController.submitSolutionIsRevealed()
    )
  }

  private fun submitCorrectAnswerForQuestion4(): EphemeralQuestion {
    return submitMultipleChoiceAnswerAndMoveToNextQuestion(1)
  }

  private fun submitCorrectAnswerForQuestion5(): EphemeralQuestion {
    return submitNumericInputAnswerAndMoveToNextQuestion(5.0)
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

  private fun getExpectedGrade(skillIdList: List<String>): UserAssessmentPerformance {
    return monitorFactory.waitForNextSuccessfulResult(
      questionAssessmentProgressController.calculateScores(skillIdList)
    )
  }

  // TODO(#89): Move this to a common test application component.
  @Module
  class TestModule {
    @Provides
    @Singleton
    fun provideContext(application: Application): Context {
      return application
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

    @Provides
    @ViewHintScorePenalty
    fun provideViewHintScorePenalty(): Int = 1

    @Provides
    @WrongAnswerScorePenalty
    fun provideWrongAnswerScorePenalty(): Int = 1

    @Provides
    @MaxScorePerQuestion
    fun provideMaxScorePerQuestion(): Int = 10

    @Provides
    @InternalScoreMultiplyFactor
    fun provideInternalScoreMultiplyFactor(): Int = 10

    @Provides
    @MaxMasteryGainPerQuestion
    fun provideMaxMasteryGainPerQuestion(): Int = 10

    @Provides
    @MaxMasteryLossPerQuestion
    fun provideMaxMasteryLossPerQuestion(): Int = -10

    @Provides
    @ViewHintMasteryPenalty
    fun provideViewHintMasteryPenalty(): Int = 2

    @Provides
    @WrongAnswerMasteryPenalty
    fun provideWrongAnswerMasteryPenalty(): Int = 5

    @Provides
    @InternalMasteryMultiplyFactor
    fun provideInternalMasteryMultiplyFactor(): Int = 100
  }

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(
    modules = [
      TestModule::class, TestQuestionModule::class, ContinueModule::class,
      FractionInputModule::class, ItemSelectionInputModule::class, MultipleChoiceInputModule::class,
      NumberWithUnitsRuleModule::class, NumericInputRuleModule::class, TextInputRuleModule::class,
      InteractionsModule::class, DragDropSortInputModule::class, TestLogReportingModule::class,
      ImageClickInputModule::class, LogStorageModule::class, TestDispatcherModule::class,
      RatioInputModule::class, RobolectricModule::class, FakeOppiaClockModule::class,
      CachingTestModule::class, HintsAndSolutionConfigModule::class,
      HintsAndSolutionProdModule::class, NetworkConnectionUtilDebugModule::class,
      AssetModule::class, LocaleProdModule::class, NumericExpressionInputModule::class,
      AlgebraicExpressionInputModule::class, MathEquationInputModule::class,
      LoggingIdentifierModule::class, ApplicationLifecycleModule::class,
      SyncStatusModule::class, PlatformParameterModule::class,
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

    fun inject(controllerTest: QuestionAssessmentProgressControllerTest)
  }

  class TestApplication : Application(), DataProvidersInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerQuestionAssessmentProgressControllerTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(controllerTest: QuestionAssessmentProgressControllerTest) {
      component.inject(controllerTest)
    }

    override fun getDataProvidersInjector(): DataProvidersInjector = component
  }

  private companion object {
    private val TEST_SKILL_ID_LIST_012 =
      listOf(TEST_SKILL_ID_0, TEST_SKILL_ID_1, TEST_SKILL_ID_2) // questions 0, 1, 2, 3, 4, 5
    private val TEST_SKILL_ID_LIST_02 =
      listOf(TEST_SKILL_ID_0, TEST_SKILL_ID_2) // questions 0, 1, 2, 4, 5
    private val TEST_SKILL_ID_LIST_01 =
      listOf(TEST_SKILL_ID_0, TEST_SKILL_ID_1) // questions 0, 1, 2, 3
    private val TEST_SKILL_ID_LIST_2 = listOf(TEST_SKILL_ID_2) // questions 2, 4, 5

    private val EGYPT_ARABIC_LOCALE = Locale("ar", "EG")
    private val TURKEY_TURKISH_LOCALE = Locale("tr", "TR")
  }
}
