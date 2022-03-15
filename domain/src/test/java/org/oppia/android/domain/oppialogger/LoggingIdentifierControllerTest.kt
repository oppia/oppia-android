package org.oppia.android.domain.oppialogger

import android.app.Application
import android.content.Context
import android.content.Context.WIFI_SERVICE
import android.net.wifi.WifiManager
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
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import org.oppia.android.domain.platformparameter.PlatformParameterSingletonModule
import org.oppia.android.testing.FakeUUIDImpl
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.data.AsyncDataSubscriptionManager
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import org.oppia.android.util.data.DataProvidersInjector
import org.oppia.android.util.data.DataProvidersInjectorProvider
import org.oppia.android.util.locale.LocaleProdModule
import org.oppia.android.util.logging.EnableConsoleLog
import org.oppia.android.util.logging.EnableFileLog
import org.oppia.android.util.logging.GlobalLogLevel
import org.oppia.android.util.logging.LogLevel
import org.oppia.android.util.networking.NetworkConnectionUtilDebugModule
import org.oppia.android.util.platformparameter.ENABLE_LANGUAGE_SELECTION_UI_DEFAULT_VALUE
import org.oppia.android.util.platformparameter.EnableLanguageSelectionUi
import org.oppia.android.util.platformparameter.LearnerStudyAnalytics
import org.oppia.android.util.platformparameter.PlatformParameterValue
import org.oppia.android.util.platformparameter.SPLASH_SCREEN_WELCOME_MSG_DEFAULT_VALUE
import org.oppia.android.util.platformparameter.SYNC_UP_WORKER_TIME_PERIOD_IN_HOURS_DEFAULT_VALUE
import org.oppia.android.util.platformparameter.SplashScreenWelcomeMsg
import org.oppia.android.util.platformparameter.SyncUpWorkerTimePeriodHours
import org.oppia.android.util.system.UserIdGenerator
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

private const val TEST_UUID = "test_uuid"
private const val TEST_MAC_ADDRESS = "test_mac_address"

@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = LoggingIdentifierControllerTest.TestApplication::class)
class LoggingIdentifierControllerTest {
  @Rule
  @JvmField
  val mockitoRule: MockitoRule = MockitoJUnit.rule()

  @Inject
  lateinit var loggingIdentifierController: LoggingIdentifierController

  @Inject
  lateinit var asyncDataSubscriptionManager: AsyncDataSubscriptionManager

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @Inject
  lateinit var fakeUUIDImpl: FakeUUIDImpl

  @Inject
  lateinit var context: Context

  @Mock
  lateinit var mockStringLiveDataObserver: Observer<AsyncResult<String>>

  @Captor
  lateinit var stringResultCaptor: ArgumentCaptor<AsyncResult<String>>

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testController_createLearnerId_verifyCreatesCorrectRandom() {
    val randomLearnerId = loggingIdentifierController.createLearnerId()
    val testLearnerId =
      String.format("%08x", java.util.Random(TestLoggingIdentifierModule.deviceIdSeed).nextInt())
    assertThat(randomLearnerId.length).isEqualTo(8)
    assertThat(randomLearnerId).isEqualTo(testLearnerId)
  }

  @Test
  fun testController_createSessionId_verifyReturnsRandomUUID() {
    val sessionId = loggingIdentifierController.getSessionId().toLiveData()
    sessionId.observeForever(mockStringLiveDataObserver)
    testCoroutineDispatchers.advanceUntilIdle()

    verify(mockStringLiveDataObserver).onChanged(stringResultCaptor.capture())
    assertThat(stringResultCaptor.value.getOrThrow()).isEqualTo(fakeUUIDImpl.getUUIDValue())
  }

  @Test
  fun testController_createSessionId_updateSessionId_verifyReturnsUpdatedValue() {
    val sessionId = loggingIdentifierController.getSessionId()
    sessionId.toLiveData().observeForever(mockStringLiveDataObserver)

    fakeUUIDImpl.setUUIDValue(TEST_UUID)
    loggingIdentifierController.updateSessionId()
    testCoroutineDispatchers.advanceUntilIdle()

    verify(mockStringLiveDataObserver).onChanged(stringResultCaptor.capture())
    assertThat(stringResultCaptor.value.getOrThrow()).isEqualTo(TEST_UUID)
  }

  @Test
  fun testController_createDeviceId_verifyReturnsCorrectDeviceId() {
    val wifiManager = context.applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
    val wifiInfo = wifiManager.connectionInfo
    shadowOf(wifiInfo).setMacAddress(TEST_MAC_ADDRESS)

    val deviceId = loggingIdentifierController.deviceId
    val expectedHash = MessageDigest.getInstance("SHA-1")
      .digest(TEST_MAC_ADDRESS.toByteArray())
      .joinToString("") { "%02x".format(it) }.substring(startIndex = 0, endIndex = 12)

    assertThat(deviceId).isEqualTo(expectedHash)
    assertThat(deviceId.length).isEqualTo(12)
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>()
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
  }

  @Module
  class TestLogStorageModule {

    @Provides
    @EventLogStorageCacheSize
    fun provideEventLogStorageCacheSize(): Int = 2
  }

  @Module
  class TestLoggingIdentifierModule {

    companion object {
      const val deviceIdSeed = 1L
    }

    @Provides
    @DeviceIdSeed
    fun provideDeviceIdSeed(): Long = deviceIdSeed

    @Provides
    fun provideUUIDWrapper(fakeUUIDImpl: FakeUUIDImpl): UserIdGenerator = fakeUUIDImpl
  }

  @Module
  class TestPlatformParameterModule {

    companion object {
      var forceLearnerAnalyticsStudy: Boolean = false
    }

    @Provides
    @SplashScreenWelcomeMsg
    fun provideSplashScreenWelcomeMsgParam(): PlatformParameterValue<Boolean> {
      return PlatformParameterValue.createDefaultParameter(SPLASH_SCREEN_WELCOME_MSG_DEFAULT_VALUE)
    }

    @Provides
    @SyncUpWorkerTimePeriodHours
    fun provideSyncUpWorkerTimePeriod(): PlatformParameterValue<Int> {
      return PlatformParameterValue.createDefaultParameter(
        SYNC_UP_WORKER_TIME_PERIOD_IN_HOURS_DEFAULT_VALUE
      )
    }

    @Provides
    @EnableLanguageSelectionUi
    fun provideEnableLanguageSelectionUi(): PlatformParameterValue<Boolean> {
      return PlatformParameterValue.createDefaultParameter(
        ENABLE_LANGUAGE_SELECTION_UI_DEFAULT_VALUE
      )
    }

    @Provides
    @LearnerStudyAnalytics
    fun provideLearnerStudyAnalytics(): PlatformParameterValue<Boolean> {
      return PlatformParameterValue.createDefaultParameter(forceLearnerAnalyticsStudy)
    }
  }

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(
    modules = [
      TestModule::class, TestLogReportingModule::class, TestLogStorageModule::class,
      TestDispatcherModule::class, RobolectricModule::class, FakeOppiaClockModule::class,
      NetworkConnectionUtilDebugModule::class, LocaleProdModule::class,
      TestPlatformParameterModule::class, PlatformParameterSingletonModule::class,
      TestLoggingIdentifierModule::class
    ]
  )
  interface TestApplicationComponent : DataProvidersInjector {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder
      fun build(): TestApplicationComponent
    }

    fun inject(loggingIdentifierControllerTest: LoggingIdentifierControllerTest)
  }

  class TestApplication : Application(), DataProvidersInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerLoggingIdentifierControllerTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(loggingIdentifierControllerTest: LoggingIdentifierControllerTest) {
      component.inject(loggingIdentifierControllerTest)
    }

    override fun getDataProvidersInjector(): DataProvidersInjector = component
  }
}
