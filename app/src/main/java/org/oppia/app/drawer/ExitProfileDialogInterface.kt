package org.oppia.app.drawer

/** Interface to handle option selection in [ExitProfileDialogFragment]. */
interface ExitProfileDialogInterface {
  fun markLastCheckedItemCloseDrawer(lastCheckedItemId: Int, isAdminSelected:Boolean)
  fun unmarkSwitchProfileItemCloseDrawer()
}
