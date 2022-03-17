package org.oppia.android.app.administratorcontrols.administratorcontrolsitemviewmodel

import androidx.appcompat.app.AppCompatActivity
import org.oppia.android.app.administratorcontrols.LoadLearnerAnalyticsListener
import org.oppia.android.app.administratorcontrols.RouteToLearnerAnalyticsListener

/**
 * [AdministratorControlsItemViewModel] to represent an option for admins to view the learner
 * analytics screen.
 */
class AdministratorControlsProfileAndDeviceIdViewModel(
  activity: AppCompatActivity
) : AdministratorControlsItemViewModel() {

  private val routeToLearnerAnalyticsListener = activity as RouteToLearnerAnalyticsListener
  private val loadLearnerAnalyticsListener = activity as LoadLearnerAnalyticsListener

  /** Shows the admin the learner analytics screen. */
  fun showLearnerAnalyticsScreen() {
    if (isMultipane.get()!!) {
      loadLearnerAnalyticsListener.loadLearnerAnalyticsData()
    } else {
      routeToLearnerAnalyticsListener.routeToLearnerAnalytics()
    }
  }
}
