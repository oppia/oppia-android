package org.oppia.android.util.logging

import org.oppia.android.util.data.DataProvider

/**
 * Manager for handling the sync status of the device during analytics log uploads.
 *
 * Implementations of this class are safe to use across threads.
 */
interface SyncStatusManager {
  /**
   * Returns the current [SyncStatus] of the device.
   *
   * This returns a different [DataProvider] for each call, but they will be updated due to changes
   * to the sync status via [setSyncStatus].
   */
  fun getSyncStatus(): DataProvider<SyncStatus>

  /**
   * Changes the current [SyncStatusManager.SyncStatus] of the device to [syncStatus] and notifies
   * the data provider returned by [getSyncStatus] of this change.
   */
  fun setSyncStatus(syncStatus: SyncStatus)

  /** The sync status values corresponding to different stages of uploading logging analytics. */
  enum class SyncStatus {
    /** The initial state where the current upload state is unknown. */
    INITIAL_UNKNOWN,

    /** Indicates that analytics are currently being uploaded. */
    DATA_UPLOADING,

    /**
     * Indicates that analytics have recently successfully finished uploading, and that no new
     * analytics are pending upload.
     */
    DATA_UPLOADED,

    /**
     * Indicates a network error was encountered during analytics upload, and the logs may be
     * attempted to be re-uploaded at a later time.
     */
    NETWORK_ERROR,

    /**
     * Indicates that the network is currently unavailable and that logs will be attempted to be
     * uploaded once connectivity resumes.
     */
    NO_CONNECTIVITY
  }
}
