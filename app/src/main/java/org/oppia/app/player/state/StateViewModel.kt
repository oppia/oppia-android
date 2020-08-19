package org.oppia.app.player.state

import androidx.databinding.ObservableField
import androidx.databinding.ObservableList
import androidx.lifecycle.ViewModel
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.model.UserAnswer
import org.oppia.app.player.state.answerhandling.AnswerErrorCategory
import org.oppia.app.player.state.answerhandling.InteractionAnswerHandler
import org.oppia.app.player.state.itemviewmodel.StateItemViewModel
import org.oppia.app.viewmodel.ObservableArrayList
import org.oppia.app.viewmodel.ObservableViewModel
import javax.inject.Inject

/** [ViewModel] for state-fragment. */
@FragmentScope
class StateViewModel @Inject constructor() : ObservableViewModel() {
  val itemList: ObservableList<StateItemViewModel> = ObservableArrayList()
  val rightItemList: ObservableList<StateItemViewModel> = ObservableArrayList()

  val isSplitView = ObservableField(false)
  val centerGuidelinePercentage = ObservableField(0.5f)

  val isAudioBarVisible = ObservableField(false)

  var newAvailableHintIndex = -1
  var allHintsExhausted = false
  val isHintBulbVisible = ObservableField(false)
  val isHintOpenedAndUnRevealed = ObservableField(false)

  var currentStateName = ObservableField<String>(null as? String?)

  private val canSubmitAnswer = ObservableField(false)

  fun setAudioBarVisibility(audioBarVisible: Boolean) {
    isAudioBarVisible.set(audioBarVisible)
  }

  fun setHintBulbVisibility(hintBulbVisible: Boolean) {
    isHintBulbVisible.set(hintBulbVisible)
  }

  fun setHintOpenedAndUnRevealedVisibility(hintOpenedAndUnRevealedVisible: Boolean) {
    isHintOpenedAndUnRevealed.set(hintOpenedAndUnRevealedVisible)
  }

  fun setCanSubmitAnswer(canSubmitAnswer: Boolean) = this.canSubmitAnswer.set(canSubmitAnswer)

  fun getCanSubmitAnswer(): ObservableField<Boolean> = canSubmitAnswer

  private fun getAnswerItemList(): List<StateItemViewModel> {
    return if (isSplitView.get() == true) {
      rightItemList
    } else {
      itemList
    }
  }

  fun getPendingAnswer(
    answerHandlerRetriever: (List<StateItemViewModel>) -> InteractionAnswerHandler?
  ): UserAnswer {
    return getPendingAnswerWithoutError(
      answerHandlerRetriever(
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
}
