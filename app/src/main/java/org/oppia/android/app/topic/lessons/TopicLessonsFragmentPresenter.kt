package org.oppia.android.app.topic.lessons

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.home.RouteToExplorationListener
import org.oppia.android.app.model.ChapterPlayState
import org.oppia.android.app.model.ChapterSummary
import org.oppia.android.app.model.StorySummary
import org.oppia.android.app.recyclerview.BindableAdapter
import org.oppia.android.app.topic.RouteToStoryListener
import org.oppia.android.databinding.LessonsChapterViewBinding
import org.oppia.android.databinding.TopicLessonsFragmentBinding
import org.oppia.android.databinding.TopicLessonsStorySummaryBinding
import org.oppia.android.databinding.TopicLessonsTitleBinding
import org.oppia.android.domain.exploration.ExplorationDataController
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.util.data.AsyncResult
import javax.inject.Inject

/** The presenter for [TopicLessonsFragment]. */
@FragmentScope
class TopicLessonsFragmentPresenter @Inject constructor(
  activity: AppCompatActivity,
  private val fragment: Fragment,
  private val oppiaLogger: OppiaLogger,
  private val explorationDataController: ExplorationDataController,
) {
  // TODO(#3479): Enable checkpointing once mechanism to resume exploration with checkpoints is
  //  implemented.

  private val routeToExplorationListener = activity as RouteToExplorationListener
  private val routeToStoryListener = activity as RouteToStoryListener

  @Inject
  lateinit var topicLessonViewModel: TopicLessonViewModel

  private var currentExpandedChapterListIndex: Int? = null

  private lateinit var binding: TopicLessonsFragmentBinding
  private var internalProfileId: Int = -1
  private lateinit var topicId: String
  private lateinit var storyId: String

  private lateinit var expandedChapterListIndexListener: ExpandedChapterListIndexListener

  private lateinit var bindingAdapter: BindableAdapter<TopicLessonsItemViewModel>

  fun handleCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    currentExpandedChapterListIndex: Int?,
    expandedChapterListIndexListener: ExpandedChapterListIndexListener,
    internalProfileId: Int,
    topicId: String,
    storyId: String
  ): View? {
    this.internalProfileId = internalProfileId
    this.topicId = topicId
    this.storyId = storyId
    this.currentExpandedChapterListIndex = currentExpandedChapterListIndex
    this.expandedChapterListIndexListener = expandedChapterListIndexListener
    binding = TopicLessonsFragmentBinding.inflate(
      inflater,
      container,
      /* attachToRoot= */ false
    )
    binding.apply {
      this.lifecycleOwner = fragment
      this.viewModel = topicLessonViewModel
    }

    topicLessonViewModel.setInternalProfileId(internalProfileId)
    topicLessonViewModel.setTopicId(topicId)
    topicLessonViewModel.setStoryId(storyId)

    bindingAdapter = createRecyclerViewAdapter()
    binding.storySummaryRecyclerView.apply {
      adapter = bindingAdapter
    }
    currentExpandedChapterListIndex?.let {
      if (storyId.isNotEmpty())
        binding.storySummaryRecyclerView.layoutManager!!.scrollToPosition(it)
    }
    return binding.root
  }

  private enum class ViewType {
    VIEW_TYPE_TITLE_TEXT,
    VIEW_TYPE_STORY_ITEM
  }

  private fun createRecyclerViewAdapter(): BindableAdapter<TopicLessonsItemViewModel> {
    return BindableAdapter.MultiTypeBuilder
      .newBuilder<TopicLessonsItemViewModel, ViewType> { viewModel ->
        when (viewModel) {
          is StorySummaryViewModel -> ViewType.VIEW_TYPE_STORY_ITEM
          is TopicLessonsTitleViewModel -> ViewType.VIEW_TYPE_TITLE_TEXT
          else -> throw IllegalArgumentException("Encountered unexpected view model: $viewModel")
        }
      }
      .registerViewBinder(
        viewType = ViewType.VIEW_TYPE_TITLE_TEXT,
        inflateView = { parent ->
          TopicLessonsTitleBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            /* attachToParent= */ false
          ).root
        },
        bindView = { _, _ -> }
      )
      .registerViewDataBinder(
        viewType = ViewType.VIEW_TYPE_STORY_ITEM,
        inflateDataBinding = TopicLessonsStorySummaryBinding::inflate,
        setViewModel = this::bindTopicLessonStorySummary,
        transformViewModel = { it as StorySummaryViewModel }
      )
      .build()
  }

  private fun bindTopicLessonStorySummary(
    binding: TopicLessonsStorySummaryBinding,
    storySummaryViewModel: StorySummaryViewModel
  ) {
    binding.viewModel = storySummaryViewModel

    val position = topicLessonViewModel.itemList.indexOf(storySummaryViewModel)
    if (storySummaryViewModel.storySummary.storyId == storyId) {
      val index = topicLessonViewModel.getIndexOfStory(storySummaryViewModel.storySummary)
      currentExpandedChapterListIndex = index + 1
    }

    var isChapterListVisible = false
    currentExpandedChapterListIndex?.let {
      isChapterListVisible = it == position
    }
    binding.isListExpanded = isChapterListVisible

    val chapterSummaries = storySummaryViewModel
      .storySummary.chapterList
    val completedChapterCount =
      chapterSummaries.map(ChapterSummary::getChapterPlayState)
        .filter {
          it == ChapterPlayState.COMPLETED
        }
        .size
    val inProgressChapterCount =
      chapterSummaries.map(ChapterSummary::getChapterPlayState)
        .filter {
          it == ChapterPlayState.IN_PROGRESS_SAVED
        }
        .size

    val storyPercentage: Int =
      (completedChapterCount * 100) / storySummaryViewModel.storySummary.chapterCount
    binding.storyPercentage = storyPercentage
    binding.storyProgressView.setStoryChapterDetails(
      storySummaryViewModel.storySummary.chapterCount,
      completedChapterCount,
      inProgressChapterCount
    )
    binding.topicPlayStoryDashedLineView.setLayerType(
      View.LAYER_TYPE_SOFTWARE,
      /* paint= */ null
    )
    binding.chapterRecyclerView.adapter = createChapterRecyclerViewAdapter()

    binding.root.setOnClickListener {
      val previousIndex: Int? = currentExpandedChapterListIndex
      currentExpandedChapterListIndex =
        if (currentExpandedChapterListIndex != null &&
          currentExpandedChapterListIndex == position
        ) {
          null
        } else {
          position
        }
      expandedChapterListIndexListener.onExpandListIconClicked(currentExpandedChapterListIndex)
      if (previousIndex != null && currentExpandedChapterListIndex != null &&
        previousIndex == currentExpandedChapterListIndex
      ) {
        bindingAdapter.notifyItemChanged(currentExpandedChapterListIndex!!)
      } else {
        previousIndex?.let {
          bindingAdapter.notifyItemChanged(previousIndex)
        }
        currentExpandedChapterListIndex?.let {
          bindingAdapter.notifyItemChanged(currentExpandedChapterListIndex!!)
        }
      }
    }
  }

  private fun createChapterRecyclerViewAdapter(): BindableAdapter<ChapterSummaryViewModel> {
    return BindableAdapter.SingleTypeBuilder
      .newBuilder<ChapterSummaryViewModel>()
      .registerViewDataBinderWithSameModelType(
        inflateDataBinding = LessonsChapterViewBinding::inflate,
        setViewModel = LessonsChapterViewBinding::setViewModel
      ).build()
  }

  private fun playExploration(
    internalProfileId: Int,
    topicId: String,
    storyId: String,
    explorationId: String,
    backflowScreen: Int?
  ) {
    explorationDataController.startPlayingExploration(
      internalProfileId,
      topicId,
      storyId,
      explorationId,
      shouldSavePartialProgress = false
    ).observe(
      fragment,
      Observer<AsyncResult<Any?>> { result ->
        when {
          result.isPending() -> oppiaLogger.d("TopicLessonsFragment", "Loading exploration")
          result.isFailure() -> oppiaLogger.e(
            "TopicLessonsFragment",
            "Failed to load exploration",
            result.getErrorOrNull()!!
          )
          else -> {
            oppiaLogger.d("TopicLessonsFragment", "Successfully loaded exploration")
            routeToExplorationListener.routeToExploration(
              internalProfileId,
              topicId,
              storyId,
              explorationId,
              backflowScreen,
              isCheckpointingEnabled = false
            )
          }
        }
      }
    )
  }

  fun selectChapterSummary(storyId: String, explorationId: String) {
    playExploration(
      internalProfileId,
      topicId,
      storyId,
      explorationId,
      backflowScreen = 0
    )
  }

  fun storySummaryClicked(storySummary: StorySummary) {
    routeToStoryListener.routeToStory(internalProfileId, topicId, storySummary.storyId)
  }
}
