package org.oppia.android.app.home.topiclist

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import org.oppia.android.R
import org.oppia.android.app.home.HomeItemViewModel
import org.oppia.android.app.home.RouteToRecentlyPlayedListener
import org.oppia.android.app.shim.IntentFactoryShim
import javax.inject.Inject

/** [ViewModel] promoted story list in [HomeFragment]. */
class PromotedStoryListViewModel @Inject constructor(
  private val activity: AppCompatActivity,
  private val internalProfileId: Int,
  private val intentFactoryShim: IntentFactoryShim
) :
  HomeItemViewModel(),
  RouteToRecentlyPlayedListener {
//  private val limit = activity.resources.getInteger(R.integer.promoted_story_list_limit)
  val paddingEnd =
    activity.resources.getDimensionPixelSize(R.dimen.home_padding_end)
  val paddingStart =
    activity.resources.getDimensionPixelSize(R.dimen.home_padding_start)

  var promotedStoryList : List<PromotedStoryViewModel> = ArrayList()

  fun setList(storyList: List<PromotedStoryViewModel>) {
    this.promotedStoryList = storyList
  }

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
