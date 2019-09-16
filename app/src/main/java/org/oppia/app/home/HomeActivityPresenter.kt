package org.oppia.app.home

import androidx.appcompat.app.AppCompatActivity
import org.oppia.app.R
import org.oppia.app.activity.ActivityScope
import javax.inject.Inject

/** The presenter for [HomeActivity]. */
@ActivityScope
class HomeActivityPresenter @Inject constructor(private val activity: AppCompatActivity) {
  fun handleOnCreate() {
    activity.setContentView(R.layout.home_activity)
    if (getHomeFragment() == null) {
      activity.supportFragmentManager.beginTransaction().add(
        R.id.home_fragment_placeholder,
        HomeFragment()
      ).commitNow()
    }

    activity.supportActionBar?.setTitle(R.string.home_activity_name)
  }

  private fun getHomeFragment(): HomeFragment? {
    return activity.supportFragmentManager.findFragmentById(R.id.home_fragment_placeholder) as HomeFragment?
  }
}
