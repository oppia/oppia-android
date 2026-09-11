package org.oppia.android.domain.platformparameter.syncup

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import dagger.multibindings.IntoSet
import dagger.multibindings.StringKey
import org.oppia.android.domain.workmanager.OppiaWorker
import org.oppia.android.domain.workmanager.StartupWorkerScheduleReadinessListener

/** Provides [PlatformParameterSyncUpWorker] related dependencies. */
@Module
interface PlatformParameterSyncUpWorkerModule {
  @Binds
  @IntoSet
  fun bindPlatformParameterSyncUpWorkerScheduler(
    scheduler: PlatformParameterSyncUpWorkerScheduler
  ): StartupWorkerScheduleReadinessListener

  @Binds
  @IntoMap
  @StringKey(PlatformParameterSyncUpWorker.WORKER_NAME)
  fun bindLogUploadWorkerFactoryProvider(
    factory: PlatformParameterSyncUpWorker.Factory
  ): OppiaWorker.Factory<*>
}
