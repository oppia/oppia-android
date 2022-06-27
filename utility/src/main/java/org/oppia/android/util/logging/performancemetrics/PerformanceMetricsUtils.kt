package org.oppia.android.util.logging.performancemetrics

import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.net.TrafficStats
import android.os.Environment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import org.oppia.android.app.model.OppiaMetricLog
import org.oppia.android.util.logging.ConsoleLogger
import java.io.File
import javax.inject.Inject

class PerformanceMetricsUtils @Inject constructor(
  private val context: Application,
  private val consoleLogger: ConsoleLogger
) : LifecycleObserver {

  // Keep the default value as false as the app is considered to be in the background until it comes
  // to foreground.
  private var isAppInForeground: Boolean = false

  /** Returns the size of the app's installed APK file, in bytes. */
  fun getApkSize(): Long {
    var apkSize: Long = 0
    try {
      val apkPath =
        context.packageManager.getPackageInfo(context.packageName, 0).applicationInfo.sourceDir
      val apkFile = File(apkPath)
      apkSize = ((apkFile.length() / 1024).toString()).toLong()
    } catch (e: Exception) {
      consoleLogger.e(
        "PerformanceMetricsUtils",
        "APK size could not be calculated."
      )
    }
    return apkSize
  }

  /**
   * Returns the amount of storage usage by the app on user's device in bytes.
   * This storage size is the cumulative size of app-specific files which include the application
   * cache but not the apk size.
   */
  fun getUsedStorage(): Long {
    var storageUsage: Long = 0
    try {
      val permanentStorageUsage = context.filesDir.totalSpace - context.filesDir.freeSpace
      val cacheStorageUsage = context.cacheDir.totalSpace - context.cacheDir.freeSpace
      storageUsage = permanentStorageUsage + cacheStorageUsage
    } catch (e: Exception) {
      consoleLogger.e(
        "PerformanceMetricsUtils",
        "Storage usage could not be calculated."
      )
    }
    return storageUsage
  }

  /** Returns the number of bytes sent by the application over a network since device reboot. */
  fun getTotalSentBytes(): Long = TrafficStats.getUidTxBytes(context.applicationInfo.uid)

  /** Returns the number of bytes received by the application over a network since device reboot. */
  fun getTotalReceivedBytes(): Long = TrafficStats.getUidRxBytes(context.applicationInfo.uid)

  /** Returns the amount of memory used by the application on the device in bytes. */
  fun getTotalPssUsed(): Long {
    val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    var totalPssUsed: Long = 0
    if (activityManager.runningAppProcesses != null) {
      val pid = ActivityManager.RunningAppProcessInfo().pid
      val processMemoryInfo = activityManager.getProcessMemoryInfo(arrayOf(pid).toIntArray())
      if (processMemoryInfo != null) {
        for (element in processMemoryInfo) {
          totalPssUsed += element.totalPss
        }
      }
    }
    return totalPssUsed
  }

  /**
   * Returns the [OppiaMetricLog.StorageTier] of the device by analysing the total storage
   * capacity of the device.
   */
  fun getDeviceStorageTier(): OppiaMetricLog.StorageTier =
    when (Environment.getDataDirectory().totalSpace / (1024 * 1024 * 1024)) {
      in 0..5 -> OppiaMetricLog.StorageTier.LOW_STORAGE
      in 5..20 -> OppiaMetricLog.StorageTier.MEDIUM_STORAGE
      else -> OppiaMetricLog.StorageTier.HIGH_STORAGE
    }

  /**
   * Returns the [OppiaMetricLog.MemoryTier] of the device by analysing the total memory
   * capacity of the device.
   */
  fun getDeviceMemoryTier(): OppiaMetricLog.MemoryTier {
    val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val memoryInfo = ActivityManager.MemoryInfo()
    activityManager.getMemoryInfo(memoryInfo)
    return when (memoryInfo.totalMem / (1024 * 1024 * 1024)) {
      in 0..1 -> OppiaMetricLog.MemoryTier.LOW_MEMORY_TIER
      in 1..2 -> OppiaMetricLog.MemoryTier.MEDIUM_MEMORY_TIER
      else -> OppiaMetricLog.MemoryTier.HIGH_MEMORY_TIER
    }
  }

  /** Occurs when application comes to foreground. */
  @OnLifecycleEvent(Lifecycle.Event.ON_START)
  fun onAppInForeground() {
    isAppInForeground = true
  }

  /** Occurs when application goes to background. */
  @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
  fun onAppInBackground() {
    isAppInForeground = false
  }

  /** Returns a boolean value indicating whether the application is in foreground or not. */
  fun isAppInForeground(): Boolean = isAppInForeground
}
