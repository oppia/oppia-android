package org.oppia.android.app.settings.profile

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import org.oppia.android.R
import org.oppia.android.app.administratorcontrols.AdministratorControlsActivity
import org.oppia.android.app.fragment.FragmentComponentImpl
import org.oppia.android.app.fragment.InjectableDialogFragment

class DeleteProfileSuccessDialogFragment : InjectableDialogFragment() {

  companion object {
    const val TAG_DELETE_DIALOG_FRAGMENT = "TAG_DELETE_DIALOG_FRAGMENT"

    fun newInstance(): DeleteProfileSuccessDialogFragment {
      return DeleteProfileSuccessDialogFragment()
    }
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    (fragmentComponent as FragmentComponentImpl).inject(this)
  }

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    return AlertDialog.Builder(requireContext(), R.style.OppiaAlertDialogTheme)
      .setMessage(R.string.profile_edit_delete_successful_message)
      .setPositiveButton(R.string.log_out_dialog_okay_button) { _, _ ->
        if (requireContext().resources.getBoolean(R.bool.isTablet)) {
          val intent =
            Intent(requireContext(), AdministratorControlsActivity::class.java)
          intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
          startActivity(intent)
        } else {
          val intent = Intent(requireContext(), ProfileListActivity::class.java)
          intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
          startActivity(intent)
        }
      }.create()
  }
}