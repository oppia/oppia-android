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
import org.oppia.android.util.logging.firebase.FirestoreEventLogger
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [fakeEventLogger]. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(manifest = Config.NONE)
class FakeFirestoreEventLoggerTest {

  @Inject
  lateinit var fakeEventLogger: FakeFirestoreEventLogger

  @Inject
  lateinit var eventLogger: FirestoreEventLogger

  private val eventLog1 = EventLog.newBuilder().setPriority(Priority.ESSENTIAL).build()
  private val eventLog2 = EventLog.newBuilder().setPriority(Priority.OPTIONAL).build()

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testfakeEventLogger_logEvent_returnsEvent() {
    eventLogger.uploadEvent(eventLog1)
    val event = fakeEventLogger.getMostRecentEvent()

    assertThat(event).isEqualTo(eventLog1)
    assertThat(event.priority).isEqualTo(Priority.ESSENTIAL)
  }

  @Test
  fun testfakeEventLogger_logEventTwice_returnsLatestEvent() {
    eventLogger.uploadEvent(eventLog1)
    eventLogger.uploadEvent(eventLog2)
    val event = fakeEventLogger.getMostRecentEvent()

    assertThat(event).isEqualTo(eventLog2)
    assertThat(event.priority).isEqualTo(Priority.OPTIONAL)
  }

  @Test
  fun testfakeEventLogger_logEvent_clearAllEvents_logEventAgain_returnsLatestEvent() {
    eventLogger.uploadEvent(eventLog1)
    fakeEventLogger.clearAllEvents()
    eventLogger.uploadEvent(eventLog2)
    val event = fakeEventLogger.getMostRecentEvent()

    assertThat(event).isEqualTo(eventLog2)
    assertThat(event.priority).isEqualTo(Priority.OPTIONAL)
  }

  @Test
  fun testfakeEventLogger_logNothing_getMostRecent_returnsFailure() {
    assertThrows<NoSuchElementException>() { fakeEventLogger.getMostRecentEvent() }
  }

  @Test
  fun testfakeEventLogger_logEvent_clearAllEvents_getMostRecent_returnsFailure() {
    eventLogger.uploadEvent(eventLog1)
    fakeEventLogger.clearAllEvents()

    val eventException = assertThrows<NoSuchElementException>() {
      fakeEventLogger.getMostRecentEvent()
    }

    assertThat(eventException).isInstanceOf(NoSuchElementException::class.java)
  }

  @Test
  fun testfakeEventLogger_clearAllEvents_returnsEmptyList() {
    fakeEventLogger.clearAllEvents()
    val isListEmpty = fakeEventLogger.noEventsPresent()

    assertThat(isListEmpty).isTrue()
  }

  @Test
  fun testfakeEventLogger_logEvent_clearAllEvents_returnsEmptyList() {
    eventLogger.uploadEvent(eventLog1)
    fakeEventLogger.clearAllEvents()
    val isListEmpty = fakeEventLogger.noEventsPresent()

    assertThat(isListEmpty).isTrue()
  }

  @Test
  fun testfakeEventLogger_logMultipleEvents_clearAllEvents_returnsEmptyList() {
    eventLogger.uploadEvent(eventLog1)
    eventLogger.uploadEvent(eventLog2)
    fakeEventLogger.clearAllEvents()
    val isListEmpty = fakeEventLogger.noEventsPresent()

    assertThat(isListEmpty).isTrue()
  }

  @Test
  fun testfakeEventLogger_logEvent_returnsNonEmptyList() {
    eventLogger.uploadEvent(eventLog1)
    val isListEmpty = fakeEventLogger.noEventsPresent()

    assertThat(isListEmpty).isFalse()
  }

  @Test
  fun testfakeEventLogger_logMultipleEvents_returnsNonEmptyList() {
    eventLogger.uploadEvent(eventLog1)
    eventLogger.uploadEvent(eventLog2)

    val eventLogStatus1 = fakeEventLogger.hasEventLogged { it == eventLog1 }
    val eventLogStatus2 = fakeEventLogger.hasEventLogged { it == eventLog2 }
    val eventListStatus = fakeEventLogger.noEventsPresent()

    assertThat(eventListStatus).isFalse()
    assertThat(eventLogStatus1).isTrue()
    assertThat(eventLogStatus2).isTrue()
  }

  @Test
  fun testGetOldestEvent_noEventsLogged_throwsException() {
    assertThrows<NoSuchElementException>() { fakeEventLogger.getOldestEvent() }
  }

  @Test
  fun testGetOldestEvent_oneEventLogged_returnsLoggedEvent() {
    eventLogger.uploadEvent(eventLog1)

    val oldestEvent = fakeEventLogger.getOldestEvent()

    assertThat(oldestEvent).isEqualTo(eventLog1)
  }

  @Test
  fun testGetOldestEvent_twoEventsLogged_returnsFirstEventLogged() {
    eventLogger.uploadEvent(eventLog2)
    eventLogger.uploadEvent(eventLog1)

    val oldestEvent = fakeEventLogger.getOldestEvent()

    assertThat(oldestEvent).isEqualTo(eventLog2)
  }

  @Test
  fun testGetOldestEvent_twoEventsLogged_clearEvents_throwsException() {
    eventLogger.uploadEvent(eventLog2)
    eventLogger.uploadEvent(eventLog1)
    fakeEventLogger.clearAllEvents()

    assertThrows<NoSuchElementException>() { fakeEventLogger.getOldestEvent() }
  }

  @Test
  fun testGetOldestEvent_eventLogged_cleared_newEventLogged_returnsLatestEventLog() {
    eventLogger.uploadEvent(eventLog2)
    fakeEventLogger.clearAllEvents()
    eventLogger.uploadEvent(eventLog1)

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
    eventLogger.uploadEvent(eventLog1)

    val mostRecentEvents = fakeEventLogger.getMostRecentEvents(count = 2)

    assertThat(mostRecentEvents).containsExactly(eventLog1)
  }

  @Test
  fun testGetMostRecentEvents_twoEvents_twoEventsLogged_returnsEventsInOrder() {
    eventLogger.uploadEvent(eventLog2)
    eventLogger.uploadEvent(eventLog1)

    val mostRecentEvents = fakeEventLogger.getMostRecentEvents(count = 2)

    assertThat(mostRecentEvents).containsExactly(eventLog2, eventLog1).inOrder()
  }

  @Test
  fun testGetMostRecentEvents_oneEvent_twoEventsLogged_returnsSingleLatestEvent() {
    eventLogger.uploadEvent(eventLog2)
    eventLogger.uploadEvent(eventLog1)

    val mostRecentEvents = fakeEventLogger.getMostRecentEvents(count = 1)

    assertThat(mostRecentEvents).containsExactly(eventLog1)
  }

  @Test
  fun testGetMostRecentEvents_zeroEvents_twoEventsLogged_returnsEmptyList() {
    eventLogger.uploadEvent(eventLog2)
    eventLogger.uploadEvent(eventLog1)

    val mostRecentEvents = fakeEventLogger.getMostRecentEvents(count = 0)

    assertThat(mostRecentEvents).isEmpty()
  }

  @Test
  fun testGetMostRecentEvents_negativeEvents_twoEventsLogged_throwsException() {
    eventLogger.uploadEvent(eventLog2)
    eventLogger.uploadEvent(eventLog1)

    assertThrows<IllegalArgumentException>() {
      fakeEventLogger.getMostRecentEvents(count = -1)
    }
  }

  @Test
  fun testGetMostRecentEvents_twoEventsLogged_eventsCleared_returnsEmptyList() {
    eventLogger.uploadEvent(eventLog2)
    eventLogger.uploadEvent(eventLog1)
    fakeEventLogger.clearAllEvents()

    val mostRecentEvents = fakeEventLogger.getMostRecentEvents(count = 2)

    assertThat(mostRecentEvents).isEmpty()
  }

  @Test
  fun testGetMostRecentEvents_eventLogged_cleared_newEventLogged_returnsNewestEvent() {
    eventLogger.uploadEvent(eventLog1)
    fakeEventLogger.clearAllEvents()
    eventLogger.uploadEvent(eventLog2)

    val mostRecentEvents = fakeEventLogger.getMostRecentEvents(count = 2)

    assertThat(mostRecentEvents).containsExactly(eventLog2)
  }

  private fun setUpTestApplicationComponent() {
    DaggerFakeFirestoreEventLoggerTest_TestApplicationComponent.builder()
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

    fun inject(fakeEventLoggerTest: FakeFirestoreEventLoggerTest)
  }
}
