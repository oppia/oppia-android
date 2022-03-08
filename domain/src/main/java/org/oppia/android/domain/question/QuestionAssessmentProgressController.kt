package org.oppia.android.domain.question

import androidx.lifecycle.LiveData
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.oppia.android.app.model.AnsweredQuestionOutcome
import org.oppia.android.app.model.EphemeralQuestion
import org.oppia.android.app.model.EphemeralState
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.Question
import org.oppia.android.app.model.State
import org.oppia.android.app.model.UserAnswer
import org.oppia.android.app.model.UserAssessmentPerformance
import org.oppia.android.domain.classify.AnswerClassificationController
import org.oppia.android.domain.classify.ClassificationResult.OutcomeWithMisconception
import org.oppia.android.domain.hintsandsolution.HintHandler
import org.oppia.android.domain.oppialogger.exceptions.ExceptionsController
import org.oppia.android.domain.question.QuestionAssessmentProgress.TrainStage
import org.oppia.android.domain.translation.TranslationController
import org.oppia.android.util.data.AsyncDataSubscriptionManager
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProvider
import org.oppia.android.util.data.DataProviders
import org.oppia.android.util.data.DataProviders.Companion.combineWith
import org.oppia.android.util.data.DataProviders.Companion.transformNested
import org.oppia.android.util.data.DataProviders.NestedTransformedDataProvider
import org.oppia.android.util.locale.OppiaLocale
import org.oppia.android.util.threading.BackgroundDispatcher

private const val BEGIN_SESSION_RESULT_PROVIDER_ID =
  "QuestionAssessmentProgressController.begin_session_result"
private const val FINISH_SESSION_RESULT_PROVIDER_ID =
  "QuestionAssessmentProgressController.finish_session_result"
private const val SUBMIT_ANSWER_RESULT_PROVIDER_ID =
  "QuestionAssessmentProgressController.submit_answer_result"
private const val SUBMIT_HINT_REVEALED_RESULT_PROVIDER_ID =
  "QuestionAssessmentProgressController.submit_hint_revealed_result"
private const val SUBMIT_SOLUTION_REVEALED_RESULT_PROVIDER_ID =
  "QuestionAssessmentProgressController.submit_solution_revealed_result"
private const val MOVE_TO_NEXT_QUESTION_RESULT_PROVIDER_ID =
  "QuestionAssessmentProgressController.move_to_next_question_result"
private const val CURRENT_QUESTION_PROVIDER_ID =
  "QuestionAssessmentProgressController.current_question"
private const val CALCULATE_SCORES_PROVIDER_ID =
  "QuestionAssessmentProgressController.calculate_scores"
private const val MONITORED_QUESTION_LIST_PROVIDER_ID = "" +
  "QuestionAssessmentProgressController.monitored_question_list"
private const val LOCALIZED_QUESTION_PROVIDER_ID =
  "QuestionAssessmentProgressController.localized_question"
private const val EPHEMERAL_QUESTION_FROM_UPDATED_QUESTION_LIST_PROVIDER_ID =
  "QuestionAssessmentProgressController.ephemeral_question_from_updated_question_list"
private const val EMPTY_QUESTIONS_LIST_DATA_PROVIDER_ID =
  "QuestionAssessmentProgressController.create_empty_questions_list_data_provider_id"

/**
 * Controller that tracks and reports the learner's ephemeral/non-persisted progress through a
 * practice training session. Note that this controller only supports one active training session at
 * a time.
 *
 * The current training session is started via the question training controller.
 *
 * This class is thread-safe, but the order of applied operations is arbitrary. Calling code should
 * take care to ensure that uses of this class do not specifically depend on ordering.
 */
@Singleton
class QuestionAssessmentProgressController @Inject constructor(
  private val dataProviders: DataProviders,
  private val asyncDataSubscriptionManager: AsyncDataSubscriptionManager,
  private val answerClassificationController: AnswerClassificationController,
  private val exceptionsController: ExceptionsController,
  private val hintHandlerFactory: HintHandler.Factory,
  private val translationController: TranslationController,
  @BackgroundDispatcher private val backgroundCoroutineDispatcher: CoroutineDispatcher
) {
  // TODO(#247): Add support for populating the list of skill IDs to review at the end of the
  //  training session.
  // TODO(#248): Add support for the assessment ending prematurely due to learner demonstrating
  //  sufficient proficiency.

  // TODO: update the documentation in this class similar to the changes needed in ExplorationProgressController.

  // TODO(#606): Replace this with a profile scope to avoid this hacky workaround (which is needed
  //  for getCurrentQuestion).
  private lateinit var profileId: ProfileId

  private val controllerCommandQueue by lazy { createControllerCommandActor() }
  private val ephemeralQuestionFlow by lazy { createAsyncResultStateFlow<EphemeralQuestion>() }
  private val calculateScoresFlow by lazy { createAsyncResultStateFlow<UserAssessmentPerformance>() }
  private val beginSessionResultFlow by lazy { createAsyncResultStateFlow<Any?>() }
  private val finishSessionResultFlow by lazy { createAsyncResultStateFlow<Any?>() }
  private val submitAnswerResultFlow by lazy {
    createAsyncResultStateFlow<AnsweredQuestionOutcome>()
  }
  private val submitHintRevealedResultFlow by lazy { createAsyncResultStateFlow<Any?>() }
  private val submitSolutionRevealedResultFlow by lazy { createAsyncResultStateFlow<Any?>() }
  private val moveToNextQuestionResultFlow by lazy { createAsyncResultStateFlow<Any?>() }
  private val ephemeralQuestionDataProvider by lazy {
    dataProviders.run {
      ephemeralQuestionFlow.convertAsyncToAutomaticDataProvider(CURRENT_QUESTION_PROVIDER_ID)
    }
  }
  private val calculateScoresDataProvider by lazy {
    dataProviders.run {
      calculateScoresFlow.convertAsyncToAutomaticDataProvider(CALCULATE_SCORES_PROVIDER_ID)
    }
  }
  private val beginSessionResultDataProvider by lazy {
    beginSessionResultFlow.convertToDataProvider(BEGIN_SESSION_RESULT_PROVIDER_ID)
  }
  private val finishSessionResultDataProvider by lazy {
    finishSessionResultFlow.convertToDataProvider(FINISH_SESSION_RESULT_PROVIDER_ID)
  }
  private val submitAnswerResultDataProvider by lazy {
    submitAnswerResultFlow.convertToDataProvider(SUBMIT_ANSWER_RESULT_PROVIDER_ID)
  }
  private val submitHintRevealedResultDataProvider by lazy {
    submitHintRevealedResultFlow.convertToDataProvider(SUBMIT_HINT_REVEALED_RESULT_PROVIDER_ID)
  }
  private val submitSolutionRevealedResultDataProvider by lazy {
    submitSolutionRevealedResultFlow.convertToDataProvider(
      SUBMIT_SOLUTION_REVEALED_RESULT_PROVIDER_ID
    )
  }
  private val moveToNextQuestionResultDataProvider by lazy {
    moveToNextQuestionResultFlow.convertToDataProvider(MOVE_TO_NEXT_QUESTION_RESULT_PROVIDER_ID)
  }

  @Inject
  internal lateinit var scoreCalculatorFactory: QuestionAssessmentCalculation.Factory
  private val monitoredQuestionListDataProvider: NestedTransformedDataProvider<Any?> =
    createCurrentQuestionDataProvider(createEmptyQuestionsListDataProvider())

  internal fun beginQuestionTrainingSession(
    questionsListDataProvider: DataProvider<List<Question>>,
    profileId: ProfileId
  ): DataProvider<Any?> {
    // Prepare to compute the session state by setting up the provided question list provider to be
    // used later in the ephemeral question data provider.
    monitoredQuestionListDataProvider.setBaseDataProvider(
      questionsListDataProvider, this::sendReceiveQuestionListEvent
    )
    this.profileId = profileId
    check(controllerCommandQueue.offer(ControllerMessage.StartInitializingController(profileId))) {
      "Failed to schedule command for initializing the question assessment progress controller."
    }
    return beginSessionResultDataProvider
  }

  internal fun finishQuestionTrainingSession(): DataProvider<Any?> {
    // Reset the base questions list provider so that the ephemeral question has no question list to
    // reference (since the session finished).
    monitoredQuestionListDataProvider.setBaseDataProvider(
      createEmptyQuestionsListDataProvider(), this::sendReceiveQuestionListEvent
    )
    sendCommandForOperation(finishSessionResultFlow, ControllerMessage.FinishSession) {
      "Failed to schedule command for finishing the question session."
    }
    return finishSessionResultDataProvider
  }

  /**
   * Submits an answer to the current question and returns how the UI should respond to this answer.
   * The returned [LiveData] will only have at most two results posted: a pending result, and then a
   * completed success/failure result. Failures in this case represent a failure of the app
   * (possibly due to networking conditions). The app should report this error in a consumable way
   * to the user so that they may take action on it. No additional values will be reported to the
   * [LiveData]. Each call to this method returns a new, distinct, [LiveData] object that must be
   * observed. Note also that the returned [LiveData] is not guaranteed to begin with a pending
   * state.
   *
   * If the app undergoes a configuration change, calling code should rely on the [LiveData] from
   * [getCurrentQuestion] to know whether a current answer is pending. That [LiveData] will have its
   * state changed to pending during answer submission and until answer resolution.
   *
   * Submitting an answer should result in the learner staying in the current question or moving to
   * a new question in the training session. Note that once a correct answer is processed, the
   * current state reported to [getCurrentQuestion] will change from a pending question to a
   * completed question since the learner completed that question card. The learner can then proceed
   * from the current completed question to the next pending question using [moveToNextQuestion].
   *
   * This method cannot be called until a training session has started and [getCurrentQuestion]
   * returns a non-pending result or the result will fail. Calling code must also take care not to
   * allow users to submit an answer while a previous answer is pending. That scenario will also
   * result in a failed answer submission.
   *
   * No assumptions should be made about the completion order of the returned [LiveData] vs. the
   * [LiveData] from [getCurrentQuestion]. Also note that the returned [LiveData] will only have a
   * single value and not be reused after that point.
   */
  fun submitAnswer(answer: UserAnswer): DataProvider<AnsweredQuestionOutcome> {
    check(controllerCommandQueue.offer(ControllerMessage.SubmitAnswer(answer))) {
      "Failed to schedule command for submitting an answer."
    }
    return submitAnswerResultDataProvider
  }

  /**
   * Notifies the controller that the user wishes to reveal a hint.
   *
   * @param hintIndex index of the hint that was revealed in the hint list of the current pending
   *     state
   * @return a one-time [LiveData] that indicates success/failure of the operation (the actual
   *     payload of the result isn't relevant)
   */
  fun submitHintIsRevealed(hintIndex: Int): DataProvider<Any?> {
    check(controllerCommandQueue.offer(ControllerMessage.HintIsRevealed(hintIndex))) {
      "Failed to schedule command for submitting a hint reveal"
    }
    return submitHintRevealedResultDataProvider
  }

  /**
   * Notifies the controller that the user has revealed the solution to the current state.
   *
   * @return a one-time [LiveData] that indicates success/failure of the operation (the actual
   *     payload of the result isn't relevant)
   */
  fun submitSolutionIsRevealed(): DataProvider<Any?> {
    check(controllerCommandQueue.offer(ControllerMessage.SolutionIsRevealed)) {
      "Failed to schedule command for submitting a solution reveal"
    }
    return submitSolutionRevealedResultDataProvider
  }

  /**
   * Navigates to the next question in the assessment. This method is only valid if the current
   * [EphemeralQuestion] reported by [getCurrentQuestion] is a completed question. Calling code is
   * responsible for ensuring this method is only called when it's possible to navigate forward.
   *
   * Note that if the current question is pending, the user needs to submit a correct answer via
   * [submitAnswer] before forward navigation can occur.
   *
   * @return a one-time [LiveData] indicating whether the movement to the next question was
   *     successful, or a failure if question navigation was attempted at an invalid time (such as
   *     if the current question is pending or terminal). It's recommended that calling code only
   *     listen to this result for failures, and instead rely on [getCurrentQuestion] for observing
   *     a successful transition to another question.
   */
  fun moveToNextQuestion(): DataProvider<Any?> {
    check(controllerCommandQueue.offer(ControllerMessage.MoveToNextQuestion)) {
      "Failed to schedule command for moving to the next question."
    }
    return moveToNextQuestionResultDataProvider
  }

  /**
   * Returns a [DataProvider] monitoring the current [EphemeralQuestion] the learner is currently
   * viewing. If this state corresponds to a a terminal state, then the learner has completed the
   * training session. Note that [moveToNextQuestion] will automatically update observers of this
   * data provider when the next question is navigated to.
   *
   * This [DataProvider] may switch from a completed to a pending result during transient operations
   * like submitting an answer via [submitAnswer]. Calling code should be made resilient to this by
   * caching the current question object to display since it may disappear temporarily during answer
   * submission. Calling code should persist this state object across configuration changes if
   * needed since it cannot rely on this [DataProvider] for immediate UI reconstitution after
   * configuration changes.
   *
   * The underlying question returned by this function can only be changed by calls to
   * [moveToNextQuestion], or the question training controller if another question session begins.
   * UI code can be confident only calls from the UI layer will trigger changes here to ensure
   * atomicity between receiving and making question state changes.
   *
   * This method is safe to be called before a training session has started. If there is no ongoing
   * session, it should return a pending state, which means the returned value can switch from a
   * success or failure state back to pending.
   */
  fun getCurrentQuestion(): DataProvider<EphemeralQuestion> {
    val writtenTranslationContentLocale =
      translationController.getWrittenTranslationContentLocale(profileId)
    // Combine ephemeral question with the monitored question list to ensure that changes to the
    // questions list trigger a recompute of the ephemeral question.
    val questionDataProvider =
      monitoredQuestionListDataProvider.combineWith(
        ephemeralQuestionDataProvider, EPHEMERAL_QUESTION_FROM_UPDATED_QUESTION_LIST_PROVIDER_ID
      ) { _, currentQuestion -> currentQuestion }
    return writtenTranslationContentLocale.combineWith(
      questionDataProvider, LOCALIZED_QUESTION_PROVIDER_ID
    ) { contentLocale, currentQuestion ->
      return@combineWith augmentEphemeralQuestion(contentLocale, currentQuestion)
    }
  }

  // TODO: update documentation to mention subsequent calls will only return based on last call.
  /**
   * Returns a [DataProvider] monitoring the [UserAssessmentPerformance] corresponding to the user's
   * computed overall performance this practice session.
   *
   * This method should only be called at the end of a practice session, after all the questions
   * have been completed.
   */
  fun calculateScores(skillIdList: List<String>): DataProvider<UserAssessmentPerformance> {
    check(controllerCommandQueue.offer(ControllerMessage.CalculateScores(skillIdList))) {
      "Failed to schedule command for moving to the next question."
    }
    return calculateScoresDataProvider
  }

  private fun createControllerCommandActor(): SendChannel<ControllerMessage> {
    val controllerState = ControllerState(QuestionAssessmentProgress())
    // Use an unlimited capacity buffer so that commands can be sent asynchronously without blocking
    // the main thread or scheduling an extra coroutine.
    return CoroutineScope(backgroundCoroutineDispatcher).actor(capacity = Channel.UNLIMITED) {
      for (message in channel) {
        @Suppress("UNUSED_VARIABLE") // A variable is used to create an exhaustive when statement.
        val unused = when (message) {
          is ControllerMessage.StartInitializingController ->
            controllerState.beginQuestionTrainingSessionInternal(message.profileId)
          is ControllerMessage.ReceiveQuestionList ->
            controllerState.handleUpdatedQuestionsList(message.questionsList)
          ControllerMessage.FinishSession -> controllerState.finishQuestionTrainingSessionInternal()
          is ControllerMessage.SubmitAnswer ->
            controllerState.submitAnswerInternal(message.userAnswer)
          is ControllerMessage.HintIsRevealed ->
            controllerState.submitHintIsRevealedInternal(message.hintIndex)
          ControllerMessage.MoveToNextQuestion -> controllerState.moveToNextQuestion()
          ControllerMessage.SolutionIsRevealed -> controllerState.submitSolutionIsRevealedInternal()
          is ControllerMessage.CalculateScores ->
            controllerState.recomputeUserAssessmentPerformanceAndNotify(message.skillIdList)
        }
      }
    }
  }

  private fun <T> sendCommandForOperation(
    resultFlow: MutableStateFlow<AsyncResult<T>>,
    message: ControllerMessage,
    lazyFailureMessage: () -> String
  ) {
    // Ensure that the result is first reset since there will be a delay before the message is
    // processed.
    resultFlow.value = AsyncResult.Pending()

    // This must succeed or the app will be entered into a bad state. Crash instead of trying to
    // recover (though recovery may be possible in the future with some changes and user messaging).
    check(controllerCommandQueue.offer(message), lazyFailureMessage)
  }

  private suspend fun sendReceiveQuestionListEvent(
    questionsList: List<Question>
  ): AsyncResult<Any?> {
    controllerCommandQueue.send(ControllerMessage.ReceiveQuestionList(questionsList))
    return AsyncResult.Success(null)
  }

  private suspend fun ControllerState.beginQuestionTrainingSessionInternal(profileId: ProfileId) {
    tryOperation(BEGIN_SESSION_RESULT_PROVIDER_ID, beginSessionResultFlow) {
      check(progress.trainStage == TrainStage.NOT_IN_TRAINING_SESSION) {
        "Cannot start a new training session until the previous one is completed."
      }
      progress.currentProfileId = profileId

      hintHandler = hintHandlerFactory.create()
      progress.advancePlayStageTo(TrainStage.LOADING_TRAINING_SESSION)

      // Reset the finish flow since the session is beginning.
      finishSessionResultFlow.value = AsyncResult.Pending()
    }
  }

  private suspend fun ControllerState.handleUpdatedQuestionsList(questionsList: List<Question>) {
    // The questions list is possibly changed which may affect the computed ephemeral question.
    if (this.questionsList != questionsList) {
      this.questionsList = questionsList
      // Only notify if the questions list is different (otherwise an infinite notify loop might be
      // started).
      recomputeCurrentQuestionAndNotify()
    }
  }

  private suspend fun ControllerState.finishQuestionTrainingSessionInternal() {
    tryOperation(FINISH_SESSION_RESULT_PROVIDER_ID, finishSessionResultFlow) {
      check(progress.trainStage != TrainStage.NOT_IN_TRAINING_SESSION) {
        "Cannot stop a new training session which wasn't started."
      }
      progress.advancePlayStageTo(TrainStage.NOT_IN_TRAINING_SESSION)
    }

    // Ensure all state is reset since a session is no longer being played.
    ephemeralQuestionFlow.value = AsyncResult.Pending()
    calculateScoresFlow.value = AsyncResult.Pending()
    beginSessionResultFlow.value = AsyncResult.Pending()
    submitAnswerResultFlow.value = AsyncResult.Pending()
    submitHintRevealedResultFlow.value = AsyncResult.Pending()
    submitSolutionRevealedResultFlow.value = AsyncResult.Pending()
    moveToNextQuestionResultFlow.value = AsyncResult.Pending()
  }

  private suspend fun ControllerState.submitAnswerInternal(answer: UserAnswer) {
    tryOperation(SUBMIT_ANSWER_RESULT_PROVIDER_ID, submitAnswerResultFlow) {
      check(progress.trainStage != TrainStage.NOT_IN_TRAINING_SESSION) {
        "Cannot submit an answer if a training session has not yet begun."
      }
      check(progress.trainStage != TrainStage.LOADING_TRAINING_SESSION) {
        "Cannot submit an answer while the training session is being loaded."
      }
      check(progress.trainStage != TrainStage.SUBMITTING_ANSWER) {
        "Cannot submit an answer while another answer is pending."
      }

      // Notify observers that the submitted answer is currently pending.
      progress.advancePlayStageTo(TrainStage.SUBMITTING_ANSWER)

      val answeredQuestionOutcome: AnsweredQuestionOutcome
      try {
        val topPendingState = progress.stateDeck.getPendingTopState()
        val classificationResult =
          answerClassificationController.classify(
            topPendingState.interaction, answer.answer, answer.writtenTranslationContext
          )
        answeredQuestionOutcome =
          progress.stateList.computeAnswerOutcomeForResult(classificationResult.outcome)
        progress.stateDeck.submitAnswer(
          answer, answeredQuestionOutcome.feedback, answeredQuestionOutcome.isCorrectAnswer
        )

        // Track the number of answers the user submitted, including any misconceptions
        val misconception = if (classificationResult is OutcomeWithMisconception) {
          classificationResult.taggedSkillId
        } else null
        progress.trackAnswerSubmitted(misconception)

        // Do not proceed unless the user submitted the correct answer.
        if (answeredQuestionOutcome.isCorrectAnswer) {
          progress.completeCurrentQuestion()
          val newState = if (!progress.isAssessmentCompleted()) {
            // Only push the next state if the assessment isn't completed.
            progress.getNextState()
          } else {
            // Otherwise, push a synthetic state for the end of the session.
            State.getDefaultInstance()
          }
          progress.stateDeck.pushState(newState, prohibitSameStateName = false)
          hintHandler.finishState(newState)
        } else {
          // Schedule a new hints or solution or show a new hint or solution immediately based on
          // the current ephemeral state of the training session because a new wrong answer was
          // submitted.
          hintHandler.handleWrongAnswerSubmission(
            computeBaseCurrentEphemeralState().pendingState.wrongAnswerCount
          )
        }
      } finally {
        // Ensure that the user always returns to the VIEWING_STATE stage to avoid getting stuck
        // in an 'always submitting answer' situation. This can specifically happen if answer
        // classification throws an exception.
        progress.advancePlayStageTo(TrainStage.VIEWING_STATE)
      }

      return@tryOperation answeredQuestionOutcome
    }
  }

  private suspend fun ControllerState.submitHintIsRevealedInternal(hintIndex: Int) {
    tryOperation(SUBMIT_HINT_REVEALED_RESULT_PROVIDER_ID, submitHintRevealedResultFlow) {
      check(progress.trainStage != TrainStage.NOT_IN_TRAINING_SESSION) {
        "Cannot submit an answer if a training session has not yet begun."
      }
      check(progress.trainStage != TrainStage.LOADING_TRAINING_SESSION) {
        "Cannot submit an answer while the training session is being loaded."
      }
      check(progress.trainStage != TrainStage.SUBMITTING_ANSWER) {
        "Cannot submit an answer while another answer is pending."
      }
      try {
        progress.trackHintViewed()
        hintHandler.viewHint(hintIndex)
      } finally {
        // Ensure that the user always returns to the VIEWING_STATE stage to avoid getting stuck
        // in an 'always showing hint' situation. This can specifically happen if hint throws an
        // exception.
        progress.advancePlayStageTo(TrainStage.VIEWING_STATE)
      }
    }
  }

  private suspend fun ControllerState.submitSolutionIsRevealedInternal() {
    tryOperation(SUBMIT_SOLUTION_REVEALED_RESULT_PROVIDER_ID, submitSolutionRevealedResultFlow) {
      check(progress.trainStage != TrainStage.NOT_IN_TRAINING_SESSION) {
        "Cannot submit an answer if a training session has not yet begun."
      }
      check(progress.trainStage != TrainStage.LOADING_TRAINING_SESSION) {
        "Cannot submit an answer while the training session is being loaded."
      }
      check(progress.trainStage != TrainStage.SUBMITTING_ANSWER) {
        "Cannot submit an answer while another answer is pending."
      }
      try {
        progress.trackSolutionViewed()
        hintHandler.viewSolution()
      } finally {
        // Ensure that the user always returns to the VIEWING_STATE stage to avoid getting stuck
        // in an 'always showing solution' situation. This can specifically happen if solution
        // throws an exception.
        progress.advancePlayStageTo(TrainStage.VIEWING_STATE)
      }
    }
  }

  private suspend fun ControllerState.moveToNextQuestion() {
    tryOperation(MOVE_TO_NEXT_QUESTION_RESULT_PROVIDER_ID, moveToNextQuestionResultFlow) {
      check(progress.trainStage != TrainStage.NOT_IN_TRAINING_SESSION) {
        "Cannot navigate to a next question if a training session has not begun."
      }
      check(progress.trainStage != TrainStage.SUBMITTING_ANSWER) {
        "Cannot navigate to a next question if an answer submission is pending."
      }
      progress.stateDeck.navigateToNextState()
      // Track whether the learner has moved to a new card.
      if (progress.isViewingMostRecentQuestion()) {
        // Update the hint state and maybe schedule new help when user moves to the pending top
        // state.
        hintHandler.navigateBackToLatestPendingState()
        progress.processNavigationToNewQuestion()
      }
    }
  }

  private fun ControllerState.computeBaseCurrentEphemeralState(): EphemeralState =
    progress.stateDeck.getCurrentEphemeralState(hintHandler.getCurrentHelpIndex().value)

  private fun createCurrentQuestionDataProvider(
    questionsListDataProvider: DataProvider<List<Question>>
  ): NestedTransformedDataProvider<Any?> {
    return questionsListDataProvider.transformNested(
      MONITORED_QUESTION_LIST_PROVIDER_ID, this::sendReceiveQuestionListEvent
    )
  }

  private suspend fun <T> ControllerState.tryOperation(
    providerId: String,
    resultFlow: MutableStateFlow<AsyncResult<T>>,
    operation: suspend ControllerState.() -> T
  ) {
    try {
      resultFlow.value = AsyncResult.Success(operation())
      recomputeCurrentQuestionAndNotify()
    } catch (e: Exception) {
      exceptionsController.logNonFatalException(e)
      resultFlow.value = AsyncResult.Failure(e)
    }
    asyncDataSubscriptionManager.notifyChange(providerId)
  }

  private suspend fun ControllerState.recomputeCurrentQuestionAndNotify() {
    // Note the explicit notification here is chosen instead of emit() because emit() won't notify
    // observers if the value hasn't changed and it may be the case that previous question hasn't
    // yet been notified even though the flow looks different from the perspective of StateFlow.
    // This also ensures the base questions list is loaded and the session is fully initialized.
    ephemeralQuestionFlow.value = if (isQuestionsListInitialized) {
      // Only compute the ephemeral question if there's a questions list loaded (otherwise the
      // controller is in a pending state).
      retrieveCurrentQuestionAsync(questionsList)
    } else AsyncResult.Pending()
    asyncDataSubscriptionManager.notifyChange(CURRENT_QUESTION_PROVIDER_ID)
  }

  private suspend fun ControllerState.recomputeUserAssessmentPerformanceAndNotify(
    skillIdList: List<String>
  ) {
    // See the message in recomputeCurrentQuestionAndNotify for detailks on the notification
    // strategy used here.
    val scoreCalculator =
      scoreCalculatorFactory.create(skillIdList, progress.questionSessionMetrics)
    calculateScoresFlow.value = AsyncResult.Success(scoreCalculator.computeAll())
    asyncDataSubscriptionManager.notifyChange(CALCULATE_SCORES_PROVIDER_ID)
  }

  private suspend fun ControllerState.retrieveCurrentQuestionAsync(
    questionsList: List<Question>
  ): AsyncResult<EphemeralQuestion> {
    return try {
      when (progress.trainStage) {
        TrainStage.NOT_IN_TRAINING_SESSION -> AsyncResult.Pending()
        TrainStage.LOADING_TRAINING_SESSION -> {
          // If the assessment hasn't yet been initialized, initialize it
          // now that a list of questions is available.
          initializeAssessment(questionsList)
          progress.advancePlayStageTo(TrainStage.VIEWING_STATE)
          AsyncResult.Success(retrieveEphemeralQuestionState(questionsList)
          )
        }
        TrainStage.VIEWING_STATE -> AsyncResult.Success(retrieveEphemeralQuestionState(questionsList)
        )
        TrainStage.SUBMITTING_ANSWER -> AsyncResult.Pending()
      }
    } catch (e: Exception) {
      exceptionsController.logNonFatalException(e)
      AsyncResult.Failure(e)
    }
  }

  /**
   * Augments the specified [EphemeralQuestion] [AsyncResult] by attaching the necessary context to
   * translate the question.
   */
  private fun augmentEphemeralQuestion(
    writtenTranslationContentLocale: OppiaLocale.ContentLocale,
    ephemeralQuestion: EphemeralQuestion
  ): EphemeralQuestion {
    return ephemeralQuestion.toBuilder().apply {
      ephemeralState = ephemeralState.toBuilder().apply {
        writtenTranslationContext =
          translationController.computeWrittenTranslationContext(
            state.writtenTranslationsMap, writtenTranslationContentLocale
          )
      }.build()
    }.build()
  }

  private fun ControllerState.retrieveEphemeralQuestionState(
    questionsList: List<Question>
  ): EphemeralQuestion {
    val currentQuestionIndex = progress.getCurrentQuestionIndex()
    val ephemeralQuestionBuilder = EphemeralQuestion.newBuilder()
      .setEphemeralState(computeBaseCurrentEphemeralState())
      .setCurrentQuestionIndex(currentQuestionIndex)
      .setTotalQuestionCount(progress.getTotalQuestionCount())
      .setInitialTotalQuestionCount(progress.getTotalQuestionCount())
    if (currentQuestionIndex < questionsList.size) {
      ephemeralQuestionBuilder.question = questionsList[currentQuestionIndex]
    }
    return ephemeralQuestionBuilder.build()
  }

  private suspend fun ControllerState.initializeAssessment(questionsList: List<Question>) {
    check(questionsList.isNotEmpty()) { "Cannot start a training session with zero questions." }
    progress.initialize(questionsList)
    // Update hint state to schedule task to show new help.
    hintHandler.startWatchingForHintsInNewState(progress.stateDeck.getCurrentState())
  }

  /** Returns a [DataProvider] that always provides an empty list of [Question]s. */
  private fun createEmptyQuestionsListDataProvider(): DataProvider<List<Question>> {
    return dataProviders.createInMemoryDataProvider(EMPTY_QUESTIONS_LIST_DATA_PROVIDER_ID) {
      listOf()
    }
  }

  private fun <T> createAsyncResultStateFlow(): MutableStateFlow<AsyncResult<T>> =
    MutableStateFlow(AsyncResult.Pending())

  private fun <T> StateFlow<AsyncResult<T>>.convertToDataProvider(id: Any): DataProvider<T> =
    dataProviders.run { convertAsyncToSimpleDataProvider(id) }

  private class ControllerState(
    val progress: QuestionAssessmentProgress = QuestionAssessmentProgress()
  ) {
    lateinit var hintHandler: HintHandler
    lateinit var questionsList: List<Question>
    val isQuestionsListInitialized: Boolean
      get() = ::questionsList.isInitialized
  }

  private sealed class ControllerMessage {
    data class StartInitializingController(val profileId: ProfileId) : ControllerMessage()

    data class ReceiveQuestionList(val questionsList: List<Question>) : ControllerMessage()

    object FinishSession : ControllerMessage()

    data class SubmitAnswer(val userAnswer: UserAnswer) : ControllerMessage()

    data class HintIsRevealed(val hintIndex: Int) : ControllerMessage()

    object SolutionIsRevealed : ControllerMessage()

    object MoveToNextQuestion : ControllerMessage()

    data class CalculateScores(val skillIdList: List<String>): ControllerMessage()
  }
}
