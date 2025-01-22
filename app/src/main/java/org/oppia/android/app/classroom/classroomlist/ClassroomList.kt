package org.oppia.android.app.classroom.classroomlist

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.oppia.android.R
import org.oppia.android.app.classroom.ThumbnailImage
import org.oppia.android.app.home.classroomlist.ClassroomSummaryViewModel

/** Test tag for the classroom list. */
const val CLASSROOM_LIST_TEST_TAG = "TEST_TAG.classroom_list"

/** Test tag for the classroom card icon. */
const val CLASSROOM_CARD_ICON_TEST_TAG = "TEST_TAG.classroom_card_icon"

/** Displays a list of classroom summaries with a header. */
@Composable
fun ClassroomList(
  classroomSummaryList: List<ClassroomSummaryViewModel>,
  selectedClassroomId: String,
  isSticky: Boolean,
) {
  LazyRow(
    modifier = Modifier
      .testTag(CLASSROOM_LIST_TEST_TAG)
      .background(
        color = colorResource(id = R.color.component_color_shared_screen_primary_background_color)
      ),
    contentPadding = PaddingValues(
      start = dimensionResource(id = R.dimen.classrooms_text_margin_start),
      top = dimensionResource(id = R.dimen.classrooms_text_margin_bottom),
      end = dimensionResource(id = R.dimen.classrooms_text_margin_end),
    ),
  ) {
    items(classroomSummaryList) {
      ClassroomCard(classroomSummaryViewModel = it, selectedClassroomId, isSticky)
    }
  }
}

/** Displays a single classroom card with an image and text, handling click events. */
@Composable
fun ClassroomCard(
  classroomSummaryViewModel: ClassroomSummaryViewModel,
  selectedClassroomId: String,
  isSticky: Boolean,
) {
  val isCardSelected = classroomSummaryViewModel.classroomSummary.classroomId == selectedClassroomId
  Card(
    modifier = Modifier
      .width(getClassroomCardWidth())
      .padding(
        start = dimensionResource(R.dimen.classrooms_card_margin_start),
        end = dimensionResource(R.dimen.classrooms_card_margin_end),
      )
      .clickable {
        classroomSummaryViewModel.handleClassroomClick()
      },
    backgroundColor = if (isCardSelected) {
      colorResource(id = R.color.component_color_classroom_card_selected_color)
    } else {
      colorResource(id = R.color.component_color_classroom_card_color)
    },
    border = BorderStroke(2.dp, color = colorResource(id = R.color.color_def_oppia_green)),
    elevation = dimensionResource(id = R.dimen.classrooms_card_elevation),
  ) {
    Column(
      modifier = Modifier.padding(
        horizontal = dimensionResource(id = R.dimen.classrooms_card_padding_horizontal),
        vertical = if (isSticky) {
          dimensionResource(id = R.dimen.classrooms_card_collapsed_padding_vertical)
        } else {
          dimensionResource(id = R.dimen.classrooms_card_padding_vertical)
        },
      ),
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      AnimatedVisibility(visible = !isSticky) {
        ThumbnailImage(
          entityId = classroomSummaryViewModel.classroomSummary.classroomId,
          entityType = classroomSummaryViewModel.entityType,
          lessonThumbnail = classroomSummaryViewModel.classroomSummary.classroomThumbnail,
          modifier = Modifier
            .testTag("${CLASSROOM_CARD_ICON_TEST_TAG}_${classroomSummaryViewModel.title}")
            .padding(bottom = dimensionResource(id = R.dimen.classrooms_card_icon_padding_bottom))
            .size(size = dimensionResource(id = R.dimen.classrooms_card_icon_size)),
        )
      }
      Text(
        text = classroomSummaryViewModel.title,
        color = colorResource(id = R.color.component_color_classroom_card_text_color),
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = dimensionResource(id = R.dimen.classrooms_card_label_text_size).value.sp,
      )
    }
  }
}

@Composable
private fun getClassroomCardWidth(): Dp {
  val configuration = LocalConfiguration.current
  val screenWidth = configuration.screenWidthDp.dp
  val horizontalPadding = dimensionResource(id = R.dimen.classrooms_text_margin_start)
  val topicCardHorizontalMargin = 8.dp
  val topicListSpanCount = integerResource(id = R.integer.home_span_count)

  val totalTopicCardWidth = screenWidth -
    (horizontalPadding.times(2) + (topicCardHorizontalMargin * (topicListSpanCount - 1) * 2))

  return totalTopicCardWidth.div(topicListSpanCount)
}
