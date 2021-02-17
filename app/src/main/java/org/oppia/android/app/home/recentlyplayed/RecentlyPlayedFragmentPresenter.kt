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
import org.oppia.android.app.recyclerview.BindableAdapter
import org.oppia.android.databinding.OngoingStoryCardBinding
import org.oppia.android.databinding.RecentlyPlayedFragmentBinding
import org.oppia.android.databinding.SectionTitleBinding
import org.oppia.android.domain.exploration.ExplorationDataController
import org.oppia.android.domain.topic.TopicListController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import org.oppia.android.util.logging.ConsoleLogger
import org.oppia.android.util.parser.StoryHtmlParserEntityType
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
  private lateinit var ongoingListAdapter: BindableAdapter<RecentlyPlayedItemViewModel>

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

    ongoingListAdapter = createRecyclerViewAdapter()
    binding.ongoingStoryRecyclerView.apply {
      adapter = ongoingListAdapter
    }
    binding.lifecycleOwner = fragment
    binding.viewModel = RecentlyPlayedViewModel(
      activity,
      fragment,
      topicListController,
      internalProfileId,
      entityType
    )

    return binding.root
  }

  private fun createRecyclerViewAdapter(): BindableAdapter<RecentlyPlayedItemViewModel> {
    return BindableAdapter.MultiTypeBuilder
      .newBuilder<RecentlyPlayedItemViewModel, ViewType> { viewModel ->
        when (viewModel) {
          is SectionTitleViewModel -> ViewType.SECTION_TITLE_TEXT
          is OngoingStoryViewModel -> ViewType.SECTION_STORY_ITEM
          else -> throw IllegalArgumentException("Encountered unexpected view model: $viewModel")
        }
      }
      .setLifecycleOwner(fragment)
      .registerViewDataBinder(
        viewType = ViewType.SECTION_TITLE_TEXT,
        inflateDataBinding = SectionTitleBinding::inflate,
        setViewModel = SectionTitleBinding::setViewModel,
        transformViewModel = { it as SectionTitleViewModel }
      )
      .registerViewDataBinder(
        viewType = ViewType.SECTION_STORY_ITEM,
        inflateDataBinding = OngoingStoryCardBinding::inflate,
        setViewModel = OngoingStoryCardBinding::setViewModel,
        transformViewModel = { it as OngoingStoryViewModel }
      )
      .build()
  }

  private enum class ViewType {
    SECTION_TITLE_TEXT,
    SECTION_STORY_ITEM
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
