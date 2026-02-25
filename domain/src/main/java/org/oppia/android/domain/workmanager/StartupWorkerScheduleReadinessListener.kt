package org.oppia.android.domain.workmanager

/**
 * Application-bound listener which will receive a prompt to start scheduling workers for periodic
 * execution.
 *
 * Note that implementation bindings should be multi-bound into a set of this listener type in order
 * to automatically be included in application startup procedures.
 */
interface StartupWorkerScheduleReadinessListener {
  /**
   * Called early in application startup to allow periodic work to be scheduled.
   *
   * Note that no guarantees can be called about when in the application initialization this is
   * called, but it is guaranteed to be called shortly after initialization. Between that and the
   * nature of `WorkManager` and Android OS job behaviors, no work that actually synchronizes with
   * application startup state should ever be scheduled through this method (or via `WorkManager` at
   * all).
   *
   * This method is guaranteed to be called exactly once per application instance.
   */
  fun scheduleWork(workManagerScheduler: WorkManagerScheduler)
}
