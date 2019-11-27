package org.oppia.app.home.topiclist

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import org.oppia.app.home.HomeItemViewModel
import org.oppia.app.home.RouteToContinuePlayingListener
import org.oppia.app.home.continueplaying.ContinuePlayingActivity

/** [ViewModel] promoted story list in [HomeFragment]. */
class PromotedStoryListViewModel(private val activity: AppCompatActivity) : HomeItemViewModel(),
  RouteToContinuePlayingListener {

  fun clickOnViewAll(@Suppress("UNUSED_PARAMETER") v: View) {
    routeToContinuePlaying()
  }

  override fun routeToContinuePlaying() {
    activity.startActivity(ContinuePlayingActivity.createContinuePlayingActivityIntent(activity.applicationContext))
  }
}
