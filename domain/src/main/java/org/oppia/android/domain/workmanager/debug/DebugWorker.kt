package org.oppia.android.domain.workmanager.debug

import org.oppia.android.domain.workmanager.OppiaWorker
import org.oppia.android.util.logging.ConsoleLogger
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * A developer-only [OppiaWorker] to demonstrate how the custom infrastructure works.
 *
 * This worker simply logs a debug line corresponding to its [Operation] when run.
 */
class DebugWorker private constructor(
  private val consoleLogger: ConsoleLogger
) : OppiaWorker<DebugWorker.Operation> {
  override suspend fun doWork(taskType: Operation): OppiaWorker.Result {
    consoleLogger.d(WORKER_NAME, "Debug worker ran with config: $taskType.")
    return OppiaWorker.Result.SUCCESS
  }

  /** The operations supported by [DebugWorker]. */
  enum class Operation(
    val period: Long,
    val periodUnit: TimeUnit,
    val requireConnectivity: Boolean
  ) : OppiaWorker.TaskType {
    /**
     * A configuration to demonstrate the worker running every 15 minutes but only when there's
     * connectivity.
     */
    RUN_EVERY_FIFTEEN_MINUTES_WITH_CONNECTIVITY(
      period = 15, periodUnit = TimeUnit.MINUTES, requireConnectivity = true
    ),
    /**
     * A configuration to demonstrate the worker running every 20 minutes even if there's no
     * connectivity.
     */
    RUN_EVERY_TWENTY_MINUTES_WITH_OR_WITHOUT_CONNECTIVITY(
      period = 20, periodUnit = TimeUnit.MINUTES, requireConnectivity = false
    ),
    /**
     * A configuration to demonstrate the worker running every 6 hours even if there's no
     * connectivity.
     */
    RUN_EVERY_SIX_HOURS_WITH_OR_WITHOUT_CONNECTIVITY(
      period = 6, periodUnit = TimeUnit.HOURS, requireConnectivity = false
    );

    // It's safe for this to just be the enum's name because it won't ever be minified since this
    // worker only goes in debug builds.
    override val persistentName = name
  }

  /** An injectable [OppiaWorker.Factor] to build new [DebugWorker]s. */
  class Factory @Inject constructor(
    private val consoleLogger: ConsoleLogger
  ) : OppiaWorker.Factory<Operation> {
    override val supportedTaskTypes: List<Operation> = Operation.values().toList()

    override fun createWorker(): OppiaWorker<Operation> = DebugWorker(consoleLogger)
  }

  companion object {
    /** The unique name corresponding to this worker. */
    const val WORKER_NAME = "DebugWorker"
  }
}
