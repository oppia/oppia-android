package org.oppia.app.story

import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.model.ChapterPlayState
import org.oppia.app.model.ChapterSummary
import org.oppia.app.model.StorySummary
import org.oppia.app.story.storyitemviewmodel.StoryChapterSummaryViewModel
import org.oppia.app.story.storyitemviewmodel.StoryHeaderViewModel
import org.oppia.app.story.storyitemviewmodel.StoryItemViewModel
import org.oppia.domain.topic.TopicController
import org.oppia.util.data.AsyncResult
import org.oppia.util.logging.Logger
import javax.inject.Inject

/** The ViewModel for [StoryFragment]. */
@FragmentScope
class StoryViewModel @Inject constructor(
  fragment: Fragment,
  private val topicController: TopicController,
  private val logger: Logger
) : ViewModel() {
  /** [storyId] needs to be set before any of the live data members can be accessed. */
  private lateinit var storyId: String
  private val explorationSelectionListener = fragment as ExplorationSelectionListener

  private val storyResultLiveData: LiveData<AsyncResult<StorySummary>> by lazy {
    topicController.getStory(storyId)
  }

  private val storyLiveData: LiveData<StorySummary> by lazy {
    Transformations.map(storyResultLiveData, ::processStoryResult)
  }

  val storyNameLiveData: LiveData<String> by lazy {
    Transformations.map(storyLiveData, StorySummary::getStoryName)
  }

  val storyChapterLiveData: LiveData<List<StoryItemViewModel>> by lazy {
    Transformations.map(storyLiveData, ::processStoryChapterList)
  }

  fun setStoryId(storyId: String) {
    this.storyId = storyId
  }

  private fun processStoryResult(storyResult: AsyncResult<StorySummary>): StorySummary {
    if (storyResult.isFailure()) {
      logger.e("StoryFragment", "Failed to retrieve Story: ", storyResult.getErrorOrNull()!!)
    }

    return storyResult.getOrDefault(StorySummary.getDefaultInstance())
  }

  private fun processStoryChapterList(storySummary: StorySummary): List<StoryItemViewModel> {
    val chapterList: List<ChapterSummary> = storySummary.chapterList
    val completedCount =
      chapterList.filter { chapter -> chapter.chapterPlayState == ChapterPlayState.COMPLETED }.size

    // List with only the header
    val itemViewModelList: MutableList<StoryItemViewModel> = mutableListOf(
      StoryHeaderViewModel(completedCount, chapterList.size) as StoryItemViewModel
    )

    // Add the rest of the list
    itemViewModelList.addAll(chapterList.mapIndexed { index, chapter ->
      StoryChapterSummaryViewModel(index, explorationSelectionListener, chapter) as StoryItemViewModel
    })

    return itemViewModelList
  }
}
