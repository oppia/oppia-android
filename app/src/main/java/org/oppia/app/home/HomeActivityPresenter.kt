package org.oppia.app.home

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import org.oppia.app.R
import org.oppia.app.activity.ActivityScope
import org.oppia.app.drawer.NavigationDrawerFragment
import javax.inject.Inject

/** The presenter for [HomeActivity]. */
@ActivityScope
class HomeActivityPresenter @Inject constructor(private val activity: AppCompatActivity) {
  var toolbar: Toolbar? = null
  var navigationDrawerFragment: NavigationDrawerFragment? = null

  fun init(title: String) {
    toolbar = activity.findViewById<View>(R.id.toolbar) as Toolbar?

    activity.setSupportActionBar(toolbar)
    activity.supportActionBar!!.setDisplayShowHomeEnabled(true)
    navigationDrawerFragment =
      activity.supportFragmentManager.findFragmentById(R.id.fragment_navigation_drawer) as NavigationDrawerFragment
    navigationDrawerFragment!!.setUpDrawer(
      activity.findViewById<View>(R.id.drawer_layout) as DrawerLayout,
      toolbar!!
    )
    activity.setTitle(title)
  }

  fun handleOnCreate() {
    activity.setContentView(R.layout.home_activity)
    init(activity.getString(R.string.menu_home))
    if (getHomeFragment() == null) {
      activity.supportFragmentManager.beginTransaction().add(
        R.id.home_fragment_placeholder,
        HomeFragment()
      ).commitNow()
    }
  }

  private fun getHomeFragment(): HomeFragment? {
    return activity.supportFragmentManager.findFragmentById(R.id.home_fragment_placeholder) as HomeFragment?
  }
}
