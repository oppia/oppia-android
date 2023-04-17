package org.oppia.android.domain.exploration

import org.oppia.android.app.model.ProfileId
import javax.inject.Inject
import javax.inject.Singleton

/** Implementation of [ExplorationProgressListener] */
@Singleton
class ExplorationProgressListenerImpl @Inject constructor(
  private val explorationActiveTimeController: ExplorationActiveTimeController
) : ExplorationProgressListener {

  override fun onExplorationSessionStarted() {
    explorationActiveTimeController.setExplorationSessionStarted()
  }

  override fun onExplorationSessionPaused(profileId: ProfileId, topicId: String) {
    explorationActiveTimeController.setExplorationSessionPaused(profileId, topicId)
  }
}
