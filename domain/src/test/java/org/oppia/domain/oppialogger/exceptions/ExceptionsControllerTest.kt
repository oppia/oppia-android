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
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
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
import org.oppia.app.model.ExceptionLog.ExceptionType
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
import org.oppia.util.logging.GlobalLogLevel
import org.oppia.util.logging.LogLevel
import org.oppia.util.networking.NetworkConnectionUtil
import org.robolectric.annotation.Config
import javax.inject.Inject
import javax.inject.Qualifier
import javax.inject.Singleton

private const val TEST_TIMESTAMP_IN_MILLIS_ONE = 1556094120000
private const val TEST_TIMESTAMP_IN_MILLIS_TWO = 1556094110000
private const val TEST_TIMESTAMP_IN_MILLIS_THREE = 1556094100000
private const val TEST_TIMESTAMP_IN_MILLIS_FOUR = 1556094000000

@RunWith(AndroidJUnit4::class)
@Config(manifest = Config.NONE)
class ExceptionsControllerTest {

  @Rule
  @JvmField
  val mockitoRule: MockitoRule = MockitoJUnit.rule()

  @Inject
  lateinit var dataProviders: DataProviders

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
  @ExperimentalCoroutinesApi
  @ObsoleteCoroutinesApi
  fun setUp() {
    networkConnectionUtil = NetworkConnectionUtil(ApplicationProvider.getApplicationContext())
    setUpTestApplicationComponent()
  }

  @Test
  fun testController_logException_nonFatal_logsToRemoteService() {
    val exceptionThrown = Exception("TEST MESSAGE", Throwable())
    exceptionsController.logNonFatalException(exceptionThrown, TEST_TIMESTAMP_IN_MILLIS_ONE)

    val exceptionLogged = fakeExceptionLogger.getMostRecentException()

    assertThat(exceptionLogged).isEqualTo(exceptionThrown)
  }

  @Test
  fun testController_logFatalException_logsToRemoteService() {
    val exceptionThrown = Exception("TEST MESSAGE", Throwable())
    exceptionsController.logFatalException(exceptionThrown, TEST_TIMESTAMP_IN_MILLIS_ONE)

    val exceptionLogged = fakeExceptionLogger.getMostRecentException()

    assertThat(exceptionLogged).isEqualTo(exceptionThrown)
  }

  // TODO(#1106): Addition of tests tracking behaviour of the controller after uploading of logs to the remote service.

  @ExperimentalCoroutinesApi
  @InternalCoroutinesApi
  @Test
  fun testController_logException_nonFatal_withNoNetwork_logsToCacheStore() {
    val exceptionThrown = Exception("TEST MESSAGE", Throwable("TEST CAUSE"))
    networkConnectionUtil.setCurrentConnectionStatus(NetworkConnectionUtil.ConnectionStatus.NONE)
    exceptionsController.logNonFatalException(exceptionThrown, TEST_TIMESTAMP_IN_MILLIS_ONE)

    val cachedExceptions =
      dataProviders.convertToLiveData(exceptionsController.getExceptionLogStore())
    cachedExceptions.observeForever(mockOppiaExceptionLogsObserver)
    testCoroutineDispatchers.advanceUntilIdle()

    verify(
      mockOppiaExceptionLogsObserver,
      atLeastOnce()
    ).onChanged(oppiaExceptionLogsResultCaptor.capture())

    val exceptionLog = oppiaExceptionLogsResultCaptor.value.getOrThrow().getExceptionLog(0)
    val exception = exceptionLog.toException()
    assertThat(exception.message).isEqualTo(exceptionThrown.message)
    assertThat(exception.stackTrace).isEqualTo(exceptionThrown.stackTrace)
    assertThat(exception.cause?.message).isEqualTo(exceptionThrown.cause?.message)
    assertThat(exception.cause?.stackTrace).isEqualTo(exceptionThrown.cause?.stackTrace)
    assertThat(exceptionLog.exceptionType).isEqualTo(ExceptionType.NON_FATAL)
  }

  @ExperimentalCoroutinesApi
  @InternalCoroutinesApi
  @Test
  fun testController_logFatalException_withNoNetwork_logsToCacheStore() {
    networkConnectionUtil.setCurrentConnectionStatus(NetworkConnectionUtil.ConnectionStatus.NONE)
    val exceptionThrown = Exception("TEST MESSAGE", Throwable("TEST"))
    exceptionsController.logFatalException(exceptionThrown, TEST_TIMESTAMP_IN_MILLIS_ONE)

    val cachedExceptions =
      dataProviders.convertToLiveData(exceptionsController.getExceptionLogStore())
    cachedExceptions.observeForever(mockOppiaExceptionLogsObserver)
    testCoroutineDispatchers.advanceUntilIdle()

    verify(mockOppiaExceptionLogsObserver, atLeastOnce())
      .onChanged(oppiaExceptionLogsResultCaptor.capture())

    val exceptionLog = oppiaExceptionLogsResultCaptor.value.getOrThrow().getExceptionLog(0)
    val exception = exceptionLog.toException()
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

    exceptionsController.logFatalException(
      Exception("TEST1", Throwable("ONE")),
      TEST_TIMESTAMP_IN_MILLIS_ONE
    )
    exceptionsController.logNonFatalException(
      Exception("TEST2", Throwable("TWO")),
      TEST_TIMESTAMP_IN_MILLIS_TWO
    )
    exceptionsController.logFatalException(
      Exception("TEST3", Throwable("THREE")),
      TEST_TIMESTAMP_IN_MILLIS_THREE
    )
    exceptionsController.logFatalException(
      Exception("TEST4", Throwable("FOUR")),
      TEST_TIMESTAMP_IN_MILLIS_FOUR
    )

    val cachedExceptions =
      dataProviders.convertToLiveData(exceptionsController.getExceptionLogStore())
    cachedExceptions.observeForever(mockOppiaExceptionLogsObserver)
    testCoroutineDispatchers.advanceUntilIdle()

    verify(mockOppiaExceptionLogsObserver, atLeastOnce())
      .onChanged(oppiaExceptionLogsResultCaptor.capture())

    val exceptionOne = oppiaExceptionLogsResultCaptor.value.getOrThrow().getExceptionLog(0)
    val exceptionTwo = oppiaExceptionLogsResultCaptor.value.getOrThrow().getExceptionLog(1)

    // In this case, 3 fatal and 1 non-fatal exceptions were logged. The order of logging was fatal->non-fatal->fatal->fatal.
    // So after pruning, none of the retained logs should have non-fatal exception type.
    assertThat(exceptionOne.exceptionType).isNotEqualTo(ExceptionType.NON_FATAL)
    assertThat(exceptionTwo.exceptionType).isNotEqualTo(ExceptionType.NON_FATAL)
    // If we analyse the order of logging of exceptions, we can see that record pruning will begin from the logging of the third record.
    // At first, the second exception log will be removed as it has non-fatal exception type and the exception logged at the third place will become the exception record at the second place in the store.
    // When the forth exception gets logged then the pruning will be purely based on timestamp of the exception as both exception logs have fatal exception type.
    // As the third exceptions's timestamp was lesser than that of the first event, it will be pruned from the store and the forth exception will become the second exception in the store.
    assertThat(exceptionOne.timestampInMillis).isEqualTo(TEST_TIMESTAMP_IN_MILLIS_ONE)
    assertThat(exceptionTwo.timestampInMillis).isEqualTo(TEST_TIMESTAMP_IN_MILLIS_FOUR)
    assertThat(exceptionOne.message).isEqualTo("TEST1")
    assertThat(exceptionTwo.message).isEqualTo("TEST4")
  }

  @ExperimentalCoroutinesApi
  @InternalCoroutinesApi
  @Test
  fun testController_logExceptions_exceedLimit_cacheSizeDoesNotExceedLimit() {
    networkConnectionUtil.setCurrentConnectionStatus(NetworkConnectionUtil.ConnectionStatus.NONE)

    exceptionsController.logFatalException(
      Exception("TEST1", Throwable("ONE")),
      TEST_TIMESTAMP_IN_MILLIS_ONE
    )
    exceptionsController.logNonFatalException(
      Exception("TEST2", Throwable("TWO")),
      TEST_TIMESTAMP_IN_MILLIS_TWO
    )
    exceptionsController.logFatalException(
      Exception("TEST3", Throwable("THREE")),
      TEST_TIMESTAMP_IN_MILLIS_THREE
    )

    val cachedExceptions =
      dataProviders.convertToLiveData(exceptionsController.getExceptionLogStore())
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
    exceptionsController.logNonFatalException(exceptionThrown, TEST_TIMESTAMP_IN_MILLIS_ONE)
    networkConnectionUtil.setCurrentConnectionStatus(NetworkConnectionUtil.ConnectionStatus.NONE)
    exceptionsController.logFatalException(exceptionThrown, TEST_TIMESTAMP_IN_MILLIS_ONE)

    val cachedExceptions =
      dataProviders.convertToLiveData(exceptionsController.getExceptionLogStore())
    cachedExceptions.observeForever(mockOppiaExceptionLogsObserver)
    testCoroutineDispatchers.advanceUntilIdle()

    verify(mockOppiaExceptionLogsObserver, atLeastOnce())
      .onChanged(oppiaExceptionLogsResultCaptor.capture())
    val exceptionFromRemoteService = fakeExceptionLogger.getMostRecentException()
    val exceptionFromCacheStorage =
      oppiaExceptionLogsResultCaptor.value.getOrThrow().getExceptionLog(0)
    val exception = exceptionFromCacheStorage.toException()

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
    exceptionsController.logNonFatalException(exceptionThrown, TEST_TIMESTAMP_IN_MILLIS_ONE)
    exceptionsController.logFatalException(exceptionThrown, TEST_TIMESTAMP_IN_MILLIS_ONE)

    val cachedExceptions =
      dataProviders.convertToLiveData(exceptionsController.getExceptionLogStore())
    cachedExceptions.observeForever(mockOppiaExceptionLogsObserver)
    testCoroutineDispatchers.advanceUntilIdle()

    verify(mockOppiaExceptionLogsObserver, atLeastOnce())
      .onChanged(oppiaExceptionLogsResultCaptor.capture())
    val exceptionOne = oppiaExceptionLogsResultCaptor.value.getOrThrow().getExceptionLog(0)
    val exceptionTwo = oppiaExceptionLogsResultCaptor.value.getOrThrow().getExceptionLog(1)
    assertThat(exceptionOne.exceptionType).isEqualTo(ExceptionType.NON_FATAL)
    assertThat(exceptionTwo.exceptionType).isEqualTo(ExceptionType.FATAL)
  }

  @ExperimentalCoroutinesApi
  @InternalCoroutinesApi
  @Test
  fun testExtension_logEmptyException_withNoNetwork_verifyRecreationOfLogs() {
    networkConnectionUtil.setCurrentConnectionStatus(NetworkConnectionUtil.ConnectionStatus.NONE)
    val exceptionThrown = Exception()
    exceptionsController.logNonFatalException(exceptionThrown, TEST_TIMESTAMP_IN_MILLIS_ONE)

    val cachedExceptions =
      dataProviders.convertToLiveData(exceptionsController.getExceptionLogStore())
    cachedExceptions.observeForever(mockOppiaExceptionLogsObserver)
    testCoroutineDispatchers.advanceUntilIdle()

    verify(mockOppiaExceptionLogsObserver, atLeastOnce())
      .onChanged(oppiaExceptionLogsResultCaptor.capture())
    val exceptionLog = oppiaExceptionLogsResultCaptor.value.getOrThrow().getExceptionLog(0)
    val exception = exceptionLog.toException()

    assertThat(exception.message).isEqualTo(null)
    assertThat(exception.stackTrace).isEqualTo(exceptionThrown.stackTrace)
    assertThat(exception.cause).isEqualTo(null)
  }

  @ExperimentalCoroutinesApi
  @InternalCoroutinesApi
  @Test
  fun testExtension_logException_withNoCause_withNoNetwork_verifyRecreationOfLogs() {
    networkConnectionUtil.setCurrentConnectionStatus(NetworkConnectionUtil.ConnectionStatus.NONE)
    val exceptionThrown = Exception("TEST")
    exceptionsController.logNonFatalException(exceptionThrown, TEST_TIMESTAMP_IN_MILLIS_ONE)

    val cachedExceptions =
      dataProviders.convertToLiveData(exceptionsController.getExceptionLogStore())
    cachedExceptions.observeForever(mockOppiaExceptionLogsObserver)
    testCoroutineDispatchers.advanceUntilIdle()

    verify(mockOppiaExceptionLogsObserver, atLeastOnce())
      .onChanged(oppiaExceptionLogsResultCaptor.capture())
    val exceptionLog = oppiaExceptionLogsResultCaptor.value.getOrThrow().getExceptionLog(0)
    val exception = exceptionLog.toException()

    assertThat(exception.message).isEqualTo("TEST")
    assertThat(exception.stackTrace).isEqualTo(exceptionThrown.stackTrace)
    assertThat(exception.cause).isEqualTo(null)
  }

  private fun setUpTestApplicationComponent() {
    DaggerExceptionsControllerTest_TestApplicationComponent.builder()
      .setApplication(ApplicationProvider.getApplicationContext())
      .build()
      .inject(this)
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
