package org.oppia.android.app.topic.revision

import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
<<<<<<< HEAD:app/src/main/java/org/oppia/android/app/topic/revision/TopicRevisionViewModel.kt
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.Topic
import org.oppia.android.app.topic.revision.revisionitemviewmodel.TopicRevisionItemViewModel
import org.oppia.android.app.viewmodel.ObservableViewModel
import org.oppia.android.domain.topic.TopicController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.logging.ConsoleLogger
import org.oppia.android.util.parser.TopicHtmlParserEntityType
=======
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.model.ProfileId
import org.oppia.app.model.Topic
import org.oppia.app.topic.revision.revisionitemviewmodel.TopicRevisionItemViewModel
import org.oppia.app.viewmodel.ObservableViewModel
import org.oppia.domain.topic.TopicController
import org.oppia.util.data.AsyncResult
import org.oppia.util.data.DataProviders.Companion.toLiveData
import org.oppia.util.logging.ConsoleLogger
import org.oppia.util.parser.TopicHtmlParserEntityType
>>>>>>> develop:app/src/main/java/org/oppia/app/topic/revision/TopicRevisionViewModel.kt
import javax.inject.Inject

/** [ObservableViewModel] for [TopicRevisionFragment]. */
@FragmentScope
class TopicRevisionViewModel @Inject constructor(
  private val topicController: TopicController,
  private val logger: ConsoleLogger,
  val fragment: Fragment,
  @TopicHtmlParserEntityType private val entityType: String
) : ObservableViewModel() {
  private lateinit var profileId: ProfileId
  private lateinit var topicId: String
  private val subtopicList: MutableList<TopicRevisionItemViewModel> = ArrayList()
  private val revisionSubtopicSelector: RevisionSubtopicSelector =
    fragment as RevisionSubtopicSelector

  private val topicResultLiveData: LiveData<AsyncResult<Topic>> by lazy {
    topicController.getTopic(profileId, topicId).toLiveData()
  }

  private val topicLiveData: LiveData<Topic> by lazy { getTopicList() }

  val subtopicLiveData: LiveData<List<TopicRevisionItemViewModel>> by lazy {
    Transformations.map(topicLiveData, ::processTopic)
  }

  private fun processTopic(topic: Topic): List<TopicRevisionItemViewModel> {
    subtopicList.clear()
    subtopicList.addAll(
      topic.subtopicList.map {
        TopicRevisionItemViewModel(topicId, it, entityType, revisionSubtopicSelector)
      }
    )
    return subtopicList
  }

  private fun getTopicList(): LiveData<Topic> {
    return Transformations.map(topicResultLiveData, ::processTopicResult)
  }

  private fun processTopicResult(topic: AsyncResult<Topic>): Topic {
    if (topic.isFailure()) {
      logger.e(
        "TopicRevisionFragment",
        "Failed to retrieve topic",
        topic.getErrorOrNull()!!
      )
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
