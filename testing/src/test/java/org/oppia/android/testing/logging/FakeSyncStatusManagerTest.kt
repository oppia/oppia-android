package org.oppia.android.testing.logging

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.data.DataProviderTestMonitor
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.data.DataProvidersInjector
import org.oppia.android.util.data.DataProvidersInjectorProvider
import org.oppia.android.util.locale.LocaleProdModule
import org.oppia.android.util.logging.LoggerModule
import org.oppia.android.util.logging.SyncStatusManager.SyncStatus.DATA_UPLOADED
import org.oppia.android.util.logging.SyncStatusManager.SyncStatus.DATA_UPLOADING
import org.oppia.android.util.logging.SyncStatusManager.SyncStatus.INITIAL_UNKNOWN
import org.oppia.android.util.logging.SyncStatusManager.SyncStatus.NETWORK_ERROR
import org.oppia.android.util.networking.NetworkConnectionUtilDebugModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [FakeSyncStatusManager]. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = FakeSyncStatusManagerTest.TestApplication::class)
class FakeSyncStatusManagerTest {
  @Inject lateinit var fakeSyncStatusManager: FakeSyncStatusManager
  @Inject lateinit var monitorFactory: DataProviderTestMonitor.Factory

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testGetSyncStatus_initialState_returnsDefaultValue() {
    val syncStatusProvider = fakeSyncStatusManager.getSyncStatus()

    val state = monitorFactory.waitForNextSuccessfulResult(syncStatusProvider)
    assertThat(state).isEqualTo(INITIAL_UNKNOWN)
  }

  @Test
  fun testGetSyncStatuses_initialState_returnsEmptyList() {
    val syncStatuses = fakeSyncStatusManager.getSyncStatuses()

    assertThat(syncStatuses).isEmpty()
  }

  @Test
  fun testGetSyncStatus_changeToUploading_returnsUploading() {
    fakeSyncStatusManager.setSyncStatus(DATA_UPLOADING)

    val syncStatusProvider = fakeSyncStatusManager.getSyncStatus()

    // The retrieved state should be updated, similar to the production implementation.
    val state = monitorFactory.waitForNextSuccessfulResult(syncStatusProvider)
    assertThat(state).isEqualTo(DATA_UPLOADING)
  }

  @Test
  fun testGetSyncStatus_changeToUploading_existingSubscription_updatesSubscriptionToUploading() {
    val syncStatusProvider = fakeSyncStatusManager.getSyncStatus()
    val monitor = monitorFactory.createMonitor(syncStatusProvider)
    monitor.waitForNextResult()

    fakeSyncStatusManager.setSyncStatus(DATA_UPLOADING)

    // The existing subscription should be updated due to a notification (similar to the production
    // implementation).
    val state = monitor.waitForNextSuccessResult()
    assertThat(state).isEqualTo(DATA_UPLOADING)
  }

  @Test
  fun testGetSyncStatuses_changeToUploading_returnsUploading() {
    fakeSyncStatusManager.setSyncStatus(DATA_UPLOADING)

    val syncStatuses = fakeSyncStatusManager.getSyncStatuses()

    // The new status value should be captured.
    assertThat(syncStatuses).containsExactly(DATA_UPLOADING)
  }

  @Test
  fun testGetSyncStatuses_changeToUploadingTwice_returnsDuplicatedUploadingStatuses() {
    fakeSyncStatusManager.setSyncStatus(DATA_UPLOADING)
    fakeSyncStatusManager.setSyncStatus(DATA_UPLOADING)

    val syncStatuses = fakeSyncStatusManager.getSyncStatuses()

    // Multiple changes to sync status should be captured.
    assertThat(syncStatuses).containsExactly(DATA_UPLOADING, DATA_UPLOADING)
  }

  @Test
  fun testGetSyncStatus_changeToUploadingThenNetworkError_returnsNetworkError() {
    fakeSyncStatusManager.setSyncStatus(DATA_UPLOADING)
    fakeSyncStatusManager.setSyncStatus(NETWORK_ERROR)

    val syncStatusProvider = fakeSyncStatusManager.getSyncStatus()

    // The latest retrieved state should be updated, similar to the production implementation.
    val state = monitorFactory.waitForNextSuccessfulResult(syncStatusProvider)
    assertThat(state).isEqualTo(NETWORK_ERROR)
  }

  @Test
  fun testGetSyncStatuses_changeToUploadingThenNetworkError_returnsUploadingAndNetworkErrors() {
    fakeSyncStatusManager.setSyncStatus(DATA_UPLOADING)
    fakeSyncStatusManager.setSyncStatus(NETWORK_ERROR)

    val syncStatuses = fakeSyncStatusManager.getSyncStatuses()

    // Both status values should be captured.
    assertThat(syncStatuses).containsExactly(DATA_UPLOADING, NETWORK_ERROR)
  }

  @Test
  fun testGetSyncStatus_changeBetweenSeveralStates_returnsLatest() {
    fakeSyncStatusManager.setSyncStatus(DATA_UPLOADING)
    fakeSyncStatusManager.setSyncStatus(NETWORK_ERROR)
    fakeSyncStatusManager.setSyncStatus(DATA_UPLOADING)
    fakeSyncStatusManager.setSyncStatus(DATA_UPLOADED)

    val syncStatusProvider = fakeSyncStatusManager.getSyncStatus()

    // The latest retrieved state should be updated, similar to the production implementation.
    val state = monitorFactory.waitForNextSuccessfulResult(syncStatusProvider)
    assertThat(state).isEqualTo(DATA_UPLOADED)
  }

  @Test
  fun testGetSyncStatuses_changeBetweenSeveralStates_returnsAllStatusesInOrder() {
    fakeSyncStatusManager.setSyncStatus(DATA_UPLOADING)
    fakeSyncStatusManager.setSyncStatus(NETWORK_ERROR)
    fakeSyncStatusManager.setSyncStatus(DATA_UPLOADING)
    fakeSyncStatusManager.setSyncStatus(DATA_UPLOADED)

    val syncStatuses = fakeSyncStatusManager.getSyncStatuses()

    // All status values should be captured, in order, including repeats.
    assertThat(syncStatuses).containsExactly(
      DATA_UPLOADING, NETWORK_ERROR, DATA_UPLOADING, DATA_UPLOADED
    ).inOrder()
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

    fun inject(test: FakeSyncStatusManagerTest)
  }

  class TestApplication : Application(), DataProvidersInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerFakeSyncStatusManagerTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(test: FakeSyncStatusManagerTest) {
      component.inject(test)
    }

    override fun getDataProvidersInjector(): DataProvidersInjector = component
  }
}
