package org.oppia.android.domain.oppialogger.exceptions

import android.app.Application
import android.content.Context
import androidx.lifecycle.Observer
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
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito.atLeastOnce
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import org.oppia.android.app.model.OppiaExceptionLogs
import org.oppia.android.domain.oppialogger.ExceptionLogStorageCacheSize
import org.oppia.android.testing.FakeExceptionLogger
import org.oppia.android.testing.TestCoroutineDispatchers
import org.oppia.android.testing.TestDispatcherModule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import org.oppia.android.util.data.DataProvidersInjector
import org.oppia.android.util.data.DataProvidersInjectorProvider
import org.oppia.android.util.logging.EnableConsoleLog
import org.oppia.android.util.logging.EnableFileLog
import org.oppia.android.util.logging.GlobalLogLevel
import org.oppia.android.util.logging.LogLevel
import org.oppia.android.util.networking.NetworkConnectionUtil
import org.oppia.android.testing.FakeExceptionLogger
import org.oppia.android.testing.TestCoroutineDispatchers
import org.oppia.android.testing.TestDispatcherModule
import org.oppia.android.testing.TestLogReportingModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Qualifier
import javax.inject.Singleton

@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = UncaughtExceptionLoggerStartupListenerTest.TestApplication::class)
class UncaughtExceptionLoggerStartupListenerTest {

  @Rule
  @JvmField
  val mockitoRule: MockitoRule = MockitoJUnit.rule()

  @Inject
  lateinit var dataProviders: DataProviders

  @Inject
  lateinit var uncaughtExceptionLoggerStartupListener: UncaughtExceptionLoggerStartupListener

  @Inject
  lateinit var networkConnectionUtil: NetworkConnectionUtil

  @Inject
  lateinit var exceptionsController: ExceptionsController

  @Inject
  lateinit var fakeExceptionLogger: FakeExceptionLogger

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @Mock
  lateinit var mockOppiaExceptionLogsObserver: Observer<AsyncResult<OppiaExceptionLogs>>

  @Captor
  lateinit var oppiaExceptionLogsResultCaptor: ArgumentCaptor<AsyncResult<OppiaExceptionLogs>>

  @Before
  fun setUp() {
    networkConnectionUtil = NetworkConnectionUtil(ApplicationProvider.getApplicationContext())
    setUpTestApplicationComponent()
  }

  @Test
  fun testHandler_throwException_withNoNetwork_verifyLogInCache() {
    networkConnectionUtil.setCurrentConnectionStatus(NetworkConnectionUtil.ConnectionStatus.NONE)
    val exceptionThrown = Exception("TEST")
    uncaughtExceptionLoggerStartupListener.uncaughtException(
      Thread.currentThread(),
      exceptionThrown
    )

    val cachedExceptions = exceptionsController.getExceptionLogStore()
    cachedExceptions.toLiveData().observeForever(mockOppiaExceptionLogsObserver)
    testCoroutineDispatchers.advanceUntilIdle()

    verify(mockOppiaExceptionLogsObserver, atLeastOnce())
      .onChanged(oppiaExceptionLogsResultCaptor.capture())

    val exceptionLog = oppiaExceptionLogsResultCaptor.value.getOrThrow().getExceptionLog(0)
    val exception = exceptionLog.toException()

    assertThat(exception.message).matches("java.lang.Exception: TEST")
  }

  @Test
  fun testHandler_throwException_withNetwork_verifyLogToRemoteService() {
    val exceptionThrown = Exception("TEST")
    uncaughtExceptionLoggerStartupListener.uncaughtException(
      Thread.currentThread(),
      exceptionThrown
    )

    val exceptionCaught = fakeExceptionLogger.getMostRecentException()
    assertThat(exceptionCaught).hasMessageThat().matches("java.lang.Exception: TEST")
    assertThat(exceptionCaught.cause).isEqualTo(Exception(exceptionThrown).cause)
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  @Qualifier
  annotation class TestDispatcher

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
    @ExceptionLogStorageCacheSize
    fun provideExceptionLogStorageCacheSize(): Int = 2
  }

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(
    modules = [
      TestModule::class, TestLogReportingModule::class,
      TestDispatcherModule::class, TestLogStorageModule::class
    ]
  )
  interface TestApplicationComponent : DataProvidersInjector {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder
      fun build(): TestApplicationComponent
    }

    fun inject(startupListenerTest: UncaughtExceptionLoggerStartupListenerTest)
  }

  class TestApplication : Application(), DataProvidersInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerUncaughtExceptionLoggerStartupListenerTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(startupListenerTest: UncaughtExceptionLoggerStartupListenerTest) {
      component.inject(startupListenerTest)
    }

    override fun getDataProvidersInjector(): DataProvidersInjector = component
  }
}
