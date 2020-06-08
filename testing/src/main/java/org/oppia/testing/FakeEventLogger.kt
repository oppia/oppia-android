package org.oppia.testing

import android.content.Context
import android.os.Bundle
import org.oppia.testing.TestLogReportingModule.Companion.eventList
import org.oppia.util.firebase.EventLogger

/**
 * A test specific fake event logger that adds the logged events to a list of events.
 */
class FakeEventLogger : EventLogger {

  /** This is used to add the logged event to a list of events. */
  override fun logEvent(context: Context, bundle: Bundle, title: String) {
    eventList.add(Event(title, bundle))
  }

  /** This is used to get the most recent event present in the list of events. */
  fun getMostRecentEvent(): Event {
    val size = eventList.size
    return if (size > 0) {
      eventList[size - 1]
    } else {
      throw NullPointerException("No value present")
    }
  }

  /** This is used to clear all the events present in the list. */
  fun clearAllEvents() {
    eventList.clear()
  }
}

/**
 * @param [title]: title of the logged event.
 * @param [bundle]: bundle that the contains information that needs to be logged.
 */
class Event(var title: String, var bundle: Bundle)
