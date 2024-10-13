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
import org.oppia.android.databinding.MarkTopicsCompletedFragmentBinding
import org.oppia.android.databinding.MarkTopicsCompletedTopicViewBinding
import org.oppia.android.domain.devoptions.ModifyLessonProgressController
import javax.inject.Inject

/** The presenter for [MarkTopicsCompletedFragment]. */
@FragmentScope
class MarkTopicsCompletedFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val fragment: Fragment,
  private val viewModel: MarkTopicsCompletedViewModel,
  private val modifyLessonProgressController: ModifyLessonProgressController,
  private val singleTypeBuilderFactory: BindableAdapter.SingleTypeBuilder.Factory
) : TopicSelector {
  private lateinit var binding: MarkTopicsCompletedFragmentBinding
  private lateinit var linearLayoutManager: LinearLayoutManager
  private lateinit var bindingAdapter: BindableAdapter<TopicViewModel>
  lateinit var selectedTopicIdList: ArrayList<String>
  private lateinit var profileId: ProfileId

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
      this.viewModel = this@MarkTopicsCompletedFragmentPresenter.viewModel
    }

    this.selectedTopicIdList = selectedTopicIdList

    this.profileId = ProfileId.newBuilder().setLoggedInInternalProfileId(internalProfileId).build()
    viewModel.setProfileId(profileId)

    linearLayoutManager = LinearLayoutManager(activity.applicationContext)

    bindingAdapter = createRecyclerViewAdapter()
    binding.markTopicsCompletedRecyclerView.apply {
      layoutManager = linearLayoutManager
      adapter = bindingAdapter
    }

    binding.markTopicsCompletedAllCheckBoxContainer.setOnClickListener {
      binding.isAllChecked = !(binding.isAllChecked ?: false)
    }

    binding.markTopicsCompletedAllCheckBox.setOnCheckedChangeListener { _, isChecked ->
      if (isChecked) {
        viewModel.getTopicList().forEach { viewModel ->
          if (!viewModel.isCompleted) topicSelected(viewModel.topic.topicId)
        }
      } else {
        viewModel.getTopicList().forEach { viewModel ->
          if (!viewModel.isCompleted) topicUnselected(viewModel.topic.topicId)
        }
      }
      bindingAdapter.notifyDataSetChanged()
    }

    binding.markTopicsCompletedMarkCompletedTextView.setOnClickListener {
      modifyLessonProgressController.markMultipleTopicsCompleted(
        profileId,
        selectedTopicIdList
      )
      activity.finish()
    }

    return binding.root
  }

  private fun createRecyclerViewAdapter(): BindableAdapter<TopicViewModel> {
    return singleTypeBuilderFactory.create<TopicViewModel>()
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
    if (viewModel.getTopicList().count { !it.isCompleted } == 0) {
      this.binding.isAllChecked = true
    }
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

  override fun topicSelected(topicId: String) {
    if (!selectedTopicIdList.contains(topicId)) {
      selectedTopicIdList.add(topicId)
    }

    if (selectedTopicIdList.size == viewModel.getTopicList().count { !it.isCompleted }) {
      binding.isAllChecked = true
    }
  }

  override fun topicUnselected(topicId: String) {
    if (selectedTopicIdList.contains(topicId)) {
      selectedTopicIdList.remove(topicId)
    }

    if (selectedTopicIdList.size != viewModel.getTopicList().count { !it.isCompleted }) {
      binding.isAllChecked = false
    }
  }
}
