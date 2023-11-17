package org.oppia.android.app.administratorcontrols

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import org.oppia.android.R
import org.oppia.android.app.fragment.FragmentComponentImpl
import org.oppia.android.app.fragment.InjectableDialogFragment
import org.oppia.android.app.profile.ProfileChooserActivity

/** [DialogFragment] that gives option to either cancel or log out from current profile. */
class LogoutDialogFragment : InjectableDialogFragment() {

  companion object {
    /** [String] key to access [LogoutDialogFragment]. */
    const val TAG_LOGOUT_DIALOG_FRAGMENT = "TAG_LOGOUT_DIALOG_FRAGMENT"

    /** Returns a new [LogoutDialogFragment] instance. */
    fun newInstance(): LogoutDialogFragment {
      return LogoutDialogFragment()
    }
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    (fragmentComponent as FragmentComponentImpl).inject(this)
  }

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    return AlertDialog.Builder(requireContext(), R.style.OppiaAlertDialogTheme)
      .setMessage(R.string.log_out_dialog_message_text)
      .setNegativeButton(R.string.log_out_dialog_cancel_button_text) { dialog, _ ->
        dialog.dismiss()
      }
      .setPositiveButton(R.string.log_out_dialog_okay_button_text) { _, _ ->
        val intent = ProfileChooserActivity.createProfileChooserActivity(activity!!)
        startActivity(intent)
      }.create()
  }
}
