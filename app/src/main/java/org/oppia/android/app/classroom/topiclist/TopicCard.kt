package org.oppia.android.app.classroom.topiclist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import org.oppia.android.R
import org.oppia.android.app.classroom.ThumbnailImage
import org.oppia.android.app.home.topiclist.TopicSummaryViewModel

/** Displays a card with the topic summary information. */
@Composable
fun TopicCard(topicSummaryViewModel: TopicSummaryViewModel) {
  Card(
    modifier = Modifier
      .padding(
        start = dimensionResource(R.dimen.topic_card_margin_start),
        end = dimensionResource(R.dimen.topic_card_margin_end),
      )
      .clickable { topicSummaryViewModel.clickOnSummaryTile() },
    elevation = dimensionResource(id = R.dimen.topic_card_elevation),
  ) {
    Column(
      verticalArrangement = Arrangement.Center,
    ) {
      ThumbnailImage(
        entityId = topicSummaryViewModel.topicSummary.topicId,
        entityType = topicSummaryViewModel.entityType,
        lessonThumbnail = topicSummaryViewModel.topicSummary.topicThumbnail,
        modifier = Modifier.aspectRatio(4f / 3f)
      )
      TopicCardTextSection(topicSummaryViewModel)
    }
  }
}

/** Displays text section of the topic card. */
@Composable
fun TopicCardTextSection(topicSummaryViewModel: TopicSummaryViewModel) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .background(
        color = colorResource(
          id = R.color.component_color_shared_topic_card_item_background_color
        )
      ),
    verticalArrangement = Arrangement.SpaceBetween,
  ) {
    Text(
      text = topicSummaryViewModel.title,
      modifier = Modifier
        .padding(
          start = dimensionResource(id = R.dimen.topic_list_item_text_padding),
          top = dimensionResource(id = R.dimen.topic_list_item_text_padding),
          end = dimensionResource(id = R.dimen.topic_list_item_text_padding)
        ),
      color = colorResource(id = R.color.component_color_shared_secondary_4_text_color),
      fontFamily = FontFamily.SansSerif,
      fontSize = dimensionResource(id = R.dimen.topic_list_item_text_size).value.sp,
      textAlign = TextAlign.Start,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
    )
    Text(
      text = topicSummaryViewModel.computeLessonCountText(),
      modifier = Modifier
        .padding(all = dimensionResource(id = R.dimen.topic_list_item_text_padding)),
      color = colorResource(id = R.color.component_color_shared_secondary_4_text_color),
      fontFamily = FontFamily.SansSerif,
      fontWeight = FontWeight.Light,
      fontSize = dimensionResource(id = R.dimen.topic_list_item_text_size).value.sp,
      fontStyle = FontStyle.Italic,
      textAlign = TextAlign.Start,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
    )
  }
}
