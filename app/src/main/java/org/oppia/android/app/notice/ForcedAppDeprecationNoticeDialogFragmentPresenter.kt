package org.oppia.android.app.notice

import android.app.Dialog
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import org.oppia.android.R
import org.oppia.android.app.splash.DeprecationNoticeActionType
import javax.inject.Inject

/** Presenter class responsible for showing an app deprecation dialog to the user. */
class ForcedAppDeprecationNoticeDialogFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity
) {
  private val deprecationNoticeExitAppListener by lazy {
    activity as DeprecationNoticeActionListener
  }

  fun handleOnCreateDialog(): Dialog {
    val dialog = AlertDialog.Builder(activity, R.style.DeprecationAlertDialogTheme)
      .setTitle(R.string.unsupported_app_version_dialog_title)
      .setMessage(R.string.unsupported_app_version_dialog_message)
      .setPositiveButton(R.string.unsupported_app_version_dialog_update_button_text) { _, _ ->
        deprecationNoticeExitAppListener.onPositiveActionButtonClicked(
          DeprecationNoticeActionType.UPDATE
        )
      }
      .setNegativeButton(R.string.unsupported_app_version_dialog_close_button_text) { _, _ ->
        deprecationNoticeExitAppListener.onNegativeActionButtonClicked(
          DeprecationNoticeActionType.CLOSE
        )
      }
      .setCancelable(false)
      .create()
    dialog.setCanceledOnTouchOutside(false)
    return dialog
  }
}
