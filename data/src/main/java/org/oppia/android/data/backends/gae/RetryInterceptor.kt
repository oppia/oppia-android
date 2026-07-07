package org.oppia.android.data.backends.gae

import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import okhttp3.Interceptor
import okhttp3.Response
import org.oppia.android.data.backends.gae.Constants.HTTP_BAD_GATEWAY
import org.oppia.android.data.backends.gae.Constants.HTTP_GATEWAY_TIMEOUT
import org.oppia.android.data.backends.gae.Constants.HTTP_REQUEST_TIMEOUT
import org.oppia.android.data.backends.gae.Constants.HTTP_SERVICE_UNAVAILABLE


/** Interceptor on top of Retrofit to retry requests when transient HTTP errors occur. */
@Singleton
class RetryInterceptor @Inject constructor(
  private val networkDelayHandler: NetworkDelayHandler
) : Interceptor {

  @Throws(IOException::class)
  override fun intercept(chain: Interceptor.Chain): Response {
    val request = chain.request()
    var response = chain.proceed(request)
    var tryCount = 0

    while (!response.isSuccessful && shouldRetry(response.code) && tryCount < RETRY_DELAYS_MILLIS.size) {
      // Close the previous response body before retrying to avoid resource leaks.
      response.close()

      try {
        networkDelayHandler.delay(RETRY_DELAYS_MILLIS[tryCount])
      } catch (e: InterruptedException) {
        Thread.currentThread().interrupt()
        throw IOException("Retry interrupted", e)
      }

      tryCount++
      response = chain.proceed(request)
    }

    return response
  }

  private fun shouldRetry(statusCode: Int): Boolean {
    return statusCode == HTTP_REQUEST_TIMEOUT ||
      statusCode == HTTP_BAD_GATEWAY ||
      statusCode == HTTP_SERVICE_UNAVAILABLE ||
      statusCode == HTTP_GATEWAY_TIMEOUT
  }

  companion object {
    private const val RETRY_DELAY_1_MILLIS = 2000L
    private const val RETRY_DELAY_2_MILLIS = 4000L
    private const val RETRY_DELAY_3_MILLIS = 10000L

    private val RETRY_DELAYS_MILLIS = listOf(
      RETRY_DELAY_1_MILLIS,
      RETRY_DELAY_2_MILLIS,
      RETRY_DELAY_3_MILLIS
    )
  }
}
