package org.oppia.android.app.administratorcontrols

/** Listener for when an activity should load [ProfileEditFragment]. */
interface LoadProfileEditListener {
  /** This method is called when the fragment of [ProfileEditFragment] is inflated. */
  fun loadProfileEdit(proifleId: Int, profileName: String)
}
