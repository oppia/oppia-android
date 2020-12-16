package org.oppia.android.app.home.promotedlist

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import org.oppia.android.R
import org.oppia.android.app.home.HomeItemViewModel
import org.oppia.android.app.home.RouteToRecentlyPlayedListener
import org.oppia.android.app.shim.IntentFactoryShim

/** [ViewModel] promoted story list in [HomeFragment]. */
class PromotedStoryListViewModel(
  private val activity: AppCompatActivity,
  private val internalProfileId: Int,
  private val intentFactoryShim: IntentFactoryShim,
  val promotedStoryList: List<PromotedStoryViewModel>
) :
  HomeItemViewModel(),
  RouteToRecentlyPlayedListener {

  /**
   * Returns an [Int] for the padding placed at the start of the promoted stories list layout displayed on the
   * home activity.
   */
  fun getStartPadding(): Int {
    return activity.resources.getDimensionPixelSize(R.dimen.home_padding_start)
  }

  /**
   * Returns an [Int] for the padding placed at the end of the promoted stories list layout displayed on the
   * home activity, centering the story if there is only one promoted story.
   */
  fun getEndPadding(): Int {
    return if (promotedStoryList.size > 1) {
      activity.resources.getDimensionPixelSize(R.dimen.home_padding_end)
    } else {
      getStartPadding()
    }
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
