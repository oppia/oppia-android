package org.oppia.android.domain.workmanager.testing

import android.content.Context
import android.util.Log
import androidx.work.Configuration
import androidx.work.WorkManager
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import androidx.work.testing.WorkManagerTestInitHelper
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import org.oppia.android.domain.workmanager.BootstrapOppiaWorker
import org.oppia.android.testing.threading.CoroutineExecutorService
import org.oppia.android.testing.time.FakeOppiaClock
import org.oppia.android.testing.time.FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS
import org.oppia.android.util.threading.BackgroundDispatcher
import org.robolectric.shadows.ShadowLog

// TODO: Ban WorkManager.getInstance and WorkManagerTestInitHelper interactions.
// TODO: Document that tests must set up FakeOppiaClock to MODE_UPTIME_MILLIS mode.
@Singleton
class OppiaWorkManagerTestInitializer @Inject constructor(
  private val context: Context,
  private val bootstrapOppiaWorkerFactory: BootstrapOppiaWorker.Factory,
  private val testDriver: OppiaWorkManagerTestDriver,
  private val testJobSchedulerMixin: TestJobSchedulerMixin,
  private val fakeOppiaClock: FakeOppiaClock,
  @BackgroundDispatcher private val backgroundDispatcher: CoroutineDispatcher
) {
  private var workManagerInternal: WorkManager? = null
  val workManager: WorkManager
    get() = checkNotNull(workManagerInternal) { "initializeWorkManager() must be called first." }

  fun initializeWorkManager(
    configurationOverride: Configuration? = null,
    autoSetUpShadowLogForwarding: Boolean = true,
    autoSetModeUptimeMillisClockMode: Boolean = configurationOverride == null
  ) {
    check(workManagerInternal == null) { "Cannot initialize work manager more than once." }
    val config = configurationOverride ?: createTestOnlyConfiguration()
    // TODO: Use USE_TIME_BASED_SCHEDULING here & set Configuration clock to sync with FakeOppiaClock.getCurrentTimeMs.
    WorkManagerTestInitHelper.initializeTestWorkManager(context, config)
    workManagerInternal = WorkManager.getInstance(context)
    testDriver.initialize(workManager)
    testJobSchedulerMixin.startWatchingForWorkers(workManager)

    // TODO: Move this to docs.
    // WorkManager and workers output most their issues issues to logcat, so this ensures those get
    // printed to the test log. This leads to a noisier test run but it makes debugging failures
    // significantly easier.
    if (autoSetUpShadowLogForwarding) ShadowLog.stream = System.out

    // TODO: Move this to docs.
    // Ensure OppiaClock is synchronized with FakeSystemClock since the latter is used when
    // controlling time with dispatchers.
    if (autoSetModeUptimeMillisClockMode) fakeOppiaClock.setFakeTimeMode(MODE_UPTIME_MILLIS)
  }

  private fun createTestOnlyConfiguration(): Configuration {
    val coroutineExecutorService = CoroutineExecutorService(backgroundDispatcher)
    return Configuration.Builder()
      .setMinimumLoggingLevel(Log.VERBOSE)
      .setExecutor(coroutineExecutorService)
      .setTaskExecutor(coroutineExecutorService)
      .setWorkerFactory(object : WorkerFactory() {
        override fun createWorker(
          appContext: Context, workerClassName: String, workerParameters: WorkerParameters
        ) = bootstrapOppiaWorkerFactory.createBootstrapWorker(workerClassName, workerParameters)
      })
      .build()
  }
}
