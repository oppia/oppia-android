package org.oppia.app.story

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import org.oppia.app.databinding.StoryChapterViewBinding
import org.oppia.app.databinding.StoryFragmentBinding
import org.oppia.app.databinding.StoryHeaderViewBinding
import org.oppia.app.home.RouteToExplorationListener
import org.oppia.app.recyclerview.BindableAdapter
import org.oppia.app.story.storyitemviewmodel.StoryChapterSummaryViewModel
import org.oppia.app.story.storyitemviewmodel.StoryHeaderViewModel
import org.oppia.app.story.storyitemviewmodel.StoryItemViewModel
import org.oppia.app.viewmodel.ViewModelProvider
import javax.inject.Inject

/** The presenter for [StoryFragment]. */
class StoryFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val fragment: Fragment,
  private val viewModelProvider: ViewModelProvider<StoryViewModel>
) {
  private val routeToExplorationListener = activity as RouteToExplorationListener

  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?, storyId: String): View? {
    val viewModel = getStoryViewModel()
    val binding = StoryFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)
    viewModel.setStoryId(storyId)

    binding.toolbar.setNavigationOnClickListener{
      (activity as StoryActivity).finish()
    }

    binding.storyChapterList.apply {
      adapter = createRecyclerViewAdapter()
    }

    // NB: Both the view model and lifecycle owner must be set in order to correctly bind LiveData elements to
    // data-bound view models.
    binding.let {
      it.lifecycleOwner = fragment
      it.viewModel = viewModel
    }
    return binding.root
  }

  fun handleSelectExploration(explorationId: String) {
    routeToExplorationListener.routeToExploration(explorationId)
  }

  private fun createRecyclerViewAdapter(): BindableAdapter<StoryItemViewModel> {
    return BindableAdapter.Builder
      .newBuilder<StoryItemViewModel>()
      .registerViewTypeComputer { viewModel ->
        when (viewModel) {
          is StoryHeaderViewModel -> ViewType.VIEW_TYPE_HEADER.ordinal
          is StoryChapterSummaryViewModel -> ViewType.VIEW_TYPE_CHAPTER.ordinal
          else -> throw IllegalArgumentException("Encountered unexpected view model: $viewModel")
        }
      }
      .registerViewDataBinder(
        viewType = ViewType.VIEW_TYPE_HEADER.ordinal,
        inflateDataBinding = StoryHeaderViewBinding::inflate,
        setViewModel = StoryHeaderViewBinding::setViewModel,
        transformViewModel = { it as StoryHeaderViewModel }
      )
      .registerViewDataBinder(
        viewType = ViewType.VIEW_TYPE_CHAPTER.ordinal,
        inflateDataBinding = StoryChapterViewBinding::inflate,
        setViewModel = StoryChapterViewBinding::setViewModel,
        transformViewModel = { it as StoryChapterSummaryViewModel }
      )
      .build()
  }

  private fun getStoryViewModel(): StoryViewModel {
    return viewModelProvider.getForFragment(fragment, StoryViewModel::class.java)
  }

  private enum class ViewType {
    VIEW_TYPE_HEADER,
    VIEW_TYPE_CHAPTER
  }
}
