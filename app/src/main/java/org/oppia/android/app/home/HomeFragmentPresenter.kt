package org.oppia.android.app.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import org.oppia.android.R
import org.oppia.android.app.drawer.NAVIGATION_PROFILE_ID_ARGUMENT_KEY
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.home.promotedlist.ComingSoonTopicListViewModel
import org.oppia.android.app.home.promotedlist.PromotedStoryListViewModel
import org.oppia.android.app.home.topiclist.AllTopicsViewModel
import org.oppia.android.app.home.topiclist.TopicSummaryViewModel
import org.oppia.android.app.model.AppStartupState
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.TopicSummary
import org.oppia.android.app.recyclerview.BindableAdapter
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.app.utility.datetime.DateTimeUtil
import org.oppia.android.databinding.AllTopicsBinding
import org.oppia.android.databinding.ComingSoonTopicListBinding
import org.oppia.android.databinding.HomeFragmentBinding
import org.oppia.android.databinding.PromotedStoryListBinding
import org.oppia.android.databinding.TopicSummaryViewBinding
import org.oppia.android.databinding.WelcomeBinding
import org.oppia.android.domain.classroom.ClassroomController
import org.oppia.android.domain.onboarding.AppStartupStateController
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.oppialogger.analytics.AnalyticsController
import org.oppia.android.domain.profile.ProfileManagementController
import org.oppia.android.domain.topic.TopicListController
import org.oppia.android.domain.translation.TranslationController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import org.oppia.android.util.parser.html.StoryHtmlParserEntityType
import org.oppia.android.util.parser.html.TopicHtmlParserEntityType
import javax.inject.Inject

/** The presenter for [HomeFragment]. */
@FragmentScope
class HomeFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val fragment: Fragment,
  private val profileManagementController: ProfileManagementController,
  private val topicListController: TopicListController,
  private val classroomController: ClassroomController,
  private val oppiaLogger: OppiaLogger,
  private val analyticsController: AnalyticsController,
  @TopicHtmlParserEntityType private val topicEntityType: String,
  @StoryHtmlParserEntityType private val storyEntityType: String,
  private val resourceHandler: AppLanguageResourceHandler,
  private val dateTimeUtil: DateTimeUtil,
  private val translationController: TranslationController,
  private val multiTypeBuilderFactory: BindableAdapter.MultiTypeBuilder.Factory,
  private val appStartupStateController: AppStartupStateController
) {
  private val routeToTopicPlayStoryListener = activity as RouteToTopicPlayStoryListener
  private lateinit var binding: HomeFragmentBinding
  private var internalProfileId: Int = -1

  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
    binding = HomeFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)
    // NB: Both the view model and lifecycle owner must be set in order to correctly bind LiveData elements to
    // data-bound view models.

    internalProfileId = activity.intent.getIntExtra(NAVIGATION_PROFILE_ID_ARGUMENT_KEY, -1)
    logHomeActivityEvent()

    val homeViewModel = HomeViewModel(
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

    val homeAdapter = createRecyclerViewAdapter()
    val spanCount = activity.resources.getInteger(R.integer.home_span_count)
    val homeLayoutManager = GridLayoutManager(activity.applicationContext, spanCount)
    homeLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
      override fun getSpanSize(position: Int): Int {
        return if (position < homeAdapter.itemCount &&
          homeAdapter.getItemViewType(position) == ViewType.TOPIC_LIST.ordinal
        ) 1
        else spanCount
      }
    }
    binding.homeRecyclerView.apply {
      adapter = homeAdapter
      layoutManager = homeLayoutManager
    }

    binding.let {
      it.lifecycleOwner = fragment
      it.viewModel = homeViewModel
    }

    binding.composeView.apply {
      setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
      setContent {
        MaterialTheme {
          GroupedList()
        }
      }
    }

    logAppOnboardedEvent()

    return binding.root
  }

  @OptIn(ExperimentalFoundationApi::class)
  @Composable
  fun GroupedList() {
    val sections = listOf("A", "B", "C", "D", "E", "F", "G")

    LazyColumn(contentPadding = PaddingValues(6.dp)) {
      sections.forEach { sectionName ->
        stickyHeader {
          Text(
            "Section $sectionName",
            Modifier.fillMaxWidth().background(Color.LightGray).padding(8.dp)
          )
        }
        items(10) { item ->
          Text(text = "Some item $item")
        }
      }
    }
  }

  private fun logAppOnboardedEvent() {
    val startupStateProvider = appStartupStateController.getAppStartupState()
    val liveData = startupStateProvider.toLiveData()
    liveData.observe(
      activity,
      object : Observer<AsyncResult<AppStartupState>> {
        override fun onChanged(startUpStateResult: AsyncResult<AppStartupState>?) {
          when (startUpStateResult) {
            null, is AsyncResult.Pending -> {
              // Do nothing
            }
            is AsyncResult.Success -> {
              liveData.removeObserver(this)

              if (startUpStateResult.value.startupMode ==
                AppStartupState.StartupMode.USER_NOT_YET_ONBOARDED
              ) {
                analyticsController.logAppOnboardedEvent(
                  ProfileId.newBuilder().setInternalId(internalProfileId).build()
                )
              }
            }
            is AsyncResult.Failure -> {
              oppiaLogger.e(
                "HomeFragment",
                "Failed to retrieve app startup state"
              )
            }
          }
        }
      }
    )
  }

  private fun createRecyclerViewAdapter(): BindableAdapter<HomeItemViewModel> {
    return multiTypeBuilderFactory.create<HomeItemViewModel, ViewType> { viewModel ->
      when (viewModel) {
        is WelcomeViewModel -> ViewType.WELCOME_MESSAGE
        is PromotedStoryListViewModel -> ViewType.PROMOTED_STORY_LIST
        is ComingSoonTopicListViewModel -> ViewType.COMING_SOON_TOPIC_LIST
        is AllTopicsViewModel -> ViewType.ALL_TOPICS
        is TopicSummaryViewModel -> ViewType.TOPIC_LIST
        else -> throw IllegalArgumentException("Encountered unexpected view model: $viewModel")
      }
    }
      .registerViewDataBinder(
        viewType = ViewType.WELCOME_MESSAGE,
        inflateDataBinding = WelcomeBinding::inflate,
        setViewModel = WelcomeBinding::setViewModel,
        transformViewModel = { it as WelcomeViewModel }
      )
      .registerViewDataBinder(
        viewType = ViewType.PROMOTED_STORY_LIST,
        inflateDataBinding = PromotedStoryListBinding::inflate,
        setViewModel = PromotedStoryListBinding::setViewModel,
        transformViewModel = { it as PromotedStoryListViewModel }
      )
      .registerViewDataBinder(
        viewType = ViewType.COMING_SOON_TOPIC_LIST,
        inflateDataBinding = ComingSoonTopicListBinding::inflate,
        setViewModel = ComingSoonTopicListBinding::setViewModel,
        transformViewModel = { it as ComingSoonTopicListViewModel }
      )
      .registerViewDataBinder(
        viewType = ViewType.ALL_TOPICS,
        inflateDataBinding = AllTopicsBinding::inflate,
        setViewModel = AllTopicsBinding::setViewModel,
        transformViewModel = { it as AllTopicsViewModel }
      )
      .registerViewDataBinder(
        viewType = ViewType.TOPIC_LIST,
        inflateDataBinding = TopicSummaryViewBinding::inflate,
        setViewModel = TopicSummaryViewBinding::setViewModel,
        transformViewModel = { it as TopicSummaryViewModel }
      )
      .build()
  }

  private enum class ViewType {
    WELCOME_MESSAGE,
    PROMOTED_STORY_LIST,
    COMING_SOON_TOPIC_LIST,
    ALL_TOPICS,
    TOPIC_LIST
  }

  fun onTopicSummaryClicked(topicSummary: TopicSummary) {
    routeToTopicPlayStoryListener.routeToTopicPlayStory(
      internalProfileId,
      topicSummary.topicId,
      topicSummary.firstStoryId
    )
  }

  private fun logHomeActivityEvent() {
    analyticsController.logImportantEvent(
      oppiaLogger.createOpenHomeContext(),
      ProfileId.newBuilder().apply { internalId = internalProfileId }.build()
    )
  }
}
