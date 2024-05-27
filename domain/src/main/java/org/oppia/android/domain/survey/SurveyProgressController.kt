package org.oppia.android.domain.survey

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.oppia.android.app.model.EphemeralSurveyQuestion
import org.oppia.android.app.model.MarketFitAnswer
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.SelectedAnswerDatabase
import org.oppia.android.app.model.SurveyQuestion
import org.oppia.android.app.model.SurveyQuestionName
import org.oppia.android.app.model.SurveySelectedAnswer
import org.oppia.android.app.model.SurveySelectedAnswer.AnswerCase
import org.oppia.android.app.model.UserTypeAnswer
import org.oppia.android.data.persistence.PersistentCacheStore
import org.oppia.android.domain.oppialogger.exceptions.ExceptionsController
import org.oppia.android.domain.oppialogger.survey.SurveyEventsLogger
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProvider
import org.oppia.android.util.data.DataProviders
import org.oppia.android.util.data.DataProviders.Companion.combineWith
import org.oppia.android.util.data.DataProviders.Companion.transformAsync
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
private const val END_SESSION_RESULT_PROVIDER_ID = "SurveyProgressController.end_session_result"
private const val RETRIEVE_RESPONSE_DATA_PROVIDER_ID =
  "retrieve_response_provider_id"
private const val AUGMENTED_QUESTION_PROVIDER_ID =
  "SurveyProgressController.augmented_question"

/**
 * A default session ID to be used before a session has been initialized.
 *
 * This session ID will never match, so messages that are received with this ID will never be
 * processed.
 */
private const val DEFAULT_SESSION_ID = "default_session_id"

/** The name of the cache used to hold selected survey responses ephemerally. */
private const val CACHE_NAME = "survey_responses_database"

/** Controller for tracking the non-persisted progress of a survey. */
@Singleton
class SurveyProgressController @Inject constructor(
  private val dataProviders: DataProviders,
  private val exceptionsController: ExceptionsController,
  @BackgroundDispatcher private val backgroundCoroutineDispatcher: CoroutineDispatcher,
  cacheStoreFactory: PersistentCacheStore.Factory,
  private val surveyLogger: SurveyEventsLogger
) {
  // TODO(#606): Replace this with a profile scope.
  private lateinit var profileId: ProfileId
  private lateinit var surveyId: String

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

  private val answerDataStore =
    cacheStoreFactory.create(CACHE_NAME, SelectedAnswerDatabase.getDefaultInstance())

  /**
   * Statuses correspond to the exceptions such that if the deferred contains an error state,
   * a corresponding exception will be passed to a failed AsyncResult.
   */
  private enum class RecordResponseActionStatus {
    /** Corresponds to a successful AsyncResult. */
    SUCCESS,

    /** Corresponds to a failed saving attempt. */
    FAILED_TO_SAVE_RESPONSE
  }

  /**
   * Begins a survey session based on a set of questions and returns a [DataProvider] indicating
   * whether the start was successful.
   */
  fun beginSurveySession(
    surveyId: String,
    profileId: ProfileId,
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
    this.profileId = profileId
    this.surveyId = surveyId
    sendCommandForOperation(initializeMessage) {
      "Failed to schedule command for initializing the survey progress controller."
    }
    return beginSessionResultFlow.convertToSessionProvider(BEGIN_SESSION_RESULT_PROVIDER_ID)
  }

  /**
   * Returns a [DataProvider] monitoring the [EphemeralSurveyQuestion] the user is currently
   * viewing.
   *
   * This [DataProvider] may switch from a completed to a pending result during transient operations
   * like submitting an answer via [submitAnswer]. Calling code should be made resilient to this by
   * caching the current question object to display since it may disappear temporarily during answer
   * submission. Calling code should persist this object across configuration changes if
   * needed since it cannot rely on this [DataProvider] for immediate UI reconstitution after
   * configuration changes.
   *
   * The underlying question returned by this function can only be changed by calls to
   * [moveToNextQuestion], or [moveToPreviousQuestion].
   *
   * This method does not need to be called for the [EphemeralSurveyQuestion] to be computed;
   * it's always computed eagerly by other state-changing methods regardless of whether there's an
   * active subscription to this method's returned [DataProvider].
   */
  fun getCurrentQuestion(): DataProvider<EphemeralSurveyQuestion> {
    val ephemeralQuestionDataProvider =
      mostRecentEphemeralQuestionFlow.convertToSessionProvider(CURRENT_QUESTION_PROVIDER_ID)

    // Combine ephemeral question with the monitored question list to ensure that changes to the
    // questions list trigger a recompute of the ephemeral question.
    val questionsListDataProvider = monitoredQuestionListDataProvider.combineWith(
      ephemeralQuestionDataProvider, EPHEMERAL_QUESTION_FROM_UPDATED_QUESTION_LIST_PROVIDER_ID
    ) { _, currentQuestion ->
      currentQuestion
    }
    val previousAnswerProvider =
      questionsListDataProvider.transformAsync(
        RETRIEVE_RESPONSE_DATA_PROVIDER_ID
      ) { ephemeralQuestion ->
        return@transformAsync AsyncResult.Success(
          retrieveSelectedAnswer(ephemeralQuestion.question.questionId.toString())
        )
      }
    return previousAnswerProvider.combineWith(
      questionsListDataProvider, AUGMENTED_QUESTION_PROVIDER_ID
    ) { previousSelectedAnswer, ephemeralQuestion ->
      return@combineWith if (previousSelectedAnswer != SurveySelectedAnswer.getDefaultInstance()) {
        augmentEphemeralQuestion(previousSelectedAnswer, ephemeralQuestion)
      } else ephemeralQuestion
    }
  }

  /**
   * Submits an answer to the current question and returns how the UI should respond.
   *
   * If the app undergoes a configuration change, calling code should rely on the [DataProvider]
   * from [getCurrentQuestion] to know whether a current answer is pending. That [DataProvider] will
   * have its state changed to pending during answer submission.
   *
   * No assumptions should be made about the completion order of the returned [DataProvider] vs. the
   * [DataProvider] from [getCurrentQuestion].
   */
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
  fun moveToNextQuestion(): DataProvider<Any?> {
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

  /**
   * Ends the current survey session and returns a [DataProvider] that indicates whether it was
   * successfully ended.
   *
   * This method must be called to explicitly notify the controller that the survey session is being
   * stopped, in order to maybe save the responses.
   *
   * @param surveyCompleted whether this finish action indicates that the survey was fully completed by
   *     the user.
   */
  fun endSurveySession(
    surveyCompleted: Boolean
  ): DataProvider<Any?> {
    // Reset the base questions list provider so that the ephemeral question has no question list to
    // reference (since the session finished).
    monitoredQuestionListDataProvider.setBaseDataProvider(createEmptyQuestionsListDataProvider()) {
      maybeSendReceiveQuestionListEvent(commandQueue = null, it)
    }
    val endSessionResultFlow = createAsyncResultStateFlow<Any?>()
    val message = ControllerMessage.FinishSurveySession(
      surveyCompleted, activeSessionId, endSessionResultFlow
    )
    sendCommandForOperation(message) {
      "Failed to schedule command for finishing the survey session."
    }
    return endSessionResultFlow.convertToSessionProvider(END_SESSION_RESULT_PROVIDER_ID)
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

  @OptIn(ObsoleteCoroutinesApi::class)
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
          is ControllerMessage.MoveToNextQuestion ->
            controllerState.moveToNextQuestionImpl(message.callbackFlow)
          is ControllerMessage.MoveToPreviousQuestion ->
            controllerState.moveToPreviousQuestionImpl(message.callbackFlow)
          is ControllerMessage.RecomputeQuestionAndNotify ->
            controllerState.recomputeCurrentQuestionAndNotifyImpl()
          is ControllerMessage.SubmitAnswer ->
            controllerState.submitAnswerImpl(message.callbackFlow, message.selectedAnswer)
          is ControllerMessage.ReceiveQuestionList ->
            controllerState.handleUpdatedQuestionsList(message.questionsList)
          is ControllerMessage.FinishSurveySession -> {
            try {
              controllerState.completeSurveyImpl(message.callbackFlow)
            } finally {
              // Ensure the actor ends since the session requires no further message processing.
              break
            }
          }
        }
      }
    }
    return commandQueue
  }

  private fun <T> sendCommandForOperation(
    message: ControllerMessage<T>,
    lazyFailureMessage: () -> String
  ) {
    val commandQueue = mostRecentCommandQueue
    val flowResult: AsyncResult<T> = when {
      commandQueue == null ->
        AsyncResult.Failure(IllegalStateException("Session isn't initialized yet."))
      !commandQueue.trySend(message).isSuccess ->
        AsyncResult.Failure(IllegalStateException(lazyFailureMessage()))
      // Ensure that the result is first reset since there will be a delay before the message is
      // processed (if there's a flow).
      else -> AsyncResult.Pending()
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
      answerDataStore.clearCacheAsync()
      progress.advancePlayStageTo(SurveyProgress.SurveyStage.LOADING_SURVEY_SESSION)
    }
  }

  private suspend fun ControllerState.submitAnswerImpl(
    submitAnswerResultFlow: MutableStateFlow<AsyncResult<Any?>>,
    selectedAnswer: SurveySelectedAnswer
  ) {
    tryOperation(submitAnswerResultFlow) {
      check(progress.surveyStage != SurveyProgress.SurveyStage.SUBMITTING_ANSWER) {
        "Cannot submit an answer while another answer is pending."
      }

      val currentQuestionId = progress.questionDeck.getTopQuestionIndex()
      if (selectedAnswer.questionName == SurveyQuestionName.NPS) {
        // compute the feedback question before navigating to it
        progress.questionGraph.computeFeedbackQuestion(
          index = currentQuestionId + 1,
          npsScore = selectedAnswer.npsScore
        )
      }

      if (!progress.questionDeck.isCurrentQuestionTerminal()) {
        saveSelectedAnswer(currentQuestionId.toString(), selectedAnswer)
        moveToNextQuestion()
      } else {
        surveyLogger.logOptionalResponse(surveyId, profileId, selectedAnswer.freeFormAnswer)
      }
    }
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  private fun ControllerState.saveSelectedAnswer(questionId: String, answer: SurveySelectedAnswer) {
    val deferred = recordSelectedAnswerAsync(questionId, answer)

    deferred.invokeOnCompletion {
      if (it == null) {
        progress.questionDeck.trackAnsweredQuestions(answer.questionName)
        deferred.getCompleted()
      } else {
        RecordResponseActionStatus.FAILED_TO_SAVE_RESPONSE
      }
    }
  }

  private fun recordSelectedAnswerAsync(
    questionId: String,
    answer: SurveySelectedAnswer
  ): Deferred<RecordResponseActionStatus> {
    return answerDataStore.storeDataWithCustomChannelAsync(
      updateInMemoryCache = true
    ) { answerDatabase ->
      answerDatabase.toBuilder().apply {
        putSelectedAnswer(questionId, answer)
      }.build() to RecordResponseActionStatus.SUCCESS
    }
  }

  private suspend fun retrieveSelectedAnswer(questionId: String): SurveySelectedAnswer {
    val answerDatabase = answerDataStore.readDataAsync().await()
    return answerDatabase.selectedAnswerMap[questionId] ?: SurveySelectedAnswer.getDefaultInstance()
  }

  private suspend fun ControllerState.moveToNextQuestionImpl(
    moveToNextQuestionResultFlow: MutableStateFlow<AsyncResult<Any?>>
  ) {
    tryOperation(moveToNextQuestionResultFlow) {
      check(progress.surveyStage != SurveyProgress.SurveyStage.SUBMITTING_ANSWER) {
        "Cannot navigate to a next question if an answer submission is pending."
      }
      progress.questionDeck.navigateToNextQuestion()
      progress.refreshDeck()
    }
  }

  private suspend fun ControllerState.moveToPreviousQuestionImpl(
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
      progress.refreshDeck()
    }
  }

  private suspend fun ControllerState.completeSurveyImpl(
    endSessionResultFlow: MutableStateFlow<AsyncResult<Any?>>
  ) {
    checkNotNull(this) { "Cannot stop a survey session which wasn't started." }
    tryOperation(endSessionResultFlow) {
      progress.advancePlayStageTo(SurveyProgress.SurveyStage.NOT_IN_SURVEY_SESSION)
      finishSurveyAndLog()
    }
  }

  private fun <T> createAsyncResultStateFlow(initialValue: AsyncResult<T> = AsyncResult.Pending()) =
    MutableStateFlow(initialValue)

  private fun <T> StateFlow<AsyncResult<T>>.convertToSessionProvider(
    baseId: String
  ): DataProvider<T> = dataProviders.run {
    convertAsyncToAutomaticDataProvider("${baseId}_$activeSessionId")
  }

  private suspend fun ControllerState.finishSurveyAndLog() {
    when {
      progress.questionDeck.hasAnsweredAllMandatoryQuestions() -> {
        surveyLogger.logMandatoryResponses(
          surveyId,
          profileId,
          getStoredResponseToUserType(),
          getStoredResponseToMarketFitAnswer(),
          getStoredResponseToNpsScore()
        )
      }
      else -> {
        val currentQuestionName = progress.questionGraph
          .getQuestion(progress.questionDeck.getTopQuestionIndex())
          .questionName

        surveyLogger.logAbandonSurvey(
          surveyId,
          profileId,
          currentQuestionName
        )
      }
    }
    answerDataStore.clearCacheAsync()
  }

  private suspend fun getStoredResponseToUserType(): UserTypeAnswer {
    return getStoredResponse(
      SurveyQuestionName.USER_TYPE, AnswerCase.USER_TYPE, SurveySelectedAnswer::getUserType
    )
  }

  private suspend fun getStoredResponseToMarketFitAnswer(): MarketFitAnswer {
    return getStoredResponse(
      SurveyQuestionName.MARKET_FIT, AnswerCase.MARKET_FIT, SurveySelectedAnswer::getMarketFit
    )
  }

  private suspend fun getStoredResponseToNpsScore(): Int {
    return getStoredResponse(
      SurveyQuestionName.NPS, AnswerCase.NPS_SCORE, SurveySelectedAnswer::getNpsScore
    )
  }

  private suspend inline fun <reified T : Any> getStoredResponse(
    questionName: SurveyQuestionName,
    expectedAnswerType: AnswerCase,
    extractValue: SurveySelectedAnswer.() -> T
  ): T {
    val answerDatabase = answerDataStore.readDataAsync().await()
    return answerDatabase.selectedAnswerMap.values.find { it.questionName == questionName }?.let {
      check(it.answerCase == expectedAnswerType) {
        "Expected answer for question $questionName to be type $expectedAnswerType, not" +
          " ${it.answerCase}."
      }
      return@let it.extractValue()
    } ?: error("Expected answer $expectedAnswerType in question $questionName (was missing).")
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
      val surveyCompleted: Boolean,
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
   * This must be used in cases when the current [ControllerState] may no longer be up-to-date.
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

  private suspend fun ControllerState.retrieveCurrentQuestionAsync(
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
          AsyncResult.Success(computeBaseCurrentEphemeralQuestion())
        }
        SurveyProgress.SurveyStage.VIEWING_SURVEY_QUESTION -> {
          AsyncResult.Success(computeBaseCurrentEphemeralQuestion())
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

  private fun ControllerState.computeBaseCurrentEphemeralQuestion(): EphemeralSurveyQuestion =
    progress.questionDeck.getCurrentEphemeralQuestion()

  /**
   * Augments the specified [EphemeralSurveyQuestion] [AsyncResult] by attaching a previously
   * selected answer to update the UI.
   */
  private fun augmentEphemeralQuestion(
    previousAnswer: SurveySelectedAnswer,
    ephemeralQuestion: EphemeralSurveyQuestion
  ): EphemeralSurveyQuestion {
    return ephemeralQuestion.toBuilder().apply {
      selectedAnswer = previousAnswer
    }.build()
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
