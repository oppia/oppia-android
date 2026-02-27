package org.oppia.android.domain.workmanager.testing

import android.content.Context
import android.os.Looper
import android.util.Log
import androidx.room.InvalidationTracker
import androidx.work.Configuration
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy.KEEP
import androidx.work.ListenableWorker
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import androidx.work.impl.WorkManagerImpl
import androidx.work.impl.model.WorkSpec
import androidx.work.testing.WorkManagerTestInitHelper
import com.google.common.truth.Truth.assertThat
import java.util.UUID
import java.util.concurrent.CopyOnWriteArraySet
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.guava.asDeferred
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.oppia.android.domain.workmanager.BootstrapOppiaWorker
import org.oppia.android.domain.workmanager.BootstrapOppiaWorker.Companion.DELEGATED_WORKER_NAME_INPUT_KEY
import org.oppia.android.domain.workmanager.BootstrapOppiaWorker.Companion.constructTaskTypeKey
import org.oppia.android.domain.workmanager.OppiaWorker
import org.oppia.android.testing.threading.CoroutineExecutorService
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.time.FakeOppiaClock
import org.oppia.android.testing.time.FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS
import org.oppia.android.util.threading.BackgroundDispatcher
import org.robolectric.shadows.ShadowLog

// TODO: Ban TestDriver interactions?
// TODO: Ban WorkManager.getInstance and WorkManagerTestInitHelper interactions.
// TODO: Just ban WorkManager entirely? Probably best way to go...
// TODO: Document that tests must set up FakeOppiaClock to MODE_UPTIME_MILLIS mode.
/**
 * A general-purpose, all-in-one test utility when interacting with WorkManager in Oppia tests.
 *
 * This utility contains a bunch of critical setup and interaction pathways that tests needing to
 * run or instead jobs will need to use. This utility is used over direct interactions with
 * WorkManager's library to help simplify changes and limitations in the API across versions, and
 * because some of the specific operations sometimes require version-sensitive operations (such as
 * accessing WorkManager's internals).
 *
 * Note that [initializeWorkManager] *must* be called before this driver can be interacted with.
 * Also, this driver must be used over [androidx.work.testing.TestDriver] and
 * [WorkManagerTestInitHelper] since it performs Oppia-specific setup that may need to be manually
 * replicated when not using this utility.
 *
 * Finally, it's expected that [WorkManager] never needs to be interacted with directly in tests.
 * Instead, use this utility. If a new part of [WorkManager]'s API is needed in tests and it's not
 * yet available in this utility, it should be added here.
 */
@Singleton
@OptIn(ExperimentalCoroutinesApi::class)
class OppiaWorkManagerTestDriver @Inject constructor(
  private val context: Context,
  private val testCoroutineDispatchers: TestCoroutineDispatchers,
  private val bootstrapOppiaWorkerFactory: BootstrapOppiaWorker.Factory,
  private val fakeOppiaClock: FakeOppiaClock,
  @BackgroundDispatcher private val backgroundDispatcher: CoroutineDispatcher
) {
  private lateinit var workManager: WorkManager
  private val workDatabase get() = (workManager as WorkManagerImpl).workDatabase
  private val testDriver by lazy { checkNotNull(WorkManagerTestInitHelper.getTestDriver(context)) }
  private val mixin by lazy { TestJobSchedulerMixin() }
  private val memoizedMonitors by lazy { mutableMapOf<UUID, WorkerMonitor>() }

  // TODO: Reorganize members.

  fun initializeWorkManager(
    configurationOverride: Configuration? = null,
    autoSetUpShadowLogForwarding: Boolean = true,
    autoSetModeUptimeMillisClockMode: Boolean = configurationOverride == null
  ) {
    check(!this::workManager.isInitialized) { "Cannot initialize WorkManager more than once." }
    val config = configurationOverride ?: createTestOnlyConfiguration()
    // TODO: Use USE_TIME_BASED_SCHEDULING here & set Configuration clock to sync with FakeOppiaClock.getCurrentTimeMs.
    WorkManagerTestInitHelper.initializeTestWorkManager(context, config)
    workManager = WorkManager.getInstance(context)
    mixin.startWatchingForWorkers()

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

  // TODO: Document memoization?
  fun lookUpPeriodicMonitor(workerName: String, operation: OppiaWorker.TaskType): WorkerMonitor {
    return lookUpMonitor(lookUpUniqueId(workerName, operation))
  }

  private fun lookUpMonitor(requestedId: UUID, actualId: UUID = requestedId): WorkerMonitor {
    // Use the requested ID so that a new implementation is created for periodic requests that may
    // actually be redundant.
    return memoizedMonitors.computeIfAbsent(requestedId) {
      // Initialize properties that shouldn't change without a new job being scheduled.
      val workInfo = runInBackground { lookUpLatestWorkInfoInBackground(actualId) }
      val workSpec = lookUpWorkSpec(actualId)
      WorkerMonitor(
        actualId,
        isRedundant = requestedId != actualId,
        tags = workInfo.tags.toList(),
        isPeriodic = workSpec.isPeriodic,
        intervalDurationMs = workSpec.intervalDuration,
        requiredNetworkType = workSpec.constraints.requiredNetworkType,
        requiresBatteryNotLow = workSpec.constraints.requiresBatteryNotLow()
      )
    }
  }

  fun runOneOffWork(
    workerName: String?,
    operation: OppiaWorker.TaskType?,
    workerClass: Class<out ListenableWorker> = BootstrapOppiaWorker::class.java
  ): WorkerMonitor = enqueueWork(workerName, operation, intervalMins = null, workerClass)

  // TODO: Document null worker name.
  fun runPeriodicWork(
    workerName: String?,
    operation: OppiaWorker.TaskType?,
    repeatIntervalMins: Long = 15,
    workerClass: Class<out ListenableWorker> = BootstrapOppiaWorker::class.java
  ): WorkerMonitor = enqueueWork(workerName, operation, repeatIntervalMins, workerClass)

  private fun enqueueWork(
    workerName: String?,
    operation: OppiaWorker.TaskType?,
    intervalMins: Long?,
    workerClass: Class<out ListenableWorker>
  ): WorkerMonitor {
    val inputData =
      Data.Builder()
        .putString(DELEGATED_WORKER_NAME_INPUT_KEY, workerName)
        .putString(constructTaskTypeKey(workerName ?: "invalid_worker"), operation?.persistentName)
        .build()
    val uniqueWorkName = "$workerName.${operation?.persistentName}"

    val id = runInBackground {
      if (intervalMins != null) {
        PeriodicWorkRequest.Builder(workerClass, intervalMins, TimeUnit.MINUTES)
          .setInputData(inputData)
          .build()
          .also { workManager.enqueueUniquePeriodicWork(uniqueWorkName, KEEP, it) }
      } else {
        OneTimeWorkRequest.Builder(workerClass)
          .setInputData(inputData)
          .build()
          .also(workManager::enqueue)
      }.id
    }

    return if (intervalMins != null && workerName != null && operation != null) {
      // Periodic jobs may have already been scheduled so check that first.
      lookUpMonitor(id, actualId = lookUpUniqueId(workerName, operation))
    } else lookUpMonitor(id)
  }

  private fun lookUpUniqueId(workerName: String, operation: OppiaWorker.TaskType): UUID {
    val workName = "$workerName.${operation.persistentName}"
    val id = runInBackground {
      workManager.getWorkInfosForUniqueWork(workName).asDeferred().await().singleOrNull()?.id
    }
    return checkNotNull(id) { "Expected worker ID for unique worker: $workerName, $operation." }
  }

  private suspend fun lookUpLatestWorkInfoInBackground(id: UUID): WorkInfo {
    return checkNotNull(workManager.getWorkInfoById(id).asDeferred().await()) {
      "Expected live WorkInfo to be available for worker: $id."
    }
  }

  private fun lookUpWorkSpec(id: UUID): WorkSpec {
    // This is a very hacky solution since it relies on WorkManagerImpl, but there's no other way to
    // access some of the worker's properties (such as its scheduled period) without this. That
    // could be observed behaviorally by leveraging clock management, but WorkManager already makes
    // that challenging. See ... TODO: Link to new issue here to fix timing. Seems simple to just make this observational rather than using the database hack.
    return checkNotNull(workDatabase.workSpecDao().getWorkSpec(id.toString())) {
      "Expected database WorkSpec to be available for worker: $id."
    }
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

  private fun <T> runInBackground(func: suspend () -> T): T {
    val resultDeferred = CoroutineScope(backgroundDispatcher).async { func() }
    testCoroutineDispatchers.runCurrent()
    assertThat(resultDeferred.isCompleted).isTrue()
    return resultDeferred.getCompleted()
  }

  inner class WorkerMonitor(
    val id: UUID,
    val isRedundant: Boolean,
    val tags: List<String>,
    val isPeriodic: Boolean,
    val intervalDurationMs: Long,
    val requiredNetworkType: NetworkType,
    val requiresBatteryNotLow: Boolean
  ) {
    var autoTrackConstraints: Boolean = true
      set(value) {
        check(!isRedundant) { "Cannot override auto-tracking on a redundant monitor." }
        field = value
      }

    // TODO: Document that this will fail if the worker disappears from records. Also, this must be called from main thread because it will always look up the latest state.
    val state: WorkInfo.State
      get() = runInBackground { lookUpLatestWorkInfoInBackground(id).state }

    // Must be called to ensure a worker is run when it has constraints, but won't override the
    // time-based scheduling constraint (the clock still needs to be advanced).
    // TODO: Document that this persists for periodic tasks (so that constraints don't need to be re-set). Document auto runCurrent() call. Reference autoTrackConstraints.
    // There's no way to configure WorkManager to set specific constraints or to follow both
    // constraints and fixed time.
    fun forceConstraintsMet() {
      testDriver.setAllConstraintsMet(id)
      testCoroutineDispatchers.runCurrent()
    }
  }

  // TODO: Document that is VERY limited and doesn't implement the more complex work manager features like back-off. It also only handles periodic jobs and doesn't interop with running jobs directly outside this tool. It doesn't support initial delays since we don't use them. Document the initialization needs and how/why/when to use this.
  // TODO: Add issue to remove.
  private inner class TestJobSchedulerMixin {
    private val workDatabase get() = (workManager as WorkManagerImpl).workDatabase
    private val trackedWorkers = CopyOnWriteArraySet<UUID>()

    fun startWatchingForWorkers() {
      workDatabase.invalidationTracker.addObserver(
        object : InvalidationTracker.Observer("WorkSpec") {
          override fun onInvalidated(tables: MutableSet<String>) {
            rescanWorkers()
          }
        })
      rescanWorkers()
    }

    private fun rescanWorkers() {
      // Check if any workers have changed.
      CoroutineScope(backgroundDispatcher).launch {
        val workerIds = workDatabase.workSpecDao().allWorkSpecIds.map(UUID::fromString)
        withContext(Dispatchers.Main) {
          // This is presumably needed to avoid a deadlock since lookUpMonitor can synchronize
          // against internal WorkManager state that may not be finished processing.
          testCoroutineDispatchers.runCurrent()
          val monitors = workerIds.map(::lookUpMonitor)
          for (monitor in monitors) {
            if (monitor.isPeriodic && monitor.id !in trackedWorkers) {
              trackedWorkers += monitor.id
              maybeScheduleNextJob(monitor, monitor.state)
            }
          }
        }
      }
    }

    private fun maybeScheduleNextJob(monitor: WorkerMonitor, latestState: WorkInfo.State) {
      if (latestState != WorkInfo.State.CANCELLED) {
        CoroutineScope(backgroundDispatcher).launch {
          delay(monitor.intervalDurationMs)
          if (monitor.autoTrackConstraints) testDriver.setAllConstraintsMet(monitor.id)
          testDriver.setPeriodDelayMet(monitor.id)
          withContext(Dispatchers.Main) {
            // Note that state MUST be retrieved here because it has to be done on the main thread.
            maybeScheduleNextJob(monitor, monitor.state)
          }
        }
      }
    }
  }
}
