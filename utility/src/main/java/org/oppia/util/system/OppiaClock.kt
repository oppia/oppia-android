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
    testTimeMs = currentTimeMs
  }

  /** returns current date and time */
  fun getCurrentCalendar(): Calendar {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = getCurrentTimeMs()
    return calendar
  }
}
