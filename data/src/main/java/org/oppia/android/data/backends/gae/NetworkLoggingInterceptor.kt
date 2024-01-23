package org.oppia.android.data.backends.gae

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import okhttp3.Interceptor
import okhttp3.Response
import org.oppia.android.app.model.EventLog.RetrofitCallContext
import org.oppia.android.app.model.EventLog.RetrofitCallFailedContext
import org.oppia.android.util.threading.BackgroundDispatcher
import java.io.IOException
import java.lang.Exception
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Interceptor on top of Retrofit to log network requests and responses.
 */
@Singleton
class NetworkLoggingInterceptor @Inject constructor(
  @BackgroundDispatcher private val backgroundDispatcher: CoroutineDispatcher,
) : Interceptor {
  private val _logNetworkCallFlow = MutableSharedFlow<RetrofitCallContext>()
  val logNetworkCallFlow: SharedFlow<RetrofitCallContext> = _logNetworkCallFlow

  private val _logFailedNetworkCallFlow = MutableSharedFlow<RetrofitCallFailedContext>()
  val logFailedNetworkCallFlow: SharedFlow<RetrofitCallFailedContext> = _logFailedNetworkCallFlow

  @Throws(IOException::class)
  override fun intercept(chain: Interceptor.Chain): Response {
    try {
      val request = chain.request()
      val response = chain.proceed(request)

      CoroutineScope(backgroundDispatcher).launch {
        _logNetworkCallFlow.emit(
          RetrofitCallContext.newBuilder()
            .setUrlCalled(request.url.toString())
            .setHeaders(request.headers.toString())
            .setResponseStatusCode(response.code)
            .setBody(response.body?.string() ?: "")
            .build()
        )
      }

      if (!response.isSuccessful) {
        CoroutineScope(backgroundDispatcher).launch {
          _logFailedNetworkCallFlow.emit(
            RetrofitCallFailedContext.newBuilder()
              .setUrlCalled(request.url.toString())
              .setHeaders(request.headers.toString())
              .setResponseStatusCode(response.code)
              .setErrorMessage(response.body?.toString() ?: "")
              .build()
          )
        }
      }

      return response
    } catch (exception: Exception) {
      val request = chain.request()

      CoroutineScope(backgroundDispatcher).launch {
        _logFailedNetworkCallFlow.emit(
          RetrofitCallFailedContext.newBuilder()
            .setUrlCalled(request.url.toString())
            .setHeaders(request.headers.toString())
            .setResponseStatusCode(0)
            .setErrorMessage(exception.message ?: "")
            .build()
        )
      }
      return chain.proceed(request)
    }
  }
}
