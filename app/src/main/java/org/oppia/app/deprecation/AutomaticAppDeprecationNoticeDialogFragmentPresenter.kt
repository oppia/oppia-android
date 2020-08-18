package org.oppia.app.deprecation

import android.app.Dialog
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import org.oppia.app.R
import javax.inject.Inject

/**
 * Presenter for the dialog that shows when the pre-release app is being automatically deprecated.
 */
class AutomaticAppDeprecationNoticeDialogFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity
) {
  private val deprecationNoticeExitAppListener by lazy {
    activity as DeprecationNoticeExitAppListener
  }

  fun handleOnCreateDialog(): Dialog {
    val dialog = AlertDialog.Builder(activity)
      .setTitle(R.string.unsupported_app_version_dialog_title)
      .setMessage(R.string.unsupported_app_version_dialog_message)
      .setNegativeButton(R.string.unsupported_app_version_dialog_close_button_text) { _, _ ->
        deprecationNoticeExitAppListener.onCloseAppButtonClicked()
      }
      .setCancelable(false)
      .create()
    dialog.setCanceledOnTouchOutside(false)
    return dialog
  }
}
