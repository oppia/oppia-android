package org.oppia.android.app.administratorcontrols

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityScope
import org.oppia.android.app.administratorcontrols.appversion.AppVersionFragment
import org.oppia.android.app.drawer.NavigationDrawerFragment
import org.oppia.android.app.settings.profile.ProfileEditFragment
import org.oppia.android.app.settings.profile.ProfileListFragment
import org.oppia.android.databinding.AdministratorControlsActivityBinding
import javax.inject.Inject

/** The presenter for [AdministratorControlsActivity]. */
@ActivityScope
class AdministratorControlsActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity
) {
  private lateinit var navigationDrawerFragment: NavigationDrawerFragment
  private var isMultipane = false
  private lateinit var lastLoadedFragment: String
  private val ADMINISTRATOR_CONTROLS_BACKSTACK: String = "ADMINISTRATOR_CONTROLS_BACKSTACK"

  private lateinit var binding: AdministratorControlsActivityBinding

  /** Initializes the [AdministratorControlsActivity] and sets the navigation drawer. */
  fun handleOnCreate(extraControlsTitle: String?, lastLoadedFragment: String) {
    binding = DataBindingUtil.setContentView(
      activity,
      R.layout.administrator_controls_activity
    )
    setUpNavigationDrawer()
    this.lastLoadedFragment = lastLoadedFragment
    binding.extraControlsTitle?.apply {
      text = extraControlsTitle
    }
    isMultipane = binding.administratorControlsFragmentMultipanePlaceholder != null
    val previousFragment = getAdministratorControlsFragment()
    if (previousFragment != null) {
      activity.supportFragmentManager.beginTransaction().remove(previousFragment).commitNow()
    }
    activity.supportFragmentManager.beginTransaction().add(
      R.id.administrator_controls_fragment_placeholder,
      AdministratorControlsFragment.newInstance(isMultipane),
      ADMINISTRATOR_CONTROLS_BACKSTACK
    ).commitNow()
    if (isMultipane) {
      when (lastLoadedFragment) {
        PROFILE_LIST_FRAGMENT -> (activity as AdministratorControlsActivity).loadProfileList()
        APP_VERSION_FRAGMENT -> (activity as AdministratorControlsActivity).loadAppVersion()
      }
    }
  }

  /** Sets up the navigation drawer. */
  private fun setUpNavigationDrawer() {
    val toolbar = binding.administratorControlsActivityToolbar as Toolbar
    activity.setSupportActionBar(toolbar)
    activity.supportActionBar!!.setDisplayShowHomeEnabled(true)
    navigationDrawerFragment = activity
      .supportFragmentManager
      .findFragmentById(
        R.id.administrator_controls_activity_fragment_navigation_drawer
      ) as NavigationDrawerFragment
    navigationDrawerFragment.setUpDrawer(
      binding.administratorControlsActivityDrawerLayout,
      toolbar, /* menuItemId= */ 0
    )
  }

  /** Returns [AdministratorControlsFragment] instance. */
  private fun getAdministratorControlsFragment(): AdministratorControlsFragment? {
    return activity
      .supportFragmentManager
      .findFragmentById(
        R.id.administrator_controls_fragment_placeholder
      ) as AdministratorControlsFragment?
  }

  /**
   * Loads the profile list fragment as the [AdministratorControlsActivity] is
   * started in multipane tablet mode.
   */
  fun loadProfileList() {
    lastLoadedFragment = PROFILE_LIST_FRAGMENT
    getAdministratorControlsFragment()!!.setSelectedFragment(lastLoadedFragment)
    activity.supportFragmentManager.beginTransaction().add(
      R.id.administrator_controls_fragment_multipane_placeholder,
      ProfileListFragment.newInstance(isMultipane),
      ADMINISTRATOR_CONTROLS_BACKSTACK
    ).commitNow()
  }

  /** Loads the [AppVersionFragment] in the multipane tablet mode. */
  fun loadAppVersion() {
    lastLoadedFragment = APP_VERSION_FRAGMENT
    getAdministratorControlsFragment()!!.setSelectedFragment(lastLoadedFragment)
    activity.supportFragmentManager.beginTransaction().add(
      R.id.administrator_controls_fragment_multipane_placeholder,
      AppVersionFragment(),
      ADMINISTRATOR_CONTROLS_BACKSTACK
    ).commitNow()
  }

  /** Loads the [ProfileEditFragment] when the user clicks on a profile in tablet multipane mode. */
  fun loadProfileEdit(profileId: Int) {
    lastLoadedFragment = PROFILE_EDIT_FRAGMENT
    binding.administratorControlsMultipaneOptionsBackButton!!.visibility = View.VISIBLE
    val fragment = ProfileEditFragment.newInstance(profileId, isMultipane)
    activity.supportFragmentManager.beginTransaction().add(
      R.id.administrator_controls_fragment_multipane_placeholder,
      fragment,
      ADMINISTRATOR_CONTROLS_BACKSTACK
    )
      .addToBackStack(ADMINISTRATOR_CONTROLS_BACKSTACK)
      .commitNow()
  }

  /** Handles the back button according to the back stack of fragments. */
  fun handleOnBackPressed() {
    if (activity.supportFragmentManager.backStackEntryCount > 0) {
      activity.supportFragmentManager.popBackStackImmediate()
    }
  }

  /** Checks and sets the visibility of back button in multipane tablet mode. */
  fun handleOnResume() {
    activity.supportFragmentManager.addOnBackStackChangedListener {
      if (activity.supportFragmentManager.backStackEntryCount == 0)
        binding.administratorControlsMultipaneOptionsBackButton!!.visibility = View.GONE
      val multipaneId = R.id.administrator_controls_fragment_multipane_placeholder
      val multipaneFragment =
        activity.supportFragmentManager.findFragmentById(multipaneId)
      if (multipaneFragment is ProfileListFragment) {
        setExtraControlsTitle(
          activity.applicationContext.resources.getString(
            R.string.administrator_controls_edit_profiles
          )
        )
      }
    }
    if (isMultipane) {
      binding.administratorControlsMultipaneOptionsBackButton!!.setOnClickListener {
        handleOnBackPressed()
      }
    }
  }

  /** Sets the title of the extra controls in multipane tablet mode. */
  fun setExtraControlsTitle(title: String) {
    binding.extraControlsTitle?.text = title
  }

  /** Saves the state of the views on configuration changes. */
  fun handleOnSaveInstanceState(outState: Bundle) {
    val titleTextView = binding.extraControlsTitle
    if (titleTextView != null) {
      outState.putString(SELECTED_CONTROLS_TITLE_SAVED_KEY, titleTextView.text.toString())
    }
    outState.putString(LAST_LOADED_FRAGMENT_EXTRA_KEY, lastLoadedFragment)
  }
}
