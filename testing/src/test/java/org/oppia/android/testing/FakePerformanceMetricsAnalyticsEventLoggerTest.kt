package org.oppia.android.testing

import android.app.Application
import android.content.Context
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
import org.oppia.android.app.model.OppiaMetricLog
import org.oppia.android.app.model.OppiaMetricLog.Priority.HIGH_PRIORITY
import org.oppia.android.app.model.OppiaMetricLog.Priority.MEDIUM_PRIORITY
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.logging.performancemetrics.PerformanceMetricsEventLogger
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [FakePerformanceMetricsAnalyticsEventLogger]. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(manifest = Config.NONE)
class FakePerformanceMetricsAnalyticsEventLoggerTest {

  @Inject
  lateinit var fakePerformanceMetricsAnalyticsEventLogger:
    FakePerformanceMetricsAnalyticsEventLogger

  @Inject
  lateinit var performanceMetricsEventLogger: PerformanceMetricsEventLogger

  private val metricLog1 = OppiaMetricLog.newBuilder().setPriority(HIGH_PRIORITY).build()
  private val metricLog2 = OppiaMetricLog.newBuilder().setPriority(MEDIUM_PRIORITY).build()

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testFakeMetricsEventLogger_logPerformanceMetric_returnsMetricEvent() {
    performanceMetricsEventLogger.logPerformanceMetric(metricLog1)
    val event = fakePerformanceMetricsAnalyticsEventLogger.getMostRecentPerformanceMetricsEvent()

    assertThat(event).isEqualTo(metricLog1)
    assertThat(event.priority).isEqualTo(HIGH_PRIORITY)
  }

  @Test
  fun testFakeMetricsEventLogger_logMetricEventTwice_returnsLatestEvent() {
    performanceMetricsEventLogger.logPerformanceMetric(metricLog1)
    performanceMetricsEventLogger.logPerformanceMetric(metricLog2)
    val event = fakePerformanceMetricsAnalyticsEventLogger.getMostRecentPerformanceMetricsEvent()

    assertThat(event).isEqualTo(metricLog2)
    assertThat(event.priority).isEqualTo(MEDIUM_PRIORITY)
  }

  @Test
  fun testFakeMetricsEventLogger_logEvent_clearAllEvents_logEventAgain_returnsLatestEvent() {
    performanceMetricsEventLogger.logPerformanceMetric(metricLog1)
    fakePerformanceMetricsAnalyticsEventLogger.clearAllPerformanceMetricsEvents()
    performanceMetricsEventLogger.logPerformanceMetric(metricLog2)
    val event = fakePerformanceMetricsAnalyticsEventLogger.getMostRecentPerformanceMetricsEvent()

    assertThat(event).isEqualTo(metricLog2)
    assertThat(event.priority).isEqualTo(MEDIUM_PRIORITY)
  }

  @Test
  fun testFakeMetricsEventLogger_logNothing_getMostRecent_returnsFailure() {
    assertThrows(NoSuchElementException::class) {
      fakePerformanceMetricsAnalyticsEventLogger.getMostRecentPerformanceMetricsEvent()
    }
  }

  @Test
  fun testFakeMetricsEventLogger_logEvent_clearAllEvents_getMostRecent_returnsFailure() {
    performanceMetricsEventLogger.logPerformanceMetric(metricLog1)
    fakePerformanceMetricsAnalyticsEventLogger.clearAllPerformanceMetricsEvents()

    assertThrows(NoSuchElementException::class) {
      fakePerformanceMetricsAnalyticsEventLogger.getMostRecentPerformanceMetricsEvent()
    }
  }

  @Test
  fun testFakeMetricsEventLogger_clearAllEvents_returnsEmptyList() {
    fakePerformanceMetricsAnalyticsEventLogger.clearAllPerformanceMetricsEvents()
    val isListEmpty = fakePerformanceMetricsAnalyticsEventLogger.noPerformanceMetricsEventsPresent()

    assertThat(isListEmpty).isTrue()
  }

  @Test
  fun testFakeMetricsEventLogger_logEvent_clearAllEvents_returnsEmptyList() {
    performanceMetricsEventLogger.logPerformanceMetric(metricLog1)
    fakePerformanceMetricsAnalyticsEventLogger.clearAllPerformanceMetricsEvents()
    val isListEmpty = fakePerformanceMetricsAnalyticsEventLogger.noPerformanceMetricsEventsPresent()

    assertThat(isListEmpty).isTrue()
  }

  @Test
  fun testFakeMetricsEventLogger_logMultipleEvents_clearAllEvents_returnsEmptyList() {
    performanceMetricsEventLogger.logPerformanceMetric(metricLog1)
    performanceMetricsEventLogger.logPerformanceMetric(metricLog2)
    fakePerformanceMetricsAnalyticsEventLogger.clearAllPerformanceMetricsEvents()
    val isListEmpty = fakePerformanceMetricsAnalyticsEventLogger.noPerformanceMetricsEventsPresent()

    assertThat(isListEmpty).isTrue()
  }

  @Test
  fun testFakeMetricsEventLogger_logEvent_returnsNonEmptyList() {
    performanceMetricsEventLogger.logPerformanceMetric(metricLog1)
    val isListEmpty = fakePerformanceMetricsAnalyticsEventLogger.noPerformanceMetricsEventsPresent()

    assertThat(isListEmpty).isFalse()
  }

  @Test
  fun testFakeMetricsEventLogger_logMultipleEvents_returnsNonEmptyList() {
    performanceMetricsEventLogger.logPerformanceMetric(metricLog1)
    performanceMetricsEventLogger.logPerformanceMetric(metricLog2)

    val eventLogStatus1 =
      fakePerformanceMetricsAnalyticsEventLogger.hasPerformanceMetricsEventLogged(
        metricLog1
      )
    val eventLogStatus2 =
      fakePerformanceMetricsAnalyticsEventLogger.hasPerformanceMetricsEventLogged(
        metricLog2
      )
    val eventListStatus =
      fakePerformanceMetricsAnalyticsEventLogger.noPerformanceMetricsEventsPresent()

    assertThat(eventListStatus).isFalse()
    assertThat(eventLogStatus1).isTrue()
    assertThat(eventLogStatus2).isTrue()
  }

  @Test
  fun testGetOldestEvent_noEventsLogged_throwsException() {
    assertThrows(NoSuchElementException::class) {
      fakePerformanceMetricsAnalyticsEventLogger.getOldestPerformanceMetricsEvent()
    }
  }

  @Test
  fun testGetOldestEvent_oneEventLogged_returnsLoggedEvent() {
    performanceMetricsEventLogger.logPerformanceMetric(metricLog1)

    val oldestEvent = fakePerformanceMetricsAnalyticsEventLogger.getOldestPerformanceMetricsEvent()

    assertThat(oldestEvent).isEqualTo(metricLog1)
  }

  @Test
  fun testGetOldestEvent_twoEventsLogged_returnsFirstEventLogged() {
    performanceMetricsEventLogger.logPerformanceMetric(metricLog2)
    performanceMetricsEventLogger.logPerformanceMetric(metricLog1)

    val oldestEvent = fakePerformanceMetricsAnalyticsEventLogger.getOldestPerformanceMetricsEvent()

    assertThat(oldestEvent).isEqualTo(metricLog2)
  }

  @Test
  fun testGetOldestEvent_twoEventsLogged_clearEvents_throwsException() {
    performanceMetricsEventLogger.logPerformanceMetric(metricLog2)
    performanceMetricsEventLogger.logPerformanceMetric(metricLog1)
    fakePerformanceMetricsAnalyticsEventLogger.clearAllPerformanceMetricsEvents()

    assertThrows(NoSuchElementException::class) {
      fakePerformanceMetricsAnalyticsEventLogger.getOldestPerformanceMetricsEvent()
    }
  }

  @Test
  fun testGetOldestEvent_eventLogged_cleared_newEventLogged_returnsLatestEventLog() {
    performanceMetricsEventLogger.logPerformanceMetric(metricLog2)
    fakePerformanceMetricsAnalyticsEventLogger.clearAllPerformanceMetricsEvents()
    performanceMetricsEventLogger.logPerformanceMetric(metricLog1)

    val oldestEvent =
      fakePerformanceMetricsAnalyticsEventLogger.getOldestPerformanceMetricsEvent()

    assertThat(oldestEvent).isEqualTo(metricLog1)
  }

  @Test
  fun testGetMostRecentEvents_twoEvents_noEventsLogged_returnsEmptyList() {
    val mostRecentEvents =
      fakePerformanceMetricsAnalyticsEventLogger.getMostRecentPerformanceMetricsEvents(
        count = 2
      )

    assertThat(mostRecentEvents).isEmpty()
  }

  @Test
  fun testGetMostRecentEvents_twoEvents_oneEventLogged_returnsOneItemList() {
    performanceMetricsEventLogger.logPerformanceMetric(metricLog1)

    val mostRecentEvents =
      fakePerformanceMetricsAnalyticsEventLogger.getMostRecentPerformanceMetricsEvents(
        count = 2
      )

    assertThat(mostRecentEvents).containsExactly(metricLog1)
  }

  @Test
  fun testGetMostRecentEvents_twoEvents_twoEventsLogged_returnsEventsInOrder() {
    performanceMetricsEventLogger.logPerformanceMetric(metricLog2)
    performanceMetricsEventLogger.logPerformanceMetric(metricLog1)

    val mostRecentEvents =
      fakePerformanceMetricsAnalyticsEventLogger.getMostRecentPerformanceMetricsEvents(
        count = 2
      )

    assertThat(mostRecentEvents).containsExactly(metricLog2, metricLog1).inOrder()
  }

  @Test
  fun testGetMostRecentEvents_oneEvent_twoEventsLogged_returnsSingleLatestEvent() {
    performanceMetricsEventLogger.logPerformanceMetric(metricLog2)
    performanceMetricsEventLogger.logPerformanceMetric(metricLog1)

    val mostRecentEvents =
      fakePerformanceMetricsAnalyticsEventLogger.getMostRecentPerformanceMetricsEvents(
        count = 1
      )

    assertThat(mostRecentEvents).containsExactly(metricLog1)
  }

  @Test
  fun testGetMostRecentEvents_zeroEvents_twoEventsLogged_returnsEmptyList() {
    performanceMetricsEventLogger.logPerformanceMetric(metricLog2)
    performanceMetricsEventLogger.logPerformanceMetric(metricLog1)

    val mostRecentEvents =
      fakePerformanceMetricsAnalyticsEventLogger.getMostRecentPerformanceMetricsEvents(
        count = 0
      )

    assertThat(mostRecentEvents).isEmpty()
  }

  @Test
  fun testGetMostRecentEvents_negativeEvents_twoEventsLogged_throwsException() {
    performanceMetricsEventLogger.logPerformanceMetric(metricLog2)
    performanceMetricsEventLogger.logPerformanceMetric(metricLog1)

    assertThrows(IllegalArgumentException::class) {
      fakePerformanceMetricsAnalyticsEventLogger.getMostRecentPerformanceMetricsEvents(count = -1)
    }
  }

  @Test
  fun testGetMostRecentEvents_twoEventsLogged_eventsCleared_returnsEmptyList() {
    performanceMetricsEventLogger.logPerformanceMetric(metricLog2)
    performanceMetricsEventLogger.logPerformanceMetric(metricLog1)
    fakePerformanceMetricsAnalyticsEventLogger.clearAllPerformanceMetricsEvents()

    val mostRecentEvents =
      fakePerformanceMetricsAnalyticsEventLogger.getMostRecentPerformanceMetricsEvents(
        count = 2
      )

    assertThat(mostRecentEvents).isEmpty()
  }

  @Test
  fun testGetMostRecentEvents_eventLogged_cleared_newEventLogged_returnsNewestEvent() {
    performanceMetricsEventLogger.logPerformanceMetric(metricLog1)
    fakePerformanceMetricsAnalyticsEventLogger.clearAllPerformanceMetricsEvents()
    performanceMetricsEventLogger.logPerformanceMetric(metricLog2)

    val mostRecentEvents =
      fakePerformanceMetricsAnalyticsEventLogger.getMostRecentPerformanceMetricsEvents(
        count = 2
      )

    assertThat(mostRecentEvents).containsExactly(metricLog2)
  }

  private fun setUpTestApplicationComponent() {
    DaggerFakePerformanceMetricsAnalyticsEventLoggerTest_TestApplicationComponent.builder()
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
  }

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(
    modules = [
      TestModule::class, TestLogReportingModule::class, RobolectricModule::class,
      TestDispatcherModule::class, LogStorageModule::class, FakeOppiaClockModule::class
    ]
  )
  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder
      fun build(): TestApplicationComponent
    }

    fun inject(
      fakePerformanceMetricsAnalyticsEventLoggerTest: FakePerformanceMetricsAnalyticsEventLoggerTest
    )
  }
}
