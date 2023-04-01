package org.oppia.android.domain.oppialogger.analytics

import androidx.work.WorkManager

/**
 * Analytics-specific application startup listener that receives an instance of [WorkManager] to
 * perform analytics-specific initialization.
 */
interface AnalyticsStartupListener {
  /**
   * Called on application creation with the singleton, application-wide [WorkManager] that should
   * be used for scheduling background analytics tasks.
   */
  fun onCreate(workManager: WorkManager)
}
