package org.oppia.android.util.logging

import org.oppia.android.app.model.OppiaEventLogs
import org.oppia.android.util.data.DataProvider

/**
 * Manager for handling the sync status of the device during analytics log uploads.
 *
 * Implementations of this class are safe to use across threads.
 */
interface SyncStatusManager {
  /**
   * Returns the current [SyncStatus] of the device, as computed by current system conditions (such
   * as network connectivity). The returned [DataProvider] will generally automatically update when
   * a new status is known.
   *
   * Note one exception: when network connectivity changes, this will not result in a change to the
   * status unless other status change is reported (such as via [reportUploadingStarted], or the
   * other similar functions below).
   */
  fun getSyncStatus(): DataProvider<SyncStatus>

  /**
   * Initializes this manager with the specified [eventLogStore] that is assumed to track all events
   * for the lifetime of the application.
   *
   * This function must be called in order for [getSyncStatus] to behave correctly, and it must not
   * be called more than once. It can be called before or after [getSyncStatus].
   */
  fun initializeEventLogStore(eventLogStore: DataProvider<OppiaEventLogs>)

  /** Indicates that upstream code is beginning an event upload operation. */
  fun reportUploadingStarted()

  /** Indicates that upstream code has ended an event upload operation. */
  fun reportUploadingEnded()

  /** Indicates that upstream code encountered an upload error while trying to upload events. */
  fun reportUploadError()

  /** The sync status values corresponding to different stages of uploading logging analytics. */
  enum class SyncStatus {
    /**
     * The initial state where the current upload state is unknown. This should be interpreted to
     * mean that events are waiting to be logged until a more specific status is received or
     * determined.
     */
    INITIAL_UNKNOWN,

    /**
     * Indicates that there are events waiting to be uploaded, but the operation hasn't started yet
     * (either manually or via the automatic background uploader).
     */
    WAITING_TO_START_UPLOADING,

    /** Indicates that analytics are currently being uploaded. */
    DATA_UPLOADING,

    /**
     * Indicates that analytics have recently successfully finished uploading, and that no new
     * analytics are pending upload.
     */
    DATA_UPLOADED,

    /**
     * Indicates that the network is currently unavailable and that logs will be attempted to be
     * uploaded once connectivity resumes.
     *
     * Note that this will take priority over all other sync statuses until network connectivity is
     * restored, including [UPLOAD_ERROR].
     */
    NO_CONNECTIVITY,

    /**
     * Indicates an unresolvable error was encountered during analytics upload, and the logs may be
     * attempted to be re-uploaded at a later time.
     */
    UPLOAD_ERROR
  }
}
