package org.oppia.android.app.administratorcontrols

/** Listener for when an activity should load [AppVersionFragment]. */
interface LoadAppVersionListener {
  /** Loads [AppVersionFragment] in multipane tablet mode in [AdministratorControlsActivity]. */
  fun loadAppVersion()
}
