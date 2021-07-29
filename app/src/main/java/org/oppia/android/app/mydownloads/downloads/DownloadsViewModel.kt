package org.oppia.android.app.mydownloads.downloads

import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.Profile
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.TopicList
import org.oppia.android.app.model.TopicSummary
import org.oppia.android.app.viewmodel.ObservableViewModel
import org.oppia.android.domain.profile.ProfileManagementController
import org.oppia.android.domain.topic.TopicListController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import org.oppia.android.util.filesize.FileSizeUtil
import org.oppia.android.util.logging.ConsoleLogger
import org.oppia.android.util.parser.html.TopicHtmlParserEntityType
import javax.inject.Inject

/** [ObservableViewModel] for [DownloadsFragment]. */
@FragmentScope
class DownloadsViewModel @Inject constructor(
  private val fragment: Fragment,
  private val logger: ConsoleLogger,
  private val downloadManagementController: TopicListController,
  @TopicHtmlParserEntityType private val topicEntityType: String,
  private val fileSizeUtil: FileSizeUtil,
  private val profileManagementController: ProfileManagementController
) : ObservableViewModel() {

  private var internalProfileId: Int = -1
  lateinit var adminPin: String
  private var sortTypeIndex: Int = 0

  val profileLiveData: LiveData<Profile> by lazy {
    Transformations.map(
      profileManagementController.getProfiles().toLiveData(),
      ::processGetProfilesResult
    )
  }

  private fun processGetProfilesResult(profilesResult: AsyncResult<List<Profile>>): Profile {
    if (profilesResult.isFailure()) {
      logger.e(
        "DownloadsViewModel",
        "Failed to retrieve the list of profiles",
        profilesResult.getErrorOrNull()!!
      )
    }

    val profileList = profilesResult.getOrDefault(emptyList())
    val adminProfile = profileList.single { profile ->
      profile.isAdmin
    }
    adminPin = adminProfile.pin
    val profileId = ProfileId.newBuilder().setInternalId(internalProfileId).build()
    return profileList.single { profile ->
      profileId == profile.id
    }
  }

  fun setInternalProfileId(internalProfileId: Int) {
    this.internalProfileId = internalProfileId
  }

  fun setSortTypeIndex(sortTypeIndex: Int) {
    this.sortTypeIndex = sortTypeIndex
  }

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

    var topicSummarySortedList = mutableListOf<TopicSummary>()
    when (sortTypeIndex) {
      0 -> {
        // TODO(#552): update it with the time stamp value in the list
        topicSummarySortedList = sortTopicListOnDownloadSize(downloadedTopicList.topicSummaryList)
      }
      1 -> {
        topicSummarySortedList = sortTopicListAlphabetically(downloadedTopicList.topicSummaryList)
      }
      2 -> {
        topicSummarySortedList = sortTopicListOnDownloadSize(downloadedTopicList.topicSummaryList)
      }
    }

    // Add the rest of the list
    downloadsItemViewModelList.addAll(
      topicSummarySortedList.map { topic ->
        DownloadsTopicViewModel(
          fragment = fragment,
          topicSummary = topic,
          topicEntityType = topicEntityType,
          topicSize = fileSizeUtil.calculateTopicSizeWithBytes(topic.diskSizeBytes),
          internalProfileId = internalProfileId
        )
      }
    )
    return downloadsItemViewModelList
  }

  private fun sortTopicListOnDownloadSize(
    topicSummaryList: MutableList<TopicSummary>
  ): MutableList<TopicSummary> {
    val sortedDownloadSizeList = mutableListOf<Long>()
    topicSummaryList.map { topic ->
      sortedDownloadSizeList.add(topic.diskSizeBytes)
    }
    sortedDownloadSizeList.sort()
    val sortedDownloadSizeTopicList = mutableListOf<TopicSummary>()
    sortedDownloadSizeList.map { size ->
      topicSummaryList.map { topicSummary ->
        if (size == topicSummary.diskSizeBytes) {
          sortedDownloadSizeTopicList.add(topicSummary)
        }
      }
    }
    return sortedDownloadSizeTopicList
  }

  private fun sortTopicListAlphabetically(
    topicSummaryList: MutableList<TopicSummary>
  ): MutableList<TopicSummary> {
    val sortedDownloadSizeList = mutableListOf<String>()
    topicSummaryList.map { topic ->
      sortedDownloadSizeList.add(topic.name)
    }
    sortedDownloadSizeList.sort()
    val sortedDownloadSizeTopicList = mutableListOf<TopicSummary>()
    sortedDownloadSizeList.map { name ->
      topicSummaryList.map { topicSummary ->
        if (name == topicSummary.name) {
          sortedDownloadSizeTopicList.add(topicSummary)
        }
      }
    }
    return sortedDownloadSizeTopicList
  }
}
