package org.oppia.android.app.topic.questionplayer

import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.databinding.ObservableList
import org.oppia.android.app.model.UserAnswer
import org.oppia.android.app.player.state.answerhandling.AnswerErrorCategory
import org.oppia.android.app.player.state.answerhandling.InteractionAnswerHandler
import org.oppia.android.app.player.state.itemviewmodel.StateItemViewModel
import org.oppia.android.app.viewmodel.ObservableArrayList
import org.oppia.android.app.viewmodel.ObservableViewModel
import javax.inject.Inject

/** [ObservableViewModel] for the question player. */
class QuestionPlayerViewModel @Inject constructor() : ObservableViewModel() {
  val itemList: ObservableList<StateItemViewModel> = ObservableArrayList<StateItemViewModel>()
  val rightItemList: ObservableList<StateItemViewModel> = ObservableArrayList()

  val isSplitView = ObservableField(false)
  val centerGuidelinePercentage = ObservableField(0.5f)

  val questionCount = ObservableField(0)
  val currentQuestion = ObservableField(0)
  val progressPercentage = ObservableField(0)
  val isAtEndOfSession = ObservableBoolean(false)
  private val canSubmitAnswer = ObservableField(false)

  var newAvailableHintIndex = -1
  var allHintsExhausted = false
  val isHintBulbVisible = ObservableField(false)
  val isHintOpenedAndUnRevealed = ObservableField(false)

  fun setHintBulbVisibility(hintBulbVisible: Boolean) {
    isHintBulbVisible.set(hintBulbVisible)
  }

  fun setHintOpenedAndUnRevealedVisibility(hintOpenedAndUnRevealedVisible: Boolean) {
    isHintOpenedAndUnRevealed.set(hintOpenedAndUnRevealedVisible)
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
}
