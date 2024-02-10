package org.oppia.android.app.player.state.itemviewmodel

import androidx.databinding.ObservableField
import org.oppia.android.app.player.state.listener.PreviousNavigationButtonListener
import org.oppia.android.app.player.state.listener.SubmitNavigationButtonListener

/** [StateItemViewModel] for navigation to previous states and submitting new answers. */
class SubmitButtonViewModel(
  val canSubmitAnswer: ObservableField<Boolean>,
  val hasConversationView: Boolean,
  val hasPreviousButton: Boolean,
  val previousNavigationButtonListener: PreviousNavigationButtonListener,
  val submitNavigationButtonListener: SubmitNavigationButtonListener,
  val isSplitView: Boolean
) : StateItemViewModel(ViewType.SUBMIT_ANSWER_BUTTON) {
  override fun areContentsTheSame(other: StateItemViewModel): Boolean {
    if (this === other) return true
    if (other !is SubmitButtonViewModel) return false

    return (
      canSubmitAnswer.get() == other.canSubmitAnswer.get() &&
        hasConversationView == other.hasConversationView &&
        hasPreviousButton == other.hasPreviousButton &&
        isSplitView == other.isSplitView
      )
  }
}
