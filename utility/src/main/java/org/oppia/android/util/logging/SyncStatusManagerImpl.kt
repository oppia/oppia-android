package org.oppia.android.util.logging

import org.oppia.android.util.data.AsyncDataSubscriptionManager
import org.oppia.android.util.data.DataProvider
import org.oppia.android.util.data.DataProviders
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject
import javax.inject.Singleton

private const val SYNC_STATUS_PROVIDER_ID = "SyncStatusManagerImpl.sync_status"

/** Manager for handling the sync status of the device during log upload to the remote service.*/
@Singleton
class SyncStatusManagerImpl @Inject constructor(
  private val dataProviders: DataProviders,
  private val asyncDataSubscriptionManager: AsyncDataSubscriptionManager
) : SyncStatusManager {
  // TODO(#4249): Replace this with a StateFlow & the DataProvider with a StateFlow-converted one.
  private var syncStatus = AtomicReference(SyncStatusManager.SyncStatus.INITIAL_UNKNOWN)

  override fun getSyncStatus(): DataProvider<SyncStatusManager.SyncStatus> =
    dataProviders.createInMemoryDataProvider(SYNC_STATUS_PROVIDER_ID) { syncStatus.get() }

  override fun setSyncStatus(syncStatus: SyncStatusManager.SyncStatus) {
    this.syncStatus.set(syncStatus)
    asyncDataSubscriptionManager.notifyChangeAsync(SYNC_STATUS_PROVIDER_ID)
  }
}
