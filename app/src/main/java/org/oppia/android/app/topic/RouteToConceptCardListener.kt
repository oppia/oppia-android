package org.oppia.app.topic

/** Listener for when an [TopicActivity] should route to a [ConceptCardFragment]. */
interface RouteToConceptCardListener {
  fun routeToConceptCard(skillId: String)
}
