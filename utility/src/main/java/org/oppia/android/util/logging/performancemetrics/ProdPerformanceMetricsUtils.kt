package org.oppia.android.util.logging.performancemetrics

import android.app.ActivityManager
import android.content.Context
import android.net.TrafficStats
import org.oppia.android.app.model.OppiaMetricLog
import org.oppia.android.util.logging.ConsoleLogger
import java.io.File
import javax.inject.Inject

/** Utility to extract performance metrics from the underlying Android system. */
class ProdPerformanceMetricsUtils @Inject constructor(
  private val context: Context,
  private val consoleLogger: ConsoleLogger
) : PerformanceMetricsUtils {

  override fun getApkSize(): Long {
    var apkSize: Long = 0
    try {
      val apkPath =
        context.packageManager.getPackageInfo(context.packageName, 0).applicationInfo.sourceDir
      val apkFile = File(apkPath)
      apkSize = ((apkFile.length() / 1024))
    } catch (e: Exception) {
      consoleLogger.e(
        "PerformanceMetricsUtils",
        "APK size could not be calculated."
      )
    }
    return apkSize
  }

  override fun getUsedStorage(): Long {
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

  override fun getTotalSentBytes(): Long = TrafficStats.getUidTxBytes(context.applicationInfo.uid)

  override fun getTotalReceivedBytes(): Long =
    TrafficStats.getUidRxBytes(context.applicationInfo.uid)

  override fun getTotalPssUsed(): Long {
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

  override fun getDeviceStorageTier(): OppiaMetricLog.StorageTier {
    return when (
      (context.filesDir.totalSpace + context.cacheDir.totalSpace).toDouble() / (1024 * 1024 * 1024)
    ) {
      in 0.00..5.00 -> OppiaMetricLog.StorageTier.LOW_STORAGE
      in 5.00..20.00 -> OppiaMetricLog.StorageTier.MEDIUM_STORAGE
      else -> OppiaMetricLog.StorageTier.HIGH_STORAGE
    }
  }

  override fun getDeviceMemoryTier(): OppiaMetricLog.MemoryTier {
    val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val memoryInfo = ActivityManager.MemoryInfo()
    activityManager.getMemoryInfo(memoryInfo)
    return when (memoryInfo.totalMem.toDouble() / (1024 * 1024 * 1024)) {
      in 0.00..1.00 -> OppiaMetricLog.MemoryTier.LOW_MEMORY_TIER
      in 1.00..2.00 -> OppiaMetricLog.MemoryTier.MEDIUM_MEMORY_TIER
      else -> OppiaMetricLog.MemoryTier.HIGH_MEMORY_TIER
    }
  }
}
