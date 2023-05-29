package org.oppia.android.domain.exploration

import org.oppia.android.app.model.ProfileId

/** Listener for when an exploration is started or ended. */
interface ExplorationProgressListener {
  /** Called when an exploration is started. */
  fun onExplorationStarted(profileId: ProfileId, topicId: String)

  /** Called when an exploration is stopped. */
  fun onExplorationEnded(profileId: ProfileId, topicId: String)
}
