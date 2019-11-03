package org.oppia.app.story.storyitemviewmodel

import org.oppia.app.story.StoryFragment

/** Header view model for the recycler view in [StoryFragment]. */
class StoryHeaderViewModel(
  val completedChapters: Int,
  val totalChapters: Int
) : StoryItemViewModel()
