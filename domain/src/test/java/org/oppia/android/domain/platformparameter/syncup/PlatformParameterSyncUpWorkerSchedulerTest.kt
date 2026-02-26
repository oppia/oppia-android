package org.oppia.android.domain.platformparameter.syncup

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.NetworkType.CONNECTED
import androidx.work.WorkInfo
import androidx.work.impl.model.WorkSpec
import com.google.common.truth.Truth.assertThat
import dagger.Binds
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import java.util.UUID
import java.util.concurrent.TimeUnit
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.data.backends.gae.RetrofitModule
import org.oppia.android.data.backends.gae.RetrofitServiceModule
import org.oppia.android.data.backends.gae.testing.NetworkConfigTestModule
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.domain.oppialogger.LoggingIdentifierModule
import org.oppia.android.domain.oppialogger.analytics.ApplicationLifecycleModule
import org.oppia.android.domain.platformparameter.PlatformParameterSingletonModule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.platformparameter.TestPlatformParameterModule
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.caching.AssetModule
import org.oppia.android.util.locale.LocaleProdModule
import org.oppia.android.util.logging.SyncStatusModule
import org.oppia.android.util.networking.NetworkConnectionDebugUtilModule
import org.oppia.android.util.networking.NetworkConnectionUtilDebugModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import org.junit.After
import org.oppia.android.domain.platformparameter.PlatformParameterControllerInjector
import org.oppia.android.domain.platformparameter.PlatformParameterControllerInjectorProvider
import org.oppia.android.domain.platformparameter.syncup.PlatformParameterSyncUpWorker.Companion.WORKER_NAME
import org.oppia.android.domain.platformparameter.syncup.PlatformParameterSyncUpWorker.Operation.REFRESH_PLATFORM_PARAMETERS
import org.oppia.android.domain.workmanager.WorkManagerScheduler
import org.oppia.android.domain.workmanager.testing.OppiaWorkManagerTestDriver
import org.oppia.android.util.data.DataProvidersInjector
import org.oppia.android.util.data.DataProvidersInjectorProvider
import org.oppia.android.util.logging.LoggerModule
import org.oppia.android.util.threading.BackgroundDispatcher
import org.oppia.android.util.threading.DispatcherInjector
import org.oppia.android.util.threading.DispatcherInjectorProvider

/** Tests for [PlatformParameterSyncUpWorkerScheduler]. */
@Suppress("FunctionName") // FunctionName: test names are conventionally named with underscores.
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = PlatformParameterSyncUpWorkerSchedulerTest.TestApplication::class)
class PlatformParameterSyncUpWorkerSchedulerTest {
  @Inject lateinit var context: Context
  @Inject lateinit var testCoroutineDispatchers: TestCoroutineDispatchers
  @Inject lateinit var workManagerScheduler: WorkManagerScheduler
  @Inject lateinit var scheduler: PlatformParameterSyncUpWorkerScheduler
  @Inject lateinit var testDriver: OppiaWorkManagerTestDriver
  @field:[Inject BackgroundDispatcher] lateinit var backgroundDispatcher: CoroutineDispatcher

  @After
  fun tearDown() {
    TestPlatformParameterModule.reset()
  }

  // TODO(#5835): Finish the tests for this suite.

  @Test
  fun testPlatformParameterSyncUpWorker_hasOneOperationType() {
    setUpTestApplicationComponent()

    // A change detector test that, if failing, means that other tests in this suite need to be
    // updated to ensure that the new operation type is properly verified.
    val operations = PlatformParameterSyncUpWorker.Operation.values().toList()
    assertThat(operations).containsExactly(REFRESH_PLATFORM_PARAMETERS)
  }

  @Test
  fun testScheduleWork_schedulesPlatformParameterSyncUpWorkerForRefreshPlatformParameters() {
    TestPlatformParameterModule.forceSyncUpWorkerTimePeriodInHours(2)
    setUpTestApplicationComponent()
    initializeDependencies()

    scheduler.scheduleWork(workManagerScheduler)

    // Verify that the job was scheduled correctly and uses the high-frequency time.
    val id = testDriver.findUniqueId(WORKER_NAME, REFRESH_PLATFORM_PARAMETERS)
    val workInfo = testDriver.lookUpWorkInfo(id)
    assertThat(workInfo?.state).isEqualTo(WorkInfo.State.ENQUEUED)
    assertThat(lookUpWorkSpec(id)?.intervalDuration).isEqualTo(TimeUnit.HOURS.toMillis(2))
    assertThat(lookUpWorkSpec(id)?.constraints?.requiredNetworkType).isEqualTo(CONNECTED)
  }

  private fun lookUpWorkSpec(id: UUID): WorkSpec? = testDriver.lookUpWorkSpec(id)

  private fun initializeDependencies() {
    testDriver.initializeWorkManager()
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  // TODO(#89): Move this to a common test application component.
  @Module
  interface TestModule {
    @Binds
    fun bindContext(application: Application): Context
  }

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(
    modules = [
      ApplicationLifecycleModule::class,
      AssetModule::class,
      FakeOppiaClockModule::class,
      LocaleProdModule::class,
      LogStorageModule::class,
      LoggingIdentifierModule::class,
      NetworkConfigTestModule::class,
      NetworkConnectionDebugUtilModule::class,
      NetworkConnectionUtilDebugModule::class,
      TestPlatformParameterModule::class,
      PlatformParameterSingletonModule::class,
      RetrofitModule::class,
      RetrofitServiceModule::class,
      RobolectricModule::class,
      SyncStatusModule::class,
      TestDispatcherModule::class,
      TestLogReportingModule::class,
      TestModule::class,
      LoggerModule::class,
      PlatformParameterSyncUpWorkerModule::class
    ]
  )
  interface TestApplicationComponent : DataProvidersInjector, DispatcherInjector, PlatformParameterControllerInjector {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder
      fun build(): TestApplicationComponent
    }

    fun inject(test: PlatformParameterSyncUpWorkerSchedulerTest)
  }

  class TestApplication : Application(), DataProvidersInjectorProvider, DispatcherInjectorProvider, PlatformParameterControllerInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerPlatformParameterSyncUpWorkerSchedulerTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(test: PlatformParameterSyncUpWorkerSchedulerTest) {
      component.inject(test)
    }

    override fun getDataProvidersInjector() = component
    override fun getPlatformParameterControllerInjector() = component
    override fun getDispatcherInjector(): DispatcherInjector = component
  }
}
