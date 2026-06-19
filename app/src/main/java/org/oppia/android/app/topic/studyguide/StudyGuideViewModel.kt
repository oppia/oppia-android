package org.oppia.android.app.topic.studyguide

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import org.oppia.android.app.model.EphemeralStudyGuide
import org.oppia.android.app.model.EphemeralSubtopic
import org.oppia.android.app.model.EphemeralTopic
import org.oppia.android.app.model.LegacyProfileId
import org.oppia.android.app.topic.RouteToStudyGuideListener
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.app.view.models.R
import org.oppia.android.app.viewmodel.ObservableViewModel
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.topic.TopicController
import org.oppia.android.domain.translation.TranslationController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import org.oppia.android.util.parser.html.TopicHtmlParserEntityType
import javax.inject.Inject

/**
 * [ObservableViewModel] for a study guide, providing its sectioned content and subtopic navigation.
 *
 * @property entityType the entity type corresponding to loaded images
 * @property topicId the ID of the topic containing the subtopic being viewed
 * @property subtopicId the ID of the subtopic being viewed
 * @property profileId the ID of the user profile
 * @property subtopicListSize the number of subtopics in the parent topic. This is used to determine
 *     whether or not to show the next/previous cards.
 */
class StudyGuideViewModel private constructor(
  activity: AppCompatActivity,
  private val topicController: TopicController,
  private val oppiaLogger: OppiaLogger,
  val entityType: String,
  private val translationController: TranslationController,
  val topicId: String,
  val subtopicId: Int,
  val profileId: LegacyProfileId,
  private val appLanguageResourceHandler: AppLanguageResourceHandler,
  val subtopicListSize: Int
) : ObservableViewModel() {

  private val routeToStudyGuideListener = activity as RouteToStudyGuideListener

  /** Called when the previous navigation card is clicked. */
  fun onPreviousCardClicked() {
    routeToStudyGuideListener.routeToStudyGuide(
      profileId,
      topicId,
      subtopicId - 1,
      subtopicListSize
    )
  }

  /** Called when the next navigation card is clicked. */
  fun onNextCardClicked() {
    routeToStudyGuideListener.routeToStudyGuide(
      profileId,
      topicId,
      subtopicId + 1,
      subtopicListSize
    )
  }

  /** The [LiveData] corresponding to the [EphemeralStudyGuide] that is currently being viewed. */
  val studyGuideLiveData: LiveData<EphemeralStudyGuide> by lazy {
    processStudyGuideLiveData()
  }

  private val topicLiveData: LiveData<AsyncResult<EphemeralTopic>> by lazy {
    getTopicResultLiveData()
  }

  private fun getTopicResultLiveData(): LiveData<AsyncResult<EphemeralTopic>> {
    return topicController.getTopic(profileId, topicId).toLiveData()
  }

  /**
   * The [LiveData] that will correspond to the next study guide that may be navigated to (as a
   * [EphemeralSubtopic]), or default instance of there isn't one.
   */
  val nextSubtopicLiveData: LiveData<EphemeralSubtopic> by lazy {
    Transformations.map(topicLiveData, ::processNextSubtopicData)
  }

  /**
   * The [LiveData] that will correspond to the previous study guide that may be navigated to (as
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

  private val studyGuideResultLiveData: LiveData<AsyncResult<EphemeralStudyGuide>> by lazy {
    topicController.getStudyGuide(profileId, topicId, subtopicId).toLiveData()
  }

  private fun processStudyGuideLiveData(): LiveData<EphemeralStudyGuide> {
    return Transformations.map(studyGuideResultLiveData, ::processStudyGuide)
  }

  private fun processStudyGuide(
    studyGuideResult: AsyncResult<EphemeralStudyGuide>
  ): EphemeralStudyGuide {
    return when (studyGuideResult) {
      is AsyncResult.Failure -> {
        oppiaLogger.e(
          "StudyGuideFragment", "Failed to retrieve Study Guide", studyGuideResult.error
        )
        EphemeralStudyGuide.getDefaultInstance()
      }
      is AsyncResult.Pending -> EphemeralStudyGuide.getDefaultInstance()
      is AsyncResult.Success -> studyGuideResult.value
    }
  }

  /** Factory to create new [StudyGuideViewModel]s. */
  class Factory @Inject constructor(
    private val activity: AppCompatActivity,
    private val topicController: TopicController,
    private val oppiaLogger: OppiaLogger,
    @TopicHtmlParserEntityType private val entityType: String,
    private val appLanguageResourceHandler: AppLanguageResourceHandler,
    private val translationController: TranslationController
  ) {
    /** Returns a new [StudyGuideViewModel]. */
    fun create(
      topicId: String,
      subtopicId: Int,
      profileId: LegacyProfileId,
      subtopicListSize: Int
    ): StudyGuideViewModel {
      return StudyGuideViewModel(
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
