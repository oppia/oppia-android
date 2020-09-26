package org.oppia.android.data.backends.gae

import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody
import org.oppia.android.data.Constants
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Interceptor on top of Retrofit to modify requests and response.
 *
 * The Interceptor removes XSSI_PREFIX from every response to produce valid Json.
 */
@Singleton
class NetworkInterceptor @Inject constructor() : Interceptor {

  @Throws(IOException::class)
  override fun intercept(chain: Interceptor.Chain): Response {
    val request = chain.request()
    val response = chain.proceed(request)

    if (response.code() == Constants.HTTP_OK) {
      if (response.body() != null) {
        var rawJson = response.body()!!.string()
        rawJson = removeXSSIPrefix(rawJson)
        val contentType = response.body()!!.contentType()
        val body = ResponseBody.create(contentType, rawJson)
        return response.newBuilder().body(body).build()
      }
    }
    return response
  }

  /**
   * This function accepts a non-null string which is a JSON response and
   * removes XSSI_PREFIX from response before deserialization.
   * @param rawJson: This is the string that we get in body of our response
   * @return String: rawJson without XSSI_PREFIX
   */
  fun removeXSSIPrefix(rawJson: String): String {
    return rawJson.removePrefix(NetworkSettings.XSSI_PREFIX).trimStart()
  }
}
