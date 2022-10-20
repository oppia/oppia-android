package org.oppia.android.util.logging.performancemetrics

import org.oppia.android.app.model.OppiaMetricLog

/** Utility to extract performance metrics from the underlying Android system. */
interface PerformanceMetricsAssessor {

  /** Returns the size of the app's installed APK file, in bytes. */
  fun getApkSize(): Long

  /**
   * Returns the amount of storage usage by the app on user's device in bytes.
   * This storage size is the cumulative size of app-specific files which include the application
   * cache but not the apk size.
   */
  fun getUsedStorage(): Long

  /** Returns the number of bytes sent by the application over a network since device reboot. */
  fun getTotalSentBytes(): Long

  /** Returns the number of bytes received by the application over a network since device reboot. */
  fun getTotalReceivedBytes(): Long

  /** Returns the amount of memory used by the application on the device in bytes. */
  fun getTotalPssUsed(): Long

  /**
   * Returns the [OppiaMetricLog.StorageTier] of the device by analysing the total storage
   * capacity of the device.
   */
  fun getDeviceStorageTier(): OppiaMetricLog.StorageTier

  /**
   * Returns the [OppiaMetricLog.MemoryTier] of the device by analysing the total memory
   * capacity of the device.
   */
  fun getDeviceMemoryTier(): OppiaMetricLog.MemoryTier

  /** Returns a [CpuSnapshot] with current values for CPU usage calculations. */
  fun computeCpuSnapshotAtCurrentTime(): CpuSnapshot

  /** Returns the relative CPU usage after comparing [firstCpuSnapshot] and [secondCpuSnapshot]. */
  fun getRelativeCpuUsage(firstCpuSnapshot: CpuSnapshot, secondCpuSnapshot: CpuSnapshot): Double?

  /**
   * Container that consists of all the necessary values that are required for calculating CPU usage
   * at that point of time.
   *
   * @property appTimeMillis denotes the amount of time since the current instance of the app begun
   * @property cpuTimeMillis denotes the amount of time CPU ran for this process
   * @property numberOfOnlineCores denotes the number of currently online/available cores
   */
  data class CpuSnapshot(
    val appTimeMillis: Long,
    val cpuTimeMillis: Long,
    val numberOfOnlineCores: Int
  )

  /** Represents the different states of the application. */
  enum class AppIconification {
    /** Indicates that the app is in foreground. */
    APP_IN_FOREGROUND,
    /** Indicates that the app is in background. */
    APP_IN_BACKGROUND
  }
}
