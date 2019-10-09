package org.oppia.domain.question

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.oppia.app.model.AnsweredQuestionOutcome
import org.oppia.app.model.EphemeralQuestion
import org.oppia.app.model.EphemeralState
import org.oppia.app.model.InteractionObject
import org.oppia.app.model.PendingState
import org.oppia.app.model.Question
import org.oppia.app.model.SubtitledHtml
import org.oppia.util.data.AsyncResult
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Controller that tracks and reports the learner's ephemeral/non-persisted progress through an practice training
 * session. Note that this controller only supports one active training session at a time.
 *
 * The current training session is started via the question training controller.
 *
 * This class is thread-safe, but the order of applied operations is arbitrary. Calling code should take care to ensure
 * that uses of this class do not specifically depend on ordering.
 */
@Singleton
class QuestionAssessmentProgressController @Inject constructor() {
  private lateinit var inProgressQuestionList: List<Question>
  private var playing: Boolean = false

  internal fun beginQuestionTrainingSession(questionsList: List<Question>) {
    check(!playing) { "Cannot start a new training session until the previous one is completed" }
    check(questionsList.isNotEmpty()) { "Cannot start a training session with zero questions." }
    inProgressQuestionList = questionsList
  }

  internal fun finishQuestionTrainingSession() {
    check(playing) { "Cannot stop a new training session which wasn't started" }
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
    val outcome = AnsweredQuestionOutcome.newBuilder()
      .setFeedback(SubtitledHtml.newBuilder().setHtml("Response to answer: $answer"))
      .setIsCorrectAnswer(true)
      .build()
    return MutableLiveData(AsyncResult.success(outcome))
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
    check(playing) { "Cannot move to the previous question unless an active training session is ongoing" }
    return MutableLiveData(AsyncResult.success<Any?>(null))
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
    check(playing) { "Cannot move to the next question unless an active training session is ongoing" }
    return MutableLiveData(AsyncResult.success<Any?>(null))
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
    val currentQuestion = inProgressQuestionList.first()
    val ephemeralQuestion = EphemeralQuestion.newBuilder()
      .setEphemeralState(EphemeralState.newBuilder()
        .setState(currentQuestion.questionState)
        .setPendingState(PendingState.getDefaultInstance()))
      .setCurrentQuestionIndex(0)
      .setTotalQuestionCount(inProgressQuestionList.size)
      .setInitialTotalQuestionCount(inProgressQuestionList.size)
      .build()
    return MutableLiveData(AsyncResult.success(ephemeralQuestion))
  }
}
