package org.oppia.app.story

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.model.ChapterSummary
import org.oppia.app.model.StorySummary
import org.oppia.domain.topic.TopicController
import org.oppia.util.data.AsyncResult
import org.oppia.util.logging.Logger
import javax.inject.Inject

@FragmentScope
class StoryViewModel @Inject constructor(
  private val topicController: TopicController,
  private val logger: Logger
) : ViewModel() {
  private lateinit var storyId: String

  val storyLiveData: LiveData<StorySummary> by lazy {
    processStoryLiveData()
  }

  val storyNameLiveData: LiveData<String> by lazy {
    processStoryNameLiveData()
  }

  val storyChapterLiveData: LiveData<List<ChapterSummary>> by lazy {
    processStoryChapterLiveData()
  }

  private val storyResultLiveData: LiveData<AsyncResult<StorySummary>> by lazy {
    topicController.getStory(storyId)
  }

  fun setStoryId(storyId: String) {
    this.storyId = storyId
  }

  private fun processStoryLiveData(): LiveData<StorySummary> {
    return Transformations.map(storyResultLiveData, ::processStoryResult)
  }

  private fun processStoryNameLiveData(): LiveData<String> {
    return Transformations.map(storyResultLiveData, ::processStoryName)
  }

  private fun processStoryChapterLiveData(): LiveData<List<ChapterSummary>> {
    return Transformations.map(storyResultLiveData, ::processStoryChapterList)
  }

  private fun processStoryResult(storyResult: AsyncResult<StorySummary>): StorySummary {
    if (storyResult.isFailure()) {
      logger.e("StoryFragment", "Failed to retrieve Story: " + storyResult.getErrorOrNull())
    }

    return storyResult.getOrDefault(StorySummary.getDefaultInstance())
  }

  private fun processStoryName(storyResult: AsyncResult<StorySummary>): String {
    if (storyResult.isFailure()) {
      logger.e("StoryFragment", "Failed to retrieve Story: " + storyResult.getErrorOrNull())
    }

    return storyResult.getOrDefault(StorySummary.getDefaultInstance()).storyName
  }

  private fun processStoryChapterList(storyResult: AsyncResult<StorySummary>): List<ChapterSummary> {
    if (storyResult.isFailure()) {
      logger.e("StoryFragment", "Failed to retrieve Story: " + storyResult.getErrorOrNull())
    }

    return storyResult.getOrDefault(StorySummary.getDefaultInstance()).chapterList
  }
}
