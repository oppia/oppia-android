package org.oppia.app.topic.reviewcard

/** Allows parent activity to dismiss the [ReviewCardFragment] */
interface ReviewCardListener {
  /** Called when the review card dialog should be dismissed. */
  fun dismiss()
}
