package org.oppia.android.app.player.state.listener

import org.oppia.android.app.model.HelpIndex

/** Listener for when an [ExplorationActivity] should route to a [HintsAndSolution]. */
interface RouteToHintsAndSolutionListener {
  fun routeToHintsAndSolution(
    id: String,
    helpIndex: HelpIndex
  )
}
