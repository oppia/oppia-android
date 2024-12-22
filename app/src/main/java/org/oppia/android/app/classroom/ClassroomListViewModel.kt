package org.oppia.android.app.classroom

import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableField
import androidx.databinding.ObservableList
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import org.oppia.android.R
import org.oppia.android.app.home.HomeItemViewModel
import org.oppia.android.app.home.WelcomeViewModel
import org.oppia.android.app.home.classroomlist.AllClassroomsViewModel
import org.oppia.android.app.home.classroomlist.ClassroomSummaryClickListener
import org.oppia.android.app.home.classroomlist.ClassroomSummaryViewModel
import org.oppia.android.app.home.promotedlist.ComingSoonTopicListViewModel
import org.oppia.android.app.home.promotedlist.ComingSoonTopicsViewModel
import org.oppia.android.app.home.promotedlist.PromotedStoryListViewModel
import org.oppia.android.app.home.promotedlist.PromotedStoryViewModel
import org.oppia.android.app.home.topiclist.AllTopicsViewModel
import org.oppia.android.app.home.topiclist.TopicSummaryClickListener
import org.oppia.android.app.home.topiclist.TopicSummaryViewModel
import org.oppia.android.app.model.ClassroomList
import org.oppia.android.app.model.ComingSoonTopicList
import org.oppia.android.app.model.Profile
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.PromotedActivityList
import org.oppia.android.app.model.PromotedStoryList
import org.oppia.android.app.model.TopicList
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.app.utility.datetime.DateTimeUtil
import org.oppia.android.app.viewmodel.ObservableViewModel
import org.oppia.android.domain.classroom.ClassroomController
import org.oppia.android.domain.classroom.TEST_CLASSROOM_ID_0
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.profile.ProfileManagementController
import org.oppia.android.domain.topic.TopicListController
import org.oppia.android.domain.translation.TranslationController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProvider
import org.oppia.android.util.data.DataProviders.Companion.combineWith
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import org.oppia.android.util.parser.html.StoryHtmlParserEntityType
import org.oppia.android.util.parser.html.TopicHtmlParserEntityType

private const val PROFILE_AND_PROMOTED_ACTIVITY_COMBINED_PROVIDER_ID =
  "profile+promotedActivityList"
private const val CLASSROOM_LIST_FRAGMENT_COMBINED_PROVIDER_ID =
  "profile+promotedActivityList+classroomListProvider"

/** [ViewModel] for layouts in classroom list fragment. */
class ClassroomListViewModel(
  private val activity: AppCompatActivity,
  private val fragment: Fragment,
  private val oppiaLogger: OppiaLogger,
  private val profileId: ProfileId,
  private val profileManagementController: ProfileManagementController,
  private val topicListController: TopicListController,
  private val classroomController: ClassroomController,
  @TopicHtmlParserEntityType private val topicEntityType: String,
  @StoryHtmlParserEntityType private val storyEntityType: String,
  private val resourceHandler: AppLanguageResourceHandler,
  private val dateTimeUtil: DateTimeUtil,
  private val translationController: TranslationController
) : ObservableViewModel() {

  private val promotedStoryListLimit = activity.resources.getInteger(
    R.integer.promoted_story_list_limit
  )

  /** An observable boolean property indicating the visibility state of the progress bar. */
  val isProgressBarVisible = ObservableField(true)

  /** An observable field to store the currently selected classroom ID. */
  val selectedClassroomId = ObservableField("")

  private val profileDataProvider: DataProvider<Profile> by lazy {
    profileManagementController.getProfile(profileId)
  }

  private val promotedActivityListSummaryDataProvider: DataProvider<PromotedActivityList> by lazy {
    topicListController.getPromotedActivityList(profileId)
  }

  private val classroomSummaryListDataProvider: DataProvider<ClassroomList> by lazy {
    classroomController.getClassroomList(profileId)
  }

  /**
   * An observable list containing the topic list of the selected classroom. The topic list updates
   * when a new classroom is selected by the user.
   */
  val topicList: ObservableList<HomeItemViewModel> by lazy {
    fetchAndUpdateTopicList() // Fetch the topic list of the last selected classroom.
    ObservableArrayList() // Initialize the topic list.
  }

  private val homeItemViewModelListDataProvider: DataProvider<List<HomeItemViewModel>> by lazy {
    // This will block until all data providers return initial results (which may be default
    // instances). If any of the data providers are pending or failed, the combined result will also
    // be pending or failed.
    profileDataProvider.combineWith(
      promotedActivityListSummaryDataProvider,
      PROFILE_AND_PROMOTED_ACTIVITY_COMBINED_PROVIDER_ID
    ) { profile, promotedActivityList ->
      if (profile.numberOfLogins > 1) {
        listOfNotNull(
          computeWelcomeViewModel(profile),
          computePromotedActivityListViewModel(promotedActivityList)
        )
      } else {
        listOfNotNull(computeWelcomeViewModel(profile))
      }
    }.combineWith(
      classroomSummaryListDataProvider,
      CLASSROOM_LIST_FRAGMENT_COMBINED_PROVIDER_ID
    ) { homeItemViewModelList, classroomSummaryList ->
      homeItemViewModelList + computeClassroomItemViewModelList(classroomSummaryList)
    }
  }

  /**
   * [LiveData] of the list of items displayed in the [ClassroomListFragment] RecyclerView.
   * The list backing this live data will automatically update if constituent parts of the UI
   * change (e.g. if the promoted story list changes). If an error occurs or data providers are
   * still pending, the list is empty and so the view shown will be empty.
   */
  val homeItemViewModelListLiveData: LiveData<List<HomeItemViewModel>> by lazy {
    Transformations.map(homeItemViewModelListDataProvider.toLiveData()) { itemListResult ->
      return@map when (itemListResult) {
        is AsyncResult.Failure -> {
          oppiaLogger.e(
            "ClassroomListFragment",
            "No classroom list fragment available -- failed to retrieve fragment data.",
            itemListResult.error
          )
          listOf()
        }
        is AsyncResult.Pending -> listOf()
        is AsyncResult.Success -> {
          isProgressBarVisible.set(false)
          itemListResult.value
        }
      }
    }
  }

  /**
   * Returns a [HomeItemViewModel] corresponding to the welcome message (see [WelcomeViewModel]), or null if
   * the specified profile has insufficient information to show the welcome message.
   */
  private fun computeWelcomeViewModel(profile: Profile): HomeItemViewModel? {
    return if (profile.name.isNotEmpty()) {
      WelcomeViewModel(profile.name, resourceHandler, dateTimeUtil)
    } else null
  }

  /**
   * Returns a [HomeItemViewModel] corresponding to the promoted stories(Recommended, Recently-played and
   * Last-played stories)[PromotedStoryListViewModel] and Upcoming topics [ComingSoonTopicListViewModel]
   * to be displayed for this learner or null if this profile does not have any promoted stories.
   * Promoted stories are determined by any recent stories last-played stories or suggested stories started by this profile.
   */
  private fun computePromotedActivityListViewModel(
    promotedActivityList: PromotedActivityList
  ): HomeItemViewModel? {
    when (promotedActivityList.recommendationTypeCase) {
      PromotedActivityList.RecommendationTypeCase.PROMOTED_STORY_LIST -> {
        val storyViewModelList = computePromotedStoryViewModelList(
          promotedActivityList.promotedStoryList
        )
        return if (storyViewModelList.isNotEmpty()) {
          return PromotedStoryListViewModel(
            activity,
            storyViewModelList,
            promotedActivityList,
            resourceHandler
          )
        } else null
      }
      PromotedActivityList.RecommendationTypeCase.COMING_SOON_TOPIC_LIST -> {
        val comingSoonTopicsList = computeComingSoonTopicViewModelList(
          promotedActivityList.comingSoonTopicList
        )
        return if (comingSoonTopicsList.isNotEmpty()) {
          return ComingSoonTopicListViewModel(
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
    promotedStoryList: PromotedStoryList
  ): List<PromotedStoryViewModel> {
    with(promotedStoryList) {
      val storyList = when {
        suggestedStoryList.isNotEmpty() -> {
          if (recentlyPlayedStoryList.isNotEmpty() || olderPlayedStoryList.isNotEmpty()) {
            recentlyPlayedStoryList +
              olderPlayedStoryList +
              suggestedStoryList
          } else {
            suggestedStoryList
          }
        }
        recentlyPlayedStoryList.isNotEmpty() -> {
          recentlyPlayedStoryList
        }
        else -> {
          olderPlayedStoryList
        }
      }

      // Check if at least one story in topic is completed. Prioritize recommended story over
      // completed story topic.
      val sortedStoryList = storyList.sortedByDescending { !it.isTopicLearned }
      return sortedStoryList.take(promotedStoryListLimit)
        .mapIndexed { index, promotedStory ->
          PromotedStoryViewModel(
            activity,
            profileId.internalId,
            sortedStoryList.size,
            storyEntityType,
            promotedStory,
            translationController,
            index
          )
        }
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
    return comingSoonTopicList.upcomingTopicList.map { topicSummary ->
      ComingSoonTopicsViewModel(
        activity,
        topicSummary,
        topicEntityType,
        comingSoonTopicList,
        translationController
      )
    }
  }

  /**
   * Returns a list of [HomeItemViewModel]s corresponding to all the classroom summaries available
   * and to be displayed on the [ClassroomListActivity] (see [ClassroomSummaryViewModel]). Returns
   * an empty list if there are no classrooms to display to the learner (caused by either
   * error or pending data providers).
   */
  private fun computeClassroomItemViewModelList(
    classroomList: ClassroomList
  ): List<HomeItemViewModel> {
    return listOf(AllClassroomsViewModel) +
      classroomList.classroomSummaryList.map { ephemeralClassroomSummary ->
        ClassroomSummaryViewModel(
          fragment as ClassroomSummaryClickListener,
          ephemeralClassroomSummary,
          translationController
        )
      }
  }

  /**
   * Returns a list of [HomeItemViewModel]s corresponding to all the lesson topics available and to
   * be displayed on the [ClassroomListActivity] (see [TopicSummaryViewModel]) along with
   * associated topics list header (see [AllTopicsViewModel]). Returns an empty list if there are
   * no topics to display to the learner (caused by either error or pending data providers).
   */
  private fun computeAllTopicsItemsViewModelList(
    topicList: TopicList
  ): List<HomeItemViewModel> {
    val allTopicsList = topicList.topicSummaryList.mapIndexed { topicIndex, ephemeralSummary ->
      TopicSummaryViewModel(
        activity,
        ephemeralSummary,
        topicEntityType,
        fragment as TopicSummaryClickListener,
        position = topicIndex,
        resourceHandler,
        translationController
      )
    }
    return if (allTopicsList.isNotEmpty()) {
      listOf(AllTopicsViewModel) + allTopicsList
    } else emptyList()
  }

  /** Fetches and updates the topic list based on the provided or last selected classroom ID. */
  fun fetchAndUpdateTopicList(classroomId: String = "") {
    if (classroomId.isBlank()) {
      // Retrieve the last selected classroom ID if no specific classroom ID is provided.
      profileManagementController.retrieveLastSelectedClassroomId(profileId)
        .toLiveData().observe(fragment) { lastSelectedClassroomIdResult ->
          when (lastSelectedClassroomIdResult) {
            is AsyncResult.Success -> {
              val lastSelectedClassroomId = lastSelectedClassroomIdResult.value
              updateTopicList(
                if (lastSelectedClassroomId.isNullOrBlank())
                  TEST_CLASSROOM_ID_0
                else
                  lastSelectedClassroomId
              )
            }
            is AsyncResult.Failure -> {
              oppiaLogger.e(
                "ClassroomListFragment",
                "Failed to retrieve last selected classroom ID",
                lastSelectedClassroomIdResult.error
              )
              // Use a default classroom ID in case of failure.
              updateTopicList(TEST_CLASSROOM_ID_0)
            }
            is AsyncResult.Pending -> {}
          }
        }
    } else {
      // Fetch the topic list using the provided classroom ID.
      updateTopicList(classroomId)
    }
  }

  private fun updateTopicList(classroomId: String) {
    selectedClassroomId.set(classroomId)
    classroomController.getTopicList(
      profileId,
      classroomId
    ).toLiveData().observe(fragment) { topicListResult ->
      when (topicListResult) {
        is AsyncResult.Success -> {
          topicList.clear()
          computeAllTopicsItemsViewModelList(topicListResult.value).map { itemViewModel ->
            topicList.add(itemViewModel)
          }
        }
        is AsyncResult.Failure -> {
          oppiaLogger.e(
            "ClassroomListFragment",
            "Failed to retrieve topic list.",
            topicListResult.error
          )
        }
        is AsyncResult.Pending -> {}
      }
    }
  }
}
