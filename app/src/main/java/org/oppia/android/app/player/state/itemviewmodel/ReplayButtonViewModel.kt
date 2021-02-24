package org.oppia.android.app.player.state.itemviewmodel

import org.oppia.android.app.player.state.listener.ReplayButtonListener

/** [StateItemViewModel] for replaying the current lesson experience. */
class ReplayButtonViewModel(
  val hasConversationView: Boolean,
  val replayButtonListener: ReplayButtonListener,
  val isSplitView: Boolean
) : StateItemViewModel(ViewType.REPLAY_NAVIGATION_BUTTON)
