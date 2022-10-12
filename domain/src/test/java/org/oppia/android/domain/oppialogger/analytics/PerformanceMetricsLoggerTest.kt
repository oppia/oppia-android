package org.oppia.android.domain.oppialogger.analytics

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.Binds
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.oppia.android.app.model.OppiaMetricLog
import org.oppia.android.app.model.OppiaMetricLog.LoggableMetric.LoggableMetricTypeCase.APK_SIZE_METRIC
import org.oppia.android.app.model.OppiaMetricLog.LoggableMetric.LoggableMetricTypeCase.CPU_USAGE_METRIC
import org.oppia.android.app.model.OppiaMetricLog.LoggableMetric.LoggableMetricTypeCase.MEMORY_USAGE_METRIC
import org.oppia.android.app.model.OppiaMetricLog.LoggableMetric.LoggableMetricTypeCase.NETWORK_USAGE_METRIC
import org.oppia.android.app.model.OppiaMetricLog.LoggableMetric.LoggableMetricTypeCase.STARTUP_LATENCY_METRIC
import org.oppia.android.app.model.OppiaMetricLog.LoggableMetric.LoggableMetricTypeCase.STORAGE_USAGE_METRIC
import org.oppia.android.app.model.OppiaMetricLog.Priority.HIGH_PRIORITY
import org.oppia.android.app.model.OppiaMetricLog.Priority.LOW_PRIORITY
import org.oppia.android.app.model.OppiaMetricLog.Priority.MEDIUM_PRIORITY
import org.oppia.android.app.model.ScreenName.HOME_ACTIVITY
import org.oppia.android.app.model.ScreenName.SCREEN_NAME_UNSPECIFIED
import org.oppia.android.domain.oppialogger.EventLogStorageCacheSize
import org.oppia.android.domain.oppialogger.LoggingIdentifierModule
import org.oppia.android.domain.oppialogger.PerformanceMetricsLogStorageCacheSize
import org.oppia.android.domain.platformparameter.PlatformParameterModule
import org.oppia.android.domain.platformparameter.PlatformParameterSingletonModule
import org.oppia.android.testing.FakePerformanceMetricAssessor
import org.oppia.android.testing.FakePerformanceMetricsEventLogger
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.logging.SyncStatusTestModule
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClock
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.data.DataProvidersInjector
import org.oppia.android.util.data.DataProvidersInjectorProvider
import org.oppia.android.util.locale.LocaleProdModule
import org.oppia.android.util.logging.EnableConsoleLog
import org.oppia.android.util.logging.EnableFileLog
import org.oppia.android.util.logging.GlobalLogLevel
import org.oppia.android.util.logging.LogLevel
import org.oppia.android.util.logging.performancemetrics.PerformanceMetricsAssessor
import org.oppia.android.util.networking.NetworkConnectionUtilDebugModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

private const val TEST_TIMESTAMP = Long.MAX_VALUE
private const val TEST_CPU_USAGE = Long.MAX_VALUE
private const val TEST_APK_SIZE = Long.MAX_VALUE
private const val TEST_STORAGE_USAGE = Long.MAX_VALUE
private const val TEST_TOTAL_PSS = Long.MAX_VALUE
private const val TEST_BYTES_SENT = Long.MAX_VALUE
private const val TEST_BYTES_RECEIVED = Long.MAX_VALUE
private const val TEST_STARTUP_LATENCY_IN_MILLISECONDS = 3000L

/** Tests for [PerformanceMetricsLogger]. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = PerformanceMetricsLoggerTest.TestApplication::class)
class PerformanceMetricsLoggerTest {

  @Inject
  lateinit var performanceMetricsLogger: PerformanceMetricsLogger

  @Inject
  lateinit var performanceMetricsController: PerformanceMetricsController

  @Inject
  lateinit var fakePerformanceMetricsEventLogger: FakePerformanceMetricsEventLogger

  @Inject
  lateinit var fakeOppiaClock: FakeOppiaClock

  @Inject
  lateinit var fakePerformanceMetricAssessor: FakePerformanceMetricAssessor

  @Mock
  lateinit var context: Context

  private val testDeviceStorageTier = OppiaMetricLog.StorageTier.MEDIUM_STORAGE
  private val testDeviceMemoryTier = OppiaMetricLog.MemoryTier.MEDIUM_MEMORY_TIER

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
    setUpFakePerformanceMetricsUtils()
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_FIXED_FAKE_TIME)
    fakeOppiaClock.setCurrentTimeMs(TEST_TIMESTAMP)
  }

  @Test
  fun testLogger_logApkSizePerformanceMetric_verifyLogsMetricCorrectly() {
    val apkSize = fakePerformanceMetricAssessor.getApkSize()
    val memoryTier = fakePerformanceMetricAssessor.getDeviceMemoryTier()
    val storageTier = fakePerformanceMetricAssessor.getDeviceStorageTier()
    val isAppInForeground = performanceMetricsController.getIsAppInForeground()
    performanceMetricsLogger.logApkSize(SCREEN_NAME_UNSPECIFIED)

    val loggedEvent = fakePerformanceMetricsEventLogger.getMostRecentPerformanceMetricsEvent()
    assertThat(loggedEvent.timestampMillis).isEqualTo(TEST_TIMESTAMP)
    assertThat(loggedEvent.priority).isEqualTo(LOW_PRIORITY)
    assertThat(loggedEvent.currentScreen).isEqualTo(SCREEN_NAME_UNSPECIFIED)
    assertThat(loggedEvent.loggableMetric.loggableMetricTypeCase).isEqualTo(APK_SIZE_METRIC)
    assertThat(loggedEvent.loggableMetric.apkSizeMetric.apkSizeBytes).isEqualTo(apkSize)
    assertThat(loggedEvent.memoryTier).isEqualTo(memoryTier)
    assertThat(loggedEvent.storageTier).isEqualTo(storageTier)
    assertThat(loggedEvent.isAppInForeground).isEqualTo(isAppInForeground)
  }

  @Test
  fun testLogger_logStorageUsagePerformanceMetric_verifyLogsMetricCorrectly() {
    val memoryTier = fakePerformanceMetricAssessor.getDeviceMemoryTier()
    val storageTier = fakePerformanceMetricAssessor.getDeviceStorageTier()
    val isAppInForeground = performanceMetricsController.getIsAppInForeground()
    val storageUsage = fakePerformanceMetricAssessor.getUsedStorage()
    performanceMetricsLogger.logStorageUsage(SCREEN_NAME_UNSPECIFIED)

    val loggedEvent = fakePerformanceMetricsEventLogger.getMostRecentPerformanceMetricsEvent()
    assertThat(loggedEvent.timestampMillis).isEqualTo(TEST_TIMESTAMP)
    assertThat(loggedEvent.priority).isEqualTo(LOW_PRIORITY)
    assertThat(loggedEvent.currentScreen).isEqualTo(SCREEN_NAME_UNSPECIFIED)
    assertThat(loggedEvent.loggableMetric.loggableMetricTypeCase).isEqualTo(STORAGE_USAGE_METRIC)
    assertThat(loggedEvent.loggableMetric.storageUsageMetric.storageUsageBytes).isEqualTo(
      storageUsage
    )
    assertThat(loggedEvent.memoryTier).isEqualTo(memoryTier)
    assertThat(loggedEvent.storageTier).isEqualTo(storageTier)
    assertThat(loggedEvent.isAppInForeground).isEqualTo(isAppInForeground)
  }

  @Test
  fun testLogger_logMemoryUsagePerformanceMetric_verifyLogsMetricCorrectly() {
    val memoryUsage = fakePerformanceMetricAssessor.getTotalPssUsed()
    val memoryTier = fakePerformanceMetricAssessor.getDeviceMemoryTier()
    val storageTier = fakePerformanceMetricAssessor.getDeviceStorageTier()
    val isAppInForeground = performanceMetricsController.getIsAppInForeground()
    performanceMetricsLogger.logMemoryUsage(SCREEN_NAME_UNSPECIFIED)

    val loggedEvent = fakePerformanceMetricsEventLogger.getMostRecentPerformanceMetricsEvent()
    assertThat(loggedEvent.timestampMillis).isEqualTo(TEST_TIMESTAMP)
    assertThat(loggedEvent.priority).isEqualTo(MEDIUM_PRIORITY)
    assertThat(loggedEvent.currentScreen).isEqualTo(SCREEN_NAME_UNSPECIFIED)
    assertThat(loggedEvent.loggableMetric.loggableMetricTypeCase).isEqualTo(MEMORY_USAGE_METRIC)
    assertThat(loggedEvent.loggableMetric.memoryUsageMetric.totalPssBytes).isEqualTo(memoryUsage)
    assertThat(loggedEvent.memoryTier).isEqualTo(memoryTier)
    assertThat(loggedEvent.storageTier).isEqualTo(storageTier)
    assertThat(loggedEvent.isAppInForeground).isEqualTo(isAppInForeground)
  }

  @Test
  fun testLogger_logStartupLatencyPerformanceMetric_verifyLogsMetricCorrectly() {
    val memoryTier = fakePerformanceMetricAssessor.getDeviceMemoryTier()
    val storageTier = fakePerformanceMetricAssessor.getDeviceStorageTier()
    val isAppInForeground = performanceMetricsController.getIsAppInForeground()
    performanceMetricsLogger.logStartupLatency(
      TEST_STARTUP_LATENCY_IN_MILLISECONDS,
      SCREEN_NAME_UNSPECIFIED
    )

    val loggedEvent = fakePerformanceMetricsEventLogger.getMostRecentPerformanceMetricsEvent()
    assertThat(loggedEvent.timestampMillis).isEqualTo(TEST_TIMESTAMP)
    assertThat(loggedEvent.priority).isEqualTo(LOW_PRIORITY)
    assertThat(loggedEvent.currentScreen).isEqualTo(SCREEN_NAME_UNSPECIFIED)
    assertThat(loggedEvent.loggableMetric.loggableMetricTypeCase).isEqualTo(STARTUP_LATENCY_METRIC)
    // Startup latency millis will be equal to 0 since the initial timestamp is equal to
    // Long.MAX_VALUE and using FakeOppiaClock we've set the recording timestamp to also be
    // Long.MAX_VALUE. The difference between the two equals 0 and hence the startup latency millis.
    assertThat(loggedEvent.loggableMetric.startupLatencyMetric.startupLatencyMillis)
      .isEqualTo(TEST_STARTUP_LATENCY_IN_MILLISECONDS)
    assertThat(loggedEvent.memoryTier).isEqualTo(memoryTier)
    assertThat(loggedEvent.storageTier).isEqualTo(storageTier)
    assertThat(loggedEvent.isAppInForeground).isEqualTo(isAppInForeground)
  }

  @Test
  fun testLogger_logCpuUsagePerformanceMetric_verifyLogsMetricCorrectly() {
    val memoryTier = fakePerformanceMetricAssessor.getDeviceMemoryTier()
    val storageTier = fakePerformanceMetricAssessor.getDeviceStorageTier()
    val isAppInForeground = performanceMetricsController.getIsAppInForeground()
    performanceMetricsLogger.logCpuUsage(
      SCREEN_NAME_UNSPECIFIED,
      HOME_ACTIVITY,
      TEST_CPU_USAGE.toDouble()
    )

    val loggedEvent = fakePerformanceMetricsEventLogger.getMostRecentPerformanceMetricsEvent()
    assertThat(loggedEvent.timestampMillis).isEqualTo(TEST_TIMESTAMP)
    assertThat(loggedEvent.priority).isEqualTo(HIGH_PRIORITY)
    assertThat(loggedEvent.currentScreen).isEqualTo(SCREEN_NAME_UNSPECIFIED)
    assertThat(loggedEvent.loggableMetric.loggableMetricTypeCase).isEqualTo(CPU_USAGE_METRIC)
    assertThat(loggedEvent.loggableMetric.cpuUsageMetric.cpuUsageMetric).isEqualTo(TEST_CPU_USAGE)
    assertThat(loggedEvent.memoryTier).isEqualTo(memoryTier)
    assertThat(loggedEvent.storageTier).isEqualTo(storageTier)
    assertThat(loggedEvent.isAppInForeground).isEqualTo(isAppInForeground)
  }

  @Test
  fun testLogger_logNetworkUsagePerformanceMetric_verifyLogsMetricCorrectly() {
    val bytesSent = fakePerformanceMetricAssessor.getTotalSentBytes()
    val bytesReceived = fakePerformanceMetricAssessor.getTotalReceivedBytes()
    val memoryTier = fakePerformanceMetricAssessor.getDeviceMemoryTier()
    val storageTier = fakePerformanceMetricAssessor.getDeviceStorageTier()
    val isAppInForeground = performanceMetricsController.getIsAppInForeground()
    performanceMetricsLogger.logNetworkUsage(SCREEN_NAME_UNSPECIFIED)

    val loggedEvent = fakePerformanceMetricsEventLogger.getMostRecentPerformanceMetricsEvent()
    assertThat(loggedEvent.timestampMillis).isEqualTo(TEST_TIMESTAMP)
    assertThat(loggedEvent.priority).isEqualTo(HIGH_PRIORITY)
    assertThat(loggedEvent.currentScreen).isEqualTo(SCREEN_NAME_UNSPECIFIED)
    assertThat(loggedEvent.loggableMetric.loggableMetricTypeCase).isEqualTo(NETWORK_USAGE_METRIC)
    assertThat(loggedEvent.loggableMetric.networkUsageMetric.bytesSent).isEqualTo(bytesSent)
    assertThat(loggedEvent.loggableMetric.networkUsageMetric.bytesReceived).isEqualTo(bytesReceived)
    assertThat(loggedEvent.memoryTier).isEqualTo(memoryTier)
    assertThat(loggedEvent.storageTier).isEqualTo(storageTier)
    assertThat(loggedEvent.isAppInForeground).isEqualTo(isAppInForeground)
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  private fun setUpFakePerformanceMetricsUtils() {
    fakePerformanceMetricAssessor.apply {
      setApkSize(TEST_APK_SIZE)
      setStorageUsage(TEST_STORAGE_USAGE)
      setTotalPss(TEST_TOTAL_PSS)
      setTotalSentBytes(TEST_BYTES_SENT)
      setTotalReceivedBytes(TEST_BYTES_RECEIVED)
      setDeviceStorageTier(testDeviceStorageTier)
      setDeviceMemoryTier(testDeviceMemoryTier)
    }
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
  class TestLogStorageModule {

    @Provides
    @PerformanceMetricsLogStorageCacheSize
    fun providePerformanceMetricsLogStorageCacheSize(): Int = 2

    @Provides
    @EventLogStorageCacheSize
    fun provideEventLogStorageCacheSize(): Int = 2
  }

  @Module
  interface TestPerformanceMetricsModule {
    @Binds
    fun bindPerformanceMetricsUtils(
      fakePerformanceMetricAssessor: FakePerformanceMetricAssessor
    ): PerformanceMetricsAssessor
  }

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(
    modules = [
      TestModule::class, TestLogReportingModule::class, RobolectricModule::class,
      TestDispatcherModule::class, TestLogStorageModule::class,
      NetworkConnectionUtilDebugModule::class, LocaleProdModule::class, FakeOppiaClockModule::class,
      PlatformParameterModule::class, PlatformParameterSingletonModule::class,
      LoggingIdentifierModule::class, SyncStatusTestModule::class,
      ActivityLifecycleObserverModule::class
    ]
  )
  interface TestApplicationComponent : DataProvidersInjector {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder
      fun build(): TestApplicationComponent
    }

    fun inject(performanceMetricsLoggerTest: PerformanceMetricsLoggerTest)
  }

  class TestApplication : Application(), DataProvidersInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerPerformanceMetricsLoggerTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(performanceMetricsLoggerTest: PerformanceMetricsLoggerTest) {
      component.inject(performanceMetricsLoggerTest)
    }

    override fun getDataProvidersInjector(): DataProvidersInjector = component
  }
}
