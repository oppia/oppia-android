package org.oppia.app.player.state.hintsandsolution

/**
 * Interface to keep track of hint-index for which HintList is visible/expanded.
 * This mainly helps to maintain the state during configuration change.
 */
interface ExpandedHintListIndexListener {
  fun onExpandListIconClicked(index: Int?)
}
