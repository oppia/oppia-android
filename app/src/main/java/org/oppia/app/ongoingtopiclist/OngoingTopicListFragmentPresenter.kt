package org.oppia.app.ongoingtopiclist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import org.oppia.app.databinding.OngoingTopicListFragmentBinding
import org.oppia.app.databinding.StoryChapterViewBinding
import org.oppia.app.recyclerview.BindableAdapter
import org.oppia.app.story.storyitemviewmodel.StoryChapterSummaryViewModel
import org.oppia.app.story.storyitemviewmodel.StoryItemViewModel
import org.oppia.app.viewmodel.ViewModelProvider
import javax.inject.Inject

/** The presenter for [OngoingTopicListFragment]. */
class OngoingTopicListFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val fragment: Fragment,
  private val viewModelProvider: ViewModelProvider<OngoingTopicListViewModel>
) {

  private lateinit var binding: OngoingTopicListFragmentBinding

  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?, internalProfileId: Int): View? {
    val viewModel = getOngoingTopicListViewModel()
    binding = OngoingTopicListFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)
    viewModel.setProfileId(internalProfileId)

    binding.ongoingTopicListToolbar.setNavigationOnClickListener {
      (activity as OngoingTopicListActivity).finish()
    }

    binding.ongoingTopicList.apply {
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
          is StoryChapterSummaryViewModel -> ViewType.VIEW_TYPE_ONGOING_TOPIC
          else -> throw IllegalArgumentException("Encountered unexpected view model: $viewModel")
        }
      }
      .registerViewDataBinder(
        viewType = ViewType.VIEW_TYPE_ONGOING_TOPIC,
        inflateDataBinding = StoryChapterViewBinding::inflate,
        setViewModel = StoryChapterViewBinding::setViewModel,
        transformViewModel = { it as StoryChapterSummaryViewModel }
      )
      .build()
  }

  private fun getOngoingTopicListViewModel(): OngoingTopicListViewModel {
    return viewModelProvider.getForFragment(fragment, OngoingTopicListViewModel::class.java)
  }

  private enum class ViewType {
    VIEW_TYPE_ONGOING_TOPIC
  }
}
