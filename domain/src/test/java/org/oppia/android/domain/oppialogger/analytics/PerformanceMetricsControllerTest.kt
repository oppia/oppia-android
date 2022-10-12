package org.oppia.android.domain.oppialogger.analytics

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.runner.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
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
import org.oppia.android.app.model.ScreenName.SCREEN_NAME_UNSPECIFIED
import org.oppia.android.domain.oppialogger.EventLogStorageCacheSize
import org.oppia.android.domain.oppialogger.LoggingIdentifierModule
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.oppialogger.PerformanceMetricsLogStorageCacheSize
import org.oppia.android.domain.oppialogger.logscheduler.MetricLogSchedulerModule
import org.oppia.android.domain.platformparameter.PlatformParameterModule
import org.oppia.android.domain.platformparameter.PlatformParameterSingletonModule
import org.oppia.android.testing.FakePerformanceMetricsEventLogger
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.data.DataProviderTestMonitor
import org.oppia.android.testing.logging.SyncStatusTestModule
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClock
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.data.DataProviders
import org.oppia.android.util.data.DataProvidersInjector
import org.oppia.android.util.data.DataProvidersInjectorProvider
import org.oppia.android.util.locale.LocaleProdModule
import org.oppia.android.util.logging.EnableConsoleLog
import org.oppia.android.util.logging.EnableFileLog
import org.oppia.android.util.logging.GlobalLogLevel
import org.oppia.android.util.logging.LogLevel
import org.oppia.android.util.networking.NetworkConnectionDebugUtil
import org.oppia.android.util.networking.NetworkConnectionUtil.ProdConnectionStatus.NONE
import org.oppia.android.util.networking.NetworkConnectionUtilDebugModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

private const val TEST_TIMESTAMP = Long.MAX_VALUE
private const val TEST_CPU_USAGE = Long.MAX_VALUE.toDouble()
private const val TEST_APK_SIZE = Long.MAX_VALUE
private const val TEST_STORAGE_USAGE = Long.MAX_VALUE
private const val TEST_STARTUP_LATENCY = Long.MAX_VALUE
private const val TEST_NETWORK_USAGE = Long.MAX_VALUE
private const val TEST_MEMORY_USAGE = Long.MAX_VALUE

/** Tests for [PerformanceMetricsController]. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = PerformanceMetricsControllerTest.TestApplication::class)
class PerformanceMetricsControllerTest {

  @Inject
  lateinit var performanceMetricsController: PerformanceMetricsController

  @Inject
  lateinit var oppiaLogger: OppiaLogger

  @Inject
  lateinit var networkConnectionUtil: NetworkConnectionDebugUtil

  @Inject
  lateinit var fakePerformanceMetricsEventLogger: FakePerformanceMetricsEventLogger

  @Inject
  lateinit var dataProviders: DataProviders

  @Inject
  lateinit var monitorFactory: DataProviderTestMonitor.Factory

  @Inject
  lateinit var fakeOppiaClock: FakeOppiaClock

  private val apkSizeTestLoggableMetric = OppiaMetricLog.LoggableMetric.newBuilder()
    .setApkSizeMetric(
      OppiaMetricLog.ApkSizeMetric.newBuilder()
        .setApkSizeBytes(TEST_APK_SIZE)
        .build()
    ).build()

  private val storageUsageTestLoggableMetric = OppiaMetricLog.LoggableMetric.newBuilder()
    .setStorageUsageMetric(
      OppiaMetricLog.StorageUsageMetric.newBuilder()
        .setStorageUsageBytes(TEST_STORAGE_USAGE)
        .build()
    ).build()

  private val startupLatencyTestLoggableMetric = OppiaMetricLog.LoggableMetric.newBuilder()
    .setStartupLatencyMetric(
      OppiaMetricLog.StartupLatencyMetric.newBuilder()
        .setStartupLatencyMillis(TEST_STARTUP_LATENCY)
        .build()
    ).build()

  private val cpuUsageTestLoggableMetric = OppiaMetricLog.LoggableMetric.newBuilder()
    .setCpuUsageMetric(
      OppiaMetricLog.CpuUsageMetric.newBuilder()
        .setCpuUsageMetric(TEST_CPU_USAGE)
        .build()
    ).build()

  private val networkUsageTestLoggableMetric = OppiaMetricLog.LoggableMetric.newBuilder()
    .setNetworkUsageMetric(
      OppiaMetricLog.NetworkUsageMetric.newBuilder()
        .setBytesReceived(TEST_NETWORK_USAGE)
        .setBytesSent(TEST_NETWORK_USAGE)
        .build()
    ).build()

  private val memoryUsageTestLoggableMetric = OppiaMetricLog.LoggableMetric.newBuilder()
    .setMemoryUsageMetric(
      OppiaMetricLog.MemoryUsageMetric.newBuilder()
        .setTotalPssBytes(TEST_MEMORY_USAGE)
        .build()
    ).build()

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_FIXED_FAKE_TIME)
    fakeOppiaClock.setCurrentTimeMs(TEST_TIMESTAMP)
  }

  @Test
  fun testController_logPerformanceMetric_withApkSizeLoggableMetric_checkLogsMetric() {
    performanceMetricsController.logPerformanceMetricsEvent(
      TEST_TIMESTAMP,
      SCREEN_NAME_UNSPECIFIED,
      apkSizeTestLoggableMetric,
      LOW_PRIORITY
    )

    val performanceMetricsLog =
      fakePerformanceMetricsEventLogger.getMostRecentPerformanceMetricsEvent()

    assertThat(performanceMetricsLog.timestampMillis).isEqualTo(TEST_TIMESTAMP)
    assertThat(performanceMetricsLog.currentScreen).isEqualTo(SCREEN_NAME_UNSPECIFIED)
    assertThat(performanceMetricsLog.priority).isEqualTo(LOW_PRIORITY)
    assertThat(performanceMetricsLog.loggableMetric.loggableMetricTypeCase).isEqualTo(
      APK_SIZE_METRIC
    )
  }

  @Test
  fun testController_logPerformanceMetric_withStorageUsageLoggableMetric_checkLogsMetric() {
    performanceMetricsController.logPerformanceMetricsEvent(
      TEST_TIMESTAMP,
      SCREEN_NAME_UNSPECIFIED,
      storageUsageTestLoggableMetric,
      LOW_PRIORITY
    )

    val performanceMetricsLog =
      fakePerformanceMetricsEventLogger.getMostRecentPerformanceMetricsEvent()

    assertThat(performanceMetricsLog.timestampMillis).isEqualTo(TEST_TIMESTAMP)
    assertThat(performanceMetricsLog.currentScreen).isEqualTo(SCREEN_NAME_UNSPECIFIED)
    assertThat(performanceMetricsLog.priority).isEqualTo(LOW_PRIORITY)
    assertThat(performanceMetricsLog.loggableMetric.loggableMetricTypeCase).isEqualTo(
      STORAGE_USAGE_METRIC
    )
  }

  @Test
  fun testController_logPerformanceMetric_withStartupLatencyLoggableMetric_checkLogsMetric() {
    performanceMetricsController.logPerformanceMetricsEvent(
      TEST_TIMESTAMP,
      SCREEN_NAME_UNSPECIFIED,
      startupLatencyTestLoggableMetric,
      LOW_PRIORITY
    )

    val performanceMetricsLog =
      fakePerformanceMetricsEventLogger.getMostRecentPerformanceMetricsEvent()

    assertThat(performanceMetricsLog.timestampMillis).isEqualTo(TEST_TIMESTAMP)
    assertThat(performanceMetricsLog.currentScreen).isEqualTo(SCREEN_NAME_UNSPECIFIED)
    assertThat(performanceMetricsLog.priority).isEqualTo(LOW_PRIORITY)
    assertThat(performanceMetricsLog.loggableMetric.loggableMetricTypeCase).isEqualTo(
      STARTUP_LATENCY_METRIC
    )
  }

  @Test
  fun testController_logPerformanceMetric_withMemoryUsageLoggableMetric_checkLogsMetric() {
    performanceMetricsController.logPerformanceMetricsEvent(
      TEST_TIMESTAMP,
      SCREEN_NAME_UNSPECIFIED,
      memoryUsageTestLoggableMetric,
      MEDIUM_PRIORITY
    )

    val performanceMetricsLog =
      fakePerformanceMetricsEventLogger.getMostRecentPerformanceMetricsEvent()

    assertThat(performanceMetricsLog.timestampMillis).isEqualTo(TEST_TIMESTAMP)
    assertThat(performanceMetricsLog.currentScreen).isEqualTo(SCREEN_NAME_UNSPECIFIED)
    assertThat(performanceMetricsLog.priority).isEqualTo(MEDIUM_PRIORITY)
    assertThat(performanceMetricsLog.loggableMetric.loggableMetricTypeCase).isEqualTo(
      MEMORY_USAGE_METRIC
    )
  }

  @Test
  fun testController_logPerformanceMetric_withNetworkUsageLoggableMetric_checkLogsMetric() {
    performanceMetricsController.logPerformanceMetricsEvent(
      TEST_TIMESTAMP,
      SCREEN_NAME_UNSPECIFIED,
      networkUsageTestLoggableMetric,
      HIGH_PRIORITY
    )

    val performanceMetricsLog =
      fakePerformanceMetricsEventLogger.getMostRecentPerformanceMetricsEvent()

    assertThat(performanceMetricsLog.timestampMillis).isEqualTo(TEST_TIMESTAMP)
    assertThat(performanceMetricsLog.currentScreen).isEqualTo(SCREEN_NAME_UNSPECIFIED)
    assertThat(performanceMetricsLog.priority).isEqualTo(HIGH_PRIORITY)
    assertThat(performanceMetricsLog.loggableMetric.loggableMetricTypeCase).isEqualTo(
      NETWORK_USAGE_METRIC
    )
  }

  @Test
  fun testController_logPerformanceMetric_withCpuUsageLoggableMetric_checkLogsMetric() {
    performanceMetricsController.logPerformanceMetricsEvent(
      TEST_TIMESTAMP,
      SCREEN_NAME_UNSPECIFIED,
      cpuUsageTestLoggableMetric,
      HIGH_PRIORITY
    )

    val performanceMetricsLog =
      fakePerformanceMetricsEventLogger.getMostRecentPerformanceMetricsEvent()

    assertThat(performanceMetricsLog.timestampMillis).isEqualTo(TEST_TIMESTAMP)
    assertThat(performanceMetricsLog.currentScreen).isEqualTo(SCREEN_NAME_UNSPECIFIED)
    assertThat(performanceMetricsLog.priority).isEqualTo(HIGH_PRIORITY)
    assertThat(performanceMetricsLog.loggableMetric.loggableMetricTypeCase).isEqualTo(
      CPU_USAGE_METRIC
    )
  }

  @Test
  fun testController_logPerformanceMetric_withNoNetwork_checkLogsEventToStore() {
    networkConnectionUtil.setCurrentConnectionStatus(NONE)
    performanceMetricsController.logPerformanceMetricsEvent(
      TEST_TIMESTAMP,
      SCREEN_NAME_UNSPECIFIED,
      apkSizeTestLoggableMetric,
      LOW_PRIORITY
    )

    val eventLogsProvider = performanceMetricsController.getMetricLogStore()

    val performanceMetricsLog =
      monitorFactory.waitForNextSuccessfulResult(eventLogsProvider).getOppiaMetricLog(0)
    assertThat(performanceMetricsLog.timestampMillis).isEqualTo(TEST_TIMESTAMP)
    assertThat(performanceMetricsLog.priority).isEqualTo(LOW_PRIORITY)
    assertThat(performanceMetricsLog.loggableMetric.loggableMetricTypeCase).isEqualTo(
      APK_SIZE_METRIC
    )
  }

  @Test
  fun testController_logPerformanceMetric_withNoNetwork_exceedLimit_checkEventLogStoreSize() {
    networkConnectionUtil.setCurrentConnectionStatus(NONE)
    logMultiplePerformanceMetrics()

    val eventLogsProvider = performanceMetricsController.getMetricLogStore()

    val performanceMetricLogs = monitorFactory.waitForNextSuccessfulResult(eventLogsProvider)
    assertThat(performanceMetricLogs.oppiaMetricLogList).hasSize(2)
  }

  @Test
  fun testController_logMultiplePerformanceMetrics_withNoNetwork_checkOrderInCache() {
    networkConnectionUtil.setCurrentConnectionStatus(NONE)
    performanceMetricsController.logPerformanceMetricsEvent(
      TEST_TIMESTAMP,
      SCREEN_NAME_UNSPECIFIED,
      apkSizeTestLoggableMetric,
      LOW_PRIORITY
    )
    performanceMetricsController.logPerformanceMetricsEvent(
      TEST_TIMESTAMP,
      SCREEN_NAME_UNSPECIFIED,
      cpuUsageTestLoggableMetric,
      HIGH_PRIORITY
    )

    val metricLogsProvider = performanceMetricsController.getMetricLogStore()

    val metricLogs = monitorFactory.waitForNextSuccessfulResult(metricLogsProvider)
    val firstEventLog = metricLogs.getOppiaMetricLog(0)
    val secondEventLog = metricLogs.getOppiaMetricLog(1)

    assertThat(firstEventLog.priority).isEqualTo(LOW_PRIORITY)
    assertThat(secondEventLog.priority).isEqualTo(HIGH_PRIORITY)
  }

  @Test
  fun testController_logPerformanceMetric_switchToNoNetwork_logPerformanceMetric_checkManagement() {
    performanceMetricsController.logPerformanceMetricsEvent(
      TEST_TIMESTAMP,
      SCREEN_NAME_UNSPECIFIED,
      cpuUsageTestLoggableMetric,
      HIGH_PRIORITY
    )
    networkConnectionUtil.setCurrentConnectionStatus(NONE)
    performanceMetricsController.logPerformanceMetricsEvent(
      TEST_TIMESTAMP,
      SCREEN_NAME_UNSPECIFIED,
      apkSizeTestLoggableMetric,
      LOW_PRIORITY
    )

    val metricLogsProvider = performanceMetricsController.getMetricLogStore()

    val uploadedMetricLog = fakePerformanceMetricsEventLogger.getMostRecentPerformanceMetricsEvent()
    val cachedMetricLog =
      monitorFactory.waitForNextSuccessfulResult(metricLogsProvider).getOppiaMetricLog(0)

    assertThat(uploadedMetricLog.timestampMillis).isEqualTo(TEST_TIMESTAMP)
    assertThat(uploadedMetricLog.priority).isEqualTo(HIGH_PRIORITY)
    assertThat(uploadedMetricLog.loggableMetric.loggableMetricTypeCase).isEqualTo(CPU_USAGE_METRIC)

    assertThat(cachedMetricLog.timestampMillis).isEqualTo(TEST_TIMESTAMP)
    assertThat(cachedMetricLog.priority).isEqualTo(LOW_PRIORITY)
    assertThat(cachedMetricLog.loggableMetric.loggableMetricTypeCase).isEqualTo(APK_SIZE_METRIC)
  }

  @Test
  fun testController_logPerformanceMetrics_exceedLimit_withNoNetwork_checkCorrectEventIsEvicted() {
    networkConnectionUtil.setCurrentConnectionStatus(NONE)
    logMultiplePerformanceMetrics()

    val metricLogsProvider = performanceMetricsController.getMetricLogStore()

    val metricLogs = monitorFactory.waitForNextSuccessfulResult(metricLogsProvider)
    val firstMetricLog = metricLogs.getOppiaMetricLog(0)
    val secondMetricLog = metricLogs.getOppiaMetricLog(1)
    assertThat(metricLogs.oppiaMetricLogList).hasSize(2)
    // In this case, 1 LOW, 1 HIGH and 2 MEDIUM priority metric logs were logged. So while pruning,
    // only records with HIGH and MEDIUM priority should be retained.
    assertThat(firstMetricLog.priority).isEqualTo(HIGH_PRIORITY)
    assertThat(secondMetricLog.priority).isEqualTo(HIGH_PRIORITY)
    // If we analyse the implementation of logMultiplePerformanceMetrics(), we can see that record
    // pruning will begin from the logging of the third record. At first, the first event log will
    // be removed as it has LOW priority and the event logged at the third place will become the
    // event record at the second place in the store. When the forth event gets logged then the
    // pruning will again happen on the basis of priority as MEDIUM priority event will be evicted.
    // Now, when the fifth event gets logged then pruning happen based on purely based on timestamp
    // of the event as both event logs have HIGH priority. As the second event's timestamp was
    // lesser than that of the fourth event, it will be pruned from the store and the fifth event
    // will become the second event in the store.
    assertThat(firstMetricLog.timestampMillis).isEqualTo(1556093110000)
    assertThat(secondMetricLog.timestampMillis).isEqualTo(1556094110000)
  }

  @Test
  fun testController_setAppInForeground_getIsAppInForeground_returnsCorrectValue() {
    performanceMetricsController.setAppInForeground()

    val isAppInForeground = performanceMetricsController.getIsAppInForeground()

    assertThat(isAppInForeground).isTrue()
  }

  @Test
  fun testController_setAppInForeground_logMetric_logsMetricWithAppInForeground() {
    performanceMetricsController.setAppInForeground()
    performanceMetricsController.logPerformanceMetricsEvent(
      TEST_TIMESTAMP,
      SCREEN_NAME_UNSPECIFIED,
      apkSizeTestLoggableMetric,
      LOW_PRIORITY
    )

    val performanceMetricsLog =
      fakePerformanceMetricsEventLogger.getMostRecentPerformanceMetricsEvent()

    assertThat(performanceMetricsLog.timestampMillis).isEqualTo(TEST_TIMESTAMP)
    assertThat(performanceMetricsLog.currentScreen).isEqualTo(SCREEN_NAME_UNSPECIFIED)
    assertThat(performanceMetricsLog.priority).isEqualTo(LOW_PRIORITY)
    assertThat(performanceMetricsLog.loggableMetric.loggableMetricTypeCase).isEqualTo(
      APK_SIZE_METRIC
    )
    assertThat(performanceMetricsLog.isAppInForeground).isTrue()
  }

  @Test
  fun testController_setAppInBackground_getIsAppInForeground_returnsCorrectValue() {
    performanceMetricsController.setAppInBackground()

    val isAppInForeground = performanceMetricsController.getIsAppInForeground()

    assertThat(isAppInForeground).isFalse()
  }

  @Test
  fun testController_setAppInBackground_logMetric_logsMetricWithAppInBackground() {
    performanceMetricsController.setAppInBackground()
    performanceMetricsController.logPerformanceMetricsEvent(
      TEST_TIMESTAMP,
      SCREEN_NAME_UNSPECIFIED,
      apkSizeTestLoggableMetric,
      LOW_PRIORITY
    )

    val performanceMetricsLog =
      fakePerformanceMetricsEventLogger.getMostRecentPerformanceMetricsEvent()

    assertThat(performanceMetricsLog.timestampMillis).isEqualTo(TEST_TIMESTAMP)
    assertThat(performanceMetricsLog.currentScreen).isEqualTo(SCREEN_NAME_UNSPECIFIED)
    assertThat(performanceMetricsLog.priority).isEqualTo(LOW_PRIORITY)
    assertThat(performanceMetricsLog.loggableMetric.loggableMetricTypeCase).isEqualTo(
      APK_SIZE_METRIC
    )
    assertThat(performanceMetricsLog.isAppInForeground).isFalse()
  }

  private fun logMultiplePerformanceMetrics() {
    performanceMetricsController.logPerformanceMetricsEvent(
      1556094120000,
      SCREEN_NAME_UNSPECIFIED,
      apkSizeTestLoggableMetric,
      LOW_PRIORITY
    )

    performanceMetricsController.logPerformanceMetricsEvent(
      1556090110000,
      SCREEN_NAME_UNSPECIFIED,
      apkSizeTestLoggableMetric,
      HIGH_PRIORITY
    )

    performanceMetricsController.logPerformanceMetricsEvent(
      1556092100000,
      SCREEN_NAME_UNSPECIFIED,
      apkSizeTestLoggableMetric,
      MEDIUM_PRIORITY
    )

    performanceMetricsController.logPerformanceMetricsEvent(
      1556093110000,
      SCREEN_NAME_UNSPECIFIED,
      apkSizeTestLoggableMetric,
      HIGH_PRIORITY
    )

    performanceMetricsController.logPerformanceMetricsEvent(
      1556094110000,
      SCREEN_NAME_UNSPECIFIED,
      apkSizeTestLoggableMetric,
      HIGH_PRIORITY
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

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(
    modules = [
      TestModule::class, TestLogReportingModule::class, RobolectricModule::class,
      TestDispatcherModule::class, TestLogStorageModule::class,
      NetworkConnectionUtilDebugModule::class, LocaleProdModule::class, FakeOppiaClockModule::class,
      PlatformParameterModule::class, PlatformParameterSingletonModule::class,
      LoggingIdentifierModule::class, SyncStatusTestModule::class, MetricLogSchedulerModule::class
    ]
  )
  interface TestApplicationComponent : DataProvidersInjector {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder
      fun build(): TestApplicationComponent
    }

    fun inject(performanceMetricsControllerTest: PerformanceMetricsControllerTest)
  }

  class TestApplication : Application(), DataProvidersInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerPerformanceMetricsControllerTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(performanceMetricsControllerTest: PerformanceMetricsControllerTest) {
      component.inject(performanceMetricsControllerTest)
    }

    override fun getDataProvidersInjector(): DataProvidersInjector = component
  }
}
