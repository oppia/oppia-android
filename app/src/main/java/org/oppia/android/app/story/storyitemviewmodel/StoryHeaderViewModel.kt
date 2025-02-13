package org.oppia.android.app.story.storyitemviewmodel

import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.app.view.models.R

/** Header view model for the recycler view in [StoryFragment]. */
class StoryHeaderViewModel(
  private val completedChapters: Int,
  private val totalChapters: Int,
  private val resourceHandler: AppLanguageResourceHandler
) : StoryItemViewModel() {
  /** Returns the user-readable progress completion text corresponding to chapters in a story. */
  fun computeStoryProgressChapterCompletedText(): String {
    return resourceHandler.getQuantityStringInLocaleWithWrapping(
      R.plurals.story_total_chapters,
      totalChapters,
      completedChapters.toString(),
      totalChapters.toString()
    )
  }
}
