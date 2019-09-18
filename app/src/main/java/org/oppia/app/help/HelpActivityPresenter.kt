package org.oppia.app.help

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import org.oppia.app.R
import org.oppia.app.activity.ActivityScope
import org.oppia.app.drawer.NavigationDrawerFragment
import javax.inject.Inject

/** The controller for [HelpActivityPresenter]. */
@ActivityScope
class HelpActivityPresenter @Inject constructor(private val activity: AppCompatActivity) {
  var toolbar: Toolbar? = null
  var navigationDrawerFragment: NavigationDrawerFragment? = null

  fun init(title: String) {
    toolbar = activity.findViewById<View>(R.id.toolbar) as Toolbar?

    activity.setSupportActionBar(toolbar)
    activity.supportActionBar!!.setDisplayShowHomeEnabled(true)
    navigationDrawerFragment =
      activity.supportFragmentManager.findFragmentById(R.id.fragment_navigation_drawer) as NavigationDrawerFragment
    navigationDrawerFragment!!.setUpDrawer(
      R.id.fragment_navigation_drawer,
      activity.findViewById<View>(R.id.drawer_layout) as DrawerLayout,
      toolbar!!
    )
    activity.setTitle(title)
  }

  fun handleOnCreate() {
    activity.setContentView(R.layout.home_activity)
    init(activity.getString(R.string.menu_help))
    activity.supportFragmentManager.beginTransaction().add(
      R.id.home_fragment_placeholder,
      HelpFragment()
    ).commitNow()
  }
}
