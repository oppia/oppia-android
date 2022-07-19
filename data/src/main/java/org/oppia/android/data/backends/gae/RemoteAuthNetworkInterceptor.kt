package org.oppia.android.data.backends.gae

import android.content.Context
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import org.oppia.android.util.extensions.getVersionCode
import org.oppia.android.util.extensions.getVersionName
import java.io.IOException
import javax.inject.Inject

/**
 * Interceptor on top of Retrofit to modify outgoing requests for authenticating messages.
 *
 * The Interceptor adds header parameters to outgoing messages.
 */
class RemoteAuthNetworkInterceptor @Inject constructor() : Interceptor {

  @Inject
  lateinit var context: Context

  @JvmField
  @field:[Inject NetworkApiKey]
  var networkApiKey: String = ""

  @Throws(IOException::class)
  override fun intercept(chain: Interceptor.Chain): Response {
    val request = addAuthHeaders(chain.request())
    return chain.proceed(request)
  }

  /**
   * This function accepts the outgoing request and adds header values to it before sending.
   *
   * @param request the request being sent over the network
   * @return the exact same request as the parameter with added headers
   */
  private fun addAuthHeaders(request: Request): Request {
    return request.newBuilder()
      .addHeader("api_key", networkApiKey)
      .addHeader("app_package_name", context.packageName)
      .addHeader("app_version_name", context.getVersionName())
      .addHeader("app_version_code", context.getVersionCode().toString())
      .build()
  }
}
