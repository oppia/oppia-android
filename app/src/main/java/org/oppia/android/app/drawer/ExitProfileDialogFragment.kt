package org.oppia.android.app.drawer

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.view.ContextThemeWrapper
import androidx.fragment.app.DialogFragment
import org.oppia.android.R
import org.oppia.android.app.profile.ProfileChooserActivity

/** [DialogFragment] that gives option to either cancel or exit current profile. */
class ExitProfileDialogFragment : DialogFragment() {

  companion object {
    // TODO(#1655): Re-restrict access to fields in tests post-Gradle.
    const val BOOL_RESTORE_LAST_CHECKED_MENU_ITEM_KEY =
      "BOOL_RESTORE_LAST_CHECKED_MENU_ITEM_KEY"

    /**
     * This function is responsible for displaying content in DialogFragment.
     *
     * @return [ExitProfileDialogFragment]: DialogFragment
     */
    fun newInstance(restoreLastCheckedMenuItem: Boolean): ExitProfileDialogFragment {
      val exitProfileDialogFragment = ExitProfileDialogFragment()
      val args = Bundle()
      args.putBoolean(BOOL_RESTORE_LAST_CHECKED_MENU_ITEM_KEY, restoreLastCheckedMenuItem)
      exitProfileDialogFragment.arguments = args
      return exitProfileDialogFragment
    }
  }

  lateinit var exitProfileDialogInterface: ExitProfileDialogInterface

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    val args =
      checkNotNull(arguments) { "Expected arguments to be pass to ExitProfileDialogFragment" }

    val restoreLastCheckedMenuItem = args.getBoolean(
      BOOL_RESTORE_LAST_CHECKED_MENU_ITEM_KEY,
      false
    )

    if (restoreLastCheckedMenuItem) {
      exitProfileDialogInterface =
        parentFragment as ExitProfileDialogInterface
    }

    val alertDialog = AlertDialog
      .Builder(ContextThemeWrapper(activity as Context, R.style.AlertDialogTheme))
      .setMessage(R.string.home_activity_back_dialog_message)
      .setNegativeButton(R.string.home_activity_back_dialog_cancel) { dialog, _ ->
        if (restoreLastCheckedMenuItem) {
          exitProfileDialogInterface.restoreLastCheckedMenuItem()
        }
        dialog.dismiss()
      }
      .setPositiveButton(R.string.home_activity_back_dialog_exit) { _, _ ->
        // TODO(#322): Need to start intent for ProfileChooserActivity to get update. Change to finish when live data bug is fixed.
        val intent = ProfileChooserActivity.createProfileChooserActivity(activity!!)
        if (!restoreLastCheckedMenuItem) {
          intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        activity!!.startActivity(intent)
      }
      .create()
    alertDialog.setCanceledOnTouchOutside(false)
    return alertDialog
  }
}
