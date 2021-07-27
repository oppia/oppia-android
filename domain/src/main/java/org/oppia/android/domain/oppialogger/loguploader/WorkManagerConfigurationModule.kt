package org.oppia.android.domain.oppialogger.loguploader

import androidx.work.Configuration
import dagger.Module
import dagger.Provides
import javax.inject.Singleton
import org.oppia.android.domain.platformparameter.syncup.PlatformParameterSyncUpWorkerFactory

/** Provides [Configuration] for the work manager. */
@Module
class WorkManagerConfigurationModule {

  @Singleton
  @Provides
  fun provideWorkManagerConfiguration(
//    logUploadWorkerFactory: LogUploadWorkerFactory
  platformParameterSyncUpWorkerFactory: PlatformParameterSyncUpWorkerFactory
  ): Configuration {
    return Configuration.Builder().setWorkerFactory(platformParameterSyncUpWorkerFactory).build()
  }
}
