package org.oppia.android.util.logging

import org.oppia.android.util.data.AsyncDataSubscriptionManager
import org.oppia.android.util.data.DataProvider
import org.oppia.android.util.data.DataProviders
import javax.inject.Inject
import javax.inject.Singleton

private const val SYNC_STATUS_PROVIDER_ID = "sync_status_provider_id"

/** Controller for handling the sync status of the device with the remote service. */
@Singleton
class SyncStatusManager @Inject constructor(
  private val dataProviders: DataProviders,
  private val asyncDataSubscriptionManager: AsyncDataSubscriptionManager
) {
  private var syncStatus: SyncStatus = SyncStatus.DEFAULT

  /** Returns the current [SyncStatus] of the device. */
  fun getSyncStatus(): DataProvider<SyncStatus> =
    dataProviders.createInMemoryDataProvider(SYNC_STATUS_PROVIDER_ID) { syncStatus }

  /**
   * Changes the current [SyncStatus] of the device to [syncStatus] and notifies the data provider
   * of this change.
   */
  fun setSyncStatus(syncStatus: SyncStatus) {
    this.syncStatus = syncStatus
    asyncDataSubscriptionManager.notifyChangeAsync(SYNC_STATUS_PROVIDER_ID)
  }

  /**
   * Enum class containing the sync status values corresponding to different stages of data upload.
   */
  enum class SyncStatus {
    DEFAULT,
    DATA_UPLOADED,
    DATA_UPLOADING,
    NETWORK_ERROR
  }
}
