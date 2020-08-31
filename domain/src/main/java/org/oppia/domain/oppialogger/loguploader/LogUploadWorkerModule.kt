package org.oppia.domain.oppialogger.loguploader

import androidx.work.WorkerFactory
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoSet
import org.oppia.domain.oppialogger.ApplicationStartupListener

/** Provides [LogUploadWorker] related dependencies. */
@Module
interface LogUploadWorkerModule {

  @Binds
  fun bindLogUploadWorkerFactory(
    worker: LogUploadWorker.FactoryLogUpload
  ): LogUploadChildWorkerFactory

  @Binds
  fun bindWorkerFactory(logUploadWorkerFactory: LogUploadWorkerFactory): WorkerFactory

  @Binds
  @IntoSet
  fun bindLogUploadWorkRequest(
    logUploadWorkManagerInitializer: LogUploadWorkManagerInitializer
  ): ApplicationStartupListener
}
