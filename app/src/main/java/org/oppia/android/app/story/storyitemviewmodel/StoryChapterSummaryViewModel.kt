package org.oppia.android.app.story.storyitemviewmodel

import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import org.oppia.android.app.model.ChapterPlayState
import org.oppia.android.app.model.ChapterSummary
import org.oppia.android.app.model.LessonThumbnail
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.story.ExplorationSelectionListener
import org.oppia.android.domain.exploration.lightweightcheckpointing.ExplorationCheckpointController
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData

/** Chapter summary view model for the recycler view in [StoryFragment]. */
class StoryChapterSummaryViewModel(
  val index: Int,
  private val fragment: Fragment,
  private val explorationSelectionListener: ExplorationSelectionListener,
  val explorationCheckpointController: ExplorationCheckpointController,
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
    if (chapterPlayState == ChapterPlayState.IN_PROGRESS_SAVED) {
      val isCheckpointCompatible =
        explorationCheckpointController.isSavedCheckpointCompatibleWithExploration(
          ProfileId.getDefaultInstance(),
          explorationId
        ).toLiveData()

      isCheckpointCompatible.observe(
        fragment,
        object : Observer<AsyncResult<Boolean>> {
          override fun onChanged(it: AsyncResult<Boolean>?) {
            if (it != null) {
              if (it.isSuccess()) {
                isCheckpointCompatible.removeObserver(this)
                startOrResumeExploration(
                  internalProfileId,
                  topicId,
                  storyId,
                  explorationId,
                  shouldSavePartialProgress,
                  canExplorationBeResumed = it.getOrThrow(),
                  backflowScreen = 1
                )
              } else if (it.isFailure()) {
                isCheckpointCompatible.removeObserver(this)
                startOrResumeExploration(
                  internalProfileId,
                  topicId,
                  storyId,
                  explorationId,
                  shouldSavePartialProgress,
                  canExplorationBeResumed = false,
                  backflowScreen = 1
                )
              }
            }
          }
        }
      )
    } else {
      startOrResumeExploration(
        internalProfileId,
        topicId,
        storyId,
        explorationId,
        shouldSavePartialProgress,
        canExplorationBeResumed = false,
        backflowScreen = 1
      )
    }
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
