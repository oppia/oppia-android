package org.oppia.android.app.topic

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.Topic
import org.oppia.android.app.viewmodel.ObservableViewModel
import org.oppia.android.domain.topic.TopicController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.logging.ConsoleLogger
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.Topic
import org.oppia.android.app.viewmodel.ObservableViewModel
import org.oppia.android.domain.topic.TopicController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import org.oppia.android.util.logging.ConsoleLogger
>>>>>>> develop:app/src/main/java/org.oppia.android.app.topic/TopicViewModel.kt
import javax.inject.Inject

/** The ObservableViewModel for [TopicFragment]. */
@FragmentScope
class TopicViewModel @Inject constructor(
  private val topicController: TopicController,
  private val logger: ConsoleLogger
) : ObservableViewModel() {
  private var internalProfileId: Int = -1
  private lateinit var topicId: String

  private val topicResultLiveData: LiveData<AsyncResult<Topic>> by lazy {
    topicController.getTopic(
      ProfileId.newBuilder().setInternalId(internalProfileId).build(),
      topicId
    ).toLiveData()
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
