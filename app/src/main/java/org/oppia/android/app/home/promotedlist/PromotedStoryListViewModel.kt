package org.oppia.android.app.home.promotedlist

import android.content.res.Configuration
import android.content.res.Resources
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import org.oppia.android.R
import org.oppia.android.app.home.HomeItemViewModel
import org.oppia.android.app.home.RouteToRecentlyPlayedListener
import org.oppia.android.app.model.RecommendedActivityList
import org.oppia.android.app.shim.IntentFactoryShim

/** [ViewModel] for the promoted story list displayed in [HomeFragment]. */
class PromotedStoryListViewModel(
  private val activity: AppCompatActivity,
  private val internalProfileId: Int,
  private val intentFactoryShim: IntentFactoryShim,
  val promotedStoryList: List<PromotedStoryViewModel>,
  val recommendedActivityList: RecommendedActivityList
) : HomeItemViewModel(){
  private val routeToRecentlyPlayedListener = activity as RouteToRecentlyPlayedListener

  // TODO(#2297): Update this span count and move to values/integers.xml once behavior is clarified
  private val promotedStoriesTabletSpanCount: Int =
    if (Resources.getSystem().configuration.orientation == Configuration.ORIENTATION_PORTRAIT) 2
    else 3

  /** Returns the padding placed at the end of the promoted stories list based on the number of promoted stories. */
  val endPadding =
    if (promotedStoryList.size > 1)
      activity.resources.getDimensionPixelSize(R.dimen.home_padding_end)
    else activity.resources.getDimensionPixelSize(R.dimen.home_padding_start)

  /** Determines and returns the visibility for the "View All" button. */
  fun getHeader(): String {
    return when {
      recommendedActivityList.recommendedStoryList.suggestedStoryCount != 0 -> {
        if (recommendedActivityList.recommendedStoryList.recentlyPlayedStoryCount != 0
          || recommendedActivityList.recommendedStoryList.olderPlayedStoryCount != 0
        ) {
          activity.getString(R.string.stories_for_you)
        } else
          activity.getString(R.string.recommended_stories)
      }
      recommendedActivityList.recommendedStoryList.recentlyPlayedStoryCount != 0 -> {
        activity.getString(R.string.recently_played_stories)
      }
      else -> {
        activity.getString(R.string.last_played_stories)
      }
    }
  }

  /**
   * Determines and returns the visibility for the "View All" button.
   */
  fun getButtonVisibility(): Int {
    when {
      recommendedActivityList.recommendedStoryList.suggestedStoryCount != 0 -> {
        return if (recommendedActivityList.recommendedStoryList.recentlyPlayedStoryCount != 0
          || recommendedActivityList.recommendedStoryList.olderPlayedStoryCount != 0
        ) {
          View.VISIBLE
        } else
          View.INVISIBLE
      }
      activity.resources.getBoolean(R.bool.isTablet) -> {
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
    }
    return View.VISIBLE
  }

  fun clickOnViewAll() {
    routeToRecentlyPlayedListener.routeToRecentlyPlayed()
  }
}
