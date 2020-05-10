package org.oppia.app.home

import android.content.res.Configuration
import android.content.res.Resources
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
import org.oppia.app.drawer.KEY_NAVIGATION_PROFILE_ID
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
import org.oppia.domain.profile.ProfileManagementController
import org.oppia.domain.topic.TopicListController
import org.oppia.util.data.AsyncResult
import org.oppia.util.datetime.DateTimeUtil
import org.oppia.util.logging.Logger
import org.oppia.util.system.OppiaClock
import javax.inject.Inject

/** The presenter for [HomeFragment]. */
@FragmentScope
class HomeFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val fragment: Fragment,
  private val profileManagementController: ProfileManagementController,
  private val topicListController: TopicListController,
  private val oppiaClock: OppiaClock,
  private val logger: Logger
) {
  private val routeToTopicListener = activity as RouteToTopicListener
  private val itemList: MutableList<HomeItemViewModel> = ArrayList()
  private val promotedStoryList: MutableList<PromotedStoryViewModel> = ArrayList()
  private lateinit var welcomeViewModel: WelcomeViewModel
  private lateinit var promotedStoryListViewModel: PromotedStoryListViewModel
  private lateinit var allTopicsViewModel: AllTopicsViewModel
  private lateinit var topicListAdapter: TopicListAdapter
  private lateinit var binding: HomeFragmentBinding
  private var internalProfileId: Int = -1
  private lateinit var profileId: ProfileId
  private lateinit var profileName: String

  private val orientation = Resources.getSystem().configuration.orientation

  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
    binding = HomeFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)
    // NB: Both the view model and lifecycle owner must be set in order to correctly bind LiveData elements to
    // data-bound view models.

    internalProfileId = activity.intent.getIntExtra(KEY_NAVIGATION_PROFILE_ID, -1)
    profileId = ProfileId.newBuilder().setInternalId(internalProfileId).build()

    welcomeViewModel = WelcomeViewModel()
    promotedStoryListViewModel = PromotedStoryListViewModel(activity, internalProfileId)
    allTopicsViewModel = AllTopicsViewModel()
    itemList.add(welcomeViewModel)
    itemList.add(promotedStoryListViewModel)
    itemList.add(allTopicsViewModel)
    topicListAdapter = TopicListAdapter(activity, itemList, promotedStoryList)

    val spanCount = if (orientation == Configuration.ORIENTATION_PORTRAIT) {
      2
    } else {
      3
    }

    val homeLayoutManager = GridLayoutManager(activity.applicationContext, spanCount)
    homeLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
      override fun getSpanSize(position: Int): Int {
        return if (position == 0 || position == 1 || position == 2) {
          /* number of spaces this item should occupy = */ spanCount
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

    subscribeToProfileLiveData()
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

  private fun setProfileName() {
    if (::welcomeViewModel.isInitialized && ::profileName.isInitialized) {
      welcomeViewModel.profileName.set(profileName)
      welcomeViewModel.greeting.set(DateTimeUtil(fragment.requireContext(), oppiaClock).getGreetingMessage())
    }
  }

  private val ongoingStoryListSummaryResultLiveData: LiveData<AsyncResult<OngoingStoryList>> by lazy {
    topicListController.getOngoingStoryList(profileId)
  }

  private fun subscribeToOngoingStoryList() {
    getAssumedSuccessfulOngoingStoryList().observe(fragment, Observer<OngoingStoryList> {
      it.recentStoryList.take(3).forEach { promotedStory ->
        val recentStory = PromotedStoryViewModel(activity, internalProfileId)
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
    routeToTopicListener.routeToTopic(internalProfileId, topicSummary.topicId)
  }
}
