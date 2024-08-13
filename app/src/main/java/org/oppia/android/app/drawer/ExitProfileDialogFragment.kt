package org.oppia.android.app.drawer

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.view.ContextThemeWrapper
import androidx.fragment.app.DialogFragment
import javax.inject.Inject
import org.oppia.android.R
import org.oppia.android.app.fragment.FragmentComponentImpl
import org.oppia.android.app.fragment.InjectableDialogFragment
import org.oppia.android.app.model.ExitProfileDialogArguments
import org.oppia.android.app.model.HighlightItem
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.profile.ProfileChooserActivity
import org.oppia.android.domain.profile.ProfileManagementController
import org.oppia.android.util.extensions.getProto
import org.oppia.android.util.extensions.putProto

/** [DialogFragment] that gives option to either cancel or exit current profile. */
class ExitProfileDialogFragment : InjectableDialogFragment() {

  companion object {
    // TODO(#1655): Re-restrict access to fields in tests post-Gradle.
    const val EXIT_PROFILE_DIALOG_ARGUMENTS_PROTO = "EXIT_PROFILE_DIALOG_ARGUMENT_PROTO"

    /**
     * This function is responsible for displaying content in DialogFragment.
     *
     * @return [ExitProfileDialogFragment]: DialogFragment
     */
    fun newInstance(
      exitProfileDialogArguments: ExitProfileDialogArguments
    ): ExitProfileDialogFragment {
      val exitProfileDialogFragment = ExitProfileDialogFragment()
      val args = Bundle()
      args.putProto(EXIT_PROFILE_DIALOG_ARGUMENTS_PROTO, exitProfileDialogArguments)
      exitProfileDialogFragment.arguments = args
      return exitProfileDialogFragment
    }
  }

  lateinit var exitProfileDialogInterface: ExitProfileDialogInterface

  override fun onAttach(context: Context) {
    super.onAttach(context)
    (fragmentComponent as FragmentComponentImpl).inject(this)
  }

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    val args =
      checkNotNull(arguments) { "Expected arguments to be pass to ExitProfileDialogFragment" }

    val exitProfileDialogArguments = args.getProto(
      EXIT_PROFILE_DIALOG_ARGUMENTS_PROTO,
      ExitProfileDialogArguments.getDefaultInstance()
    )

    val restoreLastCheckedItem = when (exitProfileDialogArguments.highlightItem) {
      HighlightItem.ADMINISTRATOR_CONTROLS_ITEM,
      HighlightItem.DEVELOPER_OPTIONS_ITEM,
      HighlightItem.LAST_CHECKED_MENU_ITEM -> true
      else -> false
    }

    val alertDialog = AlertDialog
      .Builder(ContextThemeWrapper(activity as Context, R.style.OppiaAlertDialogTheme))
      .setMessage(R.string.home_activity_back_dialog_message)
      .setNegativeButton(R.string.home_activity_back_dialog_cancel) { dialog, _ ->
        dialog.dismiss()
      }
      .setPositiveButton(R.string.home_activity_back_dialog_exit) { _, _ ->
        ProfileId.newBuilder()
          .setLoggedInInternalProfileId(0)
          .setLoggedOut(true)
          .build()
        // TODO(#3641): Investigate on using finish instead of intent.
        val intent = ProfileChooserActivity.createProfileChooserActivity(activity!!)
        if (!restoreLastCheckedItem) {
          intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        activity!!.startActivity(intent)
      }
      .create()
    alertDialog.setCanceledOnTouchOutside(false)
    return alertDialog
  }

  override fun onDismiss(dialog: DialogInterface) {
    super.onDismiss(dialog)
    val args =
      checkNotNull(arguments) { "Expected arguments to be pass to ExitProfileDialogFragment" }

    val exitProfileDialogArguments = args.getProto(
      EXIT_PROFILE_DIALOG_ARGUMENTS_PROTO,
      ExitProfileDialogArguments.getDefaultInstance()
    )

    val restoreLastCheckedItem = when (exitProfileDialogArguments.highlightItem) {
      HighlightItem.ADMINISTRATOR_CONTROLS_ITEM,
      HighlightItem.DEVELOPER_OPTIONS_ITEM,
      HighlightItem.LAST_CHECKED_MENU_ITEM -> true
      else -> false
    }

    if (restoreLastCheckedItem) {
      exitProfileDialogInterface =
        parentFragment as ExitProfileDialogInterface
      exitProfileDialogInterface.unhighlightSwitchProfileMenuItem()
      if (exitProfileDialogArguments.highlightItem == HighlightItem.LAST_CHECKED_MENU_ITEM) {
        exitProfileDialogInterface.highlightLastCheckedMenuItem()
      } else if (exitProfileDialogArguments.highlightItem == HighlightItem.DEVELOPER_OPTIONS_ITEM) {
        exitProfileDialogInterface.highlightDeveloperOptionsItem()
      } else {
        exitProfileDialogInterface.highlightAdministratorControlsItem()
      }
    }
  }
}
