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
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.PromotedActivityList
import org.oppia.android.app.model.PromotedStory
import org.oppia.android.databinding.RecentlyPlayedFragmentBinding
import org.oppia.android.domain.exploration.ExplorationDataController
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
  @StoryHtmlParserEntityType private val entityType: String
) {
  // TODO(#3479): Enable checkpointing once mechanism to resume exploration with checkpoints is
  //  implemented.

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
          binding.recentlyPlayedToolbar.title = activity.getString(R.string.recently_played_stories)
          addRecentlyPlayedStoryListSection(it.promotedStoryList.recentlyPlayedStoryList)
        }

        if (it.promotedStoryList.olderPlayedStoryList.isNotEmpty()) {
          binding.recentlyPlayedToolbar.title = activity.getString(R.string.recently_played_stories)
          addOlderStoryListSection(it.promotedStoryList.olderPlayedStoryList)
        }

        if (it.promotedStoryList.suggestedStoryList.isNotEmpty()) {
          binding.recentlyPlayedToolbar.title = activity.getString(R.string.stories_for_you)
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
    val recentSectionTitleViewModel =
      SectionTitleViewModel(activity.getString(R.string.ongoing_story_last_week), false)
    itemList.add(recentSectionTitleViewModel)
    for (promotedStory in recentlyPlayedStoryList) {
      val ongoingStoryViewModel = getOngoingStoryViewModel(promotedStory)
      itemList.add(ongoingStoryViewModel)
    }
  }

  private fun getOngoingStoryViewModel(promotedStory: PromotedStory): RecentlyPlayedItemViewModel {
    return OngoingStoryViewModel(
      promotedStory,
      entityType,
      fragment as OngoingStoryClickListener
    )
  }

  private fun addOlderStoryListSection(olderPlayedStoryList: List<PromotedStory>) {
    val showDivider = itemList.isNotEmpty()
    val olderSectionTitleViewModel =
      SectionTitleViewModel(
        activity.getString(R.string.ongoing_story_last_month),
        showDivider
      )
    itemList.add(olderSectionTitleViewModel)
    for (promotedStory in olderPlayedStoryList) {
      val ongoingStoryViewModel = getOngoingStoryViewModel(promotedStory)
      itemList.add(ongoingStoryViewModel)
    }
  }

  private fun addRecommendedStoryListSection(suggestedStoryList: List<PromotedStory>) {
    val showDivider = itemList.isNotEmpty()
    val recommendedSectionTitleViewModel =
      SectionTitleViewModel(
        activity.getString(R.string.recommended_stories),
        showDivider
      )
    itemList.add(recommendedSectionTitleViewModel)
    for (suggestedStory in suggestedStoryList) {
      val ongoingStoryViewModel = getOngoingStoryViewModel(suggestedStory)
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
    playExploration(promotedStory.topicId, promotedStory.storyId, promotedStory.explorationId)
  }

  private fun playExploration(topicId: String, storyId: String, explorationId: String) {
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
              /* backflowScreen = */ null,
              isCheckpointingEnabled = false
            )
            activity.finish()
          }
        }
      }
    )
  }
}
