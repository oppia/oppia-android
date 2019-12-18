package org.oppia.app.home.continueplaying

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
import org.oppia.app.R
import org.oppia.app.databinding.ContinuePlayingFragmentBinding
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.home.RouteToExplorationListener
import org.oppia.app.model.OngoingStoryList
import org.oppia.app.model.PromotedStory
import org.oppia.domain.exploration.ExplorationDataController
import org.oppia.domain.topic.TopicListController
import org.oppia.util.data.AsyncResult
import org.oppia.util.logging.Logger
import javax.inject.Inject

/** The presenter for [ContinuePlayingFragment]. */
@FragmentScope
class ContinuePlayingFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val fragment: Fragment,
  private val logger: Logger,
  private val explorationDataController: ExplorationDataController,
  private val topicListController: TopicListController
) {

  private val routeToExplorationListener = activity as RouteToExplorationListener

  private lateinit var binding: ContinuePlayingFragmentBinding

  private lateinit var ongoingListAdapter: OngoingListAdapter

  private val itemList: MutableList<ContinuePlayingItemViewModel> = ArrayList()

  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
    binding = ContinuePlayingFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)

    binding.continuePlayingToolbar.setNavigationOnClickListener {
      (activity as ContinuePlayingActivity).finish()
    }

    ongoingListAdapter = OngoingListAdapter(itemList)

    binding.ongoingStoryRecyclerView.apply {
      adapter = ongoingListAdapter
    }
    binding.let {
      it.lifecycleOwner = fragment
    }

    subscribeToOngoingStoryList()

    return binding.root
  }

  private val ongoingStoryListSummaryResultLiveData: LiveData<AsyncResult<OngoingStoryList>> by lazy {
    topicListController.getOngoingStoryList()
  }

  private fun subscribeToOngoingStoryList() {
    getAssumedSuccessfulOngoingStoryList().observe(fragment, Observer<OngoingStoryList> { it ->
      if (it.recentStoryCount > 0) {
        val recentSectionTitleViewModel =
          SectionTitleViewModel(activity.getString(R.string.ongoing_story_last_week), false)
        itemList.add(recentSectionTitleViewModel)
        for (promotedStory in it.recentStoryList) {
          val ongoingStoryViewModel = OngoingStoryViewModel(promotedStory, fragment as OngoingStoryClickListener)
          itemList.add(ongoingStoryViewModel)
        }
      }

      if (it.olderStoryCount > 0) {
        val showDivider = itemList.isNotEmpty()
        val olderSectionTitleViewModel =
          SectionTitleViewModel(activity.getString(R.string.ongoing_story_last_month), showDivider)
        itemList.add(olderSectionTitleViewModel)
        for (promotedStory in it.olderStoryList) {
          val ongoingStoryViewModel = OngoingStoryViewModel(promotedStory, fragment as OngoingStoryClickListener)
          itemList.add(ongoingStoryViewModel)
        }
      }
      ongoingListAdapter.notifyDataSetChanged()
    })
  }

  private fun getAssumedSuccessfulOngoingStoryList(): LiveData<OngoingStoryList> {
    // If there's an error loading the data, assume the default.
    return Transformations.map(ongoingStoryListSummaryResultLiveData) { it.getOrDefault(OngoingStoryList.getDefaultInstance()) }
  }

  fun onOngoingStoryClicked(promotedStory: PromotedStory) {
    playExploration(promotedStory.explorationId, promotedStory.topicId)
  }

  private fun playExploration(explorationId: String, topicId: String) {
    explorationDataController.startPlayingExploration(
      explorationId
    ).observe(fragment, Observer<AsyncResult<Any?>> { result ->
      when {
        result.isPending() -> logger.d("ContinuePlayingFragment", "Loading exploration")
        result.isFailure() -> logger.e(
          "ContinuePlayingFragment",
          "Failed to load exploration",
          result.getErrorOrNull()!!
        )
        else -> {
          logger.d("ContinuePlayingFragment", "Successfully loaded exploration")
          routeToExplorationListener.routeToExploration(explorationId, topicId)
          activity.finish()
        }
      }
    })
  }
}
