package org.oppia.android.domain.platformparameter.syncup

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.oppia.android.app.model.PlatformParameter
import org.oppia.android.app.utility.getVersionName
import org.oppia.android.data.backends.gae.api.PlatformParameterService
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.oppialogger.exceptions.ExceptionsController
import org.oppia.android.domain.platformparameter.PlatformParameterController
import org.oppia.android.util.threading.BackgroundDispatcher
import retrofit2.Response
import java.lang.IllegalArgumentException
import java.lang.IllegalStateException
import javax.inject.Inject

/** Worker class that fetches and caches the latest platform parameters from the remote service. */
class PlatformParameterSyncUpWorker private constructor(
  context: Context,
  params: WorkerParameters,
  private val platformParameterController: PlatformParameterController,
  private val platformParameterService: PlatformParameterService,
  private val oppiaLogger: OppiaLogger,
  private val exceptionsController: ExceptionsController,
  @BackgroundDispatcher private val backgroundDispatcher: CoroutineDispatcher
) : CoroutineWorker(context, params) {

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

  override suspend fun doWork(): Result {
    return when (inputData.getString(WORKER_TYPE_KEY)) {
      PLATFORM_PARAMETER_WORKER -> withContext(backgroundDispatcher) { refreshPlatformParameters() }
      else -> Result.failure()
    }
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
  private fun makeNetworkCallForPlatformParameters(): Response<Map<String, Any>> {
    return platformParameterService.getPlatformParametersByVersion(
      applicationContext.getVersionName()
    ).execute()
  }

  /** Extracts platform parameters from the remote service and stores them in the cache store */
  private suspend fun refreshPlatformParameters(): Result {
    return try {
      val response = makeNetworkCallForPlatformParameters()
      val responseBody = checkNotNull(response.body())
      val platformParameterList = parseNetworkResponse(responseBody)
      if (platformParameterList.isEmpty()) {
        throw IllegalArgumentException(EMPTY_RESPONSE_EXCEPTION_MSG)
      }
      val cachingResult = platformParameterController
        .updatePlatformParameterDatabase(platformParameterList)
        .retrieveData()
      if (cachingResult.isFailure()) {
        throw IllegalStateException(cachingResult.getErrorOrNull())
      }
      Result.success()
    } catch (e: Exception) {
      oppiaLogger.e(TAG, "Failed to fetch the Platform Parameters", e)
      exceptionsController.logNonFatalException(e)
      Result.failure()
    }
  }

  /** Creates an instance of [PlatformParameterSyncUpWorker] by properly injecting dependencies. */
  class Factory @Inject constructor(
    private val platformParameterController: PlatformParameterController,
    private val platformParameterService: PlatformParameterService,
    private val oppiaLogger: OppiaLogger,
    private val exceptionsController: ExceptionsController,
    @BackgroundDispatcher private val backgroundDispatcher: CoroutineDispatcher
  ) {
    /** Returns new instances of [PlatformParameterSyncUpWorker]. */
    fun create(context: Context, params: WorkerParameters): CoroutineWorker {
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
