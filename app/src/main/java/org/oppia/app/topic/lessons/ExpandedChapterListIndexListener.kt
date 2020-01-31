package org.oppia.app.topic.lessons

/**
 * Interface to keep track of story-index for which ChapterList is visible/expanded.
 * This mainly helps to maintain the state during configuration change.
 */
interface ExpandedChapterListIndexListener {
  fun onExpandListIconClicked(index: Int?)
}
