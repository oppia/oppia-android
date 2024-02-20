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
import org.oppia.android.app.model.EventLog
import org.oppia.android.app.model.EventLog.Priority
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.logging.AnalyticsEventLogger
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [FakeAnalyticsEventLogger]. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(manifest = Config.NONE)
class FakeAnalyticsEventLoggerTest {

  @Inject lateinit var fakeAnalyticsEventLogger: FakeAnalyticsEventLogger
  @Inject lateinit var analyticsEventLogger: AnalyticsEventLogger

  private val eventLog1 = EventLog.newBuilder().setPriority(Priority.ESSENTIAL).build()
  private val eventLog2 = EventLog.newBuilder().setPriority(Priority.OPTIONAL).build()

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testFakeEventLogger_logEvent_returnsEvent() {
    analyticsEventLogger.logEvent(eventLog1)
    val event = fakeAnalyticsEventLogger.getMostRecentEvent()

    assertThat(event).isEqualTo(eventLog1)
    assertThat(event.priority).isEqualTo(Priority.ESSENTIAL)
  }

  @Test
  fun testFakeEventLogger_logEventTwice_returnsLatestEvent() {
    analyticsEventLogger.logEvent(eventLog1)
    analyticsEventLogger.logEvent(eventLog2)
    val event = fakeAnalyticsEventLogger.getMostRecentEvent()

    assertThat(event).isEqualTo(eventLog2)
    assertThat(event.priority).isEqualTo(Priority.OPTIONAL)
  }

  @Test
  fun testFakeEventLogger_logEvent_clearAllEvents_logEventAgain_returnsLatestEvent() {
    analyticsEventLogger.logEvent(eventLog1)
    fakeAnalyticsEventLogger.clearAllEvents()
    analyticsEventLogger.logEvent(eventLog2)
    val event = fakeAnalyticsEventLogger.getMostRecentEvent()

    assertThat(event).isEqualTo(eventLog2)
    assertThat(event.priority).isEqualTo(Priority.OPTIONAL)
  }

  @Test
  fun testFakeEventLogger_logNothing_getMostRecent_returnsFailure() {
    assertThrows<NoSuchElementException>() { fakeAnalyticsEventLogger.getMostRecentEvent() }
  }

  @Test
  fun testFakeEventLogger_logEvent_clearAllEvents_getMostRecent_returnsFailure() {
    analyticsEventLogger.logEvent(eventLog1)
    fakeAnalyticsEventLogger.clearAllEvents()

    val eventException = assertThrows<NoSuchElementException>() {
      fakeAnalyticsEventLogger.getMostRecentEvent()
    }

    assertThat(eventException).isInstanceOf(NoSuchElementException::class.java)
  }

  @Test
  fun testFakeEventLogger_clearAllEvents_returnsEmptyList() {
    fakeAnalyticsEventLogger.clearAllEvents()
    val isListEmpty = fakeAnalyticsEventLogger.noEventsPresent()

    assertThat(isListEmpty).isTrue()
  }

  @Test
  fun testFakeEventLogger_logEvent_clearAllEvents_returnsEmptyList() {
    analyticsEventLogger.logEvent(eventLog1)
    fakeAnalyticsEventLogger.clearAllEvents()
    val isListEmpty = fakeAnalyticsEventLogger.noEventsPresent()

    assertThat(isListEmpty).isTrue()
  }

  @Test
  fun testFakeEventLogger_logMultipleEvents_clearAllEvents_returnsEmptyList() {
    analyticsEventLogger.logEvent(eventLog1)
    analyticsEventLogger.logEvent(eventLog2)
    fakeAnalyticsEventLogger.clearAllEvents()
    val isListEmpty = fakeAnalyticsEventLogger.noEventsPresent()

    assertThat(isListEmpty).isTrue()
  }

  @Test
  fun testFakeEventLogger_logEvent_returnsNonEmptyList() {
    analyticsEventLogger.logEvent(eventLog1)
    val isListEmpty = fakeAnalyticsEventLogger.noEventsPresent()

    assertThat(isListEmpty).isFalse()
  }

  @Test
  fun testFakeEventLogger_logMultipleEvents_returnsNonEmptyList() {
    analyticsEventLogger.logEvent(eventLog1)
    analyticsEventLogger.logEvent(eventLog2)

    val eventLogStatus1 = fakeAnalyticsEventLogger.hasEventLogged { it == eventLog1 }
    val eventLogStatus2 = fakeAnalyticsEventLogger.hasEventLogged { it == eventLog2 }
    val eventListStatus = fakeAnalyticsEventLogger.noEventsPresent()

    assertThat(eventListStatus).isFalse()
    assertThat(eventLogStatus1).isTrue()
    assertThat(eventLogStatus2).isTrue()
  }

  @Test
  fun testGetOldestEvent_noEventsLogged_throwsException() {
    assertThrows<NoSuchElementException>() { fakeAnalyticsEventLogger.getOldestEvent() }
  }

  @Test
  fun testGetOldestEvent_oneEventLogged_returnsLoggedEvent() {
    analyticsEventLogger.logEvent(eventLog1)

    val oldestEvent = fakeAnalyticsEventLogger.getOldestEvent()

    assertThat(oldestEvent).isEqualTo(eventLog1)
  }

  @Test
  fun testGetOldestEvent_twoEventsLogged_returnsFirstEventLogged() {
    analyticsEventLogger.logEvent(eventLog2)
    analyticsEventLogger.logEvent(eventLog1)

    val oldestEvent = fakeAnalyticsEventLogger.getOldestEvent()

    assertThat(oldestEvent).isEqualTo(eventLog2)
  }

  @Test
  fun testGetOldestEvent_twoEventsLogged_clearEvents_throwsException() {
    analyticsEventLogger.logEvent(eventLog2)
    analyticsEventLogger.logEvent(eventLog1)
    fakeAnalyticsEventLogger.clearAllEvents()

    assertThrows<NoSuchElementException>() { fakeAnalyticsEventLogger.getOldestEvent() }
  }

  @Test
  fun testGetOldestEvent_eventLogged_cleared_newEventLogged_returnsLatestEventLog() {
    analyticsEventLogger.logEvent(eventLog2)
    fakeAnalyticsEventLogger.clearAllEvents()
    analyticsEventLogger.logEvent(eventLog1)

    val oldestEvent = fakeAnalyticsEventLogger.getOldestEvent()

    assertThat(oldestEvent).isEqualTo(eventLog1)
  }

  @Test
  fun testGetMostRecentEvents_twoEvents_noEventsLogged_returnsEmptyList() {
    val mostRecentEvents = fakeAnalyticsEventLogger.getMostRecentEvents(count = 2)

    assertThat(mostRecentEvents).isEmpty()
  }

  @Test
  fun testGetMostRecentEvents_twoEvents_oneEventLogged_returnsOneItemList() {
    analyticsEventLogger.logEvent(eventLog1)

    val mostRecentEvents = fakeAnalyticsEventLogger.getMostRecentEvents(count = 2)

    assertThat(mostRecentEvents).containsExactly(eventLog1)
  }

  @Test
  fun testGetMostRecentEvents_twoEvents_twoEventsLogged_returnsEventsInOrder() {
    analyticsEventLogger.logEvent(eventLog2)
    analyticsEventLogger.logEvent(eventLog1)

    val mostRecentEvents = fakeAnalyticsEventLogger.getMostRecentEvents(count = 2)

    assertThat(mostRecentEvents).containsExactly(eventLog2, eventLog1).inOrder()
  }

  @Test
  fun testGetMostRecentEvents_oneEvent_twoEventsLogged_returnsSingleLatestEvent() {
    analyticsEventLogger.logEvent(eventLog2)
    analyticsEventLogger.logEvent(eventLog1)

    val mostRecentEvents = fakeAnalyticsEventLogger.getMostRecentEvents(count = 1)

    assertThat(mostRecentEvents).containsExactly(eventLog1)
  }

  @Test
  fun testGetMostRecentEvents_zeroEvents_twoEventsLogged_returnsEmptyList() {
    analyticsEventLogger.logEvent(eventLog2)
    analyticsEventLogger.logEvent(eventLog1)

    val mostRecentEvents = fakeAnalyticsEventLogger.getMostRecentEvents(count = 0)

    assertThat(mostRecentEvents).isEmpty()
  }

  @Test
  fun testGetMostRecentEvents_negativeEvents_twoEventsLogged_throwsException() {
    analyticsEventLogger.logEvent(eventLog2)
    analyticsEventLogger.logEvent(eventLog1)

    assertThrows<IllegalArgumentException>() {
      fakeAnalyticsEventLogger.getMostRecentEvents(count = -1)
    }
  }

  @Test
  fun testGetMostRecentEvents_twoEventsLogged_eventsCleared_returnsEmptyList() {
    analyticsEventLogger.logEvent(eventLog2)
    analyticsEventLogger.logEvent(eventLog1)
    fakeAnalyticsEventLogger.clearAllEvents()

    val mostRecentEvents = fakeAnalyticsEventLogger.getMostRecentEvents(count = 2)

    assertThat(mostRecentEvents).isEmpty()
  }

  @Test
  fun testGetMostRecentEvents_eventLogged_cleared_newEventLogged_returnsNewestEvent() {
    analyticsEventLogger.logEvent(eventLog1)
    fakeAnalyticsEventLogger.clearAllEvents()
    analyticsEventLogger.logEvent(eventLog2)

    val mostRecentEvents = fakeAnalyticsEventLogger.getMostRecentEvents(count = 2)

    assertThat(mostRecentEvents).containsExactly(eventLog2)
  }

  private fun setUpTestApplicationComponent() {
    DaggerFakeAnalyticsEventLoggerTest_TestApplicationComponent.builder()
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

    fun inject(fakeEventLoggerTest: FakeAnalyticsEventLoggerTest)
  }
}
