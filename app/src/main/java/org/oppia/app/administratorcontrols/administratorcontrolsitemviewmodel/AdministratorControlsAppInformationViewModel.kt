package org.oppia.app.administratorcontrols.administratorcontrolsitemviewmodel

import androidx.appcompat.app.AppCompatActivity
import org.oppia.app.administratorcontrols.RouteToAppVersionListener

/** [ViewModel] for the recycler view in [AdministratorControlsFragment]. */
class AdministratorControlsAppInformationViewModel(
  activity: AppCompatActivity
) : AdministratorControlsItemViewModel() {

  private val routeToAppVersionListener = activity as RouteToAppVersionListener

  fun onAppVersionClicked() {
    routeToAppVersionListener.routeToAppVersion()
  }
}
