package org.oppia.android.app.home.recentlyplayed

import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityScope
import org.oppia.android.app.model.RecentlyPlayedActivityIntentExtras
import org.oppia.android.app.model.RecentlyPlayedActivityTitle
import org.oppia.android.databinding.RecentlyPlayedActivityBinding
import javax.inject.Inject

/** The presenter for [RecentlyPlayedActivity]. */
@ActivityScope
class RecentlyPlayedActivityPresenter @Inject constructor(private val activity: AppCompatActivity) {
  fun handleOnCreate(recentlyPlayedActivityIntentExtras: RecentlyPlayedActivityIntentExtras) {
    activity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
    activity.supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_24dp)
    activity.title = getTitle(recentlyPlayedActivityIntentExtras)
    val binding =
      DataBindingUtil.setContentView<RecentlyPlayedActivityBinding>(
        activity,
        R.layout.recently_played_activity
      )

    binding.recentlyPlayedToolbar.setNavigationOnClickListener {
      (activity as RecentlyPlayedActivity).finish()
    }

    binding.recentlyPlayedToolbar.title = getTitle(recentlyPlayedActivityIntentExtras)
    if (getRecentlyPlayedFragment() == null) {
      activity.supportFragmentManager.beginTransaction().add(
        R.id.recently_played_fragment_placeholder,
        RecentlyPlayedFragment.newInstance(recentlyPlayedActivityIntentExtras.profileId.internalId),
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
    recentlyPlayedActivityIntentExtras: RecentlyPlayedActivityIntentExtras
  ): String {
    return when (recentlyPlayedActivityIntentExtras.activityTitle) {
      RecentlyPlayedActivityTitle.RECENTLY_PLAYED_STORIES -> {
        activity.getString(R.string.recently_played_activity_title)
      }
      RecentlyPlayedActivityTitle.STORIES_FOR_YOU -> {
        activity.getString(R.string.stories_for_you)
      }
      else -> {
        activity.getString(R.string.recently_played_activity_title)
      }
    }
  }
}
