package org.oppia.app.player.state.hintsandsolution

import org.oppia.app.model.State

interface HintsAndSolutionExplorationManagerListener {

  fun onExplorationStateLoaded(
    state: State,
    explorationId: String,
    newAvailableHintIndex: Int,
    allHintsExhausted: Boolean
  )
}
