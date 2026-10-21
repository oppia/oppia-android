package org.oppia.android.app.profile

import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.DialogProperties
import org.oppia.android.app.ui.R

/** Composable that represents the admin's forgot pin dialog. */
@Composable
fun AdminPinRecoveryDialog(
  onDismissRequest: () -> Unit,
  onConfirmation: () -> Unit
) {
  val appName = stringResource(R.string.app_name)

  AlertDialog(
    backgroundColor = colorResource(R.color.component_color_profile_login_dialog_background_color),
    title = {
      Text(
        text = stringResource(R.string.profile_login_forgot_pin_dialog_title),
        color = colorResource(R.color.component_color_profile_login_dialog_title_text_color)
      )
    },
    text = {
      Text(
        text = stringResource(R.string.profile_login_forgot_pin_dialog_message, appName),
        color = colorResource(R.color.component_color_profile_login_dialog_message_text_color)
      )
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
        Text(
          text = stringResource(R.string.profile_login_forgot_pin_dialog_cancel_button),
          color = colorResource(R.color.component_color_profile_login_dialog_button_text_color)
        )
      }
    },
    confirmButton = {
      TextButton(
        onClick = { onConfirmation() }
      ) {
        Text(
          text = stringResource(R.string.profile_login_forgot_pin_dialog_reset_button, appName),
          color = colorResource(R.color.component_color_profile_login_dialog_button_text_color)
        )
      }
    }
  )
}
