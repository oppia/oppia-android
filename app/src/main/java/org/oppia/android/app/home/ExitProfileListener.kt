package org.oppia.android.app.home

import org.oppia.android.app.model.ProfileType

interface ExitProfileListener {
  /** Listener for when a user wishes to exit their profile.
   *
   * A SOLE_LEARNER exits the pp completely while other [ProfileType]s are routed to the
   * [ProfileChooserActivity].
   */
  fun exitProfile(profileType: ProfileType)
}
