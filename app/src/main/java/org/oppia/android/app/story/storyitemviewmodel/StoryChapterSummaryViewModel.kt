package org.oppia.android.app.story.storyitemviewmodel

import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import org.oppia.android.R
import org.oppia.android.app.model.ChapterPlayState
import org.oppia.android.app.model.EphemeralChapterSummary
import org.oppia.android.app.model.ExplorationActivityParams
import org.oppia.android.app.model.ExplorationCheckpoint
import org.oppia.android.app.model.LessonThumbnail
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.story.ExplorationSelectionListener
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.domain.exploration.lightweightcheckpointing.ExplorationCheckpointController
import org.oppia.android.domain.translation.TranslationController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData

/** Chapter summary view model for the recycler view in [StoryFragment]. */
class StoryChapterSummaryViewModel(
  val index: Int,
  val totalChapters: Int,
  private val fragment: Fragment,
  private val explorationSelectionListener: ExplorationSelectionListener,
  val explorationCheckpointController: ExplorationCheckpointController,
  val internalProfileId: Int,
  val classroomId: String,
  val topicId: String,
  val storyId: String,
  private val ephemeralChapterSummary: EphemeralChapterSummary,
  val entityType: String,
  private val resourceHandler: AppLanguageResourceHandler,
  private val translationController: TranslationController
) : StoryItemViewModel() {
  val chapterSummary = ephemeralChapterSummary.chapterSummary
  val explorationId: String = chapterSummary.explorationId
  val description: String by lazy {
    translationController.extractString(
      chapterSummary.description, ephemeralChapterSummary.writtenTranslationContext
    )
  }
  val chapterThumbnail: LessonThumbnail = chapterSummary.chapterThumbnail
  val missingPrerequisiteChapterTitle by lazy {
    translationController.extractString(
      ephemeralChapterSummary.missingPrerequisiteChapter.chapterSummary.title,
      ephemeralChapterSummary.missingPrerequisiteChapter.writtenTranslationContext
    )
  }
  val chapterPlayState: ChapterPlayState = chapterSummary.chapterPlayState
  private val profileId by lazy {
    ProfileId.newBuilder().apply { internalId = internalProfileId }.build()
  }

  fun onExplorationClicked() {
    val canHavePartialProgressSaved =
      when (chapterPlayState) {
        ChapterPlayState.IN_PROGRESS_SAVED, ChapterPlayState.IN_PROGRESS_NOT_SAVED,
        ChapterPlayState.STARTED_NOT_COMPLETED, ChapterPlayState.NOT_STARTED -> true
        ChapterPlayState.COMPLETION_STATUS_UNSPECIFIED,
        ChapterPlayState.NOT_PLAYABLE_MISSING_PREREQUISITES, ChapterPlayState.UNRECOGNIZED,
        ChapterPlayState.COMPLETED -> false
      }
    if (chapterPlayState == ChapterPlayState.IN_PROGRESS_SAVED) {
      val explorationCheckpointLiveData =
        explorationCheckpointController.retrieveExplorationCheckpoint(
          ProfileId.newBuilder().apply {
            internalId = internalProfileId
          }.build(),
          explorationId
        ).toLiveData()

      explorationCheckpointLiveData.observe(
        fragment,
        object : Observer<AsyncResult<ExplorationCheckpoint>> {
          override fun onChanged(it: AsyncResult<ExplorationCheckpoint>) {
            if (it is AsyncResult.Success) {
              explorationCheckpointLiveData.removeObserver(this)
              explorationSelectionListener.selectExploration(
                profileId,
                classroomId,
                topicId,
                storyId,
                explorationId,
                canExplorationBeResumed = true,
                canHavePartialProgressSaved,
                parentScreen = ExplorationActivityParams.ParentScreen.STORY_SCREEN,
                explorationCheckpoint = it.value
              )
            } else if (it is AsyncResult.Failure) {
              explorationCheckpointLiveData.removeObserver(this)
              explorationSelectionListener.selectExploration(
                profileId,
                classroomId,
                topicId,
                storyId,
                explorationId,
                canExplorationBeResumed = false,
                canHavePartialProgressSaved,
                parentScreen = ExplorationActivityParams.ParentScreen.STORY_SCREEN,
                explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
              )
            }
          }
        }
      )
    } else {
      explorationSelectionListener.selectExploration(
        profileId,
        classroomId,
        topicId,
        storyId,
        explorationId,
        canExplorationBeResumed = false,
        canHavePartialProgressSaved,
        parentScreen = ExplorationActivityParams.ParentScreen.STORY_SCREEN,
        explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
      )
    }
  }

  fun computeChapterTitleText(): String {
    val title =
      translationController.extractString(
        chapterSummary.title, ephemeralChapterSummary.writtenTranslationContext
      )
    return resourceHandler.getStringInLocaleWithWrapping(
      R.string.chapter_name, (index + 1).toString(), title
    )
  }
}
