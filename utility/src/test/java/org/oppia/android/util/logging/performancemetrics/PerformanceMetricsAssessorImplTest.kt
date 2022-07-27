package org.oppia.android.util.logging.performancemetrics

import android.app.ActivityManager
import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.net.TrafficStats
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.MockitoAnnotations
import org.oppia.android.app.model.OppiaMetricLog
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.robolectric.RobolectricModule
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
import org.oppia.android.util.networking.NetworkConnectionUtilDebugModule
import org.oppia.android.util.platformparameter.ENABLE_LANGUAGE_SELECTION_UI_DEFAULT_VALUE
import org.oppia.android.util.platformparameter.EnableLanguageSelectionUi
import org.oppia.android.util.platformparameter.LearnerStudyAnalytics
import org.oppia.android.util.platformparameter.PlatformParameterValue
import org.oppia.android.util.platformparameter.SPLASH_SCREEN_WELCOME_MSG_DEFAULT_VALUE
import org.oppia.android.util.platformparameter.SYNC_UP_WORKER_TIME_PERIOD_IN_HOURS_DEFAULT_VALUE
import org.oppia.android.util.platformparameter.SplashScreenWelcomeMsg
import org.oppia.android.util.platformparameter.SyncUpWorkerTimePeriodHours
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import org.robolectric.shadows.ShadowActivityManager
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

private const val TEST_PACKAGE_LABEL = "TEST_PACKAGE_LABEL"
private const val TEST_APP_PATH = "TEST_APP_PATH"
private const val TEST_PID = 1
private const val TEST_FILE_NAME = "TEST_FILE_NAME"
private const val TEST_FILE_CONTENT = "TEST_FILE_CONTENT"

/** Tests for [PerformanceMetricsAssessorImpl]. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = PerformanceMetricsAssessorImplTest.TestApplication::class)
class PerformanceMetricsAssessorImplTest {

  @Inject
  lateinit var performanceMetricsUtilsImpl: PerformanceMetricsAssessorImpl

  @Inject
  lateinit var context: Context

  @Before
  fun setUp() {
    MockitoAnnotations.initMocks(this)
    setUpTestApplicationComponent()
  }

  @Test
  fun testPerformanceMetricsUtils_setTotalMemory_returnsCorrectMemoryTier() {
    val activityManager: ActivityManager =
      ApplicationProvider.getApplicationContext<Application>()
        .getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val shadowActivityManager: ShadowActivityManager = shadowOf(activityManager)
    val memoryInfo = ActivityManager.MemoryInfo()
    memoryInfo.totalMem = (1.5 * 1024 * 1024 * 1024).toLong()
    shadowActivityManager.setMemoryInfo(memoryInfo)
    val memoryTier = performanceMetricsUtilsImpl.getDeviceMemoryTier()

    assertThat(memoryTier).isEqualTo(OppiaMetricLog.MemoryTier.LOW_MEMORY_TIER)
  }

  @Test
  fun testPerformanceMetricsUtils_getTotalStorageUsed_returnsCorrectStorageUsage() {
    context.openFileOutput(TEST_FILE_NAME, Context.MODE_PRIVATE).use {
      it.write(TEST_FILE_CONTENT.toByteArray())
    }
    val cacheFile = File.createTempFile(TEST_APP_PATH, null, context.cacheDir)
    val expectedStorageValue = TEST_FILE_CONTENT.length + cacheFile.length()

    assertThat(performanceMetricsUtilsImpl.getUsedStorage()).isEqualTo(expectedStorageValue)
  }

  @Test
  fun testPerformanceMetricsUtils_getTotalStorageUsageTier_returnsCorrectStorageUsageTier() {
    context.openFileOutput(TEST_FILE_NAME, Context.MODE_PRIVATE).use {
      it.write(TEST_FILE_CONTENT.toByteArray())
    }
    val cacheFile = File.createTempFile(TEST_APP_PATH, null, context.cacheDir)
    val expectedStorageValue = TEST_FILE_CONTENT.length + cacheFile.length()

    val expectedStorageTierValue = when (expectedStorageValue / (1024 * 1024 * 1024)) {
      in 0L until 32L -> OppiaMetricLog.StorageTier.LOW_STORAGE
      in 32L..64L -> OppiaMetricLog.StorageTier.MEDIUM_STORAGE
      else -> OppiaMetricLog.StorageTier.HIGH_STORAGE
    }

    assertThat(performanceMetricsUtilsImpl.getDeviceStorageTier())
      .isEqualTo(expectedStorageTierValue)
  }

  @Test
  fun testPerformanceMetricsUtils_getBytesSent_returnsCorrectAmountOfNetworkBytesSent() {
    val expectedNetworkBytesSent = TrafficStats.getUidTxBytes(
        ApplicationProvider.getApplicationContext<Application>().applicationInfo.uid
      )

    assertThat(performanceMetricsUtilsImpl.getTotalSentBytes())
      .isEqualTo(expectedNetworkBytesSent)
  }

  @Test
  fun testPerformanceMetricsUtils_getBytesReceived_returnsCorrectAmountOfNetworkBytesReceived() {
    val expectedNetworkBytesReceived = TrafficStats.getUidRxBytes(
      ApplicationProvider.getApplicationContext<Application>().applicationInfo.uid
    )

    assertThat(performanceMetricsUtilsImpl.getTotalReceivedBytes())
      .isEqualTo(expectedNetworkBytesReceived)
  }

  @Test
  fun testPerformanceMetricsUtils_setAppProcesses_getMemoryUsage_returnsCorrectMemoryUsage() {
    val activityManager: ActivityManager =
      ApplicationProvider.getApplicationContext<Application>()
        .getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val process1 = ActivityManager.RunningAppProcessInfo().apply {
      this.pid = TEST_PID
      this.importanceReasonComponent = ComponentName("com.robolectric", "process 1")
    }
    val shadowActivityManager: ShadowActivityManager = shadowOf(activityManager)
    shadowActivityManager.setProcesses(listOf(process1))
    val processMemoryInfo = activityManager.getProcessMemoryInfo(arrayOf(TEST_PID).toIntArray())
    val totalPssUsedTest = processMemoryInfo?.get(0)?.totalPss?.toLong() ?: 0L

    assertThat(performanceMetricsUtilsImpl.getTotalPssUsed()).isEqualTo(totalPssUsedTest)
  }

  @Test
  fun testPerformanceMetricsUtils_removeCurrentApp_installTestApp_returnsCorrectApkSize() {
    val application: Application = ApplicationProvider.getApplicationContext()
    val applicationInfo = ApplicationInfo()
    val testApkSize = (File(TEST_APP_PATH).length() / 1024)
    applicationInfo.apply {
      this.packageName = context.packageName
      this.sourceDir = TEST_APP_PATH
      this.name = TEST_PACKAGE_LABEL
      this.flags = 0
    }
    val packageManager = application.packageManager
    val shadowPackageManager = shadowOf(packageManager)

    shadowPackageManager.removePackage(application.packageName)
    shadowPackageManager.installPackage(
      PackageInfo().apply {
        this.packageName = context.packageName
        this.applicationInfo = applicationInfo
      }
    )

    val apkSize = performanceMetricsUtilsImpl.getApkSize()
    assertThat(apkSize).isEqualTo(testApkSize)
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  // TODO(#89): Move this to a common test application component.
  @Module
  class TestModule {

    @Provides
    @Singleton
    fun providesContext(application: Application): Context = application

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
  class TestPlatformParameterModule {

    companion object {
      var forceLearnerAnalyticsStudy: Boolean = false
    }

    @Provides
    @SplashScreenWelcomeMsg
    fun provideSplashScreenWelcomeMsgParam(): PlatformParameterValue<Boolean> {
      return PlatformParameterValue.createDefaultParameter(SPLASH_SCREEN_WELCOME_MSG_DEFAULT_VALUE)
    }

    @Provides
    @SyncUpWorkerTimePeriodHours
    fun provideSyncUpWorkerTimePeriod(): PlatformParameterValue<Int> {
      return PlatformParameterValue.createDefaultParameter(
        SYNC_UP_WORKER_TIME_PERIOD_IN_HOURS_DEFAULT_VALUE
      )
    }

    @Provides
    @EnableLanguageSelectionUi
    fun provideEnableLanguageSelectionUi(): PlatformParameterValue<Boolean> {
      return PlatformParameterValue.createDefaultParameter(
        ENABLE_LANGUAGE_SELECTION_UI_DEFAULT_VALUE
      )
    }

    @Provides
    @LearnerStudyAnalytics
    fun provideLearnerStudyAnalytics(): PlatformParameterValue<Boolean> {
      return PlatformParameterValue.createDefaultParameter(forceLearnerAnalyticsStudy)
    }
  }

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(
    modules = [
      TestModule::class, TestLogReportingModule::class,
      TestDispatcherModule::class, RobolectricModule::class, FakeOppiaClockModule::class,
      NetworkConnectionUtilDebugModule::class, LocaleProdModule::class,
      TestPlatformParameterModule::class, SyncStatusModule::class,
      PerformanceMetricsAssessorModule::class
    ]
  )
  interface TestApplicationComponent : DataProvidersInjector {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder
      fun build(): TestApplicationComponent
    }

    fun inject(performanceMetricsUtilsTest: PerformanceMetricsAssessorImplTest)
  }

  class TestApplication : Application(), DataProvidersInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerPerformanceMetricsAssessorImplTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(performanceMetricsUtilsTest: PerformanceMetricsAssessorImplTest) {
      component.inject(performanceMetricsUtilsTest)
    }

    override fun getDataProvidersInjector(): DataProvidersInjector = component
  }
}
