package org.oppia.android.data.backends.gae

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.BackgroundTestDispatcher
import org.oppia.android.testing.threading.TestCoroutineDispatcher
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.util.data.DataProvidersInjector
import org.oppia.android.util.data.DataProvidersInjectorProvider
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.net.ConnectException
import java.net.HttpURLConnection
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [RemoteAuthNetworkInterceptor]. */
@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@Config(application = NetworkLoggingInterceptorTest.TestApplication::class)
@LooperMode(LooperMode.Mode.PAUSED)
class NetworkLoggingInterceptorTest {
  private companion object {
    private const val testUrl = "/"
    private const val testApiKey = "api_key"
    private const val testApiKeyValue = "api_key_value"
    private const val testResponseBody = "{\"test\": \"test\"}"
    private const val headerString = "$testApiKey: $testApiKeyValue"
  }

  @Inject
  lateinit var networkLoggingInterceptor: NetworkLoggingInterceptor
  @Inject
  lateinit var context: Context
  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @field:[Inject BackgroundTestDispatcher]
  lateinit var backgroundTestDispatcher: TestCoroutineDispatcher

  private lateinit var retrofit: Retrofit
  private lateinit var mockWebServer: MockWebServer
  private lateinit var client: OkHttpClient
  private lateinit var mockWebServerUrl: HttpUrl
  private lateinit var request: Request

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
    setUpRetrofit()

    mockWebServerUrl = mockWebServer.url(testUrl)

    request = Request.Builder().url(mockWebServerUrl).addHeader(testApiKey, testApiKeyValue).build()
  }

  @After
  fun tearDown() {
    mockWebServer.shutdown()
  }

  @Test
  fun testLoggingInterceptor_makeNetworkCall_succeeds() {
    mockWebServer.enqueue(MockResponse().setBody(testResponseBody))

    // Collect requests.
    val firstRequestsDeferred = CoroutineScope(backgroundTestDispatcher).async {
      networkLoggingInterceptor.logNetworkCallFlow.take(1).toList()
    }
    testCoroutineDispatchers.advanceUntilIdle() // Ensure the flow is subscribed before emit().
    client.newCall(request).execute()
    testCoroutineDispatchers.advanceUntilIdle()

    val firstRequest = firstRequestsDeferred.getCompleted().single()
    assertThat(firstRequest.requestUrl).isEqualTo(mockWebServerUrl.toString())
    assertThat(firstRequest.responseStatusCode).isEqualTo(HttpURLConnection.HTTP_OK)
    assertThat(firstRequest.headers).contains(headerString)
    assertThat(firstRequest.body).isEqualTo(testResponseBody)
  }

  @Test
  fun testLoggingInterceptor_makeNetworkCallWithInvalidUrl_failsAndCompletes() {
    val pageNotFound = HttpURLConnection.HTTP_NOT_FOUND
    val mockResponse = MockResponse().setResponseCode(pageNotFound).setBody(testResponseBody)

    // Collect requests & failures.
    val firstRequestsDeferred = CoroutineScope(backgroundTestDispatcher).async {
      networkLoggingInterceptor.logNetworkCallFlow.take(1).toList()
    }
    val firstFailingRequestsDeferred = CoroutineScope(backgroundTestDispatcher).async {
      networkLoggingInterceptor.logFailedNetworkCallFlow.take(1).toList()
    }
    mockWebServer.enqueue(mockResponse)
    testCoroutineDispatchers.advanceUntilIdle() // Ensure the flow is subscribed before emit().
    client.newCall(request).execute()
    testCoroutineDispatchers.advanceUntilIdle()

    val firstRequest = firstRequestsDeferred.getCompleted().single()
    val firstFailingRequest = firstFailingRequestsDeferred.getCompleted().single()
    assertThat(firstRequest.requestUrl).isEqualTo(mockWebServerUrl.toString())
    assertThat(firstRequest.responseStatusCode).isEqualTo(pageNotFound)
    assertThat(firstRequest.headers).contains(headerString)
    assertThat(firstRequest.body).isEqualTo(testResponseBody)
    assertThat(firstFailingRequest.requestUrl).isEqualTo(mockWebServerUrl.toString())
    assertThat(firstFailingRequest.responseStatusCode).isEqualTo(pageNotFound)
    assertThat(firstFailingRequest.headers).contains(headerString)
    assertThat(firstFailingRequest.body).isEmpty()
    assertThat(firstFailingRequest.errorMessage).isNotEmpty()
    assertThat(firstFailingRequest.errorMessage).isEqualTo(testResponseBody)
  }

  @Test
  fun testLoggingInterceptor_makeCrashingNetworkCall_failsAndCompletes() {
    mockWebServer.shutdown()

    // Collect failures.
    val firstFailingRequestsDeferred = CoroutineScope(backgroundTestDispatcher).async {
      networkLoggingInterceptor.logFailedNetworkCallFlow.take(1).toList()
    }
    testCoroutineDispatchers.advanceUntilIdle() // Ensure the flow is subscribed before emit().
    try {
      client.newCall(request).execute()
    } catch (e: ConnectException) {
      // Ignore exception thrown at this point since the test is verifying that the failure
      // correctly logs to networkLoggingInterceptor.logFailedNetworkCallFlow.
    }
    testCoroutineDispatchers.advanceUntilIdle()

    val firstFailingRequest = firstFailingRequestsDeferred.getCompleted().single()
    assertThat(firstFailingRequest.requestUrl).isEqualTo(mockWebServerUrl.toString())
    assertThat(firstFailingRequest.responseStatusCode).isEqualTo(0)
    assertThat(firstFailingRequest.headers).contains(headerString)
    assertThat(firstFailingRequest.body).isEmpty()
    assertThat(firstFailingRequest.errorMessage).isNotEmpty()
    assertThat(firstFailingRequest.errorMessage).contains("Failed to connect to localhost")
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  private fun setUpRetrofit() {
    mockWebServer = MockWebServer()
    client = OkHttpClient.Builder()
      .addInterceptor(networkLoggingInterceptor)
      .build()

    // Use retrofit with the MockWebServer here instead of MockRetrofit so that we can verify that
    // the full network request properly executes. MockRetrofit and MockWebServer perform the same
    // request mocking in different ways and we want to verify the full request is executed here.
    // See https://github.com/square/retrofit/issues/2340#issuecomment-302856504 for more context.
    retrofit = Retrofit.Builder()
      .baseUrl(mockWebServer.url(testUrl))
      .addConverterFactory(MoshiConverterFactory.create())
      .client(client)
      .build()
  }

  // TODO(#89): Move this to a common test application component.
  @Module
  class TestModule {
    @Provides
    @Singleton
    fun provideContext(application: Application): Context {
      return application
    }
  }

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(
    modules = [
      RobolectricModule::class, TestModule::class, TestLogReportingModule::class,
      TestDispatcherModule::class
    ]
  )
  interface TestApplicationComponent : DataProvidersInjector {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun inject(networkLoggingInterceptorTest: NetworkLoggingInterceptorTest)
  }

  class TestApplication : Application(), DataProvidersInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerNetworkLoggingInterceptorTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(networkLoggingInterceptorTest: NetworkLoggingInterceptorTest) {
      component.inject(networkLoggingInterceptorTest)
    }

    public override fun attachBaseContext(base: Context?) {
      super.attachBaseContext(base)
    }

    override fun getDataProvidersInjector(): DataProvidersInjector = component
  }
}
