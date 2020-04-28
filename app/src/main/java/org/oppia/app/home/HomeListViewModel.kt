package org.oppia.app.home

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import org.oppia.app.home.topiclist.AllTopicsViewModel
import org.oppia.app.home.topiclist.PromotedStoryListViewModel
import org.oppia.app.home.topiclist.PromotedStoryViewModel
import org.oppia.app.home.topiclist.TopicSummaryClickListener
import org.oppia.app.home.topiclist.TopicSummaryViewModel
import org.oppia.app.model.OngoingStoryList
import org.oppia.app.model.Profile
import org.oppia.app.model.ProfileId
import org.oppia.app.model.PromotedStory
import org.oppia.app.model.TopicList
import org.oppia.app.profileprogress.ProfileProgressItemViewModel
import org.oppia.app.profileprogress.RecentlyPlayedStorySummaryViewModel
import org.oppia.domain.profile.ProfileManagementController
import org.oppia.domain.topic.TopicController
import org.oppia.domain.topic.TopicListController
import org.oppia.util.data.AsyncResult
import org.oppia.util.datetime.DateTimeUtil
import org.oppia.util.logging.Logger
import org.oppia.util.system.OppiaClock
import javax.inject.Inject

/** [ViewModel] for HomeFragment.*/

class HomeListViewModel @Inject constructor(
  private val activity: AppCompatActivity,
  private val fragment: Fragment,
  private val profileManagementController: ProfileManagementController,
  private val oppiaClock: OppiaClock,
  private val topicListController: TopicListController,
  private val logger: Logger
) : ViewModel() {

  /** [internalProfileId] needs to be set before any of the live data members can be accessed. */
  private var internalProfileId: Int = -1
  private lateinit var profileId: ProfileId

  private lateinit var profileName: String

  private lateinit var welcomeViewModel: WelcomeViewModel
  private lateinit var allTopicsViewModel: AllTopicsViewModel
  private var itemViewModelList: MutableList<HomeItemViewModel> = mutableListOf()
  private val promotedStoryList: MutableList<PromotedStoryViewModel> = ArrayList()

  fun setProfileId(internalProfileId: Int) {
    profileId = ProfileId.newBuilder().setInternalId(internalProfileId).build()
    this.internalProfileId = internalProfileId

    subscribeToProfileLiveData()

    welcomeViewModel = WelcomeViewModel()
    allTopicsViewModel = AllTopicsViewModel()
    itemViewModelList.add(welcomeViewModel as HomeItemViewModel)
  }

  private val profileLiveData: LiveData<Profile> by lazy {
    getProfileData()
  }

  private fun getProfileData(): LiveData<Profile> {
    return Transformations.map(profileManagementController.getProfile(profileId), ::processGetProfileResult)
  }

  private fun subscribeToProfileLiveData() {
    profileLiveData.observe(activity, Observer<Profile> { result ->
      profileName = result.name.toString()
      setProfileName()
    })
  }

  private fun setProfileName() {
    if (::welcomeViewModel.isInitialized && ::profileName.isInitialized) {
      welcomeViewModel.profileName.set(profileName)
      welcomeViewModel.greeting.set(DateTimeUtil(fragment.requireContext(), oppiaClock).getGreetingMessage())
    }
  }

  private fun processGetProfileResult(profileResult: AsyncResult<Profile>): Profile {
    if (profileResult.isFailure()) {
      logger.e("HomeFragment", "Failed to retrieve profile", profileResult.getErrorOrNull()!!)
    }
    return profileResult.getOrDefault(Profile.getDefaultInstance())
  }

  private val ongoingStoryListResultLiveData: LiveData<AsyncResult<OngoingStoryList>> by lazy {
    topicListController.getOngoingStoryList(profileId)
  }

  private val ongoingStoryListLiveData: LiveData<OngoingStoryList> by lazy {
    Transformations.map(ongoingStoryListResultLiveData, ::processOngoingStoryResult)
  }

  val ongoingStoryListViewModelLiveData: LiveData<List<HomeItemViewModel>> by lazy {
    Transformations.map(ongoingStoryListLiveData, ::processOngoingStoryList)
  }

  private fun processOngoingStoryResult(ongoingStoryListResult: AsyncResult<OngoingStoryList>): OngoingStoryList {
    if (ongoingStoryListResult.isFailure()) {
      logger.e(
        "HomeFragment",
        "Failed to retrieve ongoing story list: ",
        ongoingStoryListResult.getErrorOrNull()!!
      )
    }
    return ongoingStoryListResult.getOrDefault(OngoingStoryList.getDefaultInstance())
  }

  private fun processOngoingStoryList(ongoingStoryList: OngoingStoryList): List<HomeItemViewModel> {
    val itemList = if (ongoingStoryList.recentStoryList.size > 3) {
      ongoingStoryList.recentStoryList.subList(0, 2)
    } else {
      ongoingStoryList.recentStoryList
    }
    itemViewModelList.addAll(itemList.map { story ->
      val recentStory = PromotedStoryViewModel(story, activity, internalProfileId)
      recentStory.setPromotedStory(story)
      promotedStoryList.add(recentStory)
      PromotedStoryViewModel(story,activity,internalProfileId) as HomeItemViewModel
    })

    itemViewModelList.add(allTopicsViewModel as HomeItemViewModel)
    subscribeToTopicList()
    return itemViewModelList
  }

  private fun subscribeToTopicList() {
    getAssumedSuccessfulTopicList().observe(fragment, Observer<TopicList> { result ->
      for (topicSummary in result.topicSummaryList) {
        val topicSummaryViewModel = TopicSummaryViewModel(topicSummary, fragment as TopicSummaryClickListener)
        itemViewModelList.add(topicSummaryViewModel)
      }
    })
  }

  private fun getAssumedSuccessfulTopicList(): LiveData<TopicList> {
    // If there's an error loading the data, assume the default.
    return Transformations.map(topicListSummaryResultLiveData) { it.getOrDefault(TopicList.getDefaultInstance()) }
  }

  private val topicListSummaryResultLiveData: LiveData<AsyncResult<TopicList>> by lazy {
    topicListController.getTopicList()
  }

  private val ongoingStoryListSummaryResultLiveData: LiveData<AsyncResult<OngoingStoryList>> by lazy {
    topicListController.getOngoingStoryList(profileId)
  }
}
