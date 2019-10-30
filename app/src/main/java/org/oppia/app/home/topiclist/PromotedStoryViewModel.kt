package org.oppia.app.home.topiclist

import android.util.Log
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.model.PromotedStory
import org.oppia.app.model.TopicList
import org.oppia.domain.topic.TopicListController
import org.oppia.util.data.AsyncResult
import javax.inject.Inject

/** [ViewModel] for displaying a promoted story. */
@FragmentScope
class PromotedStoryViewModel @Inject constructor(
  private val topicListController: TopicListController
) : ViewModel() {
  /**
   * The retrieved [LiveData] for retrieving topic summaries. This model should ensure only one
   * [LiveData] is used for all subsequent processed data to ensure the transformed [LiveData]s are
   * always in sync.
   */
  private val topicListSummaryResultLiveData: LiveData<AsyncResult<TopicList>> by lazy {
    topicListController.getTopicList()
  }
  val promotedStoryLiveData: LiveData<PromotedStory> by lazy { getPromotedStory() }
  val topicListLookupSucceeded: LiveData<Boolean> by lazy { getWhetherTopicListLookupSucceeded() }
  val topicListLookupFailed: LiveData<Boolean> by lazy { getWhetherTopicListLookupFailed() }
  val topicListIsLoading: LiveData<Boolean> by lazy { getWhetherTopicListIsLoading() }

  fun clickOnStoryTile(@Suppress("UNUSED_PARAMETER") v: View) {
    Log.d("TAG","clickOnStoryTile")
  }

  private fun getPromotedStory(): LiveData<PromotedStory> {
    return Transformations.map(topicListSummaryResultLiveData) {
      it.getOrDefault(TopicList.getDefaultInstance()).promotedStory
    }
  }

  private fun getWhetherTopicListLookupSucceeded(): LiveData<Boolean> {
    return Transformations.map(topicListSummaryResultLiveData) { it.isSuccess() }
  }

  private fun getWhetherTopicListLookupFailed(): LiveData<Boolean> {
    return Transformations.map(topicListSummaryResultLiveData) { it.isFailure() }
  }

  private fun getWhetherTopicListIsLoading(): LiveData<Boolean> {
    return Transformations.map(topicListSummaryResultLiveData) { it.isPending() }
  }
}
