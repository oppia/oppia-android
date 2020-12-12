package org.oppia.android.app.home

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.home.topiclist.TopicSummaryClickListener
import org.oppia.android.app.home.topiclist.TopicSummaryViewModel
import org.oppia.android.app.model.Profile
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.TopicList
import org.oppia.android.app.viewmodel.ObservableViewModel
import org.oppia.android.app.viewmodel.ViewModelProvider
import org.oppia.android.domain.profile.ProfileManagementController
import org.oppia.android.domain.topic.TopicListController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import org.oppia.android.util.datetime.DateTimeUtil
import org.oppia.android.util.logging.ConsoleLogger
import org.oppia.android.util.parser.TopicHtmlParserEntityType
import org.oppia.android.util.system.OppiaClock
import javax.inject.Inject

@FragmentScope
class HomeViewModel @Inject constructor(
  private val activity: AppCompatActivity,
  private val fragment: Fragment,
  private val oppiaClock: OppiaClock,
  private val logger: ConsoleLogger,
  private val profileId: ProfileId,
  private val profileManagementController: ProfileManagementController,
  private val topicListController: TopicListController,
  @TopicHtmlParserEntityType private val topicEntityType: String
  ) : ObservableViewModel() {

  val itemListLiveData by lazy {

  }

  private val profileResultLiveData: LiveData<AsyncResult<Profile>> by lazy {
    profileManagementController.getProfile(profileId).toLiveData()
  }

  private val welcomeLiveData: LiveData<MutableList<HomeItemViewModel>> by lazy {
    Transformations.map(profileResultLiveData, ::processGetProfileResult)
  }

  private fun processGetProfileResult(profileResult: AsyncResult<Profile>)
  : MutableList<HomeItemViewModel> {
    if (profileResult.isFailure()) {
      logger.e("HomeFragment", "Failed to retrieve profile", profileResult.getErrorOrNull()!!)
    }
    val profile = profileResult.getOrDefault(Profile.getDefaultInstance())
    val welcome = WelcomeViewModel(
      fragment,
      oppiaClock
    )
    welcome.profileName.set(profile.name)
    return mutableListOf(welcome)
  }

  private val topicListSummaryResultLiveData: LiveData<AsyncResult<TopicList>> by lazy {
    topicListController.getTopicList()
  }
  private val topicListLiveData: LiveData<MutableList<HomeItemViewModel>> by lazy {
    Transformations.map(assumedSuccessfulTopicListLiveData, ::processTopicList)
  }

  private fun processTopicList(itemsList: TopicList) : MutableList<HomeItemViewModel> {
    var list : MutableList<HomeItemViewModel> = ArrayList()
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
    return list
  }

  private val assumedSuccessfulTopicListLiveData: LiveData<TopicList> by lazy {
    // If there's an error loading the data, assume the default.
    Transformations.map(topicListSummaryResultLiveData) {
      it.getOrDefault(TopicList.getDefaultInstance())
    }
  }
}
