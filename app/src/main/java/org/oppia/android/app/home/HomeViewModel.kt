package org.oppia.android.app.home

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.home.topiclist.TopicSummaryClickListener
import org.oppia.android.app.home.topiclist.TopicSummaryViewModel
import org.oppia.android.app.model.TopicList
import org.oppia.android.app.viewmodel.ObservableViewModel
import org.oppia.android.app.viewmodel.ViewModelProvider
import org.oppia.android.domain.topic.TopicListController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.parser.TopicHtmlParserEntityType
import javax.inject.Inject

@FragmentScope
class HomeViewModel @Inject constructor(
  private val activity: AppCompatActivity,
  private val fragment: Fragment,
  private val topicListController: TopicListController,
  @TopicHtmlParserEntityType private val topicEntityType: String
  ) : ObservableViewModel() {

  private var itemList: MutableList<HomeItemViewModel> = ArrayList()
  private val topicListSummaryResultLiveData: LiveData<AsyncResult<TopicList>> by lazy {
    topicListController.getTopicList()
  }
  val itemListLiveData: LiveData<List<HomeItemViewModel>> by lazy {
    Transformations.map(assumedSuccessfulTopicListLiveData, ::processItemList)
  }

  private fun processItemList(it: TopicList) : List<HomeItemViewModel> {
    for (topicSummary in it.topicSummaryList) {
      val topicSummaryViewModel =
        TopicSummaryViewModel(
          activity,
          topicSummary,
          topicEntityType,
          fragment as TopicSummaryClickListener
        )
      topicSummaryViewModel.setPosition(it.topicSummaryList.indexOf(topicSummary))
      itemList.add(topicSummaryViewModel)
    }
    return itemList
  }

  private val assumedSuccessfulTopicListLiveData: LiveData<TopicList> by lazy {
    // If there's an error loading the data, assume the default.
    Transformations.map(topicListSummaryResultLiveData) {
      it.getOrDefault(TopicList.getDefaultInstance())
    }
  }

  fun addHomeItem(viewModel: HomeItemViewModel) {
    itemList.add(viewModel)
  }
}
