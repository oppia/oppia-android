package org.oppia.android.data.backends.gae

import android.content.Context
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import org.oppia.android.util.BuildConfig
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Interceptor on top of Retrofit to modify outgoing requests for authenticating messages.
 *
 * The Interceptor adds header parameters to all outgoing messages.
 */
@Singleton
class RemoteAuthNetworkInterceptor @Inject constructor() : Interceptor {

  @Inject
  lateinit var context: Context

  @NetworkApiKey
  lateinit var networkApiKey: String

  @Throws(IOException::class)
  override fun intercept(chain: Interceptor.Chain): Response {
    val request = addHeaders(chain.request())
    return chain.proceed(request)
  }

  /**
   * This function accepts the outgoing request and adds header values to it before sending.
   *
   * @param request the request being sent over the network
   * @return the exact same request as the parameter with added headers
   */
  fun addHeaders(request: Request): Request {
    return request.newBuilder()
      .addHeader("api_key", networkApiKey)
      .addHeader("app_package_name", context.packageName)
      .addHeader("app_version_name", BuildConfig.VERSION_NAME)
      .addHeader("app_version_code", BuildConfig.VERSION_CODE.toString())
      .build()
  }
}