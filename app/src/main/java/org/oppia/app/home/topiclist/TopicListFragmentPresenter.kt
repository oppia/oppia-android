package org.oppia.app.home.topiclist

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import org.oppia.app.application.ApplicationContext
import org.oppia.app.databinding.TopicListFragmentBinding
import org.oppia.app.databinding.TopicSummaryViewBinding
import org.oppia.app.model.TopicSummary
import org.oppia.app.recyclerview.BindableAdapter
import org.oppia.app.viewmodel.ViewModelProvider
import javax.inject.Inject

/** Presenter for [TopicListFragment]. */
class TopicListFragmentPresenter @Inject constructor(
  @ApplicationContext private val context: Context,
  private val fragment: Fragment,
  private val viewModelProvider: ViewModelProvider<TopicListViewModel>
) {
  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
    val binding = TopicListFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)
    binding.recyclerView.apply {
      adapter = createRecyclerViewAdapter()
      // https://stackoverflow.com/a/50075019/3689782
      layoutManager = GridLayoutManager(context, /* spanCount= */ 2)
    }
    binding.let {
      it.viewModel = getTopicListViewModel()
      it.lifecycleOwner = fragment
    }
    return binding.root
  }

  private fun createRecyclerViewAdapter(): BindableAdapter<TopicSummary> {
    return BindableAdapter.Builder
      .newBuilder<TopicSummary>()
      .registerViewDataBinder(
        inflateDataBinding = TopicSummaryViewBinding::inflate,
        setViewModel = TopicSummaryViewBinding::setTopicSummary)
      .build()
  }

  private fun getTopicListViewModel(): TopicListViewModel {
    return viewModelProvider.getForFragment(fragment, TopicListViewModel::class.java)
  }
}
