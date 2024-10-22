package org.oppia.android.app.profile

import androidx.appcompat.app.AppCompatActivity
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityScope
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.ProfileType
import org.oppia.android.app.testing.ProfileChooserFragmentTestActivity
import org.oppia.android.domain.profile.ProfileManagementController
import org.oppia.android.util.platformparameter.EnableOnboardingFlowV2
import org.oppia.android.util.platformparameter.PlatformParameterValue
import javax.inject.Inject

/** The presenter for [ProfileChooserActivity]. */
@ActivityScope
class ProfileChooserActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val profileManagementController: ProfileManagementController,
  @EnableOnboardingFlowV2
  private val enableOnboardingFlowV2: PlatformParameterValue<Boolean>
) {
  /** Adds [ProfileChooserFragment] to view. */
  fun handleOnCreate(profileId: ProfileId, profileType: ProfileType) {
    if (enableOnboardingFlowV2.value) {
      profileManagementController.updateNewProfileDetails(
        profileId = profileId,
        profileType = profileType,
        newName = "Admin",
        avatarImagePath = null,
        colorRgb = -10710042,
        isAdmin = true
      )
    } else {
      // TODO(#482): Ensures that an admin profile is present.
      // This can be removed once the new onboarding flow is finalized, as it will handle the creation of an admin profile.
      profileManagementController.addProfile(
        name = "Admin",
        pin = "",
        avatarImagePath = null,
        allowDownloadAccess = true,
        colorRgb = -10710042,
        isAdmin = true
      )
    }

    activity.setContentView(R.layout.profile_chooser_activity)
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
