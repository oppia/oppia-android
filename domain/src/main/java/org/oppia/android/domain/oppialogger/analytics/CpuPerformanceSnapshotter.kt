package org.oppia.android.domain.oppialogger.analytics

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.oppia.android.app.model.ScreenName
import org.oppia.android.app.model.ScreenName.BACKGROUND_SCREEN
import org.oppia.android.app.model.ScreenName.FOREGROUND_SCREEN
import org.oppia.android.app.model.ScreenName.SCREEN_NAME_UNSPECIFIED
import org.oppia.android.util.logging.ConsoleLogger
import org.oppia.android.util.logging.ExceptionLogger
import org.oppia.android.util.logging.performancemetrics.PerformanceMetricsAssessor
import org.oppia.android.util.logging.performancemetrics.PerformanceMetricsAssessor.AppIconification
import org.oppia.android.util.logging.performancemetrics.PerformanceMetricsAssessor.AppIconification.APP_IN_BACKGROUND
import org.oppia.android.util.logging.performancemetrics.PerformanceMetricsAssessor.AppIconification.APP_IN_FOREGROUND
import org.oppia.android.util.logging.performancemetrics.PerformanceMetricsAssessor.AppIconification.UNINITIALIZED
import org.oppia.android.util.logging.performancemetrics.PerformanceMetricsAssessor.CpuSnapshot

/**
 * Snapshotter that gracefully and sequentially logs CPU usage across foreground and background
 * moves of the application.
 */
class CpuPerformanceSnapshotter(
  private val backgroundCoroutineDispatcher: CoroutineDispatcher,
  private val performanceMetricsLogger: PerformanceMetricsLogger,
  private val consoleLogger: ConsoleLogger,
  private val exceptionLogger: ExceptionLogger,
  private val performanceMetricsAssessor: PerformanceMetricsAssessor,
  private val foregroundCpuLoggingTimePeriodMillis: Long,
  private val backgroundCpuLoggingTimePeriodMillis: Long,
  private val initialIconificationCutOffTimePeriodMillis: Long
) {

  private var currentIconification = UNINITIALIZED
  private var isSnapshotterInitialized = false
  private val commandQueue by lazy { createCommandQueueActor() }

  /**
   * Initialises the snapshotter by calling the commandQueue to start recording CPU Usage.
   *
   * It must be noted that the class can only be initialized once in the app's lifecycle. In case
   * a re-initialization is attempted, the app will throw error.
   */
  fun initialiseSnapshotter() {
    require(!isSnapshotterInitialized) {
      "CpuPerformanceSnapshotter: Class has already been initialized."
    }
    isSnapshotterInitialized = true
    CoroutineScope(backgroundCoroutineDispatcher).launch {
      delay(initialIconificationCutOffTimePeriodMillis)
      if (currentIconification == UNINITIALIZED) {
        sendSwitchIconificationCommand(APP_IN_BACKGROUND)
      }
    }
  }

  /**
   * Updates the current [AppIconification] in accordance with the app's state changes.
   *
   * This update only takes place if the app has been previously initialized and the
   * [newIconification] does not equal to UNINITIALIZED.
   */
  fun updateAppIconification(newIconification: AppIconification) {
    if (newIconification != UNINITIALIZED && isSnapshotterInitialized) {
      sendSwitchIconificationCommand(newIconification)
    }
  }

  private fun createCommandQueueActor(): SendChannel<CommandMessage> {
    var previousSnapshot = performanceMetricsAssessor.computeCpuSnapshotAtCurrentTime()
    var switchIconificationCount = 0
    val coroutineScope = CoroutineScope(backgroundCoroutineDispatcher)
    return coroutineScope.actor(capacity = Channel.UNLIMITED) {
      for (message in channel) {
        when (message) {
          is CommandMessage.SwitchIconification -> {
            ++switchIconificationCount
            if (currentIconification != UNINITIALIZED) {
              // Since there's a switch in the current iconification of the app, we'd cut short the
              // existing delay and log the current CPU usage relative to the previously logged one
              // without this explicit log command.
              performanceMetricsAssessor.getRelativeCpuUsage(
                previousSnapshot,
                performanceMetricsAssessor.computeCpuSnapshotAtCurrentTime()
              )?.let { relativeCpuUsage ->
                sendLogSnapshotDiffCommand(relativeCpuUsage, currentIconification)
              }
            }
            currentIconification = message.newIconification
            previousSnapshot = performanceMetricsAssessor.computeCpuSnapshotAtCurrentTime()
            // Schedule CPU usage logging for the new app iconification.
            sendScheduleTakeSnapshotCommand(currentIconification, switchIconificationCount)
          }
          is CommandMessage.ScheduleTakeSnapshot -> scheduleTakeSnapshot(
            message.currentIconification,
            switchIconificationCount
          )
          is CommandMessage.TakeSnapshot -> {
            if (message.switchId == switchIconificationCount) {
              val newSnapshot = performanceMetricsAssessor.computeCpuSnapshotAtCurrentTime()
              performanceMetricsAssessor.getRelativeCpuUsage(
                previousSnapshot,
                performanceMetricsAssessor.computeCpuSnapshotAtCurrentTime()
              )?.let { relativeCpuUsage ->
                sendLogSnapshotDiffCommand(relativeCpuUsage, currentIconification)
              }
              previousSnapshot = newSnapshot
              sendScheduleTakeSnapshotCommand(currentIconification, switchIconificationCount)
            }
          }
          is CommandMessage.LogSnapshotDiff -> {
            performanceMetricsLogger.logCpuUsage(
              message.screenName,
              message.relativeCpuUsage
            )
          }
        }
      }
    }
  }

  private fun sendSwitchIconificationCommand(newIconification: AppIconification) {
    commandQueue.offer(CommandMessage.SwitchIconification(newIconification)).apply {
      if (!this) {
        val exception = IllegalStateException("Iconification switching failed")
        consoleLogger.e(
          "CpuPerformanceSnapshotter",
          "Failure while switching AppIconification.",
          exception
        )
        exceptionLogger.logException(exception)
      }
    }
  }

  private suspend fun sendScheduleTakeSnapshotCommand(
    currentIconification: AppIconification,
    switchId: Int
  ) { commandQueue.send(CommandMessage.ScheduleTakeSnapshot(currentIconification, switchId)) }

  private suspend fun sendTakeSnapshotCommand(switchId: Int) {
    commandQueue.send(CommandMessage.TakeSnapshot(switchId))
  }

  private suspend fun sendLogSnapshotDiffCommand(
    relativeCpuUsage: Double,
    currentIconification: AppIconification
  ) {
    commandQueue.send(
      CommandMessage.LogSnapshotDiff(
        relativeCpuUsage,
        currentIconification.toScreenName()
      )
    )
  }

  /**
   * Schedules a delay on the basis of [currentIconification] and then sends the [CommandMessage]
   * for taking a [CpuSnapshot] that'll be used for logging the relative CPU usage of the application.
   */
  private fun scheduleTakeSnapshot(currentIconification: AppIconification, switchId: Int) {
    CoroutineScope(backgroundCoroutineDispatcher).launch {
      delay(currentIconification.getDelay())
      sendTakeSnapshotCommand(switchId)
    }.invokeOnCompletion {
      it?.let {
        consoleLogger.e(
          "PerformanceMetricsController",
          "Failed to schedule a delay and send CommandMessage to log CPU usage.",
          it
        )
      }
    }
  }

  /**
   * Represents a message that can be sent to [commandQueue] to process CPU usage logging.
   *
   * Messages are expected to be resolved serially (though their scheduling can occur across
   * multiple threads, so order cannot be guaranteed until they're enqueued).
   */
  private sealed class CommandMessage {

    /** [CommandMessage] for switching the current [AppIconification]. */
    data class SwitchIconification(val newIconification: AppIconification) : CommandMessage()

    /**
     * [CommandMessage] that schedules [TakeSnapshot] for the [currentIconification] and [switchId]
     * to take a [CpuSnapshot] and log CPU usage after a specific delay period.
     */
    data class ScheduleTakeSnapshot(
      val currentIconification: AppIconification,
      val switchId: Int
    ) : CommandMessage()

    /**
     * [CommandMessage] that takes a real-time [CpuSnapshot] and calls [LogSnapshotDiff] for logging
     * the relative CPU usage of the application.
     *
     * This relative CPU usage is calculated by comparing the real-time [CpuSnapshot] with a previous
     * [CpuSnapshot]. The current [CpuSnapshot] is then put on as the previous [CpuSnapshot] for the next
     * relative comparison.
     */
    data class TakeSnapshot(val switchId: Int) : CommandMessage()

    /** [CommandMessage] for logging the relative CPU usage of the application. */
    data class LogSnapshotDiff(
      val relativeCpuUsage: Double,
      val screenName: ScreenName
    ) : CommandMessage()
  }

  /** Returns an appropriate [ScreenName] on the basis of [AppIconification]. */
  private fun AppIconification.toScreenName(): ScreenName = when (this) {
    APP_IN_BACKGROUND -> BACKGROUND_SCREEN
    APP_IN_FOREGROUND -> FOREGROUND_SCREEN
    UNINITIALIZED -> SCREEN_NAME_UNSPECIFIED
  }

  /** Returns an appropriate delay time period in millis on the basis of [AppIconification]. */
  private fun AppIconification.getDelay(): Long = when (this) {
    APP_IN_BACKGROUND -> backgroundCpuLoggingTimePeriodMillis
    APP_IN_FOREGROUND -> foregroundCpuLoggingTimePeriodMillis
    UNINITIALIZED -> initialIconificationCutOffTimePeriodMillis
  }
}
