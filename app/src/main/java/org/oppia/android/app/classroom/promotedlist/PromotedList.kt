package org.oppia.android.app.classroom.promotedlist

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.oppia.android.R
import org.oppia.android.app.home.promotedlist.PromotedStoryListViewModel
import org.oppia.android.app.home.promotedlist.PromotedStoryViewModel

@Composable
fun PromotedStoryList(promotedStoryListViewModel: PromotedStoryListViewModel) {
  Text(
    text = promotedStoryListViewModel.getHeader(),
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
  LazyRow(
    modifier = Modifier
      .padding(
        start = dimensionResource(id = R.dimen.promoted_story_list_layout_margin_start),
        end = dimensionResource(id = R.dimen.promoted_story_list_layout_margin_end),
        bottom = 20.dp,
      )
  ) {
    items(promotedStoryListViewModel.promotedStoryList) {
      PromotedStoryCard(promotedStoryViewModel = it)
    }
  }
}

@Composable
fun PromotedStoryCard(promotedStoryViewModel: PromotedStoryViewModel) {
  Card(
    modifier = Modifier
      .width(width = dimensionResource(id = R.dimen.promoted_story_card_layout_width))
      .padding(
        start = dimensionResource(R.dimen.promoted_story_card_layout_margin_start),
        end = dimensionResource(R.dimen.promoted_story_card_layout_margin_end),
        bottom = 8.dp,
      )
      .clickable { promotedStoryViewModel.clickOnStoryTile() },
    backgroundColor = Color.Transparent,
    elevation = 4.dp,
  ) {
    Column(
      verticalArrangement = Arrangement.Center,
    ) {
      Image(
        painter = painterResource(id = promotedStoryViewModel.thumbnailResourceId),
        contentDescription = "${promotedStoryViewModel.storyTitle} Card",
        modifier = Modifier
          .aspectRatio(16f / 9f)
          .background(
            Color(
              (0xff000000L or promotedStoryViewModel.promotedStory.lessonThumbnail
                .backgroundColorRgb.toLong()).toInt()
            )
          )
      )
      Text(
        text = promotedStoryViewModel.nextChapterTitle,
        modifier = Modifier.padding(start = 16.dp, top = 8.dp, end = 16.dp),
        color = colorResource(id = R.color.component_color_shared_primary_text_color),
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
        textAlign = TextAlign.Start,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
      /*Text(
        text = promotedStoryViewModel.storyTitle,
        modifier = Modifier.padding(start = 16.dp, top = 4.dp, end = 16.dp),
        color = colorResource(id = R.color.component_color_shared_primary_text_color),
        fontFamily = FontFamily.SansSerif,
        fontSize = 16.sp,
        textAlign = TextAlign.Start,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )*/
      Text(
        text = promotedStoryViewModel.topicTitle.uppercase(),
        modifier = Modifier.padding(start = 16.dp, top = 8.dp, end = 16.dp),
        color = colorResource(id = R.color.component_color_shared_story_card_topic_name_text_color),
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Light,
        fontSize = 14.sp,
        textAlign = TextAlign.Start,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
      Text(
        text = promotedStoryViewModel.classroomTitle.uppercase(),
        modifier = Modifier
          .padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 8.dp)
          .border(
            width = 2.dp,
            color = colorResource(id = R.color.color_def_persian_blue),
            shape = RoundedCornerShape(50)
          )
          .padding(horizontal = 16.dp, vertical = 6.dp),
        color = colorResource(id = R.color.color_def_persian_blue),
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        textAlign = TextAlign.Start,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
    }
  }
}
