package org.oppia.android.app.profile

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
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
  private val fragmentManager = activity.supportFragmentManager

  /** Creates the view for [ProfileLoginActivity]. */
  fun handleOnCreate(profileId: ProfileId) {
    activity.setContentView(R.layout.profile_login_activity)

    if (getProfileLoginFragment() == null) {
      val profileLoginFragment = ProfileLoginFragment().apply {
        arguments = Bundle().also { it.decorateWithUserProfileId(profileId) }
      }

      fragmentManager.beginTransaction()
        .add(
          R.id.profile_login_fragment_placeholder, profileLoginFragment, TAG_PROFILE_LOGIN_FRAGMENT
        )
        .commitNow()
    }
  }

  /** Handles showing the [ResetPinDialogFragment]. */
  fun handleRouteToResetPinDialog(profileId: ProfileId, profileName: String) {
    val adminPinDialog = fragmentManager.findFragmentByTag(TAG_VALIDATE_ADMIN_PIN_DIALOG)
      as DialogFragment
    adminPinDialog.dismiss()

    val resetPinDialog = ResetPinDialogFragment.newInstance(profileId.internalId, profileName)

    resetPinDialog.showNow(fragmentManager, TAG_ADMIN_RESET_PIN_DIALOG)
  }

  /** Handles showing the reset pin success dialog. */
  fun handleRouteToSuccessDialog() {
    val resetPinDialog = fragmentManager.findFragmentByTag(TAG_ADMIN_RESET_PIN_DIALOG)
      as DialogFragment
    resetPinDialog.dismiss()

    // Before showing the success dialog, we need to force all pending transactions to complete
    // immediately, since there is a series of dialogs leading to one another. This helps prevent
    // stacking of dialogs and errors.
    fragmentManager.executePendingTransactions()

    showSuccessDialog()
  }

  private fun showSuccessDialog() {
    AlertDialog.Builder(activity, R.style.OppiaAlertDialogTheme)
      .setMessage(R.string.profile_login_reset_pin_success_dialog_message)
      .setPositiveButton(R.string.profile_login_reset_pin_success_dialog_close) { dialog, _ ->
        dialog.dismiss()
      }.create().show()
  }

  private fun getProfileLoginFragment(): ProfileLoginFragment? {
    return fragmentManager.findFragmentByTag(
      TAG_PROFILE_LOGIN_FRAGMENT
    ) as? ProfileLoginFragment
  }
}
