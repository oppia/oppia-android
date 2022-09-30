package org.oppia.android.app.topic.revisioncard

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.EphemeralRevisionCard
import org.oppia.android.app.model.EphemeralSubtopic
import org.oppia.android.app.model.EphemeralTopic
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.viewmodel.ObservableViewModel
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.topic.TopicController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import javax.inject.Inject

/** [ObservableViewModel] for revision card, providing rich text and worked examples */
@FragmentScope
class RevisionCardViewModel @Inject constructor(
  activity: AppCompatActivity,
  private val topicController: TopicController,
  private val oppiaLogger: OppiaLogger
) : ObservableViewModel() {
  lateinit var topicId: String
  private var subtopicId: Int = 0
  private lateinit var profileId: ProfileId

  val revisionCardLiveData: LiveData<EphemeralRevisionCard> by lazy {
    processRevisionCardLiveData()
  }

  private val topicLiveData: LiveData<AsyncResult<EphemeralTopic>> by lazy {
    getTopicResultLiveData()
  }

  private fun getTopicResultLiveData(): LiveData<AsyncResult<EphemeralTopic>> {
    return topicController.getTopic(profileId, topicId).toLiveData()
  }

  val nextSubtopicLiveData: LiveData<EphemeralSubtopic> by lazy {
    Transformations.map(topicLiveData, ::processNextSubtopicData)
  }

  val previousSubtopicLiveData: LiveData<EphemeralSubtopic> by lazy {
    Transformations.map(topicLiveData, ::processPreviousSubtopicData)
  }

  private fun processPreviousSubtopicData(
    topicLiveData: AsyncResult<EphemeralTopic>
  ): EphemeralSubtopic? {
    return when (topicLiveData) {
      is AsyncResult.Success -> {
        val topic = topicLiveData.value
        topic.subtopicsList.find {
          it.subtopic.subtopicId == subtopicId - 1
        } ?: EphemeralSubtopic.getDefaultInstance()
      }
      else -> EphemeralSubtopic.getDefaultInstance()
    }
  }

  private fun processNextSubtopicData(
    topicLiveData: AsyncResult<EphemeralTopic>
  ): EphemeralSubtopic {
    return when (topicLiveData) {
      is AsyncResult.Success -> {
        val topic = topicLiveData.value
        topic.subtopicsList.find {
          it.subtopic.subtopicId == subtopicId + 1
        } ?: EphemeralSubtopic.getDefaultInstance()
      }
      else -> EphemeralSubtopic.getDefaultInstance()
    }
  }

  /** Initializes this view model with necessary identifiers. */
  fun initialize(topicId: String, subtopicId: Int, profileId: ProfileId) {
    this.topicId = topicId
    this.subtopicId = subtopicId
    this.profileId = profileId
  }

  private val revisionCardResultLiveData: LiveData<AsyncResult<EphemeralRevisionCard>> by lazy {
    topicController.getRevisionCard(profileId, topicId, subtopicId).toLiveData()
  }

  private fun processRevisionCardLiveData(): LiveData<EphemeralRevisionCard> {
    return Transformations.map(revisionCardResultLiveData, ::processRevisionCard)
  }

  private fun processRevisionCard(
    revisionCardResult: AsyncResult<EphemeralRevisionCard>
  ): EphemeralRevisionCard {
    return when (revisionCardResult) {
      is AsyncResult.Failure -> {
        oppiaLogger.e(
          "RevisionCardFragment", "Failed to retrieve Revision Card", revisionCardResult.error
        )
        EphemeralRevisionCard.getDefaultInstance()
      }
      is AsyncResult.Pending -> EphemeralRevisionCard.getDefaultInstance()
      is AsyncResult.Success -> revisionCardResult.value
    }
  }
}
