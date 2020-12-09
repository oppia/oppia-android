package org.oppia.android.app.home.topiclist

import android.content.Context
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
class TopicSummaryViewModel (
  activity: AppCompatActivity,
  val topicSummary: TopicSummary,
  val entityType: String,
  private val topicSummaryClickListener: TopicSummaryClickListener
) : HomeItemViewModel() {
  val name: String = topicSummary.name
  val totalChapterCount: Int = topicSummary.totalChapterCount
  @ColorInt
  val backgroundColor: Int = retrieveBackgroundColor()
  @ColorInt
  val darkerBackgroundOverlayColor: Int = computeDarkerBackgroundColor()
  private val marginTopBottom = (activity as Context).resources
      .getDimensionPixelSize(R.dimen.topic_list_item_margin_top_bottom)
  private val marginMax = (activity as Context).resources.getDimensionPixelSize(R.dimen.home_margin_max)
  private val marginMin = (activity as Context).resources.getDimensionPixelSize(R.dimen.home_margin_min)
  private var position = -1
  private var spanCount = activity.resources.getInteger(R.integer.home_span_count)

  /** Callback from data-binding for when the summary tile is clicked. */
  fun clickOnSummaryTile() {
    topicSummaryClickListener.onTopicSummaryClicked(topicSummary)
  }

  fun setPosition(newPosition: Int) {
    this.position = newPosition
  }

  fun computeTopMargin(): Int {
    return marginTopBottom
  }

   fun computeBottomMargin(): Int {
    return marginTopBottom
  }

  fun computeStartMargin(): Int {
    var margin  = 0
    when (spanCount) {
      2 -> {
        when (position % spanCount) {
          0 -> margin = marginMin
          else -> margin = marginMax
        }
      }
      3 -> {
        when (position % spanCount) {
          0 -> margin = marginMax
          1 -> margin = marginMin
          2 -> margin = 0
        }
      }
      4 -> {
        when ((position + 1) % spanCount) {
          0 -> margin = marginMax
          1 -> margin = marginMin
          2 -> margin = marginMin / 2
          3 -> margin = 0
        }
      }
    }
    return margin  // error here?
  }

  fun computeEndMargin(): Int {
    var margin = 0
    when (spanCount) {
      2 -> {
        when (position % spanCount) {
          0 -> margin = marginMax
          else -> margin = marginMin
        }
      }
      3 -> {
        when (position % spanCount) {
          0 -> margin = 0
          1 -> margin = marginMin
          2 -> margin = marginMax
        }
      }
      4 -> {
        when ((position + 1) % spanCount) {
          0 -> margin = 0
          1 -> margin = marginMin / 2
          2 -> margin = marginMin
          3 -> margin = marginMax
        }
      }
    }
    return margin // error here?
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
