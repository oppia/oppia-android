package org.oppia.android.domain.oppialogger.loguploader

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoSet
import org.oppia.android.domain.oppialogger.analytics.AnalyticsStartupListener

/** Provides [LogUploadWorker] related dependencies. */
@Module
interface LogReportWorkerModule {

  @Binds
  @IntoSet
  fun bindLogReportWorkRequest(
    logReportWorkManagerInitializer: LogReportWorkManagerInitializer
  ): AnalyticsStartupListener
}
