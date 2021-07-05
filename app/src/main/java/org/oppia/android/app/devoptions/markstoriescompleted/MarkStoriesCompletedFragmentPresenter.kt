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
) : StorySelector {
  private lateinit var binding: MarkStoriesCompletedFragmentBinding
  private lateinit var linearLayoutManager: LinearLayoutManager
  private lateinit var bindingAdapter: BindableAdapter<StorySummaryViewModel>
  lateinit var selectedStoryIdList: ArrayList<String>

  fun handleCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    internalProfileId: Int,
    selectedStoryIdList: ArrayList<String>
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

    this.selectedStoryIdList = selectedStoryIdList

    getMarkStoriesCompletedViewModel().setInternalProfileId(internalProfileId)

    linearLayoutManager = LinearLayoutManager(activity.applicationContext)

    bindingAdapter = createRecyclerViewAdapter()
    binding.markStoriesCompletedStorySummaryRecyclerView.apply {
      layoutManager = linearLayoutManager
      adapter = bindingAdapter
    }

    binding.markStoriesCompletedAllCheckBox.setOnCheckedChangeListener { _, isChecked ->
      if (isChecked) {
        getMarkStoriesCompletedViewModel().itemList.forEach { storySummaryViewModel ->
          if (!storySummaryViewModel.isCompleted)
            storySelected(storySummaryViewModel.storySummary.storyId)
        }
        binding.markStoriesCompletedAllCheckBox.isEnabled = false
      } else {
        binding.markStoriesCompletedAllCheckBox.isEnabled = true
      }
      bindingAdapter.notifyDataSetChanged()
    }

    return binding.root
  }

  private fun createRecyclerViewAdapter(): BindableAdapter<StorySummaryViewModel> {
    return BindableAdapter.SingleTypeBuilder
      .newBuilder<StorySummaryViewModel>()
      .registerViewDataBinderWithSameModelType(
        inflateDataBinding = MarkStoriesCompletedStorySummaryViewBinding::inflate,
        setViewModel = this::bindStorySummaryView
      )
      .build()
  }

  private fun bindStorySummaryView(
    binding: MarkStoriesCompletedStorySummaryViewBinding,
    model: StorySummaryViewModel
  ) {
    binding.viewModel = model
    if (model.isCompleted) {
      binding.isStoryChecked = true
      binding.markStoriesCompletedStoryCheckBox.isEnabled = false
    } else {
      binding.isStoryChecked = selectedStoryIdList.contains(model.storySummary.storyId)
      binding.markStoriesCompletedStoryCheckBox.setOnCheckedChangeListener { _, isChecked ->
        if (isChecked) {
          storySelected(model.storySummary.storyId)
        } else {
          storyUnselected(model.storySummary.storyId)
        }
      }
    }
  }

  private fun getMarkStoriesCompletedViewModel(): MarkStoriesCompletedViewModel {
    return viewModelProvider.getForFragment(fragment, MarkStoriesCompletedViewModel::class.java)
  }

  override fun storySelected(storyId: String) {
    if (!selectedStoryIdList.contains(storyId)) {
      selectedStoryIdList.add(storyId)
    }

    if (selectedStoryIdList.size ==
      getMarkStoriesCompletedViewModel().itemList.count { !it.isCompleted }
    ) {
      binding.isAllChecked = true
    }
  }

  override fun storyUnselected(storyId: String) {
    if (selectedStoryIdList.contains(storyId)) {
      selectedStoryIdList.remove(storyId)
    }

    if (selectedStoryIdList.size !=
      getMarkStoriesCompletedViewModel().itemList.count { !it.isCompleted }
    ) {
      binding.isAllChecked = false
    }
  }
}
