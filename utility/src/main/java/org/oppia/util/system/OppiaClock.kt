package org.oppia.util.system

import android.os.SystemClock
import androidx.annotation.VisibleForTesting
import java.util.*
import javax.inject.Inject

/** Needs documentation. */
class OppiaClock @Inject constructor() {
  private var testTimeMs: Long? = null

  /** Needs documentation. */
  fun getElapsedRealtimeMs(): Long {
    return SystemClock.elapsedRealtime()
  }

  /** Needs documentation. */
  fun getCurrentTimeMs(): Long {
    return testTimeMs ?: System.currentTimeMillis()
  }

  /** Needs documentation. */
  @VisibleForTesting(otherwise = VisibleForTesting.NONE)
  fun setCurrentTimeMs(currentTimeMs: Long) {
    testTimeMs = currentTimeMs
  }

  /** Needs documentation. */
  fun getCurrentCalendar(): Calendar {
    val calendar = Calendar.getInstance()
    calendar.setTimeInMillis(getCurrentTimeMs())
    return calendar
  }
}
