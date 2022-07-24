package org.oppia.android.domain.platformparameter.syncup

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.google.common.base.Optional
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.SettableFuture
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import org.oppia.android.app.model.PlatformParameter
import org.oppia.android.app.utility.getVersionName
import org.oppia.android.data.backends.gae.api.PlatformParameterService
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.oppialogger.exceptions.ExceptionsController
import org.oppia.android.domain.platformparameter.PlatformParameterController
import org.oppia.android.domain.util.getStringFromData
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.threading.BackgroundDispatcher
import retrofit2.Response
import java.lang.IllegalArgumentException
import java.lang.IllegalStateException
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi

/** Worker class that fetches and caches the latest platform parameters from the remote service. */
@OptIn(ExperimentalCoroutinesApi::class)
class PlatformParameterSyncUpWorker private constructor(
  context: Context,
  params: WorkerParameters,
  private val platformParameterController: PlatformParameterController,
  private val platformParameterService: Optional<PlatformParameterService>,
  private val oppiaLogger: OppiaLogger,
  private val exceptionsController: ExceptionsController,
  @BackgroundDispatcher private val backgroundDispatcher: CoroutineDispatcher
) : ListenableWorker(context, params) {

  companion object {
    /** Exception message when the type of values received in the network response are not valid. */
    const val INCORRECT_TYPE_EXCEPTION_MSG =
      "Platform parameter value has incorrect data type, ie. other than String/Int/Boolean"

    /** Exception message when there are no values received in the network response. */
    const val EMPTY_RESPONSE_EXCEPTION_MSG = "Received an empty map in the network response"

    /** A Tag for the logs that are associated with PlatformParameterSyncUpWorker. */
    const val TAG = "PlatformParameterWorker.tag"

    /** Value of worker-type associated with this PlatformParameterSyncUpWorker. */
    const val PLATFORM_PARAMETER_WORKER = "platform_parameter_worker"

    /** Key for passing the worker-type as a parameter to PlatformParameterSyncUpWorker. */
    const val WORKER_TYPE_KEY = "worker_type_key"
  }

  override fun startWork(): ListenableFuture<Result> {
    val backgroundScope = CoroutineScope(backgroundDispatcher)
    val result = backgroundScope.async {
      when (inputData.getStringFromData(WORKER_TYPE_KEY)) {
        PLATFORM_PARAMETER_WORKER -> refreshPlatformParameters()
        else -> Result.failure()
      }
    }

    val future = SettableFuture.create<Result>()
    result.invokeOnCompletion { failure ->
      if (failure != null) {
        future.setException(failure)
      } else {
        future.set(result.getCompleted())
      }
    }
    // TODO(#4463): Add withTimeout() to avoid potential hanging.
    return future
  }

  /**
   * Parses a map of platform parameter values into a [List<PlatformParameter>]. Parameters must be
   * of type String, Int or Boolean.
   */
  private fun parseNetworkResponse(response: Map<String, Any>): List<PlatformParameter> {
    return response.map {
      val platformParameter = PlatformParameter.newBuilder().setName(it.key)
      when (val value = it.value) {
        is String -> platformParameter.string = value
        is Int -> platformParameter.integer = value
        is Boolean -> platformParameter.boolean = value
        else -> throw IllegalArgumentException(INCORRECT_TYPE_EXCEPTION_MSG)
      }
      platformParameter.build()
    }
  }

  /** Synchronously executes the network request to get platform parameters from the Oppia backend */
  private fun makeNetworkCallForPlatformParameters(): Optional<Response<Map<String, Any>>?> {
    return platformParameterService.transform { service ->
      service?.getPlatformParametersByVersion(
        applicationContext.getVersionName()
      )?.execute()
    }
  }

  /** Extracts platform parameters from the remote service and stores them in the cache store */
  private suspend fun refreshPlatformParameters(): Result {
    return try {
      val optionalResponse = makeNetworkCallForPlatformParameters()
      val response = optionalResponse.orNull()
      if (response != null) {
        val responseBody = checkNotNull(response.body())
        val platformParameterList = parseNetworkResponse(responseBody)
        if (platformParameterList.isEmpty()) {
          throw IllegalArgumentException(EMPTY_RESPONSE_EXCEPTION_MSG)
        }
        val cachingResult = platformParameterController
          .updatePlatformParameterDatabase(platformParameterList)
          .retrieveData()
        if (cachingResult is AsyncResult.Failure) {
          throw IllegalStateException(cachingResult.error)
        }
        Result.success()
      } else {
        oppiaLogger.e(TAG, "Failed to fetch platform parameters (no network stack available)")
        Result.failure()
      }
    } catch (e: Exception) {
      oppiaLogger.e(TAG, "Failed to fetch platform parameters", e)
      exceptionsController.logNonFatalException(e)
      Result.failure()
    }
  }

  /** Creates an instance of [PlatformParameterSyncUpWorker] by properly injecting dependencies. */
  class Factory @Inject constructor(
    private val platformParameterController: PlatformParameterController,
    private val platformParameterService: Optional<PlatformParameterService>,
    private val oppiaLogger: OppiaLogger,
    private val exceptionsController: ExceptionsController,
    @BackgroundDispatcher private val backgroundDispatcher: CoroutineDispatcher
  ) {
    /** Returns new instances of [PlatformParameterSyncUpWorker]. */
    fun create(context: Context, params: WorkerParameters): ListenableWorker {
      return PlatformParameterSyncUpWorker(
        context,
        params,
        platformParameterController,
        platformParameterService,
        oppiaLogger,
        exceptionsController,
        backgroundDispatcher
      )
    }
  }
}
