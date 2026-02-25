package org.oppia.android.domain.workmanager

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.Configuration
import androidx.work.Data
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import androidx.work.impl.utils.SerialExecutor
import androidx.work.impl.utils.taskexecutor.TaskExecutor
import com.google.common.truth.Truth.assertThat
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors.directExecutor
import com.google.firebase.FirebaseApp
import dagger.Binds
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import java.util.UUID
import java.util.concurrent.Executor
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton
import org.oppia.android.domain.oppialogger.ApplicationStartupListener
import org.oppia.android.domain.platformparameter.PlatformParameterControllerInjector
import org.oppia.android.domain.platformparameter.PlatformParameterControllerInjectorProvider
import org.oppia.android.testing.platformparameter.TestPlatformParameterModule
import org.oppia.android.util.caching.AssetModule
import org.oppia.android.util.locale.LocaleProdModule
import org.oppia.android.util.logging.LoggerModule
import org.oppia.android.util.logging.firebase.DebugLogReportingModule
import org.oppia.android.util.threading.DispatcherInjector
import org.oppia.android.util.threading.DispatcherInjectorProvider

/** Tests for [WorkManagerConfigurationModule]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = WorkManagerConfigurationModuleTest.TestApplication::class)
@Suppress("FunctionName") // FunctionName: test names are conventionally named with underscores.
class WorkManagerConfigurationModuleTest {
  @Inject lateinit var context: Context
  @Inject lateinit var factories: Map<String, @JvmSuppressWildcards OppiaWorker.Factory<*>>
  @Inject lateinit var listeners: Set<@JvmSuppressWildcards StartupWorkerScheduleReadinessListener>
  @Inject lateinit var appStartupListeners: Set<@JvmSuppressWildcards ApplicationStartupListener>
  @Inject lateinit var configuration: Configuration

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
    FirebaseApp.initializeApp(context)
  }

  @Test
  fun testInjection_bindsOppiaWorkersMap() {
    // The module should be setting up a multibinding that then defaults to an empty map to ensure
    // injection. Note that the real test here is that test suite actually builds.
    assertThat(factories).isEmpty()
  }

  @Test
  fun testInjection_bindsStartupWorkerScheduleReadinessListenerSet() {
    // The module should be setting up a multibinding that then defaults to an empty set to ensure
    // injection. Note that the real test here is that test suite actually builds.
    assertThat(listeners).isEmpty()
  }

  @Test
  fun testInjection_bindsStartupWorkerScheduleReadinessMonitorAsApplicationStartupListener() {
    val monitors = appStartupListeners.filterIsInstance<StartupWorkerScheduleReadinessMonitor>()
    assertThat(appStartupListeners).isNotEmpty()
    assertThat(monitors).hasSize(1)
  }

  @Test
  fun testWorkManagerConfiguration_setsCustomWorkerFactory() {
    // Note that an implied test here is that the test suite actually builds.
    val workerFactoryClassPackage = configuration.workerFactory.javaClass.packageName
    assertThat(workerFactoryClassPackage).startsWith("org.oppia.android")
  }

  @Test
  fun testConfiguredWorkerFactory_createWorker_workerNotInClassPath_returnsBootstrapWorker() {
    val workerFactory = configuration.workerFactory

    val worker =
      workerFactory.createWorker(context, "org.oppia.android.FakeWorker", createWorkerParameters())

    // The custom factory defaults to BootstrapOppiaWorker to handle non-existent workers (which may
    // happen if the old versions of the workers are scheduled).
    assertThat(worker).isInstanceOf(BootstrapOppiaWorker::class.java)
  }

  @Test
  fun testConfiguredWorkerFactory_createWorker_workerNotListenableWorker_returnsBootstrapWorker() {
    val workerFactory = configuration.workerFactory

    val worker =
      workerFactory.createWorker(context, TestModule::class.java.name, createWorkerParameters())

    // The custom factory defaults to BootstrapOppiaWorker to handle invalid workers.
    assertThat(worker).isInstanceOf(BootstrapOppiaWorker::class.java)
  }

  @Test
  fun testConfiguredWorkerFactory_createWorker_workerIsNotBootstrapWorker_returnsNull() {
    val workerFactory = configuration.workerFactory

    val worker =
      workerFactory.createWorker(
        context, RealListenableWorker::class.java.name, createWorkerParameters()
      )

    // If a real worker is encountered (such as one of WorkManager's) then the bootstrap worker
    // should not be used.
    assertThat(worker).isNull()
  }

  @Test
  fun testConfiguredWorkerFactory_createWorker_workerIsBootstrapWorker_returnsBootstrapWorker() {
    val workerFactory = configuration.workerFactory

    val worker =
      workerFactory.createWorker(
        context, BootstrapOppiaWorker::class.java.name, createWorkerParameters()
      )

    // A valid request to create a bootstrap worker should return one of those workers.
    assertThat(worker).isInstanceOf(BootstrapOppiaWorker::class.java)
  }

  private fun createWorkerParameters(): WorkerParameters {
    val taskExecutor = object: TaskExecutor {
      override fun postToMainThread(runnable: Runnable?) = error("Not used.")
      override fun getMainThreadExecutor() = error("Not used.")
      override fun executeOnBackgroundThread(runnable: Runnable?) = error("Not used.")
      override fun getBackgroundExecutor() = error("Not used.")
    }
    return WorkerParameters(
      /* id= */ UUID.randomUUID(),
      /* inputData= */ Data.EMPTY,
      /* tags= */ emptyList(),
      /* runtimeExtras= */ WorkerParameters.RuntimeExtras(),
      /* runAttemptCount= */ 0,
      /* backgroundExecutor= */ directExecutor(),
      /* workTaskExecutor= */ taskExecutor,
      /* workerFactory= */ configuration.workerFactory,
      /* progressUpdater= */ { _, _, _ -> error("Not used.") },
      /* foregroundUpdater= */ { _, _, _ -> error("Not used.") }
    )
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

    fun inject(test: WorkManagerConfigurationModuleTest)
  }

  class TestApplication : Application(), DispatcherInjectorProvider, PlatformParameterControllerInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerWorkManagerConfigurationModuleTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(test: WorkManagerConfigurationModuleTest) {
      component.inject(test)
    }

    override fun getDispatcherInjector() = component
    override fun getPlatformParameterControllerInjector() = component
  }

  private class RealListenableWorker(
    appContext: Context, workerParams: WorkerParameters
  ) : ListenableWorker(appContext, workerParams) {
    override fun startWork(): ListenableFuture<Result> {
      error("Not implemented for test.")
    }
  }
}
