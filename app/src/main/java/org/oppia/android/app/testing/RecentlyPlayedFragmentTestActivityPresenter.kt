package org.oppia.android.app.testing

import androidx.appcompat.app.AppCompatActivity
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityScope
import org.oppia.android.app.home.recentlyplayed.RecentlyPlayedFragment
import org.oppia.android.app.profile.ProfileChooserFragment
import org.oppia.android.domain.profile.ProfileManagementController
import javax.inject.Inject

/** The presenter for [RecentlyPlayedFragmentTestActivity]. */
@ActivityScope
class RecentlyPlayedFragmentTestActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity
) {
  /** Adds [ProfileChooserFragment] to view. */
  fun handleOnCreate(internalProfileId: Int) {
    activity.setContentView(R.layout.recently_played_test_activity)
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
