package org.oppia.android.data.backends.gae

import android.content.Context
import okhttp3.Interceptor
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
class RemoteAuthNetworkInterceptor @Inject constructor(
  private val context: Context,
  @NetworkApiKey private val networkApiKey: String
) : Interceptor {

  @Throws(IOException::class)
  override fun intercept(chain: Interceptor.Chain): Response {
    val request = chain.request().newBuilder()
      .addHeader("api_key", networkApiKey)
      .addHeader("app_package_name", context.packageName)
      .addHeader("app_version_name", BuildConfig.VERSION_NAME)
      .addHeader("app_version_code", BuildConfig.VERSION_CODE.toString())
      .build()
    return chain.proceed(request)
  }
}