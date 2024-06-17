package org.oppia.android.testing.logging

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineDispatcher
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.app.model.EventLog
import org.oppia.android.data.persistence.PersistentCacheStore
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.data.DataProviderTestMonitor
import org.oppia.android.testing.junit.OppiaParameterizedTestRunner
import org.oppia.android.testing.junit.OppiaParameterizedTestRunner.Iteration
import org.oppia.android.testing.junit.OppiaParameterizedTestRunner.Parameter
import org.oppia.android.testing.junit.OppiaParameterizedTestRunner.SelectRunnerPlatform
import org.oppia.android.testing.junit.ParameterizedRobolectricTestRunner
import org.oppia.android.testing.networking.NetworkConnectionTestUtil
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.data.DataProvidersInjector
import org.oppia.android.util.data.DataProvidersInjectorProvider
import org.oppia.android.util.locale.LocaleProdModule
import org.oppia.android.util.logging.LoggerModule
import org.oppia.android.util.logging.SyncStatusManager.SyncStatus
import org.oppia.android.util.logging.SyncStatusManager.SyncStatus.DATA_UPLOADED
import org.oppia.android.util.logging.SyncStatusManager.SyncStatus.DATA_UPLOADING
import org.oppia.android.util.logging.SyncStatusManager.SyncStatus.INITIAL_UNKNOWN
import org.oppia.android.util.logging.SyncStatusManager.SyncStatus.NO_CONNECTIVITY
import org.oppia.android.util.logging.SyncStatusManager.SyncStatus.UPLOAD_ERROR
import org.oppia.android.util.logging.SyncStatusManager.SyncStatus.WAITING_TO_START_UPLOADING
import org.oppia.android.util.logging.SyncStatusManagerTestBase
import org.oppia.android.util.networking.NetworkConnectionUtilDebugModule
import org.oppia.android.util.threading.BackgroundDispatcher
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [TestSyncStatusManager]. */
// Same parameter value: helpers reduce test context, even if they are used by 1 test.
// Function name: test names are conventionally named with underscores.
@Suppress("SameParameterValue", "FunctionName")
@RunWith(OppiaParameterizedTestRunner::class)
@SelectRunnerPlatform(ParameterizedRobolectricTestRunner::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = TestSyncStatusManagerTest.TestApplication::class)
class TestSyncStatusManagerTest : SyncStatusManagerTestBase() {
  @Inject override lateinit var impl: TestSyncStatusManager
  @Inject override lateinit var monitorFactory: DataProviderTestMonitor.Factory
  @Inject override lateinit var persistentCacheStoreFactory: PersistentCacheStore.Factory
  @Inject override lateinit var networkConnectionTestUtil: NetworkConnectionTestUtil
  @Inject override lateinit var testCoroutineDispatchers: TestCoroutineDispatchers
  @field:[Inject BackgroundDispatcher]
  override lateinit var backgroundDispatcher: CoroutineDispatcher

  @Parameter lateinit var status: String

  private val syncStatus by lazy { SyncStatus.valueOf(status) }

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testForceSyncStatus_initialState_forceInitialState_doesNotChangeGetSyncStatusProvider() {
    val syncStatusMonitor = monitorFactory.createMonitor(impl.getSyncStatus())

    impl.forceSyncStatus(INITIAL_UNKNOWN)
    testCoroutineDispatchers.runCurrent()

    syncStatusMonitor.verifyProviderIsNotUpdated()
  }

  @Test
  fun testForceSyncStatus_initialState_forceDifferentState_changesGetSyncStatusProvider() {
    val syncStatusMonitor = monitorFactory.createMonitor(impl.getSyncStatus())

    impl.forceSyncStatus(WAITING_TO_START_UPLOADING)
    testCoroutineDispatchers.runCurrent()

    syncStatusMonitor.ensureNextResultIsSuccess()
  }

  @Test
  fun testResetForcedSyncStatus_initialState_noForcedStatus_doesNotChangeGetSyncStatusProvider() {
    val syncStatusMonitor = monitorFactory.createMonitor(impl.getSyncStatus())

    impl.resetForcedSyncStatus()
    testCoroutineDispatchers.runCurrent()

    syncStatusMonitor.verifyProviderIsNotUpdated()
  }

  @Test
  fun testResetForcedSyncStatus_initialState_forceInitState_doesNotChangeGetSyncStatusProvider() {
    impl.forceSyncStatus(INITIAL_UNKNOWN)
    val syncStatusMonitor = monitorFactory.createMonitor(impl.getSyncStatus())

    impl.resetForcedSyncStatus()
    testCoroutineDispatchers.runCurrent()

    syncStatusMonitor.verifyProviderIsNotUpdated()
  }

  @Test
  fun testResetForcedSyncStatus_initialState_forceDifferenceState_changeGetSyncStatusProvider() {
    impl.forceSyncStatus(WAITING_TO_START_UPLOADING)
    val syncStatusMonitor = monitorFactory.createMonitor(impl.getSyncStatus())

    impl.resetForcedSyncStatus()
    testCoroutineDispatchers.runCurrent()

    // The provider should change since the initial state was actually overwritten.
    syncStatusMonitor.ensureNextResultIsSuccess()
  }

  @Test
  fun testGetSyncStatus_withForcedInitialUnknown_returnsInitialUnknown() {
    val syncStatusProvider = impl.getSyncStatus()

    impl.forceSyncStatus(INITIAL_UNKNOWN)

    val syncStatus = monitorFactory.waitForNextSuccessfulResult(syncStatusProvider)
    assertThat(syncStatus).isEqualTo(INITIAL_UNKNOWN)
  }

  @Test
  fun testGetSyncStatus_withForcedWaitingToStartUploading_returnsWaitingToStartUploading() {
    val syncStatusProvider = impl.getSyncStatus()

    impl.forceSyncStatus(WAITING_TO_START_UPLOADING)

    val syncStatus = monitorFactory.waitForNextSuccessfulResult(syncStatusProvider)
    assertThat(syncStatus).isEqualTo(WAITING_TO_START_UPLOADING)
  }

  @Test
  fun testGetSyncStatus_withForcedDataUploading_returnsDataUploading() {
    val syncStatusProvider = impl.getSyncStatus()

    impl.forceSyncStatus(DATA_UPLOADING)

    val syncStatus = monitorFactory.waitForNextSuccessfulResult(syncStatusProvider)
    assertThat(syncStatus).isEqualTo(DATA_UPLOADING)
  }

  @Test
  fun testGetSyncStatus_withForcedDataUploaded_returnsDataUploaded() {
    val syncStatusProvider = impl.getSyncStatus()

    impl.forceSyncStatus(DATA_UPLOADED)

    val syncStatus = monitorFactory.waitForNextSuccessfulResult(syncStatusProvider)
    assertThat(syncStatus).isEqualTo(DATA_UPLOADED)
  }

  @Test
  fun testGetSyncStatus_withForcedNoConnectivity_returnsNoConnectivity() {
    val syncStatusProvider = impl.getSyncStatus()

    impl.forceSyncStatus(NO_CONNECTIVITY)

    val syncStatus = monitorFactory.waitForNextSuccessfulResult(syncStatusProvider)
    assertThat(syncStatus).isEqualTo(NO_CONNECTIVITY)
  }

  @Test
  fun testGetSyncStatus_withForcedNetworkUpload_returnsUploadError() {
    val syncStatusProvider = impl.getSyncStatus()

    impl.forceSyncStatus(UPLOAD_ERROR)

    val syncStatus = monitorFactory.waitForNextSuccessfulResult(syncStatusProvider)
    assertThat(syncStatus).isEqualTo(UPLOAD_ERROR)
  }

  @Test
  fun testGetSyncStatus_withForcedUploadError_thenDataUploaded_returnsDataUploaded() {
    val syncStatusProvider = impl.getSyncStatus()
    impl.forceSyncStatus(UPLOAD_ERROR)

    impl.forceSyncStatus(DATA_UPLOADED)

    // Verify that later calls to forceSyncStatus overwrites previous calls.
    val syncStatus = monitorFactory.waitForNextSuccessfulResult(syncStatusProvider)
    assertThat(syncStatus).isEqualTo(DATA_UPLOADED)
  }

  @Test
  @Iteration("initial_unknown", "status=INITIAL_UNKNOWN")
  @Iteration("waiting_to_start_uploading", "status=WAITING_TO_START_UPLOADING")
  @Iteration("data_uploading", "status=DATA_UPLOADING")
  @Iteration("data_uploaded", "status=DATA_UPLOADED")
  @Iteration("no_connectivity", "status=NO_CONNECTIVITY")
  @Iteration("upload_error", "status=UPLOAD_ERROR")
  fun testGetSyncStatus_withInitedLogStore_forceStatus_noConnectivity_returnsForcedStatus() {
    val syncStatusProvider = impl.getSyncStatus()
    impl.initializeEventLogStore(logsCacheStore)
    disconnectNetwork()

    impl.forceSyncStatus(syncStatus)
    testCoroutineDispatchers.runCurrent()

    // The forced status should overwrite the current manager's sync status state.
    val syncStatus = monitorFactory.waitForNextSuccessfulResult(syncStatusProvider)
    assertThat(syncStatus).isEqualTo(syncStatus)
  }

  @Test
  @Iteration("initial_unknown", "status=INITIAL_UNKNOWN")
  @Iteration("waiting_to_start_uploading", "status=WAITING_TO_START_UPLOADING")
  @Iteration("data_uploading", "status=DATA_UPLOADING")
  @Iteration("data_uploaded", "status=DATA_UPLOADED")
  @Iteration("no_connectivity", "status=NO_CONNECTIVITY")
  @Iteration("upload_error", "status=UPLOAD_ERROR")
  fun testGetSyncStatus_withInitedLogStore_forceStatus_noEvents_returnsForcedStatus() {
    val syncStatusProvider = impl.getSyncStatus()
    impl.initializeEventLogStore(logsCacheStore)
    connectNetwork()

    impl.forceSyncStatus(syncStatus)
    testCoroutineDispatchers.runCurrent()

    // The forced status should overwrite the current manager's sync status state.
    val syncStatus = monitorFactory.waitForNextSuccessfulResult(syncStatusProvider)
    assertThat(syncStatus).isEqualTo(syncStatus)
  }

  @Test
  @Iteration("initial_unknown", "status=INITIAL_UNKNOWN")
  @Iteration("waiting_to_start_uploading", "status=WAITING_TO_START_UPLOADING")
  @Iteration("data_uploading", "status=DATA_UPLOADING")
  @Iteration("data_uploaded", "status=DATA_UPLOADED")
  @Iteration("no_connectivity", "status=NO_CONNECTIVITY")
  @Iteration("upload_error", "status=UPLOAD_ERROR")
  fun testGetSyncStatus_withInitedLogStore_forceStatus_withEvents_returnsForcedStatus() {
    val syncStatusProvider = impl.getSyncStatus()
    impl.initializeEventLogStore(logsCacheStore)
    connectNetwork()
    queueEventLogForUploading(EVENT_LOG_0)
    queueEventLogForUploading(EVENT_LOG_1)

    impl.forceSyncStatus(syncStatus)
    testCoroutineDispatchers.runCurrent()

    // The forced status should overwrite the current manager's sync status state.
    val syncStatus = monitorFactory.waitForNextSuccessfulResult(syncStatusProvider)
    assertThat(syncStatus).isEqualTo(syncStatus)
  }

  @Test
  @Iteration("initial_unknown", "status=INITIAL_UNKNOWN")
  @Iteration("waiting_to_start_uploading", "status=WAITING_TO_START_UPLOADING")
  @Iteration("data_uploading", "status=DATA_UPLOADING")
  @Iteration("data_uploaded", "status=DATA_UPLOADED")
  @Iteration("no_connectivity", "status=NO_CONNECTIVITY")
  @Iteration("upload_error", "status=UPLOAD_ERROR")
  fun testGetSyncStatus_withInitedLogStore_forceStatus_withEvents_allRemoved_returnsForcedStatus() {
    val syncStatusProvider = impl.getSyncStatus()
    impl.initializeEventLogStore(logsCacheStore)
    connectNetwork()
    queueEventLogForUploading(EVENT_LOG_0)
    queueEventLogForUploading(EVENT_LOG_1)
    moveEventLogToUploaded(EVENT_LOG_0)
    moveEventLogToUploaded(EVENT_LOG_1)

    impl.forceSyncStatus(syncStatus)
    testCoroutineDispatchers.runCurrent()

    // The forced status should overwrite the current manager's sync status state.
    val syncStatus = monitorFactory.waitForNextSuccessfulResult(syncStatusProvider)
    assertThat(syncStatus).isEqualTo(syncStatus)
  }

  @Test
  @Iteration("initial_unknown", "status=INITIAL_UNKNOWN")
  @Iteration("waiting_to_start_uploading", "status=WAITING_TO_START_UPLOADING")
  @Iteration("data_uploading", "status=DATA_UPLOADING")
  @Iteration("data_uploaded", "status=DATA_UPLOADED")
  @Iteration("no_connectivity", "status=NO_CONNECTIVITY")
  @Iteration("upload_error", "status=UPLOAD_ERROR")
  fun testGetSyncStatus_withInitedLogs_forceStatus_reportUploadingStarted_returnsForcedStatus() {
    val syncStatusProvider = impl.getSyncStatus()
    impl.initializeEventLogStore(logsCacheStore)
    connectNetwork()
    queueEventLogForUploading(EVENT_LOG_0)
    impl.reportUploadingStarted()

    impl.forceSyncStatus(syncStatus)
    testCoroutineDispatchers.runCurrent()

    // The forced status should overwrite the current manager's sync status state.
    val syncStatus = monitorFactory.waitForNextSuccessfulResult(syncStatusProvider)
    assertThat(syncStatus).isEqualTo(syncStatus)
  }

  @Test
  @Iteration("initial_unknown", "status=INITIAL_UNKNOWN")
  @Iteration("waiting_to_start_uploading", "status=WAITING_TO_START_UPLOADING")
  @Iteration("data_uploading", "status=DATA_UPLOADING")
  @Iteration("data_uploaded", "status=DATA_UPLOADED")
  @Iteration("no_connectivity", "status=NO_CONNECTIVITY")
  @Iteration("upload_error", "status=UPLOAD_ERROR")
  fun testGetSyncStatus_withInitedLogStore_forceStatus_reportUploadingEnded_returnsForcedStatus() {
    val syncStatusProvider = impl.getSyncStatus()
    impl.initializeEventLogStore(logsCacheStore)
    connectNetwork()
    queueEventLogForUploading(EVENT_LOG_0)
    impl.reportUploadingStarted()
    moveEventLogToUploaded(EVENT_LOG_0)
    impl.reportUploadingEnded()
    testCoroutineDispatchers.runCurrent()

    impl.forceSyncStatus(syncStatus)
    testCoroutineDispatchers.runCurrent()

    // The forced status should overwrite the current manager's sync status state.
    val syncStatus = monitorFactory.waitForNextSuccessfulResult(syncStatusProvider)
    assertThat(syncStatus).isEqualTo(syncStatus)
  }

  @Test
  @Iteration("initial_unknown", "status=INITIAL_UNKNOWN")
  @Iteration("waiting_to_start_uploading", "status=WAITING_TO_START_UPLOADING")
  @Iteration("data_uploading", "status=DATA_UPLOADING")
  @Iteration("data_uploaded", "status=DATA_UPLOADED")
  @Iteration("no_connectivity", "status=NO_CONNECTIVITY")
  @Iteration("upload_error", "status=UPLOAD_ERROR")
  fun testGetSyncStatus_withInitedLogStore_forceStatus_reportError_returnsForcedStatus() {
    val syncStatusProvider = impl.getSyncStatus()
    impl.initializeEventLogStore(logsCacheStore)
    connectNetwork()
    impl.reportUploadError()

    impl.forceSyncStatus(syncStatus)
    testCoroutineDispatchers.runCurrent()

    // The forced status should overwrite the current manager's sync status state.
    val syncStatus = monitorFactory.waitForNextSuccessfulResult(syncStatusProvider)
    assertThat(syncStatus).isEqualTo(syncStatus)
  }

  @Test
  @Iteration("initial_unknown", "status=INITIAL_UNKNOWN")
  @Iteration("waiting_to_start_uploading", "status=WAITING_TO_START_UPLOADING")
  @Iteration("data_uploading", "status=DATA_UPLOADING")
  @Iteration("data_uploaded", "status=DATA_UPLOADED")
  @Iteration("no_connectivity", "status=NO_CONNECTIVITY")
  @Iteration("upload_error", "status=UPLOAD_ERROR")
  fun testGetSyncStatus_initialState_resetStatus_returnsInitialUnknown() {
    val syncStatusProvider = impl.getSyncStatus()
    impl.forceSyncStatus(syncStatus)
    testCoroutineDispatchers.runCurrent()

    impl.resetForcedSyncStatus()
    testCoroutineDispatchers.runCurrent()

    // The original status should be returned.
    val syncStatus = monitorFactory.waitForNextSuccessfulResult(syncStatusProvider)
    assertThat(syncStatus).isEqualTo(INITIAL_UNKNOWN)
  }

  @Test
  @Iteration("initial_unknown", "status=INITIAL_UNKNOWN")
  @Iteration("waiting_to_start_uploading", "status=WAITING_TO_START_UPLOADING")
  @Iteration("data_uploading", "status=DATA_UPLOADING")
  @Iteration("data_uploaded", "status=DATA_UPLOADED")
  @Iteration("no_connectivity", "status=NO_CONNECTIVITY")
  @Iteration("upload_error", "status=UPLOAD_ERROR")
  fun testGetSyncStatus_withInitedLogStore_resetStatus_noConnectivity_returnsNoConnectivity() {
    val syncStatusProvider = impl.getSyncStatus()
    impl.initializeEventLogStore(logsCacheStore)
    disconnectNetwork()
    queueEventLogForUploading(EVENT_LOG_0)
    impl.forceSyncStatus(syncStatus)
    testCoroutineDispatchers.runCurrent()

    impl.resetForcedSyncStatus()
    testCoroutineDispatchers.runCurrent()

    // The original status should be returned.
    val syncStatus = monitorFactory.waitForNextSuccessfulResult(syncStatusProvider)
    assertThat(syncStatus).isEqualTo(NO_CONNECTIVITY)
  }

  @Test
  @Iteration("initial_unknown", "status=INITIAL_UNKNOWN")
  @Iteration("waiting_to_start_uploading", "status=WAITING_TO_START_UPLOADING")
  @Iteration("data_uploading", "status=DATA_UPLOADING")
  @Iteration("data_uploaded", "status=DATA_UPLOADED")
  @Iteration("no_connectivity", "status=NO_CONNECTIVITY")
  @Iteration("upload_error", "status=UPLOAD_ERROR")
  fun testGetSyncStatus_withInitedLogStore_resetStatus_noEvents_returnsInitialUnknown() {
    val syncStatusProvider = impl.getSyncStatus()
    impl.initializeEventLogStore(logsCacheStore)
    connectNetwork()
    impl.forceSyncStatus(syncStatus)
    testCoroutineDispatchers.runCurrent()

    impl.resetForcedSyncStatus()
    testCoroutineDispatchers.runCurrent()

    // The original status should be returned.
    val syncStatus = monitorFactory.waitForNextSuccessfulResult(syncStatusProvider)
    assertThat(syncStatus).isEqualTo(INITIAL_UNKNOWN)
  }

  @Test
  @Iteration("initial_unknown", "status=INITIAL_UNKNOWN")
  @Iteration("waiting_to_start_uploading", "status=WAITING_TO_START_UPLOADING")
  @Iteration("data_uploading", "status=DATA_UPLOADING")
  @Iteration("data_uploaded", "status=DATA_UPLOADED")
  @Iteration("no_connectivity", "status=NO_CONNECTIVITY")
  @Iteration("upload_error", "status=UPLOAD_ERROR")
  fun testGetSyncStatus_withInitedLogStore_resetStatus_withEvents_returnsWaitingToStartUploading() {
    val syncStatusProvider = impl.getSyncStatus()
    impl.initializeEventLogStore(logsCacheStore)
    connectNetwork()
    queueEventLogForUploading(EVENT_LOG_0)
    queueEventLogForUploading(EVENT_LOG_1)
    impl.forceSyncStatus(syncStatus)
    testCoroutineDispatchers.runCurrent()

    impl.resetForcedSyncStatus()
    testCoroutineDispatchers.runCurrent()

    // The original status should be returned.
    val syncStatus = monitorFactory.waitForNextSuccessfulResult(syncStatusProvider)
    assertThat(syncStatus).isEqualTo(WAITING_TO_START_UPLOADING)
  }

  @Test
  @Iteration("initial_unknown", "status=INITIAL_UNKNOWN")
  @Iteration("waiting_to_start_uploading", "status=WAITING_TO_START_UPLOADING")
  @Iteration("data_uploading", "status=DATA_UPLOADING")
  @Iteration("data_uploaded", "status=DATA_UPLOADED")
  @Iteration("no_connectivity", "status=NO_CONNECTIVITY")
  @Iteration("upload_error", "status=UPLOAD_ERROR")
  fun testGetSyncStatus_withInitedLogStore_resetStatus_withEvents_allRemoved_returnsDataUploaded() {
    val syncStatusProvider = impl.getSyncStatus()
    impl.initializeEventLogStore(logsCacheStore)
    connectNetwork()
    queueEventLogForUploading(EVENT_LOG_0)
    queueEventLogForUploading(EVENT_LOG_1)
    moveEventLogToUploaded(EVENT_LOG_0)
    moveEventLogToUploaded(EVENT_LOG_1)
    impl.forceSyncStatus(syncStatus)
    testCoroutineDispatchers.runCurrent()

    impl.resetForcedSyncStatus()
    testCoroutineDispatchers.runCurrent()

    // The original status should be returned.
    val syncStatus = monitorFactory.waitForNextSuccessfulResult(syncStatusProvider)
    assertThat(syncStatus).isEqualTo(DATA_UPLOADED)
  }

  @Test
  @Iteration("initial_unknown", "status=INITIAL_UNKNOWN")
  @Iteration("waiting_to_start_uploading", "status=WAITING_TO_START_UPLOADING")
  @Iteration("data_uploading", "status=DATA_UPLOADING")
  @Iteration("data_uploaded", "status=DATA_UPLOADED")
  @Iteration("no_connectivity", "status=NO_CONNECTIVITY")
  @Iteration("upload_error", "status=UPLOAD_ERROR")
  fun testGetSyncStatus_withInitedLogStore_resetStatus_reportUploadingStarted_returnsUploading() {
    val syncStatusProvider = impl.getSyncStatus()
    impl.initializeEventLogStore(logsCacheStore)
    connectNetwork()
    queueEventLogForUploading(EVENT_LOG_0)
    impl.reportUploadingStarted()
    impl.forceSyncStatus(syncStatus)
    testCoroutineDispatchers.runCurrent()

    impl.resetForcedSyncStatus()
    testCoroutineDispatchers.runCurrent()

    // The original status should be returned.
    val syncStatus = monitorFactory.waitForNextSuccessfulResult(syncStatusProvider)
    assertThat(syncStatus).isEqualTo(DATA_UPLOADING)
  }

  @Test
  @Iteration("initial_unknown", "status=INITIAL_UNKNOWN")
  @Iteration("waiting_to_start_uploading", "status=WAITING_TO_START_UPLOADING")
  @Iteration("data_uploading", "status=DATA_UPLOADING")
  @Iteration("data_uploaded", "status=DATA_UPLOADED")
  @Iteration("no_connectivity", "status=NO_CONNECTIVITY")
  @Iteration("upload_error", "status=UPLOAD_ERROR")
  fun testGetSyncStatus_withInitedLogStore_resetStatus_reportUploadingEnded_returnsUploaded() {
    val syncStatusProvider = impl.getSyncStatus()
    impl.initializeEventLogStore(logsCacheStore)
    connectNetwork()
    queueEventLogForUploading(EVENT_LOG_0)
    impl.reportUploadingStarted()
    moveEventLogToUploaded(EVENT_LOG_0)
    impl.reportUploadingEnded()
    impl.forceSyncStatus(syncStatus)
    testCoroutineDispatchers.runCurrent()

    impl.resetForcedSyncStatus()
    testCoroutineDispatchers.runCurrent()

    // The original status should be returned.
    val syncStatus = monitorFactory.waitForNextSuccessfulResult(syncStatusProvider)
    assertThat(syncStatus).isEqualTo(DATA_UPLOADED)
  }

  @Test
  @Iteration("initial_unknown", "status=INITIAL_UNKNOWN")
  @Iteration("waiting_to_start_uploading", "status=WAITING_TO_START_UPLOADING")
  @Iteration("data_uploading", "status=DATA_UPLOADING")
  @Iteration("data_uploaded", "status=DATA_UPLOADED")
  @Iteration("no_connectivity", "status=NO_CONNECTIVITY")
  @Iteration("upload_error", "status=UPLOAD_ERROR")
  fun testGetSyncStatus_withInitedLogStore_resetStatus_reportError_returnsUploadError() {
    val syncStatusProvider = impl.getSyncStatus()
    impl.initializeEventLogStore(logsCacheStore)
    connectNetwork()
    impl.reportUploadError()
    impl.forceSyncStatus(syncStatus)
    testCoroutineDispatchers.runCurrent()

    impl.resetForcedSyncStatus()
    testCoroutineDispatchers.runCurrent()

    // The original status should be returned.
    val syncStatus = monitorFactory.waitForNextSuccessfulResult(syncStatusProvider)
    assertThat(syncStatus).isEqualTo(UPLOAD_ERROR)
  }

  @Test
  fun testGetSyncStatuses_withForcedInitialUnknown_returnIncludesInitialUnknown() {
    impl.forceSyncStatus(INITIAL_UNKNOWN)
    testCoroutineDispatchers.runCurrent()

    val statuses = impl.getSyncStatuses()

    // The forced status should be included in the tracked statuses.
    assertThat(statuses).contains(INITIAL_UNKNOWN)
  }

  @Test
  fun testGetSyncStatuses_withForcedStartUploading_returnIncludesWaitingToStartUploading() {
    impl.forceSyncStatus(WAITING_TO_START_UPLOADING)
    testCoroutineDispatchers.runCurrent()

    val statuses = impl.getSyncStatuses()

    // The forced status should be included in the tracked statuses.
    assertThat(statuses).contains(WAITING_TO_START_UPLOADING)
  }

  @Test
  fun testGetSyncStatuses_withForcedDataUploading_returnIncludesDataUploading() {
    impl.forceSyncStatus(DATA_UPLOADING)
    testCoroutineDispatchers.runCurrent()

    val statuses = impl.getSyncStatuses()

    // The forced status should be included in the tracked statuses.
    assertThat(statuses).contains(DATA_UPLOADING)
  }

  @Test
  fun testGetSyncStatuses_withForcedDataUploaded_returnIncludesDataUploaded() {
    impl.forceSyncStatus(DATA_UPLOADED)
    testCoroutineDispatchers.runCurrent()

    val statuses = impl.getSyncStatuses()

    // The forced status should be included in the tracked statuses.
    assertThat(statuses).contains(DATA_UPLOADED)
  }

  @Test
  fun testGetSyncStatuses_withForcedNoConnectivity_returnIncludesNoConnectivity() {
    impl.forceSyncStatus(NO_CONNECTIVITY)
    testCoroutineDispatchers.runCurrent()

    val statuses = impl.getSyncStatuses()

    // The forced status should be included in the tracked statuses.
    assertThat(statuses).contains(NO_CONNECTIVITY)
  }

  @Test
  fun testGetSyncStatuses_withForcedUploadError_returnIncludesUploadError() {
    impl.forceSyncStatus(UPLOAD_ERROR)
    testCoroutineDispatchers.runCurrent()

    val statuses = impl.getSyncStatuses()

    // The forced status should be included in the tracked statuses.
    assertThat(statuses).contains(UPLOAD_ERROR)
  }

  @Test
  fun testGetSyncStatuses_initialState_returnsInitialUnknown() {
    val syncStatuses = impl.getSyncStatuses()

    assertThat(syncStatuses).containsExactly(INITIAL_UNKNOWN)
  }

  @Test
  fun testGetSyncStatuses_withInitedLogStore_noConnectivity_returnsUnknownAndNoConnectivity() {
    impl.initializeEventLogStore(logsCacheStore)
    disconnectNetwork()
    queueEventLogForUploading(EVENT_LOG_0)

    val syncStatuses = impl.getSyncStatuses()

    assertThat(syncStatuses).containsExactly(INITIAL_UNKNOWN, NO_CONNECTIVITY).inOrder()
  }

  @Test
  fun testGetSyncStatuses_withInitedLogStore_noEvents_returnsUnknown() {
    impl.initializeEventLogStore(logsCacheStore)
    connectNetwork()

    val syncStatuses = impl.getSyncStatuses()

    assertThat(syncStatuses).containsExactly(INITIAL_UNKNOWN).inOrder()
  }

  @Test
  fun testGetSyncStatuses_withInitedLogStore_withEvents_returnsSequenceUpToStartUploading() {
    impl.initializeEventLogStore(logsCacheStore)
    connectNetwork()
    queueEventLogForUploading(EVENT_LOG_0)
    queueEventLogForUploading(EVENT_LOG_1)

    val syncStatuses = impl.getSyncStatuses()

    // Note that the base manager will report multiple "waiting to start uploading" but the fake
    // intentionally de-duplicates them since the status hasn't actually changed.
    assertThat(syncStatuses).containsExactly(INITIAL_UNKNOWN, WAITING_TO_START_UPLOADING).inOrder()
  }

  @Test
  fun testGetSyncStatuses_withInitedLogStore_withEvents_allRemoved_returnsSeqUpToDataUploaded() {
    impl.initializeEventLogStore(logsCacheStore)
    connectNetwork()
    queueEventLogForUploading(EVENT_LOG_0)
    queueEventLogForUploading(EVENT_LOG_1)
    moveEventLogToUploaded(EVENT_LOG_0)
    moveEventLogToUploaded(EVENT_LOG_1)

    val syncStatuses = impl.getSyncStatuses()

    assertThat(syncStatuses)
      .containsExactly(INITIAL_UNKNOWN, WAITING_TO_START_UPLOADING, DATA_UPLOADED)
      .inOrder()
  }

  @Test
  fun testGetSyncStatuses_withInitedLogStore_reportUploadingStarted_returnsSequenceUpToUploading() {
    impl.initializeEventLogStore(logsCacheStore)
    connectNetwork()
    queueEventLogForUploading(EVENT_LOG_0)
    impl.reportUploadingStarted()
    testCoroutineDispatchers.runCurrent()

    val syncStatuses = impl.getSyncStatuses()

    assertThat(syncStatuses)
      .containsExactly(INITIAL_UNKNOWN, WAITING_TO_START_UPLOADING, DATA_UPLOADING)
      .inOrder()
  }

  @Test
  fun testGetSyncStatuses_withInitedLogStore_reportUploadingEnded_returnsSequenceUpToUploaded() {
    impl.initializeEventLogStore(logsCacheStore)
    connectNetwork()
    queueEventLogForUploading(EVENT_LOG_0)
    impl.reportUploadingStarted()
    testCoroutineDispatchers.runCurrent()
    moveEventLogToUploaded(EVENT_LOG_0)
    impl.reportUploadingEnded()
    testCoroutineDispatchers.runCurrent()

    val syncStatuses = impl.getSyncStatuses()

    assertThat(syncStatuses)
      .containsExactly(INITIAL_UNKNOWN, WAITING_TO_START_UPLOADING, DATA_UPLOADING, DATA_UPLOADED)
      .inOrder()
  }

  @Test
  fun testGetSyncStatuses_withInitedLogStore_reportError_returnsSequenceUpToUploadError() {
    impl.initializeEventLogStore(logsCacheStore)
    connectNetwork()
    queueEventLogForUploading(EVENT_LOG_0)
    impl.reportUploadingStarted()
    testCoroutineDispatchers.runCurrent()
    impl.reportUploadError()
    testCoroutineDispatchers.runCurrent()

    val syncStatuses = impl.getSyncStatuses()

    assertThat(syncStatuses)
      .containsExactly(INITIAL_UNKNOWN, WAITING_TO_START_UPLOADING, DATA_UPLOADING, UPLOAD_ERROR)
      .inOrder()
  }

  @Test
  @Iteration("initial_unknown", "status=INITIAL_UNKNOWN")
  @Iteration("waiting_to_start_uploading", "status=WAITING_TO_START_UPLOADING")
  @Iteration("data_uploading", "status=DATA_UPLOADING")
  @Iteration("data_uploaded", "status=DATA_UPLOADED")
  @Iteration("no_connectivity", "status=NO_CONNECTIVITY")
  @Iteration("upload_error", "status=UPLOAD_ERROR")
  fun testGetSyncStatuses_initialState_forceStatus_returnsWithForcedStatus() {
    impl.forceSyncStatus(syncStatus)
    testCoroutineDispatchers.runCurrent()

    val syncStatuses = impl.getSyncStatuses()

    // The most recent tracked status should be the forced one.
    assertThat(syncStatuses.last()).isEqualTo(syncStatus)
  }

  @Test
  @Iteration("initial_unknown", "status=INITIAL_UNKNOWN")
  @Iteration("waiting_to_start_uploading", "status=WAITING_TO_START_UPLOADING")
  @Iteration("data_uploading", "status=DATA_UPLOADING")
  @Iteration("data_uploaded", "status=DATA_UPLOADED")
  @Iteration("no_connectivity", "status=NO_CONNECTIVITY")
  @Iteration("upload_error", "status=UPLOAD_ERROR")
  fun testGetSyncStatuses_withInitedLogStore_forceStatus_noConnectivity_returnsWithForcedStatus() {
    impl.initializeEventLogStore(logsCacheStore)
    disconnectNetwork()
    impl.forceSyncStatus(syncStatus)
    testCoroutineDispatchers.runCurrent()

    val syncStatuses = impl.getSyncStatuses()

    // The most recent tracked status should be the forced one.
    assertThat(syncStatuses.last()).isEqualTo(syncStatus)
  }

  @Test
  @Iteration("initial_unknown", "status=INITIAL_UNKNOWN")
  @Iteration("waiting_to_start_uploading", "status=WAITING_TO_START_UPLOADING")
  @Iteration("data_uploading", "status=DATA_UPLOADING")
  @Iteration("data_uploaded", "status=DATA_UPLOADED")
  @Iteration("no_connectivity", "status=NO_CONNECTIVITY")
  @Iteration("upload_error", "status=UPLOAD_ERROR")
  fun testGetSyncStatuses_withInitedLogStore_forceStatus_noEvents_returnsWithForcedStatus() {
    impl.initializeEventLogStore(logsCacheStore)
    connectNetwork()
    impl.forceSyncStatus(syncStatus)
    testCoroutineDispatchers.runCurrent()

    val syncStatuses = impl.getSyncStatuses()

    // The most recent tracked status should be the forced one.
    assertThat(syncStatuses.last()).isEqualTo(syncStatus)
  }

  @Test
  @Iteration("initial_unknown", "status=INITIAL_UNKNOWN")
  @Iteration("waiting_to_start_uploading", "status=WAITING_TO_START_UPLOADING")
  @Iteration("data_uploading", "status=DATA_UPLOADING")
  @Iteration("data_uploaded", "status=DATA_UPLOADED")
  @Iteration("no_connectivity", "status=NO_CONNECTIVITY")
  @Iteration("upload_error", "status=UPLOAD_ERROR")
  fun testGetSyncStatuses_withInitedLogStore_forceStatus_withEvents_returnsWithForcedStatus() {
    impl.initializeEventLogStore(logsCacheStore)
    connectNetwork()
    queueEventLogForUploading(EVENT_LOG_0)
    queueEventLogForUploading(EVENT_LOG_1)
    impl.forceSyncStatus(syncStatus)
    testCoroutineDispatchers.runCurrent()

    val syncStatuses = impl.getSyncStatuses()

    // The most recent tracked status should be the forced one.
    assertThat(syncStatuses.last()).isEqualTo(syncStatus)
  }

  @Test
  @Iteration("initial_unknown", "status=INITIAL_UNKNOWN")
  @Iteration("waiting_to_start_uploading", "status=WAITING_TO_START_UPLOADING")
  @Iteration("data_uploading", "status=DATA_UPLOADING")
  @Iteration("data_uploaded", "status=DATA_UPLOADED")
  @Iteration("no_connectivity", "status=NO_CONNECTIVITY")
  @Iteration("upload_error", "status=UPLOAD_ERROR")
  fun testGetSyncStatuses_withLogStore_forceStatus_withEvents_allRemoved_returnsWithForcedStatus() {
    impl.initializeEventLogStore(logsCacheStore)
    connectNetwork()
    queueEventLogForUploading(EVENT_LOG_0)
    queueEventLogForUploading(EVENT_LOG_1)
    moveEventLogToUploaded(EVENT_LOG_0)
    moveEventLogToUploaded(EVENT_LOG_1)
    impl.forceSyncStatus(syncStatus)
    testCoroutineDispatchers.runCurrent()

    val syncStatuses = impl.getSyncStatuses()

    // The most recent tracked status should be the forced one.
    assertThat(syncStatuses.last()).isEqualTo(syncStatus)
  }

  @Test
  @Iteration("initial_unknown", "status=INITIAL_UNKNOWN")
  @Iteration("waiting_to_start_uploading", "status=WAITING_TO_START_UPLOADING")
  @Iteration("data_uploading", "status=DATA_UPLOADING")
  @Iteration("data_uploaded", "status=DATA_UPLOADED")
  @Iteration("no_connectivity", "status=NO_CONNECTIVITY")
  @Iteration("upload_error", "status=UPLOAD_ERROR")
  fun testGetSyncStatuses_withLogStore_forceStatus_reportUploading_returnsWithForcedStatus() {
    impl.initializeEventLogStore(logsCacheStore)
    connectNetwork()
    queueEventLogForUploading(EVENT_LOG_0)
    impl.reportUploadingStarted()
    testCoroutineDispatchers.runCurrent()
    impl.forceSyncStatus(syncStatus)
    testCoroutineDispatchers.runCurrent()

    val syncStatuses = impl.getSyncStatuses()

    // The most recent tracked status should be the forced one.
    assertThat(syncStatuses.last()).isEqualTo(syncStatus)
  }

  @Test
  @Iteration("initial_unknown", "status=INITIAL_UNKNOWN")
  @Iteration("waiting_to_start_uploading", "status=WAITING_TO_START_UPLOADING")
  @Iteration("data_uploading", "status=DATA_UPLOADING")
  @Iteration("data_uploaded", "status=DATA_UPLOADED")
  @Iteration("no_connectivity", "status=NO_CONNECTIVITY")
  @Iteration("upload_error", "status=UPLOAD_ERROR")
  fun testGetSyncStatuses_withLogStore_forceStatus_reportUploadingEnded_returnsWithForcedStatus() {
    impl.initializeEventLogStore(logsCacheStore)
    connectNetwork()
    queueEventLogForUploading(EVENT_LOG_0)
    impl.reportUploadingStarted()
    testCoroutineDispatchers.runCurrent()
    moveEventLogToUploaded(EVENT_LOG_0)
    impl.reportUploadingEnded()
    testCoroutineDispatchers.runCurrent()
    impl.forceSyncStatus(syncStatus)
    testCoroutineDispatchers.runCurrent()

    val syncStatuses = impl.getSyncStatuses()

    // The most recent tracked status should be the forced one.
    assertThat(syncStatuses.last()).isEqualTo(syncStatus)
  }

  @Test
  @Iteration("initial_unknown", "status=INITIAL_UNKNOWN")
  @Iteration("waiting_to_start_uploading", "status=WAITING_TO_START_UPLOADING")
  @Iteration("data_uploading", "status=DATA_UPLOADING")
  @Iteration("data_uploaded", "status=DATA_UPLOADED")
  @Iteration("no_connectivity", "status=NO_CONNECTIVITY")
  @Iteration("upload_error", "status=UPLOAD_ERROR")
  fun testGetSyncStatuses_withInitedLogStore_forceStatus_reportError_returnsWithForcedStatus() {
    impl.initializeEventLogStore(logsCacheStore)
    connectNetwork()
    queueEventLogForUploading(EVENT_LOG_0)
    impl.reportUploadingStarted()
    testCoroutineDispatchers.runCurrent()
    impl.reportUploadError()
    testCoroutineDispatchers.runCurrent()
    impl.forceSyncStatus(syncStatus)
    testCoroutineDispatchers.runCurrent()

    val syncStatuses = impl.getSyncStatuses()

    // The most recent tracked status should be the forced one.
    assertThat(syncStatuses.last()).isEqualTo(syncStatus)
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  // TODO(#89): Move this to a common test application component.
  @Module
  class TestModule {
    @Provides
    @Singleton
    fun provideContext(application: Application): Context {
      return application
    }
  }

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(
    modules = [
      TestModule::class, LogStorageModule::class, NetworkConnectionUtilDebugModule::class,
      TestLogReportingModule::class, LoggerModule::class, TestDispatcherModule::class,
      LocaleProdModule::class, FakeOppiaClockModule::class, RobolectricModule::class
    ]
  )
  interface TestApplicationComponent : DataProvidersInjector {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder
      fun build(): TestApplicationComponent
    }

    fun inject(test: TestSyncStatusManagerTest)
  }

  class TestApplication : Application(), DataProvidersInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerTestSyncStatusManagerTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(test: TestSyncStatusManagerTest) {
      component.inject(test)
    }

    override fun getDataProvidersInjector(): DataProvidersInjector = component
  }

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
