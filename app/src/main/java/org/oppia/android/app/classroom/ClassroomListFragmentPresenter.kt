package org.oppia.android.app.classroom

import ClassroomListViewModel
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import org.oppia.android.R
import org.oppia.android.app.drawer.NAVIGATION_PROFILE_ID_ARGUMENT_KEY
import org.oppia.android.app.home.HomeItemViewModel
import org.oppia.android.app.home.WelcomeViewModel
import org.oppia.android.app.home.classroomlist.ClassroomSummaryViewModel
import org.oppia.android.app.home.promotedlist.PromotedStoryListViewModel
import org.oppia.android.app.home.topiclist.AllTopicsViewModel
import org.oppia.android.app.home.topiclist.TopicSummaryViewModel
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.app.utility.datetime.DateTimeUtil
import org.oppia.android.databinding.ClassroomListFragmentBinding
import org.oppia.android.domain.classroom.ClassroomController
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.profile.ProfileManagementController
import org.oppia.android.domain.topic.TopicListController
import org.oppia.android.domain.translation.TranslationController
import org.oppia.android.util.parser.html.StoryHtmlParserEntityType
import org.oppia.android.util.parser.html.TopicHtmlParserEntityType
import javax.inject.Inject

class ClassroomListFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val fragment: Fragment,
  private val profileManagementController: ProfileManagementController,
  private val topicListController: TopicListController,
  private val classroomController: ClassroomController,
  private val oppiaLogger: OppiaLogger,
  @TopicHtmlParserEntityType private val topicEntityType: String,
  @StoryHtmlParserEntityType private val storyEntityType: String,
  private val resourceHandler: AppLanguageResourceHandler,
  private val dateTimeUtil: DateTimeUtil,
  private val translationController: TranslationController,
) {
  private lateinit var binding: ClassroomListFragmentBinding
  private var internalProfileId: Int = -1

  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
    binding = ClassroomListFragmentBinding.inflate(
      inflater,
      container,
      /* attachToRoot= */ false
    )

    internalProfileId = activity.intent.getIntExtra(NAVIGATION_PROFILE_ID_ARGUMENT_KEY, -1)

    val classroomListViewModel = ClassroomListViewModel(
      activity,
      fragment,
      oppiaLogger,
      internalProfileId,
      profileManagementController,
      topicListController,
      classroomController,
      topicEntityType,
      storyEntityType,
      resourceHandler,
      dateTimeUtil,
      translationController
    )

    classroomListViewModel.homeItemViewModelListLiveData.observe(activity) {
      binding.composeView.apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
          MaterialTheme {
            ClassroomListScreen(it)
          }
        }
      }
    }

    return binding.root
  }

  @OptIn(ExperimentalFoundationApi::class)
  @Composable
  fun ClassroomListScreen(homeItemViewModelList: List<HomeItemViewModel>) {
    LazyColumn {
      homeItemViewModelList.forEach {
        when (it) {
          is WelcomeViewModel -> item { WelcomeComponent(welcomeViewModel = it) }
          is PromotedStoryListViewModel -> item {
            PromotedStoryListComponent(promotedStoryListViewModel = it)
          }
          is ClassroomSummaryViewModel -> stickyHeader {
            ClassroomListComponent(classroomSummaryViewModel = it)
          }
          is AllTopicsViewModel -> item { AllTopicsHeaderComponent(allTopicsViewModel = it) }
          is TopicSummaryViewModel -> item { TopicListComponent(topicSummaryViewModel = it) }
        }
      }
    }
  }

  @Composable
  fun WelcomeComponent(welcomeViewModel: WelcomeViewModel) {
    val outerPadding = dimensionResource(id = R.dimen.home_welcome_outer_padding)
    val textMarginEnd = dimensionResource(id = R.dimen.home_welcome_text_view_margin_end)
    val greetingLineColor = colorResource(
      id = R.color.component_color_home_activity_layout_greeting_text_line_color
    )

    Text(
      text = welcomeViewModel.computeWelcomeText(),
      modifier = Modifier
        .padding(
          start = outerPadding,
          top = outerPadding,
          end = outerPadding + textMarginEnd,
          bottom = outerPadding
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
      fontSize = 24.sp,
      fontFamily = FontFamily.SansSerif,
    )
  }

  @Composable
  fun PromotedStoryListComponent(promotedStoryListViewModel: PromotedStoryListViewModel) {
    Text(text = "PromotedStoryList")
  }

  @Composable
  fun ClassroomListComponent(classroomSummaryViewModel: ClassroomSummaryViewModel) {
    Text(text = "ClassroomSummary")
  }

  @Composable
  fun TopicListComponent(topicSummaryViewModel: TopicSummaryViewModel) {
    Text(text = "TopicSummary")
  }

  @Composable
  fun AllTopicsHeaderComponent(allTopicsViewModel: AllTopicsViewModel) {
    Text(text = "AllTopics")
  }
}
