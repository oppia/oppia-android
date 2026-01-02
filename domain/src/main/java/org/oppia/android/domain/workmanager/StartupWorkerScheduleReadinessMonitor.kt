package org.oppia.android.domain.workmanager

import org.oppia.android.domain.oppialogger.ApplicationStartupListener
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class StartupWorkerScheduleReadinessMonitor @Inject constructor(
  private val workManagerScheduler: WorkManagerScheduler,
  private val listsProv: Provider<Set<@JvmSuppressWildcards StartupWorkerScheduleReadinessListener>>
) : ApplicationStartupListener {
  override fun onCreateStarted() {
    // Do nothing. It's not yet safe to initialize the startup listeners and schedule workers since
    // the schedulers themselves may transitively depend on platform parameters. It's also fine to
    // wait to schedule these until full initialization since there should be no cross-syncing
    // happening.
  }

  override fun onCompletedInitialization() {
    // Now it's okay to initialize the listener instances and allow them to start scheduling work.
    for (startupWorkerScheduleReadinessListener in listsProv.get()) {
      startupWorkerScheduleReadinessListener.scheduleWork(workManagerScheduler)
    }
  }
}
