package org.oppia.util.system

import android.os.SystemClock
import androidx.annotation.VisibleForTesting
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton



/** Utility to get the current date/time. */
@Singleton
class OppiaClock @Inject constructor() {
  private var testTimeMs: Long? = null

  fun getElapsedRealtimeMs(): Long {
    return SystemClock.elapsedRealtime()
  }

  fun getCurrentTimeMs(): Long {
    return testTimeMs ?: System.currentTimeMillis()
  }

  @VisibleForTesting(otherwise = VisibleForTesting.NONE)
  fun setCurrentTimeMs(currentTimeMs: Long) {

//    // Creating date format
//    val simple = SimpleDateFormat("dd MMM yyyy HH:mm:ss:SSS Z")
//
//    // Creating date from milliseconds
//    // using Date() constructor
//    val result = Date(currentTimeMs)
//
//    // Formatting Date according to the
//    // given format
//    System.out.println("Date "+simple.format(result))
//    val date = simple.parse(simple.format(result))
//    System.out.println(date.time)
//    testTimeMs = date.time
    testTimeMs = currentTimeMs
  }

  /** returns current date and time */
  fun getCurrentCalendar(): Calendar {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = getCurrentTimeMs()
    return calendar
  }
}
