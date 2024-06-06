package org.oppia.android.app.classroom

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import org.oppia.android.R
import org.oppia.android.app.drawer.NavigationDrawerFragment
import javax.inject.Inject

const val TAG_CLASSROOM_LIST_FRAGMENT = "CLASSROOM_LIST_FRAGMENT"

class ClassroomListActivityPresenter @Inject constructor(private val activity: AppCompatActivity) {
  private var navigationDrawerFragment: NavigationDrawerFragment? = null

  fun handleOnCreate() {
    activity.setContentView(R.layout.classroom_list_activity)
    setUpNavigationDrawer()
    if (getClassroomListFragment() == null) {
      activity.supportFragmentManager.beginTransaction().add(
        R.id.classroom_list_fragment_placeholder,
        ClassroomListFragment(),
        TAG_CLASSROOM_LIST_FRAGMENT
      ).commitNow()
    }
  }

  fun handleOnRestart() {
    setUpNavigationDrawer()
  }

  private fun setUpNavigationDrawer() {
    val toolbar = activity.findViewById<View>(R.id.classroom_list_activity_toolbar) as Toolbar
    activity.setSupportActionBar(toolbar)
    activity.supportActionBar!!.setDisplayShowHomeEnabled(true)
    navigationDrawerFragment = activity
      .supportFragmentManager
      .findFragmentById(
        R.id.classroom_list_activity_fragment_navigation_drawer
      ) as NavigationDrawerFragment
    navigationDrawerFragment!!.setUpDrawer(
      activity.findViewById<View>(R.id.classroom_list_activity_drawer_layout) as DrawerLayout,
      toolbar, R.id.nav_home
    )
  }

  private fun getClassroomListFragment(): ClassroomListFragment? {
    return activity.supportFragmentManager.findFragmentById(
      R.id.classroom_list_fragment_placeholder
    ) as ClassroomListFragment?
  }
}
