package org.oppia.android.app.home

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.work.impl.utils.LiveDataUtils
import org.oppia.android.R
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.home.HomeItemViewModel
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
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import org.oppia.android.util.logging.ConsoleLogger
import org.oppia.android.util.parser.StoryHtmlParserEntityType
import org.oppia.android.util.parser.TopicHtmlParserEntityType
import org.oppia.android.util.system.OppiaClock
import javax.inject.Inject

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
  private lateinit var welcomeViewModel : WelcomeViewModel
  private lateinit var promotedStoryListViewModel : PromotedStoryListViewModel
  private val limit = activity.resources.getInteger(R.integer.promoted_story_list_limit)
  private val allTopicsViewModel = AllTopicsViewModel()
  private lateinit var topicSummaryViewModelList : List<TopicSummaryViewModel>

  val itemsList = listOf(
    welcomeViewModel,
    promotedStoryListViewModel,
    allTopicsViewModel,
    topicSummaryViewModelList
  )

  private val profileResultLiveData: LiveData<AsyncResult<Profile>> by lazy {
    profileManagementController.getProfile(profileId).toLiveData()
  }

  private val welcomeViewModelLiveData: LiveData<WelcomeViewModel> by lazy {
    Transformations.map(profileResultLiveData, ::processGetProfileResult)
  }

  private fun processGetProfileResult(profileResult: AsyncResult<Profile>) : WelcomeViewModel {
    if (profileResult.isFailure()) {
      logger.e("HomeFragment",
        "Failed to retrieve profile",
        profileResult.getErrorOrNull()!!)
    }
    val profile = profileResult.getOrDefault(Profile.getDefaultInstance())
    val welcome = WelcomeViewModel(
      fragment,
      oppiaClock
    )
    welcome.profileName.set(profile.name)
    welcomeViewModel = welcome
    return welcome
  }

  private val ongoingStoryListSummaryResultLiveData: LiveData<AsyncResult<OngoingStoryList>>
    by lazy {
      topicListController.getOngoingStoryList(profileId).toLiveData()
    }

  private val assumedSuccessfulOngoingStoryListLiveData: LiveData<OngoingStoryList> by lazy {
    // If there's an error loading the data, assume the default.
    Transformations.map(ongoingStoryListSummaryResultLiveData) {
      it.getOrDefault(
        OngoingStoryList.getDefaultInstance()
      )
    }
  }

  private val promotedStoryListViewModelLiveData: LiveData<PromotedStoryListViewModel> by lazy {
    Transformations.map(promotedStoryListLiveData, ::processPromotedStories)
  }

  private fun processPromotedStories(list: List<PromotedStoryViewModel>) : PromotedStoryListViewModel{
    promotedStoryListViewModel = PromotedStoryListViewModel(
      activity,
      internalProfileId,
      intentFactoryShim
    )
    promotedStoryListViewModel.setList(list)
    return promotedStoryListViewModel
  }

  val promotedStoryListLiveData: LiveData<List<PromotedStoryViewModel>> by lazy {
    Transformations.map(assumedSuccessfulOngoingStoryListLiveData, ::processOngoingStoryList)
  }

  private fun processOngoingStoryList(ongoingStoryList: OngoingStoryList) : List<PromotedStoryViewModel> {
    var newPromotedStoryList: MutableList<PromotedStoryViewModel> = ArrayList()
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
          newPromotedStoryList.add(recentStory)
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
          newPromotedStoryList.add(oldStory)
        }
    }
    return newPromotedStoryList
  }

  private val topicListSummaryResultLiveData: LiveData<AsyncResult<TopicList>> by lazy {
    topicListController.getTopicList()
  }
  private val topicListLiveData: LiveData<MutableList<TopicSummaryViewModel>> by lazy {
    Transformations.map(assumedSuccessfulTopicListLiveData, ::processTopicList)
  }

  private fun processTopicList(itemsList: TopicList) : MutableList<TopicSummaryViewModel> {
    var list : MutableList<TopicSummaryViewModel> = ArrayList()
    for (topicSummary in itemsList.topicSummaryList) {
      val topicSummaryViewModel =
        TopicSummaryViewModel(
          activity,
          topicSummary,
          topicEntityType,
          fragment as TopicSummaryClickListener
        )
      topicSummaryViewModel.setPosition(1 + itemsList.topicSummaryList.indexOf(topicSummary))
      list.add(topicSummaryViewModel)
    }
    topicSummaryViewModelList = list
    return list
  }

  private val assumedSuccessfulTopicListLiveData: LiveData<TopicList> by lazy {
    // If there's an error loading the data, assume the default.
    Transformations.map(topicListSummaryResultLiveData) {
      it.getOrDefault(TopicList.getDefaultInstance())
    }
  }
}
