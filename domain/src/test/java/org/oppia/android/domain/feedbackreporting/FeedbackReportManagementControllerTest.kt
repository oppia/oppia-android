package org.oppia.android.domain.feedbackreporting

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
import okhttp3.OkHttpClient
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
import org.oppia.android.data.backends.gae.NetworkInterceptor
import org.oppia.android.data.backends.gae.NetworkSettings
import org.oppia.android.data.backends.gae.api.FeedbackReportingService
import org.oppia.android.domain.oppialogger.EventLogStorageCacheSize
import org.oppia.android.domain.oppialogger.ExceptionLogStorageCacheSize
import org.oppia.android.testing.RobolectricModule
import org.oppia.android.testing.TestCoroutineDispatchers
import org.oppia.android.testing.TestDispatcherModule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.network.MockFeedbackReportingService
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import org.oppia.android.util.data.DataProvidersInjector
import org.oppia.android.util.data.DataProvidersInjectorProvider
import org.oppia.android.util.logging.EnableConsoleLog
import org.oppia.android.util.logging.EnableFileLog
import org.oppia.android.util.logging.GlobalLogLevel
import org.oppia.android.util.logging.LogLevel
import org.oppia.android.util.networking.NetworkConnectionUtil
import org.oppia.android.util.networking.NetworkConnectionUtil.ConnectionStatus.LOCAL
import org.oppia.android.util.networking.NetworkConnectionUtil.ConnectionStatus.NONE
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.mock.MockRetrofit
import retrofit2.mock.NetworkBehavior
import java.io.File
import javax.inject.Inject
import javax.inject.Qualifier
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
  lateinit var networkConnectionUtil: NetworkConnectionUtil

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @Mock
  lateinit var mockReportsStoreObserver: Observer<AsyncResult<FeedbackReportingDatabase>>

  @Captor
  lateinit var reportStoreResultCaptor: ArgumentCaptor<AsyncResult<FeedbackReportingDatabase>>

  private val appContext = FeedbackReportingAppContext.newBuilder()
    .setNavigationDrawer(NavigationDrawerEntryPoint.getDefaultInstance())
    .setTextSize(ReadingTextSize.MEDIUM_TEXT_SIZE)
    .setDeviceSettings(DeviceSettings.getDefaultInstance())
    .setAudioLanguage(AudioLanguage.NO_AUDIO)
    .setTextLanguage(AppLanguage.ENGLISH_APP_LANGUAGE)
    .setIsAdmin(false)
    .build()

  private val featureSuggestion = Suggestion.newBuilder()
    .setSuggestionCategory(SuggestionCategory.FEATURE_SUGGESTION)
    .setUserSubmittedSuggestion("A feature suggestion")
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
    networkConnectionUtil = NetworkConnectionUtil(ApplicationProvider.getApplicationContext())
    setUpTestApplicationComponent()
    setUpFakeLogcatFile()
    MockitoAnnotations.initMocks(this)
  }

  @Test
  fun testController_submitFeedbackReport_withNetwork_doesNotSendRemoteToStore() {
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
  fun testController_submitFeedbackReport_withNoNetwork_sendsReportsToStore() {
    networkConnectionUtil.setCurrentConnectionStatus(NONE)
    feedbackReportManagementController.submitFeedbackReport(laterSuggestionReport)

    val reportsStore = feedbackReportManagementController.getFeedbackReportStore().toLiveData()
    reportsStore.observeForever(mockReportsStoreObserver)
    testCoroutineDispatchers.advanceUntilIdle()

    verify(mockReportsStoreObserver, atLeastOnce()).onChanged(reportStoreResultCaptor.capture())

    val report = reportStoreResultCaptor.value.getOrThrow().getReports(0)
    assertThat(report.reportSubmissionTimestampSec).isEqualTo(LATER_TIMESTAMP)
    assertThat(report.userSuppliedInfo.suggestion.suggestionCategory)
      .isEqualTo(SuggestionCategory.FEATURE_SUGGESTION)
    assertThat(report.userSuppliedInfo.suggestion.userSubmittedSuggestion)
      .isEqualTo("A feature suggestion")
  }

  @Test
  fun testController_submitMultipleFeedbackReports_withNoNetwork_sendsAllReportsToStore() {
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

  // Creates a fake logcat file in this directory so that the controller being tested has a file to
  // read when recording the logcat events.
  private fun setUpFakeLogcatFile() {
    val logFile = File(context.filesDir, "oppia_app.log")
    logFile.printWriter().use { out -> out.println("Fake logcat log") }
  }

  @Qualifier
  annotation class OppiaRetrofit

  // TODO(#89): Move this to a common test application component.
  @Module
  class TestNetworkModule {
    @OppiaRetrofit
    @Provides
    @Singleton
    fun provideMockRetrofitInstance(): MockRetrofit {
      val client = OkHttpClient.Builder()
      client.addInterceptor(NetworkInterceptor())

      val retrofit = retrofit2.Retrofit.Builder()
        .baseUrl(NetworkSettings.getBaseUrl())
        .addConverterFactory(MoshiConverterFactory.create())
        .client(client.build())
        .build()

      val behavior = NetworkBehavior.create()
      return MockRetrofit.Builder(retrofit)
        .networkBehavior(behavior)
        .build()
    }

    @Provides
    @Singleton
    fun provideFeedbackReportingService(
      @OppiaRetrofit mockRetrofit: MockRetrofit
    ): FeedbackReportingService {
      val delegate = mockRetrofit.create(FeedbackReportingService::class.java)
      return MockFeedbackReportingService(delegate)
    }
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
      FakeOppiaClockModule::class, FeedbackReportingModule::class
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

    override fun getDataProvidersInjector(): DataProvidersInjector = component
  }
}
