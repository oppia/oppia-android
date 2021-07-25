package org.oppia.android.domain.platformparameter.syncup

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
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

@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(manifest = Config.NONE)
class PlatformParameterSyncUpWorkerTest {

  @Inject
  lateinit var platformParameterSingleton: PlatformParameterSingleton

  @Inject
  lateinit var platformParameterController: PlatformParameterController

  @Inject
  lateinit var platformParameterSyncUpWorkerFactory: PlatformParameterSyncUpWorkerFactory

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @Before
  fun setup() {
    setUpTestApplicationComponent()
    val context = InstrumentationRegistry.getInstrumentation().targetContext
    val config = Configuration.Builder()
      .setExecutor(SynchronousExecutor())
      .setWorkerFactory(platformParameterSyncUpWorkerFactory)
      .build()
    WorkManagerTestInitHelper.initializeTestWorkManager(context, config)
  }

  @Test
  fun testSyncUpWorker_refreshPlatformParameters_cacheResponse_verifyMapInSingleton() {
    val workManager = WorkManager.getInstance(ApplicationProvider.getApplicationContext())

    val inputData = Data.Builder().putString(
      PlatformParameterSyncUpWorker.WORKER_TYPE_KEY,
      PlatformParameterSyncUpWorker.PLATFORM_PARAMETER_WORKER
    ).build()

    val request: OneTimeWorkRequest = OneTimeWorkRequestBuilder<PlatformParameterSyncUpWorker>()
      .setInputData(inputData)
      .build()

    workManager.enqueue(request)
    testCoroutineDispatchers.runCurrent()

    val workInfo = workManager.getWorkInfoById(request.id)
    assertThat(workInfo.get().state).isEqualTo(WorkInfo.State.SUCCEEDED)

    platformParameterController.getParameterDatabase()
    testCoroutineDispatchers.runCurrent()

    val platformParameterMap = platformParameterSingleton.getPlatformParameterMap()
    assertThat(platformParameterMap).isNotEmpty()
    assertThat(platformParameterMap).containsEntry(
      TEST_STRING_PARAM_NAME,
      TEST_STRING_PARAM_SERVER_VALUE
    )
  }

  private fun setUpTestApplicationComponent() {
    DaggerPlatformParameterSyncUpWorkerTest_TestApplicationComponent.builder()
      .setApplication(ApplicationProvider.getApplicationContext())
      .build()
      .inject(this)
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
  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder
      fun build(): TestApplicationComponent
    }

    fun inject(platformParameterSyncUpWorkerTest: PlatformParameterSyncUpWorkerTest)
  }
}
