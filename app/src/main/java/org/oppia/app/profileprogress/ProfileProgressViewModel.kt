package org.oppia.app.profileprogress

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.model.OngoingStoryList
import org.oppia.domain.topic.TopicListController
import org.oppia.util.data.AsyncResult
import org.oppia.util.logging.Logger
import javax.inject.Inject

/** The ViewModel for header in [ProfileProgressFragment]. */
@FragmentScope
class ProfileProgressViewModel @Inject constructor(
  private val topicListController: TopicListController,
  private val logger: Logger
) : ViewModel() {
  /** [internalProfileId] needs to be set before any of the live data members can be accessed. */
  private var internalProfileId: Int = -1

  private val ongoingStoryListResultLiveData: LiveData<AsyncResult<OngoingStoryList>> by lazy {
    topicListController.getOngoingStoryList()
  }

  private val ongoingStoryListLiveData: LiveData<OngoingStoryList> by lazy {
    Transformations.map(ongoingStoryListResultLiveData, ::processOngoingTopicResult)
  }

  val ongoingStoryListViewModelLiveData: LiveData<List<ProfileProgressItemViewModel>> by lazy {
    Transformations.map(ongoingStoryListLiveData, ::processOngoingStoryList)
  }

  fun setProfileId(internalProfileId: Int) {
    this.internalProfileId = internalProfileId
  }

  private fun processOngoingTopicResult(ongoingStoryListResult: AsyncResult<OngoingStoryList>): OngoingStoryList {
    if (ongoingStoryListResult.isFailure()) {
      logger.e(
        "ProfileProgressFragment",
        "Failed to retrieve OngoingStoryList: ",
        ongoingStoryListResult.getErrorOrNull()!!
      )
    }

    return ongoingStoryListResult.getOrDefault(OngoingStoryList.getDefaultInstance())
  }

  private fun processOngoingStoryList(ongoingStoryList: OngoingStoryList): List<ProfileProgressItemViewModel> {
    val itemViewModelList: MutableList<ProfileProgressItemViewModel> = mutableListOf(
      ProfileProgressHeaderViewModel(0, 0) as ProfileProgressItemViewModel
    )
    itemViewModelList.addAll(ongoingStoryList.recentStoryList.map { story ->
      RecentlyPlayedStorySummaryViewModel(story) as ProfileProgressItemViewModel
    })
    return itemViewModelList
  }
}
