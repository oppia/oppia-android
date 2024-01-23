package org.oppia.android.data.backends.gae

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.content.pm.ApplicationInfoBuilder
import androidx.test.core.content.pm.PackageInfoBuilder
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
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.data.backends.gae.api.TopicService
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.BackgroundTestDispatcher
import org.oppia.android.testing.threading.TestCoroutineDispatcher
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.util.data.DataProvidersInjector
import org.oppia.android.util.data.DataProvidersInjectorProvider
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [RemoteAuthNetworkInterceptor]. */
@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@Config(application = NetworkLoggingInterceptorTest.TestApplication::class)
@LooperMode(LooperMode.Mode.PAUSED)
class NetworkLoggingInterceptorTest {

  @Inject
  lateinit var networkLoggingInterceptor: NetworkLoggingInterceptor

  @Inject
  lateinit var context: Context

  @field:[Inject BackgroundTestDispatcher]
  lateinit var backgroundTestDispatcher: TestCoroutineDispatcher

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  private lateinit var retrofit: Retrofit

  private lateinit var mockWebServer: MockWebServer

  private lateinit var client: OkHttpClient

  private lateinit var topicService: TopicService

  private val testVersionName = "1.0"
  private val testVersionCode = 1

  private val testUrl = "/"
  private val testApiKey = "api_key"
  private val testApiKeyValue = "api_key_value"
  private val testResponseBody = "{\"test\": \"test\"}"

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
    setUpApplicationForContext()
    setUpRetrofit()
  }

  @After
  fun tearDown() {
    mockWebServer.shutdown()
  }

  @Test
  fun testLoggingInterceptor_makeCallToTopicService_logsNetworkCall() = runBlockingTest {
    val mockWebServerUrl = mockWebServer.url(testUrl)

    val request = Request.Builder()
      .url(mockWebServerUrl)
      .addHeader(testApiKey, testApiKeyValue)
      .build()

    mockWebServer.enqueue(MockResponse().setBody(testResponseBody))

    val job = launch {
      networkLoggingInterceptor.logNetworkCallFlow.collect {
        assertThat(it.urlCalled).isEqualTo(mockWebServerUrl.toString())
        assertThat(it.responseStatusCode).isEqualTo(200)
        assertThat(it.body).isEqualTo(testResponseBody)
      }
    }

    client.newCall(request).execute()
    testCoroutineDispatchers.advanceUntilIdle()
    job.cancel()
  }

  @Test
  fun testLoggingInterceptor_makeFailingCallToTopicService_logsNetworkCallFailed() =
    runBlockingTest {
      val mockWebServerUrl = mockWebServer.url(testUrl)

      val request = Request.Builder()
        .url(mockWebServerUrl)
        .build()

      val mockResponse = MockResponse()
        .setResponseCode(404)
        .setHeader(testApiKey, testApiKeyValue)
        .setBody(testResponseBody)

      mockWebServer.enqueue(mockResponse)

      val job = launch {
        networkLoggingInterceptor.logNetworkCallFlow.collect {
          assertThat(it.urlCalled).isEqualTo(mockWebServerUrl.toString())
          assertThat(it.responseStatusCode).isEqualTo(404)
          assertThat(it.body).isEqualTo(testResponseBody)
        }

        networkLoggingInterceptor.logFailedNetworkCallFlow.collect {
          assertThat(it.urlCalled).isEqualTo(mockWebServerUrl.toString())
          assertThat(it.responseStatusCode).isEqualTo(404)
          assertThat(it.headers.first()).isEqualTo(testApiKeyValue)
          assertThat(it.errorMessage).isEqualTo(testResponseBody)
        }
      }

      client.newCall(request).execute()
      testCoroutineDispatchers.advanceUntilIdle()
      job.cancel()
    }

//  @Test
//  fun testLoggingInterceptor_makeCallToTopicService_logsCorrectValues() {
//    val request = Request.Builder()
//      .url(mockWebServer.url("/"))
//      .addHeader("api_key", "wrong_api_key")
//      .build()
//
//    mockWebServer.enqueue(MockResponse().setBody(testResponseBody))
//    val response = client.newCall(request).execute()
//
//    assertThat(response.isSuccessful).isTrue()
//
//    testCoroutineDispatcher.runCurrent()
//    CoroutineScope(testCoroutineDispatcher).launch {
//      networkLoggingInterceptor.logNetworkCallFlow.collect {
//        assertThat(it.urlCalled).isEqualTo(testUrl)
//        assertThat(it.headers.first()).isEqualTo(testApiKeyValue)
//        assertThat(it.body).isEqualTo(testResponseBody)
//        assertThat(it.body.first()).isEqualTo("failed")
//        assertThat(it.responseStatusCode).isEqualTo(testResponseCode)
//      }
//    }

//    val testDataJson = "{}"
//    val successResponse = MockResponse().setBody(testDataJson)
//    mockWebServer.enqueue(successResponse)
//    topicService.getTopicByName(topicName).execute()
//    val request = mockWebServer.takeRequest()
//
//    assertThat(request.headers.get(testApiKey)).isEqualTo(testApiKeyValue)

//    mockWebServer.enqueue(MockResponse().setBody(testResponseBody))
//    val call = topicService.getTopicByName(topicName)
//
//    val response = call.execute()
//
//    assertThat(response.isSuccessful).isTrue()
//
//    val sharedFlow = networkLoggingInterceptor.logNetworkCallFlow.shareIn(CoroutineScope(testCoroutineDispatcher), SharingStarted.WhileSubscribed(200))

//    val request = Request.Builder()
//      .url(mockWebServer.url("/"))
//      .addHeader(testApiKey, testApiKeyValue)
//      .build()

//    CoroutineScope(testCoroutineDispatcher).launch {
//      println("This scope is launched")
//      networkLoggingInterceptor.logNetworkCallFlow.collect { retrofitCallContext ->
//        println("This is the retrofit call context")
//
//        assertThat(retrofitCallContext.urlCalled).isEqualTo(testUrl)
//        assertThat(retrofitCallContext.headers.first()).isEqualTo(testApiKeyValue)
//        assertThat(retrofitCallContext.body).isEqualTo(testResponseBody)
//        assertThat(retrofitCallContext.body.first()).isEqualTo("test")
//        assertThat(retrofitCallContext.responseStatusCode).isEqualTo(testResponseCode)
//      }
//    }
//
//    mockWebServer.enqueue(MockResponse().setBody("testResponseBody"))
//    client.newCall(request).execute()
//
//    CoroutineScope(testCoroutineDispatcher).launch {
//      networkLoggingInterceptor.logNetworkCallFlow.collect {
//        println("This is the retrofit call context")
//
//        assertThat(it.urlCalled).isEqualTo(testUrl)
//        assertThat(it.headers.first()).isEqualTo(testApiKeyValue)
//        assertThat(it.body).isEqualTo(testResponseBody)
//        assertThat(it.body.first()).isEqualTo("test")
//        assertThat(it.responseStatusCode).isEqualTo(testResponseCode)
//      }
//    }
//
//    val interceptedRequest = mockWebServer.takeRequest(
//      timeout = testCoroutineDispatcher.DEFAULT_TIMEOUT_SECONDS,
//      unit = testCoroutineDispatcher.DEFAULT_TIMEOUT_UNIT
//    )

//    assertThat(interceptedRequest?.headers?.get(testApiKey)).isEqualTo(testApiKeyValue)
//    assertThat(interceptedRequest?.body?.toString()).isEqualTo(testResponseBody)

//    mockWebServer.enqueue(MockResponse().setResponseCode(testResponseCode).setBody(testResponseBody))
//
//    val call = topicService.getTopicByName(topicName)
//    val serviceRequest = call.request()
//
//    assertThat(serviceRequest.header(testApiKey)).isEqualTo(testApiKeyValue)
//
//    CoroutineScope(testCoroutineDispatcher).launch {
//      println("This scope is launched")
//      networkLoggingInterceptor.logNetworkCallFlow.collect { retrofitCallContext ->
//        println("This is the retrofit call context")
//
//        assertThat(retrofitCallContext.urlCalled).isEqualTo(testUrl)
//        assertThat(retrofitCallContext.headers.first()).isEqualTo(testApiKeyValue)
//        assertThat(retrofitCallContext.body).isEqualTo(testResponseBody)
//        assertThat(retrofitCallContext.body.first()).isEqualTo("test")
//        assertThat(retrofitCallContext.responseStatusCode).isEqualTo(testResponseCode)
//      }
//    }
//  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
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
    packageInfo.versionName = testVersionName
    packageInfo.versionCode = testVersionCode
    packageManager.installPackage(packageInfo)
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

    topicService = retrofit.create(TopicService::class.java)
  }

  // TODO(#89): Move this to a common test application component.
  @Module
  class TestNetworkModule {
    @Provides
    @Singleton
    @NetworkApiKey
    fun provideNetworkApiKey(): String = "test_api_key"
  }

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
      RobolectricModule::class, TestNetworkModule::class, TestModule::class,
      TestLogReportingModule::class, TestDispatcherModule::class
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
