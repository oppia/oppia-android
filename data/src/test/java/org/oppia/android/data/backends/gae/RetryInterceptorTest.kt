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
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestDispatcherModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Singleton

/** Tests for [RetryInterceptor]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = RetryInterceptorTest.TestApplication::class)
class RetryInterceptorTest {

  private companion object {
    private const val EXPECTED_RETRY_DELAY_1_MILLIS = 2000L
    private const val EXPECTED_RETRY_DELAY_2_MILLIS = 4000L
    private const val EXPECTED_RETRY_DELAY_3_MILLIS = 10000L
  }

  private lateinit var mockWebServer: MockWebServer
  private lateinit var okHttpClient: OkHttpClient
  private lateinit var fakeNetworkDelayHandler: FakeNetworkDelayHandler
  private lateinit var retryInterceptor: RetryInterceptor

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
    setUpClient()
  }

  @After
  fun tearDown() {
    mockWebServer.shutdown()
  }

  private fun setUpClient() {
    mockWebServer = MockWebServer()
    fakeNetworkDelayHandler = FakeNetworkDelayHandler()
    retryInterceptor = RetryInterceptor(fakeNetworkDelayHandler)
    okHttpClient = OkHttpClient.Builder()
      .addInterceptor(retryInterceptor)
      .build()
  }

  private class FakeNetworkDelayHandler : NetworkDelayHandler {
    val recordedDelays = mutableListOf<Long>()

    override fun delay(millis: Long) {
      recordedDelays.add(millis)
    }
  }

  @Test
  fun testIntercept_networkReturns200_doesNotRetry_returns200() {
    mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody("{}"))

    val request = Request.Builder().url(mockWebServer.url("/")).build()
    val response = okHttpClient.newCall(request).execute()

    assertThat(response.code).isEqualTo(200)
    assertThat(mockWebServer.requestCount).isEqualTo(1)
  }

  @Test
  fun testIntercept_networkReturns404_doesNotRetry_returns404() {
    mockWebServer.enqueue(MockResponse().setResponseCode(404).setBody("{}"))

    val request = Request.Builder().url(mockWebServer.url("/")).build()
    val response = okHttpClient.newCall(request).execute()

    assertThat(response.code).isEqualTo(404)
    assertThat(mockWebServer.requestCount).isEqualTo(1)
  }

  @Test
  fun testIntercept_networkReturns502_retriesThreeTimes_returns502() {
    // Original request + 3 retries = 4 requests hitting the server
    mockWebServer.enqueue(MockResponse().setResponseCode(502).setBody("{}"))
    mockWebServer.enqueue(MockResponse().setResponseCode(502).setBody("{}"))
    mockWebServer.enqueue(MockResponse().setResponseCode(502).setBody("{}"))
    mockWebServer.enqueue(MockResponse().setResponseCode(502).setBody("{}"))

    val request = Request.Builder().url(mockWebServer.url("/")).build()
    val response = okHttpClient.newCall(request).execute()

    assertThat(response.code).isEqualTo(502)
    assertThat(mockWebServer.requestCount).isEqualTo(4)
  }

  @Test
  fun testIntercept_networkReturns503Then200_retriesOnce_returns200() {
    mockWebServer.enqueue(MockResponse().setResponseCode(503).setBody("{}"))
    mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody("{}"))

    val request = Request.Builder().url(mockWebServer.url("/")).build()
    val response = okHttpClient.newCall(request).execute()

    assertThat(response.code).isEqualTo(200)
    assertThat(mockWebServer.requestCount).isEqualTo(2)
  }

  @Test
  fun testIntercept_networkReturns502TwiceThen200_retriesTwice_returns200() {
    mockWebServer.enqueue(MockResponse().setResponseCode(502).setBody("{}"))
    mockWebServer.enqueue(MockResponse().setResponseCode(502).setBody("{}"))
    mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody("{}"))

    val request = Request.Builder().url(mockWebServer.url("/")).build()
    val response = okHttpClient.newCall(request).execute()

    assertThat(response.code).isEqualTo(200)
    assertThat(mockWebServer.requestCount).isEqualTo(3)
  }

  @Test
  fun testIntercept_networkReturnsMultipleDifferentErrorsThen200_retriesProperly_returns200() {
    mockWebServer.enqueue(MockResponse().setResponseCode(502).setBody("{}"))
    mockWebServer.enqueue(MockResponse().setResponseCode(504).setBody("{}"))
    mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody("{}"))

    val request = Request.Builder().url(mockWebServer.url("/")).build()
    val response = okHttpClient.newCall(request).execute()

    assertThat(response.code).isEqualTo(200)
    assertThat(mockWebServer.requestCount).isEqualTo(3)
  }

  @Test
  fun testIntercept_networkReturns502ThreeTimes_verifiesBackoffDelays() {
    mockWebServer.enqueue(MockResponse().setResponseCode(502).setBody("{}"))
    mockWebServer.enqueue(MockResponse().setResponseCode(502).setBody("{}"))
    mockWebServer.enqueue(MockResponse().setResponseCode(502).setBody("{}"))
    mockWebServer.enqueue(MockResponse().setResponseCode(502).setBody("{}"))

    val request = Request.Builder().url(mockWebServer.url("/")).build()
    okHttpClient.newCall(request).execute()

    assertThat(fakeNetworkDelayHandler.recordedDelays)
      .containsExactly(
        EXPECTED_RETRY_DELAY_1_MILLIS,
        EXPECTED_RETRY_DELAY_2_MILLIS,
        EXPECTED_RETRY_DELAY_3_MILLIS
      )
      .inOrder()
  }

  private fun getTestApplication() = ApplicationProvider.getApplicationContext<TestApplication>()

  private fun setUpTestApplicationComponent() {
    getTestApplication().inject(this)
  }

  @Module
  class TestModule {
    @Provides
    @Singleton
    fun provideContext(application: Application): Context = application
  }

  @Singleton
  @Component(
    modules = [
      RobolectricModule::class,
      TestDispatcherModule::class,
      TestModule::class
    ]
  )
  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder
      fun build(): TestApplicationComponent
    }

    fun inject(test: RetryInterceptorTest)
  }

  class TestApplication : Application() {
    private val component: TestApplicationComponent by lazy {
      DaggerRetryInterceptorTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(test: RetryInterceptorTest) {
      component.inject(test)
    }
  }
}
