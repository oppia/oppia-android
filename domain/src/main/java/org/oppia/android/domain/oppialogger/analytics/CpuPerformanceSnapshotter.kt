package org.oppia.android.domain.oppialogger.analytics

import android.os.Build
import android.os.Process
import android.system.Os
import android.system.OsConstants
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ObsoleteCoroutinesApi
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
import javax.inject.Inject

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

  @ObsoleteCoroutinesApi
  private val commandQueue by lazy { createCommandQueueActor() }

  @ObsoleteCoroutinesApi
  fun updateAppIconification(newIconification: AppIconification) {
    sendSwitchIconificationCommand(newIconification)
  }

  @ObsoleteCoroutinesApi
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
              previousSnapshot,
              computeSnapshotAtCurrentTime(currentIconification)
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
              sendLogSnapshotDiffCommand(previousSnapshot, newSnapshot)
              previousSnapshot = newSnapshot
              sendScheduleTakeSnapshotCommand(currentIconification, switchIconificationCount)
            }
          }
          is CommandMessage.LogSnapshotDiff -> {
            val currentScreen = when (currentIconification) {
              AppIconification.APP_IN_BACKGROUND -> ScreenName.BACKGROUND_SCREEN
              AppIconification.APP_IN_FOREGROUND -> ScreenName.FOREGROUND_SCREEN
            }
            performanceMetricsLogger.logCpuUsage(
              currentScreen,
              getRelativeCpuUsage(message.firstSnapshot, message.secondSnapshot)
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

  @ObsoleteCoroutinesApi
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

  @ObsoleteCoroutinesApi
  private suspend fun sendScheduleTakeSnapshotCommand(
    currentIconification: AppIconification,
    switchId: Int
  ) {
    commandQueue.send(CommandMessage.ScheduleTakeSnapshot(currentIconification, switchId))
  }

  @ObsoleteCoroutinesApi
  private suspend fun sendTakeSnapshotCommand(switchId: Int) {
    commandQueue.send(CommandMessage.TakeSnapshot(switchId))
  }

  @ObsoleteCoroutinesApi
  private suspend fun sendLogSnapshotDiffCommand(first: Snapshot, second: Snapshot) {
    commandQueue.send(CommandMessage.LogSnapshotDiff(first, second))
  }

  @ObsoleteCoroutinesApi
  private fun scheduleTakeSnapshot(currentIconification: AppIconification, switchId: Int) {
    val delayMs = when (currentIconification) {
      AppIconification.APP_IN_FOREGROUND -> foregroundCpuLoggingTimePeriod
      AppIconification.APP_IN_BACKGROUND -> backgroundCpuLoggingTimePeriod
    }
    CoroutineScope(backgroundCoroutineDispatcher).launch {
      delay(delayMs)
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
      appTimeMillis = oppiaClock.getCurrentTimeMs(),
      cpuTimeMillis = Process.getElapsedCpuTime(),
      numCores = getNumberOfOnlineCores(),
      iconification = iconification
    )
  }

  private fun getRelativeCpuUsage(firstSnapshot: Snapshot, secondSnapshot: Snapshot): Double {
    val deltaCpuTimeMs = secondSnapshot.cpuTimeMillis - firstSnapshot.cpuTimeMillis
    val deltaProcessTimeMs = secondSnapshot.appTimeMillis - firstSnapshot.appTimeMillis
    val numberOfCores = (secondSnapshot.numCores + firstSnapshot.numCores) / 2
    return deltaCpuTimeMs.toDouble() / (deltaProcessTimeMs.toDouble() * numberOfCores)
  }

  private fun getNumberOfOnlineCores(): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      Os.sysconf(OsConstants._SC_NPROCESSORS_ONLN).toInt()
    } else {
      Runtime.getRuntime().availableProcessors()
    }
  }

  data class Snapshot(
    val iconification: AppIconification,
    val appTimeMillis: Long,
    val cpuTimeMillis: Long,
    val numCores: Int
  )

  sealed class CommandMessage {
    data class SwitchIconification(val newIconification: AppIconification) : CommandMessage()
    data class ScheduleTakeSnapshot(
      val currentIconification: AppIconification,
      val switchId: Int
    ) : CommandMessage()
    data class TakeSnapshot(val switchId: Int) : CommandMessage()
    data class LogSnapshotDiff(val firstSnapshot: Snapshot, val secondSnapshot: Snapshot) :
      CommandMessage()
  }

  enum class AppIconification {
    APP_IN_FOREGROUND,
    APP_IN_BACKGROUND
  }

  class Factory @Inject constructor(
    private val performanceMetricsLogger: PerformanceMetricsLogger,
    private val oppiaClock: OppiaClock,
    private val consoleLogger: ConsoleLogger,
    private val exceptionLogger: ExceptionLogger,
    @ForegroundCpuLoggingTimePeriod private val foregroundCpuLoggingTimePeriod: Long,
    @BackgroundCpuLoggingTimePeriod private val backgroundCpuLoggingTimePeriod: Long,
    @BackgroundDispatcher private val backgroundCoroutineDispatcher: CoroutineDispatcher
  ) {
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
