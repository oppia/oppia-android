package org.oppia.android.app.devoptions.devoptionsitemviewmodel

import org.oppia.android.app.devoptions.RouteToViewEventLogsListener

/** [ViewModel] for the recycler view in [DeveloperOptionsFragment]. */
class DeveloperOptionsViewLogsViewModel(
  private val routeToViewEventLogsListener: RouteToViewEventLogsListener
) : DeveloperOptionsItemViewModel() {
  fun onEventLogsClicked() {
    routeToViewEventLogsListener.routeToViewEventLogs()
  }
}
