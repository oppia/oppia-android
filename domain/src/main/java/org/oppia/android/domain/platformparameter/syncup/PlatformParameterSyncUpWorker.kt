package org.oppia.android.domain.platformparameter.syncup

import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.oppialogger.exceptions.ExceptionsController
import org.oppia.android.domain.platformparameter.PlatformParameterController
import org.oppia.android.domain.workmanager.OppiaWorker
import org.oppia.android.util.data.AsyncResult
import javax.inject.Inject

/** Worker class that fetches and caches the latest platform parameters from the remote service. */
class PlatformParameterSyncUpWorker private constructor(
  private val platformParameterController: PlatformParameterController,
  private val oppiaLogger: OppiaLogger,
  private val exceptionsController: ExceptionsController
) : OppiaWorker<PlatformParameterSyncUpWorker.Operation> {

  companion object {
    const val WORKER_NAME = "PlatformParameterSyncUpWorker"
  }
  enum class Operation(override val persistentName: String) : OppiaWorker.TaskType {
    REFRESH_PLATFORM_PARAMETERS("refresh_platform_parameters")
  }

  override suspend fun doWork(taskType: Operation): OppiaWorker.Result {
    return when (taskType) {
      Operation.REFRESH_PLATFORM_PARAMETERS -> {
        // This is valid to do per the contract of the returned DataProvider (there will only ever
        // be one result from the provider).
        when (val result = platformParameterController.downloadRemoteParameters().retrieveData()) {
          is AsyncResult.Pending -> {
            oppiaLogger.e(
              WORKER_NAME, "Unexpected pending state when downloading remote parameters."
            )
            OppiaWorker.Result.FAILURE
          }
          is AsyncResult.Failure -> {
            oppiaLogger.e(WORKER_NAME, "Failed to fetch platform parameters", result.error)
            exceptionsController.logNonFatalException(
              IllegalStateException("Failed to fetch platform parameters", result.error)
            )
            OppiaWorker.Result.FAILURE
          }
          is AsyncResult.Success -> OppiaWorker.Result.SUCCESS
        }
      }
    }
  }

  /** Creates an instance of [PlatformParameterSyncUpWorker] by properly injecting dependencies. */
  class Factory @Inject constructor(
    private val platformParameterController: PlatformParameterController,
    private val oppiaLogger: OppiaLogger,
    private val exceptionsController: ExceptionsController
  ) : OppiaWorker.Factory<Operation> {
    override val supportedTaskTypes: List<Operation> = Operation.values().toList()

    override fun createWorker(): OppiaWorker<Operation> {
      return PlatformParameterSyncUpWorker(
        platformParameterController,
        oppiaLogger,
        exceptionsController
      )
    }
  }
}
