package org.oppia.app.topic.review

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.model.Topic
import org.oppia.app.topic.review.reviewitemviewmodel.TopicReviewSubtopicViewModel
import org.oppia.domain.topic.TopicController
import org.oppia.util.data.AsyncResult
import org.oppia.util.logging.Logger
import javax.inject.Inject

/** [ViewModel] for [TopicReviewFragment] */
@FragmentScope
class TopicReviewViewModel @Inject constructor(
  private val topicController: TopicController,
  private val logger: Logger
  ): ViewModel() {
  private lateinit var topicId: String
  private val subTopicList: MutableList<TopicReviewItemViewModel> = ArrayList()

  private val topicResultLiveData: LiveData<AsyncResult<Topic>> by lazy {
    topicController.getTopic(topicId)
  }

  private val topicLiveData: LiveData<Topic> by lazy { getTopicList() }

  val subTopicLiveData: LiveData<List<TopicReviewItemViewModel>> by lazy {
    Transformations.map(topicLiveData, ::processSubTopicList)
  }

  private fun processSubTopicList(topic: Topic): List<TopicReviewItemViewModel> {
    subTopicList.addAll(topic.subtopicList.map {
      TopicReviewSubtopicViewModel(it)
    })
    return subTopicList
  }

  private fun getTopicList(): LiveData<Topic> {
    return Transformations.map(topicResultLiveData, ::processTopicResult)
  }

  private fun processTopicResult(topic: AsyncResult<Topic>): Topic {
    if (topic.isFailure()) {
      logger.e("TopicReviewFragment", "Failed to retrieve topic", topic.getErrorOrNull()!!)
    }
    return topic.getOrDefault(Topic.getDefaultInstance())
  }

  fun setTopicId(topicId: String) {
    this.topicId = topicId
  }
}