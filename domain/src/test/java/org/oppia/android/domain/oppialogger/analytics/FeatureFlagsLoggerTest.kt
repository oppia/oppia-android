package org.oppia.android.domain.oppialogger.analytics

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.app.model.EventLog.FeatureFlagContext.FeatureFlagItem
import org.oppia.android.app.model.PlatformParameter.SyncStatus
import org.oppia.android.domain.oppialogger.EventLogStorageCacheSize
import org.oppia.android.domain.oppialogger.ExceptionLogStorageCacheSize
import org.oppia.android.domain.oppialogger.LoggingIdentifierModule
import org.oppia.android.domain.platformparameter.PlatformParameterSingletonModule
import org.oppia.android.testing.FakeAnalyticsEventLogger
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.logging.EventLogSubject.Companion.assertThat
import org.oppia.android.testing.logging.SyncStatusTestModule
import org.oppia.android.testing.platformparameter.TestPlatformParameterModule
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.caching.AssetModule
import org.oppia.android.util.data.DataProvidersInjector
import org.oppia.android.util.data.DataProvidersInjectorProvider
import org.oppia.android.util.locale.LocaleProdModule
import org.oppia.android.util.logging.EnableConsoleLog
import org.oppia.android.util.logging.EnableFileLog
import org.oppia.android.util.logging.GlobalLogLevel
import org.oppia.android.util.logging.LogLevel
import org.oppia.android.util.networking.NetworkConnectionUtilDebugModule
import org.oppia.android.util.platformparameter.DOWNLOADS_SUPPORT
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [FeatureFlagsLogger]. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = FeatureFlagsLoggerTest.TestApplication::class)
class FeatureFlagsLoggerTest {
  private companion object {
    private const val TEST_SESSION_ID = "test_session_id"
    private val INITIAL_SYNC_STATUS = SyncStatus.NOT_SYNCED_FROM_SERVER
    private val testEnableDownloadsSupportFeatureFlagItem = FeatureFlagItem.newBuilder()
      .setFlagName(DOWNLOADS_SUPPORT)
      .setFlagEnabledState(false)
      .setFlagSyncStatus(INITIAL_SYNC_STATUS)
      .build()
  }

  @Inject lateinit var testCoroutineDispatchers: TestCoroutineDispatchers
  @Inject lateinit var featureFlagsLogger: FeatureFlagsLogger
  @Inject lateinit var fakeAnalyticsEventLogger: FakeAnalyticsEventLogger

  @Before
  fun setup() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testLogFeatureFlags_logFeatureFlags_hasCorrectSessionId() {
    featureFlagsLogger.logAllFeatureFlags(TEST_SESSION_ID)
    testCoroutineDispatchers.runCurrent()

    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()

    assertThat(eventLog).hasFeatureFlagContextThat {
      hasSessionIdThat().isEqualTo(TEST_SESSION_ID)
    }
  }

  @Test
  fun testLogFeatureFlags_logsFeatureFlagsWithCorrectDefaultValues() {
    featureFlagsLogger.logAllFeatureFlags(TEST_SESSION_ID)
    testCoroutineDispatchers.runCurrent()

    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()

    assertThat(eventLog).hasFeatureFlagContextThat {
      hasFeatureFlagWithNameThat(DOWNLOADS_SUPPORT).isEqualTo(DOWNLOADS_SUPPORT)
    }
    assertThat(eventLog).hasFeatureFlagContextThat {
      hasNamedFeatureWithEnabledStateThat(DOWNLOADS_SUPPORT).isEqualTo(false)
    }
    assertThat(eventLog).hasFeatureFlagContextThat {
      hasNamedFeatureWithSyncStatusThat(DOWNLOADS_SUPPORT).isEqualTo(INITIAL_SYNC_STATUS)
    }
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
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
  }

  @Module
  class TestLogStorageModule {
    @Provides
    @EventLogStorageCacheSize
    fun provideEventLogStorageCacheSize(): Int = 2

    @Provides
    @ExceptionLogStorageCacheSize
    fun provideExceptionLogStorageCacheSize(): Int = 2
  }

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(
    modules = [
      TestModule::class, TestLogReportingModule::class, RobolectricModule::class,
      TestDispatcherModule::class, TestLogStorageModule::class,
      NetworkConnectionUtilDebugModule::class, LocaleProdModule::class, FakeOppiaClockModule::class,
      TestPlatformParameterModule::class, PlatformParameterSingletonModule::class,
      LoggingIdentifierModule::class, SyncStatusTestModule::class, AssetModule::class
    ]
  )

  interface TestApplicationComponent : DataProvidersInjector {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder
      fun build(): TestApplicationComponent
    }

    fun inject(featureFlagLoggerTest: FeatureFlagsLoggerTest)
  }

  class TestApplication : Application(), DataProvidersInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerFeatureFlagsLoggerTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(featureFlagLoggerTest: FeatureFlagsLoggerTest) {
      component.inject(featureFlagLoggerTest)
    }

    override fun getDataProvidersInjector(): DataProvidersInjector = component
  }
}
