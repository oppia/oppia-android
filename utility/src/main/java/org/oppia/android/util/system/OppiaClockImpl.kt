package org.oppia.android.util.system

import java.util.Calendar
import javax.inject.Inject

/** Implementation of [OppiaClock] that uses real time dependencies. */
class OppiaClockImpl @Inject constructor() : OppiaClock {
  override fun getCurrentTimeMs(): Long = System.currentTimeMillis()

  override fun getCurrentCalendar(): Calendar {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = getCurrentTimeMs()
    return calendar
  }
}
