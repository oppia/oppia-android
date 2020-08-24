package org.oppia.app.administratorcontrols

import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import org.oppia.app.R
import org.oppia.app.activity.ActivityScope
import org.oppia.app.administratorcontrols.appversion.AppVersionFragment
import org.oppia.app.drawer.NavigationDrawerFragment
import org.oppia.app.settings.profile.ProfileListFragment
import javax.inject.Inject

/** The presenter for [AdministratorControlsActivity]. */
@ActivityScope
class AdministratorControlsActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity
) {
  private lateinit var navigationDrawerFragment: NavigationDrawerFragment
  private var isMultipane = false

  fun handleOnCreate(extraControlsTitle: String?, lastLoadedFragment: String) {
    activity.setContentView(R.layout.administrator_controls_activity)
    setUpNavigationDrawer()
    val titleTextView =
      activity.findViewById<TextView>(R.id.extra_controls_title)
    if (titleTextView != null) {
      titleTextView.text = extraControlsTitle
    }
    isMultipane =
      activity
      .findViewById<FrameLayout>(
        R.id.administrator_controls_fragment_multipane_placeholder
      ) != null
    val previousFragment = getAdministratorControlsFragment()
    if (previousFragment != null) {
      activity.supportFragmentManager.beginTransaction().remove(previousFragment).commitNow()
    }
    activity.supportFragmentManager.beginTransaction().add(
      R.id.administrator_controls_fragment_placeholder,
      AdministratorControlsFragment.newInstance(isMultipane)
    ).commitNow()
    if (isMultipane) {
      when (lastLoadedFragment) {
        PROFILE_LIST_FRAGMENT -> (activity as AdministratorControlsActivity).loadProfileList()
        APP_VERSION_FRAGMENT -> (activity as AdministratorControlsActivity).loadAppVersion()
      }
    }
  }

  private fun setUpNavigationDrawer() {
    val toolbar =
      activity.findViewById<View>(R.id.administrator_controls_activity_toolbar) as Toolbar
    activity.setSupportActionBar(toolbar)
    activity.supportActionBar!!.setDisplayShowHomeEnabled(true)
    navigationDrawerFragment = activity
      .supportFragmentManager
      .findFragmentById(
        R.id.administrator_controls_activity_fragment_navigation_drawer
      ) as NavigationDrawerFragment
    navigationDrawerFragment.setUpDrawer(
      activity
        .findViewById<View>(
          R.id.administrator_controls_activity_drawer_layout
        ) as DrawerLayout,
      toolbar, /* menuItemId= */ 0
    )
  }

  private fun getAdministratorControlsFragment(): AdministratorControlsFragment? {
    return activity
      .supportFragmentManager
      .findFragmentById(
        R.id.administrator_controls_fragment_placeholder
      ) as AdministratorControlsFragment?
  }

  fun loadProfileList() {
    activity.supportFragmentManager.beginTransaction().add(
      R.id.administrator_controls_fragment_multipane_placeholder,
      ProfileListFragment.newInstance(isMultipane)
    ).commitNow()
  }

  fun loadAppVersion() {
    activity.supportFragmentManager.beginTransaction().add(
      R.id.administrator_controls_fragment_multipane_placeholder,
      AppVersionFragment()
    ).commitNow()
  }

  fun setExtraControlsTitle(title: String) {
    activity.findViewById<TextView>(R.id.extra_controls_title).text = title
  }
}
