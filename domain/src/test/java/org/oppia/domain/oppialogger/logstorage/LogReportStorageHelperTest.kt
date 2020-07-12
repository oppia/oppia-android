package org.oppia.domain.oppialogger.logstorage

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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
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
import org.oppia.app.model.EventLog
import org.oppia.app.model.ExceptionLog
import org.oppia.app.model.OppiaEventLogs
import org.oppia.app.model.OppiaExceptionLogs
import org.oppia.domain.oppialogger.LogStorageModule
import org.oppia.testing.TestCoroutineDispatchers
import org.oppia.testing.TestDispatcherModule
import org.oppia.testing.TestLogReportingModule
import org.oppia.util.data.AsyncResult
import org.oppia.util.logging.EnableConsoleLog
import org.oppia.util.logging.EnableFileLog
import org.oppia.util.logging.GlobalLogLevel
import org.oppia.util.logging.LogLevel
import org.robolectric.annotation.Config
import javax.inject.Inject
import javax.inject.Qualifier
import javax.inject.Singleton

const val TEST_TIMESTAMP = 1556094120000

@RunWith(AndroidJUnit4::class)
@Config(manifest = Config.NONE)
class LogReportStorageHelperTest {

  @Rule
  @JvmField
  val mockitoRule: MockitoRule = MockitoJUnit.rule()

  @Inject
  lateinit var logReportStorageHelper: LogReportStorageHelper

  @InternalCoroutinesApi
  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @Mock
  lateinit var mockOppiaEventLogsObserver: Observer<AsyncResult<OppiaEventLogs>>

  @Captor
  lateinit var oppiaEventLogsResultCaptor: ArgumentCaptor<AsyncResult<OppiaEventLogs>>

  @Mock
  lateinit var mockOppiaExceptionLogsObserver: Observer<AsyncResult<OppiaExceptionLogs>>

  @Captor
  lateinit var oppiaExceptionLogsResultCaptor: ArgumentCaptor<AsyncResult<OppiaExceptionLogs>>

  private val eventLog = EventLog.newBuilder().setTimestamp(TEST_TIMESTAMP).build()

  private val exceptionLog = ExceptionLog.newBuilder().setMessage("TestException").build()

  @Inject
  @field:TestDispatcher
  lateinit var testDispatcher: CoroutineDispatcher

  // https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-test/
  @ObsoleteCoroutinesApi
  private val testThread = newSingleThreadContext("TestMain")

  @Before
  @ExperimentalCoroutinesApi
  @ObsoleteCoroutinesApi
  fun setUp() {
    Dispatchers.setMain(testThread)
    setUpTestApplicationComponent()
  }

  @After
  @ExperimentalCoroutinesApi
  @ObsoleteCoroutinesApi
  fun tearDown() {
    Dispatchers.resetMain()
    testThread.close()
  }

  @InternalCoroutinesApi
  @Test
  @ExperimentalCoroutinesApi
  fun testLogStorageHelper_addEventLog_addsLogInStore() {
    logReportStorageHelper.addEventLog(eventLog)
    val eventLogs = logReportStorageHelper.getEventLogs()

    eventLogs.observeForever(mockOppiaEventLogsObserver)
    testCoroutineDispatchers.advanceUntilIdle()

    verify(mockOppiaEventLogsObserver, atLeastOnce())
      .onChanged(oppiaEventLogsResultCaptor.capture())
    assertThat(
      oppiaEventLogsResultCaptor.value.getOrThrow()
        .getEventLog(0)
    ).isEqualTo(eventLog)
  }

  @InternalCoroutinesApi
  @Test
  @ExperimentalCoroutinesApi
  fun testLogStorageHelper_addExceptionLog_addsLogInStore() {
    logReportStorageHelper.addExceptionLog(exceptionLog)
    val exceptionLogs = logReportStorageHelper.getExceptionLogs()

    exceptionLogs.observeForever(mockOppiaExceptionLogsObserver)
    testCoroutineDispatchers.advanceUntilIdle()

    verify(mockOppiaExceptionLogsObserver, atLeastOnce())
      .onChanged(oppiaExceptionLogsResultCaptor.capture())
    assertThat(
      oppiaExceptionLogsResultCaptor.value.getOrThrow()
        .getExceptionLog(0)
    ).isEqualTo(exceptionLog)
  }

  private fun setUpTestApplicationComponent() {
    DaggerLogReportStorageHelperTest_TestApplicationComponent.builder()
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

    @ExperimentalCoroutinesApi
    @Singleton
    @Provides
    @TestDispatcher
    fun provideTestDispatcher(): CoroutineDispatcher {
      return TestCoroutineDispatcher()
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

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(
    modules = [
      TestDispatcherModule::class,
      TestModule::class,
      TestLogReportingModule::class,
      LogStorageModule::class
    ]
  )
  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun inject(logReportStorageHelperTest: LogReportStorageHelperTest)
  }
}