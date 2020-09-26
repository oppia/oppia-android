package org.oppia.android.util.datetime

import android.content.Context
import org.oppia.android.util.R
import org.oppia.android.util.system.OppiaClock
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/** Utility to manage date and time. */
@Singleton
class DateTimeUtil @Inject constructor(
  private val context: Context,
  private val oppiaClock: OppiaClock
) {

  fun getGreetingMessage(): String {
    val calender = oppiaClock.getCurrentCalendar()
    return when (calender.get(Calendar.HOUR_OF_DAY)) {
      in 4..11 -> context.getString(R.string.good_morning)
      in 12..16 -> context.getString(R.string.good_afternoon)
      in 17 downTo 3 -> context.getString(R.string.good_evening)
      else -> context.getString(R.string.good_evening)
    }
  }
}
