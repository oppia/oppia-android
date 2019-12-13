package org.oppia.app.help

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import org.oppia.app.R
import org.oppia.app.activity.ActivityScope
import org.oppia.app.drawer.NavigationDrawerFragment
import javax.inject.Inject

/** The presenter for [HelpActivity]. */
@ActivityScope
class HelpActivityPresenter @Inject constructor(private val activity: AppCompatActivity) {
  private var navigationDrawerFragment: NavigationDrawerFragment? = null

  fun handleOnCreate() {
    activity.setContentView(R.layout.home_activity)
    setUpNavigationDrawer()
    activity.supportFragmentManager.beginTransaction().add(
      R.id.home_fragment_placeholder,
      HelpFragment()
    ).commitNow()
  }

  private fun setUpNavigationDrawer() {
    val toolbar = activity.findViewById<View>(R.id.toolbar) as Toolbar
    activity.setSupportActionBar(toolbar)
    activity.supportActionBar!!.setDisplayShowHomeEnabled(true)
    navigationDrawerFragment =
      activity.supportFragmentManager.findFragmentById(R.id.fragment_navigation_drawer) as NavigationDrawerFragment
    navigationDrawerFragment!!.setUpDrawer(
      activity.findViewById<View>(R.id.drawer_layout) as DrawerLayout,
      toolbar, R.id.nav_help
    )
  }
}
