package org.oppia.android.app.devoptions.markstoriescompleted

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.TopicList
import org.oppia.android.app.viewmodel.ObservableViewModel
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.topic.TopicController
import org.oppia.android.domain.topic.TopicListController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import javax.inject.Inject

/** The ViewModel for [MarkStoriesCompletedFragment]. */
@FragmentScope
class MarkStoriesCompletedViewModel @Inject constructor(
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
        "MarkStoriesCompletedFragment",
        "Failed to retrieve topicList",
        topicList.getErrorOrNull()!!
      )
    }
    return topicList.getOrDefault(TopicList.getDefaultInstance())
  }

  private fun processTopicList(topicList: TopicList): List<StorySummaryViewModel> {
    itemList.clear()
    topicList.topicSummaryList.forEach { topicSummary ->
      val topicId = topicSummary.topicId
      val topic = topicController.retrieveTopic(topicId = topicId)
      topic.storyList.forEach { storySummary ->
        itemList.add(StorySummaryViewModel(storySummary))
      }
    }
    return itemList
  }

  fun setInternalProfileId(internalProfileId: Int) {
    this.internalProfileId = internalProfileId
  }
}
