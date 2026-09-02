package org.oppia.android.app.topic.lessons

import androidx.databinding.ObservableBoolean
import org.oppia.android.app.model.ChapterPlayState
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.app.view.models.R
import org.oppia.android.app.viewmodel.ObservableViewModel

/** [ObservableViewModel] for displaying a chapter summary. */
class ChapterSummaryViewModel(
  val chapterPlayState: ChapterPlayState,
  val explorationId: String,
  val chapterTitle: String,
  val previousChapterTitle: String?,
  val storyId: String,
  private val index: Int,
  private val chapterSummarySelector: ChapterSummarySelector,
  private val resourceHandler: AppLanguageResourceHandler,
  val storyIndex: Int,
  private val lockedChapterTooltipCoordinator: LockedChapterTooltipCoordinator? = null
) : ObservableViewModel() {

  /**
   * Whether the locked-chapter micro-tooltip (prerequisite message) is currently visible.
   *
   * Only meaningful when [chapterPlayState] is
   * [ChapterPlayState.NOT_PLAYABLE_MISSING_PREREQUISITES]. Toggled by [onClick].
   */
  val isPrerequisiteTooltipVisible = ObservableBoolean(false)

  /** True when this chapter cannot be played until a prior chapter is completed. */
  val isLocked: Boolean
    get() = chapterPlayState == ChapterPlayState.NOT_PLAYABLE_MISSING_PREREQUISITES

  /**
   * Handles a chapter row click.
   *
   * Locked chapters toggle a micro-tooltip that explains which prior chapter must be
   * completed. Playable chapters route through [ChapterSummarySelector].
   */
  fun onClick(explorationId: String) {
    if (isLocked) {
      val willShow = !isPrerequisiteTooltipVisible.get()
      if (willShow) {
        lockedChapterTooltipCoordinator?.onLockedChapterTooltipRequested(this)
      }
      isPrerequisiteTooltipVisible.set(willShow)
      return
    }
    // Hide any leftover tooltip state before navigating away.
    isPrerequisiteTooltipVisible.set(false)
    chapterSummarySelector.selectChapterSummary(storyId, explorationId, chapterPlayState)
  }

  /** Hides the locked-chapter micro-tooltip if it is showing. */
  fun hidePrerequisiteTooltip() {
    if (isPrerequisiteTooltipVisible.get()) {
      isPrerequisiteTooltipVisible.set(false)
    }
  }

  /**
   * Returns the micro-tooltip / accessibility text explaining why this chapter is locked,
   * or why it is completed / in progress for non-locked states.
   */
  fun computeChapterPlayStateIconContentDescription(): String {
    return when (chapterPlayState) {
      ChapterPlayState.COMPLETED -> {
        resourceHandler.getStringInLocaleWithWrapping(
          R.string.chapter_completed, (index + 1).toString(), chapterTitle
        )
      }
      ChapterPlayState.NOT_PLAYABLE_MISSING_PREREQUISITES -> computeLockedPrerequisiteMessage()
      else -> {
        resourceHandler.getStringInLocaleWithWrapping(
          R.string.chapter_in_progress, (index + 1).toString(), chapterTitle
        )
      }
    }
  }

  /**
   * Returns the learner-facing prerequisite message shown in the locked-chapter
   * micro-tooltip.
   */
  fun computePrerequisiteTooltipText(): String = computeLockedPrerequisiteMessage()

  fun computePlayChapterIndexText(): String {
    return resourceHandler.getStringInLocaleWithWrapping(
      R.string.topic_play_chapter_index, (index + 1).toString()
    )
  }

  private fun computeLockedPrerequisiteMessage(): String {
    return if (previousChapterTitle != null) {
      resourceHandler.getStringInLocaleWithWrapping(
        R.string.chapter_locked_prerequisite_title_label,
        (index + 1).toString(),
        chapterTitle,
        index.toString(),
        previousChapterTitle
      )
    } else {
      resourceHandler.getStringInLocaleWithWrapping(
        R.string.chapter_prerequisite_title_label_without_chapter_title
      )
    }
  }

  /** Coordinates exclusive visibility of locked-chapter micro-tooltips within a story. */
  interface LockedChapterTooltipCoordinator {
    /** Called before [source] shows its tooltip so siblings can hide theirs. */
    fun onLockedChapterTooltipRequested(source: ChapterSummaryViewModel)
  }
}
