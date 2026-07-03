package org.oppia.android.domain.exploration

import org.oppia.android.app.model.LegacyProfileId

/**
 * Listener for when an exploration has started or ended.
 *
 * This listener is not safe to use across multiple threads and should only be accessed via the main
 * thread.
 *
 * These methods are invoked via the [ExplorationProgressController].
 */
interface ExplorationProgressListener {
  /** Called when an exploration has started. */
  fun onExplorationStarted(profileId: LegacyProfileId, topicId: String)

  /** Called when an exploration has ended. */
  fun onExplorationEnded()
}
