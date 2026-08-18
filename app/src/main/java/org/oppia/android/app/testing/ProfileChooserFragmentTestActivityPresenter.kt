package org.oppia.android.app.testing

import androidx.appcompat.app.AppCompatActivity
import org.oppia.android.app.activity.ActivityScope
import org.oppia.android.app.profile.ProfileChooserFragment
import org.oppia.android.app.ui.R
import org.oppia.android.domain.profile.ProfileManagementController
import javax.inject.Inject

/** The presenter for [ProfileChooserFragmentTestActivity]. */
@ActivityScope
class ProfileChooserFragmentTestActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val profileManagementController: ProfileManagementController
) {
  /** Adds [ProfileChooserFragment] to view. */
  fun handleOnCreate() {
    profileManagementController.addProfile(
      name = "Ben",
      pin = "123",
      avatarImagePath = null,
      allowDownloadAccess = false,
      colorRgb = -10710042,
      isAdmin = false
    )
    profileManagementController.addProfile(
      name = "Tom",
      pin = "123",
      avatarImagePath = null,
      allowDownloadAccess = false,
      colorRgb = -10710042,
      isAdmin = false
    )
    activity.setContentView(R.layout.profile_test_activity)
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
