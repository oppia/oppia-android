package org.oppia.app.topic.review.reviewitemviewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.model.Subtopic
import org.oppia.app.model.Topic
import org.oppia.app.topic.review.TopicReviewViewModel
import org.oppia.domain.topic.TopicController
import org.oppia.util.data.AsyncResult
import org.oppia.util.logging.Logger
import javax.inject.Inject

/** [ViewModel] for [TopicReviewFragment] */
@FragmentScope
class TopicReviewCardViewModel @Inject constructor(
  private val topicController: TopicController,
  private val logger: Logger
  ): ViewModel() {
  private lateinit var topicId: String

  private val topicResultLiveData: LiveData<AsyncResult<Topic>> by lazy {
    topicController.getTopic(topicId)
  }

  val topicLiveData: LiveData<Topic> by lazy { getTopicList() }

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