package org.oppia.android.app.home

import android.os.Build
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.drawerlayout.widget.DrawerLayout
import org.oppia.android.app.activity.ActivityScope
import org.oppia.android.app.drawer.NavigationDrawerFragment
import org.oppia.android.app.spotlight.SpotlightFragment
import org.oppia.android.app.spotlight.SpotlightManager
import org.oppia.android.app.ui.R
import org.oppia.android.util.platformparameter.EnableEdgeToEdge
import org.oppia.android.util.platformparameter.PlatformParameterValue
import javax.inject.Inject

const val TAG_HOME_FRAGMENT = "HOME_FRAGMENT"

/** The presenter for [HomeActivity]. */
@ActivityScope
class HomeActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  @EnableEdgeToEdge private val enableEdgeToEdge: PlatformParameterValue<Boolean>
) {
  private var navigationDrawerFragment: NavigationDrawerFragment? = null

  fun handleOnCreate(internalProfileId: Int) {
    activity.setContentView(R.layout.home_activity)
    setUpNavigationDrawer()
    if (enableEdgeToEdge.value) {
      applyEdgeToEdgeInsets()
    }
    if (getHomeFragment() == null) {
      activity.supportFragmentManager.beginTransaction().add(
        R.id.home_fragment_placeholder,
        HomeFragment(),
        TAG_HOME_FRAGMENT
      ).commitNow()
    }

    if (getSpotlightFragment() == null) {
      activity.supportFragmentManager.beginTransaction().add(
        R.id.home_spotlight_fragment_placeholder,
        SpotlightFragment.newInstance(internalProfileId),
        SpotlightManager.SPOTLIGHT_FRAGMENT_TAG
      ).commitNow()
    }
  }

  private fun setUpNavigationDrawer() {
    val toolbar = activity.findViewById<View>(R.id.home_activity_toolbar) as Toolbar
    activity.setSupportActionBar(toolbar)
    activity.supportActionBar!!.setDisplayShowHomeEnabled(true)
    navigationDrawerFragment = activity
      .supportFragmentManager
      .findFragmentById(R.id.home_activity_fragment_navigation_drawer) as NavigationDrawerFragment
    navigationDrawerFragment!!.setUpDrawer(
      activity.findViewById<View>(R.id.home_activity_drawer_layout) as DrawerLayout,
      toolbar, R.id.nav_home
    )
  }

  private fun getHomeFragment(): HomeFragment? {
    return activity.supportFragmentManager.findFragmentById(
      R.id.home_fragment_placeholder
    ) as HomeFragment?
  }

  private fun getSpotlightFragment(): SpotlightFragment? {
    return activity.supportFragmentManager.findFragmentById(
      R.id.home_spotlight_fragment_placeholder
    ) as? SpotlightFragment
  }

  private fun applyEdgeToEdgeInsets() {
    val toolbar = activity.findViewById<Toolbar>(R.id.home_activity_toolbar)
    val contentLayout = toolbar.parent as android.widget.LinearLayout

    // Add a colored View behind the transparent status bar to restore the darker status bar color.
    val statusBarBackground = View(activity).apply {
      setBackgroundColor(
        androidx.core.content.ContextCompat.getColor(
          activity,
          R.color.component_color_shared_activity_status_bar_color
        )
      )
    }
    contentLayout.addView(statusBarBackground, 0)
    ViewCompat.setOnApplyWindowInsetsListener(statusBarBackground) { view, insets ->
      val systemBars = insets.getInsets(
        WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
      )
      view.layoutParams.height = systemBars.top
      view.requestLayout()
      insets
    }

    val drawerLayout =
      activity.findViewById<DrawerLayout>(R.id.home_activity_drawer_layout)
    ViewCompat.setOnApplyWindowInsetsListener(drawerLayout) { view, insets ->
      val systemBarInsets = insets.getInsets(
        WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
      )
      view.updatePadding(bottom = systemBarInsets.bottom)
      insets
    }

    // Make the navigation bar fully transparent instead of translucent.
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      activity.window.isNavigationBarContrastEnforced = false
    }
  }
}
