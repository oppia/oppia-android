package org.oppia.android.domain.platformparameter.syncup

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import javax.inject.Inject

/** Custom [WorkerFactory] for the [PlatformParameterSyncUpWorker]. */
class PlatformParameterSyncUpWorkerFactory @Inject constructor(
  private val platformParameterSyncUpWorkerFactory: PlatformParameterSyncUpWorker.Factory
) : WorkerFactory() {
  override fun createWorker(
    context: Context,
    workerClassName: String,
    workerParameters: WorkerParameters
  ): ListenableWorker? {
    android.util.Log.e("@@@@@", "create ppsuw worker for class: $workerClassName")
    return platformParameterSyncUpWorkerFactory.create(context, workerParameters)
  }
}
