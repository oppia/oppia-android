package org.oppia.android.app.home.promotedlist

import android.content.res.Configuration
import android.content.res.Resources
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import org.oppia.android.R
import org.oppia.android.app.home.HomeItemViewModel
import org.oppia.android.app.home.RouteToRecentlyPlayedListener
import org.oppia.android.app.model.PromotedActivityList
import java.util.Objects

/** [ViewModel] for the promoted story list displayed in [HomeFragment]. */
class PromotedStoryListViewModel(
  private val activity: AppCompatActivity,
  val promotedStoryList: List<PromotedStoryViewModel>,
  private val promotedActivityList: PromotedActivityList
) : HomeItemViewModel() {
  private val routeToRecentlyPlayedListener = activity as RouteToRecentlyPlayedListener
  private val promotedStoryListLimit = activity.resources.getInteger(
    R.integer.promoted_story_list_limit
  )
  /** Returns the padding placed at the end of the promoted stories list based on the number of promoted stories. */
  val endPadding =
    if (promotedStoryList.size > 1)
      activity.resources.getDimensionPixelSize(R.dimen.home_padding_end)
    else activity.resources.getDimensionPixelSize(R.dimen.home_padding_start)

  /** Determines and returns the header for the promoted stories. */
  fun getHeader(): String {
    with(promotedActivityList.promotedStoryList) {
      return when {
        suggestedStoryList.isNotEmpty() -> {
          if (recentlyPlayedStoryList.isEmpty() && olderPlayedStoryList.isEmpty()) {
            activity.getString(R.string.recommended_stories)
          } else
            activity.getString(R.string.stories_for_you)
        }
        recentlyPlayedStoryList.isNotEmpty() -> {
          activity.getString(R.string.recently_played_stories)
        }
        else -> {
          activity.getString(R.string.last_played_stories)
        }
      }
    }
  }

  /** Returns the visibility for the "View All" button. */
  fun getViewAllButtonVisibility(): Int {
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
    } else {
      return if (promotedStoryList.size > promotedStoryListLimit - 1) {
        View.VISIBLE
      } else {
        View.INVISIBLE
      }
    }
    return View.VISIBLE
  }

  fun clickOnViewAll() {
    routeToRecentlyPlayedListener.routeToRecentlyPlayed()
  }

  // Overriding equals is needed so that DataProvider combine functions used in the HomeViewModel
  // will only rebind when the actual data in the data list changes, rather than when the ViewModel
  // object changes.
  override fun equals(other: Any?): Boolean {
    return other is PromotedStoryListViewModel && other.promotedStoryList == this.promotedStoryList
  }

  override fun hashCode() = Objects.hash(promotedStoryList)
}
