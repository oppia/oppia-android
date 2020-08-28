package org.oppia.app.administratorcontrols.administratorcontrolsitemviewmodel

import org.oppia.app.administratorcontrols.LoadProfileListListener
import org.oppia.app.administratorcontrols.RouteToProfileListListener

/** [ViewModel] for the recycler view in [AdministratorControlsFragment]. */
class AdministratorControlsProfileViewModel(
  private val routeToProfileListListener: RouteToProfileListListener,
  private val loadProfileListListener: LoadProfileListListener
) : AdministratorControlsItemViewModel() {

  fun onEditProfilesClicked() {
    if (isMultipane.get()!!) {
      loadProfileListListener.loadProfileList()
    } else {
      routeToProfileListListener.routeToProfileList()
    }
  }
}
