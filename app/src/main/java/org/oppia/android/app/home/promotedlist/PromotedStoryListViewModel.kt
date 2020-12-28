package org.oppia.android.app.home.promotedlist

import android.content.res.Configuration
import android.content.res.Resources
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import org.oppia.android.R
import org.oppia.android.app.home.HomeItemViewModel
import org.oppia.android.app.home.RouteToRecentlyPlayedListener
import org.oppia.android.app.model.RecommendedActivityList

/** [ViewModel] for the promoted story list displayed in [HomeFragment]. */
class PromotedStoryListViewModel(
  private val activity: AppCompatActivity,
  private val promotedStoryList: List<PromotedStoryViewModel>,
  private val recommendedActivityList: RecommendedActivityList
) : HomeItemViewModel() {
  private val routeToRecentlyPlayedListener = activity as RouteToRecentlyPlayedListener

  /** Returns the padding placed at the end of the promoted stories list based on the number of promoted stories. */
  val endPadding =
    if (promotedStoryList.size > 1)
      activity.resources.getDimensionPixelSize(R.dimen.home_padding_end)
    else activity.resources.getDimensionPixelSize(R.dimen.home_padding_start)

  /** Determines and returns the visibility for the "View All" button. */
  fun getHeader(): String {
    recommendedActivityList.recommendedStoryList.let {
      return when {
        it.suggestedStoryCount != 0 -> {
          if (it.recentlyPlayedStoryCount != 0 || it.olderPlayedStoryCount != 0) {
            activity.getString(R.string.stories_for_you)
          } else
            activity.getString(R.string.recommended_stories)
        }
        it.recentlyPlayedStoryCount != 0 -> {
          activity.getString(R.string.recently_played_stories)
        }
        else -> {
          activity.getString(R.string.last_played_stories)
        }
      }
    }
  }

  /**
   * Determines and returns the visibility for the "View All" button.
   */
  fun getButtonVisibility(): Int {
    recommendedActivityList.recommendedStoryList.let {
      if (it.suggestedStoryCount != 0) {
        return if (it.recentlyPlayedStoryCount != 0 || it.olderPlayedStoryCount != 0) {
          View.VISIBLE
        } else
          View.INVISIBLE
      }
    }
    if (activity.resources.getBoolean(R.bool.isTablet)) {
      when (Resources.getSystem().configuration.orientation) {
        Configuration.ORIENTATION_PORTRAIT -> {
          return if (promotedStoryList.size > 2) View.VISIBLE else View.INVISIBLE
        }
        Configuration.ORIENTATION_LANDSCAPE -> {
          return if (promotedStoryList.size > 3) View.VISIBLE else View.INVISIBLE
        }
        else -> View.VISIBLE
      }
    }
    return View.VISIBLE
  }

  fun clickOnViewAll() {
    routeToRecentlyPlayedListener.routeToRecentlyPlayed()
  }
}
