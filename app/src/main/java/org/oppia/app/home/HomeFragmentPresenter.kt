package org.oppia.app.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
import androidx.recyclerview.widget.GridLayoutManager
import org.oppia.app.databinding.HomeFragmentBinding
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.home.topiclist.AllTopicsViewModel
import org.oppia.app.home.topiclist.PromotedStoryListViewModel
import org.oppia.app.home.topiclist.PromotedStoryViewModel
import org.oppia.app.home.topiclist.TopicListAdapter
import org.oppia.app.home.topiclist.TopicSummaryClickListener
import org.oppia.app.home.topiclist.TopicSummaryViewModel
import org.oppia.app.model.OngoingStoryList
import org.oppia.app.model.Profile
import org.oppia.app.model.ProfileId
import org.oppia.app.model.TopicList
import org.oppia.app.model.TopicSummary
import org.oppia.app.model.UserAppHistory
import org.oppia.domain.UserAppHistoryController
import org.oppia.domain.profile.ProfileManagementController
import org.oppia.domain.topic.TopicListController
import org.oppia.util.data.AsyncResult
import org.oppia.util.logging.Logger
import javax.inject.Inject

/** The presenter for [HomeFragment]. */
@FragmentScope
class HomeFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val fragment: Fragment,
  private val profileManagementController: ProfileManagementController,
  private val userAppHistoryController: UserAppHistoryController,
  private val topicListController: TopicListController,
  private val logger: Logger
) {
  private val routeToTopicListener = activity as RouteToTopicListener
  private val itemList: MutableList<HomeItemViewModel> = ArrayList()
  private val promotedStoryList: MutableList<PromotedStoryViewModel> = ArrayList()
  private lateinit var userAppHistoryViewModel: UserAppHistoryViewModel
  private lateinit var promotedStoryListViewModel: PromotedStoryListViewModel
  private lateinit var allTopicsViewModel: AllTopicsViewModel
  private lateinit var topicListAdapter: TopicListAdapter
  private lateinit var binding: HomeFragmentBinding
  private var internalProfileId: Int = -1
  private lateinit var profileId: ProfileId
  private lateinit var profileName: String

  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
    binding = HomeFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)
    // NB: Both the view model and lifecycle owner must be set in order to correctly bind LiveData elements to
    // data-bound view models.

    userAppHistoryViewModel = UserAppHistoryViewModel()
    promotedStoryListViewModel = PromotedStoryListViewModel(activity)
    allTopicsViewModel = AllTopicsViewModel()
    itemList.add(userAppHistoryViewModel)
    itemList.add(promotedStoryListViewModel)
    itemList.add(allTopicsViewModel)
    topicListAdapter = TopicListAdapter(activity, itemList, promotedStoryList)

    internalProfileId = activity.intent.getIntExtra(KEY_HOME_PROFILE_ID, -1)
    profileId = ProfileId.newBuilder().setInternalId(internalProfileId).build()

    val homeLayoutManager = GridLayoutManager(activity.applicationContext, 2)
    homeLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
      override fun getSpanSize(position: Int): Int {
        return if (position == 0 || position == 1 || position == 2) {
          /* number of spaces this item should occupy = */ 2
        } else {
          /* number of spaces this item should occupy = */ 1
        }
      }
    }

    binding.homeRecyclerView.apply {
      adapter = topicListAdapter
      // https://stackoverflow.com/a/32763434/32763621
      layoutManager = homeLayoutManager
    }
    binding.let {
      it.presenter = this
      it.lifecycleOwner = fragment
    }

    userAppHistoryController.markUserOpenedApp()
    subscribeToProfileLiveData()
    subscribeToUserAppHistory()
    subscribeToOngoingStoryList()
    subscribeToTopicList()
    return binding.root
  }

  private val profileLiveData: LiveData<Profile> by lazy {
    getProfileData()
  }

  private fun getProfileData(): LiveData<Profile> {
    return Transformations.map(profileManagementController.getProfile(profileId), ::processGetProfileResult)
  }

  private fun subscribeToProfileLiveData() {
    profileLiveData.observe(activity, Observer<Profile> { result ->
      profileName = result.name
      setProfileName()
    })
  }

  private fun processGetProfileResult(profileResult: AsyncResult<Profile>): Profile {
    if (profileResult.isFailure()) {
      logger.e("HomeFragment", "Failed to retrieve profile", profileResult.getErrorOrNull()!!)
    }
    return profileResult.getOrDefault(Profile.getDefaultInstance())
  }

  private val topicListSummaryResultLiveData: LiveData<AsyncResult<TopicList>> by lazy {
    topicListController.getTopicList()
  }

  private fun subscribeToTopicList() {
    getAssumedSuccessfulTopicList().observe(fragment, Observer<TopicList> { result ->
      for (topicSummary in result.topicSummaryList) {
        val topicSummaryViewModel = TopicSummaryViewModel(topicSummary, fragment as TopicSummaryClickListener)
        itemList.add(topicSummaryViewModel)
      }
      topicListAdapter.notifyDataSetChanged()
    })
  }

  private fun getAssumedSuccessfulTopicList(): LiveData<TopicList> {
    // If there's an error loading the data, assume the default.
    return Transformations.map(topicListSummaryResultLiveData) { it.getOrDefault(TopicList.getDefaultInstance()) }
  }

  private fun subscribeToUserAppHistory() {
    getUserAppHistory().observe(fragment, Observer<UserAppHistory> { result ->
      userAppHistoryViewModel = UserAppHistoryViewModel()
      userAppHistoryViewModel.setAlreadyAppOpened(result.alreadyOpenedApp)
      setProfileName()
      itemList[0] = userAppHistoryViewModel
      topicListAdapter.notifyItemChanged(0)
    })
  }

  private fun getUserAppHistory(): LiveData<UserAppHistory> {
    // If there's an error loading the data, assume the default.
    return Transformations.map(userAppHistoryController.getUserAppHistory(), ::processUserAppHistoryResult)
  }

  private fun processUserAppHistoryResult(appHistoryResult: AsyncResult<UserAppHistory>): UserAppHistory {
    if (appHistoryResult.isFailure()) {
      logger.e("HomeFragment", "Failed to retrieve user app history" + appHistoryResult.getErrorOrNull())
    }
    return appHistoryResult.getOrDefault(UserAppHistory.getDefaultInstance())
  }

  private fun setProfileName() {
    if (::userAppHistoryViewModel.isInitialized && ::profileName.isInitialized) {
      userAppHistoryViewModel.profileName = "$profileName!"
    }
  }

  private val ongoingStoryListSummaryResultLiveData: LiveData<AsyncResult<OngoingStoryList>> by lazy {
    topicListController.getOngoingStoryList()
  }

  private fun subscribeToOngoingStoryList() {
    getAssumedSuccessfulOngoingStoryList().observe(fragment, Observer<OngoingStoryList> {
      it.recentStoryList.take(3).forEach { promotedStory ->
        val recentStory = PromotedStoryViewModel(activity)
        recentStory.setPromotedStory(promotedStory)
        promotedStoryList.add(recentStory)
      }
      topicListAdapter.notifyItemChanged(1)
    })
  }

  private fun getAssumedSuccessfulOngoingStoryList(): LiveData<OngoingStoryList> {
    // If there's an error loading the data, assume the default.
    return Transformations.map(ongoingStoryListSummaryResultLiveData) { it.getOrDefault(OngoingStoryList.getDefaultInstance()) }
  }

  fun onTopicSummaryClicked(topicSummary: TopicSummary) {
    routeToTopicListener.routeToTopic(topicSummary.topicId)
  }
}
