package org.oppia.android.app.profile

import org.oppia.android.app.model.LegacyProfileId

/** Interface to route dialogs in user Forgot Password flow. */
interface ProfileRouteDialogInterface {
  /** Shows [ResetPinDialogFragment]. */
  fun routeToResetPinDialog(profileId: LegacyProfileId, profileName: String)

  /** Shows Success Dialog. */
  fun routeToSuccessDialog()
}
