package org.oppia.android.domain.oppialogger.loguploader

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import dagger.multibindings.IntoSet
import dagger.multibindings.StringKey
import org.oppia.android.domain.workmanager.OppiaWorker
import org.oppia.android.domain.workmanager.StartupWorkerScheduleReadinessListener

/** Provides [LogUploadWorker] related dependencies. */
@Module
interface LogReportWorkerModule {
  @Binds
  @IntoSet
  fun bindLogReportWorkerScheduler(
    scheduler: LogReportWorkerScheduler
  ): StartupWorkerScheduleReadinessListener

  @Binds
  @IntoMap
  @StringKey(LogUploadWorker.WORKER_NAME)
  fun bindLogUploadWorkerFactoryProvider(factory: LogUploadWorker.Factory): OppiaWorker.Factory<*>
}
