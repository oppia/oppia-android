package org.oppia.android.domain.oppialogger.analytics

/**
 * Listener for when the app goes to the foreground or the background.
 *
 * Note that implementations may not receive initial signals of app switches due to there being a
 * slight delay in app startup between monitoring for foreground/background switches and actually
 * reporting them. No switches will be missed but they may not arrive until other entry points in
 * the app have already made execution progress (such as the base activity classes).
 *
 * Care should be taken when using implementations of this listener to synchronize initialization
 * state.
 */
interface ApplicationLifecycleListener {
  /** Fired when the app comes to the foreground. */
  fun onAppInForeground()

  /** Fired when the app goes to the background. */
  fun onAppInBackground()
}
