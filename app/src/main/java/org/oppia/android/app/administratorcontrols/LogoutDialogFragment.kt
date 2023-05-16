package org.oppia.android.app.administratorcontrols

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import org.oppia.android.R
import org.oppia.android.app.activity.route.ActivityRouter
import org.oppia.android.app.fragment.InjectableDialogFragment
import org.oppia.android.app.model.DestinationScreen
import org.oppia.android.app.model.ProfileChooserActivityParams
import javax.inject.Inject

/** [DialogFragment] that gives option to either cancel or log out from current profile. */
class LogoutDialogFragment : InjectableDialogFragment() {

  companion object {
    const val TAG_LOGOUT_DIALOG_FRAGMENT = "TAG_LOGOUT_DIALOG_FRAGMENT"

    fun newInstance(): LogoutDialogFragment {
      return LogoutDialogFragment()
    }
  }

  @Inject lateinit var activityRouter: ActivityRouter

  override fun onAttach(context: Context) {
    super.onAttach(context)
    (fragmentComponent as Injector).inject(this)
  }

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    return AlertDialog.Builder(requireContext(), R.style.OppiaAlertDialogTheme)
      .setMessage(R.string.log_out_dialog_message)
      .setNegativeButton(R.string.log_out_dialog_cancel_button) { dialog, _ ->
        dialog.dismiss()
      }
      .setPositiveButton(R.string.log_out_dialog_okay_button) { _, _ ->
        activityRouter.routeToScreen(
          DestinationScreen.newBuilder()
            .setProfileChooserActivityParams(ProfileChooserActivityParams.getDefaultInstance())
            .build()
        )
      }.create()
  }

  interface Injector {
    fun inject(fragment: LogoutDialogFragment)
  }
}
