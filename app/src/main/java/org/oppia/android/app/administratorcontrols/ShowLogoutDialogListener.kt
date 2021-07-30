package org.oppia.android.app.administratorcontrols

/** Listener for when the logout dialog should be shown. */
interface ShowLogoutDialogListener {

  /** Shows the logout dialog. This cannot be called at times when there's an ongoing fragment transaction. */
  fun showLogoutDialog()
}
