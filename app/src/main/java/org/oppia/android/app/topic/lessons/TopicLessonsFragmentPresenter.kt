package org.oppia.android.app.topic.lessons

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import javax.inject.Inject
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.home.RouteToExplorationListener
import org.oppia.android.app.model.ChapterPlayState
import org.oppia.android.app.model.ChapterSummary
import org.oppia.android.app.model.ExplorationCheckpoint
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.StorySummary
import org.oppia.android.app.recyclerview.BindableAdapter
import org.oppia.android.app.topic.RouteToResumeLessonListener
import org.oppia.android.app.topic.RouteToStoryListener
import org.oppia.android.databinding.LessonsChapterViewBinding
import org.oppia.android.databinding.LessonsLockedChapterViewBinding
import org.oppia.android.databinding.LessonsNotStartedChapterViewBinding
import org.oppia.android.databinding.TopicLessonsFragmentBinding
import org.oppia.android.databinding.TopicLessonsStorySummaryBinding
import org.oppia.android.databinding.TopicLessonsTitleBinding
import org.oppia.android.domain.exploration.ExplorationDataController
import org.oppia.android.domain.exploration.lightweightcheckpointing.ExplorationCheckpointController
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData

/** The presenter for [TopicLessonsFragment]. */
@FragmentScope
class TopicLessonsFragmentPresenter @Inject constructor(
  activity: AppCompatActivity,
  private val fragment: Fragment,
  private val oppiaLogger: OppiaLogger,
  private val explorationDataController: ExplorationDataController,
  private val explorationCheckpointController: ExplorationCheckpointController
) {

  private val routeToResumeLessonListener = activity as RouteToResumeLessonListener
  private val routeToExplorationListener = activity as RouteToExplorationListener
  private val routeToStoryListener = activity as RouteToStoryListener

  @Inject
  lateinit var topicLessonViewModel: TopicLessonViewModel

  private var currentExpandedChapterListIndex: Int? = null

  private lateinit var binding: TopicLessonsFragmentBinding
  private var internalProfileId: Int = -1
  private lateinit var topicId: String
  private lateinit var storyId: String
  private var isDefaultStoryExpanded: Boolean = false

  private lateinit var expandedChapterListIndexListener: ExpandedChapterListIndexListener

  private lateinit var bindingAdapter: BindableAdapter<TopicLessonsItemViewModel>

  fun handleCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    currentExpandedChapterListIndex: Int?,
    expandedChapterListIndexListener: ExpandedChapterListIndexListener,
    internalProfileId: Int,
    topicId: String,
    storyId: String,
    isDefaultStoryExpanded: Boolean
  ): View? {
    this.internalProfileId = internalProfileId
    this.topicId = topicId
    this.storyId = storyId
    this.isDefaultStoryExpanded = isDefaultStoryExpanded
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
    if (storySummaryViewModel.storySummary.storyId == storyId && !isDefaultStoryExpanded) {
      val index = topicLessonViewModel.getIndexOfStory(storySummaryViewModel.storySummary)
      currentExpandedChapterListIndex = index + 1
      isDefaultStoryExpanded = true
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
    storySummaryViewModel.setStoryPercentage(storyPercentage)
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
    addChapterRecyclerViewItemDecoration(binding)

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

  private fun addChapterRecyclerViewItemDecoration(binding: TopicLessonsStorySummaryBinding) {
    val chapterRecyclerView = binding.chapterRecyclerView
    val layoutManager = chapterRecyclerView.layoutManager as LinearLayoutManager
    val dividerItemDecoration = DividerItemDecoration(chapterRecyclerView.context, layoutManager.orientation)
    chapterRecyclerView.addItemDecoration(dividerItemDecoration)
  }

  private fun createChapterRecyclerViewAdapter(): BindableAdapter<ChapterSummaryViewModel> {
    return BindableAdapter.MultiTypeBuilder
      .newBuilder<ChapterSummaryViewModel, ChapterViewType> { viewModel ->
        when (viewModel.chapterPlayState) {
          ChapterPlayState.NOT_PLAYABLE_MISSING_PREREQUISITES -> ChapterViewType.CHAPTER_LOCKED
          ChapterPlayState.COMPLETED -> ChapterViewType.CHAPTER_COMPLETED
          else -> ChapterViewType.CHAPTER_COMPLETED
        }
      }
      .registerViewDataBinder(
        viewType = ChapterViewType.CHAPTER_LOCKED,
        inflateDataBinding = LessonsLockedChapterViewBinding::inflate,
        setViewModel = LessonsLockedChapterViewBinding::setViewModel,
        transformViewModel = { it }
      )
      .registerViewDataBinder(
        viewType = ChapterViewType.CHAPTER_COMPLETED,
        inflateDataBinding = LessonsChapterViewBinding::inflate,
        setViewModel = LessonsChapterViewBinding::setViewModel,
        transformViewModel = { it }
      )
      .registerViewDataBinder(
        viewType = ChapterViewType.CHAPTER_NOT_STARTED,
        inflateDataBinding = LessonsNotStartedChapterViewBinding::inflate,
        setViewModel = LessonsNotStartedChapterViewBinding::setViewModel,
        transformViewModel = { it }
      )
      .build()
  }

  enum class ChapterViewType {
    CHAPTER_NOT_STARTED,
    CHAPTER_COMPLETED,
    CHAPTER_LOCKED
  }

  fun storySummaryClicked(storySummary: StorySummary) {
    routeToStoryListener.routeToStory(internalProfileId, topicId, storySummary.storyId)
  }

  fun selectChapterSummary(
    storyId: String,
    explorationId: String,
    chapterPlayState: ChapterPlayState
  ) {
    val canHavePartialProgressSaved =
      when (chapterPlayState) {
        ChapterPlayState.IN_PROGRESS_SAVED, ChapterPlayState.IN_PROGRESS_NOT_SAVED,
        ChapterPlayState.STARTED_NOT_COMPLETED, ChapterPlayState.NOT_STARTED -> true
        ChapterPlayState.COMPLETION_STATUS_UNSPECIFIED,
        ChapterPlayState.NOT_PLAYABLE_MISSING_PREREQUISITES, ChapterPlayState.UNRECOGNIZED,
        ChapterPlayState.COMPLETED -> false
      }

    when (chapterPlayState) {
      ChapterPlayState.IN_PROGRESS_SAVED -> {
        val explorationCheckpointLiveData =
          explorationCheckpointController.retrieveExplorationCheckpoint(
            ProfileId.newBuilder().apply {
              internalId = internalProfileId
            }.build(),
            explorationId
          ).toLiveData()
        explorationCheckpointLiveData.observe(
          fragment,
          object : Observer<AsyncResult<ExplorationCheckpoint>> {
            override fun onChanged(it: AsyncResult<ExplorationCheckpoint>) {
              if (it is AsyncResult.Success) {
                explorationCheckpointLiveData.removeObserver(this)
                routeToResumeLessonListener.routeToResumeLesson(
                  internalProfileId,
                  topicId,
                  storyId,
                  explorationId,
                  backflowScreen = 0,
                  explorationCheckpoint = it.value
                )
              } else if (it is AsyncResult.Failure) {
                explorationCheckpointLiveData.removeObserver(this)
                playExploration(
                  internalProfileId,
                  topicId,
                  storyId,
                  explorationId,
                  canHavePartialProgressSaved,
                  hadProgress = true
                )
              }
            }
          }
        )
      }
      ChapterPlayState.IN_PROGRESS_NOT_SAVED -> {
        playExploration(
          internalProfileId,
          topicId,
          storyId,
          explorationId,
          canHavePartialProgressSaved,
          hadProgress = true
        )
      }
      else -> {
        playExploration(
          internalProfileId,
          topicId,
          storyId,
          explorationId,
          canHavePartialProgressSaved,
          hadProgress = false
        )
      }
    }
  }

  private fun playExploration(
    internalProfileId: Int,
    topicId: String,
    storyId: String,
    explorationId: String,
    canHavePartialProgressSaved: Boolean,
    hadProgress: Boolean
  ) {
    val startPlayingProvider = when {
      !canHavePartialProgressSaved -> {
        // Only explorations that have been completed can't be saved, so replay the lesson.
        explorationDataController.replayExploration(
          internalProfileId, topicId, storyId, explorationId
        )
      }
      hadProgress -> {
        // If there was progress, either the checkpoint was never saved, failed to save, or failed
        // to be retrieved. In all cases, this is a restart.
        explorationDataController.restartExploration(
          internalProfileId, topicId, storyId, explorationId
        )
      }
      else -> {
        // If there's no progress and it was never completed, then it's a new play through (or the
        // user is very low on device memory).
        explorationDataController.startPlayingNewExploration(
          internalProfileId, topicId, storyId, explorationId
        )
      }
    }
    startPlayingProvider.toLiveData().observe(fragment) { result ->
      when (result) {
        is AsyncResult.Pending -> oppiaLogger.d("TopicLessonsFragment", "Loading exploration")
        is AsyncResult.Failure ->
          oppiaLogger.e("TopicLessonsFragment", "Failed to load exploration", result.error)
        is AsyncResult.Success -> {
          oppiaLogger.d("TopicLessonsFragment", "Successfully loaded exploration")
          routeToExplorationListener.routeToExploration(
            internalProfileId,
            topicId,
            storyId,
            explorationId,
            backflowScreen = 0,
            isCheckpointingEnabled = canHavePartialProgressSaved
          )
        }
      }
    }
  }
}
