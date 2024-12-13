package org.oppia.android.app.home

import org.oppia.android.app.model.ProfileType

/** Listener for when a user wishes to exit their profile. */
interface ExitProfileListener {
  /**
   * Called when back press is clicked on the HomeScreen.
   *
   * Routing behaviour may change based on [ProfileType]
   */
  fun exitProfile(profileType: ProfileType)
}
