package org.oppia.android.app.classroom.topiclist

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.oppia.android.R

@Composable
fun AllTopicsHeaderText() {
  Text(
    text = stringResource(id = R.string.select_a_topic_to_start),
    color = colorResource(id = R.color.component_color_shared_primary_text_color),
    fontFamily = FontFamily.SansSerif,
    fontWeight = FontWeight.Medium,
    fontSize = 18.sp,
    modifier = Modifier
      .padding(
        start = dimensionResource(id = R.dimen.promoted_story_list_layout_margin_start),
        top = 24.dp,
        end = dimensionResource(id = R.dimen.promoted_story_list_layout_margin_end),
        bottom = 20.dp,
      )
  )
}
