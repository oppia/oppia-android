package org.oppia.android.domain.platformparameter.syncup

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoSet
import org.oppia.android.domain.oppialogger.ApplicationStartupListener

/** Provides [PlatformParameterSyncUpWorker] related dependencies. */
@Module
interface PlatformParameterSyncUpWorkerModule {

  @Binds
  @IntoSet
  fun bindLogUploadWorkRequest(
    platformParameterSyncUpWorkManagerInitializer: PlatformParameterSyncUpWorkManagerInitializer
  ): ApplicationStartupListener
}
