package org.oppia.android.app.preview

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
import org.oppia.android.app.topicdownloaded.TopicDownloadedActivity
import org.oppia.android.app.viewmodel.ViewModelProvider
import org.oppia.android.databinding.TopicPreviewChapterListItemBinding
import org.oppia.android.databinding.TopicPreviewFragmentBinding
import org.oppia.android.databinding.TopicPreviewSkillsItemBinding
import org.oppia.android.databinding.TopicPreviewStorySummaryBinding
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
    binding.storySummaryRecyclerView.apply {
      adapter = createStoryRecyclerViewAdapter()
    }
    return binding.root
  }

  private fun createStoryRecyclerViewAdapter(): BindableAdapter<TopicPreviewStoryItemViewModel> {
    return BindableAdapter.SingleTypeBuilder
      .newBuilder<TopicPreviewStoryItemViewModel>()
      .registerViewDataBinderWithSameModelType(
        inflateDataBinding = TopicPreviewStorySummaryBinding::inflate,
        setViewModel = this::bindStorySummary
      ).build()
  }

  private fun bindStorySummary(
    binding: TopicPreviewStorySummaryBinding,
    model: TopicPreviewStoryItemViewModel
  ) {
    binding.viewModel = model

    var isChapterListVisible = false
    binding.isListExpanded = isChapterListVisible

    binding.expandListIcon.setOnClickListener {
      isChapterListVisible = !isChapterListVisible
      binding.isListExpanded = isChapterListVisible
    }
    binding.topicPreviewChapterRecyclerView.adapter = createRecyclerViewAdapter()
  }

  private fun createRecyclerViewAdapter(): BindableAdapter<TopicPreviewChapterItemViewModel> {
    return BindableAdapter.SingleTypeBuilder
      .newBuilder<TopicPreviewChapterItemViewModel>()
      .registerViewDataBinderWithSameModelType(
        inflateDataBinding = TopicPreviewChapterListItemBinding::inflate,
        setViewModel = TopicPreviewChapterListItemBinding::setViewModel
      ).build()
  }

  private fun createSkillRecyclerViewAdapter(): BindableAdapter<TopicPreviewSkillItemViewModel> {
    return BindableAdapter.SingleTypeBuilder
      .newBuilder<TopicPreviewSkillItemViewModel>()
      .registerViewDataBinderWithSameModelType(
        inflateDataBinding = TopicPreviewSkillsItemBinding::inflate,
        setViewModel = TopicPreviewSkillsItemBinding::setViewModel
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
      oppiaLogger.e("TopicPreviewFragment", "Failed to retrieve topic", topic.getErrorOrNull()!!)
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
  ): ArrayList<TopicPreviewSkillItemViewModel> {
    val topicSkillsList = ArrayList<TopicPreviewSkillItemViewModel>()
    topicSkillsList.addAll(
      subtopicList.map {
        TopicPreviewSkillItemViewModel(it.title)
      }
    )
    return topicSkillsList
  }

  private fun extractTopicStorySummaryList(
    storySummaryList: MutableList<StorySummary>
  ): ArrayList<TopicPreviewStoryItemViewModel> {
    val topicStoryList = ArrayList<TopicPreviewStoryItemViewModel>()
    val topicStoryChapterList = ArrayList<TopicPreviewChapterItemViewModel>()
    topicStoryList.addAll(
      storySummaryList.map { storySummary ->
        topicStoryChapterList.addAll(
          storySummary.chapterList.mapIndexed { index, chapterSummary ->
            TopicPreviewChapterItemViewModel(
              index,
              chapterSummary.name
            )
          }
        )
        val newTopicStoryChapterList = ArrayList<TopicPreviewChapterItemViewModel>()
        newTopicStoryChapterList.addAll(topicStoryChapterList)
        topicStoryChapterList.clear()
        TopicPreviewStoryItemViewModel(storySummary, newTopicStoryChapterList)
      }
    )
    return topicStoryList
  }

  /** Starts TopicDownloadedActivity. */
  fun showDownloadedTopic() {
    val intent = TopicDownloadedActivity.createTopicDownloadedActivityIntent(
      activity,
      internalProfileId,
      topicId
    )
    activity.startActivity(intent)
    activity.finish()
  }

  private fun getTopicPreviewViewModel(): TopicPreviewViewModel {
    return viewModelProvider.getForFragment(fragment, TopicPreviewViewModel::class.java)
  }

  private fun controlSeeMoreTextVisibility() {
    val minimumNumberOfLines = fragment.resources.getInteger(R.integer.topic_description_collapsed)
    binding.topicPreviewDescriptionTextView.post {
      if (binding.topicPreviewDescriptionTextView.lineCount > minimumNumberOfLines) {
        topicPreviewViewModel.isDescriptionExpanded.set(false)
        topicPreviewViewModel.isSeeMoreVisible.set(true)
      } else {
        topicPreviewViewModel.isDescriptionExpanded.set(true)
        topicPreviewViewModel.isSeeMoreVisible.set(false)
      }
    }
  }
}
