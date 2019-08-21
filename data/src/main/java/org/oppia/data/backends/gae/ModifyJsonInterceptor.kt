package org.oppia.data.backends.gae

import okhttp3.*

import java.io.IOException

class ModifyJsonInterceptor : Interceptor {

  @Throws(IOException::class)
  override fun intercept(chain: Interceptor.Chain): Response {
    val request = chain.request()
    val response = chain.proceed(request)

    if (response.code() == 200) {
      if (response.body() != null) {
        var rawJson = response.body()!!.string()
        if (rawJson.startsWith(NetworkSettings.XSSI_PREFIX)) {
          rawJson = rawJson.substring(rawJson.indexOf('\n') + 1)
        }
        val contentType = response.body()!!.contentType()
        val body = ResponseBody.create(contentType, rawJson)
        return response.newBuilder().body(body).build()
      }
    } else if (response.code() == 403) {
      //TODO(#5): Identify how error handling will work (e.g. what Retrofit does),
      // whether RPC retry is supported & how it is/can be configured, etc.
    }
    return response
  }
}
