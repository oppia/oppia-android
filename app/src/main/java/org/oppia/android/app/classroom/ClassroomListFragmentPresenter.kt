package org.oppia.android.app.classroom

import ClassroomListViewModel
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import org.oppia.android.R
import org.oppia.android.app.classroom.classroomlist.ClassroomList
import org.oppia.android.app.classroom.promotedlist.PromotedStoryList
import org.oppia.android.app.classroom.topiclist.AllTopicsHeaderText
import org.oppia.android.app.classroom.topiclist.TopicCard
import org.oppia.android.app.classroom.welcome.WelcomeText
import org.oppia.android.app.drawer.NAVIGATION_PROFILE_ID_ARGUMENT_KEY
import org.oppia.android.app.home.HomeItemViewModel
import org.oppia.android.app.home.RouteToTopicPlayStoryListener
import org.oppia.android.app.home.WelcomeViewModel
import org.oppia.android.app.home.classroomlist.ClassroomSummaryViewModel
import org.oppia.android.app.home.promotedlist.PromotedStoryListViewModel
import org.oppia.android.app.home.topiclist.AllTopicsViewModel
import org.oppia.android.app.home.topiclist.TopicSummaryViewModel
import org.oppia.android.app.model.TopicSummary
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
  private val routeToTopicPlayStoryListener = activity as RouteToTopicPlayStoryListener
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
    val groupedItems = homeItemViewModelList.groupBy { it::class }
    val topicListSpanCount = integerResource(id = R.integer.home_span_count)
    LazyColumn {
      groupedItems.forEach { (type, items) ->
        when (type) {
          WelcomeViewModel::class -> items.forEach { item ->
            item {
              WelcomeText(welcomeViewModel = item as WelcomeViewModel)
            }
          }
          PromotedStoryListViewModel::class -> items.forEach { item ->
            item {
              PromotedStoryList(promotedStoryListViewModel = item as PromotedStoryListViewModel)
            }
          }
          ClassroomSummaryViewModel::class -> stickyHeader {
            ClassroomList(classroomSummaryList = items.map { it as ClassroomSummaryViewModel })
          }
          AllTopicsViewModel::class -> items.forEach { _ ->
            item {
              AllTopicsHeaderText()
            }
          }
          TopicSummaryViewModel::class -> gridItems(
            data = items.map { it as TopicSummaryViewModel },
            columnCount = topicListSpanCount,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(horizontal = 28.dp, vertical = 10.dp)
          ) { itemData ->
            TopicCard(topicSummaryViewModel = itemData)
          }
        }
      }
    }
  }

  fun onTopicSummaryClicked(topicSummary: TopicSummary) {
    routeToTopicPlayStoryListener.routeToTopicPlayStory(
      internalProfileId,
      topicSummary.topicId,
      topicSummary.firstStoryId
    )
  }
}

fun <T> LazyListScope.gridItems(
  data: List<T>,
  columnCount: Int,
  modifier: Modifier,
  horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
  itemContent: @Composable BoxScope.(T) -> Unit,
) {
  val size = data.count()
  val rows = if (size == 0) 0 else 1 + (size - 1) / columnCount
  items(rows, key = { it.hashCode() }) { rowIndex ->
    Row(
      horizontalArrangement = horizontalArrangement,
      modifier = modifier
    ) {
      for (columnIndex in 0 until columnCount) {
        val itemIndex = rowIndex * columnCount + columnIndex
        if (itemIndex < size) {
          Box(
            modifier = Modifier.weight(1F, fill = true),
            propagateMinConstraints = true
          ) {
            itemContent(data[itemIndex])
          }
        } else {
          Spacer(Modifier.weight(1F, fill = true))
        }
      }
    }
  }
}
