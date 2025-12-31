package org.oppia.android.domain.workmanager

import android.content.Context
import androidx.work.Configuration
import androidx.work.DelegatingWorkerFactory
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import dagger.Module
import dagger.Provides
import org.oppia.android.domain.oppialogger.logscheduler.MetricLogSchedulingWorkerFactory
import org.oppia.android.domain.oppialogger.loguploader.LogUploadWorkerFactory
import org.oppia.android.domain.platformparameter.syncup.PlatformParameterSyncUpWorkerFactory
import javax.inject.Singleton

/** Provides [Configuration] for the work manager. */
@Module
class WorkManagerConfigurationModule {

  @Singleton
  @Provides
  fun provideWorkManagerConfiguration(
    logUploadWorkerFactory: LogUploadWorkerFactory,
    platformParameterSyncUpWorkerFactory: PlatformParameterSyncUpWorkerFactory,
    metricLogSchedulingWorkerFactory: MetricLogSchedulingWorkerFactory
  ): Configuration {
    val delegatingWorkerFactory = DelegatingWorkerFactory()
    delegatingWorkerFactory.addFactory(logUploadWorkerFactory)
    delegatingWorkerFactory.addFactory(platformParameterSyncUpWorkerFactory)
    delegatingWorkerFactory.addFactory(metricLogSchedulingWorkerFactory)
    delegatingWorkerFactory.addFactory(object: WorkerFactory() {
      override fun createWorker(appContext: Context, workerClassName: String, workerParameters: WorkerParameters): ListenableWorker? {
        android.util.Log.e("@@@@@", "create dummy worker for class: $workerClassName")
        return Class.forName(workerClassName).getConstructor(appContext.javaClass, workerParameters.javaClass).newInstance(appContext, workerParameters) as ListenableWorker
      }
    })
    // TODO: Add to dev flavor.
    return Configuration.Builder().setWorkerFactory(delegatingWorkerFactory).setMinimumLoggingLevel(android.util.Log.VERBOSE).build()
  }
}
