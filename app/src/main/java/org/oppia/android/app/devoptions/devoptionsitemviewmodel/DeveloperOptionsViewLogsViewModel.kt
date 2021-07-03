package org.oppia.android.app.devoptions.devoptionsitemviewmodel

import org.oppia.android.app.devoptions.RouteToViewEventLogsListener

/** [DeveloperOptionsItemViewModel] to provide features to view logs such as analytic event logs. */
class DeveloperOptionsViewLogsViewModel(
  private val routeToViewEventLogsListener: RouteToViewEventLogsListener
) : DeveloperOptionsItemViewModel() {
  /** Called when the 'event logs' button is clicked by the user. */
  fun onEventLogsClicked() {
    routeToViewEventLogsListener.routeToViewEventLogs()
  }
}
