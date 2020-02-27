package org.oppia.app.drawer

import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
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
import org.oppia.app.model.Profile
import org.oppia.app.model.ProfileId
import org.oppia.app.mydownloads.MyDownloadsActivity
import org.oppia.app.profile.ProfileActivity
import org.oppia.domain.profile.ProfileManagementController
import org.oppia.util.data.AsyncResult
import org.oppia.util.logging.Logger
import javax.inject.Inject

const val KEY_NAVIGATION_PROFILE_ID = "KEY_NAVIGATION_PROFILE_ID"

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
  private lateinit var navigationDrawerFooterViewModel: NavigationDrawerFooterViewModel
  private var routeToAdministratorControlsListener = fragment as RouteToAdministratorControlsListener

  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
    binding = DrawerFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)
    binding.fragmentDrawerNavView.setNavigationItemSelectedListener(this)

    fragment.setHasOptionsMenu(true)

    internalProfileId = activity.intent.getIntExtra(KEY_NAVIGATION_PROFILE_ID, -1)
    profileId = ProfileId.newBuilder().setInternalId(internalProfileId).build()

    navigationDrawerHeaderViewModel = NavigationDrawerHeaderViewModel()
    navigationDrawerFooterViewModel = NavigationDrawerFooterViewModel()

    val headerBinding = NavHeaderNavigationDrawerBinding.inflate(inflater, container, /* attachToRoot= */ false)
    headerBinding.viewModel = navigationDrawerHeaderViewModel
    subscribeToProfileLiveData()

    binding.fragmentDrawerNavView.addHeaderView(headerBinding.root)
    binding.footerViewModel = navigationDrawerFooterViewModel
    binding.executePendingBindings()

    return binding.root
  }

  private fun getProfileData(): LiveData<Profile> {
    return Transformations.map(profileManagementController.getProfile(profileId), ::processGetProfileResult)
  }

  private fun subscribeToProfileLiveData() {
    getProfileData().observe(fragment, Observer<Profile> {
      navigationDrawerHeaderViewModel.profileName.set(it.name)
      navigationDrawerFooterViewModel.isAdmin.set(it.isAdmin)
      binding.administratorControlsLinearLayout.setOnClickListener {
        routeToAdministratorControlsListener.routeToAdministratorControls(internalProfileId)
        drawerLayout.closeDrawers()
      }
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
      when (NavigationDrawerItem.valueFromNavId(menuItemId)) {
        NavigationDrawerItem.HOME -> {
          val intent = HomeActivity.createHomeActivity(activity, internalProfileId)
          fragment.activity!!.startActivity(intent)
          fragment.activity!!.finish()
        }
        NavigationDrawerItem.HELP -> {
          val intent = HelpActivity.createHelpActivityIntent(activity, internalProfileId)
          fragment.activity!!.startActivity(intent)
          fragment.activity!!.finish()
        }
        NavigationDrawerItem.DOWNLOADS -> {
          val intent = MyDownloadsActivity.createMyDownloadsActivityIntent(activity, internalProfileId)
          fragment.activity!!.startActivity(intent)
          fragment.activity!!.finish()
        }
        NavigationDrawerItem.SWITCH_PROFILE -> {
          AlertDialog.Builder(fragment.context!!, R.style.AlertDialogTheme)
            .setMessage(R.string.home_activity_back_dialog_message)
            .setOnCancelListener { dialog ->
              binding.fragmentDrawerNavView.menu.getItem(NavigationDrawerItem.HOME.ordinal).isChecked = true
              drawerLayout.closeDrawers()
              dialog.dismiss()
            }
            .setNegativeButton(R.string.home_activity_back_dialog_cancel) { dialog, _ ->
              binding.fragmentDrawerNavView.menu.getItem(NavigationDrawerItem.HOME.ordinal).isChecked = true
              drawerLayout.closeDrawers()
              dialog.dismiss()
            }
            .setPositiveButton(R.string.home_activity_back_dialog_exit) { _, _ ->
              // TODO(#322): Need to start intent for ProfileActivity to get update. Change to finish when live data bug is fixed.
              val intent = ProfileActivity.createProfileActivity(fragment.context!!)
              fragment.activity!!.startActivity(intent)
            }.create().show()
        }
      }
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
      NavigationDrawerItem.DOWNLOADS -> {
        binding.fragmentDrawerNavView.menu.getItem(NavigationDrawerItem.DOWNLOADS.ordinal).isChecked = true
      }
      NavigationDrawerItem.SWITCH_PROFILE -> {
        binding.fragmentDrawerNavView.menu.getItem(NavigationDrawerItem.SWITCH_PROFILE.ordinal).isChecked = true
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
