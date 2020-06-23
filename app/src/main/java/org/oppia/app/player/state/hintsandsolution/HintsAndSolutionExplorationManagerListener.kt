package org.oppia.app.player.state.hintsandsolution

import org.oppia.app.model.State

/** Listener for fetching current exploration state data. */
interface HintsAndSolutionExplorationManagerListener {

  fun onExplorationStateLoaded(
    state: State,
    explorationId: String,
    newAvailableHintIndex: Int,
    allHintsExhausted: Boolean
  )
}
