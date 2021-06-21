package org.oppia.android.app.devoptions.marktopicscompleted

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.recyclerview.BindableAdapter
import org.oppia.android.app.viewmodel.ViewModelProvider
import org.oppia.android.databinding.MarkTopicsCompletedFragmentBinding
import org.oppia.android.databinding.MarkTopicsCompletedTopicSummaryViewBinding
import javax.inject.Inject

/** The presenter for [MarkTopicsCompletedFragment]. */
@FragmentScope
class MarkTopicsCompletedFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val fragment: Fragment,
  private val viewModelProvider: ViewModelProvider<MarkTopicsCompletedViewModel>
) {

  private lateinit var binding: MarkTopicsCompletedFragmentBinding
  private lateinit var linearLayoutManager: LinearLayoutManager
  private lateinit var bindingAdapter: BindableAdapter<TopicSummaryViewModel>

  fun handleCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    internalProfileId: Int
  ): View? {
    binding = MarkTopicsCompletedFragmentBinding.inflate(
      inflater,
      container,
      /* attachToRoot= */ false
    )

    binding.markTopicsCompletedToolbar.setNavigationOnClickListener {
      (activity as MarkTopicsCompletedActivity).finish()
    }

    binding.apply {
      this.lifecycleOwner = fragment
      this.viewModel = getMarkTopicsCompletedViewModel()
    }

    getMarkTopicsCompletedViewModel().setInternalProfileId(internalProfileId)

    linearLayoutManager = LinearLayoutManager(activity.applicationContext)

    bindingAdapter = createRecyclerViewAdapter()
    binding.markTopicsCompletedTopicSummaryRecyclerView.apply {
      layoutManager = linearLayoutManager
      adapter = bindingAdapter
    }

    return binding.root
  }

  private fun createRecyclerViewAdapter(): BindableAdapter<TopicSummaryViewModel> {
    return BindableAdapter.SingleTypeBuilder
      .newBuilder<TopicSummaryViewModel>()
      .registerViewDataBinderWithSameModelType(
        inflateDataBinding = MarkTopicsCompletedTopicSummaryViewBinding::inflate,
        setViewModel = MarkTopicsCompletedTopicSummaryViewBinding::setViewModel
      )
      .build()
  }

  private fun getMarkTopicsCompletedViewModel(): MarkTopicsCompletedViewModel {
    return viewModelProvider.getForFragment(fragment, MarkTopicsCompletedViewModel::class.java)
  }
}
