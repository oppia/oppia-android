package org.oppia.android.app.home.topiclist

import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatActivity
import org.oppia.android.R
import org.oppia.android.app.home.HomeItemViewModel
import org.oppia.android.app.model.TopicSummary
import java.util.Objects

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
  private val outerMargin by lazy {
    activity.resources.getDimensionPixelSize(R.dimen.home_outer_margin)
  }
  private val innerMargin by lazy {
    activity.resources.getDimensionPixelSize(R.dimen.home_inner_margin)
  }
  private val spanCount by lazy {
    activity.resources.getInteger(R.integer.home_span_count)
  }

  /** Callback from data-binding for when the summary tile is clicked. */
  fun clickOnSummaryTile() {
    topicSummaryClickListener.onTopicSummaryClicked(topicSummary)
  }

  /**
   * Determines the start margin for an individual TopicSummary relative to the grid columns laid out on the
   * HomeActivity. GridLayout columns are evenly spread out across the entire activity screen but the
   * Topic Summaries are positioned towards the center, so start margins are calculated to stagger inside each
   * fixed column but centered on the activity's layout, as shown below.
   *
   *  |        _____|      _____   |   _____      |_____        |
   *  |       |     |     |     |  |  |     |     |     |       |
   *  |       |     |     |     |  |  |     |     |     |       |
   *  |       |_____|     |_____|  |  |_____|     |_____|       |
   *  |             |              |              |             |
   *  |        _____       _____       _____       _____        |
   *  |       |     |     |     |     |     |     |     |       |
   *  |       |     |     |     |     |     |     |     |       |
   *  |       |_____|     |_____|     |_____|     |_____|       |
   *  |                                                         |
   */
  fun computeStartMargin(): Int {
    return when (spanCount) {
      2 -> when (position % spanCount) {
        0 -> outerMargin
        else -> innerMargin
      }
      3 -> when (position % spanCount) {
        0 -> outerMargin
        1 -> innerMargin
        2 -> 0
        else -> 0
      }
      4 -> when (position % spanCount) {
        0 -> outerMargin
        1 -> innerMargin
        2 -> innerMargin / 2
        3 -> 0
        else -> 0
      }
      else -> 0
    }
  }

  /**
   * Determines the end margin for an individual TopicSummary relative to the grid columns laid out on the
   * HomeActivity. The end margins are calculated to stagger inside each fixed column but centered on the
   * activity's layout (see [computeStartMargin]).
   */
  fun computeEndMargin(): Int {
    return when (spanCount) {
      2 -> when (position % spanCount) {
        0 -> innerMargin
        else -> outerMargin
      }
      3 -> when (position % spanCount) {
        0 -> 0
        1 -> innerMargin
        2 -> outerMargin
        else -> 0
      }
      4 -> when (position % spanCount) {
        0 -> 0
        1 -> innerMargin / 2
        2 -> innerMargin
        3 -> outerMargin
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

  // Overriding equals is needed so that DataProvider combine functions used in the HomeViewModel
  // will only rebind when the actual data in the data list changes, rather than when the ViewModel
  // object changes.
  override fun equals(other: Any?): Boolean {
    return other is TopicSummaryViewModel &&
      other.topicSummary == this.topicSummary &&
      other.entityType == this.entityType &&
      other.position == this.position
  }

  override fun hashCode() = Objects.hash(topicSummary, entityType, position)
}
