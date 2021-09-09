package org.oppia.android.app.utility.datetime

import android.content.Context
import org.oppia.android.R
import org.oppia.android.util.system.OppiaClock
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

/** Utility to manage date and time for user-facing strings. */
@Singleton
class DateTimeUtil @Inject constructor(
  private val context: Context,
  private val oppiaClock: OppiaClock
) {
  /**
   * Returns a user-readable string based on the time of day (to be concatenated as part of a
   * greeting for the user).
   */
  fun getGreetingMessage(): String {
    val calender = oppiaClock.getCurrentCalendar()
    return when (calender.get(Calendar.HOUR_OF_DAY)) {
      in 4..11 -> context.getString(R.string.home_screen_good_morning_greeting_fragment)
      in 12..16 -> context.getString(R.string.home_screen_good_afternoon_greeting_fragment)
      in 17 downTo 3 -> context.getString(R.string.home_screen_good_evening_greeting_fragment)
      else -> context.getString(R.string.home_screen_good_evening_greeting_fragment)
    }
  }
}
