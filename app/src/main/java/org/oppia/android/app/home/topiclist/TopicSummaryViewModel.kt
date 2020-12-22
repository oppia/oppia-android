package org.oppia.android.app.home.topiclist

import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatActivity
import org.oppia.android.R
import org.oppia.android.app.home.HomeItemViewModel
import org.oppia.android.app.model.TopicSummary

// TODO(#206): Remove the color darkening computation and properly set up the topic thumbnails.
// These values were roughly computed based on the mocks. They won't produce the same colors since darker colors in the
// mocks were not consistently darker. An alternative would be to specify both background colors together to ensure
// proper contrast with readable elements.
const val DARKEN_VALUE_MULTIPLIER: Float = 0.9f
const val DARKEN_SATURATION_MULTIPLIER: Float = 1.2f

/** The view model corresponding to individual topic summaries in the topic summary RecyclerView. */
class TopicSummaryViewModel(
  private val activity: AppCompatActivity,
  val topicSummary: TopicSummary,
  val entityType: String,
  private val topicSummaryClickListener: TopicSummaryClickListener,
  private val position: Int
) : HomeItemViewModel() {
  val name: String = topicSummary.name
  val totalChapterCount: Int = topicSummary.totalChapterCount

  @ColorInt
  val backgroundColor: Int = retrieveBackgroundColor()

  @ColorInt
  val darkerBackgroundOverlayColor: Int = computeDarkerBackgroundColor()
  private val marginMax by lazy {
    activity.resources.getDimensionPixelSize(R.dimen.home_margin_max)
  }
  private val marginMin by lazy {
    activity.resources.getDimensionPixelSize(R.dimen.home_margin_min)
  }
  private val spanCount by lazy {
    activity.resources.getInteger(R.integer.home_span_count)
  }

  /** Callback from data-binding for when the summary tile is clicked. */
  fun clickOnSummaryTile() {
    topicSummaryClickListener.onTopicSummaryClicked(topicSummary)
  }

  fun computeStartMargin(): Int {
    return when (spanCount) {
      2 -> when (position % spanCount) {
        0 -> marginMax
        else -> marginMin
      }
      3 -> when (position % spanCount) {
        0 -> marginMax
        1 -> marginMin
        2 -> 0
        else -> 0
      }
      4 -> when (position % spanCount) {
        0 -> marginMax
        1 -> marginMin
        2 -> marginMin / 2
        3 -> 0
        else -> 0
      }
      else -> 0
    }
  }

  fun computeEndMargin(): Int {
    return when (spanCount) {
      2 -> when (position % spanCount) {
        0 -> marginMin
        else -> marginMax
      }
      3 -> when (position % spanCount) {
        0 -> 0
        1 -> marginMin
        2 -> marginMax
        else -> 0
      }
      4 -> when (position % spanCount) {
        0 -> 0
        1 -> marginMin / 2
        2 -> marginMin
        3 -> marginMax
        else -> 0
      }
      else -> 0
    }
  }

  @ColorInt
  private fun retrieveBackgroundColor(): Int {
    return topicSummary.topicThumbnail.backgroundColorRgb
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
