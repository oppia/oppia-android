package org.oppia.app.player.content

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import org.oppia.app.fragment.FragmentScope
import org.oppia.data.backends.gae.model.GaeSubtitledHtml
import org.oppia.domain.content.ContentListController
import org.oppia.util.data.AsyncResult
import javax.inject.Inject

/** [ViewModel] for showing a list of content summaries. */
@FragmentScope
class ContentListViewModel @Inject constructor(
  private val contentListController: ContentListController
) : ViewModel() {
  /**
   * The retrieved [LiveData] for retrieving content summaries. This model should ensure only one
   * [LiveData] is used for all subsequent processed data to ensure the transformed [LiveData]s are
   * always in sync.
   */
//  private val contentListSummaryResultLiveData: LiveData<AsyncResult<List<GaeSubtitledHtml>>> by lazy {
//    contentListController.getContentList()
//  }
//  val contentListSummaryLiveData: LiveData<List<GaeSubtitledHtml>> by lazy { getTopicList() }
//  val contentListLookupFailed: LiveData<Boolean> by lazy { getWhetherTopicListLookupFailed() }
//
//  private fun getTopicList(): LiveData<List<GaeSubtitledHtml>> {
//    // If there's an error loading the data, assume the default.
//    return Transformations.map(contentListSummaryResultLiveData) { expandList(it.getOrDefault(emptyList())) }
//  }
//
//  private fun getWhetherTopicListLookupFailed(): LiveData<Boolean> {
//    return Transformations.map(contentListSummaryResultLiveData) { it.isFailure() }
//  }

  // TODO(BenHenning): Remove
  private fun <T> expandList(list: List<T>): List<T> {
    val v = list.first()
    return listOf(v, v, v, v, v)
  }
}
