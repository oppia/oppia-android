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
  /**
   * A flow that emits a [RetrofitCallContext] when a network call is made.
   */
  val logNetworkCallFlow: SharedFlow<RetrofitCallContext> = _logNetworkCallFlow

  private val _logFailedNetworkCallFlow = MutableSharedFlow<RetrofitCallFailedContext>()

  /**
   * A flow that emits a [RetrofitCallFailedContext] when a network call fails.
   */
  val logFailedNetworkCallFlow: SharedFlow<RetrofitCallFailedContext> = _logFailedNetworkCallFlow

  @Throws(IOException::class)
  override fun intercept(chain: Interceptor.Chain): Response {
    val request = chain.request()

    return try {
      val response = chain.proceed(request)

      val responseBody = response.body?.string()

      CoroutineScope(backgroundDispatcher).launch {
        _logNetworkCallFlow.emit(
          RetrofitCallContext.newBuilder()
            .setRequestUrl(request.url.toString())
            .setHeaders(request.headers.toString())
            .setResponseStatusCode(response.code)
            .setBody(responseBody ?: "")
            .build()
        )
      }

      if (!response.isSuccessful) {
        CoroutineScope(backgroundDispatcher).launch {
          _logFailedNetworkCallFlow.emit(
            RetrofitCallFailedContext.newBuilder()
              .setRequestUrl(request.url.toString())
              .setHeaders(request.headers.toString())
              .setResponseStatusCode(response.code)
              .setErrorMessage(responseBody ?: "")
              .build()
          )
        }
      }

      response
    } catch (exception: Exception) {
      CoroutineScope(backgroundDispatcher).launch {
        _logFailedNetworkCallFlow.emit(
          RetrofitCallFailedContext.newBuilder()
            .setRequestUrl(request.url.toString())
            .setHeaders(request.headers.toString())
            .setResponseStatusCode(0)
            .setErrorMessage(exception.toString())
            .build()
        )
      }
      chain.proceed(request)
    }
  }
}
