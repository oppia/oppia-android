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
import org.oppia.android.app.settings.profile.LoadProfileDeletionDialogListener
import org.oppia.android.app.settings.profile.ProfileEditFragment
import org.oppia.android.app.settings.profile.ProfileListFragment
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.databinding.AdministratorControlsActivityBinding
import javax.inject.Inject

/** The presenter for [AdministratorControlsActivity]. */
@ActivityScope
class AdministratorControlsActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val resourceHandler: AppLanguageResourceHandler
) {
  private lateinit var navigationDrawerFragment: NavigationDrawerFragment
  private var isMultipane = false
  private lateinit var binding: AdministratorControlsActivityBinding

  private lateinit var lastLoadedFragment: String
  private var selectedProfileId: Int? = null
  private lateinit var extraControlsTitle: String
  private var isProfileDeletionDialogVisible: Boolean? = null

  /** Initializes the [AdministratorControlsActivity] and sets the navigation drawer. */
  fun handleOnCreate(
    extraControlsTitle: String?,
    lastLoadedFragment: String,
    selectedProfileId: Int?,
    isProfileDeletionDialogVisible: Boolean?
  ) {
    binding = DataBindingUtil.setContentView(
      activity,
      R.layout.administrator_controls_activity
    )
    setUpNavigationDrawer()
    this.lastLoadedFragment = lastLoadedFragment
    this.selectedProfileId = selectedProfileId
    this.isProfileDeletionDialogVisible = isProfileDeletionDialogVisible
    binding.extraControlsTitle?.apply {
      text = extraControlsTitle
    }
    isMultipane = binding.administratorControlsFragmentMultipanePlaceholder != null
    val previousFragment = getAdministratorControlsFragment()
    if (previousFragment != null) {
      activity.supportFragmentManager.beginTransaction().remove(previousFragment).commitNow()
    }
    activity.supportFragmentManager.beginTransaction().replace(
      R.id.administrator_controls_fragment_placeholder,
      AdministratorControlsFragment.newInstance(isMultipane)
    ).commitNow()
    if (isMultipane) {
      when (lastLoadedFragment) {
        PROFILE_LIST_FRAGMENT -> (activity as AdministratorControlsActivity).loadProfileList()
        APP_VERSION_FRAGMENT -> (activity as AdministratorControlsActivity).loadAppVersion()
        PROFILE_EDIT_FRAGMENT -> selectedProfileId?.let {
          if (extraControlsTitle != null) {
            (activity as AdministratorControlsActivity).loadProfileEdit(
              profileId = it,
              profileName = extraControlsTitle
            )
            if (isProfileDeletionDialogVisible == true && it != 0) {
              val fragment = activity.supportFragmentManager.findFragmentById(
                R.id.administrator_controls_fragment_multipane_placeholder
              )
              (fragment as LoadProfileDeletionDialogListener).loadProfileDeletionDialog(it)
              this.isProfileDeletionDialogVisible = false
            }
          }
        }
      }
      setBackButtonClickListener()
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
   * Loads the profile list fragment as the [AdministratorControlsActivity]
   * is started in multipane tablet mode.
   */
  fun loadProfileList() {
    lastLoadedFragment = PROFILE_LIST_FRAGMENT
    getAdministratorControlsFragment()!!.setSelectedFragment(lastLoadedFragment)
    setMultipaneBackButtonVisibility(View.GONE)
    activity.supportFragmentManager.beginTransaction().replace(
      R.id.administrator_controls_fragment_multipane_placeholder,
      ProfileListFragment.newInstance(isMultipane)
    ).commitNow()
  }

  /** Loads the [AppVersionFragment] in the multipane tablet mode. */
  fun loadAppVersion() {
    lastLoadedFragment = APP_VERSION_FRAGMENT
    getAdministratorControlsFragment()!!.setSelectedFragment(lastLoadedFragment)
    setMultipaneBackButtonVisibility(View.GONE)
    activity.supportFragmentManager.beginTransaction().replace(
      R.id.administrator_controls_fragment_multipane_placeholder,
      AppVersionFragment()
    ).commitNow()
  }

  /** Loads the [ProfileEditFragment] when the user clicks on a profile in tablet multipane mode. */
  fun loadProfileEdit(profileId: Int, profileName: String) {
    lastLoadedFragment = PROFILE_EDIT_FRAGMENT
    selectedProfileId = profileId
    extraControlsTitle = profileName
    setExtraControlsTitle(extraControlsTitle)
    setMultipaneBackButtonVisibility(View.VISIBLE)
    val fragment = ProfileEditFragment.newInstance(profileId, isMultipane)
    activity.supportFragmentManager.beginTransaction().replace(
      R.id.administrator_controls_fragment_multipane_placeholder,
      fragment
    ).commitNow()
  }

  fun loadProfileDeletionDialog(profileDeletionDialogVisible: Boolean) {
    isProfileDeletionDialogVisible = profileDeletionDialogVisible
  }

  private fun setMultipaneBackButtonVisibility(visibility: Int) {
    binding.administratorControlsMultipaneOptionsBackButton!!.visibility = visibility
  }

  /** Handles the back button according to the back stack of fragments. */
  fun handleOnBackPressed() {
    if (isMultipane) {
      backButtonLogic()
    }
  }

  private fun setBackButtonClickListener() {
    binding.administratorControlsMultipaneOptionsBackButton!!.setOnClickListener {
      backButtonLogic()
    }
  }

  private fun backButtonLogic() {
    val currentFragment =
      activity.supportFragmentManager.findFragmentById(
        R.id.administrator_controls_fragment_multipane_placeholder
      )
    when (currentFragment) {
      is ProfileEditFragment -> {
        setExtraControlsTitle(
          resourceHandler.getStringInLocale(R.string.administrator_controls_edit_profiles)
        )
        loadProfileList()
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
    isProfileDeletionDialogVisible?.let {
      outState.putBoolean(IS_PROFILE_DELETION_DIALOG_VISIBLE_KEY, it)
    }
    selectedProfileId?.let { outState.putInt(SELECTED_PROFILE_ID_SAVED_KEY, it) }
  }
}
