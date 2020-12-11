package org.oppia.android.app.home.topiclist

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.home.HomeItemViewModel
import org.oppia.android.app.model.TopicList
import org.oppia.android.app.viewmodel.ObservableViewModel
import org.oppia.android.domain.topic.TopicListController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.parser.TopicHtmlParserEntityType
import javax.inject.Inject

@FragmentScope
class TopicSummaryListViewModel @Inject constructor(
  private val activity: AppCompatActivity,
  private val fragment: Fragment,
  private val topicListController: TopicListController,
  @TopicHtmlParserEntityType private val topicEntityType: String
  ) : HomeItemViewModel() {

  private var topicList: MutableList<HomeItemViewModel> = ArrayList()
  private val topicListSummaryResultLiveData: LiveData<AsyncResult<TopicList>> by lazy {
    topicListController.getTopicList()
  }
  val itemListLiveData: LiveData<List<HomeItemViewModel>> by lazy {
    Transformations.map(assumedSuccessfulTopicListLiveData, ::processItemList)
  }

  private fun processItemList(itemListLiveData: TopicList) : List<HomeItemViewModel> {
    for (topicSummary in itemListLiveData.topicSummaryList) {
      val topicSummaryViewModel =
        TopicSummaryViewModel(
          activity,
          topicSummary,
          topicEntityType,
          fragment as TopicSummaryClickListener
        )
      topicSummaryViewModel.setPosition(1 + itemListLiveData.topicSummaryList.indexOf(topicSummary))
      topicList.add(topicSummaryViewModel)
    }
    return topicList
  }

  private val assumedSuccessfulTopicListLiveData: LiveData<TopicList> by lazy {
    // If there's an error loading the data, assume the default.
    Transformations.map(topicListSummaryResultLiveData) {
      it.getOrDefault(TopicList.getDefaultInstance())
    }
  }
}
