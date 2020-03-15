package org.oppia.app.completedstorylist

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.model.CompletedStoryList
import org.oppia.app.model.ProfileId
import org.oppia.domain.topic.TopicController
import org.oppia.util.data.AsyncResult
import org.oppia.util.logging.Logger
import javax.inject.Inject

/** The ViewModel for [CompletedStoryListFragment]. */
@FragmentScope
class CompletedStoryListViewModel @Inject constructor(
  private val topicController: TopicController,
  private val logger: Logger
) : ViewModel() {
  /** [internalProfileId] needs to be set before any of the live data members can be accessed. */
  private var internalProfileId: Int = -1

  private val storyResultLiveData: LiveData<AsyncResult<CompletedStoryList>> by lazy {
    topicController.getCompletedStoryList(ProfileId.newBuilder().setInternalId(internalProfileId).build())
  }

  private val storyLiveData: LiveData<CompletedStoryList> by lazy {
    Transformations.map(storyResultLiveData, ::processStoryResult)
  }

  val completedStoryListLiveData: LiveData<List<CompletedStoryItemViewModel>> by lazy {
    Transformations.map(storyLiveData, ::processCompletedStoryList)
  }

  fun setProfileId(internalProfileId: Int) {
    this.internalProfileId = internalProfileId
  }

  private fun processStoryResult(storyResult: AsyncResult<CompletedStoryList>): CompletedStoryList {
    if (storyResult.isFailure()) {
      logger.e("CompletedStoryListFragment", "Failed to retrieve Story: ", storyResult.getErrorOrNull()!!)
    }

    return storyResult.getOrDefault(CompletedStoryList.getDefaultInstance())
  }

  private fun processCompletedStoryList(completedStoryList: CompletedStoryList): List<CompletedStoryItemViewModel> {
    val itemViewModelList: MutableList<CompletedStoryItemViewModel> = mutableListOf()
    itemViewModelList.addAll(completedStoryList.completedStoryList.map { completedStory ->
      CompletedStoryItemViewModel(completedStory)
    })
    return itemViewModelList
  }
}
