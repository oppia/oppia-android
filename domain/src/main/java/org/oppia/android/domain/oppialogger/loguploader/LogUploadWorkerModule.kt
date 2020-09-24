package org.oppia.domain.oppialogger.loguploader

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoSet
import org.oppia.domain.oppialogger.ApplicationStartupListener

/** Provides [LogUploadWorker] related dependencies. */
@Module
interface LogUploadWorkerModule {

  @Binds
  @IntoSet
  fun bindLogUploadWorkRequest(
    logUploadWorkManagerInitializer: LogUploadWorkManagerInitializer
  ): ApplicationStartupListener
}
