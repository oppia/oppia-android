package org.oppia.android.domain.feedbackreporting

import android.app.Application
import android.content.Context
import androidx.lifecycle.Observer
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.content.pm.ApplicationInfoBuilder
import androidx.test.core.content.pm.PackageInfoBuilder
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito.atLeastOnce
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import org.oppia.android.app.model.AppLanguage
import org.oppia.android.app.model.AudioLanguage
import org.oppia.android.app.model.Crash
import org.oppia.android.app.model.DeviceSettings
import org.oppia.android.app.model.FeedbackReport
import org.oppia.android.app.model.FeedbackReportingAppContext
import org.oppia.android.app.model.FeedbackReportingDatabase
import org.oppia.android.app.model.NavigationDrawerEntryPoint
import org.oppia.android.app.model.ReadingTextSize
import org.oppia.android.app.model.Suggestion
import org.oppia.android.app.model.Suggestion.SuggestionCategory
import org.oppia.android.app.model.UserSuppliedFeedback
import org.oppia.android.data.backends.gae.api.FeedbackReportingService
import org.oppia.android.domain.oppialogger.EventLogStorageCacheSize
import org.oppia.android.domain.oppialogger.ExceptionLogStorageCacheSize
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.network.ApiMockLoader
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.BackgroundTestDispatcher
import org.oppia.android.testing.threading.TestCoroutineDispatcher
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import org.oppia.android.util.data.DataProvidersInjector
import org.oppia.android.util.data.DataProvidersInjectorProvider
import org.oppia.android.util.logging.EnableConsoleLog
import org.oppia.android.util.logging.EnableFileLog
import org.oppia.android.util.logging.GlobalLogLevel
import org.oppia.android.util.logging.LogLevel
import org.oppia.android.util.networking.NetworkConnectionDebugUtil
import org.oppia.android.util.networking.NetworkConnectionUtil.ProdConnectionStatus.CELLULAR
import org.oppia.android.util.networking.NetworkConnectionUtil.ProdConnectionStatus.LOCAL
import org.oppia.android.util.networking.NetworkConnectionUtil.ProdConnectionStatus.NONE
import org.oppia.android.util.networking.NetworkConnectionUtilDebugModule
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

// Timestamp in sec for 3/2/21, 12:38pm GMT.
private const val EARLIER_TIMESTAMP = 1614688684

// Timestamp in sec for 3/14/21, 2:24am GMT.
private const val LATER_TIMESTAMP = 1615688684

/** Tests for [FeedbackReportManagementController]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = FeedbackReportManagementControllerTest.TestApplication::class)
class FeedbackReportManagementControllerTest {

  @Inject
  lateinit var context: Context

  @Inject
  lateinit var feedbackReportManagementController: FeedbackReportManagementController

  @Inject
  lateinit var networkConnectionUtil: NetworkConnectionDebugUtil

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @field:[Inject BackgroundTestDispatcher]
  lateinit var testCoroutineDispatcher: TestCoroutineDispatcher

  @Mock
  lateinit var mockReportsStoreObserver: Observer<AsyncResult<FeedbackReportingDatabase>>

  @Captor
  lateinit var reportStoreResultCaptor: ArgumentCaptor<AsyncResult<FeedbackReportingDatabase>>

  @field:[Inject MockServer]
  lateinit var mockWebServer: MockWebServer

  private val languageSuggestionText = "french"

  private val appContext = FeedbackReportingAppContext.newBuilder()
    .setNavigationDrawer(NavigationDrawerEntryPoint.getDefaultInstance())
    .setTextSize(ReadingTextSize.LARGE_TEXT_SIZE)
    .setDeviceSettings(DeviceSettings.getDefaultInstance())
    .setAudioLanguage(AudioLanguage.ENGLISH_AUDIO_LANGUAGE)
    .setTextLanguage(AppLanguage.ENGLISH_APP_LANGUAGE)
    .setIsAdmin(false)
    .build()

  private val featureSuggestion = Suggestion.newBuilder()
    .setSuggestionCategory(SuggestionCategory.LANGUAGE_SUGGESTION)
    .setUserSubmittedSuggestion(languageSuggestionText)
    .build()

  private val userSuppliedSuggestionFeedback = UserSuppliedFeedback.newBuilder()
    .setSuggestion(featureSuggestion)
    .build()

  private val laterSuggestionReport = FeedbackReport.newBuilder()
    .setReportSubmissionTimestampSec(LATER_TIMESTAMP)
    .setUserSuppliedInfo(userSuppliedSuggestionFeedback)
    .setAppContext(appContext)
    .build()

  private val userSuppliedCrashFeedback = UserSuppliedFeedback.newBuilder()
    .setCrash(Crash.getDefaultInstance())
    .build()

  private val earlierCrashReport = FeedbackReport.newBuilder()
    .setReportSubmissionTimestampSec(EARLIER_TIMESTAMP)
    .setUserSuppliedInfo(userSuppliedCrashFeedback)
    .setAppContext(appContext)
    .build()

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
    setUpApplicationForContext()
    setUpFakeLogcatFile()
    MockitoAnnotations.initMocks(this)
  }

  @After
  fun tearDown() {
    mockWebServer.shutdown()
  }

  @Test
  fun testController_submitFeedbackReport_withLocalNetwork_successfullySendsRequestToServer() {
    mockWebServer.enqueue(MockResponse().setBody("{}"))
    networkConnectionUtil.setCurrentConnectionStatus(LOCAL)
    feedbackReportManagementController.submitFeedbackReport(laterSuggestionReport)
    // The feedback_reporting_unformatted.json is the same as the feedback_reporting.json, in the
    // format used in the request body of reports sent in network requests.
    val feedbackReportUnformattedJson = ApiMockLoader.getFakeJson(
      "feedback_reporting_unformatted.json"
    )

    val request = mockWebServer.takeRequest(
      timeout = testCoroutineDispatcher.DEFAULT_TIMEOUT_SECONDS,
      unit = testCoroutineDispatcher.DEFAULT_TIMEOUT_UNIT
    )
    request?.let {
      assertThat(it.method).isEqualTo("POST")
      // Append newline to the request body to match new line at the end of the JSON file.
      assertThat(it.body.readUtf8() + "\n").isEqualTo(feedbackReportUnformattedJson)
    }
  }

  @Test
  fun testController_submitFeedbackReport_withCellularNetwork_successfullySendsRequestToServer() {
    mockWebServer.enqueue(MockResponse().setBody("{}"))
    networkConnectionUtil.setCurrentConnectionStatus(CELLULAR)
    feedbackReportManagementController.submitFeedbackReport(laterSuggestionReport)
    // The feedback_reporting_unformatted.json is the same as the feedback_reporting.json, in the
    // format used in the request body of reports sent in network requests.
    val feedbackReportUnformattedJson = ApiMockLoader.getFakeJson(
      "feedback_reporting_unformatted.json"
    )

    val request = mockWebServer.takeRequest(
      timeout = testCoroutineDispatcher.DEFAULT_TIMEOUT_SECONDS,
      unit = testCoroutineDispatcher.DEFAULT_TIMEOUT_UNIT
    )
    request?.let {
      assertThat(it.method).isEqualTo("POST")
      // Append newline to the request body to match new line at the end of the JSON file.
      assertThat(it.body.readUtf8() + "\n").isEqualTo(feedbackReportUnformattedJson)
    }
  }

  @Test
  fun testController_submitFeedbackReport_withoutNetwork_doesNotSendRequestToServer() {
    mockWebServer.enqueue(MockResponse().setBody("{}"))
    networkConnectionUtil.setCurrentConnectionStatus(NONE)
    feedbackReportManagementController.submitFeedbackReport(laterSuggestionReport)

    val request = mockWebServer.takeRequest(
      timeout = testCoroutineDispatcher.DEFAULT_TIMEOUT_SECONDS,
      unit = testCoroutineDispatcher.DEFAULT_TIMEOUT_UNIT
    )
    assertThat(request).isNull()
  }

  @Test
  fun testController_submitFeedbackReport_withLocalNetwork_doesNotSaveReportToStore() {
    // Enqueue a responses so that the MockWebServer knows when the request is successfully sent.
    mockWebServer.enqueue(MockResponse().setBody("{}"))
    networkConnectionUtil.setCurrentConnectionStatus(LOCAL)
    feedbackReportManagementController.submitFeedbackReport(laterSuggestionReport)

    val reportsStore = feedbackReportManagementController.getFeedbackReportStore().toLiveData()
    reportsStore.observeForever(this.mockReportsStoreObserver)
    testCoroutineDispatchers.advanceUntilIdle()

    verify(mockReportsStoreObserver, atLeastOnce()).onChanged(reportStoreResultCaptor.capture())

    val reportsList = reportStoreResultCaptor.value
      .getOrDefault(FeedbackReportingDatabase.getDefaultInstance())
      .reportsList
    assertThat(reportsList).isEmpty()
  }

  @Test
  fun testController_submitMultipleFeedbackReports_withLocalNetwork_doesNotSaveReportsToStore() {
    // Enqueue multiple responses so that the MockWebServer knows when all request are successfully
    // sent.
    mockWebServer.enqueue(MockResponse().setBody("{}"))
    mockWebServer.enqueue(MockResponse().setBody("{}"))
    networkConnectionUtil.setCurrentConnectionStatus(LOCAL)
    feedbackReportManagementController.submitFeedbackReport(earlierCrashReport)
    feedbackReportManagementController.submitFeedbackReport(laterSuggestionReport)

    val reportsStore = feedbackReportManagementController.getFeedbackReportStore().toLiveData()
    reportsStore.observeForever(mockReportsStoreObserver)
    testCoroutineDispatchers.advanceUntilIdle()

    verify(mockReportsStoreObserver, atLeastOnce()).onChanged(reportStoreResultCaptor.capture())

    val reportsList = reportStoreResultCaptor.value
      .getOrDefault(FeedbackReportingDatabase.getDefaultInstance())
      .reportsList
    assertThat(reportsList).isEmpty()
  }

  @Test
  fun testController_submitFeedbackReport_withCellularNetwork_doesNotSaveReportToStore() {
    // Enqueue a responses so that the MockWebServer knows when the request is successfully sent.
    mockWebServer.enqueue(MockResponse().setBody("{}"))
    networkConnectionUtil.setCurrentConnectionStatus(CELLULAR)
    feedbackReportManagementController.submitFeedbackReport(laterSuggestionReport)

    val reportsStore = feedbackReportManagementController.getFeedbackReportStore().toLiveData()
    reportsStore.observeForever(this.mockReportsStoreObserver)
    testCoroutineDispatchers.advanceUntilIdle()

    verify(mockReportsStoreObserver, atLeastOnce()).onChanged(reportStoreResultCaptor.capture())

    val reportsList = reportStoreResultCaptor.value
      .getOrDefault(FeedbackReportingDatabase.getDefaultInstance())
      .reportsList
    assertThat(reportsList).isEmpty()
  }

  @Test
  fun testController_submitMultipleFeedbackReports_withCellularNetwork_doesNotSaveReportsToStore() {
    // Enqueue multiple responses so that the MockWebServer knows when all requests are successfully
    // sent.
    mockWebServer.enqueue(MockResponse().setBody("{}"))
    mockWebServer.enqueue(MockResponse().setBody("{}"))
    networkConnectionUtil.setCurrentConnectionStatus(CELLULAR)
    feedbackReportManagementController.submitFeedbackReport(earlierCrashReport)
    feedbackReportManagementController.submitFeedbackReport(laterSuggestionReport)

    val reportsStore = feedbackReportManagementController.getFeedbackReportStore().toLiveData()
    reportsStore.observeForever(mockReportsStoreObserver)
    testCoroutineDispatchers.advanceUntilIdle()

    verify(mockReportsStoreObserver, atLeastOnce()).onChanged(reportStoreResultCaptor.capture())

    val reportsList = reportStoreResultCaptor.value
      .getOrDefault(FeedbackReportingDatabase.getDefaultInstance())
      .reportsList
    assertThat(reportsList).isEmpty()
  }

  @Test
  fun testController_submitFeedbackReport_withoutNetwork_savesReportToStore() {
    networkConnectionUtil.setCurrentConnectionStatus(NONE)
    feedbackReportManagementController.submitFeedbackReport(laterSuggestionReport)

    val reportsStore = feedbackReportManagementController.getFeedbackReportStore().toLiveData()
    reportsStore.observeForever(mockReportsStoreObserver)
    testCoroutineDispatchers.advanceUntilIdle()

    verify(mockReportsStoreObserver, atLeastOnce()).onChanged(reportStoreResultCaptor.capture())

    val report = reportStoreResultCaptor.value.getOrThrow().getReports(0)
    assertThat(report.reportSubmissionTimestampSec).isEqualTo(LATER_TIMESTAMP)
    assertThat(report.userSuppliedInfo.suggestion.suggestionCategory)
      .isEqualTo(SuggestionCategory.LANGUAGE_SUGGESTION)
    assertThat(report.userSuppliedInfo.suggestion.userSubmittedSuggestion)
      .isEqualTo(languageSuggestionText)
  }

  @Test
  fun testController_submitMultipleFeedbackReports_withoutNetwork_savesAllReportsToStore() {
    networkConnectionUtil.setCurrentConnectionStatus(NONE)
    feedbackReportManagementController.submitFeedbackReport(earlierCrashReport)
    feedbackReportManagementController.submitFeedbackReport(laterSuggestionReport)

    val reportsStore = feedbackReportManagementController.getFeedbackReportStore().toLiveData()
    reportsStore.observeForever(mockReportsStoreObserver)
    testCoroutineDispatchers.advanceUntilIdle()

    verify(mockReportsStoreObserver, atLeastOnce()).onChanged(reportStoreResultCaptor.capture())

    val reportsList = reportStoreResultCaptor.value.getOrThrow().reportsList
    assertThat(reportsList.size).isEqualTo(2)
    assertThat(reportsList.get(0)).isEqualTo(earlierCrashReport)
    assertThat(reportsList.get(1)).isEqualTo(laterSuggestionReport)
  }

  @Test
  fun testController_removeCachedReport_noLongerInStore() {
    networkConnectionUtil.setCurrentConnectionStatus(NONE)
    feedbackReportManagementController.submitFeedbackReport(earlierCrashReport)
    val reportsStore = feedbackReportManagementController.getFeedbackReportStore().toLiveData()
    reportsStore.observeForever(mockReportsStoreObserver)
    testCoroutineDispatchers.advanceUntilIdle()

    verify(mockReportsStoreObserver, atLeastOnce()).onChanged(reportStoreResultCaptor.capture())
    val reportsList = reportStoreResultCaptor.value.getOrThrow().reportsList
    val report = reportsList.get(0)
    assertThat(report).isEqualTo(earlierCrashReport)
    feedbackReportManagementController.removeFirstCachedReport()

    val emptyReportsStore = feedbackReportManagementController.getFeedbackReportStore().toLiveData()
    emptyReportsStore.observeForever(mockReportsStoreObserver)
    testCoroutineDispatchers.advanceUntilIdle()
    verify(mockReportsStoreObserver, atLeastOnce()).onChanged(reportStoreResultCaptor.capture())

    val reportsCount = reportStoreResultCaptor.value.getOrThrow().reportsCount
    assertThat(reportsCount).isEqualTo(0)
  }

  @Test
  fun testController_removeCachedReports_removedInOrder() {
    networkConnectionUtil.setCurrentConnectionStatus(NONE)
    feedbackReportManagementController.submitFeedbackReport(earlierCrashReport)
    feedbackReportManagementController.submitFeedbackReport(laterSuggestionReport)
    val reportsStore = feedbackReportManagementController.getFeedbackReportStore().toLiveData()
    reportsStore.observeForever(mockReportsStoreObserver)
    testCoroutineDispatchers.advanceUntilIdle()

    verify(mockReportsStoreObserver, atLeastOnce()).onChanged(reportStoreResultCaptor.capture())
    val removedReport = reportStoreResultCaptor.value.getOrThrow().getReports(0)
    feedbackReportManagementController.removeFirstCachedReport()

    val updatedReportsStore = feedbackReportManagementController.getFeedbackReportStore()
      .toLiveData()
    updatedReportsStore.observeForever(mockReportsStoreObserver)
    testCoroutineDispatchers.advanceUntilIdle()
    verify(mockReportsStoreObserver, atLeastOnce()).onChanged(reportStoreResultCaptor.capture())

    val nextReport = reportStoreResultCaptor.value.getOrThrow().getReports(0)
    assertThat(nextReport).isNotEqualTo(removedReport)
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  private fun setUpApplicationForContext() {
    // The package manager is shadowed in order to specify the package version name and package
    // version code so that the controller can correctly fetch and set these values in the feedback
    // report.
    val packageManager = Shadows.shadowOf(context.packageManager)
    val applicationInfo =
      ApplicationInfoBuilder.newBuilder()
        .setPackageName(context.packageName)
        .build()
    val packageInfo =
      PackageInfoBuilder.newBuilder()
        .setPackageName(context.packageName)
        .setApplicationInfo(applicationInfo)
        .build()
    packageInfo.versionName = "1.0"
    packageInfo.longVersionCode = 1
    packageManager.installPackage(packageInfo)
  }

  private fun setUpFakeLogcatFile() {
    // Creates a fake logcat file in this directory so that the controller being tested has a file to
    // read when recording the logcat events.
    val logFile = File(context.filesDir, "oppia_app.log")
    logFile.printWriter().use { out -> out.println("Fake logcat log") }
  }

  @Module
  class TestModule {
    @Provides
    @Singleton
    fun provideContext(application: Application): Context {
      return application
    }

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

  annotation class OppiaRetrofit

  annotation class MockServer

  @Module
  class TestNetworkModule {
    @Provides
    @Singleton
    @MockServer
    fun provideMockWebServer(): MockWebServer {
      return MockWebServer()
    }

    @Provides
    @Singleton
    @OppiaRetrofit
    fun provideRetrofitInstance(
      @MockServer mockWebServer: MockWebServer
    ): Retrofit {
      val client = OkHttpClient.Builder()
        .build()

      return retrofit2.Retrofit.Builder()
        .baseUrl(mockWebServer.url("/"))
        .addConverterFactory(MoshiConverterFactory.create())
        .client(client)
        .build()
    }

    @Provides
    @Singleton
    fun provideFeedbackReportingService(
      @OppiaRetrofit retrofit: Retrofit
    ): FeedbackReportingService {
      return retrofit.create(FeedbackReportingService::class.java)
    }
  }

  // TODO(#89): Move this to a common test application component.
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
      TestDispatcherModule::class, TestLogStorageModule::class, TestNetworkModule::class,
      FakeOppiaClockModule::class, FeedbackReportingModule::class,
      NetworkConnectionUtilDebugModule::class
    ]
  )
  interface TestApplicationComponent : DataProvidersInjector {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder
      fun build(): TestApplicationComponent
    }

    fun inject(feedbackReportManagementControllerTest: FeedbackReportManagementControllerTest)
    fun inject(feedbackREportingService: FeedbackReportingService)
  }

  class TestApplication : Application(), DataProvidersInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerFeedbackReportManagementControllerTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(feedbackReportManagementControllerTest: FeedbackReportManagementControllerTest) {
      component.inject(feedbackReportManagementControllerTest)
    }

    fun inject(feedbackReportingService: FeedbackReportingService) {
      component.inject(feedbackReportingService)
    }

    override fun getDataProvidersInjector(): DataProvidersInjector = component
  }
}
