package org.oppia.android.app.player.state.itemviewmodel

import org.oppia.android.app.player.state.listener.ReturnToQuestionButtonListener

/** [StateItemViewModel] for navigation to latest pending state. */
class ReturnToQuestionViewModel(
  val hasConversationView: Boolean,
  val isSplitView: Boolean,
  val returnToQuestionButtonListener: ReturnToQuestionButtonListener
) : StateItemViewModel(ViewType.RETURN_TO_QUESTION_BUTTON) {
  override fun areContentsTheSame(other: StateItemViewModel): Boolean {
    if (other !is ReturnToQuestionViewModel) return false
    return hasConversationView == other.hasConversationView &&
      isSplitView == other.isSplitView
  }
}
