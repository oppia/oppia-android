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
  private val viewModelProvider: ViewModelProvider<ContinuePlayViewModel>
) {

  private val routeToExplorationListener = activity as RouteToExplorationListener

  private lateinit var binding: ContinuePlayingFragmentBinding

  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
    binding = ContinuePlayingFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)
    val viewModel = getContinuePlayModel()
    binding.continuePlayingToolbar.setNavigationOnClickListener {
      (activity as ContinuePlayingActivity).finish()
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

  private fun createRecyclerViewAdapter(): BindableAdapter<ContinuePlayingItemViewModel> {
    return BindableAdapter.MultiTypeBuilder
      .newBuilder<ContinuePlayingItemViewModel, ContinuePlayingItemViewModel.ViewType> { viewModel ->
        when (viewModel) {
          is SectionTitleViewModel -> ContinuePlayingItemViewModel.ViewType.VIEW_TYPE_SECTION_TITLE_TEXT
          is OngoingStoryViewModel -> ContinuePlayingItemViewModel.ViewType.VIEW_TYPE_SECTION_STORY_ITEM
          else -> throw IllegalArgumentException("Encountered unexpected view model: $viewModel")
        }
      }
      .registerViewDataBinder(
        viewType = ContinuePlayingItemViewModel.ViewType.VIEW_TYPE_SECTION_TITLE_TEXT,
        inflateDataBinding = SectionTitleBinding::inflate,
        setViewModel = SectionTitleBinding::setViewModel,
        transformViewModel = { it as SectionTitleViewModel }
      )
      .registerViewDataBinder(
        viewType = ContinuePlayingItemViewModel.ViewType.VIEW_TYPE_SECTION_STORY_ITEM,
        inflateDataBinding = OngoingStoryCardBinding::inflate,
        setViewModel = OngoingStoryCardBinding::setViewModel,
        transformViewModel = { it as OngoingStoryViewModel }
      )
      .build()
  }

  private fun getContinuePlayModel(): ContinuePlayViewModel {
    return viewModelProvider.getForFragment(fragment, ContinuePlayViewModel::class.java)
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
