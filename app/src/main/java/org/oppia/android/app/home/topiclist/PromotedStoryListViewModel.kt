package org.oppia.android.app.home.topiclist

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import org.oppia.android.R
import org.oppia.android.app.home.HomeItemViewModel
import org.oppia.android.app.home.RouteToRecentlyPlayedListener
import org.oppia.android.app.shim.IntentFactoryShim
import javax.inject.Inject

/** [ViewModel] promoted story list in [HomeFragment]. */
class PromotedStoryListViewModel(
  private val activity: AppCompatActivity,
  private val internalProfileId: Int,
  private val intentFactoryShim: IntentFactoryShim,
  val promotedStoryList: List<PromotedStoryViewModel>
) :
  HomeItemViewModel(),
  RouteToRecentlyPlayedListener {
  val paddingEnd =
    activity.resources.getDimensionPixelSize(R.dimen.home_padding_end)
  val paddingStart =
    activity.resources.getDimensionPixelSize(R.dimen.home_padding_start)

  fun clickOnViewAll() {
    routeToRecentlyPlayed()
  }

  override fun routeToRecentlyPlayed() {
    val intent = intentFactoryShim.createRecentlyPlayedActivityIntent(
      activity.applicationContext,
      internalProfileId
    )
    activity.startActivity(intent)
  }
}
