package org.oppia.android.app.topic.revisioncard

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import org.oppia.android.app.model.EphemeralRevisionCard
import org.oppia.android.app.model.EphemeralSubtopic
import org.oppia.android.app.model.EphemeralTopic
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.topic.RouteToRevisionCardListener
import org.oppia.android.app.viewmodel.ObservableViewModel
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.topic.TopicController
import org.oppia.android.domain.translation.TranslationController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import org.oppia.android.util.parser.html.TopicHtmlParserEntityType
import javax.inject.Inject

/** [ObservableViewModel] for revision card, providing rich text and worked examples */
class RevisionCardViewModel private constructor(
  activity: AppCompatActivity,
  private val topicController: TopicController,
  private val oppiaLogger: OppiaLogger,
  val entityType: String,
  private val translationController: TranslationController,
  val topicId: String,
  val subtopicId: Int,
  val profileId: ProfileId,
  val subtopicListSize: Int
) : ObservableViewModel() {

  private val routeToReviewListener = activity as RouteToRevisionCardListener

  fun onPreviousCardClicked() {
    routeToReviewListener.routeToRevisionCard(
      profileId.internalId,
      topicId,
      subtopicId - 1,
      subtopicListSize
    )
  }

  fun onNextCardClicked() {
    routeToReviewListener.routeToRevisionCard(
      profileId.internalId,
      topicId,
      subtopicId + 1,
      subtopicListSize
    )
  }

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

  fun computeTitleText(subtopic: EphemeralSubtopic?): String {
    return subtopic?.let {
      translationController.extractString(
        subtopic.subtopic.title,
        subtopic.writtenTranslationContext
      )
    } ?: ""
  }

  private fun processPreviousSubtopicData(
    topicLiveData: AsyncResult<EphemeralTopic>
  ): EphemeralSubtopic {
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

  class Factory @Inject constructor(
    private val activity: AppCompatActivity,
    private val topicController: TopicController,
    private val oppiaLogger: OppiaLogger,
    @TopicHtmlParserEntityType private val entityType: String,
    private val translationController: TranslationController
  ) {
    fun create(
      topicId: String,
      subtopicId: Int,
      profileId: ProfileId,
      subtopicListSize: Int
    ): RevisionCardViewModel {
      return RevisionCardViewModel(
        activity,
        topicController,
        oppiaLogger,
        entityType,
        translationController,
        topicId,
        subtopicId,
        profileId,
        subtopicListSize
      )
    }
  }
}
