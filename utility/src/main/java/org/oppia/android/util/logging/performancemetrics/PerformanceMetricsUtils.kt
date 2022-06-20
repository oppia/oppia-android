package org.oppia.android.util.logging.performancemetrics

import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.net.TrafficStats
import android.os.Environment
import org.oppia.android.app.model.OppiaMetricLog
import org.oppia.android.util.logging.ConsoleLogger
import java.io.File
import javax.inject.Inject

class PerformanceMetricsUtils @Inject constructor(
  private val context: Application,
  private val consoleLogger: ConsoleLogger
) {

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

  fun getDeviceStorageTier(): OppiaMetricLog.StorageTier =
    when (Environment.getDataDirectory().totalSpace / (1024 * 1024 * 1024)) {
      in 0..5 -> OppiaMetricLog.StorageTier.LOW_STORAGE
      in 5..20 -> OppiaMetricLog.StorageTier.MEDIUM_STORAGE
      else -> OppiaMetricLog.StorageTier.HIGH_STORAGE
    }

  fun getTotalSentBytes(): Long = TrafficStats.getUidTxBytes(context.applicationInfo.uid)

  fun getTotalReceivedBytes(): Long = TrafficStats.getUidRxBytes(context.applicationInfo.uid)

  fun getTotalPssUsed(): Long {
    val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    var totalPssUsed: Long = 0
    if (activityManager.runningAppProcesses != null) {
      val pid = ActivityManager.RunningAppProcessInfo().pid
      val processMemoryInfo = activityManager.getProcessMemoryInfo(arrayOf(pid).toIntArray())
      for (element in processMemoryInfo) {
        totalPssUsed += element.totalPss
      }
    }
    return totalPssUsed
  }

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
}
