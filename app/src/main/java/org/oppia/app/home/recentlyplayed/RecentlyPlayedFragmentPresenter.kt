package org.oppia.app.home.recentlyplayed

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import org.oppia.app.databinding.RecentlyPlayedFragmentBinding
import org.oppia.app.databinding.OngoingStoryCardBinding
import org.oppia.app.databinding.SectionTitleBinding
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.home.RouteToExplorationListener
import org.oppia.app.model.PromotedStory
import org.oppia.app.recyclerview.BindableAdapter
import org.oppia.app.viewmodel.ViewModelProvider
import org.oppia.domain.exploration.ExplorationDataController
import org.oppia.util.data.AsyncResult
import org.oppia.util.logging.Logger
import javax.inject.Inject

/** The presenter for [RecentlyPlayedFragment]. */
@FragmentScope
class RecentlyPlayedFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val fragment: Fragment,
  private val logger: Logger,
  private val explorationDataController: ExplorationDataController,
  private val viewModelProvider: ViewModelProvider<RecentlyPlayedViewModel>
) {

  private val routeToExplorationListener = activity as RouteToExplorationListener

  private lateinit var binding: RecentlyPlayedFragmentBinding

  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
    binding = RecentlyPlayedFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)
    val viewModel = getContinuePlayModel()
    binding.recentlyPlayedToolbar.setNavigationOnClickListener {
      (activity as RecentlyPlayedActivity).finish()
    }

    binding.ongoingStoryRecyclerView.apply {
      adapter = createRecyclerViewAdapter()
    }
    binding.let {
      it.lifecycleOwner = fragment
      it.viewModel = viewModel
    }
    return binding.root
  }

  private fun createRecyclerViewAdapter(): BindableAdapter<RecentlyPlayedItemViewModel> {
    return BindableAdapter.MultiTypeBuilder
      .newBuilder<RecentlyPlayedItemViewModel, RecentlyPlayedItemViewModel.ViewType> { viewModel ->
        when (viewModel) {
          is SectionTitleViewModel -> RecentlyPlayedItemViewModel.ViewType.VIEW_TYPE_SECTION_TITLE_TEXT
          is OngoingStoryViewModel -> RecentlyPlayedItemViewModel.ViewType.VIEW_TYPE_SECTION_STORY_ITEM
          else -> throw IllegalArgumentException("Encountered unexpected view model: $viewModel")
        }
      }
      .registerViewDataBinder(
        viewType = RecentlyPlayedItemViewModel.ViewType.VIEW_TYPE_SECTION_TITLE_TEXT,
        inflateDataBinding = SectionTitleBinding::inflate,
        setViewModel = SectionTitleBinding::setViewModel,
        transformViewModel = { it as SectionTitleViewModel }
      )
      .registerViewDataBinder(
        viewType = RecentlyPlayedItemViewModel.ViewType.VIEW_TYPE_SECTION_STORY_ITEM,
        inflateDataBinding = OngoingStoryCardBinding::inflate,
        setViewModel = OngoingStoryCardBinding::setViewModel,
        transformViewModel = { it as OngoingStoryViewModel }
      )
      .build()
  }

  private fun getContinuePlayModel(): RecentlyPlayedViewModel {
    return viewModelProvider.getForFragment(fragment, RecentlyPlayedViewModel::class.java)
  }

  fun onOngoingStoryClicked(promotedStory: PromotedStory) {
    playExploration(promotedStory.explorationId, promotedStory.topicId)
  }

  private fun playExploration(explorationId: String, topicId: String) {
    explorationDataController.startPlayingExploration(
      explorationId
    ).observe(fragment, Observer<AsyncResult<Any?>> { result ->
      when {
        result.isPending() -> logger.d("RecentlyPlayedFragment", "Loading exploration")
        result.isFailure() -> logger.e(
          "RecentlyPlayedFragment",
          "Failed to load exploration",
          result.getErrorOrNull()!!
        )
        else -> {
          logger.d("RecentlyPlayedFragment", "Successfully loaded exploration")
          routeToExplorationListener.routeToExploration(explorationId, topicId)
          activity.finish()
        }
      }
    })
  }
}
