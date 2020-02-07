package org.oppia.app.topic.reviewcard

/** Allows parent activity to dismiss the [ConceptCardFragment] */
interface ReviewCardListener {
  /** Called when the concept card dialog should be dismissed. */
  fun dismiss()
}
