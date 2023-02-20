package org.oppia.android.app.administratorcontrols

import org.oppia.android.app.model.ProfileId

/** Listener for when an activity should load [ProfileEditFragment]. */
interface LoadProfileEditListener {
  /** Inflates [ProfileEditFragment] as part of a tablet mode a multipane fragment. */
  fun loadProfileEdit(profileId: ProfileId, profileName: String)
}
