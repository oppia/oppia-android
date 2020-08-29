package org.oppia.app.ongoingtopiclist

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.model.OngoingTopicList
import org.oppia.app.model.ProfileId
import org.oppia.app.shim.IntentFactoryShim
import org.oppia.app.viewmodel.ObservableViewModel
import org.oppia.domain.topic.TopicController
import org.oppia.util.data.AsyncResult
import org.oppia.util.logging.ConsoleLogger
import org.oppia.util.parser.TopicHtmlParserEntityType
import javax.inject.Inject

/** The ViewModel for [OngoingTopicListFragment]. */
@FragmentScope
class OngoingTopicListViewModel @Inject constructor(
  private val activity: AppCompatActivity,
  private val topicController: TopicController,
  private val logger: ConsoleLogger,
  private val intentFactoryShim: IntentFactoryShim,
  @TopicHtmlParserEntityType private val entityType: String
) : ObservableViewModel() {
  /** [internalProfileId] needs to be set before any of the live data members can be accessed. */
  private var internalProfileId: Int = -1

  private val ongoingTopicListResultLiveData: LiveData<AsyncResult<OngoingTopicList>> by lazy {
    topicController.getOngoingTopicList(
      ProfileId.newBuilder().setInternalId(internalProfileId).build()
    )
  }

  private val ongoingTopicListLiveData: LiveData<OngoingTopicList> by lazy {
    Transformations.map(ongoingTopicListResultLiveData, ::processOngoingTopicResult)
  }

  val ongoingTopicListViewModelLiveData: LiveData<List<OngoingTopicItemViewModel>> by lazy {
    Transformations.map(ongoingTopicListLiveData, ::processOngoingTopicList)
  }

  fun setProfileId(internalProfileId: Int) {
    this.internalProfileId = internalProfileId
  }

  private fun processOngoingTopicResult(
    ongoingTopicListResult: AsyncResult<OngoingTopicList>
  ): OngoingTopicList {
    if (ongoingTopicListResult.isFailure()) {
      logger.e(
        "OngoingTopicListFragment",
        "Failed to retrieve OngoingTopicList: ",
        ongoingTopicListResult.getErrorOrNull()!!
      )
    }
    return ongoingTopicListResult.getOrDefault(OngoingTopicList.getDefaultInstance())
  }

  private fun processOngoingTopicList(
    ongoingTopicList: OngoingTopicList
  ): List<OngoingTopicItemViewModel> {
    val itemViewModelList: MutableList<OngoingTopicItemViewModel> = mutableListOf()
    itemViewModelList.addAll(
      ongoingTopicList.topicList.map { topic ->
        OngoingTopicItemViewModel(activity, internalProfileId, topic, entityType, intentFactoryShim)
      }
    )
    return itemViewModelList
  }
}
