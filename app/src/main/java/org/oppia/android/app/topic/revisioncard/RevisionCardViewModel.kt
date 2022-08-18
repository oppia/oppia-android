package org.oppia.android.app.topic.revisioncard

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.EphemeralRevisionCard
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.Subtopic
import org.oppia.android.app.model.Topic
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

  private val returnToTopicClickListener: ReturnToTopicClickListener =
    activity as ReturnToTopicClickListener

  val revisionCardLiveData: LiveData<EphemeralRevisionCard> by lazy {
    processRevisionCardLiveData()
  }

  val topicLiveData: LiveData<AsyncResult<Topic>> by lazy {
    getTopicResultLiveData()
  }

  private fun getTopicResultLiveData(): LiveData<AsyncResult<Topic>> {
    return topicController.getTopic(profileId, topicId).toLiveData()
  }

  val nextSubtopicLiveData: LiveData<Subtopic> by lazy {
    Transformations.map(topicLiveData, ::processNextSubtopicData)
  }

  val previousSubtopicLiveData: LiveData<Subtopic> by lazy {
    Transformations.map(topicLiveData, ::processPreviousSubtopicData)
  }

  private fun processPreviousSubtopicData(topicLiveData: AsyncResult<Topic>): Subtopic? {
    return if (subtopicId == 0) Subtopic.getDefaultInstance()
    else {
      when (topicLiveData) {
        is AsyncResult.Success -> {
          val topic = topicLiveData.value
          topic.subtopicList.find {
            it.subtopicId == subtopicId - 1
          } ?: Subtopic.getDefaultInstance()
        }
        else -> Subtopic.getDefaultInstance()
      }
    }
  }

  private fun processNextSubtopicData(topicLiveData: AsyncResult<Topic>): Subtopic {
    return when (topicLiveData) {
      is AsyncResult.Success -> {
        val topic = topicLiveData.value
        topic.subtopicList.find {
          it.subtopicId == subtopicId + 1
        } ?: Subtopic.getDefaultInstance()
      }
      else -> Subtopic.getDefaultInstance()
    }
  }

  fun clickReturnToTopic(@Suppress("UNUSED_PARAMETER") v: View) {
    returnToTopicClickListener.onReturnToTopicClicked()
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
