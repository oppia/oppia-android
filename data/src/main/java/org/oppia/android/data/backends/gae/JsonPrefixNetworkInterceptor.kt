package org.oppia.android.data.backends.gae

import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Interceptor on top of Retrofit to modify requests and response.
 *
 * The Interceptor removes XSSI_PREFIX from every response to produce valid Json.
 */
@Singleton
class JsonPrefixNetworkInterceptor @Inject constructor(
  @XssiPrefix private val xssiPrefix: String
) : Interceptor {

  @Throws(IOException::class)
  override fun intercept(chain: Interceptor.Chain): Response {
    val request = chain.request()
    val response = chain.proceed(request)

    if (response.code == Constants.HTTP_OK) {
      response.body?.let { responseBody ->
        var rawJson = responseBody.string()
        rawJson = removeXssiPrefix(rawJson)
        val contentType = responseBody.contentType()
        val body = ResponseBody.create(contentType, rawJson)
        return response.newBuilder().body(body).build()
      }
    }
    return response
  }

  /** Removes the XSSI prefix from the specified raw JSON & returns the result. */
  fun removeXssiPrefix(rawJson: String): String {
    return rawJson.removePrefix(xssiPrefix).trimStart()
  }
}
