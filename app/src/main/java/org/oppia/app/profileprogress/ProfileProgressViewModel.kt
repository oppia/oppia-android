package org.oppia.app.profileprogress

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.model.OngoingStoryList
import org.oppia.app.model.Profile
import org.oppia.app.model.ProfileId
import org.oppia.domain.profile.ProfileManagementController
import org.oppia.domain.topic.TopicListController
import org.oppia.util.data.AsyncResult
import org.oppia.util.logging.Logger
import javax.inject.Inject

/** The ViewModel for header in [ProfileProgressFragment]. */
@FragmentScope
class ProfileProgressViewModel @Inject constructor(
  activity: AppCompatActivity,
  private val fragment: Fragment,
  private val profileManagementController: ProfileManagementController,
  private val topicListController: TopicListController,
  private val logger: Logger
) : ViewModel() {
  /** [internalProfileId] needs to be set before any of the live data members can be accessed. */
  private var internalProfileId: Int = -1

  private val headerViewModel = ProfileProgressHeaderViewModel(activity)

  private val itemViewModelList: MutableList<ProfileProgressItemViewModel> = mutableListOf(
    headerViewModel as ProfileProgressItemViewModel
  )

  fun setProfileId(internalProfileId: Int) {
    this.internalProfileId = internalProfileId

    subscribeToProfileLiveData()
  }

  private fun getProfileData(): LiveData<Profile> {
    return Transformations.map(
      profileManagementController.getProfile(
        ProfileId.newBuilder().setInternalId(
          internalProfileId
        ).build()
      ), ::processGetProfileResult
    )
  }

  private fun subscribeToProfileLiveData() {
    getProfileData().observe(fragment, Observer<Profile> {
      headerViewModel.setProfile(it)
    })
  }

  private fun processGetProfileResult(profileResult: AsyncResult<Profile>): Profile {
    if (profileResult.isFailure()) {
      logger.e("ProfileProgressFragment", "Failed to retrieve profile", profileResult.getErrorOrNull()!!)
    }
    return profileResult.getOrDefault(Profile.getDefaultInstance())
  }

  private val ongoingStoryListResultLiveData: LiveData<AsyncResult<OngoingStoryList>> by lazy {
    topicListController.getOngoingStoryList()
  }

  private val ongoingStoryListLiveData: LiveData<OngoingStoryList> by lazy {
    Transformations.map(ongoingStoryListResultLiveData, ::processOngoingTopicResult)
  }

  val ongoingStoryListViewModelLiveData: LiveData<List<ProfileProgressItemViewModel>> by lazy {
    Transformations.map(ongoingStoryListLiveData, ::processOngoingStoryList)
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
    itemViewModelList.addAll(ongoingStoryList.recentStoryList.subList(0, 2).map { story ->
      RecentlyPlayedStorySummaryViewModel(story) as ProfileProgressItemViewModel
    })
    return itemViewModelList
  }
}
