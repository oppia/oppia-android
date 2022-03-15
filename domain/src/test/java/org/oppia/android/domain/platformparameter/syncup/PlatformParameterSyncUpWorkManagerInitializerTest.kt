package org.oppia.android.domain.platformparameter.syncup

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.content.pm.ApplicationInfoBuilder
import androidx.test.core.content.pm.PackageInfoBuilder
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.WorkManager
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.WorkManagerTestInitHelper
import com.google.common.base.Optional
import com.google.common.truth.Truth.assertThat
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.data.backends.gae.BaseUrl
import org.oppia.android.data.backends.gae.JsonPrefixNetworkInterceptor
import org.oppia.android.data.backends.gae.NetworkApiKey
import org.oppia.android.data.backends.gae.NetworkConfigProdModule
import org.oppia.android.data.backends.gae.OppiaRetrofit
import org.oppia.android.data.backends.gae.RemoteAuthNetworkInterceptor
import org.oppia.android.data.backends.gae.api.PlatformParameterService
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.domain.oppialogger.LoggingIdentifierModule
import org.oppia.android.domain.platformparameter.PlatformParameterModule
import org.oppia.android.domain.platformparameter.PlatformParameterSingletonModule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.network.MockPlatformParameterService
import org.oppia.android.testing.network.RetrofitTestModule
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.locale.LocaleProdModule
import org.oppia.android.util.logging.EnableConsoleLog
import org.oppia.android.util.logging.EnableFileLog
import org.oppia.android.util.logging.GlobalLogLevel
import org.oppia.android.util.logging.LogLevel
import org.oppia.android.util.networking.NetworkConnectionDebugUtilModule
import org.oppia.android.util.networking.NetworkConnectionUtilDebugModule
import org.oppia.android.util.platformparameter.SYNC_UP_WORKER_TIME_PERIOD_IN_HOURS_DEFAULT_VALUE
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.mock.MockRetrofit
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import org.oppia.android.domain.oppialogger.LoggingIdentifierModule
import org.oppia.android.util.logging.SyncStatusModule
import org.oppia.android.util.logging.firebase.LogReportingModule

/** Tests for [PlatformParameterSyncUpWorkManagerInitializer]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(manifest = Config.NONE)
class PlatformParameterSyncUpWorkManagerInitializerTest {

  @Inject
  lateinit var syncUpWorkManagerInitializer: PlatformParameterSyncUpWorkManagerInitializer

  @Inject
  lateinit var platformParameterSyncUpWorkerFactory: PlatformParameterSyncUpWorkerFactory

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @Inject
  lateinit var context: Context

  private val testVersionName = "1.0"

  private val testVersionCode = 1

  @Before
  fun setup() {
    setUpTestApplicationComponent()
    setUpApplicationForContext()
    val config = Configuration.Builder()
      .setExecutor(SynchronousExecutor())
      .setWorkerFactory(platformParameterSyncUpWorkerFactory)
      .build()
    WorkManagerTestInitHelper.initializeTestWorkManager(context, config)
  }

  @Test
  fun testWorkRequest_onCreate_enqueuesRequest_verifyRequestId() {
    syncUpWorkManagerInitializer.onCreate()
    testCoroutineDispatchers.runCurrent()

    val enqueuedSyncUpWorkRequestId = syncUpWorkManagerInitializer.getSyncUpWorkRequestId()

    val workManager = WorkManager.getInstance(context)
    // Get all the WorkRequestInfo which have been tagged with "PlatformParameterSyncUpWorker.TAG"
    val workInfoList = workManager.getWorkInfosByTag(PlatformParameterSyncUpWorker.TAG).get()
    // There should be only one such work request having "PlatformParameterSyncUpWorker.TAG" tag
    assertThat(workInfoList.size).isEqualTo(1)
    // Match the ID of this work request with the ID of another work request which was enqueued by
    // PlatformParameterSyncUpWorkManagerInitializer
    assertThat(enqueuedSyncUpWorkRequestId).isEqualTo(workInfoList[0].id)
  }

  @Test
  fun testWorkRequest_verifyWorkerConstraints() {
    val workerConstraints = Constraints.Builder()
      .setRequiredNetworkType(NetworkType.CONNECTED)
      .setRequiresBatteryNotLow(true)
      .build()

    val syncUpWorkRequestConstraints = syncUpWorkManagerInitializer.getSyncUpWorkerConstraints()
    assertThat(syncUpWorkRequestConstraints).isEqualTo(workerConstraints)
  }

  @Test
  fun testWorkRequest_verifyWorkRequestData() {
    val workerTypeForSyncingUpParameters = Data.Builder().putString(
      PlatformParameterSyncUpWorker.WORKER_TYPE_KEY,
      PlatformParameterSyncUpWorker.PLATFORM_PARAMETER_WORKER
    ).build()

    val syncUpWorkRequestData = syncUpWorkManagerInitializer.getSyncUpWorkRequestData()

    assertThat(syncUpWorkRequestData).isEqualTo(workerTypeForSyncingUpParameters)
  }

  @Test
  fun testWorkRequest_verifyWorkRequestPeriodicity() {
    syncUpWorkManagerInitializer.onCreate()
    testCoroutineDispatchers.runCurrent()

    val syncUpWorkerTimePeriodInMs = syncUpWorkManagerInitializer.getSyncUpWorkerTimePeriod()
    val syncUpWorkerTimePeriodInHours = TimeUnit.MILLISECONDS.toHours(syncUpWorkerTimePeriodInMs)

    assertThat(syncUpWorkerTimePeriodInHours).isEqualTo(
      SYNC_UP_WORKER_TIME_PERIOD_IN_HOURS_DEFAULT_VALUE
    )
  }

  private fun setUpTestApplicationComponent() {
    DaggerPlatformParameterSyncUpWorkManagerInitializerTest_TestApplicationComponent.builder()
      .setApplication(ApplicationProvider.getApplicationContext())
      .build()
      .inject(this)
  }

  private fun setUpApplicationForContext() {
    val packageManager = Shadows.shadowOf(context.packageManager)
    val applicationInfo =
      ApplicationInfoBuilder.newBuilder()
        .setPackageName(context.packageName)
        .build()
    val packageInfo =
      PackageInfoBuilder.newBuilder()
        .setPackageName(context.packageName)
        .setApplicationInfo(applicationInfo)
        .build()
    packageInfo.versionName = testVersionName
    packageInfo.versionCode = testVersionCode
    packageManager.installPackage(packageInfo)
  }

  // TODO(#89): Move this to a common test application component.
  @Module
  class TestModule {
    @Provides
    @Singleton
    fun provideContext(application: Application): Context {
      return application
    }

    // TODO(#59): Either isolate these to their own shared test module, or use the real logging
    // module in tests to avoid needing to specify these settings for tests.
    @EnableConsoleLog
    @Provides
    fun provideEnableConsoleLog(): Boolean = true

    @EnableFileLog
    @Provides
    fun provideEnableFileLog(): Boolean = false

    @GlobalLogLevel
    @Provides
    fun provideGlobalLogLevel(): LogLevel = LogLevel.VERBOSE
  }

  @Module
  class TestNetworkModule {

    @OppiaRetrofit
    @Provides
    @Singleton
    fun provideRetrofitInstance(
      jsonPrefixNetworkInterceptor: JsonPrefixNetworkInterceptor,
      remoteAuthNetworkInterceptor: RemoteAuthNetworkInterceptor,
      @BaseUrl baseUrl: String
    ): Optional<Retrofit> {
      val client = OkHttpClient.Builder()
        .addInterceptor(jsonPrefixNetworkInterceptor)
        .addInterceptor(remoteAuthNetworkInterceptor)
        .build()

      return Optional.of(
        Retrofit.Builder()
          .baseUrl(baseUrl)
          .addConverterFactory(MoshiConverterFactory.create())
          .client(client)
          .build()
      )
    }

    @Provides
    @NetworkApiKey
    fun provideNetworkApiKey(): String = ""

    @Provides
    fun provideMockPlatformParameterService(
      mockRetrofit: MockRetrofit
    ): Optional<PlatformParameterService> {
      val delegate = mockRetrofit.create(PlatformParameterService::class.java)
      return Optional.of(MockPlatformParameterService(delegate))
    }
  }

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(
    modules = [
      LogStorageModule::class, RobolectricModule::class, TestDispatcherModule::class,
      TestModule::class, TestLogReportingModule::class, TestNetworkModule::class,
      RetrofitTestModule::class, FakeOppiaClockModule::class, PlatformParameterModule::class,
      NetworkConnectionUtilDebugModule::class, NetworkConnectionDebugUtilModule::class,
      NetworkConfigProdModule::class, PlatformParameterSingletonModule::class,
      LocaleProdModule::class, LoggingIdentifierModule::class, SyncStatusModule::class
    ]
  )
  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder
      fun build(): TestApplicationComponent
    }

    fun inject(platformParameterSyncUpWorkerTest: PlatformParameterSyncUpWorkManagerInitializerTest)
  }
}
