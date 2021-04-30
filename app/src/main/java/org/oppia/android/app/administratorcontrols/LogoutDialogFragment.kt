package org.oppia.android.app.administratorcontrols

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import org.oppia.android.R
import org.oppia.android.app.profile.ProfileChooserActivity

/** [DialogFragment] that gives option to either cancel or log out from current profile. */
class LogoutDialogFragment : DialogFragment() {

  companion object {
    const val TAG_LOGOUT_DIALOG_FRAGMENT = "TAG_LOGOUT_DIALOG_FRAGMENT"

    fun newInstance(): LogoutDialogFragment {
      return LogoutDialogFragment()
    }
  }

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    return AlertDialog.Builder(requireContext(), R.style.AlertDialogTheme)
      .setMessage(R.string.log_out_dialog_message)
      .setNegativeButton(R.string.log_out_dialog_cancel_button) { dialog, _ ->
        dialog.dismiss()
      }
      .setPositiveButton(R.string.log_out_dialog_okay_button) { _, _ ->
        val intent = ProfileChooserActivity.createProfileChooserActivity(activity!!)
        startActivity(intent)
      }.create()
  }
}
