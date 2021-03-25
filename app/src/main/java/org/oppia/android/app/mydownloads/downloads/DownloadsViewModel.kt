package org.oppia.android.app.mydownloads.downloads

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.TopicList
import org.oppia.android.app.viewmodel.ObservableViewModel
import org.oppia.android.domain.topic.TopicListController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import org.oppia.android.util.logging.ConsoleLogger
import org.oppia.android.util.parser.TopicHtmlParserEntityType
import javax.inject.Inject

/** [ObservableViewModel] for [DownloadsFragment]. */
@FragmentScope
class DownloadsViewModel @Inject constructor(
  private val logger: ConsoleLogger,
  private val downloadManagementController: TopicListController,
  @TopicHtmlParserEntityType private val topicEntityType: String
) : ObservableViewModel() {

  val downloadsViewModelLiveData: LiveData<List<DownloadsItemViewModel>> by lazy {
    Transformations.map(downloadedTopicListLiveData, ::processDownloadedTopicList)
  }

  // TODO(): use DownloadManagementController to get topic list
  private val downloadedTopicListResultLiveData: LiveData<AsyncResult<TopicList>> by lazy {
    downloadManagementController.getTopicList().toLiveData()
  }

  private val downloadedTopicListLiveData: LiveData<TopicList> by lazy {
    Transformations.map(downloadedTopicListResultLiveData, ::processDownloadedTopicListResult)
  }

  private fun processDownloadedTopicListResult(
    downloadedTopicListResult: AsyncResult<TopicList>
  ): TopicList {
    if (downloadedTopicListResult.isFailure()) {
      logger.e(
        "DownloadsFragment",
        "Failed to retrieve DownloadedTopic list: ",
        downloadedTopicListResult.getErrorOrNull()!!
      )
    }
    return downloadedTopicListResult.getOrDefault(TopicList.getDefaultInstance())
  }

  private fun processDownloadedTopicList(
    downloadedTopicList: TopicList
  ): List<DownloadsItemViewModel> {

    // Add sort by item to the list
    val downloadsItemViewModelList: MutableList<DownloadsItemViewModel> = mutableListOf(
      DownloadsSortByViewModel() as DownloadsItemViewModel
    )

    // Add the rest of the list
    downloadsItemViewModelList.addAll(
      downloadedTopicList.topicSummaryList.map { topic ->
        DownloadsTopicViewModel(
          topic,
          topicEntityType
        )
      }
    )
    return downloadsItemViewModelList
  }
}
