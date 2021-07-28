package org.oppia.android.domain.platformparameter.syncup

import android.app.Application
import android.content.Context
import androidx.lifecycle.Observer
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.content.pm.ApplicationInfoBuilder
import androidx.test.core.content.pm.PackageInfoBuilder
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
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.data.backends.gae.NetworkModule
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.domain.platformparameter.PlatformParameterController
import org.oppia.android.domain.platformparameter.PlatformParameterSingletonImpl
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.network.RetrofitTestModule
import org.oppia.android.testing.platformparameter.TEST_STRING_PARAM_NAME
import org.oppia.android.testing.platformparameter.TEST_STRING_PARAM_SERVER_VALUE
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.logging.EnableConsoleLog
import org.oppia.android.util.logging.EnableFileLog
import org.oppia.android.util.logging.GlobalLogLevel
import org.oppia.android.util.logging.LogLevel
import org.oppia.android.util.platformparameter.PlatformParameterSingleton
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton
import org.junit.Rule
import org.mockito.Mock
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import org.oppia.android.app.model.PlatformParameter
import org.oppia.android.testing.platformparameter.TEST_BOOLEAN_PARAM_NAME
import org.oppia.android.testing.platformparameter.TEST_BOOLEAN_PARAM_SERVER_VALUE
import org.oppia.android.testing.platformparameter.TEST_INTEGER_PARAM_NAME
import org.oppia.android.testing.platformparameter.TEST_INTEGER_PARAM_SERVER_VALUE
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import org.oppia.android.util.data.DataProvidersInjector
import org.oppia.android.util.data.DataProvidersInjectorProvider
import org.robolectric.Shadows

@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(
  application = PlatformParameterSyncUpWorkerTest.TestApplication::class,
  manifest = Config.NONE
)
class PlatformParameterSyncUpWorkerTest {

  @Rule
  @JvmField
  val mockitoRule: MockitoRule = MockitoJUnit.rule()

  @Mock
  lateinit var mockUnitObserver: Observer<AsyncResult<Unit>>

  @Inject
  lateinit var platformParameterSingleton: PlatformParameterSingleton

  @Inject
  lateinit var platformParameterController: PlatformParameterController

  @Inject
  lateinit var platformParameterSyncUpWorkerFactory: PlatformParameterSyncUpWorkerFactory

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @Inject
  lateinit var context : Context

  private val testVersionName = "1.0"

  private val testVersionCode = 1

  private val expectedTestStringParameter = PlatformParameter.newBuilder()
    .setName(TEST_STRING_PARAM_NAME)
    .setString(TEST_STRING_PARAM_SERVER_VALUE)
    .build()

  private val expectedTestIntegerParameter = PlatformParameter.newBuilder()
    .setName(TEST_INTEGER_PARAM_NAME)
    .setInteger(TEST_INTEGER_PARAM_SERVER_VALUE)
    .build()

  private val expectedTestBooleanParameter = PlatformParameter.newBuilder()
    .setName(TEST_BOOLEAN_PARAM_NAME)
    .setBoolean(TEST_BOOLEAN_PARAM_SERVER_VALUE)
    .build()

  // Not including "expectedTestBooleanParameter" in this list to prove that a refresh took place
  private val mockPlatformParameterList = listOf<PlatformParameter>(
    expectedTestStringParameter,
    expectedTestIntegerParameter
  )

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
  fun testSyncUpWorker_previousDatabaseIsEmpty_refreshPlatformParameters_verifyCachedValues() {
    // Empty the Platform Parameter Database to simulate the execution of first SyncUp Work request
    platformParameterController.updatePlatformParameterDatabase(listOf())

    val workManager = WorkManager.getInstance(context)

    val inputData = Data.Builder().putString(
      PlatformParameterSyncUpWorker.WORKER_TYPE_KEY,
      PlatformParameterSyncUpWorker.PLATFORM_PARAMETER_WORKER
    ).build()

    val request: OneTimeWorkRequest = OneTimeWorkRequestBuilder<PlatformParameterSyncUpWorker>()
      .setInputData(inputData)
      .build()

    // Enqueue the Work Request to fetch and cache the Platform Parameters from Remote Service
    workManager.enqueue(request)
    testCoroutineDispatchers.runCurrent()

    val workInfo = workManager.getWorkInfoById(request.id)
    assertThat(workInfo.get().state).isEqualTo(WorkInfo.State.SUCCEEDED)

    // Retrieve the previously cached Platform Parameters from Cache Store
    platformParameterController.getParameterDatabase().toLiveData().observeForever(mockUnitObserver)
    testCoroutineDispatchers.runCurrent()

    // Values retrieved from Cache store will be sent to Platform Parameter Singleton by the
    // Controller in the form of a Map, therefore verify the retrieved values from that Map
    val platformParameterMap = platformParameterSingleton.getPlatformParameterMap()
    assertThat(platformParameterMap).isNotEmpty()
    assertThat(platformParameterMap).containsEntry(
      TEST_STRING_PARAM_NAME,
      expectedTestStringParameter
    )
  }

  @Test
  fun testSyncUpWorker_previousDatabaseIsNotEmpty_refreshPlatformParameters_verifyCachedValues() {
    // Fill the Platform Parameter Database with mock values to simulate the execution of a SyncUp
    // Work request that is not first
    platformParameterController.updatePlatformParameterDatabase(mockPlatformParameterList)

    val workManager = WorkManager.getInstance(context)

    val inputData = Data.Builder().putString(
      PlatformParameterSyncUpWorker.WORKER_TYPE_KEY,
      PlatformParameterSyncUpWorker.PLATFORM_PARAMETER_WORKER
    ).build()

    val request: OneTimeWorkRequest = OneTimeWorkRequestBuilder<PlatformParameterSyncUpWorker>()
      .setInputData(inputData)
      .build()

    // Enqueue the Work Request to fetch and cache the Platform Parameters from Remote Service
    workManager.enqueue(request)
    testCoroutineDispatchers.runCurrent()

    val workInfo = workManager.getWorkInfoById(request.id)
    assertThat(workInfo.get().state).isEqualTo(WorkInfo.State.SUCCEEDED)

    // Retrieve the previously cached Platform Parameters from Cache Store
    platformParameterController.getParameterDatabase().toLiveData().observeForever(mockUnitObserver)
    testCoroutineDispatchers.runCurrent()

    // Values retrieved from Cache store will be sent to Platform Parameter Singleton by the
    // Controller in the form of a Map, therefore verify the retrieved values from that Map
    val platformParameterMap = platformParameterSingleton.getPlatformParameterMap()
    assertThat(platformParameterMap).isNotEmpty()

    // New Boolean Platform Parameter is now present in the Database along with the previous ones
    assertThat(platformParameterMap).containsEntry(
      TEST_BOOLEAN_PARAM_NAME,
      expectedTestBooleanParameter
    )
    assertThat(platformParameterMap).containsEntry(
      TEST_STRING_PARAM_NAME,
      expectedTestStringParameter
    )
    assertThat(platformParameterMap).containsEntry(
      TEST_INTEGER_PARAM_NAME,
      expectedTestIntegerParameter
    )
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
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

    @Provides
    fun providePlatformParameterSingleton(
      platformParameterSingletonImpl: PlatformParameterSingletonImpl
    ): PlatformParameterSingleton = platformParameterSingletonImpl

//    @Provides
//    @PlatformParameterServiceTest.MockPlatformParameterService
//    fun provideMockPlatformParameterService(mockRetrofit: MockRetrofit): PlatformParameterService {
//      val delegate = mockRetrofit.create(PlatformParameterService::class.java)
//      return MockPlatformParameterService(delegate)
//    }

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
      TestModule::class, TestLogReportingModule::class, NetworkModule::class,
      RetrofitTestModule::class, FakeOppiaClockModule::class
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
