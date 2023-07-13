package org.oppia.android.domain.workmanager

import androidx.work.Configuration
import androidx.work.DelegatingWorkerFactory
import dagger.Module
import dagger.Provides
import org.oppia.android.domain.oppialogger.firestoreuploader.FirestoreUploadWorkerFactory
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
    metricLogSchedulingWorkerFactory: MetricLogSchedulingWorkerFactory,
    firestoreWorkerFactory: FirestoreUploadWorkerFactory
  ): Configuration {
    val delegatingWorkerFactory = DelegatingWorkerFactory()
    delegatingWorkerFactory.addFactory(logUploadWorkerFactory)
    delegatingWorkerFactory.addFactory(platformParameterSyncUpWorkerFactory)
    delegatingWorkerFactory.addFactory(metricLogSchedulingWorkerFactory)
    delegatingWorkerFactory.addFactory(firestoreWorkerFactory)
    return Configuration.Builder().setWorkerFactory(delegatingWorkerFactory).build()
  }
}
