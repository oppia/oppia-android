package org.oppia.android.app.topic.conceptcard

/** Allows parent activity to dismiss the [ConceptCardFragment] */
interface ConceptCardListener {
  /** Called when the concept card dialog should be dismissed. */
  fun dismissConceptCard()
}
