package org.oppia.android.util.logging

import org.oppia.android.util.data.DataProvider

/** Manager for handling the sync status of the device during log uploads. */
interface SyncStatusManager {

  /** Returns the current [SyncStatus] of the device. */
  fun getSyncStatus(): DataProvider<SyncStatus>

  /** Changes the current [SyncStatus] of the device to [syncStatus]. */
  fun setSyncStatus(syncStatus: SyncStatus)

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
