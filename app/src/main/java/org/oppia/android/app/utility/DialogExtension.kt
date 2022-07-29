package org.oppia.android.app.utility

import android.content.Context
import android.content.DialogInterface
import androidx.annotation.StyleRes
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.oppia.android.R

/*
* MaterialAlertDialogBuilder Extension
* */
fun Context.alertDialog(
  @StyleRes style: Int = 0,
  dialogBuilder: MaterialAlertDialogBuilder.() -> Unit
) {
  MaterialAlertDialogBuilder(this, style)
    .apply {
      setCancelable(false)
      dialogBuilder()
      create()
      show()
    }
}

fun MaterialAlertDialogBuilder.negativeButton(
  text: String? = context.getString(R.string.no),
  onClick: (dialogInterface: DialogInterface) -> Unit = { it.dismiss() }
) {
  this.setNegativeButton(text) { dialogInterface, _ ->
    onClick(dialogInterface)
  }
}

fun MaterialAlertDialogBuilder.positiveButton(
  text: String? = context.getString(R.string.yes),
  onClick: (dialogInterface: DialogInterface) -> Unit = { it.dismiss() }
) {
  this.setPositiveButton(text) { dialogInterface, _ ->
    onClick(dialogInterface)
  }
}
