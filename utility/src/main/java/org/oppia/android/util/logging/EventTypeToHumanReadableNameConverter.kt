package org.oppia.android.util.logging

import org.oppia.android.app.model.EventLog

/**
 * Converter for event types into loggable, human-readable names.
 *
 * Different builds of the app may leverage different implementations of this converter in order to
 * customize the event names for querying purposes.
 */
interface EventTypeToHumanReadableNameConverter {
  /**
   * Returns a human-readable event name for the specified [eventType].
   *
   * Note that the same name should always be returned for a given event type within the same
   * version of the app (though the names can change across versions). The returned name should not
   * contain any personal information nor any information tied to users; they should be constant
   * regardless of the current logged in/created profile state of the app.
   */
  fun convertToHumanReadableName(eventType: EventLog.Context.ActivityContextCase): String
}
