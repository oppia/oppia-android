package org.oppia.android.app.administratorcontrols

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.R
import org.oppia.android.app.activity.InjectableAppCompatActivity
import org.oppia.android.app.activity.route.ActivityRouter
import org.oppia.android.app.drawer.NAVIGATION_PROFILE_ID_ARGUMENT_KEY
import org.oppia.android.app.model.AppVersionActivityParams
import org.oppia.android.app.model.DestinationScreen
import org.oppia.android.app.model.ProfileAndDeviceIdActivityParams
import org.oppia.android.app.model.ProfileListActivityParams
import org.oppia.android.app.model.ScreenName.ADMINISTRATOR_CONTROLS_ACTIVITY
import org.oppia.android.app.settings.profile.ProfileEditFragment
import org.oppia.android.app.settings.profile.ProfileListFragment
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.util.extensions.getStringFromBundle
import org.oppia.android.util.logging.CurrentAppScreenNameIntentDecorator.decorateWithScreenName
import javax.inject.Inject

/** Argument key for of title for selected controls in [AdministratorControlsActivity]. */
private const val SELECTED_CONTROLS_TITLE_SAVED_KEY =
  "AdministratorControlsActivity.selected_controls_title"

/**
 * Argument key for of selected profile for selected controls in [AdministratorControlsActivity].
 */
private const val SELECTED_PROFILE_ID_SAVED_KEY =
  "AdministratorControlsActivity.selected_profile_id"

/** Argument key for last loaded fragment in [AdministratorControlsActivity]. */
private const val LAST_LOADED_FRAGMENT_EXTRA_KEY =
  "AdministratorControlsActivity.last_loaded_fragment"

/** Argument key used to identify [ProfileListFragment] in the backstack. */
private const val PROFILE_LIST_FRAGMENT = "PROFILE_LIST_FRAGMENT"

/** Argument key used to identify [ProfileEditFragment] in the backstack. */
private const val PROFILE_EDIT_FRAGMENT = "PROFILE_EDIT_FRAGMENT"

/** Argument key for the Profile deletion confirmation in [ProfileEditActivity]. */
private const val IS_PROFILE_DELETION_DIALOG_VISIBLE_KEY =
  "ProfileEditActivity.is_profile_deletion_dialog_visible"

/** Argument key used to identify [AppVersionFragment] in the backstack. */
private const val APP_VERSION_FRAGMENT = "APP_VERSION_FRAGMENT"

/** Argument key used to identify [ProfileAndDeviceIdFragment] in the backstack. */
private const val PROFILE_AND_DEVICE_ID_FRAGMENT = "PROFILE_AND_DEVICE_ID_FRAGMENT"

/** Activity [AdministratorControlsActivity] that allows user to change admin controls. */
class AdministratorControlsActivity :
  InjectableAppCompatActivity(),
  RouteToProfileListListener,
  RouteToAppVersionListener,
  RouteToLearnerAnalyticsListener,
  LoadLearnerAnalyticsListener,
  LoadProfileListListener,
  LoadAppVersionListener,
  LoadProfileEditListener,
  ProfileEditDeletionDialogListener,
  ShowLogoutDialogListener {
  @Inject lateinit var presenter: AdministratorControlsActivityPresenter
  @Inject lateinit var resourceHandler: AppLanguageResourceHandler
  @Inject lateinit var activityRouter: ActivityRouter

  private lateinit var lastLoadedFragment: String
  private var isProfileDeletionDialogVisible: Boolean = false

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as Injector).inject(this)
    val extraControlsTitle =
      savedInstanceState?.getStringFromBundle(SELECTED_CONTROLS_TITLE_SAVED_KEY)
    isProfileDeletionDialogVisible =
      savedInstanceState?.getBoolean(IS_PROFILE_DELETION_DIALOG_VISIBLE_KEY) ?: false
    lastLoadedFragment = if (savedInstanceState != null) {
      savedInstanceState.getStringFromBundle(LAST_LOADED_FRAGMENT_EXTRA_KEY) as String
    } else {
      // TODO(#661): Change the default fragment in the right hand side to be EditAccount fragment in the case of multipane controls.
      PROFILE_LIST_FRAGMENT
    }
    val selectedProfileId = savedInstanceState?.getInt(SELECTED_PROFILE_ID_SAVED_KEY) ?: -1
    presenter.handleOnCreate(
      extraControlsTitle,
      lastLoadedFragment,
      selectedProfileId,
      isProfileDeletionDialogVisible
    )
    title = resourceHandler.getStringInLocale(R.string.administrator_controls)
  }

  override fun routeToAppVersion() {
    activityRouter.routeToScreen(
      DestinationScreen.newBuilder().apply {
        appVersionActivityParams = AppVersionActivityParams.getDefaultInstance()
      }.build()
    )
  }

  override fun routeToProfileList() {
    activityRouter.routeToScreen(
      DestinationScreen.newBuilder().apply {
        profileListActivityParams = ProfileListActivityParams.getDefaultInstance()
      }.build()
    )
  }

  override fun routeToLearnerAnalytics() {
    activityRouter.routeToScreen(
      DestinationScreen.newBuilder().apply {
        profileAndDeviceIdActivityParams = ProfileAndDeviceIdActivityParams.getDefaultInstance()
      }.build()
    )
  }

  override fun loadProfileEdit(profileId: Int, profileName: String) {
    lastLoadedFragment = PROFILE_EDIT_FRAGMENT
    presenter.loadProfileEdit(profileId, profileName)
  }

  override fun loadProfileDeletionDialog(isProfileDeletionDialogVisible: Boolean) {
    this.isProfileDeletionDialogVisible = isProfileDeletionDialogVisible
    presenter.loadProfileDeletionDialog(isProfileDeletionDialogVisible)
  }

  companion object {
    /** Returns an [Intent] to start this activity. */
    fun createIntent(context: Context, profileId: Int?, clearTop: Boolean = false): Intent {
      val intent = Intent(context, AdministratorControlsActivity::class.java)
      intent.putExtra(NAVIGATION_PROFILE_ID_ARGUMENT_KEY, profileId)
      if (clearTop) intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
      intent.decorateWithScreenName(ADMINISTRATOR_CONTROLS_ACTIVITY)
      return intent
    }

    /** Returns the argument key used to specify the user's internal profile ID. */
    fun getIntentKey(): String {
      return NAVIGATION_PROFILE_ID_ARGUMENT_KEY
    }
  }

  override fun onBackPressed() {
    val fragment =
      supportFragmentManager.findFragmentById(
        R.id.administrator_controls_fragment_multipane_placeholder
      )
    /*
     * If the current fragment is ProfileListFragment then the activity should end on back press.
     * If it's instead ProfileEditFragment then profileListFragment should be inflated via
     * handleOnBackPressed.
     */
    if (fragment is ProfileEditFragment) {
      presenter.handleOnBackPressed()
    } else {
      super.onBackPressed()
    }
  }

  override fun loadProfileList() {
    lastLoadedFragment = PROFILE_LIST_FRAGMENT
    presenter
      .setExtraControlsTitle(
        resourceHandler.getStringInLocale(R.string.administrator_controls_edit_profiles)
      )
    presenter.loadProfileList()
  }

  override fun loadAppVersion() {
    lastLoadedFragment = APP_VERSION_FRAGMENT
    presenter
      .setExtraControlsTitle(
        resourceHandler.getStringInLocale(R.string.administrator_controls_app_version)
      )
    presenter.loadAppVersion()
  }

  override fun loadLearnerAnalyticsData() {
    lastLoadedFragment = PROFILE_AND_DEVICE_ID_FRAGMENT
    presenter.setExtraControlsTitle(
      resourceHandler.getStringInLocale(R.string.profile_and_device_id_activity_title)
    )
    presenter.loadLearnerAnalyticsData()
  }

  override fun showLogoutDialog() {
    LogoutDialogFragment.newInstance()
      .showNow(supportFragmentManager, LogoutDialogFragment.TAG_LOGOUT_DIALOG_FRAGMENT)
  }

  override fun onSaveInstanceState(outState: Bundle) {
    presenter.handleOnSaveInstanceState(outState)
    super.onSaveInstanceState(outState)
    presenter.handleOnSaveInstanceState(outState)
  }

  interface Injector {
    fun inject(activity: AdministratorControlsActivity)
  }
}
