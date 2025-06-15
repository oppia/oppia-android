package org.oppia.android.app.player.state.itemviewmodel

import org.oppia.android.app.player.state.listener.RevisitButtonListener

/** [StateItemViewModel] for navigation to old states for revision. */
class FlashbackButtonViewModel(
  val hasConversationView: Boolean,
  val hasPreviousButton: Boolean, //this should be removed
  val isSplitView: Boolean,
  val revisitButtonListener: RevisitButtonListener,
  val flashbackStateName: String
) : StateItemViewModel(ViewType.FLASHBACK_BUTTON) {

  fun onFlashbackButtonClicked() {
    revisitButtonListener.onFlashbackButtonClicked(flashbackStateName)
  }
}
