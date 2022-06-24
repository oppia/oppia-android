package org.oppia.android.domain.oppialogger.loggenerator

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import javax.inject.Inject

/** Custom [WorkerFactory] for the [MetricLogSchedulingWorker]. */
class MetricLogSchedulingWorkerFactory @Inject constructor(
  private val workerFactory: MetricLogSchedulingWorker.Factory
) : WorkerFactory() {

  /** Returns a new [MetricLogSchedulingWorker] for the given context and parameters. */
  override fun createWorker(
    appContext: Context,
    workerClassName: String,
    workerParameters: WorkerParameters
  ): ListenableWorker {
    return workerFactory.create(appContext, workerParameters)
  }
}
