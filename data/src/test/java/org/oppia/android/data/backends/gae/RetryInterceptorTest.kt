package org.oppia.android.data.backends.gae

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.junit.After
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.LooperMode
import java.io.IOException

/** Tests for [RetryInterceptor]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
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
    setUpClient()
  }

  @After
  fun tearDown() {
    mockWebServer.shutdown()
  }

  private fun setUpClient() {
    mockWebServer = MockWebServer()
    mockWebServer.start()
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
  fun testIntercept_networkReturns408_retriesThreeTimes_returns408() {
    // OkHttp's built-in RetryAndFollowUpInterceptor also automatically retries 408s.
    // We enqueue extra responses here so MockWebServer doesn't run out of responses 
    // and cause a SocketTimeoutException.
    for (i in 1..10) {
      mockWebServer.enqueue(MockResponse().setResponseCode(408).setBody("{}"))
    }

    val request = Request.Builder().url(mockWebServer.url("/")).build()
    val response = okHttpClient.newCall(request).execute()

    assertThat(response.code).isEqualTo(408)
    assertThat(mockWebServer.requestCount).isAtLeast(4)
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

  @Test
  fun testIntercept_networkDisconnectsOnEveryAttempt_retriesThreeTimes_throwsIOException() {
    // Original request + 3 retries = 4 total attempts.
    repeat(4) {
      mockWebServer.enqueue(
        MockResponse().apply { socketPolicy = SocketPolicy.DISCONNECT_AT_START }
      )
    }
    val request = Request.Builder().url(mockWebServer.url("/")).build()

    assertThrows(IOException::class.java) {
      okHttpClient.newCall(request).execute()
    }

    assertThat(mockWebServer.requestCount).isEqualTo(4)
  }

  @Test
  fun testIntercept_networkDisconnectsTwiceThen200_retriesTwice_returns200() {
    mockWebServer.enqueue(
      MockResponse().apply { socketPolicy = SocketPolicy.DISCONNECT_AT_START }
    )
    mockWebServer.enqueue(
      MockResponse().apply { socketPolicy = SocketPolicy.DISCONNECT_AT_START }
    )
    mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody("{}"))

    val request = Request.Builder().url(mockWebServer.url("/")).build()

    val response = okHttpClient.newCall(request).execute()

    assertThat(response.code).isEqualTo(200)
    assertThat(mockWebServer.requestCount).isEqualTo(3)
  }

  @Test
  fun testIntercept_networkDisconnectsThreeTimes_verifiesBackoffDelays() {
    // Original request + 3 retries = 4 total attempts.
    repeat(4) {
      mockWebServer.enqueue(
        MockResponse().apply { socketPolicy = SocketPolicy.DISCONNECT_AT_START }
      )
    }
    val request = Request.Builder().url(mockWebServer.url("/")).build()

    assertThrows(IOException::class.java) {
      okHttpClient.newCall(request).execute()
    }

    assertThat(fakeNetworkDelayHandler.recordedDelays)
      .containsExactly(
        EXPECTED_RETRY_DELAY_1_MILLIS,
        EXPECTED_RETRY_DELAY_2_MILLIS,
        EXPECTED_RETRY_DELAY_3_MILLIS
      )
      .inOrder()
  }
}
