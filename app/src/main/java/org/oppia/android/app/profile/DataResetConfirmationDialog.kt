package org.oppia.android.app.profile

import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.colorResource
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
    backgroundColor = colorResource(R.color.component_color_profile_login_dialog_background_color),
    title = {
      Text(
        text = stringResource(R.string.admin_confirm_app_wipe_title, appName),
        color = colorResource(R.color.component_color_profile_login_dialog_title_text_color)
      )
    },
    text = {
      Text(
        text = stringResource(R.string.admin_confirm_app_wipe_message, appName),
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
          text = stringResource(R.string.admin_confirm_app_wipe_negative_button_text),
          color = colorResource(R.color.component_color_profile_login_dialog_button_text_color)
        )
      }
    },
    confirmButton = {
      TextButton(
        onClick = { deleteAppData() }
      ) {
        Text(
          text = stringResource(
            R.string.admin_confirm_app_wipe_positive_button_text,
            appName
          ),
          color = colorResource(R.color.component_color_profile_login_dialog_button_text_color)
        )
      }
    }
  )
}
