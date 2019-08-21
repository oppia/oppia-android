package org.oppia.data.backends.gae

import okhttp3.*

import java.io.IOException

//TODO: Transfer this XSSI_PREFIX to a constant file
// which is responsible for networking too.
private const val XSSI_PREFIX = ")]}\'\n"

class ModifyJsonInterceptor : Interceptor {

  @Throws(IOException::class)
  override fun intercept(chain: Interceptor.Chain): Response {
    val request = chain.request()
    val response = chain.proceed(request)

    if (response.code() == 200) {
      if (response.body() != null) {
        var rawJson = response.body()!!.string()
        if (rawJson.startsWith(XSSI_PREFIX)) {
          rawJson = rawJson.substring(rawJson.indexOf('\n') + 1)
        }
        val contentType = response.body()!!.contentType()
        val body = ResponseBody.create(contentType, rawJson)
        return response.newBuilder().body(body).build()
      }
    } else if (response.code() == 403) {
      //TODO: Manage other network errors here
    }
    return response
  }

}
