package org.oppia.data.backends.gae

import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody
import org.oppia.data.Constants
import java.io.IOException

/**
 * Interceptor on top of Retrofit to modify requests and response
 * The Interceptor intercepts requests and response and in every response
 * it checks for XSSI_PREFIX and removes it to make a valid Json
 */
class NetworkInterceptor : Interceptor {

  @Throws(IOException::class)
  override fun intercept(chain: Interceptor.Chain): Response {
    val request = chain.request()
    val response = chain.proceed(request)

    if (response.code() == Constants.RESPONSE_SUCCESS) {
      if (response.body() != null) {
        var rawJson = response.body()!!.string()
        rawJson = removeXSSIPrefixFromResponse(rawJson)
        val contentType = response.body()!!.contentType()
        val body = ResponseBody.create(contentType, rawJson)
        return response.newBuilder().body(body).build()
      }
    }
    return response
  }

  /**
   * This method accepts a non-null string which is a JSON response and
   * removes XSSI_PREFIX from response before deserialization
   * @param rawJson: This is the string that we get in body of our response
   * @return String: rawJson without XSSI_PREFIX
   */
  fun removeXSSIPrefixFromResponse(rawJson: String): String {
    return rawJson.removePrefix(NetworkSettings.XSSI_PREFIX).trimStart()
  }
}
