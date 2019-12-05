package org.oppia.app.player.state.itemviewmodel

import org.oppia.app.player.state.listener.ReplayButtonListener

/** [StateItemViewModel] for replaying the current lesson experience. */
class ReplayButtonViewModel(
  val replayButtonListener: ReplayButtonListener
) : StateItemViewModel(ViewType.REPLAY_NAVIGATION_BUTTON)
