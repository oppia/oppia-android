package org.oppia.android.app.notice

import android.app.Dialog
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import org.oppia.android.R
import org.oppia.android.app.splash.DeprecationNoticeActionType
import javax.inject.Inject

/** Presenter class responsible for showing an OS deprecation dialog to the user. */
class OsDeprecationNoticeDialogFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity
) {
  private val deprecationNoticeExitAppListener by lazy {
    activity as DeprecationNoticeActionListener
  }

  fun handleOnCreateDialog(): Dialog {
    val dialog = AlertDialog.Builder(activity, R.style.DeprecationAlertDialogTheme)
      .setTitle(R.string.os_deprecation_dialog_title)
      .setMessage(R.string.os_deprecation_dialog_message)
      .setNegativeButton(R.string.os_deprecation_dialog_dismiss_button_text) { _, _ ->
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