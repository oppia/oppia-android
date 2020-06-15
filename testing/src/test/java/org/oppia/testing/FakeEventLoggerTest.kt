package org.oppia.testing

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.model.EventLog
import org.oppia.app.model.EventLog.Priority
import org.oppia.util.logging.EventLogger
import org.oppia.util.logging.PRIORITY_KEY
import org.robolectric.annotation.Config
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.reflect.KClass
import kotlin.reflect.full.cast

@RunWith(AndroidJUnit4::class)
@Config(manifest = Config.NONE)
class FakeEventLoggerTest {

  @Inject
  lateinit var fakeEventLogger: FakeEventLogger

  @Inject
  lateinit var eventLogger: EventLogger

  private val context = ApplicationProvider.getApplicationContext<Context>()
  private val eventLog1 = EventLog.newBuilder().setPriority(Priority.ESSENTIAL).build()
  private val eventLog2 = EventLog.newBuilder().setPriority(Priority.OPTIONAL).build()

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testFakeEventLogger_logEvent_returnsEvent() {
    eventLogger.logEvent(context, eventLog1)
    val event = fakeEventLogger.getMostRecentEvent()

    assertThat(event).isEqualTo(eventLog1)
    assertThat(event.priority).isEqualTo(Priority.ESSENTIAL)
  }

  @Test
  fun testFakeEventLogger_logEventTwice_returnsLatestEvent() {
    eventLogger.logEvent(context, eventLog1)
    eventLogger.logEvent(context, eventLog2)
    val event = fakeEventLogger.getMostRecentEvent()

    assertThat(event).isEqualTo(eventLog2)
    assertThat(event.priority).isEqualTo(Priority.OPTIONAL)
  }

  @Test
  fun testFakeEventLogger_logEvent_clearAllEvents_logEventAgain_returnsLatestEvent() {
    eventLogger.logEvent(context, eventLog1)
    fakeEventLogger.clearAllEvents()
    eventLogger.logEvent(context, eventLog2)
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
    eventLogger.logEvent(context, eventLog1)
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
    eventLogger.logEvent(context, eventLog1)
    fakeEventLogger.clearAllEvents()
    val isListEmpty = fakeEventLogger.noEventsPresent()

    assertThat(isListEmpty).isTrue()
  }

  @Test
  fun testFakeEventLogger_logMultipleEvents_clearAllEvents_returnsEmptyList() {
    eventLogger.logEvent(context, eventLog1)
    eventLogger.logEvent(context, eventLog2)
    fakeEventLogger.clearAllEvents()
    val isListEmpty = fakeEventLogger.noEventsPresent()

    assertThat(isListEmpty).isTrue()
  }

  @Test
  fun testFakeEventLogger_logEvent_returnsNonEmptyList() {
    eventLogger.logEvent(context, eventLog1)
    val isListEmpty = fakeEventLogger.noEventsPresent()

    assertThat(isListEmpty).isFalse()
  }

  @Test
  fun testFakeEventLogger_logMultipleEvents_returnsNonEmptyList() {
    eventLogger.logEvent(context, eventLog1)
    eventLogger.logEvent(context, eventLog2)

    val eventLogStatus1 = fakeEventLogger.hasEventLogged(eventLog1)
    val eventLogStatus2 = fakeEventLogger.hasEventLogged(eventLog2)
    val eventListStatus = fakeEventLogger.noEventsPresent()

    assertThat(eventListStatus).isFalse()
    assertThat(eventLogStatus1).isTrue()
    assertThat(eventLogStatus2).isTrue()
  }

  @Test
  fun testFakeEventLogger_logEvent_returnsEventBundle() {
    eventLogger.logEvent(context, eventLog1)
    val eventBundle = fakeEventLogger.getMostRecentEventBundle()

    assertThat(eventBundle.get(PRIORITY_KEY)).isEqualTo(Priority.ESSENTIAL.toString())
  }

  // TODO(#89): Move to a common test library.
  /** A replacement to JUnit5's assertThrows(). */
  private fun <T : Throwable> assertThrows(type: KClass<T>, operation: () -> Unit): T {
    try {
      operation()
      fail("Expected to encounter exception of $type")
    } catch (t: Throwable) {
      if (type.isInstance(t)) {
        return type.cast(t)
      }
      // Unexpected exception; throw it.
      throw t
    }
    throw AssertionError(
      "Reached an impossible state when verifying that an exception was thrown."
    )
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
  @Component(modules = [TestModule::class, TestLogReportingModule::class])
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
