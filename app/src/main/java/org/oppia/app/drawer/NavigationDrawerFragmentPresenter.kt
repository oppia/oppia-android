package org.oppia.app.drawer

import android.content.Intent
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
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
import org.oppia.app.home.KEY_HOME_PROFILE_ID
import org.oppia.app.model.Profile
import org.oppia.app.model.ProfileId
import org.oppia.domain.profile.ProfileManagementController
import org.oppia.util.data.AsyncResult
import org.oppia.util.logging.Logger
import javax.inject.Inject

/** The presenter for [NavigationDrawerFragment]. */
@FragmentScope
class NavigationDrawerFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val fragment: Fragment,
  private val profileManagementController: ProfileManagementController,
  private val logger: Logger
) : NavigationView.OnNavigationItemSelectedListener {
  private lateinit var drawerToggle: ActionBarDrawerToggle
  private lateinit var drawerLayout: DrawerLayout
  private var previousMenuItemId: Int? = null
  private lateinit var binding: DrawerFragmentBinding
  private var internalProfileId: Int = -1
  private lateinit var profileId: ProfileId
  private lateinit var navigationDrawerHeaderViewModel: NavigationDrawerHeaderViewModel

  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
    binding = DrawerFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)
    binding.fragmentDrawerNavView.setNavigationItemSelectedListener(this)

    fragment.setHasOptionsMenu(true)

    internalProfileId = activity.intent.getIntExtra(KEY_HOME_PROFILE_ID, -1)
    profileId = ProfileId.newBuilder().setInternalId(internalProfileId).build()

    navigationDrawerHeaderViewModel = NavigationDrawerHeaderViewModel()

    val headerBinding = NavHeaderNavigationDrawerBinding.inflate(inflater, container, /* attachToRoot= */ false)
    headerBinding.viewModel = navigationDrawerHeaderViewModel
    subscribeToProfileLiveData()

    binding.fragmentDrawerNavView.addHeaderView(headerBinding.root)
    binding.executePendingBindings()

    return binding.root
  }

  private fun getProfileData(): LiveData<Profile> {
    return Transformations.map(profileManagementController.getProfile(profileId), ::processGetProfileResult)
  }

  private fun subscribeToProfileLiveData() {
    getProfileData().observe(fragment, Observer<Profile> {
      navigationDrawerHeaderViewModel.profileName.set(it.name)
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
        binding.fragmentDrawerNavView.menu.getItem(NavigationDrawerItem.HOME.ordinal).isChecked = true
      }
      NavigationDrawerItem.HELP -> {
        binding.fragmentDrawerNavView.menu.getItem(NavigationDrawerItem.HELP.ordinal).isChecked = true
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
