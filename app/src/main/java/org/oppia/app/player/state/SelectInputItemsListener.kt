package org.oppia.app.player.state

/**
 * Interface to keep track of story-index for which ChapterList is visible/expanded.
 * This mainly helps to maintain the state during configuration change.
 */
interface SelectInputItemsListener {
  fun onInputItemSelection(indexList: ArrayList<Int>)
}
