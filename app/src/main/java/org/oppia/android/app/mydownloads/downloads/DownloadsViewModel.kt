package org.oppia.android.app.mydownloads.downloads

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.Profile
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.TopicList
import org.oppia.android.app.shim.IntentFactoryShim
import org.oppia.android.app.viewmodel.ObservableViewModel
import org.oppia.android.domain.profile.ProfileManagementController
import org.oppia.android.domain.topic.TopicListController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import org.oppia.android.util.filesize.FileSizeUtil
import org.oppia.android.util.logging.ConsoleLogger
import org.oppia.android.util.parser.TopicHtmlParserEntityType
import javax.inject.Inject

/** [ObservableViewModel] for [DownloadsFragment]. */
@FragmentScope
class DownloadsViewModel @Inject constructor(
  private val activity: AppCompatActivity,
  private val logger: ConsoleLogger,
  private val downloadManagementController: TopicListController,
  @TopicHtmlParserEntityType private val topicEntityType: String,
  private val fileSizeUtil: FileSizeUtil,
  private val intentFactoryShim: IntentFactoryShim,
  private val profileManagementController: ProfileManagementController
) : ObservableViewModel() {

  private var internalProfileId: Int = -1
  private lateinit var profileId: ProfileId
  lateinit var adminPin: String

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

    return profileList.single { profile ->
      profileId == profile.id
    }
  }

  fun setInternalProfileId(internalProfileId: Int) {
    this.internalProfileId = internalProfileId
  }

  fun setProfileId(internalProfileId: Int) {
    profileId = ProfileId.newBuilder().setInternalId(internalProfileId).build()
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

    // Add the rest of the list
    downloadsItemViewModelList.addAll(
      downloadedTopicList.topicSummaryList.map { topic ->
        DownloadsTopicViewModel(
          activity = activity,
          topicSummary = topic,
          topicEntityType = topicEntityType,
          topicSize = fileSizeUtil.calculateTopicSizeWithBytes(topic.diskSizeBytes),
          intentFactoryShim = intentFactoryShim,
          internalProfileId = internalProfileId
        )
      }
    )
    return downloadsItemViewModelList
  }
}
