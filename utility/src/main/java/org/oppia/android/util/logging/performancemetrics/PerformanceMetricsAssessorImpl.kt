package org.oppia.android.util.logging.performancemetrics

import android.app.ActivityManager
import android.content.Context
import android.net.TrafficStats
import android.os.Build
import android.os.Process
import android.system.Os
import android.system.OsConstants
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import org.oppia.android.app.model.OppiaMetricLog
import org.oppia.android.app.model.OppiaMetricLog.MemoryTier.HIGH_MEMORY_TIER
import org.oppia.android.app.model.OppiaMetricLog.MemoryTier.LOW_MEMORY_TIER
import org.oppia.android.app.model.OppiaMetricLog.MemoryTier.MEDIUM_MEMORY_TIER
import org.oppia.android.app.model.OppiaMetricLog.StorageTier.HIGH_STORAGE
import org.oppia.android.app.model.OppiaMetricLog.StorageTier.LOW_STORAGE
import org.oppia.android.app.model.OppiaMetricLog.StorageTier.MEDIUM_STORAGE
import org.oppia.android.util.logging.performancemetrics.PerformanceMetricsAssessor.CpuSnapshot
import org.oppia.android.util.system.OppiaClock

/** Utility to extract performance metrics from the underlying Android system. */
@Singleton
class PerformanceMetricsAssessorImpl @Inject constructor(
  private val oppiaClock: OppiaClock,
  private val context: Context,
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

  override fun computeCpuSnapshotAtCurrentTime(): CpuSnapshot {
    return CpuSnapshot(
      appTimeMillis = oppiaClock.getCurrentTimeMs(),
      cpuTimeMillis = Process.getElapsedCpuTime(),
      numberOfOnlineCores = getNumberOfOnlineCores()
    )
  }

  override fun getRelativeCpuUsage(
    firstCpuSnapshot: CpuSnapshot,
    secondCpuSnapshot: CpuSnapshot
  ): Double? {
    val deltaCpuTimeMs = if (secondCpuSnapshot.cpuTimeMillis >= firstCpuSnapshot.cpuTimeMillis) {
      secondCpuSnapshot.cpuTimeMillis - firstCpuSnapshot.cpuTimeMillis
    } else { return null }
    val deltaProcessTimeMs = if (secondCpuSnapshot.appTimeMillis > firstCpuSnapshot.appTimeMillis) {
      secondCpuSnapshot.appTimeMillis - firstCpuSnapshot.appTimeMillis
    } else { return null }
    val numberOfCores =
      if (secondCpuSnapshot.numberOfOnlineCores >= 1 && firstCpuSnapshot.numberOfOnlineCores >= 1) {
        (secondCpuSnapshot.numberOfOnlineCores + firstCpuSnapshot.numberOfOnlineCores) / 2.0
      } else { return null }

    return when (val relativeCpuUsage = deltaCpuTimeMs / (deltaProcessTimeMs * numberOfCores)) {
      in 0.0..1.0 -> relativeCpuUsage
      else -> { null }
    }
  }

  /** Returns the number of processors that are currently online/available. */
  private fun getNumberOfOnlineCores(): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      // Returns the number of processors currently available in the system. This may be less than
      // the total number of configured processors because some of them may be offline. It must also
      // be noted that a similar OsConstant, _SC_NPROCESSORS_CONF also exists which provides the
      // total number of configured processors in the system. This value is similar to that
      // returned from Runtime.getRuntime().availableProcessors().
      // Reference: https://man7.org/linux/man-pages/man3/sysconf.3.html
      Os.sysconf(OsConstants._SC_NPROCESSORS_ONLN).toInt()
    } else {
      // Returns the maximum number of processors available. This value is never smaller than one.
      Runtime.getRuntime().availableProcessors()
    }
  }
}
