package org.oppia.android.domain.exploration

import org.oppia.android.app.model.ProfileId
import javax.inject.Inject
import org.oppia.android.domain.oppialogger.analytics.ApplicationLifecycleListener
import org.oppia.android.util.logging.ConsoleLogger

/** Implementation of [ExplorationProgressListener] */
class ExplorationProgressListenerImpl @Inject constructor(
  private val topicLearningTimeController: TopicLearningTimeController,
  private val consoleLogger: ConsoleLogger
) : ExplorationProgressListener, ApplicationLifecycleListener {
  val profileId = ProfileId.getDefaultInstance()
  val topicId = "testTopicIdImpl"

  override fun onExplorationSessionStarted() {

    startTimer()
  }

  override fun onExplorationSessionPaused() {
    stopTimerAndSave()
  }

  override fun onAppInForeground() {
  }

  override fun onAppInBackground() {
    stopTimerAndSave()
  }

  private fun startTimer() {
    topicLearningTimeController.setExplorationSessionStarted()
  }

  private fun stopTimerAndSave() {
    topicLearningTimeController.setExplorationSessionPaused(profileId, topicId)
  }
}
