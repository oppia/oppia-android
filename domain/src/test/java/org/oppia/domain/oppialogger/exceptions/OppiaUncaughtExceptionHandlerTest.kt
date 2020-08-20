package org.oppia.domain.oppialogger.exceptions

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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
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
import org.oppia.app.model.OppiaExceptionLogs
import org.oppia.domain.oppialogger.ExceptionLogStorageCacheSize
import org.oppia.testing.FakeExceptionLogger
import org.oppia.testing.TestCoroutineDispatchers
import org.oppia.testing.TestDispatcherModule
import org.oppia.testing.TestLogReportingModule
import org.oppia.util.data.AsyncResult
import org.oppia.util.data.DataProviders
import org.oppia.util.logging.EnableConsoleLog
import org.oppia.util.logging.EnableFileLog
import org.oppia.util.logging.ExceptionLogger
import org.oppia.util.logging.GlobalLogLevel
import org.oppia.util.logging.LogLevel
import org.oppia.util.networking.NetworkConnectionUtil
import org.robolectric.annotation.Config
import javax.inject.Inject
import javax.inject.Qualifier
import javax.inject.Singleton

@RunWith(AndroidJUnit4::class)
@Config(manifest = Config.NONE)
class OppiaUncaughtExceptionHandlerTest {

  @Rule
  @JvmField
  val mockitoRule: MockitoRule = MockitoJUnit.rule()

  @Inject
  lateinit var dataProviders: DataProviders

  @Inject
  lateinit var oppiaUncaughtExceptionHandler: OppiaUncaughtExceptionHandler

  @Inject
  lateinit var networkConnectionUtil: NetworkConnectionUtil

  @Inject
  lateinit var fakeDefaultExceptionHandler: FakeDefaultExceptionHandler

  @Inject
  lateinit var exceptionsController: ExceptionsController

  @Inject
  lateinit var fakeExceptionLogger: FakeExceptionLogger

  @InternalCoroutinesApi
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
    Thread.setDefaultUncaughtExceptionHandler(fakeDefaultExceptionHandler)
  }

  @ExperimentalCoroutinesApi
  @InternalCoroutinesApi
  @Test
  fun testHandler_throwException_withNoNetwork_verifyLogInCache() {
    networkConnectionUtil.setCurrentConnectionStatus(NetworkConnectionUtil.ConnectionStatus.NONE)
    val exceptionThrown = Exception("TEST")
    oppiaUncaughtExceptionHandler.uncaughtException(Thread.currentThread(), exceptionThrown)

    val cachedExceptions = exceptionsController.getExceptionLogStore()
    dataProviders.convertToLiveData(cachedExceptions).observeForever(mockOppiaExceptionLogsObserver)
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
    oppiaUncaughtExceptionHandler.uncaughtException(Thread.currentThread(), exceptionThrown)

    val exceptionCaught = fakeExceptionLogger.getMostRecentException()
    assertThat(exceptionCaught).hasMessageThat().matches("java.lang.Exception: TEST")
    assertThat(exceptionCaught.cause).isEqualTo(Exception(exceptionThrown).cause)
  }

  private fun setUpTestApplicationComponent() {
    DaggerOppiaUncaughtExceptionHandlerTest_TestApplicationComponent.builder()
      .setApplication(ApplicationProvider.getApplicationContext())
      .build()
      .inject(this)
  }

  @Test
  fun testHandler_throwException_verifyLogToDefaultHandler(){
    val exceptionThrown = Exception("TEST")
    oppiaUncaughtExceptionHandler.uncaughtException(Thread.currentThread(), exceptionThrown)

    val exceptionCaught = fakeDefaultExceptionHandler.getMostRecentException()
    assertThat(exceptionCaught).hasMessageThat().matches("java.lang.Exception: TEST")
    assertThat(exceptionCaught.cause).isEqualTo(Exception(exceptionThrown).cause)
    assertThat(Thread.getDefaultUncaughtExceptionHandler()).isEqualTo(fakeDefaultExceptionHandler)
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
  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder
      fun build(): TestApplicationComponent
    }

    fun inject(oppiaUncaughtExceptionHandlerTest: OppiaUncaughtExceptionHandlerTest)
  }
}

class FakeDefaultExceptionHandler @Inject constructor(
  private val fakeExceptionLogger: ExceptionLogger
): Thread.UncaughtExceptionHandler{

  private val exceptionList = mutableListOf<Exception>()

  override fun uncaughtException(p0: Thread?, p1: Throwable?) {
    exceptionList.add(Exception(p1))
  }

  fun getMostRecentException() = exceptionList.last()
}