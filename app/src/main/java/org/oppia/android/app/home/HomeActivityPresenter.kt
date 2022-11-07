package org.oppia.android.app.home

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityScope
import org.oppia.android.app.drawer.NavigationDrawerFragment
import javax.inject.Inject
import org.oppia.android.app.spotlight.SpotlightFragment
import org.oppia.android.app.topic.PROFILE_ID_ARGUMENT_KEY
import org.oppia.android.app.topic.SPOTLIGHT_FRAGMENT_TAG

const val TAG_HOME_FRAGMENT = "HOME_FRAGMENT"

/** The presenter for [HomeActivity]. */
@ActivityScope
class HomeActivityPresenter @Inject constructor(private val activity: AppCompatActivity) {
  private var navigationDrawerFragment: NavigationDrawerFragment? = null

  fun handleOnCreate(internalProfileId: Int) {
    activity.setContentView(R.layout.home_activity)
    setUpNavigationDrawer()
    if (getHomeFragment() == null) {
      activity.supportFragmentManager.beginTransaction().add(
        R.id.home_fragment_placeholder,
        HomeFragment(),
        TAG_HOME_FRAGMENT
      ).commitNow()
    }

    val spotlightFragment = SpotlightFragment()
    val args = Bundle()
    args.putInt(PROFILE_ID_ARGUMENT_KEY, internalProfileId)
    spotlightFragment.arguments = args
    activity.supportFragmentManager.beginTransaction().add(
      R.id.home_spotlight_fragment_placeholder,
      spotlightFragment, SPOTLIGHT_FRAGMENT_TAG
    ).commitNow()
  }

  fun handleOnRestart() {
    setUpNavigationDrawer()
  }

  private fun setUpNavigationDrawer() {
    val toolbar = activity.findViewById<View>(R.id.home_activity_toolbar) as Toolbar
    activity.setSupportActionBar(toolbar)
    activity.supportActionBar!!.setDisplayShowHomeEnabled(true)
    navigationDrawerFragment = activity
      .supportFragmentManager
      .findFragmentById(R.id.home_activity_fragment_navigation_drawer) as NavigationDrawerFragment
    navigationDrawerFragment!!.setUpDrawer(
      activity.findViewById<View>(R.id.home_activity_drawer_layout) as DrawerLayout,
      toolbar, R.id.nav_home
    )
  }

  private fun getHomeFragment(): HomeFragment? {
    return activity.supportFragmentManager.findFragmentById(
      R.id.home_fragment_placeholder
    ) as HomeFragment?
  }
}
