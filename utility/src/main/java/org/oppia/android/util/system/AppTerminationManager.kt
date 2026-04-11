package org.oppia.android.util.system

/** Manager for app termination operations. */
interface AppTerminationManager {
  /** Force closes the app by terminating the current process. */
  fun forceCloseApp()
}
