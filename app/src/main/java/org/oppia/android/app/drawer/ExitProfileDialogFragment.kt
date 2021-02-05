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
    const val EXIT_PROFILE_DIALOG_ARGUMENTS_PROTO = "EXIT_PROFILE_DIALOG_ARGUMENTS_PROTO"

    /**
     * This function is responsible for displaying content in DialogFragment.
     *
     * @return [ExitProfileDialogFragment]: DialogFragment
     */
    fun newInstance(
      restoreLastCheckedMenuItem: Boolean,
      argument: Argument
    ): ExitProfileDialogFragment {
      val exitProfileDialogFragment = ExitProfileDialogFragment()
      val args = Bundle()
      val exitProfileDialogArguments = createExitProfileDialogFragmentProto(
        argument = argument,
        restoreLastCheckedMenuItem = restoreLastCheckedMenuItem
      )

      args.putProto(EXIT_PROFILE_DIALOG_ARGUMENTS_PROTO, exitProfileDialogArguments)

      exitProfileDialogFragment.arguments = args
      return exitProfileDialogFragment
    }

    private fun createExitProfileDialogFragmentProto(
      argument: Argument,
      restoreLastCheckedMenuItem: Boolean
    ): ExitProfileDialogArguments {
      return when (argument) {
        is Argument.IsAdministratorControlsSelected -> {
          ExitProfileDialogArguments.newBuilder()
            .setRestoreLastCheckedMenuItem(restoreLastCheckedMenuItem)
            .setIsAdministratorControlsSelected(argument.value)
            .build()
        }
        is Argument.LastCheckedMenuItem -> {
          ExitProfileDialogArguments.newBuilder()
            .setRestoreLastCheckedMenuItem(restoreLastCheckedMenuItem)
            .setLastCheckedMenuItemValue(argument.navigationDrawerItem.value)
            .build()
        }
      }
    }
  }

  private lateinit var exitProfileDialogInterface: ExitProfileDialogInterface

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    val args =
      checkNotNull(arguments) { "Expected arguments to be pass to ExitProfileDialogFragment" }

    val exitProfileDialogArguments = args.getProto(
      EXIT_PROFILE_DIALOG_ARGUMENTS_PROTO,
      ExitProfileDialogArguments.getDefaultInstance()
    )

    val restoreLastCheckedMenuItem = exitProfileDialogArguments.restoreLastCheckedMenuItem
    val argument = exitProfileDialogArguments.adminControlsOrNavDrawerItemsCase

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
            when (argument.number) {
              2 -> Argument.IsAdministratorControlsSelected(
                exitProfileDialogArguments.isAdministratorControlsSelected
              )
              else -> Argument.LastCheckedMenuItem(
                getNavigationDrawerItem(
                  exitProfileDialogArguments.lastCheckedMenuItemValue
                )
              )
            }
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

  private fun getNavigationDrawerItem(value: Int): NavigationDrawerItem {
    return when (value) {
      0 -> NavigationDrawerItem.HOME
      1 -> NavigationDrawerItem.OPTIONS
      2 -> NavigationDrawerItem.HELP
      3 -> NavigationDrawerItem.DOWNLOADS
      4 -> NavigationDrawerItem.SWITCH_PROFILE
      else -> NavigationDrawerItem.HOME
    }
  }
}
