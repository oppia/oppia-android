package org.oppia.android.app.classroom

import android.os.Build
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.drawerlayout.widget.DrawerLayout
import org.oppia.android.app.drawer.NavigationDrawerFragment
import org.oppia.android.app.ui.R
import org.oppia.android.util.platformparameter.EnableEdgeToEdge
import org.oppia.android.util.platformparameter.PlatformParameterValue
import javax.inject.Inject

/** Tag for identifying the [ClassroomListFragment] in transactions. */
private const val TAG_CLASSROOM_LIST_FRAGMENT = "CLASSROOM_LIST_FRAGMENT"

/** The presenter for [ClassroomListActivity]. */
class ClassroomListActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  @EnableEdgeToEdge private val enableEdgeToEdge: PlatformParameterValue<Boolean>
) {
  private var navigationDrawerFragment: NavigationDrawerFragment? = null

  /**
   * Handles the creation of the activity. Sets the content view, sets up the navigation drawer,
   * and adds the [ClassroomListFragment] if it's not already added.
   */
  fun handleOnCreate() {
    activity.setContentView(R.layout.classroom_list_activity)
    setUpNavigationDrawer()
    if (enableEdgeToEdge.value) {
      applyEdgeToEdgeInsets()
    }
    if (getClassroomListFragment() == null) {
      activity.supportFragmentManager.beginTransaction().add(
        R.id.classroom_list_fragment_placeholder,
        ClassroomListFragment(),
        TAG_CLASSROOM_LIST_FRAGMENT
      ).commitNow()
    }
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

  private fun applyEdgeToEdgeInsets() {
    val toolbar = activity.findViewById<Toolbar>(
      R.id.classroom_list_activity_toolbar
    )
    val contentLayout = toolbar.parent as android.widget.LinearLayout
    val statusBarBackground = View(activity).apply {
      setBackgroundColor(
        ContextCompat.getColor(
          activity,
          R.color.component_color_shared_activity_status_bar_color
        )
      )
    }
    contentLayout.addView(statusBarBackground, 0)
    ViewCompat.setOnApplyWindowInsetsListener(statusBarBackground) { view, insets ->
      val systemBars = insets.getInsets(
        WindowInsetsCompat.Type.systemBars() or
          WindowInsetsCompat.Type.displayCutout()
      )
      view.layoutParams.height = systemBars.top
      view.requestLayout()
      insets
    }
    val drawerLayout = activity.findViewById<DrawerLayout>(
      R.id.classroom_list_activity_drawer_layout
    )
    ViewCompat.setOnApplyWindowInsetsListener(drawerLayout) { view, insets ->
      val systemBars = insets.getInsets(
        WindowInsetsCompat.Type.systemBars() or
          WindowInsetsCompat.Type.displayCutout()
      )
      view.updatePadding(bottom = systemBars.bottom)
      insets
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      activity.window.isNavigationBarContrastEnforced = false
    }
  }
}
