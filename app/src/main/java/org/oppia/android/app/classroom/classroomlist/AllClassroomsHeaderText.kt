package org.oppia.android.app.classroom.classroomlist

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

/** Test tag for the all classrooms section header. */
const val ALL_CLASSROOMS_HEADER_TEST_TAG = "TEST_TAG.all_classrooms_header"

/** Displays the header text for the classroom list section. */
@Composable
fun AllClassroomsHeaderText() {
  Text(
    text = stringResource(id = R.string.classrooms_list_activity_section_header),
    color = colorResource(id = R.color.component_color_classroom_shared_header_text_color),
    fontFamily = FontFamily.SansSerif,
    fontWeight = FontWeight.Normal,
    fontSize = dimensionResource(id = R.dimen.classrooms_list_header_text_size).value.sp,
    modifier = Modifier
      .testTag(ALL_CLASSROOMS_HEADER_TEST_TAG)
      .padding(
        start = dimensionResource(id = R.dimen.classrooms_text_margin_start),
        top = dimensionResource(id = R.dimen.classrooms_text_margin_top),
        end = dimensionResource(id = R.dimen.classrooms_text_margin_end),
      ),
  )
}
