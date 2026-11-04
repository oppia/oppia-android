package org.oppia.android.app.devoptions

/** Listener for when the user wants to view event logs. */
interface RouteToViewEventLogsListener {
  /** Called when the user indicates that they want to view event logs. */
  fun routeToViewEventLogs()

  /** Called when the user wants to inspect this app's Logcat output. */
  fun routeToViewLogcatLogs()

  /** Called when the user wants to inspect performance metrics. */
  fun routeToViewPerformanceMetrics()
}
