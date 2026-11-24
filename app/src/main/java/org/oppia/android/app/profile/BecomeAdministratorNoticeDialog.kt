package org.oppia.android.app.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.app.ui.R

/** Notice shown before a sole learner becomes an administrator to add another learner. */
@Composable
fun BecomeAdministratorNoticeDialog(
  appLanguageResourceHandler: AppLanguageResourceHandler,
  onContinue: () -> Unit
) {
  Dialog(
    onDismissRequest = {},
    properties = DialogProperties(
      dismissOnBackPress = false,
      dismissOnClickOutside = false
    )
  ) {
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(8.dp),
      elevation = 8.dp,
      backgroundColor = colorResource(
        R.color.component_color_onboarding_hand_over_dialog_background_color
      )
    ) {
      Column(modifier = Modifier.padding(24.dp)) {
        Text(
          text = appLanguageResourceHandler.getStringInLocaleWithWrapping(
            R.string.profile_selection_become_administrator_notice_title
          ),
          color = colorResource(
            R.color.component_color_onboarding_hand_over_dialog_title_color
          ),
          fontSize = 22.sp,
          fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
          text = appLanguageResourceHandler.getStringInLocaleWithWrapping(
            R.string.profile_selection_become_administrator_notice_message
          ),
          color = colorResource(
            R.color.component_color_onboarding_hand_over_dialog_content_color
          ),
          fontSize = 16.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
          text = appLanguageResourceHandler.getStringInLocaleWithWrapping(
            R.string.profile_selection_become_administrator_notice_description
          ),
          color = colorResource(
            R.color.component_color_onboarding_hand_over_dialog_content_color
          ),
          fontSize = 16.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        NoticeItem(
          text = appLanguageResourceHandler.getStringInLocaleWithWrapping(
            R.string.profile_selection_become_administrator_notice_manage_profiles
          )
        )
        NoticeItem(
          text = appLanguageResourceHandler.getStringInLocaleWithWrapping(
            R.string.profile_selection_become_administrator_notice_add_learners
          )
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
          onClick = onContinue,
          modifier = Modifier.fillMaxWidth(),
          colors = ButtonDefaults.buttonColors(
            backgroundColor = colorResource(
              R.color.component_color_onboarding_hand_over_dialog_button_background_color
            )
          ),
          shape = RoundedCornerShape(4.dp)
        ) {
          Text(
            text = appLanguageResourceHandler.getStringInLocale(
              R.string.profile_selection_become_administrator_notice_continue
            ),
            color = colorResource(
              R.color.component_color_onboarding_hand_over_dialog_button_text_color
            ),
            fontWeight = FontWeight.Bold
          )
        }
      }
    }
  }
}

@Composable
private fun NoticeItem(text: String) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 6.dp),
    horizontalArrangement = Arrangement.spacedBy(12.dp),
    verticalAlignment = Alignment.Top
  ) {
    Text(
      text = "✓",
      color = colorResource(R.color.component_color_onboarding_hand_over_dialog_title_color),
      fontSize = 22.sp,
      fontWeight = FontWeight.Bold
    )
    Text(
      text = text,
      color = colorResource(R.color.component_color_onboarding_hand_over_dialog_content_color),
      fontSize = 16.sp
    )
  }
}
