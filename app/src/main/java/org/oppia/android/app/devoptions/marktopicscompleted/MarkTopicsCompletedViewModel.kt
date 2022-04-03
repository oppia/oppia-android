package org.oppia.android.app.devoptions.marktopicscompleted

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.Topic
import org.oppia.android.app.viewmodel.ObservableViewModel
import org.oppia.android.domain.devoptions.ModifyLessonProgressController
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import javax.inject.Inject

/**
 * [ViewModel] for [MarkTopicsCompletedFragment]. It populates the recyclerview with a list of
 * [TopicViewModel] which in turn display the topic.
 */
@FragmentScope
class MarkTopicsCompletedViewModel @Inject constructor(
  private val oppiaLogger: OppiaLogger,
  private val modifyLessonProgressController: ModifyLessonProgressController,
) : ObservableViewModel() {

  private lateinit var profileId: ProfileId

  private val itemList = mutableListOf<TopicViewModel>()

  /**
   * List of [TopicViewModel] used to populate recyclerview of [MarkTopicsCompletedFragment]
   * to display topics.
   */
  val topicLiveData: LiveData<List<TopicViewModel>> by lazy {
    Transformations.map(allTopicsLiveData, ::processAllTopics)
  }

  private val allTopicsLiveData: LiveData<List<Topic>> by lazy { getAllTopics() }

  private val allTopicsResultLiveData: LiveData<AsyncResult<List<Topic>>> by lazy {
    modifyLessonProgressController.getAllTopicsWithProgress(profileId).toLiveData()
  }

  private fun getAllTopics(): LiveData<List<Topic>> {
    return Transformations.map(allTopicsResultLiveData, ::processAllTopicsResult)
  }

  private fun processAllTopicsResult(allTopics: AsyncResult<List<Topic>>): List<Topic> {
    return when (allTopics) {
      is AsyncResult.Failure -> {
        oppiaLogger.e(
          "MarkTopicsCompletedFragment", "Failed to retrieve all topics", allTopics.error
        )
        mutableListOf()
      }
      is AsyncResult.Pending -> mutableListOf()
      is AsyncResult.Success -> allTopics.value
    }
  }

  private fun processAllTopics(allTopics: List<Topic>): List<TopicViewModel> {
    itemList.clear()
    allTopics.forEach { topic ->
      val isCompleted = modifyLessonProgressController.checkIfTopicIsCompleted(topic)
      itemList.add(TopicViewModel(topic, isCompleted))
    }
    return itemList
  }

  fun setProfileId(profileId: ProfileId) {
    this.profileId = profileId
  }

  /** Returns a list of [TopicViewModel]s whose progress can be modified. */
  fun getTopicList(): List<TopicViewModel> = itemList.toList()
}
