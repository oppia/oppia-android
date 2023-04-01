package org.oppia.android.app.spotlight

/** The shape of the overlay screen cutout surrounding the spotlit view. */
sealed class SpotlightShape {
  /** Represents a circular spotlight highlight region. */
  object Circle : SpotlightShape()

  /** Represents a rounded rectangular spotlight highlight region. */
  object RoundedRectangle : SpotlightShape()
}
