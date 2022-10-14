package org.oppia.android.util.logging.performancemetrics

import android.app.ActivityManager
import android.content.Context
import android.net.TrafficStats
import android.os.Process
import org.oppia.android.app.model.OppiaMetricLog
import org.oppia.android.app.model.OppiaMetricLog.MemoryTier.HIGH_MEMORY_TIER
import org.oppia.android.app.model.OppiaMetricLog.MemoryTier.LOW_MEMORY_TIER
import org.oppia.android.app.model.OppiaMetricLog.MemoryTier.MEDIUM_MEMORY_TIER
import org.oppia.android.app.model.OppiaMetricLog.StorageTier.HIGH_STORAGE
import org.oppia.android.app.model.OppiaMetricLog.StorageTier.LOW_STORAGE
import org.oppia.android.app.model.OppiaMetricLog.StorageTier.MEDIUM_STORAGE
import org.oppia.android.util.system.OppiaClock
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/** Utility to extract performance metrics from the underlying Android system. */
@Singleton
class PerformanceMetricsAssessorImpl @Inject constructor(
  private val context: Context,
  private val oppiaClock: OppiaClock,
  @LowStorageTierUpperBound private val lowStorageTierUpperBound: Long,
  @MediumStorageTierUpperBound private val mediumStorageTierUpperBound: Long,
  @LowMemoryTierUpperBound private val lowMemoryTierUpperBound: Long,
  @MediumMemoryTierUpperBound private val mediumMemoryTierUpperBound: Long
) : PerformanceMetricsAssessor {

  private val activityManager: ActivityManager by lazy {
    context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
  }

  override fun getApkSize(): Long {
    val apkPath =
      context.packageManager.getPackageInfo(context.packageName, 0).applicationInfo.sourceDir
    return File(apkPath).length()
  }

  override fun getUsedStorage(): Long {
    val permanentStorageUsage = context.filesDir?.listFiles()?.map { it.length() }?.sum() ?: 0L
    val cacheStorageUsage = context.cacheDir?.listFiles()?.map { it.length() }?.sum() ?: 0L
    return permanentStorageUsage + cacheStorageUsage
  }

  override fun getTotalSentBytes(): Long = TrafficStats.getUidTxBytes(context.applicationInfo.uid)

  override fun getTotalReceivedBytes(): Long =
    TrafficStats.getUidRxBytes(context.applicationInfo.uid)

  override fun getTotalPssUsed(): Long {
    val pid = Process.myPid()
    val processMemoryInfo = activityManager.getProcessMemoryInfo(intArrayOf(pid))
    return processMemoryInfo?.map { it.totalPss }?.sum()?.toLong() ?: 0L
  }

  override fun getDeviceStorageTier(): OppiaMetricLog.StorageTier {
    val usedStorage = getUsedStorage()
    return when {
      usedStorage <= lowStorageTierUpperBound -> LOW_STORAGE
      usedStorage <= mediumStorageTierUpperBound -> MEDIUM_STORAGE
      else -> HIGH_STORAGE
    }
  }

  override fun getDeviceMemoryTier(): OppiaMetricLog.MemoryTier {
    val memoryInfo = ActivityManager.MemoryInfo()
    activityManager.getMemoryInfo(memoryInfo)
    val totalMemory = memoryInfo.totalMem
    return when {
      totalMemory <= lowMemoryTierUpperBound -> LOW_MEMORY_TIER
      totalMemory <= mediumMemoryTierUpperBound -> MEDIUM_MEMORY_TIER
      else -> HIGH_MEMORY_TIER
    }
  }
}
