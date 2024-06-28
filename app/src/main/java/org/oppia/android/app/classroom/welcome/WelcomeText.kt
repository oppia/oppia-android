package org.oppia.android.app.classroom.welcome

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.oppia.android.R
import org.oppia.android.app.home.WelcomeViewModel

/** Test tag for the welcome section. */
const val WELCOME_TEST_TAG = "TEST_TAG.welcome"

/** Displays a welcome text with an underline. */
@Composable
fun WelcomeText(welcomeViewModel: WelcomeViewModel) {
  val outerPadding = dimensionResource(id = R.dimen.home_welcome_outer_padding)
  val textMarginEnd = dimensionResource(id = R.dimen.home_welcome_text_view_margin_end)
  val greetingLineColor = colorResource(
    id = R.color.component_color_home_activity_layout_greeting_text_line_color
  )

  Text(
    text = welcomeViewModel.computeWelcomeText(),
    modifier = Modifier
      .testTag(WELCOME_TEST_TAG)
      .padding(
        start = outerPadding,
        top = outerPadding,
        end = outerPadding + textMarginEnd,
      )
      .drawBehind {
        val strokeWidthPx = 6.dp.toPx()
        val verticalOffset = size.height + 4.dp.toPx()
        drawLine(
          color = greetingLineColor,
          strokeWidth = strokeWidthPx,
          start = Offset(x = 0f, y = verticalOffset),
          end = Offset(x = size.width, y = verticalOffset),
        )
      },
    color = colorResource(id = R.color.component_color_shared_primary_text_color),
    fontSize = dimensionResource(id = R.dimen.home_welcome_text_size).value.sp,
    fontFamily = FontFamily.SansSerif,
  )
}
