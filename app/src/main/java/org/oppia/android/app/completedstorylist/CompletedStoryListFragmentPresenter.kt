package org.oppia.android.app.completedstorylist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import org.oppia.android.R
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.recyclerview.BindableAdapter
import org.oppia.android.databinding.CompletedStoryItemBinding
import org.oppia.android.databinding.CompletedStoryListFragmentBinding
import javax.inject.Inject

/** The presenter for [CompletedStoryListFragment]. */
class CompletedStoryListFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val fragment: Fragment,
  private val viewModel: CompletedStoryListViewModel,
  private val singleTypeBuilderFactory: BindableAdapter.SingleTypeBuilder.Factory
) {

  private lateinit var binding: CompletedStoryListFragmentBinding

  /** Initializes and creates the views for [CompletedStoryListFragment]. */
  fun handleCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    profileId: ProfileId
  ): View? {
    viewModel.setProfileId(profileId)

    binding = CompletedStoryListFragmentBinding
      .inflate(
        inflater,
        container,
        /* attachToRoot= */ false
      )
    binding.completedStoryListToolbar.setNavigationOnClickListener {
      (activity as CompletedStoryListActivity).finish()
    }
    binding.completedStoryList.apply {
      val spanCount = activity.resources.getInteger(R.integer.completed_story_span_count)
      layoutManager = GridLayoutManager(context, spanCount)
      adapter = createRecyclerViewAdapter()
    }
    binding.let {
      it.lifecycleOwner = fragment
      it.viewModel = viewModel
    }
    return binding.root
  }

  private fun createRecyclerViewAdapter(): BindableAdapter<CompletedStoryItemViewModel> {
    return singleTypeBuilderFactory.create<CompletedStoryItemViewModel>()
      .registerViewDataBinderWithSameModelType(
        inflateDataBinding = CompletedStoryItemBinding::inflate,
        setViewModel = CompletedStoryItemBinding::setViewModel
      )
      .build()
  }
}
