package org.oppia.android.app.profile

import org.oppia.android.app.model.ProfileId

/** Interface to route dialogs in user Forgot Password flow. */
interface ProfileRouteDialogInterface {
  /** Shows [ResetPinDialogFragment]. */
  fun routeToResetPinDialog(profileId: ProfileId, profileName: String)

  /** Shows Success Dialog. */
  fun routeToSuccessDialog()
}
