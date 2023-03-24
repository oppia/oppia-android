package org.oppia.android.domain.exploration

import org.oppia.android.app.model.ProfileId

/** Listener for when an exploration is started or paused. */
interface ExplorationProgressListener {
  /** Called when an exploration is started. */
  fun onExplorationSessionStarted()

  /** Called when an exploration is finished. */
  fun onExplorationSessionPaused(profileId: ProfileId, topicId: String)
}
