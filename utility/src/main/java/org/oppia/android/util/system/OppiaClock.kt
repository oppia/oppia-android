package org.oppia.android.util.system

import android.annotation.SuppressLint
import android.os.SystemClock
import androidx.annotation.VisibleForTesting
import java.text.ParseException
import java.text.SimpleDateFormat
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

  private fun getCurrentTimeMs(): Long {
    return testTimeMs ?: Calendar.getInstance(TimeZone.getTimeZone("UTC")).timeInMillis
  }

  @SuppressLint("SimpleDateFormat")
  @VisibleForTesting(otherwise = VisibleForTesting.NONE)
  fun setCurrentTimeMs(currentTimeMs: Long) {
    var currentTimeMsNew: Long = 0
    val format = SimpleDateFormat("yyyy-MM-dd hh:mm a")
    val dtStart = format.format(currentTimeMs)
    val date: Date?
    try {
      date = format.parse(dtStart)
      currentTimeMsNew = getLocalToUTCDate(date)
    } catch (e: ParseException) {
      e.printStackTrace()
    }
    testTimeMs = currentTimeMsNew
  }

  /** returns current date and time */
  fun getCurrentCalendar(): Calendar {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = getCurrentTimeMs()
    return calendar
  }

  @SuppressLint("SimpleDateFormat")
  fun getLocalToUTCDate(date: Date): Long {
    val calendar = Calendar.getInstance()
    calendar.time = date
    calendar.timeZone = TimeZone.getTimeZone("UTC")
    val time = calendar.time
    val outputFmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    outputFmt.timeZone = TimeZone.getTimeZone("UTC")
    val newDate = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").parse(outputFmt.format(time))
    return newDate.time
  }
}
