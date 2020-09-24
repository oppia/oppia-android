package org.oppia.app.player.exploration

import org.oppia.app.model.State

/** Listener for fetching current exploration state data. */
interface HintsAndSolutionExplorationManagerListener {
  fun onExplorationStateLoaded(state: State)
}
