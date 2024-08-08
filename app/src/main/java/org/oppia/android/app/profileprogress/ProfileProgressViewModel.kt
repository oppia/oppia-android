package org.oppia.android.app.profileprogress

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
import org.oppia.android.R
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.CompletedStoryList
import org.oppia.android.app.model.OngoingTopicList
import org.oppia.android.app.model.Profile
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.PromotedActivityList
import org.oppia.android.app.shim.IntentFactoryShim
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.app.viewmodel.ObservableViewModel
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.profile.ProfileManagementController
import org.oppia.android.domain.topic.TopicController
import org.oppia.android.domain.topic.TopicListController
import org.oppia.android.domain.translation.TranslationController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import org.oppia.android.util.parser.html.StoryHtmlParserEntityType
import javax.inject.Inject

/** The [ObservableViewModel] for [ProfileProgressFragment]. */
@FragmentScope
class ProfileProgressViewModel @Inject constructor(
  private val activity: AppCompatActivity,
  private val fragment: Fragment,
  private val intentFactoryShim: IntentFactoryShim,
  private val profileManagementController: ProfileManagementController,
  private val topicController: TopicController,
  private val topicListController: TopicListController,
  private val oppiaLogger: OppiaLogger,
  @StoryHtmlParserEntityType private val entityType: String,
  private val resourceHandler: AppLanguageResourceHandler,
  private val translationController: TranslationController
) {
  /** [internalProfileId] needs to be set before any of the live data members can be accessed. */
  private var internalProfileId: Int = -1
  private lateinit var profileId: ProfileId
  private var limit: Int = 0

  private val headerViewModel = ProfileProgressHeaderViewModel(activity, fragment)

  private val itemViewModelList: MutableList<ProfileProgressItemViewModel> = mutableListOf(
    headerViewModel as ProfileProgressItemViewModel
  )

  fun setProfileId(internalProfileId: Int) {
    profileId = ProfileId.newBuilder().setLoggedInInternalProfileId(internalProfileId).build()
    this.internalProfileId = internalProfileId

    subscribeToProfileLiveData()
    subscribeToCompletedStoryListLiveData()
    subscribeToOngoingTopicListLiveData()
  }

  private fun getProfileData(): LiveData<Profile> {
    return Transformations.map(
      profileManagementController.getProfile(profileId).toLiveData(), ::processGetProfileResult
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

  private fun processGetProfileResult(profileResult: AsyncResult<Profile>): Profile {
    return when (profileResult) {
      is AsyncResult.Failure -> {
        oppiaLogger.e("ProfileProgressFragment", "Failed to retrieve profile", profileResult.error)
        Profile.getDefaultInstance()
      }
      is AsyncResult.Pending -> Profile.getDefaultInstance()
      is AsyncResult.Success -> profileResult.value
    }
  }

  private val promotedActivityListResultLiveData:
    LiveData<AsyncResult<PromotedActivityList>> by lazy {
      topicListController.getPromotedActivityList(profileId).toLiveData()
    }

  private val promotedActivityListLiveData: LiveData<PromotedActivityList> by lazy {
    Transformations.map(
      promotedActivityListResultLiveData,
      ::processPromotedActivityListResult
    )
  }

  var refreshedPromotedActivityListViewModelLiveData =
    MutableLiveData<List<ProfileProgressItemViewModel>>()

  /**
   * Reprocesses the data of the [refreshedPromotedActivityListViewModelLiveData] so that we have the
   * correct number of items on configuration changes
   */
  fun handleOnConfigurationChange() {
    limit = fragment.resources.getInteger(R.integer.profile_progress_limit)
    refreshedPromotedActivityListViewModelLiveData =
      Transformations.map(
      promotedActivityListLiveData,
      ::processPromotedActivityList
    ) as MutableLiveData
  }

  private fun processPromotedActivityListResult(
    promotedActivityListResult: AsyncResult<PromotedActivityList>
  ): PromotedActivityList {
    return when (promotedActivityListResult) {
      is AsyncResult.Failure -> {
        oppiaLogger.e(
          "ProfileProgressFragment",
          "Failed to retrieve promoted story list: ",
          promotedActivityListResult.error
        )
        PromotedActivityList.getDefaultInstance()
      }
      is AsyncResult.Pending -> PromotedActivityList.getDefaultInstance()
      is AsyncResult.Success -> promotedActivityListResult.value
    }
  }

  private fun processPromotedActivityList(
    recommendedActivityList: PromotedActivityList
  ): List<ProfileProgressItemViewModel> {
    with(recommendedActivityList.promotedStoryList) {
      headerViewModel.setRecentlyPlayedStoryCount(recentlyPlayedStoryList.size)
      limit = fragment.resources.getInteger(R.integer.profile_progress_limit)
      val itemList =
        if (recentlyPlayedStoryList.size > limit) {
          recentlyPlayedStoryList.subList(0, limit)
        } else {
          recentlyPlayedStoryList
        }
      itemViewModelList.clear()
      itemViewModelList.add(headerViewModel as ProfileProgressItemViewModel)
      itemViewModelList.addAll(
        itemList.map { story ->
          RecentlyPlayedStorySummaryViewModel(
            activity,
            internalProfileId,
            story,
            entityType,
            intentFactoryShim,
            resourceHandler,
            translationController
          )
        }
      )
      return itemViewModelList
    }
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
      topicController.getCompletedStoryList(profileId).toLiveData(),
      ::processGetCompletedStoryListResult
    )
  }

  private fun processGetCompletedStoryListResult(
    completedStoryListResult: AsyncResult<CompletedStoryList>
  ): CompletedStoryList {
    return when (completedStoryListResult) {
      is AsyncResult.Failure -> {
        oppiaLogger.e(
          "ProfileProgressFragment",
          "Failed to retrieve completed story list",
          completedStoryListResult.error
        )
        CompletedStoryList.getDefaultInstance()
      }
      is AsyncResult.Pending -> CompletedStoryList.getDefaultInstance()
      is AsyncResult.Success -> completedStoryListResult.value
    }
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
      topicController.getOngoingTopicList(profileId).toLiveData(),
      ::processGetOngoingTopicListResult
    )
  }

  private fun processGetOngoingTopicListResult(
    ongoingTopicListResult: AsyncResult<OngoingTopicList>
  ): OngoingTopicList {
    return when (ongoingTopicListResult) {
      is AsyncResult.Failure -> {
        oppiaLogger.e(
          "ProfileProgressFragment",
          "Failed to retrieve ongoing topic list",
          ongoingTopicListResult.error
        )
        OngoingTopicList.getDefaultInstance()
      }
      is AsyncResult.Pending -> OngoingTopicList.getDefaultInstance()
      is AsyncResult.Success -> ongoingTopicListResult.value
    }
  }
}
