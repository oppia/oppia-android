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
  fun handleOnCreate() {
    // TODO(#482): Ensures that an admin profile is present. Remove when there is proper admin account creation.
    profileManagementController.addProfile("Sean", "12345", null, true, true)
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
