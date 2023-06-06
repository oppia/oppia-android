package org.oppia.android.domain.exploration

import org.oppia.android.app.model.ProfileId

/** Listener for when an exploration has started or ended. */
interface ExplorationProgressListener {
  /** Called when an exploration has started. */
  fun onExplorationStarted(profileId: ProfileId, topicId: String)

  /** Called when an exploration has ended. */
  fun onExplorationEnded(profileId: ProfileId, topicId: String)
}
