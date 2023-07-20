/*
package org.oppia.android.domain.oppialogger.firestoreuploader

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.Configuration
import androidx.work.DelegatingWorkerFactory
import androidx.work.WorkManager
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.WorkManagerTestInitHelper
import com.google.common.truth.Truth
import dagger.Binds
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import javax.inject.Inject
import javax.inject.Singleton
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.domain.oppialogger.EventLogStorageCacheSize
import org.oppia.android.domain.oppialogger.ExceptionLogStorageCacheSize
import org.oppia.android.domain.oppialogger.FirestoreLogStorageCacheSize
import org.oppia.android.domain.oppialogger.LoggingIdentifierModule
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.oppialogger.PerformanceMetricsLogStorageCacheSize
import org.oppia.android.domain.oppialogger.analytics.ApplicationLifecycleModule
import org.oppia.android.domain.oppialogger.analytics.testing.FakeLogScheduler
import org.oppia.android.domain.oppialogger.loguploader.LogReportWorkerModule
import org.oppia.android.domain.platformparameter.PlatformParameterModule
import org.oppia.android.domain.platformparameter.PlatformParameterSingletonModule
import org.oppia.android.domain.testing.oppialogger.loguploader.FakeLogUploader
import org.oppia.android.testing.FakeFirestoreDataUploader
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.caching.AssetModule
import org.oppia.android.util.data.DataProviders
import org.oppia.android.util.data.DataProvidersInjector
import org.oppia.android.util.data.DataProvidersInjectorProvider
import org.oppia.android.util.locale.LocaleProdModule
import org.oppia.android.util.logging.LogUploader
import org.oppia.android.util.logging.LoggerModule
import org.oppia.android.util.logging.MetricLogScheduler
import org.oppia.android.util.logging.SyncStatusModule
import org.oppia.android.util.networking.NetworkConnectionDebugUtil
import org.oppia.android.util.networking.NetworkConnectionUtilDebugModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode

@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = FirestoreUploadWorkManagerInitializerTest.TestApplication::class)
class FirestoreUploadWorkManagerInitializerTest {
  @Inject
  lateinit var uploadWorkerFactory: FirestoreUploadWorkerFactory

  @Inject
  lateinit var workerInitializer: FirestoreUploadWorkManagerInitializer

  @Inject
  lateinit var networkConnectionUtil: NetworkConnectionDebugUtil

  @Inject
  lateinit var dataProviders: DataProviders

  @Inject
  lateinit var oppiaLogger: OppiaLogger

  @Inject
  lateinit var fakeLogUploader: FakeFirestoreDataUploader

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  private lateinit var context: Context

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
    context = InstrumentationRegistry.getInstrumentation().targetContext

    val delegatingWorkerFactory = DelegatingWorkerFactory()
    delegatingWorkerFactory.addFactory(uploadWorkerFactory)

    val config = Configuration.Builder()
      .setExecutor(SynchronousExecutor())
      .setWorkerFactory(delegatingWorkerFactory)
      .build()
    WorkManagerTestInitHelper.initializeTestWorkManager(context, config)
  }

  @Test
  fun testWorkRequest_onCreate_enqueuesRequest_verifyRequestId() {
    workerInitializer.onCreate(WorkManager.getInstance(context))
    testCoroutineDispatchers.runCurrent()

    val enqueuedEventWorkRequestId = workerInitializer.getWorkRequestForFirestoreId()


    Truth.assertThat(fakeLogUploader.getMostRecentEvent()).isEqualTo(enqueuedEventWorkRequestId)
    Truth.assertThat(fakeLogUploader.getMostRecentExceptionRequestId()).isEqualTo(
      enqueuedExceptionWorkRequestId
    )
    Truth.assertThat(fakeLogUploader.getMostRecentPerformanceMetricsRequestId()).isEqualTo(
      enqueuedPerformanceMetricsWorkRequestId
    )
    Truth.assertThat(fakeLogScheduler.getMostRecentStorageUsageMetricLoggingRequestId()).isEqualTo(
      enqueuedSchedulingStorageUsageMetricWorkRequestId
    )
    Truth.assertThat(fakeLogScheduler.getMostRecentPeriodicUiMetricLoggingRequestId()).isEqualTo(
      enqueuedSchedulingPeriodicUiMetricWorkRequestId
    )
    Truth.assertThat(fakeLogScheduler.getMostRecentPeriodicBackgroundMetricLoggingRequestId()).isEqualTo(
      enqueuedSchedulingPeriodicBackgroundPerformanceMetricWorkRequestId
    )
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  // TODO(#89): Move this to a common test application component.
  @Module
  class TestModule {
    @Provides
    @Singleton
    fun provideContext(application: Application): Context {
      return application
    }
  }

  @Module
  class TestLogStorageModule {

    @Provides
    @EventLogStorageCacheSize
    fun provideEventLogStorageCacheSize(): Int = 2

    @Provides
    @ExceptionLogStorageCacheSize
    fun provideExceptionLogStorageSize(): Int = 2

    @Provides
    @PerformanceMetricsLogStorageCacheSize
    fun providePerformanceMetricsLogStorageCacheSize(): Int = 2

    @Provides
    @FirestoreLogStorageCacheSize
    fun provideFirestoreLogStorageCacheSize(): Int = 2
  }

  @Module
  interface TestFirebaseLogUploaderModule {

    @Binds
    fun bindsFakeLogUploader(fakeLogUploader: FakeLogUploader): LogUploader

    @Binds
    fun bindsFakeLogScheduler(fakeLogScheduler: FakeLogScheduler): MetricLogScheduler
  }

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(
    modules = [
      TestModule::class, TestLogReportingModule::class, RobolectricModule::class,
      TestLogStorageModule::class, TestDispatcherModule::class,
      LogReportWorkerModule::class, TestFirebaseLogUploaderModule::class,
      FakeOppiaClockModule::class, NetworkConnectionUtilDebugModule::class, LocaleProdModule::class,
      LoggerModule::class, AssetModule::class, LoggerModule::class, PlatformParameterModule::class,
      PlatformParameterSingletonModule::class, LoggingIdentifierModule::class,
      SyncStatusModule::class, ApplicationLifecycleModule::class,
    ]
  )
  interface TestApplicationComponent : DataProvidersInjector {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder
      fun build(): TestApplicationComponent
    }

    fun inject(uploadWorkRequestTest: FirestoreUploadWorkManagerInitializerTest)
  }

  class TestApplication : Application(), DataProvidersInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerFirestoreUploadWorkManagerInitializerTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(test: FirestoreUploadWorkManagerInitializerTest) {
      component.inject(test)
    }

    override fun getDataProvidersInjector() = component
  }
}
*/
