package org.oppia.android.domain.workmanager

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.Configuration
import androidx.work.NetworkType
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import androidx.work.impl.WorkManagerImpl
import androidx.work.impl.model.WorkSpec
import androidx.work.testing.WorkManagerTestInitHelper
import com.google.common.truth.Truth.assertThat
import com.google.firebase.FirebaseApp
import dagger.Binds
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey
import java.util.UUID
import java.util.concurrent.CopyOnWriteArrayList
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.guava.asDeferred
import org.oppia.android.domain.platformparameter.PlatformParameterControllerInjector
import org.oppia.android.domain.platformparameter.PlatformParameterControllerInjectorProvider
import org.oppia.android.testing.platformparameter.TestPlatformParameterModule
import org.oppia.android.testing.threading.CoroutineExecutorService
import org.oppia.android.testing.time.FakeOppiaClock
import org.oppia.android.util.caching.AssetModule
import org.oppia.android.util.locale.LocaleProdModule
import org.oppia.android.util.logging.LoggerModule
import org.oppia.android.util.logging.firebase.DebugLogReportingModule
import org.oppia.android.util.threading.BackgroundDispatcher
import org.oppia.android.util.threading.DispatcherInjector
import org.oppia.android.util.threading.DispatcherInjectorProvider
import org.robolectric.shadows.ShadowLog

/** Tests for [WorkManagerScheduler]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = WorkManagerSchedulerTest.TestApplication::class)
@OptIn(ExperimentalCoroutinesApi::class)
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
class WorkManagerSchedulerTest {
  @Inject lateinit var context: Context
  @Inject lateinit var testCoroutineDispatchers: TestCoroutineDispatchers
  @Inject lateinit var bootstrapOppiaWorkerFactory: BootstrapOppiaWorker.Factory
  @Inject lateinit var workManagerScheduler: WorkManagerScheduler
  @Inject lateinit var fakeOppiaClock: FakeOppiaClock
  @field:[Inject BackgroundDispatcher] lateinit var backgroundDispatcher: CoroutineDispatcher

  private lateinit var workManager: WorkManager
  private val testDriver by lazy { checkNotNull(WorkManagerTestInitHelper.getTestDriver(context)) }

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
    FirebaseApp.initializeApp(context)
    // Ensure OppiaClock is synchronized with FakeSystemClock since the latter is used when
    // controlling time with dispatchers.
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)

    val coroutineExecutorService = CoroutineExecutorService(backgroundDispatcher)
    val config = Configuration.Builder()
      .setMinimumLoggingLevel(Log.VERBOSE)
      .setExecutor(coroutineExecutorService)
      .setTaskExecutor(coroutineExecutorService)
      .setWorkerFactory(object: WorkerFactory() {
        override fun createWorker(
          appContext: Context, workerClassName: String, workerParameters: WorkerParameters
        ): BootstrapOppiaWorker =
          bootstrapOppiaWorkerFactory.createBootstrapWorker(workerClassName, workerParameters)
      })
      .setClock(fakeOppiaClock::getCurrentTimeMs)
      .build()
    WorkManagerTestInitHelper.initializeTestWorkManager(context, config, WorkManagerTestInitHelper.ExecutorsMode.USE_TIME_BASED_SCHEDULING)
    workManager = WorkManager.getInstance(context)

    // WorkManager and workers output most their issues issues to logcat, so this ensures those get
    // printed to the test log. This leads to a noisier test run but it makes debugging failures
    // significantly easier.
    ShadowLog.stream = System.out

    // Reset static state between tests.
    workerResults.clear()
  }

  @Test
  fun testSchedulePeriodicWorker_invalidWorkerName_cancelsWorker() {
    val id = schedulePeriodicWorker(
      "invalid_worker_name",
      MockOppiaWorker1.TaskType.WORKER1_TASK1,
      repeatInterval = 15,
      intervalUnit = TimeUnit.MINUTES
    )

    forceConstraintsMet(id)
    testCoroutineDispatchers.runCurrent()

    val workInfo = lookUpWorkInfo(id)
    assertThat(workerResults).isEmpty()
    assertThat(workInfo?.state).isEqualTo(WorkInfo.State.CANCELLED)
  }

  @Test
  fun testSchedulePeriodicWorker_invalidTaskType_doesNotRunAndCancelsWorker() {
    // Use a task type from a different worker since it won't be valid for this one.
    val id = schedulePeriodicWorker(
      MockOppiaWorker1.WORKER_NAME,
      MockOppiaWorker2.TaskType.WORKER2_TASK1,
      repeatInterval = 15,
      intervalUnit = TimeUnit.MINUTES
    )

    forceConstraintsMet(id)
    testCoroutineDispatchers.runCurrent()

    val workInfo = lookUpWorkInfo(id)
    assertThat(workerResults).isEmpty()
    assertThat(workInfo?.state).isEqualTo(WorkInfo.State.CANCELLED)
  }

  @Test
  fun testSchedulePeriodicWorker_validWorker_hasWorkerNameInTag() {
    val id = schedulePeriodicWorker(
      MockOppiaWorker1.WORKER_NAME,
      MockOppiaWorker1.TaskType.WORKER1_TASK1,
      repeatInterval = 15,
      intervalUnit = TimeUnit.MINUTES
    )

    val workInfo = lookUpWorkInfo(id)
    val tagList = workInfo?.tags?.toList()
    assertThat(tagList).hasSize(2)
    assertThat(tagList?.get(0)).isEqualTo(BootstrapOppiaWorker::class.java.name)
    assertThat(tagList?.get(1)).isEqualTo("MockOppiaWorker1.worker1_task1")
  }

  @Test
  fun testSchedulePeriodicWorker_scheduledForOneSecondPeriod_isClampedToFifteenMinutes() {
    val id = schedulePeriodicWorker(
      MockOppiaWorker1.WORKER_NAME,
      MockOppiaWorker1.TaskType.WORKER1_TASK1,
      repeatInterval = 1,
      intervalUnit = TimeUnit.SECONDS
    )

    val intervalDuration = lookUpWorkSpec(id)?.intervalDuration
    assertThat(intervalDuration).isEqualTo(TimeUnit.MINUTES.toMillis(15))
  }

  @Test
  fun testSchedulePeriodicWorker_scheduledForThreeHundredDays_isClampedToTwoWeeks() {
    val id = schedulePeriodicWorker(
      MockOppiaWorker1.WORKER_NAME,
      MockOppiaWorker1.TaskType.WORKER1_TASK1,
      repeatInterval = 300,
      intervalUnit = TimeUnit.DAYS
    )

    val intervalDuration = lookUpWorkSpec(id)?.intervalDuration
    assertThat(intervalDuration).isEqualTo(TimeUnit.DAYS.toMillis(14))
  }

  @Test
  fun testSchedulePeriodicWorker_validWorker_hasNoLowBatteryConstraint() {
    val id = schedulePeriodicWorker(
      MockOppiaWorker1.WORKER_NAME,
      MockOppiaWorker1.TaskType.WORKER1_TASK1,
      repeatInterval = 15,
      intervalUnit = TimeUnit.MINUTES
    )

    val workInfo = lookUpWorkInfo(id)
    val constraints = workInfo?.constraints
    assertThat(constraints?.requiresBatteryNotLow()).isTrue()
  }

  @Test
  fun testSchedulePeriodicWorker_validWorker_defaultConnectivityArg_hasConnectedNetworkType() {
    workManagerScheduler.schedulePeriodicWorker(
      MockOppiaWorker1.WORKER_NAME,
      MockOppiaWorker1.TaskType.WORKER1_TASK1,
      repeatInterval = 15,
      intervalUnit = TimeUnit.MINUTES
    )

    // The scheduler assumes network connectivity is required by default.
    val id = findUniqueId(MockOppiaWorker1.WORKER_NAME, MockOppiaWorker1.TaskType.WORKER1_TASK1)
    val workInfo = lookUpWorkInfo(id)
    val constraints = workInfo?.constraints
    assertThat(constraints?.requiredNetworkType).isEqualTo(NetworkType.CONNECTED)
  }

  @Test
  fun testSchedulePeriodicWorker_validWorker_doNotRequireConnectivity_hasNotRequiredNetworkType() {
    workManagerScheduler.schedulePeriodicWorker(
      MockOppiaWorker1.WORKER_NAME,
      MockOppiaWorker1.TaskType.WORKER1_TASK1,
      repeatInterval = 15,
      intervalUnit = TimeUnit.MINUTES,
      requireNetworkConnectivity = false
    )

    val id = findUniqueId(MockOppiaWorker1.WORKER_NAME, MockOppiaWorker1.TaskType.WORKER1_TASK1)
    val workInfo = lookUpWorkInfo(id)
    val constraints = workInfo?.constraints
    assertThat(constraints?.requiredNetworkType).isEqualTo(NetworkType.NOT_REQUIRED)
  }

  @Test
  fun testSchedulePeriodicWorker_validWorker_requireConnectivity_hasConnectedNetworkType() {
    workManagerScheduler.schedulePeriodicWorker(
      MockOppiaWorker1.WORKER_NAME,
      MockOppiaWorker1.TaskType.WORKER1_TASK1,
      repeatInterval = 15,
      intervalUnit = TimeUnit.MINUTES,
      requireNetworkConnectivity = true
    )

    val id = findUniqueId(MockOppiaWorker1.WORKER_NAME, MockOppiaWorker1.TaskType.WORKER1_TASK1)
    val workInfo = lookUpWorkInfo(id)
    val constraints = workInfo?.constraints
    assertThat(constraints?.requiredNetworkType).isEqualTo(NetworkType.CONNECTED)
  }

  @Test
  fun testSchedulePeriodicWorker_scheduledTwice_diffConstraints_keepsIdButChangesConstraints() {
    workManagerScheduler.schedulePeriodicWorker(
      MockOppiaWorker1.WORKER_NAME,
      MockOppiaWorker1.TaskType.WORKER1_TASK1,
      repeatInterval = 15,
      intervalUnit = TimeUnit.MINUTES,
      requireNetworkConnectivity = false
    )
    val id1 = findUniqueId(MockOppiaWorker1.WORKER_NAME, MockOppiaWorker1.TaskType.WORKER1_TASK1)
    workManagerScheduler.schedulePeriodicWorker(
      MockOppiaWorker1.WORKER_NAME,
      MockOppiaWorker1.TaskType.WORKER1_TASK1,
      repeatInterval = 15,
      intervalUnit = TimeUnit.MINUTES,
      requireNetworkConnectivity = true
    )
    val id2 = findUniqueId(MockOppiaWorker1.WORKER_NAME, MockOppiaWorker1.TaskType.WORKER1_TASK1)

    // Because the scheduler uses ExistingPeriodicWorkPolicy.UPDATE the existing worker should not
    // be canceled, its UUID should remain unchanged, and its constraints should be updated.
    val workInfo = lookUpWorkInfo(id1)
    val constraints = workInfo?.constraints
    assertThat(id1).isEqualTo(id2)
    assertThat(constraints?.requiredNetworkType).isEqualTo(NetworkType.CONNECTED)
  }

  @Test
  fun testSchedulePeriodicWorker_validWorker_withConstraintsUnmet_doesNotRun() {
    val id = schedulePeriodicWorker(
      MockOppiaWorker1.WORKER_NAME,
      MockOppiaWorker1.TaskType.WORKER1_TASK1,
      repeatInterval = 15,
      intervalUnit = TimeUnit.MINUTES
    )

    testCoroutineDispatchers.runCurrent()

    val workInfo = lookUpWorkInfo(id)
    assertThat(workerResults).isEmpty()
    assertThat(workInfo?.state).isEqualTo(WorkInfo.State.ENQUEUED)
  }

  @Test
  fun testSchedulePeriodicWorker_validWorker_withConstraintsMet_isRunAndReEnqueued() {
    val id = schedulePeriodicWorker(
      MockOppiaWorker1.WORKER_NAME,
      MockOppiaWorker1.TaskType.WORKER1_TASK1,
      repeatInterval = 15,
      intervalUnit = TimeUnit.MINUTES
    )

    forceConstraintsMet(id)
    testCoroutineDispatchers.runCurrent()

    val workInfo = lookUpWorkInfo(id)
    assertThat(workerResults).hasSize(1)
    assertThat(workerResults.single()).isEqualTo("Ran MockOppiaWorker1 for task: worker1_task1")
    assertThat(workInfo?.state).isEqualTo(WorkInfo.State.ENQUEUED)
  }

  @Test
  fun testSchedulePeriodicWorker_validWorker_withConstraintsMetThenUnmet_afterDelay_runsOnce() {
    val id = schedulePeriodicWorker(
      MockOppiaWorker1.WORKER_NAME,
      MockOppiaWorker1.TaskType.WORKER1_TASK1,
      repeatInterval = 15,
      intervalUnit = TimeUnit.MINUTES
    )

    forceConstraintsMet(id)
    testCoroutineDispatchers.runCurrent()
    testCoroutineDispatchers.advanceTimeBy(TimeUnit.MINUTES.toMillis(20))

    // If the constraints aren't met for the second run then the worker only runs once.
    val workInfo = lookUpWorkInfo(id)
    assertThat(workerResults).hasSize(1)
    assertThat(workerResults.single()).isEqualTo("Ran MockOppiaWorker1 for task: worker1_task1")
    assertThat(workInfo?.state).isEqualTo(WorkInfo.State.ENQUEUED)
  }

  @Test
  fun testSchedulePeriodicWorker_validWorker_withConstraintsUnmetThenMet_afterDelay_runsOnce() {
    val id = schedulePeriodicWorker(
      MockOppiaWorker1.WORKER_NAME,
      MockOppiaWorker1.TaskType.WORKER1_TASK1,
      repeatInterval = 15,
      intervalUnit = TimeUnit.MINUTES
    )

    testCoroutineDispatchers.advanceTimeBy(TimeUnit.MINUTES.toMillis(20))
    forceConstraintsMet(id)
    testCoroutineDispatchers.runCurrent()

    // If the constraints are only met after a certain delay, the worker will run once rather than
    // multiple times to "catch up."
    val workInfo = lookUpWorkInfo(id)
    assertThat(workerResults).hasSize(1)
    assertThat(workerResults.single()).isEqualTo("Ran MockOppiaWorker1 for task: worker1_task1")
    assertThat(workInfo?.state).isEqualTo(WorkInfo.State.ENQUEUED)
  }

  @Test
  fun testSchedulePeriodicWorker_validWorker_withConstraintsMet_afterDelay_runsTwice() {
    val id = schedulePeriodicWorker(
      MockOppiaWorker1.WORKER_NAME,
      MockOppiaWorker1.TaskType.WORKER1_TASK1,
      repeatInterval = 15,
      intervalUnit = TimeUnit.MINUTES
    )

    forceConstraintsMet(id)
    testCoroutineDispatchers.runCurrent()
    testCoroutineDispatchers.advanceTimeBy(TimeUnit.MINUTES.toMillis(20))
    forceConstraintsMet(id)
    testCoroutineDispatchers.runCurrent()

    // If time elapses sufficiently for the job to re-run and its constraints are satisfied at that
    // time, then it should run again.
    val workInfo = lookUpWorkInfo(id)
    assertThat(workerResults).containsExactly(
      "Ran MockOppiaWorker1 for task: worker1_task1",
      "Ran MockOppiaWorker1 for task: worker1_task1"
    ).inOrder()
    assertThat(workInfo?.state).isEqualTo(WorkInfo.State.ENQUEUED)
  }

  @Test
  fun testSchedulePeriodicWorker_multipleWorkers_withDifferentTimes_constraintsMet_runsEach() {
    val id1 = schedulePeriodicWorker(
      MockOppiaWorker1.WORKER_NAME,
      MockOppiaWorker1.TaskType.WORKER1_TASK1,
      repeatInterval = 15,
      intervalUnit = TimeUnit.MINUTES
    )
    val id2 = schedulePeriodicWorker(
      MockOppiaWorker2.WORKER_NAME,
      MockOppiaWorker2.TaskType.WORKER2_TASK1,
      repeatInterval = 20,
      intervalUnit = TimeUnit.MINUTES
    )

    // Simulate the constraints being continually met for both jobs, but note that the two 18 minute
    // steps will have different behaviors between the two. The 15 minute job will get one run at
    // each 18 minute step, but the 20 minute job will only run one more time.
    forceConstraintsMet(id1)
    forceConstraintsMet(id2)
    testCoroutineDispatchers.runCurrent()
    testCoroutineDispatchers.advanceTimeBy(TimeUnit.MINUTES.toMillis(18))
    forceConstraintsMet(id1)
    forceConstraintsMet(id2)
    testCoroutineDispatchers.runCurrent()
    testCoroutineDispatchers.advanceTimeBy(TimeUnit.MINUTES.toMillis(18))
    forceConstraintsMet(id1)
    forceConstraintsMet(id2)
    testCoroutineDispatchers.runCurrent()

    // If time elapses sufficiently for the job to re-run and its constraints are satisfied at that
    // time, then it should run again.
    val workInfo1 = lookUpWorkInfo(id1)
    val workInfo2 = lookUpWorkInfo(id2)
    // Note that order isn't checked here because exact execution order isn't guaranteed by
    // WorkManager when two workers are scheduled at the same time.
    assertThat(workerResults).containsExactly(
      "Ran MockOppiaWorker1 for task: worker1_task1",
      "Ran MockOppiaWorker2 for task: worker2_task1",
      "Ran MockOppiaWorker1 for task: worker1_task1",
      "Ran MockOppiaWorker1 for task: worker1_task1",
      "Ran MockOppiaWorker2 for task: worker2_task1"
    )
    assertThat(workInfo1?.state).isEqualTo(WorkInfo.State.ENQUEUED)
    assertThat(workInfo2?.state).isEqualTo(WorkInfo.State.ENQUEUED)
  }

  // Must be called to ensure a worker is run when it has constraints, but won't override the
  // time-based scheduling constraint (the clock still needs to be advanced).
  private fun forceConstraintsMet(id: UUID) {
    // There's no way to configure WorkManager to set specific constraints or to follow both
    // constraints and fixed time.
    testDriver.setAllConstraintsMet(id)
  }

  private fun schedulePeriodicWorker(
    workerName: String,
    taskType: OppiaWorker.TaskType,
    repeatInterval: Long,
    intervalUnit: TimeUnit
  ): UUID {
    workManagerScheduler.schedulePeriodicWorker(
      workerName, taskType, repeatInterval, intervalUnit
    )
    return findUniqueId(workerName, taskType)
  }

  private fun findUniqueId(workerName: String, taskType: OppiaWorker.TaskType): UUID {
    val workName = "${workerName}.${taskType.persistentName}"
    return runInBackground {
      workManager.getWorkInfosForUniqueWork(workName).asDeferred().await().single().id
    }
  }

  private fun lookUpWorkInfo(id: UUID): WorkInfo? =
    runInBackground { workManager.getWorkInfoById(id).asDeferred().await() }

  private fun lookUpWorkSpec(id: UUID): WorkSpec? {
    // This is a very hacky solution since it relies on WorkManagerImpl, but there's no other way to
    // access some of the worker's properties (such as its scheduled period) without this. That
    // could be observed behaviorally by leveraging clock management, but WorkManager already makes
    // that challenging. See ... TODO: Link to new issue here to fix timing. Seems simply to just make this observational rather than using the database hack.
    return runInBackground {
      (workManager as WorkManagerImpl).workDatabase.workSpecDao().getWorkSpec(id.toString())
    }
  }

  private fun <T> runInBackground(func: suspend () -> T): T {
    val resultDeferred = CoroutineScope(backgroundDispatcher).async { func() }
    testCoroutineDispatchers.runCurrent()
    assertThat(resultDeferred.isCompleted).isTrue()
    return resultDeferred.getCompleted()
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  companion object {
    val workerResults = CopyOnWriteArrayList<String>()
  }

  @Module
  interface TestModule {
    @Binds fun bindApplicationContext(application: Application): Context
  }

  @Module
  interface TestWorkManagerModule {
    @Binds
    @IntoMap
    @StringKey(MockOppiaWorker1.WORKER_NAME)
    fun provideMockOppiaWorker1Factory(
      factory: MockOppiaWorker1.Factory
    ): OppiaWorker.Factory<*>

    @Binds
    @IntoMap
    @StringKey(MockOppiaWorker2.WORKER_NAME)
    fun provideMockOppiaWorker2Factory(
      factory: MockOppiaWorker2.Factory
    ): OppiaWorker.Factory<*>
  }

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(
    modules = [
      TestModule::class, RobolectricModule::class, TestDispatcherModule::class,
      FakeOppiaClockModule::class, TestWorkManagerModule::class,
      LocaleProdModule::class,
      LoggerModule::class,
      TestPlatformParameterModule::class,
      AssetModule::class,
      DebugLogReportingModule::class
    ]
  )
  interface TestApplicationComponent : DispatcherInjector, PlatformParameterControllerInjector {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder
      fun build(): TestApplicationComponent
    }

    fun inject(test: WorkManagerSchedulerTest)
  }

  class TestApplication : Application(), DispatcherInjectorProvider, PlatformParameterControllerInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerWorkManagerSchedulerTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(test: WorkManagerSchedulerTest) {
      component.inject(test)
    }

    override fun getDispatcherInjector() = component
    override fun getPlatformParameterControllerInjector() = component
  }

  class MockOppiaWorker1 private constructor() : OppiaWorker<MockOppiaWorker1.TaskType> {
    enum class TaskType(override val persistentName: String) : OppiaWorker.TaskType {
      WORKER1_TASK1("worker1_task1"),
      WORKER1_TASK2("worker1_task2"),
      WORKER1_TASK3("worker1_task3")
    }

    override suspend fun doWork(taskType: TaskType): OppiaWorker.Result {
      workerResults += "Ran MockOppiaWorker1 for task: ${taskType.persistentName}"
      return OppiaWorker.Result.SUCCESS
    }

    class Factory @Inject constructor() : OppiaWorker.Factory<TaskType> {
      override val supportedTaskTypes: List<TaskType> = TaskType.values().toList()

      override fun createWorker(): OppiaWorker<TaskType> {
        return MockOppiaWorker1()
      }
    }

    companion object {
      const val WORKER_NAME = "MockOppiaWorker1"
    }
  }

  class MockOppiaWorker2 private constructor() : OppiaWorker<MockOppiaWorker2.TaskType> {
    enum class TaskType(override val persistentName: String) : OppiaWorker.TaskType {
      WORKER2_TASK1("worker2_task1")
    }

    override suspend fun doWork(taskType: TaskType): OppiaWorker.Result {
      workerResults += "Ran MockOppiaWorker2 for task: ${taskType.persistentName}"
      return OppiaWorker.Result.SUCCESS
    }

    class Factory @Inject constructor() : OppiaWorker.Factory<TaskType> {
      override val supportedTaskTypes: List<TaskType> = TaskType.values().toList()

      override fun createWorker(): OppiaWorker<TaskType> {
        return MockOppiaWorker2()
      }
    }

    companion object {
      const val WORKER_NAME = "MockOppiaWorker2"
    }
  }
}
