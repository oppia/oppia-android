package org.oppia.android.domain.exploration

import org.oppia.android.app.model.ProfileId

/** Listener for when an exploration is started or paused. */
interface ExplorationProgressListener {
  /** Called when an exploration session is started. */
  fun onExplorationSessionStarted()

  /** Called when an exploration session is finished. */
  fun onExplorationSessionEnded(profileId: ProfileId, topicId: String)
}
