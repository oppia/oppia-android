package org.oppia.android.domain.question

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
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
import org.oppia.android.domain.oppialogger.OppiaLogger
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
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

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
 * A default session ID to be used before a session has been initialized.
 *
 * This session ID will never match, so messages that are received with this ID will never be
 * processed.
 */
private const val DEFAULT_SESSION_ID = "default_session_id"

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
  private val oppiaLogger: OppiaLogger,
  @BackgroundDispatcher private val backgroundCoroutineDispatcher: CoroutineDispatcher
) {
  // TODO(#247): Add support for populating the list of skill IDs to review at the end of the
  //  training session.
  // TODO(#248): Add support for the assessment ending prematurely due to learner demonstrating
  //  sufficient proficiency.

  // TODO(#606): Replace this with a profile scope to avoid this hacky workaround (which is needed
  //  for getCurrentQuestion).
  private lateinit var profileId: ProfileId

  private var mostRecentSessionId: String? = null
  private val activeSessionId: String
    get() = mostRecentSessionId ?: DEFAULT_SESSION_ID

  private val controllerCommandQueue by lazy { createControllerCommandActor() }
  private val ephemeralQuestionFlow by lazy { createAsyncResultStateFlow<EphemeralQuestion>() }
  private val calculateScoresFlow by lazy {
    createAsyncResultStateFlow<UserAssessmentPerformance>()
  }
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

  /**
   * Begins a training session based on the specified question list data provider and [ProfileId],
   * and returns a [DataProvider] indicating whether the session was successfully started.
   *
   * The returned [DataProvider] has the same lifecycle considerations as the provider returned by
   * [submitAnswer].
   */
  internal fun beginQuestionTrainingSession(
    questionsListDataProvider: DataProvider<List<Question>>,
    profileId: ProfileId
  ): DataProvider<Any?> {
    // Prepare to compute the session state by setting up the provided question list provider to be
    // used later in the ephemeral question data provider.
    val sessionId = UUID.randomUUID().toString().also { mostRecentSessionId = it }
    monitoredQuestionListDataProvider.setBaseDataProvider(
      questionsListDataProvider, this::sendReceiveQuestionListEvent
    )
    this.profileId = profileId
    val initializeMessage = ControllerMessage.StartInitializingController(profileId, sessionId)
    check(controllerCommandQueue.offer(initializeMessage)) {
      "Failed to schedule command for initializing the question assessment progress controller."
    }
    return beginSessionResultDataProvider
  }

  /**
   * Ends the current training session and returns a [DataProvider] that indicates whether it was
   * successfully ended.
   *
   * The returned [DataProvider] has the same lifecycle considerations as the provider returned by
   * [submitAnswer] with one additional caveat: this method does not actually need to be called when
   * a session is over. Calling it ensures all other [DataProvider]s reset to a correct
   * out-of-session state, but subsequent calls to [beginQuestionTrainingSession] will reset the
   * session.
   */
  internal fun finishQuestionTrainingSession(): DataProvider<Any?> {
    // Reset the base questions list provider so that the ephemeral question has no question list to
    // reference (since the session finished).
    monitoredQuestionListDataProvider.setBaseDataProvider(
      createEmptyQuestionsListDataProvider(), this::sendReceiveQuestionListEvent
    )
    sendCommandForOperation(
      finishSessionResultFlow, ControllerMessage.FinishSession(activeSessionId)
    ) { "Failed to schedule command for finishing the question session." }
    return finishSessionResultDataProvider
  }

  /**
   * Submits an answer to the current state and returns how the UI should respond to this answer.
   *
   * If the app undergoes a configuration change, calling code should rely on the [DataProvider]
   * from [getCurrentQuestion] to know whether a current answer is pending. That [DataProvider] will
   * have its state changed to pending during answer submission and until answer resolution.
   *
   * Submitting an answer should result in the learner staying in the current question or moving to
   * a new question in the training session. Note that once a correct answer is processed, the
   * current state reported to [getCurrentQuestion] will change from a pending question to a
   * completed question since the learner completed that question card. The learner can then proceed
   * from the current completed question to the next pending question using [moveToNextQuestion].
   *
   * ### Lifecycle behavior
   * The returned [DataProvider] will initially be pending until the operation completes. Note that
   * the same provider is returned for each call, so it can be monitored long-term for subsequent
   * answer submissions (where new submissions and session restarts will change the provider to
   * pending). Furthermore, the returned provider does not actually need to be monitored in order
   * for the operation to complete, though it's recommended since [getCurrentQuestion] can only be
   * used to monitor the effects of the operation, not whether the operation itself succeeded.
   *
   * If this is called before a session begins it will return a provider that stays pending with no
   * updates. The operation will also silently fail rather than queue up in these circumstances, so
   * starting a session will not trigger an answer submission from an older call.
   *
   * Multiple subsequent calls during a valid session will queue up and have results delivered in
   * order (though based on the eventual consistency nature of [DataProvider]s no assumptions can be
   * made about whether all results will actually be received--[getCurrentQuestion] should be used
   * as the source of truth for the current state of the session).
   *
   * No assumptions should be made about the completion order of the returned [DataProvider] vs. the
   * [DataProvider] from [getCurrentQuestion].
   */
  fun submitAnswer(answer: UserAnswer): DataProvider<AnsweredQuestionOutcome> {
    check(controllerCommandQueue.offer(ControllerMessage.SubmitAnswer(answer, activeSessionId))) {
      "Failed to schedule command for submitting an answer."
    }
    return submitAnswerResultDataProvider
  }

  /**
   * Notifies the controller that the user wishes to reveal a hint.
   *
   * The returned [DataProvider] has the same lifecycle considerations as the provider returned by
   * [submitAnswer].
   *
   * @param hintIndex index of the hint that was revealed in the hint list of the current pending
   *     state
   * @return a [DataProvider] that indicates success/failure of the operation (the actual payload of
   *     the result isn't relevant)
   */
  fun submitHintIsRevealed(hintIndex: Int): DataProvider<Any?> {
    check(
      controllerCommandQueue.offer(
        ControllerMessage.HintIsRevealed(hintIndex, activeSessionId)
      )
    ) { "Failed to schedule command for submitting a hint reveal" }
    return submitHintRevealedResultDataProvider
  }

  /**
   * Notifies the controller that the user has revealed the solution to the current state.
   *
   * The returned [DataProvider] has the same lifecycle considerations as the provider returned by
   * [submitAnswer].
   *
   * @return a [DataProvider] that indicates success/failure of the operation (the actual payload of
   *     the result isn't relevant)
   */
  fun submitSolutionIsRevealed(): DataProvider<Any?> {
    check(controllerCommandQueue.offer(ControllerMessage.SolutionIsRevealed(activeSessionId))) {
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
   * The returned [DataProvider] has the same lifecycle considerations as the provider returned by
   * [submitAnswer].
   *
   * @return a [DataProvider] indicating whether the movement to the next question was successful,
   *     or a failure if question navigation was attempted at an invalid time (such as if the
   *     current question is pending or terminal). It's recommended that calling code only listen to
   *     this result for failures, and instead rely on [getCurrentQuestion] for observing a
   *     successful transition to another question.
   */
  fun moveToNextQuestion(): DataProvider<Any?> {
    check(controllerCommandQueue.offer(ControllerMessage.MoveToNextQuestion(activeSessionId))) {
      "Failed to schedule command for moving to the next question."
    }
    return moveToNextQuestionResultDataProvider
  }

  /**
   * Returns a [DataProvider] monitoring the current [EphemeralQuestion] the learner is currently
   * viewing.
   *
   * If this state corresponds to a terminal state, then the learner has completed the training
   * session. Note that [moveToNextQuestion] will automatically update observers of this data
   * provider when the next question is navigated to.
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
   * UI code cannot assume that only calls from the UI layer will trigger state changes here since
   * internal domain processes may also affect state (such as hint timers).
   *
   * This method is safe to be called before a training session has started. If there is no ongoing
   * session, it will return a pending state, which means the returned value can switch from a
   * success or failure state back to pending.
   *
   * This method does not actually need to be called for the [EphemeralQuestion] to be computed;
   * it's always computed eagerly by other state-changing methods regardless of whether there's an
   * active subscription to this method's returned [DataProvider].
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

  /**
   * Returns a [DataProvider] monitoring the [UserAssessmentPerformance] corresponding to the user's
   * computed overall performance this practice session.
   *
   * This method should only be called at the end of a practice session, after all the questions
   * have been completed.
   *
   * The returned [DataProvider] has the same lifecycle considerations as the provider returned by
   * [submitAnswer], which in practice means that subsequent calls to this function may result in
   * multiple [UserAssessmentPerformance]s being computed and sent to the returned [DataProvider],
   * though per eventual consistency it's expected the final result received will always be
   * corresponding to the most recent call to this method.
   */
  fun calculateScores(skillIdList: List<String>): DataProvider<UserAssessmentPerformance> {
    check(
      controllerCommandQueue.offer(ControllerMessage.CalculateScores(skillIdList, activeSessionId))
    ) { "Failed to schedule command for moving to the next question." }
    return calculateScoresDataProvider
  }

  private fun createControllerCommandActor(): SendChannel<ControllerMessage> {
    var controllerState: ControllerState? = null
    // Use an unlimited capacity buffer so that commands can be sent asynchronously without blocking
    // the main thread or scheduling an extra coroutine.
    return CoroutineScope(backgroundCoroutineDispatcher).actor(capacity = Channel.UNLIMITED) {
      for (message in channel) {
        try {
          // Since the loop essentially never ends, this is needed to ensure that the controller
          // state can be reset across sessions.
          val initedControllerState by lazy {
            checkNotNull(controllerState) { "Expected controller state to be initialized." }
          }

          // If there's an active controller, ignore messages not tied to this session since
          // leftovers from previous sessions may conflate the state of the active session.
          val currentSessionId = controllerState?.sessionId
          if (currentSessionId != null && message.sessionId != currentSessionId) continue

          @Suppress("UNUSED_VARIABLE") // A variable is used to create an exhaustive when statement.
          val unused = when (message) {
            is ControllerMessage.StartInitializingController -> {
              // Ensure the state is completely recreated for each session to avoid leaking state
              // across sessions.
              controllerState =
                ControllerState(QuestionAssessmentProgress(), message.sessionId).also {
                  it.beginQuestionTrainingSessionImpl(message.profileId)
                }
            }
            is ControllerMessage.ReceiveQuestionList ->
              initedControllerState.handleUpdatedQuestionsList(message.questionsList)
            is ControllerMessage.FinishSession -> {
              try {
                // Ensure finish is always executed even if the controller state isn't yet
                // initialized.
                controllerState.finishQuestionTrainingSessionImpl()
              } finally {
                // Ensure the controller state is always reset.
                controllerState = null
              }
            }
            is ControllerMessage.SubmitAnswer ->
              initedControllerState.submitAnswerImpl(message.userAnswer)
            is ControllerMessage.HintIsRevealed ->
              initedControllerState.submitHintIsRevealedImpl(message.hintIndex)
            is ControllerMessage.MoveToNextQuestion -> initedControllerState.moveToNextQuestion()
            is ControllerMessage.SolutionIsRevealed ->
              initedControllerState.submitSolutionIsRevealedImpl()
            is ControllerMessage.CalculateScores ->
              initedControllerState.recomputeUserAssessmentPerformanceAndNotify(message.skillIdList)
            is ControllerMessage.RecomputeQuestionAndNotify ->
              initedControllerState.recomputeCurrentQuestionAndNotifyImpl()
          }
        } catch (e: Exception) {
          exceptionsController.logNonFatalException(e)
          oppiaLogger.w(
            "QuestionAssessmentProgressController",
            "Encountered exception while processing command: $message.",
            e
          )
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
    controllerCommandQueue.send(
      ControllerMessage.ReceiveQuestionList(questionsList, activeSessionId)
    )
    return AsyncResult.Success(null)
  }

  private suspend fun ControllerState.beginQuestionTrainingSessionImpl(profileId: ProfileId) {
    tryOperation(BEGIN_SESSION_RESULT_PROVIDER_ID, beginSessionResultFlow) {
      progress.currentProfileId = profileId

      hintHandler = hintHandlerFactory.create()
      hintHandler.getCurrentHelpIndex().onEach {
        recomputeCurrentQuestionAndNotifyAsync()
      }.launchIn(CoroutineScope(backgroundCoroutineDispatcher))
      progress.advancePlayStageTo(TrainStage.LOADING_TRAINING_SESSION)

      // Reset the finish flow since the session is beginning.
      finishSessionResultFlow.value = AsyncResult.Pending()
    }
  }

  private suspend fun ControllerState.handleUpdatedQuestionsList(questionsList: List<Question>) {
    // The questions list is possibly changed which may affect the computed ephemeral question.
    if (!this.isQuestionsListInitialized || this.questionsList != questionsList) {
      this.questionsList = questionsList
      // Only notify if the questions list is different (otherwise an infinite notify loop might be
      // started).
      recomputeCurrentQuestionAndNotifySync()
    }
  }

  private suspend fun ControllerState?.finishQuestionTrainingSessionImpl() {
    checkNotNull(this) { "Cannot stop a new training session which wasn't started." }
    tryOperation(FINISH_SESSION_RESULT_PROVIDER_ID, finishSessionResultFlow) {
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

  private suspend fun ControllerState.submitAnswerImpl(answer: UserAnswer) {
    tryOperation(SUBMIT_ANSWER_RESULT_PROVIDER_ID, submitAnswerResultFlow) {
      check(progress.trainStage != TrainStage.SUBMITTING_ANSWER) {
        "Cannot submit an answer while another answer is pending."
      }

      // Notify observers that the submitted answer is currently pending.
      progress.advancePlayStageTo(TrainStage.SUBMITTING_ANSWER)
      recomputeCurrentQuestionAndNotifySync()

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

  private suspend fun ControllerState.submitHintIsRevealedImpl(hintIndex: Int) {
    tryOperation(SUBMIT_HINT_REVEALED_RESULT_PROVIDER_ID, submitHintRevealedResultFlow) {
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

  private suspend fun ControllerState.submitSolutionIsRevealedImpl() {
    tryOperation(SUBMIT_SOLUTION_REVEALED_RESULT_PROVIDER_ID, submitSolutionRevealedResultFlow) {
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
      recomputeCurrentQuestionAndNotifySync()
    } catch (e: Exception) {
      exceptionsController.logNonFatalException(e)
      resultFlow.value = AsyncResult.Failure(e)
    }
    asyncDataSubscriptionManager.notifyChange(providerId)
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
    controllerCommandQueue.send(ControllerMessage.RecomputeQuestionAndNotify(sessionId))
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

  private suspend fun ControllerState.recomputeUserAssessmentPerformanceAndNotify(
    skillIdList: List<String>
  ) {
    val calculator = scoreCalculatorFactory.create(skillIdList, progress.questionSessionMetrics)
    calculateScoresFlow.emit(AsyncResult.Success(calculator.computeAll()))
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
          AsyncResult.Success(
            retrieveEphemeralQuestionState(questionsList)
          )
        }
        TrainStage.VIEWING_STATE ->
          AsyncResult.Success(
            retrieveEphemeralQuestionState(questionsList)
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

  /**
   * Represents the current synchronized state of the controller.
   *
   * This object's instance is tied directly to a single training session, and it's not thread-safe
   * so all access must be synchronized.
   *
   * @property progress the [QuestionAssessmentProgress] corresponding to the session
   * @property sessionId the GUID corresponding to the session
   */
  private class ControllerState(
    val progress: QuestionAssessmentProgress = QuestionAssessmentProgress(),
    val sessionId: String
  ) {
    /**
     * The [HintHandler] used to monitor and trigger hints in the training session corresponding to
     * this controller state.
     */
    lateinit var hintHandler: HintHandler

    /**
     * The list of [Question]s currently being played in the training session.
     *
     * Because this is updated based on [ControllerMessage.ReceiveQuestionList], it may not be
     * initialized at the beginning of a training session. Callers should check
     * [isQuestionsListInitialized] prior to accessing this field.
     */
    lateinit var questionsList: List<Question>

    /** Indicates whether [questionsList] is initialized with values. */
    val isQuestionsListInitialized: Boolean
      get() = ::questionsList.isInitialized
  }

  /**
   * Represents a message that can be sent to [controllerCommandQueue] to process changes to
   * [ControllerState] (since all changes must be synchronized).
   *
   * Messages are expected to be resolved serially (though their scheduling can occur across
   * multiple threads, so order cannot be guaranteed until they're enqueued).
   */
  private sealed class ControllerMessage {
    /**
     * The session ID corresponding to this message (the message is expected to be ignored if it
     * doesn't correspond to an active session).
     */
    abstract val sessionId: String

    /** [ControllerMessage] for initializing a new training session. */
    data class StartInitializingController(
      val profileId: ProfileId,
      override val sessionId: String
    ) : ControllerMessage()

    /**
     * [ControllerMessage] for finishing the initialization of the training session by providing a
     * list of [Question]s to play.
     */
    data class ReceiveQuestionList(
      val questionsList: List<Question>,
      override val sessionId: String
    ) : ControllerMessage()

    /** [ControllerMessage] for ending the current training session. */
    data class FinishSession(override val sessionId: String) : ControllerMessage()

    /** [ControllerMessage] for submitting a new [UserAnswer]. */
    data class SubmitAnswer(
      val userAnswer: UserAnswer,
      override val sessionId: String
    ) : ControllerMessage()

    /**
     * [ControllerMessage] for indicating that the user revealed the hint corresponding to
     * [hintIndex].
     */
    data class HintIsRevealed(
      val hintIndex: Int,
      override val sessionId: String
    ) : ControllerMessage()

    /**
     * [ControllerMessage] for indicating that the user revealed the solution for the current
     * question.
     */
    data class SolutionIsRevealed(override val sessionId: String) : ControllerMessage()

    /** [ControllerMessage] to move to the next question in the training session. */
    data class MoveToNextQuestion(override val sessionId: String) : ControllerMessage()

    /**
     * [ControllerMessage] to calculate the current scores of the training session by computing a
     * new [UserAssessmentPerformance].
     */
    data class CalculateScores(
      val skillIdList: List<String>,
      override val sessionId: String
    ) : ControllerMessage()

    /**
     * [ControllerMessage] which recomputes the current [EphemeralQuestion] and notifies subscribers
     * of the [DataProvider] returned by [getCurrentQuestion] of the change.
     *
     * This is only used in cases where an external operation trigger changes that are only
     * reflected when recomputing the question (e.g. a new hint needing to be shown).
     */
    data class RecomputeQuestionAndNotify(override val sessionId: String) : ControllerMessage()
  }
}
