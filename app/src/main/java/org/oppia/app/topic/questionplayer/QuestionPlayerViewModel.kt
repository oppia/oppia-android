package org.oppia.app.topic.questionplayer

import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.databinding.ObservableList
import org.oppia.app.model.InteractionObject
import org.oppia.app.model.UserAnswer
import org.oppia.app.player.state.answerhandling.InteractionAnswerHandler
import org.oppia.app.player.state.itemviewmodel.StateItemViewModel
import org.oppia.app.viewmodel.ObservableViewModel
import javax.inject.Inject

class QuestionPlayerViewModel @Inject constructor() : ObservableViewModel() {
  val itemList: ObservableList<StateItemViewModel> = ObservableArrayList<StateItemViewModel>()
  val questionCount = ObservableField(0)
  val currentQuestion = ObservableField(0)
  val progressPercentage = ObservableField(0)
  val isAtEndOfSession = ObservableBoolean(false)

  /**
   * Returns whether there is currently a pending interaction that requires an additional user action to submit the
   * answer.
   */
  fun doesMostRecentInteractionRequireExplicitSubmission(itemList: List<StateItemViewModel>): Boolean {
    return getPendingAnswerHandler(itemList)?.isExplicitAnswerSubmissionRequired() ?: true
  }

  // TODO(#164): Add a hasPendingAnswer() that binds to the enabled state of the Submit button.
  fun getPendingAnswer(): UserAnswer {
    return getPendingAnswerHandler(itemList)?.getPendingAnswer() ?: UserAnswer.getDefaultInstance()
  }

  private fun getPendingAnswerHandler(itemList: List<StateItemViewModel>): InteractionAnswerHandler? {
    // Search through all items to find the latest InteractionAnswerHandler which should be the pending one. In the
    // future, it may be ideal to make this more robust by actually tracking the handler corresponding to the pending
    // interaction.
    return itemList.findLast { it is InteractionAnswerHandler } as? InteractionAnswerHandler
  }
}