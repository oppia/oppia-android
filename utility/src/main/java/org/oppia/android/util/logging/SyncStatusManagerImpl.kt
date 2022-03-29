package org.oppia.android.util.logging

import kotlinx.coroutines.flow.MutableStateFlow
import org.oppia.android.util.data.DataProvider
import org.oppia.android.util.data.DataProviders
import org.oppia.android.util.logging.SyncStatusManager.SyncStatus
import javax.inject.Inject
import javax.inject.Singleton

private const val SYNC_STATUS_PROVIDER_ID = "SyncStatusManagerImpl.sync_status"

/** Manager for handling the sync status of the device during log upload to the remote service.*/
@Singleton
class SyncStatusManagerImpl @Inject constructor(
  private val dataProviders: DataProviders
) : SyncStatusManager {
  private val syncStatusFlow = MutableStateFlow(SyncStatus.INITIAL_UNKNOWN)

  override fun getSyncStatus(): DataProvider<SyncStatus> = dataProviders.run {
    syncStatusFlow.convertToAutomaticDataProvider(SYNC_STATUS_PROVIDER_ID)
  }

  override fun setSyncStatus(syncStatus: SyncStatus) {
    check(syncStatusFlow.tryEmit(syncStatus)) { "Failed to update sync status to $syncStatus" }
  }
}
