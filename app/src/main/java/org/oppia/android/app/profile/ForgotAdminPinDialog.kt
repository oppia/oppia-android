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

/** Test tag for the admin's forgot pin dialog. */
const val ADMIN_FORGOT_PIN_DIALOG_TEST_TAG = "TEST_TAG.admin_forgot_pin"

/** Composable that represents the admin's forgot pin dialog. */
@Composable
fun ForgotAdminPinDialog(
  onDismissRequest: () -> Unit,
  onConfirmation: () -> Unit
) {
  val appName = stringResource(R.string.app_name)

  AlertDialog(
    title = {
      Text(stringResource(R.string.profile_login_forgot_pin_dialog_title))
    },
    text = {
      Text(stringResource(R.string.profile_login_forgot_pin_dialog_message, appName))
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
        Text(stringResource(R.string.profile_login_forgot_pin_dialog_cancel_button))
      }
    },
    confirmButton = {
      TextButton(
        onClick = { onConfirmation() }
      ) {
        Text(stringResource(R.string.profile_login_forgot_pin_dialog_reset_button, appName))
      }
    },
    modifier = Modifier.testTag(ADMIN_FORGOT_PIN_DIALOG_TEST_TAG)
  )
}
