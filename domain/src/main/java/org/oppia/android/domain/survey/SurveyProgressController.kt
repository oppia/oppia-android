package org.oppia.android.domain.survey

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.oppia.android.app.model.EphemeralSurveyQuestion
import org.oppia.android.app.model.SurveyQuestion
import org.oppia.android.app.model.SurveyQuestionName
import org.oppia.android.app.model.SurveySelectedAnswer
import org.oppia.android.domain.oppialogger.exceptions.ExceptionsController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProvider
import org.oppia.android.util.data.DataProviders
import org.oppia.android.util.data.DataProviders.Companion.combineWith
import org.oppia.android.util.data.DataProviders.Companion.transformNested
import org.oppia.android.util.threading.BackgroundDispatcher
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

private const val BEGIN_SESSION_RESULT_PROVIDER_ID = "SurveyProgressController.begin_session_result"
private const val EMPTY_QUESTIONS_LIST_DATA_PROVIDER_ID =
  "SurveyProgressController.create_empty_questions_list_data_provider_id"
private const val MONITORED_QUESTION_LIST_PROVIDER_ID = "" +
  "SurveyProgressController.monitored_question_list"
private const val CURRENT_QUESTION_PROVIDER_ID =
  "SurveyProgressController.current_question"
private const val EPHEMERAL_QUESTION_FROM_UPDATED_QUESTION_LIST_PROVIDER_ID =
  "SurveyProgressController.ephemeral_question_from_updated_question_list"
private const val MOVE_TO_NEXT_QUESTION_RESULT_PROVIDER_ID =
  "SurveyProgressController.move_to_next_question_result"
private const val MOVE_TO_PREVIOUS_QUESTION_RESULT_PROVIDER_ID =
  "SurveyProgressController.move_to_previous_question_result"
private const val SUBMIT_ANSWER_RESULT_PROVIDER_ID =
  "SurveyProgressController.submit_answer_result"

/**
 * A default session ID to be used before a session has been initialized.
 *
 * This session ID will never match, so messages that are received with this ID will never be
 * processed.
 */
private const val DEFAULT_SESSION_ID = "default_session_id"

/** Controller for tracking the non-persisted progress of a survey. */
@Singleton
class SurveyProgressController @Inject constructor(
  private val dataProviders: DataProviders,
  private val exceptionsController: ExceptionsController,
  @BackgroundDispatcher private val backgroundCoroutineDispatcher: CoroutineDispatcher
) {
  private var mostRecentSessionId: String? = null
  private val activeSessionId: String
    get() = mostRecentSessionId ?: DEFAULT_SESSION_ID

  private var mostRecentEphemeralQuestionFlow =
    createAsyncResultStateFlow<EphemeralSurveyQuestion>(
      AsyncResult.Failure(IllegalStateException("Survey is not yet initialized."))
    )

  private var mostRecentCommandQueue: SendChannel<ControllerMessage<*>>? = null

  private val monitoredQuestionListDataProvider: DataProviders.NestedTransformedDataProvider<Any?> =
    createCurrentQuestionDataProvider(createEmptyQuestionsListDataProvider())

  /**
   * Begins a survey session based on a set of questions and returns a [DataProvider] indicating
   * whether the start was successful.
   */
  fun beginSurveySession(
    questionsListDataProvider: DataProvider<List<SurveyQuestion>>
  ): DataProvider<Any?> {
    val ephemeralQuestionFlow = createAsyncResultStateFlow<EphemeralSurveyQuestion>()
    val sessionId = UUID.randomUUID().toString().also {
      mostRecentSessionId = it
      mostRecentEphemeralQuestionFlow = ephemeralQuestionFlow
      mostRecentCommandQueue = createControllerCommandActor()
    }
    monitoredQuestionListDataProvider.setBaseDataProvider(questionsListDataProvider) {
      maybeSendReceiveQuestionListEvent(mostRecentCommandQueue, it)
    }
    val beginSessionResultFlow = createAsyncResultStateFlow<Any?>()
    val initializeMessage: ControllerMessage<*> =
      ControllerMessage.InitializeController(
        ephemeralQuestionFlow, sessionId, beginSessionResultFlow
      )
    sendCommandForOperation(initializeMessage) {
      "Failed to schedule command for initializing the survey progress controller."
    }
    return beginSessionResultFlow.convertToSessionProvider(BEGIN_SESSION_RESULT_PROVIDER_ID)
  }

  fun getCurrentQuestion(): DataProvider<EphemeralSurveyQuestion> {
    val ephemeralQuestionDataProvider =
      mostRecentEphemeralQuestionFlow.convertToSessionProvider(CURRENT_QUESTION_PROVIDER_ID)

    // Combine ephemeral question with the monitored question list to ensure that changes to the
    // questions list trigger a recompute of the ephemeral question.
    return monitoredQuestionListDataProvider.combineWith(
      ephemeralQuestionDataProvider, EPHEMERAL_QUESTION_FROM_UPDATED_QUESTION_LIST_PROVIDER_ID
    ) { _, currentQuestion ->
      currentQuestion
    }
  }

  fun submitAnswer(selectedAnswer: SurveySelectedAnswer): DataProvider<Any?> {
    val submitResultFlow = createAsyncResultStateFlow<Any?>()
    val message = ControllerMessage.SubmitAnswer(selectedAnswer, activeSessionId, submitResultFlow)
    sendCommandForOperation(message) { "Failed to schedule command for answer submission." }
    return submitResultFlow.convertToSessionProvider(SUBMIT_ANSWER_RESULT_PROVIDER_ID)
  }

  /**
   * Navigates to the next question in the survey. Calling code is responsible for ensuring this
   * method is only called when it's possible to navigate forward.
   *
   * @return a [DataProvider] indicating whether the movement to the next question was successful,
   *     or a failure if question navigation was attempted at an invalid time (such as if the
   *     current question is pending or terminal). It's recommended that calling code only listen
   *     to this result for failures, and instead rely on [getCurrentQuestion] for observing a
   *     successful transition to another question.
   */
  private fun moveToNextQuestion(): DataProvider<Any?> {
    val moveResultFlow = createAsyncResultStateFlow<Any?>()
    val message = ControllerMessage.MoveToNextQuestion(activeSessionId, moveResultFlow)
    sendCommandForOperation(message) {
      "Failed to schedule command for moving to the next question."
    }
    return moveResultFlow.convertToSessionProvider(MOVE_TO_NEXT_QUESTION_RESULT_PROVIDER_ID)
  }

  /**
   * Navigates to the previous question in the survey. If the user is currently on the initial
   * question, this method will throw an exception. Calling code is responsible for ensuring this
   * method is only called when it's possible to navigate backward.
   *
   * @return a [DataProvider] indicating whether the movement to the previous question was
   *     successful, or a failure if question navigation was attempted at an invalid time
   *     (such as if the user is viewing the first question in the survey). It's recommended that
   *     calling code only listen to this result for failures, and instead rely on
   *     [getCurrentQuestion] for observing a successful transition to another question.
   */
  fun moveToPreviousQuestion(): DataProvider<Any?> {
    val moveResultFlow = createAsyncResultStateFlow<Any?>()
    val message = ControllerMessage.MoveToPreviousQuestion(activeSessionId, moveResultFlow)
    sendCommandForOperation(message) {
      "Failed to schedule command for moving to the previous question."
    }
    return moveResultFlow.convertToSessionProvider(MOVE_TO_PREVIOUS_QUESTION_RESULT_PROVIDER_ID)
  }

  private fun createCurrentQuestionDataProvider(
    questionsListDataProvider: DataProvider<List<SurveyQuestion>>
  ): DataProviders.NestedTransformedDataProvider<Any?> {
    return questionsListDataProvider.transformNested(MONITORED_QUESTION_LIST_PROVIDER_ID) {
      maybeSendReceiveQuestionListEvent(commandQueue = null, it)
    }
  }

  /** Returns a [DataProvider] that always provides an empty list of [SurveyQuestion]s. */
  private fun createEmptyQuestionsListDataProvider(): DataProvider<List<SurveyQuestion>> {
    return dataProviders.createInMemoryDataProvider(EMPTY_QUESTIONS_LIST_DATA_PROVIDER_ID) {
      listOf()
    }
  }

  private fun createControllerCommandActor(): SendChannel<ControllerMessage<*>> {
    lateinit var controllerState: ControllerState

    @Suppress("JoinDeclarationAndAssignment") // Warning is incorrect in this case.
    lateinit var commandQueue: SendChannel<ControllerMessage<*>>
    commandQueue = CoroutineScope(
      backgroundCoroutineDispatcher
    ).actor(capacity = Channel.UNLIMITED) {
      for (message in channel) {
        when (message) {
          is ControllerMessage.InitializeController -> {
            controllerState = ControllerState(
              SurveyProgress(),
              message.sessionId,
              message.ephemeralQuestionFlow,
              commandQueue
            ).also {
              it.beginSurveySessionImpl(message.callbackFlow)
            }
          }
          is ControllerMessage.FinishSurveySession -> {
            try {
              controllerState.completeSurveyImpl(
                message.callbackFlow
              )
            } finally {
              // Ensure the actor ends since the session requires no further message processing.
              break
            }
          }
          is ControllerMessage.MoveToNextQuestion -> controllerState.moveToNextQuestion(
            message.callbackFlow
          )
          is ControllerMessage.MoveToPreviousQuestion -> controllerState.moveToPreviousQuestion(
            message.callbackFlow
          )
          is ControllerMessage.RecomputeQuestionAndNotify ->
            controllerState.recomputeCurrentQuestionAndNotifyImpl()
          is ControllerMessage.SubmitAnswer -> controllerState.submitAnswerImpl(
            message.callbackFlow,
            message.selectedAnswer
          )
          is ControllerMessage.ReceiveQuestionList -> controllerState.handleUpdatedQuestionsList(
            message.questionsList
          )
        }
      }
    }
    return commandQueue
  }

  private fun <T> sendCommandForOperation(
    message: ControllerMessage<T>,
    lazyFailureMessage: () -> String
  ) {
    // TODO(#4119): Switch this to use trySend(), instead, which is much cleaner and doesn't require
    //  catching an exception.
    val flowResult: AsyncResult<T> = try {
      val commandQueue = mostRecentCommandQueue
      when {
        commandQueue == null ->
          AsyncResult.Failure(IllegalStateException("Session isn't initialized yet."))
        !commandQueue.offer(message) ->
          AsyncResult.Failure(IllegalStateException(lazyFailureMessage()))
        // Ensure that the result is first reset since there will be a delay before the message is
        // processed (if there's a flow).
        else -> AsyncResult.Pending()
      }
    } catch (e: Exception) {
      AsyncResult.Failure(e)
    }
    // This must be assigned separately since flowResult should always be calculated, even if
    // there's no callbackFlow to report it.
    message.callbackFlow?.value = flowResult
  }

  private suspend fun maybeSendReceiveQuestionListEvent(
    commandQueue: SendChannel<ControllerMessage<*>>?,
    questionsList: List<SurveyQuestion>
  ): AsyncResult<Any?> {
    // Only send the message if there's a queue to send it to (which there might not be for cases
    // where a session isn't active).
    commandQueue?.send(ControllerMessage.ReceiveQuestionList(questionsList, activeSessionId))
    return AsyncResult.Success(null)
  }

  private suspend fun ControllerState.beginSurveySessionImpl(
    beginSessionResultFlow: MutableStateFlow<AsyncResult<Any?>>
  ) {
    tryOperation(beginSessionResultFlow) {
      recomputeCurrentQuestionAndNotifyAsync()
      progress.advancePlayStageTo(SurveyProgress.SurveyStage.LOADING_SURVEY_SESSION)
    }
  }

  private suspend fun ControllerState.completeSurveyImpl(
    endSessionResultFlow: MutableStateFlow<AsyncResult<Any?>>
  ) {
    tryOperation(endSessionResultFlow) {
    }
  }

  private fun <T> createAsyncResultStateFlow(initialValue: AsyncResult<T> = AsyncResult.Pending()) =
    MutableStateFlow(initialValue)

  private fun <T> StateFlow<AsyncResult<T>>.convertToSessionProvider(
    baseId: String
  ): DataProvider<T> = dataProviders.run {
    convertAsyncToAutomaticDataProvider("${baseId}_$activeSessionId")
  }

  /**
   * Represents a message that can be sent to [mostRecentCommandQueue] to process changes to
   * [ControllerState] (since all changes must be synchronized).
   *
   * Messages are expected to be resolved serially (though their scheduling can occur across
   * multiple threads, so order cannot be guaranteed until they're enqueued).
   */
  private sealed class ControllerMessage<T> {
    /**
     * The session ID corresponding to this message (the message is expected to be ignored if it
     * doesn't correspond to an active session).
     */
    abstract val sessionId: String

    /**
     * The [DataProvider]-tied [MutableStateFlow] that represents the result of the operation
     * corresponding to this message, or ``null`` if the caller doesn't care about observing the
     * result.
     */
    abstract val callbackFlow: MutableStateFlow<AsyncResult<T>>?

    /** [ControllerMessage] for initializing a new survey session. */
    data class InitializeController(
      val ephemeralQuestionFlow: MutableStateFlow<AsyncResult<EphemeralSurveyQuestion>>,
      override val sessionId: String,
      override val callbackFlow: MutableStateFlow<AsyncResult<Any?>>
    ) : ControllerMessage<Any?>()

    /** [ControllerMessage] for ending the current survey session. */
    data class FinishSurveySession(
      override val sessionId: String,
      override val callbackFlow: MutableStateFlow<AsyncResult<Any?>>
    ) : ControllerMessage<Any?>()

    /** [ControllerMessage] for submitting a new [SurveySelectedAnswer]. */
    data class SubmitAnswer(
      val selectedAnswer: SurveySelectedAnswer,
      override val sessionId: String,
      override val callbackFlow: MutableStateFlow<AsyncResult<Any?>>
    ) : ControllerMessage<Any?>()

    /** [ControllerMessage] to move to the previous question in the survey. */
    data class MoveToPreviousQuestion(
      override val sessionId: String,
      override val callbackFlow: MutableStateFlow<AsyncResult<Any?>>
    ) : ControllerMessage<Any?>()

    /** [ControllerMessage] to move to the next question in the survey. */
    data class MoveToNextQuestion(
      override val sessionId: String,
      override val callbackFlow: MutableStateFlow<AsyncResult<Any?>>
    ) : ControllerMessage<Any?>()

    /**
     * [ControllerMessage] which recomputes the current [EphemeralSurveyQuestion] and notifies
     * subscribers of the [DataProvider] returned by [getCurrentQuestion] of the change.
     * This is only used in cases where an external operation trigger changes that are only
     * reflected when recomputing the question (e.g. an answer was changed).
     */
    data class RecomputeQuestionAndNotify(
      override val sessionId: String,
      override val callbackFlow: MutableStateFlow<AsyncResult<Any?>>? = null
    ) : ControllerMessage<Any?>()

    /**
     * [ControllerMessage] for finishing the initialization of the survey session by providing a
     * list of [SurveyQuestion]s to display.
     */
    data class ReceiveQuestionList(
      val questionsList: List<SurveyQuestion>,
      override val sessionId: String,
      override val callbackFlow: MutableStateFlow<AsyncResult<Any?>>? = null
    ) : ControllerMessage<Any?>()
  }

  private suspend fun ControllerState.submitAnswerImpl(
    submitAnswerResultFlow: MutableStateFlow<AsyncResult<Any?>>,
    selectedAnswer: SurveySelectedAnswer
  ) {
    tryOperation(submitAnswerResultFlow) {
      check(progress.surveyStage != SurveyProgress.SurveyStage.SUBMITTING_ANSWER) {
        "Cannot submit an answer while another answer is pending."
      }
      if (selectedAnswer.questionName == SurveyQuestionName.NPS) {
        progress.questionGraph.computeFeedbackQuestion(
          3,
          selectedAnswer.npsScore
        )
      }
    }.also {
      if (!progress.questionDeck.isCurrentQuestionTerminal()) {
        moveToNextQuestion()
      }
    }
  }

  private suspend fun ControllerState.handleUpdatedQuestionsList(
    questionsList: List<SurveyQuestion>
  ) {
    // The questions list is possibly changed which may affect the computed ephemeral question.
    if (!this.isQuestionsListInitialized || this.questionsList != questionsList) {
      this.questionsList = questionsList
      // Only notify if the questions list is different (otherwise an infinite notify loop might be
      // started).
      recomputeCurrentQuestionAndNotifySync()
    }
  }

  private suspend fun <T> ControllerState.tryOperation(
    resultFlow: MutableStateFlow<AsyncResult<T>>,
    operation: suspend ControllerState.() -> T
  ) {
    try {
      resultFlow.emit(AsyncResult.Success(operation()))
      recomputeCurrentQuestionAndNotifySync()
    } catch (e: Exception) {
      exceptionsController.logNonFatalException(e)
      resultFlow.emit(AsyncResult.Failure(e))
    }
  }

  private suspend fun ControllerState.moveToNextQuestion(
    moveToNextQuestionResultFlow: MutableStateFlow<AsyncResult<Any?>>
  ) {
    tryOperation(moveToNextQuestionResultFlow) {
      check(progress.surveyStage != SurveyProgress.SurveyStage.SUBMITTING_ANSWER) {
        "Cannot navigate to a next question if an answer submission is pending."
      }
      progress.questionDeck.navigateToNextQuestion()
      if (progress.isViewingMostRecentQuestion()) {
        progress.processNavigationToNewQuestion()
      }
    }
  }

  private suspend fun ControllerState.moveToPreviousQuestion(
    moveToPreviousQuestionResultFlow: MutableStateFlow<AsyncResult<Any?>>
  ) {
    tryOperation(moveToPreviousQuestionResultFlow) {
      check(progress.surveyStage != SurveyProgress.SurveyStage.LOADING_SURVEY_SESSION) {
        "Cannot navigate to a previous question if a session is being loaded."
      }
      check(progress.surveyStage != SurveyProgress.SurveyStage.SUBMITTING_ANSWER) {
        "Cannot navigate to a previous question if an answer submission is pending."
      }
      progress.questionDeck.navigateToPreviousQuestion()
    }
  }

  /**
   * Immediately recomputes the current question & notifies it's been changed.
   *
   * This should only be called when the caller can guarantee that the current [ControllerState] is
   * correct and up-to-date (i.e. that this is being called via a direct call path from the actor).
   *
   * All other cases must use [recomputeCurrentQuestionAndNotifyAsync].
   */
  private suspend fun ControllerState.recomputeCurrentQuestionAndNotifySync() {
    recomputeCurrentQuestionAndNotifyImpl()
  }

  /**
   * Sends a message to recompute the current question & notify it's been changed.
   *
   * This must be used in cases when the current [ControllerState] may no longer be up-to-date to
   * ensure state isn't leaked across training sessions.
   */
  private suspend fun ControllerState.recomputeCurrentQuestionAndNotifyAsync() {
    commandQueue.send(ControllerMessage.RecomputeQuestionAndNotify(sessionId))
  }

  private suspend fun ControllerState.recomputeCurrentQuestionAndNotifyImpl() {
    ephemeralQuestionFlow.emit(
      if (isQuestionsListInitialized) {
        // Only compute the ephemeral question if there's a questions list loaded (otherwise the
        // controller is in a pending state).
        retrieveCurrentQuestionAsync(questionsList)
      } else AsyncResult.Pending()
    )
  }

  private fun ControllerState.retrieveCurrentQuestionAsync(
    questionsList: List<SurveyQuestion>
  ): AsyncResult<EphemeralSurveyQuestion> {
    return try {
      when (progress.surveyStage) {
        SurveyProgress.SurveyStage.NOT_IN_SURVEY_SESSION -> AsyncResult.Pending()
        SurveyProgress.SurveyStage.LOADING_SURVEY_SESSION -> {
          // If the survey hasn't yet been initialized, initialize it
          // now that a list of questions is available.
          initializeSurvey(questionsList)
          progress.advancePlayStageTo(SurveyProgress.SurveyStage.VIEWING_SURVEY_QUESTION)
          AsyncResult.Success(
            retrieveEphemeralQuestion()
          )
        }
        SurveyProgress.SurveyStage.VIEWING_SURVEY_QUESTION -> {
          AsyncResult.Success(
            retrieveEphemeralQuestion()
          )
        }
        SurveyProgress.SurveyStage.SUBMITTING_ANSWER -> AsyncResult.Pending()
      }
    } catch (e: Exception) {
      exceptionsController.logNonFatalException(e)
      AsyncResult.Failure(e)
    }
  }

  private fun ControllerState.initializeSurvey(questionsList: List<SurveyQuestion>) {
    check(questionsList.isNotEmpty()) { "Cannot start a survey session with zero questions." }
    progress.initialize(questionsList)
  }

  private fun ControllerState.retrieveEphemeralQuestion():
    EphemeralSurveyQuestion {
      val currentQuestionIndex = progress.getCurrentQuestionIndex()
      val currentQuestion = progress.questionGraph.getQuestion(currentQuestionIndex)
      return EphemeralSurveyQuestion.newBuilder()
        .setQuestion(currentQuestion)
        .setCurrentQuestionIndex(currentQuestionIndex)
        .setTotalQuestionCount(progress.getTotalQuestionCount())
        .setTerminalQuestion(progress.questionDeck.isCurrentQuestionTerminal())
        .build()
    }

  /**
   * Represents the current synchronized state of the controller.
   *
   * This object's instance is tied directly to a single training session, and it's not thread-safe
   * so all access must be synchronized.
   *
   * @property progress the [SurveyProgress] corresponding to the session
   * @property sessionId the GUID corresponding to the session
   * @property ephemeralQuestionFlow the [MutableStateFlow] that the updated
   *     [EphemeralSurveyQuestion] is delivered to.
   * @property commandQueue the actor command queue executing all messages that change this state
   */
  private class ControllerState(
    val progress: SurveyProgress,
    val sessionId: String,
    val ephemeralQuestionFlow: MutableStateFlow<AsyncResult<EphemeralSurveyQuestion>>,
    val commandQueue: SendChannel<ControllerMessage<*>>
  ) {
    /**
     * The list of [SurveyQuestion]s currently being played in the training session.
     *
     * Because this is updated based on [ControllerMessage.ReceiveQuestionList], it may not be
     * initialized at the beginning of a session. Callers should check [isQuestionsListInitialized]
     * prior to accessing this field.
     */
    lateinit var questionsList: List<SurveyQuestion>

    /** Indicates whether [questionsList] is initialized with values. */
    val isQuestionsListInitialized: Boolean
      get() = ::questionsList.isInitialized
  }
}
