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
import org.oppia.android.app.model.ExplorationActivityParams
import org.oppia.android.app.model.ExplorationCheckpoint
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.StorySummary
import org.oppia.android.app.recyclerview.BindableAdapter
import org.oppia.android.app.topic.RouteToResumeLessonListener
import org.oppia.android.app.topic.RouteToStoryListener
import org.oppia.android.databinding.LessonsCompletedChapterViewBinding
import org.oppia.android.databinding.LessonsInProgressChapterViewBinding
import org.oppia.android.databinding.LessonsLockedChapterViewBinding
import org.oppia.android.databinding.LessonsNotStartedChapterViewBinding
import org.oppia.android.databinding.TopicLessonsFragmentBinding
import org.oppia.android.databinding.TopicLessonsStorySummaryBinding
import org.oppia.android.databinding.TopicLessonsTitleBinding
import org.oppia.android.domain.exploration.ExplorationDataController
import org.oppia.android.domain.exploration.lightweightcheckpointing.ExplorationCheckpointController
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.util.accessibility.AccessibilityService
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import org.oppia.android.util.enumfilter.filterByEnumCondition
import javax.inject.Inject

/** The presenter for [TopicLessonsFragment]. */
@FragmentScope
class TopicLessonsFragmentPresenter @Inject constructor(
  activity: AppCompatActivity,
  private val fragment: Fragment,
  private val oppiaLogger: OppiaLogger,
  private val explorationDataController: ExplorationDataController,
  private val explorationCheckpointController: ExplorationCheckpointController,
  private val topicLessonViewModel: TopicLessonViewModel,
  private val accessibilityService: AccessibilityService,
  private val multiTypeBuilderFactory: BindableAdapter.MultiTypeBuilder.Factory,
  private val singleTypeBuilderFactory: BindableAdapter.SingleTypeBuilder.Factory
) {

  private val routeToResumeLessonListener = activity as RouteToResumeLessonListener
  private val routeToExplorationListener = activity as RouteToExplorationListener
  private val routeToStoryListener = activity as RouteToStoryListener

  private var currentExpandedChapterListIndex: Int? = null

  private lateinit var binding: TopicLessonsFragmentBinding
  private lateinit var profileId: ProfileId
  private lateinit var classroomId: String
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
    profileId: ProfileId,
    classroomId: String,
    topicId: String,
    storyId: String,
    isDefaultStoryExpanded: Boolean
  ): View? {
    this.profileId = profileId
    this.classroomId = classroomId
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

    topicLessonViewModel.setProfileId(profileId)
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
    return multiTypeBuilderFactory.create<TopicLessonsItemViewModel, ViewType> { viewModel ->
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
    val completedChapterCount = filterByEnumCondition(
      chapterSummaries.map(ChapterSummary::getChapterPlayState),
      { it },
      { it == ChapterPlayState.COMPLETED }
    ).size

    val inProgressChapterCount =
      filterByEnumCondition(
        chapterSummaries.map(ChapterSummary::getChapterPlayState),
        { it },
        { it == ChapterPlayState.IN_PROGRESS_SAVED }
      ).size

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

    binding.expandListIcon.setOnClickListener {
      expandStoryList(position)
    }

    binding.root.setOnClickListener {
      expandStoryList(position)
    }

    if (accessibilityService.isScreenReaderEnabled()) {
      binding.root.isClickable = false
      binding.expandListIcon.isClickable = true
    } else {
      binding.root.isClickable = true
      binding.expandListIcon.isClickable = false
    }
  }

  private fun expandStoryList(position: Int) {
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

  private fun createChapterRecyclerViewAdapter(): BindableAdapter<ChapterSummaryViewModel> {
    return multiTypeBuilderFactory.create<ChapterSummaryViewModel, ChapterViewType> { viewModel ->
      when (viewModel.chapterPlayState) {
        ChapterPlayState.NOT_PLAYABLE_MISSING_PREREQUISITES -> ChapterViewType.CHAPTER_LOCKED
        ChapterPlayState.COMPLETED -> ChapterViewType.CHAPTER_COMPLETED
        ChapterPlayState.IN_PROGRESS_SAVED, ChapterPlayState.IN_PROGRESS_NOT_SAVED,
        ChapterPlayState.STARTED_NOT_COMPLETED, ChapterPlayState.COMPLETION_STATUS_UNSPECIFIED
        -> ChapterViewType.CHAPTER_IN_PROGRESS
        ChapterPlayState.NOT_STARTED -> ChapterViewType.CHAPTER_NOT_STARTED
        ChapterPlayState.UNRECOGNIZED -> throw IllegalArgumentException("Play state unknown")
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
        inflateDataBinding = LessonsCompletedChapterViewBinding::inflate,
        setViewModel = LessonsCompletedChapterViewBinding::setViewModel,
        transformViewModel = { it }
      )
      .registerViewDataBinder(
        viewType = ChapterViewType.CHAPTER_NOT_STARTED,
        inflateDataBinding = LessonsNotStartedChapterViewBinding::inflate,
        setViewModel = LessonsNotStartedChapterViewBinding::setViewModel,
        transformViewModel = { it }
      )
      .registerViewDataBinder(
        viewType = ChapterViewType.CHAPTER_IN_PROGRESS,
        inflateDataBinding = LessonsInProgressChapterViewBinding::inflate,
        setViewModel = LessonsInProgressChapterViewBinding::setViewModel,
        transformViewModel = { it }
      )
      .build()
  }

  private enum class ChapterViewType {
    CHAPTER_NOT_STARTED,
    CHAPTER_COMPLETED,
    CHAPTER_LOCKED,
    CHAPTER_IN_PROGRESS
  }

  fun storySummaryClicked(storySummary: StorySummary) {
    routeToStoryListener.routeToStory(
      internalProfileId = profileId.internalId,
      classroomId = classroomId,
      topicId = topicId,
      storyId = storySummary.storyId
    )
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
            profileId, explorationId
          ).toLiveData()
        explorationCheckpointLiveData.observe(
          fragment,
          object : Observer<AsyncResult<ExplorationCheckpoint>> {
            override fun onChanged(it: AsyncResult<ExplorationCheckpoint>) {
              if (it is AsyncResult.Success) {
                explorationCheckpointLiveData.removeObserver(this)
                routeToResumeLessonListener.routeToResumeLesson(
                  profileId,
                  classroomId,
                  topicId,
                  storyId,
                  explorationId,
                  parentScreen = ExplorationActivityParams.ParentScreen.TOPIC_SCREEN_LESSONS_TAB,
                  explorationCheckpoint = it.value
                )
              } else if (it is AsyncResult.Failure) {
                explorationCheckpointLiveData.removeObserver(this)
                playExploration(
                  profileId,
                  classroomId,
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
          profileId,
          classroomId,
          topicId,
          storyId,
          explorationId,
          canHavePartialProgressSaved,
          hadProgress = true
        )
      }
      else -> {
        playExploration(
          profileId,
          classroomId,
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
    profileId: ProfileId,
    classroomId: String,
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
          profileId.internalId, classroomId, topicId, storyId, explorationId
        )
      }
      hadProgress -> {
        // If there was progress, either the checkpoint was never saved, failed to save, or failed
        // to be retrieved. In all cases, this is a restart.
        explorationDataController.restartExploration(
          profileId.internalId, classroomId, topicId, storyId, explorationId
        )
      }
      else -> {
        // If there's no progress and it was never completed, then it's a new play through (or the
        // user is very low on device memory).
        explorationDataController.startPlayingNewExploration(
          profileId.internalId, classroomId, topicId, storyId, explorationId
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
            profileId,
            classroomId,
            topicId,
            storyId,
            explorationId,
            parentScreen = ExplorationActivityParams.ParentScreen.TOPIC_SCREEN_LESSONS_TAB,
            isCheckpointingEnabled = canHavePartialProgressSaved
          )
        }
      }
    }
  }
}
