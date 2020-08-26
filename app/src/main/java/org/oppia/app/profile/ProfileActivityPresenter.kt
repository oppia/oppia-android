package org.oppia.app.profile

import androidx.appcompat.app.AppCompatActivity
import org.oppia.app.R
import org.oppia.app.activity.ActivityScope
import org.oppia.app.testing.ProfileChooserFragmentTestActivity
import org.oppia.domain.profile.ProfileManagementController
import javax.inject.Inject

/** The presenter for [ProfileActivity]. */
@ActivityScope
class ProfileActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val profileManagementController: ProfileManagementController
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
    activity.setContentView(R.layout.profile_activity)
    if (getProfileChooserFragment() == null) {
      activity.supportFragmentManager.beginTransaction().add(
        R.id.profile_chooser_fragment_placeholder,
        ProfileChooserFragment(),
        ProfileChooserFragmentTestActivity.TAG_PROFILE_CHOOSER_FRAGMENT
      ).commitNow()
    }
  }

  private fun getProfileChooserFragment(): ProfileChooserFragment? {
    return activity
      .supportFragmentManager
      .findFragmentById(
        R.id.profile_chooser_fragment_placeholder
      ) as ProfileChooserFragment?
  }
}
