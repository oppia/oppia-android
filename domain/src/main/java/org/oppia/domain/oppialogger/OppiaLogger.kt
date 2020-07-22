package org.oppia.domain.oppialogger

import android.content.Context
import org.oppia.app.model.EventLog.EventAction
import org.oppia.app.model.EventLog
import org.oppia.domain.oppialogger.analytics.AnalyticsController
import org.oppia.util.logging.EventLogger
import javax.inject.Inject

/** Logger that handles exceptions, crashes, events, and console logging. */
class OppiaLogger  @Inject constructor(
  private val analyticsController: AnalyticsController,
) {
  /**
   * Logs transition events.
   * These events are given HIGH priority.
   */
  fun logTransitionEvent(
    timestamp: Long,
    eventAction: EventAction,
    eventContext: EventLog.Context?
  ) {
    analyticsController.logTransitionEvent(timestamp, eventAction, eventContext)
  }

  /**
   * Logs click events.
   * These events are given LOW priority.
   */
  fun logClickEvent(
    timestamp: Long,
    eventAction: EventAction,
    eventContext: EventLog.Context?
  ) {
    analyticsController.logClickEvent(timestamp, eventAction, eventContext)
  }
}