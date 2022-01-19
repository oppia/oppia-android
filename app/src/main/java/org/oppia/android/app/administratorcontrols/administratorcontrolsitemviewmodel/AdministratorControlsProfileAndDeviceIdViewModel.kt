package org.oppia.android.app.administratorcontrols.administratorcontrolsitemviewmodel

import androidx.appcompat.app.AppCompatActivity
import org.oppia.android.app.administratorcontrols.LoadLearnerAnalyticsListener
import org.oppia.android.app.administratorcontrols.RouteToLearnerAnalyticsListener

class AdministratorControlsProfileAndDeviceIdViewModel(
  activity: AppCompatActivity
): AdministratorControlsItemViewModel() {

  private val routeToLearnerAnalyticsListener = activity as RouteToLearnerAnalyticsListener
  private val loadLearnerAnalyticsListener = activity as LoadLearnerAnalyticsListener

  fun onProfileAndDeviceIdsClicked(){
    if (isMultipane.get()!!) {
      loadLearnerAnalyticsListener.loadLearnerAnalyticsData()
    } else {
      routeToLearnerAnalyticsListener.routeToLearnerAnalytics()
    }
  }
}
