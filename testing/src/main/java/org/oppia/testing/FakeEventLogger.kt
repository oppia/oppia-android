package org.oppia.testing

import android.content.Context
import android.os.Bundle
import org.oppia.testing.TestLogReportingModule.Companion.eventNameList
import org.oppia.util.firebase.EventLogger

/**
 * A test specific fake event logger that adds the title of logged events to a list of strings.
 */
class FakeEventLogger : EventLogger {

  /** This is used to add the title of the logged event to a list of titles. */
  override fun logEvent(context: Context, bundle: Bundle, title: String) {
    eventNameList.add(title)
  }

  /** This is used to get the most recent event title present in the list of strings. */
  fun getMostRecentTitle(): String {
    val size = eventNameList.size
    return if (size > 0) {
      eventNameList[size - 1]
    } else {
      throw NullPointerException("No value present")
    }
  }

  /** This is used to clear all the event titles present in the list. */
  fun clearAllTitles() {
    eventNameList.clear()
  }
}
