package org.oppia.app.home

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import org.oppia.app.R
import org.oppia.app.activity.ActivityScope
import org.oppia.app.drawer.NavigationDrawerFragment
import org.oppia.util.system.OppiaClock
import javax.inject.Inject

const val TAG_HOME_FRAGMENT = "TAG_HOME_FRAGMENT"

/** The presenter for [HomeActivity]. */
@ActivityScope
class HomeActivityPresenter @Inject constructor(private val activity: AppCompatActivity) {
  private var navigationDrawerFragment: NavigationDrawerFragment? = null
  private lateinit var oppiaClock: OppiaClock

  fun handleOnCreate(oppiaClock: OppiaClock) {
    activity.setContentView(R.layout.home_activity)
    setUpNavigationDrawer()
    this.oppiaClock = oppiaClock
    if (getHomeFragment() == null) {
      activity.supportFragmentManager.beginTransaction().add(
        R.id.home_fragment_placeholder,
        HomeFragment(),
        TAG_HOME_FRAGMENT
      ).commitNow()
    }
    setOppiaClockInstance()
  }

  private fun setUpNavigationDrawer() {
    val toolbar = activity.findViewById<View>(R.id.home_activity_toolbar) as Toolbar
    activity.setSupportActionBar(toolbar)
    activity.supportActionBar!!.setDisplayShowHomeEnabled(true)
    navigationDrawerFragment =
      activity.supportFragmentManager.findFragmentById(R.id.home_avtivity_fragment_navigation_drawer) as NavigationDrawerFragment
    navigationDrawerFragment!!.setUpDrawer(
      activity.findViewById<View>(R.id.home_activity_drawer_layout) as DrawerLayout,
      toolbar, R.id.nav_home
    )
  }

  private fun setOppiaClockInstance(){
    val homeFragment =
      activity.supportFragmentManager.findFragmentByTag(TAG_HOME_FRAGMENT) as HomeFragment
    homeFragment.setOppiaClockInstance(oppiaClock)
  }

  private fun getHomeFragment(): HomeFragment? {
    return activity.supportFragmentManager.findFragmentById(R.id.home_fragment_placeholder) as HomeFragment?
  }
}
