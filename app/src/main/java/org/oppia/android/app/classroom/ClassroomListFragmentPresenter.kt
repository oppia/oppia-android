package org.oppia.android.app.classroom

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.unit.dp
import androidx.databinding.ObservableList
import androidx.fragment.app.Fragment
import org.oppia.android.R
import org.oppia.android.app.classroom.classroomlist.AllClassroomsHeaderText
import org.oppia.android.app.classroom.classroomlist.ClassroomList
import org.oppia.android.app.classroom.promotedlist.ComingSoonTopicList
import org.oppia.android.app.classroom.promotedlist.PromotedStoryList
import org.oppia.android.app.classroom.topiclist.AllTopicsHeaderText
import org.oppia.android.app.classroom.topiclist.TopicCard
import org.oppia.android.app.classroom.welcome.WelcomeText
import org.oppia.android.app.home.HomeItemViewModel
import org.oppia.android.app.home.RouteToTopicPlayStoryListener
import org.oppia.android.app.home.WelcomeViewModel
import org.oppia.android.app.home.classroomlist.AllClassroomsViewModel
import org.oppia.android.app.home.classroomlist.ClassroomSummaryViewModel
import org.oppia.android.app.home.promotedlist.ComingSoonTopicListViewModel
import org.oppia.android.app.home.promotedlist.PromotedStoryListViewModel
import org.oppia.android.app.home.topiclist.AllTopicsViewModel
import org.oppia.android.app.home.topiclist.TopicSummaryViewModel
import org.oppia.android.app.model.ClassroomSummary
import org.oppia.android.app.model.LessonThumbnail
import org.oppia.android.app.model.LessonThumbnailGraphic
import org.oppia.android.app.model.TopicSummary
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.app.utility.datetime.DateTimeUtil
import org.oppia.android.databinding.ClassroomListFragmentBinding
import org.oppia.android.domain.classroom.ClassroomController
import org.oppia.android.domain.onboarding.AppStartupStateController
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.oppialogger.analytics.AnalyticsController
import org.oppia.android.domain.profile.ProfileManagementController
import org.oppia.android.domain.topic.TopicListController
import org.oppia.android.domain.translation.TranslationController
import org.oppia.android.util.locale.OppiaLocale
import org.oppia.android.util.parser.html.StoryHtmlParserEntityType
import org.oppia.android.util.parser.html.TopicHtmlParserEntityType
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.extractCurrentUserProfileId
import javax.inject.Inject

/** Test tag for the classroom list screen. */
const val CLASSROOM_LIST_SCREEN_TEST_TAG = "TEST_TAG.classroom_list_screen"

/** The presenter for [ClassroomListFragment]. */
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
  private val machineLocale: OppiaLocale.MachineLocale,
  private val appStartupStateController: AppStartupStateController,
  private val analyticsController: AnalyticsController,
) {
  private val routeToTopicPlayStoryListener = activity as RouteToTopicPlayStoryListener
  private lateinit var binding: ClassroomListFragmentBinding
  private lateinit var classroomListViewModel: ClassroomListViewModel
  private var internalProfileId: Int = -1
  private val profileId = activity.intent.extractCurrentUserProfileId()

  /** Creates and returns the view for the [ClassroomListFragment]. */
  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
    binding = ClassroomListFragmentBinding.inflate(
      inflater,
      container,
      /* attachToRoot= */ false
    )

    internalProfileId = profileId.internalId

    logHomeActivityEvent()

    classroomListViewModel = ClassroomListViewModel(
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
      refreshComposeView()
    }

    classroomListViewModel.topicList.addOnListChangedCallback(
      object : ObservableList.OnListChangedCallback<ObservableList<HomeItemViewModel>>() {
        override fun onChanged(sender: ObservableList<HomeItemViewModel>) {}

        override fun onItemRangeChanged(
          sender: ObservableList<HomeItemViewModel>,
          positionStart: Int,
          itemCount: Int
        ) {}

        override fun onItemRangeInserted(
          sender: ObservableList<HomeItemViewModel>,
          positionStart: Int,
          itemCount: Int
        ) {
          refreshComposeView()
        }

        override fun onItemRangeMoved(
          sender: ObservableList<HomeItemViewModel>,
          fromPosition: Int,
          toPosition: Int,
          itemCount: Int
        ) {}

        override fun onItemRangeRemoved(
          sender: ObservableList<HomeItemViewModel>,
          positionStart: Int,
          itemCount: Int
        ) {}
      }
    )

    return binding.root
  }

  /** Routes to the play story view for the first story in the given topic summary. */
  fun onTopicSummaryClicked(topicSummary: TopicSummary) {
    routeToTopicPlayStoryListener.routeToTopicPlayStory(
      internalProfileId,
      topicSummary.classroomId,
      topicSummary.topicId,
      topicSummary.firstStoryId
    )
  }

  /** Triggers the view model to update the topic list. */
  fun onClassroomSummaryClicked(classroomSummary: ClassroomSummary) {
    val classroomId = classroomSummary.classroomId
    profileManagementController.updateLastSelectedClassroomId(profileId, classroomId)
    classroomListViewModel.fetchAndUpdateTopicList(classroomId)
  }

  private fun refreshComposeView() {
    binding.composeView.apply {
      setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
      setContent {
        MaterialTheme {
          ClassroomListScreen()
        }
      }
    }
  }

  /** Display a list of classroom-related items grouped by their types. */
  @OptIn(ExperimentalFoundationApi::class)
  @Composable
  fun ClassroomListScreen() {
    val groupedItems = classroomListViewModel.homeItemViewModelListLiveData.value
      ?.plus(classroomListViewModel.topicList)
      ?.groupBy { it::class }
    val topicListSpanCount = integerResource(id = R.integer.home_span_count)
    val listState = rememberLazyListState()
    val classroomListIndex = groupedItems
      ?.flatMap { (type, items) -> items.map { type to it } }
      ?.indexOfFirst { it.first == AllClassroomsViewModel::class }
      ?: -1

    LazyColumn(
      modifier = Modifier.testTag(CLASSROOM_LIST_SCREEN_TEST_TAG),
      state = listState
    ) {
      groupedItems?.forEach { (type, items) ->
        when (type) {
          WelcomeViewModel::class -> items.forEach { item ->
            item {
              WelcomeText(welcomeViewModel = item as WelcomeViewModel)
            }
          }
          PromotedStoryListViewModel::class -> items.forEach { item ->
            item {
              PromotedStoryList(
                promotedStoryListViewModel = item as PromotedStoryListViewModel,
                machineLocale = machineLocale
              )
            }
          }
          ComingSoonTopicListViewModel::class -> items.forEach { item ->
            item {
              ComingSoonTopicList(
                comingSoonTopicListViewModel = item as ComingSoonTopicListViewModel,
                machineLocale = machineLocale,
              )
            }
          }
          AllClassroomsViewModel::class -> items.forEach { _ ->
            item {
              AllClassroomsHeaderText()
            }
          }
          ClassroomSummaryViewModel::class -> stickyHeader() {
            ClassroomList(
              classroomSummaryList = items.map { it as ClassroomSummaryViewModel },
              selectedClassroomId = classroomListViewModel.selectedClassroomId.get() ?: "",
              isSticky = listState.firstVisibleItemIndex >= classroomListIndex
            )
          }
          AllTopicsViewModel::class -> items.forEach { _ ->
            item {
              AllTopicsHeaderText()
            }
          }
          TopicSummaryViewModel::class -> {
            gridItems(
              data = items.map { it as TopicSummaryViewModel },
              columnCount = topicListSpanCount,
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              modifier = Modifier
            ) { itemData ->
              TopicCard(topicSummaryViewModel = itemData)
            }
          }
        }
      }
    }
  }

  private fun logHomeActivityEvent() {
    analyticsController.logImportantEvent(
      oppiaLogger.createOpenHomeContext(),
      profileId
    )
  }
}

/** Adds a grid of items to a LazyListScope with specified arrangement and item content. */
fun <T> LazyListScope.gridItems(
  data: List<T>,
  columnCount: Int,
  modifier: Modifier,
  horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
  itemContent: @Composable BoxScope.(T) -> Unit,
) {
  val size = data.count()
  // Calculate the number of rows needed.
  val rows = if (size == 0) 0 else (size + columnCount - 1) / columnCount

  // Generate items in the LazyList.
  items(rows, key = { it }) { rowIndex ->
    // Create a row with the specified horizontal arrangement and padding.
    Row(
      horizontalArrangement = horizontalArrangement,
      modifier = modifier
        .background(
          colorResource(id = R.color.component_color_classroom_topic_list_background_color)
        )
        .padding(
          horizontal = dimensionResource(id = R.dimen.classrooms_text_margin_start),
          vertical = 10.dp
        )
    ) {
      // Populate the row with columns.
      for (columnIndex in 0 until columnCount) {
        val itemIndex = rowIndex * columnCount + columnIndex
        if (itemIndex < size) {
          Box(
            modifier = Modifier.weight(1F, fill = true),
            propagateMinConstraints = true
          ) {
            itemContent(data[itemIndex]) // Provide content for each item.
          }
        } else {
          Spacer(Modifier.weight(1F, fill = true)) // Add spacer if no more items.
        }
      }
    }

    // Add bottom padding if it's the last row.
    if (rowIndex == rows - 1) {
      Spacer(
        modifier = Modifier
          .fillMaxWidth()
          .height(dimensionResource(id = R.dimen.home_fragment_padding_bottom))
          .background(
            colorResource(id = R.color.component_color_classroom_topic_list_background_color)
          )
      )
    }
  }
}

/** Retrieves the drawable resource ID for the lesson thumbnail based on its graphic type. */
fun LessonThumbnail.getDrawableResource(): Int {
  return when (thumbnailGraphic) {
    LessonThumbnailGraphic.BAKER ->
      R.drawable.lesson_thumbnail_graphic_baker
    LessonThumbnailGraphic.CHILD_WITH_BOOK ->
      R.drawable.lesson_thumbnail_graphic_child_with_book
    LessonThumbnailGraphic.CHILD_WITH_CUPCAKES ->
      R.drawable.lesson_thumbnail_graphic_child_with_cupcakes
    LessonThumbnailGraphic.CHILD_WITH_FRACTIONS_HOMEWORK ->
      R.drawable.lesson_thumbnail_graphic_child_with_fractions_homework
    LessonThumbnailGraphic.DUCK_AND_CHICKEN ->
      R.drawable.lesson_thumbnail_graphic_duck_and_chicken
    LessonThumbnailGraphic.PERSON_WITH_PIE_CHART ->
      R.drawable.lesson_thumbnail_graphic_person_with_pie_chart
    LessonThumbnailGraphic.IDENTIFYING_THE_PARTS_OF_A_FRACTION ->
      R.drawable.topic_fractions_01
    LessonThumbnailGraphic.WRITING_FRACTIONS ->
      R.drawable.topic_fractions_02
    LessonThumbnailGraphic.EQUIVALENT_FRACTIONS ->
      R.drawable.topic_fractions_03
    LessonThumbnailGraphic.MIXED_NUMBERS_AND_IMPROPER_FRACTIONS ->
      R.drawable.topic_fractions_04
    LessonThumbnailGraphic.COMPARING_FRACTIONS ->
      R.drawable.topic_fractions_05
    LessonThumbnailGraphic.ADDING_AND_SUBTRACTING_FRACTIONS ->
      R.drawable.topic_fractions_06
    LessonThumbnailGraphic.MULTIPLYING_FRACTIONS ->
      R.drawable.topic_fractions_07
    LessonThumbnailGraphic.DIVIDING_FRACTIONS ->
      R.drawable.topic_fractions_08
    LessonThumbnailGraphic.DERIVE_A_RATIO ->
      R.drawable.topic_ratios_01
    LessonThumbnailGraphic.WHAT_IS_A_FRACTION ->
      R.drawable.topic_fractions_01
    LessonThumbnailGraphic.FRACTION_OF_A_GROUP ->
      R.drawable.topic_fractions_02
    LessonThumbnailGraphic.ADDING_FRACTIONS ->
      R.drawable.topic_fractions_03
    LessonThumbnailGraphic.MIXED_NUMBERS ->
      R.drawable.topic_fractions_04
    LessonThumbnailGraphic.SCIENCE_CLASSROOM ->
      R.drawable.ic_science
    LessonThumbnailGraphic.MATHS_CLASSROOM ->
      R.drawable.ic_maths
    LessonThumbnailGraphic.ENGLISH_CLASSROOM ->
      R.drawable.ic_english
    else ->
      R.drawable.topic_fractions_01
  }
}
