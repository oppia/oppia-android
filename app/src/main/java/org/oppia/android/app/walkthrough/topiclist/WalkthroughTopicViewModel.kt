package org.oppia.android.app.walkthrough.topiclist

import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import org.oppia.android.app.home.topiclist.TopicSummaryClickListener
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.TopicList
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.app.viewmodel.ObservableViewModel
import org.oppia.android.app.walkthrough.topiclist.topiclistviewmodel.WalkthroughTopicHeaderViewModel
import org.oppia.android.app.walkthrough.topiclist.topiclistviewmodel.WalkthroughTopicSummaryViewModel
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.topic.TopicListController
import org.oppia.android.domain.translation.TranslationController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import org.oppia.android.util.parser.html.TopicHtmlParserEntityType
import javax.inject.Inject

/** The ObservableViewModel for [WalkthroughTopicListFragment]. */
class WalkthroughTopicViewModel @Inject constructor(
  private val fragment: Fragment,
  private val topicListController: TopicListController,
  private val oppiaLogger: OppiaLogger,
  @TopicHtmlParserEntityType private val topicEntityType: String,
  private val resourceHandler: AppLanguageResourceHandler,
  private val translationController: TranslationController
) : ObservableViewModel() {
  private lateinit var profileId: ProfileId

  val walkthroughTopicViewModelLiveData: LiveData<List<WalkthroughTopicItemViewModel>> by lazy {
    Transformations.map(topicListSummaryLiveData, ::processCompletedTopicList)
  }

  private val topicListSummaryResultLiveData: LiveData<AsyncResult<TopicList>> by lazy {
    topicListController.getTopicList(profileId).toLiveData()
  }

  private val topicListSummaryLiveData: LiveData<TopicList> by lazy {
    Transformations.map(topicListSummaryResultLiveData, ::processTopicListResult)
  }

  /**
   * Initializes this view model with the specified [profileId].
   *
   * This MUST be called before the view model is interacted with.
   */
  fun initialize(profileId: ProfileId) {
    this.profileId = profileId
  }

  private fun processTopicListResult(topicSummaryListResult: AsyncResult<TopicList>): TopicList {
    return when (topicSummaryListResult) {
      is AsyncResult.Failure -> {
        oppiaLogger.e(
          "WalkthroughTopicSummaryListFragment",
          "Failed to retrieve TopicSummary list: ",
          topicSummaryListResult.error
        )
        TopicList.getDefaultInstance()
      }
      is AsyncResult.Pending -> TopicList.getDefaultInstance()
      is AsyncResult.Success -> topicSummaryListResult.value
    }
  }

  private fun processCompletedTopicList(topicList: TopicList): List<WalkthroughTopicItemViewModel> {
    // List with only the header
    val itemViewModelList: MutableList<WalkthroughTopicItemViewModel> = mutableListOf(
      WalkthroughTopicHeaderViewModel() as WalkthroughTopicItemViewModel
    )

    // Add the rest of the list
    itemViewModelList.addAll(
      topicList.topicSummaryList.map { ephemeralTopicSummary ->
        WalkthroughTopicSummaryViewModel(
          topicEntityType,
          ephemeralTopicSummary,
          fragment as TopicSummaryClickListener,
          resourceHandler,
          translationController
        )
      }
    )
    return itemViewModelList
  }
}
