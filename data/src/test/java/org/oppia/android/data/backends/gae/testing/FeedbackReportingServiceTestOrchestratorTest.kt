package org.oppia.android.data.backends.gae.testing

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.JsonEncodingException
import com.squareup.moshi.Moshi
import dagger.Binds
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import data.src.main.java.org.oppia.android.data.backends.gae.testing.FeedbackReportingServiceTestOrchestrator
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.data.backends.gae.OppiaRetrofit
import org.oppia.android.data.backends.gae.RetrofitModule
import org.oppia.android.data.backends.gae.model.GaeFeedbackReport
import org.oppia.android.data.backends.gae.model.GaeFeedbackReportingAppContext
import org.oppia.android.data.backends.gae.model.GaeFeedbackReportingDeviceContext
import org.oppia.android.data.backends.gae.model.GaeFeedbackReportingEntryPoint
import org.oppia.android.data.backends.gae.model.GaeFeedbackReportingSystemContext
import org.oppia.android.data.backends.gae.model.GaeUserSuppliedFeedback
import org.oppia.android.testing.assertThrows
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestDispatcherModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import retrofit2.Retrofit
import java.io.FileNotFoundException
import java.io.InterruptedIOException
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [FeedbackReportingServiceTestOrchestrator]. */
// Function name: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = FeedbackReportingServiceTestOrchestratorTest.TestApplication::class)
class FeedbackReportingServiceTestOrchestratorTest {
  @Inject lateinit var serviceOrchestrator: FeedbackReportingServiceTestOrchestrator
  @Inject lateinit var mockWebServer: MockWebServer
  @Inject lateinit var moshi: Moshi
  @field:[Inject OppiaRetrofit] lateinit var retrofit: Retrofit

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testOrchestrator_takeGaeFeedbackReportRequest_noCalls_throwsIllegalStateException() {
    val error = assertThrows<IllegalStateException> {
      serviceOrchestrator.takeGaeFeedbackReportRequest()
    }

    assertThat(error).hasMessageThat().contains("Failed to retrieve request within timeout")
  }

  @Test
  fun testOrchestrator_takeGaeFeedbackReportRequest_oneCall_returnsRequest() {
    enqueueBasicResponse()
    retrofit.sendRawOkHttpRequestWithShortTimeout(createRequest(TEST_REPORT1))

    val feedbackReport = serviceOrchestrator.takeGaeFeedbackReportRequest()

    assertThat(feedbackReport).isEqualTo(TEST_REPORT1)
  }

  @Test
  fun testOrchestrator_takeGaeFeedbackReportRequest_twice_oneCall_throwsIllegalStateException() {
    enqueueBasicResponse()
    retrofit.sendRawOkHttpRequestWithShortTimeout(createRequest(TEST_REPORT1))
    serviceOrchestrator.takeGaeFeedbackReportRequest()

    // Try taking a second request.
    val error = assertThrows<IllegalStateException> {
      serviceOrchestrator.takeGaeFeedbackReportRequest()
    }

    assertThat(error).hasMessageThat().contains("Failed to retrieve request within timeout")
  }

  @Test
  fun testOrchestrator_takeGaeFeedbackReportRequest_somethingElseSent_throwsJsonDataException() {
    enqueueBasicResponse()
    retrofit.sendRawOkHttpRequestWithShortTimeout(createRequest("{}"))

    // Try taking a request with an invalid structure being sent to it.
    assertThrows<JsonDataException> { serviceOrchestrator.takeGaeFeedbackReportRequest() }
  }

  @Test
  fun testOrchestrator_takeGaeFeedbackReportRequest_twice_twoCalls_returnsRequestsInOrder() {
    enqueueBasicResponse()
    enqueueBasicResponse()
    retrofit.sendRawOkHttpRequestWithShortTimeout(createRequest(TEST_REPORT1))
    retrofit.sendRawOkHttpRequestWithShortTimeout(createRequest(TEST_REPORT2))

    val feedbackReport1 = serviceOrchestrator.takeGaeFeedbackReportRequest()
    val feedbackReport2 = serviceOrchestrator.takeGaeFeedbackReportRequest()

    // The reports should be captured in the order that they were sent.
    assertThat(feedbackReport1).isEqualTo(TEST_REPORT1)
    assertThat(feedbackReport2).isEqualTo(TEST_REPORT2)
  }

  @Test
  fun testOrchestrator_setNextResponseAsSuccess_sendRequest_returnsSuccess() {
    serviceOrchestrator.setNextResponseAsSuccess()

    val result = retrofit.sendRawOkHttpRequestWithShortTimeout(createRequest(TEST_REPORT1))

    assertThat(result.isSuccessful).isTrue()
  }

  @Test
  fun testOrchestrator_setNextResponseAsSuccess_sendTwoRequests_secondTimesOut() {
    serviceOrchestrator.setNextResponseAsSuccess()
    retrofit.sendRawOkHttpRequestWithShortTimeout(createRequest(TEST_REPORT1))

    assertThrows<InterruptedIOException> {
      retrofit.sendRawOkHttpRequestWithShortTimeout(createRequest(TEST_REPORT1))
    }
  }

  @Test
  fun testOrchestrator_setNextResponseAsSuccess_twice_sendTwoRequests_secondIsSuccess() {
    serviceOrchestrator.setNextResponseAsSuccess()
    serviceOrchestrator.setNextResponseAsSuccess()
    retrofit.sendRawOkHttpRequestWithShortTimeout(createRequest(TEST_REPORT1))

    val result2 = retrofit.sendRawOkHttpRequestWithShortTimeout(createRequest(TEST_REPORT1))

    assertThat(result2.isSuccessful).isTrue()
  }

  @Test
  fun testOrchestrator_setNextResponseAsServerError_sendRequest_returnsFailure() {
    serviceOrchestrator.setNextResponseAsServerError()

    val result = retrofit.sendRawOkHttpRequestWithShortTimeout(createRequest(TEST_REPORT1))

    assertThat(result.isSuccessful).isFalse()
    assertThat(result.code).isEqualTo(500)
  }

  @Test
  fun testOrchestrator_setNextResponseAsServerError_sendTwoRequests_secondIsNull() {
    serviceOrchestrator.setNextResponseAsServerError()
    retrofit.sendRawOkHttpRequestWithShortTimeout(createRequest(TEST_REPORT1))

    assertThrows<InterruptedIOException> {
      retrofit.sendRawOkHttpRequestWithShortTimeout(createRequest(TEST_REPORT1))
    }
  }

  @Test
  fun testOrchestrator_setNextResponseAsServerError_twice_sendTwoRequests_secondIsFailure() {
    serviceOrchestrator.setNextResponseAsServerError()
    serviceOrchestrator.setNextResponseAsServerError()
    retrofit.sendRawOkHttpRequestWithShortTimeout(createRequest(TEST_REPORT1))

    val result2 = retrofit.sendRawOkHttpRequestWithShortTimeout(createRequest(TEST_REPORT1))

    assertThat(result2.isSuccessful).isFalse()
    assertThat(result2.code).isEqualTo(500)
  }

  @Test
  fun testOrchestrator_setSuccessThenFailure_sendThreeRequest_returnsSuccessFailTimesOut() {
    serviceOrchestrator.setNextResponseAsSuccess()
    serviceOrchestrator.setNextResponseAsServerError()

    val result1 = retrofit.sendRawOkHttpRequestWithShortTimeout(createRequest(TEST_REPORT1))
    val result2 = retrofit.sendRawOkHttpRequestWithShortTimeout(createRequest(TEST_REPORT1))
    assertThrows<InterruptedIOException> { // Request 3 times out.
      retrofit.sendRawOkHttpRequestWithShortTimeout(createRequest(TEST_REPORT1))
    }

    assertThat(result1.isSuccessful).isTrue()
    assertThat(result2.isSuccessful).isFalse()
    assertThat(result2.code).isEqualTo(500)
  }

  @Test
  fun testOrchestrator_setFailureThenSuccess_sendThreeRequest_returnsFailSuccessTimesOut() {
    serviceOrchestrator.setNextResponseAsServerError()
    serviceOrchestrator.setNextResponseAsSuccess()

    val result1 = retrofit.sendRawOkHttpRequestWithShortTimeout(createRequest(TEST_REPORT1))
    val result2 = retrofit.sendRawOkHttpRequestWithShortTimeout(createRequest(TEST_REPORT1))
    assertThrows<InterruptedIOException> { // Request 3 times out.
      retrofit.sendRawOkHttpRequestWithShortTimeout(createRequest(TEST_REPORT1))
    }

    assertThat(result1.isSuccessful).isFalse()
    assertThat(result1.code).isEqualTo(500)
    assertThat(result2.isSuccessful).isTrue()
  }

  @Test
  fun testOrchestrator_loadGaeFeedbackReportFromTestData_nonexistentFile_throwsFileNotFoundEx() {
    assertThrows<FileNotFoundException> {
      serviceOrchestrator.loadGaeFeedbackReportFromTestData("invalid_file.json")
    }
  }

  @Test
  fun testOrchestrator_loadGaeFeedbackReportFromTestData_fileOfDifferentJsonType_throwsJsonEncEx() {
    assertThrows<JsonEncodingException> {
      serviceOrchestrator.loadGaeFeedbackReportFromTestData("dummy_response_with_xssi_prefix.json")
    }
  }

  @Test
  fun testOrchestrator_loadGaeFeedbackReportFromTestData_existingAndValidFile_returnsReport() {
    val report = serviceOrchestrator.loadGaeFeedbackReportFromTestData("feedback_reporting.json")

    // Verify that all parts of the report are successfully read.
    assertThat(report.schemaVersion).isEqualTo(1)
    assertThat(report.reportSubmissionTimestampSec).isEqualTo(1610519337)
    assertThat(report.appContext.entryPoint.entryPointName).isEqualTo("navigation_drawer")
    assertThat(report.appContext.entryPoint.topicId).isNull()
    assertThat(report.appContext.entryPoint.storyId).isNull()
    assertThat(report.appContext.entryPoint.explorationId).isNull()
    assertThat(report.appContext.entryPoint.subtopicId).isNull()
    assertThat(report.appContext.textSize).isEqualTo("large")
    assertThat(report.appContext.textLanguageCode).isEqualTo("EN")
    assertThat(report.appContext.audioLanguageCode).isEqualTo("EN")
    assertThat(report.appContext.downloadAndUpdateOnlyOnWifi).isTrue()
    assertThat(report.appContext.automaticallyUpdateTopics).isFalse()
    assertThat(report.appContext.isAdmin).isFalse()
    assertThat(report.appContext.eventLogs).containsExactly("example", "event").inOrder()
    assertThat(report.appContext.logcatLogs).containsExactly("example", "log").inOrder()
    assertThat(report.deviceContext.deviceModel).isEqualTo("example_model")
    assertThat(report.deviceContext.sdkVersion).isEqualTo(23)
    assertThat(report.deviceContext.buildFingerprint).isEqualTo("example_fingerprint_id")
    assertThat(report.deviceContext.networkType).isEqualTo("wifi")
    assertThat(report.systemContext.packageVersionName).isEqualTo("0.1-alpha-abcdef1234")
    assertThat(report.systemContext.packageVersionCode).isEqualTo(1)
    assertThat(report.systemContext.countryLocaleCode).isEqualTo("IN")
    assertThat(report.systemContext.languageLocaleCode).isEqualTo("EN")
    assertThat(report.userSuppliedFeedback.reportType).isEqualTo("suggestion")
    assertThat(report.userSuppliedFeedback.category).isEqualTo("language_suggestion")
    assertThat(report.userSuppliedFeedback.feedbackList).isEmpty()
    assertThat(report.userSuppliedFeedback.openTextUserInput).isEqualTo("french")
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  private fun enqueueBasicResponse() {
    mockWebServer.enqueue(MockResponse())
  }

  private fun createRequest(report: GaeFeedbackReport): Request = createRequest(report.toJson())

  private fun createRequest(json: String): Request {
    val requestBody = json.toRequestBody("text/json".toMediaType())
    return Request.Builder().url(mockWebServer.url("/testpath")).post(requestBody).build()
  }

  private fun GaeFeedbackReport.toJson() = moshi.adapter(GaeFeedbackReport::class.java).toJson(this)

  private fun Retrofit.sendRawOkHttpRequestWithShortTimeout(request: Request): Response {
    val oldOkHttpClient = callFactory() as OkHttpClient
    val adjustedOkHttpClient = oldOkHttpClient.newBuilder().callTimeout(1, TimeUnit.SECONDS).build()
    return adjustedOkHttpClient.newCall(request).execute()
  }

  @Module
  interface TestModule {
    @Binds
    fun provideContext(application: Application): Context
  }

  @Singleton
  @Component(
    modules = [
      TestModule::class, RetrofitModule::class, NetworkConfigTestModule::class,
      TestDispatcherModule::class, RobolectricModule::class
    ]
  )
  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder
      fun build(): TestApplicationComponent
    }

    fun inject(test: FeedbackReportingServiceTestOrchestratorTest)
  }

  class TestApplication : Application() {
    private val component: TestApplicationComponent by lazy {
      DaggerFeedbackReportingServiceTestOrchestratorTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(test: FeedbackReportingServiceTestOrchestratorTest) = component.inject(test)
  }

  private companion object {
    private val SYSTEM_CONTEXT = GaeFeedbackReportingSystemContext(
      packageVersionName = "oppia-android-test",
      packageVersionCode = 2,
      countryLocaleCode = "US",
      languageLocaleCode = "en"
    )

    private val DEVICE_CONTEXT = GaeFeedbackReportingDeviceContext(
      deviceModel = "SampleDevice123",
      sdkVersion = 30,
      buildFingerprint = "ABC-123-DEF",
      networkType = "wifi"
    )

    private val APP_CONTEXT = GaeFeedbackReportingAppContext(
      entryPoint = GaeFeedbackReportingEntryPoint(
        entryPointName = "HomeActivity",
        topicId = null,
        storyId = null,
        explorationId = null,
        subtopicId = null
      ),
      textSize = "small",
      textLanguageCode = "sw",
      audioLanguageCode = "ar",
      downloadAndUpdateOnlyOnWifi = true,
      automaticallyUpdateTopics = true,
      isAdmin = false,
      eventLogs = emptyList(),
      logcatLogs = emptyList()
    )

    private val TEST_REPORT1 = GaeFeedbackReport(
      schemaVersion = 1,
      reportSubmissionTimestampSec = 123456789,
      userSuppliedFeedback = GaeUserSuppliedFeedback(
        reportType = "suggestion",
        category = "test1",
        feedbackList = null,
        openTextUserInput = null
      ),
      systemContext = SYSTEM_CONTEXT,
      deviceContext = DEVICE_CONTEXT,
      appContext = APP_CONTEXT
    )

    private val TEST_REPORT2 = GaeFeedbackReport(
      schemaVersion = 1,
      reportSubmissionTimestampSec = 123467890,
      userSuppliedFeedback = GaeUserSuppliedFeedback(
        reportType = "issue",
        category = "test2",
        feedbackList = null,
        openTextUserInput = null
      ),
      systemContext = SYSTEM_CONTEXT,
      deviceContext = DEVICE_CONTEXT,
      appContext = APP_CONTEXT
    )
  }
}
