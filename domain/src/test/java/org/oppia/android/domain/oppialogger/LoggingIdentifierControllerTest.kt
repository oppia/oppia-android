package org.oppia.android.domain.oppialogger

import android.app.Application
import android.app.Instrumentation
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.google.protobuf.MessageLite
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.app.model.DeviceContextDatabase
import org.oppia.android.domain.oppialogger.analytics.ApplicationLifecycleModule
import org.oppia.android.domain.platformparameter.PlatformParameterSingletonModule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.data.DataProviderTestMonitor
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.data.AsyncDataSubscriptionManager
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProvidersInjector
import org.oppia.android.util.data.DataProvidersInjectorProvider
import org.oppia.android.util.locale.LocaleProdModule
import org.oppia.android.util.locale.OppiaLocale
import org.oppia.android.util.logging.EnableConsoleLog
import org.oppia.android.util.logging.EnableFileLog
import org.oppia.android.util.logging.GlobalLogLevel
import org.oppia.android.util.logging.LogLevel
import org.oppia.android.util.logging.SyncStatusModule
import org.oppia.android.util.networking.NetworkConnectionUtilDebugModule
import org.oppia.android.util.platformparameter.EnableLearnerStudyAnalytics
import org.oppia.android.util.platformparameter.PlatformParameterValue
import org.oppia.android.util.platformparameter.SPLASH_SCREEN_WELCOME_MSG_DEFAULT_VALUE
import org.oppia.android.util.platformparameter.SYNC_UP_WORKER_TIME_PERIOD_IN_HOURS_DEFAULT_VALUE
import org.oppia.android.util.platformparameter.SplashScreenWelcomeMsg
import org.oppia.android.util.platformparameter.SyncUpWorkerTimePeriodHours
import org.oppia.android.util.threading.BackgroundDispatcher
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import java.io.File
import java.io.FileOutputStream
import java.lang.IllegalStateException
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [LoggingIdentifierController]. */
// Same parameter value: helpers reduce test context, even if they are used by 1 test.
// Function name: test names are conventionally named with underscores.
@Suppress("SameParameterValue", "FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = LoggingIdentifierControllerTest.TestApplication::class)
class LoggingIdentifierControllerTest {
  @Inject lateinit var loggingIdentifierController: LoggingIdentifierController
  @Inject lateinit var asyncDataSubscriptionManager: AsyncDataSubscriptionManager
  @Inject lateinit var context: Context
  @Inject lateinit var machineLocale: OppiaLocale.MachineLocale
  @Inject lateinit var testCoroutineDispatchers: TestCoroutineDispatchers
  @Inject lateinit var monitorFactory: DataProviderTestMonitor.Factory
  @field:[BackgroundDispatcher Inject] lateinit var backgroundDispatcher: CoroutineDispatcher

  @Before
  fun setUp() {
    TestLoggingIdentifierModule.applicationIdSeed = INITIAL_APPLICATION_ID
    setUpTestApplicationComponent()
  }

  @Test
  fun testCreateLearnerId_twice_bothAreDifferent() {
    val learnerId1 = loggingIdentifierController.createLearnerId()
    val learnerId2 = loggingIdentifierController.createLearnerId()

    // There's no actual good way to test the generator beyond just verifying that two subsequent
    // IDs are different. Note that this technically has an extremely slim chance to flake, but it
    // realistically should never happen.
    assertThat(learnerId1).isNotEqualTo(learnerId2)
  }

  @Test
  fun testGetInstallationId_initialAppState_providerReturnsNewInstallationIdValue() {
    val installationId =
      monitorFactory.waitForNextSuccessfulResult(loggingIdentifierController.getInstallationId())

    assertThat(installationId).isEqualTo("1d079efe67ee")
    assertThat(installationId.length).isEqualTo(12)
  }

  @Test
  fun testGetInstallationId_secondAppOpen_providerReturnsSameInstallationIdValue() {
    monitorFactory.ensureDataProviderExecutes(loggingIdentifierController.getInstallationId())
    TestLoggingIdentifierModule.applicationIdSeed = SECOND_APP_OPEN_APPLICATION_ID
    setUpNewTestApplicationComponent() // Simulate an app re-open with a new app ID.

    val installationId =
      monitorFactory.waitForNextSuccessfulResult(loggingIdentifierController.getInstallationId())

    // The same value should return for the second instance of the controller.
    assertThat(installationId).isEqualTo("1d079efe67ee")
  }

  @Test
  fun testGetInstallationId_secondAppOpen_emptiedDatabase_providerReturnsEmptyString() {
    // Simulate initing the installation ID, then emptying/corrupting it, then reopening the app.
    monitorFactory.ensureDataProviderExecutes(loggingIdentifierController.getInstallationId())
    writeFileCache("device_context_database", DeviceContextDatabase.getDefaultInstance())
    TestLoggingIdentifierModule.applicationIdSeed = SECOND_APP_OPEN_APPLICATION_ID
    setUpNewTestApplicationComponent() // Simulate an app re-open with a new app ID.

    val installationId =
      monitorFactory.waitForNextSuccessfulResult(loggingIdentifierController.getInstallationId())

    // If the file was emptied, no installation ID can be loaded (this is a critical failure case).
    assertThat(installationId).isEmpty()
  }

  @Test
  fun testGetInstallationId_secondAppOpen_deletedDatabase_providerReturnsNewInstallationIdValue() {
    // Simulate initing the installation ID, then deleting it, then reopening the app.
    monitorFactory.ensureDataProviderExecutes(loggingIdentifierController.getInstallationId())
    deleteCacheFile("device_context_database")
    TestLoggingIdentifierModule.applicationIdSeed = SECOND_APP_OPEN_APPLICATION_ID
    setUpNewTestApplicationComponent() // Simulate an app re-open with a new app ID.

    val installationId =
      monitorFactory.waitForNextSuccessfulResult(loggingIdentifierController.getInstallationId())

    // It should seem like a reinstallation since the app's data has been cleared after restarting.
    assertThat(installationId).isEqualTo("a52e69fcfedc")
    assertThat(installationId.length).isEqualTo(12)
  }

  @Test
  fun testFetchInstallationId_initialAppState_returnsNewInstallationIdValue() {
    val installationId = fetchSuccessfulAsyncValue(loggingIdentifierController::fetchInstallationId)

    assertThat(installationId).isEqualTo("1d079efe67ee")
    assertThat(installationId?.length).isEqualTo(12)
  }

  @Test
  fun testFetchInstallationId_secondAppOpen_returnsSameInstallationIdValue() {
    monitorFactory.ensureDataProviderExecutes(loggingIdentifierController.getInstallationId())
    TestLoggingIdentifierModule.applicationIdSeed = SECOND_APP_OPEN_APPLICATION_ID
    setUpNewTestApplicationComponent() // Simulate an app re-open with a new app ID.

    val installationId = fetchSuccessfulAsyncValue(loggingIdentifierController::fetchInstallationId)

    // The same value should return for the second instance of the controller.
    assertThat(installationId).isEqualTo("1d079efe67ee")
  }

  @Test
  fun testFetchInstallationId_secondAppOpen_emptiedDatabase_returnsNull() {
    // Simulate initing the installation ID, then emptying/corrupting it, then reopening the app.
    monitorFactory.ensureDataProviderExecutes(loggingIdentifierController.getInstallationId())
    writeFileCache("device_context_database", DeviceContextDatabase.getDefaultInstance())
    TestLoggingIdentifierModule.applicationIdSeed = SECOND_APP_OPEN_APPLICATION_ID
    setUpNewTestApplicationComponent() // Simulate an app re-open with a new app ID.

    val installationId = fetchSuccessfulAsyncValue(loggingIdentifierController::fetchInstallationId)

    // If the file was emptied, no installation ID can be loaded (this is a critical failure case).
    assertThat(installationId).isNull()
  }

  @Test
  fun testFetchInstallationId_secondAppOpen_deletedDatabase_returnsNewInstallationIdValue() {
    // Simulate initing the installation ID, then deleting it, then reopening the app.
    monitorFactory.ensureDataProviderExecutes(loggingIdentifierController.getInstallationId())
    deleteCacheFile("device_context_database")
    TestLoggingIdentifierModule.applicationIdSeed = SECOND_APP_OPEN_APPLICATION_ID
    setUpNewTestApplicationComponent() // Simulate an app re-open with a new app ID.
    monitorFactory.ensureDataProviderExecutes(loggingIdentifierController.getInstallationId())

    val installationId = fetchSuccessfulAsyncValue(loggingIdentifierController::fetchInstallationId)

    // The installation ID is null since the database was overwritten.
    assertThat(installationId).isEqualTo("a52e69fcfedc")
    assertThat(installationId?.length).isEqualTo(12)
  }

  @Test
  fun testGetSessionId_initialState_returnsRandomId() {
    val sessionIdProvider = loggingIdentifierController.getSessionId()

    val sessionId = monitorFactory.waitForNextSuccessfulResult(sessionIdProvider)
    assertThat(sessionId).isEqualTo("4d0a66f3-82b6-3aa9-8f61-140bdd5f49d3")
  }

  @Test
  fun testGetSessionId_secondCall_returnsSameRandomId() {
    monitorFactory.ensureDataProviderExecutes(loggingIdentifierController.getSessionId())

    val sessionIdProvider = loggingIdentifierController.getSessionId()

    // The second call should return the same ID (since the ID doesn't automatically change).
    val sessionId = monitorFactory.waitForNextSuccessfulResult(sessionIdProvider)
    assertThat(sessionId).isEqualTo("4d0a66f3-82b6-3aa9-8f61-140bdd5f49d3")
  }

  @Test
  fun testGetSessionId_secondAppOpen_returnsNewRandomId() {
    monitorFactory.ensureDataProviderExecutes(loggingIdentifierController.getSessionId())
    TestLoggingIdentifierModule.applicationIdSeed = SECOND_APP_OPEN_APPLICATION_ID
    setUpNewTestApplicationComponent() // Simulate an app re-open with a new app ID.

    val sessionIdProvider = loggingIdentifierController.getSessionId()

    // The second call should return the same ID (since the ID doesn't automatically change).
    val sessionId = monitorFactory.waitForNextSuccessfulResult(sessionIdProvider)
    assertThat(sessionId).isEqualTo("18c2816d-f7ad-312f-b696-d3fdd51f2e92")
  }

  @Test
  fun testGetSessionIdFlow_initialState_returnsFlowWithRandomId() {
    val sessionIdFlow = loggingIdentifierController.getSessionIdFlow()

    val sessionId = sessionIdFlow.waitForLatestValue()
    assertThat(sessionId).isEqualTo("4d0a66f3-82b6-3aa9-8f61-140bdd5f49d3")
  }

  @Test
  fun testGetSessionIdFlow_secondCall_returnsFlowWithSameRandomId() {
    loggingIdentifierController.getSessionIdFlow().waitForLatestValue()

    val sessionIdFlow = loggingIdentifierController.getSessionIdFlow()

    // The second call should return the same ID (since the ID doesn't automatically change).
    val sessionId = sessionIdFlow.waitForLatestValue()
    assertThat(sessionId).isEqualTo("4d0a66f3-82b6-3aa9-8f61-140bdd5f49d3")
  }

  @Test
  fun testUpdateSessionId_changesRandomIdReturnedByGetSessionId() {
    loggingIdentifierController.updateSessionId()
    testCoroutineDispatchers.runCurrent()

    val sessionIdProvider = loggingIdentifierController.getSessionId()

    // The session ID should be changed since updateSessionId() was called.
    val sessionId = monitorFactory.waitForNextSuccessfulResult(sessionIdProvider)
    assertThat(sessionId).isEqualTo("59aea8d4-af4b-3249-b889-dfeba06d0495")
  }

  @Test
  fun testUpdateSessionId_notifiesExistingProviderOfTheChange() {
    val sessionIdProvider = loggingIdentifierController.getSessionId()
    val monitor = monitorFactory.createMonitor(sessionIdProvider)
    monitor.waitForNextResult() // Fetch the initial state.

    loggingIdentifierController.updateSessionId()
    testCoroutineDispatchers.runCurrent()

    // The existing provider should've been notified of the changed session ID.
    val sessionId = monitor.ensureNextResultIsSuccess()
    assertThat(sessionId).isEqualTo("59aea8d4-af4b-3249-b889-dfeba06d0495")
  }

  @Test
  fun testUpdateSessionId_twice_changesRandomIdReturnedByGetSessionIdAgain() {
    // Update the session ID twice.
    loggingIdentifierController.updateSessionId()
    testCoroutineDispatchers.runCurrent()
    loggingIdentifierController.updateSessionId()
    testCoroutineDispatchers.runCurrent()

    val sessionIdProvider = loggingIdentifierController.getSessionId()

    // The session ID should be changed yet again due to updateSessionId() being called twice.
    val sessionId = monitorFactory.waitForNextSuccessfulResult(sessionIdProvider)
    assertThat(sessionId).isEqualTo("8cbd31f7-bb52-3129-9fac-78dcb5cb857a")
  }

  @Test
  fun testUpdateSessionId_changesRandomIdReturnedByGetSessionIdFlow() {
    loggingIdentifierController.updateSessionId()
    testCoroutineDispatchers.runCurrent()

    val sessionIdFlow = loggingIdentifierController.getSessionIdFlow()

    // The session ID should be changed since updateSessionId() was called.
    val sessionId = sessionIdFlow.waitForLatestValue()
    assertThat(sessionId).isEqualTo("59aea8d4-af4b-3249-b889-dfeba06d0495")
  }

  @Test
  fun testUpdateSessionId_updatesExistingSessionIdFlowValue() {
    val sessionIdFlow = loggingIdentifierController.getSessionIdFlow()
    sessionIdFlow.waitForLatestValue() // Fetch the initial value.

    loggingIdentifierController.updateSessionId()
    testCoroutineDispatchers.runCurrent()

    // The current value of the exist flow should be changed now since the session ID was updated.
    assertThat(sessionIdFlow.value).isEqualTo("59aea8d4-af4b-3249-b889-dfeba06d0495")
  }

  @Test
  fun testGetAppSessionId_initialState_returnsRandomId() {
    val appSessionIdProvider = loggingIdentifierController.getAppSessionId()

    val appSessionId = monitorFactory.waitForNextSuccessfulResult(appSessionIdProvider)
    assertThat(appSessionId).isEqualTo("2a11efe0-70f8-3a40-8d94-4fc3a2bd4f14")
  }

  @Test
  fun testGetAppSessionId_secondCall_returnsSameRandomId() {
    monitorFactory.ensureDataProviderExecutes(loggingIdentifierController.getAppSessionId())

    val sessionIdProvider = loggingIdentifierController.getAppSessionId()

    // The second call should return the same ID (since the ID doesn't automatically change).
    val appSessionId = monitorFactory.waitForNextSuccessfulResult(sessionIdProvider)
    assertThat(appSessionId).isEqualTo("2a11efe0-70f8-3a40-8d94-4fc3a2bd4f14")
  }

  @Test
  fun testGetAppSessionIdFlow_initialState_returnsFlowWithRandomId() {
    val appSessionIdFlow = loggingIdentifierController.getAppSessionIdFlow()

    val appSessionId = appSessionIdFlow.waitForLatestValue()
    assertThat(appSessionId).isEqualTo("2a11efe0-70f8-3a40-8d94-4fc3a2bd4f14")
  }

  @Test
  fun testGetAppSessionIdFlow_secondCall_returnsFlowWithSameRandomId() {
    loggingIdentifierController.getSessionIdFlow().waitForLatestValue()

    val appSessionIdFlow = loggingIdentifierController.getAppSessionIdFlow()

    // The second call should return the same ID (since the ID doesn't automatically change).
    val appSessionId = appSessionIdFlow.waitForLatestValue()
    assertThat(appSessionId).isEqualTo("2a11efe0-70f8-3a40-8d94-4fc3a2bd4f14")
  }

  @Test
  fun testGetAppSessionId_onSecondAppOpen_returnsDifferentRandomId() {
    monitorFactory.ensureDataProviderExecutes(loggingIdentifierController.getAppSessionId())

    // Simulate a second app open.
    TestLoggingIdentifierModule.applicationIdSeed = SECOND_APP_OPEN_APPLICATION_ID
    setUpNewTestApplicationComponent()

    // The app session ID should be different on the second app open.
    val appSessionIdProvider = loggingIdentifierController.getAppSessionId()
    val appSessionId = monitorFactory.waitForNextSuccessfulResult(appSessionIdProvider)
    assertThat(appSessionId).isEqualTo("c9d50545-33dc-3231-a1db-6a2672498c74")
  }

  private fun <T : MessageLite> writeFileCache(cacheName: String, value: T) {
    getCacheFile(cacheName).writeBytes(value.toByteArray())
  }

  private fun deleteCacheFile(cacheName: String) {
    check(getCacheFile(cacheName).delete()) { "Failed to delete: $cacheName." }
  }

  private fun getCacheFile(cacheName: String) = File(context.filesDir, "$cacheName.cache")

  private fun File.writeBytes(data: ByteArray) {
    FileOutputStream(this).use { it.write(data) }
  }

  private fun <T> fetchSuccessfulAsyncValue(block: suspend () -> T) =
    CoroutineScope(backgroundDispatcher).async { block() }.waitForSuccessfulResult()

  private fun <T> Deferred<T>.waitForSuccessfulResult(): T {
    return when (val result = waitForResult()) {
      is AsyncResult.Pending -> error("Deferred never finished.")
      is AsyncResult.Success -> result.value
      is AsyncResult.Failure -> throw IllegalStateException("Deferred failed", result.error)
    }
  }

  private fun <T> Deferred<T>.waitForResult() = toStateFlow().waitForLatestValue()

  private fun <T> Deferred<T>.toStateFlow(): StateFlow<AsyncResult<T>> {
    val deferred = this
    return MutableStateFlow<AsyncResult<T>>(value = AsyncResult.Pending()).also { flow ->
      CoroutineScope(backgroundDispatcher).async {
        flow.emit(AsyncResult.Success(deferred.await()))
      }.invokeOnCompletion {
        it?.let { flow.tryEmit(AsyncResult.Failure(it)) }
      }
    }
  }

  private fun <T> StateFlow<T>.waitForLatestValue(): T =
    also { testCoroutineDispatchers.runCurrent() }.value

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  private fun setUpNewTestApplicationComponent() {
    createNewTestApplication().inject(this)
  }

  private fun createNewTestApplication(): TestApplication {
    return Instrumentation.newApplication(
      TestApplication::class.java,
      InstrumentationRegistry.getInstrumentation().targetContext
    ) as TestApplication
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
      internal var applicationIdSeed: Long? = null
    }

    @Provides
    @ApplicationIdSeed
    fun provideApplicationIdSeed(): Long = applicationIdSeed!! // Fail if not initialized.
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
    @EnableLearnerStudyAnalytics
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
      TestLoggingIdentifierModule::class, ApplicationLifecycleModule::class, SyncStatusModule::class
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

  companion object {
    private const val INITIAL_APPLICATION_ID = 1L
    private const val SECOND_APP_OPEN_APPLICATION_ID = 2L
  }
}
