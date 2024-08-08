package org.oppia.android.app.ongoingtopiclist

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.OngoingTopicList
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.shim.IntentFactoryShim
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.app.viewmodel.ObservableViewModel
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.topic.TopicController
import org.oppia.android.domain.translation.TranslationController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import org.oppia.android.util.parser.html.TopicHtmlParserEntityType
import javax.inject.Inject

/** The ObservableViewModel for [OngoingTopicListFragment]. */
@FragmentScope
class OngoingTopicListViewModel @Inject constructor(
  private val activity: AppCompatActivity,
  private val topicController: TopicController,
  private val oppiaLogger: OppiaLogger,
  private val intentFactoryShim: IntentFactoryShim,
  @TopicHtmlParserEntityType private val entityType: String,
  private val resourceHandler: AppLanguageResourceHandler,
  private val translationController: TranslationController
) : ObservableViewModel() {
  /** [internalProfileId] needs to be set before any of the live data members can be accessed. */
  private var internalProfileId: Int = -1

  private val ongoingTopicListResultLiveData: LiveData<AsyncResult<OngoingTopicList>> by lazy {
    topicController.getOngoingTopicList(
      ProfileId.newBuilder().setLoggedInInternalProfileId(internalProfileId).build()
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
    return when (ongoingTopicListResult) {
      is AsyncResult.Failure -> {
        oppiaLogger.e(
          "OngoingTopicListFragment",
          "Failed to retrieve OngoingTopicList: ",
          ongoingTopicListResult.error
        )
        OngoingTopicList.getDefaultInstance()
      }
      is AsyncResult.Pending -> OngoingTopicList.getDefaultInstance()
      is AsyncResult.Success -> ongoingTopicListResult.value
    }
  }

  private fun processOngoingTopicList(
    ongoingTopicList: OngoingTopicList
  ): List<OngoingTopicItemViewModel> {
    val itemViewModelList: MutableList<OngoingTopicItemViewModel> = mutableListOf()
    itemViewModelList.addAll(
      ongoingTopicList.topicList.map { ephemeralTopic ->
        OngoingTopicItemViewModel(
          activity,
          internalProfileId,
          ephemeralTopic,
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
