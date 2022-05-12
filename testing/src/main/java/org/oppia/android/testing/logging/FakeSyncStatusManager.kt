package org.oppia.android.testing.logging

import org.oppia.android.util.data.DataProvider
import org.oppia.android.util.logging.SyncStatusManager
import org.oppia.android.util.logging.SyncStatusManagerImpl
import java.util.concurrent.CopyOnWriteArrayList
import javax.inject.Inject
import javax.inject.Singleton

/**
 * A test-only implementation of [SyncStatusManager] that uses a real implementation internally, but
 * tracks all sync status set via [setSyncStatus].
 */
@Singleton
class FakeSyncStatusManager @Inject constructor(
  private val syncStatusManagerImpl: SyncStatusManagerImpl
) : SyncStatusManager {
  private val trackedSyncStatuses = CopyOnWriteArrayList<SyncStatusManager.SyncStatus>()

  override fun getSyncStatus(): DataProvider<SyncStatusManager.SyncStatus> =
    syncStatusManagerImpl.getSyncStatus()

  override fun setSyncStatus(syncStatus: SyncStatusManager.SyncStatus) {
    trackedSyncStatuses += syncStatus
    syncStatusManagerImpl.setSyncStatus(syncStatus)
  }

  /**
   * Returns the list of all [SyncStatusManager.SyncStatus]s that were set via [setSyncStatus].
   *
   * Note that the order of values in the returned list will match the order [setSyncStatus] was
   * called but may not match the exact order or presence of values propagated to the [DataProvider]
   * returned by [getSyncStatus] (since [DataProvider]s guarantee eventual consistency and may skip
   * values).
   */
  fun getSyncStatuses(): List<SyncStatusManager.SyncStatus> = trackedSyncStatuses
}
