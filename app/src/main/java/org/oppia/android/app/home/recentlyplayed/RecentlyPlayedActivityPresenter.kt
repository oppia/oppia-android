package org.oppia.android.app.home.recentlyplayed

import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityScope
import org.oppia.android.app.model.RecentlyPlayedActivityParams
import org.oppia.android.app.model.RecentlyPlayedActivityTitle
import org.oppia.android.databinding.RecentlyPlayedActivityBinding
import javax.inject.Inject
import org.oppia.android.app.translation.AppLanguageResourceHandler

/** The presenter for [RecentlyPlayedActivity]. */
@ActivityScope
class RecentlyPlayedActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val resourceHandler: AppLanguageResourceHandler
) {
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

  private fun getTitle(recentlyPlayedActivityParams: RecentlyPlayedActivityParams): String {
    return when (recentlyPlayedActivityParams.activityTitle) {
      RecentlyPlayedActivityTitle.RECENTLY_PLAYED_STORIES -> {
        resourceHandler.getStringInLocale(R.string.recently_played_activity)
      }
      RecentlyPlayedActivityTitle.STORIES_FOR_YOU -> {
        resourceHandler.getStringInLocale(R.string.stories_for_you)
      }
      else -> {
        resourceHandler.getStringInLocale(R.string.recently_played_activity)
      }
    }
  }
}
