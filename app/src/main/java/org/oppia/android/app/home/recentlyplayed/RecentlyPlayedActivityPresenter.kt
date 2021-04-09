package org.oppia.android.app.home.recentlyplayed

import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityScope
import org.oppia.android.databinding.RecentlyPlayedActivityBinding
import javax.inject.Inject

/** The presenter for [RecentlyPlayedActivity]. */
@ActivityScope
class RecentlyPlayedActivityPresenter @Inject constructor(private val activity: AppCompatActivity) {
  fun handleOnCreate(internalProfileId: Int, recentlyPlayedTitleEnum: RecentlyPlayedTitleEnum) {
    activity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
    activity.supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_24dp)
    activity.title = recentlyPlayedTitleEnum.getTitleFromStringRes(activity)
    val binding =
      DataBindingUtil.setContentView<RecentlyPlayedActivityBinding>(
        activity,
        R.layout.recently_played_activity
      )

    binding.recentlyPlayedToolbar.setNavigationOnClickListener {
      (activity as RecentlyPlayedActivity).finish()
    }

    binding.recentlyPlayedToolbar.title = recentlyPlayedTitleEnum.getTitleFromStringRes(activity)
    if (getRecentlyPlayedFragment() == null) {
      activity.supportFragmentManager.beginTransaction().add(
        R.id.recently_played_fragment_placeholder,
        RecentlyPlayedFragment.newInstance(internalProfileId),
        RecentlyPlayedFragment.TAG_RECENTLY_PLAYED_FRAGMENT
      ).commitNow()
    }
  }

  private fun getRecentlyPlayedFragment(): RecentlyPlayedFragment? {
    return activity.supportFragmentManager.findFragmentById(
      R.id.recently_played_fragment_placeholder
    ) as RecentlyPlayedFragment?
  }
}
