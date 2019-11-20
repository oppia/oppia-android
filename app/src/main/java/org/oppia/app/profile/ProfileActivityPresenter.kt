package org.oppia.app.profile

import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.oppia.app.R
import org.oppia.app.activity.ActivityScope
import org.oppia.domain.profile.ProfileManagementController
import javax.inject.Inject

/** The presenter for [ProfileActivity]. */
@ActivityScope
class ProfileActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val profileManagementController: ProfileManagementController
) {
  /** Adds [ProfileChooserFragment] to view. */
  @ExperimentalCoroutinesApi
  fun handleOnCreate() {
    // Ensures that an admin profile is present
    profileManagementController.addProfile("Sandeep", "12345", null, allowDownloadAccess = true, isAdmin = true)
    profileManagementController.addProfile("Aahlad", "123", null, allowDownloadAccess = true, isAdmin = false)
    profileManagementController.addProfile("Prachi", "963", null, allowDownloadAccess = true, isAdmin = false)
    profileManagementController.addProfile("Rucha", "123", null, allowDownloadAccess = true, isAdmin = false)
    profileManagementController.addProfile("Sneha", "123", null, allowDownloadAccess = true, isAdmin = false)
    activity.setContentView(R.layout.profile_activity)
    if (getProfileChooserFragment() == null) {
      activity.supportFragmentManager.beginTransaction().add(
        R.id.profile_chooser_fragment_placeholder,
        ProfileChooserFragment()
      ).commitNow()
    }
  }

  private fun getProfileChooserFragment(): ProfileChooserFragment? {
    return activity.supportFragmentManager.findFragmentById(R.id.profile_chooser_fragment_placeholder) as ProfileChooserFragment?
  }
}
