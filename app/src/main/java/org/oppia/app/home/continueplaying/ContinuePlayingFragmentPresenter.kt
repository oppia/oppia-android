package org.oppia.app.home.continueplaying

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import org.oppia.app.databinding.ContinuePlayingFragmentBinding
import org.oppia.app.databinding.OngoingStoryCardBinding
import org.oppia.app.databinding.SectionTitleBinding
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.home.RouteToExplorationListener
import org.oppia.app.model.PromotedStory
import org.oppia.app.recyclerview.BindableAdapter
import org.oppia.app.viewmodel.ViewModelProvider
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
  private val topicListController: TopicListController,
  private val viewModelProvider: ViewModelProvider<ContinuePlayViewModel>
) {

  private val routeToExplorationListener = activity as RouteToExplorationListener

  private lateinit var binding: ContinuePlayingFragmentBinding

//  private lateinit var ongoingListAdapter: OngoingListAdapter

  private val itemList: MutableList<ContinuePlayingItemViewModel> = ArrayList()

  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
    binding = ContinuePlayingFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)
    val viewModel = getContinuePlayModel()
    binding.continuePlayingToolbar.setNavigationOnClickListener {
      (activity as ContinuePlayingActivity).finish()
    }

//    ongoingListAdapter = OngoingListAdapter(itemList)

    binding.ongoingStoryRecyclerView.apply {
      adapter = createRecyclerViewAdapter()
    }
    binding.let {
      it.lifecycleOwner = fragment
      it.viewModel = viewModel
    }

//    subscribeToOngoingStoryList()

    return binding.root
  }

//  private val ongoingStoryListSummaryResultLiveData: LiveData<AsyncResult<OngoingStoryList>> by lazy {
//    topicListController.getOngoingStoryList()
//  }
//
//  private fun subscribeToOngoingStoryList() {
//    getAssumedSuccessfulOngoingStoryList().observe(fragment, Observer<OngoingStoryList> { it ->
//      if (it.recentStoryCount > 0) {
//        val recentSectionTitleViewModel =
//          SectionTitleViewModel(activity.getString(R.string.ongoing_story_last_week), false)
//        itemList.add(recentSectionTitleViewModel)
//        for (promotedStory in it.recentStoryList) {
//          val ongoingStoryViewModel = OngoingStoryViewModel(promotedStory, fragment as OngoingStoryClickListener)
//          itemList.add(ongoingStoryViewModel)
//        }
//      }
//
//      if (it.olderStoryCount > 0) {
//        val showDivider = itemList.isNotEmpty()
//        val olderSectionTitleViewModel =
//          SectionTitleViewModel(activity.getString(R.string.ongoing_story_last_month), showDivider)
//        itemList.add(olderSectionTitleViewModel)
//        for (promotedStory in it.olderStoryList) {
//          val ongoingStoryViewModel = OngoingStoryViewModel(promotedStory, fragment as OngoingStoryClickListener)
//          itemList.add(ongoingStoryViewModel)
//        }
//      }
////      ongoingListAdapter.notifyDataSetChanged()
//    })
//  }
  private fun createRecyclerViewAdapter(): BindableAdapter<ContinuePlayingItemViewModel> {
    return BindableAdapter.MultiTypeBuilder
      .newBuilder<ContinuePlayingItemViewModel, ViewType> { viewModel ->
        when (viewModel) {
          is SectionTitleViewModel -> ViewType.VIEW_TYPE_SECTION_TITLE_TEXT
          is OngoingStoryViewModel -> ViewType.VIEW_TYPE_SECTION_STORY_ITEM
          else -> throw IllegalArgumentException("Encountered unexpected view model: $viewModel")
        }
      }
      .registerViewDataBinder(
        viewType = ViewType.VIEW_TYPE_SECTION_TITLE_TEXT,
        inflateDataBinding = SectionTitleBinding::inflate,
        setViewModel = SectionTitleBinding::setViewModel,
        transformViewModel = { it as SectionTitleViewModel }
      )
      .registerViewDataBinder(
        viewType = ViewType.VIEW_TYPE_SECTION_STORY_ITEM,
        inflateDataBinding = OngoingStoryCardBinding::inflate,
        setViewModel = OngoingStoryCardBinding::setViewModel,
        transformViewModel = { it as OngoingStoryViewModel }
      )
      .build()
  }


  private fun getContinuePlayModel(): ContinuePlayViewModel {
    return viewModelProvider.getForFragment(fragment, ContinuePlayViewModel::class.java)
  }
  private enum class ViewType {
    VIEW_TYPE_SECTION_TITLE_TEXT,
    VIEW_TYPE_SECTION_STORY_ITEM
  }

//  private fun getAssumedSuccessfulOngoingStoryList(): LiveData<OngoingStoryList> {
//    // If there's an error loading the data, assume the default.
//    return Transformations.map(ongoingStoryListSummaryResultLiveData) { it.getOrDefault(OngoingStoryList.getDefaultInstance()) }
//  }

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
