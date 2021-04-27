package org.oppia.android.app.administratorcontrols.administratorcontrolsitemviewmodel

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import org.oppia.android.app.administratorcontrols.AdministratorControlsFragment
import org.oppia.android.app.administratorcontrols.LogoutDialogFragment

private const val TAG_LOGOUT_DIALOG = "TAG_LOGOUT_DIALOG"

/** [ViewModel] for the recycler view in [AdministratorControlsFragment]. */
class AdministratorControlsAccountActionsViewModel(
  private val fragment: Fragment
) : AdministratorControlsItemViewModel() {

  fun onLogOutClicked() {
    LogoutDialogFragment.newInstance().showNow(
      fragment.requireActivity().supportFragmentManager,
      TAG_LOGOUT_DIALOG
    )
  }
}
