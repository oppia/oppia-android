package org.oppia.android.app.administratorcontrols

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import org.oppia.android.app.ui.R

@Composable
fun AdministratorControlsDialog(onDismiss: () -> Unit) {
  Dialog(onDismissRequest = onDismiss) {
    Card(
      shape = RoundedCornerShape(16.dp),
      elevation = 8.dp,
      modifier = Modifier.padding(12.dp)
    ) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .padding(24.dp)
      ) {

        IconButton(
          onClick = onDismiss,
          modifier = Modifier.align(Alignment.TopEnd)
        ) {
          Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Close",
            tint = colorResource(R.color.component_color_shared_black_background_color)
          )
        }

        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Text(
            text = "Welcome to the Administrator Controls Page!",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = colorResource(R.color.component_color_onboarding_shared_green_color),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 12.dp)
          )

          Text(
            text = "On this page, you can add new learners, or change the profile settings for " +
              "all learners.",
            fontSize = 18.sp,
            color = colorResource(R.color.component_color_shared_primary_text_color),
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
                R.color.component_color_onboarding_shared_green_color
              )
            ),
          ) {
            Text("OK, LET'S GO", fontWeight = FontWeight.Bold, color = Color.White)
          }
        }
      }
    }
  }
}
