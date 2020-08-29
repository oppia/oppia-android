package org.oppia.domain.oppialogger.loguploader

import androidx.work.Configuration
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

/** Provides [Configuration] for the work manager. */
@Module
class WorkManagerConfigurationModule {

  @Singleton
  @Provides
  fun provideWorkManagerConfiguration(
    logUploadWorkerFactory: LogUploadWorkerFactory
  ): Configuration {
    return Configuration.Builder().setWorkerFactory(logUploadWorkerFactory).build()
  }
}
