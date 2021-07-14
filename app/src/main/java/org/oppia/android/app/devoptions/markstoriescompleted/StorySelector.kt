package org.oppia.android.app.devoptions.markstoriescompleted

/** Interface to update the selectedStoryList in [MarkStoriesCompletedFragmentPresenter]. */
interface StorySelector {
  /** This story will get added to selectedStoryList in [MarkStoriesCompletedFragmentPresenter]. */
  fun storySelected(storyId: String)

  /** This story will get removed from selectedStoryList in [MarkStoriesCompletedFragmentPresenter]. */
  fun storyUnselected(storyId: String)
}
