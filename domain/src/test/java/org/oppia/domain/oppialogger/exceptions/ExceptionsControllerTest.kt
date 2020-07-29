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
import org.oppia.app.model.ExceptionLog
import org.oppia.app.model.ExceptionLog.ExceptionType
import org.oppia.app.model.OppiaExceptionLogs
import org.oppia.domain.oppialogger.ExceptionLogStorageCacheSize
import org.oppia.testing.FakeExceptionLogger
import org.oppia.testing.TestCoroutineDispatchers
import org.oppia.testing.TestDispatcherModule
import org.oppia.testing.TestLogReportingModule
import org.oppia.util.data.AsyncResult
import org.oppia.util.logging.EnableConsoleLog
import org.oppia.util.logging.EnableFileLog
import org.oppia.util.logging.GlobalLogLevel
import org.oppia.util.logging.LogLevel
import org.oppia.util.networking.NetworkConnectionUtil
import org.robolectric.annotation.Config
import javax.inject.Inject
import javax.inject.Qualifier
import javax.inject.Singleton

private const val TEST_TIMESTAMP_ONE = 1556094120000
private const val TEST_TIMESTAMP_TWO = 1556094110000
private const val TEST_TIMESTAMP_THREE = 1556094100000
private const val TEST_TIMESTAMP_FOUR = 1556094000000

@RunWith(AndroidJUnit4::class)
@Config(manifest = Config.NONE)
class ExceptionsControllerTest {

  @Rule
  @JvmField
  val mockitoRule: MockitoRule = MockitoJUnit.rule()

  @Inject
  lateinit var exceptionsController: ExceptionsController

  @Inject
  lateinit var networkConnectionUtil: NetworkConnectionUtil

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
  }

  @Test
  fun testController_logException_nonFatal_logsToRemoteService() {
    val exceptionThrown = Exception("TEST MESSAGE", Throwable())
    exceptionsController.logNonFatalException(exceptionThrown, TEST_TIMESTAMP_ONE)

    val exceptionLogged = fakeExceptionLogger.getMostRecentException()

    assertThat(exceptionLogged).isEqualTo(exceptionThrown)
  }

  @Test
  fun testController_logFatalException_logsToRemoteService() {
    val exceptionThrown = Exception("TEST MESSAGE", Throwable())
    exceptionsController.logFatalException(exceptionThrown, TEST_TIMESTAMP_ONE)

    val exceptionLogged = fakeExceptionLogger.getMostRecentException()

    assertThat(exceptionLogged).isEqualTo(exceptionThrown)
  }

  // TODO(#1106): Addition of tests tracking behaviour of the controller after uploading of logs to the remote service.

  @ExperimentalCoroutinesApi
  @InternalCoroutinesApi
  @Test
  fun testController_logException_NonFatal_withNoNetwork_logsToCacheStore() {
    networkConnectionUtil.setCurrentConnectionStatus(NetworkConnectionUtil.ConnectionStatus.NONE)
    val exceptionThrown = Exception("TEST MESSAGE", Throwable("TEST CAUSE"))
    exceptionsController.logNonFatalException(exceptionThrown, TEST_TIMESTAMP_ONE)

    val cachedExceptions = exceptionsController.getExceptionLogs()
    cachedExceptions.observeForever(mockOppiaExceptionLogsObserver)
    testCoroutineDispatchers.advanceUntilIdle()

    verify(mockOppiaExceptionLogsObserver, atLeastOnce())
      .onChanged(oppiaExceptionLogsResultCaptor.capture())

    val exceptionLog = oppiaExceptionLogsResultCaptor.value.getOrThrow().getExceptionLog(0)
    val exception = exceptionLogToException(exceptionLog)
    assertThat(exception.message).isEqualTo(exceptionThrown.message)
    assertThat(exception.stackTrace).isEqualTo(exceptionThrown.stackTrace)
    assertThat(exception.cause?.message).isEqualTo(exceptionThrown.cause?.message)
    assertThat(exception.cause?.stackTrace).isEqualTo(exceptionThrown.cause?.stackTrace)
    assertThat(exceptionLog.exceptionType).isEqualTo(ExceptionType.NON_FATAL)
  }

  @ExperimentalCoroutinesApi
  @InternalCoroutinesApi
  @Test
  fun testController_logException_Fatal_withNoNetwork_logsToCacheStore() {
    networkConnectionUtil.setCurrentConnectionStatus(NetworkConnectionUtil.ConnectionStatus.NONE)
    val exceptionThrown = Exception("TEST MESSAGE", Throwable("TEST"))
    exceptionsController.logFatalException(exceptionThrown, TEST_TIMESTAMP_ONE)

    val cachedExceptions = exceptionsController.getExceptionLogs()
    cachedExceptions.observeForever(mockOppiaExceptionLogsObserver)
    testCoroutineDispatchers.advanceUntilIdle()

    verify(mockOppiaExceptionLogsObserver, atLeastOnce())
      .onChanged(oppiaExceptionLogsResultCaptor.capture())

    val exceptionLog = oppiaExceptionLogsResultCaptor.value.getOrThrow().getExceptionLog(0)
    val exception = exceptionLogToException(exceptionLog)
    assertThat(exception.message).isEqualTo(exceptionThrown.message)
    assertThat(exception.stackTrace).isEqualTo(exceptionThrown.stackTrace)
    assertThat(exception.cause?.message).isEqualTo(exceptionThrown.cause?.message)
    assertThat(exception.cause?.stackTrace).isEqualTo(exceptionThrown.cause?.stackTrace)
    assertThat(exceptionLog.exceptionType).isEqualTo(ExceptionType.FATAL)
  }

  @ExperimentalCoroutinesApi
  @InternalCoroutinesApi
  @Test
  fun testController_logExceptions_exceedLimit_checkCorrectEviction() {
    networkConnectionUtil.setCurrentConnectionStatus(NetworkConnectionUtil.ConnectionStatus.NONE)

    exceptionsController.logFatalException(Exception("TEST1", Throwable("ONE")), TEST_TIMESTAMP_ONE)
    exceptionsController.logNonFatalException(
      Exception("TEST2", Throwable("TWO")),
      TEST_TIMESTAMP_TWO
    )
    exceptionsController.logFatalException(
      Exception("TEST3", Throwable("THREE")),
      TEST_TIMESTAMP_THREE
    )
    exceptionsController.logFatalException(
      Exception("TEST4", Throwable("FOUR")),
      TEST_TIMESTAMP_FOUR
    )

    val cachedExceptions = exceptionsController.getExceptionLogs()
    cachedExceptions.observeForever(mockOppiaExceptionLogsObserver)
    testCoroutineDispatchers.advanceUntilIdle()

    verify(mockOppiaExceptionLogsObserver, atLeastOnce())
      .onChanged(oppiaExceptionLogsResultCaptor.capture())

    val exceptionOne = oppiaExceptionLogsResultCaptor.value.getOrThrow().getExceptionLog(0)
    val exceptionTwo = oppiaExceptionLogsResultCaptor.value.getOrThrow().getExceptionLog(1)
    val cacheStoreSize = oppiaExceptionLogsResultCaptor.value.getOrThrow().exceptionLogList.size

    // In this case, 3 fatal and 1 non-fatal exceptions were logged. So while pruning, none of the retained logs should have non-fatal exception type.
    assertThat(exceptionOne.exceptionType).isNotEqualTo(ExceptionType.NON_FATAL)
    assertThat(exceptionTwo.exceptionType).isNotEqualTo(ExceptionType.NON_FATAL)
    // If we analyse the order of logging of exceptions, we can see that record pruning will begin from the logging of the third record.
    // At first, the second exception log will be removed as it has non-fatal exception type and the exception logged at the third place will become the exception record at the second place in the store.
    // When the forth exception gets logged then the pruning will be purely based on timestamp of the exception as both exception logs have fatal exception type.
    // As the third exceptions's timestamp was lesser than that of the first event, it will be pruned from the store and the forth exception will become the second exception in the store.
    assertThat(exceptionOne.timestampInMillis).isEqualTo(TEST_TIMESTAMP_ONE)
    assertThat(exceptionTwo.timestampInMillis).isEqualTo(TEST_TIMESTAMP_FOUR)
    assertThat(exceptionOne.message).isEqualTo("TEST1")
    assertThat(exceptionTwo.message).isEqualTo("TEST4")
  }

  @ExperimentalCoroutinesApi
  @InternalCoroutinesApi
  @Test
  fun testController_logExceptions_exceedLimit_cacheSizeDoesNotExceedLimit() {
    networkConnectionUtil.setCurrentConnectionStatus(NetworkConnectionUtil.ConnectionStatus.NONE)

    exceptionsController.logFatalException(Exception("TEST1", Throwable("ONE")), TEST_TIMESTAMP_ONE)
    exceptionsController.logNonFatalException(
      Exception("TEST2", Throwable("TWO")),
      TEST_TIMESTAMP_TWO
    )
    exceptionsController.logFatalException(
      Exception("TEST3", Throwable("THREE")),
      TEST_TIMESTAMP_THREE
    )

    val cachedExceptions = exceptionsController.getExceptionLogs()
    cachedExceptions.observeForever(mockOppiaExceptionLogsObserver)
    testCoroutineDispatchers.advanceUntilIdle()

    verify(mockOppiaExceptionLogsObserver, atLeastOnce())
      .onChanged(oppiaExceptionLogsResultCaptor.capture())
    val cacheStoreSize = oppiaExceptionLogsResultCaptor.value.getOrThrow().exceptionLogList.size

    assertThat(cacheStoreSize).isEqualTo(2)
  }

  @ExperimentalCoroutinesApi
  @InternalCoroutinesApi
  @Test
  fun testController_logException_switchToNoNetwork_logException_verifyLoggingAndCaching() {
    val exceptionThrown = Exception("TEST", Throwable())
    exceptionsController.logNonFatalException(exceptionThrown, TEST_TIMESTAMP_ONE)
    networkConnectionUtil.setCurrentConnectionStatus(NetworkConnectionUtil.ConnectionStatus.NONE)
    exceptionsController.logFatalException(exceptionThrown, TEST_TIMESTAMP_ONE)

    val cachedExceptions = exceptionsController.getExceptionLogs()
    cachedExceptions.observeForever(mockOppiaExceptionLogsObserver)
    testCoroutineDispatchers.advanceUntilIdle()

    verify(mockOppiaExceptionLogsObserver, atLeastOnce())
      .onChanged(oppiaExceptionLogsResultCaptor.capture())
    val exceptionFromRemoteService = fakeExceptionLogger.getMostRecentException()
    val exceptionFromCacheStorage =
      oppiaExceptionLogsResultCaptor.value.getOrThrow().getExceptionLog(0)
    val exception = exceptionLogToException(exceptionFromCacheStorage)

    assertThat(exceptionFromRemoteService).isEqualTo(exceptionThrown)
    assertThat(exception.message).isEqualTo(exceptionThrown.message)
    assertThat(exception.stackTrace).isEqualTo(exceptionThrown.stackTrace)
    assertThat(exception.cause?.message).isEqualTo(exceptionThrown.cause?.message)
    assertThat(exception.cause?.stackTrace).isEqualTo(exceptionThrown.cause?.stackTrace)
    assertThat(exceptionFromCacheStorage.exceptionType).isEqualTo(ExceptionType.FATAL)
  }

  @ExperimentalCoroutinesApi
  @InternalCoroutinesApi
  @Test
  fun testController_logExceptions_withNoNetwork_verifyCachedInCorrectOrder() {
    networkConnectionUtil.setCurrentConnectionStatus(NetworkConnectionUtil.ConnectionStatus.NONE)
    val exceptionThrown = Exception("TEST", Throwable())
    exceptionsController.logNonFatalException(exceptionThrown, TEST_TIMESTAMP_ONE)
    exceptionsController.logFatalException(exceptionThrown, TEST_TIMESTAMP_ONE)

    val cachedExceptions = exceptionsController.getExceptionLogs()
    cachedExceptions.observeForever(mockOppiaExceptionLogsObserver)
    testCoroutineDispatchers.advanceUntilIdle()

    verify(mockOppiaExceptionLogsObserver, atLeastOnce())
      .onChanged(oppiaExceptionLogsResultCaptor.capture())
    val exceptionOne = oppiaExceptionLogsResultCaptor.value.getOrThrow().getExceptionLog(0)
    val exceptionTwo = oppiaExceptionLogsResultCaptor.value.getOrThrow().getExceptionLog(1)
    assertThat(exceptionOne.exceptionType).isEqualTo(ExceptionType.NON_FATAL)
    assertThat(exceptionTwo.exceptionType).isEqualTo(ExceptionType.FATAL)
  }

  private fun setUpTestApplicationComponent() {
    DaggerExceptionsControllerTest_TestApplicationComponent.builder()
      .setApplication(ApplicationProvider.getApplicationContext())
      .build()
      .inject(this)
  }

  private fun exceptionLogToException(exceptionLog: ExceptionLog): Exception {
    val exceptionMessage = if (exceptionLog.message != "") {
      exceptionLog.message
    } else {
      null
    }
    var exceptionCause: Throwable? = null
    if (exceptionLog.cause != null) {
      exceptionCause = if (exceptionLog.cause.message != "") {
        Throwable(exceptionLog.cause.message)
      } else {
        Throwable()
      }
    }
    exceptionCause?.let {
      it.stackTrace = createErrorStackTrace(exceptionLog.cause)
    }
    val exception = Exception(exceptionMessage, exceptionCause)
    exception.stackTrace = createErrorStackTrace(exceptionLog)
    return exception
  }

  private fun createErrorStackTrace(exceptionLog: ExceptionLog): Array<StackTraceElement> {
    return Array(
      exceptionLog.stacktraceElementCount,
      init = { i: Int ->
        StackTraceElement(
          exceptionLog.stacktraceElementList[i].declaringClass,
          exceptionLog.stacktraceElementList[i].methodName,
          exceptionLog.stacktraceElementList[i].fileName,
          exceptionLog.stacktraceElementList[i].lineNumber
        )
      }
    )
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

    fun inject(exceptionsControllerTest: ExceptionsControllerTest)
  }
}
