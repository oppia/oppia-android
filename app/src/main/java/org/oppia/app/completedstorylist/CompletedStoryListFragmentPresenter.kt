package org.oppia.app.completedstorylist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import org.oppia.app.databinding.CompletedStoryListFragmentBinding
import org.oppia.app.databinding.StoryChapterViewBinding
import org.oppia.app.databinding.StoryFragmentBinding
import org.oppia.app.recyclerview.BindableAdapter
import org.oppia.app.story.storyitemviewmodel.StoryChapterSummaryViewModel
import org.oppia.app.story.storyitemviewmodel.StoryItemViewModel
import org.oppia.app.viewmodel.ViewModelProvider
import javax.inject.Inject

/** The presenter for [StoryFragment]. */
class CompletedStoryListFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val fragment: Fragment,
  private val viewModelProvider: ViewModelProvider<CompletedStoryListViewModel>
) {

  private lateinit var binding: CompletedStoryListFragmentBinding

  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?, internalProfileId: Int): View? {
    val viewModel = getCompletedStoryListViewModel()
    binding = CompletedStoryListFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)
    viewModel.setProfileId(internalProfileId)

    binding.completedStoryListToolbar.setNavigationOnClickListener {
      (activity as CompletedStoryListActivity).finish()
    }

    binding.completedStoryList.apply {
      layoutManager = LinearLayoutManager(activity.applicationContext)
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

  private fun createRecyclerViewAdapter(): BindableAdapter<StoryItemViewModel> {
    return BindableAdapter.MultiTypeBuilder
      .newBuilder<StoryItemViewModel, ViewType> { viewModel ->
        when (viewModel) {
          is StoryChapterSummaryViewModel -> ViewType.VIEW_TYPE_COMPLETED_STORY
          else -> throw IllegalArgumentException("Encountered unexpected view model: $viewModel")
        }
      }
      .registerViewDataBinder(
        viewType = ViewType.VIEW_TYPE_COMPLETED_STORY,
        inflateDataBinding = StoryChapterViewBinding::inflate,
        setViewModel = StoryChapterViewBinding::setViewModel,
        transformViewModel = { it as StoryChapterSummaryViewModel }
      )
      .build()
  }

  private fun getCompletedStoryListViewModel(): CompletedStoryListViewModel {
    return viewModelProvider.getForFragment(fragment, CompletedStoryListViewModel::class.java)
  }

  private enum class ViewType {
    VIEW_TYPE_COMPLETED_STORY
  }
}
