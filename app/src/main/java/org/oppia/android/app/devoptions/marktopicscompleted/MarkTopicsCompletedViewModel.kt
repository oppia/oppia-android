package org.oppia.android.app.devoptions.marktopicscompleted

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.Topic
import org.oppia.android.app.viewmodel.ObservableViewModel
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.topic.ModifyLessonProgressController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import javax.inject.Inject

/** The ViewModel for [MarkTopicsCompletedFragment]. */
@FragmentScope
class MarkTopicsCompletedViewModel @Inject constructor(
  private val oppiaLogger: OppiaLogger,
  private val modifyLessonProgressController: ModifyLessonProgressController,
) : ObservableViewModel() {

  private var internalProfileId: Int = -1

  val topicSummaryLiveData: LiveData<List<TopicSummaryViewModel>> by lazy {
    Transformations.map(allTopicsLiveData, ::processAllTopics)
  }

  private val allTopicsLiveData: LiveData<List<Topic>> by lazy { getAllTopics() }

  private val allTopicsResultLiveData: LiveData<AsyncResult<List<Topic>>> by lazy {
    modifyLessonProgressController
      .getAllTopicsWithProgress(ProfileId.newBuilder().setInternalId(internalProfileId).build())
      .toLiveData()
  }

  private fun getAllTopics(): LiveData<List<Topic>> {
    return Transformations.map(allTopicsResultLiveData, ::processAllTopicsResult)
  }

  private fun processAllTopicsResult(allTopics: AsyncResult<List<Topic>>): List<Topic> {
    if (allTopics.isFailure()) {
      oppiaLogger.e(
        "MarkTopicsCompletedFragment",
        "Failed to retrieve all topics",
        allTopics.getErrorOrNull()!!
      )
    }
    return allTopics.getOrDefault(mutableListOf())
  }

  private fun processAllTopics(allTopics: List<Topic>): List<TopicSummaryViewModel> {
    val itemList = mutableListOf<TopicSummaryViewModel>()
    allTopics.forEach { topic ->
      itemList.add(TopicSummaryViewModel(topic))
    }
    return itemList
  }

  fun setInternalProfileId(internalProfileId: Int) {
    this.internalProfileId = internalProfileId
  }
}
