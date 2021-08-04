package org.oppia.android.app.topic.info

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
import org.oppia.android.databinding.TopicInfoChapterListItemBinding
import org.oppia.android.databinding.TopicInfoFragmentBinding
import org.oppia.android.databinding.TopicInfoSkillsItemBinding
import org.oppia.android.databinding.TopicInfoStorySummaryBinding
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.topic.TopicController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import org.oppia.android.util.platformparameter.MyDownloads
import org.oppia.android.util.platformparameter.PlatformParameterValue
import javax.inject.Inject
import javax.inject.Provider

/** The presenter for [TopicInfoFragment]. */
@FragmentScope
class TopicInfoFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val fragment: Fragment,
  private val viewModelProvider: ViewModelProvider<TopicInfoViewModel>,
  private val oppiaLogger: OppiaLogger,
  private val topicController: TopicController,
  @MyDownloads private val myDownloadsFeatureFlag: Provider<PlatformParameterValue<Boolean>>
) {
  private lateinit var binding: TopicInfoFragmentBinding
  private val topicInfoViewModel = getTopicInfoViewModel()
  private var internalProfileId: Int = -1
  private lateinit var topicId: String

  fun handleCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    internalProfileId: Int,
    topicId: String
  ): View? {
    this.internalProfileId = internalProfileId
    this.topicId = topicId
    binding = TopicInfoFragmentBinding.inflate(
      inflater,
      container,
      /* attachToRoot= */ false
    )
    subscribeToTopicLiveData()
    binding.let {
      it.lifecycleOwner = fragment
      it.viewModel = topicInfoViewModel
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

  private fun getTopicInfoViewModel(): TopicInfoViewModel {
    return viewModelProvider.getForFragment(fragment, TopicInfoViewModel::class.java)
  }

  private val topicLiveData: LiveData<Topic> by lazy { getTopicList() }

  private fun subscribeToTopicLiveData() {
    topicLiveData.observe(
      fragment,
      Observer<Topic> { topic ->
        topicInfoViewModel.topic.set(topic)
        topicInfoViewModel.topicDescription.set(topic.description)
        topicInfoViewModel.calculateTopicSizeWithUnit()
        controlSeeMoreTextVisibility()
        topicInfoViewModel.enableMyDownloads.set(myDownloadsFeatureFlag.get().value)
        if (myDownloadsFeatureFlag.get().value) {
          topicInfoViewModel.skillsItemList.set(extractTopicSkillList(topic.subtopicList))
          topicInfoViewModel.storyItemList.set(extractTopicStorySummaryList(topic.storyList))
        }
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
    if (myDownloadsFeatureFlag.get().value) {
      val intent = TopicDownloadedActivity.createTopicDownloadedActivityIntent(
        activity,
        internalProfileId,
        topicId,
        topicInfoViewModel.topic.get()!!.name
      )
      activity.startActivity(intent)
      activity.finish()
    }
  }

  private val topicResultLiveData: LiveData<AsyncResult<Topic>> by lazy {
    topicController.getTopic(
      ProfileId.newBuilder().setInternalId(internalProfileId).build(),
      topicId
    ).toLiveData()
  }

  private fun getTopicList(): LiveData<Topic> {
    return Transformations.map(topicResultLiveData, ::processTopicResult)
  }

  private fun processTopicResult(topic: AsyncResult<Topic>): Topic {
    if (topic.isFailure()) {
      oppiaLogger.e("TopicInfoFragment", "Failed to retrieve topic", topic.getErrorOrNull()!!)
    }
    return topic.getOrDefault(Topic.getDefaultInstance())
  }

  private fun controlSeeMoreTextVisibility() {
    val minimumNumberOfLines = fragment.resources.getInteger(R.integer.topic_description_collapsed)
    binding.topicDescriptionTextView.post {
      if (binding.topicDescriptionTextView.lineCount > minimumNumberOfLines) {
        getTopicInfoViewModel().isDescriptionExpanded.set(false)
        getTopicInfoViewModel().isSeeMoreVisible.set(true)
      } else {
        getTopicInfoViewModel().isDescriptionExpanded.set(true)
        getTopicInfoViewModel().isSeeMoreVisible.set(false)
      }
    }
  }
}
