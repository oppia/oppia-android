package org.oppia.app.administratorcontrols.administratorcontrolsitemviewmodel

import androidx.appcompat.app.AppCompatActivity
import org.oppia.app.administratorcontrols.LoadAppVersionListener
import org.oppia.app.administratorcontrols.RouteToAppVersionListener

/** [ViewModel] for the recycler view in [AdministratorControlsFragment]. */
class AdministratorControlsAppInformationViewModel(
  activity: AppCompatActivity
) : AdministratorControlsItemViewModel() {

  private val routeToAppVersionListener = activity as RouteToAppVersionListener
  private val loadAppVersionListener = activity as LoadAppVersionListener

  fun onAppVersionClicked() {
    if (isMultipane.get()!!) {
      loadAppVersionListener.loadAppVersion()
    } else {
      routeToAppVersionListener.routeToAppVersion()
    }
  }
}
