package org.oppia.android.data.backends.gae

import okhttp3.Interceptor
import okhttp3.Response
import org.oppia.android.data.backends.gae.Constants.HTTP_BAD_GATEWAY
import org.oppia.android.data.backends.gae.Constants.HTTP_GATEWAY_TIMEOUT
import org.oppia.android.data.backends.gae.Constants.HTTP_REQUEST_TIMEOUT
import org.oppia.android.data.backends.gae.Constants.HTTP_SERVICE_UNAVAILABLE
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/** Interceptor on top of Retrofit to retry requests when transient HTTP errors occur. */
@Singleton
class RetryInterceptor @Inject constructor(
  private val networkDelayHandler: NetworkDelayHandler
) : Interceptor {

  override fun intercept(chain: Interceptor.Chain): Response {
    val request = chain.request()
    var lastException: IOException? = null

    for (tryCount in 0..RETRY_DELAYS_MILLIS.size) {
      try {
        val response = chain.proceed(request)
        val isLastAttempt = tryCount == RETRY_DELAYS_MILLIS.size
        // Return the response if it succeeded, if we can't retry this error code, or if we've
        // exhausted all retry attempts.
        if (response.isSuccessful || !shouldRetry(response.code) || isLastAttempt) {
          return response
        }
        // Close the response body before retrying to avoid resource leaks.
        response.close()
      } catch (e: IOException) {
        lastException = e
        // If this was the last attempt, rethrow so the caller knows the request failed.
        if (tryCount == RETRY_DELAYS_MILLIS.size) throw e
      }
      delayBeforeRetry(tryCount)
    }

    // This point is theoretically unreachable since the for-loop always exits via return or throw.
    // The compiler requires a return/throw here because it can't statically verify that.
    throw lastException ?: IOException("Request failed after retries")
  }

  private fun delayBeforeRetry(tryCount: Int) {
    try {
      networkDelayHandler.delay(RETRY_DELAYS_MILLIS[tryCount])
    } catch (e: InterruptedException) {
      Thread.currentThread().interrupt()
      throw IOException("Retry interrupted", e)
    }
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
