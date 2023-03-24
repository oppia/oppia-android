package org.oppia.android.domain.exploration

import org.oppia.android.app.model.ProfileId
import javax.inject.Inject

/** Implementation of [ExplorationProgressListener] */
class ExplorationProgressListenerImpl @Inject constructor(
  private val topicLearningTimeController: TopicLearningTimeController
) : ExplorationProgressListener {
  override fun onExplorationSessionStarted() {
    topicLearningTimeController.setExplorationSessionStarted()
  }

  override fun onExplorationSessionPaused(profileId: ProfileId, topicId: String) {
    topicLearningTimeController.setExplorationSessionPaused(profileId, topicId)
  }
}
