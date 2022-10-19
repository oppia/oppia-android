package org.oppia.android.testing

import org.oppia.android.app.model.OppiaMetricLog
import org.oppia.android.util.logging.performancemetrics.PerformanceMetricsAssessor
import org.oppia.android.util.logging.performancemetrics.PerformanceMetricsAssessor.CpuSnapshot
import javax.inject.Inject
import javax.inject.Singleton

/** A test specific fake for performance metric utils. */
@Singleton
class FakePerformanceMetricAssessor @Inject constructor() : PerformanceMetricsAssessor {
  private var testApkSize = 0L
  private var testStorageUsage = 0L
  private var testTotalPss = 0L
  private var testTotalBytesSent = 0L
  private var testTotalReceivedBytes = 0L
  private var testDeviceStorageTier = OppiaMetricLog.StorageTier.MEDIUM_STORAGE
  private var testDeviceMemoryTier = OppiaMetricLog.MemoryTier.MEDIUM_MEMORY_TIER
  private var testRelativeCpuUsage = 0.00

  override fun getApkSize(): Long = testApkSize

  override fun getUsedStorage(): Long = testStorageUsage

  override fun getTotalPssUsed(): Long = testTotalPss

  override fun getTotalSentBytes(): Long = testTotalBytesSent

  override fun getTotalReceivedBytes(): Long = testTotalReceivedBytes

  override fun getDeviceStorageTier(): OppiaMetricLog.StorageTier = testDeviceStorageTier

  override fun getDeviceMemoryTier(): OppiaMetricLog.MemoryTier = testDeviceMemoryTier

  override fun computeCpuSnapshotAtCurrentTime(): CpuSnapshot = CpuSnapshot(0L, 0L, 1)

  override fun getRelativeCpuUsage(
    firstCpuSnapshot: CpuSnapshot,
    secondCpuSnapshot: CpuSnapshot
  ): Double {
    return testRelativeCpuUsage
  }

  /** Sets [apkSize] as the value of [testApkSize]. */
  fun setApkSize(apkSize: Long) {
    testApkSize = apkSize
  }

  /** Sets [storageUsage] as the value of [testStorageUsage]. */
  fun setStorageUsage(storageUsage: Long) {
    testStorageUsage = storageUsage
  }

  /** Sets [totalPss] as the value of [testTotalPss]. */
  fun setTotalPss(totalPss: Long) {
    testTotalPss = totalPss
  }

  /** Sets [sentBytes] as the value of [testTotalBytesSent]. */
  fun setTotalSentBytes(sentBytes: Long) {
    testTotalBytesSent = sentBytes
  }

  /** Sets [receivedBytes] as the value of [testTotalReceivedBytes]. */
  fun setTotalReceivedBytes(receivedBytes: Long) {
    testTotalReceivedBytes = receivedBytes
  }

  /** Sets [storageTier] as the value of [testDeviceStorageTier]. */
  fun setDeviceStorageTier(storageTier: OppiaMetricLog.StorageTier) {
    testDeviceStorageTier = storageTier
  }

  /** Sets [memoryTier] as the value of [testDeviceMemoryTier]. */
  fun setDeviceMemoryTier(memoryTier: OppiaMetricLog.MemoryTier) {
    testDeviceMemoryTier = memoryTier
  }

  /** Sets [relativeCpuUsage] as the value of [testRelativeCpuUsage]. */
  fun setRelativeCpuUsage(relativeCpuUsage: Double) {
    testRelativeCpuUsage = relativeCpuUsage
  }
}
