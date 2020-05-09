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

  val isAudioBarVisible = ObservableField<Boolean>(false)

  val isHintBulbVisible = ObservableField<Boolean>(false)
  val isHintOpenedAndUnRevealed = ObservableField<Boolean>(false)
  val isHintRevealed = ObservableField<Boolean>(false)

  var currentStateName: String? = null

  private val canSubmitAnswer = ObservableField(true)

  fun setAudioBarVisibility(audioBarVisible: Boolean) {
    isAudioBarVisible.set(audioBarVisible)
  }

  fun setHintBulbVisibility(hintBulbVisible: Boolean) {
    isHintBulbVisible.set(hintBulbVisible)
  }

  fun setHintOpenedAndUnRevealedVisibility(hintOpenedAndUnRevealedVisible: Boolean) {
    isHintOpenedAndUnRevealed.set(hintOpenedAndUnRevealedVisible)
  }

  fun setHintRevealedVisibility(hintRevealedVisible: Boolean) {
    isHintRevealed.set(hintRevealedVisible)
  }

  /**
   * Returns whether there is currently a pending interaction that requires an additional user action to submit the
   * answer.
   */
  fun doesMostRecentInteractionRequireExplicitSubmission(itemList: List<StateItemViewModel>): Boolean {
    return getPendingAnswerHandler(itemList)?.isExplicitAnswerSubmissionRequired() ?: true
  }

  /** Returns whether there is currently a pending interaction that also acts like a navigation button. */
  fun isMostRecentInteractionAutoNavigating(itemList: List<StateItemViewModel>): Boolean {
    return getPendingAnswerHandler(itemList)?.isAutoNavigating() ?: false
  }

  fun setCanSubmitAnswer(canSubmitAnswer: Boolean) = this.canSubmitAnswer.set(canSubmitAnswer)

  fun getCanSubmitAnswer(): ObservableField<Boolean> = canSubmitAnswer

  fun getPendingAnswer(): UserAnswer {
    return if (getPendingAnswerHandler(itemList)?.checkPendingAnswerError(AnswerErrorCategory.SUBMIT_TIME) != null) {
      UserAnswer.getDefaultInstance()
    } else
      getPendingAnswerHandler(itemList)?.getPendingAnswer() ?: UserAnswer.getDefaultInstance()
  }

  private fun getPendingAnswerHandler(itemList: List<StateItemViewModel>): InteractionAnswerHandler? {
    // Search through all items to find the latest InteractionAnswerHandler which should be the pending one. In the
    // future, it may be ideal to make this more robust by actually tracking the handler corresponding to the pending
    // interaction.
    return itemList.findLast { it is InteractionAnswerHandler } as? InteractionAnswerHandler
  }
}
