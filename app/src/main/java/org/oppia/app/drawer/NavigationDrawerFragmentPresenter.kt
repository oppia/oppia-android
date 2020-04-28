package org.oppia.app.drawer

import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.forEach
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
import com.google.android.material.navigation.NavigationView
import org.oppia.app.R
import org.oppia.app.administratorcontrols.AdministratorControlsActivity
import org.oppia.app.databinding.DrawerFragmentBinding
import org.oppia.app.databinding.NavHeaderNavigationDrawerBinding
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.help.HelpActivity
import org.oppia.app.home.HomeActivity
import org.oppia.app.model.CompletedStoryList
import org.oppia.app.model.OngoingTopicList
import org.oppia.app.model.Profile
import org.oppia.app.model.ProfileId
import org.oppia.app.mydownloads.MyDownloadsActivity
import org.oppia.app.options.OptionsActivity
import org.oppia.app.profile.ProfileActivity
import org.oppia.app.viewmodel.ViewModelProvider
import org.oppia.domain.profile.ProfileManagementController
import org.oppia.domain.topic.TopicController
import org.oppia.util.data.AsyncResult
import org.oppia.util.logging.Logger
import org.oppia.util.statusbar.StatusBarColor
import javax.inject.Inject

const val KEY_NAVIGATION_PROFILE_ID = "KEY_NAVIGATION_PROFILE_ID"

/** The presenter for [NavigationDrawerFragment]. */
@FragmentScope
class NavigationDrawerFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val fragment: Fragment,
  private val profileManagementController: ProfileManagementController,
  private val topicController: TopicController,
  private val logger: Logger,
  private val headerViewModelProvider: ViewModelProvider<NavigationDrawerHeaderViewModel>,
  private val footerViewModelProvider: ViewModelProvider<NavigationDrawerFooterViewModel>
) : NavigationView.OnNavigationItemSelectedListener {
  private lateinit var drawerToggle: ActionBarDrawerToggle
  private lateinit var drawerLayout: DrawerLayout
  private lateinit var binding: DrawerFragmentBinding
  private lateinit var profileId: ProfileId
  private var previousMenuItemId: Int? = null
  private var internalProfileId: Int = -1

  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
    binding = DrawerFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)
    binding.fragmentDrawerNavView.setNavigationItemSelectedListener(this)

    fragment.setHasOptionsMenu(true)

    internalProfileId = activity.intent.getIntExtra(KEY_NAVIGATION_PROFILE_ID, -1)
    profileId = ProfileId.newBuilder().setInternalId(internalProfileId).build()

    val headerBinding = NavHeaderNavigationDrawerBinding.inflate(inflater, container, /* attachToRoot= */ false)
    headerBinding.viewModel = getHeaderViewModel()
    subscribeToProfileLiveData()
    subscribeToCompletedStoryListLiveData()
    subscribeToOngoingTopicListLiveData()

    binding.fragmentDrawerNavView.addHeaderView(headerBinding.root)
    binding.footerViewModel = getFooterViewModel()
    binding.executePendingBindings()

    return binding.root
  }

  private fun getProfileData(): LiveData<Profile> {
    return Transformations.map(profileManagementController.getProfile(profileId), ::processGetProfileResult)
  }

  private fun subscribeToProfileLiveData() {
    getProfileData().observe(fragment, Observer<Profile> {
      getHeaderViewModel().profile.set(it)
      getFooterViewModel().isAdmin.set(it.isAdmin)
      binding.administratorControlsLinearLayout.setOnClickListener {
        binding.fragmentDrawerNavView.menu.forEach { menuItem ->
          menuItem.isCheckable = false
        }

        drawerLayout.closeDrawers()
        getFooterViewModel().isAdministratorControlsSelected.set(true)
        val intent =
          AdministratorControlsActivity.createAdministratorControlsActivityIntent(activity, internalProfileId)
        fragment.activity!!.startActivity(intent)
        if (previousMenuItemId != null && NavigationDrawerItem.valueFromNavId(previousMenuItemId!!) != NavigationDrawerItem.HOME) {
          fragment.activity!!.finish()
        }
        drawerLayout.closeDrawers()
      }
    })
  }

  private fun processGetProfileResult(profileResult: AsyncResult<Profile>): Profile {
    if (profileResult.isFailure()) {
      logger.e("NavigationDrawerFragment", "Failed to retrieve profile", profileResult.getErrorOrNull()!!)
    }
    return profileResult.getOrDefault(Profile.getDefaultInstance())
  }

  private fun getCompletedStoryListCount(): LiveData<CompletedStoryList> {
    return Transformations.map(topicController.getCompletedStoryList(profileId), ::processGetCompletedStoryListResult)
  }

  private fun subscribeToCompletedStoryListLiveData() {
    getCompletedStoryListCount().observe(fragment, Observer<CompletedStoryList> {
      getHeaderViewModel().completedStoryCount.set(it.completedStoryCount)
    })
  }

  private fun processGetCompletedStoryListResult(completedStoryListResult: AsyncResult<CompletedStoryList>): CompletedStoryList {
    if (completedStoryListResult.isFailure()) {
      logger.e(
        "NavigationDrawerFragment",
        "Failed to retrieve completed story list",
        completedStoryListResult.getErrorOrNull()!!
      )
    }
    return completedStoryListResult.getOrDefault(CompletedStoryList.getDefaultInstance())
  }

  private fun getOngoingTopicListCount(): LiveData<OngoingTopicList> {
    return Transformations.map(topicController.getOngoingTopicList(profileId), ::processGetOngoingTopicListResult)
  }

  private fun subscribeToOngoingTopicListLiveData() {
    getOngoingTopicListCount().observe(fragment, Observer<OngoingTopicList> {
      getHeaderViewModel().ongoingTopicCount.set(it.topicCount)
    })
  }

  private fun processGetOngoingTopicListResult(ongoingTopicListResult: AsyncResult<OngoingTopicList>): OngoingTopicList {
    if (ongoingTopicListResult.isFailure()) {
      logger.e(
        "NavigationDrawerFragment",
        "Failed to retrieve ongoing topic list",
        ongoingTopicListResult.getErrorOrNull()!!
      )
    }
    return ongoingTopicListResult.getOrDefault(OngoingTopicList.getDefaultInstance())
  }

  private fun openActivityByMenuItemId(menuItemId: Int) {
    getFooterViewModel().isAdministratorControlsSelected.set(false)
    if (previousMenuItemId != menuItemId) {
      when (NavigationDrawerItem.valueFromNavId(menuItemId)) {
        NavigationDrawerItem.HOME -> {
          val intent = HomeActivity.createHomeActivity(activity, internalProfileId)
          fragment.activity!!.startActivity(intent)
          drawerLayout.closeDrawers()
        }
        NavigationDrawerItem.OPTIONS -> {
          val intent = OptionsActivity.createOptionsActivity(activity, internalProfileId)
          fragment.activity!!.startActivity(intent)
          if (checkIfPreviousActivityShouldGetFinished(menuItemId)) {
            fragment.activity!!.finish()
          }
          drawerLayout.closeDrawers()
        }
        NavigationDrawerItem.HELP -> {
          val intent = HelpActivity.createHelpActivityIntent(activity, internalProfileId)
          fragment.activity!!.startActivity(intent)
          if (checkIfPreviousActivityShouldGetFinished(menuItemId)) {
            fragment.activity!!.finish()
          }
          drawerLayout.closeDrawers()
        }
        NavigationDrawerItem.DOWNLOADS -> {
          val intent = MyDownloadsActivity.createMyDownloadsActivityIntent(activity, internalProfileId)
          fragment.activity!!.startActivity(intent)
          if (checkIfPreviousActivityShouldGetFinished(menuItemId)) {
            fragment.activity!!.finish()
          }
          drawerLayout.closeDrawers()
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
    previousMenuItemId = menuItemId
    if (menuItemId != 0) {
      getFooterViewModel().isAdministratorControlsSelected.set(false)
      when (NavigationDrawerItem.valueFromNavId(menuItemId)) {
        NavigationDrawerItem.HOME -> {
          binding.fragmentDrawerNavView.menu.getItem(NavigationDrawerItem.HOME.ordinal).isChecked = true
        }
        NavigationDrawerItem.OPTIONS -> {
          binding.fragmentDrawerNavView.menu.getItem(NavigationDrawerItem.OPTIONS.ordinal).isChecked = true
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
          StatusBarColor.statusBarColorUpdate(R.color.slideDrawerOpenStatusBar, activity, false)
        }

        override fun onDrawerClosed(drawerView: View) {
          super.onDrawerClosed(drawerView)
          fragment.activity!!.invalidateOptionsMenu()
          StatusBarColor.statusBarColorUpdate(R.color.colorPrimaryDark, activity, false)
        }
      }
      drawerLayout.setDrawerListener(drawerToggle)
      /* Synchronize the state of the drawer indicator/affordance with the linked [drawerLayout]. */
      drawerLayout.post { drawerToggle.syncState() }
    } else {
      // For showing navigation drawer in AdministratorControlsActivity
      getFooterViewModel().isAdministratorControlsSelected.set(true)
      this.drawerLayout = drawerLayout
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
          StatusBarColor.statusBarColorUpdate(R.color.slideDrawerOpenStatusBar, activity, false)
        }

        override fun onDrawerClosed(drawerView: View) {
          super.onDrawerClosed(drawerView)
          fragment.activity!!.invalidateOptionsMenu()
          StatusBarColor.statusBarColorUpdate(R.color.colorPrimaryDark, activity, false)
        }
      }
      drawerLayout.setDrawerListener(drawerToggle)
      /* Synchronize the state of the drawer indicator/affordance with the linked [drawerLayout]. */
      drawerLayout.post { drawerToggle.syncState() }
      if (previousMenuItemId != NavigationDrawerItem.HOME.ordinal) {
        fragment.activity!!.finish()
      }
    }
  }

  private fun checkIfPreviousActivityShouldGetFinished(currentMenuItemId: Int): Boolean {
    if (previousMenuItemId != null && previousMenuItemId == 0 && currentMenuItemId != 0) {
      return true
    }
    if (previousMenuItemId != null && NavigationDrawerItem.valueFromNavId(previousMenuItemId!!) != NavigationDrawerItem.HOME) {
      return true
    }
    return false
  }

  override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
    openActivityByMenuItemId(menuItem.itemId)
    return true
  }

  private fun getHeaderViewModel(): NavigationDrawerHeaderViewModel {
    return headerViewModelProvider.getForFragment(fragment, NavigationDrawerHeaderViewModel::class.java)
  }

  private fun getFooterViewModel(): NavigationDrawerFooterViewModel {
    return footerViewModelProvider.getForFragment(fragment, NavigationDrawerFooterViewModel::class.java)
  }
}
