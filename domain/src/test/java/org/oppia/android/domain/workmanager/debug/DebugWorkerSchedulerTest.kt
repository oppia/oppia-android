package org.oppia.android.domain.workmanager.debug

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.Configuration
import androidx.work.NetworkType.CONNECTED
import androidx.work.NetworkType.NOT_REQUIRED
import androidx.work.WorkInfo
import com.google.common.truth.Truth.assertThat
import com.google.firebase.FirebaseApp
import dagger.Binds
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import kotlinx.coroutines.CoroutineDispatcher
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.domain.platformparameter.PlatformParameterControllerInjector
import org.oppia.android.domain.platformparameter.PlatformParameterControllerInjectorProvider
import org.oppia.android.domain.workmanager.WorkManagerConfigurationModule
import org.oppia.android.domain.workmanager.WorkManagerScheduler
import org.oppia.android.domain.workmanager.debug.DebugWorker.Companion.WORKER_NAME
import org.oppia.android.domain.workmanager.debug.DebugWorker.Operation.RUN_EVERY_FIFTEEN_MINUTES_WITH_CONNECTIVITY
import org.oppia.android.domain.workmanager.debug.DebugWorker.Operation.RUN_EVERY_SIX_HOURS_WITH_OR_WITHOUT_CONNECTIVITY
import org.oppia.android.domain.workmanager.debug.DebugWorker.Operation.RUN_EVERY_TWENTY_MINUTES_WITH_OR_WITHOUT_CONNECTIVITY
import org.oppia.android.domain.workmanager.testing.OppiaWorkManagerTestDriver
import org.oppia.android.testing.platformparameter.TestPlatformParameterModule
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.caching.AssetModule
import org.oppia.android.util.locale.LocaleProdModule
import org.oppia.android.util.logging.LoggerModule
import org.oppia.android.util.logging.firebase.DebugLogReportingModule
import org.oppia.android.util.threading.BackgroundDispatcher
import org.oppia.android.util.threading.DispatcherInjector
import org.oppia.android.util.threading.DispatcherInjectorProvider
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import org.robolectric.shadows.ShadowLog
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [DebugWorkerScheduler]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = DebugWorkerSchedulerTest.TestApplication::class)
@Suppress("FunctionName") // FunctionName: test names are conventionally named with underscores.
class DebugWorkerSchedulerTest {
  @Inject
  lateinit var context: Context

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @Inject
  lateinit var configuration: Configuration

  @Inject
  lateinit var workManagerScheduler: WorkManagerScheduler

  @Inject
  lateinit var debugWorkerScheduler: DebugWorkerScheduler

  @Inject
  lateinit var testDriver: OppiaWorkManagerTestDriver

  @field:[Inject BackgroundDispatcher]
  lateinit var backgroundDispatcher: CoroutineDispatcher

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
    FirebaseApp.initializeApp(context)
    testDriver.initializeWorkManager(configuration)
  }

  @Test
  fun testDebugWorker_hasThreeOperationTypes() {
    // A change detector test that, if failing, means that other tests in this suite need to be
    // updated to ensure that the new operation type is properly verified.
    assertThat(DebugWorker.Operation.values().toList()).containsExactly(
      RUN_EVERY_FIFTEEN_MINUTES_WITH_CONNECTIVITY,
      RUN_EVERY_TWENTY_MINUTES_WITH_OR_WITHOUT_CONNECTIVITY,
      RUN_EVERY_SIX_HOURS_WITH_OR_WITHOUT_CONNECTIVITY
    )
  }

  @Test
  fun testScheduleWork_schedulesDebugWorkerToRunEveryFifteenMinsWithConnectivity() {
    debugWorkerScheduler.scheduleWork(workManagerScheduler)

    // Check what's been scheduled. One of the workers should be a DebugWorker that runs every 15
    // minutes but only if there's internet connectivity.
    val monitor =
      testDriver.lookUpPeriodicMonitor(WORKER_NAME, RUN_EVERY_FIFTEEN_MINUTES_WITH_CONNECTIVITY)
    assertThat(monitor.state).isEqualTo(WorkInfo.State.ENQUEUED)
    assertThat(monitor.intervalDurationMs).isEqualTo(TimeUnit.MINUTES.toMillis(15))
    assertThat(monitor.requiredNetworkType).isEqualTo(CONNECTED)
  }

  @Test
  fun testScheduleWork_schedulesDebugWorkerToRunEveryTwentyMinsWithOrWithoutConnectivity() {
    debugWorkerScheduler.scheduleWork(workManagerScheduler)

    // Check what's been scheduled. One of the workers should be a DebugWorker that runs every 15
    // minutes but only if there's internet connectivity.
    val monitor =
      testDriver.lookUpPeriodicMonitor(
        WORKER_NAME, RUN_EVERY_TWENTY_MINUTES_WITH_OR_WITHOUT_CONNECTIVITY
      )
    assertThat(monitor.state).isEqualTo(WorkInfo.State.ENQUEUED)
    assertThat(monitor.intervalDurationMs).isEqualTo(TimeUnit.MINUTES.toMillis(20))
    assertThat(monitor.requiredNetworkType).isEqualTo(NOT_REQUIRED)
  }

  @Test
  fun testScheduleWork_schedulesDebugWorkerToRunEverySixHoursWithOrWithoutConnectivity() {
    debugWorkerScheduler.scheduleWork(workManagerScheduler)

    // Check what's been scheduled. One of the workers should be a DebugWorker that runs every 15
    // minutes but only if there's internet connectivity.
    val monitor =
      testDriver.lookUpPeriodicMonitor(
        WORKER_NAME, RUN_EVERY_SIX_HOURS_WITH_OR_WITHOUT_CONNECTIVITY
      )
    assertThat(monitor.state).isEqualTo(WorkInfo.State.ENQUEUED)
    assertThat(monitor.intervalDurationMs).isEqualTo(TimeUnit.HOURS.toMillis(6))
    assertThat(monitor.requiredNetworkType).isEqualTo(NOT_REQUIRED)
  }

  @Test
  fun testScheduleWork_constraintsMet_runsThreeJobs() {
    debugWorkerScheduler.scheduleWork(workManagerScheduler)

    forceConstraintsMet(RUN_EVERY_FIFTEEN_MINUTES_WITH_CONNECTIVITY)
    forceConstraintsMet(RUN_EVERY_TWENTY_MINUTES_WITH_OR_WITHOUT_CONNECTIVITY)
    forceConstraintsMet(RUN_EVERY_SIX_HOURS_WITH_OR_WITHOUT_CONNECTIVITY)
    testCoroutineDispatchers.runCurrent()

    // Order cannot be easily checked here, so just verify that they ran.
    assertThat(fetchDebugWorkerDebugLogs()).containsExactly(
      "Debug worker ran with config: RUN_EVERY_FIFTEEN_MINUTES_WITH_CONNECTIVITY.",
      "Debug worker ran with config: RUN_EVERY_TWENTY_MINUTES_WITH_OR_WITHOUT_CONNECTIVITY.",
      "Debug worker ran with config: RUN_EVERY_SIX_HOURS_WITH_OR_WITHOUT_CONNECTIVITY."
    )
  }

  private fun forceConstraintsMet(taskType: DebugWorker.Operation) {
    testDriver.lookUpPeriodicMonitor(WORKER_NAME, taskType).forceConstraintsMet()
  }

  private fun fetchDebugWorkerDebugLogs(): List<String> {
    // Extract all logs from the bootstrap worker and validate they are each errors before returning
    // the logged message lines.
    return ShadowLog.getLogs().filter { it.tag == WORKER_NAME }.map {
      assertThat(it.type).isEqualTo(Log.DEBUG)
      return@map it.msg
    }
  }
  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  @Module
  interface TestModule {
    @Binds
    fun bindApplicationContext(application: Application): Context
  }

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(
    modules = [
      TestModule::class, RobolectricModule::class, TestDispatcherModule::class,
      FakeOppiaClockModule::class,
      DebugWorkerDebugModule::class,
      WorkManagerConfigurationModule::class,
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

    fun inject(test: DebugWorkerSchedulerTest)
  }

  class TestApplication :
    Application(), DispatcherInjectorProvider, PlatformParameterControllerInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerDebugWorkerSchedulerTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(test: DebugWorkerSchedulerTest) {
      component.inject(test)
    }

    override fun getDispatcherInjector() = component
    override fun getPlatformParameterControllerInjector() = component
  }
}
