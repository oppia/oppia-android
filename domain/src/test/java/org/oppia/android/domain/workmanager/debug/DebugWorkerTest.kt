package org.oppia.android.domain.workmanager.debug

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.Configuration
import androidx.work.Data
import androidx.work.ListenableWorker
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.testing.WorkManagerTestInitHelper
import com.google.common.truth.Truth.assertThat
import com.google.firebase.FirebaseApp
import dagger.Binds
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import java.util.UUID
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.guava.asDeferred
import org.oppia.android.domain.platformparameter.PlatformParameterControllerInjector
import org.oppia.android.domain.platformparameter.PlatformParameterControllerInjectorProvider
import org.oppia.android.domain.workmanager.BootstrapOppiaWorker
import org.oppia.android.domain.workmanager.BootstrapOppiaWorker.Companion.DELEGATED_WORKER_NAME_INPUT_KEY
import org.oppia.android.domain.workmanager.BootstrapOppiaWorker.Companion.constructTaskTypeKey
import org.oppia.android.domain.workmanager.WorkManagerConfigurationModule
import org.oppia.android.domain.workmanager.testing.OppiaWorkManagerTestDriver
import org.oppia.android.domain.workmanager.testing.OppiaWorkManagerTestInitializer
import org.oppia.android.testing.platformparameter.TestPlatformParameterModule
import org.oppia.android.util.caching.AssetModule
import org.oppia.android.util.locale.LocaleProdModule
import org.oppia.android.util.logging.LoggerModule
import org.oppia.android.util.logging.firebase.DebugLogReportingModule
import org.oppia.android.util.threading.BackgroundDispatcher
import org.oppia.android.util.threading.DispatcherInjector
import org.oppia.android.util.threading.DispatcherInjectorProvider
import org.robolectric.shadows.ShadowLog

/** Tests for [DebugWorker]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = DebugWorkerTest.TestApplication::class)
@OptIn(ExperimentalCoroutinesApi::class)
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
class DebugWorkerTest {
  @Inject lateinit var context: Context
  @Inject lateinit var testCoroutineDispatchers: TestCoroutineDispatchers
  @Inject lateinit var configuration: Configuration
  @Inject lateinit var oppiaWorkManagerTestInitializer: OppiaWorkManagerTestInitializer
  @Inject lateinit var testDriver: OppiaWorkManagerTestDriver
  @field:[Inject BackgroundDispatcher] lateinit var backgroundDispatcher: CoroutineDispatcher

  private val workManager: WorkManager get() = oppiaWorkManagerTestInitializer.workManager

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
    FirebaseApp.initializeApp(context)
    oppiaWorkManagerTestInitializer.initializeWorkManager(configuration)

    // WorkManager and workers output most their issues issues to logcat, so this ensures those get
    // printed to the test log. This leads to a noisier test run but it makes debugging failures
    // significantly easier.
    ShadowLog.stream = System.out
  }

  @Test
  fun testWorker_runEveryFifteenMinsWithConnectivity_oneOff_printsLogAndSucceeds() {
    val workInfo = runOneOffWork(DebugWorker.Operation.RUN_EVERY_FIFTEEN_MINUTES_WITH_CONNECTIVITY)

    val debugLine = fetchSingleDebugWorkerDebugLog()
    assertThat(workInfo.state).isEqualTo(WorkInfo.State.SUCCEEDED)
    assertThat(debugLine)
      .isEqualTo("Debug worker ran with config: RUN_EVERY_FIFTEEN_MINUTES_WITH_CONNECTIVITY.")
  }

  @Test
  fun testWorker_runEveryTwentyMinsMinsWithOrWithoutConnectivity_oneOff_printsLogAndSucceeds() {
    val workInfo =
      runOneOffWork(DebugWorker.Operation.RUN_EVERY_TWENTY_MINUTES_WITH_OR_WITHOUT_CONNECTIVITY)

    val debugLine = fetchSingleDebugWorkerDebugLog()
    assertThat(workInfo.state).isEqualTo(WorkInfo.State.SUCCEEDED)
    assertThat(debugLine)
      .isEqualTo("Debug worker ran with config:" +
        " RUN_EVERY_TWENTY_MINUTES_WITH_OR_WITHOUT_CONNECTIVITY.")
  }

  @Test
  fun testWorker_runEverySixHoursWithOrWithoutConnectivity_oneOff_printsLogAndSucceeds() {
    val workInfo =
      runOneOffWork(DebugWorker.Operation.RUN_EVERY_SIX_HOURS_WITH_OR_WITHOUT_CONNECTIVITY)

    val debugLine = fetchSingleDebugWorkerDebugLog()
    assertThat(workInfo.state).isEqualTo(WorkInfo.State.SUCCEEDED)
    assertThat(debugLine)
      .isEqualTo("Debug worker ran with config: RUN_EVERY_SIX_HOURS_WITH_OR_WITHOUT_CONNECTIVITY.")
  }

  private fun runOneOffWork(operation: DebugWorker.Operation): WorkInfo {
    return enqueueOneOffWork<BootstrapOppiaWorker>(
      Data.Builder()
        .putString(DELEGATED_WORKER_NAME_INPUT_KEY, DebugWorker.WORKER_NAME)
        .putString(constructTaskTypeKey(DebugWorker.WORKER_NAME), operation.persistentName)
        .build()
    )
  }

  private inline fun <reified T: ListenableWorker> enqueueOneOffWork(
    inputData: Data = Data.Builder().build()
  ): WorkInfo {
    val id = runInBackground {
      OneTimeWorkRequest.Builder(T::class.java)
        .setInputData(inputData)
        .build()
        .also(workManager::enqueue)
        .id
    }
    return checkNotNull(testDriver.lookUpWorkInfo(id)) { "Expected one-off job to run." }
  }

  private fun <T> runInBackground(func: suspend () -> T): T {
    val resultDeferred = CoroutineScope(backgroundDispatcher).async { func() }
    testCoroutineDispatchers.runCurrent()
    assertThat(resultDeferred.isCompleted).isTrue()
    return resultDeferred.getCompleted()
  }

  private fun fetchDebugWorkerDebugLogs(): List<String> {
    // Extract all logs from the bootstrap worker and validate they are each errors before returning
    // the logged message lines.
    return ShadowLog.getLogs().filter { it.tag == DebugWorker.WORKER_NAME }.map {
      assertThat(it.type).isEqualTo(Log.DEBUG)
      return@map it.msg
    }
  }

  private fun fetchSingleDebugWorkerDebugLog(): String {
    val debugLogs = fetchDebugWorkerDebugLogs()
    assertThat(debugLogs).hasSize(1)
    return debugLogs.single()
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  @Module
  interface TestModule {
    @Binds fun bindApplicationContext(application: Application): Context
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

    fun inject(test: DebugWorkerTest)
  }

  class TestApplication : Application(), DispatcherInjectorProvider, PlatformParameterControllerInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerDebugWorkerTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(test: DebugWorkerTest) {
      component.inject(test)
    }

    override fun getDispatcherInjector() = component
    override fun getPlatformParameterControllerInjector() = component
  }
}
