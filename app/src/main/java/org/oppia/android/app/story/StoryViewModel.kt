package org.oppia.android.app.story

import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.ChapterPlayState
import org.oppia.android.app.model.ChapterSummary
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.StorySummary
import org.oppia.android.app.story.storyitemviewmodel.StoryChapterSummaryViewModel
import org.oppia.android.app.story.storyitemviewmodel.StoryHeaderViewModel
import org.oppia.android.app.story.storyitemviewmodel.StoryItemViewModel
import org.oppia.android.domain.exploration.ExplorationDataController
import org.oppia.android.domain.topic.TopicController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.logging.ConsoleLogger
import org.oppia.android.util.parser.StoryHtmlParserEntityType
import javax.inject.Inject

/** The ViewModel for StoryFragment. */
@FragmentScope
class StoryViewModel @Inject constructor(
  private val fragment: Fragment,
  private val topicController: TopicController,
  private val explorationDataController: ExplorationDataController,
  private val logger: ConsoleLogger,
  @StoryHtmlParserEntityType val entityType: String
) {
  private var internalProfileId: Int = -1
  private lateinit var topicId: String

  /** [storyId] needs to be set before any of the live data members can be accessed. */
  private lateinit var storyId: String
  private val explorationSelectionListener = fragment as ExplorationSelectionListener

  private val storyResultLiveData: LiveData<AsyncResult<StorySummary>> by lazy {
    topicController.getStory(
      ProfileId.newBuilder().setInternalId(internalProfileId).build(),
      topicId,
      storyId
    )
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

  fun setInternalProfileId(internalProfileId: Int) {
    this.internalProfileId = internalProfileId
  }

  fun setTopicId(topicId: String) {
    this.topicId = topicId
  }

  fun setStoryId(storyId: String) {
    this.storyId = storyId
  }

  private fun processStoryResult(storyResult: AsyncResult<StorySummary>): StorySummary {
    if (storyResult.isFailure()) {
      logger.e(
        "StoryFragment",
        "Failed to retrieve Story: ",
        storyResult.getErrorOrNull()!!
      )
    }

    return storyResult.getOrDefault(StorySummary.getDefaultInstance())
  }

  private fun processStoryChapterList(storySummary: StorySummary): List<StoryItemViewModel> {
    val chapterList: List<ChapterSummary> = storySummary.chapterList
    for (position in chapterList.indices) {
      if (storySummary.chapterList[position].chapterPlayState == ChapterPlayState.NOT_STARTED) {
        (fragment as StoryFragmentScroller).smoothScrollToPosition(position + 1)
        break
      }
    }

    val completedCount =
      chapterList.filter { chapter -> chapter.chapterPlayState == ChapterPlayState.COMPLETED }.size

    // List with only the header
    val itemViewModelList: MutableList<StoryItemViewModel> = mutableListOf(
      StoryHeaderViewModel(completedCount, chapterList.size) as StoryItemViewModel
    )

    // Add the rest of the list
    itemViewModelList.addAll(
      chapterList.mapIndexed { index, chapter ->
        StoryChapterSummaryViewModel(
          index,
          fragment,
          explorationSelectionListener,
          explorationDataController,
          logger,
          internalProfileId,
          topicId,
          storyId,
          chapter,
          entityType
        )
      }
    )

    return itemViewModelList
  }
}
