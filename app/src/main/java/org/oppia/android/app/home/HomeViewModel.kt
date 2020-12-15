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

  private val profileId: ProfileId = ProfileId.newBuilder().setInternalId(internalProfileId).build()
  private val limit = activity.resources.getInteger(R.integer.promoted_story_list_limit)

  private val profileDataProvider: DataProvider<Profile> by lazy {
    profileManagementController.getProfile(profileId)
  }

  private val ongoingStoryListSummaryDataProvider: DataProvider<OngoingStoryList> by lazy {
    topicListController.getOngoingStoryList(profileId)
  }

  private val topicListSummaryDataProvider: DataProvider<TopicList> by lazy {
    // TODO: once #2253 is merged change this function call
    topicListController.getTopicList2()
  }

  private val homeItemViewModelListDataProvider: DataProvider<List<HomeItemViewModel>> by lazy {
    // This will block until all data providers return initial results (which may be default
    // instances). If any of the data providers are pending or failed, the combined result will also
    // be pending or failed.
    profileDataProvider.combineWith(
      ongoingStoryListSummaryDataProvider,
      PROFILE_AND_ONGOING_STORY_COMBINED_PROVIDER_ID
    ) { profile, ongoingStoryList ->
      listOfNotNull(
        computeWelcomeViewModel(profile),
        computePromotedStoryListViewModel(ongoingStoryList)
      )
    }.combineWith(
      topicListSummaryDataProvider,
      HOME_FRAGMENT_COMBINED_PROVIDER_ID
    ) { homeItemViewModelList, topicList ->
      homeItemViewModelList + listOf(AllTopicsViewModel()) + computeTopicSummaryItemViewModelList(
        topicList
      )
    }
  }

  // Resulting LiveData to bind to the outer RecyclerView & that contains all ViewModels.
  val homeItemViewModelListLiveData: LiveData<List<HomeItemViewModel>> by lazy {
    Transformations.map(homeItemViewModelListDataProvider.toLiveData()) { itemListResult ->
      if (itemListResult.isFailure()) {
        logger.e(
          "HomeFragment",
          "Failed to retrieve fragment",
          itemListResult.getErrorOrNull()
        )
      }
      return@map itemListResult.getOrDefault(listOf())
    }
  }

  private fun computeWelcomeViewModel(profile: Profile): HomeItemViewModel? {
    return if (profile.name.isNotEmpty()) {
      WelcomeViewModel(fragment, oppiaClock, profile.name)
    } else null
  }

  private fun computePromotedStoryListViewModel(
    ongoingStoryList: OngoingStoryList
  ): HomeItemViewModel? {
    val storyViewModelList = computePromotedStoryViewModelList(ongoingStoryList)
    return if (storyViewModelList.isNotEmpty()) {
      return PromotedStoryListViewModel(
        activity,
        internalProfileId,
        intentFactoryShim,
        storyViewModelList
      )
    } else null
  }

  private fun computePromotedStoryViewModelList(
    ongoingStoryList: OngoingStoryList
  ): List<PromotedStoryViewModel> {
    if (ongoingStoryList.recentStoryCount != 0) {
      return ongoingStoryList.recentStoryList.take(limit)
        .map { promotedStory ->
          PromotedStoryViewModel(
            activity,
            internalProfileId,
            intentFactoryShim,
            ongoingStoryList.recentStoryCount,
            storyEntityType,
            promotedStory
          )
        }
    } else {
      // TODO(#936): Optimise this as part of recommended stories.
      return ongoingStoryList.olderStoryList.take(limit)
        .map { promotedStory ->
          PromotedStoryViewModel(
            activity,
            internalProfileId,
            intentFactoryShim,
            ongoingStoryList.olderStoryCount,
            storyEntityType,
            promotedStory
          )
        }
    }
  }

  private fun computeTopicSummaryItemViewModelList(topicList: TopicList): List<HomeItemViewModel> {
    return topicList.topicSummaryList.mapIndexed { topicIndex, topicSummary ->
      TopicSummaryViewModel(
        activity,
        topicSummary,
        topicEntityType,
        fragment as TopicSummaryClickListener,
        position = topicIndex
      )
    }
  }
}
