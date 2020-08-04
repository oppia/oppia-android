package org.oppia.app.home.topiclist

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import org.oppia.app.home.HomeItemViewModel
import org.oppia.app.home.RouteToRecentlyPlayedListener
//import org.oppia.app.home.recentlyplayed.RecentlyPlayedActivity

/** [ViewModel] promoted story list in [HomeFragment]. */
class PromotedStoryListViewModel(
  private val activity: AppCompatActivity,
  private val internalProfileId: Int
) :
  HomeItemViewModel(),
  RouteToRecentlyPlayedListener {

  fun clickOnViewAll() {
    routeToRecentlyPlayed()
  }

  override fun routeToRecentlyPlayed() {
    activity.startActivity(
      RecentlyPlayedActivity.createRecentlyPlayedActivityIntent(
        activity.applicationContext,
        internalProfileId
      )
    )
  }
}
