package org.oppia.app.testing

import androidx.appcompat.app.AppCompatActivity
import org.oppia.app.R
import org.oppia.app.activity.ActivityScope
import org.oppia.app.home.recentlyplayed.RecentlyPlayedFragment
import javax.inject.Inject

/** The presenter for [ContinuePlayingFragmentTestActivity]. */
@ActivityScope
class ContinuePlayingFragmentTestActivityPresenter @Inject constructor(private val activity: AppCompatActivity) {
  fun handleOnCreate() {
    activity.setContentView(R.layout.recently_played_activity)
    if (getContinuePlayingFragment() == null) {
      activity.supportFragmentManager.beginTransaction().add(
        R.id.recently_played_fragment_placeholder,
        RecentlyPlayedFragment()
      ).commitNow()
    }
  }

  private fun getContinuePlayingFragment(): RecentlyPlayedFragment? {
    return activity.supportFragmentManager.findFragmentById(R.id.recently_played_fragment_placeholder) as RecentlyPlayedFragment?
  }
}
