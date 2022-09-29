package org.oppia.android.app.settings.profile

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import org.oppia.android.R
import org.oppia.android.app.administratorcontrols.AdministratorControlsActivity
import org.oppia.android.app.fragment.FragmentComponentImpl
import org.oppia.android.app.fragment.InjectableDialogFragment

/** [DialogFragment] that notifies the user after a profile is successfully deleted. */
class DeleteProfileSuccessDialogFragment : InjectableDialogFragment() {

  companion object {
    /** Argument key for Profile Deletion Success Dialog in [ProfileEditFragmentPresenter]. */
    const val DELETE_PROFILE_SUCCESS_DIALOG_FRAGMENT_TAG = "DELETE_PROFILE_SUCCESS_DIALOG_FRAGMENT"

    /** This function returns a new instance of [DeleteProfileSuccessDialogFragment]. */
    fun createNewInstance(): DeleteProfileSuccessDialogFragment {
      return DeleteProfileSuccessDialogFragment()
    }
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    (fragmentComponent as FragmentComponentImpl).inject(this)
  }

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

    val alertDialog = AlertDialog
      .Builder(requireContext(), R.style.OppiaAlertDialogTheme).apply {
        setMessage(R.string.profile_edit_delete_successful_message)
        setPositiveButton(R.string.log_out_dialog_okay_button) { _, _ ->
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
        }
      }.create()
    alertDialog.setCanceledOnTouchOutside(true)
    return alertDialog
  }
}
