package org.oppia.android.app.story.storyitemviewmodel

import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import org.oppia.android.app.model.ChapterPlayState
import org.oppia.android.app.model.ChapterSummary
import org.oppia.android.app.model.ExplorationCheckpoint
import org.oppia.android.app.model.LessonThumbnail
import org.oppia.android.app.story.ExplorationSelectionListener
import org.oppia.android.domain.exploration.ExplorationDataController
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.util.data.AsyncResult

private const val STORY_VIEWER_TAG = "StoryViewer"

/** Chapter summary view model for the recycler view in [StoryFragment]. */
class StoryChapterSummaryViewModel(
  val index: Int,
  private val fragment: Fragment,
  private val explorationSelectionListener: ExplorationSelectionListener,
  private val explorationDataController: ExplorationDataController,
  private val oppiaLogger: OppiaLogger,
  val internalProfileId: Int,
  val topicId: String,
  val storyId: String,
  val chapterSummary: ChapterSummary,
  val entityType: String
) : StoryItemViewModel() {
  // TODO(#3479): Enable checkpointing once mechanism to resume exploration with checkpoints is
  //  implemented.

  val explorationId: String = chapterSummary.explorationId
  val name: String = chapterSummary.name
  val summary: String = chapterSummary.summary
  val chapterThumbnail: LessonThumbnail = chapterSummary.chapterThumbnail
  val missingPrerequisiteChapter: ChapterSummary = chapterSummary.missingPrerequisiteChapter
  val chapterPlayState: ChapterPlayState = chapterSummary.chapterPlayState

  fun onExplorationClicked() {
    val shouldSavePartialProgress =
      when (chapterPlayState) {
        ChapterPlayState.IN_PROGRESS_SAVED, ChapterPlayState.IN_PROGRESS_NOT_SAVED,
        ChapterPlayState.STARTED_NOT_COMPLETED, ChapterPlayState.NOT_STARTED -> true
        else -> false
      }
    val canExplorationBeResumed =
      when (chapterPlayState) {
        ChapterPlayState.IN_PROGRESS_SAVED -> true
        else -> false
      }

    startOrResumeExploration(
      internalProfileId,
      topicId,
      storyId,
      explorationId,
      shouldSavePartialProgress,
      canExplorationBeResumed,
      backflowScreen = 1
    )
  }

  private fun startOrResumeExploration(
    internalProfileId: Int,
    topicId: String,
    storyId: String,
    explorationId: String,
    shouldSavePartialProgress: Boolean,
    canExplorationBeResumed: Boolean,
    backflowScreen: Int?
  ) {
    if (canExplorationBeResumed) {
      explorationSelectionListener.selectExploration(
        internalProfileId,
        topicId,
        storyId,
        explorationId,
        canExplorationBeResumed,
        shouldSavePartialProgress,
        backflowScreen
      )
    } else {
      playExploration(
        canExplorationBeResumed,
        shouldSavePartialProgress,
        backflowScreen
      )
    }
  }

  private fun playExploration(
    canExplorationBeResumed: Boolean,
    shouldSavePartialProgress: Boolean,
    backflowScreen: Int?
  ) {
    explorationDataController.stopPlayingExploration()
    explorationDataController.startPlayingExploration(
      internalProfileId,
      topicId,
      storyId,
      explorationId,
      shouldSavePartialProgress = shouldSavePartialProgress,
      // Pass an empty checkpoint if the exploration does not have to be resumed.
      ExplorationCheckpoint.getDefaultInstance()
    ).observe(
      fragment,
      Observer<AsyncResult<Any?>> { result ->
        when {
          result.isPending() -> oppiaLogger.d(STORY_VIEWER_TAG, "Loading exploration")
          result.isFailure() -> oppiaLogger.e(
            STORY_VIEWER_TAG,
            "Failed to load exploration",
            result.getErrorOrNull()!!
          )
          else -> {
            oppiaLogger.d(STORY_VIEWER_TAG, "Successfully loaded exploration: $explorationId")
            explorationSelectionListener.selectExploration(
              internalProfileId,
              topicId,
              storyId,
              explorationId,
              canExplorationBeResumed,
              shouldSavePartialProgress,
              backflowScreen
            )
          }
        }
      }
    )
  }
}
