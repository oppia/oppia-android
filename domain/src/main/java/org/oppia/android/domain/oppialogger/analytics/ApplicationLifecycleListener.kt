package org.oppia.android.domain.oppialogger.analytics

/** Listener for when the app goes to the foreground or the background. */
interface ApplicationLifecycleListener {
  /** Fired when the app comes to the foreground. */
  fun onAppInForeground()

  /** Fired when the app goes to the background. */
  fun onAppInBackground()
}
