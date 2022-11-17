package org.oppia.android.app.spotlight

/** The shape of the overlay screen cutout surrounding the spotlit view. */
sealed class SpotlightShape {
  object RoundedRectangle : SpotlightShape()
  object Circle : SpotlightShape()
}
