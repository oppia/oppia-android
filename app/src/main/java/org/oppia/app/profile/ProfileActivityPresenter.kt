package org.oppia.app.profile

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import org.oppia.app.R
import org.oppia.app.activity.ActivityScope
import org.oppia.app.model.ProfileId
import org.oppia.domain.profile.ProfileManagementController
import javax.inject.Inject

/** The presenter for [ProfileActivity]. */
@ActivityScope
class
ProfileActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity, private val profileManagementController: ProfileManagementController
) {
  fun handleOnCreate() {
    profileManagementController.addProfile("James", "12345", null, true, true)
    //profileManagementController.deleteProfile(ProfileId.newBuilder().setInternalId(2).build())
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
