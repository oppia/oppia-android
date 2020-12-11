package org.oppia.android.app.home.topiclist

import android.graphics.Color
import androidx.annotation.ColorInt
import org.oppia.android.app.home.HomeItemViewModel
import org.oppia.android.app.model.TopicSummary
import java.util.*

// TODO(#206): Remove the color darkening computation and properly set up the topic thumbnails.
// These values were roughly computed based on the mocks. They won't produce the same colors since darker colors in the
// mocks were not consistently darker. An alternative would be to specify both background colors together to ensure
// proper contrast with readable elements.

/** The view model corresponding to coming soon topic summaries in the topic summary RecyclerView. */
class ComingSoonTopicsListViewModel(
  val topicSummary: TopicSummary,
  val entityType: String,
  private val topicSummaryClickListener: TopicSummaryClickListener
) : Observable() {
  val name: String = topicSummary.name
  val totalChapterCount: Int = topicSummary.totalChapterCount
  @ColorInt
  val backgroundColor: Int = retrieveBackgroundColor()
  @ColorInt
  val darkerBackgroundOverlayColor: Int = computeDarkerBackgroundColor()

  /** Callback from data-binding for when the summary tile is clicked. */
  fun clickOnSummaryTile() {
    topicSummaryClickListener.onTopicSummaryClicked(topicSummary)
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
