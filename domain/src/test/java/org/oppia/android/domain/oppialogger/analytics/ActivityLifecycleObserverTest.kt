package org.oppia.android.domain.oppialogger.analytics

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
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
import org.oppia.android.app.model.OppiaMetricLog.LoggableMetric.LoggableMetricTypeCase.APK_SIZE_METRIC
import org.oppia.android.app.model.OppiaMetricLog.LoggableMetric.LoggableMetricTypeCase.MEMORY_USAGE_METRIC
import org.oppia.android.app.model.OppiaMetricLog.LoggableMetric.LoggableMetricTypeCase.STARTUP_LATENCY_METRIC
import org.oppia.android.app.model.OppiaMetricLog.LoggableMetric.LoggableMetricTypeCase.STORAGE_USAGE_METRIC
import org.oppia.android.app.model.ScreenName
import org.oppia.android.domain.oppialogger.ApplicationIdSeed
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.domain.platformparameter.PlatformParameterSingletonModule
import org.oppia.android.testing.FakePerformanceMetricsEventLogger
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClock
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.data.DataProvidersInjector
import org.oppia.android.util.data.DataProvidersInjectorProvider
import org.oppia.android.util.extensions.putProtoExtra
import org.oppia.android.util.locale.LocaleProdModule
import org.oppia.android.util.logging.CurrentAppScreenNameIntentDecorator
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
import org.robolectric.Robolectric
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

private const val TEST_TIMESTAMP_IN_MILLIS_ONE = 1556094000000
private const val TEST_TIMESTAMP_IN_MILLIS_TWO = 1556094100000

/** Tests for [ActivityLifecycleObserver]. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = ActivityLifecycleObserverTest.TestApplication::class)
class ActivityLifecycleObserverTest {

  @Inject
  lateinit var activityLifecycleObserver: ActivityLifecycleObserver

  @Inject
  lateinit var fakePerformanceMetricsEventLogger: FakePerformanceMetricsEventLogger

  @Inject
  lateinit var fakeOppiaClock: FakeOppiaClock

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_FIXED_FAKE_TIME)
    fakeOppiaClock.setCurrentTimeMs(TEST_TIMESTAMP_IN_MILLIS_ONE)
  }

  @Test
  fun testObserver_getCurrentScreen_verifyInitialValueIsUnspecified() {
    assertThat(activityLifecycleObserver.getCurrentScreen())
      .isEqualTo(ScreenName.ACTIVITY_NAME_UNSPECIFIED.name)
  }

  @Test
  fun testObserver_onCreate_verifyPerformanceMetricsLoggingWithCorrectDetails() {
    activityLifecycleObserver.onCreate()

    val loggedMetrics = fakePerformanceMetricsEventLogger.getMostRecentPerformanceMetricsEvents(2)
    assertThat(loggedMetrics[0].loggableMetric.loggableMetricTypeCase).isEqualTo(APK_SIZE_METRIC)
    assertThat(loggedMetrics[1].loggableMetric.loggableMetricTypeCase).isEqualTo(
      STORAGE_USAGE_METRIC
    )
    assertThat(loggedMetrics[0].timestampMillis).isEqualTo(TEST_TIMESTAMP_IN_MILLIS_ONE)
    assertThat(loggedMetrics[1].timestampMillis).isEqualTo(TEST_TIMESTAMP_IN_MILLIS_ONE)
  }

  @Test
  fun testObserver_onFirstActivityResume_verifyCurrentScreenReturnsCorrectValue() {
    val activity = Robolectric.buildActivity(Activity::class.java).get()
    activity.intent = Intent().apply {
      this.putProtoExtra(
        CurrentAppScreenNameIntentDecorator.getCurrentAppScreenNameIntentKey(),
        CurrentAppScreenNameIntentDecorator.decorateWithScreenName(ScreenName.ACTIVITY_NAME_UNSPECIFIED)
      )
    }
    activityLifecycleObserver.onActivityResumed(activity)

    val currentScreenValue = activityLifecycleObserver.getCurrentScreen()
    assertThat(currentScreenValue).isEqualTo(ScreenName.ACTIVITY_NAME_UNSPECIFIED.name)
  }

  @Test
  fun testObserver_onFirstActivityResume_verifyLogsStartupLatency() {
    activityLifecycleObserver.onCreate()
    fakeOppiaClock.setCurrentTimeMs(TEST_TIMESTAMP_IN_MILLIS_TWO)
    val activity = Robolectric.buildActivity(Activity::class.java).get()
    val expectedStartupLatency = TEST_TIMESTAMP_IN_MILLIS_TWO - TEST_TIMESTAMP_IN_MILLIS_ONE

    activityLifecycleObserver.onActivityResumed(activity)
    val currentScreen = activityLifecycleObserver.getCurrentScreen()
    val startupLatencyEvents =
      fakePerformanceMetricsEventLogger.getMostRecentPerformanceMetricsEvents(3)

    val startupLatencyEvent = startupLatencyEvents[1]

    assertThat(startupLatencyEvent.loggableMetric.loggableMetricTypeCase).isEqualTo(
      STARTUP_LATENCY_METRIC
    )
    assertThat(startupLatencyEvent.timestampMillis).isEqualTo(TEST_TIMESTAMP_IN_MILLIS_TWO)
    assertThat(startupLatencyEvent.currentScreen).isEqualTo(currentScreen)
    assertThat(startupLatencyEvent.loggableMetric.startupLatencyMetric.startupLatencyMillis)
      .isEqualTo(expectedStartupLatency)
  }

  @Test
  fun testObserver_onSecondActivityResume_verifyStartupLatencyIsLoggedOnce() {
    activityLifecycleObserver.onCreate()
    fakeOppiaClock.setCurrentTimeMs(TEST_TIMESTAMP_IN_MILLIS_TWO)
    val activity = Robolectric.buildActivity(Activity::class.java).get()
    activityLifecycleObserver.onActivityResumed(activity)
    activityLifecycleObserver.onActivityResumed(activity)

    val loggedEvents = fakePerformanceMetricsEventLogger.getMostRecentPerformanceMetricsEvents(
      fakePerformanceMetricsEventLogger.getPerformanceMetricsEventListCount()
    )

    assertThat(loggedEvents.size).isEqualTo(5)
    assertThat(loggedEvents[0].loggableMetric.loggableMetricTypeCase).isEqualTo(APK_SIZE_METRIC)
    assertThat(loggedEvents[1].loggableMetric.loggableMetricTypeCase)
      .isEqualTo(STORAGE_USAGE_METRIC)
    assertThat(loggedEvents[2].loggableMetric.loggableMetricTypeCase)
      .isEqualTo(STARTUP_LATENCY_METRIC)
    assertThat(loggedEvents[3].loggableMetric.loggableMetricTypeCase).isEqualTo(MEMORY_USAGE_METRIC)
    assertThat(loggedEvents[4].loggableMetric.loggableMetricTypeCase).isEqualTo(MEMORY_USAGE_METRIC)
  }

  @Test
  fun testObserver_activityResumed_verifyLogsMemoryUsage() {
    val activity = Robolectric.buildActivity(Activity::class.java).get()
    activityLifecycleObserver.onActivityResumed(activity)

    val currentScreen = activityLifecycleObserver.getCurrentScreen()
    val memoryUsageEvent =
      fakePerformanceMetricsEventLogger.getMostRecentPerformanceMetricsEvent()

    assertThat(memoryUsageEvent.loggableMetric.loggableMetricTypeCase).isEqualTo(
      MEMORY_USAGE_METRIC
    )
    assertThat(memoryUsageEvent.timestampMillis).isEqualTo(TEST_TIMESTAMP_IN_MILLIS_ONE)
    assertThat(memoryUsageEvent.currentScreen).isEqualTo(currentScreen)
  }

  @Test
  fun testObserver_activityResumed_activityPaused_verifyCurrentScreenReturnsUnspecifiedValue() {
    val activity = Robolectric.buildActivity(Activity::class.java).get()
    activityLifecycleObserver.onActivityResumed(activity)
    activityLifecycleObserver.onActivityPaused(activity)
    val currentScreen = activityLifecycleObserver.getCurrentScreen()

    assertThat(currentScreen).isEqualTo(ScreenName.ACTIVITY_NAME_UNSPECIFIED.name)
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
  class TestLoggingIdentifierModule {
    companion object {
      const val applicationIdSeed = 1L
    }

    @Provides
    @ApplicationIdSeed
    fun provideApplicationIdSeed(): Long = applicationIdSeed
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
      TestModule::class, TestLogReportingModule::class, LogStorageModule::class,
      TestDispatcherModule::class, RobolectricModule::class, FakeOppiaClockModule::class,
      NetworkConnectionUtilDebugModule::class, LocaleProdModule::class,
      TestPlatformParameterModule::class, PlatformParameterSingletonModule::class,
      TestLoggingIdentifierModule::class, ApplicationLifecycleModule::class,
      SyncStatusModule::class
    ]
  )
  interface TestApplicationComponent : DataProvidersInjector {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder
      fun build(): TestApplicationComponent
    }

    fun inject(activityLifecycleObserverTest: ActivityLifecycleObserverTest)
  }

  class TestApplication : Application(), DataProvidersInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerActivityLifecycleObserverTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(activityLifecycleObserverTest: ActivityLifecycleObserverTest) {
      component.inject(activityLifecycleObserverTest)
    }

    override fun getDataProvidersInjector(): DataProvidersInjector = component
  }
}
