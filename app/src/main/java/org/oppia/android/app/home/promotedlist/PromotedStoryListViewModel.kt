package org.oppia.android.app.home.promotedlist

import android.content.res.Configuration
import android.content.res.Resources
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import java.util.*
import org.oppia.android.R
import org.oppia.android.app.home.HomeItemViewModel
import org.oppia.android.app.home.RouteToRecentlyPlayedListener
import org.oppia.android.app.model.PromotedActivityList
import org.oppia.android.app.model.RecentlyPlayedActivityTitle
import org.oppia.android.app.translation.AppLanguageResourceHandler

/** [ViewModel] for the promoted story list displayed in [HomeFragment]. */
class PromotedStoryListViewModel(
  private val activity: AppCompatActivity,
  val promotedStoryList: List<PromotedStoryViewModel>,
  private val promotedActivityList: PromotedActivityList,
  private val resourceHandler: AppLanguageResourceHandler
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
            resourceHandler.getStringInLocale(R.string.recommended_stories)
          } else
            resourceHandler.getStringInLocale(R.string.stories_for_you)
        }
        recentlyPlayedStoryList.isNotEmpty() -> {
          resourceHandler.getStringInLocale(R.string.recently_played_stories)
        }
        else -> {
          resourceHandler.getStringInLocale(R.string.last_played_stories)
        }
      }
    }
  }

  fun getRecentlyPlayedActivityTitle(): RecentlyPlayedActivityTitle {
    with(promotedActivityList.promotedStoryList) {
      return when {
        suggestedStoryList.isNotEmpty() -> {
          if (recentlyPlayedStoryList.isEmpty() && olderPlayedStoryList.isEmpty()) {
            RecentlyPlayedActivityTitle.RECOMMENDED_STORIES
          } else
            RecentlyPlayedActivityTitle.STORIES_FOR_YOU
        }
        recentlyPlayedStoryList.isNotEmpty() -> {
          RecentlyPlayedActivityTitle.RECENTLY_PLAYED_STORIES
        }
        else -> {
          RecentlyPlayedActivityTitle.LAST_PLAYED_STORIES
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
    routeToRecentlyPlayedListener.routeToRecentlyPlayed(getRecentlyPlayedActivityTitle())
  }

  // Overriding equals is needed so that DataProvider combine functions used in the HomeViewModel
  // will only rebind when the actual data in the data list changes, rather than when the ViewModel
  // object changes.
  override fun equals(other: Any?): Boolean {
    return other is PromotedStoryListViewModel && other.promotedStoryList == this.promotedStoryList
  }

  override fun hashCode() = Objects.hash(promotedStoryList)
}
