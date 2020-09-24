package org.oppia.android.app.player.exploration

import org.oppia.android.app.model.State

/** Listener for fetching current exploration state data. */
interface HintsAndSolutionExplorationManagerListener {
  fun onExplorationStateLoaded(state: State)
}
