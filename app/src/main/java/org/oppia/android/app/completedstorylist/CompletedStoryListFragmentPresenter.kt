package org.oppia.android.app.completedstorylist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import org.oppia.android.app.databinding.databinding.CompletedStoryItemBinding
import org.oppia.android.app.databinding.databinding.CompletedStoryListFragmentBinding
import org.oppia.android.app.model.LegacyProfileId
import org.oppia.android.app.recyclerview.BindableAdapter
import org.oppia.android.app.ui.R
import org.oppia.android.app.utility.EdgeToEdgeHelper
import org.oppia.android.util.platformparameter.EnableEdgeToEdge
import org.oppia.android.util.platformparameter.PlatformParameterValue
import javax.inject.Inject

/** The presenter for [CompletedStoryListFragment]. */
class CompletedStoryListFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val fragment: Fragment,
  private val viewModel: CompletedStoryListViewModel,
  private val singleTypeBuilderFactory: BindableAdapter.SingleTypeBuilder.Factory,
  @EnableEdgeToEdge private val enableEdgeToEdge: PlatformParameterValue<Boolean>
) {

  private lateinit var binding: CompletedStoryListFragmentBinding

  /** Initializes and creates the views for [CompletedStoryListFragment]. */
  fun handleCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    profileId: LegacyProfileId
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

    if (enableEdgeToEdge.value) {
      EdgeToEdgeHelper.applyToAppBarLayout(
        activity,
        binding.completedStoryListToolbar,
        R.color.component_color_shared_activity_status_bar_color
      )
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
