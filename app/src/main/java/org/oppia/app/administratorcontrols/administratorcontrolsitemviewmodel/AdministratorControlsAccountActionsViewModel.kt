package org.oppia.app.administratorcontrols.administratorcontrolsitemviewmodel

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import org.oppia.app.IntentFactoryShimInterface
import org.oppia.app.vm.R
import javax.inject.Inject

/** [ViewModel] for the recycler view in [AdministratorControlsFragment]. */
class AdministratorControlsAccountActionsViewModel(
  private val fragment: Fragment,
  private val intentFactoryShimInterface: IntentFactoryShimInterface
) : AdministratorControlsItemViewModel() {

  fun onLogOutClicked() {
    AlertDialog.Builder(fragment.context!!, R.style.AlertDialogTheme)
      .setMessage(R.string.log_out_dialog_message)
      .setNegativeButton(R.string.log_out_dialog_cancel_button) { dialog, _ ->
        dialog.dismiss()
      }
      .setPositiveButton(R.string.log_out_dialog_okay_button) { _, _ ->
        // TODO(#762): Replace [ProfileChooserActivity] to [LoginActivity] once it is added.
        val intent = intentFactoryShimInterface.createProfileActivityIntent(fragment.activity!!)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        fragment.activity!!.startActivity(intent)
        fragment.activity!!.finish()
      }.create().show()
  }

}
