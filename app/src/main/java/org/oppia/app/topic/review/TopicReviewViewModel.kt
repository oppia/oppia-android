package org.oppia.app.topic.review

import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.model.ProfileId
import org.oppia.app.model.Topic
import org.oppia.app.topic.review.reviewitemviewmodel.TopicReviewItemViewModel
import org.oppia.domain.topic.TopicController
import org.oppia.util.data.AsyncResult
import org.oppia.util.logging.Logger
import javax.inject.Inject

/** [ViewModel] for [TopicReviewFragment]. */
@FragmentScope
class TopicReviewViewModel @Inject constructor(
  private val topicController: TopicController,
  private val logger: Logger,
  fragment: Fragment
) : ViewModel() {
  private lateinit var profileId: ProfileId
  private lateinit var topicId: String
  private val subtopicList: MutableList<TopicReviewItemViewModel> = ArrayList()
  private val reviewSubtopicSelector: ReviewSubtopicSelector = fragment as ReviewSubtopicSelector

  private val topicResultLiveData: LiveData<AsyncResult<Topic>> by lazy {
    topicController.getTopic(profileId, topicId)
  }

  private val topicLiveData: LiveData<Topic> by lazy { getTopicList() }

  val subtopicLiveData: LiveData<List<TopicReviewItemViewModel>> by lazy {
    Transformations.map(topicLiveData, ::processTopic)
  }

  private fun processTopic(topic: Topic): List<TopicReviewItemViewModel> {
    subtopicList.addAll(topic.subtopicList.map {
      TopicReviewItemViewModel(it, reviewSubtopicSelector)
    })
    return subtopicList
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

  fun setInternalProfileId(internalProfileId: Int) {
    this.profileId = ProfileId.newBuilder().setInternalId(internalProfileId).build()
  }
}
