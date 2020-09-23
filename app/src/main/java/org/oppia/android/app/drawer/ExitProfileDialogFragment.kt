package org.oppia.android.app.drawer

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.view.ContextThemeWrapper
import androidx.fragment.app.DialogFragment
import org.oppia.android.app.R
import org.oppia.android.app.profile.ProfileChooserActivity

/** [DialogFragment] that gives option to either cancel or exit current profile. */
class ExitProfileDialogFragment : DialogFragment() {

  companion object {
    // TODO(#1655): Re-restrict access to fields in tests post-Gradle.
    const val BOOL_IS_FROM_NAVIGATION_DRAWER_EXTRA_KEY =
      "BOOL_IS_FROM_NAVIGATION_DRAWER_EXTRA_KEY"

    /**
     * This function is responsible for displaying content in DialogFragment.
     *
     * @return [ExitProfileDialogFragment]: DialogFragment
     */
    fun newInstance(isFromNavigationDrawer: Boolean): ExitProfileDialogFragment {
      val exitProfileDialogFragment = ExitProfileDialogFragment()
      val args = Bundle()
      args.putBoolean(BOOL_IS_FROM_NAVIGATION_DRAWER_EXTRA_KEY, isFromNavigationDrawer)
      exitProfileDialogFragment.arguments = args
      return exitProfileDialogFragment
    }
  }

  lateinit var exitProfileDialogInterface: ExitProfileDialogInterface

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    val args =
      checkNotNull(arguments) { "Expected arguments to be pass to ExitProfileDialogFragment" }

    val isFromNavigationDrawer = args.getBoolean(
      BOOL_IS_FROM_NAVIGATION_DRAWER_EXTRA_KEY,
      false
    )

    if (isFromNavigationDrawer) {
      exitProfileDialogInterface =
        parentFragment as ExitProfileDialogInterface
    }

    return AlertDialog
      .Builder(ContextThemeWrapper(activity as Context, R.style.AlertDialogTheme))
      .setMessage(R.string.home_activity_back_dialog_message)
      .setNegativeButton(R.string.home_activity_back_dialog_cancel) { dialog, _ ->
        if (isFromNavigationDrawer) {
          exitProfileDialogInterface.markHomeMenuCloseDrawer()
        }
        dialog.dismiss()
      }
      .setPositiveButton(R.string.home_activity_back_dialog_exit) { _, _ ->
        // TODO(#322): Need to start intent for ProfileChooserActivity to get update. Change to finish when live data bug is fixed.
        val intent = ProfileChooserActivity.createProfileChooserActivity(activity!!)
        if (!isFromNavigationDrawer) {
          intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        activity!!.startActivity(intent)
      }
      .create()
  }
}
