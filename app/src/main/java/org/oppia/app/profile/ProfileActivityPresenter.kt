package org.oppia.app.profile

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import org.oppia.app.R
import org.oppia.app.activity.ActivityScope
import org.oppia.domain.profile.ProfileManagementController
import javax.inject.Inject

/** The presenter for [ProfileActivity]. */
@ActivityScope
class
ProfileActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity, private val profileManagementController: ProfileManagementController
) {
  fun handleOnCreate() {
    profileManagementController.addProfile("James", "12345", null, allowDownloadAccess = true, isAdmin = true)
    profileManagementController.addProfile("Ben", "234", null, allowDownloadAccess = true, isAdmin = false)
    profileManagementController.addProfile("Sean", "345", null, allowDownloadAccess = true, isAdmin = false)
    profileManagementController.addProfile("Rajat", "456", null, allowDownloadAccess = true, isAdmin = false)
    profileManagementController.addProfile("Veena", "567", null, allowDownloadAccess = true, isAdmin = false)
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
