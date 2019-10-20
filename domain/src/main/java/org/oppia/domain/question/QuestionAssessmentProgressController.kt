package org.oppia.domain.question

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.oppia.app.model.AnsweredQuestionOutcome
import org.oppia.app.model.EphemeralQuestion
import org.oppia.app.model.InteractionObject
import org.oppia.app.model.Question
import org.oppia.domain.classify.AnswerClassificationController
import org.oppia.domain.question.QuestionAssessmentProgress.TrainStage
import org.oppia.util.data.AsyncDataSubscriptionManager
import org.oppia.util.data.AsyncResult
import org.oppia.util.data.DataProvider
import org.oppia.util.data.DataProviders
import java.util.concurrent.locks.ReentrantLock
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.concurrent.withLock

private const val CURRENT_QUESTION_DATA_PROVIDER_ID = "CurrentQuestionDataProvider"
private const val EMPTY_QUESTIONS_LIST_DATA_PROVIDER_ID = "EmptyQuestionsListDataProvider"

/**
 * Controller that tracks and reports the learner's ephemeral/non-persisted progress through a practice training
 * session. Note that this controller only supports one active training session at a time.
 *
 * The current training session is started via the question training controller.
 *
 * This class is thread-safe, but the order of applied operations is arbitrary. Calling code should take care to ensure
 * that uses of this class do not specifically depend on ordering.
 */
@Singleton
class QuestionAssessmentProgressController @Inject constructor(
  private val dataProviders: DataProviders,
  private val asyncDataSubscriptionManager: AsyncDataSubscriptionManager,
  private val answerClassificationController: AnswerClassificationController
) {
  // TODO(#247): Add support for populating the list of skill IDs to review at the end of the training session.
  // TODO(#248): Add support for the assessment ending prematurely due to learner demonstrating sufficient proficiency.

  private val progress = QuestionAssessmentProgress()
  private val progressLock = ReentrantLock()
  private var inProgressQuestionsListDataProvider: DataProvider<List<Question>> = createEmptyQuestionsListDataProvider()
  private val currentQuestionDataSource: DataProvider<EphemeralQuestion> by lazy {
    dataProviders.transformAsync(
      CURRENT_QUESTION_DATA_PROVIDER_ID, inProgressQuestionsListDataProvider, this::retrieveCurrentQuestionStateAsync
    )
  }

  internal fun beginQuestionTrainingSession(questionsListDataProvider: DataProvider<List<Question>>) {
    progressLock.withLock {
      check(progress.trainStage == TrainStage.NOT_IN_TRAINING_SESSION) {
        "Cannot start a new training session until the previous one is completed."
      }

      progress.advancePlayStageTo(TrainStage.LOADING_TRAINING_SESSION)
      inProgressQuestionsListDataProvider = questionsListDataProvider
      asyncDataSubscriptionManager.notifyChangeAsync(CURRENT_QUESTION_DATA_PROVIDER_ID)
    }
  }

  internal fun finishQuestionTrainingSession() {
    progressLock.withLock {
      check(progress.trainStage != TrainStage.NOT_IN_TRAINING_SESSION) {
        "Cannot stop a new training session which wasn't started."
      }
      progress.advancePlayStageTo(TrainStage.NOT_IN_TRAINING_SESSION)
      inProgressQuestionsListDataProvider = createEmptyQuestionsListDataProvider()
    }
  }

  /**
   * Submits an answer to the current question and returns how the UI should respond to this answer. The returned
   * [LiveData] will only have at most two results posted: a pending result, and then a completed success/failure
   * result. Failures in this case represent a failure of the app (possibly due to networking conditions). The app
   * should report this error in a consumable way to the user so that they may take action on it. No additional values
   * will be reported to the [LiveData]. Each call to this method returns a new, distinct, [LiveData] object that must
   * be observed. Note also that the returned [LiveData] is not guaranteed to begin with a pending state.
   *
   * If the app undergoes a configuration change, calling code should rely on the [LiveData] from [getCurrentQuestion]
   * to know whether a current answer is pending. That [LiveData] will have its state changed to pending during answer
   * submission and until answer resolution.
   *
   * Submitting an answer should result in the learner staying in the current question or moving to a new question in
   * the training session. Note that once a correct answer is processed, the current state reported to
   * [getCurrentQuestion] will change from a pending question to a completed question since the learner completed that
   * question card. The learner can then proceed from the current completed question to the next pending question using
   * [moveToNextQuestion].
   *
   * This method cannot be called until a training session has started and [getCurrentQuestion] returns a non-pending
   * result or the result will fail. Calling code must also take care not to allow users to submit an answer while a
   * previous answer is pending. That scenario will also result in a failed answer submission.
   *
   * No assumptions should be made about the completion order of the returned [LiveData] vs. the [LiveData] from
   * [getCurrentQuestion]. Also note that the returned [LiveData] will only have a single value and not be reused after
   * that point.
   */
  fun submitAnswer(answer: InteractionObject): LiveData<AsyncResult<AnsweredQuestionOutcome>> {
    try {
      progressLock.withLock {
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
        asyncDataSubscriptionManager.notifyChangeAsync(CURRENT_QUESTION_DATA_PROVIDER_ID)

        lateinit var answeredQuestionOutcome: AnsweredQuestionOutcome
        try {
          val topPendingState = progress.stateDeck.getPendingTopState()
          val outcome = answerClassificationController.classify(topPendingState.interaction, answer)
          answeredQuestionOutcome = progress.stateList.computeAnswerOutcomeForResult(outcome)
          progress.stateDeck.submitAnswer(answer, answeredQuestionOutcome.feedback)
          progress.completeCurrentCard()
          // Only push a new state if the assessment isn't completed.
          if (!progress.isAssessmentCompleted()) {
            progress.stateDeck.pushState(progress.getNextState())
          }
        } finally {
          // Ensure that the user always returns to the VIEWING_STATE stage to avoid getting stuck in an 'always
          // submitting answer' situation. This can specifically happen if answer classification throws an exception.
          progress.advancePlayStageTo(TrainStage.VIEWING_STATE)
        }

        asyncDataSubscriptionManager.notifyChangeAsync(CURRENT_QUESTION_DATA_PROVIDER_ID)

        return MutableLiveData(AsyncResult.success(answeredQuestionOutcome))
      }
    } catch (e: Exception) {
      return MutableLiveData(AsyncResult.failed(e))
    }
  }

  /**
   * Navigates to the previous question already answered. If the learner is currently on the first question, this method
   * will throw an exception. Calling code is responsible for ensuring this method is only called when it's possible to
   * navigate backward.
   *
   * @return a one-time [LiveData] indicating whether the movement to the previous question was successful, or a failure
   *     if question navigation was attempted at an invalid time (such as when viewing the first question). It's
   *     recommended that calling code only listen to this result for failures, and instead rely on [getCurrentQuestion]
   *     for observing a successful transition to another state.
   */
  fun moveToPreviousQuestion(): LiveData<AsyncResult<Any?>> {
    try {
      progressLock.withLock {
        check(progress.trainStage != TrainStage.NOT_IN_TRAINING_SESSION) {
          "Cannot navigate to a previous question if a training session has not begun."
        }
        check(progress.trainStage != TrainStage.SUBMITTING_ANSWER) {
          "Cannot navigate to a previous question if an answer submission is pending."
        }
        progress.stateDeck.navigateToPreviousState()
        asyncDataSubscriptionManager.notifyChangeAsync(CURRENT_QUESTION_DATA_PROVIDER_ID)
      }
      return MutableLiveData(AsyncResult.success<Any?>(null))
    } catch (e: Exception) {
      return MutableLiveData(AsyncResult.failed(e))
    }
  }

  /**
   * Navigates to the next question in the assessment. This method is only valid if the current [EphemeralQuestion]
   * reported by [getCurrentQuestion] is a completed question. Calling code is responsible for ensuring this method is
   * only called when it's possible to navigate forward.
   *
   * Note that if the current question is pending, the user needs to submit a correct answer via [submitAnswer] before
   * forward navigation can occur.
   *
   * @return a one-time [LiveData] indicating whether the movement to the next question was successful, or a failure if
   *     question navigation was attempted at an invalid time (such as if the current question is pending or terminal).
   *     It's recommended that calling code only listen to this result for failures, and instead rely on
   *     [getCurrentQuestion] for observing a successful transition to another question.
   */
  fun moveToNextQuestion(): LiveData<AsyncResult<Any?>> {
    try {
      progressLock.withLock {
        check(progress.trainStage != TrainStage.NOT_IN_TRAINING_SESSION) {
          "Cannot navigate to a next question if a training session has not beegun."
        }
        check(progress.trainStage != TrainStage.SUBMITTING_ANSWER) {
          "Cannot navigate to a next question if an answer submission is pending."
        }
        progress.stateDeck.navigateToNextState()
        // Track whether the learner has moved to a new card.
        if (progress.isViewingMostRecentCard()) {
          progress.processNavigationToNewCard()
        }
        asyncDataSubscriptionManager.notifyChangeAsync(CURRENT_QUESTION_DATA_PROVIDER_ID)
      }
      return MutableLiveData(AsyncResult.success<Any?>(null))
    } catch (e: Exception) {
      return MutableLiveData(AsyncResult.failed(e))
    }
  }

  /**
   * Returns a [LiveData] monitoring the current [EphemeralQuestion] the learner is currently viewing. If this state
   * corresponds to a a terminal state, then the learner has completed the training session. Note that
   * [moveToPreviousQuestion] and [moveToNextQuestion] will automatically update observers of this live data when the
   * next question is navigated to.
   *
   * This [LiveData] may switch from a completed to a pending result during transient operations like submitting an
   * answer via [submitAnswer]. Calling code should be made resilient to this by caching the current question object to
   * display since it may disappear temporarily during answer submission. Calling code should persist this state object
   * across configuration changes if needed since it cannot rely on this [LiveData] for immediate UI reconstitution
   * after configuration changes.
   *
   * The underlying question returned by this function can only be changed by calls to [moveToNextQuestion] and
   * [moveToPreviousQuestion], or the question training controller if another question session begins. UI code can be
   * confident only calls from the UI layer will trigger changes here to ensure atomicity between receiving and making
   * question state changes.
   *
   * This method is safe to be called before a training session has started. If there is no ongoing session, it should
   * return a pending state.
   */
  fun getCurrentQuestion(): LiveData<AsyncResult<EphemeralQuestion>> {
    return dataProviders.convertToLiveData(currentQuestionDataSource)
  }

  @Suppress("RedundantSuspendModifier") // 'suspend' expected by DataProviders.
  private suspend fun retrieveCurrentQuestionStateAsync(questionsList: List<Question>): AsyncResult<EphemeralQuestion> {
    progressLock.withLock {
      return try {
        when (progress.trainStage) {
          TrainStage.NOT_IN_TRAINING_SESSION -> AsyncResult.pending()
          TrainStage.LOADING_TRAINING_SESSION -> {
            // If the assessment hasn't yet been initialized, initialize it now that a list of questions is available.
            initializeAssessment(questionsList)
            AsyncResult.success(retrieveEphemeralQuestionState())
          }
          TrainStage.VIEWING_STATE -> AsyncResult.success(retrieveEphemeralQuestionState())
          TrainStage.SUBMITTING_ANSWER -> AsyncResult.pending()
        }
      } catch (e: Exception) {
        AsyncResult.failed(e)
      }
    }
  }

  private fun retrieveEphemeralQuestionState(): EphemeralQuestion {
    val ephemeralState = progress.stateDeck.getCurrentEphemeralState()
    return EphemeralQuestion.newBuilder()
      .setEphemeralState(ephemeralState)
      .setCurrentQuestionIndex(progress.getCurrentQuestionIndex())
      .setTotalQuestionCount(progress.getTotalQuestionCount())
      .setInitialTotalQuestionCount(progress.getTotalQuestionCount())
      .build()
  }

  private fun initializeAssessment(questionsList: List<Question>) {
    check(questionsList.isNotEmpty()) { "Cannot start a training session with zero questions." }
    progress.initialize(questionsList)
  }

  /** Returns a temporary [DataProvider] that always provides an empty list of [Question]s. */
  private fun createEmptyQuestionsListDataProvider(): DataProvider<List<Question>> {
    return dataProviders.createInMemoryDataProvider(EMPTY_QUESTIONS_LIST_DATA_PROVIDER_ID) { listOf<Question>() }
  }
}
