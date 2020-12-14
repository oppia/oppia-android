package org.oppia.android.app.home

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import org.oppia.android.R
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.home.topiclist.AllTopicsViewModel
import org.oppia.android.app.home.topiclist.PromotedStoryListViewModel
import org.oppia.android.app.home.topiclist.PromotedStoryViewModel
import org.oppia.android.app.home.topiclist.TopicSummaryClickListener
import org.oppia.android.app.home.topiclist.TopicSummaryViewModel
import org.oppia.android.app.model.OngoingStoryList
import org.oppia.android.app.model.Profile
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.TopicList
import org.oppia.android.app.shim.IntentFactoryShim
import org.oppia.android.app.viewmodel.ObservableViewModel
import org.oppia.android.domain.profile.ProfileManagementController
import org.oppia.android.domain.topic.TopicListController
import org.oppia.android.util.data.DataProvider
import org.oppia.android.util.data.DataProviders.Companion.combineWith
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import org.oppia.android.util.logging.ConsoleLogger
import org.oppia.android.util.parser.StoryHtmlParserEntityType
import org.oppia.android.util.parser.TopicHtmlParserEntityType
import org.oppia.android.util.system.OppiaClock
import javax.inject.Inject

private const val PROFILE_AND_ONGOING_STORY_COMBINED_PROVIDER_ID = "profile+ongoingStoryList"
private const val HOME_FRAGMENT_COMBINED_PROVIDER_ID = "profile+ongoingStoryList+topicListProvider"

/** [ViewModel] for layouts in home fragment . */
@FragmentScope
class HomeViewModel @Inject constructor(
  private val activity: AppCompatActivity,
  private val fragment: Fragment,
  private val oppiaClock: OppiaClock,
  private val logger: ConsoleLogger,
  private val internalProfileId: Int,
  private val intentFactoryShim: IntentFactoryShim,
  private val profileManagementController: ProfileManagementController,
  private val topicListController: TopicListController,
  @TopicHtmlParserEntityType private val topicEntityType: String,
  @StoryHtmlParserEntityType private val storyEntityType: String
  ) : ObservableViewModel() {

  private val profileId : ProfileId = ProfileId.newBuilder().setInternalId(internalProfileId).build()
  private val limit = activity.resources.getInteger(R.integer.promoted_story_list_limit)

  private val profileDataProvider: DataProvider<Profile> by lazy {
    profileManagementController.getProfile(profileId)
  }

  private val ongoingStoryListSummaryDataProvider: DataProvider<OngoingStoryList>
    by lazy {
      topicListController.getOngoingStoryList(profileId)
    }

  private val topicListSummaryDataProvider: DataProvider<TopicList> by lazy {
    topicListController.getTopicList()
  }

  private val combinedHomeFragmentModelDataProvider: DataProvider<HomeFragmentModel> by lazy {
    // This will block until all data providers return initial results (which may be default
    // instances). If any of the data providers are pending or failed, the combined result will also
    // be pending or failed.
    profileDataProvider.combineWith(
      ongoingStoryListSummaryDataProvider,
      PROFILE_AND_ONGOING_STORY_COMBINED_PROVIDER_ID) { profile, ongoingStoryList ->
      HomeFragmentModel(profile, ongoingStoryList)
    }.combineWith(
      topicListSummaryDataProvider,
      HOME_FRAGMENT_COMBINED_PROVIDER_ID) { combinedModel, topicList ->
      HomeFragmentModel(combinedModel.profile, combinedModel.ongoingStoryList, topicList)
    }
  }

  // Resulting LiveData to bind to the outer RecyclerView & that contains all ViewModels.
  val combinedHomeViewModelListLiveData: LiveData<List<HomeItemViewModel>> by lazy {
    Transformations.map(combinedHomeFragmentModelDataProvider.toLiveData()) { combinedModelResult ->
      if (combinedModelResult.isFailure()) {
        logger.e("HomeFragment",
        "Failed to retrieve fragment",
          combinedModelResult.getErrorOrNull()
        )
      }
      val combinedModel = combinedModelResult.getOrDefault(HomeFragmentModel())
      // Convert combinedModel into a list of view models for the home screen (proto defaults
      // should result in no view models for corresponding sections so that they don't appear).
      // This can usually be done by checking that a constituent list is empty.
      var itemList : List<HomeItemViewModel> = listOf(
        getProfileData(combinedModel.profile) as HomeItemViewModel,
        getPromotedStoryListData(combinedModel.ongoingStoryList) as HomeItemViewModel,
        AllTopicsViewModel() as HomeItemViewModel,
        getTopicListData(combinedModel.topicList) as HomeItemViewModel
      )
      return@map itemList
    }
  }

  private fun getProfileData(profile: Profile) : WelcomeViewModel {
    val welcomeViewModel = WelcomeViewModel(fragment, oppiaClock)
    if (profile.isInitialized) {
      welcomeViewModel.profileName.set(profile.name)
    } else {
      logger.i("HomeFragment",
        "Failed to retrieve profile"
      )
      welcomeViewModel.profileName.set(Profile.getDefaultInstance().name)
    }
    return welcomeViewModel
  }

  private fun getPromotedStoryListData(ongoingStoryList: OngoingStoryList) : PromotedStoryListViewModel {
    val promotedStoryListViewModel = PromotedStoryListViewModel(
      activity,
      internalProfileId,
      intentFactoryShim)
    if (ongoingStoryList.isInitialized) {
      promotedStoryListViewModel.setPromotedStories(processPromotedStories(ongoingStoryList))
    } else {
      logger.i("HomeFragment",
        "Failed to retrieve promoted stories"
      )
      promotedStoryListViewModel.setPromotedStories(
        processPromotedStories(OngoingStoryList.getDefaultInstance()))
    }
    return promotedStoryListViewModel
  }

  private fun processPromotedStories(ongoingStoryList: OngoingStoryList) : List<PromotedStoryViewModel> {
    var promotedStoryList: MutableList<PromotedStoryViewModel> = ArrayList()
    if (ongoingStoryList.recentStoryCount != 0) {
      ongoingStoryList.recentStoryList.take(limit)
        .forEach { promotedStory ->
          val recentStory = PromotedStoryViewModel(
            activity,
            internalProfileId,
            storyEntityType,
            intentFactoryShim
          )
          recentStory.setPromotedStory(promotedStory)
          recentStory.setStoryCount(ongoingStoryList.recentStoryCount)
          promotedStoryList.add(recentStory)
        }
    } else {
      // TODO(#936): Optimise this as part of recommended stories.
      ongoingStoryList.olderStoryList.take(limit)
        .forEach { promotedStory ->
          val oldStory = PromotedStoryViewModel(
            activity,
            internalProfileId,
            storyEntityType,
            intentFactoryShim
          )
          oldStory.setPromotedStory(promotedStory)
          oldStory.setStoryCount(ongoingStoryList.olderStoryCount)
          promotedStoryList.add(oldStory)
        }
    }
    return promotedStoryList
  }

  private fun getTopicListData(topicsList: TopicList) : List<TopicSummaryViewModel> {
    if (topicsList.isInitialized) {
      return processTopicsList(topicsList)
    } else {
      logger.i("HomeFragment",
        "Failed to retrieve topics list"
      )
      return processTopicsList(TopicList.getDefaultInstance())
    }
  }

  private fun processTopicsList(topicsList: TopicList) : List<TopicSummaryViewModel> {
    var list: MutableList<TopicSummaryViewModel> = ArrayList()
    for (topicSummary in topicsList.topicSummaryList) {
      val topicSummaryViewModel =
        TopicSummaryViewModel(
          activity,
          topicSummary,
          topicEntityType,
          fragment as TopicSummaryClickListener
        )
      topicSummaryViewModel.setPosition(1 + topicsList.topicSummaryList.indexOf(topicSummary))
      list.add(topicSummaryViewModel)
    }
    return list
  }

  // Model to display combined fragment data from all view models. Initialize to the default values
  // so that fragment can display on initial load until data-fetching from each Data Provider
  // finishes.
  private data class HomeFragmentModel(
    val profile: Profile = Profile.getDefaultInstance(),
    val ongoingStoryList: OngoingStoryList = OngoingStoryList.getDefaultInstance(),
    val topicList: TopicList = TopicList.getDefaultInstance()
  )
}
