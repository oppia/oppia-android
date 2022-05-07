package org.oppia.android.app.topic

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import org.oppia.android.R
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.Topic
import org.oppia.android.app.model.TopicSpotlightCheckpoint
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.app.viewmodel.ObservableViewModel
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.spotlight.SpotlightStateController
import org.oppia.android.domain.topic.TopicController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import javax.inject.Inject

/** The ObservableViewModel for [TopicFragment]. */
@FragmentScope
class TopicViewModel @Inject constructor(
  private val topicController: TopicController,
  private val oppiaLogger: OppiaLogger,
  private val resourceHandler: AppLanguageResourceHandler,
  private val spotlightStateController: SpotlightStateController
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

  private val topicNameLiveData by lazy { Transformations.map(topicLiveData, Topic::getName) }

  val topicToolbarTitleLiveData: LiveData<String> by lazy {
    Transformations.map(topicNameLiveData) { name ->
      resourceHandler.getStringInLocaleWithWrapping(R.string.topic_name, name)
    }
  }

  fun setInternalProfileId(internalProfileId: Int) {
    this.internalProfileId = internalProfileId
  }

  fun setTopicId(topicId: String) {
    this.topicId = topicId
  }

  private fun processTopicResult(topicResult: AsyncResult<Topic>): Topic {
    return when (topicResult) {
      is AsyncResult.Failure -> {
        oppiaLogger.e("TopicFragment", "Failed to retrieve Topic: ", topicResult.error)
        Topic.getDefaultInstance()
      }
      is AsyncResult.Pending -> Topic.getDefaultInstance()
      is AsyncResult.Success -> topicResult.value
    }
  }

  fun recordSpotlightCheckpoint(
    lastScreenViewed: TopicSpotlightCheckpoint.LastScreenViewed
  ) {
    val checkpoint = TopicSpotlightCheckpoint.newBuilder()
      .setLastScreenViewed(lastScreenViewed)
      .setSpotlightState(spotlightStateController.computeSpotlightState(lastScreenViewed))
      .build()

    val profileId = ProfileId.newBuilder()
      .setInternalId(internalProfileId)
      .build()
    spotlightStateController.recordSpotlightCheckpoint(profileId, checkpoint)
  }
}
