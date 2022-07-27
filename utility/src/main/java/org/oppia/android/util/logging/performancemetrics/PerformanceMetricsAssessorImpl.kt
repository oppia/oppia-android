package org.oppia.android.util.logging.performancemetrics

import android.app.ActivityManager
import android.content.Context
import android.net.TrafficStats
import org.oppia.android.app.model.OppiaMetricLog
import java.io.File
import javax.inject.Inject

/** Utility to extract performance metrics from the underlying Android system. */
class PerformanceMetricsAssessorImpl @Inject constructor(
  private val context: Context
) : PerformanceMetricsAssessor {

  private val activityManager: ActivityManager by lazy {
    context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
  }

  override fun getApkSize(): Long {
    val apkPath =
      context.packageManager.getPackageInfo(context.packageName, 0).applicationInfo.sourceDir
    return File(apkPath).length() / 1024
  }

  override fun getUsedStorage(): Long {
    val permanentStorageUsage = context.filesDir?.listFiles()?.map { it.length() }?.sum()
    val cacheStorageUsage = context.cacheDir?.listFiles()?.map { it.length() }?.sum()
    return (cacheStorageUsage?.let { permanentStorageUsage?.plus(it) }) ?: 0L
  }

  override fun getTotalSentBytes(): Long = TrafficStats.getUidTxBytes(context.applicationInfo.uid)

  override fun getTotalReceivedBytes(): Long =
    TrafficStats.getUidRxBytes(context.applicationInfo.uid)

  override fun getTotalPssUsed(): Long {
    val pid = ActivityManager.RunningAppProcessInfo().pid
    activityManager.runningAppProcesses
    val processMemoryInfo = activityManager.getProcessMemoryInfo(intArrayOf(pid))
    return processMemoryInfo?.map { it.totalPss }?.sum()?.toLong() ?: 0L
  }

  override fun getDeviceStorageTier(): OppiaMetricLog.StorageTier {
    val permanentStorageUsage = context.filesDir?.listFiles()?.map { it.length() }?.sum()
    val cacheStorageUsage = context.cacheDir?.listFiles()?.map { it.length() }?.sum()
    val usedStorage = (cacheStorageUsage?.let { permanentStorageUsage?.plus(it) })
    if (usedStorage != null) {
      return when (
        usedStorage.let { it / (1024 * 1024 * 1024) }
      ) {
        in 0L until 32L -> OppiaMetricLog.StorageTier.LOW_STORAGE
        in 32L..64L -> OppiaMetricLog.StorageTier.MEDIUM_STORAGE
        else -> OppiaMetricLog.StorageTier.HIGH_STORAGE
      }
    }
    return OppiaMetricLog.StorageTier.UNRECOGNIZED
  }

  override fun getDeviceMemoryTier(): OppiaMetricLog.MemoryTier {
    val memoryInfo = ActivityManager.MemoryInfo()
    activityManager.getMemoryInfo(memoryInfo)
    return when (memoryInfo.totalMem.toDouble() / (1024 * 1024 * 1024)) {
      in 0.00..2.00 -> OppiaMetricLog.MemoryTier.LOW_MEMORY_TIER
      in 2.00..3.00 -> OppiaMetricLog.MemoryTier.MEDIUM_MEMORY_TIER
      else -> OppiaMetricLog.MemoryTier.HIGH_MEMORY_TIER
    }
  }
}
