package org.oppia.app.home.continueplaying

import android.graphics.Color
import android.view.View
import androidx.annotation.ColorInt
import androidx.lifecycle.ViewModel
import org.oppia.app.home.HomeItemViewModel
import org.oppia.app.model.StorySummary

// TODO(#206): Remove the color darkening computation and properly set up the stpry thumbnails.
// These values were roughly computed based on the mocks. They won't produce the same colors since darker colors in the
// mocks were not consistently darker. An alternative would be to specify both background colors together to ensure
// proper contrast with readable elements.
const val DARKEN_VALUE_MULTIPLIER: Float = 0.9f
const val DARKEN_SATURATION_MULTIPLIER: Float = 1.2f

/** The view model corresponding to stpry summaries in the stpry summary RecyclerView. */
class StorySummaryViewModel(
  val stprySummary: StorySummary,
  private val storySummaryClickListener: StorySummaryClickListener
) : HomeItemViewModel() {
  val name: String = stprySummary.name
  @ColorInt
  val backgroundColor: Int = retrieveBackgroundColor()
  @ColorInt
  val darkerBackgroundOverlayColor: Int = computeDarkerBackgroundColor()

  /** Callback from data-binding for when the summary tile is clicked. */
  fun clickOnSummaryTile(@Suppress("UNUSED_PARAMETER") v: View) {
    storySummaryClickListener.onStorySummaryClicked(stprySummary)
  }

  @ColorInt
  private fun retrieveBackgroundColor(): Int {
    return stprySummary.stpryThumbnail.backgroundColorRgb
  }

  /** Returns a version of [backgroundColor] that is slightly darker. */
  private fun computeDarkerBackgroundColor(): Int {
    val hsv = floatArrayOf(0f, 0f, 0f)
    Color.colorToHSV(backgroundColor, hsv)
    hsv[1] = (hsv[1] * DARKEN_SATURATION_MULTIPLIER).coerceIn(0f, 1f)
    hsv[2] = (hsv[2] * DARKEN_VALUE_MULTIPLIER).coerceIn(0f, 1f)
    return Color.HSVToColor(hsv)
  }
}
