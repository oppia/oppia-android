package org.oppia.android.app.devoptions.marktopicscompleted

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.recyclerview.BindableAdapter
import org.oppia.android.app.viewmodel.ViewModelProvider
import org.oppia.android.databinding.MarkTopicsCompletedFragmentBinding
import org.oppia.android.databinding.MarkTopicsCompletedTopicViewBinding
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
  private lateinit var bindingAdapter: BindableAdapter<TopicViewModel>
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

    getMarkTopicsCompletedViewModel().setProfileId(
      ProfileId.newBuilder().setInternalId(internalProfileId).build()
    )

    linearLayoutManager = LinearLayoutManager(activity.applicationContext)

    bindingAdapter = createRecyclerViewAdapter()
    binding.markTopicsCompletedRecyclerView.apply {
      layoutManager = linearLayoutManager
      adapter = bindingAdapter
    }

    binding.markTopicsCompletedAllCheckBoxContainer.setOnClickListener {
      if (binding.isAllChecked == null || binding.isAllChecked == false)
        binding.isAllChecked = true
    }

    binding.markTopicsCompletedAllCheckBox.setOnCheckedChangeListener { _, isChecked ->
      if (isChecked) {
        getMarkTopicsCompletedViewModel().getTopicList().forEach { topicViewModel ->
          if (!topicViewModel.isCompleted) topicSelected(topicViewModel.topic.topicId)
        }
      }
      bindingAdapter.notifyDataSetChanged()
    }

    return binding.root
  }

  private fun createRecyclerViewAdapter(): BindableAdapter<TopicViewModel> {
    return BindableAdapter.SingleTypeBuilder
      .newBuilder<TopicViewModel>()
      .registerViewDataBinderWithSameModelType(
        inflateDataBinding = MarkTopicsCompletedTopicViewBinding::inflate,
        setViewModel = this::bindTopicSummaryView
      )
      .build()
  }

  private fun bindTopicSummaryView(
    binding: MarkTopicsCompletedTopicViewBinding,
    model: TopicViewModel
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

    if (selectedTopicIdList.size ==
      getMarkTopicsCompletedViewModel().getTopicList().count { !it.isCompleted }
    ) {
      binding.isAllChecked = true
    }
  }

  override fun topicUnselected(topicId: String) {
    if (selectedTopicIdList.contains(topicId)) {
      selectedTopicIdList.remove(topicId)
    }

    if (selectedTopicIdList.size !=
      getMarkTopicsCompletedViewModel().getTopicList().count { !it.isCompleted }
    ) {
      binding.isAllChecked = false
    }
  }
}
