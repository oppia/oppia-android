package org.oppia.android.app.utility.datetime

import org.oppia.android.R
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

  /**
   * Returns the readable string of the duration from the provided time in [Long].
   */
  fun timeAgoFromTimestamp(timestamp: Long, referenceTime: Long): String {
    val diff = (currentDate() - timestamp) / MILLI_SECONDS

    return when {
      diff < SECONDS -> resourceHandler.getStringInLocale(R.string.just_now)
      diff < HOUR -> resourceHandler.getQuantityStringInLocaleWithWrapping(
        R.plurals.minutes_ago, diff.diffToInt(SECONDS), diff.diffToString(SECONDS)
      )
      diff < DAY -> resourceHandler.getQuantityStringInLocaleWithWrapping(
        R.plurals.hours_ago, diff.diffToInt(HOUR), diff.diffToString(HOUR)
      )
      diff < WEEK -> resourceHandler.getQuantityStringInLocaleWithWrapping(
        R.plurals.days_ago, diff.diffToInt(DAY), diff.diffToString(DAY)
      )
      diff < WEEKS -> resourceHandler.getQuantityStringInLocaleWithWrapping(
        R.plurals.weeks_ago, diff.diffToInt(WEEK), diff.diffToString(WEEK)
      )
      diff < MONTHS -> resourceHandler.getQuantityStringInLocaleWithWrapping(
        R.plurals.months_ago,
        diff.diffToInt(WEEKS),
        diff.diffToString(WEEKS)
      )
      else -> resourceHandler.getQuantityStringInLocaleWithWrapping(
        R.plurals.years_ago, diff.diffToInt(MONTHS), diff.diffToString(MONTHS)
      )
    }
  }

  companion object {
    fun currentDate() = Calendar.getInstance().timeInMillis
    const val MILLI_SECONDS = 1000L
    const val SECONDS = 60L
    const val HOUR = 60 * SECONDS
    const val DAY = 24 * HOUR
    const val WEEK = 7 * DAY
    const val WEEKS = 2_628_000L
    const val MONTHS = 31_536_000L
  }
}

private fun Long.diffToInt(duration: Long) = div(duration).toInt()
private fun Long.diffToString(duration: Long) = div(duration).toString()
