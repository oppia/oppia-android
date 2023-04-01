package org.oppia.android.util.logging

import org.oppia.android.app.model.OppiaEventLogs
import org.oppia.android.util.data.AsyncDataSubscriptionManager
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProvider
import org.oppia.android.util.data.DataProviders
import org.oppia.android.util.data.DataProviders.Companion.transformNested
import org.oppia.android.util.logging.SyncStatusManager.SyncStatus
import org.oppia.android.util.logging.SyncStatusManager.SyncStatus.DATA_UPLOADED
import org.oppia.android.util.logging.SyncStatusManager.SyncStatus.DATA_UPLOADING
import org.oppia.android.util.logging.SyncStatusManager.SyncStatus.INITIAL_UNKNOWN
import org.oppia.android.util.logging.SyncStatusManager.SyncStatus.NO_CONNECTIVITY
import org.oppia.android.util.logging.SyncStatusManager.SyncStatus.UPLOAD_ERROR
import org.oppia.android.util.logging.SyncStatusManager.SyncStatus.WAITING_TO_START_UPLOADING
import org.oppia.android.util.networking.NetworkConnectionUtil
import org.oppia.android.util.networking.NetworkConnectionUtil.ProdConnectionStatus.NONE
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject
import javax.inject.Singleton

private const val SYNC_STATUS_PROVIDER_ID = "SyncStatusManagerImpl.sync_status"
private const val DEFAULT_EVENT_LOG_PROVIDER_ID = "SyncStatusManagerImpl.default_event_logs"

/** Manager for handling the sync status of the device during log upload to the remote service.*/
@Singleton
class SyncStatusManagerImpl @Inject constructor(
  private val dataProviders: DataProviders,
  private val networkConnectionUtil: NetworkConnectionUtil,
  private val asyncDataSubscriptionManager: AsyncDataSubscriptionManager
) : SyncStatusManager {
  private var eventLogStore: DataProvider<OppiaEventLogs>? = null
  private val eventLogsDataProvider: DataProvider<OppiaEventLogs> get() {
    return eventLogStore ?: dataProviders.createInMemoryDataProvider(
      DEFAULT_EVENT_LOG_PROVIDER_ID, OppiaEventLogs::getDefaultInstance
    )
  }
  private val configurableSyncStatus by lazy {
    eventLogsDataProvider.transformNested(SYNC_STATUS_PROVIDER_ID) {
      AsyncResult.Success(INITIAL_UNKNOWN)
    }
  }
  private val transientState = AtomicReference(TransientState.NOT_UPLOADING)

  override fun getSyncStatus() = configurableSyncStatus

  override fun initializeEventLogStore(eventLogStore: DataProvider<OppiaEventLogs>) {
    check(this.eventLogStore == null) { "Attempting to initialize the log store a second time." }
    this.eventLogStore = eventLogStore
    // Note that changing the base provider automatically notifies subscribers, so no need to do
    // that again.
    configurableSyncStatus.setBaseDataProvider(eventLogsDataProvider) { computeSyncStatus(it) }
  }

  override fun reportUploadingStarted() {
    reportStatusChange(TransientState.UPLOADING)
  }

  override fun reportUploadingEnded() {
    reportStatusChange(TransientState.NOT_UPLOADING)
  }

  override fun reportUploadError() {
    reportStatusChange(TransientState.UPLOAD_FAILED)
  }

  private fun reportStatusChange(newStatus: TransientState) {
    transientState.set(newStatus)
    asyncDataSubscriptionManager.notifyChangeAsync(SYNC_STATUS_PROVIDER_ID)
  }

  private fun computeSyncStatus(oppiaEventLogs: OppiaEventLogs): AsyncResult<SyncStatus> {
    // Current network activity only matters when event logs change (since that's when the decision
    // to upload/cache events takes place).
    val hasConnectivity = networkConnectionUtil.getCurrentConnectionStatus() != NONE
    val hasEventsToUpload = oppiaEventLogs.eventLogsToUploadList.isNotEmpty()
    val hasUploadedEvents = oppiaEventLogs.uploadedEventLogsList.isNotEmpty()
    return when (transientState.get()) {
      TransientState.NOT_UPLOADING -> when {
        hasEventsToUpload -> if (hasConnectivity) WAITING_TO_START_UPLOADING else NO_CONNECTIVITY
        hasUploadedEvents -> DATA_UPLOADED
        else -> INITIAL_UNKNOWN // Nothing has been uploaded yet.
      }
      TransientState.UPLOADING -> if (hasConnectivity) DATA_UPLOADING else NO_CONNECTIVITY
      TransientState.UPLOAD_FAILED, null -> if (hasConnectivity) UPLOAD_ERROR else NO_CONNECTIVITY
    }.let { AsyncResult.Success(it) }
  }

  /**
   * Represents transient state that can be explicitly indicated by callers that directly manage
   * event caching & uploading.
   */
  private enum class TransientState {
    /** Indicates that upstream code is not currently uploading events. */
    NOT_UPLOADING,

    /** Indicates that upstream code is currently uploading events. */
    UPLOADING,

    /**
     * Indicates that upstream code has failed when trying to upload events due to an unresolvable
     * error.
     */
    UPLOAD_FAILED
  }
}
