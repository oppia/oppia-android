package org.oppia.app.topic.questionplayer

import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.databinding.ObservableList
import org.oppia.app.model.UserAnswer
import org.oppia.app.player.state.StatePlayerRecyclerViewAssembler
import org.oppia.app.player.state.answerhandling.AnswerErrorCategory
import org.oppia.app.player.state.itemviewmodel.StateItemViewModel
import org.oppia.app.viewmodel.ObservableViewModel
import javax.inject.Inject

/** [ObservableViewModel] for the question player. */
class QuestionPlayerViewModel @Inject constructor() : ObservableViewModel() {
  val itemList: ObservableList<StateItemViewModel> = ObservableArrayList<StateItemViewModel>()
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
    recyclerViewAssembler: StatePlayerRecyclerViewAssembler
  ): UserAnswer {
    return getPendingAnswerWithoutError(recyclerViewAssembler) ?: UserAnswer.getDefaultInstance()
  }

  private fun getPendingAnswerWithoutError(
    recyclerViewAssembler: StatePlayerRecyclerViewAssembler
  ): UserAnswer? {
    val answerHandler = recyclerViewAssembler
      .getPendingAnswerHandler(itemList)
    return if (answerHandler?.checkPendingAnswerError(AnswerErrorCategory.SUBMIT_TIME) == null) {
      answerHandler?.getPendingAnswer()
    } else {
      null
    }
  }
}
