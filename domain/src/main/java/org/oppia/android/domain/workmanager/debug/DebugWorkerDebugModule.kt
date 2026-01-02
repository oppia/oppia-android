package org.oppia.android.domain.workmanager.debug

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import dagger.multibindings.IntoSet
import dagger.multibindings.StringKey
import org.oppia.android.domain.workmanager.OppiaWorker
import org.oppia.android.domain.workmanager.StartupWorkerScheduleReadinessListener

@Module
interface DebugWorkerDebugModule {
  @Binds
  @IntoSet
  fun bindDebugWorkerScheduler(
    scheduler: DebugWorkerScheduler
  ): StartupWorkerScheduleReadinessListener

  @Binds
  @IntoMap
  @StringKey(DebugWorker.WORKER_NAME)
  fun bindDebugWorkerFactoryProvider(factory: DebugWorker.Factory): OppiaWorker.Factory<*>
}
