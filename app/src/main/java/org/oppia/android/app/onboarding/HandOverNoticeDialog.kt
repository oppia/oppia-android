package org.oppia.android.app.onboarding

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.app.ui.R

@Composable
fun HandOverNoticeDialog(
  appLanguageResourceHandler: AppLanguageResourceHandler,
  learnerNickname: String,
  onDismiss: () -> Unit
) {
  Dialog(onDismissRequest = onDismiss) {
    Card(
      shape = RoundedCornerShape(16.dp),
      elevation = 8.dp,
      backgroundColor = colorResource(
        R.color.component_color_onboarding_hand_over_dialog_background_color
      ),
      modifier = Modifier.padding(12.dp)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .heightIn(max = 560.dp)
          .verticalScroll(rememberScrollState())
          .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Box(modifier = Modifier.fillMaxWidth()) {
          Text(
            text = appLanguageResourceHandler.getStringInLocaleWithWrapping(
              R.string.create_profile_activity_success_dialog_title, learnerNickname
            ),
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = colorResource(
              R.color.component_color_onboarding_hand_over_dialog_title_color
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier
              .fillMaxWidth()
              .padding(
                top = 8.dp,
                start = 48.dp,
                end = 48.dp,
                bottom = 12.dp
              )
          )

          IconButton(
            onClick = onDismiss,
            modifier = Modifier.align(Alignment.TopEnd)
          ) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = appLanguageResourceHandler.getStringInLocale(
                R.string.create_profile_activity_close_button_description
              ),
              tint = colorResource(
                R.color.component_color_onboarding_hand_over_dialog_content_color
              )
            )
          }
        }

        Text(
          text = appLanguageResourceHandler.getStringInLocaleWithWrapping(
            R.string.create_profile_activity_success_dialog_message, learnerNickname
          ),
          fontSize = 18.sp,
          color = colorResource(
            R.color.component_color_onboarding_hand_over_dialog_content_color
          ),
          textAlign = TextAlign.Center,
          modifier = Modifier.padding(horizontal = 16.dp)
        )

        Button(
          onClick = onDismiss,
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp),
          colors = ButtonDefaults.buttonColors(
            backgroundColor = colorResource(
              R.color.component_color_onboarding_hand_over_dialog_button_background_color
            )
          ),
          shape = RoundedCornerShape(6.dp)
        ) {
          Text(
            text = appLanguageResourceHandler
              .getStringInLocale(R.string.create_profile_activity_ok_button_text),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = colorResource(
              R.color.component_color_onboarding_hand_over_dialog_button_text_color
            )
          )
        }
      }
    }
  }
}
