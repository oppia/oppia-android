package org.oppia.util.system

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/** Utility to format date to text or parse text to date. */
@Singleton
class OppiaDateTimeFormatter @Inject constructor() {

  val dd_MMM_yyyy = "dd MMM yyyy"
  val hh_mm_a = "hh:mm a"
  val dd_MMMM_yyyy = "dd MMMM yyyy"
  val dd_MMMM_yyyy_zzzz = "dd MMMM yyyy zzzz"

  fun formatDateFromDateString(
    inputDateFormat: String,
    timestamp: Long,
    locale: Locale = Locale.getDefault()
  ): String {
    return try {
      val sdf = SimpleDateFormat(inputDateFormat, locale)
      val dateTime = Date(timestamp)
      sdf.format(dateTime)
    } catch (e: Exception) {
      e.toString()
    }
  }

  fun currentDate(): Date {
    val calendar = Calendar.getInstance()
    return calendar.time
  }

  fun checkAndConvertTimestampToMilliseconds(lastVisitedTimeStamp: Long): Long {
    var timeStamp = lastVisitedTimeStamp
    if (timeStamp < 1000000000000L)
    // If timestamp is given in seconds, convert that to milliseconds.
      timeStamp *= 1000
    return timeStamp
  }
}
