package org.oppia.android.app.classroom.topiclist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import org.oppia.android.R

/** Test tag for the all topics section header. */
const val ALL_TOPICS_HEADER_TEST_TAG = "TEST_TAG.all_topics_header"

/** Displays the header text for the topic list section. */
@Composable
fun AllTopicsHeaderText() {
  Text(
    text = stringResource(id = R.string.select_a_topic_to_start),
    color = colorResource(id = R.color.component_color_classroom_all_topics_header_text_color),
    fontFamily = FontFamily.SansSerif,
    fontWeight = FontWeight.Medium,
    fontSize = dimensionResource(id = R.dimen.all_topics_text_size).value.sp,
    modifier = Modifier
      .testTag(ALL_TOPICS_HEADER_TEST_TAG)
      .fillMaxWidth()
      .background(colorResource(
        id = R.color.component_color_classroom_topic_list_background_color)
      )
      .padding(
        start = dimensionResource(id = R.dimen.all_topics_text_margin_start),
        top = dimensionResource(id = R.dimen.all_topics_text_margin_top),
        end = dimensionResource(id = R.dimen.all_topics_text_margin_end),
        bottom = dimensionResource(id = R.dimen.all_topics_text_margin_bottom),
      ),
  )
}
