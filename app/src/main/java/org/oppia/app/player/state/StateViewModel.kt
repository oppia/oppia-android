package org.oppia.app.player.state

import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableField
import androidx.databinding.ObservableList
import androidx.databinding.ObservableMap
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
  fun doesPendingInteractionRequireExplicitSubmission(): Boolean {
    return getPendingAnswerHandler()?.isExplicitAnswerSubmissionRequired() ?: true
  }

  // TODO(BenHenning): Add a hasPendingAnswer() that binds to the enabled state of the Submit button.
  fun getPendingAnswer(): InteractionObject {
    return getPendingAnswerHandler()?.getPendingAnswer() ?: InteractionObject.getDefaultInstance()
  }

  private fun getPendingAnswerHandler(): InteractionAnswerHandler? {
    // TODO(BenHenning): Find a better way to do this. First, the search is bad. Second, the implication that more than
    // one interaction view can be active is bad.
    return itemList.findLast { it is InteractionAnswerHandler } as? InteractionAnswerHandler
  }
}
