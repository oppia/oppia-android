package org.oppia.android.app.topic.preview

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
import org.oppia.android.R
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.StorySummary
import org.oppia.android.app.model.Subtopic
import org.oppia.android.app.model.Topic
import org.oppia.android.app.recyclerview.BindableAdapter
import org.oppia.android.app.topic.info.TopicInfoChapterItemViewModel
import org.oppia.android.app.topic.info.TopicInfoSkillItemViewModel
import org.oppia.android.app.topic.info.TopicInfoStoryItemViewModel
import org.oppia.android.app.topicdownloaded.TopicDownloadedActivity
import org.oppia.android.app.viewmodel.ViewModelProvider
import org.oppia.android.databinding.TopicInfoChapterListItemBinding
import org.oppia.android.databinding.TopicInfoSkillsItemBinding
import org.oppia.android.databinding.TopicInfoStorySummaryBinding
import org.oppia.android.databinding.TopicPreviewFragmentBinding
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.topic.TopicController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import javax.inject.Inject

/** The presenter for [TopicPreviewFragment]. */
@FragmentScope
class TopicPreviewFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val fragment: Fragment,
  private val viewModelProvider: ViewModelProvider<TopicPreviewViewModel>,
  private val topicController: TopicController,
  private val oppiaLogger: OppiaLogger
) {

  private var internalProfileId: Int = -1
  private lateinit var topicId: String

  private lateinit var binding: TopicPreviewFragmentBinding
  private val topicPreviewViewModel = getTopicPreviewViewModel()

  /** Bind TopicDownloadedFragmentBinding with the TopicDownloadedFragment */
  fun handleCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    internalProfileId: Int,
    topicId: String
  ): View? {
    this.internalProfileId = internalProfileId
    this.topicId = topicId
    binding = TopicPreviewFragmentBinding.inflate(
      inflater,
      container,
      /* attachToRoot= */ false
    )
    subscribeToTopicLiveData()
    binding.let {
      it.lifecycleOwner = fragment
      it.viewModel = topicPreviewViewModel
    }
    binding.skillsRecyclerView.apply {
      adapter = createSkillRecyclerViewAdapter()
    }
    binding.topicInfoStorySummaryRecyclerView.apply {
      adapter = createStoryRecyclerViewAdapter()
    }
    return binding.root
  }

  private fun createStoryRecyclerViewAdapter(): BindableAdapter<TopicInfoStoryItemViewModel> {
    return BindableAdapter.SingleTypeBuilder
      .newBuilder<TopicInfoStoryItemViewModel>()
      .registerViewDataBinderWithSameModelType(
        inflateDataBinding = TopicInfoStorySummaryBinding::inflate,
        setViewModel = this::bindStorySummary
      ).build()
  }

  private fun bindStorySummary(
    binding: TopicInfoStorySummaryBinding,
    model: TopicInfoStoryItemViewModel
  ) {
    binding.viewModel = model

    var isChapterListVisible = false
    binding.isListExpanded = isChapterListVisible

    binding.expandListIcon.setOnClickListener {
      isChapterListVisible = !isChapterListVisible
      binding.isListExpanded = isChapterListVisible
    }
    binding.topicInfoChapterRecyclerView.adapter = createChapterRecyclerViewAdapter()
  }

  private fun createChapterRecyclerViewAdapter(): BindableAdapter<TopicInfoChapterItemViewModel> {
    return BindableAdapter.SingleTypeBuilder
      .newBuilder<TopicInfoChapterItemViewModel>()
      .registerViewDataBinderWithSameModelType(
        inflateDataBinding = TopicInfoChapterListItemBinding::inflate,
        setViewModel = TopicInfoChapterListItemBinding::setViewModel
      ).build()
  }

  private fun createSkillRecyclerViewAdapter(): BindableAdapter<TopicInfoSkillItemViewModel> {
    return BindableAdapter.SingleTypeBuilder
      .newBuilder<TopicInfoSkillItemViewModel>()
      .registerViewDataBinderWithSameModelType(
        inflateDataBinding = TopicInfoSkillsItemBinding::inflate,
        setViewModel = TopicInfoSkillsItemBinding::setViewModel
      ).build()
  }

  private val topicLiveData: LiveData<Topic> by lazy { getTopicList() }

  private fun getTopicList(): LiveData<Topic> {
    return Transformations.map(topicResultLiveData, ::processTopicResult)
  }

  private val topicResultLiveData: LiveData<AsyncResult<Topic>> by lazy {
    topicController.getTopic(
      ProfileId.newBuilder().setInternalId(internalProfileId).build(),
      topicId
    ).toLiveData()
  }

  private fun processTopicResult(topic: AsyncResult<Topic>): Topic {
    if (topic.isFailure()) {
      oppiaLogger.e("TopicInfoFragment", "Failed to retrieve topic", topic.getErrorOrNull()!!)
    }
    return topic.getOrDefault(Topic.getDefaultInstance())
  }

  private fun subscribeToTopicLiveData() {
    topicLiveData.observe(
      fragment,
      Observer<Topic> { topic ->
        topicPreviewViewModel.topic.set(topic)
        topicPreviewViewModel.topicDescription.set(topic.description)
        topicPreviewViewModel.calculateTopicSizeWithUnit()
        controlSeeMoreTextVisibility()
        topicPreviewViewModel.skillsItemList.set(extractTopicSkillList(topic.subtopicList))
        topicPreviewViewModel.storyItemList.set(extractTopicStorySummaryList(topic.storyList))
      }
    )
  }

  private fun extractTopicSkillList(
    subtopicList: MutableList<Subtopic>
  ): ArrayList<TopicInfoSkillItemViewModel> {
    val topicSkillsList = ArrayList<TopicInfoSkillItemViewModel>()
    topicSkillsList.addAll(
      subtopicList.map {
        TopicInfoSkillItemViewModel(it.title)
      }
    )
    return topicSkillsList
  }

  private fun extractTopicStorySummaryList(
    storySummaryList: MutableList<StorySummary>
  ): ArrayList<TopicInfoStoryItemViewModel> {
    val topicStoryList = ArrayList<TopicInfoStoryItemViewModel>()
    val topicStoryChapterList = ArrayList<TopicInfoChapterItemViewModel>()
    topicStoryList.addAll(
      storySummaryList.map { storySummary ->
        topicStoryChapterList.addAll(
          storySummary.chapterList.mapIndexed { index, chapterSummary ->
            TopicInfoChapterItemViewModel(index, chapterSummary.name)
          }
        )
        val newTopicStoryChapterList = ArrayList<TopicInfoChapterItemViewModel>()
        newTopicStoryChapterList.addAll(topicStoryChapterList)
        topicStoryChapterList.clear()
        TopicInfoStoryItemViewModel(storySummary, newTopicStoryChapterList)
      }
    )
    return topicStoryList
  }

  fun showDownloadedTopic() {
    val intent = TopicDownloadedActivity.createTopicDownloadedActivityIntent(
      activity,
      internalProfileId,
      topicId,
      topicPreviewViewModel.topic.get()!!.name
    )
    activity.startActivity(intent)
    activity.finish()
  }

  private fun getTopicPreviewViewModel(): TopicPreviewViewModel {
    return viewModelProvider.getForFragment(fragment, TopicPreviewViewModel::class.java)
  }

  private fun controlSeeMoreTextVisibility() {
    val minimumNumberOfLines = fragment.resources.getInteger(R.integer.topic_description_collapsed)
    binding.topicDescriptionTextView.post {
      if (binding.topicDescriptionTextView.lineCount > minimumNumberOfLines) {
        topicPreviewViewModel.isDescriptionExpanded.set(false)
        topicPreviewViewModel.isSeeMoreVisible.set(true)
      } else {
        topicPreviewViewModel.isDescriptionExpanded.set(true)
        topicPreviewViewModel.isSeeMoreVisible.set(false)
      }
    }
  }
}
