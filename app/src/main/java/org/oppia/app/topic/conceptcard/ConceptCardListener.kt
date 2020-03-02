package org.oppia.app.topic.conceptcard

/** Allows parent activity to dismissConceptCard the [ConceptCardFragment] */
interface ConceptCardListener {
  /** Called when the concept card dialog should be dismissed. */
  fun dismissConceptCard()
}
