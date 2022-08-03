package org.oppia.android.app.utility.datetime

import android.view.View
import org.oppia.android.R
import org.oppia.android.app.databinding.getResourceHandler
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.util.locale.OppiaLocale
import java.util.Calendar
import javax.inject.Inject

/** Per-activity utility to manage date and time for user-facing strings. */
class DateTimeUtil @Inject constructor(
  private val machineLocale: OppiaLocale.MachineLocale,
  private val resourceHandler: AppLanguageResourceHandler
) {
  /**
   * Returns a user-readable string based on the time of day (to be concatenated as part of a
   * greeting for the user).
   */
  fun getGreetingMessage(): String {
    return when (machineLocale.getCurrentTimeOfDay()) {
      OppiaLocale.MachineLocale.TimeOfDay.MORNING ->
        resourceHandler.getStringInLocale(R.string.home_screen_good_morning_greeting_fragment)
      OppiaLocale.MachineLocale.TimeOfDay.AFTERNOON ->
        resourceHandler.getStringInLocale(R.string.home_screen_good_afternoon_greeting_fragment)
      OppiaLocale.MachineLocale.TimeOfDay.EVENING, OppiaLocale.MachineLocale.TimeOfDay.UNKNOWN ->
        resourceHandler.getStringInLocale(R.string.home_screen_good_evening_greeting_fragment)
    }
  }

  /** Injector to retrieve instances of [DateTimeUtil] from Dagger. */
  interface Injector {
    /** Returns [DateTimeUtil] for the current Dagger graph. */
    fun getDateTimeUtil(): DateTimeUtil
  }
}

const val SECOND = 1
const val MINUTE = 60 * SECOND
const val HOUR = 60 * MINUTE
const val DAY = 24 * HOUR
const val MONTH = 30 * DAY
const val YEAR = 12 * MONTH

private fun currentDate() = Calendar.getInstance().timeInMillis

/**
* Returns the readable string of the duration from the provided time in [Long].
*/
fun Long.timeAgo(view: View): String {
  val time = this
  val now = currentDate()
  val diff = (now - time) / 1000

  val resourceHandler = getResourceHandler(view)

  return when {
    diff < MINUTE -> resourceHandler.getStringInLocale(R.string.just_now)
    diff < 2 * MINUTE -> resourceHandler.getStringInLocale(R.string.minute_ago)
    diff < 60 * MINUTE -> resourceHandler.getStringInLocaleWithWrapping(
      R.string.minutes_ago,
      diff.formatDuration(MINUTE).toString()
    )
    diff < 2 * HOUR -> resourceHandler.getStringInLocale(R.string.hour_ago)
    diff < 24 * HOUR -> resourceHandler.getStringInLocaleWithWrapping(
      R.string.hours_ago,
      diff.formatDuration(HOUR).toString()
    )
    diff < 2 * DAY -> resourceHandler.getStringInLocale(R.string.yesterday)
    diff < 30 * DAY -> resourceHandler.getStringInLocaleWithWrapping(
      R.string.days_ago,
      diff.formatDuration(DAY).toString()
    )
    diff < 2 * MONTH -> resourceHandler.getStringInLocale(R.string.month_ago)
    diff < 12 * MONTH -> resourceHandler.getStringInLocaleWithWrapping(
      R.string.months_ago,
      diff.formatDuration(MONTH).toString()
    )
    diff < 2 * YEAR -> resourceHandler.getStringInLocale(R.string.year_ago)
    else -> resourceHandler.getStringInLocaleWithWrapping(
      R.string.years_ago,
      diff.formatDuration(YEAR).toString()
    )
  }
}

private fun Long.formatDuration(duration: Int) = div(duration)
