package org.oppia.android.app.drawer

import org.oppia.android.app.model.LastCheckedMenuItem

/** Interface to handle option selection in [ExitProfileDialogFragment]. */
interface ExitProfileDialogInterface {
  fun checkLastCheckedItemAndCloseDrawer(argument: Argument)

  fun unCheckSwitchProfileItemAndCloseDrawer()
}
