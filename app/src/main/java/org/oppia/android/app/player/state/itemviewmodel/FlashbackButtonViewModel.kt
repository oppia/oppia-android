package org.oppia.android.app.player.state.itemviewmodel

import org.oppia.android.app.player.state.listener.FlashbackButtonListener

/** [StateItemViewModel] for navigation to previously states for revision. */
class FlashbackButtonViewModel(
  val hasConversationView: Boolean,
  val isSplitView: Boolean,
  private val flashbackButtonListener: FlashbackButtonListener,
  val flashbackStateName: String
) : StateItemViewModel(ViewType.FLASHBACK_BUTTON) {

  /** Called when the 'See example' button is clicked. */
  fun onFlashbackButtonClicked() {
    flashbackButtonListener.onFlashbackButtonClicked(flashbackStateName)
  }

  override fun areContentsTheSame(other: StateItemViewModel): Boolean {
    if (other !is FlashbackButtonViewModel) return false
    return hasConversationView == other.hasConversationView &&
      isSplitView == other.isSplitView &&
      flashbackStateName == other.flashbackStateName
  }
}
