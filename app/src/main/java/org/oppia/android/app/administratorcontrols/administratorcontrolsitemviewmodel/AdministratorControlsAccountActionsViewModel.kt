package org.oppia.android.app.administratorcontrols.administratorcontrolsitemviewmodel

import android.content.Intent
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import org.oppia.android.R
import org.oppia.android.app.shim.IntentFactoryShim

private var isDialogVisible = false

/** [ViewModel] for the recycler view in [AdministratorControlsFragment]. */
class AdministratorControlsAccountActionsViewModel(
  private val fragment: Fragment,
  private val intentFactoryShim: IntentFactoryShim
) : AdministratorControlsItemViewModel() {

  fun onLogOutClicked() {
    isDialogVisible = true
    AlertDialog.Builder(fragment.context!!, R.style.AlertDialogTheme)
      .setMessage(R.string.log_out_dialog_message)
      .setNegativeButton(R.string.log_out_dialog_cancel_button) { dialog, _ ->
        dialog.dismiss()
      }
      .setPositiveButton(R.string.log_out_dialog_okay_button) { _, _ ->
        // TODO(#762): Replace [ProfileChooserActivity] to [LoginActivity] once it is added.
        val intent = intentFactoryShim.createProfileChooserActivityIntent(fragment.activity!!)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        fragment.activity!!.startActivity(intent)
        fragment.activity!!.finish()
      }.setOnDismissListener {
        isDialogVisible = false
      }.create().show()
  }

  fun getDialog(): Boolean = isDialogVisible
}
