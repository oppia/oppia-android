package org.oppia.app.mydownloads

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import org.oppia.app.R
import org.oppia.app.activity.ActivityScope
import org.oppia.app.drawer.NavigationDrawerFragment
import javax.inject.Inject

/** The presenter for [MyDownloadsActivity]. */
@ActivityScope
class MyDownloadsActivityPresenter @Inject constructor(private val activity: AppCompatActivity) {
  private var navigationDrawerFragment: NavigationDrawerFragment? = null

  fun handleOnCreate() {
    activity.setContentView(R.layout.my_downloads_activity)
    setUpNavigationDrawer()
    activity.supportFragmentManager.beginTransaction().add(
      R.id.download_fragment_placeholder,
      MyDownloadsFragment()
    ).commitNow()
  }

  private fun setUpNavigationDrawer() {
    val toolbar = activity.findViewById<View>(R.id.download_activity_toolbar) as Toolbar
    activity.setSupportActionBar(toolbar)
    activity.supportActionBar!!.setDisplayShowHomeEnabled(true)
    navigationDrawerFragment =
      activity.supportFragmentManager.findFragmentById(R.id.my_downloads_fragment_placeholder) as NavigationDrawerFragment
    navigationDrawerFragment!!.setUpDrawer(
      activity.findViewById<View>(R.id.download_activity_drawer_layout) as DrawerLayout,
      toolbar, R.id.nav_my_downloads
    )
  }
}
