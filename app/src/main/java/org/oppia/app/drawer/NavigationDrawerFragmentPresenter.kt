package org.oppia.app.drawer

import android.content.Intent
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
import com.google.android.material.navigation.NavigationView
import org.oppia.app.R
import org.oppia.app.databinding.DrawerFragmentBinding
import org.oppia.app.databinding.NavHeaderNavigationDrawerBinding
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.help.HelpActivity
import org.oppia.app.home.HomeActivity
import org.oppia.app.model.Profile
import org.oppia.app.model.ProfileId
import org.oppia.domain.profile.ProfileManagementController
import org.oppia.util.data.AsyncResult
import org.oppia.util.logging.Logger
import javax.inject.Inject

/** The presenter for [NavigationDrawerFragment]. */
@FragmentScope
class NavigationDrawerFragmentPresenter @Inject constructor(
  private val fragment: Fragment,
  private val profileManagementController: ProfileManagementController,
  private val logger: Logger
) : NavigationView.OnNavigationItemSelectedListener {
  private lateinit var navView: NavigationView
  private lateinit var drawerToggle: ActionBarDrawerToggle
  private lateinit var drawerLayout: DrawerLayout
  private var previousMenuItemId: Int? = null
  private lateinit var binding: DrawerFragmentBinding
  private lateinit var navHeaderMainBinding: NavHeaderNavigationDrawerBinding
  private lateinit var profileId: ProfileId

  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
    binding = DrawerFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)
    navView = binding.fragmentDrawerNavView
    binding.fragmentDrawerNavView.setNavigationItemSelectedListener(this)

    fragment.setHasOptionsMenu(true)
    profileId = profileManagementController.getCurrentProfileId()
    navHeaderMainBinding =
      DataBindingUtil.inflate(inflater, R.layout.nav_header_navigation_drawer, navView, true)
    navView.setNavigationItemSelectedListener(this)

    subscribeToProfileLiveData()

    return binding.root
  }

  private val profileLiveData: LiveData<Profile> by lazy {
    getProfileData()
  }

  private fun getProfileData(): LiveData<Profile> {
    return Transformations.map(profileManagementController.getProfile(profileId), ::processGetProfileResult)
  }

  private fun subscribeToProfileLiveData() {
    profileLiveData.observe(fragment, Observer<Profile> { result ->
      navHeaderMainBinding.navNameTextview.text = result.name
    })
  }

  private fun processGetProfileResult(profileResult: AsyncResult<Profile>): Profile {
    if (profileResult.isFailure()) {
      logger.e("NavigationDrawerFragmentPresenter", "Failed to retrieve profile", profileResult.getErrorOrNull()!!)
    }
    return profileResult.getOrDefault(Profile.getDefaultInstance())
  }

  private fun openActivityByMenuItemId(menuItemId: Int) {
    if (previousMenuItemId != menuItemId && menuItemId != 0) {
      val intent = when (NavigationDrawerItem.valueFromNavId(menuItemId)) {
        NavigationDrawerItem.HOME -> {
          Intent(fragment.activity, HomeActivity::class.java)
        }
        NavigationDrawerItem.HELP -> {
          Intent(fragment.activity, HelpActivity::class.java)
        }
      }
      fragment.activity!!.startActivity(intent)
      fragment.activity!!.finish()
    } else {
      drawerLayout.closeDrawers()
    }
  }

  /**
   * Initializes the navigation drawer for the specified [DrawerLayout] and [Toolbar], which the host activity is
   * expected to provide. The [menuItemId] corresponds to the menu ID of the current activity, for navigation purposes.
   */
  fun setUpDrawer(drawerLayout: DrawerLayout, toolbar: Toolbar, menuItemId: Int) {
    when (NavigationDrawerItem.valueFromNavId(menuItemId)) {
      NavigationDrawerItem.HOME -> {
        navView.menu.getItem(NavigationDrawerItem.HOME.ordinal).isChecked = true
      }
      NavigationDrawerItem.HELP -> {
        navView.menu.getItem(NavigationDrawerItem.HELP.ordinal).isChecked = true
      }
    }
    this.drawerLayout = drawerLayout
    previousMenuItemId = menuItemId
    drawerToggle = object : ActionBarDrawerToggle(
      fragment.activity,
      drawerLayout,
      toolbar,
      R.string.drawer_open_content_description,
      R.string.drawer_close_content_description
    ) {
      override fun onDrawerOpened(drawerView: View) {
        super.onDrawerOpened(drawerView)
        fragment.activity!!.invalidateOptionsMenu()
      }

      override fun onDrawerClosed(drawerView: View) {
        super.onDrawerClosed(drawerView)
        fragment.activity!!.invalidateOptionsMenu()
      }
    }
    drawerLayout.setDrawerListener(drawerToggle)
    /* Synchronize the state of the drawer indicator/affordance with the linked [drawerLayout]. */
    drawerLayout.post { drawerToggle.syncState() }
  }

  override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
    openActivityByMenuItemId(menuItem.itemId)
    return true
  }
}
