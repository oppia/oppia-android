package org.oppia.android.app.home.recentlyplayed

import androidx.appcompat.app.AppCompatActivity
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityScope
import org.oppia.android.app.model.RecentlyPlayedActivityParams
import javax.inject.Inject
import org.oppia.android.app.model.RecentlyPlayedActivityTitle

/** The presenter for [RecentlyPlayedActivity]. */
@ActivityScope
class RecentlyPlayedActivityPresenter @Inject constructor(private val activity: AppCompatActivity) {
  fun handleOnCreate(recentlyPlayedActivityParams: RecentlyPlayedActivityParams) {
    activity.title = getTitle(recentlyPlayedActivityParams)
    activity.setContentView(R.layout.recently_played_activity)
    if (getRecentlyPlayedFragment() == null) {
      activity.supportFragmentManager.beginTransaction().add(
        R.id.recently_played_fragment_placeholder,
        RecentlyPlayedFragment.newInstance(recentlyPlayedActivityParams.profileId.internalId),
        RecentlyPlayedFragment.TAG_RECENTLY_PLAYED_FRAGMENT
      ).commitNow()
    }
  }

  private fun getRecentlyPlayedFragment(): RecentlyPlayedFragment? {
    return activity.supportFragmentManager.findFragmentById(
      R.id.recently_played_fragment_placeholder
    ) as RecentlyPlayedFragment?
  }

  private fun getTitle(
    recentlyPlayedActivityParams: RecentlyPlayedActivityParams
  ): String {
    return when (recentlyPlayedActivityParams.activityTitle) {
      RecentlyPlayedActivityTitle.RECENTLY_PLAYED_STORIES -> {
        activity.getString(R.string.recently_played_activity)
      }
      RecentlyPlayedActivityTitle.STORIES_FOR_YOU -> {
        activity.getString(R.string.stories_for_you)
      }
      else -> {
        activity.getString(R.string.recently_played_activity)
      }
    }
  }
}
