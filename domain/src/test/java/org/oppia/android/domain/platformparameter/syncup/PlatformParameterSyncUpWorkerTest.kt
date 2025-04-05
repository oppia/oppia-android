package org.oppia.android.domain.platformparameter.syncup

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.Configuration
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.WorkManagerTestInitHelper
import com.google.common.truth.Truth.assertThat
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import data.src.main.java.org.oppia.android.data.backends.gae.testing.PlatformParameterServiceTestOrchestrator
import data.src.main.java.org.oppia.android.data.backends.gae.testing.PlatformParameterServiceTestOrchestrator.Companion.REMOTE_PLATFORM_PARAMETERS_WITH_UNSUPPORTED_TYPE
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.app.model.PlatformParameter
import org.oppia.android.app.model.PlatformParameter.SyncStatus
import org.oppia.android.data.backends.gae.RetrofitModule
import org.oppia.android.data.backends.gae.RetrofitServiceModule
import org.oppia.android.data.backends.gae.testing.NetworkConfigTestModule
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.domain.oppialogger.LoggingIdentifierModule
import org.oppia.android.domain.oppialogger.analytics.ApplicationLifecycleModule
import org.oppia.android.domain.platformparameter.PlatformParameterController
import org.oppia.android.domain.platformparameter.PlatformParameterModule
import org.oppia.android.domain.platformparameter.PlatformParameterSingletonImpl
import org.oppia.android.testing.FakeExceptionLogger
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.data.DataProviderTestMonitor
import org.oppia.android.testing.platformparameter.TEST_BOOLEAN_PARAM_NAME
import org.oppia.android.testing.platformparameter.TEST_BOOLEAN_PARAM_SERVER_VALUE
import org.oppia.android.testing.platformparameter.TEST_INTEGER_PARAM_DEFAULT_VALUE
import org.oppia.android.testing.platformparameter.TEST_INTEGER_PARAM_NAME
import org.oppia.android.testing.platformparameter.TEST_INTEGER_PARAM_SERVER_VALUE
import org.oppia.android.testing.platformparameter.TEST_STRING_PARAM_NAME
import org.oppia.android.testing.platformparameter.TEST_STRING_PARAM_SERVER_VALUE
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.data.DataProvidersInjector
import org.oppia.android.util.data.DataProvidersInjectorProvider
import org.oppia.android.util.locale.LocaleProdModule
import org.oppia.android.util.logging.EnableConsoleLog
import org.oppia.android.util.logging.EnableFileLog
import org.oppia.android.util.logging.GlobalLogLevel
import org.oppia.android.util.logging.LogLevel
import org.oppia.android.util.logging.SyncStatusModule
import org.oppia.android.util.networking.NetworkConnectionDebugUtilModule
import org.oppia.android.util.networking.NetworkConnectionUtilDebugModule
import org.oppia.android.util.platformparameter.PlatformParameterSingleton
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [PlatformParameterSyncUpWorker]. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(
  application = PlatformParameterSyncUpWorkerTest.TestApplication::class,
  manifest = Config.NONE
)
class PlatformParameterSyncUpWorkerTest {
  @Inject lateinit var platformParameterSingleton: PlatformParameterSingleton
  @Inject lateinit var platformParameterController: PlatformParameterController
  @Inject lateinit var platformParameterSyncUpWorkerFactory: PlatformParameterSyncUpWorkerFactory
  @Inject lateinit var testCoroutineDispatchers: TestCoroutineDispatchers
  @Inject lateinit var context: Context
  @Inject lateinit var fakeExceptionLogger: FakeExceptionLogger
  @Inject lateinit var monitorFactory: DataProviderTestMonitor.Factory
  @Inject lateinit var serviceOrchestrator: PlatformParameterServiceTestOrchestrator

  private val expectedTestStringParameter = PlatformParameter.newBuilder()
    .setName(TEST_STRING_PARAM_NAME)
    .setString(TEST_STRING_PARAM_SERVER_VALUE)
    .setSyncStatus(SyncStatus.SYNCED_FROM_SERVER)
    .build()

  private val expectedTestIntegerParameter = PlatformParameter.newBuilder()
    .setName(TEST_INTEGER_PARAM_NAME)
    .setInteger(TEST_INTEGER_PARAM_SERVER_VALUE)
    .setSyncStatus(SyncStatus.SYNCED_FROM_SERVER)
    .build()

  private val defaultTestIntegerParameter = PlatformParameter.newBuilder()
    .setName(TEST_INTEGER_PARAM_NAME)
    .setInteger(TEST_INTEGER_PARAM_DEFAULT_VALUE)
    .setSyncStatus(SyncStatus.NOT_SYNCED_FROM_SERVER)
    .build()

  private val expectedTestBooleanParameter = PlatformParameter.newBuilder()
    .setName(TEST_BOOLEAN_PARAM_NAME)
    .setBoolean(TEST_BOOLEAN_PARAM_SERVER_VALUE)
    .setSyncStatus(SyncStatus.SYNCED_FROM_SERVER)
    .build()

  // Not including "expectedTestBooleanParameter" in this list to prove that a refresh took place
  private val mockPlatformParameterList = listOf<PlatformParameter>(
    expectedTestStringParameter,
    defaultTestIntegerParameter // using default value here just to prove refresh took place
  )

  @Before
  fun setup() {
    setUpTestApplicationComponent()
    val config = Configuration.Builder()
      .setExecutor(SynchronousExecutor())
      .setWorkerFactory(platformParameterSyncUpWorkerFactory)
      .build()
    WorkManagerTestInitHelper.initializeTestWorkManager(context, config)
  }

  @Test
  fun testSyncUpWorker_databaseIsEmpty_getCorrectPlatformParameters_verifyValuesAreCached() {
    serviceOrchestrator.setNextResponseAsSuccess()

    // Empty the Platform Parameter Database to simulate the execution of first SyncUp Work request.
    platformParameterController.updatePlatformParameterDatabase(listOf())

    val workManager = WorkManager.getInstance(context)

    val inputData = Data.Builder().putString(
      PlatformParameterSyncUpWorker.WORKER_TYPE_KEY,
      PlatformParameterSyncUpWorker.PLATFORM_PARAMETER_WORKER
    ).build()

    val request: OneTimeWorkRequest = OneTimeWorkRequestBuilder<PlatformParameterSyncUpWorker>()
      .setInputData(inputData)
      .build()

    // Enqueue the Work Request to fetch and cache the Platform Parameters from Remote Service.
    workManager.enqueue(request)
    testCoroutineDispatchers.runCurrent()

    val workInfo = workManager.getWorkInfoById(request.id)
    assertThat(workInfo.get().state).isEqualTo(WorkInfo.State.SUCCEEDED)

    // Retrieve the previously cached Platform Parameters from Cache Store.
    monitorFactory.ensureDataProviderExecutes(platformParameterController.getParameterDatabase())

    // Values retrieved from Cache store will be sent to Platform Parameter Singleton by the
    // Controller in the form of a Map, therefore verify the retrieved values from that Map.
    val platformParameterMap = platformParameterSingleton.getPlatformParameterMap()
    assertThat(platformParameterMap)
      .containsEntry(TEST_STRING_PARAM_NAME, expectedTestStringParameter)
  }

  @Test
  fun testSyncUpWorker_databaseIsEmpty_getWrongPlatformParameters_verifyWorkerCrashes() {
    serviceOrchestrator.setNextResponseAsSuccess(REMOTE_PLATFORM_PARAMETERS_WITH_UNSUPPORTED_TYPE)

    // Empty the Platform Parameter Database to simulate the execution of first SyncUp Work request.
    platformParameterController.updatePlatformParameterDatabase(listOf())

    val workManager = WorkManager.getInstance(context)

    val inputData = Data.Builder().putString(
      PlatformParameterSyncUpWorker.WORKER_TYPE_KEY,
      PlatformParameterSyncUpWorker.PLATFORM_PARAMETER_WORKER
    ).build()

    val request: OneTimeWorkRequest = OneTimeWorkRequestBuilder<PlatformParameterSyncUpWorker>()
      .setInputData(inputData)
      .build()

    // Enqueue the Work Request to fetch and cache the Platform Parameters from Remote Service.
    workManager.enqueue(request)
    testCoroutineDispatchers.runCurrent()

    val workInfo = workManager.getWorkInfoById(request.id)
    assertThat(workInfo.get().state).isEqualTo(WorkInfo.State.FAILED)

    val exceptionMessage = fakeExceptionLogger.getMostRecentException().message
    assertThat(exceptionMessage)
      .isEqualTo(PlatformParameterSyncUpWorker.INCORRECT_TYPE_EXCEPTION_MSG)
  }

  @Test
  fun testSyncUpWorker_databaseIsNotEmpty_getCorrectPlatformParameters_verifyValuesAreUpdated() {
    serviceOrchestrator.setNextResponseAsSuccess()

    // Fill the Platform Parameter Database with mock values to simulate the execution of a SyncUp
    // Work request that is not first.
    platformParameterController.updatePlatformParameterDatabase(mockPlatformParameterList)

    val workManager = WorkManager.getInstance(context)

    val inputData = Data.Builder().putString(
      PlatformParameterSyncUpWorker.WORKER_TYPE_KEY,
      PlatformParameterSyncUpWorker.PLATFORM_PARAMETER_WORKER
    ).build()

    val request: OneTimeWorkRequest = OneTimeWorkRequestBuilder<PlatformParameterSyncUpWorker>()
      .setInputData(inputData)
      .build()

    // Enqueue the Work Request to fetch and cache the Platform Parameters from Remote Service.
    workManager.enqueue(request)
    testCoroutineDispatchers.runCurrent()

    val workInfo = workManager.getWorkInfoById(request.id)
    assertThat(workInfo.get().state).isEqualTo(WorkInfo.State.SUCCEEDED)

    // Retrieve the previously cached Platform Parameters from Cache Store.
    monitorFactory.ensureDataProviderExecutes(platformParameterController.getParameterDatabase())

    // Values retrieved from Cache store will be sent to Platform Parameter Singleton by the
    // Controller in the form of a Map, therefore verify the retrieved values from that Map.
    val platformParameterMap = platformParameterSingleton.getPlatformParameterMap()
    assertThat(platformParameterMap).isNotEmpty()

    // New Boolean Platform Parameter is now present in the Database.
    assertThat(platformParameterMap)
      .containsEntry(TEST_BOOLEAN_PARAM_NAME, expectedTestBooleanParameter)

    // Previous String Platform Parameter is still same in the Database.
    assertThat(platformParameterMap)
      .containsEntry(TEST_STRING_PARAM_NAME, expectedTestStringParameter)

    // Previous Integer Platform Parameter updated to new value in the Database.
    assertThat(platformParameterMap)
      .containsEntry(TEST_INTEGER_PARAM_NAME, expectedTestIntegerParameter)
  }

  @Test
  fun testSyncUpWorker_databaseIsNotEmpty_getWrongPlatformParameters_verifyWorkerCrashes() {
    serviceOrchestrator.setNextResponseAsSuccess(REMOTE_PLATFORM_PARAMETERS_WITH_UNSUPPORTED_TYPE)

    // Fill the Platform Parameter Database with mock values to simulate the execution of a SyncUp
    // Work request that is not first.
    platformParameterController.updatePlatformParameterDatabase(mockPlatformParameterList)

    val workManager = WorkManager.getInstance(context)

    val inputData = Data.Builder().putString(
      PlatformParameterSyncUpWorker.WORKER_TYPE_KEY,
      PlatformParameterSyncUpWorker.PLATFORM_PARAMETER_WORKER
    ).build()

    val request: OneTimeWorkRequest = OneTimeWorkRequestBuilder<PlatformParameterSyncUpWorker>()
      .setInputData(inputData)
      .build()

    // Enqueue the Work Request to fetch and cache the Platform Parameters from Remote Service.
    workManager.enqueue(request)
    testCoroutineDispatchers.runCurrent()

    val workInfo = workManager.getWorkInfoById(request.id)
    assertThat(workInfo.get().state).isEqualTo(WorkInfo.State.FAILED)

    val exceptionMessage = fakeExceptionLogger.getMostRecentException().message
    assertThat(exceptionMessage)
      .isEqualTo(PlatformParameterSyncUpWorker.INCORRECT_TYPE_EXCEPTION_MSG)
  }

  @Test
  fun testSyncUpWorker_databaseIsNotEmpty_getEmptyResponseForWrongVersion_verifyValuesNotUpdated() {
    serviceOrchestrator.setNextResponseAsSuccess(parameterValues = emptyMap())

    // Fill the Platform Parameter Database with mock values to simulate the execution of a SyncUp
    // Work request that is not first.
    platformParameterController.updatePlatformParameterDatabase(mockPlatformParameterList)

    val workManager = WorkManager.getInstance(context)

    val inputData = Data.Builder().putString(
      PlatformParameterSyncUpWorker.WORKER_TYPE_KEY,
      PlatformParameterSyncUpWorker.PLATFORM_PARAMETER_WORKER
    ).build()

    val request: OneTimeWorkRequest = OneTimeWorkRequestBuilder<PlatformParameterSyncUpWorker>()
      .setInputData(inputData)
      .build()

    // Enqueue the Work Request to fetch and cache the Platform Parameters from Remote Service.
    workManager.enqueue(request)
    testCoroutineDispatchers.runCurrent()

    val workInfo = workManager.getWorkInfoById(request.id)
    assertThat(workInfo.get().state).isEqualTo(WorkInfo.State.FAILED)

    val exceptionMessage = fakeExceptionLogger.getMostRecentException().message
    assertThat(exceptionMessage)
      .isEqualTo(PlatformParameterSyncUpWorker.EMPTY_RESPONSE_EXCEPTION_MSG)

    // Retrieve the previously cached Platform Parameters from Cache Store.
    monitorFactory.ensureDataProviderExecutes(platformParameterController.getParameterDatabase())

    // Values retrieved from Cache store will be sent to Platform Parameter Singleton by the
    // Controller in the form of a Map, therefore verify the retrieved values from that Map.
    val platformParameterMap = platformParameterSingleton.getPlatformParameterMap()

    // Previous String Platform Parameter is still same in the Database.
    assertThat(platformParameterMap)
      .containsEntry(TEST_STRING_PARAM_NAME, expectedTestStringParameter)

    // Previous Integer Platform Parameter is still same in the Database.
    assertThat(platformParameterMap)
      .containsEntry(TEST_INTEGER_PARAM_NAME, defaultTestIntegerParameter)
  }

  @Test
  fun testSyncUpWorker_getFeatureFlags_addSyncStatusFlags_verifyCorrectStatusReturned() {
    serviceOrchestrator.setNextResponseAsSuccess()

    // Empty the Platform Parameter Database to simulate the execution of first SyncUp Work request.
    platformParameterController.updatePlatformParameterDatabase(listOf())

    val workManager = WorkManager.getInstance(context)

    val inputData = Data.Builder().putString(
      PlatformParameterSyncUpWorker.WORKER_TYPE_KEY,
      PlatformParameterSyncUpWorker.PLATFORM_PARAMETER_WORKER
    ).build()

    val request: OneTimeWorkRequest = OneTimeWorkRequestBuilder<PlatformParameterSyncUpWorker>()
      .setInputData(inputData)
      .build()

    // Enqueue the Work Request to fetch and cache the Platform Parameters from Remote Service.
    workManager.enqueue(request)
    testCoroutineDispatchers.runCurrent()

    // Retrieve the previously cached Platform Parameters from Cache Store.
    monitorFactory.ensureDataProviderExecutes(platformParameterController.getParameterDatabase())

    // Values retrieved from Cache store will be sent to Platform Parameter Singleton by the
    // Controller in the form of a Map, therefore verify the retrieved values from that Map.
    val platformParameterMap = platformParameterSingleton.getPlatformParameterMap()

    // Previous String Platform Parameter is still same in the Database.
    assertThat(platformParameterMap)
      .containsEntry(TEST_STRING_PARAM_NAME, expectedTestStringParameter)

    // SyncStatus of the platform parameter is SYNCED_FROM_SERVER
    assertThat(platformParameterMap[TEST_STRING_PARAM_NAME]?.syncStatus)
      .isEqualTo(SyncStatus.SYNCED_FROM_SERVER)
  }

  @Test
  fun testSyncUpWorker_databaseNotEmpty_getEmptyResponse_verifySyncStatusNotUpdated() {
    serviceOrchestrator.setNextResponseAsSuccess(parameterValues = emptyMap())

    // Fill the Platform Parameter Database with mock values to simulate the execution of a SyncUp
    // Work request that is not first.
    platformParameterController.updatePlatformParameterDatabase(mockPlatformParameterList)

    val workManager = WorkManager.getInstance(context)

    val inputData = Data.Builder().putString(
      PlatformParameterSyncUpWorker.WORKER_TYPE_KEY,
      PlatformParameterSyncUpWorker.PLATFORM_PARAMETER_WORKER
    ).build()

    val request: OneTimeWorkRequest = OneTimeWorkRequestBuilder<PlatformParameterSyncUpWorker>()
      .setInputData(inputData)
      .build()

    // Enqueue the Work Request to fetch and cache the Platform Parameters from Remote Service.
    workManager.enqueue(request)
    testCoroutineDispatchers.runCurrent()

    // Retrieve the previously cached Platform Parameters from Cache Store.
    monitorFactory.ensureDataProviderExecutes(platformParameterController.getParameterDatabase())

    // Values retrieved from Cache store will be sent to Platform Parameter Singleton by the
    // Controller in the form of a Map, therefore verify the retrieved values from that Map.
    val platformParameterMap = platformParameterSingleton.getPlatformParameterMap()

    // Previous Integer Platform Parameter is still same in the Database.
    assertThat(platformParameterMap)
      .containsEntry(TEST_INTEGER_PARAM_NAME, defaultTestIntegerParameter)

    // SyncStatus of the platform parameter is still the same in the database
    assertThat(platformParameterMap[TEST_INTEGER_PARAM_NAME]?.syncStatus)
      .isEqualTo(SyncStatus.NOT_SYNCED_FROM_SERVER)
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

    @Provides
    fun providePlatformParameterSingleton(
      platformParameterSingletonImpl: PlatformParameterSingletonImpl
    ): PlatformParameterSingleton = platformParameterSingletonImpl

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

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(
    modules = [
      LogStorageModule::class, RobolectricModule::class, TestDispatcherModule::class,
      TestModule::class, TestLogReportingModule::class, RetrofitModule::class,
      RetrofitServiceModule::class, FakeOppiaClockModule::class, NetworkConfigTestModule::class,
      NetworkConnectionUtilDebugModule::class, NetworkConnectionDebugUtilModule::class,
      LocaleProdModule::class, LoggingIdentifierModule::class, ApplicationLifecycleModule::class,
      SyncStatusModule::class, PlatformParameterModule::class
    ]
  )
  interface TestApplicationComponent : DataProvidersInjector {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder
      fun build(): TestApplicationComponent
    }

    fun inject(platformParameterSyncUpWorkerTest: PlatformParameterSyncUpWorkerTest)
  }

  class TestApplication : Application(), DataProvidersInjectorProvider {
    private val component: PlatformParameterSyncUpWorkerTest.TestApplicationComponent by lazy {
      DaggerPlatformParameterSyncUpWorkerTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(platformParameterSyncUpWorkerTest: PlatformParameterSyncUpWorkerTest) {
      component.inject(platformParameterSyncUpWorkerTest)
    }

    public override fun attachBaseContext(base: Context?) {
      super.attachBaseContext(base)
    }

    override fun getDataProvidersInjector(): DataProvidersInjector = component
  }
}
