package org.oppia.android.app.administratorcontrols

/** Listener for when an activity should load [ProfileEditFragment]. */
interface LoadProfileEditListener {
  fun loadProfileEdit(internalProfileId: Int, isMultipane: Boolean)
}
