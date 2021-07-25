package org.oppia.android.domain.platformparameter.syncup

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import javax.inject.Inject

class PlatformParameterSyncUpWorkerFactory @Inject constructor(
  private val parameterRefreshWorkerFactory: PlatformParameterSyncUpWorker.Factory
) : WorkerFactory() {
  override fun createWorker(
    context: Context,
    workerClassName: String,
    workerParameters: WorkerParameters
  ): ListenableWorker? {
    return parameterRefreshWorkerFactory.create(context, workerParameters)
  }
}
