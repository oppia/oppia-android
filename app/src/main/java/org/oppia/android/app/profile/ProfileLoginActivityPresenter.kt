package org.oppia.android.app.profile

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import org.oppia.android.app.activity.ActivityScope
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.ui.R
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.decorateWithUserProfileId
import javax.inject.Inject

/** Tag for identifying [AdminSettingsDialogFragment] in transactions. */
const val TAG_VALIDATE_ADMIN_PIN_DIALOG = "ADMIN_SETTINGS_DIALOG"

/** Tag for identifying [ResetPinDialogFragment] in transactions. */
const val TAG_ADMIN_RESET_PIN_DIALOG = "RESET_PIN_DIALOG"

private const val TAG_PROFILE_LOGIN_FRAGMENT = "TAG_PROFILE_LOGIN_FRAGMENT"

/** The presenter for [ProfileLoginActivity]. */
@ActivityScope
class ProfileLoginActivityPresenter @Inject constructor(private val activity: AppCompatActivity) {
  /** Creates the view for [ProfileLoginActivity]. */
  fun handleOnCreate(profileId: ProfileId) {
    activity.setContentView(R.layout.profile_login_activity)

    if (getProfileLoginFragment() == null) {
      val profileLoginFragment = ProfileLoginFragment().apply {
        arguments = Bundle().also { it.decorateWithUserProfileId(profileId) }
      }

      activity.supportFragmentManager.beginTransaction()
        .add(
          R.id.profile_login_fragment_placeholder, profileLoginFragment, TAG_PROFILE_LOGIN_FRAGMENT
        )
        .commitNow()
    }
  }

  private fun getProfileLoginFragment(): ProfileLoginFragment? {
    return activity.supportFragmentManager.findFragmentByTag(
      TAG_PROFILE_LOGIN_FRAGMENT
    ) as? ProfileLoginFragment
  }

  fun handleRouteToResetPinDialog(profileId: ProfileId, profileName: String) {
    (
      activity
        .supportFragmentManager
        .findFragmentByTag(
          TAG_VALIDATE_ADMIN_PIN_DIALOG
        ) as DialogFragment
      ).dismiss()
    val dialogFragment = ResetPinDialogFragment.newInstance(
      profileId.internalId,
      profileName
    )
    dialogFragment.showNow(activity.supportFragmentManager, TAG_ADMIN_RESET_PIN_DIALOG)
  }

  fun handleRouteToSuccessDialog() {
  }
}
