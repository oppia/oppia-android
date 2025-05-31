package org.oppia.android.util.logging.firebase

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
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.app.model.EventLog
import org.oppia.android.data.backends.gae.RetrofitModule
import org.oppia.android.data.backends.gae.RetrofitServiceModule
import org.oppia.android.data.backends.gae.testing.NetworkConfigTestModule
import org.oppia.android.domain.platformparameter.testing.PlatformParameterInitializationInjector
import org.oppia.android.domain.platformparameter.testing.PlatformParameterInitializationInjectorProvider
import org.oppia.android.domain.platformparameter.testing.PlatformParameterTestInitializer
import org.oppia.android.domain.platformparameter.testing.PlatformParameterTestModule
import org.oppia.android.testing.FakeFirestoreInstanceWrapperImpl
import org.oppia.android.testing.OppiaTestRule
import org.oppia.android.testing.assertThrows
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.caching.AssetModule
import org.oppia.android.util.data.DataProvidersInjector
import org.oppia.android.util.data.DataProvidersInjectorProvider
import org.oppia.android.util.locale.LocaleProdModule
import org.oppia.android.util.logging.AnalyticsEventLogger
import org.oppia.android.util.logging.EnableConsoleLog
import org.oppia.android.util.logging.EnableFileLog
import org.oppia.android.util.logging.ExceptionLogger
import org.oppia.android.util.logging.GlobalLogLevel
import org.oppia.android.util.logging.LogLevel
import org.oppia.android.util.logging.performancemetrics.PerformanceMetricsEventLogger
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [DebugFirestoreEventLoggerImpl]. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(manifest = Config.NONE)
class DebugFirestoreEventLoggerImplTest {
  @get:Rule
  val oppiaTestRule = OppiaTestRule()

  @Inject
  lateinit var debugFirestoreLoggerImpl: DebugFirestoreEventLoggerImpl

  @Inject
  lateinit var eventLogger: FirestoreEventLogger

  private val eventLog1 = EventLog.newBuilder().setPriority(EventLog.Priority.ESSENTIAL).build()
  private val eventLog2 = EventLog.newBuilder().setPriority(EventLog.Priority.ESSENTIAL).build()

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testDebugFirestoreEventLogger_logEvent_returnsEvent() {
    eventLogger.uploadEvent(eventLog1)
    val event = debugFirestoreLoggerImpl.getMostRecentEvent()

    assertThat(event).isEqualTo(eventLog1)
    assertThat(event.priority).isEqualTo(EventLog.Priority.ESSENTIAL)
  }

  @Test
  fun testDebugFirestoreEventLogger_logEventTwice_returnsLatestEvent() {
    eventLogger.uploadEvent(eventLog1)
    eventLogger.uploadEvent(eventLog2)
    val event = debugFirestoreLoggerImpl.getMostRecentEvent()

    assertThat(event).isEqualTo(eventLog2)
  }

  @Test
  fun testDebugFirestoreEventLogger_logEvent_clearAllEvents_logEventAgain_returnsLatestEvent() {
    eventLogger.uploadEvent(eventLog1)
    debugFirestoreLoggerImpl.clearAllEvents()
    debugFirestoreLoggerImpl.uploadEvent(eventLog2)
    val event = debugFirestoreLoggerImpl.getMostRecentEvent()

    assertThat(event).isEqualTo(eventLog2)
  }

  @Test
  fun testDebugFirestoreEventLogger_logNothing_getMostRecent_returnsFailure() {
    assertThrows<NoSuchElementException>() { debugFirestoreLoggerImpl.getMostRecentEvent() }
  }

  @Test
  fun testDebugFirestoreEventLogger_logEvent_clearAllEvents_getMostRecent_returnsFailure() {
    eventLogger.uploadEvent(eventLog1)
    debugFirestoreLoggerImpl.clearAllEvents()

    val eventException = assertThrows<NoSuchElementException>() {
      debugFirestoreLoggerImpl.getMostRecentEvent()
    }

    assertThat(eventException).isInstanceOf(NoSuchElementException::class.java)
  }

  @Test
  fun testDebugFirestoreEventLogger_clearAllEvents_returnsEmptyList() {
    debugFirestoreLoggerImpl.clearAllEvents()
    val isListEmpty = debugFirestoreLoggerImpl.getEventList().isEmpty()

    assertThat(isListEmpty).isTrue()
  }

  @Test
  fun testDebugFirestoreEventLogger_logEvent_clearAllEvents_returnsEmptyList() {
    eventLogger.uploadEvent(eventLog1)
    debugFirestoreLoggerImpl.clearAllEvents()
    val isListEmpty = debugFirestoreLoggerImpl.getEventList().isEmpty()

    assertThat(isListEmpty).isTrue()
  }

  @Test
  fun testDebugFirestoreEventLogger_logMultipleEvents_clearAllEvents_returnsEmptyList() {
    eventLogger.uploadEvent(eventLog1)
    eventLogger.uploadEvent(eventLog2)
    debugFirestoreLoggerImpl.clearAllEvents()
    val isListEmpty = debugFirestoreLoggerImpl.getEventList().isEmpty()

    assertThat(isListEmpty).isTrue()
  }

  @Test
  fun testDebugFirestoreEventLogger_logEvent_returnsNonEmptyList() {
    eventLogger.uploadEvent(eventLog1)
    val isListEmpty = debugFirestoreLoggerImpl.getEventList().isEmpty()

    assertThat(isListEmpty).isFalse()
  }

  private fun setUpTestApplicationComponent() {
    DaggerDebugFirestoreEventLoggerImplTest_TestApplicationComponent.builder()
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

    @Provides
    @Singleton
    fun provideDebugFirestoreLogger(debugFirestoreEventLogger: DebugFirestoreEventLoggerImpl):
      FirestoreEventLogger = debugFirestoreEventLogger

    @Provides
    @Singleton
    fun provideFirebaseFirestoreInstanceWrapper(
      debugWrapperImpl: FakeFirestoreInstanceWrapperImpl
    ): FirestoreInstanceWrapper = debugWrapperImpl

    @Provides
    fun provideCrashLogger(): ExceptionLogger = error("Not used in test.")

    @Provides
    fun provideFirebaseAnalyticsEventLogger(): AnalyticsEventLogger = error("Not used in test.")

    @Provides
    fun providePerformanceMetricsEventLogger(): PerformanceMetricsEventLogger =
      error("Not used in test.")
  }

  @Singleton
  @Component(
    modules = [
      AssetModule::class,
      FakeOppiaClockModule::class,
      LocaleProdModule::class,
      NetworkConfigTestModule::class,
      PlatformParameterTestModule::class,
      RetrofitModule::class,
      RetrofitServiceModule::class,
      RobolectricModule::class,
      TestDispatcherModule::class,
      TestModule::class
    ]
  )
  interface TestApplicationComponent :
    DataProvidersInjector,
    PlatformParameterInitializationInjector {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun getPlatformParameterTestInitializer(): PlatformParameterTestInitializer

    fun inject(test: DebugFirestoreEventLoggerImplTest)
  }

  class TestApplication :
    Application(),
    DataProvidersInjectorProvider,
    PlatformParameterInitializationInjectorProvider {
    val component: TestApplicationComponent by lazy {
      DaggerDebugFirestoreEventLoggerImplTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(test: DebugFirestoreEventLoggerImplTest) {
      component.inject(test)
    }

    override fun getDataProvidersInjector() = component

    override fun getPlatformParameterInitializationInjector() = component
  }
}
