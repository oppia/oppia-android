package org.oppia.android.app.administratorcontrols.administratorcontrolsitemviewmodel

import org.oppia.android.app.administratorcontrols.ShowLogoutDialogListener

/** [ViewModel] for the recycler view in [AdministratorControlsFragment]. */
class AdministratorControlsAccountActionsViewModel(
  private val showLogoutDialogListener: ShowLogoutDialogListener
) : AdministratorControlsItemViewModel() {
  /** Called when user clicks logout on [AdministratorControlsActivity]. */
  fun onLogOutClicked() {
    showLogoutDialogListener.showLogoutDialog()
  }
}
