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
import org.oppia.android.util.logging.EventLogger
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.domain.oppialogger.LoggingIdentifierModule
import org.oppia.android.domain.oppialogger.analytics.ApplicationLifecycleModule
import org.oppia.android.domain.platformparameter.PlatformParameterModule
import org.oppia.android.domain.platformparameter.PlatformParameterSingletonModule
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.logging.SyncStatusModule

@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(manifest = Config.NONE)
class FakeEventLoggerTest {

  @Inject
  lateinit var fakeEventLogger: FakeEventLogger

  @Inject
  lateinit var eventLogger: EventLogger

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
    val eventException = assertThrows(NoSuchElementException::class) {
      fakeEventLogger.getMostRecentEvent()
    }

    assertThat(eventException).isInstanceOf(NoSuchElementException::class.java)
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
  fun testFakeEventLogger_logCachedEvent_returnsEvent() {
    eventLogger.logCachedEvent(eventLog1)
    val event = fakeEventLogger.getMostRecentCachedEvent()

    assertThat(event).isEqualTo(eventLog1)
    assertThat(event.priority).isEqualTo(Priority.ESSENTIAL)
  }

  @Test
  fun testFakeEventLogger_logCachedEventTwice_returnsLatestCachedEvent() {
    eventLogger.logCachedEvent(eventLog1)
    eventLogger.logCachedEvent(eventLog2)
    val event = fakeEventLogger.getMostRecentCachedEvent()

    assertThat(event).isEqualTo(eventLog2)
    assertThat(event.priority).isEqualTo(Priority.OPTIONAL)
  }

  @Test
  fun testFakeEventLogger_logCachedEvent_clearAllCachedEvents_logCachedEventAgain_returnsLatestCachedEvent() { // ktlint-disable max-line-length
    eventLogger.logCachedEvent(eventLog1)
    fakeEventLogger.clearAllCachedEvents()
    eventLogger.logCachedEvent(eventLog2)
    val event = fakeEventLogger.getMostRecentCachedEvent()

    assertThat(event).isEqualTo(eventLog2)
    assertThat(event.priority).isEqualTo(Priority.OPTIONAL)
  }

  @Test
  fun testFakeEventLogger_logCachedEvent_clearAllCachedEvents_getMostRecent_returnsFailure() {
    eventLogger.logCachedEvent(eventLog1)
    fakeEventLogger.clearAllCachedEvents()

    val eventException = assertThrows(NoSuchElementException::class) {
      fakeEventLogger.getMostRecentCachedEvent()
    }

    assertThat(eventException).isInstanceOf(NoSuchElementException::class.java)
  }

  @Test
  fun testFakeEventLogger_clearAllCachedEvents_returnsEmptyList() {
    fakeEventLogger.clearAllCachedEvents()
    val isListEmpty = fakeEventLogger.noCachedEventsPresent()

    assertThat(isListEmpty).isTrue()
  }

  @Test
  fun testFakeEventLogger_logCachedEvent_clearAllCachedEvents_returnsEmptyList() {
    eventLogger.logCachedEvent(eventLog1)
    fakeEventLogger.clearAllCachedEvents()
    val isListEmpty = fakeEventLogger.noCachedEventsPresent()

    assertThat(isListEmpty).isTrue()
  }

  @Test
  fun testFakeEventLogger_logMultipleCachedEvents_clearAllCachedEvents_returnsEmptyList() {
    eventLogger.logCachedEvent(eventLog1)
    eventLogger.logCachedEvent(eventLog2)
    fakeEventLogger.clearAllCachedEvents()
    val isListEmpty = fakeEventLogger.noCachedEventsPresent()

    assertThat(isListEmpty).isTrue()
  }

  @Test
  fun testFakeEventLogger_logCachedEvent_returnsNonEmptyList() {
    eventLogger.logCachedEvent(eventLog1)
    val isListEmpty = fakeEventLogger.noCachedEventsPresent()

    assertThat(isListEmpty).isFalse()
  }

  @Test
  fun testFakeEventLogger_logMultipleCachedEvents_returnsNonEmptyList() {
    eventLogger.logCachedEvent(eventLog1)
    eventLogger.logCachedEvent(eventLog2)

    val eventLogStatus1 = fakeEventLogger.hasCachedEventLogged(eventLog1)
    val eventLogStatus2 = fakeEventLogger.hasCachedEventLogged(eventLog2)
    val eventListStatus = fakeEventLogger.noCachedEventsPresent()

    assertThat(eventListStatus).isFalse()
    assertThat(eventLogStatus1).isTrue()
    assertThat(eventLogStatus2).isTrue()
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
      TestDispatcherModule::class, LogStorageModule::class, LoggingIdentifierModule::class,
      FakeOppiaClockModule::class, ApplicationLifecycleModule::class, SyncStatusModule::class,
      UserIdProdModule::class, PlatformParameterModule::class,
      PlatformParameterSingletonModule::class
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
