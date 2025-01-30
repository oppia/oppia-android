package org.oppia.android.app.administratorcontrols

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityScope
import org.oppia.android.app.administratorcontrols.appversion.AppVersionFragment
import org.oppia.android.app.administratorcontrols.learneranalytics.ProfileAndDeviceIdFragment
import org.oppia.android.app.drawer.NavigationDrawerFragment
import org.oppia.android.app.model.AdministratorControlActivityStateBundle
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.settings.profile.LoadProfileEditDeletionDialogListener
import org.oppia.android.app.settings.profile.ProfileEditFragment
import org.oppia.android.app.settings.profile.ProfileListFragment
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.databinding.AdministratorControlsActivityBinding
import org.oppia.android.util.extensions.putProto
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.decorateWithUserProfileId
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
  private var selectedProfileId: ProfileId = ProfileId.getDefaultInstance()
  private lateinit var extraControlsTitle: String
  private var isProfileDeletionDialogVisible: Boolean = false

  /** Initializes the [AdministratorControlsActivity] and sets the navigation drawer. */
  fun handleOnCreate(
    extraControlsTitle: String?,
    lastLoadedFragment: String,
    selectedProfileId: ProfileId,
    isProfileDeletionDialogVisible: Boolean
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
    activity.supportFragmentManager.beginTransaction().add(
      R.id.administrator_controls_fragment_placeholder,
      AdministratorControlsFragment.newInstance(isMultipane)
    ).commitNow()
    if (isMultipane) {
      val adminControlsActivity = activity as AdministratorControlsActivity
      when (lastLoadedFragment) {
        PROFILE_LIST_FRAGMENT -> activity.loadProfileList()
        APP_VERSION_FRAGMENT -> activity.loadAppVersion()
        PROFILE_EDIT_FRAGMENT -> selectedProfileId.let { profileId ->
          if (extraControlsTitle != null) {
            activity.loadProfileEdit(profileId = profileId, profileName = extraControlsTitle)
            if (isProfileDeletionDialogVisible && profileId.internalId != 0) {
              val fragment = activity.supportFragmentManager.findFragmentById(
                R.id.administrator_controls_fragment_multipane_placeholder
              )
              (fragment as LoadProfileEditDeletionDialogListener)
                .loadProfileEditDeletionDialog(profileId)
              this.isProfileDeletionDialogVisible = false
            }
          }
        }
        PROFILE_AND_DEVICE_ID_FRAGMENT -> adminControlsActivity.loadLearnerAnalyticsData()
      }
      setBackButtonClickListener()
    }
  }

  /** Sets up the navigation drawer. */
  private fun setUpNavigationDrawer() {
    val toolbar = binding.administratorControlsActivityToolbar
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

  /** Loads the profile list fragment as the [AdministratorControlsActivity] is started in multipane tablet mode. */
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

  /** Loads the [ProfileAndDeviceIdFragment] in the multipane tablet mode. */
  fun loadLearnerAnalyticsData() {
    lastLoadedFragment = PROFILE_AND_DEVICE_ID_FRAGMENT
    getAdministratorControlsFragment()!!.setSelectedFragment(lastLoadedFragment)
    activity.supportFragmentManager.beginTransaction().replace(
      R.id.administrator_controls_fragment_multipane_placeholder,
      ProfileAndDeviceIdFragment()
    ).commitNow()
  }

  /** Loads the [ProfileEditFragment] when the user clicks on a profile in tablet multipane mode. */
  fun loadProfileEdit(profileId: ProfileId, profileName: String) {
    lastLoadedFragment = PROFILE_EDIT_FRAGMENT
    selectedProfileId = profileId
    extraControlsTitle = profileName
    setExtraControlsTitle(extraControlsTitle)
    setMultipaneBackButtonVisibility(View.VISIBLE)
    val fragment = ProfileEditFragment.newInstance(profileId.internalId, isMultipane)
    activity.supportFragmentManager.beginTransaction().replace(
      R.id.administrator_controls_fragment_multipane_placeholder,
      fragment
    ).commitNow()
  }

  /** Sets whether the user has clicked on the profile deletion [ProfileEditDeletionDialogFragment] in tablet mode. */
  fun loadProfileDeletionDialog(profileDeletionDialogVisible: Boolean) {
    isProfileDeletionDialogVisible = profileDeletionDialogVisible
  }

  private fun setMultipaneBackButtonVisibility(visibility: Int) {
    binding.administratorControlsMultipaneOptionsBackButton?.visibility = visibility
  }

  /** Handles the back button according to the back stack of fragments. */
  fun handleOnBackPressed() {
    if (isMultipane) {
      setPreviousFragmentOnBackButtonClick()
    }
  }

  private fun setBackButtonClickListener() {
    binding.administratorControlsMultipaneOptionsBackButton!!.setOnClickListener {
      setPreviousFragmentOnBackButtonClick()
    }
  }

  private fun setPreviousFragmentOnBackButtonClick() {
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
    val args = AdministratorControlActivityStateBundle.newBuilder()
      .apply {
        if (titleTextView != null) {
          selectedControlsTitle = titleTextView.text.toString()
        }
        lastLoadedFragment = this@AdministratorControlsActivityPresenter.lastLoadedFragment
        this@AdministratorControlsActivityPresenter.isProfileDeletionDialogVisible.let {
          isProfileDeletionDialogVisible = it
        }
      }
      .build()
    outState.decorateWithUserProfileId(
      this@AdministratorControlsActivityPresenter.selectedProfileId
    )
    outState.putProto(ADMINISTRATOR_CONTROLS_ACTIVITY_STATE_KEY, args)
  }
}
