package org.oppia.android.app.story.storyitemviewmodel

/** Header view model for the recycler view in [StoryFragment]. */
class StoryHeaderViewModel(
  val completedChapters: Int,
  val totalChapters: Int
) : StoryItemViewModel()
