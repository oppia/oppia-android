package org.oppia.android.app.devoptions.markstoriescompleted

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.recyclerview.BindableAdapter
import org.oppia.android.app.viewmodel.ViewModelProvider
import org.oppia.android.databinding.MarkStoriesCompletedFragmentBinding
import org.oppia.android.databinding.MarkStoriesCompletedStorySummaryViewBinding
import javax.inject.Inject

/** The presenter for [MarkStoriesCompletedFragment]. */
@FragmentScope
class MarkStoriesCompletedFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val fragment: Fragment,
  private val viewModelProvider: ViewModelProvider<MarkStoriesCompletedViewModel>
) {

  private lateinit var binding: MarkStoriesCompletedFragmentBinding
  private lateinit var linearLayoutManager: LinearLayoutManager
  private lateinit var bindingAdapter: BindableAdapter<StorySummaryViewModel>

  fun handleCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    internalProfileId: Int
  ): View? {
    binding = MarkStoriesCompletedFragmentBinding.inflate(
      inflater,
      container,
      /* attachToRoot= */ false
    )

    binding.markStoriesCompletedToolbar.setNavigationOnClickListener {
      (activity as MarkStoriesCompletedActivity).finish()
    }

    binding.apply {
      this.lifecycleOwner = fragment
      this.viewModel = getMarkStoriesCompletedViewModel()
    }

    getMarkStoriesCompletedViewModel().setInternalProfileId(internalProfileId)

    linearLayoutManager = LinearLayoutManager(activity.applicationContext)

    bindingAdapter = createRecyclerViewAdapter()
    binding.markStoriesCompletedStorySummaryRecyclerView.apply {
      layoutManager = linearLayoutManager
      adapter = bindingAdapter
    }

    return binding.root
  }

  private fun createRecyclerViewAdapter(): BindableAdapter<StorySummaryViewModel> {
    return BindableAdapter.SingleTypeBuilder
      .newBuilder<StorySummaryViewModel>()
      .registerViewDataBinderWithSameModelType(
        inflateDataBinding = MarkStoriesCompletedStorySummaryViewBinding::inflate,
        setViewModel = MarkStoriesCompletedStorySummaryViewBinding::setViewModel
      )
      .build()
  }

  private fun getMarkStoriesCompletedViewModel(): MarkStoriesCompletedViewModel {
    return viewModelProvider.getForFragment(fragment, MarkStoriesCompletedViewModel::class.java)
  }
}
