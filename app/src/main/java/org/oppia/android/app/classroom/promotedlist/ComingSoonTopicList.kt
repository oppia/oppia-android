package org.oppia.android.app.classroom.promotedlist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.oppia.android.app.classroom.ThumbnailImage
import org.oppia.android.app.home.promotedlist.ComingSoonTopicListViewModel
import org.oppia.android.app.home.promotedlist.ComingSoonTopicsViewModel
import org.oppia.android.app.ui.R
import org.oppia.android.app.ui.theme.OppiaTheme
import org.oppia.android.util.locale.OppiaLocale

/** Test tag for the header of the promoted story list. */
const val COMING_SOON_TOPIC_LIST_HEADER_TEST_TAG = "TEST_TAG.coming_soon_topic_list_header"

/** Test tag for the promoted story list. */
const val COMING_SOON_TOPIC_LIST_TEST_TAG = "TEST_TAG.coming_soon_topic_list"

/** Displays a list of topics to be published soon. */
@Composable
fun ComingSoonTopicList(
  comingSoonTopicListViewModel: ComingSoonTopicListViewModel,
  machineLocale: OppiaLocale.MachineLocale,
) {
  Text(
    text = stringResource(id = R.string.coming_soon),
    color = colorResource(id = R.color.component_color_shared_primary_text_color),
    fontFamily = FontFamily.SansSerif,
    fontWeight = FontWeight.Medium,
    fontSize = dimensionResource(id = R.dimen.coming_soon_topic_list_header_text_size).value.sp,
    modifier = Modifier
      .padding(
        start = OppiaTheme.spacing.medium,
        top = OppiaTheme.spacing.medium,
        end = OppiaTheme.spacing.medium,
      )
      .testTag(COMING_SOON_TOPIC_LIST_HEADER_TEST_TAG),
  )
  LazyRow(
    modifier = Modifier
      .padding(
        top = OppiaTheme.spacing.small
      )
      .testTag(COMING_SOON_TOPIC_LIST_TEST_TAG),
    contentPadding = PaddingValues(
      start = OppiaTheme.spacing.medium,
      end = OppiaTheme.spacing.medium,
    ),
  ) {
    items(comingSoonTopicListViewModel.comingSoonTopicList) {
      ComingSoonTopicCard(
        comingSoonTopicsViewModel = it,
        machineLocale = machineLocale,
      )
    }
  }
}

/** Displays a card with the coming soon topic summary information. */
@Composable
fun ComingSoonTopicCard(
  comingSoonTopicsViewModel: ComingSoonTopicsViewModel,
  machineLocale: OppiaLocale.MachineLocale,
) {
  Card(
    modifier = Modifier
      .width(200.dp)
      .padding(
        start = OppiaTheme.spacing.small,
        end = OppiaTheme.spacing.small,
        bottom = OppiaTheme.spacing.medium,
      ),
    elevation = OppiaTheme.spacing.small,
  ) {
    Box(
      contentAlignment = Alignment.TopEnd
    ) {
      Column(
        verticalArrangement = Arrangement.Center,
      ) {
        ThumbnailImage(
          entityId = comingSoonTopicsViewModel.topicSummary.topicId,
          entityType = comingSoonTopicsViewModel.entityType,
          lessonThumbnail = comingSoonTopicsViewModel.topicSummary.lessonThumbnail,
          modifier = Modifier.aspectRatio(4f / 3f)
        )
        ComingSoonTopicCardTextSection(comingSoonTopicsViewModel)
      }
      Text(
        text = machineLocale
          .run { stringResource(id = R.string.coming_soon).toMachineUpperCase() },
        modifier = Modifier
          .background(
            color = colorResource(
              id = R.color.component_color_coming_soon_rect_background_start_color
            ),
            shape = RoundedCornerShape(topEnd = 4.dp, bottomStart = 12.dp),
          )
          .padding(
            horizontal = OppiaTheme.spacing.small,
            vertical = OppiaTheme.spacing.extraSmall,
          ),
        fontSize = 12.sp,
        color = colorResource(id = R.color.component_color_shared_secondary_4_text_color),
        fontFamily = FontFamily.SansSerif,
        textAlign = TextAlign.End,
      )
    }
  }
}

/** Displays the topic title. */
@Composable
fun ComingSoonTopicCardTextSection(comingSoonTopicsViewModel: ComingSoonTopicsViewModel) {
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
      text = comingSoonTopicsViewModel.topicTitle,
      modifier = Modifier
        .fillMaxWidth()
        .padding(
          start = OppiaTheme.spacing.medium,
          top = OppiaTheme.spacing.medium,
          end = OppiaTheme.spacing.medium,
          bottom = OppiaTheme.spacing.small,
        ),
      color = colorResource(id = R.color.component_color_shared_secondary_4_text_color),
      fontFamily = FontFamily.SansSerif,
      fontSize = dimensionResource(id = R.dimen.topic_list_item_text_size).value.sp,
      textAlign = TextAlign.Start,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
    )
  }
}
