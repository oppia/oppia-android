package org.oppia.app.home.topiclist

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.model.TopicSummary
import org.oppia.domain.topic.TopicListController
import org.oppia.util.data.AsyncResult
import javax.inject.Inject

/** [ViewModel] for showing a list of topic summaries. */
@FragmentScope
class TopicListViewModel @Inject constructor(
  private val topicListController: TopicListController
) : ViewModel() {
  /**
   * The retrieved [LiveData] for retrieving topic summaries. This model should ensure only one
   * [LiveData] is used for all subsequent processed data to ensure the transformed [LiveData]s are
   * always in sync.
   */
  private val topicListSummaryResultLiveData: LiveData<AsyncResult<List<TopicSummary>>> by lazy {
    topicListController.getTopicList()
  }
  val topicListSummaryLiveData: LiveData<List<TopicSummary>> by lazy { getTopicList() }
  val topicListLookupFailed: LiveData<Boolean> by lazy { getWhetherTopicListLookupFailed() }

  private fun getTopicList(): LiveData<List<TopicSummary>> {
    // If there's an error loading the data, assume the default.
    return Transformations.map(topicListSummaryResultLiveData) { expandList(it.getOrDefault(emptyList())) }
  }

  private fun getWhetherTopicListLookupFailed(): LiveData<Boolean> {
    return Transformations.map(topicListSummaryResultLiveData) { it.isFailure() }
  }

  // TODO(BenHenning): Remove
  private fun <T> expandList(list: List<T>): List<T> {
    val v = list.first()
    return listOf(v, v, v, v, v)
  }
}
