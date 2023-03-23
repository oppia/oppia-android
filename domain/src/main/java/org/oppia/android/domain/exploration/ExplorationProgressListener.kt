package org.oppia.android.domain.exploration

/** Listener for when an exploration is started or paused. */
interface ExplorationProgressListener {
  /** Called when an exploration is started. */
  fun onExplorationSessionStarted()

  /** Called when an exploration is finished. */
  fun onExplorationSessionPaused()
}