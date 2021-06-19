package org.oppia.android.app.devoptions.markchapterscompleted

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import javax.inject.Inject
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.Topic
import org.oppia.android.app.model.TopicList
import org.oppia.android.app.viewmodel.ObservableViewModel
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.topic.TopicController
import org.oppia.android.domain.topic.TopicListController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData

/** The ViewModel for [MarkChaptersCompletedActivity]. */
@FragmentScope
class MarkChaptersCompletedViewModel @Inject constructor(
  private val oppiaLogger: OppiaLogger,
  private val topicListController: TopicListController,
  private val topicController: TopicController
) : ObservableViewModel() {

  private var internalProfileId: Int = -1
  val itemList: MutableList<StorySummaryViewModel> = ArrayList()

  val storySummaryLiveData: LiveData<List<StorySummaryViewModel>> by lazy {
    Transformations.map(topicListLiveData, ::processTopicList)
  }

  private val topicListLiveData: LiveData<TopicList> by lazy { getTopicList() }

  private fun getTopicList(): LiveData<TopicList> {
    return Transformations.map(topicListResultLiveData, ::processTopicListResult)
  }

  private val topicListResultLiveData: LiveData<AsyncResult<TopicList>> by lazy {
    topicListController.getTopicList().toLiveData()
  }

  private fun processTopicListResult(topicList: AsyncResult<TopicList>): TopicList {
    if (topicList.isFailure()) {
      oppiaLogger.e(
        "MarkChaptersCompletedFragment",
        "Failed to retrieve topicList",
        topicList.getErrorOrNull()!!
      )
    }
    return topicList.getOrDefault(TopicList.getDefaultInstance())
  }

  private fun processTopicList(topicList: TopicList): List<StorySummaryViewModel> {
    topicList.topicSummaryList.forEach { topicSummary ->
      val topicId = topicSummary.topicId
      val topicResultLiveData = topicController.getTopic(
        ProfileId.newBuilder().setInternalId(internalProfileId).build(),
        topicId
      ).toLiveData()
      val topicLiveData = Transformations.map(topicResultLiveData, ::processTopicResult)
      val storyList = Transformations.map(topicLiveData, ::processTopic)
      itemList.plus(storyList)
    }
    return itemList
  }

  private fun processTopicResult(topic: AsyncResult<Topic>): Topic {
    if (topic.isFailure()) {
      oppiaLogger.e(
        "MarkChaptersCompletedFragment",
        "Failed to retrieve topic",
        topic.getErrorOrNull()!!
      )
    }
    return topic.getOrDefault(Topic.getDefaultInstance())
  }

  private fun processTopic(topic: Topic): List<StorySummaryViewModel> {
    val topicList: MutableList<StorySummaryViewModel> = ArrayList()
    if (topic.storyList.isNotEmpty()) {
      for (storySummary in topic.storyList) {
        topicList.add(
          StorySummaryViewModel(storySummary)
        )
      }
    }
    return topicList
  }

  fun setInternalProfileId(internalProfileId: Int) {
    this.internalProfileId = internalProfileId
  }
}