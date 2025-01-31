package org.oppia.android.app.administratorcontrols

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAutoLocalizedAppCompatActivity
import org.oppia.android.app.administratorcontrols.appversion.AppVersionActivity
import org.oppia.android.app.administratorcontrols.learneranalytics.ProfileAndDeviceIdActivity
import org.oppia.android.app.model.AdministratorControlActivityStateBundle
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.ScreenName.ADMINISTRATOR_CONTROLS_ACTIVITY
import org.oppia.android.app.settings.profile.ProfileEditFragment
import org.oppia.android.app.settings.profile.ProfileListActivity
import org.oppia.android.app.settings.profile.ProfileListFragment
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.util.extensions.getProto
import org.oppia.android.util.logging.CurrentAppScreenNameIntentDecorator.decorateWithScreenName
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.decorateWithUserProfileId
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.extractCurrentUserProfileId
import javax.inject.Inject

/** Argument key used to identify [ProfileListFragment] in the backstack. */
const val PROFILE_LIST_FRAGMENT = "PROFILE_LIST_FRAGMENT"

/** Argument key used to identify [ProfileEditFragment] in the backstack. */
const val PROFILE_EDIT_FRAGMENT = "PROFILE_EDIT_FRAGMENT"

/** Argument key used to identify [AppVersionFragment] in the backstack. */
const val APP_VERSION_FRAGMENT = "APP_VERSION_FRAGMENT"

/** Argument key used to identify [ProfileAndDeviceIdFragment] in the backstack. */
const val PROFILE_AND_DEVICE_ID_FRAGMENT = "PROFILE_AND_DEVICE_ID_FRAGMENT"

/** Argument key for Administrator Controls Activity saved state. */
const val ADMINISTRATOR_CONTROLS_ACTIVITY_STATE_KEY = "ADMINISTRATOR_CONTROLS_ACTIVITY_STATE_KEY"

/** Activity [AdministratorControlsActivity] that allows user to change admin controls. */
class AdministratorControlsActivity :
  InjectableAutoLocalizedAppCompatActivity(),
  RouteToProfileListListener,
  RouteToAppVersionListener,
  RouteToLearnerAnalyticsListener,
  LoadLearnerAnalyticsListener,
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
  private var isProfileDeletionDialogVisible: Boolean = false

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)

    val args = savedInstanceState?.getProto(
      ADMINISTRATOR_CONTROLS_ACTIVITY_STATE_KEY,
      AdministratorControlActivityStateBundle.getDefaultInstance()
    )

    val extraControlsTitle = args?.selectedControlsTitle

    isProfileDeletionDialogVisible =
      args?.isProfileDeletionDialogVisible ?: false
    lastLoadedFragment = if (savedInstanceState != null) {
      args?.lastLoadedFragment as String
    } else {
      // TODO(#661): Change the default fragment in the right hand side to be EditAccount fragment in the case of multipane controls.
      PROFILE_LIST_FRAGMENT
    }
    val selectedProfileId = intent?.extractCurrentUserProfileId() ?: ProfileId.getDefaultInstance()

    administratorControlsActivityPresenter.handleOnCreate(
      extraControlsTitle,
      lastLoadedFragment,
      selectedProfileId,
      isProfileDeletionDialogVisible
    )
    title = resourceHandler.getStringInLocale(R.string.administrator_controls)

    onBackPressedDispatcher.addCallback(
      this,
      object : OnBackPressedCallback(/* enabled = */ true) {
        override fun handleOnBackPressed() {
          this@AdministratorControlsActivity.handleBackPress()
        }
      }
    )
  }

  override fun routeToAppVersion() {
    startActivity(AppVersionActivity.createAppVersionActivityIntent(this))
  }

  override fun routeToProfileList() {
    startActivity(ProfileListActivity.createProfileListActivityIntent(this))
  }

  override fun routeToLearnerAnalytics() {
    startActivity(ProfileAndDeviceIdActivity.createIntent(this))
  }

  override fun loadProfileEdit(profileId: ProfileId, profileName: String) {
    lastLoadedFragment = PROFILE_EDIT_FRAGMENT
    administratorControlsActivityPresenter.loadProfileEdit(profileId, profileName)
  }

  override fun loadProfileDeletionDialog(isProfileDeletionDialogVisible: Boolean) {
    this.isProfileDeletionDialogVisible = isProfileDeletionDialogVisible
    administratorControlsActivityPresenter.loadProfileDeletionDialog(isProfileDeletionDialogVisible)
  }

  companion object {

    /** Returns an [Intent] to start this activity. */
    fun createAdministratorControlsActivityIntent(context: Context, profileId: ProfileId?): Intent {

      val intent = Intent(context, AdministratorControlsActivity::class.java)
      intent.decorateWithScreenName(ADMINISTRATOR_CONTROLS_ACTIVITY)
      if (profileId != null) {
        intent.decorateWithUserProfileId(profileId)
      }
      return intent
    }
  }

  private fun handleBackPress() {
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
      administratorControlsActivityPresenter.handleOnBackPressed()
    } else {
      finish()
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

  override fun loadLearnerAnalyticsData() {
    lastLoadedFragment = PROFILE_AND_DEVICE_ID_FRAGMENT
    administratorControlsActivityPresenter.setExtraControlsTitle(
      resourceHandler.getStringInLocale(R.string.profile_and_device_id_activity_title)
    )
    administratorControlsActivityPresenter.loadLearnerAnalyticsData()
  }

  override fun showLogoutDialog() {
    LogoutDialogFragment.newInstance()
      .showNow(supportFragmentManager, LogoutDialogFragment.TAG_LOGOUT_DIALOG_FRAGMENT)
  }

  override fun onSaveInstanceState(outState: Bundle) {
    administratorControlsActivityPresenter.handleOnSaveInstanceState(outState)
    super.onSaveInstanceState(outState)
    administratorControlsActivityPresenter.handleOnSaveInstanceState(outState)
  }
}
