package org.oppia.android.app.spotlight

/** The shape of the overlay screen cutout surrounding the spotlit view. */
sealed class SpotlightShape {
  /** Circle shape for the spotlight highlight. */
  object Circle : SpotlightShape()

  /** Rounded rectangle shape for the spotlight highlight. */
  object RoundedRectangle : SpotlightShape()
}
