package org.oppia.android.app.administratorcontrols

/** Listener for when an activity should load [ProfileEditFragment]. */
interface LoadProfileEditListener {
  /** Used for inflating [ProfileEditFragment] in a multipane fragment in tablet mode. */
  fun loadProfileEdit(proifleId: Int, profileName: String)
}
