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
import org.oppia.android.app.shim.IntentFactoryShim

/** [ViewModel] for the promoted story list displayed in [HomeFragment]. */
class PromotedStoryListViewModel(
  private val activity: AppCompatActivity,
  private val internalProfileId: Int,
  private val intentFactoryShim: IntentFactoryShim,
  val promotedStoryList: List<PromotedStoryViewModel>,
  val recommendedActivityList: RecommendedActivityList
) : HomeItemViewModel(),
  RouteToRecentlyPlayedListener {

  /**
   * Returns the padding placed at the start of the promoted stories list.
   */
  fun getStartPadding(): Int = activity.resources.getDimensionPixelSize(R.dimen.home_padding_start)

  /**
   * Returns the padding placed at the end of the promoted stories list based on the number of promoted stories.
   */
  fun getEndPadding(): Int {
    return if (promotedStoryList.size > 1) {
      activity.resources.getDimensionPixelSize(R.dimen.home_padding_end)
    } else {
      getStartPadding()
    }
  }

  fun getHeader(): String{
    if (recommendedActivityList.recommendedStoryList.suggestedStoryCount != 0){
      return activity.getString(R.string.recommended_stories)
    }else if (recommendedActivityList.recommendedStoryList.recentlyPlayedStoryCount != 0){
      return activity.getString(R.string.recently_played_stories)
    }else {
      return activity.getString(R.string.last_played_stories)
    }
  }
  /**
   * Determines and returns the visibility for the "View All" button.
   */
  fun getButtonVisibility(): Int {
    if (recommendedActivityList.recommendedStoryList.suggestedStoryCount != 0){
      return View.INVISIBLE
    }else if (activity.resources.getBoolean(R.bool.isTablet)) {
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
