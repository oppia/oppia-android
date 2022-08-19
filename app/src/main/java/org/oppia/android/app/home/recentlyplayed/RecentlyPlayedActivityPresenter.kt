package org.oppia.android.app.home.recentlyplayed

import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityScope
import org.oppia.android.app.model.RecentlyPlayedActivityParams
import org.oppia.android.app.model.RecentlyPlayedActivityTitle
import javax.inject.Inject
import org.oppia.android.databinding.RecentlyPlayedActivityBinding

/** The presenter for [RecentlyPlayedActivity]. */
@ActivityScope
class RecentlyPlayedActivityPresenter @Inject constructor(private val activity: AppCompatActivity) {
  fun handleOnCreate(recentlyPlayedActivityParams: RecentlyPlayedActivityParams) {
    activity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
    activity.supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_24dp)
    activity.title = getTitle(recentlyPlayedActivityParams)
    val binding = DataBindingUtil.setContentView<RecentlyPlayedActivityBinding>(
      activity,
      R.layout.recently_played_activity
    )

    binding.recentlyPlayedToolbar.setNavigationOnClickListener {
      (activity as RecentlyPlayedActivity).finish()
    }
    binding.recentlyPlayedToolbar.title = getTitle(recentlyPlayedActivityParams)
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
