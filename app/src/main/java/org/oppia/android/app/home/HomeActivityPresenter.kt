package org.oppia.android.app.home

import android.view.View
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.forEach
import androidx.drawerlayout.widget.DrawerLayout
import org.oppia.android.app.activity.ActivityScope
import org.oppia.android.app.drawer.NavigationDrawerFragment
import org.oppia.android.app.model.Spotlight
import org.oppia.android.app.spotlight.SpotlightFragment
import org.oppia.android.app.spotlight.SpotlightManager
import org.oppia.android.app.spotlight.SpotlightShape
import org.oppia.android.app.spotlight.SpotlightTarget
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.app.ui.R
import org.oppia.android.app.utility.edgetoedge.EdgeToEdgeHelper
import org.oppia.android.util.platformparameter.EnableEdgeToEdge
import org.oppia.android.util.platformparameter.PlatformParameterValue
import javax.inject.Inject

const val TAG_HOME_FRAGMENT = "HOME_FRAGMENT"

/** The presenter for [HomeActivity]. */
@ActivityScope
class HomeActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val resourceHandler: AppLanguageResourceHandler,
  @EnableEdgeToEdge private val enableEdgeToEdge: PlatformParameterValue<Boolean>
) {
  private var navigationDrawerFragment: NavigationDrawerFragment? = null

  fun handleOnCreate(internalProfileId: Int) {
    if (enableEdgeToEdge.value) {
      EdgeToEdgeHelper.enableEdgeToEdgeDispatch(activity)
    }
    activity.setContentView(R.layout.home_activity)
    setUpNavigationDrawer()
    if (enableEdgeToEdge.value) {
      val toolbar = activity.findViewById<Toolbar>(R.id.home_activity_toolbar)
      EdgeToEdgeHelper.applyToToolbarContainer(
        activity,
        toolbar,
        R.color.component_color_shared_activity_status_bar_color
      )
    }
    if (getSpotlightFragment() == null) {
      activity.supportFragmentManager.beginTransaction().add(
        R.id.home_spotlight_fragment_placeholder,
        SpotlightFragment.newInstance(internalProfileId),
        SpotlightManager.SPOTLIGHT_FRAGMENT_TAG
      ).commitNow()
    }
    requestNavigationDrawerSpotlight()

    if (getHomeFragment() == null) {
      activity.supportFragmentManager.beginTransaction().add(
        R.id.home_fragment_placeholder,
        HomeFragment(),
        TAG_HOME_FRAGMENT
      ).commitNow()
    }
  }

  private fun requestNavigationDrawerSpotlight() {
    val spotlightManager = activity.supportFragmentManager.findFragmentByTag(
      SpotlightManager.SPOTLIGHT_FRAGMENT_TAG
    ) as? SpotlightManager ?: return
    val toolbar = activity.findViewById<Toolbar>(R.id.home_activity_toolbar)
    // ActionBarDrawerToggle installs its navigation button in a posted syncState call. Queue this
    // lookup after that call so that the button exists before trying to use it as an anchor.
    toolbar.post {
      toolbar.forEach { toolbarChild ->
        if (toolbarChild is ImageButton) {
          spotlightManager.requestSpotlightViewWithDelayedLayout(
            SpotlightTarget(
              anchor = toolbarChild,
              hint = resourceHandler.getStringInLocale(
                R.string.home_navigation_drawer_spotlight_hint
              ),
              shape = SpotlightShape.Circle,
              feature = Spotlight.FeatureCase.HOME_NAVIGATION_DRAWER_BUTTON
            )
          )
          return@post
        }
      }
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
}
