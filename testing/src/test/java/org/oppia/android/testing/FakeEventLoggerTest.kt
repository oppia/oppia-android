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
import org.oppia.android.util.logging.EventLogger
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [FakeEventLogger]. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(manifest = Config.NONE)
class FakeEventLoggerTest {

  @Inject lateinit var fakeEventLogger: FakeEventLogger
  @Inject lateinit var eventLogger: EventLogger

  private val eventLog1 = EventLog.newBuilder().setPriority(Priority.ESSENTIAL).build()
  private val eventLog2 = EventLog.newBuilder().setPriority(Priority.OPTIONAL).build()

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testFakeEventLogger_logEvent_returnsEvent() {
    eventLogger.logEvent(eventLog1)
    val event = fakeEventLogger.getMostRecentEvent()

    assertThat(event).isEqualTo(eventLog1)
    assertThat(event.priority).isEqualTo(Priority.ESSENTIAL)
  }

  @Test
  fun testFakeEventLogger_logEventTwice_returnsLatestEvent() {
    eventLogger.logEvent(eventLog1)
    eventLogger.logEvent(eventLog2)
    val event = fakeEventLogger.getMostRecentEvent()

    assertThat(event).isEqualTo(eventLog2)
    assertThat(event.priority).isEqualTo(Priority.OPTIONAL)
  }

  @Test
  fun testFakeEventLogger_logEvent_clearAllEvents_logEventAgain_returnsLatestEvent() {
    eventLogger.logEvent(eventLog1)
    fakeEventLogger.clearAllEvents()
    eventLogger.logEvent(eventLog2)
    val event = fakeEventLogger.getMostRecentEvent()

    assertThat(event).isEqualTo(eventLog2)
    assertThat(event.priority).isEqualTo(Priority.OPTIONAL)
  }

  @Test
  fun testFakeEventLogger_logNothing_getMostRecent_returnsFailure() {
    assertThrows(NoSuchElementException::class) { fakeEventLogger.getMostRecentEvent() }
  }

  @Test
  fun testFakeEventLogger_logEvent_clearAllEvents_getMostRecent_returnsFailure() {
    eventLogger.logEvent(eventLog1)
    fakeEventLogger.clearAllEvents()

    val eventException = assertThrows(NoSuchElementException::class) {
      fakeEventLogger.getMostRecentEvent()
    }

    assertThat(eventException).isInstanceOf(NoSuchElementException::class.java)
  }

  @Test
  fun testFakeEventLogger_clearAllEvents_returnsEmptyList() {
    fakeEventLogger.clearAllEvents()
    val isListEmpty = fakeEventLogger.noEventsPresent()

    assertThat(isListEmpty).isTrue()
  }

  @Test
  fun testFakeEventLogger_logEvent_clearAllEvents_returnsEmptyList() {
    eventLogger.logEvent(eventLog1)
    fakeEventLogger.clearAllEvents()
    val isListEmpty = fakeEventLogger.noEventsPresent()

    assertThat(isListEmpty).isTrue()
  }

  @Test
  fun testFakeEventLogger_logMultipleEvents_clearAllEvents_returnsEmptyList() {
    eventLogger.logEvent(eventLog1)
    eventLogger.logEvent(eventLog2)
    fakeEventLogger.clearAllEvents()
    val isListEmpty = fakeEventLogger.noEventsPresent()

    assertThat(isListEmpty).isTrue()
  }

  @Test
  fun testFakeEventLogger_logEvent_returnsNonEmptyList() {
    eventLogger.logEvent(eventLog1)
    val isListEmpty = fakeEventLogger.noEventsPresent()

    assertThat(isListEmpty).isFalse()
  }

  @Test
  fun testFakeEventLogger_logMultipleEvents_returnsNonEmptyList() {
    eventLogger.logEvent(eventLog1)
    eventLogger.logEvent(eventLog2)

    val eventLogStatus1 = fakeEventLogger.hasEventLogged(eventLog1)
    val eventLogStatus2 = fakeEventLogger.hasEventLogged(eventLog2)
    val eventListStatus = fakeEventLogger.noEventsPresent()

    assertThat(eventListStatus).isFalse()
    assertThat(eventLogStatus1).isTrue()
    assertThat(eventLogStatus2).isTrue()
  }

  @Test
  fun testGetOldestEvent_noEventsLogged_throwsException() {
    assertThrows(NoSuchElementException::class) { fakeEventLogger.getOldestEvent() }
  }

  @Test
  fun testGetOldestEvent_oneEventLogged_returnsLoggedEvent() {
    eventLogger.logEvent(eventLog1)

    val oldestEvent = fakeEventLogger.getOldestEvent()

    assertThat(oldestEvent).isEqualTo(eventLog1)
  }

  @Test
  fun testGetOldestEvent_twoEventsLogged_returnsFirstEventLogged() {
    eventLogger.logEvent(eventLog2)
    eventLogger.logEvent(eventLog1)

    val oldestEvent = fakeEventLogger.getOldestEvent()

    assertThat(oldestEvent).isEqualTo(eventLog2)
  }

  @Test
  fun testGetOldestEvent_twoEventsLogged_clearEvents_throwsException() {
    eventLogger.logEvent(eventLog2)
    eventLogger.logEvent(eventLog1)
    fakeEventLogger.clearAllEvents()

    assertThrows(NoSuchElementException::class) { fakeEventLogger.getOldestEvent() }
  }

  @Test
  fun testGetOldestEvent_eventLogged_cleared_newEventLogged_returnsLatestEventLog() {
    eventLogger.logEvent(eventLog2)
    fakeEventLogger.clearAllEvents()
    eventLogger.logEvent(eventLog1)

    val oldestEvent = fakeEventLogger.getOldestEvent()

    assertThat(oldestEvent).isEqualTo(eventLog1)
  }

  @Test
  fun testGetMostRecentEvents_twoEvents_noEventsLogged_returnsEmptyList() {
    val mostRecentEvents = fakeEventLogger.getMostRecentEvents(count = 2)

    assertThat(mostRecentEvents).isEmpty()
  }

  @Test
  fun testGetMostRecentEvents_twoEvents_oneEventLogged_returnsOneItemList() {
    eventLogger.logEvent(eventLog1)

    val mostRecentEvents = fakeEventLogger.getMostRecentEvents(count = 2)

    assertThat(mostRecentEvents).containsExactly(eventLog1)
  }

  @Test
  fun testGetMostRecentEvents_twoEvents_twoEventsLogged_returnsEventsInOrder() {
    eventLogger.logEvent(eventLog2)
    eventLogger.logEvent(eventLog1)

    val mostRecentEvents = fakeEventLogger.getMostRecentEvents(count = 2)

    assertThat(mostRecentEvents).containsExactly(eventLog2, eventLog1).inOrder()
  }

  @Test
  fun testGetMostRecentEvents_oneEvent_twoEventsLogged_returnsSingleLatestEvent() {
    eventLogger.logEvent(eventLog2)
    eventLogger.logEvent(eventLog1)

    val mostRecentEvents = fakeEventLogger.getMostRecentEvents(count = 1)

    assertThat(mostRecentEvents).containsExactly(eventLog1)
  }

  @Test
  fun testGetMostRecentEvents_zeroEvents_twoEventsLogged_returnsEmptyList() {
    eventLogger.logEvent(eventLog2)
    eventLogger.logEvent(eventLog1)

    val mostRecentEvents = fakeEventLogger.getMostRecentEvents(count = 0)

    assertThat(mostRecentEvents).isEmpty()
  }

  @Test
  fun testGetMostRecentEvents_negativeEvents_twoEventsLogged_throwsException() {
    eventLogger.logEvent(eventLog2)
    eventLogger.logEvent(eventLog1)

    assertThrows(IllegalArgumentException::class) {
      fakeEventLogger.getMostRecentEvents(count = -1)
    }
  }

  @Test
  fun testGetMostRecentEvents_twoEventsLogged_eventsCleared_returnsEmptyList() {
    eventLogger.logEvent(eventLog2)
    eventLogger.logEvent(eventLog1)
    fakeEventLogger.clearAllEvents()

    val mostRecentEvents = fakeEventLogger.getMostRecentEvents(count = 2)

    assertThat(mostRecentEvents).isEmpty()
  }

  @Test
  fun testGetMostRecentEvents_eventLogged_cleared_newEventLogged_returnsNewestEvent() {
    eventLogger.logEvent(eventLog1)
    fakeEventLogger.clearAllEvents()
    eventLogger.logEvent(eventLog2)

    val mostRecentEvents = fakeEventLogger.getMostRecentEvents(count = 2)

    assertThat(mostRecentEvents).containsExactly(eventLog2)
  }

  private fun setUpTestApplicationComponent() {
    DaggerFakeEventLoggerTest_TestApplicationComponent.builder()
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

    fun inject(fakeEventLoggerTest: FakeEventLoggerTest)
  }
}
