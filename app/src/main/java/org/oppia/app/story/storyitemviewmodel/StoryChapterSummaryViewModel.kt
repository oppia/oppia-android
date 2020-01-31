package org.oppia.app.story.storyitemviewmodel

import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import org.oppia.app.model.ChapterSummary
import org.oppia.app.model.LessonThumbnail
import org.oppia.app.story.ExplorationSelectionListener
import org.oppia.app.story.StoryFragment
import org.oppia.domain.exploration.ExplorationDataController
import org.oppia.util.data.AsyncResult
import org.oppia.util.logging.Logger

private const val STORY_VIEWER_TAG = "StoryViewer"

/** Chapter summary view model for the recycler view in [StoryFragment]. */
class StoryChapterSummaryViewModel(
  val index: Int,
  private val fragment: Fragment,
  private val explorationSelectionListener: ExplorationSelectionListener,
  private val explorationDataController: ExplorationDataController,
  private val logger: Logger,
  val chapterSummary: ChapterSummary
) : StoryItemViewModel() {
  val explorationId: String = chapterSummary.explorationId
  val name: String = chapterSummary.name
  val summary: String = chapterSummary.summary
  val chapterThumbnail: LessonThumbnail = chapterSummary.chapterThumbnail

  fun onExplorationClicked() {
    explorationDataController.stopPlayingExploration()
    explorationDataController.startPlayingExploration(
      explorationId
    ).observe(fragment, Observer<AsyncResult<Any?>> { result ->
      when {
        result.isPending() -> logger.d(STORY_VIEWER_TAG, "Loading exploration")
        result.isFailure() -> logger.e(STORY_VIEWER_TAG, "Failed to load exploration", result.getErrorOrNull()!!)
        else -> {
          logger.d(STORY_VIEWER_TAG, "Successfully loaded exploration: $explorationId")
          explorationSelectionListener.selectExploration(explorationId)
        }
      }
    })
  }
}
