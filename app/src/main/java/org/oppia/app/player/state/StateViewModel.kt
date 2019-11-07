package org.oppia.app.player.state

import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableList
import androidx.lifecycle.ViewModel
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.model.InteractionObject
import org.oppia.app.player.state.answerhandling.InteractionAnswerHandler
import org.oppia.app.player.state.itemviewmodel.StateItemViewModel
import org.oppia.app.viewmodel.ObservableViewModel
import javax.inject.Inject

/** [ViewModel] for state-fragment. */
@FragmentScope
class StateViewModel @Inject constructor() : ObservableViewModel() {
  val itemList: ObservableList<StateItemViewModel> = ObservableArrayList<StateItemViewModel>()

  /**
   * Returns whether there is currently a pending interaction that requires an additional user action to submit the
   * answer.
   */
  fun doesMostRecentInteractionRequireExplicitSubmission(itemList: List<StateItemViewModel>): Boolean {
    return getPendingAnswerHandler(itemList)?.isExplicitAnswerSubmissionRequired() ?: true
  }

  // TODO(#164): Add a hasPendingAnswer() that binds to the enabled state of the Submit button.
  fun getPendingAnswer(): InteractionObject {
    return getPendingAnswerHandler(itemList)?.getPendingAnswer() ?: InteractionObject.getDefaultInstance()
  }

  private fun getPendingAnswerHandler(itemList: List<StateItemViewModel>): InteractionAnswerHandler? {
    // Search through all items to find the latest InteractionAnswerHandler which should be the pending one. In the
    // future, it may be ideal to make this more robust by actually tracking the handler corresponding to the pending
    // interaction.
    return itemList.findLast { it is InteractionAnswerHandler } as? InteractionAnswerHandler
  }
}
