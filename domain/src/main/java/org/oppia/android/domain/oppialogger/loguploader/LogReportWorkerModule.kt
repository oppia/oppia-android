package org.oppia.android.domain.oppialogger.loguploader

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoSet
import org.oppia.android.domain.oppialogger.ApplicationStartupListener

/** Provides [LogUploadWorker] related dependencies. */
@Module
interface LogReportWorkerModule {

  @Binds
  @IntoSet
  fun bindLogReportWorkRequest(
    logReportWorkManagerInitializer: LogReportWorkManagerInitializer
  ): ApplicationStartupListener
}
