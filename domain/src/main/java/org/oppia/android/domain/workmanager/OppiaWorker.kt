package org.oppia.android.domain.workmanager

import org.oppia.android.util.logging.ConsoleLogger

/**
 * Represents a worker that's supported to perform arbitrary background operations even when the app
 * is not currently running with a foreground activity.
 *
 * This is especially useful for operations that need to happen periodically (such as updating the
 * app's platform parameter values) or upon certain constraints (such as a user's device
 * reconnecting to the internet). Job and constraint management are handled by AndroidX's
 * `WorkManager` (which delegates internal execution to OS-level schedulers in a cross-API way).
 *
 * Workers must defined different the [TaskType]s that they support for execution. While workers do
 * not need to provide more than one [TaskType] it can be really useful to split up the worker's
 * responsibilities into tasks since each one will be a unique scheduled worker in `WorkManager`
 * which means it can have its own scheduled interval and constraints.
 *
 * See [BootstrapOppiaWorker] for specifics on how to implement a new [OppiaWorker].
 */
interface OppiaWorker<T : OppiaWorker.TaskType> {
  /** Performs the background work corresponding to [taskType] and returns a [Result]. */
  suspend fun doWork(taskType: T): Result

  /** Represents the result of performing a particular task of background work. */
  enum class Result {
    /** Indicates that the background task succeeded. */
    SUCCESS,
    /** Indicates that the background task failed. */
    FAILURE
  }

  /** Represents a unique bit of work that the worker implementation supports. */
  interface TaskType {
    /**
     * The unique name corresponding to this task.
     *
     * **Important**: This name must be able to survive across different obfuscations of the app.
     * That means that it cannot just use a built-in generated enum name since those aren't
     * guaranteed to be the same across release builds.
     *
     * The name doesn't need to be stable across job runs (i.e. it can change across app versions),
     * but there's a cost in this since it may require a previous scheduled job failing and a new
     * schedule needing to replace it.
     */
    val persistentName: String
  }

  /** Creates new instances of this [OppiaWorker] implementation for the specified [TaskType]. */
  interface Factory<T : TaskType> {
    /**
     * The list of [TaskType]s supported by this worker.
     *
     * This list can change across app builds but it should be stable for the runtime of a single
     * app instance.
     */
    val supportedTaskTypes: List<T>

    /** Returns a new instance of the [OppiaWorker] implementation. */
    fun createWorker(): OppiaWorker<T>

    /**
     * Convenience function for actually creating a worker and running the work corresponding to the
     * provided task name.
     *
     * This ought to never be called directly. It needs to exist to avoid a type safety issue within
     * [BootstrapOppiaWorker]'s internal workings.
     *
     * @param consoleLogger the [ConsoleLogger] to log information messages to
     * @param workerName the name of this worker about to run (for logging purposes)
     * @param taskName the [TaskType.persistentName] corresponding to the task to run
     * @return the [Result] of running the job, or `null` if the provided task name doesn't
     *     correspond to any known [TaskType]s supported by the [OppiaWorker]
     */
    suspend fun doWorkForTaskName(
      consoleLogger: ConsoleLogger, workerName: String, taskName: String
    ): Result? {
      val taskType = supportedTaskTypes.find { taskName == it.persistentName } ?: return null
      consoleLogger.i("BootstrapOppiaWorker", "Starting task $taskName in worker $workerName.")
      return createWorker().doWork(taskType)
    }
  }
}
