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
) : TopicSelector {
  private lateinit var binding: MarkTopicsCompletedFragmentBinding
  private lateinit var linearLayoutManager: LinearLayoutManager
  private lateinit var bindingAdapter: BindableAdapter<TopicSummaryViewModel>
  lateinit var selectedTopicIdList: ArrayList<String>

  fun handleCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    internalProfileId: Int,
    selectedTopicIdList: ArrayList<String>
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

    this.selectedTopicIdList = selectedTopicIdList

    getMarkTopicsCompletedViewModel().setInternalProfileId(internalProfileId)

    linearLayoutManager = LinearLayoutManager(activity.applicationContext)

    bindingAdapter = createRecyclerViewAdapter()
    binding.markTopicsCompletedTopicSummaryRecyclerView.apply {
      layoutManager = linearLayoutManager
      adapter = bindingAdapter
    }

    binding.markTopicsCompletedAllCheckBoxContainer.setOnClickListener {
      if (binding.isAllChecked == null || binding.isAllChecked == false)
        binding.isAllChecked = true
    }

    binding.markTopicsCompletedAllCheckBox.setOnCheckedChangeListener { _, isChecked ->
      if (isChecked) {
        getMarkTopicsCompletedViewModel().availableTopicIdList.forEach { topicId ->
          topicSelected(topicId)
        }
      }
      bindingAdapter.notifyDataSetChanged()
    }

    return binding.root
  }

  private fun createRecyclerViewAdapter(): BindableAdapter<TopicSummaryViewModel> {
    return BindableAdapter.SingleTypeBuilder
      .newBuilder<TopicSummaryViewModel>()
      .registerViewDataBinderWithSameModelType(
        inflateDataBinding = MarkTopicsCompletedTopicSummaryViewBinding::inflate,
        setViewModel = this::bindTopicSummaryView
      )
      .build()
  }

  private fun bindTopicSummaryView(
    binding: MarkTopicsCompletedTopicSummaryViewBinding,
    model: TopicSummaryViewModel
  ) {
    binding.viewModel = model
    if (model.isCompleted) {
      binding.isTopicChecked = true
      binding.markTopicsCompletedTopicCheckBox.isEnabled = false
    } else {
      binding.isTopicChecked = selectedTopicIdList.contains(model.topic.topicId)
      binding.markTopicsCompletedTopicCheckBox.setOnCheckedChangeListener { _, isChecked ->
        if (isChecked) {
          topicSelected(model.topic.topicId)
        } else {
          topicUnselected(model.topic.topicId)
        }
      }
    }
  }

  private fun getMarkTopicsCompletedViewModel(): MarkTopicsCompletedViewModel {
    return viewModelProvider.getForFragment(fragment, MarkTopicsCompletedViewModel::class.java)
  }

  override fun topicSelected(topicId: String) {
    if (!selectedTopicIdList.contains(topicId)) {
      selectedTopicIdList.add(topicId)
    }

    if (selectedTopicIdList.size == getMarkTopicsCompletedViewModel().availableTopicIdList.size) {
      binding.isAllChecked = true
    }
  }

  override fun topicUnselected(topicId: String) {
    if (selectedTopicIdList.contains(topicId)) {
      selectedTopicIdList.remove(topicId)
    }

    if (selectedTopicIdList.size != getMarkTopicsCompletedViewModel().availableTopicIdList.size) {
      binding.isAllChecked = false
    }
  }
}
