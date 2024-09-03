package org.oppia.android.app.profile

import org.oppia.android.app.model.Profile

/** Listener for when a profile is clicked. */
interface ProfileClickListener {
  /** Triggered when the profile is clicked. */
  fun onProfileClicked(profile: Profile)
}
