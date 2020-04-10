package org.oppia.app.player.state.listener

/** Listener for when an [ExplorationActivity] should route to a [HintsAndSolution]. */
interface RouteToHintsAndSolutionListener {
  fun routeToHintsAndSolution(
    explorationId: String,
    newAvailableHintIndex: Int,
    allHintsExhausted: Boolean
  )
}
