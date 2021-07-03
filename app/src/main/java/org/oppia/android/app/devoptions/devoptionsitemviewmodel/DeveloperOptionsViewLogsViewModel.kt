package org.oppia.android.app.devoptions.devoptionsitemviewmodel

import org.oppia.android.app.devoptions.RouteToViewEventLogsListener

/** [DeveloperOptionsItemViewModel] to provide features to view logs such as analytic event logs. */
class DeveloperOptionsViewLogsViewModel(
  private val routeToViewEventLogsListener: RouteToViewEventLogsListener
) : DeveloperOptionsItemViewModel() {
  fun onEventLogsClicked() {
    routeToViewEventLogsListener.routeToViewEventLogs()
  }
}
