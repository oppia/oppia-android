package org.oppia.app.home.topiclist

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import org.oppia.app.IntentFactoryShimInterface
import org.oppia.app.home.HomeItemViewModel
import org.oppia.app.home.RouteToRecentlyPlayedListener
import javax.inject.Inject

/** [ViewModel] promoted story list in [HomeFragment]. */
class PromotedStoryListViewModel(
  private val activity: AppCompatActivity,
  private val internalProfileId: Int,
  private val intentFactoryShimInterface: IntentFactoryShimInterface
) :
  HomeItemViewModel(),
  RouteToRecentlyPlayedListener {

  fun clickOnViewAll() {
    routeToRecentlyPlayed()
  }

  override fun routeToRecentlyPlayed() {
    val intent = intentFactoryShimInterface.createRecentlyPlayedActivityIntent(
      activity.applicationContext,
      internalProfileId
    )
    activity.startActivity(intent)
  }
}
