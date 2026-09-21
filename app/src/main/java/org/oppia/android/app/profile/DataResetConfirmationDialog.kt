package org.oppia.android.app.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.AlertDialog
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import org.oppia.android.app.ui.R

/** Composable that represents the admin's reset app data confirmation dialog. */
@Composable
fun DataResetConfirmationDialog(
  onDismissRequest: () -> Unit,
  deleteAppData: () -> Unit
) {
  val appName = stringResource(R.string.app_name)
  var inputText by remember { mutableStateOf("") }
  val confirmationWord = "RESET"

  AlertDialog(
    title = {
      Text(
        stringResource(R.string.admin_confirm_app_wipe_title, appName)
      )
    },
    text = {
      Column {
        Text(stringResource(R.string.admin_confirm_app_wipe_message, appName))
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
          value = inputText,
          onValueChange = { inputText = it },
          label = { Text("Type '$confirmationWord' to confirm") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )
      }
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
        onClick = { deleteAppData() },
        enabled = inputText == confirmationWord
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
