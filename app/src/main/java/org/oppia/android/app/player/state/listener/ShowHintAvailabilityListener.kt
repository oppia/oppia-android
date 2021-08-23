package org.oppia.android.app.player.state.listener

import org.oppia.android.app.model.HelpIndex

/** Callback interface for when hints can be made available to the learner. */
interface ShowHintAvailabilityListener {
  /**
   * Called when a hint is available to be shown, or null if all hints have been revealed.
   *
   * @param helpIndex the latest hints/solution state
   * @param isCurrentStatePendingState whether the current state being viewed by the learner is
   *     pending and should have hints enabled
   */
  fun onHintAvailable(helpIndex: HelpIndex, isCurrentStatePendingState: Boolean)
}
