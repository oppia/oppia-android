package org.oppia.android.domain.workmanager

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.NetworkType.CONNECTED
import androidx.work.NetworkType.NOT_REQUIRED
import androidx.work.WorkInfo
import com.google.common.truth.Truth.assertThat
import com.google.firebase.FirebaseApp
import dagger.Binds
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey
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
import org.oppia.android.domain.platformparameter.PlatformParameterControllerInjector
import org.oppia.android.domain.platformparameter.PlatformParameterControllerInjectorProvider
import org.oppia.android.domain.workmanager.WorkManagerSchedulerTest.MockOppiaWorker1.TaskType.WORKER1_TASK1
import org.oppia.android.domain.workmanager.testing.OppiaWorkManagerTestDriver
import org.oppia.android.testing.platformparameter.TestPlatformParameterModule
import org.oppia.android.util.caching.AssetModule
import org.oppia.android.util.locale.LocaleProdModule
import org.oppia.android.util.logging.LoggerModule
import org.oppia.android.util.logging.firebase.DebugLogReportingModule
import org.oppia.android.util.threading.BackgroundDispatcher
import org.oppia.android.util.threading.DispatcherInjector
import org.oppia.android.util.threading.DispatcherInjectorProvider

/** Tests for [WorkManagerScheduler]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = WorkManagerSchedulerTest.TestApplication::class)
@Suppress("FunctionName") // FunctionName: test names are conventionally named with underscores.
class WorkManagerSchedulerTest {
  @Inject lateinit var context: Context
  @Inject lateinit var testCoroutineDispatchers: TestCoroutineDispatchers
  @Inject lateinit var workManagerScheduler: WorkManagerScheduler
  @Inject lateinit var testDriver: OppiaWorkManagerTestDriver
  @field:[Inject BackgroundDispatcher] lateinit var backgroundDispatcher: CoroutineDispatcher

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
    FirebaseApp.initializeApp(context)
    testDriver.initializeWorkManager()

    // Reset static state between tests.
    workerResults.clear()
  }

  @Test
  fun testSchedulePeriodicWorker_invalidWorkerName_cancelsWorker() {
    val monitor = schedulePeriodicWorker(
      "invalid_worker_name",
      WORKER1_TASK1,
      repeatInterval = 15,
      intervalUnit = TimeUnit.MINUTES
    )

    monitor.forceConstraintsMet()

    assertThat(workerResults).isEmpty()
    assertThat(monitor.state).isEqualTo(WorkInfo.State.CANCELLED)
  }

  @Test
  fun testSchedulePeriodicWorker_invalidTaskType_doesNotRunAndCancelsWorker() {
    // Use a task type from a different worker since it won't be valid for this one.
    val monitor = schedulePeriodicWorker(
      MockOppiaWorker1.WORKER_NAME,
      MockOppiaWorker2.TaskType.WORKER2_TASK1,
      repeatInterval = 15,
      intervalUnit = TimeUnit.MINUTES
    )

    monitor.forceConstraintsMet()

    assertThat(workerResults).isEmpty()
    assertThat(monitor.state).isEqualTo(WorkInfo.State.CANCELLED)
  }

  @Test
  fun testSchedulePeriodicWorker_validWorker_hasWorkerNameInTag() {
    val monitor = schedulePeriodicWorker(
      MockOppiaWorker1.WORKER_NAME,
      WORKER1_TASK1,
      repeatInterval = 15,
      intervalUnit = TimeUnit.MINUTES
    )

    assertThat(monitor.tags).hasSize(2)
    assertThat(monitor.tags[0]).isEqualTo(BootstrapOppiaWorker::class.java.name)
    assertThat(monitor.tags[1]).isEqualTo("MockOppiaWorker1.worker1_task1")
  }

  @Test
  fun testSchedulePeriodicWorker_scheduledForOneSecondPeriod_isClampedToFifteenMinutes() {
    val monitor = schedulePeriodicWorker(
      MockOppiaWorker1.WORKER_NAME,
      WORKER1_TASK1,
      repeatInterval = 1,
      intervalUnit = TimeUnit.SECONDS
    )

    assertThat(monitor.intervalDurationMs).isEqualTo(TimeUnit.MINUTES.toMillis(15))
  }

  @Test
  fun testSchedulePeriodicWorker_scheduledForThreeHundredDays_isClampedToTwoWeeks() {
    val monitor = schedulePeriodicWorker(
      MockOppiaWorker1.WORKER_NAME,
      WORKER1_TASK1,
      repeatInterval = 300,
      intervalUnit = TimeUnit.DAYS
    )

    assertThat(monitor.intervalDurationMs).isEqualTo(TimeUnit.DAYS.toMillis(14))
  }

  @Test
  fun testSchedulePeriodicWorker_validWorker_hasNoLowBatteryConstraint() {
    val monitor = schedulePeriodicWorker(
      MockOppiaWorker1.WORKER_NAME,
      WORKER1_TASK1,
      repeatInterval = 15,
      intervalUnit = TimeUnit.MINUTES
    )

    assertThat(monitor.requiresBatteryNotLow).isTrue()
  }

  @Test
  fun testSchedulePeriodicWorker_validWorker_defaultConnectivityArg_hasConnectedNetworkType() {
    workManagerScheduler.schedulePeriodicWorker(
      MockOppiaWorker1.WORKER_NAME,
      WORKER1_TASK1,
      repeatInterval = 15,
      intervalUnit = TimeUnit.MINUTES
    )

    // The scheduler assumes network connectivity is required by default.
    val monitor = testDriver.lookUpPeriodicMonitor(MockOppiaWorker1.WORKER_NAME, WORKER1_TASK1)
    assertThat(monitor.requiredNetworkType).isEqualTo(CONNECTED)
  }

  @Test
  fun testSchedulePeriodicWorker_validWorker_doNotRequireConnectivity_hasNotRequiredNetworkType() {
    workManagerScheduler.schedulePeriodicWorker(
      MockOppiaWorker1.WORKER_NAME,
      WORKER1_TASK1,
      repeatInterval = 15,
      intervalUnit = TimeUnit.MINUTES,
      requireNetworkConnectivity = false
    )

    val monitor = testDriver.lookUpPeriodicMonitor(MockOppiaWorker1.WORKER_NAME, WORKER1_TASK1)
    assertThat(monitor.requiredNetworkType).isEqualTo(NOT_REQUIRED)
  }

  @Test
  fun testSchedulePeriodicWorker_validWorker_requireConnectivity_hasConnectedNetworkType() {
    workManagerScheduler.schedulePeriodicWorker(
      MockOppiaWorker1.WORKER_NAME,
      WORKER1_TASK1,
      repeatInterval = 15,
      intervalUnit = TimeUnit.MINUTES,
      requireNetworkConnectivity = true
    )

    val monitor = testDriver.lookUpPeriodicMonitor(MockOppiaWorker1.WORKER_NAME, WORKER1_TASK1)
    assertThat(monitor.requiredNetworkType).isEqualTo(CONNECTED)
  }

  @Test
  fun testSchedulePeriodicWorker_scheduledTwice_diffConstraints_keepsIdAndConstraints() {
    workManagerScheduler.schedulePeriodicWorker(
      MockOppiaWorker1.WORKER_NAME,
      WORKER1_TASK1,
      repeatInterval = 15,
      intervalUnit = TimeUnit.MINUTES,
      requireNetworkConnectivity = false
    )
    val monitor1 = testDriver.lookUpPeriodicMonitor(MockOppiaWorker1.WORKER_NAME, WORKER1_TASK1)
    workManagerScheduler.schedulePeriodicWorker(
      MockOppiaWorker1.WORKER_NAME,
      WORKER1_TASK1,
      repeatInterval = 15,
      intervalUnit = TimeUnit.MINUTES,
      requireNetworkConnectivity = true
    )
    val monitor2 = testDriver.lookUpPeriodicMonitor(MockOppiaWorker1.WORKER_NAME, WORKER1_TASK1)

    // TODO: Update this to check for CONNECTED instead once UPDATE is used.
    // Because the scheduler uses ExistingPeriodicWorkPolicy.KEEP the existing worker should not
    // be canceled, its UUID should remain unchanged, and its constraints should be the same.
    assertThat(monitor1.id).isEqualTo(monitor2.id)
    assertThat(monitor1.requiredNetworkType).isEqualTo(NOT_REQUIRED)
  }

  @Test
  fun testSchedulePeriodicWorker_validWorker_withConstraintsUnmet_doesNotRun() {
    val monitor = schedulePeriodicWorker(
      MockOppiaWorker1.WORKER_NAME,
      WORKER1_TASK1,
      repeatInterval = 15,
      intervalUnit = TimeUnit.MINUTES
    )

    testCoroutineDispatchers.runCurrent()

    assertThat(workerResults).isEmpty()
    assertThat(monitor.state).isEqualTo(WorkInfo.State.ENQUEUED)
  }

  @Test
  fun testSchedulePeriodicWorker_validWorker_withConstraintsMet_isRunAndReEnqueued() {
    val monitor = schedulePeriodicWorker(
      MockOppiaWorker1.WORKER_NAME,
      WORKER1_TASK1,
      repeatInterval = 15,
      intervalUnit = TimeUnit.MINUTES
    )

    monitor.forceConstraintsMet()

    assertThat(workerResults).hasSize(1)
    assertThat(workerResults.single()).isEqualTo("Ran MockOppiaWorker1 for task: worker1_task1")
    assertThat(monitor.state).isEqualTo(WorkInfo.State.ENQUEUED)
  }

  @Test
  fun testSchedulePeriodicWorker_validWorker_withConstraintsMetThenUnmet_afterDelay_runsOnce() {
    val monitor = schedulePeriodicWorker(
      MockOppiaWorker1.WORKER_NAME,
      WORKER1_TASK1,
      repeatInterval = 15,
      intervalUnit = TimeUnit.MINUTES
    )

    monitor.autoTrackConstraints = false
    monitor.forceConstraintsMet()
    testCoroutineDispatchers.advanceTimeBy(TimeUnit.MINUTES.toMillis(20))

    // If the constraints aren't met for the second run then the worker only runs once.
    assertThat(workerResults).hasSize(1)
    assertThat(workerResults.single()).isEqualTo("Ran MockOppiaWorker1 for task: worker1_task1")
    assertThat(monitor.state).isEqualTo(WorkInfo.State.ENQUEUED)
  }

  @Test
  fun testSchedulePeriodicWorker_validWorker_withConstraintsUnmetThenMet_afterDelay_runsOnce() {
    val monitor = schedulePeriodicWorker(
      MockOppiaWorker1.WORKER_NAME,
      WORKER1_TASK1,
      repeatInterval = 15,
      intervalUnit = TimeUnit.MINUTES
    )

    testCoroutineDispatchers.advanceTimeBy(TimeUnit.MINUTES.toMillis(20))
    monitor.forceConstraintsMet()

    // If the constraints are only met after a certain delay, the worker will run once rather than
    // multiple times to "catch up."
    assertThat(workerResults).hasSize(1)
    assertThat(workerResults.single()).isEqualTo("Ran MockOppiaWorker1 for task: worker1_task1")
    assertThat(monitor.state).isEqualTo(WorkInfo.State.ENQUEUED)
  }

  @Test
  fun testSchedulePeriodicWorker_validWorker_withConstraintsMet_afterDelay_runsTwice() {
    val monitor = schedulePeriodicWorker(
      MockOppiaWorker1.WORKER_NAME,
      WORKER1_TASK1,
      repeatInterval = 15,
      intervalUnit = TimeUnit.MINUTES
    )

    monitor.forceConstraintsMet()
    testCoroutineDispatchers.advanceTimeBy(TimeUnit.MINUTES.toMillis(20))

    // If time elapses sufficiently for the job to re-run and its constraints are satisfied at that
    // time, then it should run again.
    assertThat(workerResults).containsExactly(
      "Ran MockOppiaWorker1 for task: worker1_task1",
      "Ran MockOppiaWorker1 for task: worker1_task1"
    ).inOrder()
    assertThat(monitor.state).isEqualTo(WorkInfo.State.ENQUEUED)
  }

  @Test
  fun testSchedulePeriodicWorker_multipleWorkers_withDifferentTimes_constraintsMet_runsEach() {
    val monitor1 = schedulePeriodicWorker(
      MockOppiaWorker1.WORKER_NAME,
      WORKER1_TASK1,
      repeatInterval = 15,
      intervalUnit = TimeUnit.MINUTES
    )
    val monitor2 = schedulePeriodicWorker(
      MockOppiaWorker2.WORKER_NAME,
      MockOppiaWorker2.TaskType.WORKER2_TASK1,
      repeatInterval = 20,
      intervalUnit = TimeUnit.MINUTES
    )

    // Simulate the constraints being continually met for both jobs, but note that the two 18 minute
    // steps will have different behaviors between the two. The 15 minute job will get one run at
    // each 18 minute step, but the 20 minute job will only run one more time.
    monitor1.forceConstraintsMet()
    monitor2.forceConstraintsMet()
    testCoroutineDispatchers.advanceTimeBy(TimeUnit.MINUTES.toMillis(18))
    testCoroutineDispatchers.advanceTimeBy(TimeUnit.MINUTES.toMillis(18))

    // Note that order isn't checked here because exact execution order isn't guaranteed by
    // WorkManager when two workers are scheduled at the same time.
    assertThat(workerResults).containsExactly(
      "Ran MockOppiaWorker1 for task: worker1_task1",
      "Ran MockOppiaWorker2 for task: worker2_task1",
      "Ran MockOppiaWorker1 for task: worker1_task1",
      "Ran MockOppiaWorker1 for task: worker1_task1",
      "Ran MockOppiaWorker2 for task: worker2_task1"
    )
    // If time elapses sufficiently for the job to re-run and its constraints are satisfied at that
    // time, then it should run again.
    assertThat(monitor1.state).isEqualTo(WorkInfo.State.ENQUEUED)
    assertThat(monitor2.state).isEqualTo(WorkInfo.State.ENQUEUED)
  }

  private fun schedulePeriodicWorker(
    workerName: String,
    taskType: OppiaWorker.TaskType,
    repeatInterval: Long,
    intervalUnit: TimeUnit
  ): OppiaWorkManagerTestDriver.WorkerMonitor {
    workManagerScheduler.schedulePeriodicWorker(
      workerName, taskType, repeatInterval, intervalUnit
    )
    return testDriver.lookUpPeriodicMonitor(workerName, taskType)
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
