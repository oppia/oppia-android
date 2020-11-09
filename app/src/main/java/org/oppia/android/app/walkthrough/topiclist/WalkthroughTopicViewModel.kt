package org.oppia.android.app.walkthrough.topiclist

import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import org.oppia.android.app.home.topiclist.TopicSummaryClickListener
import org.oppia.android.app.model.TopicList
import org.oppia.android.app.viewmodel.ObservableViewModel
import org.oppia.android.app.walkthrough.topiclist.topiclistviewmodel.WalkthroughTopicHeaderViewModel
import org.oppia.android.app.walkthrough.topiclist.topiclistviewmodel.WalkthroughTopicSummaryViewModel
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.topic.TopicListController
import org.oppia.android.util.data.AsyncResult
import javax.inject.Inject

/** The ObservableViewModel for [WalkthroughTopicListFragment]. */
class WalkthroughTopicViewModel @Inject constructor(
  private val fragment: Fragment,
  private val topicListController: TopicListController,
  private val oppiaLogger: OppiaLogger
) : ObservableViewModel() {
  val walkthroughTopicViewModelLiveData: LiveData<List<WalkthroughTopicItemViewModel>> by lazy {
    Transformations.map(topicListSummaryLiveData, ::processCompletedTopicList)
  }

  private val topicListSummaryResultLiveData: LiveData<AsyncResult<TopicList>> by lazy {
    topicListController.getTopicList()
  }

  private val topicListSummaryLiveData: LiveData<TopicList> by lazy {
    Transformations.map(topicListSummaryResultLiveData, ::processTopicListResult)
  }

  private fun processTopicListResult(topicSummaryListResult: AsyncResult<TopicList>): TopicList {
    if (topicSummaryListResult.isFailure()) {
      oppiaLogger.e(
        "WalkthroughTopicSummaryListFragment",
        "Failed to retrieve TopicSummary list: ",
        topicSummaryListResult.getErrorOrNull()!!
      )
    }
    return topicSummaryListResult.getOrDefault(TopicList.getDefaultInstance())
  }

  private fun processCompletedTopicList(topicList: TopicList): List<WalkthroughTopicItemViewModel> {
    // List with only the header
    val itemViewModelList: MutableList<WalkthroughTopicItemViewModel> = mutableListOf(
      WalkthroughTopicHeaderViewModel() as WalkthroughTopicItemViewModel
    )

    // Add the rest of the list
    itemViewModelList.addAll(
      topicList.topicSummaryList.map { topic ->
        WalkthroughTopicSummaryViewModel(
          topic,
          fragment as TopicSummaryClickListener
        )
      }
    )
    return itemViewModelList
  }
}
