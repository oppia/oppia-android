package org.oppia.android.app.profile

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.oppia.android.app.activity.ActivityScope
import org.oppia.android.app.model.ProfileChooserActivityParams.ParentScreen
import org.oppia.android.app.model.ProfileChooserFragmentArguments
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.testing.ProfileChooserFragmentTestActivity
import org.oppia.android.app.ui.R
import org.oppia.android.domain.profile.ProfileManagementController
import org.oppia.android.util.extensions.putProto
import org.oppia.android.util.platformparameter.EnableOnboardingFlowV2
import org.oppia.android.util.platformparameter.PlatformParameterValue
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.decorateWithUserProfileId
import javax.inject.Inject

/** Key for [ProfileChooserFragment]'s arguments. */
const val PROFILE_CHOOSER_ARGUMENTS_KEY = "ProfileChooserFragment.arguments"

/** The presenter for [ProfileChooserActivity]. */
@ActivityScope
class ProfileChooserActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val profileManagementController: ProfileManagementController,
  @EnableOnboardingFlowV2
  private val enableOnboardingFlowV2: PlatformParameterValue<Boolean>
) {
  /** Adds [ProfileChooserFragment] to view. */
  fun handleOnCreate(profileId: ProfileId, parentScreen: ParentScreen) {
    if (!enableOnboardingFlowV2.value) {
      // In the legacy flow, the default admin account is created here.
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
      val profileChooserFragment = ProfileChooserFragment()

      val fragmentArgs = ProfileChooserFragmentArguments
        .newBuilder()
        .setParentScreen(parentScreen)
        .build()

      val args = Bundle().apply {
        decorateWithUserProfileId(profileId)
        putProto(PROFILE_CHOOSER_ARGUMENTS_KEY, fragmentArgs)
      }

      profileChooserFragment.arguments = args

      activity.supportFragmentManager.beginTransaction().add(
        R.id.profile_chooser_fragment_placeholder,
        profileChooserFragment,
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
