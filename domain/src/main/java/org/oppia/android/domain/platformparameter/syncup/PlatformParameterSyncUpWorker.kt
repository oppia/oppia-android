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
import java.lang.IllegalArgumentException
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
      "Platform Parameter Value has incorrect data type, ie. other than String/Int/Boolean"

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

  // It's used to parse the Map of Platform Parameters to a List of Platform Parameter
  private fun parseNetworkResponse(response: Map<String, Any>): List<PlatformParameter> {
    val platformParameterList: MutableList<PlatformParameter> = mutableListOf()
    for (entry in response.entries) {
      val platformParameter = PlatformParameter.newBuilder().setName(entry.key)
      when (val value = entry.value) {
        is String -> platformParameter.string = value
        is Int -> platformParameter.integer = value
        is Boolean -> platformParameter.boolean = value
        else -> throw IllegalArgumentException(INCORRECT_TYPE_EXCEPTION_MSG)
      }
      platformParameterList.add(platformParameter.build())
    }
    return platformParameterList
  }

  /** Extracts platform parameters from the remote service and store them in the cache store */
  private fun refreshPlatformParameters(): Result {
    return try {
      val response = platformParameterService.getPlatformParametersByVersion(
        applicationContext.getVersionName()
      ).execute()
      val responseBody = checkNotNull(response.body())
      val platformParameterList = parseNetworkResponse(responseBody)
      platformParameterController.updatePlatformParameterDatabase(platformParameterList)
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
    /** A function that returns an instance of [PlatformParameterSyncUpWorker]. */
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
