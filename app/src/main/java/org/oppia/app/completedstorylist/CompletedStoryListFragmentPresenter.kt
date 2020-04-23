package org.oppia.app.completedstorylist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import org.oppia.app.R
import org.oppia.app.databinding.CompletedStoryItemBinding
import org.oppia.app.databinding.CompletedStoryListFragmentBinding
import org.oppia.app.recyclerview.BindableAdapter
import org.oppia.app.viewmodel.ViewModelProvider
import javax.inject.Inject

/** The presenter for [CompletedStoryListFragment]. */
class CompletedStoryListFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val fragment: Fragment,
  private val viewModelProvider: ViewModelProvider<CompletedStoryListViewModel>
) {

  private lateinit var binding: CompletedStoryListFragmentBinding

  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?, internalProfileId: Int): View? {
    val viewModel = getCompletedStoryListViewModel()
    viewModel.setProfileId(internalProfileId)

    binding = CompletedStoryListFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)

    val toolbar = binding.completedStoryListFragmentToolbar as Toolbar
    activity.setSupportActionBar(toolbar)
    activity.supportActionBar!!.setTitle(R.string.stories_completed)

    toolbar.setNavigationOnClickListener {
      (activity as CompletedStoryListActivity).finish()
    }
    binding.completedStoryList.apply {
      layoutManager = LinearLayoutManager(activity.applicationContext)
      adapter = createRecyclerViewAdapter()
    }
    binding.let {
      it.lifecycleOwner = fragment
      it.viewModel = viewModel
    }
    return binding.root
  }

  private fun createRecyclerViewAdapter(): BindableAdapter<CompletedStoryItemViewModel> {
    return BindableAdapter.SingleTypeBuilder
      .newBuilder<CompletedStoryItemViewModel>()
      .registerViewDataBinderWithSameModelType(
        inflateDataBinding = CompletedStoryItemBinding::inflate,
        setViewModel = CompletedStoryItemBinding::setViewModel
      )
      .build()
  }

  private fun getCompletedStoryListViewModel(): CompletedStoryListViewModel {
    return viewModelProvider.getForFragment(fragment, CompletedStoryListViewModel::class.java)
  }
}
