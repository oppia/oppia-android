package org.oppia.app.story

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.model.ChapterPlayState
import org.oppia.app.model.ChapterSummary
import org.oppia.app.model.StorySummary
import org.oppia.domain.topic.TopicController
import org.oppia.util.data.AsyncResult
import org.oppia.util.logging.Logger
import javax.inject.Inject

/** The ViewModel for [StoryFragment]. */
@FragmentScope
class StoryViewModel @Inject constructor(
  private val topicController: TopicController,
  private val logger: Logger
) : ViewModel() {
  private lateinit var storyId: String

  val storyLiveData: LiveData<StorySummary> by lazy {
    Transformations.map(storyResultLiveData, ::processStoryResult)
  }

  val storyNameLiveData: LiveData<String> by lazy {
    Transformations.map(storyResultLiveData, ::processStoryName)
  }

  val storyChapterLiveData: LiveData<List<ChapterSummary>> by lazy {
    Transformations.map(storyResultLiveData, ::processStoryChapterList)
  }

  val storyChaptersCompletedCountLiveData: LiveData<Int> by lazy {
    Transformations.map(storyResultLiveData, ::processStoryChaptersCompleted)
  }

  private val storyResultLiveData: LiveData<AsyncResult<StorySummary>> by lazy {
    topicController.getStory(storyId)
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

  private fun processStoryName(storyResult: AsyncResult<StorySummary>): String {
    if (storyResult.isFailure()) {
      logger.e("StoryFragment", "Failed to retrieve Story: ", storyResult.getErrorOrNull()!!)
    }

    return storyResult.getOrDefault(StorySummary.getDefaultInstance()).storyName
  }

  private fun processStoryChapterList(storyResult: AsyncResult<StorySummary>): List<ChapterSummary> {
    if (storyResult.isFailure()) {
      logger.e("StoryFragment", "Failed to retrieve Story: ", storyResult.getErrorOrNull()!!)
    }

    return storyResult.getOrDefault(StorySummary.getDefaultInstance()).chapterList
  }

  private fun processStoryChaptersCompleted(storyResult: AsyncResult<StorySummary>): Int {
    if (storyResult.isFailure()) {
      logger.e("StoryFragment", "Failed to retrieve Story: ", storyResult.getErrorOrNull()!!)
    }

    val chapterList = storyResult.getOrDefault(StorySummary.getDefaultInstance()).chapterList

    return chapterList.filter { chapter -> chapter.chapterPlayState == ChapterPlayState.COMPLETED }
      .size
  }
}
