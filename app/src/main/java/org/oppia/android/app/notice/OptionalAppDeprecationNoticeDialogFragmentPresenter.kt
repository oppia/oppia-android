package org.oppia.android.app.notice

import android.app.Dialog
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import org.oppia.android.R
import org.oppia.android.app.splash.DeprecationNoticeActionType
import javax.inject.Inject

/** Presenter class responsible for showing an optional update dialog to the user. */
class OptionalAppDeprecationNoticeDialogFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity
) {
  private val deprecationNoticeExitAppListener by lazy {
    activity as DeprecationNoticeActionListener
  }

  fun handleOnCreateDialog(): Dialog {
    val dialog = AlertDialog.Builder(activity, R.style.DeprecationAlertDialogTheme)
      .setTitle(R.string.optional_app_update_dialog_title)
      .setMessage(R.string.optional_app_update_dialog_message)
      .setPositiveButton(R.string.optional_app_update_dialog_update_button_text) { _, _ ->
        deprecationNoticeExitAppListener.onPositiveActionButtonClicked(
          DeprecationNoticeActionType.UPDATE
        )
      }
      .setNegativeButton(R.string.optional_app_update_dialog_dismiss_button_text) { _, _ ->
        deprecationNoticeExitAppListener.onPositiveActionButtonClicked(
          DeprecationNoticeActionType.DISMISS
        )
      }
      .setCancelable(false)
      .create()
    dialog.setCanceledOnTouchOutside(false)
    return dialog
  }
}
