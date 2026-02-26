package org.oppia.android.domain.workmanager.testing

import android.content.Context
import android.util.Log
import androidx.room.InvalidationTracker
import androidx.work.Configuration
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ListenableWorker
import androidx.work.OneTimeWorkRequest
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import androidx.work.impl.WorkManagerImpl
import androidx.work.impl.model.WorkSpec
import androidx.work.testing.WorkManagerTestInitHelper
import com.google.common.truth.Truth
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.guava.asDeferred
import kotlinx.coroutines.launch
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
  private val autoForceConstraints = mutableSetOf<UUID>()

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
    TestJobSchedulerMixin().startWatchingForWorkers(workManager)

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

  // Must be called to ensure a worker is run when it has constraints, but won't override the
  // time-based scheduling constraint (the clock still needs to be advanced).
  // TODO: Document that this persists for periodic tasks (so that constraints don't need to be re-set). Document auto runCurrent() call.
  fun forceConstraintsMet(id: UUID, autoTrack: Boolean = true) {
    // There's no way to configure WorkManager to set specific constraints or to follow both
    // constraints and fixed time.
    testDriver.setAllConstraintsMet(id)
    if (autoTrack) autoForceConstraints += id
    testCoroutineDispatchers.runCurrent()
  }

  // Must be run on main thread.
  fun lookUpWorkInfo(id: UUID): WorkInfo? =
    runInBackground { workManager.getWorkInfoById(id).asDeferred().await() }

  fun lookUpWorkSpec(id: UUID): WorkSpec? {
    // This is a very hacky solution since it relies on WorkManagerImpl, but there's no other way to
    // access some of the worker's properties (such as its scheduled period) without this. That
    // could be observed behaviorally by leveraging clock management, but WorkManager already makes
    // that challenging. See ... TODO: Link to new issue here to fix timing. Seems simple to just make this observational rather than using the database hack.
    return workDatabase.workSpecDao().getWorkSpec(id.toString())
  }

  fun findUniqueId(workerName: String, taskType: OppiaWorker.TaskType): UUID {
    val workName = "$workerName.${taskType.persistentName}"
    return runInBackground {
      workManager.getWorkInfosForUniqueWork(workName).asDeferred().await().single().id
    }
  }

  inline fun <reified T: ListenableWorker> runOneOffWork(inputData: Data): WorkInfo =
    runOneOffWork(inputData, T::class.java)

  fun runOneOffWork(workerName: String, operation: OppiaWorker.TaskType): WorkInfo {
    return runOneOffWork(
      inputData = Data.Builder()
        .putString(DELEGATED_WORKER_NAME_INPUT_KEY, workerName)
        .putString(constructTaskTypeKey(workerName), operation.persistentName)
        .build(),
      workerClass = BootstrapOppiaWorker::class.java
    )
  }

  fun <T: ListenableWorker> runOneOffWork(inputData: Data, workerClass: Class<T>): WorkInfo {
    val id = runInBackground {
      OneTimeWorkRequest.Builder(workerClass)
        .setInputData(inputData)
        .build()
        .also(workManager::enqueue)
        .id
    }
    return checkNotNull(lookUpWorkInfo(id)) { "Expected one-off job to run." }
  }

  inline fun <reified T: ListenableWorker> runPeriodicWork(
    inputData: Data, repeatIntervalMins: Long = 15
  ): WorkInfo? = runPeriodicWork(inputData, repeatIntervalMins, TimeUnit.MINUTES, T::class.java)

  fun runPeriodicWork(
    workerName: String, operation: OppiaWorker.TaskType, repeatMins: Long = 15
  ): WorkInfo? {
    return runPeriodicWork(
      inputData = Data.Builder()
        .putString(DELEGATED_WORKER_NAME_INPUT_KEY, workerName)
        .putString(constructTaskTypeKey(workerName), operation.persistentName)
        .build(),
      repeatInterval = repeatMins,
      repeatIntervalUnit = TimeUnit.MINUTES,
      workerClass = BootstrapOppiaWorker::class.java
    )
  }

  fun <T: ListenableWorker> runPeriodicWork(
    inputData: Data, repeatInterval: Long, repeatIntervalUnit: TimeUnit, workerClass: Class<T>
  ): WorkInfo? {
    val id = runInBackground {
      PeriodicWorkRequest.Builder(workerClass, repeatInterval, repeatIntervalUnit)
        .setInputData(inputData)
        .build().also {
          val workerName = inputData.getString(DELEGATED_WORKER_NAME_INPUT_KEY)
          val taskTypeName =
            workerName?.let { inputData.getString(constructTaskTypeKey(workerName)) }
          val workName = "$workerName.$taskTypeName"
          workManager.enqueueUniquePeriodicWork(workName, ExistingPeriodicWorkPolicy.KEEP, it)
        }.id
    }
    return lookUpWorkInfo(id)
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
    Truth.assertThat(resultDeferred.isCompleted).isTrue()
    return resultDeferred.getCompleted()
  }

  // TODO: Document that is VERY limited and doesn't implement the more complex work manager features like back-off. It also only handles periodic jobs and doesn't interop with running jobs directly outside this tool. It doesn't support initial delays since we don't use them. Document the initialization needs and how/why/when to use this.
  // TODO: Add issue to remove.
  private inner class TestJobSchedulerMixin {
    private lateinit var workManager: WorkManager
    private val workDatabase get() = (workManager as WorkManagerImpl).workDatabase
    private val trackedWorkers = mutableSetOf<UUID>()

    fun startWatchingForWorkers(workManager: WorkManager) {
      this.workManager = workManager
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
      for (workSpec in workDatabase.workSpecDao().run { getWorkSpecs(allWorkSpecIds) }) {
        val id = UUID.fromString(workSpec.id)
        if (workSpec.isPeriodic && id !in trackedWorkers) {
          trackedWorkers += id
          maybeScheduleNextJob(id, workSpec)
        }
      }
    }

    private fun maybeScheduleNextJob(id: UUID, workSpec: WorkSpec? = lookUpWorkSpec(id)) {
      if (workSpec != null && workSpec.state != WorkInfo.State.CANCELLED) {
        check(workSpec.isPeriodic)
        val intervalMs = workSpec.intervalDuration
        CoroutineScope(backgroundDispatcher).launch {
          delay(intervalMs)
          val wmTestDriver = checkNotNull(WorkManagerTestInitHelper.getTestDriver(context))
          if (id in autoForceConstraints) wmTestDriver.setAllConstraintsMet(id)
          wmTestDriver.setPeriodDelayMet(id)
          maybeScheduleNextJob(id)
        }
      }
    }
  }
}
