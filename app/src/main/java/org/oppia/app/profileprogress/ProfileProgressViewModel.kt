package org.oppia.app.profileprogress

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import org.oppia.app.R
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.model.CompletedStoryList
import org.oppia.app.model.OngoingStoryList
import org.oppia.app.model.OngoingTopicList
import org.oppia.app.model.Profile
import org.oppia.app.model.ProfileId
import org.oppia.domain.profile.ProfileManagementController
import org.oppia.domain.topic.TopicController
import org.oppia.domain.topic.TopicListController
import org.oppia.util.data.AsyncResult
import org.oppia.util.logging.ConsoleLogger
import org.oppia.util.parser.StoryHtmlParserEntityType
import javax.inject.Inject

/** The [ViewModel] for [ProfileProgressFragment]. */
@FragmentScope
class ProfileProgressViewModel @Inject constructor(
  activity: AppCompatActivity,
  private val fragment: Fragment,
  private val profileManagementController: ProfileManagementController,
  private val topicController: TopicController,
  private val topicListController: TopicListController,
  private val logger: ConsoleLogger,
  @StoryHtmlParserEntityType private val entityType: String
) : ViewModel() {
  /** [internalProfileId] needs to be set before any of the live data members can be accessed. */
  private var internalProfileId: Int = -1
  private lateinit var profileId: ProfileId
  private var limit: Int = 0

  private val headerViewModel = ProfileProgressHeaderViewModel(activity, fragment)

  val itemViewModelList: MutableList<ProfileProgressItemViewModel> = mutableListOf(
    headerViewModel as ProfileProgressItemViewModel
  )

  fun setProfileId(internalProfileId: Int) {
    profileId = ProfileId.newBuilder().setInternalId(internalProfileId).build()
    this.internalProfileId = internalProfileId

    subscribeToProfileLiveData()
    subscribeToCompletedStoryListLiveData()
    subscribeToOngoingTopicListLiveData()
  }

  private fun getProfileData(): LiveData<Profile> {
    return Transformations.map(
      profileManagementController.getProfile(profileId), ::processGetProfileResult
    )
  }

  private fun subscribeToProfileLiveData() {
    getProfileData().observe(
      fragment,
      Observer<Profile> {
        headerViewModel.setProfile(it)
      }
    )
  }

  private fun getStoryListResult(): LiveData<OngoingStoryList> {
    return Transformations.map(
      topicListController.getOngoingStoryList(profileId),
      ::processOngoingStoryResult
    )
  }

  private fun subscribeToOngoingStoryListResult() {
    getStoryListResult().observe(
      fragment,
      Observer {
        processOngoingStoryList(it)
      }
    )
  }

  private fun processGetProfileResult(profileResult: AsyncResult<Profile>): Profile {
    if (profileResult.isFailure()) {
      logger.e(
        "ProfileProgressFragment",
        "Failed to retrieve profile",
        profileResult.getErrorOrNull()!!
      )
    }
    return profileResult.getOrDefault(Profile.getDefaultInstance())
  }

  /**
   * Reprocesses the data of the [refreshedOngoingStoryListViewModelLiveData] so that we have the
   * correct number of items on configuration changes
   */
  fun handleOnConfigurationChange() {
    limit = fragment.resources.getInteger(R.integer.profile_progress_limit)
    subscribeToOngoingStoryListResult()
  }

  private fun processOngoingStoryResult(
    ongoingStoryListResult: AsyncResult<OngoingStoryList>
  ): OngoingStoryList {
    if (ongoingStoryListResult.isFailure()) {
      logger.e(
        "ProfileProgressFragment",
        "Failed to retrieve ongoing story list: ",
        ongoingStoryListResult.getErrorOrNull()!!
      )
    }
    return ongoingStoryListResult.getOrDefault(OngoingStoryList.getDefaultInstance())
  }

  private fun processOngoingStoryList(
    ongoingStoryList: OngoingStoryList
  ): List<ProfileProgressItemViewModel> {
    limit = fragment.resources.getInteger(R.integer.profile_progress_limit)
    val itemList = if (ongoingStoryList.recentStoryList.size > limit) {
      ongoingStoryList.recentStoryList.subList(0, limit)
    } else {
      ongoingStoryList.recentStoryList
    }
    itemViewModelList.clear()
    itemViewModelList.add(headerViewModel as ProfileProgressItemViewModel)
    itemViewModelList.addAll(
      itemList.map { story ->
        RecentlyPlayedStorySummaryViewModel(story, entityType)
      }
    )
    return itemViewModelList
  }

  private fun subscribeToCompletedStoryListLiveData() {
    getCompletedStoryListCount().observe(
      fragment,
      Observer<CompletedStoryList> {
        headerViewModel.setCompletedStoryCount(it.completedStoryCount)
      }
    )
  }

  private fun getCompletedStoryListCount(): LiveData<CompletedStoryList> {
    return Transformations.map(
      topicController.getCompletedStoryList(profileId),
      ::processGetCompletedStoryListResult
    )
  }

  private fun processGetCompletedStoryListResult(
    completedStoryListResult: AsyncResult<CompletedStoryList>
  ): CompletedStoryList {
    if (completedStoryListResult.isFailure()) {
      logger.e(
        "ProfileProgressFragment",
        "Failed to retrieve completed story list",
        completedStoryListResult.getErrorOrNull()!!
      )
    }
    return completedStoryListResult.getOrDefault(CompletedStoryList.getDefaultInstance())
  }

  private fun subscribeToOngoingTopicListLiveData() {
    getOngoingTopicListCount().observe(
      fragment,
      Observer<OngoingTopicList> {
        headerViewModel.setOngoingTopicCount(it.topicCount)
      }
    )
  }

  private fun getOngoingTopicListCount(): LiveData<OngoingTopicList> {
    return Transformations.map(
      topicController.getOngoingTopicList(profileId),
      ::processGetOngoingTopicListResult
    )
  }

  private fun processGetOngoingTopicListResult(
    ongoingTopicListResult: AsyncResult<OngoingTopicList>
  ): OngoingTopicList {
    if (ongoingTopicListResult.isFailure()) {
      logger.e(
        "ProfileProgressFragment",
        "Failed to retrieve ongoing topic list",
        ongoingTopicListResult.getErrorOrNull()!!
      )
    }
    return ongoingTopicListResult.getOrDefault(OngoingTopicList.getDefaultInstance())
  }
}
