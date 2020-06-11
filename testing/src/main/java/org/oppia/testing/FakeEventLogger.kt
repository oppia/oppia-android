package org.oppia.testing

import android.content.Context
import android.os.Bundle
import org.oppia.util.logging.EventLogger
import java.util.ArrayList
import javax.inject.Inject
import javax.inject.Singleton

/**  A test specific fake for the event logger. */
@Singleton
class FakeEventLogger @Inject constructor() : EventLogger {
  val eventList = ArrayList<Event>()

  override fun logEvent(context: Context, bundle: Bundle, title: String) {
    eventList.add(Event(title, bundle))
  }

  /** Returns the most recently logged event. */
  fun getMostRecentEvent(): Event = eventList.last()

  /** Clears all the events that are currently logged.. */
  fun clearAllEvents() = eventList.clear()
}

/**
 * Returns an event object containing title and bundle which enables event recording.
 *
 * @param [title]: title of the logged event.
 * @param [bundle]: bundle that the contains information that needs to be logged.
 */
class Event(var title: String, var bundle: Bundle)
