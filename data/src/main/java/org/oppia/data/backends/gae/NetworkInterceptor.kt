package org.oppia.data.backends.gae

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody
import org.oppia.data.Constants
import java.io.IOException

private val TAG: String = NetworkInterceptor::class.java.simpleName

/** Interceptor on top of Retrofit to modify requests and response */
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

  fun removeXSSIPrefixFromResponse(rawJson: String): String {
    return rawJson.removePrefix(NetworkSettings.XSSI_PREFIX).trimStart()
  }
}
