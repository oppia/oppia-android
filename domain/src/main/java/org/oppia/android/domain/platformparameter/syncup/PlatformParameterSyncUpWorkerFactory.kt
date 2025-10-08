package org.oppia.android.domain.platformparameter.syncup

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import javax.inject.Inject

/** Custom [WorkerFactory] for the [PlatformParameterSyncUpWorker]. */
class PlatformParameterSyncUpWorkerFactory @Inject constructor(
  private val workerFactory: PlatformParameterSyncUpWorker.Factory
) : WorkerFactory() {
  override fun createWorker(
    context: Context,
    workerClassName: String,
    workerParameters: WorkerParameters
  ): ListenableWorker? {
    return if (workerClassName == PlatformParameterSyncUpWorker::class.java.name) {
      workerFactory.create(context, workerParameters)
    } else null
  }
}
