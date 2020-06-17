package org.oppia.app.deprecation

import android.app.Dialog
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import javax.inject.Inject

class AutomaticAppDeprecationNoticeDialogFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity
) {
  private val deprecationNoticeExitAppListener by lazy {
    activity as DeprecationNoticeExitAppListener
  }

  fun handleOnCreateDialog(): Dialog {
    val dialog = AlertDialog.Builder(activity)
      .setTitle("Unsupported app version")
      .setMessage("This version of the app is no longer supported. Please update it through the Play Store.")
      .setNegativeButton("Close app") { _, _ ->
        deprecationNoticeExitAppListener.onCloseAppButtonClicked()
      }
      .setCancelable(false)
      .create()
    dialog.setCanceledOnTouchOutside(false)
    return dialog
  }
}
