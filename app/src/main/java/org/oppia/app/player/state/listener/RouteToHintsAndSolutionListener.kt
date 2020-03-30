package org.oppia.app.player.state.listener

import org.oppia.app.model.State

/** Listener for when an [ExplorationActivity] should route to a [HintsAndSolution]. */
interface RouteToHintsAndSolutionListener {
  fun routeToHintsAndSolution(
    newState: State,
    explorationId: String,
    newAvailableHintIndex: Int,
    allHintsExhausted: Boolean
  )
}
