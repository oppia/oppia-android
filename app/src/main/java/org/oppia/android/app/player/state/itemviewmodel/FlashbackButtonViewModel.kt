package org.oppia.android.app.player.state.itemviewmodel

import org.oppia.android.app.player.state.listener.FlashbackButtonListener

/** [StateItemViewModel] for navigation to old states for revision. */
class FlashbackButtonViewModel(
  val hasConversationView: Boolean,
  val isSplitView: Boolean,
  val flashbackButtonListener: FlashbackButtonListener,
  val flashbackStateName: String
) : StateItemViewModel(ViewType.FLASHBACK_BUTTON) {

  fun onFlashbackButtonClicked() {
    flashbackButtonListener.onFlashbackButtonClicked(flashbackStateName)
  }
}
