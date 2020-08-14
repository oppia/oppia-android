package org.oppia.app.hintsandsolution

/**
 * Interface to keep track of hint-index for which HintList is visible/expanded.
 * This mainly helps to maintain the state during configuration change.
 */
interface ExpandedHintListIndexListener {

  /** Manage expanded list icon */
  fun onExpandListIconClicked(index: Int?)

  /** Manage reveal hint button visibility while orientation change */
  fun onRevealHintClicked(index: Int, isHintRevealed: Boolean)

  /** Manage reveal solution button visibility while orientation change */
  fun onRevealSolutionClicked(solutionIndex: Int, isSolutionRevealed: Boolean)
}
