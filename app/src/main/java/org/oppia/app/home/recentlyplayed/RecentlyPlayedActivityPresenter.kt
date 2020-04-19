package org.oppia.app.home.recentlyplayed

import androidx.appcompat.app.AppCompatActivity
import org.oppia.app.R
import org.oppia.app.activity.ActivityScope
import javax.inject.Inject

/** The presenter for [RecentlyPlayedActivity]. */
@ActivityScope
class RecentlyPlayedActivityPresenter @Inject constructor(private val activity: AppCompatActivity) {
  fun handleOnCreate(internalProfileId: Int) {
    activity.setContentView(R.layout.recently_played_activity)
    if (getRecentlyPlayedFragment() == null) {
      activity.supportFragmentManager.beginTransaction().add(
        R.id.recently_played_activity_recently_played_fragment_placeholder,
        RecentlyPlayedFragment.newInstance(internalProfileId)
      ).commitNow()
    }
  }

  private fun getRecentlyPlayedFragment(): RecentlyPlayedFragment? {
    return activity.supportFragmentManager.findFragmentById(R.id.recently_played_activity_recently_played_fragment_placeholder) as RecentlyPlayedFragment?
  }
}
