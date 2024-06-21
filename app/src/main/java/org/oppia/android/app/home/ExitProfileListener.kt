package org.oppia.android.app.home

import org.oppia.android.app.model.ProfileType
/** Listener for when a user wishes to exit their profile. */
interface ExitProfileListener {
  /** Called when back press is clicked on the HomeScreen.
   *
   * A SOLE_LEARNER exits the pp completely while other [ProfileType]s are routed to the
   * [ProfileChooserActivity].
   */
  fun exitProfile(profileType: ProfileType)
}
