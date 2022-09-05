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
import org.oppia.android.app.model.EphemeralTopic
import org.oppia.android.domain.translation.TranslationController

/**
 * [ViewModel] for [MarkTopicsCompletedFragment]. It populates the recyclerview with a list of
 * [TopicViewModel] which in turn display the topic.
 */
@FragmentScope
class MarkTopicsCompletedViewModel @Inject constructor(
  private val oppiaLogger: OppiaLogger,
  private val modifyLessonProgressController: ModifyLessonProgressController,
  private val translationController: TranslationController
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

  private val allTopicsLiveData: LiveData<List<EphemeralTopic>> by lazy { getAllTopics() }

  private val allTopicsResultLiveData: LiveData<AsyncResult<List<EphemeralTopic>>> by lazy {
    modifyLessonProgressController.getAllTopicsWithProgress(profileId).toLiveData()
  }

  private fun getAllTopics(): LiveData<List<EphemeralTopic>> {
    return Transformations.map(allTopicsResultLiveData, ::processAllTopicsResult)
  }

  private fun processAllTopicsResult(
    ephemeralResult: AsyncResult<List<EphemeralTopic>>
  ): List<EphemeralTopic> {
    return when (ephemeralResult) {
      is AsyncResult.Failure -> {
        oppiaLogger.e(
          "MarkTopicsCompletedFragment", "Failed to retrieve all topics", ephemeralResult.error
        )
        mutableListOf()
      }
      is AsyncResult.Pending -> mutableListOf()
      is AsyncResult.Success -> ephemeralResult.value
    }
  }

  private fun processAllTopics(allTopics: List<EphemeralTopic>): List<TopicViewModel> {
    itemList.clear()
    allTopics.forEach { ephemeralTopic ->
      val isCompleted = modifyLessonProgressController.checkIfTopicIsCompleted(ephemeralTopic)
      itemList.add(TopicViewModel(ephemeralTopic, isCompleted, translationController))
    }
    return itemList
  }

  fun setProfileId(profileId: ProfileId) {
    this.profileId = profileId
  }

  /** Returns a list of [TopicViewModel]s whose progress can be modified. */
  fun getTopicList(): List<TopicViewModel> = itemList.toList()
}
