package org.oppia.android.domain.workmanager.testing

import android.content.Context
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
import java.util.UUID
import java.util.concurrent.CopyOnWriteArraySet
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * A general-purpose, all-in-one test utility when interacting with [WorkManager] in Oppia tests.
 *
 * This utility contains a bunch of critical setup and interaction pathways that tests needing to
 * run or instead jobs will need to use. This utility is used over direct interactions with
 * [WorkManager]'s library to help simplify changes and limitations in the API across versions, and
 * because some of the specific operations sometimes require version-sensitive operations (such as
 * accessing [WorkManager]'s internals). It's expected that [WorkManager] never needs to be
 * interacted with directly in tests, and any missing functionality should be added directly to this
 * utility as needed.
 *
 * Note that [initializeWorkManager] *must* be called before this driver can be interacted with.
 * Also, this driver must be used over [androidx.work.testing.TestDriver] and
 * [WorkManagerTestInitHelper] since it performs Oppia-specific setup that may need to be manually
 * replicated when not using this utility. There's some complex nuance with properly synchronizing
 * between a worker and the app's test coroutine dispatchers--make sure to carefully read
 * [initializeWorkManager]'s documentation before opting out of its default setup.
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

  /**
   * Initializes [WorkManager] for testing. This must be called before interacting with either
   * [WorkManager] production code or with this driver.
   *
   * Developers should pay particular attention to the arguments below since they can significantly
   * change the behaviors of even non-worker tests in a test suite. Most test suites specific to
   * verifying workers should be able to rely on the default arguments.
   *
   * By default this method configures the test to be able to advanced periodic jobs simply by using
   * [TestCoroutineDispatchers.advanceTimeBy], though care should be taken to never use
   * [TestCoroutineDispatchers.advanceUntilIdle] since periodic jobs will run indefinitely unless
   * cancelled.
   *
   * This method must only be accessed on the main thread.
   *
   * @param configurationOverride the [Configuration] that [WorkManager] will use, or `null` by
   *     default. If `null`, a custom [Configuration] will be created that correctly forwards worker
   *     creation requests to [BootstrapOppiaWorker.Factory] and synchronizes internal [WorkManager]
   *     execution with the app's test dispatchers. It also increases logging verbosity. This
   *     default configuration should be used unless a test has specifically special needs.
   * @param autoSetUpShadowLogForwarding whether to automatically forward Robolectric [ShadowLog]s
   *     to the standard [System.out] for easier logging (since worker logcat logs will then appear
   *     in test error logs). This is enabled by default but, since it changes the behaviors of all
   *     tests run with this initialization, it can be disabled. It's recommended to keep this
   *     enabled for easier test debugging when working with workers.
   * @param autoSetModeUptimeMillisClockMode whether to change [FakeOppiaClock] to use
   *     [MODE_UPTIME_MILLIS]. This can drastically change test behaviors so it may not always be
   *     desired, but it is sometimes necessary for testing certain worker periodic cases. Note that
   *     this is enabled by default only if the default [Configuration] is used since both are
   *     required for those periodic cases to work correctly.
   */
  fun initializeWorkManager(
    configurationOverride: Configuration? = null,
    autoSetUpShadowLogForwarding: Boolean = true,
    autoSetModeUptimeMillisClockMode: Boolean = configurationOverride == null
  ) {
    check(!this::workManager.isInitialized) { "Cannot initialize WorkManager more than once." }
    val config = configurationOverride ?: createTestOnlyConfiguration()
    // TODO(#6115): Use USE_TIME_BASED_SCHEDULING and update Configuration below to sync with
    //  FakeOppiaClock.getCurrentTimeMs to make TestJobSchedulerMixin redundant.
    WorkManagerTestInitHelper.initializeTestWorkManager(context, config)
    workManager = WorkManager.getInstance(context)
    mixin.startWatchingForWorkers()

    // WorkManager and workers output most their issues issues to logcat, so this ensures those get
    // printed to the test log. This leads to a noisier test run but it makes debugging failures
    // significantly easier.
    if (autoSetUpShadowLogForwarding) ShadowLog.stream = System.out

    // Ensure OppiaClock is synchronized with FakeSystemClock since the latter is used when
    // controlling time with dispatchers.
    if (autoSetModeUptimeMillisClockMode) fakeOppiaClock.setFakeTimeMode(MODE_UPTIME_MILLIS)
  }

  /**
   * Looks up the [WorkerMonitor] corresponding to the specific [workerName] and [operation].
   *
   * Note that this will throw if no such job has been scheduled yet (either via a production worker
   * scheduler or via [runPeriodicWork]). Note that this cannot be used for one-off jobs since they
   * are not uniquely identifiable except by their [UUID].
   *
   * Note also that the monitor returned here will always be the same monitor for the same unique
   * periodic job unless that same job is attempted to be scheduled twice (in which case a monitor
   * with [WorkerMonitor.isRedundant] being `true` will be returned).
   *
   * This method must only be accessed on the main thread.
   */
  fun lookUpPeriodicMonitor(workerName: String, operation: OppiaWorker.TaskType): WorkerMonitor {
    return lookUpMonitor(lookUpUniqueId(workerName, operation))
  }

  /**
   * Runs a one-off Oppia worker corresponding to the specified [workerName] and [operation] by
   * default, otherwise runs an unsupported non-[BootstrapOppiaWorker] (for testing purposes).
   *
   * This method must only be accessed on the main thread.
   *
   * @param workerName the worker to run, or `null` for an invalid worker
   * @param operation the worker task to run, or `null` for an invalid worker
   * @param workerClass the invalid worker class to run, or [BootstrapOppiaWorker]
   * @return the [WorkerMonitor] that can be used to inspect and observe the job and its results
   */
  fun runOneOffWork(
    workerName: String?,
    operation: OppiaWorker.TaskType?,
    workerClass: Class<out ListenableWorker> = BootstrapOppiaWorker::class.java
  ): WorkerMonitor = enqueueWork(workerName, operation, intervalMins = null, workerClass)

  /**
   * Starts a periodic Oppia worker corresponding to the specified [workerName] and [operation] by
   * default, otherwise runs an unsupported non-[BootstrapOppiaWorker] (for testing purposes).
   *
   * The [WorkerMonitor] returned can be used to inspect the latest status of the job based on its
   * last run (see [WorkerMonitor.state]. This API is specifically designed to not necessarily track
   * each individual run--the test itself should infer that multiple runs occurred by observing a
   * worker's output state.
   *
   * [WorkManager] executes periodic jobs immediately when they are first scheduled and this method
   * retains that behavior (or, rather, does not do anything to change it).
   *
   * This method must only be accessed on the main thread.
   *
   * @param workerName the worker to run, or `null` for an invalid worker
   * @param operation the worker task to run, or `null` for an invalid worker
   * @param repeatIntervalMins the minimum number of minutes between worker runs (15 by default)
   * @param workerClass the invalid worker class to run, or [BootstrapOppiaWorker]
   * @return the [WorkerMonitor] that can be used to inspect and observe the job and its results
   */
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

  private fun lookUpMonitor(requestedId: UUID, actualId: UUID = requestedId): WorkerMonitor {
    // Use the requested ID so that a new implementation is created for periodic requests that may
    // actually be redundant.
    return memoizedMonitors.getOrPut(requestedId) {
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
    // access some of the worker's properties (such as its scheduled period) without this. *Some* of
    // these can be observed behaviorally, but not all can so it's reasonable to break into
    // WorkManager's implementation like this for those specific properties to ensure that
    // WorkManager is processing them the way tests expect.
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
          appContext: Context,
          workerClassName: String,
          workerParameters: WorkerParameters
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

  /**
   * A monitor for observing the results or changed statuses of a previously or future run
   * [WorkManager] worker.
   *
   * All properties and methods of this class must only be accessed on the main thread.
   *
   * @property id the unique [UUID] corresponding to the specific job being monitored
   * @property isRedundant whether this monitor corresponds to a specific periodic work request that
   *     attempted to reschedule an already-scheduled periodic job. Note that it is generally not
   *     valid to inspect any of the properties of the monitor other than its [id] if this property
   *     is `true`. `id` can be used to lookup the correct monitor to use, instead.
   * @property tags the list of tags associated with the worker
   * @property isPeriodic whether this corresponds to a periodic or one-off job
   * @property intervalDurationMs the number of milliseconds to wait between periodic job runs. This
   *     defaults to `0` for one-off jobs (which is never a valid value for periodic jobs).
   * @property requiredNetworkType the [NetworkType] constraint that must be met before the
   *     monitored job will be allowed to run. See [WorkManager]'s documentation for the default
   *     value if left undefined.
   * @property requiresBatteryNotLow a constraint indicating whether the device (test environment)
   *     must not have a low battery before the monitored job will be allowed to run. See
   *     [WorkManager]'s documentation for the default value if left undefined.
   */
  inner class WorkerMonitor(
    val id: UUID,
    val isRedundant: Boolean,
    val tags: List<String>,
    val isPeriodic: Boolean,
    val intervalDurationMs: Long,
    val requiredNetworkType: NetworkType,
    val requiresBatteryNotLow: Boolean
  ) {
    /**
     * Whether to automatically apply constraints when it's time for the periodic job being
     * monitored to run. This is `false` (off) by default until [forceConstraintsMet] is called.
     *
     * [WorkManager] will not allowed an constrained periodic job to run in its test environment
     * unless it both has periodicity to run and its constrains are met (which must be reset each
     * time the worker should run). This can be tedious when testing multiple runs in sequence, so
     * setting this to true automatically fulfills constrains for the job when it's time to run at
     * its next period.
     *
     * This should not be changed for one-off jobs since it doesn't have any meaning. It also should
     * not be changed for redundant ([isRedundant]) jobs since they cannot actually run.
     */
    var autoTrackConstraints: Boolean = false
      set(value) {
        check(isPeriodic) { "It doesn't make sense to set auto-constraints for a one-off job. " }
        check(!isRedundant) { "Cannot override auto-tracking on a redundant monitor." }
        field = value
        hasAutoTrackOverride = true
      }

    private var hasAutoTrackOverride = false
    private var hasInitializedAutoTrackConstraints = false

    /**
     * The current [WorkInfo.State] corresponding to the monitored job.
     *
     * Note that each time this is accessed the very latest state will be fetched. It may also throw
     * an exception if it is accessed for a job that's disappeared from [WorkManager]'s internal
     * tracking (which generally should only happen if the test does something unusual or
     * [WorkManager] enters a broken state).
     */
    val state: WorkInfo.State
      get() = runInBackground { lookUpLatestWorkInfoInBackground(id).state }

    /**
     * Notifies [WorkManager] that the constraints for this job have been and thus it can be run.
     *
     * Note that this will automatically enable [autoTrackConstraints] unless it's been explicitly
     * set to false or this isn't a periodic job.
     *
     * Constraints must be met for constrained jobs to run, and [WorkManager] doesn't provide any
     * means to enable specific constraints (it's all or nothing other than timing for delayed or
     * periodic jobs). The note on periodic jobs is specifically important because calling this is
     * not sufficient for a periodic job to run its next cycle--the system clock still needs to be
     * advanced.
     */
    fun forceConstraintsMet() {
      if (!hasInitializedAutoTrackConstraints) {
        if (!hasAutoTrackOverride && isPeriodic) autoTrackConstraints = true
        hasInitializedAutoTrackConstraints = true
      }
      testDriver.setAllConstraintsMet(id)
      testCoroutineDispatchers.runCurrent()
    }
  }

  // TODO(#6115): Remove this utility (see the note in initializeWorkManager above for how the
  //  configuration must change in order for this utility to become obsolete.
  /**
   * Custom mixin to bridge the gap with older [WorkManager] test libraries to emulate periodic jobs
   * by scheduling requests to re-run them at their periodic intervals using background dispatchers
   * (to ensure correct interoperability with the test dispatcher framework).
   *
   * This utility will be able to be removed once a newer test library is used since [WorkManager]
   * eventually built this support in (and in a way that can correctly interoperate with the app's
   * custom test dispatchers).
   */
  private inner class TestJobSchedulerMixin {
    private val workDatabase get() = (workManager as WorkManagerImpl).workDatabase
    private val trackedWorkers = CopyOnWriteArraySet<UUID>()

    /**
     * Enable the mixin to start monitoring for workers being scheduled since each worker needs to
     * be tracked in order for the mixin to know when it's time to reschedule the job.
     */
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
