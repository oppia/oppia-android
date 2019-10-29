package org.oppia.app.topic.play

/**
 * Interface to keep track of index in for which ChapterList is visible/expanded.
 * This mainly helps to maintain the state during configuration change.
 */
interface ExpandedChapterListIndexListener {
  fun onExpandListIconClicked(index: Int)
}
