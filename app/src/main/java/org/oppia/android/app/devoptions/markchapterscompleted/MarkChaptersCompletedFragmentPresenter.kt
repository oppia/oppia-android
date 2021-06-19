package org.oppia.android.app.devoptions.markchapterscompleted

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.recyclerview.BindableAdapter
import org.oppia.android.app.viewmodel.ViewModelProvider
import org.oppia.android.databinding.MarkChaptersCompletedChapterSummaryViewBinding
import org.oppia.android.databinding.MarkChaptersCompletedFragmentBinding
import org.oppia.android.databinding.MarkChaptersCompletedStorySummaryViewBinding
import javax.inject.Inject

/** The presenter for [MarkChaptersCompletedFragment]. */
@FragmentScope
class MarkChaptersCompletedFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val fragment: Fragment,
  private val viewModelProvider: ViewModelProvider<MarkChaptersCompletedViewModel>
) {

  private lateinit var binding: MarkChaptersCompletedFragmentBinding
  private lateinit var linearLayoutManager: LinearLayoutManager
  private lateinit var bindingAdapter: BindableAdapter<StorySummaryViewModel>

  fun handleCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    internalProfileId: Int
  ): View? {
    binding = MarkChaptersCompletedFragmentBinding.inflate(
      inflater,
      container,
      /* attachToRoot= */ false
    )

    binding.markChaptersCompletedToolbar.setNavigationOnClickListener {
      (activity as MarkChaptersCompletedActivity).finish()
    }

    binding.apply {
      this.lifecycleOwner = fragment
      this.viewModel = getMarkChaptersCompletedViewModel()
    }

    getMarkChaptersCompletedViewModel().setInternalProfileId(internalProfileId)

    linearLayoutManager = LinearLayoutManager(activity.applicationContext)

    bindingAdapter = createRecyclerViewAdapter()
    binding.markChaptersCompletedStorySummaryRecyclerView.apply {
      layoutManager = linearLayoutManager
      adapter = bindingAdapter
    }

    return binding.root
  }

  private fun createRecyclerViewAdapter(): BindableAdapter<StorySummaryViewModel> {
    return BindableAdapter.SingleTypeBuilder
      .newBuilder<StorySummaryViewModel>()
      .registerViewDataBinderWithSameModelType(
        inflateDataBinding = MarkChaptersCompletedStorySummaryViewBinding::inflate,
        setViewModel = this::bindMarkChaptersCompletedStorySummary
      )
      .build()
  }

  private fun bindMarkChaptersCompletedStorySummary(
    binding: MarkChaptersCompletedStorySummaryViewBinding,
    storySummaryViewModel: StorySummaryViewModel
  ) {
    binding.viewModel = storySummaryViewModel
    binding.markChaptersCompletedChapterSummaryRecyclerView.adapter =
      createChapterRecyclerViewAdapter()
  }

  private fun createChapterRecyclerViewAdapter(): BindableAdapter<ChapterSummaryViewModel> {
    return BindableAdapter.SingleTypeBuilder
      .newBuilder<ChapterSummaryViewModel>()
      .registerViewDataBinderWithSameModelType(
        inflateDataBinding = MarkChaptersCompletedChapterSummaryViewBinding::inflate,
        setViewModel = MarkChaptersCompletedChapterSummaryViewBinding::setViewModel
      )
      .build()
  }

  private fun getMarkChaptersCompletedViewModel(): MarkChaptersCompletedViewModel {
    return viewModelProvider.getForFragment(fragment, MarkChaptersCompletedViewModel::class.java)
  }
}
