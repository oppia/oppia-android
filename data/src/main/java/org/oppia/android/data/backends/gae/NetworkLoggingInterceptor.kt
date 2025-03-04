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
import kotlin.text.Charsets

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

      // The response body needs to be cloned for reading otherwise it will throw since the body can
      // only be read fully at most one time and other interceptors in the chain may read it. See
      // https://stackoverflow.com/a/33862068 or OkHttp's HttpLoggingInterceptor for a reference.
      val responseBody = response.body
      val requestLength = responseBody?.contentLength()?.takeIf { it != -1L }
      val responseBodyText =
        responseBody?.source()?.also {
          it.request(requestLength ?: Long.MAX_VALUE)
        }?.buffer?.clone()?.readString(Charsets.UTF_8)

      CoroutineScope(backgroundDispatcher).launch {
        _logNetworkCallFlow.emit(
          RetrofitCallContext.newBuilder()
            .setRequestUrl(request.url.toString())
            .setHeaders(request.headers.toString())
            .setResponseStatusCode(response.code)
            .setBody(responseBodyText ?: "")
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
              .setErrorMessage(responseBodyText ?: "")
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
