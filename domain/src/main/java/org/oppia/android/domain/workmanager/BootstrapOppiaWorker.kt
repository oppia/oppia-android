package org.oppia.android.domain.workmanager

import android.content.Context
import android.os.Looper
import androidx.work.ListenableWorker
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.guava.asListenableFuture
import org.oppia.android.domain.platformparameter.PlatformParameterController
import org.oppia.android.domain.platformparameter.PlatformParameterControllerInjectorProvider
import org.oppia.android.domain.util.getStringFromData
import org.oppia.android.util.logging.ConsoleLogger
import org.oppia.android.util.threading.DispatcherInjectorProvider
import javax.inject.Inject
import javax.inject.Provider

/**
 * The root and only [ListenableWorker] that the app uses for all [WorkManager]-manged work.
 *
 * This worker will automatically locate and find the [OppiaWorker] corresponding to the work
 * request and delegate work to it in an Oppia-managed background executor. It also handles worker
 * failures, refactors, and worker discontinuity across app releases.
 *
 * In order to make a new worker, the following needs to be done:
 * - [OppiaWorker] must be properly implemented as a new class with a app-unique worker name.
 * - The [OppiaWorker]'s [OppiaWorker.Factory] must be bound to the worker name.
 *
 * At that point, the worker can be scheduled using [WorkManagerScheduler]. An optional
 * worker-specific schedule-time callback listener can be created as well by implementing
 * [StartupWorkerScheduleReadinessListener] and set-binding it.
 *
 * There is a special debug worker setup that can be referenced to see how, exactly, to set up a new
 * [OppiaWorker], its scheduler, its module, and its tests. This is a fully functioning,
 * developer-available implementation of the [OppiaWorker] infrastructure.
 *
 * One implementation detail: this worker has a private constructor intentionally to prevent
 * [WorkManager] from trying to directly construct it (which it will do in some circumstances). That
 * failure will encourage the corresponding job to be cancelled.
 *
 * Implementers of [OppiaWorker] should generally err on the side of rescheduling periodic jobs that
 * may get into a bad state since this worker is designed to aggressively self-cancel itself if it
 * encounters an unexpected situation (such as an invalid [OppiaWorker.TaskType] which is expected
 * if a worker's supported task types change).
 */
class BootstrapOppiaWorker private constructor(
  private val appContext: Context,
  private val requestedWorkerName: String,
  workerParams: WorkerParameters,
  private val consoleLogger: ConsoleLogger,
  private val oppiaWorkerFactories: Map<String, Provider<OppiaWorker.Factory<*>>>
) : ListenableWorker(appContext, workerParams) {
  override fun startWork(): ListenableFuture<Result> {
    // TODO(#4463): Add withTimeout() to avoid potential hanging.
    return CoroutineScope(getBackgroundDispatcher()).async {
      startWorkInBackground()
    }.asListenableFuture()
  }

  private suspend fun startWorkInBackground(): Result {
    // Validate if this is even the correct worker. If this fails it's likely due to a custom worker
    // that isn't using the bootstrapper or a request to run an old worker that should now be
    // migrated to use the bootstrap worker.
    if (requestedWorkerName != javaClass.name) {
      consoleLogger.e(
        "BootstrapOppiaWorker",
        "Attempting to bootstrap old or invalid worker class: $requestedWorkerName." +
          " Cancelling worker to prevent repeated failures."
      )
      WorkManager.getInstance(appContext).cancelWorkById(id)
      return Result.failure()
    }

    // This may be the very beginning of the app starting up so platform parameters must be fully
    // initialized before a worker can even be created to do work (technically even its factory
    // cannot be created since a worker's factory directly depends on the worker's required
    // dependencies which may transitively require platform parameters to be loaded).
    getPlatformParameterController().loadParametersAsync().await()

    // Retrieve the actual worker name being started and the corresponding delegate factory
    // provider. These should always be present unless there's an old bootstrap-compatible worker
    // that was previously scheduled, but has since been removed or renamed.
    val delegatedWorkerName = inputData.getStringFromData(DELEGATED_WORKER_NAME_INPUT_KEY)
    val oppiaWorkerFactoryProvider = oppiaWorkerFactories[delegatedWorkerName]
    if (delegatedWorkerName == null || oppiaWorkerFactoryProvider == null) {
      consoleLogger.e(
        "BootstrapOppiaWorker",
        "Attempting to bootstrap for an invalid worker delegate: $delegatedWorkerName (no" +
          " provider found). Cancelling worker to prevent repeated failures."
      )
      WorkManager.getInstance(appContext).cancelWorkById(id)
      return Result.failure()
    }

    // Construct the factory first, then try to fetch the type of task attempting to be run.
    val factory = oppiaWorkerFactoryProvider.get()
    val taskTypeNameKey = constructTaskTypeKey(delegatedWorkerName)
    val taskTypeName = inputData.getStringFromData(taskTypeNameKey)

    // Delegate execution to a factory helper to simplify type safety.
    val result = taskTypeName?.let { factory.doWorkForTaskName(it) }
    if (result == null) {
      // If the task type has an incompatibility then err on the side of canceling the task in case
      // there was some sort if incompatible change in the worker's API. Since the bootstrap
      // worker's contract is to encourage rescheduling, any valid worker that reaches a failure=
      // here should hopefully self-correct itself with a future reschedule.
      consoleLogger.e(
        "BootstrapOppiaWorker",
        "Encountered invalid task type when trying to prepare worker $delegatedWorkerName:" +
          " $taskTypeName. Cancelling worker to prevent repeated failures."
      )
      WorkManager.getInstance(appContext).cancelWorkById(id)
      return Result.failure()
    }

    return when (result) {
      OppiaWorker.Result.SUCCESS -> Result.success()
      OppiaWorker.Result.FAILURE -> Result.failure()
    }
  }

  private fun getPlatformParameterController(): PlatformParameterController {
    val injectorProvider = appContext as PlatformParameterControllerInjectorProvider
    val injector = injectorProvider.getPlatformParameterControllerInjector()
    return injector.getPlatformParameterController()
  }

  private fun getBackgroundDispatcher(): CoroutineDispatcher {
    val injectorProvider = appContext as DispatcherInjectorProvider
    val injector = injectorProvider.getDispatcherInjector()
    return injector.getBackgroundDispatcher()
  }

  companion object {
    /**
     * The unique worker input key that is used to store the [OppiaWorker] name which will be
     * delegated execution.
     */
    const val DELEGATED_WORKER_NAME_INPUT_KEY = "BootstrapOppiaWorker.delegated_worker_name"

    /**
     * Returns the input key specific to the worker indicated by [workerName] that should be used to
     * encode the specific operation to run for the worker.
     */
    fun constructTaskTypeKey(workerName: String): String = "$workerName.TASK_TYPE_KEY"
  }

  /**
   * Factory to create new [BootstrapOppiaWorker]s.
   *
   * This should never need to be called directly since it's only used to wire the bootstrap worker
   * to [WorkManager].
   */
  class Factory @Inject constructor(
    private val context: Context,
    private val consoleLogger: ConsoleLogger,
    private val workerFactories: Map<String, @JvmSuppressWildcards Provider<OppiaWorker.Factory<*>>>
  ) {
    /**
     * Return a new [BootstrapOppiaWorker] for background execution.
     *
     * @param requestedWorkerName the name of the worker class [WorkManager] wants to run (which is
     *     expected to be the bootstrap worker's class name)
     * @param workerParams the [WorkerParameters] to use during worker execution
     */
    fun createBootstrapWorker(
      requestedWorkerName: String,
      workerParams: WorkerParameters
    ): BootstrapOppiaWorker {
      return BootstrapOppiaWorker(
        context, requestedWorkerName, workerParams, consoleLogger, workerFactories
      )
    }
  }
}
