package org.oppia.android.app.home

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import org.oppia.android.R
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.home.promotedlist.ComingSoonTopicListViewModel
import org.oppia.android.app.home.promotedlist.ComingSoonTopicsViewModel
import org.oppia.android.app.home.promotedlist.PromotedStoryListViewModel
import org.oppia.android.app.home.promotedlist.PromotedStoryViewModel
import org.oppia.android.app.home.topiclist.AllTopicsViewModel
import org.oppia.android.app.home.topiclist.TopicSummaryClickListener
import org.oppia.android.app.home.topiclist.TopicSummaryViewModel
import org.oppia.android.app.model.ComingSoonTopicList
import org.oppia.android.app.model.Profile
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.RecommendedActivityList
import org.oppia.android.app.model.RecommendedStoryList
import org.oppia.android.app.model.TopicList
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

private const val PROFILE_AND_Recommended_Activity_COMBINED_PROVIDER_ID =
  "profile+recommendedActivityList"
private const val HOME_FRAGMENT_COMBINED_PROVIDER_ID =
  "profile+recommendedActivityList+topicListProvider"

/** [ViewModel] for layouts in home fragment . */
@FragmentScope
class HomeViewModel(
  private val activity: AppCompatActivity,
  private val fragment: Fragment,
  private val oppiaClock: OppiaClock,
  private val logger: ConsoleLogger,
  private val internalProfileId: Int,
  private val profileManagementController: ProfileManagementController,
  private val topicListController: TopicListController,
  @TopicHtmlParserEntityType private val topicEntityType: String,
  @StoryHtmlParserEntityType private val storyEntityType: String
) : ObservableViewModel() {

  private val profileId: ProfileId = ProfileId.newBuilder().setInternalId(internalProfileId).build()
  private val promotedStoryListLimit = activity.resources.getInteger(
      R.integer.promoted_story_list_limit
    )

  private val profileDataProvider: DataProvider<Profile> by lazy {
    profileManagementController.getProfile(profileId)
  }

  private val ongoingStoryListSummaryDataProvider: DataProvider<RecommendedActivityList> by lazy {
    topicListController.getRecommendedActivityList(profileId)
  }

  private val topicListSummaryDataProvider: DataProvider<TopicList> by lazy {
    topicListController.getTopicList()
  }

  private val homeItemViewModelListDataProvider: DataProvider<List<HomeItemViewModel>> by lazy {
    // This will block until all data providers return initial results (which may be default
    // instances). If any of the data providers are pending or failed, the combined result will also
    // be pending or failed.
    profileDataProvider.combineWith(
      ongoingStoryListSummaryDataProvider,
      PROFILE_AND_Recommended_Activity_COMBINED_PROVIDER_ID
    ) { profile, recommendedActivityList ->
      listOfNotNull(
        computeWelcomeViewModel(profile),
        computeRecommendedActivityListViewModel(recommendedActivityList)
      )
    }.combineWith(
      topicListSummaryDataProvider,
      HOME_FRAGMENT_COMBINED_PROVIDER_ID
    ) { homeItemViewModelList, topicList ->
      homeItemViewModelList + computeAllTopicsItemsViewModelList(topicList)
    }
  }

  /**
   * [LiveData] of the list of items displayed in the HomeFragment RecyclerView. The list backing this live data will
   * automatically update if constituent parts of the UI change (e.g. if the promoted story list changes). If an error
   * occurs or data providers are still pending, the list is empty and so the view shown will be empty.
   */
  val homeItemViewModelListLiveData: LiveData<List<HomeItemViewModel>> by lazy {
    Transformations.map(homeItemViewModelListDataProvider.toLiveData()) { itemListResult ->
      if (itemListResult.isFailure()) {
        logger.e(
          "HomeFragment",
          "No home fragment available -- failed to retrieve home fragment information.",
          itemListResult.getErrorOrNull()
        )
      }
      return@map itemListResult.getOrDefault(listOf())
    }
  }

  /**
   * Returns a [HomeItemViewModel] corresponding to the welcome message (see [WelcomeViewModel]), or null if
   * the specified profile has insufficient information to show the welcome message.
   */
  private fun computeWelcomeViewModel(profile: Profile): HomeItemViewModel? {
    return if (profile.name.isNotEmpty()) {
      WelcomeViewModel(fragment, oppiaClock, profile.name)
    } else null
  }

  /**
   * Returns a [HomeItemViewModel] corresponding to the promoted stories[PromotedStoryListViewModel] and Upcoming topics
   * [ComingSoonTopicListViewModel]  to be displayed for this learner or null if this profile does not have any promoted stories.
   * Promoted stories are determined by any recent stories or suggested stories started by this profile.
   */
  private fun computeRecommendedActivityListViewModel(
    recommendedActivityList: RecommendedActivityList
  ): HomeItemViewModel? {
    when (recommendedActivityList.recommendationTypeCase) {
      RecommendedActivityList.RecommendationTypeCase.RECOMMENDED_STORY_LIST -> {
        val storyViewModelList = computePromotedStoryViewModelList(
          recommendedActivityList.recommendedStoryList
        )
        return if (storyViewModelList.isNotEmpty()) {
          return PromotedStoryListViewModel(
            activity,
            storyViewModelList,
            recommendedActivityList
          )
        } else null
      }
      RecommendedActivityList.RecommendationTypeCase.COMING_SOON_TOPIC_LIST -> {
        val comingSoonTopicsList = computeComingSoonTopicViewModelList(
          recommendedActivityList.comingSoonTopicList
        )
        return if (comingSoonTopicsList.isNotEmpty()) {
          return ComingSoonTopicListViewModel(
            activity,
            comingSoonTopicsList
          )
        } else null
      }
      else -> return null
    }
  }

  /**
   * Returns a list of [HomeItemViewModel]s corresponding to the the [PromotedStoryListViewModel] displayed
   * for this profile (see [PromotedStoryViewModel]), or an empty list if the profile does not have any
   * ongoing stories at all.
   */
  private fun computePromotedStoryViewModelList(
    recommendedStoryList: RecommendedStoryList
  ): List<PromotedStoryViewModel> {
    val storyList = when {
      recommendedStoryList.suggestedStoryCount != 0 -> {
        if (recommendedStoryList.recentlyPlayedStoryCount != 0 ||
          recommendedStoryList.olderPlayedStoryCount != 0
        ) {
          recommendedStoryList.recentlyPlayedStoryList +
            recommendedStoryList.olderPlayedStoryList +
            recommendedStoryList.suggestedStoryList
        }
        else {
          recommendedStoryList.suggestedStoryList
        }

      }
      recommendedStoryList.recentlyPlayedStoryCount != 0 -> {
        recommendedStoryList.recentlyPlayedStoryList
      }
      else -> {
        recommendedStoryList.olderPlayedStoryList
      }
    }
    return storyList.take(promotedStoryListLimit)
      .map { promotedStory ->
        PromotedStoryViewModel(
          activity,
          internalProfileId,
          storyList.size,
          storyEntityType,
          promotedStory
        )
      }
  }

  /**
   * Returns a list of [HomeItemViewModel]s corresponding to [ComingSoonTopicListViewModel]  all the upcoming topics available in future and to be
   * displayed for this profile (see [ComingSoonTopicsViewModel]), or an empty list if the profile does not have any
   * ongoing stories at all.
   */
  private fun computeComingSoonTopicViewModelList(
    comingSoonTopicList: ComingSoonTopicList
  ): List<ComingSoonTopicsViewModel> {
    return comingSoonTopicList.upcomingTopicList.mapIndexed { topicIndex, topicSummary ->
      ComingSoonTopicsViewModel(
        topicSummary,
        topicEntityType
      )
    }
  }

  /**
   * Returns a list of [HomeItemViewModel]s corresponding to all the lesson topics available and to be
   * displayed on the home activity (see [TopicSummaryViewModel]) along with associated topics list header (see
   * [AllTopicsViewModel]). Returns an empty list if there are no topics to display to the learner (caused by
   * either error or pending data providers).
   */
  private fun computeAllTopicsItemsViewModelList(
    topicList: TopicList
  ): List<HomeItemViewModel> {
    val allTopicsList = topicList.topicSummaryList.mapIndexed { topicIndex, topicSummary ->
      TopicSummaryViewModel(
        activity,
        topicSummary,
        topicEntityType,
        fragment as TopicSummaryClickListener,
        position = topicIndex
      )
    }
    return if (!allTopicsList.isEmpty()) {
      listOf(AllTopicsViewModel) + allTopicsList
    } else emptyList()
  }
}
