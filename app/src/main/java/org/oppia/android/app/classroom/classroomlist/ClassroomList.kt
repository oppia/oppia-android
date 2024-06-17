package org.oppia.android.app.classroom.classroomlist

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.oppia.android.R
import org.oppia.android.app.home.classroomlist.ClassroomSummaryViewModel

@Composable
fun ClassroomList(classroomSummaryList: List<ClassroomSummaryViewModel>) {
  Column(
    modifier = Modifier
      .background(
        color = colorResource(
          id = R.color.component_color_shared_screen_primary_background_color
        )
      )
      .fillMaxWidth()
  ) {
    Text(
      text = stringResource(id = R.string.classrooms),
      color = colorResource(id = R.color.component_color_shared_primary_text_color),
      fontFamily = FontFamily.SansSerif,
      fontWeight = FontWeight.Medium,
      fontSize = 18.sp,
      modifier = Modifier
        .padding(
          start = dimensionResource(id = R.dimen.classrooms_text_margin_start),
          top = dimensionResource(id = R.dimen.classrooms_text_margin_top),
          end = dimensionResource(id = R.dimen.classrooms_text_margin_end),
          bottom = dimensionResource(id = R.dimen.classrooms_text_margin_bottom),
        )
    )
    LazyRow(
      modifier = Modifier
        .padding(
          start = dimensionResource(id = R.dimen.classrooms_text_margin_start),
          end = dimensionResource(id = R.dimen.classrooms_text_margin_end),
          bottom = dimensionResource(id = R.dimen.classrooms_text_margin_bottom),
        )
    ) {
      items(classroomSummaryList) {
        ClassroomCard(classroomSummaryViewModel = it)
      }
    }
  }
}

@Composable
fun ClassroomCard(classroomSummaryViewModel: ClassroomSummaryViewModel) {
  val isOrientationPortrait =
    LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT
  Card(
    modifier = Modifier
      .height(if (isOrientationPortrait) 182.dp else 60.dp)
      .width(150.dp)
      .padding(
        start = dimensionResource(R.dimen.promoted_story_card_layout_margin_start),
        end = dimensionResource(R.dimen.promoted_story_card_layout_margin_end),
      ),
    backgroundColor = colorResource(id = R.color.component_color_shared_screen_primary_background_color),
    border = BorderStroke(2.dp, color = colorResource(id = R.color.color_def_oppia_green)),
    elevation = 4.dp,
  ) {
    Column(
      modifier = Modifier.padding(all = 20.dp),
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      if (isOrientationPortrait) {
        Image(
          painter = painterResource(id = classroomSummaryViewModel.thumbnailResourceId),
          contentDescription = "${classroomSummaryViewModel.title} Card",
          modifier = Modifier
            .padding(bottom = 20.dp)
            .size(80.dp),
        )
      }
      Text(
        text = classroomSummaryViewModel.title,
        color = colorResource(id = R.color.component_color_shared_primary_text_color),
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
      )
    }
  }
}
