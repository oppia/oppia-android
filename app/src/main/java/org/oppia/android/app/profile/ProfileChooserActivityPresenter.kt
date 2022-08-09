package org.oppia.android.app.profile

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityScope
import org.oppia.android.app.model.ScreenName
import org.oppia.android.app.testing.ProfileChooserFragmentTestActivity
import org.oppia.android.domain.profile.ProfileManagementController
import org.oppia.android.util.logging.CurrentAppScreenNameWrapper
import javax.inject.Inject

/** The presenter for [ProfileChooserActivity]. */
@ActivityScope
class ProfileChooserActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val profileManagementController: ProfileManagementController,
  private val currentAppScreenNameWrapper: CurrentAppScreenNameWrapper
) {
  /** Adds [ProfileChooserFragment] to view. */
  fun handleOnCreate() {
    // TODO(#482): Ensures that an admin profile is present. Remove when there is proper admin account creation.
    profileManagementController.addProfile(
      name = "Admin",
      pin = "",
      avatarImagePath = null,
      allowDownloadAccess = true,
      colorRgb = -10710042,
      isAdmin = true
    )
    activity.setContentView(R.layout.profile_chooser_activity)
    if (getProfileChooserFragment() == null) {
      activity.supportFragmentManager.beginTransaction().add(
        R.id.profile_chooser_fragment_placeholder,
        ProfileChooserFragment(),
        ProfileChooserFragmentTestActivity.TAG_PROFILE_CHOOSER_FRAGMENT
      ).commitNow()
    }
  }

  /** Returns an intent that wraps up a proto object carrying the screen name of the activity. */
  fun getCurrentAppScreenNameIntent(): Intent =
    currentAppScreenNameWrapper.getCurrentAppScreenNameIntent(ScreenName.PROFILE_CHOOSER_ACTIVITY)

  private fun getProfileChooserFragment(): ProfileChooserFragment? {
    return activity
      .supportFragmentManager
      .findFragmentById(
        R.id.profile_chooser_fragment_placeholder
      ) as ProfileChooserFragment?
  }
}
