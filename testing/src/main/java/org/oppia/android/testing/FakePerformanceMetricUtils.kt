package org.oppia.android.testing

import javax.inject.Inject
import javax.inject.Singleton
import org.oppia.android.app.model.OppiaMetricLog
import org.oppia.android.util.logging.performancemetrics.PerformanceMetricsUtils

/** A test specific fake for performance metric utils. */
@Singleton
class FakePerformanceMetricUtils @Inject constructor(): PerformanceMetricsUtils {
  internal var testApkSize = 0L
  internal var testStorageUsage = 0L
  internal var testTotalPss = 0L
  internal var testTotalBytesSent = 0L
  internal var testTotalReceivedBytes = 0L
  internal var testDeviceStorageTier = OppiaMetricLog.StorageTier.MEDIUM_STORAGE
  internal var testDeviceMemoryTier = OppiaMetricLog.MemoryTier.MEDIUM_MEMORY_TIER

  override fun getApkSize(): Long = testApkSize

  override fun getUsedStorage(): Long = testStorageUsage

  override fun getTotalPssUsed(): Long = testTotalPss

  override fun getTotalSentBytes(): Long = testTotalBytesSent

  override fun getTotalReceivedBytes(): Long = testTotalReceivedBytes

  override fun getDeviceStorageTier(): OppiaMetricLog.StorageTier = testDeviceStorageTier

  override fun getDeviceMemoryTier(): OppiaMetricLog.MemoryTier = testDeviceMemoryTier

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
}