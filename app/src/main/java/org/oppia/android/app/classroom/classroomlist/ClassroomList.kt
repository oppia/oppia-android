package org.oppia.android.app.classroom.classroomlist

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.oppia.android.R
import org.oppia.android.app.classroom.getDrawableResource
import org.oppia.android.app.home.classroomlist.ClassroomSummaryViewModel

/** Test tag for the header of the classroom section. */
const val CLASSROOM_HEADER_TEST_TAG = "TEST_TAG.classroom_header"

/** Test tag for the classroom list. */
const val CLASSROOM_LIST_TEST_TAG = "TEST_TAG.classroom_list"

/** Displays a list of classroom summaries with a header. */
@Composable
fun ClassroomList(
  classroomSummaryList: List<ClassroomSummaryViewModel>,
  selectedClassroomId: String
) {
  Column(
    modifier = Modifier
      .background(
        color = colorResource(id = R.color.component_color_shared_screen_primary_background_color)
      )
      .fillMaxWidth(),
  ) {
    Text(
      text = stringResource(id = R.string.classrooms_list_activity_section_header),
      color = colorResource(id = R.color.component_color_shared_primary_text_color),
      fontFamily = FontFamily.SansSerif,
      fontWeight = FontWeight.Medium,
      fontSize = 18.sp,
      modifier = Modifier
        .testTag(CLASSROOM_HEADER_TEST_TAG)
        .padding(
          start = dimensionResource(id = R.dimen.classrooms_text_margin_start),
          top = dimensionResource(id = R.dimen.classrooms_text_margin_top),
          end = dimensionResource(id = R.dimen.classrooms_text_margin_end),
          bottom = dimensionResource(id = R.dimen.classrooms_text_margin_bottom),
        ),
    )
    LazyRow(
      modifier = Modifier.testTag(CLASSROOM_LIST_TEST_TAG),
      contentPadding = PaddingValues(
        start = dimensionResource(id = R.dimen.classrooms_text_margin_start),
        end = dimensionResource(id = R.dimen.classrooms_text_margin_end),
      ),
    ) {
      items(classroomSummaryList) {
        ClassroomCard(classroomSummaryViewModel = it, selectedClassroomId)
      }
    }
  }
}

/** Displays a single classroom card with an image and text, handling click events. */
@Composable
fun ClassroomCard(
  classroomSummaryViewModel: ClassroomSummaryViewModel,
  selectedClassroomId: String
) {
  val screenWidth = LocalConfiguration.current.screenWidthDp
  val isPortrait = LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT
  val isTablet = if (isPortrait) screenWidth > 600 else screenWidth > 840
  val isCardSelected = classroomSummaryViewModel.classroomSummary.classroomId == selectedClassroomId
  Card(
    modifier = Modifier
      .height(dimensionResource(id = R.dimen.classrooms_card_height))
      .width(dimensionResource(id = R.dimen.classrooms_card_width))
      .padding(
        start = dimensionResource(R.dimen.promoted_story_card_layout_margin_start),
        end = dimensionResource(R.dimen.promoted_story_card_layout_margin_end),
      )
      .clickable {
        classroomSummaryViewModel.handleClassroomClick()
      },
    backgroundColor = if (isCardSelected) {
      colorResource(id = R.color.component_color_classroom_card_color)
    } else {
      colorResource(id = R.color.component_color_shared_screen_primary_background_color)
    },
    border = BorderStroke(2.dp, color = colorResource(id = R.color.color_def_oppia_green)),
    elevation = 4.dp,
  ) {
    Column(
      modifier = Modifier.padding(all = 20.dp),
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      if (isPortrait || isTablet) { // Hides the classroom icon for landscape phone layouts.
        Image(
          painter = painterResource(
            id = classroomSummaryViewModel
              .classroomSummary
              .classroomThumbnail
              .getDrawableResource()
          ),
          contentDescription = classroomSummaryViewModel.title,
          modifier = Modifier
            .padding(bottom = 20.dp)
            .size(80.dp),
        )
      }
      Text(
        text = classroomSummaryViewModel.title,
        color = colorResource(id = R.color.component_color_classroom_card_text_color),
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
      )
    }
  }
}
