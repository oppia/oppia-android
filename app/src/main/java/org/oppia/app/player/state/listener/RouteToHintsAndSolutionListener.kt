package org.oppia.app.player.state.listener

/** Listener for when an [TopicActivity] should route to a [ConceptCardFragment]. */
interface RouteToHintsAndSolutionListener {
  fun routeToHintsAndSolution(skillId: String)
}
