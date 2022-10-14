package org.oppia.android.domain.oppialogger.analytics

import android.os.Build
import android.os.Process
import android.system.Os
import android.system.OsConstants
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.oppia.android.app.model.ScreenName
import org.oppia.android.util.logging.ConsoleLogger
import org.oppia.android.util.logging.ExceptionLogger
import org.oppia.android.util.system.OppiaClock
import org.oppia.android.util.threading.BackgroundDispatcher

/**
 * Snapshotter that gracefully and sequentially logs cpu usage across foreground and background
 * moves of the application.
 */
class CpuPerformanceSnapshotter private constructor(
  private val backgroundCoroutineDispatcher: CoroutineDispatcher,
  private val performanceMetricsLogger: PerformanceMetricsLogger,
  private val initialIconification: AppIconification,
  private val oppiaClock: OppiaClock,
  private val consoleLogger: ConsoleLogger,
  private val exceptionLogger: ExceptionLogger,
  private val foregroundCpuLoggingTimePeriod: Long,
  private val backgroundCpuLoggingTimePeriod: Long
) {

  private val commandQueue by lazy { createCommandQueueActor() }

  /** Updates the current [AppIconification] in accordance with the app's state changes. */
  fun updateAppIconification(newIconification: AppIconification) {
    sendSwitchIconificationCommand(newIconification)
  }

  private fun createCommandQueueActor(): SendChannel<CommandMessage> {
    var currentIconification = initialIconification
    var previousSnapshot = computeSnapshotAtCurrentTime(currentIconification)
    var switchIconificationCount = 0
    val coroutineScope = CoroutineScope(backgroundCoroutineDispatcher)
    return coroutineScope.actor<CommandMessage>(capacity = Channel.UNLIMITED) {
      for (message in channel) {
        when (message) {
          is CommandMessage.SwitchIconification -> {
            ++switchIconificationCount
            sendLogSnapshotDiffCommand(
              getRelativeCpuUsage(
                previousSnapshot,
                computeSnapshotAtCurrentTime(currentIconification)
              )
            )
            currentIconification = message.newIconification
            previousSnapshot = computeSnapshotAtCurrentTime(currentIconification)
            sendScheduleTakeSnapshotCommand(currentIconification, switchIconificationCount)
          }
          is CommandMessage.ScheduleTakeSnapshot -> scheduleTakeSnapshot(
            message.currentIconification,
            switchIconificationCount
          )
          is CommandMessage.TakeSnapshot -> {
            if (message.switchId == switchIconificationCount) {
              val newSnapshot = computeSnapshotAtCurrentTime(currentIconification)
              sendLogSnapshotDiffCommand(getRelativeCpuUsage(previousSnapshot, newSnapshot))
              previousSnapshot = newSnapshot
              sendScheduleTakeSnapshotCommand(currentIconification, switchIconificationCount)
            }
          }
          is CommandMessage.LogSnapshotDiff -> {
            performanceMetricsLogger.logCpuUsage(
              currentIconification.toScreenName(),
              message.relativeCpuUsage
            )
          }
        }
      }
    }.also {
      CoroutineScope(backgroundCoroutineDispatcher).launch {
        sendTakeSnapshotCommand(switchIconificationCount)
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

  private suspend fun sendLogSnapshotDiffCommand(relativeCpuUsage: Double) {
    commandQueue.send(CommandMessage.LogSnapshotDiff(relativeCpuUsage))
  }

  /**
   * Schedules a delay on the basis of [currentIconification] and then sends the [CommandMessage]
   * for taking a [Snapshot] that'll be used for logging the relative cpu-usage of the application.
   */
  private fun scheduleTakeSnapshot(currentIconification: AppIconification, switchId: Int) {
    CoroutineScope(backgroundCoroutineDispatcher).launch {
      delay(currentIconification.getDelay())
      sendTakeSnapshotCommand(switchId)
    }.invokeOnCompletion {
      it?.let {
        consoleLogger.e(
          "PerformanceMetricsController",
          "Failed to remove metric log.",
          it
        )
      }
    }
  }

  private fun computeSnapshotAtCurrentTime(iconification: AppIconification): Snapshot {
    return Snapshot(
      appTimeMillis = oppiaClock.getCurrentTimeMs().toDouble(),
      cpuTimeMillis = Process.getElapsedCpuTime().toDouble(),
      numCores = getNumberOfOnlineCores(),
      iconification = iconification
    )
  }

  private fun getRelativeCpuUsage(firstSnapshot: Snapshot, secondSnapshot: Snapshot): Double {
    val deltaCpuTimeMs = secondSnapshot.cpuTimeMillis - firstSnapshot.cpuTimeMillis
    val deltaProcessTimeMs = secondSnapshot.appTimeMillis - firstSnapshot.appTimeMillis
    val numberOfCores = (secondSnapshot.numCores + firstSnapshot.numCores) / 2
    return deltaCpuTimeMs / (deltaProcessTimeMs * numberOfCores)
  }

  /** Returns the number of processors that are currently online/available. */
  private fun getNumberOfOnlineCores(): Double {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      Os.sysconf(OsConstants._SC_NPROCESSORS_ONLN).toDouble()
    } else {
      Runtime.getRuntime().availableProcessors().toDouble()
    }
  }

  /**
   * Container that consists of all the necessary values that are required for calculating cpu usage
   * at that point of time.
   */
  data class Snapshot(
    val iconification: AppIconification,
    val appTimeMillis: Double,
    val cpuTimeMillis: Double,
    val numCores: Double
  )

  /**
   * Represents a message that can be sent to [commandQueue] to process cpu-usage logging.
   *
   * Messages are expected to be resolved serially (though their scheduling can occur across
   * multiple threads, so order cannot be guaranteed until they're enqueued).
   */
  sealed class CommandMessage {

    /** [CommandMessage] for switching the current [AppIconification]. */
    data class SwitchIconification(val newIconification: AppIconification) : CommandMessage()

    /**
     * [CommandMessage] that schedules [TakeSnapshot] for the [currentIconification] and [switchId]
     * to take a [Snapshot] and log-cpu usage after a specific delay period.
     */
    data class ScheduleTakeSnapshot(
      val currentIconification: AppIconification,
      val switchId: Int
    ) : CommandMessage()

    /**
     * [CommandMessage] that takes a real-time [Snapshot] and calls [LogSnapshotDiff] for logging
     * the relative cpu-usage of the application.
     *
     * This relative cpu-usage is calculated by comparing the real-time [Snapshot] with a previous
     * [Snapshot]. The current [Snapshot] is then put on as the previous [Snapshot] for the next
     * relative comparison.
     */
    data class TakeSnapshot(val switchId: Int) : CommandMessage()

    /** [CommandMessage] for logging the relative cpu usage of the application. */
    data class LogSnapshotDiff(val relativeCpuUsage: Double) : CommandMessage()
  }

  /** Represents the different states of the application. */
  enum class AppIconification {
    APP_IN_FOREGROUND,
    APP_IN_BACKGROUND
  }

  /** Returns an appropriate [ScreenName] on the basis of [AppIconification]. */
  private fun AppIconification.toScreenName(): ScreenName = when (this) {
    AppIconification.APP_IN_BACKGROUND -> ScreenName.BACKGROUND_SCREEN
    AppIconification.APP_IN_FOREGROUND -> ScreenName.FOREGROUND_SCREEN
  }

  private fun AppIconification.getDelay(): Long = when (this) {
    AppIconification.APP_IN_BACKGROUND -> backgroundCpuLoggingTimePeriod
    AppIconification.APP_IN_FOREGROUND -> foregroundCpuLoggingTimePeriod
  }

  /** Creates an instance of [CpuPerformanceSnapshotter] by properly injecting dependencies. */
  class Factory @Inject constructor(
    private val performanceMetricsLogger: PerformanceMetricsLogger,
    private val oppiaClock: OppiaClock,
    private val consoleLogger: ConsoleLogger,
    private val exceptionLogger: ExceptionLogger,
    @ForegroundCpuLoggingTimePeriod private val foregroundCpuLoggingTimePeriod: Long,
    @BackgroundCpuLoggingTimePeriod private val backgroundCpuLoggingTimePeriod: Long,
    @BackgroundDispatcher private val backgroundCoroutineDispatcher: CoroutineDispatcher
  ) {

    /** Returns an instance of [CpuPerformanceSnapshotter]. */
    fun createSnapshotter(): CpuPerformanceSnapshotter =
      CpuPerformanceSnapshotter(
        backgroundCoroutineDispatcher,
        performanceMetricsLogger,
        AppIconification.APP_IN_FOREGROUND,
        oppiaClock,
        consoleLogger,
        exceptionLogger,
        foregroundCpuLoggingTimePeriod,
        backgroundCpuLoggingTimePeriod
      )
  }
}
