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
  private var toolbar: Toolbar? = null
  private var navigationDrawerFragment: NavigationDrawerFragment? = null

  fun handleOnCreate() {
    activity.setContentView(R.layout.home_activity)
    setUpNavigationDrawer(activity.getString(R.string.menu_help))
    activity.supportFragmentManager.beginTransaction().add(
      R.id.home_fragment_placeholder,
      HelpFragment()
    ).commitNow()
  }

  fun setUpNavigationDrawer(title: String) {
    toolbar = activity.findViewById<View>(R.id.toolbar) as Toolbar?

    activity.setSupportActionBar(toolbar)
    activity.supportActionBar!!.setDisplayShowHomeEnabled(true)
    navigationDrawerFragment =
      activity.supportFragmentManager.findFragmentById(R.id.fragment_navigation_drawer) as NavigationDrawerFragment
    navigationDrawerFragment!!.navigationDrawerFragmentPresenter.setUpDrawer(
      activity.findViewById<View>(R.id.drawer_layout) as DrawerLayout,
      toolbar!!
    )
    activity.setTitle(title)
  }

}
