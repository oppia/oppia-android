package org.oppia.domain.oppialogger

import androidx.work.Worker
import dagger.Binds
import dagger.MapKey
import dagger.Module
import dagger.multibindings.IntoMap
import kotlin.reflect.KClass

@Retention(AnnotationRetention.RUNTIME)
@MapKey
annotation class WorkerKey(val value: KClass<out Worker>)

@Module
interface LogUploadWorkerModule {

  @Binds
  @WorkerKey(OppiaLogUploadWorker::class)
  @IntoMap
  fun bindLogUploadWorkerFactory(worker: OppiaLogUploadWorker.Factory): ChildWorkerFactory
}