package org.oppia.android.util.logging

import android.net.ConnectivityManager
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.junit.Test
import org.oppia.android.app.model.EventLog
import org.oppia.android.app.model.OppiaEventLogs
import org.oppia.android.data.persistence.PersistentCacheStore
import org.oppia.android.data.persistence.PersistentCacheStore.PublishMode.PUBLISH_TO_IN_MEMORY_CACHE
import org.oppia.android.data.persistence.PersistentCacheStore.UpdateMode.UPDATE_ALWAYS
import org.oppia.android.testing.assertThrows
import org.oppia.android.testing.data.DataProviderTestMonitor
import org.oppia.android.testing.networking.NetworkConnectionTestUtil
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.logging.SyncStatusManager.SyncStatus

/**
 * Tests for [SyncStatusManager]. This provides the common tests needed to verify the manager's API
 * regardless of the implementation. This suite should not be used directly, and instead should only
 * be used by implementation test suites of [SyncStatusManager].
 *
 * NOTE TO DEVELOPERS: This is essentially the only time inheritance is good practice for tests
 * since JUnit doesn't provide a way to compose the tests themselves, only behaviors around the
 * tests.
 */
// Same parameter value: helpers reduce test context, even if they are used by 1 test.
// Function name: test names are conventionally named with underscores.
@Suppress("SameParameterValue", "FunctionName")
abstract class SyncStatusManagerTestBase {
  protected abstract val impl: SyncStatusManager
  protected abstract val monitorFactory: DataProviderTestMonitor.Factory
  protected abstract val persistentCacheStoreFactory: PersistentCacheStore.Factory
  protected abstract val networkConnectionTestUtil: NetworkConnectionTestUtil
  protected abstract val testCoroutineDispatchers: TestCoroutineDispatchers
  protected abstract val backgroundDispatcher: CoroutineDispatcher

  private val backgroundDispatcherScope by lazy { CoroutineScope(backgroundDispatcher) }
  protected val logsCacheStore by lazy { createPrimedLogStore() }

  @Test
  fun testGetSyncStatus_initialState_returnsInitialUnknown() {
    val syncStatusProvider = impl.getSyncStatus()

    val syncStatus = monitorFactory.waitForNextSuccessfulResult(syncStatusProvider)
    assertThat(syncStatus).isEqualTo(SyncStatus.INITIAL_UNKNOWN)
  }

  @Test
  fun testInitializeEventLogStore_twice_throwsException() {
    impl.initializeEventLogStore(logsCacheStore)

    val error = assertThrows<IllegalStateException>() {
      impl.initializeEventLogStore(logsCacheStore)
    }

    assertThat(error)
      .hasMessageThat()
      .contains("Attempting to initialize the log store a second time.")
  }

  @Test
  fun testGetSyncStatus_withInitedLogStore_noConnectivity_noEvents_returnsInitialUnknown() {
    val syncStatusProvider = impl.getSyncStatus()
    impl.initializeEventLogStore(logsCacheStore)
    disconnectNetwork()

    val syncStatus = monitorFactory.waitForNextSuccessfulResult(syncStatusProvider)

    // While there's no connectivity, it's not worth reporting since there are not actually any
    // events waiting to be uploaded.
    assertThat(syncStatus).isEqualTo(SyncStatus.INITIAL_UNKNOWN)
  }

  @Test
  fun testGetSyncStatus_withInitedLogStore_noEvents_returnsInitialUnknown() {
    val syncStatusProvider = impl.getSyncStatus()
    impl.initializeEventLogStore(logsCacheStore)
    connectNetwork()

    val syncStatus = monitorFactory.waitForNextSuccessfulResult(syncStatusProvider)

    assertThat(syncStatus).isEqualTo(SyncStatus.INITIAL_UNKNOWN)
  }

  @Test
  fun testGetSyncStatus_withInitedLogStore_withEvents_returnsWaitingToStartUploading() {
    val syncStatusProvider = impl.getSyncStatus()
    impl.initializeEventLogStore(logsCacheStore)
    connectNetwork()
    queueEventLogForUploading(EVENT_LOG_0)
    queueEventLogForUploading(EVENT_LOG_1)

    val syncStatus = monitorFactory.waitForNextSuccessfulResult(syncStatusProvider)

    assertThat(syncStatus).isEqualTo(SyncStatus.WAITING_TO_START_UPLOADING)
  }

  @Test
  fun testGetSyncStatus_withInitedLogStore_withEvents_someRemoved_returnsWaitingToStartUploading() {
    impl.initializeEventLogStore(logsCacheStore)
    connectNetwork()
    val syncStatusMonitor = monitorFactory.createMonitor(impl.getSyncStatus())
    queueEventLogForUploading(EVENT_LOG_0)
    queueEventLogForUploading(EVENT_LOG_1)

    moveEventLogToUploaded(EVENT_LOG_0) // Simulate uploading the first event.
    testCoroutineDispatchers.runCurrent()

    // One, updating the cache should trigger a change in the monitor's provider. Two, having one
    // more event remaining means that the upload isn't complete yet. This is simulating the case
    // when uploading started and stopped, and only one event was successfully uploaded in that
    // time.
    val syncStatus = syncStatusMonitor.ensureNextResultIsSuccess()
    assertThat(syncStatus).isEqualTo(SyncStatus.WAITING_TO_START_UPLOADING)
  }

  @Test
  fun testGetSyncStatus_withInitedLogStore_withEvents_allRemoved_returnsDataUploaded() {
    impl.initializeEventLogStore(logsCacheStore)
    connectNetwork()
    val syncStatusMonitor = monitorFactory.createMonitor(impl.getSyncStatus())
    queueEventLogForUploading(EVENT_LOG_0)
    queueEventLogForUploading(EVENT_LOG_1)

    // Simulate uploading both events.
    moveEventLogToUploaded(EVENT_LOG_0)
    moveEventLogToUploaded(EVENT_LOG_1)
    testCoroutineDispatchers.runCurrent()

    // No events remaining to upload indicates that everything has been uploaded.
    val syncStatus = syncStatusMonitor.ensureNextResultIsSuccess()
    assertThat(syncStatus).isEqualTo(SyncStatus.DATA_UPLOADED)
  }

  @Test
  fun testGetSyncStatus_withInitedLogStore_noConnection_reportUploading_returnsNoConnectivity() {
    impl.initializeEventLogStore(logsCacheStore)
    disconnectNetwork()
    val syncStatusMonitor = monitorFactory.createMonitor(impl.getSyncStatus())
    queueEventLogForUploading(EVENT_LOG_0)

    impl.reportUploadingStarted()
    testCoroutineDispatchers.runCurrent()

    // No connectivity is a higher priority state to report.
    val syncStatus = syncStatusMonitor.ensureNextResultIsSuccess()
    assertThat(syncStatus).isEqualTo(SyncStatus.NO_CONNECTIVITY)
  }

  @Test
  fun testGetSyncStatus_withInitedLogStore_connected_reportUploadingStarted_returnsUploading() {
    impl.initializeEventLogStore(logsCacheStore)
    connectNetwork()
    val syncStatusMonitor = monitorFactory.createMonitor(impl.getSyncStatus())
    queueEventLogForUploading(EVENT_LOG_0)

    impl.reportUploadingStarted()
    testCoroutineDispatchers.runCurrent()

    // Reporting the uploading state should trigger a change in the sync status data provider.
    val syncStatus = syncStatusMonitor.ensureNextResultIsSuccess()
    assertThat(syncStatus).isEqualTo(SyncStatus.DATA_UPLOADING)
  }

  @Test
  fun testGetSyncStatus_noConnection_reportUploaded_uploadedEvent_returnsUploaded() {
    impl.initializeEventLogStore(logsCacheStore)
    disconnectNetwork()
    val syncStatusMonitor = monitorFactory.createMonitor(impl.getSyncStatus())
    queueEventLogForUploading(EVENT_LOG_0)
    impl.reportUploadingStarted()
    testCoroutineDispatchers.runCurrent()
    moveEventLogToUploaded(EVENT_LOG_0)

    impl.reportUploadingEnded()
    testCoroutineDispatchers.runCurrent()

    // No connectivity only matters if there are pending events. Since an event was logged, that
    // status should be reported instead.
    val syncStatus = syncStatusMonitor.ensureNextResultIsSuccess()
    assertThat(syncStatus).isEqualTo(SyncStatus.DATA_UPLOADED)
  }

  @Test
  fun testGetSyncStatus_noConnection_reportUploaded_uploadedPendingEvents_returnsNoConnectivity() {
    impl.initializeEventLogStore(logsCacheStore)
    disconnectNetwork()
    val syncStatusMonitor = monitorFactory.createMonitor(impl.getSyncStatus())
    queueEventLogForUploading(EVENT_LOG_0)
    impl.reportUploadingStarted()
    testCoroutineDispatchers.runCurrent()
    moveEventLogToUploaded(EVENT_LOG_0)

    // Queue another event.
    impl.reportUploadingEnded()
    queueEventLogForUploading(EVENT_LOG_0)
    testCoroutineDispatchers.runCurrent()

    // Despite there being an uploaded event, there's now no connection for a pending event so that
    // should take priority in reporting.
    val syncStatus = syncStatusMonitor.ensureNextResultIsSuccess()
    assertThat(syncStatus).isEqualTo(SyncStatus.NO_CONNECTIVITY)
  }

  @Test
  fun testGetSyncStatus_withInitedLogStore_connected_reportUploadingEnded_returnsUploaded() {
    impl.initializeEventLogStore(logsCacheStore)
    connectNetwork()
    val syncStatusMonitor = monitorFactory.createMonitor(impl.getSyncStatus())
    queueEventLogForUploading(EVENT_LOG_0)
    impl.reportUploadingStarted()
    testCoroutineDispatchers.runCurrent()
    moveEventLogToUploaded(EVENT_LOG_0)

    impl.reportUploadingEnded()
    testCoroutineDispatchers.runCurrent()

    // Reporting the uploaded state should trigger a change in the sync status data provider.
    val syncStatus = syncStatusMonitor.ensureNextResultIsSuccess()
    assertThat(syncStatus).isEqualTo(SyncStatus.DATA_UPLOADED)
  }

  @Test
  fun testGetSyncStatus_noConnection_reportError_withEvent_returnsNoConnectivity() {
    impl.initializeEventLogStore(logsCacheStore)
    disconnectNetwork()
    val syncStatusMonitor = monitorFactory.createMonitor(impl.getSyncStatus())

    queueEventLogForUploading(EVENT_LOG_0)
    impl.reportUploadingStarted()
    impl.reportUploadError()
    testCoroutineDispatchers.runCurrent()

    // No connectivity is a higher priority state to report.
    val syncStatus = syncStatusMonitor.ensureNextResultIsSuccess()
    assertThat(syncStatus).isEqualTo(SyncStatus.NO_CONNECTIVITY)
  }

  @Test
  fun testGetSyncStatus_withInitedLogStore_connected_reportError_returnsUploadError() {
    impl.initializeEventLogStore(logsCacheStore)
    connectNetwork()
    val syncStatusMonitor = monitorFactory.createMonitor(impl.getSyncStatus())

    impl.reportUploadingStarted()
    impl.reportUploadError()
    testCoroutineDispatchers.runCurrent()

    // Reporting the upload error state should trigger a change in the sync status data provider.
    val syncStatus = syncStatusMonitor.ensureNextResultIsSuccess()
    assertThat(syncStatus).isEqualTo(SyncStatus.UPLOAD_ERROR)
  }

  @Test
  fun testGetSyncStatus_withInitedLogStore_noConnectionThenConnected_reportError_returnsError() {
    impl.initializeEventLogStore(logsCacheStore)
    val syncStatusMonitor = monitorFactory.createMonitor(impl.getSyncStatus())
    queueEventLogForUploading(EVENT_LOG_0)

    // Test a possible scenario that mimics a real possible case where starting to upload events
    // races against a sudden loss of connectivity which, in turns, results in an upload error
    // around the time connectivity is restored.
    disconnectNetwork()
    impl.reportUploadingStarted()
    connectNetwork()
    impl.reportUploadError()
    testCoroutineDispatchers.runCurrent()

    val syncStatus = syncStatusMonitor.ensureNextResultIsSuccess()
    assertThat(syncStatus).isEqualTo(SyncStatus.UPLOAD_ERROR)
  }

  private fun createPrimedLogStore(): PersistentCacheStore<OppiaEventLogs> {
    return persistentCacheStoreFactory.create(
      "test_cache_logs", OppiaEventLogs.getDefaultInstance()
    ).also {
      // Priming the log store ahead of time is, strictly speaking, not necessary for the tests.
      // However, it's closer to how the log store would likely behave in production so this can
      // help minimize some potential behavioral skew.
      it.primeInMemoryAndDiskCacheAsync(
        UPDATE_ALWAYS, PUBLISH_TO_IN_MEMORY_CACHE
      ).waitForSuccessfulResult()
    }
  }

  protected fun queueEventLogForUploading(eventLog: EventLog) {
    logsCacheStore.storeDataAsync { eventLogs ->
      eventLogs.toBuilder().apply { addEventLogsToUpload(eventLog) }.build()
    }.waitForSuccessfulResult()
  }

  protected fun moveEventLogToUploaded(eventLog: EventLog) {
    logsCacheStore.storeDataAsync { eventLogs ->
      eventLogs.toBuilder().apply {
        removeEventLogsToUpload(eventLogsToUploadList.indexOf(eventLog))
        addUploadedEventLogs(eventLog)
      }.build()
    }.waitForSuccessfulResult()
  }

  @Suppress("DEPRECATION") // Robolectric Shadow API requires interacting with deprecated fields.
  protected fun disconnectNetwork() {
    networkConnectionTestUtil.setNetworkInfo(
      status = ConnectivityManager.TYPE_MOBILE,
      networkState = android.net.NetworkInfo.State.DISCONNECTED
    )
  }

  @Suppress("DEPRECATION") // Robolectric Shadow API requires interacting with deprecated fields.
  protected fun connectNetwork() {
    networkConnectionTestUtil.setNetworkInfo(
      status = ConnectivityManager.TYPE_MOBILE,
      networkState = android.net.NetworkInfo.State.CONNECTED
    )
  }

  private fun <T> Deferred<T>.waitForSuccessfulResult() {
    return when (val result = waitForResult()) {
      is AsyncResult.Pending -> error("Deferred never finished.")
      is AsyncResult.Success -> {} // Nothing to do; the result succeeded.
      is AsyncResult.Failure -> throw IllegalStateException("Deferred failed", result.error)
    }
  }

  private fun <T> Deferred<T>.waitForResult() = toStateFlow().waitForLatestValue()

  private fun <T> Deferred<T>.toStateFlow(): StateFlow<AsyncResult<T>> {
    val deferred = this
    return MutableStateFlow<AsyncResult<T>>(value = AsyncResult.Pending()).also { flow ->
      backgroundDispatcherScope.async {
        flow.emit(AsyncResult.Success(deferred.await()))
      }.invokeOnCompletion {
        it?.let { flow.tryEmit(AsyncResult.Failure(it)) }
      }
    }
  }

  private fun <T> StateFlow<T>.waitForLatestValue(): T =
    also { testCoroutineDispatchers.runCurrent() }.value

  private companion object {
    private const val EVENT_LOG_TIMESTAMP_0 = 1556061720000
    private const val EVENT_LOG_TIMESTAMP_1 = 1556094120000

    private val OPEN_HOME_CONTEXT = EventLog.Context.newBuilder().apply { openHome = true }.build()
    private val EVENT_LOG_0 = createEventLog(EVENT_LOG_TIMESTAMP_0, OPEN_HOME_CONTEXT)
    private val EVENT_LOG_1 = createEventLog(EVENT_LOG_TIMESTAMP_1, OPEN_HOME_CONTEXT)

    private fun createEventLog(timestamp: Long, context: EventLog.Context): EventLog {
      return EventLog.newBuilder().apply {
        this.timestamp = timestamp
        this.priority = EventLog.Priority.ESSENTIAL
        this.context = context
      }.build()
    }
  }
}
