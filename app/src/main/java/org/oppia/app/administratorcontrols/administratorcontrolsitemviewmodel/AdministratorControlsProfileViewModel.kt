package org.oppia.app.administratorcontrols.administratorcontrolsitemviewmodel

import org.oppia.app.administratorcontrols.RouteToProfileListListener

/** [ViewModel] for the recycler view in [AdministratorControlsFragment]. */
class AdministratorControlsProfileViewModel(
  private val routeToProfileListListener: RouteToProfileListListener
) : AdministratorControlsItemViewModel() {

  fun onEditProfilesClicked() {
    routeToProfileListListener.routeToProfileList()
  }
}
