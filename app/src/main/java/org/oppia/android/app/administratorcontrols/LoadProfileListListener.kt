package org.oppia.android.app.administratorcontrols

/** Listener for when an activity should load [ProfileListFragment]. */
interface LoadProfileListListener {
  /** Loads [ProfileListFragment] in multipane tablet mode in [AdministratorControlsActivity]. */
  fun loadProfileList()
}
