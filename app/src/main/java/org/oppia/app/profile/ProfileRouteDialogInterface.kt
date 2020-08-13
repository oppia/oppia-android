package org.oppia.app.profile

/** Interface to route dialogs in user Forgot Password flow. */
interface ProfileRouteDialogInterface {
  /** Shows [ResetPinDialogFragment]. */
  fun routeToResetPinDialog()

  /** Shows Success Dialog. */
  fun routeToSuccessDialog()
}
