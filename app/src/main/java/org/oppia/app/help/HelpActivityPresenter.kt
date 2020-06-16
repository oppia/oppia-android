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
  private lateinit var navigationDrawerFragment: NavigationDrawerFragment

  fun handleOnCreate() {
    activity.setContentView(R.layout.help_activity)
    setUpNavigationDrawer()
    if (getHelpFragment() == null) {
      activity.supportFragmentManager.beginTransaction().add(
        R.id.help_fragment_placeholder,
        HelpFragment()
      ).commitNow()
    }
  }

  private fun setUpNavigationDrawer() {
    val toolbar = activity.findViewById<View>(R.id.help_activity_toolbar) as Toolbar
    activity.setSupportActionBar(toolbar)
    activity.supportActionBar!!.setDisplayShowHomeEnabled(true)
    navigationDrawerFragment = activity
      .supportFragmentManager
      .findFragmentById(
        R.id.help_activity_fragment_navigation_drawer
      ) as NavigationDrawerFragment
    navigationDrawerFragment.setUpDrawer(
      activity.findViewById<View>(R.id.help_activity_drawer_layout) as DrawerLayout,
      toolbar, R.id.nav_help
    )
  }

  private fun getHelpFragment(): HelpFragment? {
    return activity
      .supportFragmentManager
      .findFragmentById(R.id.help_fragment_placeholder) as HelpFragment?
  }
}
