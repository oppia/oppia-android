package org.oppia.android.app.administratorcontrols.administratorcontrolsitemviewmodel

import androidx.appcompat.app.AppCompatActivity
import org.oppia.android.app.administratorcontrols.LoadAppVersionListener
import org.oppia.android.app.administratorcontrols.RouteToAppVersionListener

/** [ViewModel] for the recycler view in [AdministratorControlsFragment]. */
class AdministratorControlsAppInformationViewModel(
  activity: AppCompatActivity
) : AdministratorControlsItemViewModel() {

  private val routeToAppVersionListener = activity as RouteToAppVersionListener
  private val loadAppVersionListener = activity as LoadAppVersionListener

  /** Called when a user clicks on AppVersion in [AdministratorControlsActivity]. */
  fun onAppVersionClicked() {
    if (isMultipane.get()!!) {
      loadAppVersionListener.loadAppVersion()
    } else {
      routeToAppVersionListener.routeToAppVersion()
    }
  }
}
