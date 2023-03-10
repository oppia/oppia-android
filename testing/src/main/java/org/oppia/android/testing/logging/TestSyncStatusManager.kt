package org.oppia.android.testing.logging

import org.oppia.android.app.model.OppiaEventLogs
import org.oppia.android.data.persistence.PersistentCacheStore
import org.oppia.android.util.data.AsyncDataSubscriptionManager
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.transform
import org.oppia.android.util.logging.SyncStatusManager
import org.oppia.android.util.logging.SyncStatusManager.SyncStatus
import org.oppia.android.util.logging.SyncStatusManager.SyncStatus.DATA_UPLOADED
import org.oppia.android.util.logging.SyncStatusManager.SyncStatus.DATA_UPLOADING
import org.oppia.android.util.logging.SyncStatusManager.SyncStatus.INITIAL_UNKNOWN
import org.oppia.android.util.logging.SyncStatusManager.SyncStatus.UPLOAD_ERROR
import org.oppia.android.util.logging.SyncStatusManagerImpl
import org.oppia.android.util.threading.AtomicEnum
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject
import javax.inject.Singleton

private const val STATUS_PROVIDER_ID = "TestSyncStatusManager.sync_status_provider"

/**
 * A test-only implementation of [SyncStatusManager] that uses a real implementation internally, but
 * tracks all sync statuses that are either produced by the real [SyncStatusManagerImpl] or are
 * forced via calls to [forceSyncStatus]. These statuses can be observed using [getSyncStatuses].
 */
@Singleton
class TestSyncStatusManager @Inject constructor(
  private val syncStatusManagerImpl: SyncStatusManagerImpl,
  private val asyncDataSubscriptionManager: AsyncDataSubscriptionManager
) : SyncStatusManager {
  // Include the initial status of the manager (since it won't be observed as a "change").
  private val trackedSyncStatuses = CopyOnWriteArrayList(listOf(INITIAL_UNKNOWN))
  private val syncStatusProvider =
    syncStatusManagerImpl.getSyncStatus().also { syncStatusProvider ->
      asyncDataSubscriptionManager.subscribe(syncStatusProvider.getId()) {
        (syncStatusProvider.retrieveData() as? AsyncResult.Success)?.let {
          maybeTrackNewSyncStatus(it.value)
        }
      }
    }.transform(STATUS_PROVIDER_ID, ::augmentSyncStatus)
  private val forcedSyncStatus = AtomicReference<SyncStatus?>(null)
  private val latestSyncStatus = AtomicEnum.create(INITIAL_UNKNOWN)

  override fun getSyncStatus() = syncStatusProvider

  override fun initializeEventLogStore(eventLogStore: PersistentCacheStore<OppiaEventLogs>) {
    syncStatusManagerImpl.initializeEventLogStore(eventLogStore)
  }

  override fun reportUploadingStarted() {
    maybeTrackNewSyncStatus(DATA_UPLOADING)
    syncStatusManagerImpl.reportUploadingStarted()
  }

  override fun reportUploadingEnded() {
    maybeTrackNewSyncStatus(DATA_UPLOADED)
    syncStatusManagerImpl.reportUploadingEnded()
  }

  override fun reportUploadError() {
    maybeTrackNewSyncStatus(UPLOAD_ERROR)
    syncStatusManagerImpl.reportUploadError()
  }

  /**
   * Forces [getSyncStatus] to return the specified [syncStatus].
   *
   * The status will also be logged as part of the statuses returned by [getSyncStatuses].
   *
   * To revert the status back to the implementation's computed value, use [resetForcedSyncStatus].
   */
  fun forceSyncStatus(syncStatus: SyncStatus) {
    maybeTrackNewSyncStatus(syncStatus, isForced = true)
    updateForcedSyncStatus(syncStatus)
  }

  /**
   * Resets the forced sync status set via [forceSyncStatus], if any, such that [getSyncStatus] will
   * next return the correct computed sync status.
   */
  fun resetForcedSyncStatus() {
    updateForcedSyncStatus(syncStatus = null)
  }

  /**
   * Returns the list of all [SyncStatus]es that were computed via [SyncStatusManagerImpl] or forced
   * via [forceSyncStatus].
   *
   * Note that the order of values in the returned list will match the order that statuses were
   * computed, or forced. However, the asynchronous nature of sync statuses is such that there may
   * be intermittent statuses reported "between" other changed events (such as
   * [SyncStatus.NO_CONNECTIVITY] which sometimes will appear between other uploading statuses).
   *
   * Note also that changes to network connectivity will **not** result in changes to tracked
   * statuses unless that change is also observed via the underlying implementation. See
   * [SyncStatusManager.getSyncStatus] for more context.
   */
  fun getSyncStatuses(): List<SyncStatus> = trackedSyncStatuses.toList()

  private fun updateForcedSyncStatus(syncStatus: SyncStatus?) {
    forcedSyncStatus.set(syncStatus)
    asyncDataSubscriptionManager.notifyChangeAsync(STATUS_PROVIDER_ID)
  }

  private fun maybeTrackNewSyncStatus(syncStatus: SyncStatus, isForced: Boolean = false) {
    // It's possible for INITIAL_UNKNOWN to be delivered in slightly race condition-esque
    // situations. While this is valid from SyncStatusManager's point of view (since the race
    // condition will very quickly resolve itself due to DataProvider's eventual consistency
    // property), it adds extra noise to tests that run into this situation. To make this a bit
    // cleaner, filter out reversions back to "INITIAL_UNKNOWN" (unless they are forced).
    if (!isForced && syncStatus == INITIAL_UNKNOWN) return
    if (latestSyncStatus.getAndSet(syncStatus) != syncStatus) {
      trackedSyncStatuses += syncStatus
    }
  }

  private fun augmentSyncStatus(syncStatus: SyncStatus) = forcedSyncStatus.get() ?: syncStatus
}
