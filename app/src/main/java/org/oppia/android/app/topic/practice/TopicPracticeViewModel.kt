package org.oppia.app.topic.practice

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.model.ProfileId
import org.oppia.app.model.Topic
import org.oppia.app.topic.practice.practiceitemviewmodel.TopicPracticeFooterViewModel
import org.oppia.app.topic.practice.practiceitemviewmodel.TopicPracticeHeaderViewModel
import org.oppia.app.topic.practice.practiceitemviewmodel.TopicPracticeItemViewModel
import org.oppia.app.topic.practice.practiceitemviewmodel.TopicPracticeSubtopicViewModel
import org.oppia.app.viewmodel.ObservableViewModel
import org.oppia.domain.topic.TopicController
import org.oppia.util.data.AsyncResult
import org.oppia.util.data.DataProviders.Companion.toLiveData
import org.oppia.util.logging.ConsoleLogger
import javax.inject.Inject

/** [ObservableViewModel] for [TopicPracticeFragment]. */
@FragmentScope
class TopicPracticeViewModel @Inject constructor(
  private val logger: ConsoleLogger,
  private val topicController: TopicController
) : ObservableViewModel() {
  private val itemViewModelList: MutableList<TopicPracticeItemViewModel> = ArrayList()
  private lateinit var topicId: String
  private var internalProfileId: Int = -1

  private val topicResultLiveData: LiveData<AsyncResult<Topic>> by lazy {
    topicController.getTopic(
      ProfileId.newBuilder().setInternalId(internalProfileId).build(),
      topicId
    ).toLiveData()
  }

  private val topicLiveData: LiveData<Topic> by lazy { getTopicList() }

  private fun getTopicList(): LiveData<Topic> {
    return Transformations.map(topicResultLiveData, ::processTopicResult)
  }

  val topicPracticeSkillLiveData: LiveData<List<TopicPracticeItemViewModel>> by lazy {
    Transformations.map(topicLiveData, ::processTopicPracticeSkillList)
  }

  fun setTopicId(topicId: String) {
    this.topicId = topicId
  }

  fun setInternalProfileId(internalProfileId: Int) {
    this.internalProfileId = internalProfileId
  }

  private fun processTopicResult(topic: AsyncResult<Topic>): Topic {
    if (topic.isFailure()) {
      logger.e(
        "TopicPracticeFragment",
        "Failed to retrieve topic",
        topic.getErrorOrNull()!!
      )
    }
    return topic.getOrDefault(Topic.getDefaultInstance())
  }

  private fun processTopicPracticeSkillList(topic: Topic): List<TopicPracticeItemViewModel> {
    itemViewModelList.clear()
    itemViewModelList.add(TopicPracticeHeaderViewModel() as TopicPracticeItemViewModel)

    itemViewModelList.addAll(
      topic.subtopicList.map { subtopic ->
        TopicPracticeSubtopicViewModel(subtopic) as TopicPracticeItemViewModel
      }
    )

    itemViewModelList.add(TopicPracticeFooterViewModel() as TopicPracticeItemViewModel)

    return itemViewModelList
  }
}
