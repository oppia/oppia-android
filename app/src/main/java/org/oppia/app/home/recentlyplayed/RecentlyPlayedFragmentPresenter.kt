package org.oppia.app.home.recentlyplayed

import android.content.res.Resources
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
import org.oppia.app.R
import org.oppia.app.databinding.RecentlyPlayedFragmentBinding
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.home.RouteToExplorationListener
import org.oppia.app.model.OngoingStoryList
import org.oppia.app.model.ProfileId
import org.oppia.app.model.PromotedStory
import org.oppia.domain.exploration.ExplorationDataController
import org.oppia.domain.topic.TopicListController
import org.oppia.util.data.AsyncResult
import org.oppia.util.logging.ConsoleLogger
import org.oppia.util.parser.StoryHtmlParserEntityType
import javax.inject.Inject

/** The presenter for [RecentlyPlayedFragment]. */
@FragmentScope
class RecentlyPlayedFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val fragment: Fragment,
  private val logger: ConsoleLogger,
  private val explorationDataController: ExplorationDataController,
  private val topicListController: TopicListController,
  @StoryHtmlParserEntityType private val entityType: String
) {

  private val routeToExplorationListener = activity as RouteToExplorationListener
  private var internalProfileId: Int = -1
  private lateinit var binding: RecentlyPlayedFragmentBinding
  private lateinit var ongoingListAdapter: OngoingListAdapter
  private val itemList: MutableList<RecentlyPlayedItemViewModel> = ArrayList()
  private val orientation = Resources.getSystem().configuration.orientation

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
    LiveData<AsyncResult<OngoingStoryList>>
    by lazy {
      topicListController.getOngoingStoryList(
        ProfileId.newBuilder().setInternalId(internalProfileId).build()
      )
    }

  private fun subscribeToOngoingStoryList() {
    getAssumedSuccessfulOngoingStoryList().observe(
      fragment,
      Observer<OngoingStoryList> { it ->
        if (it.recentStoryCount > 0) {
          val recentSectionTitleViewModel =
            SectionTitleViewModel(activity.getString(R.string.ongoing_story_last_week), false)
          itemList.add(recentSectionTitleViewModel)
          for (promotedStory in it.recentStoryList) {
            val ongoingStoryViewModel =
              OngoingStoryViewModel(
                promotedStory,
                entityType,
                fragment as OngoingStoryClickListener
              )
            itemList.add(ongoingStoryViewModel)
          }
        }

        if (it.olderStoryCount > 0) {
          val showDivider = itemList.isNotEmpty()
          val olderSectionTitleViewModel =
            SectionTitleViewModel(
              activity.getString(R.string.ongoing_story_last_month),
              showDivider
            )
          itemList.add(olderSectionTitleViewModel)
          for (promotedStory in it.olderStoryList) {
            val ongoingStoryViewModel =
              OngoingStoryViewModel(
                promotedStory,
                entityType,
                fragment as OngoingStoryClickListener
              )
            itemList.add(ongoingStoryViewModel)
          }
        }
        binding.ongoingStoryRecyclerView.layoutManager =
          createLayoutManager(it.recentStoryCount, it.olderStoryCount)
        ongoingListAdapter.notifyDataSetChanged()
      }
    )
  }

  private fun getAssumedSuccessfulOngoingStoryList(): LiveData<OngoingStoryList> {
    // If there's an error loading the data, assume the default.
    return Transformations.map(ongoingStoryListSummaryResultLiveData) {
      it.getOrDefault(
        OngoingStoryList.getDefaultInstance()
      )
    }
  }

  private fun createLayoutManager(
    recentStoryCount: Int,
    oldStoryCount: Int
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

    val spanCount = activity.resources.getInteger(R.integer.recently_played_span_count)
    ongoingListAdapter.setSpanCount(spanCount)

    val layoutManager = GridLayoutManager(activity.applicationContext, spanCount)
    layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
      override fun getSpanSize(position: Int): Int {
        return if (position == sectionTitle0Position || position == sectionTitle1Position) {
          /* number of spaces this item should occupy = */ spanCount
        } else {
          /* number of spaces this item should occupy = */ 1
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
      explorationId
    ).observe(
      fragment,
      Observer<AsyncResult<Any?>> { result ->
        when {
          result.isPending() -> logger.d("RecentlyPlayedFragment", "Loading exploration")
          result.isFailure() -> logger.e(
            "RecentlyPlayedFragment",
            "Failed to load exploration",
            result.getErrorOrNull()!!
          )
          else -> {
            logger.d("RecentlyPlayedFragment", "Successfully loaded exploration")
            routeToExplorationListener.routeToExploration(
              internalProfileId,
              topicId,
              storyId,
              explorationId,
              /* backflowScreen = */ null
            )
            activity.finish()
          }
        }
      }
    )
  }
}
