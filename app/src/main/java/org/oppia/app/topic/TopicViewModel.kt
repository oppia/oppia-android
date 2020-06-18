package org.oppia.app.topic

import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.model.ProfileId
import org.oppia.app.model.Topic
import org.oppia.domain.topic.TopicController
import org.oppia.util.data.AsyncResult
import org.oppia.util.logging.Logger
import javax.inject.Inject

/** The ViewModel for [TopicFragment]. */
@FragmentScope
class TopicViewModel @Inject constructor(
  private val topicController: TopicController,
  private val logger: Logger
) : ViewModel() {
  private var internalProfileId: Int = -1
  private lateinit var topicId: String

  private val topicResultLiveData: LiveData<AsyncResult<Topic>> by lazy {
    topicController.getTopic(
      ProfileId.newBuilder().setInternalId(internalProfileId).build(),
      topicId
    )
  }

  private val topicLiveData: LiveData<Topic> by lazy {
    Transformations.map(topicResultLiveData, ::processTopicResult)
  }

  val topicNameLiveData: LiveData<String> by lazy {
    Transformations.map(topicLiveData, Topic::getName)
  }

  fun setInternalProfileId(internalProfileId: Int) {
    this.internalProfileId = internalProfileId
  }

  fun setTopicId(topicId: String) {
    this.topicId = topicId
  }

  private fun processTopicResult(topicResult: AsyncResult<Topic>): Topic {
    if (topicResult.isFailure()) {
      logger.e(
        "TopicFragment",
        "Failed to retrieve Topic: ",
        topicResult.getErrorOrNull()!!
      )
    }

    return topicResult.getOrDefault(Topic.getDefaultInstance())
  }
}
