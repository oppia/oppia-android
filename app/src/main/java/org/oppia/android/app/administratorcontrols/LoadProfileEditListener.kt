package org.oppia.android.app.administratorcontrols

import org.oppia.android.app.model.LegacyProfileId

/** Listener for when an activity should load [ProfileEditFragment]. */
interface LoadProfileEditListener {
  /** Inflates [ProfileEditFragment] as part of a tablet mode a multipane fragment. */
  fun loadProfileEdit(profileId: LegacyProfileId, profileName: String)
}
