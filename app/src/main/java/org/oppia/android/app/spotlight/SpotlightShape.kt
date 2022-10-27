package org.oppia.android.app.spotlight

sealed class SpotlightShape {
  object RoundedRectangle : SpotlightShape()
  object Circle : SpotlightShape()
}
