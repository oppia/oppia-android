package org.oppia.android.domain.oppialogger.exceptions

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
import org.oppia.android.domain.oppialogger.ExceptionLogStorageCacheSize
import org.oppia.android.testing.FakeExceptionLogger
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.data.DataProviderTestMonitor
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.caching.AssetModule
import org.oppia.android.util.data.DataProviders
import org.oppia.android.util.data.DataProvidersInjector
import org.oppia.android.util.data.DataProvidersInjectorProvider
import org.oppia.android.util.locale.LocaleProdModule
import org.oppia.android.util.logging.LoggerModule
import org.oppia.android.util.networking.NetworkConnectionDebugUtil
import org.oppia.android.util.networking.NetworkConnectionUtil.ProdConnectionStatus.NONE
import org.oppia.android.util.networking.NetworkConnectionUtilDebugModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Qualifier
import javax.inject.Singleton

// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = UncaughtExceptionLoggerStartupListenerTest.TestApplication::class)
class UncaughtExceptionLoggerStartupListenerTest {
  @Inject lateinit var dataProviders: DataProviders
  @Inject lateinit var uncaughtExceptionStartupListener: UncaughtExceptionLoggerStartupListener
  @Inject lateinit var networkConnectionUtil: NetworkConnectionDebugUtil
  @Inject lateinit var exceptionsController: ExceptionsController
  @Inject lateinit var fakeExceptionLogger: FakeExceptionLogger
  @Inject lateinit var monitorFactory: DataProviderTestMonitor.Factory

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testHandler_throwException_withNoNetwork_verifyLogInCache() {
    networkConnectionUtil.setCurrentConnectionStatus(NONE)
    val exceptionThrown = Exception("TEST")
    uncaughtExceptionStartupListener.uncaughtException(
      Thread.currentThread(),
      exceptionThrown
    )

    val cachedExceptions = exceptionsController.getExceptionLogStore()

    val exceptionLogs = monitorFactory.waitForNextSuccessfulResult(cachedExceptions)
    val exception = exceptionLogs.getExceptionLog(0).toException()
    assertThat(exception.message).matches("java.lang.Exception: TEST")
  }

  @Test
  fun testHandler_throwException_withNetwork_verifyLogToRemoteService() {
    val exceptionThrown = Exception("TEST")
    uncaughtExceptionStartupListener.uncaughtException(
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
      TestModule::class, TestLogReportingModule::class, RobolectricModule::class,
      TestDispatcherModule::class, TestLogStorageModule::class, FakeOppiaClockModule::class,
      NetworkConnectionUtilDebugModule::class, LocaleProdModule::class, LoggerModule::class,
      AssetModule::class, UncaughtExceptionLoggerModule::class
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
