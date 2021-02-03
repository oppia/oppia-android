package org.oppia.android.app.drawer

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.view.ContextThemeWrapper
import androidx.fragment.app.DialogFragment
import org.oppia.android.R
import org.oppia.android.app.model.ExitProfileDialogArguments
import org.oppia.android.app.profile.ProfileChooserActivity
import org.oppia.android.util.extensions.getProto
import org.oppia.android.util.extensions.putProto

/** [DialogFragment] that gives option to either cancel or exit current profile. */
class ExitProfileDialogFragment : DialogFragment() {

  companion object {
    // TODO(#1655): Re-restrict access to fields in tests post-Gradle.
    const val EXIT_PROFILE_DIALOG_ARGUMENTS = "EXIT_PROFILE_DIALOG_ARGUMENNTS"

    /**
     * This function is responsible for displaying content in DialogFragment.
     *
     * @return [ExitProfileDialogFragment]: DialogFragment
     */
    fun newInstance(
      restoreLastCheckedMenuItem: Boolean,
      isAdministratorControlsSelected: Boolean,
      lastCheckedItemId: Int
    ): ExitProfileDialogFragment {
      val exitProfileDialogFragment = ExitProfileDialogFragment()
      val args = Bundle()
      val exitProfileDialogArguments =
        ExitProfileDialogArguments.newBuilder()
          .setRestoreLastCheckedMenuItem(restoreLastCheckedMenuItem)
          .setIsAdministratorControlsSelected(isAdministratorControlsSelected)
          .setLastCheckedMenuItemId(lastCheckedItemId).build()
      args.putProto(EXIT_PROFILE_DIALOG_ARGUMENTS, exitProfileDialogArguments)

      exitProfileDialogFragment.arguments = args
      return exitProfileDialogFragment
    }
  }

  private lateinit var exitProfileDialogInterface: ExitProfileDialogInterface

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    val args =
      checkNotNull(arguments) { "Expected arguments to be pass to ExitProfileDialogFragment" }

    val exitProfileDialogArguments = args.getProto(
      EXIT_PROFILE_DIALOG_ARGUMENTS,
      ExitProfileDialogArguments.getDefaultInstance()
    )

    val restoreLastCheckedMenuItem = exitProfileDialogArguments.restoreLastCheckedMenuItem
    val isAdministratorControlsSelected = exitProfileDialogArguments.isAdministratorControlsSelected
    val lastCheckedItemId = exitProfileDialogArguments.lastCheckedMenuItemId

    if (restoreLastCheckedMenuItem) {
      exitProfileDialogInterface =
        parentFragment as ExitProfileDialogInterface
    }

    return AlertDialog
      .Builder(ContextThemeWrapper(activity as Context, R.style.AlertDialogTheme))
      .setMessage(R.string.home_activity_back_dialog_message)
      .setNegativeButton(R.string.home_activity_back_dialog_cancel) { dialog, _ ->
        if (restoreLastCheckedMenuItem) {
          exitProfileDialogInterface.checkLastCheckedItemAndCloseDrawer(
            lastCheckedItemId,
            isAdministratorControlsSelected
          )
          exitProfileDialogInterface.unCheckSwitchProfileItemAndCloseDrawer()
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
  }
}
