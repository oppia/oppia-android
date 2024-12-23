package org.oppia.android.app.topic.revisioncard

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import org.oppia.android.R
import org.oppia.android.app.model.EphemeralRevisionCard
import org.oppia.android.app.model.EphemeralSubtopic
import org.oppia.android.app.model.EphemeralTopic
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.topic.RouteToRevisionCardListener
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.app.viewmodel.ObservableViewModel
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.topic.TopicController
import org.oppia.android.domain.translation.TranslationController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import org.oppia.android.util.parser.html.TopicHtmlParserEntityType
import javax.inject.Inject

/**
 * [ObservableViewModel] for revision card, providing rich text and worked examples
 *
 * @property entityType the entity type corresponding to loaded images
 * @property topicId the ID of the topic containing the subtopic being viewed
 * @property subtopicId the ID of the subtopic being viewed
 * @property profileId the ID of the user profile
 * @property subtopicListSize the number of subtopics in the parent topic. This is used to determine
 *     whether or not to show the next/previous cards.
 */
class RevisionCardViewModel private constructor(
  activity: AppCompatActivity,
  private val topicController: TopicController,
  private val oppiaLogger: OppiaLogger,
  val entityType: String,
  private val translationController: TranslationController,
  val topicId: String,
  val subtopicId: Int,
  val profileId: ProfileId,
  private val appLanguageResourceHandler: AppLanguageResourceHandler,
  val subtopicListSize: Int
) : ObservableViewModel() {

  private val routeToReviewListener = activity as RouteToRevisionCardListener

  /** Called when the previous navigation card is clicked. */
  fun onPreviousCardClicked() {
    routeToReviewListener.routeToRevisionCard(
      profileId,
      topicId,
      subtopicId - 1,
      subtopicListSize
    )
  }

  /** Called when the next navigation card is clicked. */
  fun onNextCardClicked() {
    routeToReviewListener.routeToRevisionCard(
      profileId,
      topicId,
      subtopicId + 1,
      subtopicListSize
    )
  }

  /** The [LiveData] corresponding to the [EphemeralRevisionCard] that is currently being viewed. */
  val revisionCardLiveData: LiveData<EphemeralRevisionCard> by lazy {
    processRevisionCardLiveData()
  }

  private val topicLiveData: LiveData<AsyncResult<EphemeralTopic>> by lazy {
    getTopicResultLiveData()
  }

  private fun getTopicResultLiveData(): LiveData<AsyncResult<EphemeralTopic>> {
    return topicController.getTopic(profileId, topicId).toLiveData()
  }

  /**
   * The [LiveData] that will correspond to the next revision card that may be navigated to (as a
   * [EphemeralSubtopic]), or default instance of there isn't one.
   */
  val nextSubtopicLiveData: LiveData<EphemeralSubtopic> by lazy {
    Transformations.map(topicLiveData, ::processNextSubtopicData)
  }

  /**
   * The [LiveData] that will correspond to the previous revision card that may be navigated to (as
   * a [EphemeralSubtopic]), or default instance of there isn't one.
   */
  val previousSubtopicLiveData: LiveData<EphemeralSubtopic> by lazy {
    Transformations.map(topicLiveData, ::processPreviousSubtopicData)
  }

  /** Returns the localised title of the subtopic. */
  fun computeTitleText(subtopic: EphemeralSubtopic?): String {
    return subtopic?.let {
      translationController.extractString(
        subtopic.subtopic.title,
        subtopic.writtenTranslationContext
      )
    } ?: ""
  }

  /** Returns the content description of the subtopic. */
  fun computeContentDescriptionText(subtopicLiveData: LiveData<EphemeralSubtopic>): String {
    return when (subtopicLiveData) {
      previousSubtopicLiveData -> appLanguageResourceHandler.getStringInLocaleWithWrapping(
        R.string.previous_subtopic_talkback_text,
        computeTitleText(previousSubtopicLiveData.value)
      )
      nextSubtopicLiveData -> appLanguageResourceHandler.getStringInLocaleWithWrapping(
        R.string.next_subtopic_talkback_text,
        computeTitleText(nextSubtopicLiveData.value)
      )
      else -> ""
    }
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

  /** Factory to create new [RevisionCardViewModel]s. */
  class Factory @Inject constructor(
    private val activity: AppCompatActivity,
    private val topicController: TopicController,
    private val oppiaLogger: OppiaLogger,
    @TopicHtmlParserEntityType private val entityType: String,
    private val appLanguageResourceHandler: AppLanguageResourceHandler,
    private val translationController: TranslationController
  ) {
    /** Returns a new [RevisionCardViewModel]. */
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
        appLanguageResourceHandler,
        subtopicListSize
      )
    }
  }
}
