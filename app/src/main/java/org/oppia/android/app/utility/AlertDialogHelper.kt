package org.oppia.android.app.utility

import android.content.Context
import android.content.DialogInterface
import android.view.View
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import org.oppia.android.R

/** AlertDialogHelper helps to create AlertDialog. */
class AlertDialogHelper {

  companion object {
    fun getAlertDialog(
      context: Context,
      view: View,
      @StringRes title: Int,
      @StringRes message: Int,
      @StringRes positiveButtonText: Int,
      @StringRes negativeButtonText: Int,
      alertDialogListener: (DialogInterface, Int) -> Unit
    ): AlertDialog {
      val dialog = AlertDialog.Builder(context, R.style.AlertDialogTheme)
        .setTitle(title)
        .setView(view)
        .setMessage(message)
        .setPositiveButton(positiveButtonText, null)
        .setNegativeButton(negativeButtonText, null)
        .create()

      // This logic prevents the dialog from being dismissed. https://stackoverflow.com/a/7636468.
      dialog.setOnShowListener {
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
          alertDialogListener.invoke(dialog, AlertDialog.BUTTON_POSITIVE)
        }
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener {
          alertDialogListener.invoke(dialog, AlertDialog.BUTTON_NEGATIVE)
        }
      }
      return dialog
    }
  }
}
