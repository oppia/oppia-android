package org.oppia.android.app.profile

import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.DialogProperties
import org.oppia.android.app.ui.R

/** Composable that represents the admin's reset app data confirmation dialog. */
@Composable
fun DataResetConfirmationDialog(
  onDismissRequest: () -> Unit,
  deleteAppData: () -> Unit
) {
  val appName = stringResource(R.string.app_name)

  AlertDialog(
    title = {
      Text(
        stringResource(R.string.admin_confirm_app_wipe_title, appName)
      )
    },
    text = {
      Text(stringResource(R.string.admin_confirm_app_wipe_message, appName))
    },
    properties = DialogProperties(
      dismissOnClickOutside = false,
      dismissOnBackPress = false
    ),
    onDismissRequest = { onDismissRequest() },
    dismissButton = {
      TextButton(
        onClick = { onDismissRequest() }
      ) {
        Text(stringResource(R.string.admin_confirm_app_wipe_negative_button_text))
      }
    },
    confirmButton = {
      TextButton(
        onClick = { deleteAppData() }
      ) {
        Text(
          stringResource(
            R.string.admin_confirm_app_wipe_positive_button_text,
            appName
          )
        )
      }
    }
  )
}
