package org.oppia.android.testing

import org.oppia.android.app.model.OppiaMetricLog
import org.oppia.android.util.logging.performancemetrics.PerformanceMetricsAssessor
import javax.inject.Inject
import javax.inject.Singleton
import org.oppia.android.app.model.ApplicationState
import org.oppia.android.app.model.CpuUsageParameters

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
  private var testCpuUsage = 0.00

  override fun getApkSize(): Long = testApkSize

  override fun getUsedStorage(): Long = testStorageUsage

  override fun getTotalPssUsed(): Long = testTotalPss

  override fun getTotalSentBytes(): Long = testTotalBytesSent

  override fun getTotalReceivedBytes(): Long = testTotalReceivedBytes

  override fun getDeviceStorageTier(): OppiaMetricLog.StorageTier = testDeviceStorageTier

  override fun getDeviceMemoryTier(): OppiaMetricLog.MemoryTier = testDeviceMemoryTier

  override fun getRelativeCpuUsage(
    cpuUsageAtStartOfTimeWindow: CpuUsageParameters,
    cpuUsageAtEndOfTimeWindow: CpuUsageParameters
  ): Double = testCpuUsage

  override fun getCurrentCpuUsageParameters(
    currentApplicationState: ApplicationState
  ): CpuUsageParameters = CpuUsageParameters.getDefaultInstance()

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

  /** Sets [relativeCpuUsage] as the value of [testCpuUsage]. */
  fun setRelativeCpuUsage(relativeCpuUsage: Double) {
    testCpuUsage = relativeCpuUsage
  }
}
