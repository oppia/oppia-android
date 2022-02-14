package org.oppia.android.app.administratorcontrols

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAppCompatActivity
import org.oppia.android.app.administratorcontrols.appversion.AppVersionActivity
import org.oppia.android.app.drawer.NAVIGATION_PROFILE_ID_ARGUMENT_KEY
import org.oppia.android.app.settings.profile.ProfileEditFragment
import org.oppia.android.app.settings.profile.ProfileListActivity
import org.oppia.android.app.settings.profile.ProfileListFragment
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.util.extensions.getStringFromBundle
import javax.inject.Inject

/** Argument key for of title for selected controls in [AdministratorControlsActivity]. */
const val SELECTED_CONTROLS_TITLE_SAVED_KEY =
  "AdministratorControlsActivity.selected_controls_title"

/** Argument key for of selected profile for selected controls in [AdministratorControlsActivity]. */
const val SELECTED_PROFILE_ID_SAVED_KEY =
  "AdministratorControlsActivity.selected_profile_id"

/** Argument key for last loaded fragment in [AdministratorControlsActivity]. */
const val LAST_LOADED_FRAGMENT_EXTRA_KEY = "AdministratorControlsActivity.last_loaded_fragment"

/** Argument key used to identify [ProfileListFragment] in the backstack. */
const val PROFILE_LIST_FRAGMENT = "PROFILE_LIST_FRAGMENT"

/** Argument key used to identify [ProfileEditFragment] in the backstack. */
const val PROFILE_EDIT_FRAGMENT = "PROFILE_EDIT_FRAGMENT"

/** Argument key for the Profile deletion confirmation in [ProfileEditActivity]. */
const val IS_PROFILE_DELETION_DIALOG_VISIBLE_KEY =
  "ProfileEditActivity.is_profile_deletion_dialog_visible"

/** Argument key used to identify [AppVersionFragment] in the backstack. */
const val APP_VERSION_FRAGMENT = "APP_VERSION_FRAGMENT"

/** Activity [AdministratorControlsActivity] that allows user to change admin controls. */
class AdministratorControlsActivity :
  InjectableAppCompatActivity(),
  RouteToProfileListListener,
  RouteToAppVersionListener,
  LoadProfileListListener,
  LoadAppVersionListener,
  LoadProfileEditListener,
  ProfileEditDeletionDialogListener,
  ShowLogoutDialogListener {
  @Inject
  lateinit var administratorControlsActivityPresenter: AdministratorControlsActivityPresenter

  @Inject
  lateinit var resourceHandler: AppLanguageResourceHandler

  private lateinit var lastLoadedFragment: String
  private var isProfileDeletionDialogVisible: Boolean? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)
    val extraControlsTitle =
      savedInstanceState?.getStringFromBundle(SELECTED_CONTROLS_TITLE_SAVED_KEY)
    isProfileDeletionDialogVisible =
      savedInstanceState?.getBoolean(IS_PROFILE_DELETION_DIALOG_VISIBLE_KEY)
    lastLoadedFragment = if (savedInstanceState != null) {
      savedInstanceState.getStringFromBundle(LAST_LOADED_FRAGMENT_EXTRA_KEY) as String
    } else {
      // TODO(#661): Change the default fragment in the right hand side to be EditAccount fragment in the case of multipane controls.
      PROFILE_LIST_FRAGMENT
    }
    val selectedProfileId = savedInstanceState?.getInt(SELECTED_PROFILE_ID_SAVED_KEY)
    administratorControlsActivityPresenter.handleOnCreate(
      extraControlsTitle,
      lastLoadedFragment,
      selectedProfileId,
      isProfileDeletionDialogVisible
    )
    title = resourceHandler.getStringInLocale(R.string.administrator_controls)
  }

  override fun routeToAppVersion() {
    startActivity(AppVersionActivity.createAppVersionActivityIntent(this))
  }

  override fun routeToProfileList() {
    startActivity(ProfileListActivity.createProfileListActivityIntent(this))
  }

  override fun onBackPressed() {
    val fragment =
      supportFragmentManager.findFragmentById(
        R.id.administrator_controls_fragment_multipane_placeholder
      )
    /* when fragment is ProfileEditFragment then we should switch fragment to ProfileListFragment or else
       we just end the activity as then we are having ProfileListFragment on top. */
    if (fragment is ProfileEditFragment) {
      administratorControlsActivityPresenter.handleOnBackPressed()
    } else {
      super.onBackPressed()
    }
  }

  override fun loadProfileList() {
    lastLoadedFragment = PROFILE_LIST_FRAGMENT
    administratorControlsActivityPresenter
      .setExtraControlsTitle(
        resourceHandler.getStringInLocale(R.string.administrator_controls_edit_profiles)
      )
    administratorControlsActivityPresenter.loadProfileList()
  }

  override fun loadAppVersion() {
    lastLoadedFragment = APP_VERSION_FRAGMENT
    administratorControlsActivityPresenter
      .setExtraControlsTitle(
        resourceHandler.getStringInLocale(R.string.administrator_controls_app_version)
      )
    administratorControlsActivityPresenter.loadAppVersion()
  }

  override fun loadProfileEdit(profileId: Int, profileName: String) {
    lastLoadedFragment = PROFILE_EDIT_FRAGMENT
    administratorControlsActivityPresenter.loadProfileEdit(profileId, profileName)
  }

  override fun loadBooleanProfileEditDeletionDialog(isProfileDeletionDialogVisible: Boolean) {
    this.isProfileDeletionDialogVisible = isProfileDeletionDialogVisible
    administratorControlsActivityPresenter.loadProfileDeletionDialog(isProfileDeletionDialogVisible)
  }

  companion object {
    /** Returns an [Intent] to start this activity. */
    fun createAdministratorControlsActivityIntent(context: Context, profileId: Int?): Intent {
      val intent = Intent(context, AdministratorControlsActivity::class.java)
      intent.putExtra(NAVIGATION_PROFILE_ID_ARGUMENT_KEY, profileId)
      return intent
    }

    /** Returns the argument key used to specify the user's internal profile ID. */
    fun getIntentKey(): String {
      return NAVIGATION_PROFILE_ID_ARGUMENT_KEY
    }
  }

  override fun showLogoutDialog() {
    LogoutDialogFragment.newInstance()
      .showNow(supportFragmentManager, LogoutDialogFragment.TAG_LOGOUT_DIALOG_FRAGMENT)
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    administratorControlsActivityPresenter.handleOnSaveInstanceState(outState)
  }
}
