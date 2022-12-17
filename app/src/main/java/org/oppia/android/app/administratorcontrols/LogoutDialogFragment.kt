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
    const val TAG_LOGOUT_DIALOG_FRAGMENT = "TAG_LOGOUT_DIALOG_FRAGMENT"

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
      .setMessage(R.string.administrator_controls_activity_log_out_dialog_fragment_message)
      .setNegativeButton(
        R.string.administrator_controls_activity_log_out_dialog_fragment_cancel_button
      ) { dialog, _ ->
        dialog.dismiss()
      }
      .setPositiveButton(
        R.string.administrator_controls_activity_log_out_dialog_fragment_okay_button
      ) { _, _ ->
        val intent = ProfileChooserActivity.createProfileChooserActivity(activity!!)
        startActivity(intent)
      }.create()
  }
}
