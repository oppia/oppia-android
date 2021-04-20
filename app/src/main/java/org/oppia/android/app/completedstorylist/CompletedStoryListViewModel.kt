package org.oppia.android.app.completedstorylist

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.CompletedStoryList
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.shim.IntentFactoryShim
import org.oppia.android.app.viewmodel.ObservableViewModel
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.topic.TopicController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import org.oppia.android.util.parser.StoryHtmlParserEntityType
import javax.inject.Inject

/** The ObservableViewModel for [CompletedStoryListFragment]. */
@FragmentScope
class CompletedStoryListViewModel @Inject constructor(
  private val activity: AppCompatActivity,
  private val intentFactoryShim: IntentFactoryShim,
  private val topicController: TopicController,
  private val oppiaLogger: OppiaLogger,
  @StoryHtmlParserEntityType private val entityType: String
) : ObservableViewModel() {
  /** [internalProfileId] needs to be set before any of the live data members can be accessed. */
  private var internalProfileId: Int = -1

  private val completedStoryListResultLiveData: LiveData<AsyncResult<CompletedStoryList>> by lazy {
    topicController.getCompletedStoryList(
      ProfileId.newBuilder().setInternalId(internalProfileId).build()
    ).toLiveData()
  }

  private val completedStoryLiveData: LiveData<CompletedStoryList> by lazy {
    Transformations.map(completedStoryListResultLiveData, ::processCompletedStoryListResult)
  }

  val completedStoryListLiveData: LiveData<List<CompletedStoryItemViewModel>> by lazy {
    Transformations.map(completedStoryLiveData, ::processCompletedStoryList)
  }

  fun setProfileId(internalProfileId: Int) {
    this.internalProfileId = internalProfileId
  }

  private fun processCompletedStoryListResult(
    completedStoryListResult: AsyncResult<CompletedStoryList>
  ): CompletedStoryList {
    if (completedStoryListResult.isFailure()) {
      oppiaLogger.e(
        "CompletedStoryListFragment",
        "Failed to retrieve CompletedStory list: ",
        completedStoryListResult.getErrorOrNull()!!
      )
    }
    return completedStoryListResult.getOrDefault(CompletedStoryList.getDefaultInstance())
  }

  private fun processCompletedStoryList(
    completedStoryList: CompletedStoryList
  ): List<CompletedStoryItemViewModel> {
    val itemViewModelList: MutableList<CompletedStoryItemViewModel> = mutableListOf()
    itemViewModelList.addAll(
      completedStoryList.completedStoryList.map { completedStory ->
        CompletedStoryItemViewModel(
          activity,
          internalProfileId,
          completedStory,
          entityType,
          intentFactoryShim
        )
      }
    )
    return itemViewModelList
  }
}
