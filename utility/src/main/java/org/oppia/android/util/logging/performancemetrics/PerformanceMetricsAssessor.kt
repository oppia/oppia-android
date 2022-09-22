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
}
