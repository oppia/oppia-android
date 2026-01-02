package org.oppia.android.domain.workmanager.debug

import org.oppia.android.domain.workmanager.OppiaWorker
import org.oppia.android.util.logging.ConsoleLogger
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class DebugWorker private constructor(
  private val consoleLogger: ConsoleLogger
) : OppiaWorker<DebugWorker.Operation> {
  override suspend fun doWork(taskType: Operation): OppiaWorker.Result {
    consoleLogger.d(WORKER_NAME, "Debug worker ran with config: $taskType.")
    return OppiaWorker.Result.SUCCESS
  }

  enum class Operation(
    val period: Long,
    val periodUnit: TimeUnit,
    val requireConnectivity: Boolean
  ) : OppiaWorker.TaskType {
    RUN_EVERY_FIFTEEN_MINUTES_WITH_CONNECTIVITY(
      period = 15, periodUnit = TimeUnit.MINUTES, requireConnectivity = true
    ),
    RUN_EVERY_TWENTY_MINUTES_WITH_OR_WITHOUT_CONNECTIVITY(
      period = 20, periodUnit = TimeUnit.MINUTES, requireConnectivity = false
    ),
    RUN_EVERY_SIX_HOURS_WITH_OR_WITHOUT_CONNECTIVITY(
      period = 6, periodUnit = TimeUnit.HOURS, requireConnectivity = false
    );

    // It's safe for this to just be the enum's name because it won't ever be minified since this
    // worker only goes in debug builds.
    override val persistentName = name
  }

  class Factory @Inject constructor(
    private val consoleLogger: ConsoleLogger
  ) : OppiaWorker.Factory<Operation> {
    override val supportedTaskTypes: List<Operation> = Operation.values().toList()

    override fun createWorker(): OppiaWorker<Operation> = DebugWorker(consoleLogger)
  }

  companion object {
    const val WORKER_NAME = "DebugWorker"
  }
}
