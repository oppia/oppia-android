package org.oppia.app.player.state.hintsandsolution

/**
 * Interface to keep track of story-index for which ChapterList is visible/expanded.
 * This mainly helps to maintain the state during configuration change.
 */
interface ExpandedHintListIndexListener {
  fun onExpandListIconClicked(index: Int?)
}
