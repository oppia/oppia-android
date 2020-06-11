package org.oppia.testing

import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import org.junit.Before
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.util.logging.EventLogger
import org.robolectric.annotation.Config
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.reflect.KClass
import kotlin.reflect.full.cast

@RunWith(AndroidJUnit4::class)
@Config(manifest = Config.NONE)
class FakeEventLoggerTest {

  @Inject lateinit var fakeEventLogger: FakeEventLogger

  @Inject lateinit var eventLogger: EventLogger

  private val context = ApplicationProvider.getApplicationContext<Context>()
  private val bundle1 = Bundle()
  private val title1 = "Event_title1"
  private val bundle2 = Bundle()
  private val title2 = "Event_title2"
  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testFakeEventLogger_logEvent_returnsEvent() {
    bundle1.putString("KEY1", "Bundle1")

    eventLogger.logEvent(context, bundle1, title1)
    val event = fakeEventLogger.getMostRecentEvent()

    assertThat(event.bundle).isEqualTo(bundle1)
    assertThat(event.bundle.getString("KEY1")).matches("Bundle1")
    assertThat(event.title).matches(title1)
  }

  @Test
  fun testFakeEventLogger_logEventTwice_returnsLatestEvent() {
    bundle1.putString("KEY1", "Bundle1")
    bundle2.putString("KEY2", "Bundle2")

    eventLogger.logEvent(context, bundle1, title1)
    eventLogger.logEvent(context, bundle2, title2)
    val event = fakeEventLogger.getMostRecentEvent()

    assertThat(event.bundle).isEqualTo(bundle2)
    assertThat(event.bundle.getString("KEY2")).matches("Bundle2")
    assertThat(event.title).matches(title2)
  }

  @Test
  fun testFakeEventLogger_logEvent_clearAllEvents_logEventAgain_returnsLatestEvent() {
    bundle1.putString("KEY1", "Bundle1")
    bundle2.putString("KEY2", "Bundle2")

    eventLogger.logEvent(context, bundle1, title1)
    fakeEventLogger.clearAllEvents()
    eventLogger.logEvent(context, bundle2, title2)
    val event = fakeEventLogger.getMostRecentEvent()

    assertThat(event.bundle).isEqualTo(bundle2)
    assertThat(event.bundle.getString("KEY2")).matches("Bundle2")
    assertThat(event.title).matches(title2)
  }

  @Test
  fun testFakeEventLogger_logNothing_getMostRecent_returnsFailure() {
    val eventException = assertThrows(NoSuchElementException::class) {
      fakeEventLogger.getMostRecentEvent()
    }

    assertThat(eventException).isInstanceOf(NoSuchElementException::class.java)
    assertThat(eventException).hasMessageThat().contains("List is empty.")
  }

  @Test
  fun testFakeEventLogger_logEvent_clearAllEvents_getMostRecent_returnsFailure() {
    eventLogger.logEvent(context, bundle1, title1)
    fakeEventLogger.clearAllEvents()

    val eventException = assertThrows(NoSuchElementException::class) {
      fakeEventLogger.getMostRecentEvent()
    }

    assertThat(eventException).isInstanceOf(NoSuchElementException::class.java)
    assertThat(eventException).hasMessageThat().contains("List is empty.")
  }

  @Test
  fun testFakeEventLogger_clearAllEvents_returnsEmptyList() {
    fakeEventLogger.clearAllEvents()
    val eventList = fakeEventLogger.eventList

    assertThat(eventList.isEmpty()).isTrue()
  }

  @Test
  fun testFakeEventLogger_logEvent_clearAllEvents_returnsEmptyList() {
    eventLogger.logEvent(context, bundle1, title1)
    fakeEventLogger.clearAllEvents()
    val eventList = fakeEventLogger.eventList

    assertThat(eventList.isEmpty()).isTrue()
  }

  @Test
  fun testFakeEventLogger_logMultipleEvents_clearAllEvents_returnsEmptyList() {
    bundle1.putString("KEY1", "Bundle1")
    bundle2.putString("KEY2", "Bundle2")

    eventLogger.logEvent(context, bundle1, title1)
    eventLogger.logEvent(context, bundle2, title2)
    fakeEventLogger.clearAllEvents()
    val eventList = fakeEventLogger.eventList

    assertThat(eventList.isEmpty()).isTrue()
  }

  @Test
  fun testFakeEventLogger_logEvent_returnsNonEmptyList() {
    bundle1.putString("KEY1", "Bundle1")

    eventLogger.logEvent(context, bundle1, title1)
    val eventList = fakeEventLogger.eventList
    val eventListSize = eventList.size

    assertThat(eventList.isNotEmpty()).isTrue()
    assertThat(eventList[eventListSize - 1].bundle).isEqualTo(bundle1)
    assertThat(eventList[eventListSize - 1].bundle.getString("KEY1")).matches("Bundle1")
    assertThat(eventList[eventListSize - 1].title).matches(title1)
  }

  @Test
  fun testFakeEventLogger_logMultipleEvents_returnsNonEmptyList() {
    bundle1.putString("KEY1", "Bundle1")
    bundle2.putString("KEY2", "Bundle2")

    eventLogger.logEvent(context, bundle1, title1)
    eventLogger.logEvent(context, bundle2, title2)
    val eventList = fakeEventLogger.eventList
    val eventListSize = eventList.size

    assertThat(eventList.isNotEmpty()).isTrue()
    assertThat(eventList[eventListSize - 2].bundle).isEqualTo(bundle1)
    assertThat(eventList[eventListSize - 2].bundle.getString("KEY1")).matches("Bundle1")
    assertThat(eventList[eventListSize - 2].title).matches(title1)
    assertThat(eventList[eventListSize - 1].bundle).isEqualTo(bundle2)
    assertThat(eventList[eventListSize - 1].bundle.getString("KEY2")).matches("Bundle2")
    assertThat(eventList[eventListSize - 1].title).matches(title2)

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