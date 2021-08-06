package org.oppia.android.app.drawer

import android.os.Build
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.forEach
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
import com.google.android.material.navigation.NavigationView
import org.oppia.android.R
import org.oppia.android.app.administratorcontrols.AdministratorControlsActivity
import org.oppia.android.app.devoptions.DeveloperOptionsStarter
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.help.HelpActivity
import org.oppia.android.app.home.HomeActivity
import org.oppia.android.app.model.CompletedStoryList
import org.oppia.android.app.model.ExitProfileDialogArguments
import org.oppia.android.app.model.HighlightItem
import org.oppia.android.app.model.OngoingTopicList
import org.oppia.android.app.model.Profile
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.mydownloads.MyDownloadsActivity
import org.oppia.android.app.options.OptionsActivity
import org.oppia.android.app.profileprogress.ProfileProgressActivity
import org.oppia.android.app.topic.TopicActivity
import org.oppia.android.app.viewmodel.ViewModelProvider
import org.oppia.android.databinding.DrawerFragmentBinding
import org.oppia.android.databinding.NavHeaderNavigationDrawerBinding
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.profile.ProfileManagementController
import org.oppia.android.domain.topic.TopicController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import org.oppia.android.util.statusbar.StatusBarColor
import java.util.Optional
import javax.inject.Inject

const val NAVIGATION_PROFILE_ID_ARGUMENT_KEY =
  "NavigationDrawerFragmentPresenter.navigation_profile_id"
const val TAG_SWITCH_PROFILE_DIALOG = "SWITCH_PROFILE_DIALOG"

/** The presenter for [NavigationDrawerFragment]. */
@FragmentScope
class NavigationDrawerFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val fragment: Fragment,
  private val profileManagementController: ProfileManagementController,
  private val topicController: TopicController,
  private val oppiaLogger: OppiaLogger,
  private val headerViewModelProvider: ViewModelProvider<NavigationDrawerHeaderViewModel>,
  private val footerViewModelProvider: ViewModelProvider<NavigationDrawerFooterViewModel>,
  private val developerOptionsStarter: Optional<DeveloperOptionsStarter>
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

    internalProfileId = activity.intent.getIntExtra(NAVIGATION_PROFILE_ID_ARGUMENT_KEY, -1)
    profileId = ProfileId.newBuilder().setInternalId(internalProfileId).build()

    val headerBinding =
      NavHeaderNavigationDrawerBinding.inflate(
        inflater,
        container,
        /* attachToRoot= */ false
      )
    headerBinding.viewModel = getHeaderViewModel()
    subscribeToProfileLiveData()
    subscribeToCompletedStoryListLiveData()
    subscribeToOngoingTopicListLiveData()

    binding.fragmentDrawerNavView.addHeaderView(headerBinding.root)
    binding.footerViewModel = getFooterViewModel()
    binding.executePendingBindings()

    // TODO(#3382): Remove debug only code from prod build (also check imports, constructor and drawer_fragment.xml)
    setIfDeveloperOptionsMenuItemListener()

    return binding.root
  }

  // TODO(#3382): Remove debug only code from prod build (also check imports, constructor and drawer_fragment.xml)
  private fun setIfDeveloperOptionsMenuItemListener() {
    // TODO(#3383): Find a way to make this work below N
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
      developerOptionsStarter.ifPresent { starter ->
        getFooterViewModel().isDebugMode.set(true)
        binding.developerOptionsLinearLayout.setOnClickListener {
          if (getFooterViewModel().isDeveloperOptionsSelected.get() == true) {
            drawerLayout.closeDrawers()
            return@setOnClickListener
          }
          uncheckAllMenuItemsWhenAdministratorControlsOrDeveloperOptionsIsSelected()
          drawerLayout.closeDrawers()
          getFooterViewModel().isDeveloperOptionsSelected.set(true)
          val intent = starter.createIntent(activity, internalProfileId)
          fragment.activity!!.startActivity(intent)
          if (previousMenuItemId == 0) fragment.activity!!.finish()
          else if (previousMenuItemId != null &&
            NavigationDrawerItem.valueFromNavId(previousMenuItemId!!) !=
            NavigationDrawerItem.HOME
          ) {
            fragment.activity!!.finish()
          }
          drawerLayout.closeDrawers()
        }
      }
    }
  }

  private fun getProfileData(): LiveData<Profile> {
    return Transformations.map(
      profileManagementController.getProfile(profileId).toLiveData(),
      ::processGetProfileResult
    )
  }

  private fun subscribeToProfileLiveData() {
    getProfileData().observe(
      fragment,
      Observer<Profile> {
        getHeaderViewModel().profile.set(it)
        getFooterViewModel().isAdmin.set(it.isAdmin)
        binding.administratorControlsLinearLayout.setOnClickListener {
          if (getFooterViewModel().isAdministratorControlsSelected.get() == true) {
            drawerLayout.closeDrawers()
            return@setOnClickListener
          }

          uncheckAllMenuItemsWhenAdministratorControlsOrDeveloperOptionsIsSelected()

          drawerLayout.closeDrawers()
          getFooterViewModel().isAdministratorControlsSelected.set(true)
          val intent =
            AdministratorControlsActivity.createAdministratorControlsActivityIntent(
              activity,
              internalProfileId
            )
          fragment.activity!!.startActivity(intent)
          if (previousMenuItemId == -1) fragment.activity!!.finish()
          else if (previousMenuItemId != null &&
            NavigationDrawerItem.valueFromNavId(previousMenuItemId!!) !=
            NavigationDrawerItem.HOME
          ) {
            fragment.activity!!.finish()
          }
          drawerLayout.closeDrawers()
        }
      }
    )
  }

  private fun processGetProfileResult(profileResult: AsyncResult<Profile>): Profile {
    if (profileResult.isFailure()) {
      oppiaLogger.e(
        "NavigationDrawerFragment",
        "Failed to retrieve profile",
        profileResult.getErrorOrNull()!!
      )
    }
    return profileResult.getOrDefault(Profile.getDefaultInstance())
  }

  private fun getCompletedStoryListCount(): LiveData<CompletedStoryList> {
    return Transformations.map(
      topicController.getCompletedStoryList(profileId).toLiveData(),
      ::processGetCompletedStoryListResult
    )
  }

  private fun subscribeToCompletedStoryListLiveData() {
    getCompletedStoryListCount().observe(
      fragment,
      Observer<CompletedStoryList> {
        getHeaderViewModel().completedStoryCount.set(it.completedStoryCount)
      }
    )
  }

  private fun processGetCompletedStoryListResult(
    completedStoryListResult: AsyncResult<CompletedStoryList>
  ): CompletedStoryList {
    if (completedStoryListResult.isFailure()) {
      oppiaLogger.e(
        "NavigationDrawerFragment",
        "Failed to retrieve completed story list",
        completedStoryListResult.getErrorOrNull()!!
      )
    }
    return completedStoryListResult.getOrDefault(CompletedStoryList.getDefaultInstance())
  }

  private fun getOngoingTopicListCount(): LiveData<OngoingTopicList> {
    return Transformations.map(
      topicController.getOngoingTopicList(profileId).toLiveData(),
      ::processGetOngoingTopicListResult
    )
  }

  private fun subscribeToOngoingTopicListLiveData() {
    getOngoingTopicListCount().observe(
      fragment,
      Observer<OngoingTopicList> {
        getHeaderViewModel().ongoingTopicCount.set(it.topicCount)
      }
    )
  }

  private fun processGetOngoingTopicListResult(
    ongoingTopicListResult: AsyncResult<OngoingTopicList>
  ): OngoingTopicList {
    if (ongoingTopicListResult.isFailure()) {
      oppiaLogger.e(
        "NavigationDrawerFragment",
        "Failed to retrieve ongoing topic list",
        ongoingTopicListResult.getErrorOrNull()!!
      )
    }
    return ongoingTopicListResult.getOrDefault(OngoingTopicList.getDefaultInstance())
  }

  private fun openActivityByMenuItemId(menuItemId: Int) {
    if (previousMenuItemId != menuItemId) {
      when (NavigationDrawerItem.valueFromNavId(menuItemId)) {
        NavigationDrawerItem.HOME -> {
          val intent = HomeActivity.createHomeActivity(activity, internalProfileId)
          fragment.activity!!.startActivity(intent)
          drawerLayout.closeDrawers()
        }
        NavigationDrawerItem.OPTIONS -> {
          val intent = OptionsActivity.createOptionsActivity(
            activity, internalProfileId,
            /* isFromNavigationDrawer= */ true
          )
          fragment.activity!!.startActivity(intent)
          if (checkIfPreviousActivityShouldGetFinished(menuItemId)) {
            fragment.activity!!.finish()
          }
          drawerLayout.closeDrawers()
        }
        NavigationDrawerItem.HELP -> {
          val intent = HelpActivity.createHelpActivityIntent(
            activity, internalProfileId,
            /* isFromNavigationDrawer= */ true
          )
          fragment.activity!!.startActivity(intent)
          if (checkIfPreviousActivityShouldGetFinished(menuItemId)) {
            fragment.activity!!.finish()
          }
          drawerLayout.closeDrawers()
        }
        NavigationDrawerItem.DOWNLOADS -> {
          val intent =
            MyDownloadsActivity.createMyDownloadsActivityIntent(activity, internalProfileId)
          fragment.activity!!.startActivity(intent)
          if (checkIfPreviousActivityShouldGetFinished(menuItemId)) {
            fragment.activity!!.finish()
          }
          drawerLayout.closeDrawers()
        }
        NavigationDrawerItem.SWITCH_PROFILE -> {
          val previousFragment =
            fragment.childFragmentManager.findFragmentByTag(TAG_SWITCH_PROFILE_DIALOG)
          if (previousFragment != null) {
            fragment.childFragmentManager.beginTransaction().remove(previousFragment).commitNow()
          }
          val exitProfileDialogArguments =
            if (getFooterViewModel().isAdministratorControlsSelected.get() == true) {
              ExitProfileDialogArguments
                .newBuilder()
                .setHighlightItem(HighlightItem.ADMINISTRATOR_CONTROLS_ITEM)
                .build()
            } else if (getFooterViewModel().isDeveloperOptionsSelected.get() == true) {
              ExitProfileDialogArguments
                .newBuilder()
                .setHighlightItem(HighlightItem.DEVELOPER_OPTIONS_ITEM)
                .build()
            } else {
              ExitProfileDialogArguments
                .newBuilder()
                .setHighlightItem(HighlightItem.LAST_CHECKED_MENU_ITEM)
                .build()
            }
          getFooterViewModel().isAdministratorControlsSelected.set(false)
          getFooterViewModel().isDeveloperOptionsSelected.set(false)
          binding.fragmentDrawerNavView.menu.getItem(
            NavigationDrawerItem.SWITCH_PROFILE.ordinal
          ).isChecked =
            true
          val dialogFragment = ExitProfileDialogFragment
            .newInstance(exitProfileDialogArguments = exitProfileDialogArguments)
          dialogFragment.showNow(fragment.childFragmentManager, TAG_SWITCH_PROFILE_DIALOG)
        }
      }
    } else {
      drawerLayout.closeDrawers()
    }
  }

  fun openProfileProgress(profileId: Int) {
    activity.startActivity(
      ProfileProgressActivity.createProfileProgressActivityIntent(
        activity,
        profileId
      )
    )
  }

  fun highlightLastCheckedMenuItem() {
    previousMenuItemId?.let { itemId ->
      if (itemId != 0) {
        binding.fragmentDrawerNavView.menu.getItem(
          NavigationDrawerItem.valueFromNavId(
            itemId
          ).ordinal
        ).isChecked =
          true
      }
      drawerLayout.closeDrawers()
    }
  }

  fun highlightAdministratorControlsItem() {
    getFooterViewModel().isAdministratorControlsSelected.set(true)
    uncheckAllMenuItemsWhenAdministratorControlsOrDeveloperOptionsIsSelected()
    drawerLayout.closeDrawers()
  }

  fun highlightDeveloperOptionsItem() {
    getFooterViewModel().isDeveloperOptionsSelected.set(true)
    uncheckAllMenuItemsWhenAdministratorControlsOrDeveloperOptionsIsSelected()
    drawerLayout.closeDrawers()
  }

  fun unhighlightSwitchProfileMenuItem() {
    binding.fragmentDrawerNavView.menu.getItem(
      NavigationDrawerItem.SWITCH_PROFILE.ordinal
    ).isChecked =
      false
  }

  private fun uncheckAllMenuItemsWhenAdministratorControlsOrDeveloperOptionsIsSelected() {
    binding.fragmentDrawerNavView.menu.forEach {
      it.isCheckable = false
    }
  }

  /**
   * Initializes the navigation drawer for the specified [DrawerLayout] and [Toolbar], which the host activity is
   * expected to provide. The [menuItemId] corresponds to the menu ID of the current activity, for navigation purposes.
   */
  fun setUpDrawer(drawerLayout: DrawerLayout, toolbar: Toolbar, menuItemId: Int) {
    previousMenuItemId = if (activity is TopicActivity) null else menuItemId
    if (menuItemId != 0 && menuItemId != -1) {
      getFooterViewModel().isAdministratorControlsSelected.set(false)
      getFooterViewModel().isDeveloperOptionsSelected.set(false)
      when (NavigationDrawerItem.valueFromNavId(menuItemId)) {
        NavigationDrawerItem.HOME -> {
          binding.fragmentDrawerNavView.menu.getItem(
            NavigationDrawerItem.HOME.ordinal
          ).isChecked =
            true
        }
        NavigationDrawerItem.OPTIONS -> {
          binding.fragmentDrawerNavView.menu.getItem(
            NavigationDrawerItem.OPTIONS.ordinal
          ).isChecked =
            true
        }
        NavigationDrawerItem.HELP -> {
          binding.fragmentDrawerNavView.menu.getItem(
            NavigationDrawerItem.HELP.ordinal
          ).isChecked =
            true
        }
        NavigationDrawerItem.DOWNLOADS -> {
          binding.fragmentDrawerNavView.menu.getItem(
            NavigationDrawerItem.DOWNLOADS.ordinal
          ).isChecked =
            true
        }
        NavigationDrawerItem.SWITCH_PROFILE -> {
          binding.fragmentDrawerNavView.menu.getItem(
            NavigationDrawerItem.SWITCH_PROFILE.ordinal
          ).isChecked =
            true
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
          StatusBarColor.statusBarColorUpdate(
            R.color.slideDrawerOpenStatusBar,
            activity,
            false
          )
        }

        override fun onDrawerClosed(drawerView: View) {
          super.onDrawerClosed(drawerView)
          fragment.activity!!.invalidateOptionsMenu()
          StatusBarColor.statusBarColorUpdate(
            R.color.colorPrimaryDark,
            activity,
            false
          )
        }
      }
      drawerLayout.setDrawerListener(drawerToggle)
      /* Synchronize the state of the drawer indicator/affordance with the linked [drawerLayout]. */
      drawerLayout.post { drawerToggle.syncState() }
    } else {
      // For showing navigation drawer in AdministratorControlsActivity
      if (menuItemId == 0) getFooterViewModel().isAdministratorControlsSelected.set(true)
      // For showing navigation drawer in DeveloperOptionsActivity
      else if (menuItemId == -1) getFooterViewModel().isDeveloperOptionsSelected.set(true)
      uncheckAllMenuItemsWhenAdministratorControlsOrDeveloperOptionsIsSelected()
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
          StatusBarColor.statusBarColorUpdate(
            R.color.slideDrawerOpenStatusBar,
            activity,
            false
          )
        }

        override fun onDrawerClosed(drawerView: View) {
          super.onDrawerClosed(drawerView)
          fragment.activity!!.invalidateOptionsMenu()
          StatusBarColor.statusBarColorUpdate(
            R.color.colorPrimaryDark,
            activity,
            false
          )
        }
      }
      drawerLayout.setDrawerListener(drawerToggle)
      /* Synchronize the state of the drawer indicator/affordance with the linked [drawerLayout]. */
      drawerLayout.post { drawerToggle.syncState() }
      if (previousMenuItemId != NavigationDrawerItem.HOME.ordinal && previousMenuItemId != -1) {
        fragment.activity!!.finish()
      }
    }
  }

  private fun checkIfPreviousActivityShouldGetFinished(currentMenuItemId: Int): Boolean {
    if (previousMenuItemId != null &&
      (previousMenuItemId == 0 || previousMenuItemId == -1) &&
      currentMenuItemId != 0
    ) {
      return true
    }
    if (previousMenuItemId != null &&
      NavigationDrawerItem.valueFromNavId(previousMenuItemId!!) !=
      NavigationDrawerItem.HOME
    ) {
      return true
    }
    return false
  }

  override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
    openActivityByMenuItemId(menuItem.itemId)
    return true
  }

  private fun getHeaderViewModel(): NavigationDrawerHeaderViewModel {
    return headerViewModelProvider.getForFragment(
      fragment,
      NavigationDrawerHeaderViewModel::class.java
    )
  }

  private fun getFooterViewModel(): NavigationDrawerFooterViewModel {
    return footerViewModelProvider.getForFragment(
      fragment,
      NavigationDrawerFooterViewModel::class.java
    )
  }
}
