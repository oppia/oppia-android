package org.oppia.android.data.backends.gae

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.content.pm.ApplicationInfoBuilder
import androidx.test.core.content.pm.PackageInfoBuilder
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.JsonDataException
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.data.backends.gae.api.FeedbackReportingService
import org.oppia.android.data.backends.gae.api.PlatformParameterService
import org.oppia.android.testing.assertThrows
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.BackgroundTestDispatcher
import org.oppia.android.testing.threading.TestCoroutineDispatcher
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestDispatcherModule
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.http.GET
import java.net.HttpURLConnection
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

/** Tests for [NetworkModule]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = NetworkModuleTest.TestApplication::class)
class NetworkModuleTest {
  @field:[Inject NetworkApiKey] lateinit var networkApiKey: String
  @field:[Inject OppiaRetrofit] lateinit var retrofit: Retrofit
  @field:[Inject OppiaRetrofit] lateinit var retrofitProvider: Provider<Retrofit>
  @Inject lateinit var context: Context
  @Inject lateinit var mockWebServer: MockWebServer
  @Inject lateinit var platformParameterService: PlatformParameterService
  @Inject lateinit var feedbackReportingServiceProvider: Provider<FeedbackReportingService>
  @Inject lateinit var platformParameterServiceProvider: Provider<PlatformParameterService>
  @Inject lateinit var testCoroutineDispatchers: TestCoroutineDispatchers
  @Inject lateinit var networkLoggingInterceptor: NetworkLoggingInterceptor

  @field:[Inject BackgroundTestDispatcher]
  lateinit var backgroundTestDispatcher: TestCoroutineDispatcher

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
    setUpApplicationForContext()
  }

  @After
  fun tearDown() {
    mockWebServer.shutdown()
  }

  @Test
  fun testInjectedRetrofit_hasMockBaseUrl() {
    val baseUrl = retrofit.baseUrl().toUrl().toString()

    assertThat(baseUrl).isEqualTo(mockWebServer.url("/").toUrl().toString())
  }

  @Test
  fun testInjectedRetrofit_doesNotHaveOppiaBaseUrl() {
    val baseUrl = retrofit.baseUrl().toUrl().toString()

    // The URL should point to a local development server since a MockWebServer is being used.
    assertThat(baseUrl).doesNotContain("oppia.org")
  }

  @Test
  fun testInjectedRetrofit_secondInjection_returnsSingletonInstance() {
    val firstInjection = retrofitProvider.get()
    val secondInjection = retrofitProvider.get()

    // Multiple injections should yield the same instance due to it being a singleton.
    assertThat(firstInjection).isEqualTo(secondInjection)
  }

  @Test
  fun testRetrofit_withTestService_responseWithoutXssiPrefix_nonJsonResponse_callSucceeds() {
    val service = retrofit.create(TestService::class.java)
    setUpTestServiceResponse(json = "{}")

    val parametersCall = service.fetchNothing().execute()

    assertThat(parametersCall.isSuccessful).isTrue()
  }

  @Test
  fun testRetrofit_withTestService_malformedJsonResponse_throwsJsonDataException() {
    val service = retrofit.create(TestService::class.java)
    setUpTestServiceResponse(json = "{}")

    val exception = assertThrows<JsonDataException>() { service.fetchTestObject().execute() }

    // Verify that Moshi deserialization fails correctly on malformed JSON responses.
    assertThat(exception).hasMessageThat().contains("Required value 'field1' missing")
  }

  @Test
  fun testRetrofit_withTestService_responseWithoutXssiPrefix_jsonResponse_returnsCorrectObject() {
    val service = retrofit.create(TestService::class.java)
    setUpTestServiceResponse(json = "{\"field1\":\"asdf\",\"field2\":1}")

    val testObject = service.fetchTestObject().execute().body()

    // Verify that Moshi deserialization works correctly.
    assertThat(testObject?.field1).isEqualTo("asdf")
    assertThat(testObject?.field2).isEqualTo(1)
  }

  @Test
  fun testRetrofit_withTestService_responseWithXssiPrefix_jsonResponse_returnsCorrectObject() {
    val service = retrofit.create(TestService::class.java)
    setUpTestObjectServiceResponse(field1 = "field val", field2 = 3)

    val testObject = service.fetchTestObject().execute().body()

    // Verify that the XSSI prefix is correctly removed.
    assertThat(testObject?.field1).isEqualTo("field val")
    assertThat(testObject?.field2).isEqualTo(3)
  }

  @Test
  fun testRetrofit_withTestService_sendsCorrectAuthHeaderContext() {
    val service = retrofit.create(TestService::class.java)
    setUpTestObjectServiceResponse(field1 = "field val", field2 = 3)

    service.fetchTestObject().execute()

    val request = mockWebServer.takeRequest()
    assertThat(request.getHeader("api_key")).isEmpty() // Verifies presence, but value is empty.
    assertThat(request.getHeader("app_package_name")).isEqualTo("org.oppia.android.data")
    assertThat(request.getHeader("app_version_name")).isEqualTo(TEST_APP_VERSION_NAME)
    assertThat(request.getHeader("app_version_code")).isEqualTo("$TEST_APP_VERSION_CODE")
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testRetrofit_withTestService_logsSuccessfulResponse() {
    val service = retrofit.create(TestService::class.java)
    setUpTestObjectServiceResponse(field1 = "field val", field2 = 3)
    // Collect requests.
    val firstRequestsDeferred = CoroutineScope(backgroundTestDispatcher).async {
      networkLoggingInterceptor.logNetworkCallFlow.take(1).toList()
    }
    testCoroutineDispatchers.advanceUntilIdle() // Ensure the flow is subscribed before emit().

    service.fetchTestObject().execute()
    testCoroutineDispatchers.advanceUntilIdle()

    val firstRequest = firstRequestsDeferred.getCompleted().single()
    assertThat(firstRequest.responseStatusCode).isEqualTo(HttpURLConnection.HTTP_OK)
    assertThat(firstRequest.body).isEqualTo("{\"field1\":\"field val\",\"field2\":3}")
  }

  @Test
  fun testInjectedFeedbackReportingService_secondInjection_returnsSingletonInstance() {
    val firstInjection = feedbackReportingServiceProvider.get()
    val secondInjection = feedbackReportingServiceProvider.get()

    // Multiple injections should yield the same instance due to it being a singleton.
    assertThat(firstInjection).isEqualTo(secondInjection)
  }

  @Test
  fun testInjectedPlatformParameterService_secondInjection_returnsSingletonInstance() {
    val firstInjection = platformParameterServiceProvider.get()
    val secondInjection = platformParameterServiceProvider.get()

    // Multiple injections should yield the same instance due to it being a singleton.
    assertThat(firstInjection).isEqualTo(secondInjection)
  }

  @Test
  fun testInjectedNetworkApiKey_isEmptyByDefault() {
    // The network API key is empty by default on developer builds.
    assertThat(networkApiKey).isEmpty()
  }

  private fun setUpTestObjectServiceResponse(field1: String, field2: Int) {
    setUpTestServiceResponse(json = "$XSSI_PREFIX\n{\"field1\":\"$field1\",\"field2\":$field2}")
  }

  private fun setUpTestServiceResponse(json: String) {
    mockWebServer.enqueue(MockResponse().setBody(json))
  }

  private fun getTestApplication() = ApplicationProvider.getApplicationContext<TestApplication>()

  private fun setUpTestApplicationComponent() {
    getTestApplication().inject(this)
  }

  private fun setUpApplicationForContext() {
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
    packageInfo.versionName = TEST_APP_VERSION_NAME
    @Suppress("DEPRECATION") // versionCode is needed to test production code.
    packageInfo.versionCode = TEST_APP_VERSION_CODE
    packageManager.installPackage(packageInfo)
  }

  @Module
  class TestModule {
    @Provides
    @Singleton
    fun provideContext(application: Application): Context = application

    @Provides
    @Singleton
    fun provideMockWebServer() = MockWebServer().also { it.start() }

    @Provides
    @BaseUrl
    fun provideNetworkBaseUrl(mockWebServer: MockWebServer): String =
      mockWebServer.url("/").toUrl().toString()

    @Provides
    @XssiPrefix
    fun provideXssiPrefix() = XSSI_PREFIX
  }

  @Singleton
  @Component(
    modules = [
      TestModule::class, NetworkModule::class, TestDispatcherModule::class, RobolectricModule::class
    ]
  )

  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder
      fun build(): TestApplicationComponent
    }

    fun inject(networkModuleTest: NetworkModuleTest)
  }

  class TestApplication : Application() {
    private val component: TestApplicationComponent by lazy {
      DaggerNetworkModuleTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(networkModuleTest: NetworkModuleTest) {
      component.inject(networkModuleTest)
    }
  }

  interface TestService {
    @GET("test_path/test_object_handler")
    // TODO(#76): Update return payload for handling storage failures once retry policy is defined.
    fun fetchTestObject(): Call<TestMoshiObject>

    @GET("test_path/test_nothing_handler")
    fun fetchNothing(): Call<Any>
  }

  @JsonClass(generateAdapter = true)
  data class TestMoshiObject(
    @Json(name = "field1") val field1: String,
    @Json(name = "field2") val field2: Int
  )

  private companion object {
    private const val XSSI_PREFIX = ")]}'"
    private const val TEST_APP_VERSION_NAME = "oppia-android-test-0123456789"
    private const val TEST_APP_VERSION_CODE = 1
  }
}
