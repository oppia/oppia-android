package org.oppia.android.app.ongoingtopiclist

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
<<<<<<< HEAD:app/src/main/java/org/oppia/android/app/ongoingtopiclist/OngoingTopicListViewModel.kt
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.OngoingTopicList
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.shim.IntentFactoryShim
import org.oppia.android.app.viewmodel.ObservableViewModel
import org.oppia.android.domain.topic.TopicController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.logging.ConsoleLogger
import org.oppia.android.util.parser.TopicHtmlParserEntityType
=======
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.model.OngoingTopicList
import org.oppia.app.model.ProfileId
import org.oppia.app.shim.IntentFactoryShim
import org.oppia.app.viewmodel.ObservableViewModel
import org.oppia.domain.topic.TopicController
import org.oppia.util.data.AsyncResult
import org.oppia.util.data.DataProviders.Companion.toLiveData
import org.oppia.util.logging.ConsoleLogger
import org.oppia.util.parser.TopicHtmlParserEntityType
>>>>>>> develop:app/src/main/java/org/oppia/app/ongoingtopiclist/OngoingTopicListViewModel.kt
import javax.inject.Inject

/** The ObservableViewModel for [OngoingTopicListFragment]. */
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
    ).toLiveData()
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
