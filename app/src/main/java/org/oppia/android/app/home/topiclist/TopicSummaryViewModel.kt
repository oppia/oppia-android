package org.oppia.android.app.home.topiclist

import android.content.Context
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.updateLayoutParams
import androidx.databinding.BindingAdapter
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
  private var spanCount = 0

  /** Callback from data-binding for when the summary tile is clicked. */
  fun clickOnSummaryTile() {
    topicSummaryClickListener.onTopicSummaryClicked(topicSummary)
  }

// position is the order that it is displayed
  // position is fixed based on itemList
  // we know position of ViewModel
  // new notion of position -- position of topic within topic list
  // we know this when we make the ViewModels
  // store position and span count and just recalculate the margins with four different function
 // (start end, top bottom are four margins)
  // compute each of thes eindividually and call for them in data binding
  fun setSpanCount(newSpan: Int) {
    this.spanCount = newSpan
  }

  fun setPosition(newPosition: Int) {
    this.position = newPosition
  }

  @BindingAdapter("layout_marginTop")
  fun setLayoutMarginTop(view: View) {
    view.updateLayoutParams<ViewGroup.MarginLayoutParams> {
      this.topMargin = computeTopMargin()
    }
  }

  @BindingAdapter("layout_marginBottom")
  fun setLayoutMarginBottom(view: View) {
    view.updateLayoutParams<ViewGroup.MarginLayoutParams> {
      this.bottomMargin = computeBottomMargin()
    }
  }

  @BindingAdapter("layout_marginStart")
  fun setLayoutMarginStart(view: View) {
    view.updateLayoutParams<ViewGroup.MarginLayoutParams> {
      this.setMarginStart(computeStartMargin())
    }
  }

  @BindingAdapter("layout_marginEnd")
  fun setLayoutMarginEnd(view: View) {
    view.updateLayoutParams<ViewGroup.MarginLayoutParams> {
      this.setMarginEnd(computeEndMargin())
    }
  }

  private fun computeTopMargin(): Int {
    return marginTopBottom
  }

  private fun computeBottomMargin(): Int {
    return marginTopBottom
  }

  private fun computeStartMargin(): Int {
    when (spanCount) {
      2 -> {
        when (position % spanCount) {
          0 -> return marginMin
          else -> return marginMax
        }
      }
      3 -> {
        when (position % spanCount) {
          0 -> return marginMax
          1 -> return marginMin
          2 -> return 0
        }
      }
      4 -> {
        when ((position + 1) % spanCount) {
          0 -> return marginMax
          1 -> return marginMin
          2 -> return marginMin / 2
          3 -> return 0
        }
      }
    }
    return 0  // error here?
  }

  private fun computeEndMargin(): Int {
    when (spanCount) {
      2 -> {
        when (position % spanCount) {
          0 -> return marginMax
          else -> return marginMin
        }
      }
      3 -> {
        when (position % spanCount) {
          0 -> return 0
          1 -> return marginMin
          2 -> return marginMax
        }
      }
      4 -> {
        when ((position + 1) % spanCount) {
          0 -> return 0
          1 -> return marginMin / 2
          2 -> return marginMin
          3 -> return marginMax
        }
      }
    }
    return 0 // error here?
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
