package org.oppia.app.administratorcontrols

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import org.oppia.app.R
import org.oppia.app.activity.ActivityScope
import org.oppia.app.drawer.NavigationDrawerFragment
import javax.inject.Inject

/** The presenter for [AdministratorControlsActivity]. */
@ActivityScope
class AdministratorControlsActivityPresenter @Inject constructor(private val activity: AppCompatActivity) {
  private lateinit var navigationDrawerFragment: NavigationDrawerFragment

  fun handleOnCreate() {
    activity.setContentView(R.layout.administrator_controls_activity)
    setUpNavigationDrawer()
    if (getAdministratorControlsFragment() == null) {
      activity.supportFragmentManager.beginTransaction().add(
        R.id.administrator_controls_fragment_placeholder,
        AdministratorControlsFragment()
      ).commitNow()
    }
  }

  private fun setUpNavigationDrawer() {
    val toolbar = activity.findViewById<View>(R.id.administrator_controls_activity_toolbar) as Toolbar
    activity.setSupportActionBar(toolbar)
    activity.supportActionBar!!.setDisplayShowHomeEnabled(true)
    navigationDrawerFragment =
      activity.supportFragmentManager.findFragmentById(R.id.administrator_controls_activity_fragment_navigation_drawer) as NavigationDrawerFragment
    navigationDrawerFragment.setUpDrawer(
      activity.findViewById<View>(R.id.administrator_controls_activity_drawer_layout) as DrawerLayout,
      toolbar, /* menuItemId = */ 0
    )
  }

  private fun getAdministratorControlsFragment(): AdministratorControlsFragment? {
    return activity.supportFragmentManager.findFragmentById(R.id.administrator_controls_fragment_placeholder) as AdministratorControlsFragment?
  }
}
