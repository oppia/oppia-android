package org.oppia.android.app.topic.questionplayer

import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.databinding.ObservableList
import org.oppia.android.R
import org.oppia.android.app.model.AnswerErrorCategory
import org.oppia.android.app.model.UserAnswer
import org.oppia.android.app.model.UserAnswerState
import org.oppia.android.app.player.state.answerhandling.InteractionAnswerHandler
import org.oppia.android.app.player.state.itemviewmodel.StateItemViewModel
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.app.viewmodel.ObservableArrayList
import org.oppia.android.app.viewmodel.ObservableViewModel
import javax.inject.Inject

/** [ObservableViewModel] for the question player. */
class QuestionPlayerViewModel @Inject constructor(
  private val resourceHandler: AppLanguageResourceHandler
) : ObservableViewModel() {
  val itemList: ObservableList<StateItemViewModel> = ObservableArrayList<StateItemViewModel>()
  val rightItemList: ObservableList<StateItemViewModel> = ObservableArrayList()

  val isSplitView = ObservableField(false)
  val centerGuidelinePercentage = ObservableField(0.5f)

  val questionCount = ObservableField(0)
  val currentQuestion = ObservableField(0)
  val progressPercentage = ObservableField(0)
  val isAtEndOfSession = ObservableBoolean(false)
  private val canSubmitAnswer = ObservableField(false)

  val isHintBulbVisible = ObservableField(false)
  val isHintOpenedAndUnRevealed = ObservableField(false)

  val questionProgressText: ObservableField<String> =
    ObservableField(
      computeQuestionProgressText(
        DEFAULT_CURRENT_QUESTION, DEFAULT_QUESTION_COUNT, DEFAULT_IS_AT_END_OF_SESSION
      )
    )

  fun setHintBulbVisibility(hintBulbVisible: Boolean) {
    isHintBulbVisible.set(hintBulbVisible)
  }

  fun setHintOpenedAndUnRevealedVisibility(hintOpenedAndUnRevealedVisible: Boolean) {
    isHintOpenedAndUnRevealed.set(hintOpenedAndUnRevealedVisible)
  }

  fun updateQuestionProgress(
    currentQuestion: Int,
    questionCount: Int,
    progressPercentage: Int,
    isAtEndOfSession: Boolean
  ) {
    this.currentQuestion.set(currentQuestion)
    this.questionCount.set(questionCount)
    this.progressPercentage.set(progressPercentage)
    this.isAtEndOfSession.set(isAtEndOfSession)
    questionProgressText.set(
      computeQuestionProgressText(currentQuestion, questionCount, isAtEndOfSession)
    )
  }

  fun setCanSubmitAnswer(canSubmitAnswer: Boolean) = this.canSubmitAnswer.set(canSubmitAnswer)

  fun getCanSubmitAnswer(): ObservableField<Boolean> = canSubmitAnswer

  fun getPendingAnswer(
    retrieveAnswerHandler: (List<StateItemViewModel>) -> InteractionAnswerHandler?
  ): UserAnswer {
    return getPendingAnswerWithoutError(
      retrieveAnswerHandler(
        getAnswerItemList()
      )
    ) ?: UserAnswer.getDefaultInstance()
  }

  private fun computeQuestionProgressText(
    currentQuestion: Int,
    questionCount: Int,
    isAtEndOfSession: Boolean
  ): String {
    return if (isAtEndOfSession) {
      resourceHandler.getStringInLocale(R.string.question_training_session_progress_finished)
    } else {
      resourceHandler.getStringInLocaleWithWrapping(
        R.string.question_training_session_progress,
        currentQuestion.toString(),
        questionCount.toString()
      )
    }
  }

  fun getUserAnswerState(
    retrieveAnswerHandler: (List<StateItemViewModel>) -> InteractionAnswerHandler?
  ): UserAnswerState {
    return retrieveAnswerHandler(getAnswerItemList())?.getUserAnswerState()
      ?: UserAnswerState.getDefaultInstance()
  }

  private fun getPendingAnswerWithoutError(
    answerHandler: InteractionAnswerHandler?
  ): UserAnswer? {
    return if (answerHandler?.checkPendingAnswerError(AnswerErrorCategory.SUBMIT_TIME) == null) {
      answerHandler?.getPendingAnswer()
    } else {
      null
    }
  }

  private fun getAnswerItemList(): List<StateItemViewModel> {
    return if (isSplitView.get() == true) {
      rightItemList
    } else {
      itemList
    }
  }

  private companion object {
    private const val DEFAULT_CURRENT_QUESTION = 0
    private const val DEFAULT_QUESTION_COUNT = 0
    private const val DEFAULT_IS_AT_END_OF_SESSION = false
  }
}
