package org.oppia.app.player.state.listener

import org.oppia.app.model.HelpIndex

/** Callback interface for when hints can be made available to the learner. */
interface ShowHintAvailabilityListener {
  /** Called when a hint is available to be shown, or null if all hints have been revealed. */
  fun onHintAvailable(helpIndex: HelpIndex)

  fun hintVisibilityBetweenState(hintVisibility: Boolean)
}
