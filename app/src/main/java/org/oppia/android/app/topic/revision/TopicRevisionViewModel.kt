package org.oppia.android.app.topic.revision

import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.EphemeralTopic
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.topic.revision.revisionitemviewmodel.TopicRevisionItemViewModel
import org.oppia.android.app.viewmodel.ObservableViewModel
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.topic.TopicController
import org.oppia.android.domain.translation.TranslationController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import org.oppia.android.util.parser.html.TopicHtmlParserEntityType
import javax.inject.Inject

/** [ObservableViewModel] for [TopicRevisionFragment]. */
@FragmentScope
class TopicRevisionViewModel @Inject constructor(
  private val topicController: TopicController,
  private val oppiaLogger: OppiaLogger,
  val fragment: Fragment,
  private val translationController: TranslationController,
  @TopicHtmlParserEntityType private val entityType: String
) : ObservableViewModel() {
  private lateinit var profileId: ProfileId
  private lateinit var topicId: String
  private val subtopicList: MutableList<TopicRevisionItemViewModel> = ArrayList()
  private val revisionSubtopicSelector: RevisionSubtopicSelector =
    fragment as RevisionSubtopicSelector

  private val topicResultLiveData: LiveData<AsyncResult<EphemeralTopic>> by lazy {
    topicController.getTopic(profileId, topicId).toLiveData()
  }

  private val topicLiveData: LiveData<EphemeralTopic> by lazy { getTopicList() }

  val subtopicLiveData: LiveData<List<TopicRevisionItemViewModel>> by lazy {
    Transformations.map(topicLiveData, ::processTopic)
  }

  private fun processTopic(ephemeralTopic: EphemeralTopic): List<TopicRevisionItemViewModel> {
    subtopicList.clear()
    subtopicList.addAll(
      ephemeralTopic.subtopicsList.map {
        TopicRevisionItemViewModel(
          topicId, it, entityType, revisionSubtopicSelector, translationController
        )
      }
    )
    return subtopicList
  }

  private fun getTopicList(): LiveData<EphemeralTopic> {
    return Transformations.map(topicResultLiveData, ::processTopicResult)
  }

  private fun processTopicResult(ephemeralResult: AsyncResult<EphemeralTopic>): EphemeralTopic {
    return when (ephemeralResult) {
      is AsyncResult.Failure -> {
        oppiaLogger.e("TopicRevisionFragment", "Failed to retrieve topic", ephemeralResult.error)
        EphemeralTopic.getDefaultInstance()
      }
      is AsyncResult.Pending -> EphemeralTopic.getDefaultInstance()
      is AsyncResult.Success -> ephemeralResult.value
    }
  }

  fun setTopicId(topicId: String) {
    this.topicId = topicId
  }

  fun setProfileId(profileId: ProfileId) {
    this.profileId = profileId
  }
}
