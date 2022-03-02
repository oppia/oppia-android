package org.oppia.android.app.home.recentlyplayed

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.oppia.android.R
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.home.RouteToExplorationListener
import org.oppia.android.app.model.ChapterPlayState
import org.oppia.android.app.model.ExplorationCheckpoint
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.PromotedActivityList
import org.oppia.android.app.model.PromotedStory
import org.oppia.android.app.topic.RouteToResumeLessonListener
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.databinding.RecentlyPlayedFragmentBinding
import org.oppia.android.domain.exploration.ExplorationDataController
import org.oppia.android.domain.exploration.lightweightcheckpointing.ExplorationCheckpointController
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.topic.TopicListController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import org.oppia.android.util.parser.html.StoryHtmlParserEntityType
import javax.inject.Inject

/** The presenter for [RecentlyPlayedFragment]. */
@FragmentScope
class RecentlyPlayedFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val fragment: Fragment,
  private val oppiaLogger: OppiaLogger,
  private val explorationDataController: ExplorationDataController,
  private val topicListController: TopicListController,
  private val explorationCheckpointController: ExplorationCheckpointController,
  @StoryHtmlParserEntityType private val entityType: String,
  private val resourceHandler: AppLanguageResourceHandler
) {

  private val routeToResumeLessonListener = activity as RouteToResumeLessonListener
  private val routeToExplorationListener = activity as RouteToExplorationListener
  private var internalProfileId: Int = -1
  private lateinit var binding: RecentlyPlayedFragmentBinding
  private lateinit var ongoingListAdapter: OngoingListAdapter
  private val itemList: MutableList<RecentlyPlayedItemViewModel> = ArrayList()

  fun handleCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    internalProfileId: Int
  ): View? {
    binding = RecentlyPlayedFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)
    binding.recentlyPlayedToolbar.setNavigationOnClickListener {
      (activity as RecentlyPlayedActivity).finish()
    }

    this.internalProfileId = internalProfileId

    ongoingListAdapter = OngoingListAdapter(activity, itemList)
    binding.ongoingStoryRecyclerView.apply {
      adapter = ongoingListAdapter
    }
    binding.lifecycleOwner = fragment

    subscribeToOngoingStoryList()
    return binding.root
  }

  private val ongoingStoryListSummaryResultLiveData:
    LiveData<AsyncResult<PromotedActivityList>>
    by lazy {
      topicListController.getPromotedActivityList(
        ProfileId.newBuilder().setInternalId(internalProfileId).build()
      ).toLiveData()
    }

  private fun subscribeToOngoingStoryList() {
    getAssumedSuccessfulPromotedActivityList().observe(
      fragment,
      {
        if (it.promotedStoryList.recentlyPlayedStoryList.isNotEmpty()) {
          binding.recentlyPlayedToolbar.title =
            resourceHandler.getStringInLocale(R.string.recently_played_stories)
          addRecentlyPlayedStoryListSection(it.promotedStoryList.recentlyPlayedStoryList)
        }

        if (it.promotedStoryList.olderPlayedStoryList.isNotEmpty()) {
          binding.recentlyPlayedToolbar.title =
            resourceHandler.getStringInLocale(R.string.recently_played_stories)
          addOlderStoryListSection(it.promotedStoryList.olderPlayedStoryList)
        }

        if (it.promotedStoryList.suggestedStoryList.isNotEmpty()) {
          binding.recentlyPlayedToolbar.title =
            resourceHandler.getStringInLocale(R.string.stories_for_you)
          addRecommendedStoryListSection(it.promotedStoryList.suggestedStoryList)
        }

        binding.ongoingStoryRecyclerView.layoutManager =
          createLayoutManager(
            it.promotedStoryList.recentlyPlayedStoryCount,
            it.promotedStoryList.olderPlayedStoryCount,
            it.promotedStoryList.suggestedStoryCount
          )
        ongoingListAdapter.notifyDataSetChanged()
      }
    )
  }

  private fun addRecentlyPlayedStoryListSection(
    recentlyPlayedStoryList: MutableList<PromotedStory>
  ) {
    itemList.clear()
    val recentSectionTitleViewModel =
      SectionTitleViewModel(
        resourceHandler.getStringInLocale(R.string.ongoing_story_last_week), false
      )
    itemList.add(recentSectionTitleViewModel)
    recentlyPlayedStoryList.forEachIndexed { index, promotedStory ->
      val ongoingStoryViewModel = getOngoingStoryViewModel(promotedStory, index)
      itemList.add(ongoingStoryViewModel)
    }
  }

  private fun getOngoingStoryViewModel(
    promotedStory: PromotedStory,
    index: Int
  ): RecentlyPlayedItemViewModel {
    return OngoingStoryViewModel(
      activity,
      promotedStory,
      entityType,
      fragment as OngoingStoryClickListener,
      index,
      resourceHandler
    )
  }

  private fun addOlderStoryListSection(olderPlayedStoryList: List<PromotedStory>) {
    val showDivider = itemList.isNotEmpty()
    val olderSectionTitleViewModel =
      SectionTitleViewModel(
        resourceHandler.getStringInLocale(R.string.ongoing_story_last_month),
        showDivider
      )
    itemList.add(olderSectionTitleViewModel)
    olderPlayedStoryList.forEachIndexed { index, promotedStory ->
      val ongoingStoryViewModel = getOngoingStoryViewModel(promotedStory, index)
      itemList.add(ongoingStoryViewModel)
    }
  }

  private fun addRecommendedStoryListSection(suggestedStoryList: List<PromotedStory>) {
    val showDivider = itemList.isNotEmpty()
    val recommendedSectionTitleViewModel =
      SectionTitleViewModel(
        resourceHandler.getStringInLocale(R.string.recommended_stories),
        showDivider
      )
    itemList.add(recommendedSectionTitleViewModel)
    suggestedStoryList.forEachIndexed { index, suggestedStory ->
      val ongoingStoryViewModel = getOngoingStoryViewModel(suggestedStory, index)
      itemList.add(ongoingStoryViewModel)
    }
  }

  private fun getAssumedSuccessfulPromotedActivityList(): LiveData<PromotedActivityList> {
    // If there's an error loading the data, assume the default.
    return Transformations.map(ongoingStoryListSummaryResultLiveData) {
      it.getOrDefault(
        PromotedActivityList.getDefaultInstance()
      )
    }
  }

  private fun createLayoutManager(
    recentStoryCount: Int,
    oldStoryCount: Int,
    suggestedStoryCount: Int
  ): RecyclerView.LayoutManager {
    val sectionTitle0Position = if (recentStoryCount == 0) {
      // If recent story count is 0, that means that section title 0 will not be visible.
      -1
    } else {
      0
    }
    val sectionTitle1Position = if (oldStoryCount == 0) {
      // If old story count is 0, that means that section title 1 will not be visible.
      -1
    } else if (recentStoryCount == 0) {
      0
    } else {
      recentStoryCount + 1
    }
    val sectionTitle2Position = when {
      suggestedStoryCount == 0 -> {
        -1 // If suggested story count is 0, that means that section title 1 will not be visible.
      }
      oldStoryCount == 0 && recentStoryCount == 0 -> {
        0
      }
      oldStoryCount > 0 && recentStoryCount > 0 -> {
        recentStoryCount + oldStoryCount + 2
      }
      else -> {
        recentStoryCount + oldStoryCount + 1
      }
    }

    val spanCount = activity.resources.getInteger(R.integer.recently_played_span_count)
    ongoingListAdapter.setSpanCount(spanCount)

    val layoutManager = GridLayoutManager(activity.applicationContext, spanCount)
    layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
      override fun getSpanSize(position: Int): Int {
        return when (position) {
          sectionTitle0Position, sectionTitle1Position, sectionTitle2Position -> {
            /* number of spaces this item should occupy = */ spanCount
          }
          else -> {
            /* number of spaces this item should occupy = */ 1
          }
        }
      }
    }
    return layoutManager
  }

  fun onOngoingStoryClicked(promotedStory: PromotedStory) {
    val shouldSavePartialProgress =
      when (promotedStory.chapterPlayState) {
        ChapterPlayState.IN_PROGRESS_SAVED, ChapterPlayState.IN_PROGRESS_NOT_SAVED,
        ChapterPlayState.STARTED_NOT_COMPLETED, ChapterPlayState.NOT_STARTED -> true
        else -> false
      }
    if (promotedStory.chapterPlayState == ChapterPlayState.IN_PROGRESS_SAVED) {
      val explorationCheckpointLiveData =
        explorationCheckpointController.retrieveExplorationCheckpoint(
          ProfileId.newBuilder().apply {
            internalId = internalProfileId
          }.build(),
          promotedStory.explorationId
        ).toLiveData()

      explorationCheckpointLiveData.observe(
        fragment,
        object : Observer<AsyncResult<ExplorationCheckpoint>> {
          override fun onChanged(it: AsyncResult<ExplorationCheckpoint>) {
            if (it.isSuccess()) {
              explorationCheckpointLiveData.removeObserver(this)
              routeToResumeLessonListener.routeToResumeLesson(
                internalProfileId,
                promotedStory.topicId,
                promotedStory.storyId,
                promotedStory.explorationId,
                backflowScreen = null,
                explorationCheckpoint = it.getOrThrow()
              )
            } else if (it.isFailure()) {
              explorationCheckpointLiveData.removeObserver(this)
              playExploration(
                promotedStory.topicId,
                promotedStory.storyId,
                promotedStory.explorationId,
                shouldSavePartialProgress
              )
            }
          }
        }
      )
    } else {
      playExploration(
        promotedStory.topicId,
        promotedStory.storyId,
        promotedStory.explorationId,
        shouldSavePartialProgress
      )
    }
  }

  private fun playExploration(
    topicId: String,
    storyId: String,
    explorationId: String,
    shouldSavePartialProgress: Boolean,
  ) {
    explorationDataController.startPlayingExploration(
      internalProfileId,
      topicId,
      storyId,
      explorationId,
      shouldSavePartialProgress,
      // Pass an empty checkpoint if the exploration does not have to be resumed.
      ExplorationCheckpoint.getDefaultInstance()
    ).toLiveData().observe(
      fragment,
      Observer<AsyncResult<Any?>> { result ->
        when {
          result.isPending() -> oppiaLogger.d("RecentlyPlayedFragment", "Loading exploration")
          result.isFailure() -> oppiaLogger.e(
            "RecentlyPlayedFragment",
            "Failed to load exploration",
            result.getErrorOrNull()!!
          )
          else -> {
            oppiaLogger.d("RecentlyPlayedFragment", "Successfully loaded exploration")
            routeToExplorationListener.routeToExploration(
              internalProfileId,
              topicId,
              storyId,
              explorationId,
              backflowScreen = null,
              shouldSavePartialProgress
            )
            activity.finish()
          }
        }
      }
    )
  }
}
