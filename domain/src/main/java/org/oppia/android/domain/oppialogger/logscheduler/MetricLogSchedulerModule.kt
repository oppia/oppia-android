package org.oppia.android.domain.oppialogger.logscheduler

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import dagger.multibindings.IntoSet
import dagger.multibindings.StringKey
import org.oppia.android.domain.workmanager.OppiaWorker
import org.oppia.android.domain.workmanager.StartupWorkerScheduleReadinessListener

/** Provides metric log scheduler related dependencies. */
@Module
interface MetricLogSchedulerModule {
  @Binds
  @IntoSet
  fun bindMetricLogSchedulingWorkerScheduler(
    scheduler: MetricLogSchedulingWorkerScheduler
  ): StartupWorkerScheduleReadinessListener

  @Binds
  @IntoMap
  @StringKey(MetricLogSchedulingWorker.WORKER_NAME)
  fun bindLogUploadWorkerFactoryProvider(
    factory: MetricLogSchedulingWorker.Factory
  ): OppiaWorker.Factory<*>
}
