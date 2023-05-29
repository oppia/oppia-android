package org.oppia.android.domain.exploration

import org.oppia.android.app.model.ProfileId
import org.oppia.android.domain.oppialogger.analytics.ApplicationLifecycleListener
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Controller to decide when the session timer is started and stopped based on signals received
 * from [ExplorationProgressListener] and [ApplicationLifecycleListener].
 */
@Singleton
class ExplorationSessionTimerController @Inject constructor(
  private val explorationActiveTimeController: ExplorationActiveTimeController
) : ExplorationProgressListener, ApplicationLifecycleListener {
  private var isAppInForeground: Boolean = false
  private var explorationStarted: Boolean = false
  private var profileId: ProfileId? = null
  private var topicId: String? = null

  override fun onAppInForeground() {
    this.isAppInForeground = true
    if (explorationStarted) {
      startSessionTimer()
    }
  }

  override fun onAppInBackground() {
    this.isAppInForeground = false
    if (profileId != null && topicId != null) {
      stopSessionTimer(profileId!!, topicId!!)
    }
  }

  override fun onExplorationStarted(profileId: ProfileId, topicId: String) {
    this.explorationStarted = true
    this.profileId = profileId
    this.topicId = topicId

    if (isAppInForeground) {
      startSessionTimer()
    }
  }

  override fun onExplorationEnded(profileId: ProfileId, topicId: String) {
    this.explorationStarted = false
    stopSessionTimer(profileId, topicId)
  }

  private fun startSessionTimer() {
    explorationActiveTimeController.setExplorationSessionStarted()
  }

  private fun stopSessionTimer(profileId: ProfileId, topicId: String) {
    if ((!isAppInForeground && explorationStarted) || !explorationStarted) {
      explorationActiveTimeController.setExplorationSessionStopped(profileId, topicId)
    }
  }
}
