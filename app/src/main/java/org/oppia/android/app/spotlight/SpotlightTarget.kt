package org.oppia.android.app.spotlight

import android.view.View
import org.oppia.android.app.model.Spotlight.FeatureCase

/**
 * Represents a screen target that can be spotlit using [SpotlightManager].
 *
 * @property anchor the view that should be spotlit
 * @property hint the hint text that should be shown on the spotlight overlay
 * @property shape [SpotlightShape] of the spotlight
 * @property feature the [Spotlight] feature being spotlit by this target
 */
data class SpotlightTarget(
  val anchor: View,
  val hint: String,
  val shape: SpotlightShape = SpotlightShape.RoundedRectangle,
  val feature: FeatureCase
) {

  /** The left margin of the anchor. */
  val anchorLeft: Float = calculateAnchorLeft()
  /** The top margin of the anchor. */
  val anchorTop: Float = calculateAnchorTop()
  /** The height of the anchor. */
  val anchorHeight: Int = calculateAnchorHeight()
  /** The width of the anchor. */
  val anchorWidth: Int = calculateAnchorWidth()
  /** The position of the vertical centre of the anchor. */
  val anchorCentreX = calculateAnchorCentreX()
  /** The position of the horizontal centre of the anchor. */
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
