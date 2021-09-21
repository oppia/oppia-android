package org.oppia.android.app.story.storyitemviewmodel

import org.oppia.android.R
import org.oppia.android.app.translation.AppLanguageResourceHandler

/** Header view model for the recycler view in [StoryFragment]. */
class StoryHeaderViewModel(
  private val completedChapters: Int,
  private val totalChapters: Int,
  private val resourceHandler: AppLanguageResourceHandler
) : StoryItemViewModel() {
  fun computeStoryProgressChapterCompletedText(): String {
    return resourceHandler.getQuantityStringInLocaleWithWrapping(
      R.plurals.story_total_chapters,
      totalChapters,
      completedChapters.toString(),
      totalChapters.toString()
    )
  }
}
