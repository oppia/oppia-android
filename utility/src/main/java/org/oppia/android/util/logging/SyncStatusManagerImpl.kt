package org.oppia.android.util.logging

import org.oppia.android.util.data.AsyncDataSubscriptionManager
import org.oppia.android.util.data.DataProvider
import org.oppia.android.util.data.DataProviders
import javax.inject.Inject
import javax.inject.Singleton

private const val SYNC_STATUS_PROVIDER_ID = "sync_status_provider_id"

/** Manager for handling the sync status of the device during log upload to the remote service.*/
@Singleton
class SyncStatusManagerImpl @Inject constructor(
  private val dataProviders: DataProviders,
  private val asyncDataSubscriptionManager: AsyncDataSubscriptionManager
): SyncStatusManager {
  private var syncStatus: SyncStatusManager.SyncStatus = SyncStatusManager.SyncStatus.DEFAULT

  override fun getSyncStatus(): DataProvider<SyncStatusManager.SyncStatus> =
    dataProviders.createInMemoryDataProvider(SYNC_STATUS_PROVIDER_ID) { syncStatus }

  /**
   * Changes the current [SyncStatus] of the device to [syncStatus] and notifies the data provider
   * of this change.
   */
  override fun setSyncStatus(syncStatus: SyncStatusManager.SyncStatus) {
    this.syncStatus = syncStatus
    asyncDataSubscriptionManager.notifyChangeAsync(SYNC_STATUS_PROVIDER_ID)
  }
}
