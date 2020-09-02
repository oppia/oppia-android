package org.oppia.app.drawer

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.view.ContextThemeWrapper
import androidx.fragment.app.DialogFragment
import org.oppia.app.R
import org.oppia.app.profile.ProfileChooserActivity

/** [DialogFragment] that gives option to either cancel or exit current profile */
class ExitProfileDialogFragment : DialogFragment() {

  companion object {
    /**
     * This function is responsible for displaying content in DialogFragment.
     *
     * @return [ExitProfileDialogFragment]: DialogFragment
     */
    fun newInstance(): ExitProfileDialogFragment {
      return ExitProfileDialogFragment()
    }
  }

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

    val exitProfileDialogInterface: ExitProfileDialogInterface =
      parentFragment as ExitProfileDialogInterface

    return AlertDialog
      .Builder(ContextThemeWrapper(activity as Context, R.style.AlertDialogTheme))
      .setMessage(R.string.home_activity_back_dialog_message)
      .setNegativeButton(R.string.home_activity_back_dialog_cancel) { dialog, _ ->
        exitProfileDialogInterface.markHomeMenuCloseDrawer()
        dialog.dismiss()
      }
      .setPositiveButton(R.string.home_activity_back_dialog_exit) { _, _ ->
        // TODO(#322): Need to start intent for ProfileChooserActivity to get update. Change to finish when live data bug is fixed.
        val intent = ProfileChooserActivity.createProfileChooserActivity(activity!!)
        activity!!.startActivity(intent)
      }
      .create()
  }
}
