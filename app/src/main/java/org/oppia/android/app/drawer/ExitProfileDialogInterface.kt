package org.oppia.android.app.drawer

/** Interface to handle option selection in [ExitProfileDialogFragment]. */
interface ExitProfileDialogInterface {
  fun highlightLastCheckedMenuItem()
  fun highlightAdministratorControlsItem()
  fun unhighlightSwitchProfileMenuItem()
}
