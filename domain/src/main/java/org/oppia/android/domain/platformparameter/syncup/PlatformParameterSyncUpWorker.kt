package org.oppia.android.domain.platformparameter.syncup

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.guava.asListenableFuture
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.oppialogger.exceptions.ExceptionsController
import org.oppia.android.domain.platformparameter.PlatformParameterController
import org.oppia.android.domain.util.getStringFromData
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.threading.BackgroundDispatcher
import javax.inject.Inject

/** Worker class that fetches and caches the latest platform parameters from the remote service. */
class PlatformParameterSyncUpWorker private constructor(
  context: Context,
  params: WorkerParameters,
  private val platformParameterController: PlatformParameterController,
  private val oppiaLogger: OppiaLogger,
  private val exceptionsController: ExceptionsController,
  @BackgroundDispatcher private val backgroundDispatcher: CoroutineDispatcher
) : ListenableWorker(context, params) {

  companion object {
    /** A Tag for the logs that are associated with PlatformParameterSyncUpWorker. */
    const val TAG = "PlatformParameterWorker.tag"

    /** Value of worker-type associated with this PlatformParameterSyncUpWorker. */
    const val PLATFORM_PARAMETER_WORKER = "platform_parameter_worker"

    /** Key for passing the worker-type as a parameter to PlatformParameterSyncUpWorker. */
    const val WORKER_TYPE_KEY = "worker_type_key"
  }

  override fun startWork(): ListenableFuture<Result> {
    val backgroundScope = CoroutineScope(backgroundDispatcher)
    // TODO(#4463): Add withTimeout() to avoid potential hanging.
    return backgroundScope.async {
      when (inputData.getStringFromData(WORKER_TYPE_KEY)) {
        PLATFORM_PARAMETER_WORKER -> refreshPlatformParameters()
        else -> Result.failure()
      }
    }.asListenableFuture()
  }

  /** Extracts platform parameters from the remote service and stores them in the cache store. */
  private suspend fun refreshPlatformParameters(): Result {
    // This is valid to do per the contract of the returned DataProvider (there will only ever be
    // one result from the provider).
    val result = platformParameterController.downloadRemoteParameters().retrieveData()
    return when (result) {
      is AsyncResult.Pending -> {
        oppiaLogger.e(TAG, "Unexpected pending state when downloading remote parameters.")
        Result.failure()
      }
      is AsyncResult.Failure -> {
        oppiaLogger.e(TAG, "Failed to fetch platform parameters", result.error)
        exceptionsController.logNonFatalException(
          IllegalStateException("Failed to fetch platform parameters", result.error)
        )
        Result.failure()
      }
      is AsyncResult.Success -> Result.Success()
    }
  }

  /** Creates an instance of [PlatformParameterSyncUpWorker] by properly injecting dependencies. */
  class Factory @Inject constructor(
    private val platformParameterController: PlatformParameterController,
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
        oppiaLogger,
        exceptionsController,
        backgroundDispatcher
      )
    }
  }
}
