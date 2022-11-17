package org.oppia.android.app.spotlight

import android.view.View

/**
 * Data class to hold a [SpotlightTarget].
 *
 * @param anchor the view that should be spotlit
 * @param hint the hint text that should be shown on the spotlight overlay
 * @param shape [SpotlightShape] of the spotlight
 * @param feature The [Spotlight] feature-case corresponding to the spotlight target
 */
data class SpotlightTarget(
  val anchor: View,
  val hint: String = "",
  val shape: SpotlightShape = SpotlightShape.RoundedRectangle,
  val feature: org.oppia.android.app.model.Spotlight.FeatureCase
) {
  val anchorLeft: Float = calculateAnchorLeft()
  val anchorTop: Float = calculateAnchorTop()
  val anchorHeight: Int = calculateAnchorHeight()
  val anchorWidth: Int = calculateAnchorWidth()
  val anchorCentreX = calculateAnchorCentreX()
  val anchorCentreY = calculateAnchorCentreY()

  init {
    calculateAnchorLeft()
    calculateAnchorTop()
    calculateAnchorHeight()
    calculateAnchorWidth()
    calculateAnchorCentreX()
    calculateAnchorCentreY()
  }

  private fun calculateAnchorLeft(): Float {
    val location = IntArray(2)
    anchor.getLocationInWindow(location)
    val x = location[0]
    return x.toFloat()
  }

  private fun calculateAnchorTop(): Float {
    val location = IntArray(2)
    anchor.getLocationInWindow(location)
    val y = location[1]
    return y.toFloat()
  }

  private fun calculateAnchorHeight(): Int {
    return anchor.height
  }

  private fun calculateAnchorWidth(): Int {
    return anchor.width
  }

  private fun calculateAnchorCentreX(): Float {
    return anchorLeft + anchorWidth / 2
  }

  private fun calculateAnchorCentreY(): Float {
    return anchorTop + anchorHeight / 2
  }
}
