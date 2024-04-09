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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runBlockingTest
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

  @Inject lateinit var networkLoggingInterceptor: NetworkLoggingInterceptor
  @Inject lateinit var context: Context
  @Inject lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

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

    request = Request.Builder()
      .url(mockWebServerUrl)
      .addHeader(testApiKey, testApiKeyValue)
      .build()
  }

  @After
  fun tearDown() {
    mockWebServer.shutdown()
  }

  @Test
  fun testLoggingInterceptor_makeNetworkCall_succeeds() = runBlockingTest {
    mockWebServer.enqueue(MockResponse().setBody(testResponseBody))
    client.newCall(request).execute()
    testCoroutineDispatchers.advanceUntilIdle()

    val networkJob = launch {
      networkLoggingInterceptor.logNetworkCallFlow.collect {
        assertThat(it.requestUrl).isEqualTo(mockWebServerUrl.toString())
        assertThat(it.responseStatusCode).isEqualTo(HttpURLConnection.HTTP_OK)
        assertThat(it.headers).contains(headerString)
        assertThat(it.body).isEqualTo(testResponseBody)
      }
    }

    networkJob.cancel()
  }

  @Test
  fun testLoggingInterceptor_makeNetworkCallWithInvalidUrl_failsAndCompletes() =
    runBlockingTest {
      val pageNotFound = HttpURLConnection.HTTP_NOT_FOUND
      val mockResponse = MockResponse()
        .setResponseCode(pageNotFound)
        .setBody(testResponseBody)

      mockWebServer.enqueue(mockResponse)
      client.newCall(request).execute()

      val networkJob = launch {
        networkLoggingInterceptor.logNetworkCallFlow.collect {
          assertThat(it.requestUrl).isEqualTo(mockWebServerUrl.toString())
          assertThat(it.responseStatusCode).isEqualTo(pageNotFound)
          assertThat(it.headers).contains(headerString)
          assertThat(it.body).isEqualTo(testResponseBody)
        }
      }

      val failedNetworkJob = launch {
        networkLoggingInterceptor.logFailedNetworkCallFlow.collect {
          assertThat(it.requestUrl).isEqualTo(mockWebServerUrl.toString())
          assertThat(it.responseStatusCode).isEqualTo(pageNotFound)
          assertThat(it.headers).contains(headerString)
          assertThat(it.body).isEmpty()
          assertThat(it.errorMessage).isNotEmpty()
          assertThat(it.errorMessage).isEqualTo(testResponseBody)
        }
      }

      networkJob.cancel()
      failedNetworkJob.cancel()
    }

  @Test
  fun testLoggingInterceptor_makeCrashingNetworkCall_failsAndCompletes() =
    runBlockingTest {
      mockWebServer.shutdown()
      try { client.newCall(request).execute() } catch (e: Exception) { }

      val failedNetworkJob = launch {
        networkLoggingInterceptor.logFailedNetworkCallFlow.collect {
          assertThat(it.requestUrl).isEqualTo(mockWebServerUrl.toString())
          assertThat(it.responseStatusCode).isEqualTo(0)
          assertThat(it.headers).contains(headerString)
          assertThat(it.body).isEmpty()
          assertThat(it.errorMessage).isNotEmpty()
          assertThat(it.errorMessage).contains("Failed to connect to localhost")
        }
      }
      failedNetworkJob.cancel()
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
