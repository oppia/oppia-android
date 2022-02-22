package org.oppia.android.testing

import javax.inject.Inject
import javax.inject.Singleton
import org.oppia.android.util.data.DataProvider
import org.oppia.android.util.logging.SyncStatusManager
import org.oppia.android.util.logging.SyncStatusManagerImpl

/** A test-specific fake for the Sync Manager. */
@Singleton
class FakeSyncStatusManager @Inject constructor(
  private val syncStatusManagerImpl: SyncStatusManagerImpl
): SyncStatusManager {

  private val syncStatusList = ArrayList<SyncStatusManager.SyncStatus>()

  override fun getSyncStatus(): DataProvider<SyncStatusManager.SyncStatus> {
    return syncStatusManagerImpl.getSyncStatus()
  }

  override fun setSyncStatus(syncStatus: SyncStatusManager.SyncStatus) {
    syncStatusList.add(syncStatus)
  }

  /** Returns the list of sync statuses that were set during execution. */
  fun getSyncStatusList() = syncStatusList
}
