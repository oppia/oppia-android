package org.oppia.android.util.system

import java.util.Calendar
import java.util.Date

/** Utility to get the current date/time. Tests should use the fake version of this class. */
interface OppiaClock {
  /**
   * Returns the current wall clock time in milliseconds since the Unix epoch. The returned time is
   * in UTC.
   *
   * Note that this should be used instead of [System.currentTimeMillis] since this one can be
   * faked within both Robolectric & Espresso tests (whereas the system coordinated time relies on
   * differing behaviors between Robolectric & Espresso).
   */
  fun getCurrentTimeMs(): Long

  /**
   * Returns the current date and time as a [Calendar]. Unlike [getCurrentTimeMs], the returned
   * [Calendar] takes into account the user's local time zone.
   */
  fun getCurrentCalendar(): Calendar = Calendar.getInstance().apply {
    timeInMillis = getCurrentTimeMs()
  }

  /**
   * Returns the [Date] corresponding to the current instant in time, according to
   * [getCurrentTimeMs].
   */
  fun getCurrentDate(): Date = Date(getCurrentTimeMs())
}
