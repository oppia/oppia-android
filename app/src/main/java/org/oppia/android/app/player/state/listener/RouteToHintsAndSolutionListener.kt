package org.oppia.app.player.state.listener

/** Listener for when an [ExplorationActivity] should route to a [HintsAndSolution]. */
interface RouteToHintsAndSolutionListener {
  fun routeToHintsAndSolution(
    id: String,
    newAvailableHintIndex: Int,
    allHintsExhausted: Boolean
  )
}
