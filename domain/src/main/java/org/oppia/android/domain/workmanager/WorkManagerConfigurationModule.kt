package org.oppia.android.domain.workmanager

import android.content.Context
import androidx.work.Configuration
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet
import dagger.multibindings.Multibinds
import org.oppia.android.domain.oppialogger.ApplicationStartupListener
import javax.inject.Singleton

/** Provides [Configuration] for the work manager. */
@Module
interface WorkManagerConfigurationModule {
  @Multibinds
  fun bindOppiaWorkerFactories(): Map<String, OppiaWorker.Factory<*>>

  @Multibinds
  fun bindStartupWorkerScheduleReadinessListeners(): Set<StartupWorkerScheduleReadinessListener>

  @Binds
  @IntoSet
  fun bindStartupWorkerScheduleReadinessMonitorAsStartupListener(
    monitor: StartupWorkerScheduleReadinessMonitor
  ): ApplicationStartupListener

  companion object ConfigurationCreationModule {
    @Provides
    @Singleton
    fun provideWorkManagerConfiguration(
      bootstrapWorkerFactory: BootstrapOppiaWorker.Factory
    ): Configuration {
      // The app uses only one worker factory since all work is bootstrapped.
      val workerFactory = object : WorkerFactory() {
        override fun createWorker(
          appContext: Context,
          workerClassName: String,
          workerParameters: WorkerParameters
        ): ListenableWorker? {
          // The only worker the app uses is the bootstrap one, and so all work requests should be
          // directed to it even if it's not the worker being requested. This inconsistency can
          // happen for old queued jobs before the worker was created and the worker is designed to
          // check to make sure the correct class is being requested before proceeding. However,
          // there's one set of counter cases: WorkManager's own internal workers route through this
          // factory. There's no reliable way to check for those other than validating the class is
          // a real class loadable in the app and that it's a ListenableFuture. This combined with a
          // regex content check should prevent any Oppia workers from being ListenableWorker
          // directly.
          val workerExistsInApk = try {
            ListenableWorker::class.java.isAssignableFrom(Class.forName(workerClassName))
          } catch (e: ClassNotFoundException) { false }
          if (workerExistsInApk && workerClassName != BootstrapOppiaWorker::class.java.name) {
            // Existing workers that aren't the bootstrap worker must be handled through reflection
            // since they're almost certainly WorkManager internal workers. Note that the
            // ListenableWorker check is necessary since there may be worker classes that previously
            // were scheduled as ListenableWorkers but are now run through the bootstrap worker (and
            // haven't been renamed).
            return null
          }

          return bootstrapWorkerFactory.createBootstrapWorker(workerClassName, workerParameters)
        }
      }
      return Configuration.Builder().setWorkerFactory(workerFactory).build()
    }
  }
}
